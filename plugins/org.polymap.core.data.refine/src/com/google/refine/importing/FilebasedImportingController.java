/*

Copyright 2011, Google Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    * Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
copyright notice, this list of conditions and the following disclaimer
in the documentation and/or other materials provided with the
distribution.
    * Neither the name of Google Inc. nor the names of its
contributors may be used to endorse or promote products derived from
this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,           
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY           
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

package com.google.refine.importing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.polymap.core.data.refine.impl.RefineRequest;

import com.google.common.io.Files;
import com.google.refine.commands.HttpUtilities;
import com.google.refine.importing.ImportingUtilities.Progress;
import com.google.refine.util.JSONUtilities;
import com.google.refine.util.ParsingUtilities;

public class FilebasedImportingController
        extends DefaultImportingController {

    private static Log log = LogFactory.getLog( FilebasedImportingController.class );


    //
    // protected RefineServlet servlet;
    //
    // @Override
    // public void init(RefineServlet servlet) {
    // this.servlet = servlet;
    // }
    //
    // @Override
    // public void doGet(HttpServletRequest request, HttpServletResponse response)
    // throws ServletException, IOException {
    // throw new ServletException("GET verb not implemented");
    // }
    //
    @Override
    public void doPost( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException {

        /*
         * The uploaded file is in the POST body as a "file part". If we call
         * request.getParameter() then the POST body will get read and we won't have
         * a chance to parse the body ourselves. This is why we have to parse the URL
         * for parameters ourselves.
         */
        Properties parameters = ParsingUtilities.parseUrlParameters( request );
        String subCommand = parameters.getProperty( "subCommand" );
        if ("load-raw-data".equals( subCommand )) {
            doLoadRawData( request, response, parameters );
        }
        else {
            super.doPost( request, response );
        }
    }


    private void doLoadRawData( HttpServletRequest request, HttpServletResponse response,
            Properties parameters ) throws ServletException, IOException {

        long jobID = Long.parseLong( parameters.getProperty( "jobID" ) );
        ImportingJob job = ImportingManager.getJob( jobID );
        if (job == null) {
            HttpUtilities.respond( response, "error", "No such import job" );
            return;
        }

        job.updating = true;
        try {
            JSONObject config = job.getOrCreateDefaultConfig();
            if (!("new".equals( config.getString( "state" ) ))) {
                HttpUtilities.respond( response, "error",
                        "Job already started; cannot load more data" );
                return;
            }

            loadDataAndPrepareJob( (RefineRequest)request, response, parameters, job, config );
        }
        catch (JSONException e) {
            throw new ServletException( e );
        }
        finally {
            job.touch();
            job.updating = false;
        }
    }


    // based on
    // com.google.refine.importing.ImportingUtilities.loadDataAndPrepareJob(HttpServletRequest,
    // HttpServletResponse, Properties, ImportingJob, JSONObject)
    private void loadDataAndPrepareJob( RefineRequest request, HttpServletResponse response,
            Properties parameters, final ImportingJob job, JSONObject config )
                    throws IOException, ServletException {

        JSONObject retrievalRecord = new JSONObject();
        JSONUtilities.safePut( config, "retrievalRecord", retrievalRecord );
        JSONUtilities.safePut( config, "state", "loading-raw-data" );

        final JSONObject progress = new JSONObject();
        JSONUtilities.safePut( config, "progress", progress );
        try {
            retrieveContentFromRefineRequest( request, parameters, job.getRawDataDir(),
                    retrievalRecord, new Progress() {

                        @Override
                        public void setProgress( String message, int percent ) {
                            if (message != null) {
                                JSONUtilities.safePut( progress, "message", message );
                            }
                            JSONUtilities.safePut( progress, "percent", percent );
                        }


                        @Override
                        public boolean isCanceled() {
                            return job.canceled;
                        }
                    } );
        }
        catch (Exception e) {
            log.error( "Error uploading data", e );
            JSONUtilities.safePut( config, "state", "error" );
            JSONUtilities.safePut( config, "error", "Error uploading data" );
            JSONUtilities.safePut( config, "errorDetails", e.getLocalizedMessage() );
            return;
        }

        JSONArray fileSelectionIndexes = new JSONArray();
        JSONUtilities.safePut( config, "fileSelection", fileSelectionIndexes );

        String bestFormat = ImportingUtilities.autoSelectFiles( job, retrievalRecord,
                fileSelectionIndexes );
        bestFormat = ImportingUtilities.guessBetterFormat( job, bestFormat );

        JSONArray rankedFormats = new JSONArray();
        ImportingUtilities.rankFormats( job, bestFormat, rankedFormats );
        JSONUtilities.safePut( config, "rankedFormats", rankedFormats );

        JSONUtilities.safePut( config, "state", "ready" );
        JSONUtilities.safePut( config, "hasData", true );
        config.remove( "progress" );
    }


    // based on
    // com.google.refine.importing.ImportingUtilities.retrieveContentFromPostRequest(HttpServletRequest,
    // Properties, File, JSONObject, Progress)
    private void retrieveContentFromRefineRequest( RefineRequest request, Properties parameters,
            File rawDataDir, JSONObject retrievalRecord, final Progress progress )
                    throws Exception {
        JSONArray fileRecords = new JSONArray();
        JSONUtilities.safePut( retrievalRecord, "files", fileRecords );
        if (request.stream() == null && request.file() == null) {
            throw new IllegalStateException( "either stream or file must not be null" );
        }

        File targetFile = ImportingUtilities.allocateFile( rawDataDir,
                request.getParameter( "fileName" ) );

        InputStream origin = request.stream();

        if (request.file() != null) {
            Files.move( request.file(), targetFile );
        }

        JSONObject fileRecord = new JSONObject();
        JSONUtilities.safePut( fileRecord, "origin", "upload" );
        JSONUtilities.safePut( fileRecord, "declaredEncoding", request.getCharacterEncoding() );
        JSONUtilities.safePut( fileRecord, "declaredMimeType", request.getContentType() );
        JSONUtilities.safePut( fileRecord, "fileName", targetFile.getName() );
        JSONUtilities.safePut( fileRecord, "location",
                ImportingUtilities.getRelativePath( targetFile, rawDataDir ) );
                //
                // progress.setProgress(
                // "Saving file " + file.getName() + " locally (" +
                // NumberFormat.getIntegerInstance().format( fileSize ) + " bytes)",
                // calculateProgressPercent( update.totalExpectedSize,
                // update.totalRetrievedSize ) );

        // if base was a file simply use them, otherwise copy the stream into the new
        // targetFile
        JSONUtilities.safePut( fileRecord, "size", (request.file() != null) ? targetFile.length()
                : ImportingUtilities.saveStreamToFile( origin, targetFile, null ) );
        ImportingUtilities.postProcessRetrievedFile( rawDataDir, targetFile, fileRecord,
                fileRecords, progress );

        JSONUtilities.safePut( retrievalRecord, "uploadCount", 1 );
        JSONUtilities.safePut( retrievalRecord, "downloadCount", 0 );
        JSONUtilities.safePut( retrievalRecord, "clipboardCount", 0 );
        JSONUtilities.safePut( retrievalRecord, "archiveCount", 0 );
    }
    //
    // private void doUpdateFileSelection(HttpServletRequest request,
    // HttpServletResponse response, Properties parameters)
    // throws ServletException, IOException {
    //
    // long jobID = Long.parseLong(parameters.getProperty("jobID"));
    // ImportingJob job = ImportingManager.getJob(jobID);
    // if (job == null) {
    // HttpUtilities.respond(response, "error", "No such import job");
    // return;
    // }
    //
    // job.updating = true;
    // try {
    // JSONObject config = job.getOrCreateDefaultConfig();
    // if (!("ready".equals(config.getString("state")))) {
    // HttpUtilities.respond(response, "error", "Job not ready");
    // return;
    // }
    //
    // JSONArray fileSelectionArray = ParsingUtilities.evaluateJsonStringToArray(
    // request.getParameter("fileSelection"));
    //
    // ImportingUtilities.updateJobWithNewFileSelection(job, fileSelectionArray);
    //
    // replyWithJobData(request, response, job);
    // } catch (JSONException e) {
    // throw new ServletException(e);
    // } finally {
    // job.touch();
    // job.updating = false;
    // }
    // }
    //
    // private void doUpdateFormatAndOptions(HttpServletRequest request,
    // HttpServletResponse response, Properties parameters)
    // throws ServletException, IOException {
    //
    // long jobID = Long.parseLong(parameters.getProperty("jobID"));
    // ImportingJob job = ImportingManager.getJob(jobID);
    // if (job == null) {
    // HttpUtilities.respond(response, "error", "No such import job");
    // return;
    // }
    //
    // job.updating = true;
    // try {
    // JSONObject config = job.getOrCreateDefaultConfig();
    // if (!("ready".equals(config.getString("state")))) {
    // HttpUtilities.respond(response, "error", "Job not ready");
    // return;
    // }
    //
    // String format = request.getParameter("format");
    // JSONObject optionObj = ParsingUtilities.evaluateJsonStringToObject(
    // request.getParameter("options"));
    //
    // List<Exception> exceptions = new LinkedList<Exception>();
    //
    // ImportingUtilities.previewParse(job, format, optionObj, exceptions);
    //
    // Writer w = response.getWriter();
    // JSONWriter writer = new JSONWriter(w);
    // try {
    // writer.object();
    // if (exceptions.size() == 0) {
    // job.project.update(); // update all internal models, indexes, caches, etc.
    //
    // writer.key("status"); writer.value("ok");
    // } else {
    // writer.key("status"); writer.value("error");
    // writer.key("errors");
    // writer.array();
    // writeErrors(writer, exceptions);
    // writer.endArray();
    // }
    // writer.endObject();
    // } catch (JSONException e) {
    // throw new ServletException(e);
    // } finally {
    // w.flush();
    // w.close();
    // }
    // } catch (JSONException e) {
    // throw new ServletException(e);
    // } finally {
    // job.touch();
    // job.updating = false;
    // }
    // }
    //
    // private void doInitializeParserUI(HttpServletRequest request,
    // HttpServletResponse response, Properties parameters)
    // throws ServletException, IOException {
    //
    // long jobID = Long.parseLong(parameters.getProperty("jobID"));
    // ImportingJob job = ImportingManager.getJob(jobID);
    // if (job == null) {
    // HttpUtilities.respond(response, "error", "No such import job");
    // return;
    // }
    //
    // String format = request.getParameter("format");
    // Format formatRecord = ImportingManager.formatToRecord.get(format);
    // if (formatRecord != null && formatRecord.parser != null) {
    // JSONObject options = formatRecord.parser.createParserUIInitializationData(
    // job, job.getSelectedFileRecords(), format);
    // JSONObject result = new JSONObject();
    // JSONUtilities.safePut(result, "status", "ok");
    // JSONUtilities.safePut(result, "options", options);
    //
    // HttpUtilities.respond(response, result.toString());
    // } else {
    // HttpUtilities.respond(response, "error", "Unrecognized format or format has no
    // parser");
    // }
    // }
    //
    // private void doCreateProject(HttpServletRequest request, HttpServletResponse
    // response, Properties parameters)
    // throws ServletException, IOException {
    //
    // long jobID = Long.parseLong(parameters.getProperty("jobID"));
    // ImportingJob job = ImportingManager.getJob(jobID);
    // if (job == null) {
    // HttpUtilities.respond(response, "error", "No such import job");
    // return;
    // }
    //
    // job.updating = true;
    // job.touch();
    // try {
    // JSONObject config = job.getOrCreateDefaultConfig();
    // if (!("ready".equals(config.getString("state")))) {
    // HttpUtilities.respond(response, "error", "Job not ready");
    // return;
    // }
    //
    // String format = request.getParameter("format");
    // JSONObject optionObj = ParsingUtilities.evaluateJsonStringToObject(
    // request.getParameter("options"));
    //
    // List<Exception> exceptions = new LinkedList<Exception>();
    //
    // ImportingUtilities.createProject(job, format, optionObj, exceptions, false);
    //
    // HttpUtilities.respond(response, "ok", "done");
    // } catch (JSONException e) {
    // throw new ServletException(e);
    // }
    // }
    //
    // private void replyWithJobData(HttpServletRequest request, HttpServletResponse
    // response, ImportingJob job)
    // throws ServletException, IOException {
    //
    // Writer w = response.getWriter();
    // JSONWriter writer = new JSONWriter(w);
    // try {
    // writer.object();
    // writer.key("code"); writer.value("ok");
    // writer.key("job"); job.write(writer, new Properties());
    // writer.endObject();
    // } catch (JSONException e) {
    // throw new ServletException(e);
    // } finally {
    // w.flush();
    // w.close();
    // }
    // }
    //
    // static public void writeErrors(JSONWriter writer, List<Exception> exceptions)
    // throws JSONException {
    // for (Exception e : exceptions) {
    // StringWriter sw = new StringWriter();
    // e.printStackTrace(new PrintWriter(sw));
    //
    // writer.object();
    // writer.key("message");
    // writer.value(e.getLocalizedMessage());
    // writer.key("stack");
    // writer.value(sw.toString());
    // writer.endObject();
    // }
    // }
    //
    // static public JSONArray convertErrorsToJsonArray(List<Exception> exceptions) {
    // JSONArray a = new JSONArray();
    // for (Exception e : exceptions) {
    // StringWriter sw = new StringWriter();
    // e.printStackTrace(new PrintWriter(sw));
    //
    // JSONObject o = new JSONObject();
    // JSONUtilities.safePut(o, "message", e.getLocalizedMessage());
    // JSONUtilities.safePut(o, "stack", sw.toString());
    // JSONUtilities.append(a, o);
    // }
    // return a;
    // }

}
