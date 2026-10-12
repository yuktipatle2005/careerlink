package com.example.demo.controller;

import com.example.demo.dto.UserRegisterDto;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Controller handling user authentication pages (Login, Register).
 */
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("userDto", new UserRegisterDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute UserRegisterDto userDto, Model model) {
        try {
            userService.registerUser(userDto);
            return "redirect:/login?registered=true";
        } catch (DuplicateResourceException ex) {
            model.addAttribute("error", ex.getMessage());
            return "register";
        } catch (Exception ex) {
            model.addAttribute("error", "An unexpected error occurred during registration. Please try again.");
            return "register";
        }
    }
}
