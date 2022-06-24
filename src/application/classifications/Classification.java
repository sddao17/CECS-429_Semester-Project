
package application.classifications;

import java.util.List;

public interface Classification {

    String classifyDocument(String directoryPath, int documentId);
    List<Double> getVector(String directoryPath, int documentId);
}
