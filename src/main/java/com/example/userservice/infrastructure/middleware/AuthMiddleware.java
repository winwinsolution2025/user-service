package com.example.userservice.infrastructure.middleware;

import com.example.userservice.service.CustomClaim;
import com.example.userservice.service.JwtService;
import io.javalin.http.Handler;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpResponseException;

public class AuthMiddleware {
    public static Handler authenticate = ctx -> {
        String authHeader = ctx.header("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new HttpResponseException(401, "Missing or invalid token");
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        if (!JwtService.validateToken(token)) {
            throw new HttpResponseException(401, "Invalid or expired token");
        }

        CustomClaim claim = JwtService.getClaimFromToken(token);
        //add email to context
        ctx.attribute("authenticatedEmail", claim.getEmail());

        // bypass authentication for create a new user
        if (ctx.path().equals("/api/v1/users") && ctx.method() == HandlerType.POST) {
            return;
        }

        if (ctx.path().equals("/api/v1/users/me")) {
            return;
        }

        if (!claim.getRole().equals("ADMIN")) {
            throw new HttpResponseException(403, "Insufficient privileges");
        }


    };
} 