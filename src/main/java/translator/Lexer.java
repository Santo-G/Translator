package translator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Lexer {

    public static int line = 1;
    private char peek = ' ';

    private void readch(BufferedReader br) {
        try {
            peek = (char) br.read();
        } catch (IOException exc) {
            peek = (char) -1; // ERROR
        }
    }


    public Token lexical_scan(BufferedReader br) {
        while (peek == ' ' ||peek == '\t' ||peek == '\n' ||peek == '\r'){
            if (peek == '\n')
                line++;
            readch(br);
        }

        switch (peek) {

            case '!':
                peek = ' ';
                return Token.not;

            case '(':
                peek = ' ';
                return Token.lpt;

            case ')':
                peek = ' ';
                return Token.rpt;

            case '{':
                peek = ' ';
                return Token.lpg;

            case '}':
                peek = ' ';
                return Token.rpg;

            case '+':
                peek = ' ';
                return Token.plus;

            case '-':
                peek = ' ';
                return Token.minus;

            case '*':
                peek = ' ';
                return Token.mult;

            case '/':
                readch(br);
                if(peek == '*') {
                    readch(br);
                    if (isAMultipleLineComment(br)) {
                        peek = ' ';
                        return lexical_scan(br);
                    } else {
                        System.err.println("> Error. Multiple line comment not correctly closed !!!");
                        return null;
                    }
                } else if( peek == '/') {
                    readch(br);
                    while(peek != '\n' && peek != (char) -1) {
                        readch(br);
                    }
                    peek = ' ';
                    return lexical_scan(br);
                } else {
                    peek = ' ';
                    return Token.div;
                }

            case ';':
                peek = ' ';
                return Token.semicolon;

            case ',':
                peek = ' ';
                return Token.comma;

            case '&':
                readch(br);
                if (peek == '&'){
                    peek = ' ';
                return Word.and;
            } else{
                System.err.println("Erroneous character" + " after & : " + peek);
                return null;
            }

            case '|':
                readch(br);
                if (peek == '|'){
                    peek = ' ';
                    return Word.or;
                } else{
                    System.err.println("Erroneous character" + " after | : " + peek);
                    return null;
                }

            case '<':
                readch(br);
                if (peek == '=') {
                    peek = ' ';
                    return Word.le;
                } else if (peek == '>') {
                    peek = ' ';
                    return Word.ne;
                } else {
                    return Word.lt;
                }

            case '>':
                readch(br);
                if (peek == '=') {
                    peek = ' ';
                    return Word.ge;
                } else {
                    return Word.gt;
                }

            case '=':
                readch(br);
                if (peek == '=') {
                    peek = ' ';
                    return Word.eq;
                } else {
                    System.err.println("Erroneous character" + " after = : " + peek);
                    return null;
                }

            case '_':
                StringBuilder builderId = new StringBuilder();
                builderId.append(peek);
                readch(br);
                if (Character.isLetter(peek) || Character.isDigit(peek) || peek == '_') {
                    return readIdentifier(br, builderId);
                } else {
                    System.err.println("Erroneous character" + " after _ : " + peek);
                    return null;
                }

            case (char) -1:
                return new Token(Tag.EOF);

            default:
                if (Character.isLetter(peek)) {
                    StringBuilder builder = new StringBuilder();
                    switch (peek) {

                        case 'a':
                            builder.append(peek);
                            readch(br);
                            if(peek == 's') {
                                builder.append(peek);
                                readch(br);
                                if(peek == 's') {
                                    builder.append(peek);
                                    readch(br);
                                    if(peek == 'i') {
                                        builder.append(peek);
                                        readch(br);
                                        if(peek == 'g') {
                                            builder.append(peek);
                                            readch(br);
                                            if(peek == 'n') {
                                                builder.append(peek);
                                                readch(br);
                                                if(Character.isDigit(peek) || Character.isLetter(peek))
                                                    return readIdentifier(br, builder);
                                                else {
                                                    return Word.assign;
                                                }
                                            } else {
                                                return readIdentifier(br, builder);
                                            }
                                        } else {
                                            return readIdentifier(br, builder);
                                        }
                                    } else {
                                        return readIdentifier(br, builder);
                                    }
                                } else {
                                    return readIdentifier(br, builder);
                                }
                            } else {
                                return readIdentifier(br, builder);
                            }

                    case 't':
                        builder.append(peek);
                        readch(br);
                        if(peek == 'o') {
                            builder.append(peek);
                            readch(br);
                            if(Character.isDigit(peek) || Character.isLetter(peek))
                                return readIdentifier(br, builder);
                            else {
                                return Word.to;
                            }
                        } else {
                            return readIdentifier(br, builder);
                        }

                        case 'i':
                            builder.append(peek);
                            readch(br);
                            if(peek == 'f') {
                                builder.append(peek);
                                readch(br);
                                if(Character.isDigit(peek) || Character.isLetter(peek))
                                    return readIdentifier(br, builder);
                                else {
                                    return Word.iftok;
                                }
                            } else {
                                return readIdentifier(br, builder);
                            }

                        case 'e':
                            builder.append(peek);
                            readch(br);
                            if(peek == 'l') {
                                builder.append(peek);
                                readch(br);
                                if(peek == 's') {
                                    builder.append(peek);
                                    readch(br);
                                    if(peek == 'e') {
                                        builder.append(peek);
                                        readch(br);
                                        if(Character.isDigit(peek) || Character.isLetter(peek))
                                            return readIdentifier(br, builder);
                                         else {
                                            return Word.elsetok;
                                        }
                                    } else {
                                        return readIdentifier(br, builder);
                                    }
                                } else {
                                    return readIdentifier(br, builder);
                                }
                            } else if (peek == 'n') {
                                builder.append(peek);
                                readch(br);
                                if (peek == 'd') {
                                    builder.append(peek);
                                    readch(br);
                                    if(Character.isDigit(peek) || Character.isLetter(peek))
                                        return readIdentifier(br, builder);
                                    else {
                                        return Word.end;
                                    }
                                } else {
                                    return readIdentifier(br, builder);
                                }
                            } else {
                                return readIdentifier(br, builder);
                            }

                        case 'w':
                            builder.append(peek);
                            readch(br);
                            if(peek == 'h') {
                                builder.append(peek);
                                readch(br);
                                if(peek == 'i') {
                                    builder.append(peek);
                                    readch(br);
                                    if(peek == 'l') {
                                        builder.append(peek);
                                        readch(br);
                                        if(peek == 'e') {
                                            builder.append(peek);
                                            readch(br);
                                            if (Character.isDigit(peek) || Character.isLetter(peek))
                                                return readIdentifier(br, builder);
                                            else {
                                                return Word.whiletok;
                                            }
                                        } else {
                                            return readIdentifier(br, builder);
                                        }
                                    } else {
                                        return readIdentifier(br, builder);
                                    }
                                } else {
                                    return readIdentifier(br, builder);
                                }
                            } else {
                                return readIdentifier(br, builder);
                            }

                        case 'b':
                            builder.append(peek);
                            readch(br);
                            if(peek == 'e') {
                                builder.append(peek);
                                readch(br);
                                if(peek == 'g') {
                                    builder.append(peek);
                                    readch(br);
                                    if(peek == 'i') {
                                        builder.append(peek);
                                        readch(br);
                                        if(peek == 'n') {
                                            builder.append(peek);
                                            readch(br);
                                            if (Character.isDigit(peek) || Character.isLetter(peek))
                                                return readIdentifier(br, builder);
                                            else {
                                                return Word.begin;
                                            }
                                        } else {
                                            return readIdentifier(br, builder);
                                        }
                                    } else {
                                        return readIdentifier(br, builder);
                                    }
                                } else {
                                    return readIdentifier(br, builder);
                                }
                            } else {
                                return readIdentifier(br, builder);
                            }

                        case 'p':
                            builder.append(peek);
                            readch(br);
                            if(peek == 'r') {
                                builder.append(peek);
                                readch(br);
                                if(peek == 'i') {
                                    builder.append(peek);
                                    readch(br);
                                    if(peek == 'n') {
                                        builder.append(peek);
                                        readch(br);
                                        if(peek == 't') {
                                            builder.append(peek);
                                            readch(br);
                                            if (Character.isDigit(peek) || Character.isLetter(peek))
                                                return readIdentifier(br, builder);
                                            else {
                                                return Word.print;
                                            }
                                        } else {
                                            return readIdentifier(br, builder);
                                        }
                                    } else {
                                        return readIdentifier(br, builder);
                                    }
                                } else {
                                    return readIdentifier(br, builder);
                                }
                            } else {
                                return readIdentifier(br, builder);
                            }


                        case 'r':
                            builder.append(peek);
                            readch(br);
                            if(peek == 'e') {
                                builder.append(peek);
                                readch(br);
                                if(peek == 'a') {
                                    builder.append(peek);
                                    readch(br);
                                    if(peek == 'd') {
                                        builder.append(peek);
                                        readch(br);
                                        if (Character.isDigit(peek) || Character.isLetter(peek))
                                            return readIdentifier(br, builder);
                                        else {
                                            return Word.read;
                                        }
                                    } else {
                                        return readIdentifier(br, builder);
                                    }
                                } else {
                                    return readIdentifier(br, builder);
                                }
                            } else {
                                return readIdentifier(br, builder);
                            }

                        default:
                            return readIdentifier(br, builder);
                    }

                } else if (Character.isDigit(peek)) {
                    StringBuilder builder = new StringBuilder();
                    if(peek == '0'){
                        builder.append(peek);
                        readch(br);
                        if (peek >= '0' && peek <= '9'){
                            System.err.println("> Number cannot start with 0");
                            return null;
                        } else {
                            return new NumberTok(builder.toString());
                        }
                    } else if (peek >= '1' && peek <= '9'){
                        while(Character.isDigit(peek)) {
                            builder.append(peek);
                            readch(br);
                        }
                        return new NumberTok(builder.toString());
                    }

                } else {
                    System.err.println("Erroneous character: " + peek);
                    return null;
                }
        }
        return null;
    }

    private Word readIdentifier(BufferedReader br, StringBuilder identifier) {
        while((peek >= 'a' && peek <= 'z') || (peek >= 'A' && peek <= 'Z') || (peek >= '0' && peek <= '9') || peek == '_') {
            identifier.append(peek);
            readch(br);
        }
        return new Word(Tag.ID, identifier.toString());
    }

    private boolean isAMultipleLineComment(BufferedReader br){
        boolean endOfComment = false;
        while(peek != '*') {
            readch(br);
            if(peek == (char)-1) {
                endOfComment = false;
                return endOfComment;
            }
        }
        readch(br);
        if(peek == '/') {
            endOfComment = true;
        } else if (peek == (char) -1 || peek == '\n'){
            peek = ' ';
            endOfComment = false;
        } else {
            return isAMultipleLineComment(br);
        }
        return endOfComment;
    }


    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "src/test/testFile2.txt";     // path of the test file to use
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Token tok;
            do {
                tok = lex.lexical_scan(br);
                System.out.println("Scan: " + tok);
            } while (tok != null && tok.tag != Tag.EOF);
                br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
