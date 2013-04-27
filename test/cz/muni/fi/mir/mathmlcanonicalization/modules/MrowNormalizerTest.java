package cz.muni.fi.mir.mathmlcanonicalization.modules;

import org.junit.Test;

/**
 * Test for MrowNormalizer canonicalization DOM Module.
 * 
 * @author David Formanek
 */
public class MrowNormalizerTest extends AbstractModuleTest{
    
    private static final Module defaultInstance = new MrowNormalizer();
    
    @Test
    public void testSquareRoot() {
        testXML(defaultInstance, "mrowNormalizer/sqrt");
    }
    
    @Test
    public void testTuple() {
        testXML(defaultInstance, "mrowNormalizer/tuple");
    }
    
    @Test
    public void testParentheses1() {
        testXML(defaultInstance, "mrowNormalizer/parentheses1");
    }
    
    @Test
    public void testParentheses2() {
        testXML(defaultInstance, "mrowNormalizer/parentheses2");
    }
}