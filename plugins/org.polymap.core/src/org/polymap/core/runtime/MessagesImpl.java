/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.core.runtime;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides a default implementation as a backend for the static Messages class of a
 * bundle/plugin.
 * 
 * @see ResourceBundle
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MessagesImpl
        implements IMessages {

    private static Log log = LogFactory.getLog( MessagesImpl.class );

    public static final String  SEPARATOR = "_";
    
    private static final WorkspaceResourceBundleControl rbcontrol = new WorkspaceResourceBundleControl();

    
    // instance *******************************************
    
    private String              bundleName;

    private ClassLoader         cl;
    
    private String              prefix;


    /**
     * 
     * @param bundleName The name of the resource bundle. This is different from the
     *        OSGi bundle name.For example: PLUGIN_ID + ".messages".
     * @param cl The {@link ClassLoader} to be used to load the resources.
     */
    public MessagesImpl( String bundleName, ClassLoader cl ) {
        this( bundleName, cl, null );
    }

    /**
     * 
     * @param bundleName The name of the resource bundle. This is different from the
     *        OSGi bundle name.For example: PLUGIN_ID + ".messages".
     * @param cl The {@link ClassLoader} to be used to load the resources.
     * @param prefix
     */
    public MessagesImpl( String bundleName, ClassLoader cl, String prefix ) {
        this.bundleName = bundleName;
        this.cl = cl;
        this.prefix = prefix != null ? prefix + SEPARATOR : "";
    }


    /**
     * Find the localized message for the given key. If arguments are given, then the
     * result message is formatted via {@link MessageFormat}.
     *
     * @param key
     * @param args If not null, then the message is formatted via {@link MessageFormat}
     * @return The message for the given key.
     */
    @Override
    public String get( String key, Object... args ) {
        Locale locale = Polymap.getSessionLocale();
        return get( locale, key, args );
    }


    /**
     * Find the localized message for the given key. If arguments are given, then the
     * result message is formatted via {@link MessageFormat}.
     *
     * @param locale The locale to use to localize the given message.
     * @param key
     * @param args If not null, then the message is formatted via {@link MessageFormat}
     * @return The message for the given key.
     */
    @Override
    public String get( Locale locale, String key, Object... args ) {
        assert key != null && key.length() > 0 : "The given messages key is empty.";
        try {
            // getBundle() caches the bundles
            ResourceBundle bundle = ResourceBundle.getBundle( bundleName, locale, cl, rbcontrol );
            if (args == null || args.length == 0) {
                return bundle.getString( prefix + key );
            }
            else {
                String msg = bundle.getString( prefix + key );
                return MessageFormat.format( msg, args );
            }
        }
        catch (Exception e) {
            return key;
        }
    }


    public String get( Object caller, String keySuffix, Object... args ) {
        String key = new StringBuilder( 64 )
                .append( caller.getClass().getSimpleName() )
                .append( SEPARATOR )
                .append( keySuffix ).toString();
        return get( key, args );
    }

    
    @Override
    public IMessages forClass( final Class type ) {
        return forPrefix( type.getSimpleName() );
    }

    
    public IMessages forPrefix( String _prefix ) {
        assert _prefix != null && _prefix.length() > 0;
        return new MessagesImpl( bundleName, cl, _prefix );
    }

    
    /**
     * Controls the bundle loading. Enable properties files loaded from workspace.
     * Default behaviour (properties from classloader) is fallback. If the workspace
     * file does not contain a particular key then the properties file from the
     * classloader (default) is checked for that key.
     * <p/>
     * Also enables time-to-live and bundle reload for default bundles from
     * classloader and the bundles loaded from the workspace files.
     */
    static class WorkspaceResourceBundleControl
            extends ResourceBundle.Control  {

        //static ResourceBundle.Control      defaultControl = new ResourceBundle.Control() {};
        
        protected File workspaceFile( String baseName, Locale locale ) {
            String bundleName = toBundleName( baseName, locale );
            String resName = bundleName + ".properties";
            return new File( Polymap.getWorkspacePath().toFile(), resName );
        }
        
        
        @Override
        public List<String> getFormats( String baseName ) {
            assert baseName != null;
            List<String> result = new ArrayList();
            result.add( "workspace.properties" );
            result.addAll( super.getFormats( baseName ) );
            return result;
        }
        
        
        @Override
        public ResourceBundle newBundle( String baseName, Locale locale, String format, ClassLoader loader, boolean reload ) 
                throws IllegalAccessException, InstantiationException, IOException {
            assert baseName != null && locale != null && format != null && loader != null;

            final ResourceBundle defaultResult = super.newBundle( baseName, locale, FORMAT_PROPERTIES.get(0), loader, reload );
            
            if (format.equals( "workspace.properties" )) {
                File f = workspaceFile( baseName, locale );
                if (f.exists()) {
                    InputStream in = null;
                    try {
                        in = new BufferedInputStream( new FileInputStream( f ) );
                        return new PropertyResourceBundle( in ) {
                            
                            private PropertyResourceBundle  delegate = defaultResult instanceof PropertyResourceBundle ? (PropertyResourceBundle)defaultResult : null;

                            @Override
                            public Object handleGetObject( String key ) {
                                Object result = super.handleGetObject( key );
                                return result == null && delegate != null ? delegate.handleGetObject( key ) : result;
                            }

                            @Override
                            public Enumeration<String> getKeys() {
                                // XXX Auto-generated method stub
                                throw new RuntimeException( "not yet implemented." );
                            }

                            @Override
                            protected Set<String> handleKeySet() {
                                // XXX Auto-generated method stub
                                throw new RuntimeException( "not yet implemented." );
                            }
                        };
                    }
                    finally {
                        IOUtils.closeQuietly( in );
                    }
                }
            }
            return defaultResult;
        }

        
        @Override
        public long getTimeToLive( String baseName, Locale locale ) {
            return 10 * 1000;
        }

        
        @Override
        public boolean needsReload( String baseName, Locale locale, String format, ClassLoader loader,
                ResourceBundle bundle, long loadTime ) {
            if (format.equals( "workspace.properties" )) {
                return workspaceFile( baseName, locale ).lastModified() > loadTime;
            }
            return super.needsReload( baseName, locale, format, loader, bundle, loadTime );
        }
        
    }
    
}
