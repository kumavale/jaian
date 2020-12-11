package jaian;

/** 抽象構文木のノードの型 */
public class Node {
    private NodeKind kind;  /** ノードの型 */
    private Node lhs;       /** 左辺 */
    private Node rhs;       /** 右辺 */
    private int val;        /** kindがNumの場合のみ使用 */
    private int offset;     /** kindがVarの場合のみ使用 */

    public static Node new_node(NodeKind kind, Node lhs, Node rhs) {
        Node node = new Node();
        node.kind = kind;
        node.lhs  = lhs;
        node.rhs  = rhs;
        return node;
    }

    public static Node new_node_num(int val) {
        Node node = new Node();
        node.kind = NodeKind.Num;
        node.val  = val;
        return node;
    }

    // Getters
    public NodeKind kind() { return this.kind; }
    public Node lhs()      { return this.lhs; }
    public Node rhs()      { return this.rhs; }
    public int val()       { return this.val; }
    public int offset()    { return this.offset; }

    // Setters
    public void set_offset(int offset) { this.offset = offset; }
}

