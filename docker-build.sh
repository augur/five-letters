#!/bin/bash
BACKEND_VERSION=20.2.2

docker build -t docker.pkg.github.com/augur/five-letters/fl-backend:$BACKEND_VERSION .