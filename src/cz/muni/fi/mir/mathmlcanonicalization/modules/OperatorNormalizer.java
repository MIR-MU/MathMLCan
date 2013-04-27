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
 * @author Jaroslav Dufek
 */
public class OperatorNormalizer extends AbstractModule implements DOMModule {
    
    /**
     * Path to the property file with module settings.
     */
    private static final String PROPERTIES_FILENAME = "/res/operator-normalizer.properties";
    
    final static Map<String, List<String>> replaceMap = new HashMap<String, List<String>>();
    
    public OperatorNormalizer() {
        loadProperties(PROPERTIES_FILENAME);
        // TODO: put some properties to the file
    }
    
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
            // TODO: rewrite this using getProperty()
            br = new BufferedReader(new InputStreamReader(
                    OperatorNormalizer.class.getResourceAsStream(
                        "operator-normalizer.properties"), "UTF-8"));
            // TODO: improve parsing
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
            // TODO: better exception handling
            e.printStackTrace();
        }
        try {
            // TODO: handle null pointer
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
}
