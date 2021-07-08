CREATE TABLE google_fitness_token
(
	chaster_user_id TEXT NOT NULL UNIQUE,
	token TEXT NOT NULL,
	valid_until TIMESTAMPTZ NOT NULL,
	refresh_token TEXT NOT NULL
)