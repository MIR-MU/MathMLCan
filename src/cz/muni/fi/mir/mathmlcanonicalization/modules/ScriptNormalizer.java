package cz.muni.fi.mir.mathmlcanonicalization.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;

/**
 * Handle sub/super/under/over/multi script elements in MathML.
 *
 * <p>Normalize the occurence of &lt;msub&gt;, &lt;msup&gt;, &lt;msubsup&gt;,
 * &lt;munder&gt;, &lt;mover&gt;, &lt;munderover&gt; and &lt;mmultiscripts&gt;
 * (with children &lt;mprescripts/&gt; and &lt;none/&gt;) elements in MathML.
 * </p><h4>Input</h4>
 * Well-formed MathML
 * <h4>Output</h4>
 * The original code with always used:<ul>
 * <li>&lt;msubsup&gt; (or &lt;msub&gt;) for sums, integrals, etc. (converted
 * from &lt;munderover&gt;, &lt;munder&gt; and &lt;msub&gt;, &lt;msup&gt;
 * combinations)</li>
 * <li>&lt;msub&gt; inside &lt;msup&gt; in nested formulae</li>
 * <li>nested &lt;msub&gt; and &lt;msup&gt; instead of &lt;msubsup&gt; in
 * identifiers (not for sums, integrals, etc.)</li>
 * <li>Unicode scripts converted to MathML scripts</li>
 * <li>(sub/super)scripts instead of &lt;mmultiscript&gt; where possible</li>
 * <li>maybe conversion all (under/over)scripts to (sub/super) scripts?</li>
 * </ul>
 *
 * @author Jaroslav Dufek
 * @author David Formanek
 */
public class ScriptNormalizer extends AbstractModule implements DOMModule {

    /**
     * Path to the property file with module settings.
     */
    private static final String PROPERTIES_FILENAME = "/res/script-normalizer.properties";
    private static final Logger LOGGER = Logger.getLogger(ScriptNormalizer.class.getName());
    
    // properties key names
    private static final String SWAP_SCRIPTS = "swapscripts";
    private static final String SPLIT_SCRIPTS_ELEMENTS = "splitscriptselements";
    private static final String UNIFY_SCRIPTS = "unifyscripts";

    public ScriptNormalizer() {
        loadProperties(PROPERTIES_FILENAME);
    }

    @Override
    public void execute(final Document doc) {
        if (doc == null) {
            throw new NullPointerException("doc");
        }
        final Element root = doc.getRootElement();
        if (isEnabled(UNIFY_SCRIPTS)) {
            final Map<String, String> replaceMap = new HashMap<String, String>();
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
            LOGGER.fine("Msubsup conversion is switched off");
        } else {
            normalizeMsubsup(root, chosenElements);
        }
        // TODO: convert sub/sup combination with not chosen elements to subsup
    }

    private void normalizeSupInSub(final Element element) {
        assert element != null;
        final List<Element> children = element.getChildren();
        for (int i = 0; i < children.size(); i++) {
            final Element actual = children.get(i);
            normalizeSupInSub(actual);
            if (!actual.getName().equals(SUBSCRIPT)) {
                continue;
            }
            List<Element> subscriptChildren = actual.getChildren();
            if (subscriptChildren.size() != 2) {
                LOGGER.info("Invalid msub, skipped");
                continue;
            }
            if (!subscriptChildren.get(0).getName().equals(SUPERSCRIPT)) {
                continue;
            }
            final List<Element> superscriptChildren = subscriptChildren.get(0).getChildren();
            if (superscriptChildren.size() != 2) {
                LOGGER.info("Invalid msup, skipped");
                continue;
            }
            final Element newMsub = new Element(SUBSCRIPT);
            newMsub.addContent(superscriptChildren.get(0).detach());
            newMsub.addContent(subscriptChildren.get(1).detach());
            final Element newMsup = new Element(SUPERSCRIPT);
            newMsup.addContent(newMsub);
            newMsup.addContent(superscriptChildren.get(0).detach());
            children.set(i, newMsup);
            LOGGER.fine("Sub/sup scripts swapped");
        }
    }

    private void normalizeMsubsup(final Element element, Collection<String> firstChildren) {
        assert element != null && firstChildren != null;
        final List<Element> children = element.getChildren();
        for (int i = 0; i < children.size(); i++) {
            final Element actual = children.get(i);
            if (actual.getName().equals(SUBSUP)) {
                final List<Element> actualChildren = actual.getChildren();
                if (actualChildren.size() != 3) {
                    LOGGER.info("Invalid msubsup, skipped");
                    continue;
                }
                if (!firstChildren.contains(actualChildren.get(0).getName())) {
                    continue;
                }
                final Element newMsub = new Element(SUBSCRIPT);
                newMsub.addContent(actualChildren.get(0).detach());
                newMsub.addContent(actualChildren.get(0).detach());
                final Element newMsup = new Element(SUPERSCRIPT);
                newMsup.addContent(newMsub);
                newMsup.addContent(actualChildren.get(0).detach());
                children.set(i, newMsup);
                i--; // move back to check the children of the new transformation
                LOGGER.fine("Msubsup converted to nested msub and msup");
            } else {
                normalizeMsubsup(actual, firstChildren);
            }
        }
    }
    
    private void replaceDescendants(final Element ancestor, final Map<String, String> map) {
        assert ancestor != null && map != null;
        final List<Element> elements = new ArrayList<Element>();
        for (Element element : ancestor.getDescendants(new ElementFilter())) {
            if (map.containsKey(element.getName())) {
                elements.add(element);
            }
        }
        for (Element element : elements) {
            replaceElement(element, map.get(element.getName()));
        }
    }
    
    private void replaceElement(final Element toReplace, final String replacementName) {
        assert toReplace != null && replacementName != null;
        assert !replacementName.isEmpty();
        final Element parent = toReplace.getParentElement();
        assert parent != null;
        final Element replacement = new Element(replacementName);
        replacement.addContent(toReplace.removeContent());
        final List<Attribute> attributes = toReplace.getAttributes();
        for (Attribute attribute : attributes) {
            replacement.setAttribute(attribute.detach());
        }
        final int parentIndex = parent.indexOf(toReplace);
        parent.removeContent(parentIndex);
        parent.addContent(parentIndex, replacement);
        LOGGER.log(Level.FINE, "{0} replaced with {1}",
                new Object[]{toReplace, replacementName});
    }
}
