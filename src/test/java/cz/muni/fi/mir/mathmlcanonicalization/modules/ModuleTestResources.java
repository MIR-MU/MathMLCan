package cz.muni.fi.mir.mathmlcanonicalization.modules;

import java.io.InputStream;
import java.util.Iterator;

/**
 * Class gives access to all *.original.xml test resources.
 */
public class ModuleTestResources {

    private static final class ResourceIterator implements Iterator<InputStream> {

        private int index = 0;

        @Override
        public boolean hasNext() {
            return index < TEST_RESOURCES.length;
        }

        @Override
        public InputStream next() {
            return ModuleTestResources.class.getResourceAsStream( TEST_RESOURCES[index++] );
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Test resources relative to this class package.
     */
    private static final String[] TEST_RESOURCES = {
        "ElementMinimizerTest/attributes.original.xml",
        "ElementMinimizerTest/comments.original.xml",
        "ElementMinimizerTest/mfrac.original.xml",
        "ElementMinimizerTest/mphantom.original.xml",
        "FunctionNormalizerTest/function2.original.xml",
        "FunctionNormalizerTest/function3.original.xml",
        "FunctionNormalizerTest/function.original.xml",
        "FunctionNormalizerTest/sin.original.xml",
        "MfencedReplacerTest/blank-separators.original.xml",
        "MfencedReplacerTest/interval-configured.original.xml",
        "MfencedReplacerTest/interval.original.xml",
        "MfencedReplacerTest/nested.original.xml",
        "MfencedReplacerTest/no-children.original.xml",
        "MfencedReplacerTest/sequence-separators.original.xml",
        "MrowNormalizerTest/configured.original.xml",
        "MrowNormalizerTest/frac.original.xml",
        "MrowNormalizerTest/interval.original.xml",
        "MrowNormalizerTest/mixed1.original.xml",
        "MrowNormalizerTest/mixed2.original.xml",
        "MrowNormalizerTest/mixed3.original.xml",
        "MrowNormalizerTest/mixed4.original.xml",
        "MrowNormalizerTest/parentheses1.original.xml",
        "MrowNormalizerTest/parentheses2.original.xml",
        "MrowNormalizerTest/parentheses3.original.xml",
        "MrowNormalizerTest/parentheses4.original.xml",
        "MrowNormalizerTest/parentheses5.original.xml",
        "MrowNormalizerTest/sqrt.original.xml",
        "MrowNormalizerTest/tuple.original.xml",
        "OperatorNormalizerTest/identifier-replacement.original.xml",
        "OperatorNormalizerTest/multiplication-blank.original.xml",
        "OperatorNormalizerTest/multiplication-cdot.original.xml",
        "OperatorNormalizerTest/operator2identifier.original.xml",
        "OperatorNormalizerTest/operator-unification.original.xml",
        "OperatorNormalizerTest/unicode.original.xml",
        "ScriptNormalizerTest/complexsubsup.original.xml",
        "ScriptNormalizerTest/invalid-scripts.original.xml",
        "ScriptNormalizerTest/nested-sub-sup.original.xml",
        "ScriptNormalizerTest/sub-sup.original.xml",
        "ScriptNormalizerTest/subsup.original.xml",
        "ScriptNormalizerTest/underover.original.xml"
    };
    
    public static Iterable<InputStream> getAllTestResources() {
        return new Iterable<InputStream>() {
            @Override
            public Iterator<InputStream> iterator() {
                return new ResourceIterator();
            }
        };
    }
    
}
