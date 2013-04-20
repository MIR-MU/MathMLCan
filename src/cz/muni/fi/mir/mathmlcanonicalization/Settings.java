package cz.muni.fi.mir.mathmlcanonicalization;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

// TODO: add javadoc description
/**
 *
 * @author mato
 */
public class Settings {
    
    private static Properties props;
    public static final String MATHMLDTD = "mathmldtd";
    
    static {
        try {
            props = new Properties();
            props.load(Settings.class.getResourceAsStream("canonicalizer.properties"));
            System.out.println("Canonicalizer properties loaded succesfully");
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static String getProperty(String key) {
        return props.getProperty(key);
    }
    
    public static void setProperty(String key, String value) {
        props.put(key, value);
    }
    
    public static XMLInputFactory setupXMLInputFactory() {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, true);
        inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, true);
        inputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, true);
        inputFactory.setProperty(XMLInputFactory.RESOLVER, new XMLResolver() {
            @Override
            public Object resolveEntity(String publicID, String systemID, String baseURI, String namespace) throws XMLStreamException {
                if (systemID.endsWith("dtd")) {
                    try {
                        return new FileInputStream(Settings.getProperty(Settings.MATHMLDTD));
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                return null;
            }
        });
        return inputFactory;
    }
    
    public static SAXBuilder setupSAXBuilder() {
        SAXBuilder builder = new SAXBuilder();
        builder.setXMLReaderFactory(XMLReaders.NONVALIDATING);
        builder.setFeature("http://xml.org/sax/features/validation", false);
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", true);
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);
        builder.setEntityResolver(new EntityResolver() {

            @Override
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                if (systemId.endsWith("dtd")) {
                    try {
                        return new InputSource(new FileInputStream(Settings.getProperty(Settings.MATHMLDTD)));
                    } catch (FileNotFoundException ex) {
                        System.err.println(ex);
                    }
                }
                return null;
            }
        });
        return builder;
    }
    
}
