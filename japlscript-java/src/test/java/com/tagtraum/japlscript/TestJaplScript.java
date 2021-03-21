package com.tagtraum.japlscript;

import com.tagtraum.japlscript.types.ReferenceImpl;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * TestJaplScript.
 * <p/>
 * Date: Feb 28, 2006
 * Time: 10:24:52 AM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestJaplScript {

	@Test
    public void testSimpleQuote() {
        String quotedHallo = JaplScript.quote("Hallo");
        assertEquals("(\"Hallo\")", quotedHallo);
    }

	@Test
	public void testUnicodeQuote() {
        final String s = "A\u00afB";
        System.out.println(s);
        String quotedHallo = JaplScript.quote(s);
        System.out.println(quotedHallo);
        assertEquals("(\"A\" & (\u00abdata utf8c2af\u00bb as Unicode text) & \"B\")", quotedHallo);
    }

	@Test
	public void testUnicodeQuote2() {
        final String s = "A\ubaa0B";
        System.out.println(s);
        String quotedHallo = JaplScript.quote(s);
        System.out.println(quotedHallo);
        assertEquals("(\"A\" & (\u00abdata utf8ebaaa0\u00bb as Unicode text) & \"B\")", quotedHallo);
    }

	@Test
	public void testUnicodeExtQuote() {
        final String s = "A\u00af\u00afB";
        System.out.println(s);
        String quotedHallo = JaplScript.quote(s);
        System.out.println(quotedHallo);
        assertEquals("(\"A\" & (\u00abdata utf8c2afc2af\u00bb as Unicode text) & \"B\")", quotedHallo);
    }

	@Test
	public void testTrailingUnicodeQuote() {
        final String s = "AB\u00af\u00af";
        System.out.println(s);
        String quotedHallo = JaplScript.quote(s);
        System.out.println(quotedHallo);
        assertEquals("(\"AB\" & (\u00abdata utf8c2afc2af\u00bb as Unicode text))", quotedHallo);
    }

    @Test
    public void testGetDate() throws IOException {
        final ScriptExecutor scriptExecutor = ScriptExecutor.newInstance();
        scriptExecutor.setScript("tell application \"Finder\"\n" +
                "\treturn current date\n" +
                "end tell");
        final String result = scriptExecutor.execute();
        final Date date = JaplScript.cast(Date.class, new ReferenceImpl(result, null));
        System.out.println("current date: " + date);
        assertEquals(System.currentTimeMillis(), date.getTime(), 2000);
    }

    @Test
    public void testCreateDate() throws IOException, ParseException {
        final ObjectInvocationHandler handler = new ObjectInvocationHandler(new ReferenceImpl(null, "application \"iTunes\""));
        final Date date = handler.executeAppleScript(new ReferenceImpl(null, "application \"iTunes\""), "return my createDate(1955, 5, 2, 13, 15, 22)", Date.class);
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-M-d H:m:s");
        assertEquals(format.parse("1955-5-2 13:15:22"), date);
    }

}
