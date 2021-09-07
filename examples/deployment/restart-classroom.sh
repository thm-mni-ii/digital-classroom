#!/bin/bash
cd $CLASSROOM_DIR
docker-compose pull classroom
docker-compose up -d --remove-orphans classroom
