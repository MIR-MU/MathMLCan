/**
 * Copyright 2013 MIR@MU Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
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
