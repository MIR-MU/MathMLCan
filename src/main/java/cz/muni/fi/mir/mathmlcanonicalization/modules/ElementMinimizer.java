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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

/**
 * Remove useless elements and attributes from MathML.
 *
 * <div class="simpleTagLabel">Input:</div>
 * <ul>
 * <li>Well-formed MathML, first module</li>
 * <li>Property file(s) with names of elements and attributes for removal or
 * preservation</li>
 * </ul>
 * <div class="simpleTagLabel">Output:</div>
 * The original code with:
 * <ul>
 * <li>removed elements insignificant for the formula searching and indexing
 * purpose (e.q. spacing and appearance altering tags) including the content
 * between open and close tag or preserving it (depending on the tag)</li>
 * <li>removed useless attributes but preserved those that are used in other
 * modules, e.q. separator attribute in mfenced element</li>
 * <li>removed attributes with default values?</li>
 * <li>removed redundant spaces?</li>
 * </ul>
 *
 * @author Maros Kucbel
 */
public class ElementMinimizer extends AbstractModule implements DOMModule {

    private static final Logger LOGGER = Logger.getLogger(ElementMinimizer.class.getName());

    private Set<String> removeWithChildren;
    private Set<String> removeKeepChildren;

    public ElementMinimizer() {
        declareProperty("remove_all");
        declareProperty("remove");
        declareProperty("keepAttributes");
        declareProperty("keepAttributes.mfrac");
        declareProperty("keepAttributes.cn");
        declareProperty("keepAttributes.ci");
        declareProperty("keepAttributes.set");
        declareProperty("keepAttributes.tendsto");
        declareProperty("keepAttributes.interval");
        declareProperty("keepAttributes.declare");
        declareProperty("keepAttributes.mfenced");
        declareProperty("keepAttributes.math");
    }

    @Override
    public void execute(final Document doc) {
        if (doc == null) {
            throw new NullPointerException("doc");
        }
        removeWithChildren = getPropertySet("remove_all");
        removeKeepChildren = getPropertySet("remove");

        LOGGER.fine("Starting ElementMinimizer");
        minimizeElements(doc.root());
        LOGGER.fine("ElementMinimizer finished succesfully");
    }

    private void minimizeElements(Element element) {
        minimizeAttributes(element);
        for (int i = 0; i < element.childNodeSize(); i++) {
            final Node childNode = element.childNode(i);
            if (childNode instanceof Comment) {
                childNode.remove();
                i--; // we need to stay at the same position
                continue;
            }
            if (childNode instanceof Element) {
                final Element child = (Element) childNode;
                if (removeWithChildren.contains(child.tagName())) {
                    child.remove();
                    i--;
                    continue;
                }
                if (removeKeepChildren.contains(child.tagName())) {
                    child.unwrap(); // removes element but keeps the children
                    i--;
                    continue;
                }
                if (child.tagName().equals(MGLYPH)) {
                    final TextNode textNode = new TextNode(child.attr("alt"));
                    child.replaceWith(textNode);
                }
                minimizeElements(child);
            }
        }
    }

    private void minimizeAttributes(Element element) {
        String property = getProperty("keepAttributes");
        final String elementPropertyName = "keepAttributes." + element.tagName();
        if (isProperty(elementPropertyName)) {
            property += " " + getProperty(elementPropertyName);
        }
        final List<String> whitelist = Arrays.asList(property.split(" "));

        final List<Attribute> attributes = element.attributes().asList();
        for (int i = 0; i < attributes.size(); i++) {
            if (!isWhitelisted(attributes.get(i), whitelist)) {
                element.removeAttr(attributes.get(i).getKey());
            }
        }
    }

    private Boolean isWhitelisted(Attribute attribute, List<String> whitelist) {
        for (String attr : whitelist) {
            if (attribute.getKey().equals(attr)
                    || attr.contains("=")
                            && attribute.getKey().equals(attr.substring(0, attr.lastIndexOf('=')))
                            && attribute.getValue().equals(attr.substring(attr.lastIndexOf('=') + 1))) {
                return true;
            }
        }
        return false;
    }

}
