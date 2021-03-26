/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * TestSession.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestSession {

    @Test
    public void testAddRemoveAspect() {
        final Session session = Session.getSession();
        final Aspect testAspect = new Aspect() {
            @Override
            public String before(final String application, final String body) {
                return null;
            }

            @Override
            public String after(final String application, final String body) {
                return null;
            }
        };
        session.addAspect(testAspect);
        assertTrue(session.getAspects().contains(testAspect));
        assertTrue(session.removeAspect(testAspect));
        assertFalse(session.removeAspect(testAspect));
    }
}
