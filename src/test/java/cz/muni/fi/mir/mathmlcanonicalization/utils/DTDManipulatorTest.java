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
package cz.muni.fi.mir.mathmlcanonicalization.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.xml.sax.SAXException;
import static org.junit.Assert.*;

/**
 *
 * @author Michal Růžička
 */
public class DTDManipulatorTest {

    private static final Logger LOGGER = Logger.getLogger(DTDManipulatorTest.class.getName());
    private static final String RESOURCE_SUBDIR = DTDManipulatorTest.class.getSimpleName() + "/";

    /**
     * Test of injectXHTML11PlusMathML20PlusSVG11DTD method, of class DTDManipulator.
     */
    @Test
    public void testInjectXHTMLPlusMathMLPlusSVGDTD() throws IOException {

        System.out.println("testInjectXHTMLPlusMathMLDTD");

        InputStream in = this.getClass().getResourceAsStream(RESOURCE_SUBDIR + "injectXHTMLPlusMathMLPlusSVGDTD.input.xml");
        InputStream expResult = this.getClass().getResourceAsStream(RESOURCE_SUBDIR + "injectXHTMLPlusMathMLPlusSVGDTD.output.xml");
        InputStream result = DTDManipulator.injectXHTML11PlusMathML20PlusSVG11DTD(in);

        StringWriter resultWriter = new StringWriter();
        IOUtils.copy(result, resultWriter);
        String resultString = resultWriter.toString();
        StringWriter expResultWriter = new StringWriter();
        IOUtils.copy(expResult, expResultWriter);
        String expResultString = expResultWriter.toString();

        assertEquals("DTD not properly injected", expResultString, resultString);

    }

    /**
     * Test of removeDTD method, of class DTDManipulator.
     */
    @Test
    public void testRemoveDTD() throws Exception {

        System.out.println("removeDTD");

        InputStream in = this.getClass().getResourceAsStream(RESOURCE_SUBDIR + "testRemoveDTD.input.xml");
        InputStreamReader result = new InputStreamReader(DTDManipulator.removeDTD(in));

        StringWriter resultWriter = new StringWriter();
        IOUtils.copy(result, resultWriter);
        String resultString = resultWriter.toString();

        assertFalse("DTD not properly removed – string '<!DOCTYPE' presented in the result", resultString.contains("<!DOCTYPE"));

        InputStreamReader expResult = new InputStreamReader(this.getClass().getResourceAsStream(RESOURCE_SUBDIR + "testRemoveDTD.output.xml"));
        // Once read, the input stream can not be read again.
        in = this.getClass().getResourceAsStream(RESOURCE_SUBDIR + "testRemoveDTD.input.xml");
        result = new InputStreamReader(DTDManipulator.removeDTD(in));
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setExpandEntityReferences(true);
        try {
            new XMLTestCase() {
            }.assertXMLEqual("DTD not properly removed – result differs from expected code", expResult, result);
        } catch (SAXException ex) {
            LOGGER.log(Level.SEVERE, "cannot compare XML streams", ex);
        }

    }
}