package jaian;

/** トークンの種類 */
public enum TokenKind {
    Ident,   /** 識別子 */
    String,  /** 文字列リテラル */
    Punct,   /** 区切り文字 */
    Num,     /** 整数 */
    EOF,     /** 入力の終わり */

    // Types
    Int,      /** "int" */
    Char,     /** "char" */
    Boolean,  /** "boolean" */

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

