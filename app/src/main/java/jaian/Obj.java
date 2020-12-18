package jaian;

/** ローカル変数の型 */
// リンクトリストで管理する
public class Obj {
    String name;  /** 変数の名前 */
    Type type;    /** 変数の型 */
    int offset;   /** RBPからのオフセット */
    //int scope;    /** スコープレベル */ TODO

    // コンストラクタ
    public Obj(String name, Type type, int offset) {
        this.name   = name;
        this.type   = type;
        this.offset = offset;
    }

    /** 変数の長さを返す。 */
    public int len() {
        return this.name.length();
    }

    // Getters
    public String name() { return this.name; }
    public Type type()   { return this.type; }
    public int offset()  { return this.offset; }
}

