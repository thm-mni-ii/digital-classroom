#!/bin/bash
cd "$CLASSROOM_DIR" || exit
docker-compose pull classroom
docker-compose up -d --remove-orphans classroom
