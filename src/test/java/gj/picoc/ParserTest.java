package gj.picoc;

import org.junit.Test;

public class ParserTest {

    @Test
    public void testSimple1() {
        Scanner scanner = new Scanner(" if (a > 2) out(1); else out(2);");
        Parser parser = new Parser(scanner);
        Node n = parser.program();
        System.out.println(n);
    }

}
