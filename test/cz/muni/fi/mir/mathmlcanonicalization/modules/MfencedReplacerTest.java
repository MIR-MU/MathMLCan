package cz.muni.fi.mir.mathmlcanonicalization.modules;

import java.io.IOException;
import org.junit.Test;

/**
 * Test for MfencedReplacer canonicalization DOM Module.
 * 
 * @author David Formanek
 */
public class MfencedReplacerTest extends AbstractDOMModuleTest {

    @Test
    public void testSimpleInterval() throws IOException {
        MfencedReplacer instance = new MfencedReplacer();
        testXML(instance, "mfencedreplacer/interval");
    }

    @Test
    public void testMoreSeparators() throws IOException {
        MfencedReplacer instance = new MfencedReplacer();
        testXML(instance, "mfencedreplacer/sequence-separators");
    }
    
    @Test
    public void testBlankSeparators() throws IOException {
        MfencedReplacer instance = new MfencedReplacer();
        testXML(instance, "mfencedreplacer/blank-separators");
    }
    
    @Test
    public void testNoChildren() throws IOException {
        MfencedReplacer instance = new MfencedReplacer();
        testXML(instance, "mfencedreplacer/no-children");
    }
    
    @Test
    public void testNestedMfenced() throws IOException {
        MfencedReplacer instance = new MfencedReplacer();
        testXML(instance, "mfencedreplacer/nested");
    }
}
