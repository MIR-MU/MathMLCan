package cz.muni.fi.mir.mathmlcanonicalization.modules;

import cz.muni.fi.mir.mathmlcanonicalization.Settings;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Module implementation with property loading
 * 
 * @author David Formanek
 */
abstract class AbstractModule implements Module {

    protected Properties properties = new Properties();

    @Override
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    @Override
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
    
    protected void loadProperties(String propertiesFilename) {
        try {
            InputStream resourceAsStream = this.getClass().getResourceAsStream(propertiesFilename);
            if (resourceAsStream == null) {
                throw new IOException("cannot find the property file");
            }
            properties.load(resourceAsStream);
            Logger.getLogger(Settings.class.getName()).log(
                    Level.FINER, "module properties loaded succesfully");
        } catch (IOException ex) {
            Logger.getLogger(Settings.class.getName()).log(
                    Level.SEVERE, "cannot load " + propertiesFilename, ex);
        }
    }
    
    protected void loadConfiguration() {
        try {
            if (Settings.configDocument != null) {
                XPath xpath = XPathFactory.newInstance().newXPath();
                XPathExpression expr = xpath.compile("//config/module[@name='" + this.getClass().getSimpleName() + "']/property");
                Object result = expr.evaluate(Settings.configDocument, XPathConstants.NODESET);
                
                NodeList nodes = (NodeList) result;
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element propertyElement = (Element) nodes.item(i);
                    String property = propertyElement.getAttribute("name");
                   
                    if (property != null) {
                        properties.setProperty(property, propertyElement.getTextContent());
                    }
                }
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(AbstractModule.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}