package gj.picoc;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ScannerTest {

    private void test(String code, String expected) {
        Scanner scanner = new Scanner(code);
        List<Token.TokenType> actual = new ArrayList<>();
        while (scanner.peekToken().getType() != Token.TokenType.EOF) {
            actual.add(scanner.nextToken().getType());
        }
        // Did we get back as expected?
        Assert.assertThat(String.valueOf(actual), Matchers.equalTo(expected));
    }

    @Test
    public void testScanner1() {
        String code = "if (a < 10) { b = 12; c = b + 6; }";
        String expected = "[KW_IF, LPAR, ID, CMP_S, VAL_INT, RPAR, LBRA, ID, ASSIGN, VAL_INT, SEMI, ID, ASSIGN, ID, PLUS, VAL_INT, SEMI, RBRA]";
        test(code, expected);
    }

    @Test
    public void testScanner2() {
        String code = "while (a < -10) { a=a+1; } // this is a comment";
        String expected = "[KW_WHILE, LPAR, ID, CMP_S, VAL_INT, RPAR, LBRA, ID, ASSIGN, ID, PLUS, VAL_INT, SEMI, RBRA]";
        test(code, expected);
    }

    @Test
    public void testScanner3() {
        String code = "if (value >=(90)) final = 1; else final = 0;";
        String expected = "[KW_IF, LPAR, ID, CMP_GE, LPAR, VAL_INT, RPAR, RPAR, ID, ASSIGN, VAL_INT, SEMI, KW_ELSE, ID, ASSIGN, VAL_INT, SEMI]";
        test(code, expected);
    }

    @Test
    public void testIntegers() {
        Scanner scanner = new Scanner("--12");
        Assert.assertThat(scanner.nextToken().getType(), Matchers.equalTo(Token.TokenType.MINUS));
        Assert.assertThat(scanner.peekToken().getType(), Matchers.equalTo(Token.TokenType.VAL_INT));
        Assert.assertThat(scanner.nextToken().getLexeme(), Matchers.equalTo("-12"));
    }

    @Test
    public void testFloats() {
        Scanner scanner = new Scanner("-3.141592");
        Assert.assertThat(scanner.peekToken().getType(), Matchers.equalTo(Token.TokenType.VAL_FLOAT));
        Assert.assertThat(scanner.nextToken().getLexeme(), Matchers.equalTo("-3.141592"));
    }

    @Test
    public void testIds() {
        Scanner scanner = new Scanner("counter");
        Assert.assertThat(scanner.nextToken().getLexeme(), Matchers.equalTo("counter"));
    }


}
