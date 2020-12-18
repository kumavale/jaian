package jaian;

/** トークンの種類 */
public enum TokenKind {
    Ident,  /** 識別子 */
    Punct,  /** 区切り文字 */
    Num,    /** 整数 */
    EOF,    /** 入力の終わり */

    // Types
    Int,      /** "int" */
    Boolean,  /** "boolean" */

    // Keywords
    If,      /** "if" */
    Else,    /** "else" */
    While,   /** "while" */
    For,     /** "for" */
    Return,  /** "return" */
    True,    /** "true" */
    False,   /** "false" */
}

