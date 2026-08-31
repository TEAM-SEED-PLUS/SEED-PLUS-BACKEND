package seed.seedplusbackend.analysis.application.result;

import java.util.List;
import seed.seedplusbackend.analysis.domain.entity.AnalysisCollectionRunStatus;

public record AnalysisDataCollectionResult(
    Long runId, AnalysisCollectionRunStatus status, List<String> failedDataTypes) {}
