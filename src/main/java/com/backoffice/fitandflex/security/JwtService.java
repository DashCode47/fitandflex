package com.backoffice.fitandflex.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private Long jwtExpirationMs;

    private Key getSigningKey(){
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails){
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token){
        try {
            return parseClaims(token).getSubject();
        } catch (ExpiredJwtException e) {
            // Extraer username incluso si el token está expirado
            return e.getClaims().getSubject();
        }
    }

    public boolean isTokenExpired(String token){
        try {
            return parseClaims(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateToken(String token, UserDetails userDetails){
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extrae claims de un token incluso si está expirado (útil para refresh token)
     */
    public Claims extractAllClaims(String token) {
        try {
            return parseClaims(token);
        } catch (ExpiredJwtException e) {
            // Retornar claims incluso si el token está expirado
            return e.getClaims();
        }
    }

    /**
     * Valida si un token expirado puede ser refrescado (dentro de una ventana de gracia)
     * Por defecto, permite refrescar tokens expirados hasta 7 días después
     */
    public boolean canRefreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Date expiration = claims.getExpiration();
            Date now = new Date();
            
            // Permitir refrescar si el token expiró hace menos de 7 días
            long daysSinceExpiration = (now.getTime() - expiration.getTime()) / (1000 * 60 * 60 * 24);
            return daysSinceExpiration <= 7;
        } catch (Exception e) {
            return false;
        }
    }

    public Long getExpirationTime(){
        return jwtExpirationMs;
    }

    private Claims parseClaims(String token){
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // Re-lanzar para que pueda ser manejado específicamente
            throw e;
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }
}
