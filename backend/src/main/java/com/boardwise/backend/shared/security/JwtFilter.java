package com.boardwise.backend.shared.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.boardwise.backend.user_service.services.MyUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Component
public class JwtFilter extends OncePerRequestFilter{

    private final JWTService service;

    private final ApplicationContext context;

    JwtFilter(JWTService service, ApplicationContext context) {
        this.service = service;
        this.context = context;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        String userId = null;
        String token = null;

        try{
            if(authHeader != null && authHeader.startsWith("Bearer ")){
                token = authHeader.split(" ")[1];
                userId = service.extractUserId(token).toString();
            }
            
            if(userId != null && SecurityContextHolder.getContext().getAuthentication() == null){
                UserDetails userDeets = context.getBean(MyUserDetailsService.class).loadUserByUserId(userId);
                
                if(service.validateToken(token, userDeets)){
                    UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(userDeets, null, userDeets.getAuthorities());
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }
        catch (SignatureException e) {
            handleJwtException(response, "JWT signature is invalid. Not signed with server key.");
            return;
        } 
        catch (ExpiredJwtException e) {
            handleJwtException(response, "JWT token has expired");
            return;
        } 
        catch (MalformedJwtException e) {
            handleJwtException(response, "JWT token is malformed");
            return;
        } 
        catch (Exception e) {
            handleJwtException(response, "Invalid JWT token");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void handleJwtException(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(errorResponse));
    }

}
