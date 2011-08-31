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
package org.polymap.core.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.login.LoginException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import org.polymap.core.runtime.Polymap;

import sun.security.acl.PrincipalImpl;

/**
 * Dummy authentication based on user/role setting in the jaas_config file or
 * a properties file.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class DummyLoginModule
        implements javax.security.auth.spi.LoginModule {

    private static Log log = LogFactory.getLog( DummyLoginModule.class );

    /** Maps user name to passwd. */
    private Map<String,DummyUserPrincipal>  users = new HashMap();

    private CallbackHandler                 callbackHandler;

    private boolean                         loggedIn;

    private Subject                         subject;
    
    private DummyUserPrincipal              principal;
    
    private File                            configFile;


    public DummyLoginModule() {
    }

    
    public File getConfigFile() {
        return configFile;
    }


    @SuppressWarnings("hiding")
    public void initialize( Subject subject, CallbackHandler callbackHandler, Map sharedState,
            Map options ) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        
        // check user/passwd settings in options
        for (Object elm : options.entrySet()) {
            Map.Entry<String,String> option = (Map.Entry)elm;
            log.debug( "option: key=" + option.getKey() + " = " + option.getValue() );
            
            if (option.getKey().equals( "configFile" )) {
                // absolute path or in the workspace
                configFile = option.getValue().startsWith( File.pathSeparator )
                        ? new File( option.getValue() )
                        : new File( Polymap.getWorkspacePath().toFile(), option.getValue() );
            }
            else {
                String user = option.getKey();
                users.put( user, new DummyUserPrincipal( user, option.getValue() ) );
            }
        }
        
        // check loginConfig file
        if (configFile != null) {
            try {
                Properties props = new Properties();
                if (!configFile.exists()) {
                    log.info( "Creating default login config: " + configFile.getAbsolutePath() );
                    props.put( "admin", "admin" );
                    props.store( new FileOutputStream( configFile ), "DummyLoginModule config file." );
                }
                else {
                    log.info( "Loading login config: " + configFile.getAbsolutePath() );
                    props.load( new FileInputStream( configFile )  );
                }
                for (String user : props.stringPropertyNames()) {
                    users.put( user, new DummyUserPrincipal( user, props.getProperty( user ) ) );
                }
            }
            catch (Exception e) {
                throw new RuntimeException( "Fehler beim Lesen/Schreiben: " + configFile.getAbsolutePath(), e );
            }
        }
    }


    public boolean login()
    throws LoginException {
        // check if there is a user with "login" password
        for (DummyUserPrincipal candidate : users.values()) {
            if (candidate.getPassword().equals( "login" )) {
                principal = candidate;
                return loggedIn = true;
            }
        }

        Callback label = new TextOutputCallback( TextOutputCallback.INFORMATION,
                "POLYMAP3 Workbench" );
        NameCallback nameCallback = new NameCallback( "Nutzername", "admin" );
        PasswordCallback passwordCallback = new PasswordCallback( "Passwort", false );
        try {
            callbackHandler.handle( new Callback[] { label, nameCallback, passwordCallback } );
        }
        catch (Exception e) {
            log.warn( "", e );
            throw new LoginException( e.getLocalizedMessage() );
        }

        String username = nameCallback.getName();

        String password = "";
        if (passwordCallback.getPassword() != null) {
            password = String.valueOf( passwordCallback.getPassword() );
        }

        DummyUserPrincipal candidate = users.get( username );
        if (candidate.getPassword().equals( password )) {
            principal = candidate;
            loggedIn = true;
            return true;
        }
        return false;
    }


    public boolean commit()
            throws LoginException {
        subject.getPrincipals().add( principal );
        subject.getPrincipals().addAll( principal.getRoles() );

        subject.getPrivateCredentials().add( this );
        subject.getPrivateCredentials().add( Display.getCurrent() );
        subject.getPrivateCredentials().add( SWT.getPlatform() );
        return loggedIn;
    }


    public boolean abort()
            throws LoginException {
        loggedIn = false;
        return true;
    }


    public boolean logout()
            throws LoginException {
        subject.getPrincipals().remove( principal );
        loggedIn = false;
        return true;
    }
    

    /**
     * 
     * 
     */
    final class DummyUserPrincipal
            extends UserPrincipal {

        private final String        passwd;
        

        /**
         * Creates a principal.
         * 
         * @param name The principal's string name.
         * @exception NullPointerException If the <code>name</code> is
         *            <code>null</code>.
         */
        public DummyUserPrincipal( String name, String configString ) {
            super( name );
            assert configString != null : "configString must not be null";

            String[] configStrings = StringUtils.split( configString, ",: " );
            if (configStrings.length == 0) {
                throw new RuntimeException( "Config für Nutzer ist zu kurz (kein Passwort): " + configString );
            }
            this.passwd = configStrings[0];
            
            for (int i=1; i<configStrings.length; i++) {
                roles.add( new PrincipalImpl( configStrings[i] ) );
            }
        }

        public String getPassword() {
            return passwd;
        }

    }

}
