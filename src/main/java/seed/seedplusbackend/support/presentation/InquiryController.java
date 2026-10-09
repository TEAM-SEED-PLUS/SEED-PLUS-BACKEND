package seed.seedplusbackend.support.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.support.application.InquiryService;
import seed.seedplusbackend.support.presentation.dto.CreateInquiryRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inquiries")
public class InquiryController {

  private final InquiryService inquiryService;

  @PostMapping
  public ResponseEntity<ApiResponse<Void>> createInquiry(
      @Valid @RequestBody CreateInquiryRequest request) {
    inquiryService.createInquiry(request.toCommand());
    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK));
  }
}
