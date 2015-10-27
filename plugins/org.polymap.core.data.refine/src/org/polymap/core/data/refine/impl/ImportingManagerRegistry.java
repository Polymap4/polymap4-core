package org.polymap.core.data.refine.impl;

import com.google.refine.RefineServlet;
import com.google.refine.importing.ImportingManager;

import edu.mit.simile.butterfly.ButterflyModuleImpl;

public class ImportingManagerRegistry {

	public static void initialize(RefineServlet refineServlet) {
		ImportingManager.initialize(refineServlet);
		registerFormats();
		registerControllers();
	}

	public static void registerFormats() {
		/*
		 * Formats and their UI class names and parsers: - UI class names are
		 * used on the client-side in Javascript to instantiate code that lets
		 * the user configure the parser's options. - Parsers are server-side
		 * code that do the actual parsing. Because they have access to the raw
		 * files, they also generate defaults for the client-side UIs to
		 * initialize.
		 */

		ImportingManager.registerFormat("text", "Text files"); // generic
																// format, no
																// parser to
		// handle it
		ImportingManager.registerFormat("text/line-based", "Line-based text files", "LineBasedParserUI",
				new com.google.refine.importers.LineBasedImporter());
		ImportingManager.registerFormat("text/line-based/*sv", "CSV / TSV / separator-based files",
				"SeparatorBasedParserUI", new com.google.refine.importers.SeparatorBasedImporter());
		ImportingManager.registerFormat("text/line-based/fixed-width", "Fixed-width field text files",
				"FixedWidthParserUI", new com.google.refine.importers.FixedWidthImporter());

		ImportingManager.registerFormat("text/rdf+n3", "RDF/N3 files", "RdfTriplesParserUI",
				new com.google.refine.importers.RdfTripleImporter());

		ImportingManager.registerFormat("text/xml", "XML files", "XmlParserUI",
				new com.google.refine.importers.XmlImporter());
		ImportingManager.registerFormat("text/xml/xlsx", "Excel (.xlsx) files", "ExcelParserUI",
				new com.google.refine.importers.ExcelImporter());
		ImportingManager.registerFormat("text/xml/ods", "Open Document Format spreadsheets (.ods)", "ExcelParserUI",
				new com.google.refine.importers.OdsImporter());
		ImportingManager.registerFormat("text/xml/rdf", "RDF/XML files", "RdfTriplesParserUI",
				new com.google.refine.importers.RdfXmlTripleImporter());
		ImportingManager.registerFormat("text/json", "JSON files", "JsonParserUI",
				new com.google.refine.importers.JsonImporter());
		ImportingManager.registerFormat("text/marc", "MARC files", "XmlParserUI",
				new com.google.refine.importers.MarcImporter());

		ImportingManager.registerFormat("binary", "Binary files"); // generic
																	// format,
																	// no
		// parser to handle it
		ImportingManager.registerFormat("binary/xls", "Excel files", "ExcelParserUI",
				new com.google.refine.importers.ExcelImporter());

		ImportingManager.registerFormat("service", "Services"); // generic
																// format, no
																// parser
		// to handle it

		/*
		 * Extension to format mappings
		 */
		ImportingManager.registerExtension(".txt", "text/line-based");
		ImportingManager.registerExtension(".csv", "text/line-based/*sv");
		ImportingManager.registerExtension(".tsv", "text/line-based/*sv");

		ImportingManager.registerExtension(".xml", "text/xml");
		ImportingManager.registerExtension(".rdf", "text/xml/rdf");

		ImportingManager.registerExtension(".json", "text/json");
		ImportingManager.registerExtension(".js", "text/json");

		ImportingManager.registerExtension(".xls", "binary/xls");
		ImportingManager.registerExtension(".xlsx", "text/xml/xlsx");

		ImportingManager.registerExtension(".ods", "text/xml/ods");

		ImportingManager.registerExtension(".n3", "text/rdf+n3");

		ImportingManager.registerExtension(".marc", "text/marc");
		ImportingManager.registerExtension(".mrc", "text/marc");

		/*
		 * Mime type to format mappings
		 */
		ImportingManager.registerMimeType("text/plain", "text/line-based");
		ImportingManager.registerMimeType("text/csv", "text/line-based/*sv");
		ImportingManager.registerMimeType("text/x-csv", "text/line-based/*sv");
		ImportingManager.registerMimeType("text/tab-separated-value", "text/line-based/*sv");

		ImportingManager.registerMimeType("text/fixed-width", "text/line-based/fixed-width");

		ImportingManager.registerMimeType("text/rdf+n3", "text/rdf+n3");

		ImportingManager.registerMimeType("application/msexcel", "binary/xls");
		ImportingManager.registerMimeType("application/x-msexcel", "binary/xls");
		ImportingManager.registerMimeType("application/x-ms-excel", "binary/xls");
		ImportingManager.registerMimeType("application/vnd.ms-excel", "binary/xls");
		ImportingManager.registerMimeType("application/x-excel", "binary/xls");
		ImportingManager.registerMimeType("application/xls", "binary/xls");
		ImportingManager.registerMimeType("application/x-xls", "text/xml/xlsx");

		ImportingManager.registerMimeType("application/vnd.oasis.opendocument.spreadsheet", "text/xml/ods");

		ImportingManager.registerMimeType("application/json", "text/json");
		ImportingManager.registerMimeType("application/javascript", "text/json");
		ImportingManager.registerMimeType("text/json", "text/json");

		ImportingManager.registerMimeType("application/rdf+xml", "text/xml/rdf");

		ImportingManager.registerMimeType("application/marc", "text/marc");

		/*
		 * Format guessers: these take a format derived from extensions or
		 * mime-types, look at the actual files' content, and try to guess a
		 * better format.
		 */
		ImportingManager.registerFormatGuesser("text", new com.google.refine.importers.TextFormatGuesser());
		ImportingManager.registerFormatGuesser("text/line-based",
				new com.google.refine.importers.LineBasedFormatGuesser());
	}

	public static void registerControllers() {
		/*
		 * Controllers: these implement high-level UI flows for importing data.
		 * For example, the default controller lets the user specify one or more
		 * source files, either local or remote or on the clipboard, lets the
		 * user select which files to actually import in case any of the
		 * original file is an archive containing several files, and then lets
		 * the user configure parsing options.
		 */
		ImportingManager.registerController(new ButterflyModuleImpl() {
			@Override
			public String getName() {
				return "core";
			}
		}, "default-importing-controller",
				new com.google.refine.importing.DefaultImportingController());
        ImportingManager.registerController(new ButterflyModuleImpl() {
            @Override
            public String getName() {
                return "core";
            }
        }, "filebased-importing-controller",
                new com.google.refine.importing.FilebasedImportingController());
	}
}
