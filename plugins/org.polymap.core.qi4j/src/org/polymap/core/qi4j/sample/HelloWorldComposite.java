package org.polymap.core.qi4j.sample;

import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.mixin.Mixins;

/**
 * This Composite interface declares all the Fragments
 * of the HelloWorld composite.
 * <p/>
 * Currently it only declares one Mixin.
 */
@Mixins( {HelloWorldMixin.class, LabelMixin.class} )
public interface HelloWorldComposite
    extends HelloWorld, TransientComposite {
    
}
