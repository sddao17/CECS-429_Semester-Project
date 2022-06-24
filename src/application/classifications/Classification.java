
package application.classifications;

import java.util.List;
import java.util.Map;

public interface Classification {

    Map.Entry<String, Double> classifyDocument(String directoryPath, int documentId);
    List<String> getVocabulary(String directoryPath);
    List<Double> getVector(String directoryPath, int documentId);
}
