#!/bin/bash

# Jekyll Start Script
# Enhanced version with better error handling and logging

set -e  # Exit on any error

# Configuration
JEKYLL_LOG="$HOME/jekyll.log"
JEKYLL_PID_FILE="$HOME/.jekyll.pid"
JEKYLL_PORT=4000
JEKYLL_HOST="127.0.0.1"

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

# Check if Jekyll is already running
if [ -f "$JEKYLL_PID_FILE" ]; then
    PID=$(cat "$JEKYLL_PID_FILE")
    if ps -p "$PID" > /dev/null 2>&1; then
        print_warning "Jekyll is already running (PID: $PID)"
        print_status "You can access your site at: http://$JEKYLL_HOST:$JEKYLL_PORT"
        exit 0
    else
        print_warning "Stale PID file found, removing..."
        rm -f "$JEKYLL_PID_FILE"
    fi
fi

# Check if port is already in use
if lsof -Pi :$JEKYLL_PORT -sTCP:LISTEN -t >/dev/null 2>&1; then
    print_error "Port $JEKYLL_PORT is already in use by another process"
    print_status "Use 'lsof -i :$JEKYLL_PORT' to see what's using the port"
    exit 1
fi

# Check if Gemfile exists
if [ ! -f "Gemfile" ]; then
    print_error "Gemfile not found. Are you in the correct directory?"
    exit 1
fi

# Check if bundle is installed
if ! command -v bundle &> /dev/null; then
    print_error "Bundler is not installed. Please run 'gem install bundler'"
    exit 1
fi

# Install/update dependencies if needed
print_status "Checking dependencies..."
bundle check || {
    print_status "Installing missing gems..."
    bundle install
}

# Start Jekyll
print_status "Starting Jekyll server..."
print_status "Host: $JEKYLL_HOST"
print_status "Port: $JEKYLL_PORT"
print_status "Log file: $JEKYLL_LOG"

# Start Jekyll in background and capture PID
nohup bundle exec jekyll serve --host "$JEKYLL_HOST" --port "$JEKYLL_PORT" --livereload > "$JEKYLL_LOG" 2>&1 &
JEKYLL_PID=$!

# Save PID to file
echo "$JEKYLL_PID" > "$JEKYLL_PID_FILE"

# Wait a moment and check if Jekyll started successfully
sleep 3
if ps -p "$JEKYLL_PID" > /dev/null 2>&1; then
    print_status "Jekyll started successfully (PID: $JEKYLL_PID)"
    print_status "Server running at: http://$JEKYLL_HOST:$JEKYLL_PORT"
    print_status "Log file: $JEKYLL_LOG"
    print_status "Use './stop_jekyll.sh' to stop the server"
else
    print_error "Jekyll failed to start. Check the log file: $JEKYLL_LOG"
    rm -f "$JEKYLL_PID_FILE"
    exit 1
fi
