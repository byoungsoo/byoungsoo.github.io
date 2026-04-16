#!/bin/bash

# Hugo Restart Script
# Enhanced version that uses the improved start and stop scripts

# Colors for output
GREEN='\033[0;32m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_status "Restarting Hugo server..."

# Stop Hugo if running
print_status "Stopping Hugo server..."
./stop_hugo.sh

# Wait a moment before starting
sleep 1

# Start Hugo
print_status "Starting Hugo server..."
./start_hugo.sh
