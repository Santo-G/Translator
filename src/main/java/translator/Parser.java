package translator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Parser {
    private Lexer lex;
    private BufferedReader pbr;
    private Token look;

    public Parser(Lexer l, BufferedReader br) {
        lex = l;
        pbr = br;
        move();
    }

    void move() {
        look = lex.lexical_scan(pbr);
        System.out.println("token = " + look);
    }

    void error(String s) {
        throw new Error("near line " + lex.line + ": " + s);
    }

    void match(int t) {
        if (look.tag == t) {
            if (look.tag != Tag.EOF) move();
        } else error("syntax error");
    }

    public void prog() { // P
        switch(look.tag){

            case Tag.ASSIGN:
            case Tag.PRINT:
            case Tag.READ:
            case Tag.WHILE:
            case Tag.IF:
            case '{':
                statlist();
                match(Tag.EOF);
                break;

            default:
                error("Syntax Error in prog(): ASSIGN, PRINT, READ, WHILE, IF and \"{\" are the only accepted characters.");
        }
    }

    private void statlist() { // S
        switch(look.tag){

            case Tag.ASSIGN:
            case Tag.PRINT:
            case Tag.READ:
            case Tag.WHILE:
            case Tag.IF:
            case '{':
                stat();
                statlistp();
                break;

            default:
                error("Syntax Error in statlist(): ASSIGN, PRINT, READ, WHILE, IF and \"{\" are the only accepted characters.");
        }
    }

    private void statlistp() { // S'
        switch (look.tag) {

            case ';':
                match(';');
                stat();
                statlistp();
                break;

            case Tag.EOF:
            case '}':
                // do nothing
                break;

            default:
                error("Syntax Error in statlistp(): \";\" is the only accepted character.");
        }
    }

    private void stat() { // S"
        switch(look.tag){

            case Tag.ASSIGN:
                match(Tag.ASSIGN);
                expr();
                match(Tag.TO);
                idlist();
                break;

            case Tag.PRINT:
                match(Tag.PRINT);
                match('(');
                exprlist();
                match(')');
                break;

            case Tag.READ:
                match(Tag.READ);
                match('(');
                idlist();
                match(')');
                break;

            case Tag.WHILE:
                match(Tag.WHILE);
                match('(');
                bexpr();
                match(')');
                stat();
                break;

            case Tag.IF:
                match(Tag.IF);
                match('(');
                bexpr();
                match(')');
                stat();
                statp();
                break;

            case '{':
                match('{');
                statlist();
                match('}');
                break;

            default:
                error("Syntax Error in stat(): ASSIGN, PRINT, READ, WHILE, IF and \"{\" are the only accepted characters.");
        }
    }

    private void statp() { // S"'
        switch(look.tag){

            case Tag.END:
                match(Tag.END);
                break;

            case Tag.ELSE:
                match(Tag.ELSE);
                stat();
                match(Tag.END);
                break;

            default:
                error("Syntax Error in statp(): AND and ELSE are the only accepted characters.");
        }
    }

    private void idlist() { // I
        switch(look.tag){

            case Tag.ID:
                match(Tag.ID);
                idlistp();
                break;

            default:
                error("Syntax Error in idlist(): ID is the only accepted character.");
        }
    }

    private void idlistp() { // I'
        switch (look.tag){

            case ',':
                match(',');
                match(Tag.ID);
                idlistp();

            case ';':
            case Tag.EOF:
            case '}':
            case Tag.END:
            case Tag.ELSE:
            case ')':
                // do nothing
                break;

            default:
                error("Syntax Error in idlistp(): \",\", \";\", EOF, \"}\", END, ELSE and \")\" are the only accepted characters.");
        }
    }

    private void bexpr() { // B
        switch (look.tag){

            case Tag.RELOP:
                match(Tag.RELOP);
                expr();
                expr();
                break;

            default:
                error("Syntax Error in bexpr(): RELOP is the only accepted character.");
        }
    }


    private void expr() { // E
        switch(look.tag){

            case '+':
                match('+');
                match('(');
                exprlist();
                match(')');
                break;

            case '-':
                match('-');
                expr();
                expr();
                break;

            case '*':
                match('*');
                match('(');
                exprlist();
                match(')');
                break;

            case '/':
                match('/');
                expr();
                expr();
                break;

            case Tag.NUM:
                match(Tag.NUM);
                break;

            case Tag.ID:
                match(Tag.ID);
                break;

            default:
                error("Syntax Error in expr(): \"+\", \"-\", \"*\", \"/\", NUM and ID are the only accepted characters.");
        }
    }

    private void exprlist() { // E'
        switch (look.tag) {

            case '+':
            case '-':
            case '*':
            case '/':
            case Tag.NUM:
            case Tag.ID:
                expr();
                exprlistp();
                break;

            default:
                error("Syntax Error in exprlist(): \"+\", \"-\", \"*\", \"/\", NUM and ID are the only accepted characters.");

        }
    }

    private void exprlistp() { // E"
        switch (look.tag) {

            case ',':
                match(',');
                expr();
                exprlistp();
                break;

            case ')':
                // do nothing
                break;

            default:
                error("Syntax Error in exprlistp(): \",\" and \")\" are the only accepted characters.");

        }
    }


    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "test/TestFile.txt";     // path of the test file used
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Parser parser = new Parser(lex, br);
            parser.prog();
            System.out.println("Input OK");
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
