package translator;

import java.io.*;

public class Translator {
    private Lexer lex;
    private BufferedReader pbr;
    private Token look;

    SymbolTable st = new SymbolTable();
    CodeGenerator code = new CodeGenerator();
    int count=0;

    public Translator(Lexer l, BufferedReader br) {
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

    public void prog() {
        switch (look.tag) {
            case Tag.ASSIGN:
            case Tag.PRINT:
            case Tag.READ:
            case Tag.WHILE:
            case Tag.IF:
            case '{':
                int lnext_prog = code.newLabel();   // label L0
                statlist(lnext_prog);               // address 0
                code.emitLabel(lnext_prog);
                match(Tag.EOF);
                try {
                    code.toJasmin();        // "Output.j" file generation
                }
                catch(IOException e) {
                    System.out.println("IO error\n");
                }
                break;
            default:
                error("> Error in prog(): something went wrong.");
        }
    }

    private void statlist(int lnext_statlist) {
        switch (look.tag) {
            case Tag.ASSIGN:
            case Tag.PRINT:
            case Tag.READ:
            case Tag.WHILE:
            case Tag.IF:
            case '{':
                int lnext_prev = lnext_statlist;
                lnext_statlist = code.newLabel();	// label L1
                stat(lnext_statlist);				// address 1
                code.emitLabel(lnext_statlist);
                statlistp(lnext_statlist);
                code.emit(OpCode.GOto, lnext_prev);
                break;
            default:
                error("> Error in statlist(): something went wrong.");
        }
    }

    private void statlistp(int lnext_statlistp) {
        switch (look.tag) {
            case ';':
                match(';');
                lnext_statlistp = code.newLabel();	// label L2
                stat(lnext_statlistp);				// address 2
                code.emitLabel(lnext_statlistp);
                statlistp(lnext_statlistp);
                break;
            case Tag.EOF:  // do nothing
            case '}':
                break;
            default:
                error("> Error in statlistp(): something went wrong.");
        }
    }

    public void stat(int lnext_stat) {
        int l_value_true, l_value_false;

        switch(look.tag) {

            case Tag.ASSIGN:
                match(Tag.ASSIGN);
                expr();
                match(Tag.TO);
                idlist(Tag.ASSIGN);
                code.emit(OpCode.GOto, lnext_stat);
                break;

            case Tag.PRINT:
                match(Tag.PRINT);
                match('(');
                exprlist('p');
                match(')');
                code.emit(OpCode.GOto, lnext_stat);
                break;

            case Tag.READ:
                match(Tag.READ);
                match('(');
	            idlist(Tag.READ);
                match(')');
                code.emit(OpCode.GOto, lnext_stat);
                break;

            case Tag.WHILE:
                l_value_false = lnext_stat;
                lnext_stat = code.newLabel();
                l_value_true = code.newLabel();
                code.emitLabel(lnext_stat);
                match(Tag.WHILE);
                match('(');
                bexpr(l_value_true, l_value_false);
                match(')');
                code.emitLabel(l_value_true);
                stat(lnext_stat);
                break;

            case Tag.IF:
                l_value_true = code.newLabel();
                l_value_false = code.newLabel();
                match(Tag.IF);
                match('(');
                bexpr(l_value_true, l_value_false);
                match(')');
                code.emitLabel(l_value_true);
                stat(lnext_stat);
                code.emitLabel(l_value_false);
                statp(lnext_stat);
                break;

            case '{':
                match('{');
                statlist(lnext_stat);
                match('}');
                break;
            default:
                error("> Error in stat(): something went wrong.");
        }
     }

    private void statp(int lnext_statp) {
        switch (look.tag) {
            case Tag.END:
                match(Tag.END);
                break;
            case Tag.ELSE:
                match(Tag.ELSE);
                stat(lnext_statp);
                match(Tag.END);
                break;
            default:
                error("> Error in statp(): something went wrong.");
        }
    }

    private void idlist(int origin_tag) {
        switch(look.tag) {
	    case Tag.ID:
        	int id_addr = st.lookupAddress(((Word)look).lexeme);
                if (id_addr==-1) {
                    id_addr = count;
                    st.insert(((Word)look).lexeme, count++);
                }
                match(Tag.ID);
            if(origin_tag == Tag.READ) {
                code.emit(OpCode.invokestatic, 0);     // 1 --> print, 0 --> read
            }
            code.emit(OpCode.istore, id_addr);
            idlistp(id_addr,origin_tag);
            break;
            default:
                error("> Error in idlist(): something went wrong.");
    	}
    }

    private void idlistp(int first_id, int origin_tag) {
        switch (look.tag) {
            case ',':
                match(',');
                int id_addr = st.lookupAddress(((Word)look).lexeme);
                if (id_addr==-1) {
                    id_addr = count;
                    st.insert(((Word)look).lexeme,count++);
                }
                match(Tag.ID);
                switch(origin_tag) {
                    case Tag.READ:
                        code.emit(OpCode.invokestatic,0);	// 1 --> print, 0 --> read
                        break;
                    case Tag.ASSIGN:
                        code.emit(OpCode.iload, first_id);
                        break;
                    default:
                        error("> Error in idlistp(): Not possible in inner switch - wrong Tag detected");
                }
                code.emit(OpCode.istore, id_addr);
                idlistp(first_id, origin_tag);
                break;
            case Tag.END:	// epsilon - do nothing
            case Tag.ELSE:
            case Tag.EOF:
            case '}':
            case ')':
            case ';':
                break;
            default:
                error("> Error in idlist(): something went wrong - outer switch.");
        }
    }

    private void bexpr(int l_value_true_bexpr, int l_value_false_bexpr) {
        switch (look.tag) {
            case Tag.RELOP:
                String s = ((Word)look).lexeme;
                match(Tag.RELOP);
                expr();
                expr();
                switch (s) {
                    case "<":
                        code.emit(OpCode.if_icmplt, l_value_true_bexpr);
                        break;
                    case ">":
                        code.emit(OpCode.if_icmpgt, l_value_true_bexpr);
                        break;
                    case "==":
                        code.emit(OpCode.if_icmpeq, l_value_true_bexpr);
                        break;
                    case "<=":
                        code.emit(OpCode.if_icmple, l_value_true_bexpr);
                        break;
                    case "<>":
                        code.emit(OpCode.if_icmpne, l_value_true_bexpr);
                        break;
                    case ">=":
                        code.emit(OpCode.if_icmpge, l_value_true_bexpr);
                        break;
                }
                code.emit(OpCode.GOto, l_value_false_bexpr);
                break;
            default:
                error("> Error in bexpr(): something went wrong.");
        }
    }

    private void expr( ) {
        switch(look.tag) {

            case '+':
                match('+');
                match('(');
                exprlist('+');
                match(')');
                break;

            case '*':
                match('*');
                match('(');
                exprlist('*');
                match(')');
                break;

            case '-':
                match('-');
                expr();
                expr();
                code.emit(OpCode.isub);
                break;

            case '/':
                match('/');
                expr();
                expr();
                code.emit(OpCode.idiv);
                break;

            case Tag.NUM:
                code.emit(OpCode.ldc, Integer.parseInt(((NumberTok)look).lexeme));
                match(Tag.NUM);
                break;

            case Tag.ID:
                int id_addr = st.lookupAddress(((Word)look).lexeme);
                if (id_addr==-1) {
                    error("> Error in expr(): missing identificator while expr evaluation");
                }
                match(Tag.ID);
                code.emit(OpCode.iload, id_addr);
                break;

            default:
                error("> Error in expr(): something went wrong.");
        }
    }

    private void exprlist(int origin_mode) {
        switch (look.tag) {
            case '+':
            case '*':
            case '-':
            case '/':
            case Tag.NUM:
            case Tag.ID:
                expr();
                exprlistp(origin_mode);
                break;
            default:
                error("> Error in exprlist(): something went wrong.");
        }
    }

    private void exprlistp(int origin_mode) {
        if(origin_mode == 'p')
            code.emit(OpCode.invokestatic, 1);
        switch (look.tag) {
            case ',':
                match(',');
                expr();
                exprlistp(origin_mode);
                switch(origin_mode){
                    case '+':
                        code.emit(OpCode.iadd);
                        break;
                    case '*':
                        code.emit(OpCode.imul);
                        break;
                }
                break;
            case ')':	// epsilon - do nothing
                break;
            default:
                error("> Error in exprlistp(): something went wrong.");
        }
    }

    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "test/TestFile.txt";     // path of the test file used
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Translator translator = new Translator(lex, br);
            translator.prog();
            br.close();
        } catch (IOException e) {e.printStackTrace();}
    }

}

