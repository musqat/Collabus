package com.muscat.Collabus.config.websocket;

import com.muscat.Collabus.config.jwt.JwtUtil;
import com.muscat.Collabus.config.security.CustomUserDetails;
import com.muscat.Collabus.config.token.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || accessor.getCommand() != StompCommand.CONNECT) {
            return message;
        }

        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("[WebSocket] Authorization 헤더가 없거나 형식이 올바르지 않습니다. (expected: Bearer <token>)");
            throw new IllegalArgumentException("WebSocket 연결에 Authorization 토큰이 필요합니다.");
        }

        String token = authHeader.substring(7);

        if (refreshTokenService.isBlacklisted(token)) {
            log.warn("[WebSocket] 블랙리스트에 등록된 토큰입니다. 로그아웃된 사용자의 연결 시도일 수 있습니다.");
            throw new IllegalArgumentException("만료된 토큰입니다.");
        }

        if (!jwtUtil.validateToken(token)) {
            log.warn("[WebSocket] 토큰 검증 실패. 위변조되었거나 만료된 토큰입니다.");
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        CustomUserDetails userDetails = CustomUserDetails.from(jwtUtil.parseClaims(token));
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        accessor.setUser(authentication);
        log.info("[WebSocket] '{}' 인증 완료, 실시간 연결을 시작합니다.", userDetails.getUsername());
        return message;
    }
}
