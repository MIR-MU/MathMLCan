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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Module implementation with property loading
 *
 * @author David Formanek
 */
abstract class AbstractModule implements Module {

    protected final Properties properties = new Properties();
    private static final Logger LOGGER = Logger.getLogger(AbstractModule.class.getName());
    protected static final Namespace MATHMLNS = Namespace.getNamespace("http://www.w3.org/1998/Math/MathML");
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

    public void declareProperty(String key) {
        properties.setProperty(key, "");
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

    protected Set<String> getPropertySet(final String property) {
        assert property != null && !property.isEmpty();
        return new HashSet<>(Arrays.asList(getProperty(property).split(" ")));
    }

    protected boolean isOperator(final Element element, final String operator) {
        return isOperator(element) && element.getTextTrim().equals(operator);
    }

    protected boolean isOperator(final Element element) {
        assert element != null;
        return element.getName().equals(OPERATOR);
    }

    protected void replaceElement(final Element toReplace, final String replacementName) {
        assert toReplace != null && replacementName != null;
        assert !replacementName.isEmpty();
        final Element parent = toReplace.getParentElement();
        assert parent != null;
        final Element replacement = new Element(replacementName, toReplace.getNamespace());
        replacement.addContent(toReplace.removeContent());
        final List<Attribute> attributes = toReplace.getAttributes();
        while (attributes.size() > 0) {
            Attribute currentAttribute = attributes.get(0);
            replacement.setAttribute(currentAttribute.detach());
        }
        final int parentIndex = parent.indexOf(toReplace);
        parent.removeContent(parentIndex);
        parent.addContent(parentIndex, replacement);
        LOGGER.log(Level.FINE, "{0} replaced with {1}",
                new Object[]{toReplace, replacementName});
    }

}
