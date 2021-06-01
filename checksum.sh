#!/usr/bin/env bash
echo -n "$1""$2""$3" | sha1sum | head -c 40
