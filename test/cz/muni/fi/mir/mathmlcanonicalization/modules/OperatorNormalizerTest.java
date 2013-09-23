package cz.muni.fi.mir.mathmlcanonicalization.modules;

import org.junit.Test;

/**
 * Test for OperatorNormalizer canonicalization DOM Module.
 * 
 * @author David Formanek
 */
public class OperatorNormalizerTest extends AbstractModuleTest{
    
    private static final Module DEFAULT_INSTANCE = new OperatorNormalizer();
    
    @Test
    public void testFunction() {
        testXML(DEFAULT_INSTANCE, "operatorNormalizer/function");
    }
    
    @Test
    public void testFunction2() {
        testXML(DEFAULT_INSTANCE, "operatorNormalizer/function2");
    }
    
    @Test
    public void testFunction3() {
        testXML(DEFAULT_INSTANCE, "operatorNormalizer/function3");
    }
    
    @Test
    public void testSine() {
        testXML(DEFAULT_INSTANCE, "operatorNormalizer/sin");
    }
    
    @Test
    public void testMultiplicationCdot() {
        testXML(DEFAULT_INSTANCE, "operatorNormalizer/multiplication-cdot");
    }
    
    @Test
    public void testMultiplicationBlank() {
        testXML(DEFAULT_INSTANCE, "operatorNormalizer/multiplication-blank");
    }
    
    @Test
    public void testUnification() {
        testXML(DEFAULT_INSTANCE, "operatorNormalizer/operator-unification");
    }
    
    @Test
    public void testUnicode() {
        // not working for normalized multibyte symbols
        testXML(DEFAULT_INSTANCE, "operatorNormalizer/unicode");
    }
}
