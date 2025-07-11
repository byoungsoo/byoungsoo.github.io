#!/bin/bash

# Jekyll Stop Script
# Enhanced version with better error handling and process management

# Configuration
JEKYLL_PID_FILE="$HOME/.jekyll.pid"

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

# Function to kill Jekyll processes
kill_jekyll_processes() {
    local pids=$(ps -ef | grep "jekyll serve" | grep -v grep | awk '{print $2}')
    if [ -n "$pids" ]; then
        print_status "Found Jekyll processes: $pids"
        echo "$pids" | xargs kill -TERM 2>/dev/null || true
        
        # Wait a moment for graceful shutdown
        sleep 2
        
        # Check if any processes are still running and force kill if necessary
        local remaining_pids=$(ps -ef | grep "jekyll serve" | grep -v grep | awk '{print $2}')
        if [ -n "$remaining_pids" ]; then
            print_warning "Some processes didn't respond to SIGTERM, force killing..."
            echo "$remaining_pids" | xargs kill -KILL 2>/dev/null || true
        fi
        
        return 0
    else
        return 1
    fi
}

# Check if PID file exists and process is running
if [ -f "$JEKYLL_PID_FILE" ]; then
    PID=$(cat "$JEKYLL_PID_FILE")
    if ps -p "$PID" > /dev/null 2>&1; then
        print_status "Stopping Jekyll server (PID: $PID)..."
        kill -TERM "$PID" 2>/dev/null || true
        
        # Wait for graceful shutdown
        sleep 2
        
        # Check if process is still running
        if ps -p "$PID" > /dev/null 2>&1; then
            print_warning "Process didn't respond to SIGTERM, force killing..."
            kill -KILL "$PID" 2>/dev/null || true
        fi
        
        rm -f "$JEKYLL_PID_FILE"
        print_status "Jekyll server stopped successfully"
    else
        print_warning "PID file exists but process is not running, cleaning up..."
        rm -f "$JEKYLL_PID_FILE"
    fi
else
    print_warning "No PID file found, searching for Jekyll processes..."
    if kill_jekyll_processes; then
        print_status "Jekyll processes stopped"
    else
        print_warning "No Jekyll processes found"
    fi
fi

# Double-check that all Jekyll processes are stopped
remaining_processes=$(ps -ef | grep "jekyll serve" | grep -v grep | wc -l)
if [ "$remaining_processes" -gt 0 ]; then
    print_error "Some Jekyll processes may still be running"
    print_status "Run 'ps -ef | grep jekyll' to check manually"
    exit 1
else
    print_status "All Jekyll processes have been stopped"
fi
