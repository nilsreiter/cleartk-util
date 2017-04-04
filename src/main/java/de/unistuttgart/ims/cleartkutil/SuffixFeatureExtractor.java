package de.unistuttgart.ims.cleartkutil;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;

public class SuffixFeatureExtractor<T extends Annotation> implements FeatureExtractor1<T> {

	String suf = "in";

	public SuffixFeatureExtractor(String suffix) {
		suf = suffix;
	}

	public List<Feature> extract(JCas view, T focusAnnotation) throws CleartkExtractorException {
		String surf = focusAnnotation.getCoveredText();
		boolean b = Character.isUpperCase(surf.charAt(0)) && surf.endsWith(suf);
		return Arrays.asList(new Feature("Suffix_" + suf, String.valueOf(b)));
	}
}