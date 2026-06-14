package com.example.sportswms.global.util;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageUtils {

    private final MessageSource messageSource;
    private static MessageSource staticMessageSource;

    @PostConstruct
    public void init() {
        staticMessageSource = this.messageSource;
    }

    /**
     * @param code messages.properties에 정의된 키 값
     * @param args 메시지에 치환될 인자들
     * @return 치환된 메시지 문자열
     */
    public static String getMessage(String code, Object... args) {
        if (staticMessageSource == null) {
            return code;
        }
        return staticMessageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
