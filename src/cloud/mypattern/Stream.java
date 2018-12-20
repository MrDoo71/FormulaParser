/*
A stream that provides functions required of a simple parser.

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

/**
 * A stream with parser support.
 * 
 * @author Jason Dore
 */
public class Stream
{
    
    class SyntaxException extends Exception
    {
        public SyntaxException(String msg)
        {
            super(msg);
        }
    };


    SyntaxException throwException( String msg )
    {
        return new SyntaxException(msg);
    }
    
    
    char[] chars;
    int index;

    
    /**
     * Create a stream from the provided String.
     * 
     * @param s 
     */
    public Stream( String s )
    {
        //this.s = s;
        chars = s.toCharArray();
        index = 0;
    }


    /** 
     * Returns the character at the current index position, whilst consuming it and moving the index forward. 
     */
    char consumeChar()
    {
        return chars[ index++ ];
    }


    /**
     * Test whether we have reached the end of the stream.
     * 
     * @return true if we are at the end of the file.
     */
    boolean isEOF()
    {
        return index >= chars.length;
    }

    
    /**
     * Consume the current character, but only if it is the character we are expecting,
     * otherwise throw an error.
     * 
     * @param expectedChar
     * @return
     * @throws cloud.mypattern.Stream.SyntaxException 
     */
    char consumeChar( char expectedChar ) throws SyntaxException
    {
        if ( lookahead() == expectedChar )
            return consumeChar();

        throw new SyntaxException( "Expected " + expectedChar + " found " + lookahead() );
    }


    /** 
     * Returns the character at the current index position, without consuming it. 
     */
    char lookahead()
    {
        if ( index >= chars.length )
            return 0;

        return chars[ index ];
    }


    /**
     * Get the next token from the stream using specified delimiters.
     * 
     * A token in a sequence of characters that does not include any of the 
     * delimiters.
     * 
     * @return
     * @throws cloud.mypattern.Stream.SyntaxException 
     */    
    String getToken( char[] delimiters ) throws SyntaxException
    {
        StringBuilder token = new StringBuilder();

        if ( isEOF() )
            throw new SyntaxException( "getToken() EOF. " );
        
        char c = consumeChar();

        if ( isWhiteSpace( c ) )
            throw new SyntaxException("getToken() found whitespace. " );

        token.append( c );

        char l = lookahead();

        while( (!isTokenDelimeter(l, delimiters)) && (!isEOF()) )
        {
            token.append( consumeChar() );
            l = lookahead();
        }
        return token.toString();
    }


    /**
     * Test whether the specified character is whitespace.
     * 
     * @param c
     * @return 
     */
    private boolean isWhiteSpace( char c )
    {
        return (( c == ' ' ) || ( c == '\n' ) || ( c == '\t') );
    }


    /**
     * Test whether the specified character is in the set of delimiters.
     * 
     * @param c
     * @param delimiters
     * @return true if c is a character in the array of delimiters.
     */
    boolean isTokenDelimeter( char c, char[] delimiters )
    {
        for( int i=0, s=delimiters.length; i<s; i++ )
        {
            if ( c == delimiters[i] )
                return true;
        }
        return false;
    }


    /** 
     * Consume whitepsace from the stream, return true if any was consumed. 
     */
    boolean consumeOptionalWhiteSpace()
    {
        boolean whiteSpaceFound = false;

        while( true )
        {
            if ( isWhiteSpace( lookahead() ) )
            {
                consumeChar();
                whiteSpaceFound = true;
            }
            else //not whiteSpace, or EOF
            {
                return whiteSpaceFound;
            }
        }
    }


    @Override
    public String toString()
    {
        if ( index >= chars.length )
            return String.copyValueOf(chars) + "[]";
        
        return ( index > 0 ? String.copyValueOf(chars, 0, index) : "" )
                + "[" + chars[index] + "]"
                + ( (index+1) < (chars.length-1) ? String.copyValueOf(chars, index+1, chars.length - index -1 ) : "" );
    }
}
