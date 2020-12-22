package jaian;

/** ローカル変数の型 */
public class Obj {
    String name;       /** 変数の名前 */
    Type type;         /** 変数の型 */
    int offset;        /** RBPからのオフセット */
    int elements;      /** 要素数 */ // 配列でない場合: 0
    int scope;         /** スコープレベル */
    boolean is_local;  /** ローカル変数なのかグローバル変数なのか */
    String literal;    /** 文字列リテラルの場合、その文字列が入る */

    // コンストラクタ
    public Obj(String name, Type type, int offset, int elements, int scope) {
        this.name     = name;
        this.type     = type;
        this.offset   = offset;
        this.elements = elements;
        this.scope    = scope;
        this.is_local = true;
    }

    /** 変数の長さを返す。 */
    public int len() {
        return this.name.length();
    }

    /** 配列か否かを返す。 */
    public boolean is_array() {
        return 0 < this.elements;
    }

    /** グローバル変数として宣言 */
    public void set_is_global() {
        this.is_local = false;
    }

    // Getters
    public String name()      { return this.name; }
    public Type type()        { return this.type; }
    public int offset()       { return this.offset; }
    public int elements()     { return this.elements <= 0 ? 1 : this.elements; }
    public int scope()        { return this.scope; }
    public boolean is_local() { return this.is_local; }
    public String literal()   { return this.literal; }

    // Setters
    public void set_literal(String literal) { this.literal = literal; }
}

