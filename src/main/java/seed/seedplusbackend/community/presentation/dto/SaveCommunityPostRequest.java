package seed.seedplusbackend.community.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import seed.seedplusbackend.community.application.command.SaveCommunityPostCommand;

@Schema(description = "게시글 작성·전체 편집 요청. 태그 생략 또는 빈 배열은 미선택/제거")
public record SaveCommunityPostRequest(
    @Schema(description = "제목", example = "강남구 카페 창업 정보를 공유합니다") @NotBlank @Size(max = 100)
        String title,
    @Schema(description = "일반 텍스트 본문", example = "임대료와 유동인구를 비교하고 있습니다.")
        @NotBlank
        @Size(max = 10000)
        String content,
    @Schema(description = "선택 위치", example = "서울 강남구") @Size(max = 200) String location,
    @Schema(description = "자치구 지역 ID (SIGUNGU), 중복 불가, 최대 2개", example = "[1]") @Size(max = 2)
        List<@NotNull @Positive Long> regionIds,
    @Schema(description = "활성 업종 ID, 중복 불가, 최대 2개", example = "[1]") @Size(max = 2)
        List<@NotNull @Positive Long> industryIds) {
  public SaveCommunityPostCommand toCommand() {
    return new SaveCommunityPostCommand(title, content, location, regionIds, industryIds);
  }
}
