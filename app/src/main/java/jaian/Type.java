package jaian;

/** 型の種類 */
public enum Type {
    Literal,  /** 数値リテラル */  // IntかCharになる
    String,   /** 文字列リテラル */
    Int,      /** "int" */
    Char,     /** "char" */
    Boolean,  /** "boolean" */
    ;

    /** 型のサイズ(byte)を返す。 */
    public int size() {
        switch (this) {
            case Literal: return 8;
            case Int:     return 8;
            case Char:    return 1;
            case String:  return 1;
            case Boolean: return 1;
            default:
                App.error("unreachable");
                return 0;
        }
    }
}

