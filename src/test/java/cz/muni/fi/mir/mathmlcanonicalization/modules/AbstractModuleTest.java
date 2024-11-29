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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.junit.jupiter.api.*;

/**
 * Abstract class to allow descendants to simple compare desired and produced
 * XML documents by calling testXML method (tests idempotence by default)
 *
 * @author David Formanek
 */
@Disabled // no tests
abstract class AbstractModuleTest {

    private static final Logger LOGGER = Logger.getLogger(
            AbstractModuleTest.class.getName());
    private boolean shouldPrintProcessed = true;

    protected void setPrintProcessed(boolean shouldPrintProcessed) {
        this.shouldPrintProcessed = shouldPrintProcessed;
    }

    protected void testXML(Module instance, String testFile) {
        testHTML(instance, testFile, true);
    }

    protected void testHTML(Module instance, String testFile, boolean testIdempotence) {
        try {
            final String processed = new String(getProcessed(instance, testFile, shouldPrintProcessed).readAllBytes(),
                    StandardCharsets.UTF_8);
            final String canonical = new String(getCanonical(testFile).readAllBytes(), StandardCharsets.UTF_8);

            assertEquals(normalize(canonical), normalize(processed));
        } catch (IOException e) {

        }

        if (testIdempotence) {
            testIdempotence(instance, testFile);
        }
    }

    protected void testIdempotence(Module instance, String testFile) {
        try {
            final String processed = new String(getProcessed(instance, testFile, false).readAllBytes(),
                    StandardCharsets.UTF_8);
            final String processedTwice = new String(
                    getProcessed(instance, getProcessed(instance, testFile, false), false).readAllBytes(),
                    StandardCharsets.UTF_8);

            assertEquals(normalize(processed), normalize(processedTwice));
        } catch (IOException e) {

        }
    }

    private static String normalize(String original) {
        return original.replaceAll("(^\\s+)|(\\s*\n+\\s+)", "") // remove whitespaces at line start & newlines
                .replaceAll("(>)(\\s+)(<)", "$1$3") // remove white space between tags
                .toLowerCase();
    }

    private InputStream getProcessed(Module instance, String testFile, boolean shouldPrint) {
        InputStream resourceStream = this.getClass().getResourceAsStream(testFile + ".original.html");
        return getProcessed(instance, resourceStream, shouldPrint);
    }

    /**
     * Get input processed by specified module.
     *
     * @param instance a configured instance of the tested module
     * @param in       input stream with original XML document
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
            LOGGER.log(Level.SEVERE, "cannot execute the module", ex);
            return null;
        }
    }

    private InputStream getProcessed(DOMModule instance, InputStream in, boolean shouldPrint) {
        Document doc;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            doc = Jsoup.parse(in, null, "");
            instance.execute(doc);
            output.write(doc.html().getBytes("UTF-8"));
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "cannot convert between stream and DOM", ex);
        } catch (ModuleException ex) {
            LOGGER.log(Level.SEVERE, "cannot execute the module", ex);
        }
        if (shouldPrint) {
            printDocument(output, "Output of " + instance);
        }
        return getInputStream(output);
    }

    private InputStream getCanonical(String testFile) {
        return this.getClass().getResourceAsStream(testFile + ".canonical.html");
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
            LOGGER.log(Level.WARNING, "cannot print result", ex);
        }
        System.out.println("--------------------");
        System.out.println();
    }

}
