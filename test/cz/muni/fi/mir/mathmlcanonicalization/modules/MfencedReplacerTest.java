package cz.muni.fi.mir.mathmlcanonicalization.modules;

import cz.muni.fi.mir.mathmlcanonicalization.XMLTestUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Test for MfencedReplacer canonicalization DOM Module.
 * 
 * @author David Formanek
 */
public class MfencedReplacerTest extends XMLTestCase {

    /**
     * Test of interval transformation.
     */
    @Test
    public void testSimpleInterval() throws IOException {
        MfencedReplacer instance = new MfencedReplacer();
        System.out.println(instance);
        testXML(instance, "interval");
    }

    // TODO: add more tests
    
    private void testXML(DOMModule instance, String testFile) throws IOException {
        final InputStreamReader processed = getProcessed(instance, testFile);
        final InputStreamReader canonical = getCanonical(testFile);
        XMLUnit.setIgnoreWhitespace(true);
        try {
            assertXMLEqual(canonical, processed);
        } catch (SAXException ex) {
            Logger.getLogger(MfencedReplacerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private InputStreamReader getProcessed(DOMModule instance, String testFile) {
        InputStream resource = this.getClass().
                getResourceAsStream("/res/mfencedReplacer/" + testFile + ".original.xml");
        return XMLTestUtils.getProcessed(instance, resource);
    }

    private InputStreamReader getCanonical(String testFile) {
        InputStream resource = this.getClass().
                getResourceAsStream("/res/mfencedReplacer/" + testFile + ".canonical.xml");
        return new InputStreamReader(resource);
    }
}