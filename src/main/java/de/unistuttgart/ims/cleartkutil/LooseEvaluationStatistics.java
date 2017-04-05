package de.unistuttgart.ims.cleartkutil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

public class LooseEvaluationStatistics<ANNOTATION_TYPE extends Annotation, BASE_ANNOTATION extends Annotation> {
	Class<BASE_ANNOTATION> baseAnnotationClass;
	Class<ANNOTATION_TYPE> annotationClass;
	PRStat prStat = new PRStat();

	public LooseEvaluationStatistics(Class<ANNOTATION_TYPE> annotationClass,
			Class<BASE_ANNOTATION> baseAnnotationClass) {
		this.baseAnnotationClass = baseAnnotationClass;
		this.annotationClass = annotationClass;
	}

	public void add(JCas gold, JCas silver, Collection<? extends ANNOTATION_TYPE> referenceAnnotations,
			Collection<? extends ANNOTATION_TYPE> predictedAnnotations) {
		Map<BASE_ANNOTATION, Collection<ANNOTATION_TYPE>> goldIndex = JCasUtil.indexCovering(gold, baseAnnotationClass,
				annotationClass);
		Map<BASE_ANNOTATION, Collection<ANNOTATION_TYPE>> silverIndex = JCasUtil.indexCovering(silver,
				baseAnnotationClass, annotationClass);

		Map<HashableSpan, BASE_ANNOTATION> gTokenMap = new HashMap<HashableSpan, BASE_ANNOTATION>();
		Map<HashableSpan, BASE_ANNOTATION> sTokenMap = new HashMap<HashableSpan, BASE_ANNOTATION>();

		for (BASE_ANNOTATION t : JCasUtil.select(gold, baseAnnotationClass))
			gTokenMap.put(new HashableSpan(t), t);
		for (BASE_ANNOTATION t : JCasUtil.select(silver, baseAnnotationClass))
			sTokenMap.put(new HashableSpan(t), t);

		for (ANNOTATION_TYPE e : JCasUtil.select(gold, annotationClass)) {
			boolean isFound = false;
			for (BASE_ANNOTATION gt : JCasUtil.selectCovered(baseAnnotationClass, e)) {
				HashableSpan gts = new HashableSpan(gt);
				if (sTokenMap.containsKey(gts)) {
					if (!silverIndex.get(sTokenMap.get(gts)).isEmpty()) {
						isFound = true;
						break;
					}
				}
			}

			if (isFound) {
				prStat.tp1();
			} else {
				prStat.fn1();
				System.out.println(e.getCoveredText());
			}
		}

		for (ANNOTATION_TYPE e : JCasUtil.select(silver, annotationClass)) {
			boolean isFound = false;
			for (BASE_ANNOTATION st : JCasUtil.selectCovered(baseAnnotationClass, e)) {
				HashableSpan sts = new HashableSpan(st);
				if (gTokenMap.containsKey(sts)) {
					if (!goldIndex.get(gTokenMap.get(sts)).isEmpty()) {
						isFound = true;
						break;
					}
				}
			}

			if (isFound) {
			} else {
				prStat.fp1();
			}
		}
	}

	class PRStat {
		int tp = 0;
		int fp = 0;
		int fn = 0;

		public double precision() {
			return tp / ((double) tp + fp);
		}

		public double recall() {
			return tp / ((double) tp + fn);
		}

		public void tp1() {
			tp++;
		}

		public void fp1() {
			fp++;
		}

		public void fn1() {
			fn++;
		}

		public int tp() {
			return tp;
		}

		public int fp() {
			return fp;
		}

		public int fn() {
			return fn;
		}

	}

	static class HashableSpan {
		public int end;

		public int begin;

		public HashableSpan(Annotation annotation) {
			this.begin = annotation.getBegin();
			this.end = annotation.getEnd();
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(this.begin, this.end);
		}

		@Override
		public boolean equals(Object obj) {
			if (!this.getClass().equals(obj.getClass())) {
				return false;
			}
			HashableSpan that = (HashableSpan) obj;
			return this.begin == that.begin && this.end == that.end;
		}

		@Override
		public String toString() {
			ToStringHelper helper = Objects.toStringHelper(this);
			helper.add("begin", this.begin);
			helper.add("end", this.end);
			return helper.toString();
		}
	}

	public double precision() {
		return prStat.precision();
	}

	public double recall() {
		return prStat.recall();
	}

	public int tp() {
		return prStat.tp();
	}

	public int fp() {
		return prStat.fp();
	}

	public int fn() {
		return prStat.fn();
	}
}
