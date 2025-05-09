package com.banchojar.handlers;

import io.javalin.security.RouteRole;

public enum BanchoJarRoles implements RouteRole {
    FORM,
    QUERY,
    AGENT,
    OSUPARAM_US,
    OSUPARAM_HA,
    OSUPARAM_U,
    OSUPARAM_H,
    OSUPARAM_P,
}
