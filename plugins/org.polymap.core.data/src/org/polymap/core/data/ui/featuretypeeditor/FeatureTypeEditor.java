/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
 * Copyright 2011, Falko Bräutigam
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data.ui.featuretypeeditor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.refractions.udig.internal.ui.UiPlugin;
import net.refractions.udig.ui.CRSDialogCellEditor;
import net.refractions.udig.ui.internal.Messages;
import net.refractions.udig.ui.preferences.PreferenceConstants;

import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;

import org.eclipse.ui.IActionBars;

import org.polymap.core.data.feature.LegalAttributeType;

/**
 * A composite editor based on a JFace TreeViewer for creating and editing feature types.
 * <p>
 * The code was originally found in {@link net.refractions.udig.ui.FeatureTypeEditor}.
 *
 * @author jones
 * @author Andrea Antonello (www.hydrologis.com)
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.0
 */
public class FeatureTypeEditor {

    // _p3: use default by dialect
    //private static final int MAX_ATTRIBUTE_LENGTH = 65535;  //Maximum allows by mysql (postgis bigger) and is "big enough"
    /**
     * The index of the name column in the viewer.
     */
    static final int NAME_COLUMN = 0;
    /**
     * The index of the type column in the viewer.
     */
    static final int TYPE_COLUMN = 1;
    /**
     * The index of the type column in the viewer.
     */
    static final int OTHER_COLUMN = 2;


    TreeViewer                             viewer;

    List<ValueViewerColumn>                viewerColumns = new ArrayList();

    List<LegalAttributeType>               legalTypes = LegalAttributeType.types();

    SimpleFeatureType                      featureType;

    ControlDecoration                      errorDecorator;

    private IAction                        createAttributeAction;

    private IAction                        deleteAttributeAction;

    private Text                           nameText;


    /**
     * Create the table control and set the input.
     *
     * @param parent the composite that will be used as the TreeViewer's parent.
     * @param layoutData the layout data to use to layout the editor. If null
     *        GridData(Fill_Both)
     */
    public void createTable( Composite parent, Object layoutData ) {
        createTable( parent, layoutData, featureType, true );
    }


    /**
     * Create the table control and set the input.
     *
     * @param parent the composite that will be used as the TreeViewer's parent.
     * @param layoutData the layout data to use to layout the editor. If null
     *        GridData(Fill_Both)
     */
    public void createTable( Composite parent, Object layoutData, SimpleFeatureType type ) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName( type.getName() );
        builder.init( type );
        createTable( parent, layoutData, builder.buildFeatureType(), true );
    }


    /**
     * Create the table control and set the input.
     *
     * @param parent the composite that will be used as the TreeViewer's parent.
     * @param layoutData the layout data to use to layout the editor. If null GridData(Fill_Both).
     * @param featureType the {@link FeatureType} to use to populate the table.
     * @param editable the editable flag of the table
     */
    public void createTable( Composite parent, Object layoutData, SimpleFeatureType _featureType,
            boolean editable ) {

        viewer = new TreeViewer( parent, SWT.FULL_SELECTION );

        Tree tree = viewer.getTree();
        tree.setLinesVisible( true );

        if (layoutData == null) {
            tree.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        } else {
            tree.setLayoutData( layoutData );
        }
        tree.setHeaderVisible( true );
        TableLayout tableLayout = new TableLayout();
        tableLayout.addColumnData( new ColumnWeightData( 1 ) );
        tableLayout.addColumnData( new ColumnWeightData( 1 ) );
        tableLayout.addColumnData( new ColumnWeightData( 1 ) );

        for (ValueViewerColumn viewerColumn : viewerColumns) {
            tableLayout.addColumnData( new ColumnWeightData( 1 ) );
        }

        tree.setLayout( tableLayout );

        TreeColumn column = new TreeColumn( tree, SWT.CENTER );
        column.setResizable( true );
        column.setText( Messages.FeatureTypeEditor_nameColumnName );

        column = new TreeColumn( tree, SWT.LEFT );
        column.setResizable( true );
        column.setText( Messages.FeatureTypeEditor_typeColumnName );

        column = new TreeColumn( tree, SWT.LEFT );
        column.setResizable( true );

        for (ValueViewerColumn viewerColumn : viewerColumns) {
            column = new TreeColumn( tree, SWT.LEFT );
            column.setResizable( true );
            column.setText( viewerColumn.getHeaderText() );
        }

        viewer.setContentProvider( new FeatureTypeContentProvider( viewer ) );
        viewer.setLabelProvider( new FeatureTypeLabelProvider( this ) );

        List<String> columnProps = new ArrayList();
        columnProps.add( String.valueOf( NAME_COLUMN ) );
        columnProps.add( String.valueOf( TYPE_COLUMN ) );
        columnProps.add( String.valueOf( OTHER_COLUMN ) );
        for (ValueViewerColumn viewerColumn : viewerColumns) {
            columnProps.add( String.valueOf( viewerColumn.getColumnProperty() ) );
        }
        viewer.setColumnProperties( columnProps.toArray( new String[columnProps.size()] ) );

        setEditable( editable );
        setFeatureType( _featureType );
    }


    public void addViewerColumn( ValueViewerColumn viewerColumn ) {
        viewerColumn.init( viewerColumns.size() + 3, this );
        viewerColumns.add( viewerColumn );
    }


    /**
     * Sets whether the table is editable or just a viewer.
     *
     * @param editable if true then the table can be edited
     */
    public void setEditable( boolean editable ) {
        if (editable) {
            Tree tree = viewer.getTree();
            String[] comboItems = new String[legalTypes.size()];
            for (int i = 0; i < comboItems.length; i++) {
                comboItems[i] = legalTypes.get( i ).getName();
            }

            TextCellEditor attributeNameEditor = new TextCellEditor( tree );
            ComboBoxCellEditor attributeTypeEditor = new ComboBoxCellEditor( tree, comboItems,
                    SWT.READ_ONLY | SWT.FULL_SELECTION );

            List<CellEditor> cellEditors = new ArrayList();
            cellEditors.add( attributeNameEditor );
            cellEditors.add( attributeTypeEditor );
            cellEditors.add( new CRSDialogCellEditor( tree ) );
            for (ValueViewerColumn viewerColumn : viewerColumns) {
                cellEditors.add( viewerColumn.newCellEditor( tree ) );
            }

            viewer.setCellEditors( cellEditors.toArray( new CellEditor[cellEditors.size()] ) );

            viewer.setCellModifier( new AttributeCellModifier( this ) );
        }
        else {
            viewer.setCellEditors( null );
            viewer.setCellModifier( null );
        }
    }


    public SimpleFeatureTypeBuilder builderFromFeatureType( SimpleFeatureType type ) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.init( type );
        return builder;
        
//        builder.setName( type.getName() );
//        builder.setCRS( type.getCoordinateReferenceSystem() );
//        
//        for (AttributeDescriptor attr : type.getAttributeDescriptors()) {
//            int length = findVarcharColumnLength( attr );
//            length = length > 0 ? length : LegalAttributeType.DEFAULT_STRING_LENGTH;
//            builder.length( length );
//            builder.add( attr.getLocalName(), attr.getType().getBinding() );
//        }
//        return builder;
    }


    /**
     * Creates a ContextMenu (the menu is created using the Table's composite as a parent) and returns
     * the contextMenu.
     *
     * <p>It is recommended that the MenuManager be registered with an IWorkbenchPartSite</p>
     * @return a MenuManager for the contextMenu.
     */
    public MenuManager createContextMenu(){
        final MenuManager contextMenu = new MenuManager();

        contextMenu.setRemoveAllWhenShown(true);
        contextMenu.addMenuListener(new IMenuListener(){
            public void menuAboutToShow( IMenuManager mgr ) {
                contextMenu.add( new CreateAttributeAction( FeatureTypeEditor.this ) );
                contextMenu.add( new DeleteAttributeAction( FeatureTypeEditor.this ) );
            }
        });

        Menu menu = contextMenu.createContextMenu(viewer.getTree());
        viewer.getControl().setMenu(menu);

        return contextMenu;
    }

    /**
     * Sets the Global actions that apply.  IE sets the delete global action.
     *
     * @param actionBars
     */
    public void setGlobalActions( IActionBars actionBars){
        throw new RuntimeException( "FIXME check if just addeing a new action is ok." );
//        actionBars.setGlobalActionHandler( ActionFactory.DELETE.getId(), getDeleteAction() );
    }

    /**
     * Sets the {@link SimpleFeatureType} being edited.
     *
     * <p>If type is null then a new featureType is created. Must be
     * called in the display thread.</p>
     *
     * @param type then new SimpleFeatureType to be edited, or null to create a new type.
     */
    public void setFeatureType( SimpleFeatureType type ) {
        if (type != null) {
            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            builder.init( type );
            featureType = builder.buildFeatureType();
            
//            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
//            builder.setName( type.getName() );
//            builder.setCRS( type.getCoordinateReferenceSystem() );
//            
//            for (AttributeDescriptor attr : type.getAttributeDescriptors()) {
//                int length = findVarcharColumnLength( attr );
//                length = length > 0 ? length : LegalAttributeType.DEFAULT_STRING_LENGTH;
//                builder.length( length );
//                builder.add( attr.getLocalName(), attr.getType().getBinding() );
//            }
//            featureType = builder.buildFeatureType();
        }
        else {
            featureType = createDefaultFeatureType();
        }
        if (viewer != null) {
            setInput( featureType );
        }
    }

    private int findVarcharColumnLength( AttributeDescriptor att ) {
        for (Filter r : att.getType().getRestrictions()) {
            if (r instanceof PropertyIsLessThanOrEqualTo) {
                PropertyIsLessThanOrEqualTo c = (PropertyIsLessThanOrEqualTo)r;
                if (c.getExpression1() instanceof Function
                        && ((Function)c.getExpression1()).getName().toLowerCase().endsWith( "length" )) {
                    if (c.getExpression2() instanceof Literal) {
                        Integer length = c.getExpression2().evaluate( null, Integer.class );
                        if (length != null) {
                            return length;
                        }
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Creates a default {@link FeatureType}.
     *
     * <p>
     * The default type has a {@link Geometry} attribute and a name attribute.
     * The geometry attribute is a {@link LineString}.
     * </p>
     *
     * @return a default FeatureType.
     */
    public SimpleFeatureType createDefaultFeatureType() {
        SimpleFeatureTypeBuilder builder;
        builder = new SimpleFeatureTypeBuilder();
        builder.setName( Messages.FeatureTypeEditor_newFeatureTypeName );
        builder.setCRS( getDefaultCRS() );
        builder.length( LegalAttributeType.DEFAULT_STRING_LENGTH );
        builder.add( Messages.FeatureTypeEditor_defaultNameAttributeName, String.class );
        builder.add( Messages.FeatureTypeEditor_defaultGeometryName, LineString.class );
        return builder.buildFeatureType();
    }


    CoordinateReferenceSystem getDefaultCRS() {
        String crsInfo = UiPlugin.getDefault().getPreferenceStore().getString(
                PreferenceConstants.P_DEFAULT_GEOMEMTRY_CRS );
        if (crsInfo != null && crsInfo.trim().length() > 0) {
            try {
                crsInfo = crsInfo.trim();
                if (crsInfo.startsWith( "EPSG" )) { //$NON-NLS-1$
                    return CRS.decode( crsInfo );
                }
                return CRS.parseWKT( crsInfo );
            }
            catch (Throwable t) {
                UiPlugin.log( "", t ); //$NON-NLS-1$
            }
        }
        return DefaultGeographicCRS.WGS84;
    }


    public void setDefaultCRS( CoordinateReferenceSystem crs ) {
        String crsInfo = null;

        Set<ReferenceIdentifier> identifiers = crs.getIdentifiers();
        for (Identifier identifier : identifiers) {
            if (identifier.toString().startsWith( "EPSG" )) { //$NON-NLS-1$
                crsInfo = identifier.toString();
                break;
            }
        }

        if (crsInfo == null) {
            crsInfo = crs.toWKT();
        }
        UiPlugin.getDefault().getPreferenceStore().setValue(
                PreferenceConstants.P_DEFAULT_GEOMEMTRY_CRS, crsInfo );

        SimpleFeatureTypeBuilder tmpBuilder = new SimpleFeatureTypeBuilder();
        tmpBuilder.init( featureType );
        tmpBuilder.setName( featureType.getTypeName() );
        tmpBuilder.setCRS( crs );
        featureType = tmpBuilder.buildFeatureType();
    }


    private void setInput( SimpleFeatureType featureType ) {
        viewer.setInput( featureType );
        if (nameText != null && !nameText.isDisposed()) {
            nameText.setText( featureType.getTypeName() );
        }
    }


    /**
     * Retrieves the new SimpleFeatureType. Must be called in the display thread. May return null.
     *
     * @return the new SimpleFeatureType.
     */
    public SimpleFeatureType getFeatureType() {
        if (viewer == null) {
            return null;
        }
        return (SimpleFeatureType) viewer.getInput();
    }

    /**
     * Returns the FeatureTypeBuilder that is used for editing the feature type.
     *
     * @return the FeatureTypeBuilder that is used for editing the feature type.
     */
    public SimpleFeatureTypeBuilder getFeatureTypeBuilder() {
        return viewer != null
            ?  builderFromFeatureType((SimpleFeatureType) viewer.getInput()) : null;
    }

    /**
     * Returns the control that is the FeatureTypeEditor.
     *
     * @return the control that is the FeatureTypeEditor.
     */
    public Control getControl() {
        return viewer.getControl();
    }

    /**
     * Updates the viewer so it matches the state of the builder.
     */
    public void builderChanged() {
        viewer.refresh();
        if (nameText != null && !nameText.isDisposed()) {
            if (viewer.getInput() != null) {
                String typeName = ((SimpleFeatureType)viewer.getInput()).getTypeName();
                nameText.setText( typeName );
            }
        }
    }

    public void setErrorMessage( String errorMessage ) {
        errorDecorator.setDescriptionText( errorMessage );
        errorDecorator.show();
        // XXX _p3: no showHoverText()
        // errorDecorator.showHoverText(errorMessage);
    }

}
