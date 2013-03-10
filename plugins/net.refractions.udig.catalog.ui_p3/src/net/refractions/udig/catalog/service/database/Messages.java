package net.refractions.udig.catalog.service.database;

import org.eclipse.rwt.RWT;

import org.eclipse.osgi.util.NLS;

import org.polymap.core.runtime.MessagesImpl;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "net.refractions.udig.catalog.service.database.messages"; //$NON-NLS-1$
//    public static String DatabaseWizardLocalization_brokenElements;
//    public static String DatabaseWizardLocalization_changePasswordQuery;
//    public static String DatabaseWizardLocalization_confirmRemoveConnection;
//    public static String DatabaseWizardLocalization_database;
//    public static String DatabaseWizardLocalization_databaseConnectionInterrupted;
//    public static String DatabaseWizardLocalization_databasePermissionProblemMessage;
//    public static String DatabaseWizardLocalization_filter;
//    public static String DatabaseWizardLocalization_geometryType;
//    public static String DatabaseWizardLocalization_geometryName;
//    public static String DatabaseWizardLocalization_host;
//    public static String DatabaseWizardLocalization_incorrectConfiguration;
//    public static String DatabaseWizardLocalization_list;
//    public static String DatabaseWizardLocalization_password;
//    public static String DatabaseWizardLocalization_port;
//    public static String DatabaseWizardLocalization_portError;
//    public static String DatabaseWizardLocalization_previousConnections;
//    public static String DatabaseWizardLocalization_publicSchema;
//    public static String DatabaseWizardLocalization_publicSchemaTooltip;
//    public static String DatabaseWizardLocalization_removeConnection;
//    public static String DatabaseWizardLocalization_requiredField;
//    public static String DatabaseWizardLocalization_schema;
//    public static String DatabaseWizardLocalization_storePassword;
//    public static String DatabaseWizardLocalization_table;
//    public static String DatabaseWizardLocalization_tableSelectionFilterTooltip;
//    public static String DatabaseWizardLocalization_unexpectedError;
//    public static String DatabaseWizardLocalization_username;
//    
//    static {
//        // initialize resource bundle
//        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
//    }
    
    private static final MessagesImpl   instance = new MessagesImpl( BUNDLE_NAME, Messages.class.getClassLoader() );

    private Messages() {
        // prevent instantiation
    }

    public static String get( String key, Object... args ) {
        return instance.get( key, args );
    }

    public static String get2( Object caller, String key, Object... args ) {
        return instance.get( caller, key, args );
    }

    public static Messages get() {
        Class clazz = Messages.class;
        return (Messages)RWT.NLS.getISO8859_1Encoded( BUNDLE_NAME, clazz );
    }
    
}
