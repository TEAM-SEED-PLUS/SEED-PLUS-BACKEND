ALTER TABLE builder_store_bookmarks
    ADD COLUMN estimated_sales_quarter VARCHAR(5),
    ADD COLUMN estimated_sales_amount BIGINT,
    ADD COLUMN business_survival_year INTEGER,
    ADD COLUMN survival_rate DECIMAL(7, 3),
    ADD COLUMN business_count_year INTEGER,
    ADD COLUMN active_business_count DECIMAL(20, 3),
    ADD COLUMN new_business_count DECIMAL(20, 3),
    ADD COLUMN closed_business_count DECIMAL(20, 3),
    ADD COLUMN store_info_collected_at TIMESTAMPTZ,
    ADD COLUMN store_count INTEGER,
    ADD COLUMN rent_reference_year INTEGER,
    ADD COLUMN rent_reference_quarter INTEGER,
    ADD COLUMN rent_per_square_meter_thousand_krw DECIMAL(12, 3),
    ADD COLUMN data_refreshed_at TIMESTAMPTZ;

ALTER TABLE builder_store_bookmarks
    ADD CONSTRAINT chk_builder_store_bookmark_sales_quarter
        CHECK (estimated_sales_quarter IS NULL OR estimated_sales_quarter ~ '^[0-9]{4}[1-4]$'),
    ADD CONSTRAINT chk_builder_store_bookmark_survival_year
        CHECK (business_survival_year IS NULL OR business_survival_year BETWEEN 1900 AND 2200),
    ADD CONSTRAINT chk_builder_store_bookmark_count_year
        CHECK (business_count_year IS NULL OR business_count_year BETWEEN 1900 AND 2200),
    ADD CONSTRAINT chk_builder_store_bookmark_rent_period
        CHECK (
            (rent_reference_year IS NULL AND rent_reference_quarter IS NULL)
            OR (
                rent_reference_year BETWEEN 1900 AND 2200
                AND rent_reference_quarter BETWEEN 1 AND 4
            )
        );

CREATE INDEX idx_builder_store_bookmarks_user_created
    ON builder_store_bookmarks(user_id, created_at DESC);
