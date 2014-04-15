/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.zip.GZIPOutputStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PolymapServiceController {

    protected static CommandLineArg<String>  exe = new CommandLineArg( "exe", null, "The executable to start/stop." );
    protected static CommandLineArg<String>  serviceName = new CommandLineArg( "serviceName", null, "The name of the service. Used just for logging and console." );
    protected static CommandLineArg<String>  user = new CommandLineArg( "user", null, "The name of the user to run this service for." );
    protected static CommandLineArg<String>  logFile = new CommandLineArg( "log", null, "The log file. The path can be absolute or relative." );
    protected static CommandLineArg<Integer> logFilesMaxSizeMB = new CommandLineArg( "logMaxSizeMB", 3, "(default: 3 (MB))" );
    protected static CommandLineArg<Integer> logFilesMaxCount = new CommandLineArg( "logMaxCount", 3, "The maximum number of compressed log files to keep. (default: 3)" );
    protected static CommandLineArg<Integer> debug = new CommandLineArg( "debug", 0, "Enable debugging. Values: 0 or 1. (default: 0 (off))" );    
    protected static CommandLineArg<String>  command = new SingleCommandLineArg( "<command>", new String [] {"start","stop","restart","status"}, "The command to execute: start|stop|restart|status" );
    
    protected static CommandLineArg[] allArgs = {exe, serviceName, user, debug, logFile, logFilesMaxSizeMB, logFilesMaxCount, command};    
    
    
    /**
     * Main
     */
    public static void main( String[] args ) {
        // parse command line
        int i = 0;
        nextArg: while (i < args.length) {
            for (CommandLineArg cla : allArgs) {
                int nextIndex = cla.parse( args, i );
                if (nextIndex > i) {
                    i = nextIndex;
                    continue nextArg;
                }
            }
            // nothing found
            printUsage( "Unknown argument: " + args[i] );
            System.exit( -1 );
        }
        // exe given?
        if (exe.value == null) {
            printUsage( "No executable specified (-" + exe.name + ")." );
            System.exit( -1 );
        }
        // serviceName given?
        if (serviceName.value == null) {
            serviceName.value = new File( exe.value ).getName();
        }
        // logFile
        if (logFile.value == null) {
            logFile.value = "/var/log/" + serviceName.value + ".log";
        }

        //
        int exitCode = 0;
        if ("start".equals( command.value )) {
            exitCode = start();
        }
        else if ("stop".equals( command.value )) {
            exitCode = stop();
        }
//        else if ("restart".equals( command.value )) {
//            stop();
//            start();
//        }
        else if ("status".equals( command.value )) {
            String pid = processPid();
            if (pid != null) {
                System.out.println( serviceName.value + " is up and running. (pid: " + pid + ")" );
            }
            else {
                System.out.println( serviceName.value + " is not running." );
                exitCode = 3;
            }
            // Status has a slightly different for the status command:
            // 0 - service running
            // 1 - service dead, but /var/run/  pid  file exists
            // 2 - service dead, but /var/lock/ lock file exists
            // 3 - service not running
        }
        else {
            printUsage( "Unknown command: " + command.value );
            exitCode = -1;
        }
        System.exit( exitCode );            
    }
    
    
    protected static int start() {
        String pid = processPid();
        if (pid != null) {
            System.out.println( serviceName.value + " is running already. (pid: " + pid + ")" );
            return -1;
        }
        try {
            ProcessBuilder pb = user.value != null 
                    ? new ProcessBuilder( "su", "-", user.value, "-c", exe.value ) 
                    : new ProcessBuilder( "setsid", exe.value );
            log( "execute: ", pb.command() );
            pb.redirectOutput( checkLogFile() );
            pb.redirectErrorStream();
            Process proc = pb.start();
            System.out.println( serviceName.value + " started." );
            return 0;
        }
        catch (IOException e) {
            System.out.println( "Unable to start service." );
            log( e );
            return -1;
        }
    }
    
    
    protected static int stop() {
        String pid = processPid();
        if (pid == null) {
            System.out.println( serviceName.value + " is not running." );
            return -1;
        }
        try {
            String cmd = "kill -- -" + pid; 
            log( "execute: ", cmd );
            Process proc = Runtime.getRuntime().exec( cmd );
            System.out.println( serviceName.value + " stopped." );
            return 0;
        }
        catch (IOException e) {
            System.out.println( "Unable to start service." );
            log( e );
            return -1;
        }
    }
    
    
    protected static File checkLogFile() {
        final File log = new File( logFile.value );
        
        if (log.exists()) {
            // rotate log file
            InputStream in = null;
            OutputStream out = null;
            try {
                // find name for gzipped file
                SimpleDateFormat df = new SimpleDateFormat( "yyyyMMdd" );
                Date now = new Date();
                File gzipf = new File( log.getAbsolutePath() + "." + df.format( now ) + ".gz" );
                for (int i=1; gzipf.exists(); i++) {
                    gzipf = new File( log.getAbsolutePath() + "." + df.format( now ) + "-" + i + ".gz" );                    
                }
                log( "rotate: " + gzipf.getAbsolutePath() );
                // gzip log file
                in = new FileInputStream( log );
                out = new GZIPOutputStream( new FileOutputStream( gzipf ) );
                byte[] buf = new byte[1024*4];
                for (int len; (len = in.read( buf )) != -1; ) {
                    out.write( buf, 0, len );
                }
                out.flush();
                log.delete();                
            } 
            catch (IOException e) {
                System.out.println( "Unable to rotate log file." );
                log( e );
            }
            finally {
                try { in.close(); out.close(); } catch (Exception e) { log( e ); }
            }
            
            // eviction
            long allSize = 0;
            TreeMap<Long,File> sorted = new TreeMap();
            for (File f : log.getParentFile().listFiles()) {
                if (f.getName().startsWith( log.getName() )) {
                    sorted.put( f.lastModified(), f );
                    allSize += f.length();
                }
            };
            // delete oldest first
            log( "logFiles: current count=" + sorted.size() + ", sorted=" + sorted );
            log( "logFiles: current log files size = " + allSize );
            for (Iterator<File> it=sorted.values().iterator(); it.hasNext(); ) {
                File f = it.next();
                if (sorted.size() > logFilesMaxCount.value
                        || allSize > (logFilesMaxSizeMB.value*1024*1024)) {
                    allSize -= f.length();
                    f.delete();
                    it.remove();
                }
                else {
                    break;
                }
            }
        }
        return log;
    }

    
    protected static String processPid() {
        try {
            //String cmds[] = {"cmd", "/c", "tasklist"};
            String cmd = "ps ax -o pid,command";
            log( "execute:", cmd );
            Process proc = Runtime.getRuntime().exec( cmd );
            BufferedReader in = new BufferedReader( new InputStreamReader( proc.getInputStream() ) );
            String line;
            while ((line = in.readLine()) != null) {
                StringTokenizer tokens = new StringTokenizer( line, " " );
                String pid = tokens.nextToken();
                String pidCmd = tokens.nextToken();
                // FIXME
                if (pidCmd.equalsIgnoreCase( "/bin/sh" )) {
                    pidCmd = tokens.nextToken();
                }
                //log( "line:", "pid="+pid, "cmd="+pidCmd );
                if (pidCmd.startsWith( exe.value )) {
                    return pid;
                }
            }
            return null;
        }
        catch (IOException e) {
            System.out.println( "Unable to get process info." );
            log( e );
            System.exit( -1 );
            return null;
        }
    }
    
    
    private static void printUsage( String msg ) {
        System.out.println( msg );
        System.out.println( "Usage: java -jar PolymapServiceController.jar <options> [start|stop|restart|status]" );
        System.out.println( "Where possible options are:" );
        for (CommandLineArg arg : allArgs) {
            System.out.print( "    -" + arg.name ); 
            if (arg.name.length() < 8) {
                System.out.print( "\t\t" ); 
            }
            else if (arg.name.length() < 16) {
                System.out.print( "\t" ); 
            }
            else {
                System.out.print( "\n\t\t" ); 
            }
            System.out.println( arg.description ); 
        }
    }
    
    
    private static void log( Object... args ) {
        if (debug.value > 0) {
            for (Object arg : args) {
                if (arg instanceof Exception) {
                    ((Exception)arg).printStackTrace();
                }
                else {
                    System.out.print( arg.toString() );                    
                    System.out.print( " " );                    
                }
            }
            System.out.println( "" );                    
        }
    }


    /**
     * 
     */
    protected static class CommandLineArg<T> {
        protected String    name;
        protected String    description;
        protected T         value;
        
        public CommandLineArg( String name, T defaultValue, String description ) {
            this.name = name;
            this.description = description;
            this.value = defaultValue;
        }
        
        public int parse( String[] args, int i ) {
            if (args[i].equalsIgnoreCase( "-" + name )) {
                try { // try Integer
                    value = (T)new Integer( args[i+1] );
                    log( "arg: " + args[i] + " = " + value.toString() );
                    return i + 2;
                }
                catch (Exception e) { }
                try { // other is String
                    value = (T)args[i+1];
                    log( "arg: " + args[i] + " = " + value );
                    return i + 2;
                }
                catch (Exception e) { }
            }
            return -1;
        }
    }


    /**
     * 
     */
    protected static class SingleCommandLineArg
            extends CommandLineArg<String> {
        protected String[]  allowed;
    
        public SingleCommandLineArg( String name, String[] allowed, String description ) {
            super( name, null, description );
            this.allowed = allowed;
        }
        
        @Override
        public int parse( String[] args, int i ) {
            for (String candidate : allowed) {
                if (args[i].equalsIgnoreCase( candidate )) {
                    this.value = candidate;
                    log( "command:", this.value);
                    return i + 1;
                }
            }
            return -1;
        }
    }
    
}
