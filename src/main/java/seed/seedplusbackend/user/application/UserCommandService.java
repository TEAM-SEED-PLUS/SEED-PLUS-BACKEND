package seed.seedplusbackend.user.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.user.application.command.UpdateUserCommand;
import seed.seedplusbackend.user.domain.entity.User;
import seed.seedplusbackend.user.domain.repository.UserRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserCommandService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public User updateMe(Long userId, UpdateUserCommand command) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_USER));

    String encodedPassword =
        command.getPassword() == null ? null : passwordEncoder.encode(command.getPassword());
    user.updateProfile(command.getName(), encodedPassword);
    log.info(
        "[UserCommandService] 내 프로필 수정 완료 사용자ID={} 비밀번호변경여부={}", userId, encodedPassword != null);

    return user;
  }
}
