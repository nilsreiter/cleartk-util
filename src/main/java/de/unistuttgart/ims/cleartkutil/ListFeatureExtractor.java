package de.unistuttgart.ims.cleartkutil;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;

public class ListFeatureExtractor<T extends Annotation> implements FeatureExtractor1<T> {

	List<String> strList;
	String fName;

	public ListFeatureExtractor(String featureName, List<String> list) {
		strList = list;
		fName = featureName;
	}

	public List<Feature> extract(JCas view, T focusAnnotation) throws CleartkExtractorException {
		if (strList.contains(focusAnnotation.getCoveredText()))
			return Arrays.asList(new Feature(fName));
		else
			return Arrays.asList();
	}

}