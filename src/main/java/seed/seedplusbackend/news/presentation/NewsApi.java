package seed.seedplusbackend.news.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.response.*;
import seed.seedplusbackend.global.security.AuthenticatedUser;
import seed.seedplusbackend.global.swagger.annotation.ApiErrorCodeExamples;
import seed.seedplusbackend.news.presentation.dto.*;

@Tag(name = "뉴스", description = "비회원 조회·검색·조회 기록 가능. RSS는 원문 링크, 인사이트는 내부 표시.")
public interface NewsApi {
  @Operation(
      summary = "뉴스 검색",
      description = "0-base 기사 수 페이징. KST 날짜 범위, 지역·업종 내부 OR 및 필터 간 AND. 최신순 고정.")
  @ApiErrorCodeExamples({ErrorCode.INVALID_PARAMETER, ErrorCode.INVALID_SORT})
  @GetMapping("/articles")
  ResponseEntity<ApiResponse<PageResponse<NewsArticleResponse>>> articles(
      @Valid @ParameterObject NewsSearchRequest request);

  @Operation(
      summary = "뉴스 상세",
      description = "상세 GET은 조회수를 증가시키지 않는다. OUTLINK의 summary/body는 null.")
  @ApiErrorCodeExamples({ErrorCode.NOT_FOUND_NEWS})
  @GetMapping("/articles/{id}")
  ResponseEntity<ApiResponse<NewsArticleResponse>> article(@PathVariable @Positive long id);

  @Operation(
      summary = "뉴스 조회 기록",
      description = "기사·사용자 또는 서명된 익명 쿠키·한국 날짜별 1회. 비회원은 credentials: include 필요.")
  @ApiErrorCodeExamples({ErrorCode.NOT_FOUND_NEWS, ErrorCode.NEWS_VIEWER_UNAVAILABLE})
  @PostMapping("/articles/{id}/views")
  ResponseEntity<ApiResponse<Void>> view(
      @PathVariable @Positive long id,
      @AuthenticationPrincipal AuthenticatedUser user,
      @CookieValue(name = "seed_news_viewer", required = false) String cookie,
      HttpServletResponse response);

  @Operation(summary = "최근 7일 TOP3", description = "한국 시간 오늘 포함 7일 유효 조회수, 동률은 최신순. 최대 1분 캐시.")
  @GetMapping("/popular")
  ResponseEntity<ApiResponse<PageResponse<NewsArticleResponse>>> popular();

  @Operation(
      summary = "일간 뉴스 다이제스트",
      description = "기본 date=KST 오늘. 전일 기사 카테고리별 최대 3개. 미생성 날짜는 빈 결과.")
  @GetMapping("/digests")
  ResponseEntity<ApiResponse<PageResponse<NewsArticleResponse>>> digest(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date);
}
