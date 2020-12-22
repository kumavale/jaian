/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package jaian;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
//import java.io.IOException;
//import java.nio.charset.Charset;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.stream.Collectors;

public class App {
    private static Token token;                                      // 現在注目しているトークン
    private static SymbolTable global_st = new SymbolTable();        // グローバル変数用のシンボルテーブル
    private static SymbolTable func_st   = new SymbolTable();        // 関数名用のシンボルテーブル
    private static List<Function> code = new ArrayList<Function>();  // 関数毎のコード
    private static Function current_func;                            // 現在処理中のFunction
    private static int seq = 0;                                      // ラベル用シーケンスナンバー
    private static final String[] argregs8  = { "dil", "sil",  "dl",  "cl", "r8b", "r9b" };  // 引数用レジスタ( 8bit)
    private static final String[] argregs64 = { "rdi", "rsi", "rdx", "rcx",  "r8",  "r9" };  // 引数用レジスタ(64bit)

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Invalid arguments.");
            System.exit(1);
        }

        // コマンドライン引数でファイル名を指定して、それを読み込む
        // 1ファイルのみ
        String src = args[0]; // read_file(args[0]); // TODO 実装済。テストが整ったらファイル読み込みに変更する

        // トークナイズしてパースする
        // 結果はcodeに保存される
        token = Token.tokenize(src);
        program();

        // アセンブリの前半部分を出力
        System.out.println(".intel_syntax noprefix");

        // グローバル変数のコード生成
        gen_data();

        // 各関数毎にコード生成
        for (int i = 0; i < code.size(); ++i) {
            gen_func(code.get(i));
        }
    }

    /** エラーを出力して終了 */
    // printfと同じ引数を取る
    public static void error(String fmt, Object... values) {
        System.err.printf(fmt, values);
        System.exit(1);
    }

    /** エラー位置を出力して終了 */
    public static void error_at(String fmt, Object... values) {
        Token current_line = token.current_line();
        System.err.printf(" \033[34m-->\033[0m %d:%d\n", token.line(), token.idx() - current_line.idx() + 1);
        System.err.println(current_line.str());
        if (0 < (token.idx() - current_line.idx())) {
            System.err.printf("%" + (token.idx() - current_line.idx()) + "s", "");
        }
        System.err.printf("\033[31m%s\033[0m ", String.join("", Collections.nCopies(token.len(), "^")));
        System.err.printf(fmt, values);
        System.exit(1);
    }

    ///** パスから文字列を読み込み、一つのStringにして返す */
    //private static String read_file(final String path) {
    //    try {
    //        return Files.lines(Paths.get(path), Charset.forName("UTF-8"))
    //            .collect(Collectors.joining(System.getProperty("line.separator")));
    //    } catch(IOException e) {
    //        error("%s", e);
    //        return "";
    //    }
    //}

    /**
     * 次のトークンが期待している記号の時には、トークンを1つ読み進めてtrueを返す。
     * それ以外の場合はfalseを返す。
     */
    private static boolean consume(String op) {
        if (token.kind() != TokenKind.Punct || !token.str().equals(op)) {
            return false;
        }
        consume();
        return true;
    }

    /**
     * 次のトークンが期待しているTokenKindの時には、トークンを1つ読み進めてtrueを返す。
     * それ以外の場合はfalseを返す。
     */
    private static boolean consume(TokenKind kind) {
        if (token.kind() != kind) {
            return false;
        }
        consume();
        return true;
    }

    /** 無条件にトークンを1つ読み進める。 */
    private static void consume() {
        if (token.next() != null) {
            token = token.next();
        }
    }

    /** 無条件にトークンを1つ戻る。 */
    private static void back() {
        if (token.prev() != null) {
            token = token.prev();
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
        consume();
        return tok;
    }

    /**
     * 次のトークンがtypeである場合、トークンを進めてtypeを返す
     * それ以外の場合にはnullを返す。
     */
    private static Type consume_type() {
        switch (token.kind()) {
            case Int:     consume(); return Type.Int;
            case Char:    consume(); return Type.Char;
            case Boolean: consume(); return Type.Boolean;
            default:                 return null;
        }
    }

    /**
     * 次のトークンが識別子である場合、トークンを進めてそれを返す
     * それ以外の場合にはエラーを出力して終了する。
     */
    private static Token expect_ident() {
        if (token.kind() != TokenKind.Ident) {
            error_at("expected identifier");
        }
        Token tok = token;
        consume();
        return tok;
    }

    /**
     * 次のトークンが期待しているTokenKindのときには、トークンを1つ読み進める。
     * それ以外の場合にはエラーを報告して終了する。
     */
    private static void expect(TokenKind kind) {
        if (token.kind() != kind) {
            error_at("expected token \"%s\", but got \"%s\"", kind.toString().toLowerCase(), token.str());
        }
        consume();
    }

    /**
     * 次のトークンが期待している記号のときには、トークンを1つ読み進める。
     * それ以外の場合にはエラーを報告して終了する。
     */
    private static void expect(String op) {
        if (token.kind() != TokenKind.Punct || !token.str().equals(op)) {
            error_at("expected token \"%s\", but got \"%s\"", op, token.str());
        }
        consume();
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
        consume();
        return val;
    }

    /**
     * 次のトークンがType(型)の場合、トークンを1つ読み進めてそのTypeを返す。
     * それ以外の場合にはエラーを報告する。
     */
    private static Type expect_type() {
        switch (token.kind()) {
            case Int:     consume(); return Type.Int;
            case Char:    consume(); return Type.Char;
            case Boolean: consume(); return Type.Boolean;
            default: error_at("Not a type: \"%s\"", token.str());
        }
        return null;  // unreachable
    }

    /** 指定のノードがbooleanとして解釈されない場合、エラーを報告して終了。 */
    private static void expect_boolean(Node node) {
        switch (node.kind()) {
            case Eq:
            case Ne:
            case Lt:
            case Le:
            case True:
            case False: break;
            default: error_at("incompatible types: expected \"boolean\"");
        }
    }

    // program = function*
    private static void program() {
        while (!token.at_eof()) {
            if (is_function()) {
                code.add(function());  // 関数毎にASTを構築
            } else {
                // グローバル変数の宣言
                // TODO 初期化
                Type type = consume_type();
                if (type == null) {
                    error_at("invalid type");
                }
                do {
                    Token ident = expect_ident();
                    Obj val = global_st.find_var(ident);
                    if (val != null) {
                        back();
                        error_at("already defined");
                    }
                    if (consume("[")) {
                        int element = expect_number();  // 数値リテラルのみ
                        expect("]");
                        val = new Obj(ident.str(), type, 0, element, 0);
                        val.set_is_global();
                        global_st.push(val, element);
                    } else {
                        val = new Obj(ident.str(), type, 0, 0, 0);
                        val.set_is_global();
                        global_st.push(val, 1);
                    }
                } while (consume(","));
                expect(";");
            }
        }
    }

    /** 現在のトークンが関数の始まりなのかを返す。 */
    private static boolean is_function() {
        return token.is_type() && token.next() != null && token.next().kind() == TokenKind.Ident
            && token.next().next() != null && token.next().next().str().equals("(");
    }

    // function = type ident "(" (param ("," param)*)? ")" "{" stmt* "}"
    // param = type ident
    // type = "int" | "char" | "boolean"
    private static Function function() {
        Function func = new Function();
        current_func = func;
        Type return_type = expect_type();
        Token funcname = expect_ident();
        func.set_type(return_type);
        func.set_name(funcname.str());

        if (func_st.find_var(funcname) != null) {
            back();
            error_at("already defined");
        }
        func_st.push(new Obj(funcname.str(), return_type, 0, 0, 0), 0);

        // 仮引数
        expect("(");
        while (!consume(")")) {
            Type type = consume_type();
            if (type != null) {
                // 仮引数の宣言
                Token ident = expect_ident();
                Obj val = func.st().find_var(ident);
                if (val != null) {
                    back();
                    error_at("already defined");
                }
                if (consume("[")) {
                    int element = expect_number();  // 数値リテラルのみ
                    expect("]");
                    val = new Obj(ident.str(), type, func.st().offset() + type.size(), element, func.st().scope());
                    func.st().push(val, element);
                } else {
                    val = new Obj(ident.str(), type, func.st().offset() + type.size(), 0, func.st().scope());
                    func.st().push(val, 1);
                }
                func.push_param(ident);
            } else {
                expect(")");
                break;
            }
            if (!consume(",")) {
                expect(")");
                break;
            }
        }

        // 中身
        expect("{");
        while (!consume("}")) {
            func.push(stmt());
        }
        return func;
    }

    // stmt = expr? ";"
    //      | declaration
    //      | "{" expr* "}"
    //      | "if" "(" expr ")" stmt ("else" stmt)?
    //      | "do" stmt "while" "(" expr ")"
    //      | "while" "(" expr ")" stmt
    //      | "for" "(" (declaration|expr)? ";" expr? ";" expr? ")" stmt
    //      | "return" expr ";"
    private static Node stmt() {
        // declaration
        if (token.is_type()) {
            Type type = consume_type();
            Node node = assign_declaration(type);
            expect(";");
            return node;
        }

        // "{" expr* "}"
        if (consume("{")) {
            current_func.st().scope_in();
            Node head = new Node();
            Node cur = head;
            while (!consume("}")) {
                cur.set_next(stmt());
                cur = cur.next();
            }
            Node node = Node.new_node(NodeKind.Block, null, null);
            node.set_body(head.next());
            current_func.st().scope_out();
            return node;
        }

        // "if" "(" expr ")" stmt ("else" stmt)?
        if (consume(TokenKind.If)) {
            Node node = Node.new_node(NodeKind.If, null, null);
            expect("(");
            Node cond = expr(null);
            expect_boolean(cond);
            node.set_cond(cond);
            expect(")");
            node.set_then(stmt());
            if (consume(TokenKind.Else)) {
                node.set_els(stmt());
            }
            return node;
        }

        // "do" stmt "while" "(" expr ")"
        if (consume(TokenKind.Do)) {
            Node node = Node.new_node(NodeKind.Do, null, null);
            node.set_then(stmt());
            if (!consume(TokenKind.While)) {
                error_at("expect \"while\", but got \"%s\"", token.str());
            }
            expect("(");
            Node cond = expr(null);
            expect_boolean(cond);
            node.set_cond(cond);
            expect(")");
            return node;
        }

        // "while" "(" expr ")" stmt
        if (consume(TokenKind.While)) {
            Node node = Node.new_node(NodeKind.While, null, null);
            expect("(");
            Node cond = expr(null);
            expect_boolean(cond);
            node.set_cond(cond);
            expect(")");
            node.set_then(stmt());
            return node;
        }

        // "for" "(" (declaration|expr)? ";" expr? ";" expr? ")" stmt
        if (consume(TokenKind.For)) {
            current_func.st().scope_in();
            Node node = Node.new_node(NodeKind.For, null, null);
            expect("(");
            if (!consume(";")) {
                if (token.is_type()) {
                    Type type = consume_type();
                    node.set_init(assign_declaration(type));
                } else {
                    node.set_init(expr(null));
                }
                expect(";");
            }
            if (!consume(";")) {
                Node cond = expr(null);
                expect_boolean(cond);
                node.set_cond(cond);
                expect(";");
            }
            if (!consume(")")) {
                node.set_inc(expr(null));
                expect(")");
            }
            node.set_then(stmt());
            current_func.st().scope_out();
            return node;
        }

        // "return" expr ";"
        if (consume(TokenKind.Return)) {
            Node return_value = expr(null);
            if (return_value.type() != current_func.type() && return_value.type() != null) {
                if (return_value.type() == Type.Literal) {
                    return_value.set_type(current_func.type());
                // TODO: キャスト
                //} else {
                //    back();
                //    error_at("mismatched return type");
                }
            }
            Node node = Node.new_node(NodeKind.Return, return_value, null);
            expect(";");
            return node;
        }

        // ";"
        if (consume(";")) {
            return Node.new_node(NodeKind.Block, null, null);
        }

        // expr ";"
        Node node = expr(null);
        expect(";");
        return node;
    }

    // expr = assign
    private static Node expr(Type ty) {
        return assign(ty);
    }

    // assign = equality ("=" assign)?
    private static Node assign(Type ty) {
        Node lhs = equality(ty);
        if (consume("=")) {
            switch (lhs.kind()) {
                case Var:
                case Array: break;
                default: back(); back(); error_at("invalid assign");
            }
            Node rhs = assign(ty);
            if (lhs.type() != rhs.type()) {
                if (rhs.type() == Type.Literal) {
                    rhs.set_type(lhs.type());
                } else {
                    back();
                    error_at("mismatched types");
                }
            }
            lhs = Node.new_node(NodeKind.Assign, lhs, rhs);
            lhs.set_type(rhs.type());
        }
        return lhs;
    }

    // 宣言時専用の割り当て
    // 宣言のみで代入していない場合は無視
    // 関数宣言も無視
    // assign_declaration = type equality ("=" assign)? ("," equality ("=" assign)?)*
    private static Node assign_declaration(Type type) {
        Node head = new Node();
        Node cur = head;
        do {
            Node lhs = equality(type);
            lhs.set_type(type);
            if (!consume("=")) {
                continue;
            }
            Node rhs = assign(type);
            if (lhs.type() != rhs.type()) {
                if (rhs.type() == Type.Literal) {
                    rhs.set_type(lhs.type());
                } else {
                    back();
                    error_at("mismatched types");
                }
            }
            cur.set_next(Node.new_node(NodeKind.Assign, lhs, rhs));
            cur = cur.next();
        } while (consume(","));
        Node node = Node.new_node(NodeKind.Block, null, null);
        node.set_body(head.next());
        return node;
    }

    // equality = relational ("==" relational | "!=" relational)*
    private static Node equality(Type ty) {
        Node node = relational(ty);

        while (true) {
            if (consume("==")) {
                node = Node.new_node(NodeKind.Eq, node, relational(ty));
            } else if (consume("!=")) {
                node = Node.new_node(NodeKind.Ne, node, relational(ty));
            } else {
                return node;
            }
            node.set_type(Type.Boolean);
        }
    }

    // relational = add ("<" add | "<=" add | ">" add | ">=" add)*
    private static Node relational(Type ty) {
        Node node = add(ty);

        while (true) {
            if (consume("<")) {
                node = Node.new_node(NodeKind.Lt, node, add(ty));
            } else if (consume("<=")) {
                node = Node.new_node(NodeKind.Le, node, add(ty));
            } else if (consume(">")) {
                node = Node.new_node(NodeKind.Lt, add(ty), node);
            } else if (consume(">=")) {
                node = Node.new_node(NodeKind.Le, add(ty), node);
            } else {
                return node;
            }
            node.set_type(Type.Boolean);
        }
    }

    // add = mul ("+" mul | "-" mul)*
    private static Node add(Type ty) {
        Node node = mul(ty);

        while (true) {
            if (consume("+")) {
                Type type = node.type();
                node = Node.new_node(NodeKind.Add, node, mul(ty));
                node.set_type(type);
            } else if (consume("-")) {
                Type type = node.type();
                node = Node.new_node(NodeKind.Sub, node, mul(ty));
                node.set_type(type);
            } else {
                return node;
            }
        }
    }

    // mul = unary ("*" unary | "/" unary)*
    private static Node mul(Type ty) {
        Node node = unary(ty);

        while (true) {
            if (consume("*")) {
                Type type = node.type();
                node = Node.new_node(NodeKind.Mul, node, unary(ty));
                node.set_type(type);
            } else if (consume("/")) {
                Type type = node.type();
                node = Node.new_node(NodeKind.Div, node, unary(ty));
                node.set_type(type);
            } else {
                return node;
            }
        }
    }

    // unary = ("+" | "-")? unary
    private static Node unary(Type ty) {
        if (consume("+")) {
            return unary(ty);
        }
        if (consume("-")) {
            Node unary = unary(ty);
            Node node = Node.new_node(NodeKind.Sub, Node.new_node_num(0), unary);
            node.set_type(unary.type());
            return node;
        }
        return primary(ty);
    }

    // funccall = ident "(" (assign ("," assign)*)? ")"
    private static Node funccall(Token tok) {
        Node head = new Node();
        Node cur = head;
        while (!consume(")")) {
            if (cur != head) {
                consume(",");
            }
            cur.set_next(assign(null));
            cur = cur.next();
        }
        Node node = Node.new_node(NodeKind.FuncCall, null, null);
        node.set_funcname(tok.str());
        node.set_args(head.next());
        return node;
    }

    // primary = "(" expr ")" | ident ("[" expr "]")? | ident func-args? | str | num | boolean
    private static Node primary(Type ty) {
        // 次のトークンが "(" なら、 "(" expr ")" のはず
        if (consume("(")) {
            Node node = expr(ty);
            expect(")");
            return node;
        }

        Token ident = consume_ident();
        if (ident != null) {
            // 識別子の次が括弧の場合、関数である。
            if (consume("(")) {
                return funccall(ident);
            }
            // そうでなければ変数
            Obj val = current_func.st().find_var(ident);
            // tyがnullでなければ宣言文である
            if (ty != null) {
                if (val != null && val.scope() == current_func.st().scope()) {
                    back();
                    error_at("already defined");
                }
                // valがnullでなくても、スコープが違うなら新たに宣言する
                if (consume("[")) {
                    int element = expect_number();  // 数値リテラルのみ
                    expect("]");
                    val = new Obj(ident.str(), ty, current_func.st().offset() + ty.size(), element, current_func.st().scope());
                    current_func.st().push(val, element);
                } else {
                    val = new Obj(ident.str(), ty, current_func.st().offset() + ty.size(), 0, current_func.st().scope());
                    current_func.st().push(val, 1);
                }
            } else if (val == null) {
                // ローカル変数から見つからなかったら、グローバル変数から探す
                val = global_st.find_var(ident);
                if (val == null) {
                    back();
                    error_at("cannot find symbol");
                }
            }
            // 配列
            // TODO postfix = primary ("[" expr "]")*
            if (consume("[")) {
                if (!val.is_array()) {
                    token = ident;
                    error_at("not a array");
                }
                Node node = Node.new_node(NodeKind.Array, null, null);
                node.set_element(expr(ty));
                node.set_type(val.type());
                node.set_variable(val);
                expect("]");
                return node;
            } else {
                Node node = Node.new_node(NodeKind.Var, null, null);
                node.set_offset(val.offset());
                node.set_type(val.type());
                node.set_variable(val);
                return node;
            }
        }

        // 文字列リテラル
        if (token.kind() == TokenKind.String) {
            Obj literal = global_st.find_literal(token.str());
            if (literal == null) {
                String label_name = String.format(".LC%d", global_st.label_seq());
                global_st.inc_label_seq();
                literal = new Obj(label_name, Type.String, 0, token.str().length(), 0);
                literal.set_literal(token.str());
                literal.set_is_global();
                global_st.push(literal, 0);
            }
            consume();
            Node node;
            if (consume("[")) {
                node = Node.new_node(NodeKind.Array, null, null);
                node.set_element(expr(ty));
                expect("]");
            } else {
                node = Node.new_node(NodeKind.Addr, null, null);
                node.set_element(Node.new_node_num(0));
            }
            node.set_variable(literal);
            node.set_type(Type.Char);
            return node;
        }

        // boolean型
        if (consume(TokenKind.True)) {
            Node node = Node.new_node(NodeKind.True, null, null);
            node.set_type(Type.Boolean);
            return node;
        } else if (consume(TokenKind.False)) {
            Node node = Node.new_node(NodeKind.False, null, null);
            node.set_type(Type.Boolean);
            return node;
        }

        // そうでなければ数値のはず
        Node node = Node.new_node_num(expect_number());
        node.set_type(Type.Literal);
        return node;
    }

    /** 連番をインクリメントして返す */
    private static int sequence() {
        return seq++;
    }

    /** グローバル変数用 .dataセクション コード生成 */
    private static void gen_data() {
        for (Obj obj: global_st.first()) {
            System.out.printf("    .data\n");
            System.out.printf("    .globl %s\n", obj.name());
            System.out.printf("%s:\n", obj.name());
            if (obj.type() == Type.Int) {
                System.out.printf("    .zero %d\n", obj.type().size() * obj.elements());
            } else if (obj.type() == Type.String) {
                System.out.printf("    .string \"%s\"\n", obj.literal());
            } else {
                error("unreachable");
            }
        }
    }

    /** 関数単位のコード生成 */
    private static void gen_func(Function func) {
        current_func = func;

        // 関数のラベル
        System.out.printf("    .globl %s\n", func.name());
        System.out.printf("    .text\n");
        System.out.printf("%s:\n", func.name());

        // プロローグ
        // 変数の領域を確保する
        System.out.println("    push rbp");
        System.out.println("    mov rbp, rsp");
        System.out.printf("    sub rsp, %d\n", func.st().offset());

        // レジスタから仮引数へ保存する
        int i = 0;
        for (Token param: func.params()) {
            Obj val = func.st().find_var(param);
            if (val.type().size() == 1) {
                System.out.printf("    mov [rbp-%d], %s\n", val.offset(), argregs8[i++]);
            } else {
                System.out.printf("    mov [rbp-%d], %s\n", val.offset(), argregs64[i++]);
            }
        }

        // 先頭の式から順にコード生成
        for (i = 0; i < func.size(); ++i) {
            gen(func.get(i));
        }

        // 式の評価結果としてスタックに一つの値が残っている
        // はずなので、スタックが溢れないようにポップしておく
        System.out.println("    pop rax");

        // エピローグ
        // 最後の式の結果がRAXに残っているのでそれが返り値になる
        System.out.println("    mov rsp, rbp");
        System.out.println("    pop rbp");
        System.out.println("    ret");
    }

    /** 式を左辺値として評価 */
    private static void gen_val(Node node) {
        if (node.kind() != NodeKind.Var) {
            error("not a variable");
        }
        if (node.variable().is_local()) {
            System.out.printf("    mov rax, rbp\n");
            System.out.printf("    sub rax, %d\n", node.offset());
        } else {
            System.out.printf("    lea rax, %s[rip]\n", node.variable().name());
        }
        System.out.printf("    push rax\n");
    }

    /** 式を左辺値として評価(配列) */
    private static void gen_array(Node node) {
        gen(node.element());
        System.out.printf("    pop rdx\n");
        System.out.printf("    imul rdx, %d\n", node.type().size());
        if (node.variable().is_local()) {
            System.out.printf("    mov rax, rbp\n");
            System.out.printf("    sub rax, %d\n", node.offset());
            System.out.printf("    sub rax, rdx\n");
        } else {
            System.out.printf("    lea rax, %s[rip]\n", node.variable().name());
            System.out.printf("    add rax, rdx\n");
        }
        System.out.printf("    push rax\n");
    }

    /** コード生成 */
    private static void gen(Node node) {
        switch (node.kind()) {
            case Num:
                System.out.printf("    push %d\n", node.val());
                return;
            case True:
                System.out.println("    push 1");
                return;
            case False:
                System.out.println("    push 0");
                return;
            case Var:
                gen_val(node);
                System.out.println("    pop rax");
                if (node.type().size() == 1) {
                    System.out.println("    movsx rax, BYTE PTR [rax]");
                } else {
                    System.out.println("    mov rax, [rax]");
                }
                System.out.println("    push rax");
                return;
            case Array:
                gen_array(node);
                System.out.println("    pop rax");
                if (node.type().size() == 1) {
                    System.out.println("    movsx rax, BYTE PTR [rax]");
                } else {
                    System.out.println("    mov rax, [rax]");
                }
                System.out.println("    push rax");
                return;
            case Addr:
                gen_array(node);
                return;
            case Assign:
                // 左辺は変数か配列でなければならない
                switch (node.lhs().kind()) {
                    case Var:   gen_val  (node.lhs()); break;
                    case Array: gen_array(node.lhs()); break;
                    default: error("invalid assign");
                }
                gen(node.rhs());
                System.out.println("    pop rdi");  // rhsの結果
                System.out.println("    pop rax");  // lhsのアドレス
                if (node.lhs().type().size() == 1) {
                    System.out.println("    mov [rax], dil");
                } else {
                    System.out.println("    mov [rax], rdi");
                }
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
            case FuncCall:
                int nargs = 0;
                for (Node arg = node.args(); arg != null; arg = arg.next()) {
                    // 実引数の計算結果(rax)をスタックにpushしている
                    gen(arg);
                    ++nargs;
                }
                // 6つより多い引数はとりあえず実装しない
                if (6 < nargs) {
                    error("Not supported arguments greater than 6: '%s'", node.funcname());
                }
                // 引数6以下は専用のレジスタに格納
                for (int i = nargs - 1; 0 <= i; --i) {
                    // スタックから引数をレジスタにpop
                    System.out.printf("    pop %s\n", argregs64[i]);
                }
                System.out.printf("    mov al, 0\n");
                System.out.printf("    call %s\n", node.funcname());
                System.out.printf("    push rax\n");
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
            case Do: {
                int count = sequence();
                System.out.printf(".L.begin.%d:\n", count);
                gen(node.then());
                gen(node.cond());
                System.out.println("    pop rax");
                System.out.println("    cmp rax, 0");
                System.out.printf("    jne .L.begin.%d\n", count);
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

