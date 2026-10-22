package seed.seedplusbackend.support.application.port;

import seed.seedplusbackend.support.application.command.CreateInquiryCommand;

public interface InquiryMailSender {

  void send(CreateInquiryCommand command);
}
