package cz.muni.fi.mir.mathmlcanonicalization.modules;

import org.junit.Test;

/**
 * Test for ElementMinimizer canonicalization stream module
 * 
 * @author David Formanek
 */
public class ElementMinimizerTest extends AbstractModuleTest {

    private static final Module DEFAULT_INSTANCE = new ElementMinimizer();
    
    @Test
    public void testPhantom() {
        testXML(DEFAULT_INSTANCE, "elementMinimizer/mphantom");
    }
    
    @Test
    public void testFraction() {
        testXML(DEFAULT_INSTANCE, "elementMinimizer/mfrac");
    }
}