CREATE VIEW kosis_business_rates AS
SELECT
    organization_id,
    table_id,
    industry_code,
    MAX(industry_name) AS industry_name,
    reference_year,
    MAX(business_count) FILTER (WHERE item_id = 'T01') AS active_business_count,
    MAX(business_count) FILTER (WHERE item_id = 'T02') AS new_business_count,
    MAX(business_count) FILTER (WHERE item_id = 'T03') AS closed_business_count,
    ROUND(
        MAX(business_count) FILTER (WHERE item_id = 'T02')
            / NULLIF(MAX(business_count) FILTER (WHERE item_id = 'T01'), 0)
            * 100,
        3
    ) AS new_business_rate,
    ROUND(
        MAX(business_count) FILTER (WHERE item_id = 'T03')
            / NULLIF(MAX(business_count) FILTER (WHERE item_id = 'T01'), 0)
            * 100,
        3
    ) AS closure_rate
FROM kosis_business_counts
WHERE item_id IN ('T01', 'T02', 'T03')
GROUP BY organization_id, table_id, industry_code, reference_year;
