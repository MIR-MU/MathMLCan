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
package cz.muni.fi.mir.mathmlcanonicalization.modules;

import static cz.muni.fi.mir.mathmlcanonicalization.modules.AbstractModule.MATHMLNS;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * <div class="simpleTagLabel">Input</div> Well-formed MathML, not processed by
 * MrowMinimizer yet
 * <div class="simpleTagLabel">Output</div> The original code with:
 * <ul>
 * <li>normalized Unicode symbols</li>
 * <li>unified operators</li>
 * <li>no redundant operators</li>
 * </ul>
 *
 * @author David Formanek
 */
public class OperatorNormalizer extends AbstractModule implements DOMModule {

    private static final Logger LOGGER = Logger.getLogger(OperatorNormalizer.class.getName());
    // properties key names
    private static final String REMOVE_EMPTY_OPERATORS = "removeempty";
    private static final String OPERATORS_TO_REMOVE = "removeoperators";
    private static final String OPERATOR_REPLACEMENTS = "replaceoperators";
    private static final String COLON_REPLACEMENT = "colonreplacement";
    private static final String NORMALIZATION_FORM = "normalizationform";
    private static final String OPERATORS = "operators";
    private static final String IDENTIFIERS = "identifiers";

    public OperatorNormalizer() {
        declareProperty(REMOVE_EMPTY_OPERATORS);
        declareProperty(OPERATORS_TO_REMOVE);
        declareProperty(OPERATOR_REPLACEMENTS);
        declareProperty(COLON_REPLACEMENT);
        declareProperty(NORMALIZATION_FORM);
        declareProperty(OPERATORS);
        declareProperty(IDENTIFIERS);
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
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid configuration value: "
                        + NORMALIZATION_FORM, ex);
            }
        }
        unifyOperators(root);
    }

    /**
     * Converts bad identifiers to operators, removes redundant and replaces
     */
    private void unifyOperators(final Element ancestor) {
        assert ancestor != null;
        final Set<String> toRemove = getPropertySet(OPERATORS_TO_REMOVE);
        final Map<String, String> replaceMap = getPropertyMap(OPERATOR_REPLACEMENTS);
        if (!getProperty(COLON_REPLACEMENT).isEmpty()) {
            replaceMap.put(":", getProperty(COLON_REPLACEMENT));
        }
        final Set<String> operators = getPropertySet(OPERATORS);
        operators.addAll(toRemove);
        operators.addAll(replaceMap.keySet());
        operators.addAll(replaceMap.values());

        replaceIdentifiers(ancestor, operators);

        if (isEnabled(REMOVE_EMPTY_OPERATORS) || !toRemove.isEmpty()) {
            removeSpareOperators(ancestor, toRemove);
        } else {
            LOGGER.fine("No operators set for removal");
        }

        if (replaceMap.isEmpty()) {
            LOGGER.fine("No operators set to replace");
        } else {
            replaceOperators(ancestor, replaceMap);
        }
        
        final Set<String> identifiers = getPropertySet(IDENTIFIERS);
        operatorsToIdentifiers(ancestor, identifiers);
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
        assert element != null && spareOperators != null && !spareOperators.isEmpty();
        final List<Element> children = element.getChildren();
        for (int i = 0; i < children.size(); i++) {
            final Element actual = children.get(i); // actual element
            if (isOperator(actual)) {
            	//Keep special case where asterisk is by itself in a subscript
                String parent = actual.getParentElement().getName();
                if (isSpareOperator(actual, spareOperators) && !(parent.equals("msub"))
                        && !(parent.equals("msubsup") && !(parent.equals("msup")))) {
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
        assert operator != null && spareOperators != null && isOperator(operator);
        return (isEnabled(REMOVE_EMPTY_OPERATORS) && operator.getText().isEmpty())
                || (spareOperators.contains(operator.getTextTrim()));
    }

    private void replaceOperators(final Element element, final Map<String, String> replacements) {
        assert element != null && replacements != null;
        List<Element> operatorsToReplace = new ArrayList<Element>();
        for (Element operator : element.getDescendants(new ElementFilter(OPERATOR, MATHMLNS))) {
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

    private void replaceIdentifiers(final Element ancestor, final Set<String> operators) {
        assert ancestor != null && operators != null;
        final List<Element> toReplace = new ArrayList<Element>();
        for (Element element : ancestor.getDescendants(new ElementFilter(IDENTIFIER, MATHMLNS))) {
            // TODO: control whole ranges of symbols rather than listed ones
            if (operators.contains(element.getTextTrim())) {
                toReplace.add(element);
            }
        }
        for (Element element : toReplace) {
            LOGGER.log(Level.FINE, "Creating an operator from {0}", element.getText());
            replaceElement(element, OPERATOR);
        }
    }

    private void operatorsToIdentifiers(final Element ancestor, final Set<String> identifiers) {
        assert ancestor != null && identifiers != null;
        final List<Element> toReplace = new ArrayList<Element>();
        for (Element element : ancestor.getDescendants(new ElementFilter(OPERATOR, MATHMLNS))) {
            if (identifiers.contains(element.getTextTrim())) {
                toReplace.add(element);
            }
        }
        for (Element element : toReplace) {
            LOGGER.log(Level.FINE, "Creating an identifier from {0}", element.getText());
            replaceElement(element, IDENTIFIER);
        }
    }
    
    private Map<String, String> getPropertyMap(final String property) {
        assert property != null && isProperty(property);
        final Map<String, String> propertyMap = new HashMap<String, String>();
        final String[] mappings = getProperty(property).split(" ");
        for (String mapping : mappings) {
            final String[] mappingPair = mapping.split(":", 2);
            if (mappingPair.length != 2) {
                throw new IllegalArgumentException("property has wrong format");
            }
            propertyMap.put(mappingPair[0], mappingPair[1]);
        }
        return propertyMap;
    }
}
