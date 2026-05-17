package seed.seedplusbackend.user.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seed.seedplusbackend.global.response.ApiResponse;
import seed.seedplusbackend.global.security.AuthenticatedUser;
import seed.seedplusbackend.user.application.UserCommandService;
import seed.seedplusbackend.user.application.UserQueryService;
import seed.seedplusbackend.user.presentation.dto.UserMeResponse;
import seed.seedplusbackend.user.presentation.dto.UserUpdateRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController implements UserApi {

  private final UserQueryService userQueryService;
  private final UserCommandService userCommandService;

  @Override
  public ResponseEntity<ApiResponse<UserMeResponse>> me(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
    return ResponseEntity.ok(
        ApiResponse.success(
            UserMeResponse.from(userQueryService.getMe(authenticatedUser.getId()))));
  }

  @Override
  public ResponseEntity<ApiResponse<UserMeResponse>> updateMe(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Valid @RequestBody UserUpdateRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            UserMeResponse.from(
                userCommandService.updateMe(authenticatedUser.getId(), request.toCommand()))));
  }
}
