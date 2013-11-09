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

import gnu.regexp.RE;
import gnu.regexp.REException;
import gnu.regexp.REFilterInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * Utilities for manipulating DTD in XML documents.
 *
 * @author Michal Růžička
 */
public class DTDManipulator {

    public static InputStream injectXHTMLPlusMathMLDTD(InputStream in) {

        try {
            in = new REFilterInputStream(
                    // Remove any existing DTD
                    new REFilterInputStream(in, new RE("<!DOCTYPE [^>]+>\n?"), ""),
                    // Add XHTML + MathML 1.1 DTD after XML prolog
                    new RE("(<\\?xml.*\\?>)"),
                    "$1\n<!DOCTYPE math SYSTEM \"xhtml-math11.dtd\">");
        } catch (REException ex) {
            Logger.getLogger(DTDManipulator.class.getName()).log(Level.WARNING,
                    "DOCTYPE injection failed", ex);
        }

        return in;

    }

    public static InputStream removeDTD(InputStream in) throws XMLStreamException {

        XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(in);
        ByteArrayOutputStream noDtdOutputStream = new ByteArrayOutputStream();
        XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter(noDtdOutputStream);

        while (reader.hasNext()) {
            XMLEvent event = (XMLEvent) reader.next();

            if (event.getEventType() != event.DTD) {
                writer.add(event);
            }
        }
        writer.flush();

        return new ByteArrayInputStream(noDtdOutputStream.toByteArray());

    }
}
