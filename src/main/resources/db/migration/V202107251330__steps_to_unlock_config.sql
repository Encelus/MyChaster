CREATE TABLE steps_to_unlock_config
(
	id UUID NOT NULL PRIMARY KEY,
	created_on TIMESTAMPTZ NOT NULL,
	modified_on TIMESTAMPTZ NOT NULL,
	optlock BIGINT DEFAULT 0 NOT NULL,
	chaster_lock_id TEXT NOT NULL UNIQUE,
	required_steps INTEGER NOT NULL,
	is_frozen BOOLEAN NOT NULL
);
