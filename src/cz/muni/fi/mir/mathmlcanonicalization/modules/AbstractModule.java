package cz.muni.fi.mir.mathmlcanonicalization.modules;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Module implementation with property loading
 * 
 * @author David Formanek
 */
abstract class AbstractModule implements Module {

    protected final Properties properties = new Properties();
    private static final Logger LOGGER = Logger.getLogger(AbstractModule.class.getName());
    // MathML elements
    protected static final String FENCED = "mfenced";
    protected static final String IDENTIFIER = "mi";
    protected static final String MATH = "math";
    protected static final String OPERATOR = "mo";
    protected static final String ROW = "mrow";
    protected static final String SUBSCRIPT = "msub";
    protected static final String SUPERSCRIPT = "msup";
    protected static final String SUBSUP = "msubsup";

    @Override
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    @Override
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
    
    @Override
    public Set<String> getPropertyNames() {
        return properties.stringPropertyNames();
    }
    
    protected boolean isEnabled(String key) {
        return properties.getProperty(key).equals("1")
                || properties.getProperty(key).equals("true");
    }
    
    protected void loadProperties(String propertiesFilename) {
        try {
            InputStream resourceAsStream = this.getClass().getResourceAsStream(propertiesFilename);
            if (resourceAsStream == null) {
                throw new IOException("cannot find the property file");
            }
            properties.load(resourceAsStream);
            LOGGER.log(Level.FINE,"Module properties loaded succesfully from {0}",
                    propertiesFilename);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Cannot load " + propertiesFilename, ex);
        }
    }
    
     protected Set<String> getPropertySet(final String property) {
        return new HashSet<String>(Arrays.asList(getProperty(property).split(" ")));
    }
}
