package org.polymap.core.data.ui.featureTable;

import org.eclipse.jface.action.IAction;

import org.polymap.core.geohub.LayerFeatureSelectionManager.MODE;

/**
 * REPLACE 
 */
public class Replace
        extends FeatureSelectionModeAction {
    
    public void run( IAction action ) {
        setMode( MODE.REPLACE );
    }
}