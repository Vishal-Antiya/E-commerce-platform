package com.turbo.adminservice.security;

import com.turbo.adminservice.model.Admin;
import com.turbo.adminservice.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminDetailsService implements UserDetailsService {

    @Autowired
    private AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found with username: " + username));

        //  Get roles from the admin object.
        List<GrantedAuthority> authorities = admin.getRoles().stream() // Assuming getRoles() returns a List<String>
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new User(
                admin.getUsername(),
                admin.getPasswordHash(),
                authorities //  Pass the authorities (roles) here
        );
    }
}
