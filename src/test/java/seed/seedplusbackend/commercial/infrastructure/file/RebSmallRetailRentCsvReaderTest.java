package seed.seedplusbackend.commercial.infrastructure.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import seed.seedplusbackend.commercial.application.result.RebSmallRetailRentFileResult;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@DisplayName("한국부동산원 소규모상가 임대료 CSV 파서")
class RebSmallRetailRentCsvReaderTest {

  private final RebSmallRetailRentCsvReader reader = new RebSmallRetailRentCsvReader();

  @Test
  @DisplayName("R-ONE의 연도별 행과 분기 열 형식을 변환한다")
  void read_convertsRoneYearRowFormat() {
    RebSmallRetailRentFileResult result = reader.read(roneCsv().getBytes(Charset.forName("MS949")));

    assertThat(result.periods()).hasSize(7);
    assertThat(result.rows()).hasSize(21);
    assertThat(result.rows())
        .anySatisfy(
            row -> {
              assertThat(row.areaPath()).isEqualTo("서울 > 도심");
              assertThat(row.referenceYear()).isEqualTo(2026);
              assertThat(row.referenceQuarter()).isEqualTo(1);
              assertThat(row.rentPerSquareMeterThousandKrw()).isEqualByComparingTo("73.6");
            });
  }

  @Test
  @DisplayName("다중 헤더와 지역 계층을 분기별 행으로 변환한다")
  void read_convertsWideFormatToLongFormat() {
    RebSmallRetailRentFileResult result = reader.read(csv().getBytes(StandardCharsets.UTF_8));

    assertThat(result.periods()).hasSize(2);
    assertThat(result.rows()).hasSize(10);
    assertThat(result.rows())
        .anySatisfy(
            row -> {
              assertThat(row.areaPath()).isEqualTo("서울 > 도심 > 남대문");
              assertThat(row.areaLevel()).isEqualTo(3);
              assertThat(row.referenceYear()).isEqualTo(2026);
              assertThat(row.referenceQuarter()).isEqualTo(1);
              assertThat(row.rentPerSquareMeterThousandKrw()).isEqualByComparingTo("77.0");
            });
  }

  @Test
  @DisplayName("MS949 CSV도 읽는다")
  void read_supportsMs949() {
    RebSmallRetailRentFileResult result = reader.read(csv().getBytes(Charset.forName("MS949")));

    assertThat(result.rows()).hasSize(10);
    assertThat(result.rows().getFirst().areaName()).isEqualTo("전국");
  }

  @Test
  @DisplayName("분기 헤더가 없으면 잘못된 파일로 처리한다")
  void read_rejectsFileWithoutPeriodHeader() {
    byte[] invalid = "No,지역,임대료\n1,전국,20.6".getBytes(StandardCharsets.UTF_8);

    assertThatThrownBy(() -> reader.read(invalid))
        .isInstanceOfSatisfying(
            ApplicationException.class,
            exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REB_RENT_FILE_INVALID));
  }

  @Test
  @DisplayName("병합 셀처럼 연도가 한 번만 나온 헤더의 후속 분기를 이어서 읽는다")
  void read_carriesMergedYearHeaderAcrossQuarterColumns() {
    String csv =
        """
        No,지역,,,2025년,,,,2026년,,,
        ,,,,1분기,2분기,3분기,4분기,1분기,2분기,3분기,4분기
        1,전국,,,20.1,20.2,20.3,20.4,21.1,21.2,21.3,21.4
        """;

    RebSmallRetailRentFileResult result = reader.read(csv.getBytes(StandardCharsets.UTF_8));

    assertThat(result.periods()).hasSize(8);
    assertThat(result.rows()).hasSize(8);
    assertThat(result.rows())
        .anySatisfy(
            row -> {
              assertThat(row.referenceYear()).isEqualTo(2026);
              assertThat(row.referenceQuarter()).isEqualTo(4);
              assertThat(row.rentPerSquareMeterThousandKrw()).isEqualByComparingTo("21.4");
            });
  }

  @Test
  @DisplayName("분기 블록이 반복되면 첫 번째 블록을 사용한다")
  void read_usesFirstRepeatedQuarterBlock() {
    String csv =
        """
        No,지역,지역,지역,항목,단위,통계자료,주기,1분기,2분기,3분기,4분기,1분기,2분기,3분기,4분기
        1,전국,전국,전국,임대료,천원/㎡,원자료,2026년,20.1,20.2,20.3,20.4,5.1,5.2,5.3,5.4
        """;

    RebSmallRetailRentFileResult result = reader.read(csv.getBytes(StandardCharsets.UTF_8));

    assertThat(result.rows()).hasSize(4);
    assertThat(result.rows().getFirst().rentPerSquareMeterThousandKrw())
        .isEqualByComparingTo("20.1");
  }

  private String csv() {
    return """
        No,지역,,,2025년 4분기,2026년 1분기
        ,,,,임대료,임대료
        ,,,,천원/㎡,천원/㎡
        1,전국,,,20.6,20.7
        2,서울,,,52.2,52.5
        3,서울,도심,,73.1,73.6
        4,,도심,광화문,91.6,92.2
        5,,,남대문,76.5,77.0
        """;
  }

  private String roneCsv() {
    return """
        No,지역,지역,지역,항목,단위,통계자료,주기,1분기,2분기,3분기,4분기
        1,전국,전국,전국,임대료,천원/㎡,원자료,2024년,,,20.7,20.7
        2,전국,전국,전국,임대료,천원/㎡,원자료,2025년,20.6,20.6,20.6,20.6
        3,전국,전국,전국,임대료,천원/㎡,원자료,2026년,20.6,,,
        4,서울,서울,서울,임대료,천원/㎡,원자료,2024년,,,51.2,51.4
        5,서울,서울,서울,임대료,천원/㎡,원자료,2025년,51.4,51.6,51.8,52.2
        6,서울,서울,서울,임대료,천원/㎡,원자료,2026년,52.5,,,
        7,서울,도심,도심,임대료,천원/㎡,원자료,2024년,,,71.6,71.8
        8,서울,도심,도심,임대료,천원/㎡,원자료,2025년,71.8,72.5,72.7,73.1
        9,서울,도심,도심,임대료,천원/㎡,원자료,2026년,73.6,,,
        """;
  }
}
