/*
 * =================================================
 * Copyright 2021 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.japlscript.execution;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.io.IOException;
import java.io.Reader;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

/**
 * TestReaderPump.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestReaderPump {

    @Test
    public void testReadException() throws IOException, InterruptedException {
        final Reader mockReader = mock(Reader.class);
        when(mockReader.read((char[]) ArgumentMatchers.any())).thenThrow(IOException.class);
        final ReaderPump pump = new ReaderPump(mockReader);
        final Thread t = new Thread(pump);
        t.start();
        t.join(5000);
        assertNotNull(pump.getIOException());
    }

    @Test
    public void testCloseException() throws IOException, InterruptedException {
        final Reader mockReader = mock(Reader.class);
        when(mockReader.read((char[]) ArgumentMatchers.any())).thenReturn(-1);
        doThrow(new IOException()).when(mockReader).close();
        final ReaderPump pump = new ReaderPump(mockReader);
        final Thread t = new Thread(pump);
        t.start();
        t.join(5000);
        // it's ok to not lead to an exception
        assertNull(pump.getIOException());
    }
}
