package cz.muni.fi.mir.mathmlcanonicalization.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;

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
    
    // MathML elements
    private static final String IDENTIFIER = "mi";
    private static final String OPERATOR = "mo";
    private static final String ROW = "mrow";
    // properties key names
    private static final String NORMALIZE_FUNCTIONS = "normalizefunctions";
    private static final String APPLY_FUNCTION_OPERATORS = "functionoperators";
    private static final String REMOVE_EMPTY_OPERATORS = "removeempty";
    private static final String OPERATORS_TO_REMOVE = "removeoperators";
    private static final String OPERATOR_REPLACEMENTS = "replaceoperators";
    private static final String COLON_REPLACEMENT = "colonreplacement";
    
    public OperatorNormalizer() {
        loadProperties(PROPERTIES_FILENAME);
    }
    
    @Override
    public void execute(final Document doc) {
        if (doc == null) {
            throw new IllegalArgumentException("document is null");
        }
        final Element root = doc.getRootElement();
        if (isEnabled(REMOVE_EMPTY_OPERATORS) || !getProperty(OPERATORS_TO_REMOVE).isEmpty()) {
            removeSpareOperators(root, getPropertyCollection(OPERATORS_TO_REMOVE));
        }
        final Map<String, String> replaceMap = getPropertyMap(OPERATOR_REPLACEMENTS);
        if (!getProperty(COLON_REPLACEMENT).isEmpty()) {
            replaceMap.put(":", getProperty(COLON_REPLACEMENT));
        }
        if (!replaceMap.isEmpty()) {
            replaceOperators(root, replaceMap);
        }
        if (isEnabled(NORMALIZE_FUNCTIONS)) {
            normalizeFunctionApplication(root, getPropertyCollection(APPLY_FUNCTION_OPERATORS));
        }
    }
    
    private void removeSpareOperators(final Element element, final Collection<String> spareOperators) {
        final List<Element> children = element.getChildren();
        for (int i = 0; i < children.size(); i++) {
            final Element actual = children.get(i); // actual element
            if (isOperator(actual)) {
                if (isSpareOperator(actual, spareOperators)){
                    actual.detach();
                    i--; // move iterator back after detaching so it points to next element
                }
            } else {
                removeSpareOperators(actual, spareOperators);
            }
        }
    }
    
    private boolean isSpareOperator(final Element operator, final Collection<String> spareOperators) {
        return (isEnabled(REMOVE_EMPTY_OPERATORS) && operator.getText().isEmpty())
                || (spareOperators.contains(operator.getTextTrim()));
    }
    
    private void normalizeFunctionApplication(final Element element,
            final Collection<String> functionOperators) {
        final List<Element> children = element.getChildren();
        for (int i = 0; i < children.size(); i++) {
            if (isFunction(i, children, functionOperators)) {
                final int parameterPosition = i + 2;
                Element parameter = children.get(parameterPosition);
                // mrow in which the parameter will be stored
                final Element newParameter = new Element(ROW);
                
                if (parameter.getName().equals(ROW)) {
                    if(hasInsideBrackets(parameter)) {
                        children.get(i + 1).detach(); // just detach operator
                    } else { // add parentheses
                        parameter.addContent(1, new Element(OPERATOR).setText("("));
                        parameter.addContent(new Element(OPERATOR).setText(")"));
                        children.get(i + 1).detach(); // detach funct app operator
                    }
                    continue; // no need to set newParameter
                } else if (isOperator(parameter, "(")) {
                    int bracketsDepth = 1;
                    newParameter.addContent(parameter.detach());
                    
                    while ((parameterPosition < children.size()) && (bracketsDepth > 0)) {
                        parameter = children.get(parameterPosition);
                        if (isOperator(parameter, "(")) {
                            bracketsDepth++;
                        }
                        else if (isOperator(parameter, ")")) {
                            bracketsDepth--;
                        }
                        newParameter.addContent(parameter.detach());
                    }
                    for (; bracketsDepth > 0; bracketsDepth--) { // add missing right brackets
                        newParameter.addContent(new Element(OPERATOR).setText(")"));
                    }
                } else { // if the paramether is neither mrow or (
                    newParameter.addContent(new Element(OPERATOR).setText("(")); // add left bracket
                    newParameter.addContent(children.get(parameterPosition).detach());
                    newParameter.addContent(new Element(OPERATOR).setText(")")); // add right bracket
                }
                children.set(i + 1, newParameter); // replace function app operator with newParameter
            } else { // if there isnt start of function application apply normalization on children
                normalizeFunctionApplication(children.get(i), functionOperators);
            }
        }
    }
    
    private boolean isFunction(final int i, final List<Element> children,
            final Collection<String> functionOperators) {
        return ((i < children.size() - 2)
                && children.get(i).getName().equals(IDENTIFIER)
                && isOperator(children.get(i+1))
                && functionOperators.contains(children.get(i+1).getTextTrim()));
    }
    
    private boolean hasInsideBrackets(final Element mrow) {
        final List<Element> children = mrow.getChildren();
        if ((children.size() > 1) && isOperator(children.get(0), "(")) {
            int bracketsDepth = 1;
            Element child;
            for (int i = 1; i < children.size(); i++) {
                child = children.get(i);
                if (isOperator(child, "(")) {
                    bracketsDepth++;
                } else if (isOperator(child, ")")) {
                    bracketsDepth--;
                }
                if (bracketsDepth == 0) {
                    if (i < children.size() - 1) {
                        return false;
                    }
                    if (i == children.size() - 1) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private void replaceOperators(final Element element, final Map<String,String> replacements) {
        List<Element> operatorsToReplace = new ArrayList<Element>();
        for (Element operator : element.getDescendants(new ElementFilter(OPERATOR))) {
            if (replacements.containsKey(operator.getTextTrim())) {
                operatorsToReplace.add(operator);
            }
        }
        for (Element operator : operatorsToReplace) {
            operator.setText(replacements.get(operator.getTextTrim()));
        }
    }
    
    private boolean isOperator(final Element element, final String operator) {
        return isOperator(element) && element.getTextTrim().equals(operator);
    }
    
    private boolean isOperator(final Element element) {
        return element.getName().equals(OPERATOR);
    }
    
    private Collection<String> getPropertyCollection(final String property) {
        return new HashSet<String>(Arrays.asList(getProperty(property).split(" ")));
    }
    
    private Map<String,String> getPropertyMap(final String property) {
        final Map<String,String> propertyMap = new HashMap<String,String>();
        final String[] mappings = getProperty(property).split(" ");
        for (int i = 0; i < mappings.length; i++) {
            final String[] mapping = mappings[i].split(":",2);
            if (mapping.length != 2) {
                throw new IllegalArgumentException("property has wrong format");
            }
            propertyMap.put(mapping[0], mapping[1]);
        }
        return propertyMap;
    }
}
