-- Keep only the newest RUNNING attempt for each target before adding the unique guard.
WITH ranked_running AS (
    SELECT commercial_data_collect_history_id,
           ROW_NUMBER() OVER (
               PARTITION BY data_type, target_key
               ORDER BY started_at DESC, commercial_data_collect_history_id DESC
           ) AS row_number
    FROM commercial_data_collect_histories
    WHERE status = 'RUNNING'
)
UPDATE commercial_data_collect_histories history
SET status = 'FAILED',
    error_message = 'Duplicate RUNNING history cleaned up while adding the collection guard.',
    finished_at = now(),
    updated_at = now()
FROM ranked_running ranked
WHERE history.commercial_data_collect_history_id = ranked.commercial_data_collect_history_id
  AND ranked.row_number > 1;

CREATE UNIQUE INDEX uq_commercial_data_collect_histories_running
    ON commercial_data_collect_histories(data_type, target_key)
    WHERE status = 'RUNNING';
