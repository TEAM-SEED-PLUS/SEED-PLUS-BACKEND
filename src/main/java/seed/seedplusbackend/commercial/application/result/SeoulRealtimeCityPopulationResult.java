package seed.seedplusbackend.commercial.application.result;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SeoulRealtimeCityPopulationResult(
    String areaCode,
    String areaName,
    String congestionLevel,
    String congestionMessage,
    long populationMin,
    long populationMax,
    BigDecimal malePopulationRate,
    BigDecimal femalePopulationRate,
    BigDecimal populationRate0,
    BigDecimal populationRate10,
    BigDecimal populationRate20,
    BigDecimal populationRate30,
    BigDecimal populationRate40,
    BigDecimal populationRate50,
    BigDecimal populationRate60,
    BigDecimal populationRate70,
    BigDecimal residentPopulationRate,
    BigDecimal nonResidentPopulationRate,
    boolean replacementUsed,
    LocalDateTime populationTime) {

  public long estimatedPopulation() {
    return populationMin + (populationMax - populationMin) / 2;
  }
}
