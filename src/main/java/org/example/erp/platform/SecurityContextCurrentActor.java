package org.example.erp.platform;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
class SecurityContextCurrentActor implements CurrentActor {
    @Override
    public String username() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
