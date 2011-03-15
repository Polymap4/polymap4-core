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
package org.polymap.core.style;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Represents a bean style metadata ancestor for metadata about a {@link IStyle}.
 * <p>
 * The methods within this class must be non-blocking. This class, and
 * sub-classes represent cached versions of the metadata about a particular
 * style.
 * <p>
 * Any changes to this content will be communicate by an event by the associated
 * {@link IStyle}.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class IStyleInfo {

    protected String             title, description, name;

    protected String[]           keywords;

    protected ImageDescriptor    icon;

    private Icon                 awtIcon;


    protected IStyleInfo() {
        // for over-riding
    }


    public IStyleInfo( String title, String name, String description,
            String[] keywords, ImageDescriptor icon ) {
        this.title = title;
        this.description = description;
        this.name = name;
        int i = 0;
        if (keywords != null) {
            i = keywords.length;
        }
        String[] k = new String[i];
        if (keywords != null) {
            System.arraycopy( keywords, 0, k, 0, k.length );
        }
        this.keywords = k;
        this.icon = icon;
    }

    /**
     * Returns the style's title
     * 
     * @return Readable title (in current local)
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the keywords assocaited with this style.
     * 
     * @return Keywords for use with search, or <code>null</code> unavailable.
     */
    public Set<String> getKeywords() { // aka Subject
        if( keywords == null ){
            return Collections.emptySet();
        }
        List<String> asList = Arrays.asList(keywords);
        Set<String> set = new HashSet<String>(asList);
        return set;
    }

    /**
     * Returns the resource's description.
     * 
     * @return description of resource, or <code>null</code> if unavailable
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the name of the data ... such as the typeName or LayerName.
     * 
     * @return name of the data, used with getSchema() to identify resource
     */
    public String getName() {
        return name;
    }


    /**
     * Base symbology (with out decorators) representing this resource.
     * <p>
     * The ImageDescriptor returned should conform the the Eclipse User
     * Interface Guidelines (16x16 image with a 16x15 glyph centered).
     * <p>
     * This plug-in provides default based on resource type:
     * <pre>
     * <code>
     *  &lt;b&gt;return&lt;/b&gt; ISharedImages.getImagesDescriptor( IGeoResoruce );
     * </code>
     * </pre>
     * 
     * <ul>
     * <p>
     * Any LabelProvider should use the default image, a label decorator should
     * be used to pick up these images in a separate thread. This allows
     * resources like WMS to make blocking request of an external service.
     * </p>
     * 
     * @return ImageDescriptor symbolizing this resource
     */
    public Icon getIcon() {
        throw new RuntimeException( "not yet implemented." );
//        if( awtIcon!=null ){
//            return awtIcon;
//        }
//        if( icon==null ){
//            return null;
//        }
//        
//        Icon awtIcon = AWTSWTImageUtils.imageDescriptor2awtIcon(icon);
//        return awtIcon;        
    }

    /**
     * Default implementation calls getIcon and converts the icon to an ImageDescriptor.
     *
     * @return the icon as an image descriptor
     */
    public ImageDescriptor getImageDescriptor() {
        throw new RuntimeException( "not yet implemented." );
//        if( icon!=null ){
//            return icon;
//        }
//        
//        Icon icon2 = getIcon();
//        if( icon2 == null ){
//            return null;
//        }
//        
//        return AWTSWTImageUtils.awtIcon2ImageDescriptor(icon2);
    }

}
