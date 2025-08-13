package com.company.chat.service;

import com.company.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        var auths = AuthorityUtils.commaSeparatedStringToAuthorityList(user.getRoles());
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),  // именно захешированный пароль!
                auths
        );
    }
}
