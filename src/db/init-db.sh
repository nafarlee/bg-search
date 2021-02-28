#!/usr/bin/env oil
docker run --network host \
           -it \
           -v "$PWD":/_ \
           postgres:11.3 \
           sh -c "psql -h localhost -U postgres -f _/db.pgsql"
