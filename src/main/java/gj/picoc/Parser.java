package gj.picoc;

/* A note on the syntax:
 * ---------------------
 * Using a variation on BNF where [] is optional and {} refers to zero or more.  Actual keywords/tokens inside double
 * quotes.
 *
 * <program> ::= <statement>
 * <statement> ::= <if_statement> |
 *                 <while_statement> |
 *                 <block_statement> |
 *                 <output_statement> |
 *                 <assignment_statement> |
 *                 ";"
 *
 * <if_statement> ::= "if" "(" <expr> ")" <statement> [ "else" <statement> ]
 * <while_statement> ::= "while" "(" <expr> ")" <statement>
 * <block_statement> ::= "{" { <statement> } "}"
 * <output_statement> ::=  "out" "(" <simple_expr> ")" ";"
 * <assignment_statement> ::= <id> "=" <simple_expr> {"," <id> "=" <simple_expr> } ";"
 * <declare_statement> ::= <type_specifier> <id> ["=" <simple_expr> ] {"," <id> ["="  <simple_expr> ] } ";"

 * <type_specifier> ::= <int> | <float>
 *
 * <bool_expression> ::= <bool_term> { "||" <bool_term> }
 * <bool_term ::= <bool_factor> { "&&" <bool_factor> }
 * <bool_factor> ::= ["!"] ( <id> | <relation> )
 * <relation> ::= <expression> [ (">" | "<" | "<=" | ">=" | "==" | "=") <expression> ]
 * <expression> :== <term> { ("+"|"-") <term> }
 * <term> :== ("+"|"-") <factor> { ("*" "/") <factor> }
 * <factor> :==  <id> | <int> | <float> | "(" bool_expression ")"
 *
 * <id> ::= <letter> { <letter> | <digit> }
 * <int> ::= ["+"|"-"] ( 0 | ("1".."9") { <digit> } )
 * <float> ::= { int } ) [ "." { digit } ]
 * <letter> := "a".."z", "A".."Z"
 * <digit> := "0"..9"
 *
 * Tokens: if, else, while ( ) { } int float, = , ; out > < <= >= == + - * / || && ! <int> <float> <id>
 */

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final Scanner scanner;

    public Parser(Scanner scanner) {
        this.scanner = scanner;
    }

    // --- Convenience, house-keeping functions. ---
    // It is ALWAYS a good idea to write a few housekeeping functions to make the actual parsing functions (which can
    // get tricky enough) as short as possible.

    // Convenience method to check for a specific token expected at a location.
    private Token mustHave(Token.TokenType type, String looksLike) {
        Token token = scanner.nextToken();
        if (token.getType() != type) {
            throw new RuntimeException(String.format("%s expected, line %d.", looksLike, token.getLineNumber()));
        }
        return token;
    }

    // Convenience method to check if thing is in things.
    private <T> boolean in(T thing, T... things) {
        for (T e : things) {
            if (e.equals(thing)) return true;
        }
        return false;
    }

    // This converts blocks of statements and declarations into a sequence tree structure
    private Node listToTree(List<Node> list) {

        // Optimisation: If the list contains a single sequence, collapse immediately.
        if (list.size() == 1 && list.get(0).getType() == Node.NodeType.SEQ) {
            return list.get(0);
        }

        int child = 0;
        Node root = new Node(Node.NodeType.SEQ);
        for (Node node : list) {
            root.setChild(child++, node);
            if (child > 2) {
                Node tmp = new Node(Node.NodeType.SEQ, null, root);
                root = tmp;
                child = 1;
            }
        }
        return root;
    }

    // --- Start of recursive descent methods ---
    // The break in Java convention for method names is intensional.  It mimics the CFG's non-terminals.

    // <program> ::= <statement>
    public Node program() {
        Node program = new Node(Node.NodeType.PROG);
        program.setChild(0, statement());

        // No garbage at the end.
        if (scanner.nextToken().getType() != Token.TokenType.EOF) {
            throw new RuntimeException("End-of-file expected.");
        }
        return program;
    }

    //   <statement> ::= <if_statement> |
    //                   <while_statement> |
    //                   <block_statement> |
    //                   <output_statement> |
    //                   <declare_statement> |
    //                   <assignment_statement> |
    //                   ";"
    private Node statement() {
        Token peek = scanner.peekToken();
        if (peek.getType() == Token.TokenType.KW_IF) {
            return if_statement();
        } else
        if (peek.getType() == Token.TokenType.KW_WHILE) {
            return while_statement();
        } else
        if (peek.getType() == Token.TokenType.LBRA) {
            return block_statement();
        } else
        if (peek.getType() == Token.TokenType.KW_OUT) {
            return output_statement();
        } else
        if (in(peek.getType(), Token.TYPES)) {
            return declare_statement();
        } else
        if (peek.getType() == Token.TokenType.ID) {
            return assignment_statement();
        } else
        if (peek.getType() == Token.TokenType.SEMI) {
            scanner.nextToken();
            return new Node(Node.NodeType.EMPTY);
        }

        // Missing statement.
        throw new RuntimeException(String.format("Statement or } expected, line %d", peek.getLineNumber()));
    }

    // <if_statement> ::= "if" "(" <bool_expr> ")" <statement> [ "else" <statement> ]
    private Node if_statement() {
        scanner.nextToken(); // gobble up "if".
        Node statement = new Node(Node.NodeType.IF);
        mustHave(Token.TokenType.LPAR, "(");
        statement.setChild(0, bool_expr());
        mustHave(Token.TokenType.RPAR, ")");
        statement.setChild(1, statement());

        // is there an else?
        if (scanner.peekToken().getType() == Token.TokenType.KW_ELSE) {
            scanner.nextToken(); // gobble up "else"
            statement.setChild(2, statement());
        }

        return statement;
    }

    // <while_statement> ::= "while" "(" <expr> ")" <statement>
    private Node while_statement() {
        scanner.nextToken(); //gobble up "white"
        Node statement = new Node(Node.NodeType.WHILE);
        mustHave(Token.TokenType.LPAR, "(");
        statement.setChild(0, bool_expr());
        mustHave(Token.TokenType.RPAR, ")");
        statement.setChild(1, statement());
        return statement;
    }

    // <block_statement> ::= "{" { <statement> } "}"
    private Node block_statement() {
        scanner.nextToken(); // gobble up "{"

        // Extract all the sequences and put them in a list.  This can also be done without using a list, but gets
        // hairy quickly.  This makes it easier to follow even though it requires a slightly larger memory footprint.
        List<Node> sequence = new ArrayList<>();
        while (scanner.peekToken().getType() != Token.TokenType.RBRA) {
            sequence.add(statement());
        }
        scanner.nextToken(); // gobble up "}"

        // Put all the statements in a tree with the lower leaves the first statements to execute.
        return listToTree(sequence);
    }

    // <output_statement> ::=  "out" "(" <simple_expr> ")" ";"
    private Node output_statement() {
        scanner.nextToken(); // gobble up "out"
        mustHave(Token.TokenType.LPAR, "(");
        Node expr = bool_expr();
        mustHave(Token.TokenType.RPAR, ")");
        mustHave(Token.TokenType.SEMI, ";");
        return new Node(Node.NodeType.OUTPUT, null, expr);
    }

    // <declare_statement> ::= <type_specifier> <id> ["=" <simple_expr> ] {"," <id> ["="  <simple_expr> ] } ";"
    private Node declare_statement() {

        // Since multiple declarations are possible, get each and put in a list.
        List<Node> declarations = new ArrayList<>();

        Token type = scanner.nextToken();
        Node id = new Node(Node.NodeType.ID, mustHave(Token.TokenType.ID, "identifier").getLexeme());
        Node declare = new Node(Node.NodeType.DECLARATION, type.getLexeme(), id);
        if (scanner.peekToken().getType() == Token.TokenType.ASSIGN) {
            scanner.nextToken(); // gobble up "="
            declare.setChild(1, bool_expr());
        }
        declarations.add(declare);

        // Add extras.
        while (scanner.peekToken().getType() == Token.TokenType.COMMA)  {
            scanner.nextToken(); // gobble up ","

            id = new Node(Node.NodeType.ID, mustHave(Token.TokenType.ID, "identifier").getLexeme());
            declare = new Node(Node.NodeType.DECLARATION, type.getLexeme(), id);
            if (scanner.peekToken().getType() == Token.TokenType.ASSIGN) {
                scanner.nextToken(); // gobble up "="
                declare.setChild(1, bool_expr());
            }
            declarations.add(declare);
        }
        mustHave(Token.TokenType.SEMI, ";");

        // Put all the assignments in a tree with the lower leaves the first assignments to execute.
        return listToTree(declarations);
    }

    // <assign_statement> ::= <id> "=" <simple_expr> {"," <id> "="  <simple_expr> } ";"
    private Node assignment_statement() {

        // Since multiple assignments are possible, get each and put in a list.
        List<Node> assignments = new ArrayList<>();

        // Get the first one.
        Token id = scanner.nextToken();
        mustHave(Token.TokenType.ASSIGN, "=");
        assignments.add(new Node(Node.NodeType.ASSIGNMENT, null,
                new Node(Node.NodeType.ID, id.getLexeme()), bool_expr()));

        // Add extras.
        while (scanner.peekToken().getType() == Token.TokenType.COMMA)  {
            scanner.nextToken(); // gobble up ","
            id = mustHave(Token.TokenType.ID, "identifier");
            mustHave(Token.TokenType.ASSIGN, "=");
            assignments.add(new Node(Node.NodeType.ASSIGNMENT, id.getLexeme(), bool_expr()));
        }
        mustHave(Token.TokenType.SEMI, ";");

        // Put all the assignments in a tree with the lower leaves the first assignments to execute.
        return listToTree(assignments);
    }

    // A technique to handle boolean expressions is to mix them with normal expressions.  This helps generalize the
    // parser, but can slow it down.  In these case, the parser alwyas looks for a boolean expression first.

    // <bool_expression> ::= <bool_term> { "||" <bool_term> }
    private Node bool_expr() {
        Node root = bool_term();
        while (scanner.peekToken().getType() == Token.TokenType.OR) {
            scanner.nextToken(); // gobble up the ||
            root = new Node(Node.NodeType.OR, null, root, bool_term());
        }
        return root;
    }

    // <bool_term> ::= <bool_factor> { "&&" <bool_factor> }
    private Node bool_term() {
        Node root = bool_factor();
        while (scanner.peekToken().getType() == Token.TokenType.AND) {
            scanner.nextToken(); // gobble up the &&
            root = new Node(Node.NodeType.AND, null, root, bool_factor());
        }
        return root;
    }

    // <bool_factor> ::= ["!"] <relation>
    private Node bool_factor() {

        boolean hasNot = false;
        if (scanner.peekToken().getType() == Token.TokenType.NOT) {
            scanner.nextToken();
            hasNot = true;
        }

        Node relation = relation();
        return hasNot ? new Node(Node.NodeType.NOT, null, relation) : relation;
    }

    // The transition from boolean expression to normal expression occurs here.  The trick is to make the last
    // comparison part optional.
    //
    // <relation> ::= <expression> [ (">" | "<" | "<=" | ">=" | "==" | "!=" | "=") <expression> ]
    private Node relation() {
        Node expression = expression();
        if (in(scanner.peekToken().getType(), Token.BOOL_OP) || (scanner.peekToken().getType() == Token.TokenType.ASSIGN)) {
            Token token = scanner.nextToken();
            switch (token.getType()) {
                case CMP_G:
                    return new Node(Node.NodeType.GREATER, null, expression, expression());
                case CMP_S:
                    return new Node(Node.NodeType.SMALLER, null, expression, expression());
                case CMP_SE:
                    return new Node(Node.NodeType.SMALLER_EQUAL, null, expression, expression());
                case CMP_GE:
                    return new Node(Node.NodeType.GREATER_EQUAL, null, expression, expression());
                case CMP_EQUALS:
                    return new Node(Node.NodeType.EQUALS, null, expression, expression());
                case CMP_NE:
                    return new Node(Node.NodeType.NOT_EQUALS, null, expression, expression());
                case ASSIGN:
                    return new Node(Node.NodeType.ASSIGNMENT, null, expression, expression());
            }
        }
        return expression;
    }

    //  <expression> :== <term> { ("+"|"-") <term> }
    private Node expression() {
        Node root = term();
        while (in(scanner.peekToken().getType(), Token.ADDITION_OP)) {
            Token token = scanner.nextToken();
            if (token.getType() == Token.TokenType.PLUS) {
                root = new Node(Node.NodeType.PLUS, null, root, term());
            } else if (token.getType() == Token.TokenType.PLUS) {
                root = new Node(Node.NodeType.MINUS, null, root, term());
            }
        }
        return root;
    }

    // <term> :== ("+"|"-") <factor> { ("*" "/") <factor> }
    private Node term() {
        boolean negate = false;
        if (in(scanner.peekToken().getType(), Token.ADDITION_OP)) {
            Token token = scanner.nextToken();
            if (token.getType() == Token.TokenType.MINUS) {
                negate = true;
            } // ...just skip the plus.
        }

        Node root = factor();
        if (negate) {
            root = new Node(Node.NodeType.NEGATE, null, root);
        }

        while (in(scanner.peekToken().getType(), Token.MULTIPLY_OP)) {
            Token token = scanner.nextToken();
            if (token.getType() == Token.TokenType.MUL) {
                root = new Node(Node.NodeType.MUL, null, root, factor());
            } else if (token.getType() == Token.TokenType.DIV) {
                root = new Node(Node.NodeType.DIV, null, root, factor());
            }
        }
        return root;
    }

    // <factor> :==  <id> | <int> | <float> | "(" bool_expression ")"
    private Node factor() {
        Token token = scanner.nextToken();
        switch (token.getType()) {
            case LPAR:
                Node expr = bool_expr();
                mustHave(Token.TokenType.RPAR, ")");
                return expr;
            case ID:
                return new Node(Node.NodeType.ID, token.getLexeme());
            case VAL_INT:
                return new Node(Node.NodeType.VAL_INT, token.getLexeme());
            case VAL_FLOAT:
                return new Node(Node.NodeType.VAL_FLOAT, token.getLexeme());
        }
        String error = String.format("Unexpected symbol %s on line %d, expected a factor (i.d. like an ID or constant)",
                token.getLexeme(), token.getLineNumber());
        throw new RuntimeException(error);
    }

}
