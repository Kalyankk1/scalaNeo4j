package Saddahaq;
import java.io.File;
import java.io.IOException;

import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.LMClassifier;
import com.aliasi.util.AbstractExternalizable;

public class SentimentClassifier {

	String[] categories;
	LMClassifier cl;

	public SentimentClassifier() {
	
	try {
		cl= (LMClassifier) AbstractExternalizable.readObject(new File("/var/classifier.txt"));
		categories = cl.categories();
	}
	catch (ClassNotFoundException e) {
		e.printStackTrace();
	}
	catch (IOException e) {
		e.printStackTrace();
	}

	}

	public String classify(String text) {
	ConditionalClassification classification = cl.classify(text);
	return classification.bestCategory();
	}
	
}