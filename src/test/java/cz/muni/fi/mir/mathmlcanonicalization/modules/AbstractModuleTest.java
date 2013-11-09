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

import cz.muni.fi.mir.mathmlcanonicalization.Settings;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.junit.*;
import org.xml.sax.SAXException;

/**
 * Abstract class to allow descendants to simple compare desired and produced
 * XML documents by calling testXML method (tests idempotence by default)
 *
 * @author David Formanek
 */
@Ignore // no tests
abstract class AbstractModuleTest {

    private static final Logger LOGGER = Logger.getLogger(
            AbstractModuleTest.class.getName());
    private boolean shouldPrintProcessed = true;

    protected void setPrintProcessed(boolean shouldPrintProcessed) {
        this.shouldPrintProcessed = shouldPrintProcessed;
    }

    protected void testXML(Module instance, String testFile) {
        testXML(instance, testFile, true);
    }

    protected void testXML(Module instance, String testFile, boolean testIdempotence) {
        final InputStream processed = getProcessed(instance, testFile, shouldPrintProcessed);
        final InputStream canonical = getCanonical(testFile);
        XMLUnit.setIgnoreWhitespace(true);
        try {
            new XMLTestCase() {
            }.assertXMLEqual(getReader(canonical), getReader(processed));
        } catch (SAXException ex) {
            LOGGER.log(Level.SEVERE, "cannot compare XML streams", ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "problem with streams", ex);
        }
        if (testIdempotence) {
            testIdempotence(instance, testFile);
        }
    }

    protected void testIdempotence(Module instance, String testFile) {
        final InputStream processed = getProcessed(instance, testFile, false);
        final InputStream processedTwice = getProcessed(
                instance, getProcessed(instance, testFile, false), false);
        try {
            new XMLTestCase() {
            }.assertXMLEqual(getReader(processed), getReader(processedTwice));
        } catch (SAXException ex) {
            LOGGER.log(Level.SEVERE, "cannot compare XML streams", ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "problem with streams", ex);
        }
    }

    private InputStream getProcessed(Module instance, String testFile, boolean shouldPrint) {
        InputStream resourceStream = this.getClass().getResourceAsStream(testFile + ".original.xml");
        return getProcessed(instance, resourceStream, shouldPrint);
    }

    /**
     * Get input processed by specified module.
     *
     * @param instance a configured instance of the tested module
     * @param in input stream with original XML document
     * @return input stream with processed input
     */
    private InputStream getProcessed(Module instance, InputStream in, boolean shouldPrint) {
        if (instance instanceof StreamModule) {
            return getProcessed((StreamModule) instance, in, shouldPrint);
        } else if (instance instanceof DOMModule) {
            return getProcessed((DOMModule) instance, in, shouldPrint);
        } else {
            throw new UnsupportedOperationException("Module type not supported");
        }
    }

    private InputStream getProcessed(StreamModule instance, InputStream in, boolean shouldPrint) {
        try {
            ByteArrayOutputStream output = instance.execute(in);
            if (shouldPrint) {
                printDocument(output, "Output of " + instance);
            }
            return getInputStream(output);
        } catch (ModuleException ex) {
            Logger.getLogger(this.getClass().getName()).log(
                    Level.SEVERE, "cannot execute the module", ex);
            return null;
        }
    }

    private InputStream getProcessed(DOMModule instance, InputStream in, boolean shouldPrint) {
        SAXBuilder builder = Settings.setupSAXBuilder();
        Document doc;
        XMLOutputter serializer = new XMLOutputter();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            doc = builder.build(in);
            instance.execute(doc);
            serializer.output(doc, output);
        } catch (JDOMException ex) {
            Logger.getLogger(this.getClass().getName()).log(
                    Level.SEVERE, "cannot build document", ex);
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(
                    Level.SEVERE, "cannot convert between stream and DOM", ex);
        } catch (ModuleException ex) {
            Logger.getLogger(this.getClass().getName()).log(
                    Level.SEVERE, "cannot execute the module", ex);
        }
        if (shouldPrint) {
            printDocument(output, "Output of " + instance);
        }
        return getInputStream(output);
    }

    private InputStream getCanonical(String testFile) {
        return this.getClass().getResourceAsStream(testFile + ".canonical.xml");
    }

    private InputStreamReader getReader(InputStream inputStream) {
        return new InputStreamReader(inputStream);
    }

    private InputStream getInputStream(ByteArrayOutputStream output) {
        return new ByteArrayInputStream(output.toByteArray());
    }

    private void printDocument(ByteArrayOutputStream output, String header) {
        System.out.println(header);
        System.out.println("--------------------");
        try {
            output.writeTo(System.out);
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(
                    Level.WARNING, "cannot print result", ex);
        }
        System.out.println("--------------------");
        System.out.println();
    }
}
