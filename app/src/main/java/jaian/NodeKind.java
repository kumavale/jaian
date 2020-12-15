package jaian;

/** 抽象構文木のノードの種類 */
public enum NodeKind {
    Add,     /** + */
    Sub,     /** - */
    Mul,     /** * */
    Div,     /** / */
    Eq,      /** == */
    Ne,      /** != */
    Lt,      /** < */
    Le,      /** <= */
    Assign,  /** = */
    Var,     /** 変数 */
    Num,     /** 整数 */

    // Keywords
    If,      /** "if" */
    Else,    /** "else" */
    While,   /** "while" */
    Return,  /** "return" */
}

