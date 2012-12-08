package org.polymap.core.project.ui.properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.ui.internal.dialogs.AdaptableForwarder;
import org.eclipse.ui.views.properties.IPropertySource;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.ui.properties.PropertyProviderAdapterFactory.LayerPropertySourceProvider;

/**
 * Basic properties of an {@link ILayer}.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
@SuppressWarnings("restriction")
public class LayerBasePropertyPage 
        extends SheetPropertyPage {

    private static Log log = LogFactory.getLog( LayerBasePropertyPage.class );

    private LayerPropertySourceProvider     provider = new LayerPropertySourceProvider();
    

    public LayerBasePropertyPage() {
    }

    public ILayer getLayer() {
        return (ILayer)getElement();
    }

    public IPropertySource getPropertySource( Object obj ) {
        if (obj instanceof AdaptableForwarder) {
            obj = ((AdaptableForwarder)obj).getAdapter( ILayer.class );
        }
        return provider.getPropertySource( obj );
    }

}