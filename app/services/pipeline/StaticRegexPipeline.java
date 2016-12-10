package services.pipeline;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import play.libs.Json;
import services.annotator.ConceptAnnotator;
import services.annotator.StaticRegexAnnotator;
import util.PipelineUtil;
import util.StaticFunctions;
import util.UimaUtil;

import java.io.IOException;
import java.util.List;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

/**
 * Created by mahabaleshwar on 11/2/2016.
 */
public class StaticRegexPipeline extends Pipeline {
    private String projectId;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @Override
    public String preProcessDocument() {
        return null;
    }

    @Override
    public ArrayNode processDocument() {
        ArrayNode annotations = Json.newArray();
        try {
            AnalysisEngineDescription conceptAnnotatorDesc = createEngineDescription(ConceptAnnotator.class);
            AnalysisEngineDescription regexDesc = createEngineDescription(StaticRegexAnnotator.class);
            // aggregate AE for basic pipe
            AnalysisEngineDescription basicPipeDesc = createEngineDescription(regexDesc, conceptAnnotatorDesc);
            AnalysisEngine pipe = createEngine(basicPipeDesc);

            JCas jCas = UimaUtil.produceJCas(StaticFunctions.CONCEPT, StaticFunctions.REGEX);
            jCas.setDocumentText(this.getDocument().getRawContent() + ":projectId:" + this.getProjectId());
            jCas.setDocumentLanguage(this.getDocument().getLanguage());

            pipe.process(jCas);
            annotations = getAnnotations(jCas);

            jCas.reset();
            pipe.destroy();
        } catch (ResourceInitializationException e) {
            e.printStackTrace();
        } catch (AnalysisEngineProcessException e) {
            e.printStackTrace();
        } catch (InvalidXMLException e) {
            e.printStackTrace();
        } catch (CASException e) {
            e.printStackTrace();
        } catch (IOException e) {
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

            System.out.println(Integer.parseInt(featureAsJson.get(StaticFunctions.BEGIN).asText()));
            if(isWaitConditionSatisfied(featureAsJson.get(StaticFunctions.NAME).asText(), jCas.getDocumentText().substring(0,
                            Integer.parseInt(featureAsJson.get(StaticFunctions.BEGIN).asText())))) {
                annotations.add(featureAsJson);
            }
        }
        return annotations;
    }

    private boolean isWaitConditionSatisfied(String name, String subString) {
        String[] lines = subString.split("\n");
        if(lines.length > 0) {
            String line = lines[lines.length - 1];
            return !((line.contains("//") || line.contains("*") || line.contains("' ")) && line.contains("wait") && name.contains("wait"));
        }
        return true;
    }
}
