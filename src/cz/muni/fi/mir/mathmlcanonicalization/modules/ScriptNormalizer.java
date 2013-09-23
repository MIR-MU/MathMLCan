package cz.muni.fi.mir.mathmlcanonicalization.modules;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import org.jdom2.Document;
import org.jdom2.Element;

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

    public ScriptNormalizer() {
        loadProperties(PROPERTIES_FILENAME);
    }

    @Override
    public void execute(final Document doc) {
        if (doc == null) {
            throw new IllegalArgumentException("document is null");
        }
        // TODO: convert munder/mover/munderover to scripts
        // TODO: normalize unconverted munder/mover/munderover
        // TODO: convert multiscript where possible
        final Element root = doc.getRootElement();
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
    // TODO: move to mrowNormalizer with other elements removing too
    /*
     private void removeEmptyScripts(final Element element) {
     final List<Element> children = element.getChildren();
     for (int i = 0; i < children.size(); i++) {
     final Element actual = children.get(i); // actual element
     removeEmptyScripts(actual);
     if (scripts.contains(actual.getName())) {
     final List<Element> actualList = actual.getChildren();

     if (actualList.size() < 1) { // no entry at all
     actual.detach();
     i--; // move iterator back because element was removed and next is on his place now
     }
     }
     }
     }

     private void normalizeOneItemScripts(final Element element) {
     final List<Element> children = element.getChildren();
     for (int i = 0; i < children.size(); i++) {
     final Element actual = children.get(i); // actual element
     normalizeOneItemScripts(actual);
     if (scripts.contains(actual.getName())) {
     final List<Element> actualList = actual.getChildren();

     if (actualList.size() == 1) {
     children.set(i, actualList.get(0).detach());
     }
     }
     }
     }
     */
}
