/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package jaian;

import java.util.List;
import java.util.ArrayList;

public class App {
    private static Token token;
    private static SymbolTable st = new SymbolTable();
    private static List<Node> code = new ArrayList<Node>();
    private static int seq = 0;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Invalid arguments.");
            System.exit(1);
        }

        // トークナイズしてパースする
        // 結果はcodeに保存される
        String src = args[0];
        token = Token.tokenize(src);
        program();

        // アセンブリの前半部分を出力
        System.out.println(".intel_syntax noprefix");
        System.out.println(".globl main");
        System.out.println("main:");

        // プロローグ
        // 変数26個分の領域を確保する
        System.out.println("    push rbp");
        System.out.println("    mov rbp, rsp");
        System.out.println("    sub rsp, 208");

        // 先頭の式から順にコード生成
        for (int i = 0; i < code.size(); ++i) {
            gen(code.get(i));

            // 式の評価結果としてスタックに一つの値が残っている
            // はずなので、スタックが溢れないようにポップしておく
            System.out.println("    pop rax");
        }

        // エピローグ
        // 最後の式の結果がRAXに残っているのでそれが返り値になる
        System.out.println("    mov rsp, rbp");
        System.out.println("    pop rbp");
        System.out.println("    ret");
    }

    /** エラーを出力して終了 */
    // printfと同じ引数を取る
    public static void error(String fmt, Object... values) {
        System.err.printf(fmt, values);
        System.exit(1);
    }

    /** エラー位置を出力して終了 */
    public static void error_at(String fmt, Object... values) {
        System.err.println(token.src());
        System.err.printf("%" + token.idx() + "s", "");
        System.err.printf("^ ");
        System.err.printf(fmt, values);
        System.exit(1);
    }

    /**
     * 次のトークンが期待している記号の時には、トークンを1つ読み進めてtrueを返す。
     * それ以外の場合はfalseを返す。
     */
    private static boolean consume(String op) {
        if (token.kind() != TokenKind.Punct || !token.str().equals(op)) {
            return false;
        }
        token = token.next();
        return true;
    }

    /**
     * 次のトークンが期待している予約語の時には、トークンを1つ読み進めてtrueを返す。
     * それ以外の場合はfalseを返す。
     */
    private static boolean consume_keyword(TokenKind kind) {
        if (token.kind() == kind) {
            token = token.next();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 次のトークンが識別子である場合、トークンを進めてそれを返す
     * それ以外の場合にはnullを返す。
     */
    private static Token consume_ident() {
        if (token.kind() != TokenKind.Ident) {
            return null;
        }
        Token tok = token;
        token = token.next();
        return tok;
    }

    /**
     * 次のトークンが期待している記号のときには、トークンを1つ読み進める。
     * それ以外の場合にはエラーを報告する。
     */
    private static void expect(String op) {
        if (token.kind() != TokenKind.Punct || !token.str().equals(op)) {
            error_at("Unexpected token: \"%s\"", token.str());
        }
        token = token.next();
    }

    /**
     * 次のトークンが数値の場合、トークンを1つ読み進めてその数値を返す。
     * それ以外の場合にはエラーを報告する。
     */
    private static int expect_number() {
        if (token.kind() != TokenKind.Num) {
            error_at("Not a number: \"%s\"", token.str());
        }
        int val = token.val();
        token = token.next();
        return val;
    }

    // program = stmt*
    private static void program() {
        int i = 0;
        while (!token.at_eof()) {
            code.add(stmt());
        }
    }

    // stmt = expr? ";"
    //      | "{" expr* "}"
    //      | "if" "(" expr ")" stmt ("else" stmt)?  // TODO "(" boolean ")"
    //      | "while" "(" expr ")" stmt  // TODO "(" boolean ")"
    //      | "for" "(" expr? ";" expr? ";" expr? ")" stmt  // TODO "(" boolean ")"
    //      | "return" expr ";"
    private static Node stmt() {
        // "{" expr* "}"
        if (consume("{")) {
            Node head = new Node();
            Node cur = head;
            while (!consume("}")) {
                cur.set_next(stmt());
                cur = cur.next();
            }
            Node node = Node.new_node(NodeKind.Block, null, null);
            node.set_body(head.next());
            return node;
        }

        // "if" "(" expr ")" stmt ("else" stmt)?
        if (consume_keyword(TokenKind.If)) {
            Node node = Node.new_node(NodeKind.If, null, null);
            expect("(");
            node.set_cond(expr());
            expect(")");
            node.set_then(stmt());
            if (consume_keyword(TokenKind.Else)) {
                node.set_els(stmt());
            }
            return node;
        }

        // "while" "(" expr ")" stmt  // TODO "(" boolean ")"
        if (consume_keyword(TokenKind.While)) {
            Node node = Node.new_node(NodeKind.While, null, null);
            expect("(");
            node.set_cond(expr());
            expect(")");
            node.set_then(stmt());
            return node;
        }

        // "for" "(" expr? ";" expr? ";" expr? ")" stmt
        if (consume_keyword(TokenKind.For)) {
            Node node = Node.new_node(NodeKind.For, null, null);
            expect("(");
            if (!consume(";")) {
                node.set_init(expr());
                expect(";");
            }
            if (!consume(";")) {
                node.set_cond(expr());
                expect(";");
            }
            if (!consume(")")) {
                node.set_inc(expr());
                expect(")");
            }
            node.set_then(stmt());
            return node;
        }

        // "return" expr ";"
        if (consume_keyword(TokenKind.Return)) {
            Node node = Node.new_node(NodeKind.Return, expr(), null);
            expect(";");
            return node;
        }

        // expr? ";"
        if (consume(";")) {
            return Node.new_node(NodeKind.Block, null, null);
        }
        Node node = expr();
        expect(";");
        return node;
    }

    // expr = assign
    private static Node expr() {
        return assign();
    }

    // assign = equality ("=" assign)?
    private static Node assign() {
        Node node = equality();
        if (consume("=")) {
            node = Node.new_node(NodeKind.Assign, node, assign());
        }
        return node;
    }

    // equality = relational ("==" relational | "!=" relational)*
    private static Node equality() {
        Node node = relational();

        while (true) {
            if (consume("==")) {
                node = Node.new_node(NodeKind.Eq, node, relational());
            } else if (consume("!=")) {
                node = Node.new_node(NodeKind.Ne, node, relational());
            } else {
                return node;
            }
        }
    }

    // relational = add ("<" add | "<=" add | ">" add | ">=" add)*
    private static Node relational() {
        Node node = add();

        while (true) {
            if (consume("<")) {
                node = Node.new_node(NodeKind.Lt, node, add());
            } else if (consume("<=")) {
                node = Node.new_node(NodeKind.Le, node, add());
            } else if (consume(">")) {
                node = Node.new_node(NodeKind.Lt, add(), node);
            } else if (consume(">=")) {
                node = Node.new_node(NodeKind.Le, add(), node);
            } else {
                return node;
            }
        }
    }

    // add = mul ("+" mul | "-" mul)*
    private static Node add() {
        Node node = mul();

        while (true) {
            if (consume("+")) {
                node = Node.new_node(NodeKind.Add, node, mul());
            } else if (consume("-")) {
                node = Node.new_node(NodeKind.Sub, node, mul());
            } else {
                return node;
            }
        }
    }

    // mul = unary ("*" unary | "/" unary)*
    private static Node mul() {
        Node node = unary();

        while (true) {
            if (consume("*")) {
                node = Node.new_node(NodeKind.Mul, node, unary());
            } else if (consume("/")) {
                node = Node.new_node(NodeKind.Div, node, unary());
            } else {
                return node;
            }
        }
    }

    // unary = ("+" | "-")? unary
    private static Node unary() {
        if (consume("+")) {
            return unary();
        }
        if (consume("-")) {
            return Node.new_node(NodeKind.Sub, Node.new_node_num(0), unary());
        }
        return primary();
    }

    // primary = "(" expr ")" | ident | num
    private static Node primary() {
        // 次のトークンが "(" なら、 "(" expr ")" のはず
        if (consume("(")) {
            Node node = expr();
            expect(")");
            return node;
        }

        Token tok = consume_ident();
        if (tok != null) {
            Node node = Node.new_node(NodeKind.Var, null, null);

            Obj obj = st.find_var(tok);
            if (obj != null) {
                node.set_offset(obj.offset());
            } else {
                Obj new_obj = new Obj(tok.str(), st.offset() + 8);
                node.set_offset(new_obj.offset());
                st.push(new_obj);
            }
            return node;
        }

        // そうでなければ数値のはず
        return Node.new_node_num(expect_number());
    }

    /** 連番をインクリメントして返す */
    private static int sequence() {
        return seq++;
    }

    /** 式を左辺値として評価 */
    private static void gen_val(Node node) {
        if (node.kind() != NodeKind.Var) {
            error("not a variable");
        }
        System.out.printf("    mov rax, rbp\n");
        System.out.printf("    sub rax, %d\n", node.offset());
        System.out.printf("    push rax\n");
    }

    /** コード生成 */
    private static void gen(Node node) {
        switch (node.kind()) {
            case Num:
                System.out.printf("    push %d\n", node.val());
                return;
            case Var:
                gen_val(node);
                System.out.println("    pop rax");
                System.out.println("    mov rax, [rax]");
                System.out.println("    push rax");
                return;
            case Assign:
                gen_val(node.lhs());
                gen(node.rhs());
                System.out.println("    pop rdi");
                System.out.println("    pop rax");
                System.out.println("    mov [rax], rdi");
                System.out.println("    push rdi");
                return;
            case Block:
                for (Node stmt = node.body(); stmt != null; stmt = stmt.next()) {
                    gen(stmt);
                }
                return;
            case Return:
                gen(node.lhs());
                System.out.println("    pop rax");
                System.out.println("    mov rsp, rbp");
                System.out.println("    pop rbp");
                System.out.println("    ret");
                return;
            case If: {
                int count = sequence();
                gen(node.cond());
                System.out.println("    pop rax");
                System.out.println("    cmp rax, 0");
                if (node.els() != null) {
                    System.out.printf("    je .L.else.%d\n", count);
                    gen(node.then());
                    System.out.printf("    jmp .L.end.%d\n", count);
                    System.out.printf(".L.else.%d:\n", count);
                    gen(node.els());
                    System.out.printf(".L.end.%d:\n", count);
                } else {
                    System.out.printf("    je .L.end.%d\n", count);
                    gen(node.then());
                    System.out.printf(".L.end.%d:\n", count);
                }
                return;
            }
            case While: {
                int count = sequence();
                System.out.printf(".L.begin.%d:\n", count);
                gen(node.cond());
                System.out.println("    pop rax");
                System.out.println("    cmp rax, 0");
                System.out.printf("    je .L.end.%d\n", count);
                gen(node.then());
                System.out.printf("    jmp .L.begin.%d\n", count);
                System.out.printf(".L.end.%d:\n", count);
                return;
            }
            case For: {
                int count = sequence();
                if (node.init() != null) {
                    gen(node.init());
                }
                System.out.printf(".L.begin.%d:\n", count);
                if (node.cond() != null) {
                    gen(node.cond());
                    System.out.println("    pop rax");
                    System.out.println("    cmp rax, 0");
                    System.out.printf("    je .L.end.%d\n", count);
                }
                gen(node.then());
                if (node.inc() != null) {
                    gen(node.inc());
                }
                System.out.printf("    jmp .L.begin.%d\n", count);
                System.out.printf(".L.end.%d:\n", count);
                return;
            }
        }

        gen(node.lhs());
        gen(node.rhs());

        System.out.println("    pop rdi");
        System.out.println("    pop rax");

        switch (node.kind()) {
            case Add:
                System.out.println("    add rax, rdi");
                break;
            case Sub:
                System.out.println("    sub rax, rdi");
                break;
            case Mul:
                System.out.println("    imul rax, rdi");
                break;
            case Div:
                System.out.println("    cqo");
                System.out.println("    idiv rdi");
                break;
            case Eq:
                System.out.println("    cmp rax, rdi");
                System.out.println("    sete al");
                System.out.println("    movzb rax, al");
                break;
            case Ne:
                System.out.println("    cmp rax, rdi");
                System.out.println("    setne al");
                System.out.println("    movzb rax, al");
                break;
            case Lt:
                System.out.println("    cmp rax, rdi");
                System.out.println("    setl al");
                System.out.println("    movzb rax, al");
                break;
            case Le:
                System.out.println("    cmp rax, rdi");
                System.out.println("    setle al");
                System.out.println("    movzb rax, al");
                break;
        }

        System.out.println("    push rax");
    }
}

