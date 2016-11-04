/*
 * polymap.org Copyright (C) 2009-2013, Polymap GmbH. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.core.runtime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import java.io.File;
import java.security.Principal;

import javax.security.auth.Subject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.security.auth.ILoginContext;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.lifecycle.LifeCycleUtil;
import org.eclipse.rap.rwt.internal.service.ContextProvider;

import org.polymap.core.CorePlugin;
import org.polymap.core.runtime.session.SessionSingleton;
import org.polymap.core.security.UserPrincipal;
import org.polymap.core.ui.UIUtils;

/**
 * Static access to the runtime infrastructure.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.0
 */
@SuppressWarnings("restriction")
public final class Polymap {

    private static Log log = LogFactory.getLog( Polymap.class );

//    public static final String      DEFAULT_LOGIN_CONFIG = "Workbench";
//    /** The name of the service JAAS login config. See {@link ServicesCallbackHandler}. */
//    public static final String      SERVICES_LOGIN_CONFIG = "Services";
    
    static {
        // set the default locale to 'en' as all of our message_xx.properties
        // files have 'en' as default
        Locale.setDefault( Locale.ENGLISH );
    }
    
    // static factory *************************************
    
    /**
     * Gets or creates the Polymap instance for the application session of the
     * current thread.
     */
    public static Polymap instance() {
        return SessionSingleton.instance( Polymap.class );
    }
    

    /**
     * @deprecated See {@link CorePlugin#getDataLocation(org.osgi.framework.Bundle)}
     */
    public static IPath getWorkspacePath() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        IPath path = root.getLocation();
        return path;
    }


    /**
     * @deprecated See {@link CorePlugin#getDataLocation(org.osgi.framework.Bundle)}
     */
    public static File getCacheDir() {
        File cacheDir = new File( getWorkspacePath().toFile(), "cache" );
        cacheDir.mkdirs();
        return cacheDir;
    }

    /**
     * @deprecated See {@link CorePlugin#getDataLocation(org.osgi.framework.Bundle)}
     */
    public static File getConfigDir() {
        File configDir = new File( getWorkspacePath().toFile(), "config" );
        configDir.mkdirs();
        return configDir;
    }

    /**
     * @deprecated See {@link CorePlugin#getDataLocation(org.osgi.framework.Bundle)}
     */
    public static File getDataDir() {
        File dataDir = new File( getWorkspacePath().toFile(), "data" );
        dataDir.mkdirs();
        return dataDir;
    }


    /**
     * The {@link Display} of the session of the current thread. Null, if the current
     * thread has no session. The result is equivalent to
     * {@link Display#getCurrent()} except that the calling thread does not have to
     * be the UI thread of the session.
     * 
     * @deprecated Use {@link UIUtils} instead.
     */
    public static Display getSessionDisplay() {
        return LifeCycleUtil.getSessionDisplay();
    }
    
    
    public static Locale getSessionLocale() {
        try {
            return instance().getLocale();
        }
        catch (Exception e) {
            return null;
        }
    }

    
// public static ExecutorService      executorService = new PolymapJobExecutor();
// public static ExecutorService      executorService = PolymapThreadPoolExecutor.newInstance();
    private static ExecutorService      executorService = UnboundPoolExecutor.newInstance();
    

    /**
     * Returns the {@link ExecutorService} for the calling session. This should be
     * used whenever working threads are needed for multi processing. The default
     * Eclipse {@link Job} should be used for normal async business logic operations.
     */
    public static ExecutorService executorService() {
        return executorService;
    }


    // instance *******************************************

    /** The session attributes. */
    private Map             attributes = new HashMap();
    
    private ILoginContext   secureContext;
    
    private Subject         subject;
    
    private Set<Principal>  principals = new HashSet();
    
    private UserPrincipal   user;

    private Map             initHttpParams;
    
    /** 
     * The locale of the HTTP Accept-Langueage header or the server default locale. 
     * <p/>
     * Cache the locale for calls from outside the request lifecycle.
     */
    private LazyInit<Locale> locale = new PlainLazyInit( new Supplier<Locale>() {
        public Locale get() {
            try {
                // outside request lifecycle -> exception
                return ContextProvider.getRequest().getLocale();
            }
            catch (Exception e) {
                return null;  // try again next time
            }
        }
    });

    
    /**
     * Use {@link #instance()} to get the instance of the current thread.
     */
    private Polymap() {
    }


    public Locale getLocale() {
        //return Locale.GERMAN;
        return RWT.getLocale();
    }


    /**
     * 
     *
     * @param key
     * @param defaultValue
     * @return The value, or the defaultValue if no such init param was given.
     */
    public String getInitRequestParam( String key, String defaultValue ) {
        assert initHttpParams != null;
        String[] value = (String[])initHttpParams.get( key );
        return value != null ? value[0] : defaultValue;
    }

    
//    /**
//     * Returns the preference service for the current session and user.
//     */
//    public IPreferencesService getPreferenceService() {
//        //return Platform.getPreferencesService().
//        CorePlugin.getDefault().
//    }

}
