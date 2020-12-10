package jaian;

/** 抽象構文木のノードの種類 */
public enum NodeKind {
    Add,  /** + */
    Sub,  /** - */
    Mul,  /** * */
    Div,  /** / */
    Eq,   /** == */
    Ne,   /** != */
    Lt,   /** < */
    Le,   /** <= */
    Num,  /** 整数 */
}

