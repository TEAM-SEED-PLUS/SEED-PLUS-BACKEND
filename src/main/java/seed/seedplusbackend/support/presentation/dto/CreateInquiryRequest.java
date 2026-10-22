package seed.seedplusbackend.support.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import seed.seedplusbackend.support.application.command.CreateInquiryCommand;
import seed.seedplusbackend.support.domain.entity.InquiryType;

@Schema(description = "문의 접수 요청")
public record CreateInquiryRequest(
    @NotNull @Schema(description = "문의 유형") InquiryType type,
    @NotBlank @Schema(description = "이름") String name,
    @NotBlank @Email @Schema(description = "이메일") String email,
    @NotBlank @Schema(description = "제목") String title,
    @NotBlank @Schema(description = "문의 내용") String content,
    @AssertTrue(message = "개인정보 처리 동의가 필요합니다.")
        @Schema(description = "개인정보 처리 동의", example = "true")
        boolean personalInformationConsent) {

  public CreateInquiryCommand toCommand() {
    return new CreateInquiryCommand(type, name, email, title, content);
  }
}
