package com.project.app.security;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.project.app.exceptions.ApiException;

// sliding window limit per client ip for login and password reset requests
@Component
public class LoginRateLimiter {

    private static final long WINDOW_MS = 60_000;

    private final Map<String, Deque<Long>> attempts = new ConcurrentHashMap<>();

    @Value("${app.security.login-attempts-per-minute:20}")
    private int maxPerMinute;

    public void check(String key) {
        long now = System.currentTimeMillis();
        Deque<Long> window = this.attempts.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (window) {
            while (!window.isEmpty() && now - window.peekFirst() > WINDOW_MS) {
                window.pollFirst();
            }
            if (window.size() >= this.maxPerMinute) {
                throw new ApiException("too many attempts, please wait a minute and try again", HttpStatus.TOO_MANY_REQUESTS);
            }
            window.addLast(now);
        }
    }
}
