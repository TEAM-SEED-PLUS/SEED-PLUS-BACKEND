package seed.seedplusbackend.analysis.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import seed.seedplusbackend.analysis.application.command.AnalysisCollectionTarget;
import seed.seedplusbackend.analysis.application.result.AnalysisDataCollectionResult;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionRun;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionType;
import seed.seedplusbackend.analysis.domain.repository.AnalysisCollectionRunRepository;
import seed.seedplusbackend.commercial.application.command.CommercialDataCollectCommand;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;
import seed.seedplusbackend.user.domain.entity.User;
import seed.seedplusbackend.user.domain.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AnalysisDataCollectionCoordinator {

  private final UserRepository userRepository;
  private final AnalysisCollectionRunRepository runRepository;
  private final AnalysisCollectionTargetResolver targetResolver;
  private final AnalysisCollectionCommandFactory commandFactory;
  private final AnalysisDataCollectionService collectionService;

  public AnalysisDataCollectionResult collect(
      Long userId, AnalysisCollectionType analysisType, String regionCode, String industryCode) {
    return collect(userId, analysisType, regionCode, industryCode, true);
  }

  public AnalysisDataCollectionResult collectWithoutRealtime(
      Long userId, AnalysisCollectionType analysisType, String regionCode, String industryCode) {
    return collect(userId, analysisType, regionCode, industryCode, false);
  }

  private AnalysisDataCollectionResult collect(
      Long userId,
      AnalysisCollectionType analysisType,
      String regionCode,
      String industryCode,
      boolean includeRealtime) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_USER));
    AnalysisCollectionTarget target = targetResolver.resolve(regionCode, industryCode);
    List<CommercialDataCollectCommand> commands =
        createCommands(analysisType, target, includeRealtime);
    AnalysisCollectionRun run =
        runRepository.save(
            AnalysisCollectionRun.create(user, analysisType, regionCode, industryCode));

    return collectionService.collect(run.getId(), commands);
  }

  public AnalysisDataCollectionResult retry(
      Long userId,
      Long runId,
      AnalysisCollectionType analysisType,
      String regionCode,
      String industryCode) {
    return retry(userId, runId, analysisType, regionCode, industryCode, true);
  }

  public AnalysisDataCollectionResult retryWithoutRealtime(
      Long userId,
      Long runId,
      AnalysisCollectionType analysisType,
      String regionCode,
      String industryCode) {
    return retry(userId, runId, analysisType, regionCode, industryCode, false);
  }

  private AnalysisDataCollectionResult retry(
      Long userId,
      Long runId,
      AnalysisCollectionType analysisType,
      String regionCode,
      String industryCode,
      boolean includeRealtime) {
    AnalysisCollectionRun run =
        runRepository
            .findByIdAndUserId(runId, userId)
            .orElseThrow(
                () ->
                    new ApplicationException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "analysisCollectionRunId=%s".formatted(runId)));
    validateRetryCondition(run, analysisType, regionCode, industryCode);
    AnalysisCollectionTarget target =
        targetResolver.resolve(run.getRegionCode(), run.getIndustryCode());
    List<CommercialDataCollectCommand> commands =
        createCommands(analysisType, target, includeRealtime);
    return collectionService.collect(runId, commands);
  }

  private List<CommercialDataCollectCommand> createCommands(
      AnalysisCollectionType analysisType,
      AnalysisCollectionTarget target,
      boolean includeRealtime) {
    return includeRealtime
        ? commandFactory.create(analysisType, target)
        : commandFactory.createWithoutRealtime(analysisType, target);
  }

  private void validateRetryCondition(
      AnalysisCollectionRun run,
      AnalysisCollectionType analysisType,
      String regionCode,
      String industryCode) {
    if (run.getAnalysisType() != analysisType
        || !run.getRegionCode().equals(regionCode)
        || !run.getIndustryCode().equals(industryCode)) {
      throw new ApplicationException(
          ErrorCode.INVALID_REQUEST, "재시도 요청의 분석 유형, 지역 또는 업종이 기존 수집 실행과 일치하지 않습니다.");
    }
  }
}
