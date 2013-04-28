package cz.muni.fi.mir.mathmlcanonicalization.modules;

import org.junit.Test;

/**
 * Test for OperatorNormalizer canonicalization DOM Module.
 * 
 * @author David Formanek
 */
public class OperatorNormalizerTest extends AbstractModuleTest{
    
    private static final Module DEFAULT_INSTANCE = new MrowNormalizer();
    
    @Test
    public void testFunction() {
        testXML(DEFAULT_INSTANCE, "operatorNormalizer/function");
    }
    
    @Test
    public void testSine() {
        testXML(DEFAULT_INSTANCE, "operatorNormalizer/sin");
    }
}
