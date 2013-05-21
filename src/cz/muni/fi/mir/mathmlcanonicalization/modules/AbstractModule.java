package cz.muni.fi.mir.mathmlcanonicalization.modules;

import cz.muni.fi.mir.mathmlcanonicalization.Settings;
import java.io.IOException;
import java.io.InputStream;
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
            Logger.getLogger(Settings.class.getName()).log(
                    Level.FINER, "module properties loaded succesfully");
        } catch (IOException ex) {
            Logger.getLogger(Settings.class.getName()).log(
                    Level.SEVERE, "cannot load " + propertiesFilename, ex);
        }
    }
}
