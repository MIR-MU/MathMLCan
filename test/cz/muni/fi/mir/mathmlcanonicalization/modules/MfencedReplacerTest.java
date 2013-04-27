package cz.muni.fi.mir.mathmlcanonicalization.modules;

import org.junit.Test;

/**
 * Test for MfencedReplacer canonicalization DOM Module.
 * 
 * @author David Formanek
 */
public class MfencedReplacerTest extends AbstractModuleTest {

    private static final Module defaultInstance = new MfencedReplacer();
    
    @Test
    public void testSimpleInterval() {
        testXML(defaultInstance, "mfencedReplacer/interval");
    }

    @Test
    public void testMoreSeparators() {
        testXML(defaultInstance, "mfencedReplacer/sequence-separators");
    }
    
    @Test
    public void testBlankSeparators() {
        testXML(defaultInstance, "mfencedReplacer/blank-separators");
    }
    
    @Test
    public void testNoChildren() {
        testXML(defaultInstance, "mfencedReplacer/no-children");
    }
    
    @Test
    public void testNestedMfenced() {
        testXML(defaultInstance, "mfencedReplacer/nested");
    }
}
