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

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Normalize the way to express an function applied to arguments in MathML.
 * <div class="simpleTagLabel">Input</div> Well-formed MathML, not processed by
 * MrowMinimizer yet
 * <div class="simpleTagLabel">Output</div> The original code with:
 * <ul>
 * <li>removed entities for function application (and multiplying where it
 * should not be)</li>
 * <li>the name of function placed in {@code <mi>} element (not
 * {@code <mo>})</li>
 * <li>function arguments placed in parentheses and {@code <mrow>}</li>
 * </ul>
 *
 * @author Jaroslav Dufek
 */
public class FunctionNormalizer extends AbstractModule implements DOMModule {

    private static final Logger LOGGER = Logger.getLogger(FunctionNormalizer.class.getName());
    // properties key names
    private static final String APPLY_FUNCTION_OPERATORS = "functionoperators";

    public FunctionNormalizer() {
        declareProperty("functionoperators");
    }

    @Override
    public void execute(final Document doc) {
        if (doc == null) {
            throw new NullPointerException("doc");
        }
        normalizeFunctionApplication(doc.root(), getPropertySet(APPLY_FUNCTION_OPERATORS));
    }

    // TODO: refactoring
    private void normalizeFunctionApplication(final Element element,
            final Collection<String> functionOperators) {
        assert element != null && functionOperators != null;
        final List<Element> children = element.children();
        for (int i = 0; i < children.size(); i++) {
            if (isFunction(i, children, functionOperators)) {
                final int parameterPosition = i + 2;
                Element parameter = children.get(parameterPosition);
                // mrow in which the parameter will be stored
                final Element newParameter = new Element(ROW);

                if (parameter.tagName().equals(ROW)) {
                    if (hasInsideBrackets(parameter)) {
                        children.get(i + 1).remove(); // just detach operator
                    } else { // add parentheses
                        parameter.insertChildren(0, new Element(OPERATOR).text("("));
                        parameter.appendChild(new Element(OPERATOR).text(")"));
                        LOGGER.fine("Parentheses around function argument added");
                        children.get(i + 1).remove(); // detach funct app operator
                    }
                    LOGGER.fine("Function application operator removed");
                    continue; // no need to set newParameter
                } else if (isOperator(parameter, "(")) {
                    int bracketsDepth = 1;
                    newParameter.appendChild(parameter);
                    int viewPosition = parameterPosition + 1;
                    while ((viewPosition < children.size()) && (bracketsDepth > 0)) {
                        parameter = children.get(viewPosition);
                        if (isOperator(parameter, "(")) {
                            bracketsDepth++;
                        } else if (isOperator(parameter, ")")) {
                            bracketsDepth--;
                        }
                        newParameter.appendChild(parameter);
                        viewPosition++;
                    }
                    for (; bracketsDepth > 0; bracketsDepth--) { // add missing right brackets
                        newParameter.appendChild(new Element(OPERATOR).text(")"));
                        LOGGER.fine("Added missing )");
                    }
                } else { // if the paramether is neither mrow or (
                    newParameter.appendChild(new Element(OPERATOR).text("(")); // add left bracket
                    newParameter.appendChild(children.get(parameterPosition));
                    newParameter.appendChild(new Element(OPERATOR).text(")")); // add right bracket
                    LOGGER.fine("Function argument wrapped with parentheses and mrow");
                }
                children.get(i + 1).replaceWith(newParameter); // replace function app operator with newParameter
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
                && children.get(i).tagName().equals(IDENTIFIER)
                && isOperator(children.get(i + 1))
                && functionOperators.contains(children.get(i + 1).ownText().trim()));
    }

    private boolean hasInsideBrackets(final Element element) {
        assert element != null;
        final List<Element> children = element.children();
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

}
