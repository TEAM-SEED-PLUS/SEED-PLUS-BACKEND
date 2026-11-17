package seed.seedplusbackend.payment.application.port;

import seed.seedplusbackend.payment.application.command.GmpayPaymentFeedbackCommand;
import seed.seedplusbackend.payment.application.result.GmpayPaymentInfo;

public interface GmpayPaymentVerificationPort {

  void validateFeedback(GmpayPaymentFeedbackCommand command);

  GmpayPaymentInfo getPaymentInfo(String providerOrderNo);
}
