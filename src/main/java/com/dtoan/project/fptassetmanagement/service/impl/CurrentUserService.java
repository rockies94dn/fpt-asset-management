package com.dtoan.project.fptassetmanagement.service.impl;

import com.dtoan.project.fptassetmanagement.entity.User;
import com.dtoan.project.fptassetmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User requireUser(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("Bạn chưa đăng nhập.");
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy người dùng hiện tại."));
    }

    public boolean hasRole(User user, String roleName) {
        return user != null
                && user.getRole() != null
                && roleName.equals(user.getRole().getName());
    }
}
