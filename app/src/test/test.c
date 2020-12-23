
#define assert_eq(expect, program)                                                                            \
    do {                                                                                                      \
        if (expect == (program)) {                                                                            \
            printf("'%s' => %d ... \x1b[32mok\x1b[0m\n", #program, expect);                                   \
            OKCNT = OKCNT + 1;                                                                                \
        } else {                                                                                              \
            printf("'%s' ... \x1b[31mFAILED\x1b[0m: expected %d, but got %d\n", #program, expect, (program)); \
            NGCNT = NGCNT + 1;                                                                                \
        }                                                                                                     \
    } while (false)

int OKCNT;
int NGCNT;

int x;
int y[4];

int ret3() {
    return 3;
    return 5;
}

int add2(int x, int y) {
    return x + y;
}

int sub2(int x, int y) {
    return x - y;
}

int add6(int a, int b, int c, int d, int e, int f) {
    return a + b + c + d + e + f;
}

int sub_char(char a, char b, char c) {
    return a - b - c;
}

int fib(int x) {
    if (x <= 1) {
        return 1;
    }
    return fib(x-1) + fib(x-2);
}

int main() {
    // calculation
    assert_eq( 0, 0);
    assert_eq(42, 42);
    assert_eq(21, 5+20-4);
    assert_eq(41, 12 + 34 - 5 );
    assert_eq(47, 5+6*7);
    assert_eq(15, 5*(9-6));
    assert_eq( 4, (3+5)/2);
    assert_eq(10, -10+20);
    assert_eq(10, - -10);
    assert_eq(10, - - +10);
    assert_eq(false, 0==1);
    assert_eq(true, 42==42);
    assert_eq(true, 0!=1);
    assert_eq(false, 42!=42);
    assert_eq(true, 0<1);
    assert_eq(false, 1<1);
    assert_eq(false, 2<1);
    assert_eq(true, 0<=1);
    assert_eq(true, 1<=1);
    assert_eq(false, 2<=1);
    assert_eq(true, 1>0);
    assert_eq(false, 1>1);
    assert_eq(false, 1>2);
    assert_eq(true, 1>=0);
    assert_eq(true, 1>=1);
    assert_eq(false, 1>=2);

    // int variable
    assert_eq(3, { int a; a=3; a; });
    assert_eq(2, { int a; 2; });
    assert_eq(3, { int a=3; a; });
    assert_eq(4, { int a=3; a=4; a; });
    assert_eq(8, { int a=3; int z=5; a+z; });
    assert_eq(6, ({ int a,b; a=b=3; a+b; }));
    assert_eq(3, { int foo=3; foo; });
    assert_eq(8, ({ int foo_123=3, bar=5; foo_123+bar; }));

    // control
    assert_eq( 3, { int x; if (false) x=2; else x=3; x; });
    assert_eq( 3, { int x; if (1-1==1) x=2; else x=3; x; });
    assert_eq( 2, { int x; if (true) x=2; else x=3; x; });
    assert_eq( 2, { int x; if (2-1==1) x=2; else x=3; x; });
    assert_eq( 2, { bool t=true; if(t) { 2; } else { 3; } });
    assert_eq( 3, { bool f=false; if(f) { 2; } else { 3; } });
    assert_eq(55, { int j=0; for (int i=0; i<=10; i=i+1) j=i+j; j; });
    assert_eq(10, { int i=0; while(i<10) i=i+1; i; });
    assert_eq( 3, { 1; {2;} 3; });
    assert_eq( 5, { ;;; 5; });
    assert_eq(10, { int i=0; while(i<10) i=i+1; i; });
    assert_eq(55, { int i=0; int j=0; while(i<=10) {j=i+j; i=i+1;} j; });
    assert_eq( 5, { int i=0; do { i=i+1; } while(i<5); i; });
    assert_eq(42, { int i; do { i=42; } while(false); i; });
    assert_eq(10, { int j=0; for(int i=0; i<5; i=i+1) j=j+i; j; });
    assert_eq(42, { int i=42; for(int i=0; i<5; i=i+1); i; });

    // function
    assert_eq(  3, ret3());
    assert_eq(  8, add2(3, 5));
    assert_eq(  2, sub2(5, 3));
    assert_eq( 21, add6(1,2,3,4,5,6));
    assert_eq( 66, add6(1,2,add6(3,4,5,6,7,8),9,10,11));
    assert_eq(136, add6(1,2,add6(3,add6(4,5,6,7,8,9),10,11,12,13),14,15,16));
    assert_eq(  7, add2(3,4));
    assert_eq(  1, sub2(4,3));
    assert_eq( 55, fib(9));
    assert_eq(  1, { sub_char(7, 3, 3); });

    // block
    assert_eq( 3, { 1; {2;} 3; });
    assert_eq( 5, { ;;; 5; });
    assert_eq(42, { do {} while(false); 42; });
    assert_eq(42, { {{}} 42; });
    assert_eq(42, { ({}); 42; });
    assert_eq(42, { (({})); 42; });
    assert_eq(42, { (({({{}});})); 42; });

    // array
    assert_eq( 6, ({ int a[3]; a[0]=1; a[1]=2; a[2]=3; a[0]+a[1]+a[2]; }));
    assert_eq(15, ({ int a, b[99], c; a=4; b[42]=5; c=6; a+b[42]+c; }));
    assert_eq( 2, ({ int a[3]; a[0]=2; a[1+1]=a[0]; a[a[0]]; }));

    // char
    assert_eq(42, { char x=42; x; });
    assert_eq( 1, { char x=1; char y=2; x; });
    assert_eq( 2, { char x=1; char y=2; y; });
    assert_eq( 3, { sub_char(31, 17, 11); });
    assert_eq( 4, { char x[10]; x[0]=1; x[1]=2; x[2]=3; x[0]+x[2]; });
    assert_eq( 5, { char x[10]; x[5]=5; x[5]; });
    assert_eq(11, { char x=5; int a=7; char y=6; int b=8; x+y; });
    assert_eq(15, { char x=5; int a=7; char y=6; int b=8; a+b; });

    // scope
    assert_eq(6, ({ int a=2,b; { int a=4; b=a; } a+b; }));

    // global
    assert_eq( 0, { x; });
    assert_eq( 3, { x=3; x; });
    assert_eq( 7, { int y; x=3; y=4; x+y; });
    assert_eq( 0, { y[0]=0; y[1]=1; y[2]=2; y[3]=3; y[0]; });
    assert_eq( 1, { y[1]; });
    assert_eq( 2, { y[2]; });
    assert_eq( 3, { y[3]; });
    assert_eq(16, { int z[4]; y[1]=9; z[1]=7; y[1]+z[1]; });

    // string
    assert_eq(  0, ""[0]);
    assert_eq( 97, "abc"[0]);
    assert_eq( 98, "abc"[1]);
    assert_eq( 99, "abc"[2]);
    assert_eq(  0, "abc"[3]);
    assert_eq( 34, "\"abc\""[4]);
    assert_eq( 97, "\a"[0]);  // \a not support
    assert_eq(  8, "\b"[0]);
    assert_eq(  9, "\t"[0]);
    assert_eq( 10, "\n"[0]);
    assert_eq( 11, "\v"[0]);
    assert_eq( 12, "\f"[0]);
    assert_eq( 13, "\r"[0]);
    assert_eq(101, "\e"[0]);  // \e not support
    assert_eq(106, "\j"[0]);
    assert_eq(107, "\k"[0]);
    assert_eq(108, "\l"[0]);
    assert_eq( 97, "\ax\ny"[0]);  // \a not support
    assert_eq(120, "\ax\ny"[1]);
    assert_eq( 10, "\ax\ny"[2]);
    assert_eq(121, "\ax\ny"[3]);
    assert_eq(  0, "\0"[0]);
    assert_eq( 16, "\20"[0]);
    assert_eq( 65, "\101"[0]);
    assert_eq(104, "\1500"[0]);
    assert_eq(  0, "\x00"[0]);
    assert_eq(119, "\x77"[0]);
    assert_eq(  5, { printf("[abc]"); });                 // 出力される文字列も要確認
    assert_eq(  7, { printf("[2+3=%d]", 2+3); });         // 出力される文字列も要確認
    assert_eq( 15, { printf("[\x1b[34mBLUE\x1b[0m]"); });  // 出力される文字列も要確認
    assert_eq(  0, { if ("abc" == "abc") 0; else 1; });  // アドレスの比較
    assert_eq(  1, { if ("abc" == "xyz") 0; else 1; });  // アドレスの比較

    // statement expression
    assert_eq(42, { ({ 42; }); });
    assert_eq( 3, { int i=({ 1+2; }); i; });
    assert_eq( 4, { bool i=({ 1==1; }); if(i) 4; else 5; });
    assert_eq( 2, { ({ 0; 1; 2; }); });
    assert_eq( 3, { ({ 0; 1; 2; }); 3; });
    assert_eq( 6, { ({ 1; }) + ({ 2; }) + ({ 3; }); });
    assert_eq( 3, { ({ int x=3; x; }); });

    // result
    printf("\n");
    if (NGCNT == 0) {
        printf("test result: \x1b[32mok\x1b[0m. %d passed; %d failed;\n", OKCNT, NGCNT);
        return 0;
    } else {
        printf("test result: \x1b[31mFAILED\x1b[0m. %d passed; %d failed;\n", OKCNT, NGCNT);
        return 1;
    }
}

