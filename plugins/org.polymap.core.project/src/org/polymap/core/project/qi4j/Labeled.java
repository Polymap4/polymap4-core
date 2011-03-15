/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */

package org.polymap.core.project.qi4j;

import java.util.Set;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.property.Property;

import org.polymap.core.model.ModelProperty;

/**
 * Provides interface and mixin to give entities a label. 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public interface Labeled
        extends org.polymap.core.project.Labeled {

    @ModelProperty(PROP_LABEL)
    public void setLabel( String string );

    @ModelProperty(PROP_KEYWORDS)
    public void setKeywords( Set<String> keywords );

    //@NotEmpty
    @UseDefaults
    //@MaxLength(30)
    @Queryable
    abstract Property<String>       label();

    @UseDefaults
    @Queryable
    abstract Property<Set<String>>  keywords();

    /**
     * The mixin.
     */
    public abstract static class Mixin
            implements Labeled {

        public String getLabel() {
            return label().get();
        }

        @ModelProperty(PROP_LABEL)
        public void setLabel( String value ) {
            label().set( value );
        }

        public Set<String> getKeywords() {
            return keywords().get();
        }

        @ModelProperty(PROP_KEYWORDS)
        public void setKeywords( Set<String> keywords ) {
            keywords().set( keywords );
        }

//        @ModelProperty(PROP_KEYWORDS)
//        public void setKeywords( String csv ) {
//            String[] array = StringUtils.split( csv, ", " );
//            keywords().set( Sets.newHashSet( array ) );
//        }
    }
    
}
