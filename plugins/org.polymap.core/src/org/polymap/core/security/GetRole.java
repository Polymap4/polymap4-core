package org.polymap.core.security;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

/**
 * The class GetRole search with ldap for the users domain roles and returns
 * them.
 * <p>
 * Based on http://www.oio.de/public/java/jaas/sso-jaas-kerberos-tutorial.htm
 * 
 * @author Orientation in Objects GmbH
 */
class GetRole {

	private NamingEnumeration role = null;


    /**
     * The Method scanRole search with ldap for the users domain roles
     * 
     * @param userName userPrincipalName of the loginuser
     */
    public void scanRole( String userName ) {

        String principal = "yourPrincipal";
        String credentials = "yourCredential";

        Hashtable env = new Hashtable( 11 );
        env.put( Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory" );
        env.put( Context.PROVIDER_URL, "ldap://yourServer:yourPort/yourDomain" );

        env.put( Context.SECURITY_PRINCIPAL, principal );
        env.put( Context.SECURITY_CREDENTIALS, credentials );

        try {
            // Erstellt eine Initial Directory Context
            DirContext ctx = new InitialDirContext( env );
            Attributes attr = new BasicAttributes( "userPrincipalName", userName );
            NamingEnumeration userData = ctx.search( "cn=Users", attr );

            while (userData.hasMoreElements()) {
                SearchResult sr = (SearchResult)userData.next();
                sr.getAttributes();
                Attributes userAttributes = sr.getAttributes();
                Attribute at = userAttributes.get( "memberOf" );

                if (at != null) {
                    role = at.getAll();
                }
                else {
                    role = null;
                }
            }
        }
        catch (NamingException e) {
            e.printStackTrace();
            role = null;
        }
    }


    /**
     * Returns the domain roles of an user as NamingEnumeration
     * 
     * @return the domain roles of an user as NamingEnumeration
     */
    public NamingEnumeration getRoleAsEnumeration() {

        return role;

    }

}
