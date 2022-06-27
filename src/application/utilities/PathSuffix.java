
package application.utilities;

public enum PathSuffix {
    INDEX("/index"),
    POSTINGS_FILE("/postings.bin"),
    DOC_WEIGHTS_FILE("/docWeights.bin"),
    BTREE_FILE("/bTree.bin"),
    KGRAMS_FILE("/kGrams.bin"),
    BIWORD_FILE("/biwordBin.bin"),
    BIWORD_BTREE("/biwordBTree.bin");

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