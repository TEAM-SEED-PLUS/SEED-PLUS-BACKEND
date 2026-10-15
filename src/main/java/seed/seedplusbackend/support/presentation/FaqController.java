package seed.seedplusbackend.support.presentation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.response.PageResponse;
import seed.seedplusbackend.support.application.FaqQueryService;
import seed.seedplusbackend.support.domain.entity.FaqCategory;
import seed.seedplusbackend.support.domain.entity.FaqGroup;
import seed.seedplusbackend.support.presentation.dto.FaqResponse;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/faqs")
public class FaqController {

  private final FaqQueryService faqQueryService;

  @GetMapping
  public ResponseEntity<ApiResponse<PageResponse<FaqResponse>>> getFaqs(
      @RequestParam(required = false) FaqGroup group,
      @RequestParam(required = false) FaqCategory category,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    return ResponseEntity.ok(
        ApiResponse.success(
            PageResponse.from(faqQueryService.getFaqs(group, category, page, size), FaqResponse::from)));
  }
}
