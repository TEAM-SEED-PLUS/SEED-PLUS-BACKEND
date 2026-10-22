package seed.seedplusbackend.support.infrastructure.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.support.application.command.CreateInquiryCommand;
import seed.seedplusbackend.support.application.port.InquiryMailSender;

@Slf4j
@Component
@RequiredArgsConstructor
public class JavaMailInquiryMailSender implements InquiryMailSender {

  private static final String SUBJECT_PREFIX = "[SEED+] 문의하기 - ";
  private static final String BODY_FORMAT =
      """
      문의 유형: %s
      이름: %s
      이메일: %s

      %s
      """;

  private final JavaMailSender mailSender;
  private final InquiryMailProperties properties;

  @Override
  public void send(CreateInquiryCommand command) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(properties.fromAddress());
    message.setTo(properties.recipientAddress());
    message.setReplyTo(command.getEmail());
    message.setSubject(SUBJECT_PREFIX + command.getTitle());
    message.setText(
        BODY_FORMAT.formatted(
            command.getType(), command.getName(), command.getEmail(), command.getContent()));

    try {
      mailSender.send(message);
    } catch (MailException e) {
      log.error("[JavaMailInquiryMailSender] 문의 메일 발송 실패", e);
      throw new ApplicationException(ErrorCode.MAIL_SEND_FAILED, e);
    }
  }
}
