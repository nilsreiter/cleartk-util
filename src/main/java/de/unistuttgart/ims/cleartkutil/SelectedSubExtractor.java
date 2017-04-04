package de.unistuttgart.ims.cleartkutil;

import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;

public abstract class SelectedSubExtractor<M extends Annotation, SUB extends Annotation>
		implements FeatureExtractor1<M> {

	FeatureExtractor1<SUB> subExtractor;

	public SelectedSubExtractor(FeatureExtractor1<SUB> subExtractor) {
		this.subExtractor = subExtractor;
	}

	public List<Feature> extract(JCas view, M focusAnnotation) throws CleartkExtractorException {
		return subExtractor.extract(view, selectSubAnnotation(focusAnnotation));
	}

	public abstract SUB selectSubAnnotation(M mainAnnotation);

}
