
/* First created by JCasGen Wed Sep 14 16:15:24 CEST 2016 */
package services.annotationType.concept;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Wed Sep 14 16:15:24 CEST 2016
 * @generated */
public class ConceptAnnotatorResource_Type extends Annotation_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (ConceptAnnotatorResource_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = ConceptAnnotatorResource_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new ConceptAnnotatorResource(addr, ConceptAnnotatorResource_Type.this);
  			   ConceptAnnotatorResource_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new ConceptAnnotatorResource(addr, ConceptAnnotatorResource_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = ConceptAnnotatorResource.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("services.annotationType.concept.ConceptAnnotatorResource");
 
  /** @generated */
  final Feature casFeat_name;
  /** @generated */
  final int     casFeatCode_name;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getName(int addr) {
        if (featOkTst && casFeat_name == null)
      jcas.throwFeatMissing("name", "services.annotationType.concept.ConceptAnnotatorResource");
    return ll_cas.ll_getStringValue(addr, casFeatCode_name);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setName(int addr, String v) {
        if (featOkTst && casFeat_name == null)
      jcas.throwFeatMissing("name", "services.annotationType.concept.ConceptAnnotatorResource");
    ll_cas.ll_setStringValue(addr, casFeatCode_name, v);}
    
  
 
  /** @generated */
  final Feature casFeat_conceptType;
  /** @generated */
  final int     casFeatCode_conceptType;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getConceptType(int addr) {
        if (featOkTst && casFeat_conceptType == null)
      jcas.throwFeatMissing("conceptType", "services.annotationType.concept.ConceptAnnotatorResource");
    return ll_cas.ll_getStringValue(addr, casFeatCode_conceptType);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setConceptType(int addr, String v) {
        if (featOkTst && casFeat_conceptType == null)
      jcas.throwFeatMissing("conceptType", "services.annotationType.concept.ConceptAnnotatorResource");
    ll_cas.ll_setStringValue(addr, casFeatCode_conceptType, v);}
    
  
 
  /** @generated */
  final Feature casFeat_similarityScore;
  /** @generated */
  final int     casFeatCode_similarityScore;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public double getSimilarityScore(int addr) {
        if (featOkTst && casFeat_similarityScore == null)
      jcas.throwFeatMissing("similarityScore", "services.annotationType.concept.ConceptAnnotatorResource");
    return ll_cas.ll_getDoubleValue(addr, casFeatCode_similarityScore);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSimilarityScore(int addr, double v) {
        if (featOkTst && casFeat_similarityScore == null)
      jcas.throwFeatMissing("similarityScore", "services.annotationType.concept.ConceptAnnotatorResource");
    ll_cas.ll_setDoubleValue(addr, casFeatCode_similarityScore, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public ConceptAnnotatorResource_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_name = jcas.getRequiredFeatureDE(casType, "name", "uima.cas.String", featOkTst);
    casFeatCode_name  = (null == casFeat_name) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_name).getCode();

 
    casFeat_conceptType = jcas.getRequiredFeatureDE(casType, "conceptType", "uima.cas.String", featOkTst);
    casFeatCode_conceptType  = (null == casFeat_conceptType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_conceptType).getCode();

 
    casFeat_similarityScore = jcas.getRequiredFeatureDE(casType, "similarityScore", "uima.cas.Double", featOkTst);
    casFeatCode_similarityScore  = (null == casFeat_similarityScore) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_similarityScore).getCode();

  }
}



    