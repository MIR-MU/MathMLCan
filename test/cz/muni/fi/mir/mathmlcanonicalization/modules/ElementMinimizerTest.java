package cz.muni.fi.mir.mathmlcanonicalization.modules;

import java.io.IOException;
import org.junit.Test;

/**
 * Test for MfencedReplacer canonicalization DOM Module.
 * 
 * @author David Formanek
 */
public class ElementMinimizerTest extends AbstractModuleTest {

    @Test
    public void testPhantom() throws IOException {
        ElementMinimizer instance = new ElementMinimizer();
        testXML(instance, "elementMinimizer/mphantom");
    }
    
    @Test
    public void testFraction() throws IOException {
        ElementMinimizer instance = new ElementMinimizer();
        testXML(instance, "elementMinimizer/mfrac");
    }
}