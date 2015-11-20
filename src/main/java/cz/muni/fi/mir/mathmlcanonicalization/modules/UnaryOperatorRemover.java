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
 * Well-formed MathML
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
 * </p>
 *
 * @author Michal Růžička
 */
public class UnaryOperatorRemover extends AbstractModule implements DOMModule {

    private static final Logger LOGGER = Logger.getLogger(ScriptNormalizer.class.getName());
    private static final List<Namespace> namespaces = new ArrayList<Namespace>();

    public UnaryOperatorRemover() {
        namespaces.add(Namespace.getNamespace("mathml", "http://www.w3.org/1998/Math/MathML"));
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

        XPathExpression<Element> xp = XPathFactory.instance().compile("//mathml:mo[count(preceding-sibling::*) = 0]|//mo[count(preceding-sibling::*) = 0]", Filters.element(), null, namespaces);
        List<Element> elemsToRemove = xp.evaluate(rootElem);

        for (Element toRemove : elemsToRemove) {
            LOGGER.finest("Removing element '" + toRemove.getQualifiedName() + "' with value '" + toRemove.getValue() + "'.");
            toRemove.detach();
        }

        LOGGER.finer("RemoveUnaryOperator finished");

    }

}
