package de.unistuttgart.ims.cleartkutil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.xml.sax.SAXException;

public class FileListCollectionReader extends JCasCollectionReader_ImplBase {

	public static final String PARAM_URLLIST = "List of URLs";
	public static final String PARAM_LENIENT = "Lenient";
	public static final String PARAM_SEPARATOR = "Separator";

	@ConfigurationParameter(name = PARAM_URLLIST)
	String urlListString;

	@ConfigurationParameter(name = PARAM_LENIENT, mandatory = false, defaultValue = "false")
	boolean lenient = false;

	@ConfigurationParameter(name = PARAM_SEPARATOR, defaultValue = ";")
	String separator = ";";

	List<URL> urlList;

	int currentUrl = 0;

	@Override
	public void initialize(final UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		String[] urlString = urlListString.split(separator);
		urlList = new ArrayList<URL>(urlString.length);
		for (int i = 0; i < urlString.length; i++) {
			try {
				urlList.add(new URL(urlString[i]));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean hasNext() throws IOException, CollectionException {
		return currentUrl < urlList.size();
	}

	public Progress[] getProgress() {
		return null;
	}

	@Override
	public void getNext(JCas jcas) throws IOException, CollectionException {
		URL url = urlList.get(currentUrl);
		try {
			XmiCasDeserializer.deserialize(url.openStream(), jcas.getCas(), lenient);
		} catch (SAXException e) {
			throw new CollectionException(e);
		}

		currentUrl++;
	}

	public static CollectionReaderDescription getCollectionReaderDescription(String fileList)
			throws ResourceInitializationException {
		return CollectionReaderFactory.createReaderDescription(FileListCollectionReader.class,
				FileListCollectionReader.PARAM_URLLIST, fileList);
	}

}
