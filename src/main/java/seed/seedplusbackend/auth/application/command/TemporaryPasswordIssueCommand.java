package seed.seedplusbackend.auth.application.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TemporaryPasswordIssueCommand {

  private final String email;
}
