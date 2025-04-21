#!/bin/bash

# Exit if any command fails
set -e

# List your microservices here (must match folder names)
services=(
  "discovery-server"
  "api-gateway"
  "product-service"
  "order-service"
  "inventory-service"
  "notification-service"
)

echo "🛠️  Starting Jib local Docker builds for microservices..."

for service in "${services[@]}"; do
  echo "➡️  Building image for $service..."
  cd $service

  # Build image locally with a custom name (e.g., product-service)
  mvn compile jib:dockerBuild -Dimage=$service

  cd ..
  echo "✅ Finished building $service"
done

echo "🎉 All services built and available in your local Docker images!"
