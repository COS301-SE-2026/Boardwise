#!/bin/bash
set -euo pipefail

JMX="${JMX:-boardwise.jmx}"
HOST="${HOST:-localhost}"
PORT="${PORT:-8080}"
THREADS="${THREADS:-}"    
RAMPUP="${RAMPUP:-}"      
LOOPS="${LOOPS:-}"       

TIMESTAMP=$(date +%Y%m%d_%H%M%S)
RESULTS_DIR="results"
JTL="${RESULTS_DIR}/result_${TIMESTAMP}.jtl"
REPORT_DIR="${RESULTS_DIR}/report_${TIMESTAMP}"
LOG="${RESULTS_DIR}/jmeter_${TIMESTAMP}.log"

mkdir -p "${RESULTS_DIR}"

if [ ! -f "${JMX}" ]; then
  echo "Error: test plan '${JMX}' not found in $(pwd)" >&2
  exit 1
fi

# Build optional -J overrides
EXTRA_ARGS=()
[ -n "${THREADS}" ] && EXTRA_ARGS+=("-Jthreads=${THREADS}")
[ -n "${RAMPUP}" ]  && EXTRA_ARGS+=("-Jrampup=${RAMPUP}")
[ -n "${LOOPS}" ]   && EXTRA_ARGS+=("-Jloops=${LOOPS}")

echo "Running ${JMX} against ${HOST}:${PORT}"
echo "Results: ${JTL}"
echo "Report:  ${REPORT_DIR}"

jmeter -n -t "${JMX}" \
  -l "${JTL}" \
  -j "${LOG}" \
  -e -o "${REPORT_DIR}" \
  -Jhost="${HOST}" -Jport="${PORT}" \
  "${EXTRA_ARGS[@]}"

echo "Done. Open ${REPORT_DIR}/index.html for the HTML dashboard."