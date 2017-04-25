package de.unistuttgart.ims.cleartkutil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.AnnotationFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.token.type.Token;
import org.junit.Test;

public class TestCreateGoldView {
	private static final String GOLD_VIEW = "gold";
	JCas jcas;

	@Test
	public void testBasics() throws UIMAException {
		jcas = JCasFactory.createJCas();
		jcas.setDocumentText("abcdefgh");

		AnnotationFactory.createAnnotation(jcas, 0, 1, Token.class).setLemma("first token");
		AnnotationFactory.createAnnotation(jcas, 1, 2, Token.class).setPos("a pos");
		AnnotationFactory.createAnnotation(jcas, 2, 3, Token.class).setScore(1.0);

		SimplePipeline.runPipeline(jcas,
				AnalysisEngineFactory.createEngine(CreateGoldView.class, CreateGoldView.PARAM_GOLD_ANNOTATION_TYPE,
						Token.class, CreateGoldView.PARAM_GOLD_VIEW_NAME, GOLD_VIEW));

		JCas other = jcas.getView(GOLD_VIEW);
		assertNotNull(jcas);
		assertNotNull(other);
		assertFalse(JCasUtil.exists(jcas, Token.class));
		assertTrue(JCasUtil.exists(other, Token.class));

		Token token;
		token = JCasUtil.selectByIndex(other, Token.class, 0);
		assertNotNull(token);
		assertEquals(0, token.getBegin());
		assertEquals(1, token.getEnd());
		assertEquals("first token", token.getLemma());
		assertNull(token.getPos());

		token = JCasUtil.selectByIndex(other, Token.class, 1);
		assertNotNull(token);
		assertEquals(1, token.getBegin());
		assertEquals(2, token.getEnd());
		assertEquals("a pos", token.getPos());
		assertNull(token.getLemma());

		token = JCasUtil.selectByIndex(other, Token.class, 2);
		assertNotNull(token);
		assertEquals(1.0, token.getScore(), 1e-3);
		assertNull(token.getPos());
	}
}
