#!/bin/bash

# Hugo Start Script
# Enhanced version with better error handling and logging

set -e  # Exit on any error

# Configuration
HUGO_LOG="/tmp/hugo.log"
HUGO_PID_FILE="/tmp/hugo.pid"
HUGO_PORT=1313
HUGO_HOST="0.0.0.0"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Hugo is already running
if [ -f "$HUGO_PID_FILE" ]; then
    PID=$(cat "$HUGO_PID_FILE")
    if ps -p "$PID" > /dev/null 2>&1; then
        print_warning "Hugo is already running (PID: $PID)"
        print_status "You can access your site at: http://localhost:$HUGO_PORT"
        exit 0
    else
        print_warning "Stale PID file found, removing..."
        rm -f "$HUGO_PID_FILE"
    fi
fi

# Check if port is already in use
if lsof -Pi :$HUGO_PORT -sTCP:LISTEN -t >/dev/null 2>&1; then
    print_error "Port $HUGO_PORT is already in use by another process"
    print_status "Use 'lsof -i :$HUGO_PORT' to see what's using the port"
    exit 1
fi

# Check if hugo is installed
if ! command -v hugo &> /dev/null; then
    print_error "Hugo is not installed. Please install Hugo first."
    exit 1
fi

# Check if hugo.toml or config.toml exists
if [ ! -f "hugo.toml" ] && [ ! -f "config.toml" ] && [ ! -f "hugo.yaml" ] && [ ! -f "config.yaml" ]; then
    print_error "Hugo config file not found. Are you in the correct directory?"
    exit 1
fi

# Start Hugo
print_status "Starting Hugo server..."
print_status "Host: $HUGO_HOST"
print_status "Port: $HUGO_PORT"
print_status "Log file: $HUGO_LOG"

# Start Hugo in background and capture PID
nohup hugo server -D \
  --bind "$HUGO_HOST" \
  --port "$HUGO_PORT" > "$HUGO_LOG" 2>&1 &

HUGO_PID=$!

# Save PID to file
echo "$HUGO_PID" > "$HUGO_PID_FILE"

# Wait a moment and check if Hugo started successfully
sleep 3
if ps -p "$HUGO_PID" > /dev/null 2>&1; then
    print_status "Hugo started successfully (PID: $HUGO_PID)"
    print_status "Server running at: http://localhost:$HUGO_PORT"
    print_status "Log file: $HUGO_LOG"
    print_status "Use './stop_hugo.sh' to stop the server"
else
    print_error "Hugo failed to start. Check the log file: $HUGO_LOG"
    rm -f "$HUGO_PID_FILE"
    exit 1
fi
