#!/bin/sh
set -e

chown -R fast:fast /app/data

exec gosu fast "$@"