package cz.muni.fi.mir.mathmlcanonicalization.modules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Replace mfenced elements in MathML for equivalent.
 * 
 * <h4>Input</h4>
 * Well-formed MathML, preserved non-default attributes in &lt;mfenced&gt; tags,
 *  not processed by MrowMinimizer yet
 * <h4>Output</h4>
 * The original code containing no &lt;mfenced&gt; elements, originally fenced 
 * formulas are enclosed in &lt;mrow&gt; tag, contain delimiters and separators 
 * (from &lt;mfenced&gt; attributes) in &lt;mo&gt; elements, inner content is 
 * placed into another &lt;mrow&gt; element.
 * <h4>Example</h4>
 * <pre> &lt;mfenced open="["&gt;
 *     &lt;mi&gt;x&lt;mi&gt;
 *     &lt;mi&gt;y&lt;mi&gt;
 * &lt;/mfenced&gt;</pre>
 * is transformed to<pre>
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
public class MfencedReplacer implements DOMModule {
    
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
    
    private static List<Element> getMfenced(Element element) {
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
    
    private static void replaceMfenced(Element element) {
        String openStr = element.getAttributeValue("open", "(");
        String closeStr = element.getAttributeValue("close", ")");
        char[] separators = element.getAttributeValue("separators", ",").trim().toCharArray();
        int last = separators.length - 1;
        List<Element> children = element.getChildren();
        
        // MrowMinimizer should add theese mrows too so this is probably redundant
        Element replacement = new Element("mrow").setNamespace(element.getNamespace());
        Element inside;
        if (children.size()==1 && children.get(0).getName().equals("mrow")) {
            inside = children.get(0).clone();
        } else {
            inside = new Element("mrow").setNamespace(element.getNamespace());
            for (int i = 0; i < children.size(); i++) {
                if (i > 0) {
                    char separatorChar = separators[(i > last) ? last : i];
                    String separatorString = Character.toString(separatorChar);
                    inside.addContent(new Element("mo").setText(separatorString).setNamespace(element.getNamespace()));
                }
                // FIXME: generates xmlns "attribute" (it is not considered to be attribute by JDOM)
                inside.addContent(children.get(i).clone());
            }
        }
        
        replacement.addContent(new Element("mo").setText(openStr).setNamespace(element.getNamespace()));
        replacement.addContent(inside);
        replacement.addContent(new Element("mo").setText(closeStr).setNamespace(element.getNamespace()));
        
        Element parent = element.getParentElement();
        
        int index = parent.indexOf(element);
        parent.removeContent(element);
        parent.addContent(index, replacement);
    }
}
