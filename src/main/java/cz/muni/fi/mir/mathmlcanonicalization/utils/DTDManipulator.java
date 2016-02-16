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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.custommonkey.xmlunit.DoctypeInputStream;

import cz.muni.fi.mir.mathmlcanonicalization.Settings;

/**
 * Utilities for manipulating DTD in XML documents.
 *
 * @author Michal Růžička
 */
public class DTDManipulator {

    /**
     * Inject into a XML document XHTML 1.1 plus MathML 2.0 plus SVG 1.1 DTD
     * reference ({@code <!DOCTYPE math SYSTEM "xhtml-math11-f.dtd">}). Named
     * MathML entities ({@code &alpha;} ...) can be used in such a XML document.
     *
     * @param in XML document as InputStream
     * @return the XML document with injected XHTML 1.1 plus MathML 2.0 plus SVG
     * 1.1 DTD reference
     */
    public static InputStream injectXHTML11PlusMathML20PlusSVG11DTD(InputStream in) {

        return new DoctypeInputStream(in, "UTF-8", "math", "xhtml-math11-f.dtd");

    }

    /**
     * Inject into a XML document MathML 2.0 DTD reference
     * ({@code  <!DOCTYPE math SYSTEM "mathml2.dtd">}). Named MathML entities
     * ({@code &alpha;} ...) can be used in such a XML document.
     *
     * @param in XML document as InputStream
     * @return the XML document with injected MathML 2.0 DTD reference
     */
    public static InputStream injectMathML20DTD(InputStream in) {

        return new DoctypeInputStream(in, "UTF-8", "math", "mathml2.dtd");

    }

    /**
     * Inject into a XML document MathML 3.0 DTD reference
     * ({@code <!DOCTYPE math SYSTEM "mathml3.dtd">}). Named MathML entities
     * ({@code &alpha;} ...) can be used in such a XML document.
     *
     * @param in XML document as InputStream
     * @return the XML document with injected MathML 3.0 DTD reference
     */
    public static InputStream injectMathML30DTD(InputStream in) {

        return new DoctypeInputStream(in, "UTF-8", "math", "mathml3.dtd");

    }

    /**
     * Remove any DTD reference from a XML document.
     *
     * @param in XML document as InputStream
     * @return the XML document without DTD reference
     * @throws javax.xml.stream.XMLStreamException an error with XML processing
     * occurs
     */
    public static InputStream removeDTD(InputStream in) throws XMLStreamException {
        byte[] buffer = removeDTDAndReturnOutputStream(in).toByteArray();

        return new ByteArrayInputStream(buffer);

    }

    public static ByteArrayOutputStream removeDTDAndReturnOutputStream(InputStream in) throws XMLStreamException {

        XMLEventReader reader = Settings.defaultXmlInputFactory().createXMLEventReader(in);
        ByteArrayOutputStream noDtdOutputStream = new ByteArrayOutputStream();
        XMLEventWriter writer = Settings.xmlOutputFactory().createXMLEventWriter(noDtdOutputStream, "UTF-8");

        while (reader.hasNext()) {
            XMLEvent event = (XMLEvent) reader.next();

            if (event.getEventType() != XMLStreamConstants.DTD) {
                writer.add(event);
            }
        }
        writer.flush();

        return noDtdOutputStream;
    }

}
