#!/bin/bash
BACKEND_VERSION=20.12.1

docker build -t docker.pkg.github.com/augur/five-letters/fl-backend:$BACKEND_VERSION .
