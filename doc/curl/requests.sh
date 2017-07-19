#!/usr/bin/env bash

curl http://localhost:9000/docs \
  -H "x-curri-user: 1" \
  -H "x-curri-group: all"
