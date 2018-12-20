/*
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

import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 *
 * @author jase
 */
public class FormulaTests {
    

	@SuppressWarnings("unused")
	private final static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getName() );
	

	

    @Test
    public void basicTests() throws Stream.SyntaxException
    {
        test( "a + 5", "a+5", 
                " [a +  5I ] ", 
                "<operation type=\"add\"><variable>a</variable><integer>5</integer></operation>" );
        
        //Test that divide has precedence over add
        test( "a + 5 / 2", "a+5/2", 
                    " [a +  [ 5I  /  2I ] ] ",
                    "<operation type=\"add\">"
                        + "<variable>a</variable>"
                        + "<operation type=\"divide\">"
                            + "<integer>5</integer>"
                            + "<integer>2</integer>"
                        + "</operation>"
                    + "</operation>" );

        //Test that multiple has precedence over add
        test( "a + 5 * 2", "a+5*2", 
                " [a +  [ 5I  *  2I ] ] ",
                "<operation type=\"add\">"
                        + "<variable>a</variable>"
                        + "<operation type=\"multiply\">"
                            + "<integer>5</integer>"
                            + "<integer>2</integer>"
                        + "</operation>"
                    + "</operation>" );

        //Test that brackets give addition precedence
        test( "(a + 5) / 2", "(a+5)/2", 
                " [( [a +  5I ] ) /  2I ] ",
                "<operation type=\"divide\">"
                    + "<parenthesis>"
                        + "<operation type=\"add\">"
                            + "<variable>a</variable>"
                            + "<integer>5</integer>"
                        + "</operation>"
                    + "</parenthesis>"
                    + "<integer>2</integer>"
                + "</operation>" );        
        

        test( "2 + 3 + 4 / 5 + 6 + 7", "2+3+4/5+6+7", 
                " [ 2I  +  [ [ [ 3I  +  [ 4I  /  5I ] ]  +  6I ]  +  7I ] ] ",
                "<operation type=\"add\">"
                    + "<integer>2</integer>"
                    + "<operation type=\"add\">"
                        + "<operation type=\"add\">"
                            + "<operation type=\"add\">"
                                + "<integer>3</integer>"
                                + "<operation type=\"divide\">"
                                    + "<integer>4</integer>"
                                    + "<integer>5</integer>"
                                + "</operation>"
                            + "</operation>"
                            + "<integer>6</integer>"
                        + "</operation>"
                        + "<integer>7</integer>"
                    + "</operation>"
                + "</operation>" );
//            
              //Parenthesis test
//            test( "(( 2 + 3 + 4 ) / (( 5 + 6 ) + 7) )" );
//            
              //Another mixed precedence test
//            test( "1+2*3^4*5" );
//            
//            test( "3.1415" );
//            
              //Integer and decimal literals
              test( "-3 + -3.1415", "-3+-3.1415", 
                      " [ -3I  +  -3.1415D ] ", 
                      "<operation type=\"add\"><integer>-3</integer><decimal>-3.1415</decimal></operation>");
//            
//            test( "-3" );

              //Another precedence test
              test( "3.1415 * 22 + @width * height ", 
                      "3.1415*22+@width*height", 
                      " [ [ 3.1415D  *  22I ]  +  [@width * height] ] ",
                      "<operation type=\"add\">"
                          + "<operation type=\"multiply\">"
                              + "<decimal>3.1415</decimal>"
                              + "<integer>22</integer>"
                          + "</operation>"
                          + "<operation type=\"multiply\">"
                              + "<variable custom=\"true\">@width</variable>"
                              + "<variable>height</variable>"
                          + "</operation>"
                      + "</operation>" );
              
              //Nested ternary operators
              test( "a == 1 ? 1 : a == 2 ? 2 : 3", "a==1?1:a==2?2:3", 
                      " [ [a ==  1I ]  ?  1I :  [ [a ==  2I ]  ?  2I :  3I ] ] ", 
                      "<operation type=\"ternary\">"
                          //if
                          + "<operation type=\"equalTo\">"
                              + "<variable>a</variable>"
                              + "<integer>1</integer>"
                          + "</operation>"
                          //then
                          + "<integer>1</integer>"
                          //else
                          + "<operation type=\"ternary\">"
                              //if
                              + "<operation type=\"equalTo\">"
                                  + "<variable>a</variable>"
                                  + "<integer>2</integer>"
                              + "</operation>"
                              //then
                              + "<integer>2</integer>"
                              //else
                              + "<integer>3</integer>"
                          + "</operation>"
                      + "</operation>" );
            
            
              //Test functions and ternary operators
              test( "sin(a) > atan(b+c/2) ? 2.41 * #length : Line_A1_A2", 
                      "sin(a)>atan(b+c/2)?2.41*#length:Line_A1_A2", 
                      " [ [sin(a) > atan( [b +  [c /  2I ] ] )]  ?  [ 2.41D  * #length] : Line_A1_A2] ", 
                      "<operation type=\"ternary\">"
                            //if
                          + "<operation type=\"greaterThan\">"
                              + "<function type=\"sin\">"
                                  + "<variable>a</variable>"
                              + "</function>"
                              + "<function type=\"atan\">"
                                  + "<operation type=\"add\">"
                                      + "<variable>b</variable>"
                                      + "<operation type=\"divide\">"
                                          + "<variable>c</variable>"
                                          + "<integer>2</integer>"
                                      + "</operation>"
                                  + "</operation>"
                              + "</function>"
                          + "</operation>"
                            //then
                          + "<operation type=\"multiply\">"
                              + "<decimal>2.41</decimal>"
                              + "<variable hash=\"true\">#length</variable>"
                          + "</operation>"
                            //else
                          + "<variable>Line_A1_A2</variable>"
                   + "</operation>");
//            
//            test( "tan(a) * cos(b) + max( a, b ) + max( a; b )");

              //Some references start with #
              test( "(#BustCircumfence < 100 ? #BustCircumfence/5-1: #BustCircumfence/10+10.5)+3", 
                      "(#BustCircumfence<100?#BustCircumfence/5-1:#BustCircumfence/10+10.5)+3", 
                      " [( [ [#BustCircumfence <  100I ]  ?  [ [#BustCircumfence /  5I ]  -  1I ] :  [ [#BustCircumfence /  10I ]  +  10.5D ] ] ) +  3I ] ", 
                      "<operation type=\"add\"><parenthesis><operation type=\"ternary\"><operation type=\"lessThan\"><variable hash=\"true\">#BustCircumfence</variable><integer>100</integer></operation><operation type=\"subtract\"><operation type=\"divide\"><variable hash=\"true\">#BustCircumfence</variable><integer>5</integer></operation><integer>1</integer></operation><operation type=\"add\"><operation type=\"divide\"><variable hash=\"true\">#BustCircumfence</variable><integer>10</integer></operation><decimal>10.5</decimal></operation></operation></parenthesis><integer>3</integer></operation>");
              
//            test( "(#BustCircumfence < 112 ? #BustCircumfence/5-1 : #BustCircumfence/2-(#BustCircumfence/10+2+#BustCircumfence/10+10.5))+1.5" );

              test( "-(AngleLine_A50_A27-AngleLine_A51_A27)", null, null, 
                      "<function type=\"-\">"
                          + "<operation type=\"subtract\">"
                              + "<variable>AngleLine_A50_A27</variable>"
                              + "<variable>AngleLine_A51_A27</variable>"
                          + "</operation>"
                      + "</function>");
              
//            test( "#CG/10+(#CG<116?11:10.5 )" );

              test( "( #isCloseFittingSleevelessBlock == 0 ? 0 : -1 )", null, null, null) ;
//            test( "3.5 / 12 * #BustWaistDifference  + ( #isCloseFittingSleevelessBlock == 0 ? 0 : -1 )") ;

              //Unary operator
//            test( " - bust_circ" );

              //Unary negative and function
              test( " - sin(360)", 
                      "-sin(360)", 
                      "-(sin( 360I ))", 
                      "<function type=\"-\">"
                          + "<function type=\"sin\">"
                              + "<integer>360</integer>"
                          + "</function>"
                      + "</function>" );
              
              //Unary and binary operator              
              test( " - bust_circ / 2", 
                      "-bust_circ/2", 
                      " [-(bust_circ) /  2I ] ", 
                      "<operation type=\"divide\">"
                          + "<function type=\"-\">"
                              + "<variable>bust_circ</variable>"
                          + "</function>"
                          + "<integer>2</integer>"
                      + "</operation>"); 
              
//            test( "( -(bust_circ) / 2 * #NegativeBustEase)" );
//            test( "( #isCloseFittingSleevelessBlock == 1 ? 2 : ( -(bust_circ) / 2 * #NegativeBustEase) )" );
//            test( "#isCloseFittingSleevelessBlock == 0 ?  5 : ( #isCloseFittingSleevelessBlock == 1 ? 2 : ( -(bust_circ) / 2 * #NegativeBustEase) )" );            
//            test( "#isCloseFittingSleevelessBlock == 0 ?  5 : ( #isCloseFittingSleevelessBlock == 1 ? 2 : ( - bust_circ / 2 * #NegativeBustEase) )" );
            
            
            //test( "bust_circ <= 112 ? (bust_circ*(2/10)) +#FaktorBrustbreite+#ZugabeBrustbreite : (bust_circ/2) - (bust_circ/10+#FaktorRückenbreite) - (bust_circ/10+#FaktorArmlochdurchmesser) +#ZugabeBrustbreite" );
            test( "#isCloseFittingSleevelessBlock == 0 ?  2 : #isCloseFittingSleevelessBlock == 1 ? 0 : ( - hip_circ / 2 * #NegativeHipEase)", 
                    null, 
                    " [ [#isCloseFittingSleevelessBlock ==  0I ]  ?  2I :  [ [#isCloseFittingSleevelessBlock ==  1I ]  ?  0I : ( [-(hip_circ) /  [ 2I  * #NegativeHipEase] ] )] ] ", 
                    "<operation type=\"ternary\">"
                        //if 
                        + "<operation type=\"equalTo\">"
                            + "<variable hash=\"true\">#isCloseFittingSleevelessBlock</variable>"
                            + "<integer>0</integer>"
                        + "</operation>"
                        //then
                        + "<integer>2</integer>"
                        //else
                        + "<operation type=\"ternary\">"
                            //if
                            + "<operation type=\"equalTo\">"
                                + "<variable hash=\"true\">#isCloseFittingSleevelessBlock</variable>"
                                + "<integer>1</integer>"
                            + "</operation>"
                            //then
                            + "<integer>0</integer>"
                            //else
                            + "<parenthesis>"
                                + "<operation type=\"divide\">"
                                    + "<function type=\"-\">"
                                        + "<variable>hip_circ</variable>"
                                    + "</function>"
                                    + "<operation type=\"multiply\">"
                                        + "<integer>2</integer>"
                                        + "<variable hash=\"true\">#NegativeHipEase</variable>"
                                    + "</operation>"
                                + "</operation>"
                            + "</parenthesis>"
                        + "</operation>"
                    + "</operation>" );
            
            //High precedence of power
            test( "1+2*3^4*5", "1+2*3^4*5", 
                    " [ 1I  +  [ 2I  *  [ [ 3I  ^  4I ]  *  5I ] ] ] ", 
                    "<operation type=\"add\">"
                        + "<integer>1</integer>"
                        + "<operation type=\"multiply\">"
                            + "<integer>2</integer>"
                            + "<operation type=\"multiply\">"
                                + "<operation type=\"power\">"
                                    + "<integer>3</integer>"
                                    + "<integer>4</integer>"
                                + "</operation>"
                                + "<integer>5</integer>"
                            + "</operation>"
                        + "</operation>"
                    + "</operation>" );        
        
    }
    
    
    private static void test( String formula, String expectedNormative, String expectedToString, String expectedXML ) throws Stream.SyntaxException
    {
        Stream s = new Stream( formula );
        FormulaParser u = new FormulaParser( s );
        
        if (( expectedNormative == null ) || ( expectedXML == null ) || ( expectedToString == null ) )
            System.out.println(formula );
        
        if ( expectedNormative != null )
            assertTrue( "Normative form failure for " + formula, expectedNormative.equals( u.expression.toNormativeString() ) );
        else
            System.out.println( u.expression.toNormativeString() );    

        if ( expectedToString != null )
            assertTrue( "toString failure for " + formula, expectedToString.equals( u.toString() ) );
        else
            System.out.println( u.toString() );    
        
        
        try {

            if ( expectedXML != null )
                assertTrue( "XML form failure for " + formula, ("<?xml version=\"1.0\" ?>" + expectedXML).equals( u.marshall() ) );
            else
                System.out.println( u.marshall() );
                
        } catch ( FormulaParser.FormulaSyntaxException ex ) {
            throw s.throwException( ex.getMessage() );
        }       
    }    
    
        
}    
