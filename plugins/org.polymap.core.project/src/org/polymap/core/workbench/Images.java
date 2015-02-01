package org.polymap.core.workbench;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.polymap.core.CorePlugin;


final class Images {
    
    final static Image IMG_TOP_LEFT = registerImage( "top_left.gif" );
    final static Image IMG_TOP_CENTER = registerImage( "top_center.gif" );
    final static Image IMG_TOP_RIGHT = registerImage( "top_right.gif" );
    final static Image IMG_MIDDLE_LEFT = registerImage( "middle_left.gif" );
    final static Image IMG_MIDDLE_CENTER = registerImage( "middle_center.gif" );
    final static Image IMG_MIDDLE_RIGHT = registerImage( "middle_right.gif" );
    final static Image IMG_BOTTOM_LEFT = registerImage( "bottom_left.gif" );
    final static Image IMG_BOTTOM_CENTER = registerImage( "bottom_center.gif" );
    final static Image IMG_BOTTOM_RIGHT = registerImage( "bottom_right.gif" );
    final static Image IMG_BANNER_ROUNDED_LEFT = registerImage( "banner_rounded_left.png" );
    final static Image IMG_BANNER_ROUNDED_RIGHT = registerImage( "banner_rounded_right.png" );
    final static Image IMG_BANNER_BG = registerImage( "banner_bg.png" );


    private Images() {
        // prevent instance creation
    }


    private static Image registerImage( final String imageName ) {
        String folder = "icons/";
        ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin( 
                CorePlugin.PLUGIN_ID, folder + imageName );
        return descriptor.createImage();
    }

}
