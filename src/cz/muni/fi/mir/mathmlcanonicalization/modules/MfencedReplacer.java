package cz.muni.fi.mir.mathmlcanonicalization.modules;

import java.util.ArrayList;
import java.util.Iterator;
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
    private static final String MROW = "mrow";
    private static final String MO = "mo";

    public MfencedReplacer() {
        loadProperties(PROPERTIES_FILENAME);
    }

    @Override
    public void execute(Document doc) {
        if (doc == null) {
            throw new IllegalArgumentException("document is null");
        }
        List<Element> toReplace = getMfenced(doc.getRootElement());
        for (int i = 0; i < toReplace.size(); i++) {
            Element e = toReplace.get(i);
            replaceMfenced(e);
        }
    }

    private List<Element> getMfenced(Element element) {
        List<Element> toReplace = new ArrayList<Element>();
        Iterator<Element> iterator = element.getChildren().iterator();
        while (iterator.hasNext()) {
            Element e = iterator.next();
            toReplace.addAll(getMfenced(e));

            if (e.getName().equals("mfenced")) {
                toReplace.add(e);
            }
        }
        return toReplace;
    }

    private void replaceMfenced(Element element) {
        String openStr = getProperty("open");
        if (!isEnabled("forceopen")) {
            openStr = element.getAttributeValue("open", openStr);
        }
        String closeStr = getProperty("close");
        if (!isEnabled("forceclose")) {
            closeStr = element.getAttributeValue("close", closeStr);
        }
        char[] separators;
        if (isEnabled("forceseparators")) {
            separators = getProperty("separators").toCharArray();
        } else {
            separators = element.getAttributeValue("separators",
                    getProperty("separators")).trim().toCharArray();
        }
        
        Namespace namespace = element.getNamespace();
        List<Element> children = element.getChildren();
        int nChildren = children.size();
        int last = Math.min(separators.length - 1, nChildren - 2);

        Element replacement = new Element(MROW).setNamespace(namespace);
        Element inside;
        if (nChildren == 1 && children.get(0).getName().equals(MROW)) {
            inside = children.get(0).detach();
        } else {
            inside = new Element(MROW).setNamespace(namespace);
            for (int i = 0; i < nChildren; i++) {
                if (i > 0 && last >= 0) {
                    char separatorChar = separators[(i - 1 > last) ? last : i - 1];
                    String separatorString = Character.toString(separatorChar);
                    inside.addContent(new Element(MO).setText(separatorString)
                            .setNamespace(namespace));
                }
                inside.addContent(children.get(0).detach());
            }
        }

        replacement.addContent(new Element(MO).setText(openStr)
                .setNamespace(namespace));
        if (nChildren != 0) {
            replacement.addContent(inside);
        }
        replacement.addContent(new Element(MO).setText(closeStr)
                .setNamespace(namespace));

        Element parent = element.getParentElement();
        int index = parent.indexOf(element);
        parent.removeContent(index);
        parent.addContent(index, replacement);
    }
}
