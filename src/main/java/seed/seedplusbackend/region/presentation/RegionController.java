package seed.seedplusbackend.region.presentation;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import seed.seedplusbackend.region.application.RegionQueryService;
import seed.seedplusbackend.region.domain.entity.RegionCodeType;
import seed.seedplusbackend.region.presentation.dto.RegionResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/regions")
public class RegionController implements RegionApi {

  private final RegionQueryService regionQueryService;

  @Override
  public ResponseEntity<List<RegionResponse>> getRegions(
      @RequestParam(required = false) String sido,
      @RequestParam(required = false) String sigungu,
      @RequestParam(required = false) RegionCodeType codeType) {
    return ResponseEntity.ok(
        regionQueryService.getRegions(sido, sigungu, codeType).stream()
            .map(RegionResponse::from)
            .toList());
  }

  @Override
  public ResponseEntity<RegionResponse> getRegion(@PathVariable Long regionId) {
    return ResponseEntity.ok(RegionResponse.from(regionQueryService.getRegion(regionId)));
  }
}
