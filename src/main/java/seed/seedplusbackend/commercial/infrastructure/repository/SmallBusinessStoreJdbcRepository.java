package seed.seedplusbackend.commercial.infrastructure.repository;

import java.sql.Types;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import seed.seedplusbackend.commercial.application.port.SmallBusinessStoreStorePort;
import seed.seedplusbackend.commercial.application.result.SmallBusinessStoreRowResult;

@Repository
@RequiredArgsConstructor
public class SmallBusinessStoreJdbcRepository implements SmallBusinessStoreStorePort {

  private final JdbcTemplate jdbcTemplate;

  @Override
  public void upsertAll(String commercialAreaCode, List<SmallBusinessStoreRowResult> rows) {
    if (rows == null || rows.isEmpty()) {
      return;
    }

    String sql =
        """
        INSERT INTO small_business_stores (
          store_id, commercial_area_code, store_name, branch_name,
          large_industry_code, large_industry_name,
          medium_industry_code, medium_industry_name,
          small_industry_code, small_industry_name,
          standard_industry_code, standard_industry_name,
          sido_code, sido_name, sigungu_code, sigungu_name,
          administrative_dong_code, administrative_dong_name,
          legal_dong_code, legal_dong_name,
          lot_number_address, road_name_address,
          building_management_number, building_name, floor_number, room_number,
          longitude, latitude, collected_at, updated_at
        ) VALUES (
          ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
          ?, ?, ?, ?, ?, ?, ?, ?, now(), now()
        )
        ON CONFLICT (store_id) DO UPDATE SET
          commercial_area_code = EXCLUDED.commercial_area_code,
          store_name = EXCLUDED.store_name,
          branch_name = EXCLUDED.branch_name,
          large_industry_code = EXCLUDED.large_industry_code,
          large_industry_name = EXCLUDED.large_industry_name,
          medium_industry_code = EXCLUDED.medium_industry_code,
          medium_industry_name = EXCLUDED.medium_industry_name,
          small_industry_code = EXCLUDED.small_industry_code,
          small_industry_name = EXCLUDED.small_industry_name,
          standard_industry_code = EXCLUDED.standard_industry_code,
          standard_industry_name = EXCLUDED.standard_industry_name,
          sido_code = EXCLUDED.sido_code,
          sido_name = EXCLUDED.sido_name,
          sigungu_code = EXCLUDED.sigungu_code,
          sigungu_name = EXCLUDED.sigungu_name,
          administrative_dong_code = EXCLUDED.administrative_dong_code,
          administrative_dong_name = EXCLUDED.administrative_dong_name,
          legal_dong_code = EXCLUDED.legal_dong_code,
          legal_dong_name = EXCLUDED.legal_dong_name,
          lot_number_address = EXCLUDED.lot_number_address,
          road_name_address = EXCLUDED.road_name_address,
          building_management_number = EXCLUDED.building_management_number,
          building_name = EXCLUDED.building_name,
          floor_number = EXCLUDED.floor_number,
          room_number = EXCLUDED.room_number,
          longitude = EXCLUDED.longitude,
          latitude = EXCLUDED.latitude,
          collected_at = now(),
          updated_at = now()
        """;

    jdbcTemplate.batchUpdate(
        sql,
        rows,
        500,
        (statement, row) -> {
          int index = 1;
          statement.setString(index++, row.storeId());
          statement.setString(index++, commercialAreaCode);
          statement.setString(index++, row.storeName());
          statement.setString(index++, row.branchName());
          statement.setString(index++, row.largeIndustryCode());
          statement.setString(index++, row.largeIndustryName());
          statement.setString(index++, row.mediumIndustryCode());
          statement.setString(index++, row.mediumIndustryName());
          statement.setString(index++, row.smallIndustryCode());
          statement.setString(index++, row.smallIndustryName());
          statement.setString(index++, row.standardIndustryCode());
          statement.setString(index++, row.standardIndustryName());
          statement.setString(index++, row.sidoCode());
          statement.setString(index++, row.sidoName());
          statement.setString(index++, row.sigunguCode());
          statement.setString(index++, row.sigunguName());
          statement.setString(index++, row.administrativeDongCode());
          statement.setString(index++, row.administrativeDongName());
          statement.setString(index++, row.legalDongCode());
          statement.setString(index++, row.legalDongName());
          statement.setString(index++, row.lotNumberAddress());
          statement.setString(index++, row.roadNameAddress());
          statement.setString(index++, row.buildingManagementNumber());
          statement.setString(index++, row.buildingName());
          statement.setString(index++, row.floorNumber());
          statement.setString(index++, row.roomNumber());
          statement.setObject(index++, row.longitude(), Types.DECIMAL);
          statement.setObject(index, row.latitude(), Types.DECIMAL);
        });
  }
}
