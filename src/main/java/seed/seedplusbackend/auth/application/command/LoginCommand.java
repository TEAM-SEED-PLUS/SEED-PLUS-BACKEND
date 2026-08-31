package seed.seedplusbackend.auth.application.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LoginCommand {

  private final String loginId;
  private final String password;
}
