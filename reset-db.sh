#!/usr/bin/env bash
set -euo pipefail

# Usa o diretório onde o script está (que é a raiz do projeto)
PROJECT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
cd "$PROJECT_DIR"

COMPOSE_CMD=${COMPOSE_CMD:-docker compose}

# Derruba tudo + volumes do compose
$COMPOSE_CMD down --volumes

# ⚠️ CONFIRMAR o nome real do volume com `docker volume ls`
VOLUME_NAME="unihub_pgdata"

if docker volume inspect "$VOLUME_NAME" >/dev/null 2>&1; then
  docker volume rm "$VOLUME_NAME" >/dev/null
fi

echo "PostgreSQL data volume removed. Run '$COMPOSE_CMD up --build' to recreate the containers with a fresh schema."
