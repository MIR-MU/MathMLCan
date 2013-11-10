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
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import org.custommonkey.xmlunit.DoctypeInputStream;

/**
 * Utilities for manipulating DTD in XML documents.
 *
 * @author Michal Růžička
 */
public class DTDManipulator {

    /**
     * Inject into a XML document XHTML + MathML 1.1 DTD reference 
     * (<code>&lt;!DOCTYPE math SYSTEM "xhtml-math11.dtd"&gt;</code>).
     * Named MathML entities (<code>&amp;alpha;</code> ...) can be used in such 
     * a XML document.
     *
     * @param in XML document as InputStream
     * @return the XML document with injected XHTML + MathML 1.1 DTD reference
     */
    public static InputStream injectXHTMLPlusMathMLDTD(InputStream in) {

        return new DoctypeInputStream(in, "UTF-8", "math", "xhtml-math11.dtd");

    }

    /**
     * Remove any DTD reference from a XML document.
     *
     * @param in XML document as InputStream
     * @return the XML document without DTD reference
     */
    public static InputStream removeDTD(InputStream in) throws XMLStreamException {

        XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(in);
        ByteArrayOutputStream noDtdOutputStream = new ByteArrayOutputStream();
        XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter(noDtdOutputStream, "UTF-8");

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
