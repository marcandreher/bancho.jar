package com.banchojar.javalin;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;

import java.lang.reflect.Method;
import java.util.Arrays;

public class RouteRegistrar {

    public static void registerAnnotatedRoutes(Javalin app, Object controller) {
        for (Method method : controller.getClass().getDeclaredMethods()) {
            System.out.println("Handling request for path: " + method.getName());
            if (method.isAnnotationPresent(HostRestricted.class)) {
                System.out.println("Found annotations for: " + method.getName());
                HostRestricted restriction = method.getAnnotation(HostRestricted.class);
                
                // Ensure method signature is correct
                if (method.getParameterCount() != 1 || method.getParameterTypes()[0] != Context.class) {
                    throw new IllegalArgumentException("Invalid handler method: " + method.getName());
                }

                // Derive route path from method name (e.g., /admin)
                String path = restriction.path();
                System.out.println("Found annotations for: " + path);

                app.before(path, new Handler() {
                    @Override
                    public void handle(Context ctx) throws Exception {
                        String host = "";
                        
                        // Iterate trough the headers to find the host
                        System.out.println("URL" + ctx.fullUrl());

                        for (String singleHost : restriction.hosts()) {
                            System.out.println("Found host: " + singleHost);
                            if(!ctx.fullUrl().contains(singleHost)) {
                                System.out.println("Deny host: " + singleHost);
                                ctx.status(403).result("Forbidden: Host not allowed.");
                              return;
                            }
                        }

                     
                        // Call the annotated method
                        method.invoke(controller, ctx);
                    }
                });
                
            }
        }
    }
}
