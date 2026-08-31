package seed.seedplusbackend.commercial.application.result;

import java.util.List;

public record SeoulSdotFootTrafficPageResult(
    int totalCount, List<SeoulSdotFootTrafficRowResult> rows) {}
