#!/bin/bash
set -eu

escape_sql() {
    printf '%s' "$1" | sed "s/'/''/g"
}

CLIENTS_PASSWORD=$(escape_sql "$DB_CLIENTS_PASSWORD")
ABONNEMENTS_PASSWORD=$(escape_sql "$DB_ABONNEMENTS_PASSWORD")
FACTURATION_PASSWORD=$(escape_sql "$DB_FACTURATION_PASSWORD")
NOTIFICATIONS_PASSWORD=$(escape_sql "$DB_NOTIFICATIONS_PASSWORD")

mysql --protocol=socket -uroot -p"$MYSQL_ROOT_PASSWORD" <<EOSQL
CREATE DATABASE IF NOT EXISTS billing_clients CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'billing_clients'@'%' IDENTIFIED BY '${CLIENTS_PASSWORD}';
GRANT ALL PRIVILEGES ON billing_clients.* TO 'billing_clients'@'%';

CREATE DATABASE IF NOT EXISTS billing_abonnements CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'billing_abonnements'@'%' IDENTIFIED BY '${ABONNEMENTS_PASSWORD}';
GRANT ALL PRIVILEGES ON billing_abonnements.* TO 'billing_abonnements'@'%';

CREATE DATABASE IF NOT EXISTS billing_facturation CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'billing_facturation'@'%' IDENTIFIED BY '${FACTURATION_PASSWORD}';
GRANT ALL PRIVILEGES ON billing_facturation.* TO 'billing_facturation'@'%';

CREATE DATABASE IF NOT EXISTS billing_notifications CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'billing_notifications'@'%' IDENTIFIED BY '${NOTIFICATIONS_PASSWORD}';
GRANT ALL PRIVILEGES ON billing_notifications.* TO 'billing_notifications'@'%';

FLUSH PRIVILEGES;
EOSQL
