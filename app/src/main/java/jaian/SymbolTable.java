package jaian;

import java.util.List;
import java.util.ArrayList;

/** シンボルテーブル */
public class SymbolTable {
    List<List<Obj>> objs;  /** スコープ毎の変数リスト */
    int offset;            /** RBPからのオフセット */
    int scope;             /** スコープの深さ */

    // コンストラクタ
    public SymbolTable() {
        this.objs   = new ArrayList<List<Obj>>();
        this.objs.add(new ArrayList<Obj>());
        this.offset = 0;
        this.scope  = 0;
    }

    /** 変数を名前で検索する。見つからなかった場合はnullを返す。 */
    public Obj find_var(Token tok) {
        for (int i = this.objs.size()-1; 0 <= i; --i) {
            for (Obj obj: this.objs.get(i)) {
                if (obj.len() == tok.len() && obj.name().equals(tok.str())) {
                    return obj;
                }
            }
        }
        return null;
    }

    /** 変数の追加 */
    public void push(Obj obj, int element) {
        this.objs.get(this.scope).add(obj);
        this.offset += obj.type().size() * element;
    }

    /** 新たなスコープに入る */
    public void scope_in() {
        ++this.scope;
        if (this.objs.size() <= this.scope) {
            this.objs.add(new ArrayList<Obj>());
        }
    }

    /** スコープから出る */
    public void scope_out() {
        if (0 < this.scope) {
            if (this.scope < this.objs.size()) {
                this.objs.remove(this.scope);
            }
            --this.scope;
        }
    }

    /** objsの最初の要素を返す。 */
    public List<Obj> first() {
        return this.objs.get(0);
    }

    // Getters
    public int offset() { return this.offset; }
    public int scope()  { return this.scope; }
}

