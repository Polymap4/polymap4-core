/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.runtime;

import java.util.Map;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.FieldPosition;
import java.text.MessageFormat;

import com.google.common.collect.ImmutableList;

/**
 * Provides methods to create and manipulate Strings.
 * <ul>
 * <li>building Strings by appending, insering, replacing, joining parts</li>
 * <li>pluggable converters to transform different types of arguments into Strings</li>
 * <li>formatting via {@link MessageFormat}
 * <li>converting into valid names of files, URL components, etc.</li>
 * <li>splitting and substrings</li>
 * </ul>
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public final class Stringer
        implements CharSequence, Appendable {

    // static factories ***********************************
    
    public static Stringer on( String s ) {
        return new Stringer( s );    
    }
    
    public static Stringer withCapacity( int capacity ) {
        return new Stringer( capacity );
    }
    
    
    // instance *******************************************
    
    private StringBuilder                   data;
    
    private Map<Class,ToStringConverter>    converters;
    
    
    protected Stringer( String s ) {
        this.data = new StringBuilder( s );
    }
    
    protected Stringer( int capacity ) {
        this.data = new StringBuilder( capacity );
    }
    
    public boolean equals( CharSequence s ) {
        int n = length();
        if (n == s.length()) {
            for (int i=0; i<n; i++) {
                if (charAt( i ) != s.charAt(i))
                    return false;
            }
            return true;
        }
        else {
            return false;
        }
    }
    
    public boolean isEmpty() {
        return length() == 0;
    }
    
    public Stringer replace( CharSequence s ) {
        if (!equals( s )) {
            replace( 0, length(), s );
        }
        return this;
    }
    
    public String toString() {
        return data.toString();
    }
    
    
    // toString converter *********************************
    
    /**
     * 
     */
    public static interface ToStringConverter {
        public String toString( Object o );
    }
    
    public Stringer useConverter( Class cl, ToStringConverter converter ) {
        assert cl != null;
        assert converter != null;
        return this;
    }
    
    protected ToStringConverter converterFor( Class cl ) {
        return converters != null ? converters.get( cl ) : null;
    }
    
    protected String toString( Object o ) {
        ToStringConverter converter = converterFor( o.getClass() );
        return converter != null ? converter.toString( o ) : o.toString();
    }
    
    // substring ******************************************

    /**
     * Gets the substring after the first occurrence of a separator. The separator is
     * not returned.
     * <p/>
     * A <code>null</code> string input will return <code>null</code>. An empty ("")
     * string input will return the empty string. A <code>null</code> separator will
     * return the empty string if the input string is not <code>null</code>.
     * <p/>
     * <pre>
     * ""       : .substringAfter( * )       = ""
     * *        : .substringAfter( null )    = NPE
     * "abc"    : .substringAfter( "a" )     = "bc"
     * "abcba"  : .substringAfter( "b" )     = "cba"
     * "abc"    : .substringAfter( "c" )     = ""
     * "abc"    : .substringAfter( "d")      = ""
     * "abc"    : .substringAfter( "" )      = "abc"
     * </pre>
     * 
     * @param delimiter The String to search for, must not be null.
     * @return The substring after the first occurrence of the delimiter.
     */
    public Stringer substringAfter( CharSequence delimiter ) {
        if (delimiter == null) {
            throw new NullPointerException( "'delimiter' must not be null." );
        }
        if (!isEmpty()) {
            int pos = indexOf( delimiter );
            if (pos > -1) {
                delete( 0, pos + delimiter.length() );
            }
        }
        return this;
    }
    
        
    // formatting *****************************************
    
    public Stringer leftPad( int minLength, CharSequence padChars ) {
        if (length() < minLength) {
            for (int i=length(); i<minLength; i++) {
                insert( 0, padChars );
            }
        }
        return this;
    }
    
    
    public Stringer rightPad( int minLength, CharSequence padChars ) {
        if (length() < minLength) {
            for (int i=length(); i<minLength; i++) {
                append( padChars );
            }
        }
        return this;
    }
    
    
    /**
     * Format via {@link MessageFormat#format(Object[], StringBuffer, FieldPosition)}.
     * 
     * @see MessageFormat
     */
    public Stringer format( Object... args ) {
        // XXX would be way cooler to do this on data directly
        MessageFormat mf = new MessageFormat( data.toString(), Polymap.getSessionLocale() );
        StringBuffer formatted = mf.format( args, new StringBuffer(), new FieldPosition( 0 ) );
        replace( 0, length(), formatted.toString() );
        return this;
    }
    
    
    public Stringer toEncodedURI() {
        // http://stackoverflow.com/questions/607176/java-equivalent-to-javascripts-encodeuricomponent-that-produces-identical-outpu
        try {
            String encoded = URLEncoder.encode( toString(), "UTF-8" )
                    .replaceAll( "\\+", "%20" )
                    .replaceAll( "\\%21", "!" )
                    .replaceAll( "\\%27", "'" )
                    .replaceAll( "\\%28", "(" )
                    .replaceAll( "\\%29", ")" )
                    .replaceAll( "\\%7E", "~" );
            replace( 0, length(), encoded.toString() );
            return this;
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException( e );
        }
    }


    /**
     * Returns a valid filename. Replaces all non-letters, -digits, '_', '.' and '-'
     * with the given replacement.
     */
    public Stringer toFilename( CharSequence replacement ) {
        for (int i=0; i<length(); i++) {
            char c = charAt( i );
            if (!Character.isLetterOrDigit( c ) && c!='.' && c!='-' && c!='_') {
                replace( i, i+1, replacement );
            }
        }
        return this;
    }


    /**
     * Returns a valid URI path. Replaces all non-URI-path chars with the given
     * replacement. This differs from {@link #toEncodedURI()} in that it does not
     * encode but replace chars.
     * <p/>
     * <code>replaceAll( "[^a-zA-Z0-9\\-_/]", "" )</code>
     */
    public Stringer toURIPath( String replacement ) {
        replace( toString().replaceAll( "[^a-zA-Z0-9-_/]", replacement ) );
        return this;
    }

    
    /**
     * Replace (german) umlauts by their ASCII char sequences.
     */
    public Stringer replaceUmlauts() {
        for (int i=0; i<length(); i++) {
            char c = charAt( i );
            switch (c) {
                case 'Ü': replace( i, i+1, "Ue" ); break;
                case 'ü': replace( i, i+1, "ue" ); break;
                case 'Ä': replace( i, i+1, "Ae" ); break;
                case 'ä': replace( i, i+1, "ae" ); break;
                case 'Ö': replace( i, i+1, "Oe" ); break;
                case 'ö': replace( i, i+1, "oe" ); break;
                case 'ß': replace( i, i+1, "ss" ); break;
            }
        }
        return this;
    }

    
    // joining ********************************************
    
    public Stringer join( String separator, Iterable args ) {
        int i = 0;
        for (Object arg : args ) {
            if (i++ > 0) {
                append( separator );
            }
            append( toString( arg ) );
        }
        return this;
    }
    
    
    public Stringer join( String separator, Object... args ) {
        return join( separator, ImmutableList.copyOf( args ) );
    }
    
    
    // CharSequence / Appendable **************************
    
    public Stringer append( char c ) {
        data.append( c ); 
        return this;
    }

    public Stringer append( char[] str, int offset, int len ) {
        data.append( str, offset, len );
        return this;
    }

    public Stringer append( char[] str ) {
        data.append( str );
        return this;
    }

    public Stringer append( CharSequence s, int start, int end ) {
        data.append( s, start, end );
        return this;
    }

    public Stringer append( CharSequence s ) {
        data.append( s );
        return this;
    }

    public Stringer append( Object obj ) {
        data.append( obj );
        return this;
    }

//    public StringBuilder append( boolean b ) {
//        return data.append( b );
//    }
//
//    public StringBuilder append( double d ) {
//        return data.append( d );
//    }
//
//    public StringBuilder append( float f ) {
//        return data.append( f );
//    }
//
//    public StringBuilder append( int i ) {
//        return data.append( i );
//    }
//
//    public StringBuilder append( long lng ) {
//        return data.append( lng );
//    }

//    public StringBuilder append( String str ) {
//        return data.append( str );
//    }

    public int capacity() {
        return data.capacity();
    }

    public char charAt( int index ) {
        return data.charAt( index );
    }

    public Stringer delete( int start, int end ) {
        data.delete( start, end );
        return this;
    }

    public Stringer deleteCharAt( int index ) {
        data.deleteCharAt( index );
        return this;
    }

    public void ensureCapacity( int minimumCapacity ) {
        data.ensureCapacity( minimumCapacity );
    }

    public boolean equals( Object obj ) {
        return data.equals( obj );
    }

    public void getChars( int srcBegin, int srcEnd, char[] dst, int dstBegin ) {
        data.getChars( srcBegin, srcEnd, dst, dstBegin );
    }

    public int hashCode() {
        return data.hashCode();
    }

    public StringBuilder insert( int offset, boolean b ) {
        return data.insert( offset, b );
    }

    public StringBuilder insert( int offset, char c ) {
        return data.insert( offset, c );
    }

    public StringBuilder insert( int index, char[] str, int offset, int len ) {
        return data.insert( index, str, offset, len );
    }

    public StringBuilder insert( int offset, char[] str ) {
        return data.insert( offset, str );
    }

    public StringBuilder insert( int dstOffset, CharSequence s, int start, int end ) {
        return data.insert( dstOffset, s, start, end );
    }

    public StringBuilder insert( int dstOffset, CharSequence s ) {
        return data.insert( dstOffset, s );
    }

    public StringBuilder insert( int offset, double d ) {
        return data.insert( offset, d );
    }

    public StringBuilder insert( int offset, float f ) {
        return data.insert( offset, f );
    }

    public StringBuilder insert( int offset, int i ) {
        return data.insert( offset, i );
    }

    public StringBuilder insert( int offset, long l ) {
        return data.insert( offset, l );
    }

    public StringBuilder insert( int offset, Object obj ) {
        return data.insert( offset, obj );
    }

    public StringBuilder insert( int offset, String str ) {
        return data.insert( offset, str );
    }

    public int lastIndexOf( CharSequence str, int fromIndex ) {
        return data.lastIndexOf( charSequenceToString( str ), fromIndex );
    }

    public int lastIndexOf( CharSequence str ) {
        return data.lastIndexOf( charSequenceToString( str ) );
    }

    public int indexOf( CharSequence str, int fromIndex ) {
        return data.indexOf( charSequenceToString( str ), fromIndex );
    }

    public int indexOf( CharSequence str ) {
        return data.indexOf( charSequenceToString( str ) );
    }

    public int length() {
        return data.length();
    }


    /**
     * Replaces the characters in a substring of this sequence with characters in the
     * specified <code>String</code>. The substring begins at the specified
     * <code>start</code> and extends to the character at index <code>end - 1</code>
     * or to the end of the sequence if no such character exists. First the
     * characters in the substring are removed and then the specified
     * <code>String</code> is inserted at <code>start</code>. (This sequence will be
     * lengthened to accommodate the specified String if necessary.)
     * 
     * @param start The beginning index, inclusive.
     * @param end The ending index, exclusive.
     * @param str String that will replace previous contents.
     * @return This object.
     * @throws StringIndexOutOfBoundsException if <code>start</code> is negative,
     *         greater than <code>length()</code>, or greater than <code>end</code>.
     */
    public Stringer replace( int start, int end, CharSequence str ) {
        data.replace( start, end, charSequenceToString( str ) );
        return this;
    }

    public StringBuilder reverse() {
        return data.reverse();
    }

    public void setCharAt( int index, char ch ) {
        data.setCharAt( index, ch );
    }

    public CharSequence subSequence( int start, int end ) {
        return data.subSequence( start, end );
    }

    public String substring( int start, int end ) {
        return data.substring( start, end );
    }

    public String substring( int start ) {
        return data.substring( start );
    }

    public void trimToSize() {
        data.trimToSize();
    }
    
    public boolean startsWith( CharSequence str ) {
        assert str != null;
        if (length() < str.length()) {
            return false;
        }
        for (int i=0; i<str.length(); i++) {
            if (charAt( i ) != str.charAt( i )) {
                return false;
            }
        }
        return true;
    }
    
    public boolean endsWith( CharSequence str ) {
        assert str != null;
        if (length() < str.length()) {
            return false;
        }
        int i = length() - 1;
        int j = str.length() - 1;
        while (j >= 0) {
            if (charAt( i-- ) != str.charAt( j-- )) {
                return false;
            }
        }
        return true;
    }
    
    private String charSequenceToString( CharSequence input ) {
        return input instanceof String ? (String)input : input.toString();
    }
    
    
    /**
     * Tests.
     */
    public static void main( String[] args ) throws Exception {
        Stringer digits = Stringer.on( "0123456789" );
        check( digits.startsWith( "0" ) );
        check( digits.startsWith( "01" ) );
        check( !digits.startsWith( "001" ) );
        
        check( digits.endsWith( "9" ) );
        check( digits.endsWith( "89" ) );
        check( !digits.endsWith( "889" ) );
        System.out.println( "ok." );
    }
    
    public static void check( boolean success, String msg ) {
        if (!success) {
            throw new AssertionError( msg );
        }
    }

    public static void check( boolean success ) {
        if (!success) {
            throw new AssertionError();
        }
    }
    
}
