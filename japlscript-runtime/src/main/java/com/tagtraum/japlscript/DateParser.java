/*
 * =================================================
 * Copyright 2016 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fallback parser for {@link com.tagtraum.japlscript.execution.Osascript} dates.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class DateParser {

    private final Locale locale;
    private final DateFormatSymbols dateFormatSymbols;
    private final SimpleDateFormat[] universalFormats;

    public DateParser(final Locale locale) {
        this.locale = locale;
        this.dateFormatSymbols = DateFormatSymbols.getInstance(locale);
        if (Locale.CANADA.getCountry().equals(locale.getCountry()) || Locale.US.getCountry().equals(locale.getCountry())) {
            this.universalFormats = new SimpleDateFormat[]{
                    new SimpleDateFormat("M d yyyy h.mm.ss aa", Locale.US),
                    new SimpleDateFormat("M d yyyy H.mm.ss", Locale.US),
            };
        } else if (Locale.JAPAN.getLanguage().equals(locale.getLanguage())) {
            this.universalFormats = new SimpleDateFormat[] {
                    new SimpleDateFormat("yyyy M d h.mm.ss aa", Locale.US),
                    new SimpleDateFormat("yyyy M d H.mm.ss", Locale.US),
            };
        } else {
            this.universalFormats = new SimpleDateFormat[] {
                    new SimpleDateFormat("d M yyyy h.mm.ss aa", Locale.US),
                    new SimpleDateFormat("d M yyyy H.mm.ss", Locale.US),

                    new SimpleDateFormat("d. M yyyy h.mm.ss aa", Locale.US),
                    new SimpleDateFormat("d. M yyyy H.mm.ss", Locale.US),
            };
        }
        if (Locale.GERMAN.getLanguage().equals(locale.getLanguage())) {
            this.dateFormatSymbols.setAmPmStrings(new String[]{"vorm.", "nachm."});
        }
        if ("nl".equals(locale.getLanguage())) {
            this.dateFormatSymbols.setAmPmStrings(new String[]{"a.m.", "p.m."});
        }
    }

    public Date parse(final String s) throws ParseException {
        String universalDate = s;
        int month = getSymbolIndex(s, dateFormatSymbols.getMonths(), dateFormatSymbols.getShortMonths());
        if (month != -1) {
            if (universalDate.contains(dateFormatSymbols.getMonths()[month])) universalDate = universalDate.replace(dateFormatSymbols.getMonths()[month], (month + 1) + " ");
            else if (universalDate.contains(dateFormatSymbols.getShortMonths()[month])) universalDate = universalDate.replace(dateFormatSymbols.getShortMonths()[month], (month + 1) + " ");
        } else {
            final String[] lowerMonths = toLowerCase(dateFormatSymbols.getMonths());
            final String[] lowerShortMonths = toLowerCase(dateFormatSymbols.getShortMonths());
            month = getSymbolIndex(s, lowerMonths, lowerShortMonths);
            if (month != -1) {
                if (universalDate.contains(lowerMonths[month])) universalDate = universalDate.replace(lowerMonths[month], (month + 1) + " ");
                else if (universalDate.contains(lowerShortMonths[month])) universalDate = universalDate.replace(lowerShortMonths[month], (month + 1) + " ");
            }
        }
        final String am = dateFormatSymbols.getAmPmStrings()[0];
        final String pm = dateFormatSymbols.getAmPmStrings()[1];

        final boolean containsAM = universalDate.contains(am);
        final boolean containsPM = universalDate.contains(pm);

        if (containsAM) universalDate = universalDate.replace(am, "");
        if (containsPM) universalDate = universalDate.replace(pm, "");

        // remove every letter and comma
        final StringBuilder sb = new StringBuilder();
        boolean lastIsWhiteSpace = false;
        for (final char c : universalDate.toCharArray()) {
            if (!Character.isLetter(c) && c != ',' &&  c != '-' && c != ' ') {
                sb.append(c);
                lastIsWhiteSpace = false;
            } else if (!lastIsWhiteSpace) {
                sb.append(' ');
                lastIsWhiteSpace = true;
            }
        }
        universalDate = sb.toString();
        if (universalDate.endsWith(".")) {
            universalDate = universalDate.substring(0, universalDate.length()-1);
        }
        // convert all colons to dots
        // convert lone dots to spaces
        universalDate = universalDate.replace(':', '.').replace('/', '.').replace(" . ", " ");
        universalDate = universalDate.trim();
        // append am/pm
        if (containsAM) universalDate += " AM";
        if (containsPM) universalDate += " PM";
        universalDate = universalDate.trim();
        // now parse
        for (final SimpleDateFormat format : universalFormats) {
            try {
                return format.parse(universalDate);
            } catch (ParseException e) {
                // ignore
            }
        }
        throw new ParseException("Failed to parse " + s + " with locale " + locale, -1);
    }

    private String[] toLowerCase(final String[] symbols) {
        final String[] lower = new String[symbols.length];
        for (int i=0; i<lower.length; i++) {
            lower[i] = symbols[i].toLowerCase(locale);
        }
        return lower;
    }

    private int getSymbolIndex(final String dateString, final String[] symbolArray, final String[] shortSymbolArray) {
        int index = getSymbolIndex(dateString, symbolArray);
        if (index == -1) {
            index = getSymbolIndex(dateString, shortSymbolArray);
        }
        return index;
    }

    private int getSymbolIndex(final String dateString, final String[] symbolArray) {
        int index = -1;
        for (int i = 0; i< symbolArray.length; i++) {
            if (dateString.contains(symbolArray[i]) && !symbolArray[i].isEmpty()) {
                index = i;
                break;
            }
        }
        return index;
    }
}
