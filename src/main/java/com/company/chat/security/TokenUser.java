package com.company.chat.security;

/** Слепок из JWT, по которому синхронизируем локального User. */
public record TokenUser(
        String username,
        String displayName,
        String email,
        String phone
) {}
