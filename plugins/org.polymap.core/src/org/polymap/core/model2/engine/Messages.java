/* 
 * polymap.org
 * Copyright (C) 2009-2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.model2.engine;

import org.polymap.core.CorePlugin;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.MessagesImpl;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a> 
 */
public class Messages {

    private static final String BUNDLE_NAME = CorePlugin.PLUGIN_ID + ".model2.engine.messages"; //$NON-NLS-1$

    private static final MessagesImpl instance = new MessagesImpl( BUNDLE_NAME, Messages.class.getClassLoader() );

    
    private Messages() {
    }

    public static String get( String key, Object... args ) {
        return instance.get( key, args );
    }
    
    public static IMessages forClass( Class type ) {
        return instance.forClass( type );
    }

    public static IMessages forPrefix( String _prefix ) {
        return instance.forPrefix( _prefix );
    }

}
