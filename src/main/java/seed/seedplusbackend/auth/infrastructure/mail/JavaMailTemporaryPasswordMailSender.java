package seed.seedplusbackend.auth.infrastructure.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.auth.application.port.TemporaryPasswordMailSender;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@Slf4j
@Component
@RequiredArgsConstructor
public class JavaMailTemporaryPasswordMailSender implements TemporaryPasswordMailSender {

  private static final String SUBJECT = "[SEED+] 임시 비밀번호 안내";
  private static final String BODY_FORMAT =
      """
      안녕하세요, SEED+입니다.

      요청하신 임시 비밀번호는 아래와 같습니다.

      임시 비밀번호: %s

      임시 비밀번호로 로그인한 뒤 반드시 새 비밀번호로 변경해 주세요.
      본인이 요청하지 않았다면 이 메일을 무시하셔도 됩니다.
      """;

  private final JavaMailSender mailSender;
  private final TemporaryPasswordMailProperties properties;

  @Override
  public void send(String toAddress, String temporaryPassword) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(properties.fromAddress());
    message.setTo(toAddress);
    message.setSubject(SUBJECT);
    message.setText(BODY_FORMAT.formatted(temporaryPassword));

    try {
      mailSender.send(message);
    } catch (MailException e) {
      log.error("[JavaMailTemporaryPasswordMailSender] 임시 비밀번호 메일 발송 실패, 사유={}", e.getMessage(), e);
      throw new ApplicationException(ErrorCode.MAIL_SEND_FAILED, e);
    }
  }
}
