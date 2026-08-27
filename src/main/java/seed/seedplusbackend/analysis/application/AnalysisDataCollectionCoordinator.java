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
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_USER));
    AnalysisCollectionTarget target = targetResolver.resolve(regionCode, industryCode);
    List<CommercialDataCollectCommand> commands = commandFactory.create(target);
    AnalysisCollectionRun run =
        runRepository.save(
            AnalysisCollectionRun.create(user, analysisType, regionCode, industryCode));

    return collectionService.collect(run.getId(), commands);
  }
}
