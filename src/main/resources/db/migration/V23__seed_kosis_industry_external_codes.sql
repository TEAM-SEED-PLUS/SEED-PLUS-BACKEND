-- KOSIS business-demography tables classify industries by KSIC section.
-- Every internal descendant uses the code of its matching top-level section.
WITH RECURSIVE source_data(internal_code, external_code) AS (
    VALUES
        ('G2', 'G'),
        ('I1', 'I'),
        ('I2', 'I'),
        ('L1', 'L'),
        ('M1', 'M'),
        ('N1', 'N'),
        ('P1', 'P'),
        ('Q1', 'Q'),
        ('R1', 'R'),
        ('S2', 'S')
), descendant_mappings(industry_id, external_code) AS (
    SELECT industry.industry_id, source_data.external_code
    FROM source_data
    JOIN industries industry ON industry.industry_code = source_data.internal_code
    UNION ALL
    SELECT child.industry_id, mapping.external_code
    FROM descendant_mappings mapping
    JOIN industries child ON child.parent_industry_id = mapping.industry_id
), sources(source) AS (
    VALUES
        ('KOSIS_BUSINESS_SURVIVAL_RATE'),
        ('KOSIS_BUSINESS_COUNT')
)
INSERT INTO industry_external_code_mappings (industry_id, source, external_code)
SELECT DISTINCT mapping.industry_id, sources.source, mapping.external_code
FROM descendant_mappings mapping
CROSS JOIN sources
ON CONFLICT ON CONSTRAINT uq_industry_external_code_mapping
DO NOTHING;
