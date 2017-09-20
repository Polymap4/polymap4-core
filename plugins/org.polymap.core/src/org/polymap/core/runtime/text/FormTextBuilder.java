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

import org.apache.commons.lang3.StringUtils;

import org.polymap.core.runtime.Polymap;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class FormTextBuilder
        extends TextBuilder {

    public static FormTextBuilder forMarkdown() {
        return new FormTextBuilder( new MarkdownGenerator(), Polymap.getSessionLocale() );
    }

    public static FormTextBuilder forHtml() {
        return new FormTextBuilder( new HtmlGenerator(), Polymap.getSessionLocale() );
    }

    
    public FormTextBuilder( Generator generator, Locale locale ) {
        super( generator, locale );
    }

    
    /**
     * 
     *
     * @param title The title/heading of the form, or null if no title should be
     *        rendered.
     * @param b
     */
    public FormTextBuilder form( String title, SubtreeBuilder<FormTextBuilder> b ) {
        if (!StringUtils.isBlank( title )) {
            build( Element.H2, title );
        }
        //build( Element.UL, b );  // left padding
        b.build( this );
        return this;
    }

    
    public FormTextBuilder formField( String label, Object value ) {
        build( Element.NOOP, "<span style=\"width:100px;display:inline-block;\"><b>{0}</b></span> {1}<br/>", label, value );
        return this;
    }
}
