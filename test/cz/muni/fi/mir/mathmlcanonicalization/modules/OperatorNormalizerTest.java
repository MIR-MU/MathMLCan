package cz.muni.fi.mir.mathmlcanonicalization.modules;

import org.junit.Test;

/**
 * Test for OperatorNormalizer canonicalization DOM Module.
 * 
 * @author David Formanek
 */
public class OperatorNormalizerTest extends AbstractModuleTest{
    
    private static final Module defaultInstance = new MrowNormalizer();
    
    @Test
    public void testFunction() {
        testXML(defaultInstance, "operatorNormalizer/function");
    }
    
    @Test
    public void testSine() {
        testXML(defaultInstance, "operatorNormalizer/sin");
    }
}
