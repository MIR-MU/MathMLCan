package cz.muni.fi.mir.mathmlcanonicalization.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Parent;

/**
 * Normalize the number of mrow elements in MathML.
 * 
 * <h4>Input</h4>
 * Well-formed MathML, already processed by other modules 
 * (especially ElementMinimizer, MfencedReplacer and FunctionNormalizer)
 * <h4>Output</h4>
 * The original code with changes in mrow elements:<ul>
 * <li>added mrow elements to places, where detected fenced formulae 
 * (and not already encapsulated in mrow)</li>
 * <li>removed redundant mrow elements in unneeded grouping - e.q. parents 
 * requiring only one child element accept any number of elements so the mrow 
 * tag is not needed (see example) or grouping with only presentation purpose
 * </li></ul>
 * <h4>Example</h4><pre>
 * &lt;msqrt&gt;
 *     &lt;mrow&gt;
 *         &lt;mo&gt;-&lt;/mo&gt;
 *         &lt;mn&gt;1&lt;/mn&gt;
 *     &lt;/mrow&gt;
 * &lt;/msqrt&gt;</pre>
 * is transformed to<pre>
 * &lt;msqrt&gt;
 *     &lt;mo&gt;-&lt;/mo&gt;
 *     &lt;mn&gt;1&lt;/mn&gt;
 * &lt;/msqrt&gt;
 * </pre>
 * 
 * @author Jakub Adler
 */
public class MrowNormalizer extends AbstractModule implements DOMModule {

    /**
     * Path to the property file with module settings.
     */
    private static final String PROPERTIES_FILENAME = "/res/mrow-normalizer.properties";
    private static HashMap<String, Integer> childCount;
    private static HashSet<String> openingParentheses;
    private static HashSet<String> closingParentheses;

    public MrowNormalizer() {
        loadProperties(PROPERTIES_FILENAME);
        // TODO: put some properties to the file
    }

    @Override
    public void execute(Document doc) {
        /* initialize childCount according to http://www.w3.org/TR/MathML3/chapter3.html#id.3.1.3.2
        value 1 indicates an inferred mrow as described in the document above */
        childCount = new HashMap<String, Integer>();
        //childCount.put("mrow", 0); //
        //childCount.put("mfrac", 2);
        childCount.put("msqrt", 1);
        //childCount.put("mroot", 2);
        childCount.put("mstyle", 1);
        childCount.put("merror", 1);
        childCount.put("mpadded", 1);
        childCount.put("mphantom", 1);
        //childCount.put("mfenced", 0); //
        childCount.put("menclose", 1);
        //childCount.put("msub", 2);
        //childCount.put("msup", 2);
        //childCount.put("msubsup", 3);
        //childCount.put("munder", 2);
        //childCount.put("mover", 2);
        //childCount.put("munderover", 3);
        //childCount.put("mmultiscripts", 1); //
        //childCount.put("mtable", 0);
        //childCount.put("mlabeledtr", 1); //
        //childCount.put("mtr", 0); //
        childCount.put("mtd", 1);
        //childCount.put("mstack", 0); //
        //childCount.put("longdiv", 3); //
        //childCount.put("gsgroup", 0); //
        //childCount.put("msrow", 0); //
        //childCount.put("mscarries", 0); //
        childCount.put("mscarry", 1);
        //childCount.put("maction", 1); //
        //childCount.put("math", 1);

        openingParentheses = new HashSet<String>();
        openingParentheses.add("(");
        openingParentheses.add("[");
        openingParentheses.add("{");

        closingParentheses = new HashSet<String>();
        closingParentheses.add(")");
        closingParentheses.add("]");
        closingParentheses.add("}");

        traverseChildrenElements(doc.getRootElement());
    }

    /**
     * Recursively searches element content to possibly remove or add mrow where needed.
     * @param element element to start at
     */
    private static void traverseChildrenElements(Element element) {
        List<Element> children = new ArrayList<Element>(element.getChildren());

        for (Element child : children) {
            traverseChildrenElements(child);
        }

        if (element.getName().equals("mrow")) {
            checkRemoval(element);
        } else {
            checkAddition(element);
        }
    }

    /**
     * Removes a mrow element if possible.
     * @param element the mrow element
     */
    private static void checkRemoval(Element element) {
        Parent parent = element.getParent();
        Element parentElement;

        // no parent element
        if (!(parent instanceof Element)) {
            return;
        }

        parentElement = (Element) parent;

        List<Element> children = element.getChildren();
        List<Element> siblings = parentElement.getChildren();

        if (children.size() == 1) {
            removeElement(element, parentElement);
            return;
        }

        // unknown parent element
        if (!childCount.containsKey(parentElement.getName())) {
            return;
        }

        if (childCount.get(parentElement.getName()) == 1 || // parent can accept any number of elements so we can remove mrow
                children.size() + parentElement.getChildren().size() - 1 == childCount.get(parentElement.getName()) ||
                siblings.indexOf(element) >= childCount.get(parentElement.getName())) {
            removeElement(element, parentElement);
        }
    }

    private static void removeElement(Element element, Element parent) {
        parent.addContent(parent.indexOf(element), element.cloneContent());
        element.detach();
    }

    /**
     * Wrap fenced expressions with mrow (if not) to be same as would be after mfenced replacement
     * @param element
     */
    private static void checkAddition(Element element) {

        Parent parent;
        Element parentElement;

        // get parent element
        parent = element.getParent();
        if (!(parent instanceof Element)) {
            return;
        }
        parentElement = (Element) parent;

        List<Element> siblings = parentElement.getChildren();

        // element is an opening parenthesis
        if (element.getName().equals("mo") && openingParentheses.contains(element.getTextNormalize())) {
            int nesting = 0;
            String openingStr = element.getTextNormalize();

            List<Element> children = new ArrayList<Element>();
            int openingIndex = parentElement.indexOf(element);

            for (int i = siblings.indexOf(element) + 1; i < siblings.size(); i++) {
                Element current = siblings.get(i);

                if (current.getName().equals("mo") && openingParentheses.contains(current.getTextNormalize())) {
                    // opening parenthase reached
                    nesting++;
                }

                if (current.getName().equals("mo") && closingParentheses.contains(current.getTextNormalize())) {
                    // closing parenthase reached

                    if (nesting == 0) {
                        for (Element e : children) {
                            e.detach();
                        }

                        Element innerElement;
                        if (children.isEmpty()) {
                            innerElement = null;
                        } else if (children.size() == 1/* && children.get(0).getName().equals("mrow")*/) {
                            innerElement = children.get(0);
                        } else {
                            innerElement = new Element("mrow");
                            innerElement.addContent(children);
                        }

                        // parentheses already wrapped in mrow
                        if (parentElement.getName().equals("mrow")
                                && siblings.get(0) == element
                                && siblings.get(siblings.size() - 1) == current) {
                            if (innerElement != null) {
                                parentElement.addContent(openingIndex + 1, innerElement);
                            }
                        } else {
                            element.detach();
                            current.detach();

                            Element outerMrowElement = new Element("mrow");

                            outerMrowElement.addContent(element);
                            if (innerElement != null) {
                                outerMrowElement.addContent(innerElement);
                            }
                            outerMrowElement.addContent(current);
                            parentElement.addContent(openingIndex, outerMrowElement);
                        }
                        break;
                    } else {
                        nesting--;
                    }

                }
                children.add(current);
            }
        }
    }
}
