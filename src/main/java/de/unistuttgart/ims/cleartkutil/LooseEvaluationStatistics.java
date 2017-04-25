package de.unistuttgart.ims.cleartkutil;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

public class LooseEvaluationStatistics<ANNOTATION_TYPE extends Annotation, BASE_ANNOTATION extends Annotation, OUTCOME_TYPE> {
	Class<BASE_ANNOTATION> baseAnnotationClass;
	Class<ANNOTATION_TYPE> annotationClass;
	PRStat prStat = new PRStat();

	public LooseEvaluationStatistics() {
	}

	public LooseEvaluationStatistics(Class<ANNOTATION_TYPE> annotationClass,
			Class<BASE_ANNOTATION> baseAnnotationClass) {
		init(annotationClass, baseAnnotationClass);
	}

	public void init(Class<ANNOTATION_TYPE> annotationClass, Class<BASE_ANNOTATION> baseAnnotationClass) {
		this.baseAnnotationClass = baseAnnotationClass;
		this.annotationClass = annotationClass;

	}

	public void add(JCas gold, JCas silver, Collection<? extends ANNOTATION_TYPE> referenceAnnotations,
			Collection<? extends ANNOTATION_TYPE> predictedAnnotations,
			Function<ANNOTATION_TYPE, OUTCOME_TYPE> annotationToOutcome) {
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
			OUTCOME_TYPE cat = annotationToOutcome.apply(e);
			outerloop: for (BASE_ANNOTATION gt : JCasUtil.selectCovered(baseAnnotationClass, e)) {
				HashableSpan gts = new HashableSpan(gt);
				for (ANNOTATION_TYPE e2 : silverIndex.get(sTokenMap.get(gts))) {
					OUTCOME_TYPE cat2 = annotationToOutcome.apply(e2);
					if (Objects.equal(cat, cat2)) {
						isFound = true;
						break outerloop;
					}
				}

			}

			if (isFound) {
				prStat.tp1();
				prStat.tp1(cat);
			} else {
				prStat.fn1();
				prStat.fn1(cat);
				// System.out.println(e.getCoveredText());
			}
		}

		for (ANNOTATION_TYPE e : JCasUtil.select(silver, annotationClass)) {
			boolean isFound = false;
			OUTCOME_TYPE cat = annotationToOutcome.apply(e);

			outerloop: for (BASE_ANNOTATION st : JCasUtil.selectCovered(baseAnnotationClass, e)) {
				HashableSpan sts = new HashableSpan(st);
				if (gTokenMap.containsKey(sts)) {
					for (ANNOTATION_TYPE e2 : goldIndex.get(gTokenMap.get(sts))) {
						if (Objects.equal(cat, annotationToOutcome.apply(e2))) {
							isFound = true;
							break outerloop;
						}
					}
				}
			}

			if (!isFound) {
				prStat.fp1();
				prStat.fp1(cat);
			}
		}
	}

	public void add(LooseEvaluationStatistics<ANNOTATION_TYPE, BASE_ANNOTATION, OUTCOME_TYPE> other) {
		prStat.tp(other.tp());
		prStat.fp(other.fp());
		prStat.fn(other.fn());
		for (OUTCOME_TYPE ot : other.getPrStat().getClasses()) {
			prStat.tp(ot, other.getPrStat().tp(ot));
			prStat.fp(ot, other.getPrStat().fp(ot));
			prStat.fn(ot, other.getPrStat().fn(ot));
		}
	}

	public static <ANNOTATION_TYPE extends Annotation, BASE_ANNOTATION_TYPE extends Annotation, OUTCOME_TYPE> LooseEvaluationStatistics<ANNOTATION_TYPE, BASE_ANNOTATION_TYPE, OUTCOME_TYPE> addAll(
			Iterable<LooseEvaluationStatistics<ANNOTATION_TYPE, BASE_ANNOTATION_TYPE, OUTCOME_TYPE>> other) {
		LooseEvaluationStatistics<ANNOTATION_TYPE, BASE_ANNOTATION_TYPE, OUTCOME_TYPE> ret = new LooseEvaluationStatistics<ANNOTATION_TYPE, BASE_ANNOTATION_TYPE, OUTCOME_TYPE>();
		for (LooseEvaluationStatistics<ANNOTATION_TYPE, BASE_ANNOTATION_TYPE, OUTCOME_TYPE> s : other) {
			ret.getPrStat().tp(s.getPrStat().tp());
			ret.getPrStat().fp(s.getPrStat().fp());
			ret.getPrStat().fn(s.getPrStat().fn());
			for (OUTCOME_TYPE ot : s.getPrStat().getClasses()) {
				ret.getPrStat().tp(ot, s.getPrStat().tp(ot));
				ret.getPrStat().fp(ot, s.getPrStat().fp(ot));
				ret.getPrStat().fn(ot, s.getPrStat().fn(ot));
			}
		}
		return ret;
	}

	@Override
	public String toString() {
		return prStat.toString();
	}

	public class PRStat {
		Bag<OUTCOME_TYPE> truePositives = new HashBag<OUTCOME_TYPE>();
		Bag<OUTCOME_TYPE> falsePositives = new HashBag<OUTCOME_TYPE>();
		Bag<OUTCOME_TYPE> falseNegatives = new HashBag<OUTCOME_TYPE>();
		Set<OUTCOME_TYPE> classes = new HashSet<OUTCOME_TYPE>();
		int tp = 0;
		int fp = 0;
		int fn = 0;

		public double precision(OUTCOME_TYPE ot) {
			return truePositives.getCount(ot) / ((double) truePositives.getCount(ot) + falsePositives.getCount(ot));
		}

		public int tp(OUTCOME_TYPE ot) {
			return truePositives.getCount(ot);
		}

		public double precision() {
			return tp / ((double) tp + fp);
		}

		public double recall(OUTCOME_TYPE ot) {
			return truePositives.getCount(ot) / ((double) truePositives.getCount(ot) + falseNegatives.getCount(ot));
		}

		public double recall() {
			return tp / ((double) tp + fn);
		}

		public void tp1() {
			tp++;
		}

		public void tp1(OUTCOME_TYPE ot) {
			truePositives.add(ot);
			classes.add(ot);
		}

		public void tp(OUTCOME_TYPE ot, int t) {
			truePositives.add(ot, t);
			classes.add(ot);
		}

		private void tp(int t) {
			tp += t;
		}

		private void fp(int t) {
			fp += t;
		}

		public int fp(OUTCOME_TYPE ot) {
			return falsePositives.getCount(ot);
		}

		public int fn(OUTCOME_TYPE ot) {
			return falseNegatives.getCount(ot);
		}

		public void fp(OUTCOME_TYPE ot, int t) {
			falsePositives.add(ot, t);
			classes.add(ot);
		}

		private void fn(int t) {
			fn += t;
		}

		public void fn(OUTCOME_TYPE ot, int t) {
			falseNegatives.add(ot, t);
			classes.add(ot);
		}

		public void fp1() {
			fp++;
		}

		public void fp1(OUTCOME_TYPE ot) {
			falsePositives.add(ot);
			classes.add(ot);

		}

		public void fn1() {
			fn++;
		}

		public void fn1(OUTCOME_TYPE ot) {
			falseNegatives.add(ot);
			classes.add(ot);

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

		public double f1() {
			return 0.0;
		}

		public double f1(OUTCOME_TYPE ot) {
			return 0.0;
		}

		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			result.append("P\tR\tF1\n");
			result.append(String.format("%.3f\t%.3f\t%.3f\tOVERALL\n", this.precision(), this.recall(), this.f1()));
			for (OUTCOME_TYPE ot : classes) {
				result.append(
						String.format("%.3f\t%.3f\t%.3f\t%s\n", this.precision(ot), this.recall(ot), this.f1(ot), ot));

			}
			return result.toString();
		}

		public Set<OUTCOME_TYPE> getClasses() {
			return classes;
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

	public PRStat getPrStat() {
		return prStat;
	}
}
