package util;

import org.apache.uima.UIMAFramework;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;
import org.apache.uima.util.XMLParser;
import play.Play;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Tobias Waltl on 05.10.2015.
 */
public class UimaUtil {

    private static final String CONCEPT_TYPESYSTEM_PATH = "app" + File.separator + "resources" + File.separator + "Concept.xml";
    private static final String SPOTLIGHT_TYPESYSTEM_PATH = "app" + File.separator + "resources" + File.separator + "SpotlightTypeSystemDescriptor.xml";

    public static JCas produceJCas() throws IOException, InvalidXMLException, ResourceInitializationException, CASException {
        Set<TypeSystemDescription> typeSystemDescs = new HashSet<TypeSystemDescription>();

        XMLParser xmlParser = UIMAFramework.getXMLParser();
        TypeSystemDescription aeDesc = xmlParser.parseTypeSystemDescription(new XMLInputSource(Play.application().getFile(CONCEPT_TYPESYSTEM_PATH).getAbsolutePath()));
        TypeSystemDescription saeDesc = xmlParser.parseTypeSystemDescription(new XMLInputSource(Play.application().getFile(SPOTLIGHT_TYPESYSTEM_PATH).getAbsolutePath()));
        typeSystemDescs.add(aeDesc);
        typeSystemDescs.add(saeDesc);


        TypeSystemDescription tsDesc = CasCreationUtils.mergeTypeSystems(typeSystemDescs);

        return CasCreationUtils.createCas(tsDesc, null, null).getJCas();
    }
}
