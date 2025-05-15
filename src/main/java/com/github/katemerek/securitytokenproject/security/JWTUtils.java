package com.github.katemerek.securitytokenproject.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

@Component
public class JWTUtils {

    // Ключ шифрования для JWT  
    private final SecretKey secretKey;

    // Время действия токена в миллисекундах (24 часа)  
    private static final long EXPIRATION_TIME = 86400000L;

    public JWTUtils(){
        String secretString = "EqjWQ7MYqS4VkF2vZLNOOH9r4XVa3XrIsibni4cJ6eEsAphTKHvnMoRmZdfC0PLD";
        byte[] keyBytes = Base64.getDecoder().decode(secretString.getBytes(StandardCharsets.UTF_8));
        this.secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    /*Метод для генерации JWT токена на основе данных пользователя*/
    public String generateToken(MyUserDetails myUserDetails){
        return Jwts.builder()
                .subject(myUserDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey)
                .compact();
    }

    // Метод для генерации токена обновления (refresh token) с дополнительными данными  
    public String generateRefreshToken(HashMap<String, Object> claims, MyUserDetails myUserDetails){
        return Jwts.builder()
                .claims(claims)
                .subject(myUserDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME * 30))
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    // Метод для извлечения имени пользователя из токена  
    private <T> T extractClaims(String token, Function<Claims, T> claimsTFunction) {
        return claimsTFunction.apply
                (Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                .parseSignedClaims(token)
                        .getPayload());
    }

    public boolean isTokenValid(String token, MyUserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

        private boolean isTokenExpired(String token) {
            return extractClaims(token, Claims::getExpiration).before(new Date());
        }
}
