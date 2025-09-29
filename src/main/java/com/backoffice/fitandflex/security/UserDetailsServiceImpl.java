package com.backoffice.fitandflex.security;

import com.backoffice.fitandflex.entity.User;
import com.backoffice.fitandflex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailWithRole(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Convert role -> GrantedAuthority. We prefix with ROLE_ to use hasRole(...)
        String roleName = user.getRole() != null ? user.getRole().getName() : "USER";
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleName));

        boolean enabled = Boolean.TRUE.equals(user.getActive());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                enabled,
                true,
                true,
                true,
                authorities
        );
    }
}
