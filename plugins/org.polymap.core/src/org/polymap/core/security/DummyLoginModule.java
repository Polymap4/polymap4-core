/* 
 * polymap.org
 * Copyright (C) 2009-2013, Polymap GmbH. All rights reserved.
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
package org.polymap.core.security;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Principal;

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

import org.eclipse.ui.IWorkbenchPreferencePage;

import org.polymap.core.Messages;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.Polymap;

import sun.security.acl.PrincipalImpl;

/**
 * Dummy authentication based on user/role setting in the jaas_config file or
 * a properties file.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.0
 */
public class DummyLoginModule
        implements PreferencesLoginModule {

    private static Log log = LogFactory.getLog( DummyLoginModule.class );

    public static final String              OPTIONS_PROPS_FILE = "configFile";
    
    private static final IMessages          i18n = Messages.forPrefix( "LoginDialog" );
    
    /** Maps user name to passwd. */
    private Map<String,DummyUserPrincipal>  users = new HashMap();

    private CallbackHandler                 callbackHandler;

    private boolean                         loggedIn;

    private Subject                         subject;
    
    private DummyUserPrincipal              principal;
    
    private File                            configFile;

    private String                          dialogTitle = i18n.get( "dialogTitle" );
    
    private AuthorizationModule             authModule;


    public DummyLoginModule() {
    }

    
    @Override
    public IWorkbenchPreferencePage createPreferencePage() {
        return new DummyLoginPreferences( this );
    }


    public File getConfigFile() {
        return configFile;
    }


    @SuppressWarnings("hiding")
    public void initialize( Subject subject, CallbackHandler callbackHandler, Map sharedState,
            Map options ) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        
        // default Authorization: DummyAuthorization
        this.authModule = new DummyAuthorizationModule();
        this.authModule.init( this );
        
        // check option for other Autorization
        AuthorizationModuleExtension authExt = AuthorizationModuleExtension.forOptions( options );
        if (authExt != null) {
            authModule = authExt.initialize( this, subject, callbackHandler, sharedState, options );
        }
        
        // check user/passwd settings in options
        for (Object elm : options.entrySet()) {
            Map.Entry<String,String> option = (Map.Entry)elm;
            log.debug( "option: key=" + option.getKey() + " = " + option.getValue() );
            
            if (option.getKey().equals( OPTIONS_PROPS_FILE )) {
                // absolute path or in the workspace
                configFile = option.getValue().startsWith( File.pathSeparator )
                        ? new File( option.getValue() )
                        : new File( Polymap.getWorkspacePath().toFile(), option.getValue() );
            }
            else if (option.getKey().equals( "dialogTitle" )) {
                dialogTitle = option.getValue();
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


    protected DummyUserPrincipal userForName( String username ) {
        return users.get( username );
    }
    
    
    public boolean login() throws LoginException {
        // check if there is a user with "login" password
        for (DummyUserPrincipal candidate : users.values()) {
            if (candidate.getPassword().equals( "login" )) {
                principal = candidate;
                return loggedIn = true;
            }
        }

        try {
            Callback label = new TextOutputCallback( TextOutputCallback.INFORMATION, 
                    // empty if service login
                    StringUtils.defaultIfEmpty( dialogTitle, "POLYMAP3 Workbench" ) );
            NameCallback nameCallback = new NameCallback( 
                    StringUtils.defaultIfEmpty( i18n.get( "username" ), "Username" ), "default" );
            PasswordCallback passwordCallback = new PasswordCallback( 
                    StringUtils.defaultIfEmpty( i18n.get( "password" ), "Password" ), false );

            callbackHandler.handle( new Callback[] { label, nameCallback, passwordCallback } );

            String username = nameCallback.getName();

            String password = "";
            if (passwordCallback.getPassword() != null) {
                password = String.valueOf( passwordCallback.getPassword() );
            }

            DummyUserPrincipal candidate = userForName( username );
            if (candidate.getPassword().equals( password )) {
                principal = candidate;
                loggedIn = true;
                return true;
            }
            return false;
        }
        catch (Exception e) {
            log.warn( "", e );
            throw new LoginException( e.getLocalizedMessage() );
        }
    }


    public boolean commit() throws LoginException {
        subject.getPrincipals().add( principal );

        subject.getPrivateCredentials().add( this );
        subject.getPrivateCredentials().add( authModule );
        
        return loggedIn;
    }


    public boolean abort() throws LoginException {
        loggedIn = false;
        return true;
    }


    public boolean logout() throws LoginException {
        subject.getPrincipals().remove( principal );
        subject.getPrivateCredentials().remove( this );
        subject.getPrivateCredentials().remove( authModule );
        loggedIn = false;
        return true;
    }
    

    /**
     * 
     */
    final class DummyUserPrincipal
            extends UserPrincipal {

        private final String        passwd;
        
        private Set<Principal>      roles = new HashSet();
        

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

        
        public Set<Principal> getRoles() {
            return roles;
        }

    }

}
