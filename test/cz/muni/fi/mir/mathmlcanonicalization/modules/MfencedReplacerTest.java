package cz.muni.fi.mir.mathmlcanonicalization.modules;

import org.junit.Test;

/**
 * Test for MfencedReplacer canonicalization DOM Module.
 * 
 * @author David Formanek
 */
public class MfencedReplacerTest extends AbstractModuleTest {

    private static final Module DEFAULT_INSTANCE = new MfencedReplacer();
    private static final Module CONFIGURED_INSTANCE = new MfencedReplacer();

    public MfencedReplacerTest() {
        setPrintProcessed(false);
        CONFIGURED_INSTANCE.setProperty("open", "[[");
        CONFIGURED_INSTANCE.setProperty("close", "]]");
        CONFIGURED_INSTANCE.setProperty("separators", ";");
        CONFIGURED_INSTANCE.setProperty("forceopen", "1");
        CONFIGURED_INSTANCE.setProperty("forceclose", "1");
        CONFIGURED_INSTANCE.setProperty("forceseparators", "1");
        CONFIGURED_INSTANCE.setProperty("outermrow", "0");
    }
    
    @Test
    public void testSimpleInterval() {
        testXML(DEFAULT_INSTANCE, "mfencedReplacer/interval");
    }

    @Test
    public void testMoreSeparators() {
        testXML(DEFAULT_INSTANCE, "mfencedReplacer/sequence-separators");
    }
    
    @Test
    public void testBlankSeparators() {
        testXML(DEFAULT_INSTANCE, "mfencedReplacer/blank-separators");
    }
    
    @Test
    public void testNoChildren() {
        testXML(DEFAULT_INSTANCE, "mfencedReplacer/no-children");
    }
    
    @Test
    public void testNestedMfenced() {
        testXML(DEFAULT_INSTANCE, "mfencedReplacer/nested");
    }
    
    @Test
    public void testConfigured() {
        testXML(CONFIGURED_INSTANCE, "mfencedReplacer/interval-configured");
    }
}
