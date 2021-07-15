CREATE TABLE application_message
(
	id UUID NOT NULL PRIMARY KEY,
	created_on TIMESTAMPTZ NOT NULL,
	modified_on TIMESTAMPTZ NOT NULL,
	optlock BIGINT DEFAULT 0 NOT NULL,
	consumer TEXT NOT NULL,
	payload TEXT NOT NULL,
	failure TEXT
);
