# jaian

A C-like toy compiler  

## Usage

```.sh
gradle run --quiet --args="tmp.c"
gcc -o tmp app/tmp.s
./tmp
```

## Test

```.sh
./test.sh
```

## Example

```.c
int add(int x, int y) { return (int)(x + y); }

int main() {
    int x, y[4];
    for (int i = 0; i < 5; i=i+1) {
        bool b = true;
        if (b) {
            x = 1;
            {
                int x = 42;
            }
        } else {
            /*/*
                unreachable
                           */*/
            if ("abc"=="abc") {
                char a='a';
            }
        }
    }
    do {
        x = add(x, 1);
    } while (x==1);

    return 0;
}
```

## EBNF

```.ebnf
program     = function*
function    = type ident "(" (param ("," param)*)? ")" "{" stmt+ "}"
param       = type ident
stmt        = expr? ";"
            | declaration
            | "{" stmt* "}"
            | "if" "(" expr ")" stmt ("else" stmt)?
            | "do" stmt "while" "(" expr ")"
            | "while" "(" expr ")" stmt
            | "for" "(" (declaration|expr)? ";" expr? ";" expr? ")" stmt
            | "return" expr ";"
declaration = type equality ("=" assign)? ("," equality ("=" assign)?)*
expr        = assign
assign      = equality ("=" assign)?
equality    = relational ("==" relational | "!=" relational)*
relational  = add ("<" add | "<=" add | ">" add | ">=" add)*
add         = mul ("+" mul | "-" mul)*
mul         = cast ("*" cast | "/" cast)*
cast        = "(" type ")" cast | unary
unary       = ("+" | "-") cast
            | primary
primary     = "(" "{" stmt+ "}" ")"
            | "(" expr ")"
            | ident ("[" expr "]")?
            | funccall
            | str
            | num
            | bool
funccall    = ident "(" (assign ("," assign)*)? ")"
type        = "int" | "char" | "bool"
comment     = "//" .* "\n"
            | ("/*" .* "*/")+
```

