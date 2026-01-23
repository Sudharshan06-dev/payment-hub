package com.userservice.services;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.userservice.models.Users;

public class UserPrincipal implements UserDetails {

    private Users user;

    public UserPrincipal(Users users) {
        this.user = users;

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convert the RoleType enum to a GrantedAuthority
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }
    
}
