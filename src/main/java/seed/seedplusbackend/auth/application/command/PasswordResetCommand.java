package seed.seedplusbackend.auth.application.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PasswordResetCommand {

  private final String email;
  private final String currentPassword;
  private final String newPassword;
  private final String newPasswordConfirmation;
}
