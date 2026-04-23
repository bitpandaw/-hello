package com.mall.security.jwt;

import com.mall.security.user.MallPrincipal;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final AntPathMatcher M = new AntPathMatcher();
    public static final List<String> PUB = List.of(
        "/v3/api-docs", "/v3/api-docs/**", "/doc.html", "/webjars/**", "/swagger-ui/**", "/favicon.ico", "/error",
        "/api/ums/captcha", "/api/ums/captcha/**", "/api/ums/register", "/api/ums/login", "/api/ums/refresh",
        "/admin/auth/login", "/admin/auth/refresh"
    );
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (isPublic(path) || pmsGetPublic(request, path) || (path != null && path.startsWith("/uploads/"))) {
            filterChain.doFilter(request, response);
            return;
        }
        String h = request.getHeader("Authorization");
        if (h == null || !h.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            Claims c = jwtService.parseAndValidateAccess(h);
            if (!JwtService.TYPE_ACCESS.equals(c.get(JwtService.CLAIM_TYPE))) {
                filterChain.doFilter(request, response);
                return;
            }
            String kind = c.get(JwtService.CLAIM_KIND, String.class);
            if (path.startsWith("/admin/") && !JwtService.KIND_ADMIN.equals(kind)) {
                filterChain.doFilter(request, response);
                return;
            }
            if (path.startsWith("/api/") && !path.startsWith("/api/pms/") && !JwtService.KIND_MEMBER.equals(kind)) {
                filterChain.doFilter(request, response);
                return;
            }
            long sub = Long.parseLong(c.getSubject());
            MallPrincipal p = new MallPrincipal();
            p.setId(sub);
            p.setKind(kind);
            p.setUsername("user-" + sub);
            var auth = new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception e) {
            // leave unauthenticated -> 401 or next filter
        }
        filterChain.doFilter(request, response);
    }

    private static boolean pmsGetPublic(HttpServletRequest request, String path) {
        return "GET".equalsIgnoreCase(request.getMethod()) && path.startsWith("/api/pms/");
    }

    private static boolean isPublic(String path) {
        if (path == null) {
            return false;
        }
        if (path.equals("/v3/api-docs") || path.startsWith("/v3/") || path.equals("/doc.html") || path.startsWith("/webjars/") || path.startsWith("/swagger-ui")) {
            return true;
        }
        if (path.equals("/favicon.ico") || path.equals("/error")) {
            return true;
        }
        for (String s : PUB) {
            if (M.match(s, path)) {
                return true;
            }
        }
        return false;
    }
}
