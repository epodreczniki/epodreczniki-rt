package pl.psnc.ep.rt.util;

import java.io.IOException;

public class ObjectMalformedException extends IOException {

    public ObjectMalformedException() {
        super();
    }


    public ObjectMalformedException(String msg, Throwable cause) {
        super(msg, cause);
    }


    public ObjectMalformedException(String msg) {
        super(msg);
    }


    public ObjectMalformedException(Throwable cause) {
        super(cause);
    }
}