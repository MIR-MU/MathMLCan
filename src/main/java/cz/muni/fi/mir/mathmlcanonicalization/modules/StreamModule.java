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
     * Returned {@link ByteArrayOutputStream} can be converted
     * to {@link InputStream} instance using
     * {@link ByteArrayInputStream#ByteArrayInputStream(byte[])}.
     * 
     * @param input input stream to be processed
     * @return the result in accordance with the module specification
     * @throws ModuleException when cannot transform the input by this module
     */
    public ByteArrayOutputStream execute(InputStream input) throws ModuleException;
}
