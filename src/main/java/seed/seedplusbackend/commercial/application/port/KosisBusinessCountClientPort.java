package seed.seedplusbackend.commercial.application.port;

import java.util.List;
import seed.seedplusbackend.commercial.application.command.KosisBusinessCountCollectCommand;
import seed.seedplusbackend.commercial.application.result.KosisBusinessCountRowResult;

public interface KosisBusinessCountClientPort {

  List<KosisBusinessCountRowResult> fetch(KosisBusinessCountCollectCommand command);
}
