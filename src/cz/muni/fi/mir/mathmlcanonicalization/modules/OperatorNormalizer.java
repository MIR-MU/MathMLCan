package cz.muni.fi.mir.mathmlcanonicalization.modules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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
    
    private static List<String> removeList;
    private static List<String> functionOperatorsList;
    
    public OperatorNormalizer() {
        loadProperties(PROPERTIES_FILENAME);
    }
    
    @Override
    public void execute(Document doc) {
        if (isEnabled("remove_empty") || isEnabled("remove")) {
            removeList = Arrays.asList(getProperty("remove.operators").split(" "));
            removeSpareOperators(doc.getRootElement());
        }
        if (isEnabled("function_app")) {
            functionOperatorsList = Arrays.asList(getProperty("function_app.operators").split(" "));
            //normalizeFunctionApplication(doc.getRootElement());
        }
    }
    
    private void removeSpareOperators(Element element) {
        if (element.getName().equals("mo")) {
            if (isEnabled("remove_empty") && element.getText().isEmpty()) element.detach();
            else if (isEnabled("remove") && removeList.contains(element.getText())) element.detach();
        } else {
            List<Element> children = element.getChildren();
            for(int i = 0; i < children.size(); i++) { // can't use iterator, due to detaching
                removeSpareOperators(children.get(i));
            }
        }
    }
    
    /*private void normalizeFunctionApplication(Element element) {
        if (element.getName().equals("mo")) {
            if (isEnabled("remove") && removeList.contains(element.getText())) element.detach();
            else if (element.getText().isEmpty() && isEnabled("remove_empty")) element.detach();
        } else {
            List<Element> children = element.getChildren();
            for(int i = 0; i < (children.size()-2); i++) { // can't use iterator, due to detaching
                if(children.get(i).getName().equals("mi") && children.get(i+1).getName().equals("mo")) {
                    if (functionOperatorsList.contains(children.get(i+1).getText())) {
                        Element parameter = children.get(i+2);
                        Element newParameter = new Element("mrow");
                        if (parameter.getName().equals("mo") && parameter.getText().equals("(")) {
                            for 
                        }
                        
                    }
                    
                }
                normalizeFunctionApplication();
            }
        }
    }*/
}
