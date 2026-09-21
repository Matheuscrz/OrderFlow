#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname postgres \
  --set=order_password="$ORDER_DB_PASSWORD" \
  --set=processor_password="$PROCESSOR_DB_PASSWORD" <<'EOSQL'
CREATE ROLE order_service LOGIN PASSWORD :'order_password';
CREATE ROLE processor_service LOGIN PASSWORD :'processor_password';

CREATE DATABASE orderflow_order OWNER order_service;
CREATE DATABASE orderflow_processor OWNER processor_service;
EOSQL

psql -v ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname orderflow_order <<'EOSQL'
GRANT ALL ON SCHEMA public TO order_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT ALL ON TABLES TO order_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT ALL ON SEQUENCES TO order_service;
EOSQL

psql -v ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname orderflow_processor <<'EOSQL'
GRANT ALL ON SCHEMA public TO processor_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT ALL ON TABLES TO processor_service;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT ALL ON SEQUENCES TO processor_service;
EOSQL