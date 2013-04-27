package cz.muni.fi.mir.mathmlcanonicalization.modules;

import org.junit.Test;

/**
 * Test for ElementMinimizer canonicalization stream module
 * 
 * @author David Formanek
 */
public class ElementMinimizerTest extends AbstractModuleTest {

    private static final Module defaultInstance = new ElementMinimizer();
    
    @Test
    public void testPhantom() {
        testXML(defaultInstance, "elementMinimizer/mphantom");
    }
    
    @Test
    public void testFraction() {
        testXML(defaultInstance, "elementMinimizer/mfrac");
    }
}