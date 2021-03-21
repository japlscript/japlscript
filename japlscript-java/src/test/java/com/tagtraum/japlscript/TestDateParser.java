/*
 * =================================================
 * Copyright 2016 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * TestDateParser.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestDateParser {

    private Date amDate;
    private Date pmDate;

    @Before
    public void setup() throws ParseException {
        amDate = new SimpleDateFormat("d.M.yyyy H:mm:ss").parse("8.4.2016 1:04:56");
        pmDate = new SimpleDateFormat("d.M.yyyy H:mm:ss").parse("8.4.2016 13:04:56");
    }

    @Test
    public void testGerman() throws ParseException {
        final DateParser parser = new DateParser(Locale.GERMANY);
        assertEquals(amDate, parser.parse("Freitag, 8. April 2016 1:04:56 vorm."));
        assertEquals(pmDate, parser.parse("Freitag, 8. April 2016 1:04:56 nachm."));
        assertEquals(pmDate, parser.parse("Freitag, 8. April 2016 13:04:56"));
    }

    @Test
    public void testUS() throws ParseException {
        final DateParser parser = new DateParser(Locale.US);
        assertEquals(amDate, parser.parse("Friday, April 8, 2016 at 1:04:56 AM"));
        assertEquals(pmDate, parser.parse("Friday, April 8, 2016 at 1:04:56 PM"));
        assertEquals(pmDate, parser.parse("Friday, April 8, 2016 at 13:04:56"));
    }

    @Test
    public void testCanada() throws ParseException {
        if (System.getProperty("java.version").startsWith("1.8")) {
            final DateParser parser = new DateParser(Locale.CANADA);
            assertEquals(amDate, parser.parse("Friday, April 8, 2016 at 1:04:56 AM"));
            assertEquals(pmDate, parser.parse("Friday, April 8, 2016 at 1:04:56 PM"));
            assertEquals(pmDate, parser.parse("Friday, April 8, 2016 at 13:04:56"));
        } else {
            final DateParser parser = new DateParser(Locale.CANADA);
            assertEquals(amDate, parser.parse("Friday, April 8, 2016 at 1:04:56 a.m."));
            assertEquals(pmDate, parser.parse("Friday, April 8, 2016 at 1:04:56 p.m."));
            assertEquals(pmDate, parser.parse("Friday, April 8, 2016 at 13:04:56"));
        }
    }

    @Test
    public void testFrench() throws ParseException {
        final DateParser parser = new DateParser(Locale.FRANCE);
        assertEquals(amDate, parser.parse("vendredi 8 avril 2016 1:04:56 AM"));
        assertEquals(pmDate, parser.parse("vendredi 8 avril 2016 1:04:56 PM"));
        assertEquals(pmDate, parser.parse("vendredi 8 avril 2016 13:04:56"));
    }

    @Test
    public void testDanish() throws ParseException {
        final DateParser parser = new DateParser(Locale.forLanguageTag("da"));
        assertEquals(amDate, parser.parse("fredag den 8. april 2016 kl. 1.04.56 AM"));
        assertEquals(pmDate, parser.parse("fredag den 8. april 2016 kl. 1.04.56 PM"));
        assertEquals(pmDate, parser.parse("fredag den 8. april 2016 kl. 13.04.56"));
    }

    @Test
    public void testItalian() throws ParseException {
        final DateParser parser = new DateParser(Locale.ITALY);
        assertEquals(amDate, parser.parse("venerdì 8 aprile 2016 1.04.56 AM"));
        assertEquals(pmDate, parser.parse("venerdì 8 aprile 2016 1.04.56 PM"));
        assertEquals(pmDate, parser.parse("venerdì 8 aprile 2016 13.04.56"));
    }

    @Test
    public void testDutch() throws ParseException {
        final DateParser parser = new DateParser(Locale.forLanguageTag("nl"));
        assertEquals(amDate, parser.parse("vrijdag 8 april 2016 1.04.56 a.m."));
        assertEquals(pmDate, parser.parse("vrijdag 8 april 2016 1.04.56 p.m."));
        assertEquals(pmDate, parser.parse("vrijdag 8 april 2016 13.04.56"));
    }

    @Test
    public void testPortuguese() throws ParseException {
        final DateParser parser = new DateParser(Locale.forLanguageTag("pt"));
        assertEquals(amDate, parser.parse("sexta-feira, 8 de abril de 2016 1.04.56 AM"));
        assertEquals(pmDate, parser.parse("sexta-feira, 8 de abril de 2016 1.04.56 PM"));
        assertEquals(pmDate, parser.parse("sexta-feira, 8 de abril de 2016 13.04.56"));
    }

    @Test
    public void testFinnish() throws ParseException {
        final DateParser parser = new DateParser(Locale.forLanguageTag("fi"));
        assertEquals(amDate, parser.parse("perjantai 8. huhtikuuta 2016 1.04.56 ap."));
        assertEquals(pmDate, parser.parse("perjantai 8. huhtikuuta 2016 1.04.56 ip."));
        assertEquals(pmDate, parser.parse("perjantai 8. huhtikuuta 2016 13.04.56"));
    }

    @Test
    public void testJapanese() throws ParseException {
        final DateParser parser = new DateParser(Locale.JAPANESE);
        assertEquals(amDate, parser.parse("2016年4月8日金曜日 午前1:04:56"));
        assertEquals(pmDate, parser.parse("2016年4月8日金曜日 午後1:04:56"));
        assertEquals(pmDate, parser.parse("2016年4月8日金曜日 13:04:56"));
    }

    @Test
    public void testSpanish() throws ParseException {
        if (System.getProperty("java.version").startsWith("1.8")) {
            final DateParser parser = new DateParser(Locale.forLanguageTag("es"));
            assertEquals(amDate, parser.parse("viernes, 8 de abril de 2016, 1:04:56 AM"));
            assertEquals(pmDate, parser.parse("viernes, 8 de abril de 2016, 1:04:56 PM"));
            assertEquals(pmDate, parser.parse("viernes, 8 de abril de 2016, 13:04:56"));
        } else {
            final DateParser parser = new DateParser(Locale.forLanguageTag("es"));
            assertEquals(amDate, parser.parse("viernes, 8 de abril de 2016, 1:04:56 a. m."));
            assertEquals(pmDate, parser.parse("viernes, 8 de abril de 2016, 1:04:56 p. m."));
            assertEquals(pmDate, parser.parse("viernes, 8 de abril de 2016, 13:04:56"));
        }
    }
}
