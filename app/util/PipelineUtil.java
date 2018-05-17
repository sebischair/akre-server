package util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bwaltl on 20.10.15.
 */
public class PipelineUtil {
    public static boolean IS_ON_ANNOTATION_BLACK_LIST(String typeName) {
        for (String excludedAnnotation : ANNOTATION_BLACK_LIST) {
            if (typeName.startsWith(excludedAnnotation))
                return true;
        }
        return false;
    }

    public static boolean IS_ON_ANNOTATION_WHITE_LIST(String typeName) {
        for (String includedAnnotation : ANNOTATION_WHITE_LIST) {
            if (typeName.startsWith(includedAnnotation))
                return true;
        }
        return false;
    }

    public static boolean IS_ON_FEATURE_BLACK_LIST(String typeName) {
        for (String excludedAnnotation : FEATURE_BLACK_LIST) {
            if (typeName.startsWith(excludedAnnotation))
                return true;
        }
        return false;
    }

    private static List<String> ANNOTATION_BLACK_LIST = new ArrayList<>();
    static {
        ANNOTATION_BLACK_LIST.add("uima.tcas.");
        ANNOTATION_BLACK_LIST.add("de.tudarmstadt.ukp.dkpro.core.api.metadata.");
        ANNOTATION_BLACK_LIST.add("org.apache.uima.ruta.type.");
        ANNOTATION_BLACK_LIST.add("de.tudarmstadt.ukp.dkpro.");
        ANNOTATION_BLACK_LIST.add("informationExtraction.lexiaTypes.ArticleHeader");
    }

    private static List<String> ANNOTATION_WHITE_LIST = new ArrayList<>();
    static {
        ANNOTATION_WHITE_LIST.add("de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN");
        ANNOTATION_WHITE_LIST.add("de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence");
    }

    private static List<String> FEATURE_BLACK_LIST = new ArrayList<>();
    static {
        FEATURE_BLACK_LIST.add("uima.cas.AnnotationBase:sofa");
        ANNOTATION_BLACK_LIST.add("de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token:pos");
    }
}
