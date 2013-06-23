package cz.muni.fi.mir.mathmlcanonicalization.modules;

/**
 * 
 * General purpose exception for problems with modules.
 */
public class ModuleException extends Exception {
    public ModuleException(String message) {
        super(message);
    }
    
    public ModuleException(String message, Throwable ex) {
        super(message, ex);
    }
}
