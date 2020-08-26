package gj.picoc;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Node {

    private static final int MAX_CHILDREN = 3;

    public enum NodeType {
        PROG, IF, MINUS, PLUS, OR, MUL, DIV, AND, ID, VAL_INT, VAL_FLOAT, NOT, NEGATE,
        SMALLER, GREATER, SMALLER_EQUAL, GREATER_EQUAL, EQUALS, NOT_EQUALS, EMPTY, WHILE,
        SEQ,
        ASSIGNMENT, DECLARATION,
        OUTPUT
    }

    // Every node in the parse tree has a type, a number of defined children (according to its type) as well as a
    // possible value.
    private final NodeType type;
    private final Node[] children;
    private final String value;

    public Node(NodeType type, String value, Node... children) {
        this.type = type;
        this.value = value;
        this.children = new Node[MAX_CHILDREN];

        for (int i = 0; i < Math.min(children.length, MAX_CHILDREN); i++) {
            this.children[i] = children[i];
        }
    }

    public Node(NodeType type) {
        this(type, null);
    }

    public String getValue() {
        return value;
    }

    public NodeType getType() {
        return type;
    }

    public void setChild(int which, Node c) {
        this.children[which] = c;
    }

    public Node[] getChildren() {
        return children;
    }

    public boolean isLeaf() {
        for (Node c : children) {
            if (c != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(type.toString());
        if (value != null)  result.append("=" + value);
        int cnt = MAX_CHILDREN - 1;
        while (cnt >= 0 && this.children[cnt] == null) cnt--;
        if (cnt >= 0) {
            result.append("{");
            for (int i = 0; i <= cnt; i++) result.append(this.children[i].toString()+ " ");
            result.append("}");
        }
        return result.toString();
    }
}
