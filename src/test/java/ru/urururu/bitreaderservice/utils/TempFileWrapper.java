package ru.urururu.bitreaderservice.utils;

import java.io.File;
import java.io.IOException;

/**
* @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
*/
public class TempFileWrapper extends FileWrapper {
    public TempFileWrapper(String prefix, String suffix) {
        super(createTempFile(prefix, suffix));
    }

    private static File createTempFile(String prefix, String suffix) {
        try {
            return File.createTempFile(prefix, suffix);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        file.delete();
    }

}
