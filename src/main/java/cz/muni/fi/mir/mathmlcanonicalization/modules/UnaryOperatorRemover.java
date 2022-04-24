/**
 * Copyright 2015 MIR@MU Project
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Removes unary operators, i.e. {@code //mo[count(preceding-sibling::*) = 0]}.
 *
 * <p>
 * <span class="simpleTagLabel">Input</span>
 * <p>
 * Well-formed Presentation or Content MathML
 * </p>
 * <span class="simpleTagLabel">Output</span>
 * <p>
 * The original code with all unatry operators removed
 * </p>
 * <span class="simpleTagLabel">Example Input</span>
 * 
 * <pre>{@code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <math>
 *   <mrow>
 *     }<strong>{@code <mo>-</mo>}</strong>{@code
 *     <mi>E</mi>
 *     <mo>=</mo>
 *     <mi>m</mi>
 *     <msup>
 *       <mi>c</mi>
 *       <mn>2</mn>
 *     </msup>
 *   </mrow>
 * </math>
 * }</pre>
 * 
 * <span class="simpleTagLabel">Example Output</span>
 * 
 * <pre>{@code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <math>
 *   <mrow>
 *     <mi>E</mi>
 *     <mo>=</mo>
 *     <mi>m</mi>
 *     <msup>
 *       <mi>c</mi>
 *       <mn>2</mn>
 *     </msup>
 *   </mrow>
 * </math>
 * }</pre>
 *
 * @author Michal Růžička
 */
public class UnaryOperatorRemover extends AbstractModule implements DOMModule {

    private static final Logger LOGGER = Logger.getLogger(ScriptNormalizer.class.getName());

    // properties key names
    private static final String PM_UNARY_OPERATORS_TO_REMOVE = "pmathremoveunaryoperators";
    private static final String CM_UNARY_OPERATORS_TO_REMOVE = "cmathremoveunaryoperators";

    public UnaryOperatorRemover() {
        declareProperty(PM_UNARY_OPERATORS_TO_REMOVE);
        declareProperty(CM_UNARY_OPERATORS_TO_REMOVE);
    }

    @Override
    public void execute(final Document doc) {

        if (doc == null) {
            throw new NullPointerException("doc");
        }

        final Element root = doc.root();

        removeUnaryOperator(root);

    }

    private void removeUnaryOperator(final Element rootElem) {

        assert rootElem != null;

        /* Presentation MathML */
        final Set<String> pmCharsToRemove = getPropertySet(PM_UNARY_OPERATORS_TO_REMOVE);

        if (!pmCharsToRemove.isEmpty()) {
            List<Element> pmElemsToRemove = new ArrayList<>();
            for (Element operator : rootElem.getElementsByTag(OPERATOR)) {
                int index = operator.elementSiblingIndex();
                if (index != 0 && operator.previousElementSibling().tagName().equals(OPERATOR)) {
                    System.out.println(operator.text());
                    index = 0;
                }
                if (index == 0) {
                    pmElemsToRemove.add(operator);
                }
            }

            // Unary operators
            for (Element toRemove : pmElemsToRemove) {
                if (pmCharsToRemove.contains(toRemove.text())) {
                    LOGGER.finest(
                            "Removing element '" + toRemove.tagName() + "' with value '" + toRemove.text() + "'.");
                    toRemove.remove();
                } else {
                    LOGGER.finest(
                            "Skipping element '" + toRemove.tagName() + "' with value '" + toRemove.text() + "'.");
                }
            }

        }

        LOGGER.finer("RemoveUnaryOperator Presentation MathML finished");

        /* Content MathML */
        List<Element> applyWithTwoChildrens = new ArrayList<>();
        for (Element element : rootElem.getElementsByTag("apply")) {
            if (element.childrenSize() == 2) {
                applyWithTwoChildrens.add(element);
            }
        }

        final Set<String> cmOperatorsToRemove = getPropertySet(CM_UNARY_OPERATORS_TO_REMOVE);

        for (Element applyElem : applyWithTwoChildrens) {
            Element operator = applyElem.children().get(0);
            if (cmOperatorsToRemove.contains(operator.tagName())) {
                Element operand = applyElem.children().get(1);
                LOGGER.finest(
                        "Removing operator '" + operator.tagName() + "' for operand '" + operand.tagName() + "'.");
                Element parent = applyElem.parent();
                int applyElemIndex = applyElem.siblingIndex();
                parent.insertChildren(applyElemIndex, operand);
                applyElem.remove();
            }
        }

        LOGGER.finer("RemoveUnaryOperator Content MathML finished");

    }

}
