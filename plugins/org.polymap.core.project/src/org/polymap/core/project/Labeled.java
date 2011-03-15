/*
 * polymap.org Copyright 2009, Polymap GmbH, and individual contributors as
 * indicated by the @authors tag.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 * 
 * $Id$
 */

package org.polymap.core.project;

import java.util.Set;

import org.polymap.core.model.ModelProperty;

/**
 * This general entity feature allows to give the entity a label and
 * a set of keywords for searching.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public interface Labeled {

    public static final String      PROP_LABEL = "label";

    public static final String      PROP_KEYWORDS = "keywords";

    
    /**
     * The label property
     */
    public String getLabel();

    /**
     * Updates the label of the entity.
     * <p>
     * ModelProperty: {@link #PROP_LABEL}
     */
    @ModelProperty(PROP_LABEL)
    public void setLabel( String string );


    /**
     * The keywords property
     */
    public Set<String> getKeywords();

    /**
     * Updates the keywords of the entity.
     * <p>
     * ModelProperty: {@link #PROP_KEYWORDS}
     */
    @ModelProperty(PROP_KEYWORDS)
    public void setKeywords( Set<String> keywords );

//    @ModelProperty(PROP_KEYWORDS)
//    public void setKeywords( String csv );
    
}