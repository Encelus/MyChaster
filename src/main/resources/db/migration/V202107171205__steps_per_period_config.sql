CREATE TABLE steps_per_period_config
(
	id UUID NOT NULL PRIMARY KEY,
	created_on TIMESTAMPTZ NOT NULL,
	modified_on TIMESTAMPTZ NOT NULL,
	optlock BIGINT DEFAULT 0 NOT NULL,
	chaster_lock_id TEXT NOT NULL UNIQUE,
	period TEXT NOT NULL,
	required_steps INTEGER NOT NULL,
	penalty TEXT NOT NULL
);
