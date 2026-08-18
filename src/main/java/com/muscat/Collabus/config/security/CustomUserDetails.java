package com.muscat.Collabus.config.security;

import com.muscat.Collabus.User.entity.User;
import io.jsonwebtoken.Claims;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import static com.muscat.Collabus.config.jwt.JwtUtil.CLAIM_DISPLAY_NAME;
import static com.muscat.Collabus.config.jwt.JwtUtil.CLAIM_ROLE;
import static com.muscat.Collabus.config.jwt.JwtUtil.CLAIM_USER_ID;

/**
 * 엔티티가 아니라 값만 들고 있어서, JWT 클레임만으로도 만들 수 있다.
 * 요청마다 사용자를 다시 조회하지 않아도 된다.
 */
public class CustomUserDetails implements UserDetails {

  private final Long userId;
  private final String email;
  private final String password;
  private final String role;
  private final String displayName;

  private CustomUserDetails(Long userId, String email, String password, String role,
      String displayName) {
    this.userId = userId;
    this.email = email;
    this.password = password;
    this.role = role;
    this.displayName = displayName;
  }

  public CustomUserDetails(User user) {
    this(user.getId(), user.getEmail(), user.getPassword(), user.getRole().name(),
        user.getDisplayName());
  }

  /**
   * 검증이 끝난 Access Token 의 클레임으로 인증 주체를 만든다
   * 비밀번호는 인증 이후 단계에서 쓰이지 않으므로 담지 않는다.
   */
  public static CustomUserDetails from(Claims claims) {
    return new CustomUserDetails(
        claims.get(CLAIM_USER_ID, Number.class).longValue(),
        claims.getSubject(),
        null,
        claims.get(CLAIM_ROLE, String.class),
        claims.get(CLAIM_DISPLAY_NAME, String.class));
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority(role));
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public String getDisplayName() {
    return displayName;
  }

  public Long getUserId() {
    return userId;
  }
}
