package com.userservice.config;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.userservice.models.Users;
import com.userservice.repositories.UsersRepository;
import com.userservice.services.OauthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class Oauth2HandlerConfig implements AuthenticationSuccessHandler {
    
    @Autowired
    private OauthService oauthService;
    
    @Autowired
    private UsersRepository usersRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                       HttpServletResponse response,
                                       Authentication authentication) throws IOException {
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        // Extract user info from Google
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub");
        
        // Find or create user in your database
        Users user = usersRepository.findByEmail(email)
                .orElseGet(() -> {
                    Users newUser = new Users();
                    newUser.setEmail(email);
                    newUser.setUsername(email);
                    newUser.setFirstName(name);
                    newUser.setLastName(name);
                    newUser.setGoogleId(googleId);
                    return usersRepository.save(newUser);
                });
        
        // Generate JWT token
        String jwtToken = oauthService.generateToken(user.getUsername(), user.getEmail(), user.getUserId());
        
        // Redirect to frontend with token
        response.sendRedirect("http://localhost:4200/login?token=" + jwtToken);
    }
}
