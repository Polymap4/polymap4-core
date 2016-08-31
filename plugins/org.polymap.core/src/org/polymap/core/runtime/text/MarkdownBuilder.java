/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.runtime.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultBoolean;

/**
 * Simple helper for building markdown and send it to a {@link Writer}.
 * <p/>
 * There are two <b>modes</b> of processing:
 * <ul>
 * <li>write a entiry element from formated parts</li>
 * <li>write hierarchy of elements via SubtreeBuilder</li>
 * </ul>
 * Both modes can be mixed.
 * <p/>
 * Add more methods as needed.
 *
 * @author Falko Bräutigam
 */
public class MarkdownBuilder
        extends Configurable {

    private static final Log log = LogFactory.getLog( MarkdownBuilder.class );
    
    @FunctionalInterface
    public interface SubtreeBuilder {
        public void build();
    }


    @FunctionalInterface
    public interface StringFilter
            extends Function<CharSequence,CharSequence> {
    }


    // instance *******************************************
    
    public Config2<MarkdownBuilder,Locale>  locale;
    
    @DefaultBoolean( true )
    public Config2<MarkdownBuilder,Boolean> skipBlank;
    
    private Writer                          writer;
    
    private String                          last = "";
    
    private List<StringFilter>              filters = new ArrayList();

    
    public MarkdownBuilder( Writer writer ) {
        this.writer = writer;
    }

    public Writer writer() {
        return writer;
    }

    public MarkdownBuilder h3( CharSequence s, Object... args ) {
        if (notBlank( s )) {
            newline( 2 ).write( "### ", format( s, args ) );
        }
        return this;
    }
    
    public MarkdownBuilder h4( CharSequence s, Object... args ) {
        if (notBlank( s )) {
            newline( 2 ).write( "#### ", format( s, args ) );
        }
        return this;
    }
    
    public MarkdownBuilder add( CharSequence s, Object... args ) {
       write( format( s, args ) );
       return this;
    }
    
    public MarkdownBuilder paragraph( CharSequence s, Object... args ) {
        if (notBlank( s )) {
            newline( 2 ).write( format( s, args ) );
        }
        return this;
    }
    
    public MarkdownBuilder paragraph( SubtreeBuilder builder ) {
        newline( 2 );
        builder.build();
        return this;
    }
    
    public MarkdownBuilder bold( SubtreeBuilder builder ) {
        add( "**" );
        builder.build();
        add( "**" );
        return this;
    }
    
    public MarkdownBuilder em( SubtreeBuilder builder ) {
        add( "*" );
        builder.build();
        add( "*" );
        return this;
    }
    
    public MarkdownBuilder join( String delim, Iterable<CharSequence> parts ) {
        StringBuilder buf = new StringBuilder( 256 );
        for (CharSequence part : flatten( parts )) {
            if (notBlank( part )) {
                buf.append( buf.length() > 0 ? delim : "" ).append( part );
            }
        }
        add( buf );
        return this;
    }
    
    public MarkdownBuilder join( String delim, Object... args ) {
        join( delim, (Iterable)Arrays.asList( args ) );
        return this;
    }
    
    /**
     * Ensures that at least the given number of newlines are there.
     */
    public MarkdownBuilder newline( int num ) {
        if (StringUtils.isBlank( last )) {
            return this;
        }
        int c = 0;
        for (int i=last.length()-1; i>=0; i--) {
            if (last.charAt( i ) != '\n') {
                break;
            }
            c++;
        }
        String nls = StringUtils.rightPad( "", Math.max( num-c, 0 ), '\n' );
        write( nls );
        return this;
    }

    protected CharSequence format( CharSequence cs, Object... args ) {
        String s = cs.toString();
        return s.indexOf( "{" ) > -1
                ? new MessageFormat( s, locale.get() )
                        .format( flatten( args ), new StringBuffer(), null )
                        .toString()
                : s;
    }

    
    protected Object[] flatten( Object... args ) {
        return flatten( Arrays.asList( args ) ).toArray();
    }
    
    /**
     * Convert Collections and arrays into their elements.
     */
    protected <T> List<T> flatten( Iterable<T> args ) {
        List flatten = new LinkedList();
        for (Object arg : args) {
            if (arg == null) {
            }
            else if (arg instanceof Iterable) {
                flatten.addAll( Lists.newArrayList( (Iterable)arg ) );
            }
            else if (arg.getClass().isArray()) {
                
            }
            else {
                flatten.add( arg );
            }
        }
        return flatten;
    }

    protected boolean notBlank( CharSequence s ) {
        return !StringUtils.isBlank( s );
    }

    protected void write( CharSequence... array ) {
        try {
            for (CharSequence s : array) {
                writer.write( s.toString() );
                last = last + s.toString();
            }
            last = StringUtils.right( last, 10 );
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }
    
}
