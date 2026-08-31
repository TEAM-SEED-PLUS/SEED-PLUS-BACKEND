-- Crosswalk source: Seoul 100 neighborhood-business industry classification (version 3)
-- https://golmok.seoul.go.kr/images/100_v3.pdf
-- Only classifications with matching business scopes are included.
WITH RECURSIVE source_data(internal_code, external_code) AS (
    VALUES
        ('G20404', 'CS300001'),
        ('G20405', 'CS300002'),
        ('G20503', 'CS300007'),
        ('G20505', 'CS300008'),
        ('G20506', 'CS300009'),
        ('G20509', 'CS300010'),
        ('G20902', 'CS300011'),
        ('G20910', 'CS300014'),
        ('G21501', 'CS300018'),
        ('G21503', 'CS300022'),
        ('I10102', 'CS200034'),
        ('I10103', 'CS200035'),
        ('I20101', 'CS100001'),
        ('I20102', 'CS100001'),
        ('I20107', 'CS100001'),
        ('I20110', 'CS100001'),
        ('I20111', 'CS100001'),
        ('I20201', 'CS100002'),
        ('I20301', 'CS100003'),
        ('I20402', 'CS100004'),
        ('I21001', 'CS100005'),
        ('I21006', 'CS100007'),
        ('I21007', 'CS100008'),
        ('I21103', 'CS100009'),
        ('I21104', 'CS100009'),
        ('I21201', 'CS100010'),
        ('L10203', 'CS200033'),
        ('M10301', 'CS200010'),
        ('M10402', 'CS200015'),
        ('M11101', 'CS200009'),
        ('P10501', 'CS200001'),
        ('P10601', 'CS200005'),
        ('P10603', 'CS200005'),
        ('P10609', 'CS200003'),
        ('P10611', 'CS200003'),
        ('P10615', 'CS200002'),
        ('P10627', 'CS200004'),
        ('Q10201', 'CS200006'),
        ('Q10204', 'CS200006'),
        ('Q10210', 'CS200007'),
        ('Q10211', 'CS200008'),
        ('R10202', 'CS200038'),
        ('R10307', 'CS200024'),
        ('R10310', 'CS200016'),
        ('R10311', 'CS200017'),
        ('R10406', 'CS200019'),
        ('R10407', 'CS200037'),
        ('S20301', 'CS200025'),
        ('S20302', 'CS200026'),
        ('S20701', 'CS200028'),
        ('S20702', 'CS200030'),
        ('S20703', 'CS200029'),
        ('S20901', 'CS200031'),
        ('S20902', 'CS200031')
), resolved_mappings AS (
    SELECT industry.industry_id,
           industry.parent_industry_id,
           source_data.external_code
    FROM source_data
    JOIN industries industry ON industry.industry_code = source_data.internal_code
), ancestor_mappings(industry_id, parent_industry_id, external_code) AS (
    SELECT industry_id, parent_industry_id, external_code
    FROM resolved_mappings
    UNION ALL
    SELECT parent.industry_id, parent.parent_industry_id, mapping.external_code
    FROM ancestor_mappings mapping
    JOIN industries parent ON parent.industry_id = mapping.parent_industry_id
)
INSERT INTO industry_external_code_mappings (industry_id, source, external_code)
SELECT DISTINCT industry_id, 'SEOUL_ESTIMATED_SALES', external_code
FROM ancestor_mappings
ON CONFLICT ON CONSTRAINT uq_industry_external_code_mapping
DO NOTHING;
