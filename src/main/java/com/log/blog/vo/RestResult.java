package com.log.blog.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RestResult {
    private static final String DATA_PROPERTY_MESSAGE = "message";
    private static final String RENDER_MESSAGE_PREFIX = "{";
    private static final String RENDER_MESSAGE_SUFFIX = "}";

    @JsonView(View.Base.class)
    private final RestRootError errors;

    @JsonView(View.Base.class)
    private final RestData data;

    @JsonView(View.Base.class)
    private String requestId;

    @JsonIgnore
    private Locale locale;

    @JsonIgnore
    private final MessageSource messageSource;

    public RestResult(RestRootError errors, RestData data, MessageSource messageSource) {
        this.errors = errors;
        this.data = data;
        this.messageSource = messageSource;
    }

    public RestRootError getErrors() {
        return errors;
    }

    public RestData getData() {
        return data;
    }

    public String getRequestId() {
        return requestId;
    }

    public RestResult setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public RestResult setLocale(Locale locale) {
        this.locale = locale;
        return this;
    }

    public RestResult setError(String target, String code, String defaultMessage) {
        String message = renderMessage(code, defaultMessage);
        errors.setTarget(target);
        errors.setMessage(message);
        return this;
    }

    public RestResult setErrors(BindingResult errors) {
        // GlobalError
        String target = errors.getObjectName();
        ObjectError globalError = errors.getGlobalError();
        if (globalError != null) {
            String code = globalError.getCode();
            String defaultMessage = globalError.getDefaultMessage();
            setError(target, code, defaultMessage);
        } else {
            setError(target, "error.abstract", null);
        }

        // FieldErrors
        List<RestError> detailErrors = errors.getAllErrors().stream()
                .filter(e -> e instanceof FieldError)
                .map(e -> {
                    FieldError fe = (FieldError) e;
                    String code = fe.getCode();
                    String defaultMessage = fe.getDefaultMessage();
                    return new RestError(fe.getField(), renderMessage(code, defaultMessage));
                }).collect(Collectors.toUnmodifiableList());
        return setDetailErrors(detailErrors);
    }

    public RestResult addDetailError(String target, String code, String defaultMessage) {
        RestError e = new RestError();
        e.setTarget(target);
        e.setMessage(renderMessage(code, defaultMessage));
        errors.getDetails().add(e);
        return this;
    }

    public RestResult setDetailErrors(List<RestError> detailErrors) {
        errors.setDetails(detailErrors);
        return this;
    }

    public RestResult setDataMessage(String code) {
        return setDateMessage(code, null);
    }

    public RestResult setDateMessage(String code, String defaultMessage) {
        String message = renderMessage(code, defaultMessage);
        data.put(DATA_PROPERTY_MESSAGE, message);
        return this;
    }

    public RestResult setDataProperty(String name, Object value) {
        data.put(name, value);
        return this;
    }

    private String renderDefaultMessage(String defaultMessage) {
        if (defaultMessage == null) {
            return null;
        } else if (defaultMessage.startsWith(RENDER_MESSAGE_PREFIX) && defaultMessage.endsWith(RENDER_MESSAGE_SUFFIX)) {
            String code = defaultMessage.substring(1, defaultMessage.length() - 2);
            return renderMessage(code, defaultMessage);
        } else {
            return defaultMessage;
        }
    }

    private String renderMessage(String code, String defaultMessage) {
        return code == null ? renderDefaultMessage(defaultMessage)
                : messageSource.getMessage(code, null, defaultMessage, locale);
    }
}
