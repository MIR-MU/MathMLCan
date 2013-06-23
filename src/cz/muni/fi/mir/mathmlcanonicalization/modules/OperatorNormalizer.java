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
            normalizeFunctionApplication(doc.getRootElement());
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
    
    private void normalizeFunctionApplication(Element element) {
        List<Element> children = element.getChildren(); // this list is "live" changes to it changes element
        for(int i = 0; i < children.size(); i++) {
            if((i < children.size()-2) && children.get(i).getName().equals("mi") && children.get(i+1).getName().equals("mo")
            && functionOperatorsList.contains(children.get(i+1).getText())) { // check for function app operators
                int parPos = i+2;
                Element parameter = children.get(parPos);
                
                Element newParameter = new Element("mrow"); // mrow in which the parameter will be stored
                
                if (parameter.getName().equals("mrow")) {
                    if(hasInsideBrackets(parameter)) {
                        children.get(i+1).detach(); // mrow is in right format -> just detach operator
                    } else {
                        parameter.addContent(1, new Element("mo").setText("(")); // add left bracket to mrow
                        parameter.addContent(new Element("mo").setText(")")); // add right bracket to mrow
                        children.get(i+1).detach(); // detach funct app operator
                    }
                    continue; // no need to set newParameter
                } else if (parameter.getName().equals("mo") && parameter.getText().equals("(")) {
                    int bracketsDepth = 1;
                    newParameter.addContent(children.get(parPos).detach());
                    
                    while ((parPos < children.size()) && (bracketsDepth > 0)) {
                        if (children.get(parPos).getName().equals("mo")
                        && children.get(parPos).getText().equals("("))
                            bracketsDepth++;
                        else if (children.get(parPos).getName().equals("mo")
                        && children.get(parPos).getText().equals(")"))
                            bracketsDepth--;
                        newParameter.addContent(children.get(parPos).detach());
                    }
                    for (; bracketsDepth > 0; bracketsDepth--) { // add missing right brackets
                        newParameter.addContent(new Element("mo").setText(")"));
                    }
                } else { // if the paramether is neither mrow or (
                    newParameter.addContent(new Element("mo").setText("(")); // add left bracket
                    newParameter.addContent(children.get(parPos).detach());
                    newParameter.addContent(new Element("mo").setText(")")); // add right bracket
                }
                
                children.set(i+1, newParameter); // replace function app operator with newParameter
            } else { // if there isnt start of function application apply normalization on children
                normalizeFunctionApplication(children.get(i));
            }
            
        }
    }
    private boolean hasInsideBrackets(Element mrow) {
        List<Element> children = mrow.getChildren();
        if ((children.size() > 1) && children.get(0).getName().equals("mo")
        && children.get(0).getText().equals("(")) {
            int bracketsDepth = 1;
            for (int i = 1; i < children.size(); i++) {
                if (children.get(i).getName().equals("mo") && children.get(i).getText().equals("("))
                    bracketsDepth++;
                else if (children.get(i).getName().equals("mo") && children.get(i).getText().equals(")"))
                    bracketsDepth--;
                if (bracketsDepth == 0) {
                    if (i < children.size()-1)
                        return false;
                    if (i == children.size()-1)
                        return true;
                }
            }
        }
        return false;
    }
}
