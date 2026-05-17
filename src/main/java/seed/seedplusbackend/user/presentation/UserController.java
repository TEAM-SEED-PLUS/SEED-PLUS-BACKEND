package seed.seedplusbackend.user.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.security.AuthenticatedUser;
import seed.seedplusbackend.global.swagger.annotation.ApiErrorCodeExamples;
import seed.seedplusbackend.user.application.UserQueryService;
import seed.seedplusbackend.user.presentation.dto.UserMeResponse;

@Tag(name = "유저", description = "사용자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserQueryService userQueryService;

  @Operation(summary = "내 정보 조회", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiErrorCodeExamples({ErrorCode.UNAUTHORIZED, ErrorCode.NOT_FOUND_USER})
  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserMeResponse>> me(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
    return ResponseEntity.ok(
        ApiResponse.success(
            UserMeResponse.from(userQueryService.getMe(authenticatedUser.getId()))));
  }
}
