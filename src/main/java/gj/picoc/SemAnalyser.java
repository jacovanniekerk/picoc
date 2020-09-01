package gj.picoc;

import java.util.Map;

public class SemAnalyser {

    public SemAnalyser() {
    }

    public void analyse(Node root) {

    }


    //PROG, IF, MINUS, PLUS, OR, MUL, DIV, AND, ID, VAL_INT, VAL_FLOAT, NOT, NEGATE,
    //                    SMALLER, GREATER, SMALLER_EQUAL, GREATER_EQUAL, EQUALS, NOT_EQUALS, EMPTY, WHILE,
    //                    SEQ,
    //                    ASSIGNMENT, DECLARATION,
    //                    OUTPUT

    // assign/check all types
    private void analyse(Node root, Map<String, Node.TypeType> ids) {

        // Ensure children have all types.
        for (Node child : root.getChildren()) {
            if (child != null && child.getTypeType() == null) {
                analyse(child);
            }
        }

        // Derive/check types
        switch (root.getNodeType()) {
            case IF: doIF(root); break;
            case MINUS: doEXPR(root); break;
            case PLUS: doEXPR(root); break;
            case OR: doBOOL(root); break;
            case MUL: doEXPR(root); break;
            case DIV: doEXPR(root); break;
            case AND: doBOOL(root); break;
            case ID: doID(root); break;
        }

    }

    private void doIF(Node root) {
        if (root.getChildren()[0].getTypeType() != Node.TypeType.INT) {
            throw new RuntimeException("Semantic error, if-expression must be integer");
        }
        root.setTypeType(Node.TypeType.EMPTY);
    }

    private void doEXPR(Node root) {
        Node.TypeType c1 = root.getChildren()[0].getTypeType();
        Node.TypeType c2 = root.getChildren()[1].getTypeType();

        if (c1 != Node.TypeType.INT && c1 != Node.TypeType.FLOAT) {
            throw new RuntimeException("Semantic error, left hand side of operation must be integer/float");
        }
        if (c2 != Node.TypeType.INT && c2 != Node.TypeType.FLOAT) {
            throw new RuntimeException("Semantic error, right hand side of operation must be integer/float");
        }

        if (c1 == Node.TypeType.INT && c2 == Node.TypeType.INT) {
            root.setTypeType(Node.TypeType.INT);
        } else {
            root.setTypeType(Node.TypeType.FLOAT);
        }
    }

    private void doBOOL(Node root) {
        Node.TypeType c1 = root.getChildren()[0].getTypeType();
        Node.TypeType c2 = root.getChildren()[1].getTypeType();

        if (c1 != Node.TypeType.INT || c2 != Node.TypeType.INT) {
            throw new RuntimeException("Semantic error, boolean logic only applies to integer types");
        }
        root.setTypeType(Node.TypeType.INT);
    }

    private void doID(Node root) {
    }


}
