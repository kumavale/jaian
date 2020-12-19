package jaian;

/** ローカル変数の型 */
public class Obj {
    String name;       /** 変数の名前 */
    Type type;         /** 変数の型 */
    int offset;        /** RBPからのオフセット */
    int elements;      /** 要素数 */ // 配列でない場合: 0
    //int scope;    /** スコープレベル */ TODO

    // コンストラクタ
    public Obj(String name, Type type, int offset, int elements) {
        this.name     = name;
        this.type     = type;
        this.offset   = offset;
        this.elements = elements;
    }

    /** 変数の長さを返す。 */
    public int len() {
        return this.name.length();
    }

    /** 配列か否かを返す。 */
    public boolean is_array() {
        return 0 < this.elements;
    }

    // Getters
    public String name()  { return this.name; }
    public Type type()    { return this.type; }
    public int offset()   { return this.offset; }
    public int elements() { return this.elements; }
}

