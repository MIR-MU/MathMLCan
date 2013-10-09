package cz.muni.fi.mir.mathmlcanonicalization.modules;

import org.junit.Test;

/**
 * Test for FunctionNormalizer canonicalization DOM Module.
 * 
 * @author David Formanek
 */
public class FunctionNormalizerTest extends AbstractModuleTest{
    
    private static final Module DEFAULT_INSTANCE = new FunctionNormalizer();
    
    @Test
    public void testFunction() {
        testXML(DEFAULT_INSTANCE, "functionNormalizer/function");
    }
    
    @Test
    public void testFunction2() {
        testXML(DEFAULT_INSTANCE, "functionNormalizer/function2");
    }
    
    @Test
    public void testFunction3() {
        testXML(DEFAULT_INSTANCE, "functionNormalizer/function3");
    }
    
    @Test
    public void testSine() {
        testXML(DEFAULT_INSTANCE, "functionNormalizer/sin");
    }
}
