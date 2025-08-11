package com.company.chat.security;

public record TokenUser(String externalId, String username, String displayName, String email) {}