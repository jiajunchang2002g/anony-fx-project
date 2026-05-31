create table users (
    id uuid primary key,
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    role varchar(20) not null,
    status varchar(20) not null,
    created_at timestamp with time zone not null
);

create table wallets (
    id uuid primary key,
    user_id uuid not null references users(id),
    currency varchar(3) not null,
    balance numeric(24,6) not null,
    version bigint not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint uk_wallet_user_currency unique (user_id, currency)
);
create index idx_wallets_user_id on wallets(user_id);
create index idx_wallets_currency on wallets(currency);

create table transfers (
    id uuid primary key,
    idempotency_key varchar(100) not null unique,
    requested_by_user_id uuid not null references users(id),
    source_wallet_id uuid not null references wallets(id),
    target_wallet_id uuid not null references wallets(id),
    source_currency varchar(3) not null,
    target_currency varchar(3) not null,
    source_amount numeric(24,6) not null,
    target_amount numeric(24,6) not null,
    fx_rate numeric(24,10) not null,
    status varchar(20) not null,
    failure_reason varchar(500),
    created_at timestamp with time zone not null,
    completed_at timestamp with time zone
);
create index idx_transfers_requested_by on transfers(requested_by_user_id);
create index idx_transfers_source_wallet on transfers(source_wallet_id);
create index idx_transfers_target_wallet on transfers(target_wallet_id);

create table ledger_entries (
    id uuid primary key,
    transfer_id uuid not null references transfers(id),
    wallet_id uuid not null references wallets(id),
    direction varchar(20) not null,
    currency varchar(3) not null,
    amount numeric(24,6) not null,
    balance_after numeric(24,6) not null,
    description varchar(500) not null,
    created_at timestamp with time zone not null
);
create index idx_ledger_entries_wallet_id on ledger_entries(wallet_id);
create index idx_ledger_entries_transfer_id on ledger_entries(transfer_id);
create index idx_ledger_entries_created_at on ledger_entries(created_at);

create table fx_rates (
    id uuid primary key,
    base_currency varchar(3) not null,
    quote_currency varchar(3) not null,
    rate numeric(24,10) not null,
    source varchar(100) not null,
    fetched_at timestamp with time zone not null,
    expires_at timestamp with time zone not null,
    constraint uk_fx_rate_pair unique (base_currency, quote_currency)
);
create index idx_fx_rates_base_currency on fx_rates(base_currency);

create table bank_requests (
    id uuid primary key,
    partner varchar(20) not null,
    request_type varchar(100) not null,
    payload text not null,
    signature varchar(255) not null,
    status varchar(20) not null,
    response_body text,
    created_at timestamp with time zone not null
);
create index idx_bank_requests_partner on bank_requests(partner);
create index idx_bank_requests_created_at on bank_requests(created_at);
