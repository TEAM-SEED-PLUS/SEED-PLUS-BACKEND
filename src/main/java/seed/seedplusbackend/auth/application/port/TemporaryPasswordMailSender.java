package seed.seedplusbackend.auth.application.port;

public interface TemporaryPasswordMailSender {

  void send(String toAddress, String temporaryPassword);
}
