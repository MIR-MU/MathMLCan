package cz.muni.fi.mir.mathmlcanonicalization;

import cz.muni.fi.mir.mathmlcanonicalization.modules.DOMModule;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author David Formanek
 */
public class XMLTestUtils {

    /**
     * Get input processed by specified module.
     * 
     * @param instance a configured instance of the tested module
     * @param in input stream with original XML document
     * @return stream reader for processed input
     */
    public static InputStreamReader getProcessed(DOMModule instance, InputStream in) {
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
            Logger.getLogger(XMLTestUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(XMLTestUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private static void printDocument(ByteArrayOutputStream output, String header) throws IOException {
        System.out.println(header);
        System.out.println("--------------------");
        output.writeTo(System.out);
        System.out.println("--------------------");
        System.out.println();
    }
}
