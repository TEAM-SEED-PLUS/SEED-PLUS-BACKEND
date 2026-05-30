package seed.seedplusbackend.user.application.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UpdateUserCommand {

  private final String name;
  private final String password;
}
