package cz.muni.fi.mir.mathmlcanonicalization.modules;

import cz.muni.fi.mir.mathmlcanonicalization.Settings;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.junit.*;
import org.xml.sax.SAXException;

/**
 * Abstract class to allow descendants to simple compare desired and produced
 * xml
 *
 * @author David Formanek
 */
@Ignore // no tests
abstract class AbstractModuleTest {

    private static final String PATH_TO_TESTFILES = "/res/";

    protected void testXML(Module instance, String testFile) throws IOException {
        final InputStreamReader processed = getProcessed(instance, testFile);
        final InputStreamReader canonical = getCanonical(testFile);
        XMLUnit.setIgnoreWhitespace(true);
        try {
            new XMLTestCase() {
            }.assertXMLEqual(canonical, processed);
        } catch (SAXException ex) {
            Logger.getLogger(MfencedReplacerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected InputStreamReader getProcessed(Module instance, String testFile) {
        InputStream resource = this.getClass().
                getResourceAsStream(PATH_TO_TESTFILES + testFile + ".original.xml");
        return getProcessed(instance, resource);
    }

    protected InputStreamReader getCanonical(String testFile) {
        InputStream resource = this.getClass().
                getResourceAsStream(PATH_TO_TESTFILES + testFile + ".canonical.xml");
        return new InputStreamReader(resource);
    }

    private InputStreamReader getProcessed(Module instance, InputStream in) {
        if (instance instanceof StreamModule) {
            return getProcessed((StreamModule) instance, in);
        } else if (instance instanceof DOMModule) {
            return getProcessed((DOMModule) instance, in);
        } else {
            throw new UnsupportedOperationException("Module type not supported");
        }
    }
    
    /**
     * Get input processed by specified module.
     *
     * @param instance a configured instance of the tested module
     * @param in input stream with original XML document
     * @return stream reader for processed input
     */
    private InputStreamReader getProcessed(DOMModule instance, InputStream in) {
        SAXBuilder builder = Settings.setupSAXBuilder();
        Document doc;
        try {
            doc = builder.build(in);
            instance.execute(doc);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            XMLOutputter serializer = new XMLOutputter();
            serializer.output(doc, output);

            printDocument(output, "Output of " + instance);

            return new InputStreamReader(new ByteArrayInputStream(output.toByteArray()));

        } catch (JDOMException ex) {
            Logger.getLogger(this.getClass().getName()).log(
                    Level.SEVERE, "cannot build document", ex);
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(
                    Level.SEVERE, "cannot convert between stream and DOM", ex);
        }
        return null;
    }

    private InputStreamReader getProcessed(StreamModule instance, InputStream in) {
        ByteArrayOutputStream output = instance.execute(in);
        printDocument(output, "Output of " + instance);
        return new InputStreamReader(new ByteArrayInputStream(output.toByteArray()));
    }

    private void printDocument(ByteArrayOutputStream output, String header) {
        System.out.println(header);
        System.out.println("--------------------");
        try {
            output.writeTo(System.out);
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(
                    Level.WARNING, "cannot print result", ex);
        }
        System.out.println("--------------------");
        System.out.println();
    }
}