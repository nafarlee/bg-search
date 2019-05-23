#!/usr/bin/env sh
docker run --network host \
           -it \
           -v /home/nafarlee/Projects/bg-search:/_ \
           postgres:11.3 \
           sh -c "psql -h localhost -U postgres -f _/db.pgsql && psql -h localhost -U postgres -f _/data.pgsql"
