package cz.muni.fi.mir.mathmlcanonicalization.modules;

import org.junit.Test;

/**
 * Test for ScriptNormalizer canonicalization DOM Module.
 * 
 * @author David Formanek
 */
public class ScriptNormalizerTest extends AbstractModuleTest {

    private static final Module defaultInstance = new ScriptNormalizer();
    
    @Test
    public void testNestedSuperscript() {
        testXML(defaultInstance, "scriptNormalizer/sub-sup");
    }

    @Test
    public void testMoreSubsup() {
        testXML(defaultInstance, "scriptNormalizer/subsup");
    }
}
