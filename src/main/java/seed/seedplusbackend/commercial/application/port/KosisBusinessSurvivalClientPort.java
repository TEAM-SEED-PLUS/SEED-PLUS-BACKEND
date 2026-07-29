package seed.seedplusbackend.commercial.application.port;

import java.util.List;
import seed.seedplusbackend.commercial.application.command.KosisBusinessSurvivalCollectCommand;
import seed.seedplusbackend.commercial.application.result.KosisBusinessSurvivalRowResult;

public interface KosisBusinessSurvivalClientPort {

  List<KosisBusinessSurvivalRowResult> fetch(KosisBusinessSurvivalCollectCommand command);
}
