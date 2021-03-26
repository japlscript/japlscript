package com.tagtraum.japlscript;

import com.tagtraum.japlscript.types.ReferenceImpl;
import com.tagtraum.japlscript.types.TypeClass;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.*;

import static org.junit.Assert.*;

/**
 * Test {@link JaplScript}.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestJaplScript {

    @Test
    public void testAddRemoveGlobalAspects() {
        final Aspect aspect = new Aspect() {
            @Override
            public String before(final String application, final String body) {
                return null;
            }

            @Override
            public String after(final String application, final String body) {
                return null;
            }
        };
        JaplScript.addGlobalAspect(aspect);
        assertTrue(JaplScript.getGlobalAspects().contains(aspect));
        JaplScript.removeGlobalAspect(aspect);
        assertFalse(JaplScript.getGlobalAspects().contains(aspect));
    }

    @Test
    public void testStartSession() throws ExecutionException, InterruptedException, TimeoutException {
        final Session session = JaplScript.startSession();
        // always return same session when called from the same thread
        assertSame(session, JaplScript.startSession());
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            final Session otherThreadSession = executorService.submit(new Callable<Session>() {
                @Override
                public Session call() throws Exception {
                    return JaplScript.startSession();
                }
            }).get(5L, TimeUnit.SECONDS);
            assertNotEquals(session, otherThreadSession);
        } finally {
            executorService.shutdown();
        }
    }

    @Test
    public void testGetApplication() {
        final Application app = JaplScript.getApplication(Application.class, "MyApp");
        assertEquals("application \"MyApp\"", app.getApplicationReference());
        assertNull(app.getObjectReference());
    }

    /**
     * Test application.
     */
    @com.tagtraum.japlscript.Code("capp")
    @com.tagtraum.japlscript.Name("application")
    public interface Application extends Reference {
        TypeClass CLASS = TypeClass.getInstance("application", "\u00abclass capp\u00bb", null, null);
    }

    @Test
    public void testCastList() {
        final String[] result = JaplScript.cast(String[].class, new ReferenceImpl("{\"hallo\"}", null));
        assertArrayEquals(new String[]{"hallo"}, result);
    }

    @Test
    public void testCastBoolean() {
        final Boolean trueResult = JaplScript.cast(Boolean.TYPE, new ReferenceImpl("true", null));
        assertEquals(Boolean.TRUE, trueResult);
        final Boolean falseResult = JaplScript.cast(Boolean.TYPE, new ReferenceImpl("false", null));
        assertEquals(Boolean.FALSE, falseResult);
    }

    @Test
    public void testCastInteger() {
        final Integer result = JaplScript.cast(Integer.TYPE, new ReferenceImpl("-123", null));
        assertEquals(Integer.valueOf(-123), result);
    }

    @Test
    public void testCastLong() {
        final Long result = JaplScript.cast(Long.TYPE, new ReferenceImpl("-123", null));
        assertEquals(Long.valueOf(-123), result);
    }

    @Test
    public void testCastFloat() {
        final Float result = JaplScript.cast(Float.TYPE, new ReferenceImpl("-123.123", null));
        assertEquals(Float.valueOf(-123.123f), result);
    }

    @Test
    public void testCastDouble() {
        final Double result = JaplScript.cast(Double.TYPE, new ReferenceImpl("-123.123", null));
        assertEquals(Double.valueOf(-123.123), result);
    }

    @Test
    public void testCastDate() throws ParseException {
        final Date result = JaplScript.cast(Date.class, new ReferenceImpl("\"Friday, April 8, 2016 at 1:04:56 AM\"", null));
        assertNotNull(result);
        final String dateString = "1999-05-28T11:25:27Z";
        final Date date = JaplScript.cast(Date.class, new ReferenceImpl(dateString, null));
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertEquals(date, format.parse(dateString));
    }

    @Test
    public void testSimpleQuote() {
        String quotedHallo = JaplScript.quote("Hallo");
        assertEquals("(\"Hallo\")", quotedHallo);
    }

    @Test
    public void testQuoteQuote() {
        String quotedHallo = JaplScript.quote("\"Hallo\"");
        assertEquals("(\"\\\"Hallo\\\"\")", quotedHallo);
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
