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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.ElementFilter;

/**
 * Replace mfenced elements in MathML for equivalent.
 *
 * <div class="simpleTagLabel">Input</div>
 * Well-formed MathML, preserved non-default attributes in {@code <mfenced>}
 * tags, not processed by MrowMinimizer yet
 * <div class="simpleTagLabel">Output</div>
 * The original code containing no {@code <mfenced>} elements, originally fenced
 * formulae are enclosed in {@code <mrow>} tag, contain delimiters and
 * separators (from {@code <mfenced>} attributes) in {@code <mo>} elements,
 * inner content is placed into another {@code <mrow>} element. Module can be
 * configured not to add mrow outside and inside or your own fixed or default
 * parentheses and separators for fenced expressions can be specified.
 * <div class="simpleTagLabel">Example</div>
 * <pre>{@code
 * <mfenced open="[">
 *   <mi>x<mi>
 *   <mi>y<mi>
 * </mfenced>
 * }</pre> is transformed to
 * <pre>{@code
 * <mrow>
 *     <mo>[</mo>
 *     <mrow>
 *         <mi>x<mi>
 *         <mo>,</mo>
 *         <mi>y<mi>
 *     </mrow>
 *     <mo>)</mo>
 * </mrow>
 * }</pre>
 *
 * @author David Formanek
 */
public class MfencedReplacer extends AbstractModule implements DOMModule {

    private static final Logger LOGGER = Logger.getLogger(MfencedReplacer.class.getName());
    // MathML attributes
    private static final String OPEN_FENCE = "open";
    private static final String CLOSE_FENCE = "close";
    private static final String SEPARATORS = "separators";
    // properties key names
    private static final String DEFAULT_OPEN = "open";
    private static final String DEFAULT_CLOSE = "close";
    private static final String DEFAULT_SEPARATORS = "separators";
    private static final String FORCE_DEFAULT_OPEN = "forceopen";
    private static final String FORCE_DEFAULT_CLOSE = "forceclose";
    private static final String FORCE_DEFAULT_SEPARATORS = "forceseparators";
    private static final String ADD_OUTER_ROW = "outermrow";
    private static final String ADD_INNER_ROW = "innermrow";

    public MfencedReplacer() {
        declareProperty(ADD_OUTER_ROW);
        declareProperty(ADD_INNER_ROW);
        declareProperty(DEFAULT_OPEN);
        declareProperty(DEFAULT_CLOSE);
        declareProperty(DEFAULT_SEPARATORS);
        declareProperty(FORCE_DEFAULT_OPEN);
        declareProperty(FORCE_DEFAULT_CLOSE);
        declareProperty(FORCE_DEFAULT_SEPARATORS);
    }

    @Override
    public void execute(final Document doc) {
        if (doc == null) {
            throw new NullPointerException("doc");
        }
        final List<Element> toReplace = new ArrayList<>();
        for (Element mfenced : doc.getDescendants(new ElementFilter(FENCED, MATHMLNS))) {
            toReplace.add(mfenced);
        }
        if (toReplace.isEmpty()) {
            LOGGER.fine("No mfenced elements found");
            return;
        }
        for (Element mfenced : toReplace) {
            replaceMfenced(mfenced);
        }
    }

    private void replaceMfenced(final Element mfencedElement) {
        assert mfencedElement != null;
        final char[] separators = getSeparators(mfencedElement);
        final Namespace ns = mfencedElement.getNamespace();
        final List<Element> children = mfencedElement.getChildren();
        final int nChildren = children.size();
        final int last = Math.min(separators.length - 1, nChildren - 2);

        Element insideFence = null;
        if (nChildren == 1 && children.get(0).getName().equals(ROW)) {
            // we do not want to add another mrow
            insideFence = children.get(0).detach();
        } else if (nChildren != 0) {
            insideFence = new Element(ROW, ns);
            for (int i = 0; i < nChildren; i++) {
                // add separator
                if (i > 0 && last >= 0) { // not before first or when blank separators
                    char separatorChar = separators[(i - 1 > last) ? last : i - 1];
                    String separatorStr = Character.toString(separatorChar);
                    insideFence.addContent(new Element(OPERATOR, ns).setText(separatorStr));
                }
                // add original child
                insideFence.addContent(children.get(0).detach());
            }
        }
        replaceMfenced(mfencedElement, insideFence);
    }

    private void replaceMfenced(final Element mfencedElement, final Element insideContent) {
        assert mfencedElement != null; // but insideContent can be null
        final Namespace ns = mfencedElement.getNamespace();
        Element replacement = new Element(ROW, ns);
        String openStr = getProperty(DEFAULT_OPEN);
        String closeStr = getProperty(DEFAULT_CLOSE);
        if (openStr.isEmpty() || closeStr.isEmpty()) {
            LOGGER.warning("Default open or close fence not set");
        }

        if (!isEnabled(FORCE_DEFAULT_OPEN)) {
            openStr = mfencedElement.getAttributeValue(OPEN_FENCE, openStr);
        }
        if (!isEnabled(FORCE_DEFAULT_CLOSE)) {
            closeStr = mfencedElement.getAttributeValue(CLOSE_FENCE, closeStr);
        }

        replacement.addContent(new Element(OPERATOR, ns).setText(openStr));
        if (insideContent != null) {
            if (isEnabled(ADD_INNER_ROW)) {
                replacement.addContent(insideContent);
            } else {
                replacement.addContent(insideContent.removeContent());
            }
        }
        replacement.addContent(new Element(OPERATOR, ns).setText(closeStr));

        final Element parent = mfencedElement.getParentElement();
        final int index = parent.indexOf(mfencedElement);
        parent.removeContent(index);
        if (isEnabled(ADD_OUTER_ROW)) {
            parent.addContent(index, replacement);
        } else {
            parent.addContent(index, replacement.removeContent());
        }
        LOGGER.fine("Mfenced element converted");
    }

    private char[] getSeparators(final Element element) {
        assert element != null;
        if (isEnabled(FORCE_DEFAULT_SEPARATORS)) {
            return getProperty(DEFAULT_SEPARATORS).toCharArray();
        }
        return element.getAttributeValue(SEPARATORS,
                getProperty(DEFAULT_SEPARATORS)).trim().toCharArray();
    }

}
