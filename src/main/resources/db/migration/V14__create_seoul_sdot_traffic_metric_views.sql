CREATE VIEW seoul_sdot_latest_sensor_traffic AS
SELECT DISTINCT ON (serial_number)
    serial_number,
    model_name,
    sensing_time,
    region_type,
    autonomous_district,
    administrative_district,
    visitor_count,
    registered_at
FROM seoul_sdot_foot_traffic
ORDER BY serial_number, sensing_time DESC;

CREATE VIEW seoul_sdot_district_traffic_metrics AS
SELECT
    autonomous_district,
    administrative_district,
    COUNT(*) AS sensor_count,
    SUM(visitor_count) AS total_visitor_count,
    ROUND(AVG(visitor_count), 3) AS average_visitor_count,
    MIN(sensing_time) AS oldest_sensing_time,
    MAX(sensing_time) AS latest_sensing_time
FROM seoul_sdot_latest_sensor_traffic
WHERE autonomous_district IS NOT NULL
  AND autonomous_district <> ''
  AND administrative_district IS NOT NULL
  AND administrative_district <> ''
GROUP BY autonomous_district, administrative_district;
