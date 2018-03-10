package ru.urururu.bitreaderservice.utils;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public abstract class FileWrapper implements AutoCloseable {
    File file;

    public FileWrapper(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }

    public abstract void close() throws IOException;
}
