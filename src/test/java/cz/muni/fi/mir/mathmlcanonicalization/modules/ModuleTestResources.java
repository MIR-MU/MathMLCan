package cz.muni.fi.mir.mathmlcanonicalization.modules;

import java.io.InputStream;
import java.util.Iterator;

/**
 * Class gives access to all *.original.html test resources.
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
            return ModuleTestResources.class.getResourceAsStream(TEST_RESOURCES[index++]);
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
            "ElementMinimizerTest/attributes.original.html",
            "ElementMinimizerTest/comments.original.html",
            "ElementMinimizerTest/mfrac.original.html",
            "ElementMinimizerTest/mglyph.original.html",
            "ElementMinimizerTest/mphantom.original.html",
            "FunctionNormalizerTest/function2.original.html",
            "FunctionNormalizerTest/function3.original.html",
            "FunctionNormalizerTest/function.original.html",
            "FunctionNormalizerTest/sin.original.html",
            "MfencedReplacerTest/blank-separators.original.html",
            "MfencedReplacerTest/interval-configured.original.html",
            "MfencedReplacerTest/interval.original.html",
            "MfencedReplacerTest/nested.original.html",
            "MfencedReplacerTest/no-children.original.html",
            "MfencedReplacerTest/sequence-separators.original.html",
            "MrowNormalizerTest/configured.original.html",
            "MrowNormalizerTest/frac.original.html",
            "MrowNormalizerTest/interval.original.html",
            "MrowNormalizerTest/mixed1.original.html",
            "MrowNormalizerTest/mixed2.original.html",
            "MrowNormalizerTest/mixed3.original.html",
            "MrowNormalizerTest/mixed4.original.html",
            "MrowNormalizerTest/parentheses1.original.html",
            "MrowNormalizerTest/parentheses2.original.html",
            "MrowNormalizerTest/parentheses3.original.html",
            "MrowNormalizerTest/parentheses4.original.html",
            "MrowNormalizerTest/parentheses5.original.html",
            "MrowNormalizerTest/sqrt.original.html",
            "MrowNormalizerTest/tuple.original.html",
            "OperatorNormalizerTest/identifier-replacement.original.html",
            "OperatorNormalizerTest/multiplication-blank.original.html",
            "OperatorNormalizerTest/multiplication-cdot.original.html",
            "OperatorNormalizerTest/operator2identifier.original.html",
            "OperatorNormalizerTest/operator-unification.original.html",
            "OperatorNormalizerTest/unicode.original.html",
            "ScriptNormalizerTest/complexsubsup.original.html",
            "ScriptNormalizerTest/invalid-scripts.original.html",
            "ScriptNormalizerTest/nested-sub-sup.original.html",
            "ScriptNormalizerTest/sub-sup.original.html",
            "ScriptNormalizerTest/subsup.original.html",
            "ScriptNormalizerTest/underover.original.html"
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
