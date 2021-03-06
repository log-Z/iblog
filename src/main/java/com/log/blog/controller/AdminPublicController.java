package com.log.blog.controller;

import com.log.blog.dto.AdminParam;
import com.log.blog.entity.Admin;
import com.log.blog.service.AdminService;
import com.log.blog.utils.HtmlEscapeUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminPublicController {
    public final static String SESSION_KEY_ADMIN_IDENTITY = "adminIdentity";
    private final AdminService adminService;

    public AdminPublicController(@Qualifier("adminBasicService") AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("adminRegisterForm", new AdminParam());
        return "admin-register.jsp";
    }

//    @PostMapping(value = "/register")
//    public String register(
//            @Validated(Admin.Register.class) AdminParam adminParam,
//            BindingResult errors,
//            Model model
//    ) {
//        if (!errors.hasErrors()) {
//            passwordAgainValidator.validate(adminParam, errors);
//        }
//        if (!errors.hasErrors() && adminService.register(adminParam)) {
//            return "redirect:/admin/login";
//        } else {
//            model.addAttribute("adminRegisterForm", HtmlEscapeUtils.escape(adminParam));
//            return "admin-register.jsp";
//        }
//    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("admin", new Admin());
        return "admin-login.jsp";
    }

//    @PostMapping("/login")
//    public String login(
//            @Validated(Admin.Login.class) Admin admin,
//            BindingResult errors,
//            HttpSession session,
//            Model model
//    ) {
//        if (!errors.hasErrors()) {
//            String adminId = adminService.loginCheck(admin);
//            if (adminId != null) {
//                session.setAttribute(SESSION_KEY_ADMIN_IDENTITY, adminId);
//                return "redirect:/admin";
//            }
//        }
//        model.addAttribute("admin", HtmlEscapeUtils.escape(admin));
//        return "admin-login.jsp";
//    }

    @RequestMapping("/error/401")
    public String error403(Model model) {
        model.addAttribute("refreshPath", "/admin/login");
        return "error-401.jsp";
    }
}
