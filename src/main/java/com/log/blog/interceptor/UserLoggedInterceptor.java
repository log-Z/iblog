package com.log.blog.interceptor;

import com.log.blog.controller.UserPublicController;
import com.log.blog.entity.User;
import com.log.blog.service.UserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
@Scope("prototype")
public class UserLoggedInterceptor implements HandlerInterceptor {
    private final UserService userService;

    public UserLoggedInterceptor(@Qualifier("userBasicService") UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        Object userId = session.getAttribute(UserPublicController.SESSION_KEY_USER_IDENTITY);
        if (userId instanceof String) {
            User currentUser = userService.getUser((String) userId);
            if (currentUser != null) {
                request.setAttribute("redirectPath", "/" + userId);
                request.getRequestDispatcher("/redirect").forward(request, response);
                return false;
            } else {
                session.removeAttribute(UserPublicController.SESSION_KEY_USER_IDENTITY);
            }
        }
        return true;
    }
}
