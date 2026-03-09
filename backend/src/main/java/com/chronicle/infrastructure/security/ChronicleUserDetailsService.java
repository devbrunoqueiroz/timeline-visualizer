package com.chronicle.infrastructure.security;

import com.chronicle.infrastructure.persistence.jpa.UserJpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class ChronicleUserDetailsService implements UserDetailsService {

    private final UserJpaRepository userJpaRepository;

    public ChronicleUserDetailsService(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userJpaRepository.findByEmail(email)
                .map(e -> new ChronicleUserDetails(e.getId(), e.getEmail(), e.getPasswordHash()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}
