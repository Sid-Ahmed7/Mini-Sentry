package com.sentry.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.context.MessageSource;

import com.sentry.backend.common.CurrentLocale;
import com.sentry.backend.constants.CommonConstants;
import com.sentry.backend.dto.request.LoginRequest;
import com.sentry.backend.security.LoginAttempts;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final MessageSource messageSource;
    private final HttpSessionSecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();
    private final LoginAttempts loginAttempts = new LoginAttempts(CommonConstants.MAX_FAILURES, CommonConstants.WINDOW_MILLS);

    public AuthController(AuthenticationManager authenticationManager, MessageSource messageSource) {
        this.authenticationManager = authenticationManager;
        this.messageSource = messageSource;
    }
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody  LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {

        String userIdentification = request.getRemoteAddr() + "|" + loginRequest.username().toLowerCase();

        if(loginAttempts.isLocked(userIdentification)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("error", messageSource.getMessage("auth.tooManyRequests", null, CurrentLocale.getCurrentLocale())));
        }

        try {
            Authentication authentication  = authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.username(), loginRequest.password()));
            loginAttempts.reset(userIdentification);

            HttpSession session = request.getSession(false);

            if(session != null) {
                session.invalidate();
            }

            request.getSession(true);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            contextRepository.saveContext(context, request, response);

           return ResponseEntity.ok(Map.of("authenticated", true, "username", authentication.getName()));
        } catch (AuthenticationException e) {
            loginAttempts.recordFailure(userIdentification);
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", messageSource.getMessage("auth.invalidCredentials", null, CurrentLocale.getCurrentLocale())));
        }
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        boolean logged = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
        return logged ? Map.of("authenticated", true, "username", auth.getName()) : Map.of("authenticated", false);
    }
}