package jaian;

import java.util.List;
import java.util.ArrayList;

/** シンボルテーブル */
public class SymbolTable {
    List<Obj> objs;  /** 変数リスト */
    int offset;      /** RBPからのオフセット */

    // コンストラクタ
    public SymbolTable() {
        this.objs   = new ArrayList<Obj>();
        this.offset = 0;
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
    public void push(Obj obj, int element) {
        this.objs.add(obj);
        this.offset += obj.type().size() * element;
    }

    // Getters
    public int offset() {
        return this.offset;
    }
}

