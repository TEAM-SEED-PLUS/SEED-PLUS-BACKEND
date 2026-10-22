package seed.seedplusbackend.support.application.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import seed.seedplusbackend.support.domain.entity.InquiryType;

@Getter
@RequiredArgsConstructor
public class CreateInquiryCommand {

  private final InquiryType type;
  private final String name;
  private final String email;
  private final String title;
  private final String content;
}
