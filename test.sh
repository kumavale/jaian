#!/bin/bash

OKCNT=0
NGCNT=0


# Assertion
function assert() {
    expected="$1"
    input="$2"

    gradle run --quiet --no-rebuild --args="'$input'" > tmp.s
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
if [ "$?" -ne "0" ]; then
    exit 1
fi
echo

# Tests
assert  0 '0;'
assert 42 '42;'
assert 21 '5+20-4;'
assert 41 ' 12 + 34 - 5 ; '
assert 47 '5+6*7;'
assert 15 '5*(9-6);'
assert  4 '(3+5)/2;'
assert 10 '-10+20;'
assert 10 '- -10;'
assert 10 '- - +10;'

assert  0 '0==1;'
assert  1 '42==42;'
assert  1 '0!=1;'
assert  0 '42!=42;'

assert  1 '0<1;'
assert  0 '1<1;'
assert  0 '2<1;'
assert  1 '0<=1;'
assert  1 '1<=1;'
assert  0 '2<=1;'

assert  1 '1>0;'
assert  0 '1>1;'
assert  0 '1>2;'
assert  1 '1>=0;'
assert  1 '1>=1;'
assert  0 '1>=2;'

assert  3 '1; 2; 3;'

assert  3 'a=3; a;'
assert  8 'a=3; z=5; a+z;'
assert  6 'a=b=3; a+b;'
assert  3 'foo=3; foo;'
assert  8 'foo_123=3; bar=5; foo_123+bar;'

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

