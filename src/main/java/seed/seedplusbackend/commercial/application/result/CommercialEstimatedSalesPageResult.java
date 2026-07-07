package seed.seedplusbackend.commercial.application.result;

import java.util.List;

public record CommercialEstimatedSalesPageResult(
    int totalCount, List<CommercialEstimatedSalesRowResult> rows) {}
