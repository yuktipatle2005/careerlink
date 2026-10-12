package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.NotificationService;
import com.example.demo.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Controller Advice that injects global view attributes across all Thymeleaf templates.
 * Provides real-time unread notification counts for authenticated users without repetitive controller code.
 */
@ControllerAdvice
public class GlobalModelAttributesAdvice {

    private final UserService userService;
    private final NotificationService notificationService;

    public GlobalModelAttributesAdvice(UserService userService,
                                       NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    /**
     * Injects the unread notification count for the currently logged-in user into the Model.
     * Accessible in any Thymeleaf template as ${unreadNotificationCount}.
     */
    @ModelAttribute("unreadNotificationCount")
    public long addUnreadNotificationCount(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            try {
                User user = userService.findByEmail(authentication.getName());
                if (user != null) {
                    return notificationService.getUnreadCount(user.getId());
                }
            } catch (Exception ignored) {
                // In case of non-user authentication tokens or transient states
            }
        }
        return 0L;
    }
}
