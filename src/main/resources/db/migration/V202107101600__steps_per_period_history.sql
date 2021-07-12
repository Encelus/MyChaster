CREATE TABLE steps_per_period_history
(
	id UUID NOT NULL PRIMARY KEY,
	created_on TIMESTAMPTZ NOT NULL,
	modified_on TIMESTAMPTZ NOT NULL,
	optlock BIGINT DEFAULT 0 NOT NULL,
	chaster_user_id TEXT NOT NULL,
	chaster_lock_id TEXT NOT NULL,
	period_start TIMESTAMPTZ NOT NULL,
	period_end TIMESTAMPTZ NOT NULL,
	steps INTEGER NOT NULL,
	applied_punishment TEXT,
	is_final BOOL NOT NULL
);

CREATE INDEX ON steps_per_period_history (chaster_user_id);
CREATE INDEX ON steps_per_period_history (chaster_lock_id);
CREATE INDEX ON steps_per_period_history (period_start);
CREATE INDEX ON steps_per_period_history (period_end);