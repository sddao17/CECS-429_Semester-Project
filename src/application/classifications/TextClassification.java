
package application.classifications;

import java.util.List;
import java.util.Map;

public interface TextClassification {

    /**
     * Classifies a document according to its classification method.
     * @param directoryPath the path of the directory to the document
     * @param documentId the document ID of the document
     * @return the classification of the document in the form of <code>(classification, distance)<code/>
     */
    Map.Entry<String, Double> classifyDocument(String directoryPath, int documentId);

    /**
     * Returns the list of vocabulary terms of the specified directory.
     * @param directoryPath the path of the directory
     * @return the list of vocabulary terms of the directory
     */
    List<String> getVocabulary(String directoryPath);
}
