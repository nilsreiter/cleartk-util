package de.unistuttgart.ims.cleartkutil;

import java.util.LinkedList;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;

public class SubtypeFeatureExtractor<T extends Annotation> implements FeatureExtractor1<T> {
	Class<? extends Annotation> goalClass;

	public SubtypeFeatureExtractor(Class<? extends Annotation> goalClass) {
		super();
		this.goalClass = goalClass;
	}

	public List<Feature> extract(JCas view, T focusAnnotation) throws CleartkExtractorException {
		List<Feature> flist = new LinkedList<Feature>();
		for (Annotation anno : JCasUtil.select(view, goalClass)) {
			Feature f = new Feature();
			f.setName("subtype");
			f.setValue(anno.getType().getShortName());
			flist.add(f);
		}
		return flist;
	}

}
