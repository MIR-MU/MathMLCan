package cz.muni.fi.mir.mathmlcanonicalization;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

// TODO: add javadoc description
/**
 *
 * @author mato
 */
public class Settings {

    private static Properties props;
    public static final String PROPERIES_FILENAME = "/res/canonicalizer.properties";
    public static final String MATHMLDTD = "mathmldtd";

    static {
        try {
            props = new Properties();
            props.load(Settings.class.getResourceAsStream(PROPERIES_FILENAME));
            Logger.getLogger(Settings.class.getName()).log(
                    Level.FINER, "canonicalizer properties loaded succesfully");
        } catch (Exception ex) {
            Logger.getLogger(Settings.class.getName()).log(
                    Level.SEVERE, "cannot find " + "canonicalizer.properties", ex);
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
            public Object resolveEntity(String publicID, String systemID, String baseURI, String namespace) {
                if (systemID.endsWith("dtd")) {
                    String dtdLocation = Settings.getProperty(Settings.MATHMLDTD);
                    return Settings.class.getResourceAsStream(dtdLocation);
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
            public InputSource resolveEntity(String publicId, String systemId) {
                if (systemId.endsWith("dtd")) {
                    String dtdLocation = Settings.getProperty(Settings.MATHMLDTD);
                    return new InputSource(
                            Settings.class.getResourceAsStream(dtdLocation));
                }
                return null;
            }
        });
        return builder;
    }
}
