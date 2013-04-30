package cz.muni.fi.mir.mathmlcanonicalization.modules;

import org.junit.Test;

/**
 * Test for MrowNormalizer canonicalization DOM Module.
 * 
 * @author David Formanek
 */
public class MrowNormalizerTest extends AbstractModuleTest{
    
    private static final Module DEFAULT_INSTANCE = new MrowNormalizer();
    
    @Test
    public void testSquareRoot() {
        testXML(DEFAULT_INSTANCE, "mrowNormalizer/sqrt");
    }
    
    @Test
    public void testTuple() {
        testXML(DEFAULT_INSTANCE, "mrowNormalizer/tuple");
    }
    
    @Test
    public void testParentheses1() {
        testXML(DEFAULT_INSTANCE, "mrowNormalizer/parentheses1");
    }
    
    @Test
    public void testParentheses2() {
        testXML(DEFAULT_INSTANCE, "mrowNormalizer/parentheses2");
    }
    
    @Test
    public void testParentheses3() {
        testXML(DEFAULT_INSTANCE, "mrowNormalizer/parentheses3");
    }
    
    @Test
    public void testInterval() {
        testXML(DEFAULT_INSTANCE, "mrowNormalizer/interval");
    }
    
    @Test
    public void testFrac() {
        testXML(DEFAULT_INSTANCE, "mrowNormalizer/frac");
    }
}