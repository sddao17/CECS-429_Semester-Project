package application.classifications;

import java.util.List;
import java.util.Map;

public class KnnClassification implements Classification {
    @Override
    public Map.Entry<String, Double> classifyDocument(String directoryPath, int documentId) {
        return null;
    }

    @Override
    public List<String> getVocabulary(String directoryPath) {
        return null;
    }

    @Override
    public List<Double> getVector(String directoryPath, int documentId) {
        return null;
    }
}
