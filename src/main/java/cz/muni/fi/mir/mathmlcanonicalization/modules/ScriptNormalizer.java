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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Handle sub/super/under/over/multi script elements in MathML.
 *
 * <p>
 * Normalize the occurence of {@code <msub>}, {@code <msup>}, {@code <msubsup>},
 * {@code <munder>}, {@code <mover>}, {@code <munderover>} and
 * {@code <mmultiscripts>} (with children {@code <mprescripts/>} and
 * {@code <none/>}) elements in MathML.
 * </p>
 * <span class="simpleTagLabel">Input</span>
 * Well-formed MathML
 * <div class="simpleTagLabel">Output</div>
 * The original code with always used:
 * <ul>
 * <li>{@code <msubsup>} (or {@code <msub>}) for sums, integrals, etc.
 * (converted from {@code <munderover>}, {@code <munder>} and
 * {@code <msub>}, {@code <msup>} combinations)</li>
 * <li>{@code <msub>} inside {@code <msup>} in nested formulae</li>
 * <li>nested {@code <msub>} and {@code <msup>} instead of {@code <msubsup>} in
 * identifiers (not for sums, integrals, etc.)</li>
 * <li>Unicode scripts converted to MathML scripts</li>
 * <li>(sub/super)scripts instead of {@code <mmultiscript>} where possible</li>
 * <li>maybe conversion all (under/over)scripts to (sub/super) scripts?</li>
 * </ul>
 *
 * @author Jaroslav Dufek
 * @author David Formanek
 */
public class ScriptNormalizer extends AbstractModule implements DOMModule {

    private static final Logger LOGGER = Logger.getLogger(ScriptNormalizer.class.getName());
    // properties key names
    private static final String SWAP_SCRIPTS = "swapscripts";
    private static final String SPLIT_SCRIPTS_ELEMENTS = "splitscriptselements";
    private static final String UNIFY_SCRIPTS = "unifyscripts";

    public ScriptNormalizer() {
        declareProperty(SWAP_SCRIPTS);
        declareProperty(SPLIT_SCRIPTS_ELEMENTS);
        declareProperty(UNIFY_SCRIPTS);
    }

    @Override
    public void execute(final Document doc) {
        if (doc == null) {
            throw new NullPointerException("doc");
        }
        final Element root = doc.root();
        if (isEnabled(UNIFY_SCRIPTS)) {
            final Map<String, String> replaceMap = new HashMap<>();
            replaceMap.put(UNDERSCRIPT, SUBSCRIPT);
            replaceMap.put(OVERSCRIPT, SUPERSCRIPT);
            replaceMap.put(UNDEROVER, SUBSUP);
            replaceDescendants(root, replaceMap);
        } else {
            // TODO: normalize unconverted munder/mover/munderover
        }
        // TODO: convert multiscript where possible
        if (isEnabled(SWAP_SCRIPTS)) {
            normalizeSupInSub(root);
        }
        Collection<String> chosenElements = getPropertySet(SPLIT_SCRIPTS_ELEMENTS);
        if (chosenElements.isEmpty()) {
            LOGGER.finer("Msubsup conversion is switched off");
        } else {
            normalizeMsubsup(root, chosenElements);
        }
        // TODO: convert sub/sup combination with not chosen elements to subsup
    }

    private void normalizeSupInSub(final Element element) {
        assert element != null;
        final List<Element> children = element.children();
        for (int i = 0; i < children.size(); i++) {
            final Element actual = children.get(i);
            normalizeSupInSub(actual);
            if (!actual.tagName().equals(SUBSCRIPT)) {
                continue;
            }
            List<Element> subscriptChildren = actual.children();
            if (subscriptChildren.size() != 2) {
                LOGGER.fine("Invalid msub, skipped");
                continue;
            }
            if (!subscriptChildren.get(0).tagName().equals(SUPERSCRIPT)) {
                continue;
            }
            final List<Element> superscriptChildren = subscriptChildren.get(0).children();
            if (superscriptChildren.size() != 2) {
                LOGGER.fine("Invalid msup, skipped");
                continue;
            }
            final Element newMsub = new Element(SUBSCRIPT);
            newMsub.appendChild(superscriptChildren.get(0));
            newMsub.appendChild(subscriptChildren.get(1));
            final Element newMsup = new Element(SUPERSCRIPT);
            newMsup.appendChild(newMsub);
            newMsup.appendChild(superscriptChildren.get(1));
            children.get(i).replaceWith(newMsup);
            LOGGER.finer("Sub/sup scripts swapped");
        }
    }

    private void normalizeMsubsup(final Element element, Collection<String> firstChildren) {
        assert element != null && firstChildren != null;
        final List<Element> children = element.children();
        for (int i = 0; i < children.size(); i++) {
            final Element actual = children.get(i);

            // traverse and normalise children first
            normalizeMsubsup(actual, firstChildren);

            if (actual.tagName().equals(SUBSUP)) {
                final List<Element> actualChildren = actual.children();
                if (actualChildren.size() != 3) {
                    LOGGER.fine("Invalid msubsup, skipped");
                    continue;
                }
                if (!firstChildren.contains(actualChildren.get(0).tagName())) {
                    continue;
                }
                final Element newMsub = new Element(SUBSCRIPT);
                newMsub.appendChild(actualChildren.get(0));
                newMsub.appendChild(actualChildren.get(1));
                final Element newMsup = new Element(SUPERSCRIPT);
                newMsup.appendChild(newMsub);
                newMsup.appendChild(actualChildren.get(2));
                children.get(i).replaceWith(newMsup);
                LOGGER.finer("Msubsup converted to nested msub and msup");
            }
        }
    }

    private void replaceDescendants(final Element ancestor, final Map<String, String> map) {
        assert ancestor != null && map != null;
        final List<Element> toReplace = new ArrayList<>();
        for (Element element : ancestor.getAllElements()) {
            if (map.containsKey(element.tagName())) {
                toReplace.add(element);
            }
        }
        for (Element element : toReplace) {
            replaceElement(element, map.get(element.tagName()));
        }
    }

}
