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
