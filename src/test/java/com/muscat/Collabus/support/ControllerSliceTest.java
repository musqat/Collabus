package com.muscat.Collabus.support;

import com.muscat.Collabus.config.jwt.JwtUtil;
import com.muscat.Collabus.config.ratelimit.ClientIpResolver;
import com.muscat.Collabus.config.ratelimit.RateLimitPolicy;
import com.muscat.Collabus.config.ratelimit.RedisWindowCounter;
import com.muscat.Collabus.config.security.RestAccessDeniedHandler;
import com.muscat.Collabus.config.security.RestAuthenticationEntryPoint;
import com.muscat.Collabus.config.security.SecurityConfig;
import com.muscat.Collabus.config.security.SecurityErrorResponder;
import com.muscat.Collabus.config.token.RefreshTokenService;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * 컨트롤러 하나와 실제 보안 필터 · 예외 처리만 띄운다. 서비스는 각 테스트가 대역으로 넣는다
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@WebMvcTest
@Import({SecurityConfig.class, SecurityErrorResponder.class, RestAuthenticationEntryPoint.class,
    RestAccessDeniedHandler.class, RateLimitPolicy.class})
@MockitoBean(types = {JwtUtil.class, RefreshTokenService.class, ClientIpResolver.class,
    RedisWindowCounter.class})
public @interface ControllerSliceTest {

  @AliasFor(annotation = WebMvcTest.class, attribute = "controllers")
  Class<?>[] value() default {};
}
