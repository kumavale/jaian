package jaian;

/** 抽象構文木のノードの型 */
public class Node {
    private NodeKind kind;  /** ノードの型 */
    private Node lhs;       /** 左辺 */
    private Node rhs;       /** 右辺 */
    private int val;        /** kindがNumの場合のみ使用 */
    private int offset;     /** kindがVarの場合のみ使用 */

    private String funcname;  /** 関数名 */
    private Node args;        /** 関数の引数 */

    // "if"|"while"|"for" statement
    private Node cond;  /** 条件式 */
    private Node then;  /** 真の処理 */
    private Node els;   /** 偽の処理 */

    // "for" statement
    private Node init;  /** 初期化 */
    private Node inc;   /** インクリメント */

    // Block "{" ... "}"
    private Node next;  /** 次のstmt。ない場合はnull */
    private Node body;  /** ブロック内のstmt */

    public static Node new_node(NodeKind kind, Node lhs, Node rhs) {
        Node node = new Node();
        node.kind = kind;
        node.lhs  = lhs;
        node.rhs  = rhs;
        return node;
    }

    // 数値リテラル
    public static Node new_node_num(int val) {
        Node node = new Node();
        node.kind = NodeKind.Num;
        node.val  = val;
        return node;
    }

    // Getters
    public NodeKind kind()   { return this.kind; }
    public Node lhs()        { return this.lhs; }
    public Node rhs()        { return this.rhs; }
    public Node cond()       { return this.cond; }
    public Node then()       { return this.then; }
    public Node els()        { return this.els; }
    public Node init()       { return this.init; }
    public Node inc()        { return this.inc; }
    public Node next()       { return this.next; }
    public Node body()       { return this.body; }
    public int val()         { return this.val; }
    public int offset()      { return this.offset; }
    public Node element()    { return this.body; }
    public String funcname() { return this.funcname; }
    public Node args()       { return this.args; }

    // Setters
    public void set_offset(int offset)    { this.offset   = offset; }
    public void set_cond(Node expr)       { this.cond     = expr; }
    public void set_then(Node stmt)       { this.then     = stmt; }
    public void set_els(Node stmt)        { this.els      = stmt; }
    public void set_init(Node expr)       { this.init     = expr; }
    public void set_inc(Node expr)        { this.inc      = expr; }
    public void set_next(Node node)       { this.next     = node; }
    public void set_body(Node stmt)       { this.body     = stmt; }
    public void set_element(Node stmt)    { this.body     = stmt; }
    public void set_funcname(String name) { this.funcname = name; }
    public void set_args(Node args)       { this.args     = args; }
}

