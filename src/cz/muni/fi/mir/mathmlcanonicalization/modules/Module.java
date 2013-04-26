package cz.muni.fi.mir.mathmlcanonicalization.modules;

/**
 * Every canonicalization module
 * 
 * @author David Formanek
 */
public interface Module {
    /**
     * Gets given property of the module
     * 
     * @param key property name
     * @return property value
     */
    public String getProperty(String key);

    /**
     * Sets given property of the module
     * 
     * @param key property name
     * @param value property value
     */
    public void setProperty(String key, String value);
}
