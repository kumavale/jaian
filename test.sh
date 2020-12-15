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
assert  0 '{ return 0; }'
assert 42 '{ return 42; }'
assert 21 '{ return 5+20-4; }'
assert 41 '{ return  12 + 34 - 5 ; } '
assert 47 '{ return 5+6*7; }'
assert 15 '{ return 5*(9-6); }'
assert  4 '{ return (3+5)/2; }'
assert 10 '{ return -10+20; }'
assert 10 '{ return - -10; }'
assert 10 '{ return - - +10; }'

assert  0 '{ return 0==1; }'
assert  1 '{ return 42==42; }'
assert  1 '{ return 0!=1; }'
assert  0 '{ return 42!=42; }'

assert  1 '{ return 0<1; }'
assert  0 '{ return 1<1; }'
assert  0 '{ return 2<1; }'
assert  1 '{ return 0<=1; }'
assert  1 '{ return 1<=1; }'
assert  0 '{ return 2<=1; }'

assert  1 '{ return 1>0; }'
assert  0 '{ return 1>1; }'
assert  0 '{ return 1>2; }'
assert  1 '{ return 1>=0; }'
assert  1 '{ return 1>=1; }'
assert  0 '{ return 1>=2; }'

assert  3 '{ 1; 2; return 3; }'
assert  2 '{ 1; return 2; 3; }'
assert  1 '{ return 1; 2; 3; }'

assert  3 '{ a=3; return a; }'
assert  8 '{ a=3; z=5; return a+z; }'
assert  6 '{ a=b=3; return a+b; }'
assert  3 '{ foo=3; return foo; }'
assert  8 '{ foo_123=3; bar=5; return foo_123+bar; }'

assert  3 '{ if (0) return 2; return 3; }'
assert  3 '{ if (1-1) return 2; return 3; }'
assert  3 '{ if (1==0) return 2; return 3; }'
assert  2 '{ if (2-1) return 2; return 3; }'
assert  3 '{ if (1) if(0) return 2; return 3; }'
assert  2 '{ if (1) { return 2; } return 3; }'

assert  2 '{ if(1) return 2; else return 3; }'
assert  3 '{ if(0) return 2; else if(1) return 3; }'
assert  4 '{ if(0) return 2; else if(0) return 3; else return 4; }'
assert  2 '{ if(1) { return 2; } else { return 3; } }'

assert  4 '{ i=5; j=0; while(i=i-1) j=j+1; return j; }'
assert  1 '{ while(0) return 0; return 1; }'
assert 55 '{ i=0; j=0; while(i<=10) {j=i+j; i=i+1; } return j; }'

assert 10 '{ j=0; for(i=0; i<5; i=i+1) j=j+i; return j; }'
assert  1 '{ for(;;) return 1; return 0; }'

assert  3 '{1; {2;} return 3;}'

assert  5 '{ ;;; return 5; }'

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

