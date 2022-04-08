package com.tagtraum.japlscript;

import com.tagtraum.japlscript.execution.Aspect;
import com.tagtraum.japlscript.execution.JaplScriptException;
import com.tagtraum.japlscript.execution.ScriptExecutor;
import com.tagtraum.japlscript.execution.Session;
import com.tagtraum.japlscript.language.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test {@link JaplScript}.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestJaplScript {

    @Test
    public void testAddRemoveType() {
        System.out.println("start testAddRemoveType");
        // remove all types first
        final List<Codec<?>> originalTypes = new ArrayList<>(JaplScript.getTypes());
        try {
            for (final Codec<?> t : JaplScript.getTypes()) {
                JaplScript.removeType(t);
            }
            assertTrue(JaplScript.getTypes().isEmpty());
            // now add something
            JaplScript.addType(Text.getInstance());
            assertEquals(1, JaplScript.getTypes().size());
            assertEquals(Text.getInstance(), JaplScript.getTypes().get(0));
        } finally {
            originalTypes.forEach(JaplScript::addType);
        }
        System.out.println("end testAddRemoveType");
    }

    @Test
    public void testAddRemoveGlobalAspects() {
        System.out.println("start testAddRemoveGlobalAspects");
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
        System.out.println("end testAddRemoveGlobalAspects");
    }

    @Test
    public void testStartSession() throws ExecutionException, InterruptedException, TimeoutException {
        System.out.println("start testStartSession");
        final Session session = JaplScript.startSession();
        // always return same session when called from the same thread
        assertSame(session, JaplScript.startSession());
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            final Session otherThreadSession = executorService.submit(JaplScript::startSession)
                .get(5L, TimeUnit.SECONDS);
            assertNotEquals(session, otherThreadSession);
        } finally {
            executorService.shutdown();
        }
        System.out.println("end testStartSession");
    }

    @Test
    public void testGetApplication() {
        System.out.println("start testGetApplication");
        final Application app = JaplScript.getApplication(Application.class, "MyApp");
        assertEquals("application \"MyApp\"", app.getApplicationReference());
        assertNull(app.getObjectReference());
        System.out.println("end testGetApplication");
    }

    @Test
    public void testGetScriptingAddition() {
        System.out.println("start testGetScriptingAddition");
        final Application app = JaplScript.getScriptingAddition(Application.class, "MyApp");
        assertEquals("scripting addition \"MyApp\"", app.getApplicationReference());
        assertNull(app.getObjectReference());
        System.out.println("end testGetScriptingAddition");
    }

    /**
     * Test application.
     */
    @Code("capp")
    @Name("application")
    public interface Application extends Reference {
        TypeClass CLASS = new TypeClass("application", "\u00abclass capp\u00bb", Application.class, null);
        Set<Class<?>> APPLICATION_CLASSES = Collections.singleton(Application.class);
    }

    @Test
    public void testCastNullReference() {
        System.out.println("start testCastNullReference");
        final String result = JaplScript.cast(String.class, null);
        assertNull(result);
        System.out.println("end testCastNullReference");
    }

    @Test
    public void testCastStringList() {
        System.out.println("start testCastStringList");
        final String[] result = JaplScript.cast(String[].class, new ReferenceImpl("{\"hallo\"}", null));
        assertArrayEquals(new String[]{"hallo"}, result);
        System.out.println("end testCastStringList");
    }

    @Test
    public void testCastDoubleList() {
        System.out.println("start testCastDoubleList");
        final double[] result = JaplScript.cast(new double[0].getClass(), new ReferenceImpl("{50.961045,6.956445}", null));
        assertArrayEquals(new double[]{50.961045, 6.956445}, result);
        System.out.println("end testCastDoubleList");
    }

    @Test
    public void testCastIntList() {
        System.out.println("start testCastIntList");
        final int[] result = JaplScript.cast(new int[0].getClass(), new ReferenceImpl("{50,6}", null));
        assertArrayEquals(new int[]{50, 6}, result);
        System.out.println("end testCastIntList");
    }

    @Test
    public void testCastBooleanList() {
        System.out.println("start testCastBooleanList");
        final boolean[] result = JaplScript.cast(new boolean[0].getClass(), new ReferenceImpl("{true,false}", null));
        assertArrayEquals(new boolean[]{true, false}, result);
        System.out.println("end testCastBooleanList");
    }

    @Test
    public void testCastRecord() {
        System.out.println("start testCastRecord");
        final Map<String, Object> result = JaplScript.cast(Map.class, new ReferenceImpl("{name:\"hendrik\", index:3, creation date:date \"Sunday, January 7, 2007 at 23:32:16\", icon:missing value}", null));
        assertEquals(new HashSet<>(Arrays.asList("name", "index", "creation date", "icon")), result.keySet());
        System.out.println("end testCastRecord");
    }

    @Test
    public void testCastNullRecord() {
        System.out.println("start testCastNullRecord");
        final Map<String, Object> result = JaplScript.cast(Map.class, new ReferenceImpl(null, "my app"));
        assertTrue(result.isEmpty());
        System.out.println("end testCastNullRecord");
    }

    @Test
    public void testCastEmptyString() {
        System.out.println("start testCastEmptyString");
        final Object result = JaplScript.cast(FileOutputStream.class, new ReferenceImpl("", "my app"));
        assertNull(result);
        System.out.println("end testCastEmptyString");
    }

    @Test
    public void testCastUnknownClass() {
        System.out.println("start testCastUnknownClass");
        Assertions.assertThrows(JaplScriptException.class, () -> {
            JaplScript.cast(FileOutputStream.class, new ReferenceImpl("dvd", "my app"));
        });
        System.out.println("end testCastUnknownClass");
    }

    @Test
    public void testCastBoolean() {
        System.out.println("start testCastBoolean");
        final java.lang.Boolean trueResult = JaplScript.cast(java.lang.Boolean.TYPE, new ReferenceImpl("true", null));
        assertEquals(java.lang.Boolean.TRUE, trueResult);
        final java.lang.Boolean falseResult = JaplScript.cast(java.lang.Boolean.TYPE, new ReferenceImpl("false", null));
        assertEquals(java.lang.Boolean.FALSE, falseResult);
        System.out.println("end testCastBoolean");
    }

    @Test
    public void testCastInteger() {
        System.out.println("start testCastInteger");
        final java.lang.Integer result = JaplScript.cast(java.lang.Integer.TYPE, new ReferenceImpl("-123", null));
        assertEquals(java.lang.Integer.valueOf(-123), result);
        System.out.println("end testCastInteger");
    }

    @Test
    public void testCastLong() {
        System.out.println("start testCastLong");
        final java.lang.Long result = JaplScript.cast(java.lang.Long.TYPE, new ReferenceImpl("-123", null));
        assertEquals(java.lang.Long.valueOf(-123), result);
        System.out.println("end testCastLong");
    }

    @Test
    public void testCastFloat() {
        System.out.println("start testCastFloat");
        final java.lang.Float result = JaplScript.cast(java.lang.Float.TYPE, new ReferenceImpl("-123.123", null));
        assertEquals(java.lang.Float.valueOf(-123.123f), result);
        System.out.println("end testCastFloat");
    }

    @Test
    public void testCastDouble() {
        System.out.println("start testCastDouble");
        final java.lang.Double result = JaplScript.cast(java.lang.Double.TYPE, new ReferenceImpl("-123.123", null));
        assertEquals(java.lang.Double.valueOf(-123.123), result);
        System.out.println("end testCastDouble");
    }

    @Test
    public void testCastDate() throws ParseException {
        System.out.println("start testCastDate");
        final java.util.Date result = JaplScript.cast(java.util.Date.class, new ReferenceImpl("\"Friday, April 8, 2016 at 1:04:56 AM\"", null));
        assertSame(java.util.Date.class, result.getClass());
        final String dateString = "1999-05-28T11:25:27Z";
        final java.util.Date date = JaplScript.cast(java.util.Date.class, new ReferenceImpl(dateString, null));
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertEquals(date, format.parse(dateString));
        System.out.println("end testCastDate");
    }

    @Test
    public void testCastBadDate() {
        System.out.println("start testCastBadDate");
        Assertions.assertThrows(JaplScriptException.class, () -> {
            JaplScript.cast(java.util.Date.class, new ReferenceImpl("\"XXX, XXX 8, 2016 at 1:04:56 AM\"", null));
        });
        System.out.println("end testCastBadDate");
    }

    @Test
    public void testCastDateNullObjectReference() {
        System.out.println("start testCastDateNullObjectReference");
        final java.util.Date result = JaplScript.cast(java.util.Date.class, new ReferenceImpl(null, null));
        assertNull(result);
        System.out.println("end testCastDateNullObjectReference");
    }

    @Test
    public void testCastPictureNullObjectReference() {
        System.out.println("start testCastPictureNullObjectReference");
        final Picture result = JaplScript.cast(Picture.class, new ReferenceImpl(null, null));
        assertNull(result);
        System.out.println("end testCastPictureNullObjectReference");
    }

    @Test
    public void testCastPicture() {
        System.out.println("start testCastPicture");
        final String finderApplicationReference = "application \"Finder\"";
        final Picture picture = JaplScript.cast(Picture.class, new ReferenceImpl("«data ABCDBBBB»", finderApplicationReference));
        assertSame(Picture.class, picture.getClass());
        final TypeClass expected = new TypeClass("«class ABCD»", "«class ABCD»", Application.class, null);
        assertEquals(expected, picture.getTypeClass());
        assertArrayEquals(new byte[]{-69, -69}, picture.getData());
        System.out.println("end testCastPicture");
    }

    @Test
    public void testCastTdtaNullObjectReference() {
        System.out.println("start testCastTdtaNullObjectReference");
        final Tdta result = JaplScript.cast(Tdta.class, new ReferenceImpl(null, null));
        assertNull(result);
        System.out.println("end testCastTdtaNullObjectReference");
    }

    @Test
    public void testCastTdta() {
        System.out.println("start testCastTdta");
        final String finderApplicationReference = "application \"Finder\"";
        final Tdta tdta = JaplScript.cast(Tdta.class, new ReferenceImpl("«data tdtaBBBB»", finderApplicationReference));
        assertSame(Tdta.class, tdta.getClass());
        final TypeClass expected = new TypeClass("«class tdta»", "«class tdta»", Application.class, null);
        assertEquals(expected, tdta.getTypeClass());
        assertArrayEquals(new byte[]{-69, -69}, tdta.getTdta());
        System.out.println("end testCastTdta");
    }

    @Test
    public void testCastJaplScriptFileNullObjectReference() {
        System.out.println("start testCastJaplScriptFileNullObjectReference");
        final JaplScriptFile result = JaplScript.cast(JaplScriptFile.class, new ReferenceImpl(null, null));
        assertNull(result);
        System.out.println("end testCastJaplScriptFileNullObjectReference");
    }

    @Test
    public void testCastJaplScriptFile() {
        System.out.println("start testCastJaplScriptFile");
        final String finderApplicationReference = "application \"Finder\"";
        final JaplScriptFile japlScriptFile = JaplScript.cast(JaplScriptFile.class, new ReferenceImpl("(POSIX file \"/Users\")", finderApplicationReference));
        assertSame(JaplScriptFile.class, japlScriptFile.getClass());
        final TypeClass expected = new TypeClass("«class furl»", "«class furl»", Application.class, null);
        assertEquals(expected, japlScriptFile.getTypeClass());
        assertEquals(Paths.get("/Users"), japlScriptFile.getPath());
        System.out.println("end testCastJaplScriptFile");
    }


    @Test
    public void testCastTypeClassNullObjectReference() {
        System.out.println("start testCastTypeClassNullObjectReference");
        final TypeClass result = JaplScript.cast(TypeClass.class, new ReferenceImpl(null, null));
        assertNull(result);
        System.out.println("end testCastTypeClassNullObjectReference");
    }

    @Test
    public void testCastStringArrayNullObjectReference() {
        System.out.println("start testCastStringArrayNullObjectReference");
        final String[] result = JaplScript.cast(String[].class, new ReferenceImpl(null, null));
        assertNull(result);
        System.out.println("end testCastStringArrayNullObjectReference");
    }

    @Test
    public void testCastIntArrayNullObjectReference() {
        System.out.println("start testCastIntArrayNullObjectReference");
        final int[] result = JaplScript.cast(int[].class, new ReferenceImpl(null, null));
        assertNull(result);
        System.out.println("end testCastIntArrayNullObjectReference");
    }

    @Test
    public void testCastIntArrayObjectReference() {
        System.out.println("start testCastIntArrayObjectReference");
        final int[] result = JaplScript.cast(int[].class, new ReferenceImpl("{1, 2, 3}", null));
        assertArrayEquals(new int[]{1, 2, 3}, result);
        System.out.println("end testCastIntArrayObjectReference");
    }

    @Test
    public void testCastShortArrayObjectReference() {
        System.out.println("start testCastShortArrayObjectReference");
        final short[] result = JaplScript.cast(short[].class, new ReferenceImpl("{1, 2, 3}", null));
        assertArrayEquals(new short[]{1, 2, 3}, result);
        System.out.println("end testCastShortArrayObjectReference");
    }

    @Test
    public void testCastLongArrayObjectReference() {
        System.out.println("start testCastLongArrayObjectReference");
        final long[] result = JaplScript.cast(long[].class, new ReferenceImpl("{1, 2}", null));
        assertArrayEquals(new long[]{1, 2}, result);
        System.out.println("end testCastLongArrayObjectReference");
    }

    @Test
    public void testCastFloatArrayObjectReference() {
        System.out.println("start testCastFloatArrayObjectReference");
        final float[] result = JaplScript.cast(float[].class, new ReferenceImpl("{1.3, 2.3}", null));
        assertArrayEquals(new float[]{1.3f, 2.3f}, result);
        System.out.println("end testCastFloatArrayObjectReference");
    }

    @Test
    public void testCastDoubleArrayObjectReference() {
        System.out.println("start testCastDoubleArrayObjectReference");
        final double[] result = JaplScript.cast(double[].class, new ReferenceImpl("{1.3, 2.3}", null));
        assertArrayEquals(new double[]{1.3, 2.3}, result);
        System.out.println("end testCastDoubleArrayObjectReference");
    }

    @Test
    public void testCastBooleanArrayObjectReference() {
        System.out.println("start testCastBooleanArrayObjectReference");
        final boolean[] result = JaplScript.cast(boolean[].class, new ReferenceImpl("{true, false}", null));
        assertArrayEquals(new boolean[]{true, false}, result);
        System.out.println("end testCastBooleanArrayObjectReference");
    }

    @Test
    public void testCastEmptyStringObjectReference() {
        System.out.println("start testCastEmptyStringObjectReference");
        final Color result = JaplScript.cast(Color.class, new ReferenceImpl("", null));
        assertNull(result);
        System.out.println("end testCastEmptyStringObjectReference");
    }

    @Test
    public void testCastDataNullObjectReference() {
        System.out.println("start testCastDataNullObjectReference");
        final Data result = JaplScript.cast(Data.class, new ReferenceImpl(null, null));
        assertNull(result);
        System.out.println("end testCastDataNullObjectReference");
    }

    @Test
    public void testCastData() {
        System.out.println("start testCastData");
        final String finderApplicationReference = "application \"Finder\"";
        final Data data = JaplScript.cast(Data.class, new ReferenceImpl("«data ABCDBBBB»", finderApplicationReference));
        assertSame(Data.class, data.getClass());
        final TypeClass expected = new TypeClass("«class ABCD»", "«class ABCD»", Application.class, null);
        assertEquals(expected, data.getTypeClass());
        assertArrayEquals(new byte[]{-85, -51, -69, -69}, data.getData());
        System.out.println("end testCastData");
    }

    @Test
    public void testCastAliasNullObjectReference() {
        System.out.println("start testCastAliasNullObjectReference");
        final Alias result = JaplScript.cast(Alias.class, new ReferenceImpl(null, null));
        assertNull(result);
        System.out.println("end testCastAliasNullObjectReference");
    }

    @Test
    public void testCastAlias() {
        System.out.println("start testCastAlias");
        final String finderApplicationReference = "application \"Finder\"";
        final Alias alias = JaplScript.cast(Alias.class, new ReferenceImpl("/Users", finderApplicationReference));
        assertSame(Alias.class, alias.getClass());
        final TypeClass expected = new TypeClass("«class furl»", "«class furl»", Application.class, null);
        assertEquals(expected, alias.getTypeClass());
        assertEquals(Paths.get("/Users"), alias.getPath());
        System.out.println("end testCastAlias");
    }

    @Test
    public void testSimpleQuote() {
        System.out.println("start testSimpleQuote");
        String quotedHallo = JaplScript.quote("Hallo");
        assertEquals("(\"Hallo\")", quotedHallo);
        System.out.println("end testSimpleQuote");
    }

    @Test
    public void testQuoteQuote() {
        System.out.println("start testQuoteQuote");
        String quotedHallo = JaplScript.quote("\"Hallo\"");
        assertEquals("(\"\\\"Hallo\\\"\")", quotedHallo);
        System.out.println("end testQuoteQuote");
    }

    @Test
	public void testUnicodeQuote() {
        System.out.println("start testUnicodeQuote");
        final String s = "A\u00afB";
        System.out.println(s);
        String quotedHallo = JaplScript.quote(s);
        System.out.println(quotedHallo);
        assertEquals("(\"A\" & (\u00abdata utf8c2af\u00bb as Unicode text) & \"B\")", quotedHallo);
        System.out.println("end testUnicodeQuote");
    }

	@Test
	public void testUnicodeQuote2() {
        System.out.println("start testUnicodeQuote2");
        final String s = "A\ubaa0B";
        System.out.println(s);
        String quotedHallo = JaplScript.quote(s);
        System.out.println(quotedHallo);
        assertEquals("(\"A\" & (\u00abdata utf8ebaaa0\u00bb as Unicode text) & \"B\")", quotedHallo);
        System.out.println("end testUnicodeQuote2");
    }

	@Test
	public void testUnicodeExtQuote() {
        System.out.println("start testUnicodeExtQuote");
        final String s = "A\u00af\u00afB";
        System.out.println(s);
        String quotedHallo = JaplScript.quote(s);
        System.out.println(quotedHallo);
        assertEquals("(\"A\" & (\u00abdata utf8c2afc2af\u00bb as Unicode text) & \"B\")", quotedHallo);
        System.out.println("end testUnicodeExtQuote");
    }

	@Test
	public void testTrailingUnicodeQuote() {
        System.out.println("start testTrailingUnicodeQuote");
        final String s = "AB\u00af\u00af";
        System.out.println(s);
        String quotedHallo = JaplScript.quote(s);
        System.out.println(quotedHallo);
        assertEquals("(\"AB\" & (\u00abdata utf8c2afc2af\u00bb as Unicode text))", quotedHallo);
        System.out.println("end testTrailingUnicodeQuote");
    }

    @Test
    public void testGetDate() throws IOException {
        System.out.println("start testGetDate");
        final ScriptExecutor scriptExecutor = ScriptExecutor.newInstance();
        scriptExecutor.setScript("tell application \"Finder\"\n" +
                "\treturn current date\n" +
                "end tell");
        final String result = scriptExecutor.execute();
        final java.util.Date date = JaplScript.cast(java.util.Date.class, new ReferenceImpl(result, null));
        System.out.println("current date: " + date);
        assertEquals(System.currentTimeMillis(), date.getTime(), 2000);
        System.out.println("end testGetDate");
    }

    @Test
    public void testCreateDate() throws IOException, ParseException {
        System.out.println("start testCreateDate");
        final ObjectInvocationHandler handler = new ObjectInvocationHandler(new ReferenceImpl(null, "application \"iTunes\""));
        final java.util.Date date = handler.executeAppleScript(new ReferenceImpl(null, "application \"iTunes\""), "return my createDate(1955, 5, 2, 13, 15, 22)", java.util.Date.class);
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-M-d H:m:s");
        assertEquals(format.parse("1955-5-2 13:15:22"), date);
        System.out.println("end testCreateDate");
    }

    @Test
    public void testInternFailureMissingApplication() {
        System.out.println("start testInternFailureMissingApplication");
        final TypeClass typeClass = new TypeClass("name", new Chevron("class", "name"));
        assertSame(typeClass, JaplScript.internTypeClass(typeClass));
        System.out.println("end testInternFailureMissingApplication");
    }

    @Test
    public void testGetPropertyWithNoApplicationReference() {
        System.out.println("start testGetPropertyWithNoApplicationReference");
        Assertions.assertThrows(JaplScriptException.class, () -> {
            final TypeClass typeClass = new TypeClass("text", new Chevron("class", "ctxt"));
            JaplScript.getProperty(new ReferenceImpl("text", null), typeClass, "property");
        });
        System.out.println("end testGetPropertyWithNoApplicationReference");
    }

    @Test
    public void testGetPropertyWithUnregisteredApplicationReference() {
        System.out.println("start testGetPropertyWithUnregisteredApplicationReference");
        final TypeClass typeClass = new TypeClass("text", new Chevron("class", "ctxt"));
        final Property property = JaplScript.getProperty(new ReferenceImpl("text", "SomeApp"), typeClass, "property");
        assertNull(property);
        System.out.println("end testGetPropertyWithUnregisteredApplicationReference");
    }

}
