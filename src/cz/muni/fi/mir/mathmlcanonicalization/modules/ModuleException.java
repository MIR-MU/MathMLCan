/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.mir.mathmlcanonicalization.modules;

/**
 *
 * @author rob
 */
public class ModuleException extends Exception {
    public ModuleException(String message) {
        super(message);
    }
    
    public ModuleException(String message, Throwable ex) {
        super(message, ex);
    }
}
