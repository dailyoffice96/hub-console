package com.smconsole.admin;

public record AdminCreateRequest (
    String loginId,
    String password,
    String name,
    AdminRole role
){ }
