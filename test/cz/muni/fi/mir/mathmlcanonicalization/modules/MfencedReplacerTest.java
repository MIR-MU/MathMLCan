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

    // TODO: add more tests
}