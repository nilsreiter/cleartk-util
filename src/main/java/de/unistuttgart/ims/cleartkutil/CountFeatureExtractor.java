package de.unistuttgart.ims.cleartkutil;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;

public class CountFeatureExtractor<T extends Annotation> implements FeatureExtractor1<T> {

	Class<? extends Annotation> goalClass;

	public CountFeatureExtractor(Class<? extends Annotation> goalClass) {
		super();
		this.goalClass = goalClass;
	}

	public List<Feature> extract(JCas view, T focusAnnotation) throws CleartkExtractorException {
		Feature f = new Feature();
		int c = JCasUtil.selectCovered(goalClass, focusAnnotation).size();
		f.setValue(c);
		f.setName("count-" + goalClass.getSimpleName() + "-" + c);
		return Arrays.asList(f);
	}

}
