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
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLResolver;
import javax.xml.validation.SchemaFactory;

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

    private static final Logger log = Logger.getLogger(Settings.class.getName());

    // thread local which allow creation of factories only once per thread
    private static final ThreadLocal<XmlFactories> xmlFactories = new ThreadLocal<XmlFactories>() {
        @Override
        protected XmlFactories initialValue() {
            return new XmlFactories();
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

    private static final Properties PROPERTIES = readConfiguration();

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
     * NB: this method creates factory only once per thread
     *
     * @return initialized XMLInputFactory instance
     */
    public static XMLInputFactory setupXMLInputFactory() {
        return xmlFactories.get().getXmlInputFactory();
    }

    /**
     * Returns XMLInputFactory instance with default configuration.
     *
     * NB: setupXMLInputFactory returns different factory customized for MathML
     * NB: this method creates factory only once per thread
     */
    public static XMLInputFactory defaultXmlInputFactory() {
        return xmlFactories.get().getDefaultXmlInputFactory();
    }

    /**
     * Returns XMLOutputFactory instance with default configuration.
     *
     * NB: this method creates factory only once per thread
     */
    public static XMLOutputFactory xmlOutputFactory() {
        return xmlFactories.get().getXmlOutputFactory();
    }

    /**
     * Returns DocumentBuilderFactory instance with default configuration.
     *
     * NB: this method creates factory only once per thread
     */
    public static DocumentBuilderFactory documentBuilderFactory() {
        return xmlFactories.get().getDocumentBuilderFactory();
    }

    /**
     * Returns SchemaFactory instance dedicated to XML W3C Schema.
     *
     * NB: this method creates factory only once per thread
     */
    public static SchemaFactory xmlSchemaFactory() {
        return xmlFactories.get().getXmlSchemaFactory();
    }

    /**
     * Returns SAXBuilder dedicated for MathML normalization
     *
     * NB: this method creates factory only once per thread
     */
    public static SAXBuilder setupSAXBuilder() {
        return xmlFactories.get().getSaxBuilder();
    }

    /**
     * Returns URL of classpath resource defined by specified property
     */
    public static URL getResourceFromProperty(String property) {
        String resource = getProperty(property);
        URL result = Settings.class.getResource(resource);
        if (result == null) {
            throw new ConfigError("Classpath resource '" + resource + "' defined by property '" + property
                    + " does not exist");
        }
        return result;
    }

    /**
     * Returns stream of classpath resource defined by specified property
     */
    public static InputStream getStreamFromProperty(String property) {
        try {
            return getResourceFromProperty(property).openStream();
        } catch (IOException e) {
            throw new ConfigError("Classpath resource resource defined by property '" + property
                    + " could not be read", e);
        }
    }

    private Settings() {
        assert false;
    }

    private static Properties readConfiguration() throws ConfigError {
        Properties result = new Properties();

        final InputStream resourceAsStream = Settings.class.getResourceAsStream(PROPERTIES_FILENAME);
        if (resourceAsStream == null) {
            throw new ConfigError("cannot find property file " + PROPERTIES_FILENAME);
        }

        try {
            result.load(resourceAsStream);
        } catch (IOException e) {
            throw new ConfigError("Error while reading configuration");
        }
        log.finer("canonicalizer properties loaded succesfully");

        return result;
    }

    // single container for XML factories
    private static class XmlFactories {

        private final SAXBuilder saxBuilder = createSaxBuilder();
        private final XMLInputFactory xmlInputFactory = createXmlInputFactory();
        private final XMLInputFactory defaultXmlInputFactory = XMLInputFactory.newInstance();
        private final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        private final SchemaFactory xmlSchemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        public SAXBuilder getSaxBuilder() {
            return saxBuilder;
        }

        public XMLInputFactory getXmlInputFactory() {
            return xmlInputFactory;
        }

        public XMLInputFactory getDefaultXmlInputFactory() {
            return defaultXmlInputFactory;
        }

        public XMLOutputFactory getXmlOutputFactory() {
            return xmlOutputFactory;
        }

        public DocumentBuilderFactory getDocumentBuilderFactory() {
            return documentBuilderFactory;
        }

        public SchemaFactory getXmlSchemaFactory() {
            return xmlSchemaFactory;
        }

        private static SAXBuilder createSaxBuilder() {
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
                        return new InputSource(getStreamFromProperty(XHTMLPlusMATHMLPlusSVGDTD));
                    }
                    return null;
                }
            });

            return builder;
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
                        return getStreamFromProperty(XHTMLPlusMATHMLPlusSVGDTD);
                    }
                    return null;
                }
            });
            return inputFactory;
        }

    }

}
