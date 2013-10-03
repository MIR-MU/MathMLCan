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
    public void testInvalidScript() {
        testXML(DEFAULT_INSTANCE, "scriptNormalizer/invalid-scripts");
    }
    
    @Test
    public void testNestedSuperscript() {
        testXML(DEFAULT_INSTANCE, "scriptNormalizer/sub-sup");
    }
    
    @Test
    public void testComplexNestedSuperscript() {
        testXML(DEFAULT_INSTANCE, "scriptNormalizer/nested-sub-sup");
    }

    @Test
    public void testSubsup() {
        testXML(DEFAULT_INSTANCE, "scriptNormalizer/subsup");
    }
    
    @Test
    public void testComplexSubsup() {
        testXML(DEFAULT_INSTANCE, "scriptNormalizer/complexsubsup");
    }
    
    @Test
    public void testUnderOver() {
        testXML(DEFAULT_INSTANCE, "scriptNormalizer/underover");
    }
}
