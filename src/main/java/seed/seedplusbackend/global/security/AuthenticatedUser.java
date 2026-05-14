package seed.seedplusbackend.global.security;

import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import seed.seedplusbackend.user.domain.entity.UserRole;

@Getter
public class AuthenticatedUser implements UserDetails {

  private final Long id;
  private final String phoneNumber;
  private final String email;
  private final UserRole role;

  public AuthenticatedUser(Long id, String phoneNumber, String email, UserRole role) {
    this.id = id;
    this.phoneNumber = phoneNumber;
    this.email = email;
    this.role = role;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }

  @Override
  public String getPassword() {
    return "";
  }

  @Override
  public String getUsername() {
    return phoneNumber;
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
}
