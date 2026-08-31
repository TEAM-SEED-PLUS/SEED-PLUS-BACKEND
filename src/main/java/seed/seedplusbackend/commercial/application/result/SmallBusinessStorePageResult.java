package seed.seedplusbackend.commercial.application.result;

import java.util.List;

public record SmallBusinessStorePageResult(
    int totalCount, List<SmallBusinessStoreRowResult> rows) {}
