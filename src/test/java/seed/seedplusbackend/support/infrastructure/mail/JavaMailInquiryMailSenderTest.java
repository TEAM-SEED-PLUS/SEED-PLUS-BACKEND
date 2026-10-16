package seed.seedplusbackend.support.infrastructure.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.support.application.command.CreateInquiryCommand;
import seed.seedplusbackend.support.domain.entity.InquiryType;

@ExtendWith(MockitoExtension.class)
@DisplayName("문의 메일 발송")
class JavaMailInquiryMailSenderTest {

  @Mock private JavaMailSender mailSender;

  private final InquiryMailProperties properties =
      new InquiryMailProperties("noreply@seedplus.kr", "seedbusiness0@gmail.com");

  @Test
  @DisplayName("문의 내용을 지정된 수신자에게 전달한다")
  void send_sendsInquiryMail() {
    JavaMailInquiryMailSender inquiryMailSender =
        new JavaMailInquiryMailSender(mailSender, properties);
    CreateInquiryCommand command =
        new CreateInquiryCommand(
            InquiryType.SERVICE_ERROR,
            "홍길동",
            "user@example.com",
            "오류 문의",
            "분석 결과가 보이지 않습니다.");

    inquiryMailSender.send(command);

    ArgumentCaptor<SimpleMailMessage> messageCaptor =
        ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mailSender).send(messageCaptor.capture());
    SimpleMailMessage message = messageCaptor.getValue();
    assertThat(message.getFrom()).isEqualTo("noreply@seedplus.kr");
    assertThat(message.getTo()).containsExactly("seedbusiness0@gmail.com");
    assertThat(message.getReplyTo()).isEqualTo("user@example.com");
    assertThat(message.getSubject()).isEqualTo("[SEED+] 문의하기 - 오류 문의");
    assertThat(message.getText()).contains("SERVICE_ERROR", "홍길동", "분석 결과가 보이지 않습니다.");
  }

  @Test
  @DisplayName("메일 발송에 실패하면 공통 예외를 발생시킨다")
  void send_throwsApplicationExceptionWhenMailSendFails() {
    JavaMailInquiryMailSender inquiryMailSender =
        new JavaMailInquiryMailSender(mailSender, properties);
    CreateInquiryCommand command =
        new CreateInquiryCommand(
            InquiryType.SERVICE_USE, "홍길동", "user@example.com", "서비스 문의", "문의 내용입니다.");
    doThrow(new MailSendException("메일 발송 실패")).when(mailSender).send(any(SimpleMailMessage.class));

    assertThatThrownBy(() -> inquiryMailSender.send(command))
        .isInstanceOfSatisfying(
            ApplicationException.class,
            exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MAIL_SEND_FAILED));
  }
}
