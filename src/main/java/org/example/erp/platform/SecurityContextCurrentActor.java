package org.example.erp.platform;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
class SecurityContextCurrentActor implements CurrentActor {
    /** Work started outside a request, such as start-up bootstrapping, is attributed to the system. */
    private static final String SYSTEM = "system";

    @Override
    public String username() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? SYSTEM : auth.getName();
    }
}
