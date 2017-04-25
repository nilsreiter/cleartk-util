package de.unistuttgart.ims.cleartkutil;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

public class StatisticsExport<SEGMENT_TYPE extends Annotation, BASE_ANNOTATION extends Annotation> {
	Class<BASE_ANNOTATION> baseAnnotationClass;
	Class<SEGMENT_TYPE> annotationClass;

	public StatisticsExport() {
	}

	public StatisticsExport(Class<SEGMENT_TYPE> annotationClass, Class<BASE_ANNOTATION> baseAnnotationClass) {
		init(annotationClass, baseAnnotationClass);
	}

	public void init(Class<SEGMENT_TYPE> annotationClass, Class<BASE_ANNOTATION> baseAnnotationClass) {
		this.baseAnnotationClass = baseAnnotationClass;
		this.annotationClass = annotationClass;

	}

	@SafeVarargs
	public void add(JCas gold, JCas silver, Appendable appendable, String did, boolean printHeader,
			Class<? extends Annotation>... classes) throws IOException {
		Map<BASE_ANNOTATION, Collection<SEGMENT_TYPE>> goldIndex = JCasUtil.indexCovering(gold, baseAnnotationClass,
				annotationClass);
		Map<BASE_ANNOTATION, Collection<SEGMENT_TYPE>> silverIndex = JCasUtil.indexCovering(silver, baseAnnotationClass,
				annotationClass);

		Map<HashableSpan, BASE_ANNOTATION> gTokenMap = new HashMap<HashableSpan, BASE_ANNOTATION>();
		Map<HashableSpan, BASE_ANNOTATION> sTokenMap = new HashMap<HashableSpan, BASE_ANNOTATION>();

		for (BASE_ANNOTATION t : JCasUtil.select(gold, baseAnnotationClass))
			gTokenMap.put(new HashableSpan(t), t);
		for (BASE_ANNOTATION t : JCasUtil.select(silver, baseAnnotationClass))
			sTokenMap.put(new HashableSpan(t), t);

		List<String> columns = new LinkedList<String>();
		columns.add("document");
		columns.add("id");
		columns.add("surface");
		columns.add("begin");
		columns.add("end");
		for (Class<?> c : classes) {
			columns.add(c.getSimpleName());
		}
		columns.add("gold");
		columns.add("silver");

		CSVPrinter p;
		if (printHeader)
			p = new CSVPrinter(appendable, CSVFormat.DEFAULT.withHeader(columns.toArray(new String[columns.size()])));
		else
			p = new CSVPrinter(appendable, CSVFormat.DEFAULT);

		int i = 0;
		for (BASE_ANNOTATION a : JCasUtil.select(gold, baseAnnotationClass)) {
			p.print(did);
			p.print(i++);
			// p.print(a.getCoveredText().substring(0,
			// Math.min(a.getCoveredText().length(),
			// 20)).replaceAll("\\p{Space}",
			// " "));
			p.print(a.getBegin());
			p.print(a.getEnd());

			BASE_ANNOTATION silverToken = sTokenMap.get(new HashableSpan(a));
			for (Class<? extends Annotation> c : classes) {
				p.print(JCasUtil.selectCovered(c, silverToken).size());
			}

			p.print((goldIndex.get(a).isEmpty() ? "False" : "True"));

			p.print((silverIndex.get(silverToken).isEmpty() ? "False" : "True"));
			p.println();
		}

		p.flush();
		IOUtils.closeQuietly(p);

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

}
