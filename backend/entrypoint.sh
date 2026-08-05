#!/bin/bash
set -e

#default variables 

TEST_PLAN="${TEST_PLAN:-/jmeter/scripts/testPlan.jmx}"

TIMESTAMP=$(date +%Y%m%d_%H%M%S)
RESULTS_DIR="/jmeter/results/${TIMESTAMP}"
mkdir -p "${RESULTS_DIR}"

echo "Starting JMeter Test execution..."
jmeter -n \
       -t "${TEST_PLAN}" \
       -l "${RESULTS_DIR}/results.jtl" \
       -e -o "${RESULTS_DIR}/report"

echo "Test finished."