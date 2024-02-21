#!/bin/bash

# Initial sleep time between requests and decrement value
initial_sleep=0.125
decrement=2
current_sleep=$initial_sleep

# Duration of each interval (in seconds)
interval_duration=10

# Request counter for unique ID
request_counter=0

# Global success and failure counters
global_success_count=0
global_failure_count=0

# Temporary directory for storing response codes
tmp_dir=$(mktemp -d)
echo "Using temporary directory $tmp_dir for response codes"

# Static payload for your specific endpoint
static_payload='{
    "OrderId": 123456,
    "Symbol": "BTC",
    "Quantity": 10
}'

# Function to send a request and log its status code
send_request() {
    ((request_counter++))
    status_code_file="$tmp_dir/status_$request_counter.txt"
    curl -o /dev/null -s -w "%{http_code}" -X POST 'http://localhost:3000/placeOrder' \
        -H 'Content-Type: application/json' \
        -d "$static_payload" > "$status_code_file" &
}

# Main loop
while true; do
    start_time=$(date +%s)
    end_time=$((start_time + interval_duration))
    echo "Interval started. Sending requests for $interval_duration seconds..."
    echo "Current request sleep time will be $current_sleep seconds..."

    while [ $(date +%s) -lt $end_time ]; do
        send_request
        echo "Request #$request_counter sent. Sleeping for $current_sleep seconds..."
        sleep $current_sleep
    done

    wait # Wait for all background jobs to complete

    # Count success and failure for the interval
    interval_success_count=$(grep -l '^200$' "$tmp_dir"/* | wc -l)
    interval_failure_count=$(grep -L '^200$' "$tmp_dir"/* | wc -l)

    # Update global success and failure counts
    global_success_count=$((global_success_count + interval_success_count))
    global_failure_count=$((global_failure_count + interval_failure_count))

    echo "Interval complete. Success: $interval_success_count, Failure: $interval_failure_count"

    # Prepare for next interval
    current_sleep=$(echo "scale=5; $current_sleep / $decrement" | bc)

    # Check for minimum sleep time
    if [ $(echo "$current_sleep < 0.03125" | bc) -eq 1 ]; then
        echo "Sleep time has decreased to less than 0.25 seconds. Stopping script."
        break # Exit the loop and script
    fi

    # Clear temporary directory for the next interval
    rm "$tmp_dir"/*
done

# Log total success and failure
echo "Total requests succeeded: $global_success_count"
echo "Total requests failed: $global_failure_count"

# Cleanup temporary directory
rm -r "$tmp_dir"
echo "Cleanup complete, temporary files removed."
