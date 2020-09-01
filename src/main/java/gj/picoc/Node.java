package gj.picoc;

public class Node {

    private static final int MAX_CHILDREN = 3;

    public enum NodeType {
        PROG, IF, MINUS, PLUS, OR, MUL, DIV, AND, ID, VAL_INT, VAL_FLOAT, NOT, NEGATE,
        SMALLER, GREATER, SMALLER_EQUAL, GREATER_EQUAL, EQUALS, NOT_EQUALS, EMPTY, WHILE,
        SEQ,
        ASSIGNMENT, DECLARATION,
        OUTPUT
    }

    // The EMPTY type is associated with nodes that does not carry a type, i.e. a "SEQ" or "PROG" for example.
    public enum TypeType {
        FLOAT, INT, EMPTY
    }

    // Every node in the parse tree has a type, a number of defined children (according to its type) as well as a
    // possible value.
    private final NodeType nodeType;
    private final String value;
    private final Node[] children;

    // For semantic analysis, the type of the tree can be either float or int.
    private TypeType typeType;

    public Node(NodeType nodeType, String value, Node... children) {
        this.nodeType = nodeType;

        this.value = value;
        this.children = new Node[MAX_CHILDREN];

        for (int i = 0; i < Math.min(children.length, MAX_CHILDREN); i++) {
            this.children[i] = children[i];
        }

        this.typeType = null;
    }

    public Node(NodeType nodeType) {
        this(nodeType, null);
    }

    // Getters/setters

    public String getValue() {
        return value;
    }
    public NodeType getNodeType() {
        return nodeType;
    }
    public void setChild(int which, Node c) {
        this.children[which] = c;
    }
    public Node[] getChildren() {
        return children;
    }
    public void setTypeType(TypeType typeType) { this.typeType = typeType; }
    public TypeType getTypeType() { return typeType; }

    // Other methods

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
        StringBuilder result = new StringBuilder(nodeType.toString());
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
