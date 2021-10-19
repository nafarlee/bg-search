#!/usr/bin/env sh
yq eval '.env_variables | .. style="single"' app.yaml \
  | awk 'BEGIN {FS = ": "} ; {print "set -x " $1 " " $2}'
