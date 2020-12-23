#!/bin/bash

set -e

# Build
echo -e '\033[32mCompiling\033[0m'
gcc -c -o util.o app/src/test/util.c
gcc -E -P -C -o tmp.c app/src/test/test.c
gradle run -q --args="../tmp.c" > tmp.s
gcc -o tmp util.o tmp.s

echo -e '\033[32mRunning\033[0m test.c'
echo
./tmp
echo

gradle run -q --args="src/test/comment.c" > tmp.s
gcc -o tmp util.o tmp.s
echo -e '\033[32mRunning\033[0m comment.c'
echo
./tmp
echo

# Clean
rm -f tmp tmp.s tmp.c util.o

