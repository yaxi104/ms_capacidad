package com.reactivo.capacidad.domain.api;

public interface JwtServicePort {
    boolean validateToken(String token);
    String getEmailFromToken(String token);
    String getRoleFromToken(String token);
}