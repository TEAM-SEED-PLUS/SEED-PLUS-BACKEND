package seed.seedplusbackend.analysis.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import seed.seedplusbackend.analysis.application.command.AnalysisCollectionTarget;
import seed.seedplusbackend.analysis.application.command.SmallBusinessCollectionTarget;
import seed.seedplusbackend.commercial.application.command.CommercialDataCollectCommand;
import seed.seedplusbackend.commercial.application.command.CommercialEstimatedSalesCollectCommand;
import seed.seedplusbackend.commercial.application.command.KosisBusinessCountCollectCommand;
import seed.seedplusbackend.commercial.application.command.KosisBusinessSurvivalCollectCommand;
import seed.seedplusbackend.commercial.application.command.SeoulSdotFootTrafficCollectCommand;
import seed.seedplusbackend.commercial.application.command.SmallBusinessStoreCollectCommand;

@DisplayName("분석용 공공데이터 수집 명령 팩토리")
class AnalysisCollectionCommandFactoryTest {

  private final AnalysisCollectionCommandFactory factory = new AnalysisCollectionCommandFactory();

  @Test
  @DisplayName("동적 계산에 필요한 공공데이터 수집 명령을 모두 생성한다")
  void createsAllRequiredCommands() {
    SmallBusinessCollectionTarget first =
        new SmallBusinessCollectionTarget("9151", "Q", "Q12", "Q12A01");
    SmallBusinessCollectionTarget second =
        new SmallBusinessCollectionTarget("9152", "Q", "Q12", "Q12A01");

    List<CommercialDataCollectCommand> commands =
        factory.create(new AnalysisCollectionTarget("20262", List.of(first, second)));

    assertThat(commands)
        .hasSize(6)
        .anySatisfy(
            command -> {
              assertThat(command).isInstanceOf(CommercialEstimatedSalesCollectCommand.class);
              assertThat(command.force()).isTrue();
            })
        .filteredOn(SmallBusinessStoreCollectCommand.class::isInstance)
        .hasSize(2);
    assertThat(commands)
        .anyMatch(KosisBusinessSurvivalCollectCommand.class::isInstance)
        .anyMatch(KosisBusinessCountCollectCommand.class::isInstance)
        .anyMatch(SeoulSdotFootTrafficCollectCommand.class::isInstance);
  }

  @Test
  @DisplayName("같은 상권과 업종 대상은 한 번만 수집한다")
  void removesDuplicatedSmallBusinessTargets() {
    SmallBusinessCollectionTarget duplicated =
        new SmallBusinessCollectionTarget("9151", "Q", "Q12", "Q12A01");

    List<CommercialDataCollectCommand> commands =
        factory.create(new AnalysisCollectionTarget("20262", List.of(duplicated, duplicated)));

    assertThat(commands).filteredOn(SmallBusinessStoreCollectCommand.class::isInstance).hasSize(1);
  }

  @Test
  @DisplayName("실시간 데이터를 제외하면 S-DoT 수집 명령을 생성하지 않는다")
  void excludesRealtimeCommand() {
    SmallBusinessCollectionTarget target =
        new SmallBusinessCollectionTarget("9151", "Q", "Q12", "Q12A01");

    List<CommercialDataCollectCommand> commands =
        factory.createWithoutRealtime(new AnalysisCollectionTarget("20262", List.of(target)));

    assertThat(commands)
        .noneMatch(SeoulSdotFootTrafficCollectCommand.class::isInstance)
        .anyMatch(CommercialEstimatedSalesCollectCommand.class::isInstance)
        .anyMatch(SmallBusinessStoreCollectCommand.class::isInstance)
        .anyMatch(KosisBusinessSurvivalCollectCommand.class::isInstance)
        .anyMatch(KosisBusinessCountCollectCommand.class::isInstance);
  }
}
