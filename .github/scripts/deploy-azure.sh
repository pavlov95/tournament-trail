#!/bin/bash
set -eu

PROJECT="/home/azureuser/tournament-trail"

echo "Updating TournamentTrail source code..."
runuser -u azureuser -- git -C "$PROJECT" pull --ff-only origin main

cd "$PROJECT"

echo "Validating Docker Compose..."
docker compose -f compose.azure.yaml config --quiet

echo "Building and deploying TournamentTrail..."
docker compose -f compose.azure.yaml up -d --build --no-deps tournament-trail

echo "Checking application health..."

for attempt in $(seq 1 36); do
    if curl -sS --max-time 5 http://127.0.0.1:8080/actuator/health \
       | grep -q '"status":"UP"'; then
        echo "DEPLOYMENT_OK"
        exit 0
    fi

    sleep 5
done

echo "DEPLOYMENT_FAILED: Application did not become healthy."
exit 1