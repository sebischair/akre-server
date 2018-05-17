package services.pipeline;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.DBpediaToken;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import play.libs.Json;
import services.SparqlQueryExecuter;
import services.annotator.ConceptAnnotator;
import services.annotator.SpotlightAnnotator;
import util.PipelineUtil;
import util.StaticFunctions;
import util.UimaUtil;

import java.util.List;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

/**
 * Created by mahabaleshwar on 6/23/2016.
 */
public class ConceptAnnotatorPipeline extends UIMAPipeline {
    @Override
    public ArrayNode processDocument() {
        ArrayNode annotations = Json.newArray();
        try {

            AnalysisEngineDescription conceptAnnotatorDesc = createEngineDescription(ConceptAnnotator.class);

            AnalysisEngineDescription spotlightDesc = createEngineDescription(SpotlightAnnotator.class);

            AnalysisEngineDescription basicPipeDesc = createEngineDescription(spotlightDesc, conceptAnnotatorDesc);

            AnalysisEngine pipe = createEngine(basicPipeDesc);

            JCas jCas = UimaUtil.produceJCas(StaticFunctions.CONCEPT, StaticFunctions.SPOTLIGHT);
            jCas.setDocumentText(this.getDocument().getRawContent());
            jCas.setDocumentLanguage(this.getDocument().getLanguage());
            pipe.process(jCas);
            annotations = getAnnotations(jCas);

            jCas.reset();
            pipe.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return annotations;
    }

    private ArrayNode getAnnotations(JCas jCas) {
        ArrayNode annotations = Json.newArray();
        FSIterator iterator = jCas.getAnnotationIndex().iterator();
        while (iterator.hasNext()) {
            Annotation a = (Annotation) iterator.next();
            Type annotationType = a.getType();
            if (PipelineUtil.IS_ON_ANNOTATION_BLACK_LIST(annotationType.getName()) && !PipelineUtil.IS_ON_ANNOTATION_WHITE_LIST(annotationType.getName())) continue;

            List<Feature> featureList = annotationType.getFeatures();
            ObjectNode featureAsJson = Json.newObject();
            for (Feature feature : featureList) {
                if (!PipelineUtil.IS_ON_FEATURE_BLACK_LIST(feature.getName())) {
                    featureAsJson.put(feature.getShortName(), a.getFeatureValueAsString(feature));
                }
            }
				
            if(featureAsJson.has(StaticFunctions.BEGIN) && featureAsJson.has(StaticFunctions.END))
                featureAsJson.put(StaticFunctions.TOKEN, this.getDocument().getRawContent().substring(Integer.parseInt(featureAsJson.get(StaticFunctions.BEGIN).asText()), Integer.parseInt(featureAsJson.get(StaticFunctions.END).asText())));

            DBpediaToken savedToken = new DBpediaToken().findByName(featureAsJson.get(StaticFunctions.TOKEN).asText());
           if(savedToken != null && savedToken.getScore() >= 0) {
                featureAsJson.put(StaticFunctions.CONCEPTTYPE, savedToken.getType());
           } else if (featureAsJson.has(StaticFunctions.URI.toUpperCase())) {
                String queryString = "select DISTINCT ?x where { <" + featureAsJson.get(StaticFunctions.URI.toUpperCase()).asText() + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?x }";
                SparqlQueryExecuter e = new SparqlQueryExecuter();
                ArrayNode result = e.query(queryString);
				String conceptType = "dbpedia";
				//DBpediaToken dbType = new DBpediaToken(featureAsJson.get(StaticFunctions.TOKEN).asText(), conceptType, 1);
				//dbType.save();
                for(int i=0; i<result.size(); i++) {
                    String type = result.get(i).get(StaticFunctions.URI).asText();
                    String typeName = type.substring(type.lastIndexOf('/') + 1).toLowerCase();
                    if(typeName.contains("genre") || typeName.contains("software") || typeName.contains("concept") ||
                            type.toLowerCase().contains("libraries") || type.toLowerCase().contains("algorithm")) {
                        DBpediaToken dbType = new DBpediaToken(featureAsJson.get(StaticFunctions.TOKEN).asText(), conceptType, 1);
                        dbType.save();
                        break;
                    }
                }
               featureAsJson.put(StaticFunctions.CONCEPTTYPE, conceptType);
            }

            savedToken = new DBpediaToken().findByName(featureAsJson.get(StaticFunctions.TOKEN).asText());
            if(savedToken != null && savedToken.getScore() >= 0) {
                annotations.add(featureAsJson);
            }
        }
        return annotations;
    }
}
