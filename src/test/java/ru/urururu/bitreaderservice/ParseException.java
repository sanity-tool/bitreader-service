package ru.urururu.bitreaderservice;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class ParseException extends Exception {
    public ParseException(int resultCode, String error) {
        super("resultCode = [" + resultCode + "], error = [" + error + "]");
    }

    public ParseException(Exception e) {
        super(e);
    }
}
