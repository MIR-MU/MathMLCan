package cz.muni.fi.mir.mathmlcanonicalization.modules;

import org.jdom2.Document;

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
 * @author David Formanek
 */
public class ScriptNormalizer {
    /**
     * Normalize the occurence of &lt;msub&gt;, &lt;msup&gt;, &lt;msubsup&gt;, 
     * &lt;munder&gt;, &lt;mover&gt;, &lt;munderover&gt;, &lt;mmultiscripts&gt;.
     * 
     * @param doc DOM document to be canonicalized
     */
    public static void execute(Document doc) {
        // TODO: ScriptNormalizer implementation
    }
}
