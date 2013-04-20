package cz.muni.fi.mir.mathmlcanonicalization.modules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Normalize the way to express an function applied to arguments in MathML.
 * <h4>Input</h4> Well-formed MathML, not processed by MrowMinimizer yet
 * <h4>Output</h4> The original code with:<ul> <li>removed entities for function
 * application (and multiplying where it should not be)</li> <li>the name of
 * function placed in &lt;mi&gt; element (not &lt;mo&gt;)</li> <li>function
 * arguments placed in parentheses and &lt;mrow&gt; (or leave &lt;mrow&gt;
 * adding for the MrowNormalizer module?)</li></ul>
 *
 * @author David Formanek
 */
public class OperatorNormalizer implements DOMModule {

    final static Map<String, List<String>> replaceMap = new HashMap<String, List<String>>();
    
    @Override
    public void execute(Document doc) {
        normalizeOperators(doc.getRootElement());
    }
    
    private static void normalizeOperators(Element element) {
        if (element.getName().equals("mo")) {
            for (Map.Entry<String, List<String>> map : replaceMap.entrySet()) {
                if (map.getValue().contains(element.getText())) {
                    element.setText(map.getKey());
                    break;
                }
            }

        }
        for (Element e : element.getChildren()) {
            normalizeOperators(e);
        }
    }
    
    static {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(
                    OperatorNormalizer.class.getResourceAsStream(
                        "OperatorNormalizer.properties"), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                String representant = line.substring(0, line.indexOf("="));
                String[] replacees = (line.substring(line.indexOf("=") + 1)).split(",");
                List<String> replaceesList = new ArrayList<String>();
                for (String s : replacees) {
                    if (s.length() > 1) {
                        s = Character.valueOf((char) Integer.parseInt(s, 16)).toString();
                    }
                    replaceesList.add(s);
                }
                replaceMap.put(representant, replaceesList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    // OperatorNormalizer is a DOM module for now
    /*
    public static ByteArrayOutputStream execute(final InputStream input) {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            XMLInputFactory inputFactory = Settings.setupXMLInputFactory();
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            // stream for reading event from input stream
            XMLStreamReader reader = inputFactory.createXMLStreamReader(input);
            // stream that writes events to given output stream
            final XMLStreamWriter writer = outputFactory.createXMLStreamWriter(outputStream, "UTF-8");
            writer.writeStartDocument(reader.getEncoding(), reader.getVersion());
            // check for event
            boolean mo = false;
            while (reader.hasNext()) {
                // get event code
                final int event = reader.next();
                // based on event code choose action
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT: {
                        String localName = reader.getLocalName();
                        writer.writeStartElement(reader.getName().getPrefix(), localName, reader.getName().getNamespaceURI());
                        for (int index = 0; index < reader.getAttributeCount(); ++index) {
                            if (reader.getAttributeNamespace(index)!=null) {
                                writer.writeAttribute(reader.getAttributePrefix(index), reader.getAttributeNamespace(index), reader.getAttributeLocalName(index), reader.getAttributeValue(index));
                            } else {
                                writer.writeAttribute(reader.getAttributeLocalName(index), reader.getAttributeValue(index));
                            }
                        }
                        for (int index = 0; index < reader.getNamespaceCount(); ++index) {
                            writer.writeNamespace(reader.getNamespacePrefix(index), reader.getNamespaceURI(index));
                        }

                        if (localName.equals("mo")) {
                            mo = true;
                        }
                        break;
                    }
                    case XMLStreamConstants.END_ELEMENT: {
                        writer.writeEndElement();
                        break;
                    }
                    case XMLStreamConstants.CHARACTERS: {
                        String text = reader.getText();
                        if (mo) {
                            for (Map.Entry<String, List<String>> map : replaceMap.entrySet())
                            if (map.getValue().contains(text)) {
                                text = map.getKey();
                                break;
                            }
                        }
                        writer.writeCharacters(text);
                        break;
                    }
                    case XMLStreamConstants.ENTITY_REFERENCE: {
                        break;
                    }
                    case XMLStreamConstants.END_DOCUMENT: {
                        writer.writeEndDocument();
                        break;
                    }
                    case XMLStreamConstants.COMMENT: {
                        break;
                    }
                    case XMLStreamConstants.DTD: {
                        writer.writeDTD(reader.getText());
                    }
                    default: {
                        break;
                    }
                }
            }
            writer.flush();
            writer.close();
        } catch (final XMLStreamException ex) {
            ex.printStackTrace();
        }
        return outputStream;
    }
    */
}
