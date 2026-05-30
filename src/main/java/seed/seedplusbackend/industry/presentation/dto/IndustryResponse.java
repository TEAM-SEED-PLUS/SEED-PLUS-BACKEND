package seed.seedplusbackend.industry.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import seed.seedplusbackend.industry.application.result.IndustryResult;
import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.industry.domain.entity.IndustryLevel;

@Schema(description = "업종 응답")
public record IndustryResponse(
    @Schema(description = "업종 ID", example = "1") Long industryId,
    @Schema(description = "업종 코드", example = "I561") String industryCode,
    @Schema(description = "업종명", example = "음식점업") String name,
    @Schema(description = "상위 업종 ID", example = "1") Long parentIndustryId,
    @Schema(description = "업종 분류 레벨", example = "LARGE") IndustryLevel level,
    @Schema(description = "하위 업종 목록") List<IndustryResponse> children) {

  public IndustryResponse {
    children = children == null ? List.of() : List.copyOf(children);
  }

  public static IndustryResponse from(IndustryResult result) {
    return new IndustryResponse(
        result.industryId(),
        result.industryCode(),
        result.name(),
        result.parentIndustryId(),
        result.level(),
        from(result.children()));
  }

  public static IndustryResponse from(Industry industry) {
    Long parentIndustryId =
        industry.getParentIndustry() == null ? null : industry.getParentIndustry().getId();

    return new IndustryResponse(
        industry.getId(),
        industry.getIndustryCode(),
        industry.getName(),
        parentIndustryId,
        industry.getLevel(),
        List.of());
  }

  public static List<IndustryResponse> from(List<IndustryResult> results) {
    return results.stream().map(IndustryResponse::from).toList();
  }
}
