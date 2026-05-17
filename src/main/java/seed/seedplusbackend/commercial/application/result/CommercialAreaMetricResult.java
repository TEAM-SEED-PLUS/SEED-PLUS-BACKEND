package seed.seedplusbackend.commercial.application.result;

import java.math.BigDecimal;
import java.time.LocalDate;
import seed.seedplusbackend.metrics.domain.entity.CommercialAreaMonthlyMetric;

public record CommercialAreaMetricResult(
    LocalDate referenceMonth,
    long floatingPopulation,
    BigDecimal vacancyRate,
    long averageRent,
    BigDecimal openingRate,
    BigDecimal closureRate,
    BigDecimal salesGrowthRate,
    BigDecimal competitionDensity,
    int activityScore) {

  public static CommercialAreaMetricResult from(CommercialAreaMonthlyMetric metric) {
    return new CommercialAreaMetricResult(
        metric.getReferenceMonth(),
        metric.getFloatingPopulation(),
        metric.getVacancyRate(),
        metric.getAverageRent(),
        metric.getOpeningRate(),
        metric.getClosureRate(),
        metric.getSalesGrowthRate(),
        metric.getCompetitionDensity(),
        metric.getActivityScore());
  }
}
