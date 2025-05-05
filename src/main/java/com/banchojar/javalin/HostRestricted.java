package com.banchojar.javalin;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HostRestricted {
    String path();
    String[] hosts(); // Allowed hosts
    
}
