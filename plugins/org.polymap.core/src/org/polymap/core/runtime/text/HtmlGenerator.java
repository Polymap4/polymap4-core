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

import java.util.ArrayDeque;
import java.util.Deque;

import java.io.IOException;
import java.io.Writer;

import org.polymap.core.runtime.text.TextBuilder.Element;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class HtmlGenerator
        extends Generator {

    private Deque<Element>      stack = new ArrayDeque();
    
    private Writer              out;

    
    @Override
    public void init( @SuppressWarnings( "hiding" ) Writer out ) {
        this.out = out;
    }

    
    @Override
    public void begin( Element elm, CharSequence s ) {
        stack.push( elm );
        try {
            switch (elm) {
                case H1: {
                    out.append( "<h1>" ).append( s ); break;
                }
                case H2: {
                    out.append( "<h2>" ).append( s ); break;
                }
                case H3: {
                    out.append( "<h3>" ).append( s ); break;
                }
                case H4: {
                    out.append( "<h4>" ).append( s ); break;
                }
                case P: {
                    out.append( "<p>" ).append( s ); break;
                }
                case UL: {
                    assert s == null;
                    out.append( "<ul>" ); break;
                }
                case LI: {
                    out.append( "<li>" ).append( s ); break;
                }
                case NOOP: {
                    out.append( s ); break;
                }
                default: {
                    throw new RuntimeException( "Unhandled element: " + elm );                
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }

    
    @Override
    public void end( Element elm ) {
        assert stack.pop() == elm;
        try {
            switch (elm) {
                case H1: {
                    out.append( "</h1>" ); break;
                }
                case H2: {
                    out.append( "</h2>" ); break;
                }
                case H3: {
                    out.append( "</h3>" ); break;
                }
                case H4: {
                    out.append( "</h4>" ); break;
                }
                case P: {
                    out.append( "</p>" ); break;
                }
                case UL: {
                    out.append( "</ul>" ); break;
                }
                case LI: {
                    out.append( "</li>" ); break;
                }
                case NOOP: {
                    break;
                }
                default: {
                    throw new RuntimeException( "Unhandled element: " + elm );                
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }
    
}
