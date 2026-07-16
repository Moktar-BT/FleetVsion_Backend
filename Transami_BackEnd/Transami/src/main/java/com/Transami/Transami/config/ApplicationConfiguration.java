package com.Transami.Transami.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import com.Transami.Transami.dao.AdminDao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfiguration {

    private final AdminDao adminDao;

    // ============================================
    // UserDetailsService → charge l'Admin par email
    // ============================================
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> adminDao.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Admin non trouvé avec l'email : " + username
                ));
    }

    // ============================================
    // AuthenticationProvider
    // ============================================
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ============================================
    // AuthenticationManager
    // ============================================
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ============================================
    // PasswordEncoder
    // ============================================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dpbpgjyxz",
                "api_key", "184972978198324",
                "api_secret", "roHu1y1a7cF0tRXae7goZfPAfrw"
        ));
    }

}