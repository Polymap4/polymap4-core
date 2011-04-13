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
import java.util.Map;
import java.util.Set;

import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.lf5.util.StreamUtils;

import org.eclipse.swt.widgets.Display;

import org.eclipse.rwt.SessionSingletonBase;
import org.eclipse.rwt.internal.lifecycle.RWTLifeCycle;

import org.eclipse.jface.dialogs.ErrorDialog;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.equinox.security.auth.ILoginContext;
import org.eclipse.equinox.security.auth.LoginContextFactory;

import org.polymap.core.CorePlugin;
import org.polymap.core.security.UserPrincipal;

/**
 * Static access to the runtime infrastructure.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
@SuppressWarnings("restriction")
public final class Polymap
        extends SessionSingletonBase {

    private static Log log = LogFactory.getLog( Polymap.class );

    public static final String      DEFAULT_LOGIN_CONFIG = "POLYMAP";
    

    // static factory *************************************
    
    /**
     * Gets or creates the Polymap instance for the application session of the
     * current thread.
     */
    public static Polymap instance() {
        return (Polymap)getInstance( Polymap.class );
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
     * The {@link Display} of the session of the current thread. Null, if the
     * current thread has no session. The result is equivalent to
     * {@link Display#getCurrent()} except that the calling thread does need to
     * be the UI thread of the session.
     */
    public static Display getSessionDisplay() {
        return RWTLifeCycle.getSessionDisplay();
    }
    
    
    /**
     * Returns a named attribute for the session of the current thread.
     * 
     * @param key
     * @return The value found for the given key, or null if there is no such attribute.
     */
    public static Object getSessionAttribute( String key ) {
        return instance().attributes.get( key );    
    }


    /**
     * Sets the named attribute for the session of the current thread.
     * 
     * @param key
     * @param value
     * @return The old value for the given key, or null of there was no such
     *         attribute.
     */
    public static Object setSessionAttribute( String key, Object value ) {
        return instance().attributes.put( key, value );
    }
    
    
    // instance *******************************************

    /** The session attributes. */
    private Map             attributes = new HashMap();
    
    private ILoginContext   secureContext;
    
    private Subject         subject;
    
    private Set<Principal>  principals;
    
    private UserPrincipal   user;
    
    
    /**
     * Parameterless default ctor for implicite creation by {@link #getInstance(Class)}.
     */
    private Polymap() {
    }


    public void addSessionShutdownHook() {
        throw new RuntimeException( "not yet impemented." );
    }


    /**
     * Logging in using default JAAS config.
     */
    public void login() {
        String jaasConfigFile = "jaas_config.txt";
        File configFile = new File( getWorkspacePath().toFile(), jaasConfigFile );
        
        // create default config
        if (!configFile.exists()) {
            try {
                log.info( "Creating default JAAS config: " + configFile.getAbsolutePath() );
                URL defaultConfigUrl = CorePlugin.getDefault().getBundle().getEntry( jaasConfigFile );
                StreamUtils.copyThenClose( defaultConfigUrl.openStream(), 
                        new FileOutputStream( configFile ) );
            }
            catch (Exception e) {
                throw new RuntimeException( "Unable to create default jaas_config.txt in workspace.", e );
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
