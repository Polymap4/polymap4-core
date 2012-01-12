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
package org.polymap.core.runtime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;

import org.eclipse.rwt.RWT;
import org.eclipse.rwt.internal.lifecycle.LifeCycleUtil;
import org.eclipse.jface.dialogs.ErrorDialog;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.security.auth.ILoginContext;
import org.eclipse.equinox.security.auth.LoginContextFactory;

import org.polymap.core.CorePlugin;
import org.polymap.core.security.ServicesCallbackHandler;
import org.polymap.core.security.UserPrincipal;

/**
 * Static access to the runtime infrastructure.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
@SuppressWarnings("restriction")
public final class Polymap {

    private static Log log = LogFactory.getLog( Polymap.class );

    public static final String      DEFAULT_LOGIN_CONFIG = "POLYMAP";
    public static final String      SERVICES_LOGIN_CONFIG = "Services";
    

    // static factory *************************************
    
    /**
     * Gets or creates the Polymap instance for the application session of the
     * current thread.
     */
    public static Polymap instance() {
        return SessionSingleton.instance( Polymap.class );
    }
    

    /**
     *
     */
    public static IPath getWorkspacePath() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        IPath path = root.getLocation();
        return path;
    }


    /**
     *
     */
    public static File getCacheDir() {
        File cacheDir = new File( getWorkspacePath().toFile(), "cache" );
        cacheDir.mkdirs();
        return cacheDir;
    }


    /**
     * The {@link Display} of the session of the current thread. Null, if the
     * current thread has no session. The result is equivalent to
     * {@link Display#getCurrent()} except that the calling thread does need to
     * be the UI thread of the session.
     */
    public static Display getSessionDisplay() {
        return LifeCycleUtil.getSessionDisplay();
    }
    
    
    public static Locale getSessionLocale() {
        return RWT.getLocale();
    }

    
    private static ExecutorService      executorService;

    /**
     * Returns the {@link ExecutorService} for the calling session. This should be
     * used whenever working threads are needed for multi processing. The default
     * Eclipse {@link Job} should be used for normal async business logic operations.
     */
    public static ExecutorService executorService() {
        return executorService;
    }

    
    static {
        ThreadFactory threadFactory = new ThreadFactory() {

            final AtomicInteger threadNumber = new AtomicInteger( 1 );

            public Thread newThread( Runnable r ) {
                String prefix = "polymap-pool-";
                Thread t = new Thread( r, prefix + threadNumber.getAndIncrement() );
                t.setDaemon( false );
                t.setPriority( Thread.NORM_PRIORITY );
                return t;
            }
        };

        BlockingQueue queue = new SynchronousQueue();           

        int procNum = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor pool = new ThreadPoolExecutor( procNum, 
                100, 3, TimeUnit.MINUTES, queue );
        pool.setThreadFactory( threadFactory );
        executorService = pool;
    }
    
    
    // instance *******************************************

    /** The session attributes. */
    private Map             attributes = new HashMap();
    
    private ILoginContext   secureContext;
    
    private Subject         subject;
    
    private Set<Principal>  principals = new HashSet();
    
    private UserPrincipal   user;
    
    
    /**
     * Parameterless default ctor for implicite creation by {@link #getInstance(Class)}.
     */
    private Polymap() {
    }


    /**
     * Logging in using default JAAS config.
     */
    public void login() {
        String jaasConfigFile = "jaas_config.txt";
        File configFile = new File( getWorkspacePath().toFile(), jaasConfigFile );
        
        // create default config
        if (!configFile.exists()) {
            FileOutputStream out = null;
            try {
                log.info( "Creating default JAAS config: " + configFile.getAbsolutePath() );
                URL defaultConfigUrl = CorePlugin.getDefault().getBundle().getEntry( jaasConfigFile );
                out = new FileOutputStream( configFile );
                IOUtils.copy( defaultConfigUrl.openStream(), out );
            }
            catch (Exception e) {
                throw new RuntimeException( "Unable to create default jaas_config.txt in workspace.", e );
            }
            finally {
                IOUtils.closeQuietly( out );
            }
        }

        // create secureContext
        try {
            secureContext = LoginContextFactory.createContext( DEFAULT_LOGIN_CONFIG, 
                    configFile.toURI().toURL() );
        }
        catch (MalformedURLException e) {
            throw new RuntimeException( "Should never happen.", e );
        }
        
        // login
        for (boolean loggedIn=false; !loggedIn; ) {
            try {
                secureContext.login();
                subject = secureContext.getSubject();
                principals = new HashSet( subject.getPrincipals() );
                
                // find user
                for (Principal principal : principals) {
                    if (principal instanceof UserPrincipal) {
                        user = (UserPrincipal)principal;
                        break;
                    }
                }
                if (user == null) {
                    throw new LoginException( "Es wurde kein Nutzer in der Konfiguration gefunden" );
                }
                
                loggedIn = true;
            } 
            catch (LoginException e) {
                //log.warn( "Login error: " + e, e );
                IStatus status = new Status( IStatus.ERROR, CorePlugin.PLUGIN_ID,
                        "Login fehlgeschlagen.", e );
                ErrorDialog.openError( null, "Achtung", "Login fehlgeschlagen", status );
            }
        }
    }

    
    public void login( String username, String passwd )
    throws LoginException {
        String jaasConfigFile = "jaas_config.txt";
        File configFile = new File( getWorkspacePath().toFile(), jaasConfigFile );

        ServicesCallbackHandler.challenge( username, passwd );
        
        // create secureContext
        try {
            secureContext = LoginContextFactory.createContext( SERVICES_LOGIN_CONFIG, 
                    configFile.toURI().toURL() );
        }
        catch (MalformedURLException e) {
            throw new RuntimeException( "Should never happen.", e );
        }
        
        // login
        secureContext.login();
        subject = secureContext.getSubject();
        principals = new HashSet( subject.getPrincipals() );

        // find user
        for (Principal principal : principals) {
            if (principal instanceof UserPrincipal) {
                user = (UserPrincipal)principal;
                break;
            }
        }
        if (user == null) {
            throw new LoginException( "Es wurde kein Nutzer in der Konfiguration gefunden" );
        }
    }

    
    public void addPrincipal( Principal principal ) {
        principals.add( principal );
        if (principal instanceof UserPrincipal) {
            user = (UserPrincipal)principal;
        }
    }
    
    
    public Set<Principal> getPrincipals() {
        return principals;
    }

    
    public Principal getUser() {
        return user;
    }


    public Subject getSubject() {
        return subject;    
    }

    
//    /**
//     * Returns the preference service for the current session and user.
//     */
//    public IPreferencesService getPreferenceService() {
//        //return Platform.getPreferencesService().
//        CorePlugin.getDefault().
//    }

}
