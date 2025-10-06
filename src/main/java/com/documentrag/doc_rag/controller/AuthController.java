package com.documentrag.doc_rag.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Map<String, String[]> USERS = Map.of(
        "admin", new String[]{"adminpass", "ADMIN"},
        "user", new String[]{"userpass", "USER"}
    );
    private static final String SECRET = "dev-jwt-secret-please-change-123456!!";

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> login(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null || !USERS.containsKey(username) || !USERS.get(username)[0].equals(password)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return Map.of("error", "Invalid username or password");
        }

        String role = USERS.get(username)[1];
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 1000 * 60 * 60); // 1 hour expiry

        Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .setSubject(username)
                .claim("roles", Collections.singletonList("ROLE_" + role))
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return Map.of("token", token, "role", role);
    }
}
