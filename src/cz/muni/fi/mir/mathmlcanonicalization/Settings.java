package cz.muni.fi.mir.mathmlcanonicalization;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Global settings shared among all instances.
 * 
 * @author mato
 */
public class Settings {

    /**
     * Path to the property file with canonicalizer settings.
     */
    private static final String PROPERTIES_FILENAME = "/res/settings.properties";
    
    /**
     * Name of the property containing path to the MathML DTD
     */
    private static final String MATHMLDTD = "mathmldtd";
    
    private static Properties properties = new Properties();

    // load default properties from the file specified by PROPERTIES_FILENAME
    static {
        try {
            InputStream resourceAsStream = Settings.class.getResourceAsStream(PROPERTIES_FILENAME);
            if (resourceAsStream == null) {
                throw new IOException("cannot find the property file");
            }
            properties.load(resourceAsStream);
            Logger.getLogger(Settings.class.getName()).log(
                    Level.FINER, "canonicalizer properties loaded succesfully");
        } catch (IOException ex) {
            Logger.getLogger(Settings.class.getName()).log(
                    Level.SEVERE, "cannot load " + PROPERTIES_FILENAME, ex);
        }
    }

    /**
     * Gets given global property from {@link
     * cz.muni.fi.mir.mathmlcanonicalization.Settings#PROPERTIES_FILENAME}
     * 
     * @param key property name
     * @return property value
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Sets given global property
     * 
     * @param key property name
     * @param value property value
     */
    public static void setProperty(String key, String value) {
        properties.put(key, value);
    }

    /**
     * Sets properties desired for MathML normalization purpose
     * 
     * @return initialized XMLInputFactory instance
     */
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

    /**
     * Sets properties desired for MathML normalization purpose
     * 
     * @return initialized SAXBuilder instance
     */
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