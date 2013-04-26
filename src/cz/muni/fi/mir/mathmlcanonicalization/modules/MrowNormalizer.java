package cz.muni.fi.mir.mathmlcanonicalization.modules;

import java.util.HashMap;
import org.jdom2.Document;
import org.jdom2.Element;

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
 * @author David Formánek
 */
public class MrowNormalizer extends AbstractModule implements DOMModule {
    
    /**
     * Path to the property file with module settings.
     */
    private static final String PROPERTIES_FILENAME = "/res/mrow-normalizer.properties";
    
    private static HashMap<String, Integer> childCount;
    
    public MrowNormalizer() {
        loadDefaultProperties(PROPERTIES_FILENAME);
        // TODO: put some properties to the file
    }
    
    @Override
    public void execute(Document doc) {
        // TODO: initialize childCount according to http://www.w3.org/TR/MathML3/chapter3.html#id.3.1.3.2
        traverseChildrenElements(doc.getRootElement());
    }

    private static void traverseChildrenElements(Element element) {

        if (element.getName().equals("mrow")) {
            checkRemoval(element);
        } else {
            checkAddition(element);
        }

        for (Element child : element.getChildren()) {
            traverseChildrenElements(child);
        }
    }
    
    private static void checkRemoval(Element element) {
        // TODO: compare children+siblings count with childCount and possibly remove mrow
    }
    
    private static void checkAddition(Element element) {
        // TODO: wrap fenced expressions with mrow (if not) to be same as would be after mfenced replacement
    }
}
