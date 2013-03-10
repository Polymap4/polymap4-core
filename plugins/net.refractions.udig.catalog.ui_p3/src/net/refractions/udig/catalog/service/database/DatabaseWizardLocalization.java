package net.refractions.udig.catalog.service.database;

/**
 * This class allows the wizards to be shared by different database plugins.  There are defaults given and
 * they can be changed on construction as required by the actual implementation if the defaults don't work  
 * 
 * @author jesse
 * @since 1.1.0
 */
public class DatabaseWizardLocalization {

    public String portError = Messages.get("DatabaseWizardLocalization_portError");
    public String host = Messages.get("DatabaseWizardLocalization_host");
    public String port = Messages.get("DatabaseWizardLocalization_port");
    public String username = Messages.get("DatabaseWizardLocalization_username");
    public String password = Messages.get("DatabaseWizardLocalization_password");
    public String database = Messages.get("DatabaseWizardLocalization_database");
    public String storePassword = Messages.get("DatabaseWizardLocalization_storePassword");
    public String removeConnection = Messages.get("DatabaseWizardLocalization_removeConnection");
    public String confirmRemoveConnection = Messages.get("DatabaseWizardLocalization_confirmRemoveConnection");
    public String previousConnections = Messages.get("DatabaseWizardLocalization_previousConnections");
    public String requiredField = Messages.get("DatabaseWizardLocalization_requiredField");
    public String changePasswordQuery = Messages.get("DatabaseWizardLocalization_changePasswordQuery");
    public String databaseConnectionInterrupted = Messages.get("DatabaseWizardLocalization_databaseConnectionInterrupted");
    public String unexpectedError = Messages.get("DatabaseWizardLocalization_unexpectedError");
    public String brokenElements = Messages.get("DatabaseWizardLocalization_brokenElements");
    public String table = Messages.get("DatabaseWizardLocalization_table");
    public String schema = Messages.get("DatabaseWizardLocalization_schema");
    public String geometryName = Messages.get("DatabaseWizardLocalization_geometryName");
    public String geometryType = Messages.get("DatabaseWizardLocalization_geometryType");
    public String publicSchema = Messages.get("DatabaseWizardLocalization_publicSchema");
    public String publicSchemaTooltip = Messages.get("DatabaseWizardLocalization_publicSchemaTooltip");
    public String filter = Messages.get("DatabaseWizardLocalization_filter");
    public String tableSelectionFilterTooltip = Messages.get("DatabaseWizardLocalization_tableSelectionFilterTooltip");
    public String incorrectConfiguration = Messages.get("DatabaseWizardLocalization_incorrectConfiguration");
    public String list = Messages.get("DatabaseWizardLocalization_list");
    public String databasePermissionProblemMessage = Messages.get("DatabaseWizardLocalization_databasePermissionProblemMessage");

}
