package io.streamzi.registry;

/**
 *
 * @author hhiden
 */
public class RegistryException extends Exception {

    /**
     * Creates a new instance of <code>RegistryException</code> without detail message.
     */
    public RegistryException() {
    }

    /**
     * Constructs an instance of <code>RegistryException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public RegistryException(String msg) {
        super(msg);
    }
    
    /**
     * Counstructs an exception with both a detail message and a cause
     * @param msg detail message
     * @param cause underlying cause
     */
    public RegistryException(String msg, Throwable cause){
        super(msg, cause);
    }
}
