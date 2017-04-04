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
		boolean fValue = false;
		if (strList.contains(focusAnnotation.getCoveredText()))
			fValue = true;
		Feature f = new Feature(fName, String.valueOf(fValue));
		return Arrays.asList(f);
	}

}