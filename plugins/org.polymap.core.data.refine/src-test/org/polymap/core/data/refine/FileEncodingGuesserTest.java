package org.polymap.core.data.refine;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

public class FileEncodingGuesserTest {

    @Test
    public void testEncoding() throws IOException {
        detect( "/data/example-utf8.tsv", Charsets.UTF_8.displayName() );
        detect( "/data/example-utf16.tsv", Charsets.UTF_16BE.displayName() );
        detect( "/data/example-utf16BE.tsv", Charsets.UTF_16BE.displayName() );
        detect( "/data/example-utf16LE.tsv", Charsets.UTF_16LE.displayName() );
        detect( "/data/example-latin1.tsv", Charsets.ISO_8859_1.displayName() );
        detect( "/data/example-latin15.tsv", Charsets.ISO_8859_1.displayName() );
        detect( "/data/example-latin15s.tsv", Charsets.ISO_8859_1.displayName() );
        // detect("/data/boris.csv");
    }


    private void detect( String filename, String charset ) throws IOException {
         detect( new File( this.getClass().getResource( filename ).getFile() ), charset );
    }


    private void detect(File file, String expectedCharset) throws IOException {
        byte[] fileContent = null;
        FileInputStream fin = null;

        //create FileInputStream object
        fin = new FileInputStream(file.getPath());

        /*
         * Create byte array large enough to hold the content of the file.
         * Use File.length to determine size of the file in bytes.
         */
        fileContent = new byte[(int) file.length()];

        /*
         * To read content of the file in byte array, use
         * int read(byte[] byteArray) method of java FileInputStream class.
         *
         */
        fin.read(fileContent);

        byte[] data =  fileContent;

        CharsetDetector detector = new CharsetDetector();
        detector.setText(data);

        CharsetMatch cm = detector.detect();

//        if (cm != null) {
            int confidence = cm.getConfidence();
            String name = cm.getName();
            assertEquals(expectedCharset, name);
            System.out.println("File: " + file.getName() + " - Encoding: " + cm.getName() + ":" + cm.getLanguage() + " - Confidence: " + confidence + "%");
//            if ("ISO-8859-1".equals(name)) {
//                "\u20ac".codePoints().forEach(a -> System.out.println(a));
////                for (int i=0;i<data.length;i++) {
////                    System.out.print(data[i]);
////                }
//                System.out.println("===");
//                cm.getString().codePoints().forEach(a -> System.out.println(a));
//            }
            //Here you have the encode name and the confidence
            //In my case if the confidence is > 50 I return the encode, else I return the default value
            //if (confidence > 50) {
            //}
//        }
    }


    public static void main( String[] args ) throws ParseException, UnsupportedEncodingException {
//        NumberFormat nf = NumberFormat.getInstance();
//        // nf.setMaximumIntegerDigits( 3 );
//        System.out.println( nf.parse( "123E3" ) );
//
//        ListMultimap<String, Locale> formats = ArrayListMultimap.create();
//        for (Locale locale : DecimalFormat.getAvailableLocales()) {
//            if (locale.getCountry().length() != 0) {
//                continue; // Skip sub-language locales
//            }
//            formats.put( ((DecimalFormat)NumberFormat.getInstance( locale )).toLocalizedPattern(), locale );
//        }
//        formats.asMap().forEach( (key, value) -> System.out.println(key + ": " + value) );
//        long start = System.currentTimeMillis();
//        int count = 0;
//        for (int i = 0; i< (1000000); i++) {
//            TypeGuesser.guess( "83,456,456.45-" );
//            count++;
//        }
//        System.out.println( "Needed " + (System.currentTimeMillis() - start) + "ms for " + count );
        
//        String s1 = new String( "a�" );
//        for (byte b : s1.getBytes()) {
//            System.out.println( UnicodeEscaper.hex( b ));
//        }
//        Pattern asciiOnly = Pattern.compile("\\p{ASCII}*");
//        List<String> strings = Lists.newArrayList( "a�", "abc", "a.b.c", "abc\n", "�" );
//        strings.forEach( str ->  System.out.println( str + " matches? " + asciiOnly.matcher( str ).matches() ));
        
        
        String str = "���";
        byte[] b = str.getBytes("utf-8");
        
        String str2 = new String(b, "utf-8");
        System.out.println( str2 );
        
    }
}
