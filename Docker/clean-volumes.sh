#!/usr/bin/env bash


# https://github.com/chadoe/docker-cleanup-volumes
docker volume rm $(docker volume ls -qf dangling=true)