#!/bin/bash

OKCNT=0
NGCNT=0

# Assertion
function assert() {
    expected="$1"
    input="$2"

    gradle run -q --args="$input" > tmp.s
    cc -o tmp tmp.s
    ./tmp
    actual="$?"

    if [ "$expected" = "$actual" ]; then
        echo -e "'$input' => $actual ... \033[32mok\033[0m"
        OKCNT=$((OKCNT+1))
    else
        echo -e "'$input' => $actual ... \033[31mFAILED\033[0m: expected $expected, but got $actual"
        # exit 1
        NGCNT=$((NGCNT+1))
    fi
}

# Build
gradle assemble
echo

# Tests
assert  0 '0'
assert 42 '42'

# Clean
rm -f tmp tmp.s

# Result
echo
if [ $NGCNT -eq 0 ]; then
    echo -e "test result: \033[32mok\033[0m. $OKCNT passed; $NGCNT failed;\n"
else
    echo -e "test result: \033[31mFAILED\033[0m. $OKCNT passed; $NGCNT failed;\n"
    exit 1
fi

