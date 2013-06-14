package cz.muni.fi.mir.mathmlcanonicalization.modules;

import java.util.ArrayList;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Replace mfenced elements in MathML for equivalent.
 *
 * <h4>Input</h4>
 * Well-formed MathML, preserved non-default attributes in &lt;mfenced&gt; tags,
 * not processed by MrowMinimizer yet
 * <h4>Output</h4>
 * The original code containing no &lt;mfenced&gt; elements, originally fenced
 * formulas are enclosed in &lt;mrow&gt; tag, contain delimiters and separators
 * (from &lt;mfenced&gt; attributes) in &lt;mo&gt; elements, inner content is
 * placed into another &lt;mrow&gt; element.
 * <h4>Example</h4>
 * <pre> &lt;mfenced open="["&gt;
 *     &lt;mi&gt;x&lt;mi&gt;
 *     &lt;mi&gt;y&lt;mi&gt;
 * &lt;/mfenced&gt;</pre> is transformed to<pre>
 * &lt;mrow&gt;
 *     &lt;mo&gt;[&lt;/mo&gt;
 *     &lt;mrow&gt;
 *         &lt;mi&gt;x&lt;mi&gt;
 *         &lt;mo&gt;,&lt;/mo&gt;
 *         &lt;mi&gt;y&lt;mi&gt;
 *     &lt;/mrow&gt;
 *     &lt;mo&gt;)&lt;/mo&gt;
 * &lt;/mrow&gt;</pre>
 *
 * @author David Formanek
 */
public class MfencedReplacer extends AbstractModule implements DOMModule {

    /**
     * Path to the property file with module settings.
     */
    private static final String PROPERTIES_FILENAME = "/res/mfenced-replacer.properties";
    // MathML elements
    private static final String ROW = "mrow";
    private static final String OPERATOR = "mo";
    private static final String FENCED = "mfenced";
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

    public MfencedReplacer() {
        loadProperties(PROPERTIES_FILENAME);
    }

    @Override
    public void execute(final Document doc) {
        if (doc == null) {
            throw new IllegalArgumentException("document is null");
        }
        List<Element> toReplace = getMfenced(doc.getRootElement());
        for (int i = 0; i < toReplace.size(); i++) {
            Element e = toReplace.get(i);
            replaceMfenced(e);
        }
    }

    private List<Element> getMfenced(final Element element) {
        List<Element> toReplace = new ArrayList<Element>();
        for (Element e : element.getChildren()) {
            toReplace.addAll(getMfenced(e));
            if (e.getName().equals(FENCED)) {
                toReplace.add(e);
            }
        }
        return toReplace;
    }

    private void replaceMfenced(final Element mfencedElement) {
        final char[] separators = getSeparators(mfencedElement);
        final Namespace ns = mfencedElement.getNamespace();
        final List<Element> children = mfencedElement.getChildren();
        final int nChildren = children.size();
        final int last = Math.min(separators.length - 1, nChildren - 2);

        Element insideFence = null;
        if (nChildren == 1 && children.get(0).getName().equals(ROW)) {
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
        final Namespace NS = mfencedElement.getNamespace();
        Element replacement = new Element(ROW, NS);
        String openStr = getProperty(DEFAULT_OPEN);
        String closeStr = getProperty(DEFAULT_CLOSE);
        
        if (!isEnabled(FORCE_DEFAULT_OPEN)) {
            openStr = mfencedElement.getAttributeValue(OPEN_FENCE, openStr);
        }
        if (!isEnabled(FORCE_DEFAULT_CLOSE)) {
            closeStr = mfencedElement.getAttributeValue(CLOSE_FENCE, closeStr);
        }
        
        replacement.addContent(new Element(OPERATOR, NS).setText(openStr));
        if (insideContent != null) {
            replacement.addContent(insideContent);
        }
        replacement.addContent(new Element(OPERATOR, NS).setText(closeStr));

        final Element parent = mfencedElement.getParentElement();
        final int index = parent.indexOf(mfencedElement);
        parent.removeContent(index);
        parent.addContent(index, replacement);
    }
    
    private char[] getSeparators(final Element element) {
        if (isEnabled(FORCE_DEFAULT_SEPARATORS)) {
            return getProperty(DEFAULT_SEPARATORS).toCharArray();
        }
        return element.getAttributeValue(SEPARATORS,
                getProperty(DEFAULT_SEPARATORS)).trim().toCharArray();
    }
}
