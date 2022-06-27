
package application.indexes;

import org.apache.jdbm.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DiskPositionalIndex implements Index<String, Posting>, Closeable {

    private final String pathToBTreeBin;    // the String path to the B+ Tree mappings of terms -> byte positions
    private final BTree<String, Integer> bTree;
    private RandomAccessFile randomAccessPosting;   // keep the Posting file open for getPosting() calls

    public DiskPositionalIndex(BTree<String, Integer> inputBTree, String newPathToBTreeBin, String newPathToPostingsBin) {
        bTree = inputBTree;
        pathToBTreeBin = newPathToBTreeBin;

        try {
            // be able to read from the postings file and extract the index data
            randomAccessPosting = new RandomAccessFile(newPathToPostingsBin, "r");
        } catch (FileNotFoundException e) {
            System.err.println("Index files were not found; please restart the program and build an index.");
            System.exit(0);
        }
    }

    /**
     * Returns a list of postings including positions.
     * @param term the term to find postings for
     * @return the term's list of postings including positions.
     */
    @Override
    public List<Posting> getPostings(String term) {
        List<Posting> resultPostings = new ArrayList<>();

        try {
            // retrieve the byte position value for the term key within the B+ Tree
            int bytePosition = bTree.get(term);
            // jump to the offset containing the term's postings
            randomAccessPosting.seek(bytePosition);
            // the current int value at the offset is the size of the postings list
            int postingsSize = randomAccessPosting.readInt();
            int latestDocumentId = 0;

            // iterate through all postings for the term
            for (int i = 0; i < postingsSize; ++i) {
                ArrayList<Integer> positions = new ArrayList<>();
                // first document ID is as-is; the rest are gaps
                int currentDocumentId = randomAccessPosting.readInt() + latestDocumentId;
                latestDocumentId = currentDocumentId - latestDocumentId;
                int positionsSize = randomAccessPosting.readInt();
                int latestPosition = 0;

                for (int j = 0; j < positionsSize; ++j) {
                    // first position is as-is; the rest are gaps
                    int currentPosition = randomAccessPosting.readInt() + latestPosition;
                    positions.add(currentPosition);
                    latestPosition = currentPosition - latestPosition;
                }
                Posting newPosting = new Posting(currentDocumentId, positions);
                resultPostings.add(newPosting);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // bTree.get(term) returning null means that the term does not exist in the vocabulary
            return new ArrayList<>();
        }

        return resultPostings;
    }

    /**
     * Returns a list of postings excluding positions.
     * @param term the term to find postings for
     * @return the term's list of postings excluding positions.
     */
    @Override
    public List<Posting> getPositionlessPostings(String term) {
        List<Posting> resultPostings = new ArrayList<>();

        try {
            // retrieve the byte position value for the term key within the B+ Tree
            int bytePosition = bTree.get(term);
            // jump to the offset containing the term's postings
            randomAccessPosting.seek(bytePosition);
            // the current int value at the offset is the size of the postings list
            int postingsSize = randomAccessPosting.readInt();
            int latestDocumentId = 0;

            // iterate through all postings for the term
            for (int i = 0; i < postingsSize; ++i) {
                ArrayList<Integer> positions = new ArrayList<>();
                // first document ID is as-is; the rest are gaps
                int currentDocumentId = randomAccessPosting.readInt() + latestDocumentId;
                latestDocumentId = currentDocumentId - latestDocumentId;
                int positionsSize = randomAccessPosting.readInt();
                // skip the other position bytes
                randomAccessPosting.skipBytes(positionsSize * Integer.BYTES);

                // add empty positions
                for (int j = 0; j < positionsSize; ++j) {
                    positions.add(0);
                }

                Posting newPosting = new Posting(currentDocumentId, positions);
                resultPostings.add(newPosting);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // bTree.get(term) returning null means that the term does not exist in the vocabulary
            return new ArrayList<>();
        }

        return resultPostings;
    }

    @Override
    public List<String> getVocabulary() {
        List<String> vocabulary = new ArrayList<>();

        try (FileInputStream fileStream = new FileInputStream(pathToBTreeBin);
             BufferedInputStream bufferStream = new BufferedInputStream(fileStream);
             DataInputStream dataStream = new DataInputStream(bufferStream)) {
            // write the total size of the vocabulary
            int vocabularySize = dataStream.readInt();

            // traverse through the vocabulary terms
            for (int i = 0; i < vocabularySize; ++i) {
                // read each String byte length normally
                int currentBytesLength = dataStream.readInt();
                StringBuilder term = new StringBuilder();

                // convert each byte to a character and build to our original term
                for (int j = 0; j < currentBytesLength; ++j) {
                    term.append((char) dataStream.readByte());
                }

                vocabulary.add(term.toString());

                // after reading the term bytes, skip the next byte position since we only need the vocabulary
                dataStream.skipBytes(4);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // vocabulary should already be sorted
        return vocabulary;
    }

    @Override
    public void close() throws IOException {
        randomAccessPosting.close();
    }
}
