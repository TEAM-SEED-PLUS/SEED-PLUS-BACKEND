package seed.seedplusbackend.user.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.user.domain.entity.User;
import seed.seedplusbackend.user.domain.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserQueryService {

  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public User getMe(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_USER));
  }
}
