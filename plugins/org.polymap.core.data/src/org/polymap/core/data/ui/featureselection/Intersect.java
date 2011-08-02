package org.polymap.core.data.ui.featureselection;

import org.eclipse.jface.action.IAction;

import org.polymap.core.geohub.LayerFeatureSelectionManager.MODE;

/**
 * INTERSECT
 */
public class Intersect
        extends FeatureSelectionModeAction {
    
    public void run( IAction action ) {
        setMode( MODE.INTERSECT );
    }
}