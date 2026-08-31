package seed.seedplusbackend.analysis.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import seed.seedplusbackend.analysis.application.result.PublicDataMetrics;
import seed.seedplusbackend.commercial.domain.entity.ExternalDataSource;
import seed.seedplusbackend.commercial.domain.entity.IndustryExternalCodeMapping;
import seed.seedplusbackend.commercial.domain.entity.RegionExternalCodeMapping;
import seed.seedplusbackend.commercial.infrastructure.repository.IndustryExternalCodeMappingJpaRepository;
import seed.seedplusbackend.commercial.infrastructure.repository.RegionExternalCodeMappingJpaRepository;
import seed.seedplusbackend.industry.domain.entity.Industry;
import seed.seedplusbackend.industry.domain.entity.IndustryLevel;
import seed.seedplusbackend.industry.domain.entity.IndustryStatus;
import seed.seedplusbackend.industry.infrastructure.repository.IndustryJpaRepository;
import seed.seedplusbackend.region.domain.entity.Region;
import seed.seedplusbackend.region.domain.entity.RegionCodeType;
import seed.seedplusbackend.region.infrastructure.repository.RegionJpaRepository;
import seed.seedplusbackend.support.AbstractPostgresContainerTest;
import seed.seedplusbackend.support.RepositoryTest;

@RepositoryTest
@DisplayName("JdbcPublicDataResolver")
class JdbcPublicDataResolverTest extends AbstractPostgresContainerTest {

  private static final String REGION_CODE = "9999999998";
  private static final String INDUSTRY_CODE = "Q12A01";

  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private RegionJpaRepository regionRepository;
  @Autowired private IndustryJpaRepository industryRepository;
  @Autowired private RegionExternalCodeMappingJpaRepository regionMappingRepository;
  @Autowired private IndustryExternalCodeMappingJpaRepository industryMappingRepository;

  @Test
  @DisplayName("내부 코드와 외부 코드 매핑으로 공공데이터를 정확히 조회한다")
  void resolvesMetricsByCodes() {
    Industry industry = saveReferenceData();
    saveMappings(industry);
    insertPublicData();

    PublicDataMetrics metrics = resolver().resolve(REGION_CODE, INDUSTRY_CODE);

    assertThat(metrics.monthlySalesAmount()).isEqualTo(150L);
    assertThat(metrics.storeCountInCommercialArea()).isEqualTo(1);
    assertThat(metrics.districtAverageSalesAmount()).isEqualByComparingTo("150");
    assertThat(metrics.cityAverageSalesAmount()).isEqualByComparingTo("300");
    assertThat(metrics.salesGrowthRate()).isEqualByComparingTo("50");
    assertThat(metrics.trafficIndex()).isEqualTo(123);
    assertThat(metrics.survivalRate()).isEqualByComparingTo("70");
    assertThat(metrics.closedBusinesses()).isEqualByComparingTo("5");
    assertThat(metrics.activeBusinesses()).isEqualByComparingTo("100");
    assertThat(metrics.newBusinesses()).isEqualByComparingTo("10");
    assertThat(metrics.fallbackUsed()).isFalse();
    assertThat(metrics.dataSources()).hasSize(4);
  }

  @Test
  @DisplayName("매핑과 원천 데이터가 없으면 임의의 계산값을 만들지 않는다")
  void doesNotInventFallbackValues() {
    saveRegion();

    PublicDataMetrics metrics = resolver().resolve(REGION_CODE, INDUSTRY_CODE);

    assertThat(metrics.monthlySalesAmount()).isNull();
    assertThat(metrics.storeCountInCommercialArea()).isZero();
    assertThat(metrics.salesGrowthRate()).isNull();
    assertThat(metrics.trafficIndex()).isNull();
    assertThat(metrics.survivalRate()).isNull();
    assertThat(metrics.fallbackUsed()).isTrue();
    assertThat(metrics.dataSources()).containsExactly("소상공인시장진흥공단 상가정보");
  }

  private Industry saveReferenceData() {
    saveRegion();
    return industryRepository.save(
        Industry.builder()
            .industryCode(INDUSTRY_CODE)
            .name("커피 전문점")
            .level(IndustryLevel.SMALL)
            .status(IndustryStatus.ACTIVE)
            .build());
  }

  private void saveRegion() {
    regionRepository.save(
        Region.builder()
            .sido("서울특별시")
            .sigungu("강남구")
            .dong("역삼1동")
            .code(REGION_CODE)
            .codeType(RegionCodeType.LEGAL_DONG)
            .build());
  }

  private void saveMappings(Industry industry) {
    regionMappingRepository.save(
        RegionExternalCodeMapping.builder()
            .regionCode(REGION_CODE)
            .source(ExternalDataSource.SEOUL_ESTIMATED_SALES)
            .externalCode("T1")
            .externalName("테스트 상권")
            .build());
    industryMappingRepository.save(
        mapping(industry, ExternalDataSource.SEOUL_ESTIMATED_SALES, "CS1"));
    industryMappingRepository.save(
        mapping(industry, ExternalDataSource.KOSIS_BUSINESS_COUNT, "K1"));
    industryMappingRepository.save(
        mapping(industry, ExternalDataSource.KOSIS_BUSINESS_SURVIVAL_RATE, "K1"));
  }

  private IndustryExternalCodeMapping mapping(
      Industry industry, ExternalDataSource source, String externalCode) {
    return IndustryExternalCodeMapping.builder()
        .industry(industry)
        .source(source)
        .externalCode(externalCode)
        .externalName("테스트 업종")
        .build();
  }

  private void insertPublicData() {
    insertSales("20261", "T1", 100);
    insertSales("20262", "T1", 150);
    insertSales("20262", "T2", 450);
    jdbcTemplate.update(
        """
        INSERT INTO small_business_stores (
          store_id, commercial_area_code, store_name,
          small_industry_code, legal_dong_code
        ) VALUES ('S1', 'A1', '테스트 상가', ?, ?)
        """,
        INDUSTRY_CODE,
        REGION_CODE);
    insertKosisCount("T01", "활동기업", new BigDecimal("100"));
    insertKosisCount("T02", "신생기업", new BigDecimal("10"));
    insertKosisCount("T03", "소멸기업", new BigDecimal("5"));
    jdbcTemplate.update(
        """
        INSERT INTO kosis_business_survival_rates (
          organization_id, table_id, industry_code, industry_name,
          item_id, item_name, unit_name, period_type, reference_year, survival_rate
        ) VALUES ('101', 'SURVIVAL', 'K1', '테스트 업종',
                  'S01', '1년 생존율', '%', 'Y', 2025, 70)
        """);
    jdbcTemplate.update(
        """
        INSERT INTO seoul_sdot_foot_traffic (
          serial_number, sensing_time, autonomous_district,
          administrative_district, visitor_count
        ) VALUES ('sensor-1', TIMESTAMP '2026-08-27 10:00:00', '강남구', '역삼1동', 123)
        """);
  }

  private void insertSales(String quarter, String areaCode, long amount) {
    jdbcTemplate.update(
        """
        INSERT INTO commercial_estimated_sales (
          stdr_yyqu_cd, trdar_cd, trdar_cd_nm,
          svc_induty_cd, svc_induty_cd_nm, thsmon_selng_amt
        ) VALUES (?, ?, '테스트 상권', 'CS1', '커피 전문점', ?)
        """,
        quarter,
        areaCode,
        amount);
  }

  private void insertKosisCount(String itemId, String itemName, BigDecimal count) {
    jdbcTemplate.update(
        """
        INSERT INTO kosis_business_counts (
          organization_id, table_id, industry_code, industry_name,
          item_id, item_name, unit_name, period_type, reference_year, business_count
        ) VALUES ('101', 'COUNT', 'K1', '테스트 업종', ?, ?, '개', 'Y', 2025, ?)
        """,
        itemId,
        itemName,
        count);
  }

  private JdbcPublicDataResolver resolver() {
    return new JdbcPublicDataResolver(jdbcTemplate);
  }
}
