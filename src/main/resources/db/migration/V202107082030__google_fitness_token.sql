CREATE TABLE google_fitness_token
(
	id UUID NOT NULL PRIMARY KEY,
	created_on TIMESTAMPTZ NOT NULL,
	modified_on TIMESTAMPTZ NOT NULL,
	optlock BIGINT DEFAULT 0 NOT NULL,
	chaster_user_id TEXT NOT NULL UNIQUE,
	token TEXT NOT NULL,
	valid_until TIMESTAMPTZ NOT NULL,
	refresh_token TEXT
)