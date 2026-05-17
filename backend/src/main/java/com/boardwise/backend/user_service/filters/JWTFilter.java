package com.boardwise.backend.user_service.filters;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.boardwise.backend.user_service.services.JWTService;
import com.boardwise.backend.user_service.services.MyUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JWTFilter extends OncePerRequestFilter{

    @Autowired
    private JWTService service;

    @Autowired
    private ApplicationContext context;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        String username = null;
        String token = null;

        if(authHeader != null && authHeader.startsWith("Bearer ")){
            token = authHeader.split(" ")[1];
            username = service.extractUsername(token);
        }
        
        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDeets = context.getBean(MyUserDetailsService.class).loadUserByUsername(username);
            
            if(service.validateToken(token, userDeets)){
                UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(userDeets, null ,userDeets.getAuthorities());
                
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

}
