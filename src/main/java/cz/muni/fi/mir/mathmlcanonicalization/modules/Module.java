package cz.muni.fi.mir.mathmlcanonicalization.modules;

import java.util.Set;

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
     * @return property value (not null)
     * @throws IllegalArgumentException when property not set
     */
    public String getProperty(String key);

    /**
     * Finds out if the property is set
     * 
     * @param key property name
     * @return true if property is set, false otherwise
     */
    public boolean isProperty(String key);
    
    /**
     * Sets given property of the module
     * 
     * @param key property name
     * @param value property value
     */
    public void setProperty(String key, String value);
    
    /**
     * Gets the module property names
     * 
     * @return the module property names of type String
     */
    public Set<String> getPropertyNames();
}
