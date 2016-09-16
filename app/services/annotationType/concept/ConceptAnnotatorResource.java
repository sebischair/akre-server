

/* First created by JCasGen Wed Sep 14 16:15:24 CEST 2016 */
package services.annotationType.concept;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Wed Sep 14 16:15:24 CEST 2016
 * XML source: C:/SEBIS/projects/uima-workspace/ConceptAnnotator/desc/conceptAnnotatorResource.xml
 * @generated */
public class ConceptAnnotatorResource extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(ConceptAnnotatorResource.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected ConceptAnnotatorResource() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public ConceptAnnotatorResource(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public ConceptAnnotatorResource(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public ConceptAnnotatorResource(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: name

  /** getter for name - gets 
   * @generated
   * @return value of the feature 
   */
  public String getName() {
    if (ConceptAnnotatorResource_Type.featOkTst && ((ConceptAnnotatorResource_Type)jcasType).casFeat_name == null)
      jcasType.jcas.throwFeatMissing("name", "services.annotationType.concept.ConceptAnnotatorResource");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ConceptAnnotatorResource_Type)jcasType).casFeatCode_name);}
    
  /** setter for name - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setName(String v) {
    if (ConceptAnnotatorResource_Type.featOkTst && ((ConceptAnnotatorResource_Type)jcasType).casFeat_name == null)
      jcasType.jcas.throwFeatMissing("name", "services.annotationType.concept.ConceptAnnotatorResource");
    jcasType.ll_cas.ll_setStringValue(addr, ((ConceptAnnotatorResource_Type)jcasType).casFeatCode_name, v);}    
   
    
  //*--------------*
  //* Feature: conceptType

  /** getter for conceptType - gets 
   * @generated
   * @return value of the feature 
   */
  public String getConceptType() {
    if (ConceptAnnotatorResource_Type.featOkTst && ((ConceptAnnotatorResource_Type)jcasType).casFeat_conceptType == null)
      jcasType.jcas.throwFeatMissing("conceptType", "services.annotationType.concept.ConceptAnnotatorResource");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ConceptAnnotatorResource_Type)jcasType).casFeatCode_conceptType);}
    
  /** setter for conceptType - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setConceptType(String v) {
    if (ConceptAnnotatorResource_Type.featOkTst && ((ConceptAnnotatorResource_Type)jcasType).casFeat_conceptType == null)
      jcasType.jcas.throwFeatMissing("conceptType", "services.annotationType.concept.ConceptAnnotatorResource");
    jcasType.ll_cas.ll_setStringValue(addr, ((ConceptAnnotatorResource_Type)jcasType).casFeatCode_conceptType, v);}    
   
    
  //*--------------*
  //* Feature: similarityScore

  /** getter for similarityScore - gets 
   * @generated
   * @return value of the feature 
   */
  public double getSimilarityScore() {
    if (ConceptAnnotatorResource_Type.featOkTst && ((ConceptAnnotatorResource_Type)jcasType).casFeat_similarityScore == null)
      jcasType.jcas.throwFeatMissing("similarityScore", "services.annotationType.concept.ConceptAnnotatorResource");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((ConceptAnnotatorResource_Type)jcasType).casFeatCode_similarityScore);}
    
  /** setter for similarityScore - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSimilarityScore(double v) {
    if (ConceptAnnotatorResource_Type.featOkTst && ((ConceptAnnotatorResource_Type)jcasType).casFeat_similarityScore == null)
      jcasType.jcas.throwFeatMissing("similarityScore", "services.annotationType.concept.ConceptAnnotatorResource");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((ConceptAnnotatorResource_Type)jcasType).casFeatCode_similarityScore, v);}    
  }

    