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

public class ServerWebApp {

    private static final String HANDLER_PACKAGE = "com.osuserverlist.bjar.handlers";

    public static void registerRoutes(JavalinConfig app) {
        registerAnnotatedHandlers(app);
    }

    private static void registerAnnotatedHandlers(JavalinConfig app) {
        Map<RouteKey, List<HostHandler>> routesByPath = new HashMap<>();

        try (var scan = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .acceptPackages(HANDLER_PACKAGE)
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
            String path = key.path;
            HostHandler[] handlers = entry.getValue().toArray(new HostHandler[0]);

            registerRoute(app, key.method, path, handlers);
        }
    }

    private static void registerRoute(JavalinConfig app, String method, String path, HostHandler[] handlers) {
        switch (method) {
            case "POST":
                app.routes.post(path, ctx -> dispatchByHost(ctx, handlers));
                break;
            case "DELETE":
                app.routes.delete(path, ctx -> dispatchByHost(ctx, handlers));
                break;
            default:
                app.routes.get(path, ctx -> dispatchByHost(ctx, handlers));
                break;
        }
    }

    private static void dispatchByHost(Context ctx, HostHandler[] handlers) throws Exception {
        String host = extractHost(ctx);

        int dotIndex = host.indexOf('.');
        String subdomain = dotIndex > 0 ? host.substring(0, dotIndex) : host;

        for (HostHandler hostHandler : handlers) {
            if (matchesHost(host, subdomain, hostHandler.hosts)) {
                hostHandler.handler.handle(ctx);
                return;
            }
        }

        ctx.status(404);
    }

    private static String normalizeMethod(String method) {
        if (method == null || method.isBlank()) {
            return "GET";
        }

        return method.trim().toUpperCase(Locale.ROOT);
    }

    private static String[] normalizeHosts(String[] hosts) {
        String[] normalized = new String[hosts.length];
        for (int i = 0; i < hosts.length; i++) {
            normalized[i] = hosts[i].toLowerCase(Locale.ROOT);
        }
        return normalized;
    }

    private static Handler instantiateHandler(Class<?> handlerClass) {
        try {
            return (Handler) handlerClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException ex) {
            throw new IllegalStateException("Failed to instantiate handler: " + handlerClass.getName(), ex);
        }
    }

    private static String extractHost(Context ctx) {
        String hostHeader = ctx.header("Host");
        if (hostHeader == null || hostHeader.isEmpty()) {
            return "";
        }

        int colonIndex = hostHeader.indexOf(':');
        String host = colonIndex >= 0 ? hostHeader.substring(0, colonIndex) : hostHeader;
        return host.toLowerCase(Locale.ROOT);
    }

    private static boolean matchesHost(String host, String subdomain, String[] allowedHosts) {
        for (String allowed : allowedHosts) {
            if (allowed.equals(host)) {
                return true;
            }

            if (allowed.endsWith(".")) {
                if (host.regionMatches(0, allowed, 0, allowed.length())) {
                    return true;
                }
                continue;
            }

            if (allowed.equals(subdomain)) {
                return true;
            }
        }

        return false;
    }

    private static final class HostHandler {
        private final String[] hosts;
        private final Handler handler;

        private HostHandler(String[] hosts, Handler handler) {
            this.hosts = hosts;
            this.handler = handler;
        }
    }

    private static final class RouteKey {
        private final String method;
        private final String path;

        private RouteKey(String method, String path) {
            this.method = method;
            this.path = path;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof RouteKey)) {
                return false;
            }

            RouteKey that = (RouteKey) other;
            return method.equals(that.method) && path.equals(that.path);
        }

        @Override
        public int hashCode() {
            int result = method.hashCode();
            result = 31 * result + path.hashCode();
            return result;
        }
    }

}