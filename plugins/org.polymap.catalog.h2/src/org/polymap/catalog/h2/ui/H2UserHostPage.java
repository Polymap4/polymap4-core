package org.polymap.catalog.h2.ui;

import net.refractions.udig.catalog.service.database.UserHostPage;

public class H2UserHostPage
        extends UserHostPage {

    public H2UserHostPage( ) {
        super( new H2Dialect() );
    }

}
