package seed.seedplusbackend.analysis.application;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.analysis.application.command.AnalysisCollectionTarget;
import seed.seedplusbackend.analysis.application.command.SmallBusinessCollectionTarget;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionType;
import seed.seedplusbackend.commercial.application.command.CommercialDataCollectCommand;
import seed.seedplusbackend.commercial.application.command.CommercialEstimatedSalesCollectCommand;
import seed.seedplusbackend.commercial.application.command.KosisBusinessCountCollectCommand;
import seed.seedplusbackend.commercial.application.command.KosisBusinessSurvivalCollectCommand;
import seed.seedplusbackend.commercial.application.command.SeoulSdotFootTrafficCollectCommand;
import seed.seedplusbackend.commercial.application.command.SmallBusinessStoreCollectCommand;

@Component
public class AnalysisCollectionCommandFactory {

  private static final int KOSIS_LATEST_YEAR_COUNT = 3;

  public List<CommercialDataCollectCommand> create(
      AnalysisCollectionType analysisType, AnalysisCollectionTarget target) {
    return create(analysisType, target, true);
  }

  public List<CommercialDataCollectCommand> createWithoutRealtime(
      AnalysisCollectionType analysisType, AnalysisCollectionTarget target) {
    return create(analysisType, target, false);
  }

  private List<CommercialDataCollectCommand> create(
      AnalysisCollectionType analysisType,
      AnalysisCollectionTarget target,
      boolean includeRealtime) {
    List<CommercialDataCollectCommand> commands = new ArrayList<>();
    commands.add(new CommercialEstimatedSalesCollectCommand(target.estimatedSalesQuarter(), true));

    target.smallBusinessTargets().stream()
        .distinct()
        .map(this::smallBusinessCommand)
        .forEach(commands::add);

    if (analysisType == AnalysisCollectionType.PROFIT) {
      return List.copyOf(commands);
    }

    commands.add(
        new KosisBusinessSurvivalCollectCommand(null, null, KOSIS_LATEST_YEAR_COUNT, true));
    commands.add(new KosisBusinessCountCollectCommand(null, null, KOSIS_LATEST_YEAR_COUNT, true));

    if (includeRealtime) {
      commands.add(new SeoulSdotFootTrafficCollectCommand(true));
    }

    return List.copyOf(commands);
  }

  private SmallBusinessStoreCollectCommand smallBusinessCommand(
      SmallBusinessCollectionTarget target) {
    return new SmallBusinessStoreCollectCommand(
        target.commercialAreaCode(),
        target.largeIndustryCode(),
        target.mediumIndustryCode(),
        target.smallIndustryCode(),
        true,
        target.queryType());
  }
}
