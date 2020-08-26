package gj.picoc;

/**
 * Scanner component for SimpleSee language.
 *
 * Tokens: if, else, while ( ) { } int float, = , ; out > < <= >= + - * / || && ! <int> <float> <id>
 */
public class Scanner {

    // Define EOF to be the Unicode character for the end-of-line.
    private static final char EOF = '\u001a';

    // Store the entire program in a String and define 'charPointer' that will be used to point to the next character to
    // process.
    private final String program;
    private int charPointer;

    // We store the next token to return in token.  This is used so that 'peekToken' can be supported.  If we call
    // 'peekToken' we can temporarily store the token so that ether 'peekToken' or 'nextToken' can still return it.
    private Token token;

    // As we process characters, lineNumber is incremented for every '\n' we encounter.
    private int lineNumber;

    public Scanner(String program) {
        this.program = program;
        this.charPointer = 0;
        this.lineNumber = 1;
    }

    // Returns the current character in the program (or EOF) and increment the charPointer to advance.
    private char nextChar() {
        return charPointer >= program.length() ? EOF : program.charAt(charPointer++);
    }

    // Returns the current character in the program (or EOF), but do NOT advance the charPointer.
    private char peekChar() {
        return charPointer >= program.length() ? EOF : program.charAt(charPointer);
    }

    // The next three methods checks for the type of the character.  It is also possible to use the build in function
    // for the Character class, i.e. Character.isDigit(), Character.isAlpha() and Character.isWhiteSpace() but these
    // also return True for unicode characters.  This scanner should only accept a limited set as digit, alpha and
    // white space so these are redefined here.

    private boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private boolean isAlpha(char ch) {
        return ch >= 'a' && ch <= 'z';
    }

    private boolean isWhiteSpace(char ch) {
        return ch == ' ' || ch == '\n' || ch == '\t' || ch == '\r';
    }


    // Handle comments (c-style)
    private char handleComments(char ch) {
        if (ch == '/' && peekChar() == '/') {
            while (ch != '\n' && ch != EOF) ch = nextChar();
        }
        return handleWhiteSpace(ch);
    }

    // All kinds of white-space.
    private char handleWhiteSpace(char ch) {
        while (isWhiteSpace(ch)) {
            if (ch == '\n') {
                lineNumber++;
            }
            ch = nextChar();
        }
        return ch;
    }

    // Special characters forming double-character tokens.
    private Token handleDoubleSpecial(char ch) {
        if ((String.valueOf(ch) + peekChar()).equals(">=")) {
            nextChar();
            return new Token(Token.TokenType.CMP_GE, ">=", lineNumber);
        } else if ((String.valueOf(ch) + peekChar()).equals("<=")) {
            nextChar();
            return new Token(Token.TokenType.CMP_SE, "<=", lineNumber);
        } else if ((String.valueOf(ch) + peekChar()).equals("==")) {
            nextChar();
            return new Token(Token.TokenType.CMP_EQUALS, "==", lineNumber);
        } else if ((String.valueOf(ch) + peekChar()).equals("||")) {
            nextChar();
            return new Token(Token.TokenType.OR, "||", lineNumber);
        } else if ((String.valueOf(ch) + peekChar()).equals("&&")) {
            nextChar();
            return new Token(Token.TokenType.AND, "&&", lineNumber);
        } else if ((String.valueOf(ch) + peekChar()).equals("!=")) {
            nextChar();
            return new Token(Token.TokenType.CMP_NE, "!=", lineNumber);
        } else if (ch == '>') {
            return new Token(Token.TokenType.CMP_G, ">", lineNumber);
        } else if (ch == '<') {
            return new Token(Token.TokenType.CMP_S, "<", lineNumber);
        } else if (ch == '=') {
            return new Token(Token.TokenType.ASSIGN, "=", lineNumber);
        } else if (ch == '!') {
            return new Token(Token.TokenType.NOT, "!", lineNumber);
        }
        return null;
    }

    // Handles numbers and the minus sign.
    private Token handleNumbersAndNegativeSign(char ch) {

        // Handle the MINUS token as well as negative numbers.
        StringBuilder builder = new StringBuilder();
        boolean isNegative = false;
        if (ch == '-') {
            if (!isDigit(peekChar())) {
                return new Token(Token.TokenType.MINUS, "-", lineNumber);
            }
            ch = nextChar();
            builder.append("-");
        }

        // If the current number is not a digit, return immediately.
        if (!isDigit(ch)) {
            return null;
        }

        // Get the number, either INT or FLOAT.
        builder.append(ch);
        while (isDigit(peekChar()) || peekChar() == '.') {
            builder.append (nextChar());
        }
        if (builder.toString().contains(".")) {
            return new Token(Token.TokenType.VAL_FLOAT, builder.toString(), lineNumber);
        }
        return new Token(Token.TokenType.VAL_INT, builder.toString(), lineNumber);
    }

    // Keywords and ids.
    private Token handleKeywordsAndIDs(char ch) {
        if (!isAlpha(ch)) {
            return null;
        }

        StringBuilder tmp = new StringBuilder(String.valueOf(ch));
        while (isAlpha(peekChar()) || isDigit(peekChar())) {
            tmp.append(nextChar());
        }
        String alpha = tmp.toString();

        // A keyword?
        if (alpha.equals("int")) return new Token(Token.TokenType.KW_INT, "int", lineNumber);
        if (alpha.equals("float")) return new Token(Token.TokenType.KW_FLOAT, "float", lineNumber);
        if (alpha.equals("if")) return new Token(Token.TokenType.KW_IF, "if", lineNumber);
        if (alpha.equals("else")) return new Token(Token.TokenType.KW_ELSE, "else", lineNumber);
        if (alpha.equals("while")) return new Token(Token.TokenType.KW_WHILE, "while", lineNumber);
        if (alpha.equals("out")) return new Token(Token.TokenType.KW_OUT, "out", lineNumber);

        // Then it must be an id.
        return new Token(Token.TokenType.ID, tmp.toString(), lineNumber);
    }

    // Find the next token.
    private Token next() {
        char ch = nextChar();

        // Did we hit raw whitespace?  Remove until we find the next usable character.
        ch = handleWhiteSpace(ch);

        // Did we hit comments? Trim it out.
        ch = handleComments(ch);

        switch (ch) {
            case '(': return new Token(Token.TokenType.LPAR, "(", lineNumber);
            case ')': return new Token(Token.TokenType.RPAR, ")", lineNumber);
            case '{': return new Token(Token.TokenType.LBRA, "{", lineNumber);
            case '}': return new Token(Token.TokenType.RBRA, "}", lineNumber);
            case ',': return new Token(Token.TokenType.COMMA, ",", lineNumber);
            case ';': return new Token(Token.TokenType.SEMI, ";", lineNumber);
            case '+': return new Token(Token.TokenType.PLUS, "+", lineNumber);
            case '*': return new Token(Token.TokenType.MUL, "*", lineNumber);
            case '/': return new Token(Token.TokenType.DIV, "/", lineNumber);
            case EOF: return new Token(Token.TokenType.EOF, "EOF", lineNumber);

            default: {
                // Characters that may be part of double "special character" tokens, these are:
                // <, <=, >, >=, =, ==, ||, &&, !=, !
                Token t = handleDoubleSpecial(ch);
                if (t != null) {
                    return t;
                }

                // Integers, floats and the minus
                t = handleNumbersAndNegativeSign(ch);
                if (t != null) {
                    return t;
                }

                // Reserved Keywords
                t = handleKeywordsAndIDs(ch);
                if (t != null) {
                    return t;
                }

                throw new RuntimeException("Lexical error, unknown character: " + ch + "(ordinal=" + (int)ch + ")");
            }
        }
    }

    /**
     * Peek ahead what the next token is without advancing.
     *
     * @return current token.
     */
    public Token peekToken() {
        // If token is null, get the next token.
        if (token == null) {
            token = next();
        }
        // ...and simply return it.
       // System.out.println(token.getLexeme());
        return token;
    }

    /**
     * Return the next token and advance.
     *
     * @return current token.
     */
    public Token nextToken() {
        // If token is null, get the next token.
        if (token == null) {
            token = next();
        }
        Token result = token;
        token = null; // set token to null so that both 'peekToken' and 'nextToken' is force to call next().
        //System.out.println(result.getLexeme());
        return result;
    }
}
