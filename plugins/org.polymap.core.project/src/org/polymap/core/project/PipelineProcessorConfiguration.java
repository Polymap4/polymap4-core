package org.polymap.core.project;

import java.util.Properties;
import java.util.Set;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The configuration of a pipeline processor associated with a
 * {@link PipelineHolder}.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class PipelineProcessorConfiguration
        implements Externalizable {

    private static Log log = LogFactory.getLog( PipelineProcessorConfiguration.class );

    static final long           serialVersionUID = 1L;
    
    static final long           subVersion = 1L;
    
    private String              extId;
    
    private String              name;
    
    private Properties          props = new Properties();


    /**
     * Allow deserialization.
     */
    public PipelineProcessorConfiguration() {
    }
    
    public PipelineProcessorConfiguration( String extId, String name ) {
        assert extId != null;
        this.extId = extId;
        this.name = name;
    }

    public void writeExternal( ObjectOutput out )
    throws IOException {
        out.writeLong( subVersion );
        out.writeUTF( extId );
        out.writeUTF( name );
        
        Set<String> propNames = props.stringPropertyNames();
        log.info( "write: property names= " + propNames );
        out.writeInt( propNames.size() );
        
        for (String propName : propNames) {
            String value = props.getProperty( propName );
            log.info( "   property name= " + propName + ", value= " + value );
            out.writeUTF( propName );
            out.writeUTF( value );
        }
    }

    public void readExternal( ObjectInput in )
    throws IOException, ClassNotFoundException {
        long streamVersion = in.readLong();
        extId = in.readUTF();
        name = in.readUTF();
        
        int size = in.readInt();
        for (int i=0; i<size; i++) {
            props.setProperty( in.readUTF(), in.readUTF() );
        }
    }
    
    public String getExtensionId() {
        return extId;
    }
    
    public String getName() {
        return name;
    }

    public Properties getConfig() {
        return props;
    }

    public int hashCode() {
        return extId.hashCode();
    }

    public boolean equals( Object obj ) {
        if (obj == this) {
            return true;
        }
        else if (obj instanceof PipelineProcessorConfiguration) {
            return extId.equals( ((PipelineProcessorConfiguration)obj).extId );
        }
        return false;
    }

}