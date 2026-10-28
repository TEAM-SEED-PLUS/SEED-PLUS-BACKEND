package seed.seedplusbackend.payment.application.port;

import seed.seedplusbackend.payment.application.command.GmpayCardRegistrationRequestCommand;
import seed.seedplusbackend.payment.application.result.GmpayCardRegistrationRequestResult;

public interface GmpayCardRegistrationPort {

  GmpayCardRegistrationRequestResult requestCardRegistration(
      GmpayCardRegistrationRequestCommand command);
}
