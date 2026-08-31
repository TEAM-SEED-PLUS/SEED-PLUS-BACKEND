package seed.seedplusbackend.analysis.application.support;

import java.math.BigDecimal;

public final class SurvivalCalculatorFallback {

  public static final BigDecimal SALES_GROWTH_RATE = BigDecimal.ZERO;
  public static final int STORE_DENSITY = 40;
  public static final BigDecimal VACANCY_RATE = new BigDecimal("8.0");
  public static final int TRAFFIC_INDEX = 14000;

  private SurvivalCalculatorFallback() {}
}
