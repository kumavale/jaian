package jaian;

/** トークン型 */
public class Token {
    private static String src;  /** 入力文字列 */

    private TokenKind kind;     /** トークンの型 */
    private Token next;         /** 次のトークン */
    private int idx;            /** 現在のインデックス */
    private int len;            /** トークンの長さ */

    /** Tokenクラスの初期化 */
    public Token() {}
    /** 入力文字列をトークナイズしてそれを返す */
    public static Token tokenize(String src) {
        Token.src = src;
        Token head = new Token();
        Token cur = head;
        int idx = 0;

        for (; idx < src.length(); ++idx) {
            char ch = src.charAt(idx);

            // 空白文字をスキップ
            if (is_whitespace(ch)) {
                continue;
            }

            // 整数リテラル
            if (Character.isDigit(ch)) {
                cur = new_token(TokenKind.Num, cur, idx, 1);
                while (idx+1 < src.length() && Character.isDigit(src.charAt(idx+1))) {
                    ++cur.len;
                    ++idx;
                };
                continue;
            }

            // 識別子 もしくは 予約語
            // アルファベットから始まり、アルファベットか数字かアンダースコアが続く
            if (Character.isAlphabetic(ch)) {
                int begin = idx;
                while (idx+1 < src.length()) {
                    char ch2 = src.charAt(idx+1);
                    if (is_alnum_(ch2)) {
                        ++idx;
                    } else {
                        break;
                    }
                }
                switch (src.substring(begin, idx + 1)) {
                    case "if":     cur = new_token(TokenKind.If, cur, begin, idx - begin + 1); break;
                    case "else":   cur = new_token(TokenKind.Else, cur, begin, idx - begin + 1); break;
                    case "while":  cur = new_token(TokenKind.While, cur, begin, idx - begin + 1); break;
                    case "for":    cur = new_token(TokenKind.For, cur, begin, idx - begin + 1); break;
                    case "return": cur = new_token(TokenKind.Return, cur, begin, idx - begin + 1); break;
                    default:       cur = new_token(TokenKind.Ident, cur, begin, idx - begin + 1);
                }
                continue;
            }

            // "==", "!=", "<=", ">="
            if (("=!<>".indexOf(ch) != -1) && (idx < src.length()) && (src.charAt(idx+1) == '=')) {
                cur = new_token(TokenKind.Punct, cur, idx++, 2);
                continue;
            }

            // 区切り文字
            if ("+-*/()<>=;".indexOf(ch) != -1) {
                cur = new_token(TokenKind.Punct, cur, idx, 1);
                continue;
            }

            App.error_at("invalid token");
        }

        new_token(TokenKind.EOF, cur, idx, 0);
        return head.next;
    }

    /** 新しいトークンを作成してcurに繋げる */
    public static Token new_token(TokenKind kind, Token cur, int index, int len) {
        Token tok = new Token();
        tok.kind = kind;
        cur.next = tok;
        tok.idx  = index;
        tok.len  = len;
        return tok;
    }

    /** 現在のトークンの文字列を返す */
    public String str() {
        return this.src.substring(this.idx, this.idx + this.len);
    }

    /** 整数を返す */
    public int val() {
        return Integer.parseInt(this.str());
    }

    /** tokenがEOFかどうか */
    public boolean at_eof() {
        return this.kind == TokenKind.EOF;
    }

    // Getters
    public String src()     { return this.src; }
    public TokenKind kind() { return this.kind; }
    public Token next()     { return this.next; }
    public int idx()        { return this.idx; }
    public int len()        { return this.len; }

    /** 空白文字ならtrueを返す */
    private static boolean is_whitespace(char c) {
        return c == ' ' || c == '\n' || c == '\r' || c == '\t';
    }

    /** アルファベット、数字、アンダースコアであればtrueを返す */
    private static boolean is_alnum_(char c) {
        return Character.isAlphabetic(c) || Character.isDigit(c) || c == '_';
    }
}

