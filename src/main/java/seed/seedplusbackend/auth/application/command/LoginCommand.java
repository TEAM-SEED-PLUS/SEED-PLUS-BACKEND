package seed.seedplusbackend.auth.application.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LoginCommand {

  private final String phoneNumber;
  private final String password;
}
