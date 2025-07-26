package com.dgarciacasam.authService.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.dgarciacasam.authService.Models.User;
import com.dgarciacasam.authService.Models.UserPrincipal;
import com.dgarciacasam.authService.Repositories.AuthRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService{
    @Autowired
    AuthRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if(user == null){
            throw new UsernameNotFoundException("User not found");
        }

        return new UserPrincipal(user);
    }
    
}
