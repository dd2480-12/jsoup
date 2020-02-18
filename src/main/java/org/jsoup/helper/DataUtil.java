package org.jsoup.helper;

import org.jsoup.UncheckedIOException;
import org.jsoup.internal.ConstrainableInputStream;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.XmlDeclaration;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Internal static utilities for handling data.
 *
 */
public final class DataUtil {
    private static final Pattern charsetPattern = Pattern.compile("(?i)\\bcharset=\\s*(?:[\"'])?([^\\s,;\"']*)");
    static final String defaultCharset = "UTF-8"; // used if not found in header or meta charset
    private static final int firstReadBufferSize = 1024 * 5;
    static final int bufferSize = 1024 * 32;
    private static final char[] mimeBoundaryChars =
            "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    static final int boundaryLength = 32;

    private DataUtil() {}

    /**
     * Loads a file to a Document.
     * @param in file to load
     * @param charsetName character set of input
     * @param baseUri base URI of document, to resolve relative links against
     * @return Document
     * @throws IOException on IO error
     */
    public static Document load(File in, String charsetName, String baseUri) throws IOException {
        return parseInputStream(new FileInputStream(in), charsetName, baseUri, Parser.htmlParser());
    }

    /**
     * Parses a Document from an input steam.
     * @param in input stream to parse. You will need to close it.
     * @param charsetName character set of input
     * @param baseUri base URI of document, to resolve relative links against
     * @return Document
     * @throws IOException on IO error
     */
    public static Document load(InputStream in, String charsetName, String baseUri) throws IOException {
        return parseInputStream(in, charsetName, baseUri, Parser.htmlParser());
    }

    /**
     * Parses a Document from an input steam, using the provided Parser.
     * @param in input stream to parse. You will need to close it.
     * @param charsetName character set of input
     * @param baseUri base URI of document, to resolve relative links against
     * @param parser alternate {@link Parser#xmlParser() parser} to use.
     * @return Document
     * @throws IOException on IO error
     */
    public static Document load(InputStream in, String charsetName, String baseUri, Parser parser) throws IOException {
        return parseInputStream(in, charsetName, baseUri, parser);
    }

    /**
     * Writes the input stream to the output stream. Doesn't close them.
     * @param in input stream to read from
     * @param out output stream to write to
     * @throws IOException on IO error
     */
    static void crossStreams(final InputStream in, final OutputStream out) throws IOException {
        final byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
    }
    
    public static class parseInputStream_test{
        static boolean [] branch_num = new boolean [18];
        static double result ;
        
        public static void coverage() {
            double cover =0;
            for(int i=0;i<branch_num.length;i++) {
                if (branch_num[i]) {
                    cover +=1;
                }
            }
            result = cover /branch_num.length;
        }
        
        public static void print_branches() {
            for(int i = 0; i < branch_num.length; i++) {
                System.out.println("ID " + (i + 1) + " covered? " + branch_num[i]);
            }
        }
        public static void print_coverage() {
            coverage();
            System.out.println("The percentage of branch coverage is "+result);
        }
    }

    static Document parseInputStream(InputStream input, String charsetName, String baseUri, Parser parser) throws IOException  {
        if (input == null) {// empty body
            parseInputStream_test.branch_num[0] = true;
            return new Document(baseUri);
        }
        input = ConstrainableInputStream.wrap(input, bufferSize, 0);

        Document doc = null;

        // read the start of the stream and look for a BOM or meta charset
        input.mark(bufferSize);
        ByteBuffer firstBytes = readToByteBuffer(input, firstReadBufferSize - 1); // -1 because we read one more to see if completed. First read is < buffer size, so can't be invalid.
        boolean fullyRead = (input.read() == -1);
        input.reset();

        // look for BOM - overrides any other header or input
        BomCharset bomCharset = detectCharsetFromBom(firstBytes);
        if (bomCharset != null) {
            charsetName = bomCharset.charset;
            parseInputStream_test.branch_num[1] = true;
        }

        if (charsetName == null) { // determine from meta. safe first parse as UTF-8
            parseInputStream_test.branch_num[2] = true;
            String docData = Charset.forName(defaultCharset).decode(firstBytes).toString();
            try {
                doc = parser.parseInput(docData, baseUri);
            } catch (UncheckedIOException e) {
                throw e.ioException();
            }

            // look for <meta http-equiv="Content-Type" content="text/html;charset=gb2312"> or HTML5 <meta charset="gb2312">
            Elements metaElements = doc.select("meta[http-equiv=content-type], meta[charset]");
            String foundCharset = null; // if not found, will keep utf-8 as best attempt
            for (Element meta : metaElements) {
                if (meta.hasAttr("http-equiv")) {
                    foundCharset = getCharsetFromContentType(meta.attr("content"));
                    parseInputStream_test.branch_num[3] = true;
                }
                if (foundCharset == null && meta.hasAttr("charset")) {
                    foundCharset = meta.attr("charset");
                    parseInputStream_test.branch_num[4] = true;
                }
                if (foundCharset != null) {
                    parseInputStream_test.branch_num[5] = true;
                    break;
                }
            }

            // look for <?xml encoding='ISO-8859-1'?>
            if (foundCharset == null && doc.childNodeSize() > 0) {
                Node first = doc.childNode(0);
                XmlDeclaration decl = null;
                if (first instanceof XmlDeclaration) {
                    decl = (XmlDeclaration) first;
                    parseInputStream_test.branch_num[6] = true;
                }
                else if (first instanceof Comment) {
                    Comment comment = (Comment) first;
                    if (comment.isXmlDeclaration()) {
                        decl = comment.asXmlDeclaration();
                        parseInputStream_test.branch_num[7] = true;    
                    }else {
                        parseInputStream_test.branch_num[8] = true;

                    }
                }
                if (decl != null) {
                    if (decl.name().equalsIgnoreCase("xml")) {
                        foundCharset = decl.attr("encoding");
                        parseInputStream_test.branch_num[9] = true;
                    }else {
                        parseInputStream_test.branch_num[10] = true;
                    }
                }
            }
            foundCharset = validateCharset(foundCharset);
            if (foundCharset != null && !foundCharset.equalsIgnoreCase(defaultCharset)) { // need to re-decode. (case insensitive check here to match how validate works)
                parseInputStream_test.branch_num[11] = true;
                foundCharset = foundCharset.trim().replaceAll("[\"']", "");
                charsetName = foundCharset;
                doc = null;
            } else if (!fullyRead) {
                parseInputStream_test.branch_num[12] = true;
                doc = null;
            }
        } else { // specified by content type header (or by user on file load)
            parseInputStream_test.branch_num[13] = true;
            Validate.notEmpty(charsetName, "Must set charset arg to character set of file to parse. Set to null to attempt to detect from HTML");
        }
        if (doc == null) {
            if (charsetName == null) {
                charsetName = defaultCharset;
                parseInputStream_test.branch_num[14] = true;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, charsetName), bufferSize);
            if (bomCharset != null && bomCharset.offset) { // creating the buffered reader ignores the input pos, so must skip here
                parseInputStream_test.branch_num[15] = true;
                long skipped = reader.skip(1);
                Validate.isTrue(skipped == 1); // WTF if this fails.
            }
            try {
                doc = parser.parseInput(reader, baseUri);
            } catch (UncheckedIOException e) {
                // io exception when parsing (not seen before because reading the stream as we go)
                throw e.ioException();
            }
            Charset charset = Charset.forName(charsetName);
            doc.outputSettings().charset(charset);
            if (!charset.canEncode()) {
                parseInputStream_test.branch_num[16] = true;
                // some charsets can read but not encode; switch to an encodable charset and update the meta el
                doc.charset(Charset.forName(defaultCharset));
            }
        }else {
            parseInputStream_test.branch_num[17] = true;
        }
        input.close();
        parseInputStream_test.print_coverage();
        return doc;
    }

    /**
     * Read the input stream into a byte buffer. To deal with slow input streams, you may interrupt the thread this
     * method is executing on. The data read until being interrupted will be available.
     * @param inStream the input stream to read from
     * @param maxSize the maximum size in bytes to read from the stream. Set to 0 to be unlimited.
     * @return the filled byte buffer
     * @throws IOException if an exception occurs whilst reading from the input stream.
     */
    public static ByteBuffer readToByteBuffer(InputStream inStream, int maxSize) throws IOException {
        Validate.isTrue(maxSize >= 0, "maxSize must be 0 (unlimited) or larger");
        final ConstrainableInputStream input = ConstrainableInputStream.wrap(inStream, bufferSize, maxSize);
        return input.readToByteBuffer(maxSize);
    }

    static ByteBuffer emptyByteBuffer() {
        return ByteBuffer.allocate(0);
    }

    /**
     * Parse out a charset from a content type header. If the charset is not supported, returns null (so the default
     * will kick in.)
     * @param contentType e.g. "text/html; charset=EUC-JP"
     * @return "EUC-JP", or null if not found. Charset is trimmed and uppercased.
     */
    static String getCharsetFromContentType(String contentType) {
        if (contentType == null) return null;
        Matcher m = charsetPattern.matcher(contentType);
        if (m.find()) {
            String charset = m.group(1).trim();
            charset = charset.replace("charset=", "");
            return validateCharset(charset);
        }
        return null;
    }

    private static String validateCharset(String cs) {
        if (cs == null || cs.length() == 0) return null;
        cs = cs.trim().replaceAll("[\"']", "");
        try {
            if (Charset.isSupported(cs)) return cs;
            cs = cs.toUpperCase(Locale.ENGLISH);
            if (Charset.isSupported(cs)) return cs;
        } catch (IllegalCharsetNameException e) {
            // if our this charset matching fails.... we just take the default
        }
        return null;
    }

    /**
     * Creates a random string, suitable for use as a mime boundary
     */
    static String mimeBoundary() {
        final StringBuilder mime = StringUtil.borrowBuilder();
        final Random rand = new Random();
        for (int i = 0; i < boundaryLength; i++) {
            mime.append(mimeBoundaryChars[rand.nextInt(mimeBoundaryChars.length)]);
        }
        return StringUtil.releaseBuilder(mime);
    }
    
    public static class DetectCharsetFromBom_test {
    	static boolean entered_branch[] = new boolean[5];

    	public static void print_to_file() {
    		String s = print();
    		double d = coverage();
    		try {
    			PrintWriter out = new PrintWriter("DetectCharsetFromBomTest.txt");
    			out.println(s + "\n" + "Coverage\n" + d);
    			out.flush();
    			out.close();
    		}
    		catch(Exception e) {

    		}
    	}

    	public static double coverage() {
    		double cover = 0;
    		for(int i = 0; i < entered_branch.length; i++) {
    			if(entered_branch[i]) {
    				cover++;
    			}
    		}
    		return cover / entered_branch.length;
    	}
    	public static String print() {
    		StringBuilder s = new StringBuilder();
    		s.append("Entered Branches\n");
    		for(int i = 0; i < entered_branch.length; i++) {
    			if(entered_branch[i]) {
    				s.append((i + 1) + " ");
    			}
    		}
    		s.append("\n");
    		return s.toString();
    	}
    }

    private static BomCharset detectCharsetFromBom(final ByteBuffer byteData) {
        final Buffer buffer = byteData; // .mark and rewind used to return Buffer, now ByteBuffer, so cast for backward compat
        buffer.mark();
        byte[] bom = new byte[4];
        if (byteData.remaining() >= bom.length) { //ID 1
        	DetectCharsetFromBom_test.entered_branch[0] = true;
            byteData.get(bom);
            buffer.rewind();
        }
        if (bom[0] == 0x00 && bom[1] == 0x00 && bom[2] == (byte) 0xFE && bom[3] == (byte) 0xFF || // BE //ID 2
            bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE && bom[2] == 0x00 && bom[3] == 0x00) { // LE
        	DetectCharsetFromBom_test.entered_branch[1] = true;
        	DetectCharsetFromBom_test.print_to_file();
            return new BomCharset("UTF-32", false); // and I hope it's on your system
        } else if (bom[0] == (byte) 0xFE && bom[1] == (byte) 0xFF || // BE //ID 3
            bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE) {
        	DetectCharsetFromBom_test.entered_branch[2] = true;
        	DetectCharsetFromBom_test.print_to_file();
            return new BomCharset("UTF-16", false); // in all Javas
        } else if (bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF) { //ID 4
        	DetectCharsetFromBom_test.entered_branch[3] = true;
        	DetectCharsetFromBom_test.print_to_file();
            return new BomCharset("UTF-8", true); // in all Javas
            // 16 and 32 decoders consume the BOM to determine be/le; utf-8 should be consumed here
        } //ID 5
        DetectCharsetFromBom_test.entered_branch[4] = true;
        DetectCharsetFromBom_test.print_to_file();
        return null;
    }

    private static class BomCharset {
        private final String charset;
        private final boolean offset;

        public BomCharset(String charset, boolean offset) {
            this.charset = charset;
            this.offset = offset;
        }
    }
}
