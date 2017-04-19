package de.unistuttgart.ims.cleartkutil;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.Feature;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnnotationFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

public class CreateGoldView extends JCasAnnotator_ImplBase {

	public static final String PARAM_GOLD_VIEW_NAME = "Gold View Name";
	public static final String PARAM_GOLD_ANNOTATION_TYPE = "Annotation Type";

	@ConfigurationParameter(name = PARAM_GOLD_VIEW_NAME)
	String goldViewName;

	@ConfigurationParameter(name = PARAM_GOLD_ANNOTATION_TYPE)
	String annotationTypeName;

	Class<? extends Annotation> annotationType;

	@SuppressWarnings("unchecked")
	@Override
	public void initialize(final UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		try {
			annotationType = (Class<? extends Annotation>) Class.forName(annotationTypeName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		JCas goldView = null;
		try {
			goldView = jcas.createView(goldViewName);
		} catch (CASException e) {
			throw new AnalysisEngineProcessException(e);
		}
		goldView.setDocumentText(jcas.getDocumentText());

		Set<Annotation> toRemove = new HashSet<Annotation>();
		for (Annotation a : JCasUtil.select(jcas, annotationType)) {
			System.err.println(a);
			try {
				Annotation newAnnotation = AnnotationFactory.createAnnotation(goldView, a.getBegin(), a.getEnd(),
						a.getClass());
				for (Feature f : a.getType().getFeatures()) {
					String rangeName = f.getRange().getName();
					if (rangeName.equals("uima.cas.String")) {
						newAnnotation.setStringValue(f, a.getStringValue(f));
					} else if (rangeName.equals("uima.cas.Integer")) {
						newAnnotation.setIntValue(f, a.getIntValue(f));
					} else if (rangeName.equals("uima.cas.Double")) {
						newAnnotation.setDoubleValue(f, a.getDoubleValue(f));
					}
				}
				System.err.println(newAnnotation);

			} catch (UIMAException e) {
				e.printStackTrace();
			}
			toRemove.add(a);

		}
		for (Annotation a : toRemove) {
			a.removeFromIndexes();
		}

	}

}
