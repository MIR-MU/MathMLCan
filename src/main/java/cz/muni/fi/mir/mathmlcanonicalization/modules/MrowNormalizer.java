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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Normalize the number of mrow elements in MathML.
 *
 * <div class="simpleTagLabel">Input</div>
 * Well-formed MathML, already processed by other modules (especially
 * ElementMinimizer, MfencedReplacer and FunctionNormalizer)
 * <div class="simpleTagLabel">Output</div>
 * The original code with changes in mrow elements:
 * <ul>
 * <li>added mrow elements to places, where detected fenced formulae (and not
 * already encapsulated in mrow)</li>
 * <li>removed redundant mrow elements in unneeded grouping – e.q. parents
 * requiring only one child element accept any number of elements so the mrow
 * tag is not needed (see example) or grouping with only presentation purpose
 * </li>
 * </ul>
 * <div class="simpleTagLabel">Example</div>
 * 
 * <pre>{@code
 * <msqrt>
 *     <mrow>
 *         <mo>-</mo>
 *         <mn>1</mn>
 *     </mrow>
 * </msqrt>}</pre>
 * 
 * is transformed to
 * 
 * <pre>{@code
 * <msqrt>
 *     <mo>-</mo>
 *     <mn>1</mn>
 * </msqrt>
 * }</pre>
 *
 * @author Jakub Adler
 */
public class MrowNormalizer extends AbstractModule implements DOMModule {

    private static final Logger LOGGER = Logger.getLogger(MrowNormalizer.class.getName());
    // properties
    private static final String CHILD_COUNT_PREFIX = "childCount.";
    private static final String OPENING = "open";
    private static final String CLOSING = "close";
    private static final String WRAP_ISIDE = "wrapInside";
    private static final String WRAP_OUTSIDE = "wrapOutside";

    public MrowNormalizer() {
        declareProperty(WRAP_OUTSIDE);
        declareProperty(WRAP_ISIDE);
        declareProperty(OPENING);
        declareProperty(CLOSING);
        declareProperty("childCount.msqrt");
        declareProperty("childCount.mfrac");
        declareProperty("childCount.mroot");
        declareProperty("childCount.mstyle");
        declareProperty("childCount.merror");
        declareProperty("childCount.mpadded");
        declareProperty("childCount.mphantom");
        declareProperty("childCount.mfenced");
        declareProperty("childCount.menclose");
        declareProperty("childCount.msub");
        declareProperty("childCount.msup");
        declareProperty("childCount.msubsup");
        declareProperty("childCount.munder");
        declareProperty("childCount.munderover");
        declareProperty("childCount.mtd");
        declareProperty("childCount.mscarry");
        declareProperty("childCount.math");
        declareProperty("childCount.mrow");
    }

    @Override
    public void execute(final Document doc) {
        if (doc == null) {
            throw new NullPointerException("doc");
        }
        traverseRemoval(doc.root());
        traverseAddition(doc.root());
    }

    /**
     * Recursively searches element content to possibly add mrow where needed
     *
     * @param element element to start at
     */
    private void traverseAddition(final Element element) {
        assert element != null;
        final List<Element> children = new ArrayList<>(element.children());
        for (Element child : children) {
            traverseAddition(child);
        }
        checkAddition(element);
    }

    /**
     * Recursively searches element content to possibly remove mrow where needed
     *
     * @param element element to start at
     */
    private void traverseRemoval(final Element element) {
        assert element != null;
        final List<Element> children = new ArrayList<>(element.children());
        for (Element child : children) {
            traverseRemoval(child);
        }
        if (element.tagName().equals(ROW)) {
            checkRemoval(element);
        }
    }

    /**
     * Removes a mrow element if possible.
     *
     * @param mrowElement the mrow element
     */
    private void checkRemoval(final Element mrowElement) {
        assert mrowElement != null && mrowElement.tagName().equals(ROW);
        if (!mrowElement.hasParent()) {
            return; // no parent element
        }
        final Element parentElement = mrowElement.parent();
        final List<Element> children = mrowElement.children();

        if (children.size() <= 1) {
            removeElement(mrowElement, parentElement);
            LOGGER.log(Level.FINE, "Element \"{0}\" removed", mrowElement);
            return;
        }

        final String childCountPropertyName = CHILD_COUNT_PREFIX + parentElement.tagName();
        if (!isProperty(childCountPropertyName)) {
            return; // unknown parent element
        }
        final String childCountProperty = getProperty(childCountPropertyName);
        final int childCount;
        try {
            childCount = Integer.parseInt(childCountProperty);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING,
                    "\"{0}\" is not an integer for \"" + childCountPropertyName + "\", property ignored",
                    childCountProperty);
            return;
        }

        if (childCount == 1 || // parent can accept any number of elements so we can remove mrow
                children.size() + parentElement.childNodeSize() - 1 == childCount) {
            removeElement(mrowElement, parentElement);
        }
    }

    private static void removeElement(final Element element, final Element parent) {
        assert element != null && parent != null;
        parent.insertChildren(element.siblingIndex(), element.childNodes());
        element.remove();
    }

    /**
     * Test if element is an operator representing an opening or closing
     * parenthesis according to properties
     *
     * @param element      element to test
     * @param propertyName name of property specifiyng opening or closing
     *                     parentheses
     * @return true if element is a parentheses according to propertyName
     */
    private Boolean isParenthesis(final Element element, final String propertyName) {
        assert element != null && propertyName != null && isProperty(propertyName);
        if (!element.tagName().equals(OPERATOR)) {
            return false;
        }
        return getPropertySet(propertyName).contains(element.ownText().trim());
    }

    /**
     * Wrap previously detected fenced expressions in mrow to be same as output
     * of MfencedReplacer
     *
     * @param siblings children of parent element
     * @param fenced   list of elements inside parentheses, children of parent
     *                 element
     * @param opening  opening parenthesis, child of parent element
     * @param closing  closing parenthesis, child of parent element
     */
    private void wrapFenced(final List<Element> siblings, final List<Element> fenced,
            final Element opening, final Element closing) {
        assert siblings != null && fenced != null && opening != null;
        final Element parent = opening.parent();
        assert closing != null && closing.parent().equals(parent);
        final int openingIndex = opening.siblingIndex();

        // Element to be placed inside parentheses.
        // If null, the original 'fenced' list will be used.
        final Element innerElement;
        if (fenced.isEmpty() || !isEnabled(WRAP_ISIDE)) {
            innerElement = null; // will not wrap inside in mrow
        } else if (fenced.size() == 1) {
            innerElement = fenced.get(0); // no need to wrap, just one element
        } else {
            innerElement = new Element(ROW);
            innerElement.appendChildren(fenced);
            LOGGER.fine("Inner mrow added");
        }

        if (((parent.tagName().equals(ROW)
                && siblings.get(0) == opening
                && siblings.get(siblings.size() - 1) == closing))
                || !isEnabled(WRAP_OUTSIDE)) {
            // will not wrap outside in mrow
            if (innerElement == null) {
                parent.insertChildren(openingIndex + 1, fenced);
            } else {
                parent.insertChildren(openingIndex + 1, innerElement);
            }
            return;
        }
        // wrap outside in mrow
        final Element outerMrowElement = new Element(ROW);
        outerMrowElement.appendChild(opening);
        if (innerElement != null) {
            outerMrowElement.appendChild(innerElement);
        } else {
            outerMrowElement.appendChildren(fenced);
        }
        outerMrowElement.appendChild(closing);
        parent.insertChildren(openingIndex, outerMrowElement);
        LOGGER.fine("Outer mrow added");
    }

    /**
     * Add mrow if necessary
     */
    private void checkAddition(final Element element) {
        assert element != null;
        if (!element.hasParent()) {
            return;
        }
        final Element parentElement = element.parent();
        final List<Element> siblings = parentElement.children();

        if (isParenthesis(element, OPENING)) {
            // Need to find matching closing par and register the elements between
            int nesting = 0;

            // list of elements inside parentheses
            final List<Element> fenced = new ArrayList<>();

            for (int i = siblings.indexOf(element) + 1; i < siblings.size(); i++) {
                final Element current = siblings.get(i);

                if (isParenthesis(current, OPENING)) {
                    nesting++; // opening parenthase reached
                } else if (isParenthesis(current, CLOSING)) { // closing parenthase reached
                    if (nesting == 0) {
                        // matching closing parenthase
                        wrapFenced(siblings, fenced, element, current);
                        break;
                    } else {
                        nesting--;
                    }
                }
                fenced.add(current);
            }
        }
    }

}
