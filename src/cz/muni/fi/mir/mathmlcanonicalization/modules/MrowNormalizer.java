package cz.muni.fi.mir.mathmlcanonicalization.modules;

import java.util.ArrayList;
import java.util.Arrays;
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

    // MathML elements
    private static final String ROW = "mrow";
    private static final String OPERATOR = "mo";

    // properties
    private static final String CHILD_COUNT_PREFIX = "childCount.";
    private static final String OPENING = "open";
    private static final String CLOSING = "close";
    private static final String WRAP_ISIDE = "wrapInside";
    private static final String WRAP_OUTSIDE = "wrapOutside";

    private final HashSet<String> openingParentheses;
    private final HashSet<String> closingParentheses;

    public MrowNormalizer() {
        loadProperties(PROPERTIES_FILENAME);

        String openingProperty = getProperty(OPENING);
        openingParentheses = new HashSet<String>(Arrays.asList(openingProperty.split(" ")));

        String closingProperty = getProperty(CLOSING);
        closingParentheses = new HashSet<String>(Arrays.asList(closingProperty.split(" ")));
    }

    @Override
    public void execute(final Document doc) {
        traverseChildrenElements(doc.getRootElement());
    }

    /**
     * Recursively searches element content to possibly remove or add mrow where needed.
     * @param element element to start at
     */
    private void traverseChildrenElements(final Element element) {
        final List<Element> children = new ArrayList<Element>(element.getChildren());

        for (Element child : children) {
            traverseChildrenElements(child);
        }

        if (element.getName().equals(ROW)) {
            checkRemoval(element);
        } else {
            checkAddition(element);
        }
    }

    /**
     * Removes a mrow element if possible.
     * 
     * @param element the mrow element
     */
    private void checkRemoval(final Element element) {
        final Parent parent = element.getParent();
        final Element parentElement;

        // no parent element
        if (!(parent instanceof Element)) {
            return;
        }

        parentElement = (Element) parent;

        List<Element> children = element.getChildren();
        List<Element> siblings = parentElement.getChildren();

        if (children.size() <= 1) {
            removeElement(element, parentElement);
            return;
        }

        // unknown parent element
        if (getProperty(CHILD_COUNT_PREFIX + parentElement.getName()) == null) {
            return;
        }

        final int childCount;
        try {
            childCount = Integer.parseInt(getProperty(CHILD_COUNT_PREFIX + parentElement.getName()));
        } catch (NumberFormatException e) {
            return;
        }

        if (childCount == 1 || // parent can accept any number of elements so we can remove mrow
                children.size() + parentElement.getChildren().size() - 1 == childCount) {
            removeElement(element, parentElement);
        }
    }

    private static void removeElement(final Element element, final Element parent) {
        parent.addContent(parent.indexOf(element), element.cloneContent());
        element.detach();
    }

    private Boolean isOpening(final Element element) {
        return element.getName().equals(OPERATOR)
            && openingParentheses.contains(element.getTextNormalize());
    }

    private Boolean isClosing(final Element element) {
        return element.getName().equals(OPERATOR)
            && closingParentheses.contains(element.getTextNormalize());
    }

    /**
     * Wrap previously detected fenced expressions in mrow to be same as output of MfencedReplacer
     * @param parent parent element
     * @param siblings children of parent element
     * @param fenced list of elements inside parentheses, children of parent element
     * @param opening opening parenthesis, child of parent element
     * @param closing closing parenthesis, child of parent element
     */
    private void wrapFenced(final Element parent, final List<Element> siblings,
            final List<Element> fenced, final Element opening, final Element closing) {

        for (Element e : fenced) {
            e.detach();
        }

        final int openingIndex = parent.indexOf(opening);

        // Element to be placed inside parentheses..
        // If null, the original 'fenced' list will be used.
        final Element innerElement;
        if (fenced.isEmpty() || !isEnabled(WRAP_ISIDE)) {
            // will not wrap inside in mrow
            innerElement = null;
        } else if (fenced.size() == 1) {
            // no need to wrap just one element
            innerElement = fenced.get(0);
        } else {
            innerElement = new Element(ROW);
            innerElement.addContent(fenced);
        }

        if ((parent.getName().equals(ROW)
                && siblings.get(0) == opening
                && siblings.get(siblings.size() - 1) == closing) || !isEnabled(WRAP_OUTSIDE)) {
            // will not wrap outside in mrow
            if (innerElement == null) {
                parent.addContent(openingIndex + 1, fenced);
            } else {
                parent.addContent(openingIndex + 1, innerElement);
            }
        } else {
            // wrap outside in mrow
            opening.detach();
            closing.detach();

            final Element outerMrowElement = new Element(ROW);

            outerMrowElement.addContent(opening);
            if (innerElement != null) {
                outerMrowElement.addContent(innerElement);
            } else {
                outerMrowElement.addContent(fenced);
            }
            outerMrowElement.addContent(closing);
            parent.addContent(openingIndex, outerMrowElement);
        }
    }

    /**
     * Add mrow if necessary
     */
    private void checkAddition(final Element element) {

        final Parent parent = element.getParent();
        
        if (!(parent instanceof Element)) {
            return;
        }
        final Element parentElement = (Element) parent;

        final List<Element> siblings = parentElement.getChildren();

        if (isOpening(element)) {
            // Element is an opening parenthesis.
            // Need to find matching closing parenthesis and register the elements between them.
            int nesting = 0;

            // list of elements inside parentheses
            final List<Element> fenced = new ArrayList<Element>();

            for (int i = siblings.indexOf(element) + 1; i < siblings.size(); i++) {
                Element current = siblings.get(i);

                if (isOpening(current)) {
                    // opening parenthase reached
                    nesting++;
                } else if (isClosing(current)) {
                    // closing parenthase reached
                    if (nesting == 0) {
                        // matching closing parenthase
                        wrapFenced(parentElement, siblings, fenced, element, current);
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
