package cz.muni.fi.mir.mathmlcanonicalization.modules;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Element;

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
    protected static final String OVERSCRIPT = "mover";
    protected static final String ROW = "mrow";
    protected static final String SUBSCRIPT = "msub";
    protected static final String SUPERSCRIPT = "msup";
    protected static final String SUBSUP = "msubsup";
    protected static final String UNDEROVER = "munderover";
    protected static final String UNDERSCRIPT = "munder";

    @Override
    public String getProperty(String key) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        final String property = properties.getProperty(key);
        if (property == null) {
            throw new IllegalArgumentException("Property '" + key + "' not set");
        }
        return property;
    }
    
    @Override
    public boolean isProperty(String key) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        return properties.getProperty(key) != null;
    }
    
    @Override
    public void setProperty(String key, String value) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        if (value == null) {
            throw new NullPointerException("value");
        }
        properties.setProperty(key, value);
    }
    
    @Override
    public Set<String> getPropertyNames() {
        return properties.stringPropertyNames();
    }
    
    protected boolean isEnabled(String key) {
        assert key != null;
        if (properties.getProperty(key).equals("1")
                || properties.getProperty(key).equals("true")) {
            return true;
        }
        if (properties.getProperty(key).equals("0")
                || properties.getProperty(key).equals("false")) {
            return false;
        }
        throw new IllegalArgumentException("'" + properties.getProperty(key)
                + "' is not a valid boolean value of " + key);
    }
    
    protected void loadProperties(String propertiesFilename) {
        assert propertiesFilename != null && !propertiesFilename.isEmpty();
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
        assert property != null && !property.isEmpty();
        return new HashSet<String>(Arrays.asList(getProperty(property).split(" ")));
    }
     
    protected boolean isOperator(final Element element, final String operator) {
        return isOperator(element) && element.getTextTrim().equals(operator);
    }
    
    protected boolean isOperator(final Element element) {
        assert element != null;
        return element.getName().equals(OPERATOR);
    }
}
