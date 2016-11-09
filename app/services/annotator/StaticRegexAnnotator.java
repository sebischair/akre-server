package services.annotator;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import services.annotationType.StaticRegex.StaticRegex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mahabaleshwar on 11/2/2016.
 */
public class StaticRegexAnnotator extends JCasAnnotator_ImplBase {
    private Pattern pattern1 = Pattern.compile("\\bcu[a-zA-Z0-9_]+\\b");
    private Pattern pattern2 = Pattern.compile("wait\\(");

    @Override
    public void initialize(UimaContext ctx) throws ResourceInitializationException {
        super.initialize(ctx);
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        String documentText = jCas.getDocumentText().toLowerCase();
        if (documentText == null || documentText.isEmpty()) {
            return;
        }
        addAnnotations(jCas, documentText, pattern1, "Hardcoded system name detected");
        addAnnotations(jCas, documentText, pattern2, "Hardcoded wait function detected");
    }

    private void addAnnotations(JCas aJCas, String docText, Pattern pattern, String name) {
        Matcher matcher = pattern.matcher(docText);
        int pos = 0;
        while (matcher.find(pos)) {
            StaticRegex annotation = new StaticRegex(aJCas);
            annotation.setBegin(matcher.start());
            annotation.setEnd(matcher.end());
            annotation.setName(name);
            annotation.addToIndexes();
            pos = matcher.end();
        }
    }
}
