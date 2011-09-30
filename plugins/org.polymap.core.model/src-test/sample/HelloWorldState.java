package org.polymap.core.qi4j.sample;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.MaxLength;

/**
 * This interface contains only the state
 * of the HelloWorld object.
 * The exceptions will be thrown by Qi4j automatically if
 * null is sent in as values. The parameters would have to be declared
 * as @Optional if null is allowed.
 */
public interface HelloWorldState
{

    void setPhrase( String phrase )
        throws IllegalArgumentException;

    String getPhrase();

    void setName( String name )
        throws IllegalArgumentException;

    String getName();
}
