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
    public void testEmptyScript() {
        testXML(DEFAULT_INSTANCE, "scriptNormalizer/empty-script");
    }
    
    @Test
    public void testOneItemScript() {
        testXML(DEFAULT_INSTANCE, "scriptNormalizer/oneitem-script");
    }
    
    @Test
    public void testNestedSuperscript() {
        testXML(DEFAULT_INSTANCE, "scriptNormalizer/sub-sup");
    }

    @Test
    public void testSubsup() {
        testXML(DEFAULT_INSTANCE, "scriptNormalizer/subsup");
    }
    
    @Test
    public void testSubsup2() {
        testXML(DEFAULT_INSTANCE, "scriptNormalizer/subsup2");
    }
    
    @Test
    public void testComplexSubsup() {
        testXML(DEFAULT_INSTANCE, "scriptNormalizer/complexsubsup");
    }
    
}
