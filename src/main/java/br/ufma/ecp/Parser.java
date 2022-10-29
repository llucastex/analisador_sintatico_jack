package br.ufma.ecp;

import static br.ufma.ecp.token.TokenType.*;

import java.lang.reflect.Method;

import br.ufma.ecp.token.Token;
import br.ufma.ecp.token.TokenType;

public class Parser {

    private Scanner scan;
    private Token currentToken;
    private Token peekToken;

    private StringBuilder xmlOutput = new StringBuilder();

    public Parser (byte[] input) {
        scan = new Scanner(input);
        nextToken();
    }

    private void nextToken() {
        currentToken = peekToken;
        peekToken = scan.nextToken();
    }

   

    void parser () {
        parseClass();
    }
    //'class' className '{' classVarDec* subroutineDec* '}'
    //'class' className '{' classVarDec '}'
    void parseClass() {
        printNonTerminal("class");
        expectPeek(CLASS);
        expectPeek(IDENTIFIER);
        expectPeek(LBRACE);
        while (peekToken.type == FIELD || peekToken.type == STATIC) {
            parseClassVardec();
        }
        
        expectPeek(RBRACE);
        printNonTerminal("/class");
    }
    //( 'static' | 'field' ) type varName ( ',' varName)* ';'
    void parseClassVardec() {
        printNonTerminal("classVarDec");
        expectPeek(FIELD, STATIC);
        expectPeek(INT, CHAR, BOOLEAN, IDENTIFIER);
        expectPeek(IDENTIFIER);
        while (peekToken.type == COMMA) {
            expectPeek(COMMA);
            expectPeek(IDENTIFIER);
        }
        expectPeek(SEMICOLON);
        printNonTerminal("/classVarDec");
    }

    void parseVardec () {
        printNonTerminal("varDec");
        expectPeek(VAR);
        // 'int' | 'char' | 'boolean' | className
        expectPeek(INT,CHAR,BOOLEAN,IDENTIFIER);
        expectPeek(IDENTIFIER);

        while (peekTokenIs(COMMA)) {
            expectPeek(COMMA);
            expectPeek(IDENTIFIER);
        }

        expectPeek(SEMICOLON);
        printNonTerminal("/varDec");
    }

    void parseSubRoutineDec(){
        printNonTerminal("classVarDec");
        expectPeek(CONSTRUCTOR, FUNCTION, METHOD);
        expectPeek(VOID, INT, CHAR, BOOLEAN, IDENTIFIER);
        expectPeek(IDENTIFIER);
        expectPeek(LPAREN);
        parseParameterList();
        expectPeek(RPAREN);
        parseSubroutineBody();

        printNonTerminal("/classVarDec");
    }

    void parseParameterList()
    {
        printNonTerminal("parameterList");


        if (!peekTokenIs(RPAREN)) // verifica se tem pelo menos uma expressao
        {
            expectPeek(INT,CHAR,BOOLEAN,IDENTIFIER);
            expectPeek(IDENTIFIER);
        }


        while (peekTokenIs(COMMA))
        {
            expectPeek(COMMA);
            expectPeek(INT,CHAR,BOOLEAN,IDENTIFIER);
            expectPeek(IDENTIFIER);
        }

        printNonTerminal("/parameterList");
    }

    void parseSubroutineBody () {
        expectPeek(LBRACE);
        parseStatements();
        expectPeek(RBRACE);
    }
   
    // 'while' '(' expression ')' '{' statements '}'
    void parseWhile () {
        System.out.println("<whileStatement>");
        expectPeek(WHILE);
        expectPeek(LPAREN);
        parseExpression();
        expectPeek(RPAREN);
        expectPeek(LBRACE);
        parseStatements();
        expectPeek(RBRACE);
        System.out.println("</whileStatement>");
    }

    void parseIf () {
        System.out.println("<ifStatement>");
        expectPeek(IF);
        expectPeek(LPAREN);
        parseExpression();
        expectPeek(RPAREN);
        expectPeek(LBRACE);
        parseStatements();
        expectPeek(RBRACE);
        System.out.println("</ifStatement>");
    }

    void parseReturn () {
        printNonTerminal("returnStatement");
        expectPeek(RETURN);
        if (!peekTokenIs(SEMICOLON)) {
            parseExpression();
        }
        expectPeek(SEMICOLON);

        printNonTerminal("/returnStatement");
    }

    void parseSubroutineCall () {
        if (peekTokenIs (LPAREN)) {
            expectPeek(LPAREN);
            parseExpressionList();
            expectPeek(RPAREN);
        } else {
            // pode ser um metodo de um outro objeto ou uma função
            expectPeek(DOT);
            expectPeek(IDENTIFIER);
            expectPeek(LPAREN);
            parseExpressionList();
            expectPeek(RPAREN);
        }
    }

    void parseDo () {
        printNonTerminal("doStatement");
        expectPeek(DO);
        expectPeek(IDENTIFIER);
        parseSubroutineCall();
        expectPeek(SEMICOLON);

        printNonTerminal("/doStatement");
    }

    void parseExpressionList() {
        printNonTerminal("expressionList");

        if (!peekTokenIs(RPAREN)) // verifica se tem pelo menos uma expressao
        {
            parseExpression();
        }

        // procurando as demais
        while (peekTokenIs(COMMA)) {
            expectPeek(COMMA);
            parseExpression();
        }

        printNonTerminal("/expressionList");
    }

    void parseStatements () {
        System.out.println("<statements>");
        while (peekToken.type == WHILE ||
        peekToken.type == IF ||
        peekToken.type == LET ||
        peekToken.type == DO ||
        peekToken.type == RETURN ) {
            parseStatement();
        }
        
        System.out.println("</statements>");
    }

    void parseStatement () {
        switch (peekToken.type) {
            case LET:
                parseLet();
                break;
            case WHILE:
                parseWhile();
                break;
            default:
                throw new Error("Syntax error - expected a statement");
        }
    }

    // letStatement -> 'let' varName  '=' term ';'
    // term -> number;
    void parseLet() {
        printNonTerminal("letStatement");
        expectPeek(LET);
        expectPeek(IDENTIFIER);
        if (peekTokenIs (LBRACKET)) {
            expectPeek(LBRACKET);
            parseExpression();
            expectPeek(RBRACKET);
        }
        expectPeek(EQ);
        parserTerm();
        // parseExpression();
        // parseSubroutineCall();
        expectPeek(SEMICOLON);
        printNonTerminal("/letStatement");

    }

    // void parseTerm () {
    //     printNonTerminal("term");
    //     switch (peekToken.type) {
    //         case NUMBER:
    //             expectPeek(NUMBER);
    //             break;
    //         default:
    //             ;

    //     }
    //     printNonTerminal("/term");
    // }
    // auxiliares

    boolean currentTokenIs (TokenType type) {
        return currentToken.type == type;
    }


    boolean peekTokenIs (TokenType type) {
        return peekToken.type == type;
    }


    private void expectPeek(TokenType type) {
        if (peekToken.type == type ) {
            nextToken();
            xmlOutput.append(String.format("%s\r\n", currentToken.toString()));
        } else {
            throw new Error("Syntax error - expected "+type+" found " + peekToken.lexeme);
        }
    }

    private void expectPeek(TokenType... types) {
        
        for (TokenType type : types) {
            if (peekToken.type == type) {
                expectPeek(type);
                return;
            }
        }
        
        throw new Error("Syntax error ");

    }

    private boolean isOperator (TokenType type) {
        return type.ordinal() >= PLUS.ordinal() && type.ordinal() <= EQ.ordinal();
    }

    void parseExpression() {
        System.out.println("<expression>");
        parserTerm ();
        while (isOperator(peekToken.type)) {
            expectPeek(peekToken.type);
            parserTerm();
        }
        System.out.println("</expression>");
    }

    void parserTerm () {
        System.out.println("<termaaaaaaaaaaaaaaaaaaaa>");
        switch (peekToken.type) {
            case NUMBER:
                expectPeek(NUMBER);
                break;
            case IDENTIFIER:
                expectPeek(IDENTIFIER);
                if (peekTokenIs(LPAREN) || peekTokenIs(DOT)) {
                    parseSubroutineCall();
                } else {
                    expectPeek(LBRACKET);
                    parseExpression();
                    expectPeek(RBRACKET);
                }
                break;
            case STRING:
                expectPeek(STRING);
                break;
            case FALSE:
            case NULL:
            case TRUE:
                expectPeek(FALSE,NULL,TRUE);
                break;
            default:
                ;
        }
        System.out.println("</term>");
    }


    public String XMLOutput() {
        return xmlOutput.toString();
    }

    private void printNonTerminal(String nterminal) {
        xmlOutput.append(String.format("<%s>\r\n", nterminal));
    }



  

}
