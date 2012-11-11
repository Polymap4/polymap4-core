/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.mapeditor.tooling;

import com.google.common.base.Predicate;

import org.eclipse.core.runtime.IPath;

/**
 * Static methods that provides {@link Predicate} that filter EditorTools
 * via the {@link IEditorToolSite#filterTools(Predicate)} method.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EditorTools {

    /**
     * The returned {@link Predicate} checks if the given {@link IPath}
     * is equal to the toolPath of the checked {@link IEditorTool}.
     */
    public static Predicate<IEditorTool> isEqual( final IPath toolPath ) {
        assert toolPath != null;
        return new Predicate<IEditorTool>() {
            public boolean apply( IEditorTool input ) {
                assert input.getToolPath() != null;
                return toolPath.equals( input.getToolPath() );
            }
        };
    }

    /**
     * The returned {@link Predicate} checks if the given {@link IPath}
     * is prefix of the toolPath of the checked {@link IEditorTool}.
     */
    public static Predicate<IEditorTool> hasPrefix( final IPath prefix ) {
        assert prefix != null;
        return new Predicate<IEditorTool>() {
            public boolean apply( IEditorTool input ) {
                return prefix.isPrefixOf( input.getToolPath() );
            }
        };
    }

    
    /**
     * The returned {@link Predicate} checks if the given {@link IPath}
     * is a direct parent of an of the checked {@link IEditorTool}.
     */
    public static Predicate<IEditorTool> hasStrictPrefix( final IPath prefix ) {
        assert prefix != null;
        return new Predicate<IEditorTool>() {
            public boolean apply( IEditorTool input ) {
                return prefix.isPrefixOf( input.getToolPath() )
                        && prefix.segmentCount() + 1 == input.getToolPath().segmentCount();
            }
        };
    }
    
}
