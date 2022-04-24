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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

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
        final Element root = doc.root();

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

        final List<Node> texts = new ArrayList<>();
        for (Element descendant : ancestor.getAllElements()) {
            for (Node text : descendant.textNodes()) {
                texts.add(text);
            }
        }

        for (Node text : texts) {
            final String textString = text.attr("#text");
            if (Normalizer.isNormalized(textString, form)) {
                continue;
            }
            text.attr("#text", Normalizer.normalize(textString, form));
            LOGGER.log(Level.FINE, "Text ''{0}'' normalized to ''{1}''",
                    new Object[] { textString, text.attr("#text") });
            assert Normalizer.isNormalized(text.attr("#text"), form);
        }
    }

    private void removeSpareOperators(final Element element, final Collection<String> spareOperators) {
        assert element != null && spareOperators != null && !spareOperators.isEmpty();
        final List<Element> children = element.children();
        for (int i = 0; i < children.size(); i++) {
            final Element actual = children.get(i); // actual element
            if (isOperator(actual)) {
                // Keep special case where asterisk is by itself in a subscript
                String parent = actual.parent().tagName();
                if (isSpareOperator(actual, spareOperators) && !(parent.equals("msub"))
                        && !(parent.equals("msubsup") && !(parent.equals("msup")))) {
                    actual.remove();
                    LOGGER.log(Level.FINE, "Operator {0} removed", actual);
                }
            } else {
                removeSpareOperators(actual, spareOperators);
            }
        }
    }

    private boolean isSpareOperator(final Element operator, final Collection<String> spareOperators) {
        assert operator != null && spareOperators != null && isOperator(operator);
        if (!isEnabled(REMOVE_EMPTY_OPERATORS)) {
            return false;
        }
        if (operator.textNodes().isEmpty()) {
            return true;
        }
        String operatorText = "";
        for (TextNode textNode : operator.textNodes()) {
            operatorText += textNode.getWholeText().trim();
        }
        return spareOperators.contains(operatorText);
    }

    private void replaceOperators(final Element element, final Map<String, String> replacements) {
        assert element != null && replacements != null;
        for (Element operator : element.getElementsByTag(OPERATOR)) {
            for (TextNode textNode : operator.textNodes()) {
                final String oldOperator = textNode.getWholeText().trim();
                final String newOperator = replacements.get(oldOperator);
                if (replacements.containsKey(oldOperator)) {
                    operator.text(newOperator);
                    LOGGER.log(Level.FINE, "Operator ''{0}'' was replaced by ''{1}''",
                            new Object[] { oldOperator, newOperator });
                    break;
                }
            }
        }
    }

    private void replaceIdentifiers(final Element ancestor, final Set<String> operators) {
        assert ancestor != null && operators != null;
        final List<Element> toReplace = new ArrayList<>();
        for (Element element : ancestor.getElementsByTag(IDENTIFIER)) {
            // TODO: control whole ranges of symbols rather than listed ones
            if (operators.contains(element.ownText().trim())) {
                toReplace.add(element);
            }
        }
        for (Element element : toReplace) {
            LOGGER.log(Level.FINE, "Creating an operator from {0}", element.ownText());
            replaceElement(element, OPERATOR);
        }
    }

    private void operatorsToIdentifiers(final Element ancestor, final Set<String> identifiers) {
        assert ancestor != null && identifiers != null;
        final List<Element> toReplace = new ArrayList<>();
        for (Element element : ancestor.getElementsByTag(OPERATOR)) {
            if (identifiers.contains(element.ownText().trim())) {
                toReplace.add(element);
            }
        }
        for (Element element : toReplace) {
            LOGGER.log(Level.FINE, "Creating an identifier from {0}", element.ownText());
            replaceElement(element, IDENTIFIER);
        }
    }

    private Map<String, String> getPropertyMap(final String property) {
        assert property != null && isProperty(property);
        final Map<String, String> propertyMap = new HashMap<>();
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
