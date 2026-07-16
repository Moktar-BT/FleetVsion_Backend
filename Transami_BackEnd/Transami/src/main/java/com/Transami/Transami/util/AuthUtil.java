package com.Transami.Transami.util;

import lombok.RequiredArgsConstructor;
import com.Transami.Transami.dao.AdminDao;
import com.Transami.Transami.entity.Admin;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUtil {

    private final AdminDao adminDao;

    public Admin getCurrentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return adminDao.findWithTelephonesByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin non trouvé : " + email));
    }

    public Long getCurrentAdminId() {
        return getCurrentAdmin().getId();
    }
}