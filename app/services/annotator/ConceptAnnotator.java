package services.annotator;

import model.DBpediaToken;
import model.Expert;
import model.Method;
import model.Template;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import services.annotationType.concept.ConceptAnnotatorResource;
import util.StaticFunctions;

import java.util.ArrayList;
import java.util.List;

/**
 * An example annotator that annotates Tokens and Sentences.
 */
public class ConceptAnnotator extends JCasAnnotator_ImplBase {
  @Override
  public void initialize(UimaContext ctx) throws ResourceInitializationException {
    super.initialize(ctx);
  }

  @Override
  public void destroy() {
    super.destroy();
  }

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {
    String documentText = jcas.getDocumentText().toLowerCase();

    if (documentText == null || documentText.isEmpty()) {
      return;
    }

    List<String> customAnnotationWords = getCustomAnnotationWords();
    addAnnotations(jcas, customAnnotationWords, StaticFunctions.CUSTOM);

    addAnnotations(jcas, new Expert().findAllExpertiseTokens(), StaticFunctions.EXPERTISE);

    addAnnotations(jcas, new Method().findAllMethodNames(), StaticFunctions.METHOD);

    addAnnotations(jcas, new Template().findAllTemplateNames(), StaticFunctions.TEMPLATE);
  }

  private void addAnnotations(JCas jcas, List<String> tokens, String conceptType) {
    String documentText = jcas.getDocumentText().toLowerCase();

    for(String token: tokens) {
      int index = documentText.indexOf(token.toLowerCase());

      while(index >= 0) {
        ConceptAnnotatorResource concept = new ConceptAnnotatorResource(jcas);
        concept.setBegin(index);
        concept.setEnd(index+token.length());
        concept.setConceptType(conceptType);
        concept.setSimilarityScore(100);
        concept.setName(token);
        concept.addToIndexes();

        index = documentText.indexOf(token, index+1);
      }
    }
  }

  public List<String> getCustomAnnotationWords() {
    List<DBpediaToken> savedTokens = new DBpediaToken().findAllCustomTokens();
    List<String> tokens = new ArrayList<String>();
    for(DBpediaToken token : savedTokens) {
      if(token.getScore() >= 0) {
        tokens.add(token.getName());
      }
    }
    return tokens;
  }

}