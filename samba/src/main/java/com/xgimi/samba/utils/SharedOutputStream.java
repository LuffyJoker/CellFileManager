package com.xgimi.samba.utils;

import com.hierynomus.smbj.share.File;

import java.io.IOException;
import java.io.OutputStream;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 11:28
 * desc   :
 */
public class SharedOutputStream extends OutputStream {

    /**
     * File that provides the output stream.
     */
    private final File file;

    /**
     * output stream of the file that will be decorated.
     */
    private final OutputStream outputStream;

    /**
     * Create a new decorated output stream that respects the reference counting close mechanism of the file. It's possible to append or
     * overwrite existing content.
     *
     * @param file File that will provide the output stream
     */
    public SharedOutputStream(File file, boolean appendContent) {
        this.file = file;
        this.outputStream = file.getOutputStream(appendContent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(int i) throws IOException {
        outputStream.write(i);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        outputStream.flush();
        outputStream.close();
        file.close();
    }
}
