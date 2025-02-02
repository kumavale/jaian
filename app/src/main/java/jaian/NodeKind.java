package jaian;

/** 抽象構文木のノードの種類 */
public enum NodeKind {
    Add,        /** + */
    Sub,        /** - */
    Mul,        /** * */
    Div,        /** / */
    Neg,        /** unary - */
    Eq,         /** == */
    Ne,         /** != */
    Lt,         /** < */
    Le,         /** <= */
    Assign,     /** = */
    Block,      /** "{" stmt* "}" */
    BlockExpr,  /** "(" "{" stmt* "}" ")" */
    ExprStmt,   /** */
    Var,        /** 変数 */
    Array,      /** 配列 */
    Addr,       /** アドレス */
    Num,        /** 整数 */
    FuncCall,   /** 関数呼び出し */

    // Keywords
    If,      /** "if" */
    Else,    /** "else" */
    Do,      /** "do" */
    While,   /** "while" */
    For,     /** "for" */
    Return,  /** "return" */
    True,    /** "true" */
    False,   /** "false" */
}

