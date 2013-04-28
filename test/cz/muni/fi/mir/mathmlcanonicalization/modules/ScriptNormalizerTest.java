package cz.muni.fi.mir.mathmlcanonicalization.modules;

import org.junit.Test;

/**
 * Test for ScriptNormalizer canonicalization DOM Module.
 * 
 * @author David Formanek
 */
public class ScriptNormalizerTest extends AbstractModuleTest {

    private static final Module DEFAULT_INSTANCE = new ScriptNormalizer();
    
    @Test
    public void testNestedSuperscript() {
        testXML(DEFAULT_INSTANCE, "scriptNormalizer/sub-sup");
    }

    @Test
    public void testMoreSubsup() {
        testXML(DEFAULT_INSTANCE, "scriptNormalizer/subsup");
    }
}
