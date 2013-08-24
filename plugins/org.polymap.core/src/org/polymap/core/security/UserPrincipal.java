package org.polymap.core.security;

import java.security.Principal;

/**
 * A user principal identified by a username or account name.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.0
 */
public class UserPrincipal
        implements Principal, java.io.Serializable {

    private static final long       serialVersionUID = 892106070870210969L;

    protected final String          name;
    
//    /**
//     * Must not contain UserPrincipal in order to distinguish between groups and
//     * users.
//     */
//    protected final Set<Principal>  roles = new HashSet();


    /**
     * Creates a principal.
     * 
     * @param name The principal's string name.
     * @exception NullPointerException If the <code>name</code> is
     *            <code>null</code>.
     */
    public UserPrincipal( String name ) {
        assert name != null : "name must not be null";
        this.name = name;
    }

    /**
     * Compares this principal to the specified object.
     * 
     * @param object The object to compare this principal against.
     * @return true if they are equal; false otherwise.
     */
    public boolean equals( Object object ) {
        if (this == object) {
            return true;
        }
        if (object instanceof UserPrincipal) {
            return name.equals( ((UserPrincipal)object).getName() );
        }
        return false;
    }

    /**
     * Returns a hash code for this principal.
     * 
     * @return The principal's hash code.
     */
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Returns the name of this principal.
     */
    public String getName() {
        return name;
    }

//    /**
//     * Returns the password of this principal.
//     */
//    public abstract String getPassword();
//    
//    public Set<Principal> getRoles() {
//        return roles;
//    }

    /**
     * Returns a string representation of this principal.
     */
    public String toString() {
        return name;
    }

}