package org.esa.snap.utils;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Jean Coravu
 */
public class BufferedInputStreamWrapper {

    private final InputStream fileInputStream;
    private final BufferedInputStream bufferredInputStream;

    public BufferedInputStreamWrapper(Path file) throws IOException {
        this.fileInputStream = Files.newInputStream(file);
        this.bufferredInputStream = new BufferedInputStream(this.fileInputStream);
    }

    public void close() throws IOException {
        // close first the buffered input stream and then the file input stream
        try {
            this.bufferredInputStream.close();
        } finally {
            this.fileInputStream.close();
        }
    }

    public final boolean readBoolean() throws IOException {
        int ch = this.bufferredInputStream.read();
        if (ch < 0) {
            throw new EOFException();
        }
        return (ch != 0);
    }

    public final long readLong() throws IOException {
        //return ((long)(readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
        return ((long)(readInt()) << 32) | (readInt() & 0xFFFFFFFFL);
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    public final int readInt() throws IOException {
        int ch1 = this.bufferredInputStream.read();
        int ch2 = this.bufferredInputStream.read();
        int ch3 = this.bufferredInputStream.read();
        int ch4 = this.bufferredInputStream.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0) {
            throw new EOFException();
        }
        //return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
        return ((ch1 << 24) | (ch2 << 16) | (ch3 << 8) | (ch4 << 0));
    }

    public final void readFully(byte[] b) throws IOException {
        this.bufferredInputStream.read(b);
    }

    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }
}
