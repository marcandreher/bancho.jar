package com.banchojar.db.models;

public record UserRecord(
        int id,
        String username,
        String email,
        String password_hash,
        String country) {
}
