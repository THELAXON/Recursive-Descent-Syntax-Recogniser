import java.io.IOException;

/**
 * SyntaxAnalyser class which extends AbstractSyntaxAnalyser.
 * This class implements the parsing logic for the given grammar rules.
 * 
 * @author Laksan Thavarajah
 */
public class SyntaxAnalyser extends AbstractSyntaxAnalyser{
    String fileName;
    
    /**
     * Constructs a new SyntaxAnalyser object with the specified file name.
     * Initializes the lexical analyzer with the provided file.
     * @param fileName the name of the source code file to be analyzed
     */
    public SyntaxAnalyser(String fileName)
    {
        this.fileName = fileName;
        try 
        {
            lex = new LexicalAnalyser(fileName);
        } 
        catch (IOException e) 
        {
            System.err.println("Cannot load Lexical Analyser due to Error: " + e.getMessage());
        }
    }

     /***
     * returns an error string for all tokens. 
     * @param expected is the expected value
     * @param next is the next token
     * @return the error message string
     */
    public String errorString(String expected, Token next)
    {
        return this.fileName + ": - Expected these tokens: " + expected + " , Found: (' " + next.text + " ')";
    }
 
    /**
     * returns an error string for all non terminals.
     * @param nonTerminal is the name of the non terminal
     * @param token used to receive the line number of the error
     * @return the error message string
     */
    public String nonTerminalError(String nonTerminal, Token token)
    {
        return this.fileName + ": - Parsed error : " + nonTerminal;
    }
    
    /**
     * Accepts a terminal symbol if it matches the next token in the input stream.
     * 
     * @param symbol the expected terminal symbol
     * @throws IOException if an I/O error occurs while parsing
     * @throws CompilationException if a compilation error occurs
     */
    public void acceptTerminal(int symbol) throws IOException, CompilationException 
    {
        if(nextToken.symbol == symbol)
        {
            myGenerate.insertTerminal(nextToken);
            nextToken = lex.getNextToken();
        }
        else
        {
            //if token causes error a string statement is printed
            myGenerate.reportError(nextToken, errorString(" ' " + Token.getName(symbol) + " ' ", nextToken));
        }
    }

    /**
     * Parses the statement part, following grammar rules <statement part> ::= begin <statement list> end.
     * 
     * @throws IOException if an I/O error occurs while parsing
     * @throws CompilationException if a compilation error occurs
     */
	public void _statementPart_() throws IOException, CompilationException 
    {
        myGenerate.commenceNonterminal("StatementPart"); 
        try 
        {
            acceptTerminal(Token.beginSymbol); //check if begin symbol exists
            statementList(); // Parse through statement list
            acceptTerminal(Token.endSymbol); // Accept if end
        } 
        catch (CompilationException e) 
        {
            throw new CompilationException(nonTerminalError(" statement part ", nextToken), e);
        } 
        myGenerate.finishNonterminal("StatementPart");
    }
    /**
     * Parses a statement list, following grammar rules <statement list> ::= <statement> | <statement list> ; <statement.
     * 
     * @throws IOException if an I/O error occurs while parsing
     * @throws CompilationException if a compilation error occurs
     */
    public void statementList() throws IOException, CompilationException
    {
        myGenerate.commenceNonterminal("StatementList"); // Kept name together to avoid confusion in showing start and end
        try 
        {
            statement(); // Parse statement
            while(nextToken.symbol == Token.semicolonSymbol)
            {
                acceptTerminal(Token.semicolonSymbol);
                statementList();
            }
        } 
        catch (CompilationException e)
        {
            throw new CompilationException(nonTerminalError(" statement List ", nextToken), e);
        }
        myGenerate.finishNonterminal("StatementList");
    }

    /**
     * Parses a single statement, following grammar rules <statement> ::= <assignment statement> | <if statement> | <while statement> | <procedure statement> | <until statement> | <for statement>.
     * 
     * @throws IOException if an I/O error occurs while parsing
     * @throws CompilationException if a compilation error occurs
     */
    public void statement() throws IOException, CompilationException
    {
        myGenerate.commenceNonterminal("Statement"); 
        // use try and catch for matching the statement to token and handle error checking. 
        try
        {
            switch(nextToken.symbol)
            {
                case Token.identifier:
                        assignmentStatement();
                        break;
                    case Token.whileSymbol:
                        whileStatement();
                        break;
                    case Token.ifSymbol:
                        ifStatement();
                        break;
                    case Token.callSymbol:
                        procedureStatement();
                        break;
                    case Token.untilSymbol:
                        untilStatement();
                        break;
                    case Token.forSymbol:
                        forStatement();
                        break;
                default:
                    // Defaults to an error if none of the symbols are matched
                    myGenerate.reportError(nextToken, errorString(" ' IDENTIFIER ' , ' if ' , ' while ' , ' call ' , ' until ',  or ' for ' ", nextToken));
            }
        } 
        catch (CompilationException e) 
        {
            throw new CompilationException(nonTerminalError(" statement ", nextToken), e);
        }
        myGenerate.finishNonterminal("Statement");
    }

    /**
     * Parses an assignment statement, following grammar rules  <assignment statement> ::= identifier := <expression> | identifier := stringConstant.
     * 
     * @throws IOException if an I/O error occurs while parsing
     * @throws CompilationException if a compilation error occurs
     */
    public void assignmentStatement() throws IOException, CompilationException
    {
        myGenerate.commenceNonterminal("AssignmentStatement"); // Kept name together to avoid confusion in showing start and end
        try
        {
            acceptTerminal(Token.identifier); // Accepts the identifier
            acceptTerminal(Token.becomesSymbol);// Accepts the assignment operator
            if (nextToken.symbol == Token.stringConstant)// Check if string Constant exists 
            {
                acceptTerminal(Token.stringConstant);
            }
            else
            {
                expression(); // parses the expression
            } 
        } 
        catch (CompilationException e) 
        {
            throw new CompilationException(nonTerminalError(" Assignment Statement ", nextToken), e);
        }
        myGenerate.finishNonterminal("AssignmentStatement");
    }

    /**
     * Parses an if statement following grammar <if statement> ::= if <condition> then <statement list> end if | if <condition> then <statement list> else <statement list> end if.
     * 
     * @throws IOException if an I/O error occurs while parsing
     * @throws CompilationException if a compilation error occurs
     */
    public void ifStatement() throws IOException, CompilationException
    {
        myGenerate.commenceNonterminal("IfStatement"); 
        try
        { 
            acceptTerminal(Token.ifSymbol); //Accepts if symbol
            condition(); // parse the conditions of if statement
            acceptTerminal(Token.thenSymbol); //accept the then symbol
            statementList(); // Parses statement list
            if (nextToken.symbol == Token.elseSymbol)  //checks for else symbol exists then accept it
            {
                acceptTerminal(Token.elseSymbol); //Acccept else
                statementList(); // parse statement list
            }

            acceptTerminal(Token.endSymbol); //Accept end symbol
            acceptTerminal(Token.ifSymbol); // Check for if to follow end to close statement
        }   
        catch (CompilationException e)
        {
            throw new CompilationException(nonTerminalError(" If Statement ", nextToken), e);
        }
        myGenerate.finishNonterminal("IfStatement");
    }
    
    /**
     * Parses a while statement, following grammar rules <while statement> ::= while <condition> loop <statement list> end loop.
     * 
     * @throws IOException if an I/O error occurs while parsing
     * @throws CompilationException if a compilation error occurs
     */
    public void whileStatement() throws IOException, CompilationException
    {
        myGenerate.commenceNonterminal("WhileStatement"); 
        try 
        {
            acceptTerminal(Token.whileSymbol); // Accepts while symbol
            condition();                       // Parses the condition for while loop
            acceptTerminal(Token.loopSymbol);  // Accept loop symbol
            statementList();                   // Parse statement list
            acceptTerminal(Token.endSymbol);   // Accepts end
            acceptTerminal(Token.loopSymbol);  // Checks for loop behind 'end' to close loop
        }
        catch (CompilationException e)
        {
            throw new CompilationException(nonTerminalError(" While statement ", nextToken), e);
        }   
        myGenerate.finishNonterminal("WhileStatement");
    }

    /**
     * Parses a procedure statement following grammar rules <procedure statement> ::= call identifier ( <argument list> ).
     * 
     * @throws IOException if an I/O error occurs while parsing
     * @throws CompilationException if a compilation error occurs
     */
    public void procedureStatement() throws IOException, CompilationException
    {
        myGenerate.commenceNonterminal("ProcedureStatement"); 
        try
        {
            acceptTerminal(Token.callSymbol);   //Accepts call symbol
            acceptTerminal(Token.identifier);   // Accepts identifier
            acceptTerminal(Token.leftParenthesis); // Accepts left paranthesis
            argumentList();                     // Parse argument list
            acceptTerminal(Token.rightParenthesis);// Accepts right paranthesis
        } 
        catch (CompilationException e) 
        {
            throw new CompilationException(nonTerminalError(" Procedure list ", nextToken), e);
        }   
        myGenerate.finishNonterminal("ProcedureStatement");
    }

    /**
     * Parses an until statement, following grammar rules <until statement> ::= do <statement list> until <condition>.
     * 
     * @throws IOException if an I/O error occurs while parsing
     * @throws CompilationException if a compilation error occurs
     */
    public void untilStatement() throws IOException, CompilationException
    {
        myGenerate.commenceNonterminal("UntilStatement"); // Kept name together to avoid confusion in showing start and end
        try
        {
            acceptTerminal(Token.doSymbol); // Accepts do symbol
            statementList();                // Parse through statement list
            acceptTerminal(Token.untilSymbol); // Accepts until symbol
            condition();                     // Parse condition for the loop
        }
        catch (CompilationException e) 
        {
            throw new CompilationException(nonTerminalError(" Until Statement ", nextToken), e);
        } 
        myGenerate.finishNonterminal("UntilStatement");
    }
    
    /**
     * Parses a for statement, following grammar rules <for statement> ::= for ( <assignment statement> ; <condition> ; <assignment statement> ) do <statement list> end loop.
     * 
     * @throws IOException if an I/O error occurs while parsing
     * @throws CompilationException if a compilation error occurs
     */
    public void forStatement() throws IOException, CompilationException
    {
        myGenerate.commenceNonterminal("ForStatement");
        try{
            acceptTerminal(Token.forSymbol); // Accept for symbol
            acceptTerminal(Token.leftParenthesis); // Accept left paranthesis
            assignmentStatement();              // Parse assignment statement
            acceptTerminal(Token.semicolonSymbol); // Accepts the expected semicolon
            condition();                          // parse condition
            acceptTerminal(Token.semicolonSymbol); // Accept semi colon
            assignmentStatement();                 // Parse assignment statement
            acceptTerminal(Token.rightParenthesis); // accepts the right paranthesis
            acceptTerminal(Token.doSymbol);        // accept do symbol
            statementList();                      // parse statement list
            acceptTerminal(Token.endSymbol); // Accepts the end symbol
            acceptTerminal(Token.loopSymbol); // Check for loop symbol to close loop
        }
        catch (CompilationException e) 
        {
            throw new CompilationException(nonTerminalError(" For Statement ", nextToken), e);
        }   

        myGenerate.finishNonterminal("ForStatement");
    }

    /**
     * Parses an argument list, following grammar rules <argument list> ::= identifier |<argument list> , identifier.
     * 
     * @throws IOException if an I/O error occurs while parsing
     * @throws CompilationException if a compilation error occurs
     */
    public void argumentList() throws IOException, CompilationException
    {
        myGenerate.commenceNonterminal("ArgumentList"); 
        try{
            acceptTerminal(Token.identifier);    // Accept first argument
            while (nextToken.symbol == Token.commaSymbol)
            {
                acceptTerminal(Token.commaSymbol); // Accepts comma
                argumentList();                    // parse argument List recursively
            }
        }
        catch (CompilationException e) 
        {
            throw new CompilationException(nonTerminalError(" Argument List ", nextToken), e);
        }   
        myGenerate.finishNonterminal("ArgumentList");
    }

    /**
     * Parses a condition, following grammar rules <condition> ::= identifier <conditional operator> identifier | identifier <conditional operator> numberConstant | identifier <conditional operator> stringConstant.
     * 
     * @throws IOException if an I/O error occurs while parsing
     * @throws CompilationException if a compilation error occurs
     */
    public void condition() throws IOException, CompilationException
    {
        myGenerate.commenceNonterminal("Condition"); 
        try 
        {
            acceptTerminal(Token.identifier); // Accepts identifier
            conditionalOperator();  
            switch(nextToken.symbol)         // switch statement to see which case is the next token
            {
                case Token.numberConstant:
                    acceptTerminal((Token.numberConstant));
                    break;
                case Token.stringConstant:
                    acceptTerminal(Token.stringConstant);
                    break;
                case Token.identifier:
                    acceptTerminal(Token.identifier);
                    break;
                default:
                   // Prints error if none of the symbols match
                    myGenerate.reportError(nextToken, errorString(" < identifer > , < number constant > or < string constant > ", nextToken));
            }
        } 
        catch (CompilationException e) 
        {
            throw new CompilationException(nonTerminalError(" Condition ", nextToken), e);
        }
        myGenerate.finishNonterminal("Condition");

    }

    /**
     * Parses a conditional operator, following grammar rules <conditional operator> ::= > | >= | = | /= | < | <=.
     * 
     * @throws IOException if an I/O error occurs while parsing
     * @throws CompilationException if a compilation error occurs
     */
    public void conditionalOperator() throws IOException, CompilationException
    {
        myGenerate.commenceNonterminal("ConditionalOperator"); 
        try
        {
            switch(nextToken.symbol){// switch statement to see which case will be the next symbol it will then accept that token symbol
                case Token.equalSymbol:
                    acceptTerminal(Token.equalSymbol);
                    break;
                case Token.notEqualSymbol:
                    acceptTerminal(Token.notEqualSymbol);
                    break;
                case Token.greaterThanSymbol:
                    acceptTerminal(Token.greaterThanSymbol);
                    break;
                case Token.lessThanSymbol:
                    acceptTerminal(Token.lessThanSymbol);
                    break;
                case Token.greaterEqualSymbol:
                    acceptTerminal(Token.greaterEqualSymbol);
                    break;
                case Token.lessEqualSymbol:
                    acceptTerminal(Token.lessEqualSymbol);
                    break;
                default:
                    myGenerate.reportError(nextToken, errorString("  ' > '  , ' >= ' , ' = ' , ' /= ' , ' < ' or ' <= ' ", nextToken));
            }
        }
        catch (CompilationException e) 
        {
            throw new CompilationException(nonTerminalError(" Conditional Operator ", nextToken), e);
        } 
        myGenerate.finishNonterminal("ConditionalOperator");
    }

    /**
     * Parses an expression, following grammar rules <expression> ::= <term> | <expression> + <term> | <expression> - <term>.
     * 
     * @throws IOException if an I/O error occurs while parsing
     * @throws CompilationException if a compilation error occurs
     */
    public void expression() throws IOException, CompilationException{
        myGenerate.commenceNonterminal("Expression"); // Kept name together to avoid confusion in showing start and end
        try 
        {
            term();
            // Checks if the + or - symbol exists and if it does accept the correct symbol and then call expression function
            while (nextToken.symbol == Token.plusSymbol || nextToken.symbol == Token.minusSymbol) 
            {
                acceptTerminal(nextToken.symbol); // Accept the '+' or '-' symbol
                expression(); // Handle the next Term recursively
            }
        }
        catch (CompilationException e) 
        {
            throw new CompilationException(nonTerminalError(" Expression ", nextToken), e);
        } 
        myGenerate.finishNonterminal("Expression");
    }

    /**
     * Parses a term, following grammar rules <term> ::= <factor> | <term> * <factor> | <term> / <factor> .
     * 
     * @throws IOException if an I/O error occurs while parsing
     * @throws CompilationException if a compilation error occurs
     */
    public void term() throws IOException, CompilationException
    {
        myGenerate.commenceNonterminal("Term"); // Kept name together to avoid confusion in showing start and end
        try 
        {
            factor();
            // Checks if the times symbol or divide symbol exists and if it does accept the correct symbol and then call term statement
            while(nextToken.symbol == Token.timesSymbol || nextToken.symbol == Token.divideSymbol)
            {
                acceptTerminal(nextToken.symbol); // Accept the number or identifier
                term(); // Handle the next Factor recursively
            }
        }
        catch (CompilationException e) 
        {
            throw new CompilationException(nonTerminalError(" Term ", nextToken), e);
        }
        myGenerate.finishNonterminal("Term");
    }

    /**
     * Parses a factor, following grammar rules <factor> ::= identifier | numberConstant | ( <expression> ).
     * 
     * @throws IOException if an I/O error occurs while parsing
     * @throws CompilationException if a compilation error occurs
     */
    public void factor() throws IOException, CompilationException
    {
        myGenerate.commenceNonterminal("Factor");
        try
        {
            switch(nextToken.symbol) // switch cases to check the next token and default case for report error. 
            {
                case Token.identifier:
                    acceptTerminal(Token.identifier); // Handle identifier
                    break;
                case Token.numberConstant:
                    acceptTerminal(Token.numberConstant); // Handler number Constant
                    break;
                case Token.leftParenthesis:
                    acceptTerminal(Token.leftParenthesis); // handle Left Paranthesis
                    expression();                          // parse expression
                    acceptTerminal(Token.rightParenthesis); // close with right paranthesis
                    break;
                default:
                    // If none of the symbols match then an error is printed
                    myGenerate.reportError(nextToken, errorString(" ' identifier ' , ' number constant ' , ' ( ' , ' ) ' ", nextToken));
            }
        }
        catch (CompilationException e) 
        {
            throw new CompilationException(nonTerminalError(" Factor ", nextToken), e);
        }
        myGenerate.finishNonterminal("Factor");
    }
}