#!/usr/bin/env bash
# One-command setup: builds and starts the ERP with PostgreSQL in Docker,
# then creates the basic users and starter data. Safe to run again.
set -euo pipefail
cd "$(dirname "$0")"

command -v docker >/dev/null || { echo "Docker is required: https://docs.docker.com/get-docker/"; exit 1; }
docker info >/dev/null 2>&1 || { echo "Docker is installed but not running. Start Docker Desktop and retry."; exit 1; }

if [ ! -f .env ]; then
  cp .env.example .env
  secret=$( (openssl rand -hex 32 2>/dev/null) || head -c 48 /dev/urandom | base64 | tr -d '\n=+/' )
  sed -i.bak "s|^JWT_SECRET=.*|JWT_SECRET=${secret}|" .env && rm -f .env.bak
  echo "Created .env (generated JWT secret)."
fi
set -a; . ./.env; set +a

docker compose up -d --build

BASE="http://localhost:${WEB_PORT:-8088}/api"
JSON='Content-Type: application/json'

echo -n "Waiting for the ERP to start"
for _ in $(seq 1 90); do
  code=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$BASE/auth/login" -H "$JSON" \
    -d "{\"username\":\"$ERP_ADMIN_USERNAME\",\"password\":\"$ERP_ADMIN_PASSWORD\"}" || true)
  [ "$code" = "200" ] && break
  echo -n "."; sleep 2
done
echo
[ "$code" = "200" ] || { echo "ERP did not become ready. Check: docker compose logs backend"; exit 1; }

TOKEN=$(curl -s -X POST "$BASE/auth/login" -H "$JSON" \
  -d "{\"username\":\"$ERP_ADMIN_USERNAME\",\"password\":\"$ERP_ADMIN_PASSWORD\"}" \
  | sed 's/.*"token":"\([^"]*\)".*/\1/')
AUTH="Authorization: Bearer $TOKEN"

get()  { curl -s "$BASE/$1" -H "$AUTH"; }
post() { curl -s -f -X POST "$BASE/$1" -H "$JSON" -H "$AUTH" -d "$2" >/dev/null; }

# Users (the CEO admin already exists from ERP_ADMIN_* on first start)
users=$(get users)
add_user() {
  case "$users" in *"\"username\":\"$1\""*) return;; esac
  post users "{\"username\":\"$1\",\"password\":\"$2\",\"fullName\":\"$3\",\"role\":\"$4\"}"
  echo "  user $1 ($4) password: $2"
}
echo "Users:"
add_user manager    'Manager123!' 'Sales Manager'    MANAGER
add_user accountant 'Account123!' 'Chief Accountant' ACCOUNTANT

# Starter data, only into an empty database
if [ "$(get partners)" = "[]" ]; then
  post partners '{"name":"Silk Road Trading","type":"CUSTOMER","contactPerson":"Aziz Karimov","phone":"+998901234567","address":"Tashkent"}'
  post partners '{"name":"Samarkand Foods","type":"CUSTOMER","contactPerson":"Dilya Rashidova","phone":"+998907654321","address":"Samarkand"}'
  post partners '{"name":"Fergana Supplies","type":"SUPPLIER","contactPerson":"Bobur Aliyev","phone":"+998935551122","address":"Fergana"}'
  echo "Created starter partners."
fi
if [ "$(get inventory/products)" = "[]" ]; then
  post inventory/products '{"code":"RICE-01","name":"Rice 25kg","unit":"KG","salePrice":18000}'
  post inventory/products '{"code":"OIL-01","name":"Sunflower oil 1L","unit":"PCS","salePrice":22000}'
  post inventory/adjustments '{"productId":1,"quantity":500,"unitCost":13000,"reason":"Opening stock"}'
  post inventory/adjustments '{"productId":2,"quantity":200,"unitCost":17000,"reason":"Opening stock"}'
  echo "Created starter products with opening stock."
fi

echo
echo "ERP is running at http://localhost:${WEB_PORT:-8088}"
echo "CEO login: $ERP_ADMIN_USERNAME / $ERP_ADMIN_PASSWORD"
echo "Stop: docker compose down   |   Reset all data: docker compose down -v"
