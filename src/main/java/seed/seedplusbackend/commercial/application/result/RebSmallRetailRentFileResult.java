package seed.seedplusbackend.commercial.application.result;

import java.util.List;

public record RebSmallRetailRentFileResult(
    List<RebSmallRetailRentPeriod> periods, List<RebSmallRetailRentRowResult> rows) {}
