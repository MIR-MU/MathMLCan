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
package cz.muni.fi.mir.mathmlcanonicalization;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.FactoryConfigurationError;
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

    private static final ThreadLocal<XMLInputFactory> xmlInputFactory = new ThreadLocal<XMLInputFactory>() {
        protected XMLInputFactory initialValue() {
            return createXmlInputFactory();
        }
    };
    
    /**
     * Path to the property file with canonicalizer settings.
     */
    private static final String PROPERTIES_FILENAME = "settings.properties";
    /**
     * Name of the property containing path to the MathML DTD
     */
    private static final String XHTMLPlusMATHMLPlusSVGDTD = "dtdXHTMLPlusMathMLPlusSVG";
    private static final Properties PROPERTIES = new Properties();

    // load default properties from the file specified by PROPERTIES_FILENAME
    static {
        try {
            final InputStream resourceAsStream = Settings.class.getResourceAsStream(PROPERTIES_FILENAME);
            if (resourceAsStream == null) {
                throw new IOException("cannot find the property file");
            }
            PROPERTIES.load(resourceAsStream);
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
     * @return property value (never null)
     * @throws IllegalArgumentException when property not set
     */
    public static String getProperty(String key) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        final String property = PROPERTIES.getProperty(key);
        if (property == null) {
            throw new IllegalArgumentException("Property '" + key + "' not set");
        }
        return property;
    }

    /**
     * Finds out if the global property is set
     *
     * @param key property name
     * @return true if property is set, false otherwise
     */
    public static boolean isProperty(String key) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        return PROPERTIES.getProperty(key) != null;
    }

    /**
     * Sets given global property
     *
     * @param key property name
     * @param value property value
     */
    public static void setProperty(String key, String value) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        if (value == null) {
            throw new NullPointerException("value");
        }
        PROPERTIES.put(key, value);
    }

    /**
     * Sets properties desired for MathML normalization purpose
     *
     * @return initialized XMLInputFactory instance
     */
    public static XMLInputFactory setupXMLInputFactory() {
        return xmlInputFactory.get();
    }

    private static XMLInputFactory createXmlInputFactory() throws FactoryConfigurationError {
        final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, true);
        inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, true);
        inputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, true);
        inputFactory.setProperty(XMLInputFactory.RESOLVER, new XMLResolver() {
            @Override
            public Object resolveEntity(String publicID, String systemID, String baseURI, String namespace) {
                if (systemID.endsWith("dtd")) {
                    String dtdLocation = Settings.getProperty(Settings.XHTMLPlusMATHMLPlusSVGDTD);
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
        final SAXBuilder builder = new SAXBuilder();
        builder.setXMLReaderFactory(XMLReaders.NONVALIDATING);
        builder.setFeature("http://xml.org/sax/features/validation", false);
        builder.setFeature("http://xml.org/sax/features/external-general-entities", true);
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", true);
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);
        builder.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String publicId, String systemId) {
                if (publicId.equalsIgnoreCase("-//W3C//DTD XHTML 1.1 plus MathML 2.0 plus SVG 1.1//EN")
                        || publicId.equalsIgnoreCase("-//W3C//DTD XHTML 1.1 plus MathML 2.0//EN")
                        || systemId.endsWith("xhtml-math11-f.dtd")) {
                    String dtdLocation = Settings.getProperty(Settings.XHTMLPlusMATHMLPlusSVGDTD);
                    return new InputSource(
                            Settings.class.getResourceAsStream(dtdLocation));
                }
                return null;
            }
        });

        return builder;
    }

    private Settings() {
        assert false;
    }
}
