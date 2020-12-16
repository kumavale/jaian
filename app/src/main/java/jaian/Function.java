package jaian;

import java.util.List;
import java.util.ArrayList;

/** 関数 */
public class Function {
    private String name;            /** 関数名 */
    private List<Node> statements;  /** 関数内の式や文 */

    // コンストラクタ
    public Function() {
        this.statements = new ArrayList<Node>();
    }

    // Getters
    public String name()           { return this.name; }
    public List<Node> statements() { return this.statements; }

    // Setters
    public void set_name(String name) { this.name = name; }

    /** statementsの追加 */
    public void push(Node stmt) {
        this.statements.add(stmt);
    }

    /** statementsの取得 */
    public Node get(int index) {
        return this.statements.get(index);
    }

    /** statementsのサイズを返す */
    public int size() {
        return this.statements.size();
    }
}

