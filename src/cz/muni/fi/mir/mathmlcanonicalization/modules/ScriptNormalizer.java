package cz.muni.fi.mir.mathmlcanonicalization.modules;

import java.util.Arrays;
import java.util.List;
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
 * <li>nested &lt;msub&gt; and &lt;msup&gt; instead of &lt;msubsup&gt;
 * in identifiers (not for sums, integrals, etc.)</li>
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
    
    private static List<String> scripts;
    
    public ScriptNormalizer() {
        loadProperties(PROPERTIES_FILENAME);
    }
    
    @Override
    public void execute(Document doc) {
        if (isEnabled("remove_empty") || isEnabled("normalize_oneitem")) {
            scripts = Arrays.asList(getProperty("scripts").split(" "));
        }
        if (isEnabled("remove_empty"))
            removeEmptyScripts(doc.getRootElement());
        if (isEnabled("normalize_oneitem"))
            normalizeOneItemScripts(doc.getRootElement());
        if (isEnabled("normalize_sub_in_sup"))
            normalizeSupInSub(doc.getRootElement());
        if (isEnabled("normalize_msubsup"))
            normalizeMsubsup(doc.getRootElement());
    }
    
    private void removeEmptyScripts(Element element) {
        List<Element> children = element.getChildren();
        for(int i = 0; i < children.size(); i++) {
            Element actual = children.get(i); // actual element
            removeEmptyScripts(actual);
            if (scripts.contains(actual.getName())) {
                List<Element> actualList = actual.getChildren();
                
                if(actualList.size() < 1) { // no entry at all
                    actual.detach();
                    i--; // move iterator back because element was removed and next is on his place now
                }
            }
        }
    }
    
    private void normalizeOneItemScripts(Element element) {
        List<Element> children = element.getChildren();
        for(int i = 0; i < children.size(); i++) {
            Element actual = children.get(i); // actual element
            normalizeOneItemScripts(actual);
            if (scripts.contains(actual.getName())) {
                List<Element> actualList = actual.getChildren();
                
                if(actualList.size() == 1) // one element entry
                    children.set(i, actualList.get(0).detach());
            }
        }
    }
    
    private void normalizeSupInSub (Element element) {
        List<Element> children = element.getChildren();
        for(int i = 0; i < children.size(); i++) {
            Element actual = children.get(i); // actual element
            normalizeSupInSub(actual);
            if (actual.getName().equals("msub")) {
                List<Element> msubList = actual.getChildren();
                if (msubList.size() > 1) { // well-formed msub, spare elements are discarded
                    if (msubList.get(0).getName().equals("msup")) {
                        List<Element> msupList = msubList.get(0).getChildren();
                        if (msupList.size() > 1) { // well-formed msup, spare elements are discarded
                            Element newMsub = new Element("msub");
                            newMsub.addContent(msupList.get(0).detach());
                            newMsub.addContent(msubList.get(1).detach());

                            Element newMsup = new Element("msup");
                            newMsup.addContent(newMsub);
                            newMsup.addContent(msupList.get(0).detach());
                            children.set(i, newMsup);
                        }
                    }
                }
            }
        }
    }
    
    private void normalizeMsubsup (Element element) {
        List<Element> children = element.getChildren();
        for(int i = 0; i < children.size(); i++) {
            Element actual = children.get(i); // actual element
            if (actual.getName().equals("msubsup")) {
                List<Element> actualList = actual.getChildren();
                
                if (actualList.size() == 2) { // just entry with base and subscript
                    Element newMsub = new Element("msub");
                    newMsub.addContent(actualList.get(0).detach());
                    newMsub.addContent(actualList.get(0).detach());
                    children.set(i, newMsub);
                } else if (actualList.size() > 2) { // complete msubsup, spare elements are discarded
                    Element newMsub = new Element("msub");
                    newMsub.addContent(actualList.get(0).detach());
                    newMsub.addContent(actualList.get(0).detach());
                    
                    Element newMsup = new Element("msup");
                    newMsup.addContent(newMsub);
                    newMsup.addContent(actualList.get(0).detach());
                    children.set(i, newMsup);
                }
                i--; // move iterator back so it will check the children of new transformation
            } else {
                normalizeMsubsup(actual);
            }
        }
    }
}
