package com.gomdolbook.api.service.Auth;

import com.gomdolbook.api.persistence.entity.SecurityUser;
import com.gomdolbook.api.persistence.entity.User;
import com.gomdolbook.api.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserDetailServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("Cannot find user : " + username));
        return new SecurityUser(user);
    }
}
