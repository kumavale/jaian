package jaian;

/** ローカル変数の型 */
// リンクトリストで管理する
public class Obj {
    String name;  /** 変数の名前 */
    int offset;   /** RBPからのオフセット */
    //int scope;    /** スコープレベル */ TODO

    // コンストラクタ
    public Obj(String name, int offset) {
        this.name   = name;
        this.offset = offset;
    }

    /** 変数の長さを返す。 */
    public int len() {
        return this.name.length();
    }

    // Getters
    public String name() { return this.name; }
    public int offset()  { return this.offset; }
}

