package cz.muni.fi.mir.mathmlcanonicalization;

/**
 *
 * General purpose exception for problems with XML configuration.
 */
public class ConfigException extends Exception {
    public ConfigException(String message) {
        super(message);
    }
    
    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
