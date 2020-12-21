#!/bin/bash

OKCNT=0
NGCNT=0


# C function
cat << EOF | gcc -xc -c -o func.o -
    #include <assert.h>
    int ret0() { return 0; }
    int ret42() { return 42; }
    int retx(int x) { return x; }
    int add(int x, int y) { return x+y; }
    int add6(int a, int b, int c, int d, int e, int f) {
        assert(a==1 && b==2 && c==3 && d==4 && e==5 && f==add(6, 7));
        return a+b+c+d+e+f;
    }
EOF

# Assertion
function assert() {
    expected="$1"
    input="$2"

    gradle run --quiet --no-rebuild --args="'$input'" > tmp.s
    if [ "$?" -ne "0" ]; then
        echo -e "'$input' ... \033[31mFAILED\033[0m"
        NGCNT=$((NGCNT+1))
        return 1;
    fi
    cc -o tmp tmp.s func.o && ./tmp
    actual="$?"

    if [ "$expected" = "$actual" ]; then
        echo -e "'$input' => $actual ... \033[32mok\033[0m"
        OKCNT=$((OKCNT+1))
    else
        echo -e "'$input' => $actual ... \033[31mFAILED\033[0m: expected $expected, but got $actual"
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
assert  0 'int main() { return 0; }'
assert 42 'int main() { return 42; }'
assert 21 'int main() { return 5+20-4; }'
assert 41 ' int main ( ) { return 12 + 34 - 5 ; } '
assert 47 'int main() { return 5+6*7; }'
assert 15 'int main() { return 5*(9-6); }'
assert  4 'int main() { return (3+5)/2; }'
assert 10 'int main() { return -10+20; }'
assert 10 'int main() { return - -10; }'
assert 10 'int main() { return - - +10; }'

assert  0 'int main() { if (0==1) return 1; return 0; }'
assert  1 'int main() { if (42==42) return 1; return 0; }'
assert  1 'int main() { if (0!=1) return 1; return 0; }'
assert  0 'int main() { if (42!=42) return 1; return 0; }'

assert  1 'int main() { if (0<1) return 1; return 0; }'
assert  0 'int main() { if (1<1) return 1; return 0; }'
assert  0 'int main() { if (2<1) return 1; return 0; }'
assert  1 'int main() { if (0<=1) return 1; return 0; }'
assert  1 'int main() { if (1<=1) return 1; return 0; }'
assert  0 'int main() { if (2<=1) return 1; return 0; }'

assert  1 'int main() { if (1>0) return 1; return 0; }'
assert  0 'int main() { if (1>1) return 1; return 0; }'
assert  0 'int main() { if (1>2) return 1; return 0; }'
assert  1 'int main() { if (1>=0) return 1; return 0; }'
assert  1 'int main() { if (1>=1) return 1; return 0; }'
assert  0 'int main() { if (1>=2) return 1; return 0; }'

assert  3 'int main() { 1; 2; return 3; }'
assert  2 'int main() { 1; return 2; 3; }'
assert  1 'int main() { return 1; 2; 3; }'

assert  3 'int main() { int a; a=3; return a; }'
assert  2 'int main() { int a; return 2; }'
assert  3 'int main() { int a=3; return a; }'
assert  4 'int main() { int a=3; a=4; return a; }'
assert  8 'int main() { int a=3; int z=5; return a+z; }'
assert  6 'int main() { int a,b; a=b=3; return a+b; }'
assert  3 'int main() { int foo=3; return foo; }'
assert  8 'int main() { int foo_123=3, bar=5; return foo_123+bar; }'

assert  3 'int main() { if (false) return 2; return 3; }'
assert  3 'int main() { if (1-1!=0) return 2; return 3; }'
assert  3 'int main() { if (1==0) return 2; return 3; }'
assert  2 'int main() { if (2-1==1) return 2; return 3; }'
assert  3 'int main() { if (true) if(false) return 2; return 3; }'
assert  2 'int main() { if (true) { return 2; } return 3; }'

assert  2 'int main() { if(true) return 2; else return 3; }'
assert  3 'int main() { if(false) return 2; else if(true) return 3; }'
assert  4 'int main() { if(false) return 2; else if(false) return 3; else return 4; }'
assert  2 'int main() { if(true) { return 2; } else { return 3; } }'

assert  4 'int main() { int i=5; int j=0; while(0<(i=i-1)) j=j+1; return j; }'
assert  1 'int main() { while(false) return 0; return 1; }'
assert 55 'int main() { int i=0; int j=0; while(i<=10) {j=i+j; i=i+1; } return j; }'

assert 10 'int main() { int j=0; for(int i=0; i<5; i=i+1) j=j+i; return j; }'
assert 42 'int main() { for(int i=42; i<100; i=i+1) return i; return 21; }'
assert 42 'int main() { for(int i=42, j; true;) { j = i; return j; } return 21; }'
assert  1 'int main() { for(;;) return 1; return 0; }'
assert 42 'int main() { int i=42; for(int i=0; i<5; i=i+1); return i; }'

assert  3 'int main() { 1; {2;} return 3; }'

assert  5 'int main() { ;;; return 5; }'

assert  0 'int main() { return ret0(); }'
assert 42 'int main() { return ret42(); }'
assert  3 'int main() { return retx(1+2); }'
assert  7 'int main() { return add(3, 4); }'
assert 10 'int main() { return add(5+6, 7-8); }'
assert 28 'int main() { return add6(1,2,3,4,5,add(6,7)); }'

assert 32 'int main() { return ret32(); } int ret32() { return 32; }'
assert  7 'int main() { return add2(3,4); } int add2(int x, int y) { return x+y; }'
assert  1 'int main() { return sub2(4,3); } int sub2(int x, int y) { return x-y; }'
assert 55 'int main() { return fib(9); } int fib(int x) { if (x<=1) return 1; return fib(x-1) + fib(x-2); }'
assert 28 'int main() { return six(1, 2, 3, 4, 5, add(6, 7)); }
int six(int a, int b, int c, int d, int e, int f) { return add6(a, b, c, d, e, f); }'

assert 42 'int main() { // this is a linecomment
return 42; } // and here'
assert 42 'int main() { /**/ /* this is a block comment
until */ return 42; } /* and here */'
assert 42 'int main() { // /* this is a line comment
return 42; /* // block */ }'

assert  6 'int main() { int a[3]; a[0]=1; a[1]=2; a[2]=3; return a[0]+a[1]+a[2]; }'
assert 15 'int main() { int a, b[99], c; a=4; b[42]=5; c=6; return a+b[42]+c; }'
assert  2 'int main() { int a[3]; a[0]=2; a[1+1]=a[0]; return a[a[0]]; }'

assert 42 'int main() { char x=42; return x; }'
assert  1 'int main() { char x=1; char y=2; return x; }'
assert  2 'int main() { char x=1; char y=2; return y; }'
assert  3 'int main() { return sub_char(31, 17, 11); } int sub_char(char a, char b, char c) { return a-b-c; }'
assert  4 'int main() { char x[10]; x[0]=1; x[1]=2; x[2]=3; return x[0]+x[2]; }'
assert  5 'int main() { char x[10]; x[5]=5; return x[5]; }'
assert 11 'int main() { char x=5; int a=7; char y=6; int b=8; return x+y; }'
assert 15 'int main() { char x=5; int a=7; char y=6; int b=8; return a+b; }'

assert 42 'int main() { return 42; } int foo(int a) { int b; return 0; } int bar(int a) { int b; return 0; }'
assert  6 'int main() { int a=2,b; { int a=4; b=a; } return a+b; }'

assert  0 'int x; int main() { return x; }'
assert  3 'int x; int main() { x=3; return x; }'
assert  7 'int x; int y; int main() { x=3; y=4; return x+y; }'
assert  7 'int x, y; int main() { x=3; y=4; return x+y; }'
assert  0 'int x[4]; int main() { x[0]=0; x[1]=1; x[2]=2; x[3]=3; return x[0]; }'
assert  1 'int x[4]; int main() { x[0]=0; x[1]=1; x[2]=2; x[3]=3; return x[1]; }'
assert  2 'int x[4]; int main() { x[0]=0; x[1]=1; x[2]=2; x[3]=3; return x[2]; }'
assert  3 'int x[4]; int main() { x[0]=0; x[1]=1; x[2]=2; x[3]=3; return x[3]; }'
assert 16 'int x[4]; int main() { int y[4]; x[1]=9; y[1]=7; return x[1]+y[1]; }'
assert  8 'int x; int main() { int y=8; return x+y; }'

# Clean out
rm -f tmp tmp.s func.o

# Result
echo
if [ $NGCNT -eq 0 ]; then
    echo -e "test result: \033[32mok\033[0m. $OKCNT passed; $NGCNT failed;\n"
else
    echo -e "test result: \033[31mFAILED\033[0m. $OKCNT passed; $NGCNT failed;\n"
    exit 1
fi

