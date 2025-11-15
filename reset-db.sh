#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
cd "$PROJECT_DIR"

COMPOSE_CMD=${COMPOSE_CMD:-docker compose}

# Stop containers and remove associated volumes to ensure database is recreated.
$COMPOSE_CMD down --volumes

# Make sure the named volume is gone so Postgres starts with a clean data directory.
VOLUME_NAME="unihub_pgdata"
if docker volume inspect "$VOLUME_NAME" >/dev/null 2>&1; then
  docker volume rm "$VOLUME_NAME" >/dev/null
fi

echo "PostgreSQL data volume removed. Run '$COMPOSE_CMD up --build' to recreate the containers with a fresh schema."