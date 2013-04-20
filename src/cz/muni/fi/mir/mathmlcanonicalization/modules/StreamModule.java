package cz.muni.fi.mir.mathmlcanonicalization.modules;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Modules processing the input as a stream (no DOM)
 * 
 * @author David Formanek
 */
public interface StreamModule extends Module {
    /**
     * Executes the canonicalization module.
     * 
     * @param input input stream to be processed
     * @return the result in accordance with the module specification
     */
    public ByteArrayOutputStream execute(InputStream input);
}
