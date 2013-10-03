package cz.muni.fi.mir.mathmlcanonicalization.modules;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Text;
import org.jdom2.filter.ContentFilter;
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
 * @author David Formanek
 */
public class OperatorNormalizer extends AbstractModule implements DOMModule {
    
    /**
     * Path to the property file with module settings.
     */
    private static final String PROPERTIES_FILENAME = "/res/operator-normalizer.properties";
    private static final Logger LOGGER = Logger.getLogger(OperatorNormalizer.class.getName());
    
    // properties key names
    private static final String NORMALIZE_FUNCTIONS = "normalizefunctions";
    private static final String APPLY_FUNCTION_OPERATORS = "functionoperators";
    private static final String REMOVE_EMPTY_OPERATORS = "removeempty";
    private static final String OPERATORS_TO_REMOVE = "removeoperators";
    private static final String OPERATOR_REPLACEMENTS = "replaceoperators";
    private static final String COLON_REPLACEMENT = "colonreplacement";
    private static final String NORMALIZATION_FORM = "normalizationform";
    
    public OperatorNormalizer() {
        loadProperties(PROPERTIES_FILENAME);
    }
    
    @Override
    public void execute(final Document doc) {
        if (doc == null) {
            throw new NullPointerException("doc");
        }
        final Element root = doc.getRootElement();
        
        // TODO: convert Unicode superscripts (supX entities) to msup etc.
        
        final String normalizerFormStr = getProperty(NORMALIZATION_FORM);
        if (normalizerFormStr.isEmpty()) {
            LOGGER.fine("Unicode text normalization is switched off");
        } else {
            try {
                Normalizer.Form normalizerForm = Normalizer.Form.valueOf(normalizerFormStr);
                normalizeUnicode(root, normalizerForm);
            } catch(IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid configuration value: "
                        + NORMALIZATION_FORM, ex);
            }
        }
        
        if (isEnabled(REMOVE_EMPTY_OPERATORS) || !getProperty(OPERATORS_TO_REMOVE).isEmpty()) {
            removeSpareOperators(root, getPropertySet(OPERATORS_TO_REMOVE));
        } else {
            LOGGER.fine("No operators set for removal");
        }
        
        final Map<String, String> replaceMap = getPropertyMap(OPERATOR_REPLACEMENTS);
        if (!getProperty(COLON_REPLACEMENT).isEmpty()) {
            replaceMap.put(":", getProperty(COLON_REPLACEMENT));
        }
        if (replaceMap.isEmpty()) {
            LOGGER.fine("No operators set to replace");
        } else {
            replaceOperators(root, replaceMap);
        }
        
        if (isEnabled(NORMALIZE_FUNCTIONS)) {
            normalizeFunctionApplication(root, getPropertySet(APPLY_FUNCTION_OPERATORS));
        }
    }
    
    private void normalizeUnicode(final Element ancestor, final Normalizer.Form form) {
        assert ancestor != null && form != null;
        final List<Text> texts = new ArrayList<Text>();
        final ContentFilter textFilter = new ContentFilter(ContentFilter.TEXT);
        for (Content text : ancestor.getContent(textFilter)) {
            texts.add((Text) text);
        }
        for (Element element : ancestor.getDescendants(new ElementFilter())) {
            for (Content text : element.getContent(textFilter)) {
                texts.add((Text) text);
            }
        }
        for (Text text : texts) {
            if (Normalizer.isNormalized(text.getText(), form)) {
                continue;
            }
            final String normalizedString = Normalizer.normalize(text.getText(), form);
            LOGGER.log(Level.FINE, "Text ''{0}'' normalized to ''{1}''",
                    new Object[]{text.getText(), normalizedString});
            text.setText(normalizedString);
            assert Normalizer.isNormalized(text.getText(), form);
        }
    }
    
    private void removeSpareOperators(final Element element, final Collection<String> spareOperators) {
        assert element != null && spareOperators != null;
        final List<Element> children = element.getChildren();
        for (int i = 0; i < children.size(); i++) {
            final Element actual = children.get(i); // actual element
            if (isOperator(actual)) {
                if (isSpareOperator(actual, spareOperators)){
                    actual.detach();
                    i--; // move iterator back after detaching so it points to next element
                    LOGGER.log(Level.FINE, "Operator {0} removed", actual);
                }
            } else {
                removeSpareOperators(actual, spareOperators);
            }
        }
    }
    
    private boolean isSpareOperator(final Element operator, final Collection<String> spareOperators) {
        assert operator != null && spareOperators != null;
        return (isEnabled(REMOVE_EMPTY_OPERATORS) && operator.getText().isEmpty())
                || (spareOperators.contains(operator.getTextTrim()));
    }
    
    private void normalizeFunctionApplication(final Element element,
            final Collection<String> functionOperators) {
        assert element != null && functionOperators != null;
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
                        LOGGER.fine("Parentheses around function argument added");
                        children.get(i + 1).detach(); // detach funct app operator
                    }
                    LOGGER.fine("Function application operator removed");
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
                        LOGGER.fine("Added missing )");
                    }
                } else { // if the paramether is neither mrow or (
                    newParameter.addContent(new Element(OPERATOR).setText("(")); // add left bracket
                    newParameter.addContent(children.get(parameterPosition).detach());
                    newParameter.addContent(new Element(OPERATOR).setText(")")); // add right bracket
                    LOGGER.fine("Function argument wrapped with parentheses and mrow");
                }
                children.set(i + 1, newParameter); // replace function app operator with newParameter
                LOGGER.fine("Function application operator removed");
            } else { // if there isnt start of function application apply normalization on children
                normalizeFunctionApplication(children.get(i), functionOperators);
            }
        }
    }
    
    private boolean isFunction(final int i, final List<Element> children,
            final Collection<String> functionOperators) {
        assert i >= 0 && children != null && i < children.size() && functionOperators != null;
        return ((i < children.size() - 2)
                && children.get(i).getName().equals(IDENTIFIER)
                && isOperator(children.get(i+1))
                && functionOperators.contains(children.get(i+1).getTextTrim()));
    }
    
    private boolean hasInsideBrackets(final Element element) {
        assert element != null;
        final List<Element> children = element.getChildren();
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
        assert element != null && replacements != null;
        List<Element> operatorsToReplace = new ArrayList<Element>();
        for (Element operator : element.getDescendants(new ElementFilter(OPERATOR))) {
            if (replacements.containsKey(operator.getTextTrim())) {
                operatorsToReplace.add(operator);
            }
        }
        for (Element operator : operatorsToReplace) {
            final String oldOperator = operator.getTextTrim();
            final String newOperator = replacements.get(oldOperator);
            operator.setText(newOperator);
            LOGGER.log(Level.FINE, "Operator ''{0}'' was replaced by ''{1}''",
                    new Object[]{oldOperator, newOperator});
        }
    }
    
    private boolean isOperator(final Element element, final String operator) {
        return isOperator(element) && element.getTextTrim().equals(operator);
    }
    
    private boolean isOperator(final Element element) {
        assert element != null;
        return element.getName().equals(OPERATOR);
    }
    
    private Map<String,String> getPropertyMap(final String property) {
        assert property != null && isProperty(property);
        final Map<String,String> propertyMap = new HashMap<String,String>();
        final String[] mappings = getProperty(property).split(" ");
        for (int i = 0; i < mappings.length; i++) {
            final String[] mapping = mappings[i].split(":", 2);
            if (mapping.length != 2) {
                throw new IllegalArgumentException("property has wrong format");
            }
            propertyMap.put(mapping[0], mapping[1]);
        }
        return propertyMap;
    }
}
