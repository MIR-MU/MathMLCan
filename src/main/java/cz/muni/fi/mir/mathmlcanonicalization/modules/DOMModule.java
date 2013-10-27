package cz.muni.fi.mir.mathmlcanonicalization.modules;

import org.jdom2.Document;

/**
 * Modules processing the input using Document Object Model
 * 
 * @author David Formanek
 */
public interface DOMModule extends Module {
    
    /**
     * Executes the canonicalization module
     * 
     * @param doc document to be modified according to the module specification
     * @throws ModuleException when cannot transform the input by this module
     */
    public void execute(Document doc) throws ModuleException;
}
