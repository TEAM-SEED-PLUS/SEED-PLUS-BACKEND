package seed.seedplusbackend.builderstore.application.query;

import org.springframework.data.domain.Pageable;

public record BuilderStoreSearchQuery(
    Long regionId,
    Long commercialAreaId,
    Long industryId,
    Integer minArea,
    Integer maxArea,
    Pageable pageable) {}
