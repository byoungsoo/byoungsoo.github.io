#!/bin/bash

# Jekyll Restart Script
# Enhanced version that uses the improved start and stop scripts

# Colors for output
GREEN='\033[0;32m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_status "Restarting Jekyll server..."

# Stop Jekyll if running
print_status "Stopping Jekyll server..."
./stop_jekyll.sh

# Wait a moment before starting
sleep 1

# Start Jekyll
print_status "Starting Jekyll server..."
./start_jekyll.sh
