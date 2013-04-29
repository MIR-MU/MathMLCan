package cz.muni.fi.mir.mathmlcanonicalization.modules;

import cz.muni.fi.mir.mathmlcanonicalization.Settings;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import javax.xml.stream.*;

/**
 * Remove useless elements and attributes from MathML.
 * 
 * <h4>Input:</h4><ul>
 * <li>Well-formed MathML, first module</li>
 * <li>Property file(s) with names of elements and attributes for removal or
 * preservation</li>
 * <h4>Output:</h4>
 * The original code with:<ul>
 * <li>removed elements insignificant for the formula searching and indexing 
 * purpose (e.q. spacing and appearance altering tags) including the content
 * between open and close tag or preserving it (depending on the tag)</li>
 * <li>removed useless attributes but preserved those that are used in other
 * modules, e.q. separator attribute in mfenced element</li>
 * <li>removed attributes with default values?</li>
 * <li>removed redundant spaces?</li></ul>
 * 
 * @author Maros Kucbel
 * @date 2012-10-08T21:34:49+0200
 */
public class ElementMinimizer extends AbstractModule implements StreamModule {
    
    /**
     * Path to the property file with module settings.
     */
    private static final String PROPERTIES_FILENAME = "/res/element-minimizer.properties";
    
    public ElementMinimizer() {
        loadProperties(PROPERTIES_FILENAME);
        // TODO: put some properties to the file
    }
    
    /**
     * Removes attributes using StAX instead of DOM.
     * So far I haven't been able to receive all types of events (i.e. comment, start document 
     * are never received). Maybe it can be configured somehow, but I haven't figured it out yet.
     * 
     * Returned {@link ByteArrayOutputStream} can be converted to {@link InputStream} instance
     * using {@link ByteArrayInputStream#ByteArrayInputStream(byte[])}.
     */
    @Override
    public ByteArrayOutputStream execute(final InputStream input) {        
        String property = getProperty("remove_all");
        String[] removeAll = property.split(" ");
        List<String> removeWithChildren = Arrays.asList(removeAll);
        property = getProperty("remove");
        String[] remove = property.split(" ");
        List<String> removeKeepChildren = Arrays.asList(remove);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            final XMLInputFactory inputFactory = Settings.setupXMLInputFactory();
            final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            // stream for reading event from input stream
            final XMLStreamReader reader = inputFactory.createXMLStreamReader(input);
            // stream that writes events to given output stream
            final XMLStreamWriter writer = outputFactory.createXMLStreamWriter(outputStream, "UTF-8");
            writer.writeStartDocument(reader.getEncoding(), reader.getVersion());
            // depth of current branch, used when removing element with all its children
            int depth = 0;
            boolean math = false;
            // check for event
            while (reader.hasNext()) {
                // get event code
                final int event = reader.next();
                // based on event code choose action
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT: {
                        // write this element
                        // omit if it should be skipped
                        String name = reader.getLocalName();
                        if (name.equals("math")) {
                            math = true;
                        }
                        if (math) {
                            if (removeKeepChildren.contains(name)) {
                                continue;
                            }
                            // omit this element if it is marked to skip or is a child
                            // of such an element
                            if (removeWithChildren.contains(name)) {
                                depth++;
                            }
                            if (depth > 0) {
                                continue;
                            }
                        }
                        writer.writeStartElement(reader.getName().getPrefix(), name, reader.getName().getNamespaceURI());
                        for (int index = 0; index < reader.getAttributeCount(); ++index) {
                            final String attributeName = reader.getAttributeLocalName(index);
                            final String attributeValue = reader.getAttributeValue(index);
                            final String attributePrefix = reader.getAttributePrefix(index);
                            final String attributeNamespace = reader.getAttributeNamespace(index);
                            // write only chosen attributes
                            if (!math || (math && ("mathvariant".equals(attributeName)
                                    || ("linethickness".equals(attributeName) && "0".equals(attributeValue))
                                    || ("annotation-xml".equals(name) && "encoding".equals(attributeName))))) {
                                if (attributeNamespace==null) {
                                    writer.writeAttribute(attributeName, attributeValue);
                                } else {
                                    writer.writeAttribute(attributePrefix, attributeNamespace, attributeName, attributeValue);
                                }
                            }
                        }
                        for (int index = 0; index < reader.getNamespaceCount(); ++index) {
                            writer.writeNamespace(reader.getNamespacePrefix(index), reader.getNamespaceURI(index));
                        }

                        break;
                    }
                    case XMLStreamConstants.END_ELEMENT: {
                        if (math) {
                            String name = reader.getLocalName();
                            if (name.equals("math")) {
                                math = false;
                            }
                            if (removeKeepChildren.contains(name)) {
                                continue;
                            }
                            if (depth > 0) {
                                if (removeWithChildren.contains(name)) {
                                    depth--;
                                }
                                continue;
                            }
                        }
                        writer.writeEndElement();
                        break;
                    }
                    case XMLStreamConstants.CHARACTERS: {
                        // warning: white space is counted as CHARACTER event (new line after element)
                        if (depth > 0) {
                            continue;
                        }
                        writer.writeCharacters(reader.getText());
                        break;
                    }   
                    case XMLStreamConstants.ATTRIBUTE: {
                        String name = reader.getLocalName();
                        // I never got this event...
                        for (int index = 0; index < reader.getAttributeCount(); ++index) {
                            final String attributeName = reader.getAttributeLocalName(index);
                            final String attributeValue = reader.getAttributeValue(index);
                            // write only chosen attributes
                            if (!math || (math && ("mathvariant".equals(attributeName)
                                    || ("linethickness".equals(attributeName) && "0".equals(attributeValue))
                                    || ("annotation-xml".equals(name) && "encoding".equals(attributeName))))) {
                                writer.writeAttribute(attributeName, attributeValue);
                            }
                        }
                        break;
                    }
                    case XMLStreamConstants.END_DOCUMENT: {
                        writer.writeEndDocument();
                        break;
                    }
                    case XMLStreamConstants.DTD: {
                        writer.writeDTD(reader.getText());
                        break;
                    }
                    default: {
                        break;
                    }   
                }
            }
            writer.flush();
            writer.close();
        } catch (final XMLStreamException ex) {
            System.err.println(ex);
        }
        return outputStream;
    }
}
