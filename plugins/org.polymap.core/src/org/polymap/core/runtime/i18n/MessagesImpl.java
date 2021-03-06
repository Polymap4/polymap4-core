/*
 * polymap.org
 * Copyright 2011, Falko Br�utigam, and other contributors as
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
package org.polymap.core.runtime.i18n;

import static java.util.Collections.enumeration;
import static java.util.Collections.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
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

import com.google.common.base.Joiner;

import org.polymap.core.runtime.Polymap;

/**
 * Provides a default implementation as a backend for the static Messages class of a
 * bundle/plugin.
 * <p/>
 * <b>Example declaration:</b>
 * <pre>
 * public class Messages {
 *   private static final String       BUNDLE_NAME = Plugin.ID + ".messages";
 *   private static final MessagesImpl instance = new MessagesImpl( BUNDLE_NAME, Messages.class.getClassLoader() );
 *   
 *   public static IMessages forPrefix( String prefix ) {
 *       return instance.forPrefix( prefix );
 *   }
 *
 *   private Messages() { // prevent instantiation }
 *
 *   public static String get( String key, Object... args ) {
 *      return instance.get( key, args );
 *   }
 *   // more specific methods
 * }
 * </pre>
 * <p/>
 * <b>Example usage:</b>
 * <pre>
 *     // class specific prefix
 *     private static final IMessages i18n = Messages.forPrefix( "ImportPanel" );
 *     ...
 *     i18n.get( "title" )  // gets/inserts "ImportPanel_title" from the messages file
 * </pre>
 * 
 * @see ResourceBundle
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
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


    @Override
    public boolean contains( Locale locale, String key ) {
        assert key != null && key.length() > 0 : "The given messages key is empty.";
        ResourceBundle bundle = resourceBundle( locale );
        // bundle.containsKey() depends on handleKeySet() which we cannot implement
        // so we have to get and search all keys
        return Collections.list( bundle.getKeys() ).contains( prefix + key );
    }

    
    @Override
    public boolean contains( String key ) {
        return contains( Polymap.getSessionLocale(), key );
    }

    
    @Override
    public String get( Locale locale, String key, Object... args ) {
        assert key != null && key.length() > 0 : "The given messages key is empty.";
        try {
            ResourceBundle bundle = resourceBundle( locale );
            String result = bundle.getString( prefix + key );
            if (args != null && args.length > 0) {
                result = new MessageFormat( result, locale ).format( args );
            }
            return result;
        }
        catch (MissingResourceException e) {
            log.warn( "Can't find key '" + prefix + key + "' in bundle " + bundleName );
            // ResourceBundleEditor removes empty entries :(
            return NO_SUCH_KEY;
        }
        catch (Exception e) {
            log.warn( "", e );
            return key;
        }
    }

    @Override
    public String get( String key, Object... args ) {
        Locale locale = Polymap.getSessionLocale();
        return get( locale != null ? locale : Locale.getDefault(), key, args );
    }


    public String get( Object caller, String keySuffix, Object... args ) {
        String key = Joiner.on( SEPARATOR ).join( caller.getClass().getSimpleName(), keySuffix );
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

    
    public ResourceBundle resourceBundle( Locale locale ) {
        // getBundle() caches the bundles
        return ResourceBundle.getBundle( bundleName, locale, cl, rbcontrol );        
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
                                Set<String> result = new HashSet( list( super.getKeys() ) );
                                if (delegate != null) {
                                    result.addAll( list( delegate.getKeys() ) );
                                }
                                return enumeration( result );
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
