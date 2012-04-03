package org.polymap.core.project.ui.properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.ui.internal.dialogs.AdaptableForwarder;
import org.eclipse.ui.views.properties.IPropertySource;
import org.polymap.core.project.IMap;
import org.polymap.core.project.ui.properties.PropertyProviderAdapterFactory.MapPropertySourceProvider;

/**
 * Basic properties of an {@link IMap}.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
@SuppressWarnings("restriction")
public class MapBasePropertyPage 
        extends SheetPropertyPage {

    private static Log log = LogFactory.getLog( MapBasePropertyPage.class );
    
    private MapPropertySourceProvider   provider = new MapPropertySourceProvider();
    
    
    public MapBasePropertyPage() {
        super();
    }

    
    public IMap getMap() {
        return (IMap)getElement();
    }


    public IPropertySource getPropertySource( Object obj ) {
        if (obj instanceof AdaptableForwarder) {
            obj = ((AdaptableForwarder)obj).getAdapter( IMap.class );
        }
        return provider.getPropertySource( obj );
    }
    
}