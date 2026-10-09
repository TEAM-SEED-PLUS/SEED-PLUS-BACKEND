package seed.seedplusbackend.support.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import seed.seedplusbackend.support.application.command.CreateInquiryCommand;
import seed.seedplusbackend.support.application.port.InquiryMailSender;

@Service
@RequiredArgsConstructor
public class InquiryService {

  private final InquiryMailSender inquiryMailSender;

  public void createInquiry(CreateInquiryCommand command) {
    inquiryMailSender.send(command);
  }
}
