package org.polymap.core.qi4j.sample;

import org.qi4j.api.injection.scope.This;


/**
 * This is the implementation of the HelloWorld interface. The behaviour and
 * state is mixed.
 */
public class HelloWorldMixin
        implements HelloWorldBehaviour, HelloWorldState {

    String                  phrase = "phrase";

    String                  name = "name";
    
    @This Labeled           labeled;
    
//    @UseDefaults
//    @MaxLength(50)
//    Property<String>                        label();

    //@This HelloWorldState state;


    public String say() {
        return labeled.getLabel() + ": " + getPhrase() + " " + getName();
    }


    public void setPhrase( String phrase )
            throws IllegalArgumentException {
        if (phrase == null) {
            throw new IllegalArgumentException( "Phrase may not be null" );
        }
        this.phrase = phrase;
    }


    public String getPhrase() {
        return phrase;
    }


    public void setName( String name )
            throws IllegalArgumentException {
        if (name == null) {
            throw new IllegalArgumentException( "Name may not be null" );
        }
        this.name = name;
    }


    public String getName() {
        return name;
    }
}
