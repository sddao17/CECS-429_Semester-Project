
package application.utilities;

public enum PathSuffix {
    INDEX_DIRECTORY("/index"),
    POSTINGS_FILE("/postings.bin"),
    BTREE_FILE("/bTree.bin"),
    KGRAMS_FILE("/kGrams.bin"),
    DOC_WEIGHTS_FILE("/docWeights.bin");

    private String label;

    PathSuffix(String inputLabel) {
        label = inputLabel;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String newLabel) {
        label = newLabel;
    }
}