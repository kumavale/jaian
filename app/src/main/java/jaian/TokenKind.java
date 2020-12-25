package jaian;

/** トークンの種類 */
public enum TokenKind {
    Ident,    /** 識別子 */
    String,   /** 文字列リテラル */
    Ch,       /** charリテラル */
    Num,      /** 整数 */
    Punct,    /** 区切り文字 */
    EOF,      /** 入力の終わり */
    Invalid,  /** 無効なトークン */

    // Types
    Int,   /** "int" */
    Char,  /** "char" */
    Bool,  /** "bool" */

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

