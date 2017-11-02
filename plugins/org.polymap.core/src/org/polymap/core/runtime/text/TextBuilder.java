/* 
 * polymap.org
 * Copyright (C) 2017, the @authors. All rights reserved.
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

import java.util.Locale;

import java.io.Writer;
import java.text.MessageFormat;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rap.rwt.RWT;

/**
 * Builds text formats which are hierarchically marked up.
 *
 * @author Falko Bräutigam
 */
public class TextBuilder {

    private static final Log log = LogFactory.getLog( TextBuilder.class );
    
    public static TextBuilder forMarkdown() {
        return new TextBuilder( new MarkdownGenerator(), RWT.getLocale() );
    }

    /**
     * 
     */
    public enum Element {
        H1, H2, H3, H4,
        P,
        UL, OL, LI,
        EM,
        BOLD,
        /** No particular markup, just add the text. */
        NOOP
    }
    
    @FunctionalInterface
    public interface SubtreeBuilder<T extends TextBuilder> {
        public void build( T builder );
    }


    // instance *******************************************
    
    private Locale              locale;
    
    private Generator           generator;
    
    private Writer              out;

    private boolean             skipBlank;
    
    private String              useForNull = "[null]";

    
    public TextBuilder( Generator generator, Locale locale ) {
        this( new StringBuilderWriter( 4096 ), generator, locale );
    }

    
    public TextBuilder( Writer out, Generator generator, Locale locale ) {
        this.locale = locale;
        this.generator = generator;
        this.generator.init( this.out = out );
    }

    
    /**
     * Specifies that the entire Element should be skipped if the contents is null or
     * empty.
     */
    public <R extends TextBuilder> R skipBlank( @SuppressWarnings( "hiding" ) boolean skipBlank ) {
        this.skipBlank = skipBlank;
        return (R)this;
    }
    
    
    /**
     * Specifies that null content strings and format arguments are replaced by the
     * given string. Defaults to "[null]".
     */
    public <R extends TextBuilder> R useForNull( @SuppressWarnings( "hiding" ) String useForNull ) {
        this.useForNull = useForNull;
        return (R)this;
    }
    
    
    /**
     * Builds the given {@link Element} with no direct content.
     * <p/>
     * The given CharSequence is formatted via {@link MessageFormat}. Use braces to
     * refer to the args.
     *
     * @param elm The Elemt to generate.
     * @param s
     * @param args Arguments to be used for {@link MessageFormat}.
     */
    public <R extends TextBuilder> R build( Element elm ) {
        generator.begin( elm, "" );
        generator.end( elm );
        return (R)this;
    }
    
    
    /**
     * Builds the given {@link Element} with the given content.
     * <p/>
     * The given CharSequence is formatted via {@link MessageFormat}. Use braces to
     * refer to the args.
     *
     * @param elm The Elemt to generate.
     * @param s
     * @param args Arguments to be used for {@link MessageFormat}.
     */
    public <R extends TextBuilder> R build( Element elm, CharSequence s, Object... args ) {
        if (!shouldBeSkipped( s )) {
            generator.begin( elm, format( s, args ) );
            generator.end( elm );
        }
        return (R)this;
    }

    
    public <R extends TextBuilder> R build( Element elm, SubtreeBuilder builder ) {
        return build( elm, null, builder );
    }

    
    public <R extends TextBuilder> R build( Element elm, CharSequence s, SubtreeBuilder builder ) {
        generator.begin( elm, s );
        builder.build( this );
        generator.end( elm );
        return (R)this;        
    }

    
    /**
     * Returns the result of {@link Writer#toString()} of the Writer of this
     * TextBuilder. For the default used {@link StringBuilderWriter} this
     * returns the generated String.
     */
    @Override
    public String toString() {
        log.info( out.toString() );
        return out.toString();
    }


    protected boolean shouldBeSkipped( CharSequence s ) {
        return skipBlank && StringUtils.isBlank( s );
    }


    protected CharSequence format( CharSequence cs, Object... args ) {
        if (cs == null) {
            return useForNull;
        }
        String s = cs.toString();
        if (s.indexOf( "{" ) > -1) {
            return new MessageFormat( s, locale )
                    .format( substituteNull( args ), new StringBuffer(), null )
                    .toString();
        }
        else {
            return s;
        }
    }

    
    protected Object[] substituteNull( Object[] args ) {
        for (int i=0; i<args.length; i++) {
            args[i] = args[i] != null ? args[i] : useForNull;
        }
        return args;
    }
    
    
//    protected Object[] flatten( Object... args ) {
//        return flatten( Arrays.asList( args ) ).toArray();
//    }
//    
//    
//    /**
//     * Convert Collections and Arrays into their elements.
//     */
//    protected <T> List<T> flatten( Iterable<T> args ) {
//        List flatten = new LinkedList();
//        for (Object arg : args) {
//            if (arg == null) {
//            }
//            else if (arg instanceof Iterable) {
//                flatten.addAll( Lists.newArrayList( (Iterable)arg ) );
//            }
//            else if (arg.getClass().isArray()) {
//                
//            }
//            else {
//                flatten.add( arg );
//            }
//        }
//        return flatten;
//    }

}
