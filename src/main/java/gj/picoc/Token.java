package gj.picoc;

public class Token {

    public enum TokenType {
        KW_INT, KW_FLOAT, KW_IF, KW_ELSE, KW_WHILE, KW_OUT, // keywords
        LPAR, RPAR, LBRA, RBRA, COMMA, SEMI,                // braces, punctuation
        CMP_G, CMP_S, CMP_GE, CMP_SE, CMP_EQUALS, CMP_NE,  // tests
        ASSIGN,                                             // assignment
        PLUS, MINUS, MUL, DIV,                              // math
        OR, AND, NOT,                                       // logic
        VAL_INT, VAL_FLOAT, ID, EOF                         // constants, ids, EOF
    }

    public static TokenType[] TYPES = { TokenType.KW_INT, TokenType.KW_FLOAT };
    public static TokenType[] BOOL_OP = { TokenType.CMP_G, TokenType.CMP_S, TokenType.CMP_GE, TokenType.CMP_SE,
            TokenType.CMP_EQUALS, TokenType.CMP_NE };
    public static TokenType[] ADDITION_OP = { TokenType.PLUS, TokenType.MINUS };
    public static TokenType[] MULTIPLY_OP = { TokenType.MUL, TokenType.DIV };

    // For each token, we store its type, the lexeme as well as the line number ('lineNumber' on which it occurs.  The
    // line number is useful for error reporting.
    private final TokenType type;
    private final String lexeme;
    private final int lineNumber;

    public Token(TokenType type, String lexeme, int lineNumber) {
        this.type = type;
        this.lexeme = lexeme;
        this.lineNumber = lineNumber;
    }

//    public Token(TokenType type, int lineNumber) {
//        this(type, null, lineNumber);
//    }

    public TokenType getType() {
        return type;
    }
    public String getLexeme() {
        return lexeme;
    }
    public int getLineNumber() { return  lineNumber; }

    @Override
    public String toString() {
        return "Token{type=" + type +
                (lexeme != null ? ", lexeme='" + lexeme + "', lineNumber=" + lineNumber + "}"
                        : ", lineNumber=" + lineNumber + "}");
    }
}
