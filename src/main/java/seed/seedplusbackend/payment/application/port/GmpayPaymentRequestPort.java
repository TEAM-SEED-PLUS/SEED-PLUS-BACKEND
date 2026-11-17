package seed.seedplusbackend.payment.application.port;

import seed.seedplusbackend.payment.application.command.GmpayPaymentRequestCommand;
import seed.seedplusbackend.payment.application.result.GmpayPaymentRequestResult;

public interface GmpayPaymentRequestPort {

  GmpayPaymentRequestResult requestPayment(GmpayPaymentRequestCommand command);
}
