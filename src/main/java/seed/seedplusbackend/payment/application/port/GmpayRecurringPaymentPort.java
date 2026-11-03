package seed.seedplusbackend.payment.application.port;

import seed.seedplusbackend.payment.application.command.GmpayRecurringPaymentCommand;
import seed.seedplusbackend.payment.application.result.GmpayRecurringPaymentResult;

public interface GmpayRecurringPaymentPort {

  GmpayRecurringPaymentResult requestPayment(GmpayRecurringPaymentCommand command);
}
