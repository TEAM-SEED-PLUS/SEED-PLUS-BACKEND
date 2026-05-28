package seed.seedplusbackend.analysis.application.port;

import seed.seedplusbackend.analysis.application.command.ProfitAnalysisLambdaCommand;
import seed.seedplusbackend.analysis.application.command.SurvivalAnalysisLambdaCommand;
import seed.seedplusbackend.analysis.application.result.ProfitAnalysisResult;
import seed.seedplusbackend.analysis.application.result.SurvivalAnalysisResult;

public interface AnalysisLambdaClient {

  ProfitAnalysisResult requestProfit(ProfitAnalysisLambdaCommand command);

  SurvivalAnalysisResult requestSurvival(SurvivalAnalysisLambdaCommand command);
}
