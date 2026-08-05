package seed.seedplusbackend.commercial.infrastructure.file;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.commercial.application.port.RebSmallRetailRentFileReaderPort;
import seed.seedplusbackend.commercial.application.result.RebSmallRetailRentFileResult;
import seed.seedplusbackend.commercial.application.result.RebSmallRetailRentPeriod;
import seed.seedplusbackend.commercial.application.result.RebSmallRetailRentRowResult;
import seed.seedplusbackend.global.error.ApplicationException;
import seed.seedplusbackend.global.error.ErrorCode;

@Component
public class RebSmallRetailRentCsvReader implements RebSmallRetailRentFileReaderPort {

  private static final Charset MS949 = Charset.forName("MS949");
  private static final Pattern PERIOD_PATTERN = Pattern.compile("(20\\d{2})년?.*?([1-4])\\s*분기");
  private static final Pattern YEAR_PATTERN = Pattern.compile("(20\\d{2})년?");
  private static final Pattern QUARTER_PATTERN = Pattern.compile("([1-4])\\s*분기");
  private static final Pattern ROW_NUMBER_PATTERN = Pattern.compile("\\d+");

  @Override
  public RebSmallRetailRentFileResult read(byte[] fileContent) {
    try {
      String text = decode(fileContent);
      List<List<String>> records = parseCsv(text, detectDelimiter(text));
      return convert(records);
    } catch (ApplicationException exception) {
      throw exception;
    } catch (RuntimeException exception) {
      throw invalidFile(exception.getMessage(), exception);
    }
  }

  private RebSmallRetailRentFileResult convert(List<List<String>> records) {
    RebYearRowHeader yearRowHeader = findYearRowHeader(records);
    if (yearRowHeader != null) {
      return convertYearRowFormat(records, yearRowHeader);
    }
    return convertPeriodColumnFormat(records);
  }

  private RebSmallRetailRentFileResult convertYearRowFormat(
      List<List<String>> records, RebYearRowHeader header) {
    List<RebSmallRetailRentRowResult> rows = new ArrayList<>();
    Set<RebSmallRetailRentPeriod> periods = new LinkedHashSet<>();
    Set<RebMetricKey> metricKeys = new LinkedHashSet<>();

    for (int rowIndex = header.rowIndex() + 1; rowIndex < records.size(); rowIndex++) {
      List<String> record = records.get(rowIndex);
      String rowNumberValue = cell(record, header.noColumn());
      if (!ROW_NUMBER_PATTERN.matcher(rowNumberValue).matches()) {
        continue;
      }

      Matcher yearMatcher = YEAR_PATTERN.matcher(cell(record, header.yearColumn()));
      if (!yearMatcher.matches()) {
        throw invalidFile("연도를 확인할 수 없는 행이 있습니다. row=" + rowNumberValue, null);
      }
      int year = Integer.parseInt(yearMatcher.group(1));
      List<String> pathParts = collapseRepeatedAreaNames(record, header.regionColumns());
      if (pathParts.isEmpty()) {
        throw invalidFile("지역명이 없는 행이 있습니다. row=" + rowNumberValue, null);
      }

      int sourceRowNumber = Integer.parseInt(rowNumberValue);
      String areaPath = String.join(" > ", pathParts);
      String sourceAreaKey = sha256(areaPath);
      String areaName = pathParts.getLast();

      for (Map.Entry<Integer, Integer> entry : header.quarterColumns().entrySet()) {
        int quarter = entry.getKey();
        BigDecimal rent = parseRent(cell(record, entry.getValue()), sourceRowNumber);
        if (rent == null) {
          continue;
        }

        RebSmallRetailRentPeriod period = new RebSmallRetailRentPeriod(year, quarter);
        RebMetricKey metricKey = new RebMetricKey(sourceAreaKey, year, quarter);
        if (!metricKeys.add(metricKey)) {
          throw invalidFile("중복된 지역·분기 데이터가 있습니다. path=" + areaPath, null);
        }
        periods.add(period);
        rows.add(
            new RebSmallRetailRentRowResult(
                sourceAreaKey,
                sourceRowNumber,
                areaName,
                areaPath,
                pathParts.size(),
                year,
                quarter,
                rent));
      }
    }

    validateParsedData(periods, rows);
    return new RebSmallRetailRentFileResult(List.copyOf(periods), List.copyOf(rows));
  }

  private RebSmallRetailRentFileResult convertPeriodColumnFormat(List<List<String>> records) {
    int noColumn = findNoColumn(records);
    int firstDataRow = findFirstDataRow(records, noColumn);
    Map<RebSmallRetailRentPeriod, Integer> periodColumns =
        findPeriodColumns(records.subList(0, firstDataRow));

    if (periodColumns.isEmpty()) {
      throw invalidFile("연도·분기 열을 찾을 수 없습니다.", null);
    }

    int firstPeriodColumn = periodColumns.values().stream().min(Integer::compareTo).orElseThrow();
    if (firstPeriodColumn <= noColumn + 1) {
      throw invalidFile("지역 계층 열을 찾을 수 없습니다.", null);
    }

    List<String> areaContext = new ArrayList<>();
    for (int index = noColumn + 1; index < firstPeriodColumn; index++) {
      areaContext.add("");
    }

    List<RebSmallRetailRentRowResult> rows = new ArrayList<>();
    Set<String> areaPaths = new LinkedHashSet<>();
    for (int rowIndex = firstDataRow; rowIndex < records.size(); rowIndex++) {
      List<String> record = records.get(rowIndex);
      String rowNumberValue = cell(record, noColumn);
      if (!ROW_NUMBER_PATTERN.matcher(rowNumberValue).matches()) {
        continue;
      }

      updateAreaContext(areaContext, record, noColumn + 1);
      List<String> pathParts = areaContext.stream().filter(value -> !value.isBlank()).toList();
      if (pathParts.isEmpty()) {
        throw invalidFile("지역명이 없는 행이 있습니다. row=" + rowNumberValue, null);
      }

      String areaPath = String.join(" > ", pathParts);
      if (!areaPaths.add(areaPath)) {
        throw invalidFile("중복된 지역 경로가 있습니다. path=" + areaPath, null);
      }

      int sourceRowNumber = Integer.parseInt(rowNumberValue);
      String areaName = pathParts.getLast();
      String sourceAreaKey = sha256(areaPath);
      for (Map.Entry<RebSmallRetailRentPeriod, Integer> entry : periodColumns.entrySet()) {
        BigDecimal rent = parseRent(cell(record, entry.getValue()), sourceRowNumber);
        if (rent == null) {
          continue;
        }
        RebSmallRetailRentPeriod period = entry.getKey();
        rows.add(
            new RebSmallRetailRentRowResult(
                sourceAreaKey,
                sourceRowNumber,
                areaName,
                areaPath,
                pathParts.size(),
                period.year(),
                period.quarter(),
                rent));
      }
    }

    if (areaPaths.isEmpty() || rows.isEmpty()) {
      throw invalidFile("적재할 임대료 데이터가 없습니다.", null);
    }

    Set<RebSmallRetailRentPeriod> periodsWithData = new LinkedHashSet<>();
    for (RebSmallRetailRentRowResult row : rows) {
      periodsWithData.add(
          new RebSmallRetailRentPeriod(row.referenceYear(), row.referenceQuarter()));
    }
    if (!periodsWithData.containsAll(periodColumns.keySet())) {
      throw invalidFile("임대료 값이 하나도 없는 분기 열이 있습니다.", null);
    }

    return new RebSmallRetailRentFileResult(List.copyOf(periodColumns.keySet()), List.copyOf(rows));
  }

  private RebYearRowHeader findYearRowHeader(List<List<String>> records) {
    for (int rowIndex = 0; rowIndex < Math.min(records.size(), 10); rowIndex++) {
      List<String> record = records.get(rowIndex);
      int noColumn = -1;
      int yearColumn = -1;
      List<Integer> regionColumns = new ArrayList<>();
      Map<Integer, Integer> quarterColumns = new LinkedHashMap<>();

      for (int column = 0; column < record.size(); column++) {
        String value = cell(record, column);
        if ("no".equalsIgnoreCase(value)) {
          noColumn = column;
        } else if ("지역".equals(value)) {
          regionColumns.add(column);
        } else if ("주기".equals(value)) {
          yearColumn = column;
        } else {
          Matcher quarterMatcher = QUARTER_PATTERN.matcher(value);
          if (quarterMatcher.matches()) {
            quarterColumns.put(Integer.parseInt(quarterMatcher.group(1)), column);
          }
        }
      }

      if (noColumn >= 0
          && yearColumn >= 0
          && !regionColumns.isEmpty()
          && !quarterColumns.isEmpty()) {
        return new RebYearRowHeader(
            rowIndex, noColumn, yearColumn, List.copyOf(regionColumns), quarterColumns);
      }
    }
    return null;
  }

  private List<String> collapseRepeatedAreaNames(List<String> record, List<Integer> regionColumns) {
    List<String> pathParts = new ArrayList<>();
    for (int regionColumn : regionColumns) {
      String value = cell(record, regionColumn);
      if (!value.isBlank() && (pathParts.isEmpty() || !pathParts.getLast().equals(value))) {
        pathParts.add(value);
      }
    }
    return List.copyOf(pathParts);
  }

  private void validateParsedData(
      Set<RebSmallRetailRentPeriod> periods, List<RebSmallRetailRentRowResult> rows) {
    if (periods.isEmpty() || rows.isEmpty()) {
      throw invalidFile("적재할 임대료 데이터가 없습니다.", null);
    }
  }

  private int findNoColumn(List<List<String>> records) {
    for (List<String> record : records) {
      for (int column = 0; column < record.size(); column++) {
        if ("no".equalsIgnoreCase(normalize(record.get(column)))) {
          return column;
        }
      }
    }
    throw invalidFile("No 열을 찾을 수 없습니다.", null);
  }

  private int findFirstDataRow(List<List<String>> records, int noColumn) {
    for (int row = 0; row < records.size(); row++) {
      if (ROW_NUMBER_PATTERN.matcher(cell(records.get(row), noColumn)).matches()) {
        return row;
      }
    }
    throw invalidFile("데이터 행을 찾을 수 없습니다.", null);
  }

  private Map<RebSmallRetailRentPeriod, Integer> findPeriodColumns(List<List<String>> headerRows) {
    int columnCount = headerRows.stream().mapToInt(List::size).max().orElse(0);
    Map<RebSmallRetailRentPeriod, Integer> columns = new LinkedHashMap<>();
    Integer currentYear = null;
    for (int column = 0; column < columnCount; column++) {
      StringBuilder header = new StringBuilder();
      for (List<String> row : headerRows) {
        String value = cell(row, column);
        if (!value.isBlank() && header.indexOf(value) < 0) {
          header.append(' ').append(value);
        }
      }

      Matcher yearMatcher = YEAR_PATTERN.matcher(header);
      if (yearMatcher.find()) {
        currentYear = Integer.parseInt(yearMatcher.group(1));
      }

      Matcher matcher = PERIOD_PATTERN.matcher(header);
      if (matcher.find()) {
        RebSmallRetailRentPeriod period =
            new RebSmallRetailRentPeriod(
                Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
        columns.putIfAbsent(period, column);
        continue;
      }

      Matcher quarterMatcher = QUARTER_PATTERN.matcher(header);
      if (currentYear != null && quarterMatcher.find()) {
        RebSmallRetailRentPeriod period =
            new RebSmallRetailRentPeriod(currentYear, Integer.parseInt(quarterMatcher.group(1)));
        columns.putIfAbsent(period, column);
      }
    }
    return columns;
  }

  private void updateAreaContext(
      List<String> areaContext, List<String> record, int firstAreaColumn) {
    int firstChangedLevel = -1;
    for (int level = 0; level < areaContext.size(); level++) {
      String value = cell(record, firstAreaColumn + level);
      if (value.isBlank()) {
        continue;
      }
      if (firstChangedLevel < 0) {
        firstChangedLevel = level;
        for (int clearLevel = level; clearLevel < areaContext.size(); clearLevel++) {
          areaContext.set(clearLevel, "");
        }
      }
      areaContext.set(level, value);
    }
  }

  private BigDecimal parseRent(String value, int rowNumber) {
    if (value.isBlank() || "-".equals(value)) {
      return null;
    }
    try {
      return new BigDecimal(value.replace(",", ""));
    } catch (NumberFormatException exception) {
      throw invalidFile("임대료 값이 숫자가 아닙니다. row=" + rowNumber + ", value=" + value, exception);
    }
  }

  private String decode(byte[] content) {
    try {
      return StandardCharsets.UTF_8
          .newDecoder()
          .onMalformedInput(CodingErrorAction.REPORT)
          .onUnmappableCharacter(CodingErrorAction.REPORT)
          .decode(ByteBuffer.wrap(content))
          .toString()
          .replace("\uFEFF", "");
    } catch (CharacterCodingException exception) {
      return MS949.decode(ByteBuffer.wrap(content)).toString().replace("\uFEFF", "");
    }
  }

  private char detectDelimiter(String text) {
    int commaCount = 0;
    int tabCount = 0;
    for (String line : text.lines().limit(10).toList()) {
      commaCount += count(line, ',');
      tabCount += count(line, '\t');
    }
    return tabCount > commaCount ? '\t' : ',';
  }

  private int count(String value, char target) {
    int count = 0;
    for (int index = 0; index < value.length(); index++) {
      if (value.charAt(index) == target) {
        count++;
      }
    }
    return count;
  }

  private List<List<String>> parseCsv(String text, char delimiter) {
    try (Reader input =
            new InputStreamReader(
                new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8);
        PushbackReader reader = new PushbackReader(input)) {
      List<List<String>> records = new ArrayList<>();
      List<String> record = new ArrayList<>();
      StringBuilder cell = new StringBuilder();
      boolean quoted = false;
      int read;
      while ((read = reader.read()) != -1) {
        char current = (char) read;
        if (current == '"') {
          if (quoted) {
            int next = reader.read();
            if (next == '"') {
              cell.append('"');
            } else {
              quoted = false;
              if (next != -1) {
                reader.unread(next);
              }
            }
          } else if (cell.isEmpty()) {
            quoted = true;
          } else {
            cell.append(current);
          }
        } else if (current == delimiter && !quoted) {
          record.add(normalize(cell.toString()));
          cell.setLength(0);
        } else if ((current == '\n' || current == '\r') && !quoted) {
          if (current == '\r') {
            int next = reader.read();
            if (next != '\n' && next != -1) {
              reader.unread(next);
            }
          }
          record.add(normalize(cell.toString()));
          cell.setLength(0);
          if (record.stream().anyMatch(value -> !value.isBlank())) {
            records.add(List.copyOf(record));
          }
          record = new ArrayList<>();
        } else {
          cell.append(current);
        }
      }
      if (quoted) {
        throw invalidFile("닫히지 않은 따옴표가 있습니다.", null);
      }
      if (!cell.isEmpty() || !record.isEmpty()) {
        record.add(normalize(cell.toString()));
        records.add(List.copyOf(record));
      }
      return records;
    } catch (IOException exception) {
      throw invalidFile("CSV 파일을 읽을 수 없습니다.", exception);
    }
  }

  private String cell(List<String> record, int column) {
    return column < record.size() ? normalize(record.get(column)) : "";
  }

  private String normalize(String value) {
    return value == null ? "" : value.strip().replaceAll("\\s+", " ");
  }

  private String sha256(String value) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is not available", exception);
    }
  }

  private ApplicationException invalidFile(String detail, Throwable cause) {
    return new ApplicationException(ErrorCode.REB_RENT_FILE_INVALID, detail, cause);
  }

  private record RebYearRowHeader(
      int rowIndex,
      int noColumn,
      int yearColumn,
      List<Integer> regionColumns,
      Map<Integer, Integer> quarterColumns) {}

  private record RebMetricKey(String sourceAreaKey, int year, int quarter) {}
}
