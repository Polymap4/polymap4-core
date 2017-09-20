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

import java.io.Writer;

import org.polymap.core.runtime.text.TextBuilder.Element;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class MarkdownGenerator
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
        switch (elm) {
            case P: {
                
            }
            case UL: {
                // nothing to do
            }
            default: {
                throw new RuntimeException( "Unhandled element: " + elm );                
            }
        }
    }

    
    @Override
    public void end( Element elm ) {
        assert stack.pop() == elm;
    }
    

//    /**
//     * Ensures that at least the given number of newlines are there.
//     */
//    public MarkdownBuilder newline( int num ) {
//        if (StringUtils.isBlank( last )) {
//            return this;
//        }
//        int c = 0;
//        for (int i=last.length()-1; i>=0; i--) {
//            if (last.charAt( i ) != '\n') {
//                break;
//            }
//            c++;
//        }
//        String nls = StringUtils.rightPad( "", Math.max( num-c, 0 ), '\n' );
//        write( nls );
//        return this;
//    }

}
