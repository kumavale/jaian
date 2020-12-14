package jaian;

import java.util.ArrayList;

/** シンボルテーブル */
public class SymbolTable {
    ArrayList<Obj> objs;  /** 変数リスト */

    // コンストラクタ
    public SymbolTable() {
        this.objs = new ArrayList<Obj>();
    }

    /** 変数を名前で検索する。見つからなかった場合はnullを返す。 */
    public Obj find_var(Token tok) {
        for (Obj obj: this.objs) {
            if (obj.len() == tok.len() && obj.name().equals(tok.str())) {
                return obj;
            }
        }
        return null;
    }

    /** 変数の追加 */
    public void push(Obj obj) {
        this.objs.add(obj);
    }

    /** RBPからのオフセットを返す */
    public int offset() {
        return this.objs.size() * 8;
    }
}

