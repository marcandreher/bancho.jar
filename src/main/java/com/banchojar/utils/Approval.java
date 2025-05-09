package com.banchojar.utils;

import java.util.Arrays;


public enum Approval {
    UNSPECIFIED(-3),
    Graveyard(-2),
    WIP(-1),
    Pending(0),
    Ranked(1),
    Approved(2),
    Qualified(3),
    Loved(4);
 
    private final int id;
 
    private Approval(int id) {
       this.id = id;
    }
 
    public int getId() {
       return this.id;
    }
 
    public static Approval getById(int id) {
       return (Approval)Arrays.stream(values()).filter((o) -> {
          return o.id == id;
       }).findFirst().orElse(UNSPECIFIED);
    }

    
 }
 