/*
A parser that can understand Valentina/Seamly2D formula, as seen, for example in 
the length attribute of drawing objects.

This outputs the formula as XML which represents the tree structure of the formula 
taking operator precedence into account, and recognising tokens as being numbers 
or references.

MIT License

Copyright (c) 2018 MrDoo71 Jason Dore

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package cloud.mypattern;

import java.io.CharArrayWriter;
import java.io.Writer;
import javax.xml.stream.XMLStreamWriter;    
import javax.xml.stream.XMLOutputFactory;   
import javax.xml.stream.XMLStreamException; 

/**
 * The FormulaParse.
 * 
 * @author Jason Dore
 */
public class FormulaParser 
{

    //Operators.
    private static final int OP_NONE            = 0;
    private static final int OP_MULT            = 1;
    private static final int OP_ADD             = 2;
    private static final int OP_DIVIDE          = 3;
    private static final int OP_SUBTRACT        = 4;
    private static final int OP_POWER           = 5;
    private static final int OP_TERNARY         = 6;
    private static final int OP_GREATERTHAN     = 7;
    private static final int OP_LESSTHAN        = 8;
    private static final int OP_EQUALS          = 9;
    private static final int OP_NOTEQUALS       = 10;
    private static final int OP_GREATERTHANOREQ = 11;
    private static final int OP_LESSTHANOREQ    = 12;
    
    //These can not validly be included in any token    
    char[] delimiters = {' ', '\n', '\t', '(', ')', '*', '+', '-', '/', '^', ',', ';',':','<','>','?' };
    
    
    /** 
     * A part of the formula.
     */
    abstract class Expression
    {        
        String toNormativeString()
        {
            return toString();
        }

        abstract void marshallXML( XMLStreamWriter xmlStream ) throws XMLStreamException;
    }
    
    
    public static class FormulaSyntaxException extends Exception
    {
        FormulaSyntaxException( Exception cause ) 
        {
            super(cause);
        }
    }    
    
    
    /**
     * An operation that has three operands.
     * 
     * For example:  b ? n : m
     * 
     */
    class TertiaryOperation extends Expression
    {
        Expression condition;
        Expression ifTrue;
        Expression ifFalse;
        
        TertiaryOperation( Expression e ) 
        {
            this.condition = e;
        }
        
        @Override
        public String toString()
        {
            return toString( false );
        }
        
        private String toString( boolean normative )
        {
            StringBuilder sb = new StringBuilder();
            boolean showWithBrackets = (!normative);
            if ( showWithBrackets )
                sb.append( " [");
            
            if ( normative )
                sb.append( condition.toNormativeString() );
            else
                sb.append( condition ).append(" ");
            
            sb.append( "?" ); 
            
            if ( normative )
                sb.append( ifTrue.toNormativeString() );
            else
                sb.append(" ").append( ifTrue );
            
            sb.append( ":" ); 
            
            if ( normative )
                sb.append( ifFalse.toNormativeString() );
            else
                sb.append(" ").append( ifFalse );
                        
            if ( showWithBrackets )
                sb.append( "] ");
            
            return sb.toString();
        }      
        
        @Override
        String toNormativeString()
        {
            return toString( true );
        }        
        
        @Override
        void marshallXML( XMLStreamWriter xmlStream ) throws XMLStreamException
        {
            xmlStream.writeStartElement( "operation" );
            xmlStream.writeAttribute( "type", "ternary" );
            condition.marshallXML(xmlStream);
            ifTrue.marshallXML(xmlStream);
            ifFalse.marshallXML(xmlStream);
            xmlStream.writeEndElement();
        }                
    }
    
    
    /**
     * An operation that has two operands.
     * 
     * For example:  a + b
     * 
     */    
    class TwoParameterOperation extends Expression
    {
        Expression parameter1;
        int operator;
        Expression parameter2;

        TwoParameterOperation( Expression e, int operator ) 
        {
            this.parameter1 = e;
            this.operator = operator;
        }
        
        @Override
        public String toString()
        {
            return toString( false );
        }
        
        private String toString( boolean normative )
        {
            StringBuilder sb = new StringBuilder();
            boolean showWithBrackets = (!normative) && getOperatorPrecedence(operator) > 1 ;
            if ( showWithBrackets )
                sb.append( " [");
            
            if ( normative )
                sb.append( parameter1.toNormativeString() );
            else
                sb.append( parameter1 ).append(" ");
            
            switch( operator)
            {
                case OP_ADD             : sb.append( "+" ); break;
                case OP_MULT            : sb.append( "*" ); break;
                case OP_DIVIDE          : sb.append( "/" ); break;
                case OP_SUBTRACT        : sb.append( "-" ); break;
                case OP_POWER           : sb.append( "^" ); break;
                case OP_GREATERTHAN     : sb.append( ">" );break;
                case OP_LESSTHAN        : sb.append( "<" ); break;
                case OP_GREATERTHANOREQ : sb.append( ">" );break;
                case OP_LESSTHANOREQ    : sb.append( "<" ); break;
                case OP_EQUALS          : sb.append( "==" ); break;
                case OP_NOTEQUALS       : sb.append( "!=" ); break;
            }
            
            if ( normative )
                sb.append( parameter2.toNormativeString() );
            else
                sb.append(" ").append( parameter2 );
            
            if ( showWithBrackets )
                sb.append( "] ");
            
            return sb.toString();
        }      
        
        @Override
        String toNormativeString()
        {
            return toString( true );
        }        
        
        @Override
        void marshallXML( XMLStreamWriter xmlStream ) throws XMLStreamException
        {
            String tag;
            switch( operator)
            {
                case OP_ADD             : tag = "add"; break;
                case OP_MULT            : tag = "multiply"; break;
                case OP_DIVIDE          : tag = "divide"; break;
                case OP_SUBTRACT        : tag = "subtract"; break;
                case OP_POWER           : tag = "power"; break;
                case OP_GREATERTHAN     : tag = "greaterThan"; break;
                case OP_LESSTHAN        : tag = "lessThan"; break;
                case OP_GREATERTHANOREQ : tag = "greaterThanOrEqual"; break;
                case OP_LESSTHANOREQ    : tag = "lessThanOrEqual"; break;
                case OP_EQUALS          : tag = "equalTo"; break;
                case OP_NOTEQUALS       : tag = "notEqualTo"; break;
                default:
                    throw new XMLStreamException("Uknown operator");
            }
            
            xmlStream.writeStartElement( "operation" );
            xmlStream.writeAttribute( "type", tag );
            parameter1.marshallXML(xmlStream);
            parameter2.marshallXML(xmlStream);
            xmlStream.writeEndElement();
        }        
    }
    
    
    /**
     * A pair of parenthesis.
     */
    class BracketPair extends Expression
    {        
        Expression innerExpression;
        
        @Override
        public String toString()
        {
            return "(" + innerExpression.toString() + ")";
        }        
        @Override
        String toNormativeString()
        {
            return "(" + innerExpression.toNormativeString() + ")";
        }        
        
        @Override
        void marshallXML( XMLStreamWriter xmlStream ) throws XMLStreamException
        {
            xmlStream.writeStartElement( "parenthesis" );
            innerExpression.marshallXML(xmlStream);
            xmlStream.writeEndElement();
        }            
    }
    
    
    /**
     * A function. 
     * 
     * For example:  b ? n : m
     */    
    class Function extends Expression
    {
        String function;
        Expression parameter1;
        Expression parameter2;
        
        @Override
        public String toString()
        {
            return function + "(" + parameter1 + ( parameter2 == null ? "" : "," + parameter2 ) + ")";
        }                
        
        @Override
        String toNormativeString()
        {
            if (   ( function.equals("-") ) 
                && ( parameter2 == null ))
                return function + parameter1.toNormativeString();
                
            return function + "(" + parameter1.toNormativeString() + ( parameter2 == null ? "" : "," + parameter2.toNormativeString() ) + ")";
        }        
        
        @Override
        void marshallXML( XMLStreamWriter xmlStream ) throws XMLStreamException
        {
            xmlStream.writeStartElement( "function" );
            
            xmlStream.writeAttribute( "type", function );
            
            parameter1.marshallXML(xmlStream);
            
            if ( parameter2 != null )
                parameter2.marshallXML(xmlStream);
            
            xmlStream.writeEndElement();
        }                            
    }
    
    
    /**
     * A variable name, measurement name etc.
     * 
     * Also overridden for tokens that are numbers.
     */    
    class Token extends Expression
    {        
        String token;
        boolean isCustom;
        boolean isHashToken;
        
        @Override
        public String toString()
        {
            return token;
        }                
        
        @Override
        String toNormativeString()
        {
            return token;
        }        
        
        @Override
        void marshallXML( XMLStreamWriter xmlStream ) throws XMLStreamException
        {
            xmlStream.writeStartElement( "variable" );
            
            if ( isCustom )
                xmlStream.writeAttribute( "custom", "true" );
            
            if ( isHashToken )
                xmlStream.writeAttribute( "hash", "true" );         
            
            xmlStream.writeCharacters( token );
            xmlStream.writeEndElement();
        }                    
    }
    
        
    /**
     * A double float numeric token.
     * 
     * E.g. 3.1415
     */
    class DoubleToken extends Expression
    {        
        Double d;

        private DoubleToken(Double d) {
            this.d = d;
        }
        
        @Override
        public String toString()
        {
            return " " + Double.toString(d) + "D ";
        }                
        
        @Override
        String toNormativeString()
        {
            return Double.toString(d);
        }        
        
        @Override
        void marshallXML( XMLStreamWriter xmlStream ) throws XMLStreamException
        {
            xmlStream.writeStartElement( "decimal" );
            xmlStream.writeCharacters( Double.toString(d) );
            xmlStream.writeEndElement();
        }                            
    }
    
    
    /**
     * A integer numeric token.
     * 
     * E.g. 2
     */
    class IntegerToken extends Expression
    {        
        Integer i;

        private IntegerToken(Integer i) {
            this.i = i;
        }
        
        @Override
        public String toString()
        {
            return " " + Integer.toString(i) + "I ";
        }                
        
        @Override
        String toNormativeString()
        {
            return Integer.toString(i);
        }        
        
        @Override
        void marshallXML( XMLStreamWriter xmlStream ) throws XMLStreamException
        {
            xmlStream.writeStartElement( "integer" );
            xmlStream.writeCharacters( Integer.toString(i) );
            xmlStream.writeEndElement();
        }                                    
    }    

    
    //The outer expression
    Expression expression;
    
    
    /**
     * Parse the formula provided and return it as an XML representation.
     * 
     * This is the static entry allowing this library to be used as a single static
     * method call. 
     * 
     * @param formula
     * @return
     * @throws cloud.mypattern.FormulaParser.FormulaSyntaxException 
     */
    public static String formulaToXML( String formula ) throws FormulaSyntaxException
    {
        FormulaParser fp;
        try {
            fp = new FormulaParser( new Stream( formula ) );
        } catch ( Exception e ) {
            
            throw new FormulaSyntaxException( e );
        }
        
        return fp.marshall();
    }
    
    
    /**
     * Create a new FormulaParser.
     * 
     * @param s  The stream from the text representation. 
     * @throws cloud.mypattern.Stream.SyntaxException 
     */
    public FormulaParser( Stream s ) throws Stream.SyntaxException
    {
        expression = parseExpression( s, 1 );
    }    
    
    
    /**
     * Get a parameter from the stream for use with the specified precedence
     * 
     * So, if the parameter's next operation is high precedence then we should handle that here.
     * 
     * e.g. 
     * parseExpressionParameter( "2 + 3 * 4^5 + 1", 1 ) should return "2"
     * parseExpressionParameter( "3 * 4^5 + 1" , 1 ) should return "3 * 4^5"
     * 
     * @param s
     * @param precedence
     * @return
     * @throws Stream.SyntaxException 
     */
    private Expression parseExpressionParameter( Stream s, int precedence ) throws Stream.SyntaxException
    {
        if ( s.isTokenDelimeter( s.lookahead(), new char[]{'+','*','/','^'} ) ) //nb '-' is permitted here because it might be: -1
        {
            throw s.throwException( "Unexpected token (operator not expected here):" + s.lookahead() );
        } 
        
        Expression e;
        
        if ( s.lookahead() == '(')
        {
            s.consumeChar( '(' );
            
            BracketPair bp = new BracketPair();
            bp.innerExpression = parseExpression(s, 1);
                    
            s.consumeOptionalWhiteSpace();
            s.consumeChar( ')' );
            e = bp; 
        }
        else if ( s.lookahead() == '@' )
        {
            s.consumeChar( '@' );
            Token tk = new Token();
            tk.token = "@" + s.getToken( delimiters );
            tk.isCustom = true;
            e = tk;
        }
        else if ( s.lookahead() == '#' )
        {
            s.consumeChar( '#' );
            Token tk = new Token();
            tk.token = "#" + s.getToken( delimiters );
            tk.isHashToken = true;
            e = tk;
        }
        else 
        {
            boolean unaryNegative = false;
            if ( s.lookahead() == '-')
            {
                unaryNegative = true;
                s.consumeChar('-');
                s.consumeOptionalWhiteSpace();
            }
            
            if ( unaryNegative && ( s.lookahead() == '(' ) )
            {
                s.consumeChar( '(' );
                
                Function f = new Function();
                f.function = "-";
                f.parameter1 = parseExpression(s, 1);

                s.consumeOptionalWhiteSpace();
                s.consumeChar( ')' );                    
                e = f;
            }
            else
            {
                String token = s.getToken( delimiters );
                //Token tk;

                try {
                    Integer i = Integer.parseInt( token );

                    if ( unaryNegative )
                        i = -1 * i;

                    e = new IntegerToken( i );


                } catch ( NumberFormatException ne ) {

                    try {
                        Double d = Double.parseDouble(token);

                        if ( unaryNegative )
                            d = -1 * d;

                        e = new DoubleToken( d );

                    } catch ( Exception fe ) {         

                        s.consumeOptionalWhiteSpace();
                        if ( s.lookahead() == '(' )
                        {
                            //a method call
                            Function f = new Function();
                            f.function = token;
                            s.consumeChar( '(' );
                            f.parameter1 = parseExpression(s, 1);
                            s.consumeOptionalWhiteSpace();

                            if (   ( s.lookahead() == ',' )
                                || ( s.lookahead() == ';' ) )
                            {
                                s.consumeChar();
                                f.parameter2 = parseExpression(s, 1);
                                s.consumeOptionalWhiteSpace();                           
                            }
                            s.consumeChar( ')' );
                            e = f;
                            
                            if ( unaryNegative )
                            {
                                Function un = new Function();
                                un.function = "-";
                                un.parameter1 = f;
                                e = un;
                            }
                            
                            s.consumeOptionalWhiteSpace();

                        }
                        else
                        {
                            //Stick with the string.
                            Token tk = new Token();
                            tk.token = token;     
                            e = tk;
                            
                            if ( unaryNegative )
                            {
                                Function un = new Function();
                                un.function = "-";
                                un.parameter1 = tk;
                                e = un;
                            }                            
                        }
                    }                
                }
            }
        }        
        
        s.consumeOptionalWhiteSpace();
        int operator = getOperatorFromLookahead(s);
        int nextPrecedence = getOperatorPrecedence( operator );
        
        if ( nextPrecedence > precedence )
        {
            assert operator != OP_TERNARY; //as this is the lowest precedence
            
            s.consumeChar(); //the operator.
            
            //Some operators are formed of two characters: == !=
            switch( operator )
            {
                case OP_GREATERTHAN:
                    if ( s.lookahead() =='=' )
                    {
                        s.consumeChar( '=' );
                        operator = OP_GREATERTHANOREQ; //this has the same precedence
                    }
                    break;
                    
                case OP_LESSTHAN:
                    if ( s.lookahead() =='=' )
                    {
                        s.consumeChar( '=' );
                        operator = OP_LESSTHANOREQ; //this has the same precedence
                    }
                    break;
                    
                case OP_EQUALS:
                case OP_NOTEQUALS:
                    s.consumeChar( '=' );
            }
            
            s.consumeOptionalWhiteSpace();
            TwoParameterOperation twoOp = new TwoParameterOperation( e, operator );
            twoOp.parameter2 = parseExpression( s, nextPrecedence );
            e = twoOp;
        }
        
        return e;
    }
    
    
    /**
     * Parse an expression from the stream.
     * ( a )
     * (( a ))
     * a + b / c - d
     * 
     * 
     * @param s
     * @throws Stream.SyntaxException 
     */
    private Expression parseExpression( Stream s, int precedence ) throws Stream.SyntaxException
    {
        int operation = OP_NONE; //might just be a bracketted expression. 
        
        s.consumeOptionalWhiteSpace();

        Expression expression = parseExpressionParameter( s, precedence );
        
        s.consumeOptionalWhiteSpace();
        
        while( ! s.isEOF() )
        {
            operation = getOperatorFromLookahead( s );

            if ( operation == OP_NONE )
            {
                break;
            }
            
            //Some operators are formed of two characters: == !=
            switch( operation )
            {
                case OP_GREATERTHAN:
                    if ( s.lookahead() =='=' )
                    {
                        s.consumeChar( '=' );
                        operation = OP_GREATERTHANOREQ; //this has the same precedence
                    }
                    break;
                    
                case OP_LESSTHAN:
                    if ( s.lookahead() =='=' )
                    {
                        s.consumeChar( '=' );
                        operation = OP_LESSTHANOREQ; //this has the same precedence
                    }
                    break;
                                    
                case OP_EQUALS:
                case OP_NOTEQUALS:
                    s.consumeChar( '=' );
            }

            int nextPrecedence = getOperatorPrecedence( operation );
//TODO why does this fail with 3.1415 * 22 + @width * height 
//            assert nextPrecedence <= precedence; //otherwise it would have got handled by parseExpressionParameter
            
            if ( nextPrecedence < precedence )
                return expression;

            if ( operation == OP_TERNARY )
            {
                TertiaryOperation threeOp = new TertiaryOperation(expression);
                s.consumeChar( '?' ); //the operator
                s.consumeOptionalWhiteSpace();
                
                threeOp.ifTrue = parseExpression(s,1);//Parameter( s, 2 );//precedence );
                
                s.consumeOptionalWhiteSpace();
                s.consumeChar(':');
                s.consumeOptionalWhiteSpace();
                
                
                threeOp.ifFalse = parseExpression(s,1);//Parameter( s, 2);/// precedence );
                expression = threeOp;
            }
            else
            {
                TwoParameterOperation twoOp = new TwoParameterOperation( expression, operation );

                s.consumeChar(); //the operator
                s.consumeOptionalWhiteSpace();

                twoOp.parameter2 = parseExpressionParameter( s, precedence );

                expression = twoOp;    
            }
                        
            s.consumeOptionalWhiteSpace();
        }
        
        return expression;
    }
    
    
    int getOperatorPrecedence( int operator )
    {
        switch( operator )
        {
            case OP_POWER:
                return 5;
                
            case OP_DIVIDE:
            case OP_MULT:
                return 4;
            
            case OP_ADD:
            case OP_SUBTRACT:
                return 3;
                
            case OP_GREATERTHAN:
            case OP_LESSTHAN:
            case OP_EQUALS:
            case OP_NOTEQUALS:
            case OP_GREATERTHANOREQ:
            case OP_LESSTHANOREQ:
                return 2;
                
            case
                OP_TERNARY:
                return 1;
                    
            default:     
                return 0;
        }
    }
    
    
    /**
     * Return the operator, or OP_NONE represented by the next character in the
     * stream.
     *
     * @param s
     * @return 
     */
    int getOperatorFromLookahead( Stream s )
    {
        if ( s.lookahead() == '*' )
            return OP_MULT;
        else if ( s.lookahead() == '+' )
            return OP_ADD;
        else if ( s.lookahead() == '/' )
            return OP_DIVIDE;
        else if ( s.lookahead() == '-' )
            return OP_SUBTRACT;
        else if ( s.lookahead() == '^' )
            return OP_POWER;
        else if ( s.lookahead() == '?' )
            return OP_TERNARY;
        else if ( s.lookahead() == '>' )
            return OP_GREATERTHAN;
        else if ( s.lookahead() == '<' )
            return OP_LESSTHAN;
        else if ( s.lookahead() == '=' )
            return OP_EQUALS;
        else if ( s.lookahead() == '!' )
            return OP_NOTEQUALS;
        else
            return OP_NONE;
    }     


    @Override
    public String toString()
    {
        return expression.toString();
    }       
    
    
    /**
     * Return the expression as XML.
     * 
     * @return 
     */
    String marshall() throws FormulaSyntaxException
    {
        Writer out = new CharArrayWriter();
        try { 
            XMLOutputFactory output = XMLOutputFactory.newInstance();
            XMLStreamWriter xmlStream = output.createXMLStreamWriter( out );
            
            //XSNamespace ns = binding.getSchema().getNamespace();
            //if ( ns != null )
            //    xmlStream.setDefaultNamespace( ns.getURI() );
                        
            xmlStream.writeStartDocument();      
            
            expression.marshallXML( xmlStream );
            
            out.flush();
            return out.toString();
                        
        } catch ( Exception e ) {
            throw new FormulaSyntaxException(e);
        }  
    }
    
}
