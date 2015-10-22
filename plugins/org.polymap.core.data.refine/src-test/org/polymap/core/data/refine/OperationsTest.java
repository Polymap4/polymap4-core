package org.polymap.core.data.refine;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.polymap.core.data.refine.impl.CSVFormatAndOptions;
import org.polymap.core.data.refine.impl.ImportResponse;
import org.polymap.core.data.refine.impl.RefineServiceImpl;

import com.google.common.io.Files;
import com.google.refine.model.ColumnModel;
import com.google.refine.model.Row;

public class OperationsTest {

    private RefineServiceImpl service;


    @Before
    public void setUp() {
        service = RefineServiceImpl.INSTANCE( new File( System.getProperty( "java.io.tmpdir" ) ) );
    }


    @Test
    public void testSSV() throws Exception {
        // ; separated file
        File wohngebiete = new File(
                this.getClass().getResource( "/data/wohngebiete_sachsen.csv" ).getFile() );
        File tmp = File.createTempFile( "foo", ".csv" );
        Files.copy( wohngebiete, tmp );
        ImportResponse<CSVFormatAndOptions> response = service.importFile( tmp,
                CSVFormatAndOptions.createDefault() );
        assertEquals( ";", response.options().separator() );

        // get the loaded models
        ColumnModel columns = response.job().project.columnModel;
        assertEquals( 12, columns.columns.size() );

        List<Row> rows = response.job().project.rows;
        assertEquals( "Baugenehmigungen: Neue Wohn-u.Nichtwohngeb. einschl. Wohnh.,",
                rows.get( 0 ).cells.get( 0 ).value );
        assertEquals( 100, rows.size() );
        assertEquals( "neue Wohngeb. mit 1 od.2 Wohnungen, Räume u.Fläche d.Wohn.,",
                rows.get( 1 ).cells.get( 0 ).value );

        CSVFormatAndOptions options = response.options();
        options.setSeparator( "\\t" );
        service.updateOptions( response.job(), options );
        
//        Map<String, String> params = Maps.newHashMap();
//        String columnToRemove = columns.columns.get( 2 ).getName();
//        params.put( "columnName", columnToRemove );
//        params.put( "project", "" + response.job().project.id );
//        service.post( ReorderRowsCommand.class, params );
//        
//        columns = response.job().project.columnModel;
//        assertEquals( 11, columns.columns.size() );
    }
}
