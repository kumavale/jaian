#!/bin/bash

set -e

# Build
gradle assemble
gcc -c -o util.o app/src/test/util.c
gcc -E -P -C -o tmp.c app/src/test/test.c
gradle run -q --args="../tmp.c"
gcc -o tmp util.o app/tmp.s

echo -e '\033[32mRunning\033[0m test.c'
echo
./tmp

gradle run -q --args="src/test/comment.c"
gcc -o tmp util.o app/comment.s
echo -e '\033[32mRunning\033[0m comment.c'
echo
./tmp
echo

# Clean
rm -f tmp app/tmp.s app/comment.s tmp.c util.o

