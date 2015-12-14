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

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

/**
 * Removes unary operators, i.e.
 * <code>//mo[count(preceding-sibling::*) = 0]</code>.
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
 * <pre>
 * &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;
 * &lt;math&gt;
 *   &lt;mrow&gt;
 *     <strong>&lt;mo&gt;-&lt;/mo&gt;</strong>
 *     &lt;mi&gt;E&lt;/mi&gt;
 *     &lt;mo&gt;=&lt;/mo&gt;
 *     &lt;mi&gt;m&lt;/mi&gt;
 *     &lt;msup&gt;
 *       &lt;mi&gt;c&lt;/mi&gt;
 *       &lt;mn&gt;2&lt;/mn&gt;
 *     &lt;/msup&gt;
 *   &lt;/mrow&gt;
 * &lt;/math&gt;
 * </pre>
 * <span class="simpleTagLabel">Example Output</span>
 * <pre>
 * &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;
 * &lt;math&gt;
 *   &lt;mrow&gt;
 *     &lt;mi&gt;E&lt;/mi&gt;
 *     &lt;mo&gt;=&lt;/mo&gt;
 *     &lt;mi&gt;m&lt;/mi&gt;
 *     &lt;msup&gt;
 *       &lt;mi&gt;c&lt;/mi&gt;
 *       &lt;mn&gt;2&lt;/mn&gt;
 *     &lt;/msup&gt;
 *   &lt;/mrow&gt;
 * &lt;/math&gt;
 * </pre>
 *
 * @author Michal Růžička
 */
public class UnaryOperatorRemover extends AbstractModule implements DOMModule {

    private static final Logger LOGGER = Logger.getLogger(ScriptNormalizer.class.getName());

    // properties key names
    private static final String PM_UNARY_OPERATORS_TO_REMOVE = "pmathremoveunaryoperators";
    private static final String CM_UNARY_OPERATORS_TO_REMOVE = "cmathremoveunaryoperators";

    private static final XPathExpression<Element> xpPMUnaryOperators = XPathFactory.instance().compile(
            "//mathml:mo[count(preceding-sibling::*) = 0]|//mo[count(preceding-sibling::*) = 0]",
            Filters.element(), null,
            Namespace.getNamespace("mathml", "http://www.w3.org/1998/Math/MathML"));
    private static final XPathExpression<Element> xpPMSecondOperatorInDoubleOperators = XPathFactory.instance().compile(
            "//mathml:mo[preceding-sibling::*[1][self::mathml:mo]]|//mo[preceding-sibling::*[1][self::mo]]",
            Filters.element(), null,
            Namespace.getNamespace("mathml", "http://www.w3.org/1998/Math/MathML"));
    private static final XPathExpression<Element> xpCMApplyWithTwoChildrens = XPathFactory.instance().compile(
            "//mathml:apply[count(child::*)=2]|//apply[count(child::*)=2]",
            Filters.element(), null,
            Namespace.getNamespace("mathml", "http://www.w3.org/1998/Math/MathML"));

    public UnaryOperatorRemover() {
        declareProperty(PM_UNARY_OPERATORS_TO_REMOVE);
        declareProperty(CM_UNARY_OPERATORS_TO_REMOVE);
    }

    @Override
    public void execute(final Document doc) {

        if (doc == null) {
            throw new NullPointerException("doc");
        }

        final Element root = doc.getRootElement();

        removeUnaryOperator(root);

    }

    private void removeUnaryOperator(final Element rootElem) {

        assert rootElem != null;

        /* Presentation MathML */
        final Set<String> pmCharsToRemove = getPropertySet(PM_UNARY_OPERATORS_TO_REMOVE);

        if (!pmCharsToRemove.isEmpty()) {

            // Unary operators
            List<Element> pmElemsToRemove = xpPMUnaryOperators.evaluate(rootElem);
            for (Element toRemove : pmElemsToRemove) {
                if (pmCharsToRemove.contains(toRemove.getValue())) {
                    LOGGER.finest("Removing element '" + toRemove.getQualifiedName() + "' with value '" + toRemove.getValue() + "'.");
                    toRemove.detach();
                } else {
                    LOGGER.finest("Skipping element '" + toRemove.getQualifiedName() + "' with value '" + toRemove.getValue() + "'.");
                }
            }

            // Second of the double operators
            pmElemsToRemove = xpPMSecondOperatorInDoubleOperators.evaluate(rootElem);
            for (Element toRemove : pmElemsToRemove) {
                if (pmCharsToRemove.contains(toRemove.getValue())) {
                    LOGGER.finest("Removing the second element out of double elements '" + toRemove.getQualifiedName() + "' with value '" + toRemove.getValue() + "'.");
                    toRemove.detach();
                } else {
                    LOGGER.finest("Skipping the second element out of double elements '" + toRemove.getQualifiedName() + "' with value '" + toRemove.getValue() + "'.");
                }
            }

        }

        LOGGER.finer("RemoveUnaryOperator Presentation MathML finished");

        /* Content MathML */
        List<Element> applyWithTwoChildrens = xpCMApplyWithTwoChildrens.evaluate(rootElem);
        final Set<String> cmOperatorsToRemove = getPropertySet(CM_UNARY_OPERATORS_TO_REMOVE);

        for (Element applyElem : applyWithTwoChildrens) {
            Element operator = applyElem.getChildren().get(0);
            if (cmOperatorsToRemove.contains(operator.getName())) {
                Element operand = applyElem.getChildren().get(1);
                LOGGER.finest("Removing operator '" + operator.getQualifiedName() + "' for operand '" + operand.getQualifiedName() + "'.");
                operand.detach();
                Element parent = applyElem.getParentElement();
                int applyElemIndex = parent.indexOf(applyElem);
                parent.setContent(applyElemIndex, operand);
                applyElem.detach();
            }
        }

        LOGGER.finer("RemoveUnaryOperator Content MathML finished");

    }

}
