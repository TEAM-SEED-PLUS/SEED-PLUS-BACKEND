package seed.seedplusbackend.user.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.security.AuthenticatedUser;
import seed.seedplusbackend.global.swagger.annotation.ApiErrorCodeExamples;
import seed.seedplusbackend.user.presentation.dto.UserMeResponse;
import seed.seedplusbackend.user.presentation.dto.UserUpdateRequest;

@Tag(name = "회원", description = "사용자 API")
public interface UserApi {

  @Operation(
      summary = "내 정보 조회",
      operationId = "getMyProfile",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiErrorCodeExamples({ErrorCode.UNAUTHORIZED, ErrorCode.NOT_FOUND_USER})
  @GetMapping("/me")
  ResponseEntity<ApiResponse<UserMeResponse>> me(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser);

  @Operation(
      summary = "내 정보 수정",
      operationId = "updateMyProfile",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiErrorCodeExamples({
    ErrorCode.INVALID_PARAMETER,
    ErrorCode.UNAUTHORIZED,
    ErrorCode.NOT_FOUND_USER
  })
  @PatchMapping("/me")
  ResponseEntity<ApiResponse<UserMeResponse>> updateMe(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Valid @RequestBody UserUpdateRequest request);
}
