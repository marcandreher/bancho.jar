package com.osuserverlist.bjar.modules.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.osuserverlist.bjar.models.osu.Privileges;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BanchoCommand {
    String name();
    String description() default "";
    Privileges requiredPrivileges() default Privileges.VERIFIED;
}
