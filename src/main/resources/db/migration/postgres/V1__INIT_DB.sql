CREATE TABLE account
(
    id                 SERIAL PRIMARY KEY,
    currency_shortname VARCHAR(3)               NOT NULL,
    balance            DECIMAL(18, 2)           NOT NULL DEFAULT 0,
    datetime           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_datetime   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE account_limit
(
    id                 SERIAL PRIMARY KEY,
    account            INTEGER                  NOT NULL,
    expense_category   VARCHAR(50)              NOT NULL,
    sum                DECIMAL(18, 2)           NOT NULL DEFAULT 1000.00 CHECK (sum > 0),
    currency_shortname VARCHAR(3)               NOT NULL DEFAULT 'USD',
    datetime           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account) REFERENCES account (id)
);

CREATE INDEX account_on_limit_account_idx
    ON account_limit (account);

CREATE TABLE transactional_operation
(
    id                      SERIAL PRIMARY KEY,
    account_from            INTEGER                  NOT NULL,
    account_to              INTEGER                  NOT NULL,
    corresponding_limit     INTEGER                  NOT NULL,
    currency_shortname      VARCHAR(3)               NOT NULL,
    sum                     DECIMAL(18, 2)           NOT NULL,
    expense_category        VARCHAR(50)              NOT NULL,
    datetime                TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    limit_exceeded          BOOLEAN DEFAULT FALSE,
    remaining_monthly_limit DECIMAL(18, 2)           NOT NULL,
    FOREIGN KEY (account_from) REFERENCES account (id),
    FOREIGN KEY (account_to) REFERENCES account (id),
    FOREIGN KEY (corresponding_limit) REFERENCES account_limit (id)
);

CREATE INDEX accounts_on_transactional_operation_idx
    ON transactional_operation (account_from, account_to);
CREATE INDEX corresponding_limit_on_transactional_operation_idx
    ON transactional_operation (corresponding_limit);

CREATE OR REPLACE FUNCTION update_account_modified_at()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE account
    SET updated_at = now()::TIMESTAMP
    WHERE id = NEW.id;
END;
$$;
CREATE OR REPLACE TRIGGER on_update_account
    AFTER UPDATE
    ON account
    FOR EACH ROW
    WHEN (pg_trigger_depth() = 1)
EXECUTE PROCEDURE update_account_modified_at();