/**
 * Copyright 2016 MIR@MU Project
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
import java.util.logging.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Make sure any {@code <math>} element has exactly one child.
 *
 * Travers input XML and check all found {@code <math>} elements. If the
 * {@code <math>} element has single child keep it untouched. If the
 * {@code <math>} element has multiple children add new artificial
 * {@code <mrow>}, move all the children under this new {@code <mrow>} and
 * finally set the {@code <mrow>} as the only child of the {@code <math>}
 * element.
 *
 * @author Michal Růžička
 */
public class SingleTopElementOutputter extends AbstractModule implements DOMModule {

    private static final Logger LOGGER = Logger.getLogger(SingleTopElementOutputter.class.getName());

    @Override
    public void execute(final Document doc) {
        if (doc == null) {
            throw new NullPointerException("doc");
        }
        traverseDocument(doc.root());
    }

    private void traverseDocument(final Element rootElement) {
        assert rootElement != null;
        final List<Element> mathElements = rootElement.getElementsByTag("math");
        LOGGER.fine(mathElements.size() + MATH + " elements found");

        for (Element mathElement : mathElements) {
            if (mathElement.childNodeSize() > 1) {
                LOGGER.fine(MATH + " element found have multiple children");
                Element mrow = new Element(ROW);
                mrow.appendChildren(mathElement.childNodes());
                mathElement.appendChild(mrow);
                LOGGER.fine(MATH + " children moved under new " + ROW + " element");
            }
        }
    }

}
