package seed.seedplusbackend.auth.application.command;

import java.time.LocalDate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SignupCommand {

  private final String phoneNumber;
  private final String password;
  private final String name;
  private final LocalDate birthDate;
}
