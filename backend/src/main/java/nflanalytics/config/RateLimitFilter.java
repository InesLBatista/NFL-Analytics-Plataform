package nflanalytics.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;



@Component
public class RateLimitFilter implements Filter {
    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final long WINDOW_MILLIS = 60_000;

    private final ConcurrentHashMap<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (!request.getRequestURI().equals("/api/assistant/ask")) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        String clientIp = request.getRemoteAddr();
        long now = System.currentTimeMillis();

        Deque<Long> timestamps = requestLog.computeIfAbsent(clientIp, k -> new ConcurrentLinkedDeque<>());


        while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MILLIS) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= MAX_REQUESTS_PER_MINUTE) {
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Too many requests. Try again in a minute.\"}");
            return;
        }

        timestamps.addLast(now);
        chain.doFilter(servletRequest, servletResponse);
    }
}