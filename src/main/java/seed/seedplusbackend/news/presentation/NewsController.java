package seed.seedplusbackend.news.presentation;

import jakarta.servlet.http.HttpServletResponse;
import java.time.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import seed.seedplusbackend.global.response.*;
import seed.seedplusbackend.global.security.AuthenticatedUser;
import seed.seedplusbackend.news.application.*;
import seed.seedplusbackend.news.presentation.dto.*;

@RestController
@Validated
@RequestMapping("/api/v1/news")
public class NewsController implements NewsApi {
  private final NewsQueryService query;
  private final NewsViewService views;
  private final boolean secure;

  public NewsController(
      NewsQueryService query,
      NewsViewService views,
      @Value("${news.viewer.cookie-secure:true}") boolean secure) {
    this.query = query;
    this.views = views;
    this.secure = secure;
  }

  @Override
  public ResponseEntity<ApiResponse<PageResponse<NewsArticleResponse>>> articles(
      NewsSearchRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            PageResponse.from(query.search(request.toQuery()), NewsArticleResponse::from)));
  }

  @Override
  public ResponseEntity<ApiResponse<NewsArticleResponse>> article(long id) {
    return ResponseEntity.ok(ApiResponse.success(NewsArticleResponse.from(query.detail(id))));
  }

  @Override
  public ResponseEntity<ApiResponse<Void>> view(
      long id, AuthenticatedUser user, String cookie, HttpServletResponse response) {
    String newCookie = views.record(id, user == null ? null : user.getId(), cookie);
    if (newCookie != null)
      response.addHeader(
          HttpHeaders.SET_COOKIE,
          ResponseCookie.from("seed_news_viewer", newCookie)
              .httpOnly(true)
              .secure(secure)
              .sameSite(secure ? "None" : "Lax")
              .path("/api/v1/news")
              .maxAge(Duration.ofDays(90))
              .build()
              .toString());
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(ApiResponse.success(HttpStatus.OK));
  }

  @Override
  public ResponseEntity<ApiResponse<PageResponse<NewsArticleResponse>>> popular() {
    return ResponseEntity.ok(
        ApiResponse.success(PageResponse.from(query.popular(), NewsArticleResponse::from)));
  }

  @Override
  public ResponseEntity<ApiResponse<NewsDigestResponse>> digest(LocalDate date) {
    return ResponseEntity.ok(ApiResponse.success(NewsDigestResponse.from(query.digest(date))));
  }
}
