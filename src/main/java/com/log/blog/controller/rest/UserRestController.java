package com.log.blog.controller.rest;

import com.fasterxml.jackson.annotation.JsonView;
import com.log.blog.dto.Range;
import com.log.blog.dto.NameForm;
import com.log.blog.dto.PasswordForm;
import com.log.blog.entity.User;
import com.log.blog.service.UserAdvancedService;
import com.log.blog.utils.AuthenticationUtils;
import com.log.blog.validator.PasswordAgainValidator;
import com.log.blog.vo.rest.RestRange;
import com.log.blog.vo.rest.RestResult;
import com.log.blog.vo.rest.RestUser;
import com.log.blog.vo.rest.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.log.blog.controller.UserPublicController.SESSION_KEY_USER_IDENTITY;
import static com.log.blog.interceptor.UserRequiredInterceptor.REQUEST_KEY_CURRENT_USER;

@RestController
@RequestMapping("/api/user")
public class UserRestController {
    private static final String DATA_PROPERTY_USER_LIST = "users";
    private static final String DATA_PROPERTY_RANGE = "range";

    private UserAdvancedService userAdvancedService;
    private ConversionService restConversionService;
    private UserPublicRestController userPublicRestController;
    private Validator passwordAgainValidator;

    @Autowired
    public void init(
            UserAdvancedService userAdvancedService,
            @Qualifier("restConverterService") ConversionService restConversionService,
            UserPublicRestController userPublicRestController,
            PasswordAgainValidator passwordAgainValidator
    ) {
        this.userAdvancedService = userAdvancedService;
        this.restConversionService = restConversionService;
        this.userPublicRestController = userPublicRestController;
        this.passwordAgainValidator = passwordAgainValidator;
    }

    @DeleteMapping("/session")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpSession session) {
        session.removeAttribute(SESSION_KEY_USER_IDENTITY);
    }

    @GetMapping("/{userId:\\d{1,11}}")
    @JsonView(View.Owner.class)
    public RestResult info(
            @PathVariable String userId,
            @RequestAttribute(value = REQUEST_KEY_CURRENT_USER, required = false) User user,
            @ModelAttribute RestResult result,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws NoHandlerFoundException, IOException {
        if (user == null || !userId.equals(user.getUserId()))
            response.sendRedirect("/api/user/" + userId + "$base");
        return userPublicRestController.info(userId, result, request);
    }

    @DeleteMapping("/{userId:\\d{1,11}}")
    @JsonView(View.Base.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public RestResult delete(
            @PathVariable String userId,
            @ModelAttribute RestResult result,
            HttpServletRequest request
    ) throws NoHandlerFoundException {
        if (userAdvancedService.getUser(userId) == null) {
            throw new NoHandlerFoundException("DELETE", request.getRequestURI(), new HttpHeaders());
        } else {
            userAdvancedService.deleteUser(userId);
            return null;
        }
    }

    @PutMapping("/{userId:\\d{1,11}}/name")
    @JsonView(View.Base.class)
    public RestResult updateName(
            @PathVariable String userId,
            @RequestAttribute(REQUEST_KEY_CURRENT_USER) User user,
            @ModelAttribute RestResult result,
            @Validated NameForm form,
            BindingResult errors,
            HttpServletResponse response
    ) {
        if (errors.hasErrors()) {
            response.setStatus(400);
            errors.reject("update.name.failed");
            return result.setErrors(errors);
        }

        AuthenticationUtils.checkOwnerAuthentication(user.getUserId(), userId);
        boolean successful = userAdvancedService.updateName(userId, form.getName());
        if (successful) {
            response.setStatus(204);
            return null;
        } else {
            throw new RuntimeException();
        }
    }

    @PutMapping("/{userId:\\d{1,11}}/password")
    @JsonView(View.Base.class)
    public RestResult updatePassword(
            @PathVariable String userId,
            @RequestAttribute(value = REQUEST_KEY_CURRENT_USER) User user,
            @ModelAttribute RestResult result,
            @Validated PasswordForm form,
            BindingResult errors,
            HttpServletResponse response
    ) {
        if (!errors.hasErrors()) {
            passwordAgainValidator.validate(form, errors);
            if (!errors.hasErrors()) {
                AuthenticationUtils.checkOwnerAuthentication(user.getUserId(), userId);
                boolean successful = userAdvancedService.updatePassword(userId, form.getOldPassword(), form.getNewPassword());
                if (successful) {
                    response.setStatus(204);
                    return null;
                } else {
                    throw new RuntimeException();
                }
            }
        }

        response.setStatus(400);
        errors.reject("update.password.failed");
        return result.setErrors(errors);
    }

    @GetMapping("/list")
    @JsonView(View.Guest.class)
    public RestResult list(
            @ModelAttribute RestResult result,
            @Validated Range range,
            BindingResult errors,
            HttpServletResponse response
    ) {
        if (errors.hasErrors()) {
            response.setStatus(400);
            return result.setErrors(errors);
        }

        RestRange restRange = restConversionService.convert(range, RestRange.class);
        assert restRange != null;
        restRange.setTotal(userAdvancedService.getUsersCount());

        List<User> users = userAdvancedService.getUsers(range);
        List<RestUser> restUsers = users.stream()
                .map(u -> {
                    RestUser ru = restConversionService.convert(u, RestUser.class);
                    if (ru == null) ru = new RestUser();
                    return ru;
                }).collect(Collectors.toUnmodifiableList());

        return result.setDataProperty(DATA_PROPERTY_RANGE, restRange)
                .setDataProperty(DATA_PROPERTY_USER_LIST, restUsers);
    }
}