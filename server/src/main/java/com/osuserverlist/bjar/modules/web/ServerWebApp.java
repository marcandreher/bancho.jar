package com.osuserverlist.bjar.modules.web;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.osuserverlist.bjar.modules.web.engine.Host;
import com.osuserverlist.bjar.modules.web.engine.HttpMethod;
import com.osuserverlist.bjar.modules.web.engine.Path;

import io.github.classgraph.ClassGraph;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import io.javalin.http.Handler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ServerWebApp {

    private final String HANDLER_PACKAGE = "com.osuserverlist.bjar.handlers";

    public void registerRoutes(JavalinConfig app) {
        registerAnnotatedHandlers(app, HANDLER_PACKAGE);
    }

    private void registerAnnotatedHandlers(JavalinConfig app, String packageName) {
        Map<RouteKey, List<HostHandler>> routesByPath = new HashMap<>();

        try (var scan = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .acceptPackages(packageName)
                .scan()) {
            for (var classInfo : scan.getClassesImplementing(Handler.class.getName())) {
                Class<?> handlerClass = classInfo.loadClass();
                Host hostAnnotation = handlerClass.getAnnotation(Host.class);
                HttpMethod methodAnnotation = handlerClass.getAnnotation(HttpMethod.class);
                Path pathAnnotation = handlerClass.getAnnotation(Path.class);

                if (hostAnnotation == null || pathAnnotation == null) {
                    continue;
                }

                Handler handler = instantiateHandler(handlerClass);
                String path = pathAnnotation.value();
                String[] hosts = normalizeHosts(hostAnnotation.value());
                String[] methods = methodAnnotation == null
                        ? new String[] { "GET" }
                        : methodAnnotation.value();

                for (String method : methods) {
                    String normalized = normalizeMethod(method);
                    routesByPath
                            .computeIfAbsent(new RouteKey(normalized, path), key -> new ArrayList<>())
                            .add(new HostHandler(hosts, handler));
                }
            }
        }

        for (var entry : routesByPath.entrySet()) {
            RouteKey key = entry.getKey();
            String path = key.getPath();
            HostHandler[] handlers = entry.getValue().toArray(new HostHandler[0]);

            registerRoute(app, key.getMethod(), path, handlers);
        }
    }

    private void registerRoute(JavalinConfig app, String method, String path, HostHandler[] handlers) {
        HostRouter router = HostRouter.build(handlers);

        switch (method) {
            case "POST":
                app.routes.post(path, router::dispatch);
                break;
            case "DELETE":
                app.routes.delete(path, router::dispatch);
                break;
            default:
                app.routes.get(path, router::dispatch);
                break;
        }
    }

    private String normalizeMethod(String method) {
        if (method == null || method.isBlank()) {
            return "GET";
        }

        return method.trim().toUpperCase(Locale.ROOT);
    }

    private String[] normalizeHosts(String[] hosts) {
        String[] normalized = new String[hosts.length];
        for (int i = 0; i < hosts.length; i++) {
            normalized[i] = hosts[i].toLowerCase(Locale.ROOT);
        }
        return normalized;
    }

    private Handler instantiateHandler(Class<?> handlerClass) {
        try {
            return (Handler) handlerClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException ex) {
            throw new IllegalStateException("Failed to instantiate handler: " + handlerClass.getName(), ex);
        }
    }

    private String extractHost(Context ctx) {
        String hostHeader = ctx.header("Host");
        if (hostHeader == null || hostHeader.isEmpty()) {
            return "";
        }

        int colonIndex = hostHeader.indexOf(':');
        String host = colonIndex >= 0 ? hostHeader.substring(0, colonIndex) : hostHeader;
        return host.toLowerCase(Locale.ROOT);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class HostHandler {
        private final String[] hosts;
        private final Handler handler;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class HostRouter {
        private final Map<String, Handler> exactHosts;
        private final Map<String, Handler> subdomainHosts;
        private final List<Map.Entry<String, Handler>> prefixWildcards;

        static HostRouter build(HostHandler[] handlers) {
            Map<String, Handler> exact = new HashMap<>();
            Map<String, Handler> subdomain = new HashMap<>();
            List<Map.Entry<String, Handler>> prefixes = new ArrayList<>();

            for (HostHandler hh : handlers) {
                for (String host : hh.hosts) {
                    if (host.endsWith(".")) {
                        prefixes.add(Map.entry(host, hh.handler));
                    } else {
                        exact.putIfAbsent(host, hh.handler);
                        subdomain.putIfAbsent(host, hh.handler);
                    }
                }
            }

            // Longest prefix first, so a more specific wildcard is preferred
            // over a shorter, more general one.
            prefixes.sort((a, b) -> Integer.compare(b.getKey().length(), a.getKey().length()));

            return new HostRouter(exact, subdomain, prefixes);
        }

        void dispatch(Context ctx) throws Exception {
            String host = extractHost(ctx);

            Handler handler = exactHosts.get(host);

            if (handler == null) {
                int dotIndex = host.indexOf('.');
                String subdomain = dotIndex > 0 ? host.substring(0, dotIndex) : host;
                handler = subdomainHosts.get(subdomain);
            }

            if (handler == null) {
                for (Map.Entry<String, Handler> entry : prefixWildcards) {
                    String prefix = entry.getKey();
                    if (host.regionMatches(0, prefix, 0, prefix.length())) {
                        handler = entry.getValue();
                        break;
                    }
                }
            }

            if (handler != null) {
                handler.handle(ctx);
            } else {
                ctx.status(404);
            }
        }
    }

    @Value
    private static class RouteKey {
        String method;
        String path;
    }

}