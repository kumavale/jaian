package jaian;

/** トークン型 */
public class Token {
    private TokenKind kind;     /** トークンの型 */
    private Token next;         /** 次のトークン */
    private int val;            /** kindがNumの場合、その数値 */
    private int cur;            /** 現在のインデックス */
    private static String src;  /** 入力文字列 */

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
            if (is_whitespace(ch)) {
                continue;
            }

            if (ch == '+' || ch == '-') {
                cur = new_token(TokenKind.Reserved, cur, idx);
                continue;
            }

            if (Character.isDigit(ch)) {
                cur = new_token(TokenKind.Num, cur, idx);
                int begin = idx;
                while (idx+1 < src.length() && Character.isDigit(src.charAt(idx+1))) { ++idx; };
                cur.val = Integer.parseInt(src.substring(begin, idx+1));
                continue;
            }

            App.error("Failed tokenize");
        }

        new_token(TokenKind.EOF, cur, idx);
        return head.next;
    }

    /** 新しいトークンを作成してcurに繋げる */
    public static Token new_token(TokenKind kind, Token cur, int index) {
        Token tok = new Token();
        tok.kind = kind;
        cur.next = tok;
        tok.cur = index;
        return tok;
    }

    /** 現在のインデックスと引数で渡されたインデックスを合計したインデックスの文字を返す */
    public char cur(int index) {
        return src.charAt(this.cur + index);
    }

    /** tokenがEOFかどうか */
    public boolean at_eof() {
        return this.kind == TokenKind.EOF;
    }

    // Getters
    public TokenKind kind() { return this.kind; }
    public Token next()     { return this.next; }
    public int val()        { return this.val; }

    /** 空白文字ならtrueを返す */
    private static boolean is_whitespace(char c) {
        return c == ' ' || c == '\n' || c == '\r' || c == '\t';
    }
}

