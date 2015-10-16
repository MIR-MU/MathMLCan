package cz.muni.fi.mir.mathmlcanonicalization;

/**
 * Unrecoverable configuration error.
 * NB: Error subclasses are treated as an unchecked exception.
 */
public class ConfigError extends Error {

    private static final long serialVersionUID = -5662822593110473614L;

    public ConfigError(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigError(String message) {
        super(message);
    }

}
