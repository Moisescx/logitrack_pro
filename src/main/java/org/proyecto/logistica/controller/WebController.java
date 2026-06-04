package org.proyecto.logistica.controller;

import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class WebController {

    @GetMapping("/login")
    public String verLogin() {
        return "login";
    }

    @GetMapping("/")
    public String redireccionarSegunRol(Authentication authentication) {
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return "redirect:/admin";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DESPACHADOR"))) {
            return "redirect:/despachador";
        } else {
            return "redirect:/chofer";
        }
    }
}
