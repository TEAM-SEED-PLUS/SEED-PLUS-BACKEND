package seed.seedplusbackend.support.presentation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.response.PageResponse;
import seed.seedplusbackend.support.application.NoticeQueryService;
import seed.seedplusbackend.support.domain.entity.NoticeCategory;
import seed.seedplusbackend.support.presentation.dto.NoticeDetailResponse;
import seed.seedplusbackend.support.presentation.dto.NoticeResponse;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notices")
public class NoticeController {

  private final NoticeQueryService noticeQueryService;

  @GetMapping
  public ResponseEntity<ApiResponse<PageResponse<NoticeResponse>>> getNotices(
      @RequestParam(required = false) NoticeCategory category,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    return ResponseEntity.ok(
        ApiResponse.success(
            PageResponse.from(
                noticeQueryService.getNotices(category, page, size), NoticeResponse::from)));
  }

  @GetMapping("/{noticeId}")
  public ResponseEntity<ApiResponse<NoticeDetailResponse>> getNotice(
      @PathVariable @Positive Long noticeId) {
    return ResponseEntity.ok(
        ApiResponse.success(NoticeDetailResponse.from(noticeQueryService.getNotice(noticeId))));
  }
}
