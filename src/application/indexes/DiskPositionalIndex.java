
package application.indexes;

import org.apache.jdbm.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DiskPositionalIndex implements Index<String, Posting>, Closeable {

    private final DB database;
    private BTree<String, Integer> bTree;
    private final String pathToVocabTableBin;   // the path to the B+ Tree mappings of terms -> byte positions
    private final String pathToPostingsBin;   // the path to the `postings.bin` files
    private RandomAccessFile randomAccessVocabTable;    // keep the file open until the program ends, then close it
    private RandomAccessFile randomAccessPosting;    // keep the file open until the program ends, then close it

    public DiskPositionalIndex(String newDirectoryPath) {
        // the directory of the indexes on disk
        pathToVocabTableBin = newDirectoryPath + "/vocabTable.bin";
        pathToPostingsBin = newDirectoryPath + "/postings.bin";
        database = DBMaker.openFile(newDirectoryPath + "/db").deleteFilesAfterClose().closeOnExit().make();

        try {
            bTree = BTree.createInstance((DBAbstract) database);
            // read from the `vocabTable.bin` file and extract the postings from each term
            randomAccessVocabTable = new RandomAccessFile(pathToVocabTableBin, "rw");
            randomAccessPosting = new RandomAccessFile(pathToPostingsBin, "r");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void writeBTreeToDisk(List<String> vocabulary, List<Integer> bytePositions) {
        try {
            // write to a `vocabTable.bin` file and store the byte positions for each term
            randomAccessVocabTable = new RandomAccessFile(pathToVocabTableBin, "rw");
            // write the size of the vocabulary as the first 4 bytes
            randomAccessVocabTable.writeInt(vocabulary.size());

            for (int i = 0; i < vocabulary.size(); ++i) {
                String currentTerm = vocabulary.get(i);
                byte[] bytes = currentTerm.getBytes();
                int currentBytesLength = bytes.length;

                randomAccessVocabTable.writeInt(currentBytesLength);
                randomAccessVocabTable.write(bytes);

                // since the byte positions are in ascending order, we can write them as gaps
                int currentBytePosition = bytePositions.get(i);
                randomAccessVocabTable.writeInt(currentBytePosition);

                bTree.insert(currentTerm, currentBytePosition, false);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readBTreeFromDisk() {
        // load the persisted index into the B+ Tree
        try {
            // write the total size of the vocabulary
            int vocabularySize = randomAccessVocabTable.readInt();
            int latestBytePosition = 0;

            // traverse through the vocabulary terms
            for (int i = 0; i < vocabularySize; ++i) {
                // read each String byte length normally
                int currentBytesLength = randomAccessVocabTable.readInt();
                StringBuilder term = new StringBuilder();

                // convert each byte to a character and build to our original term
                for (int j = 0; j < currentBytesLength; ++j) {
                    term.append((char) randomAccessVocabTable.read());
                }

                // since the byte positions are originally in ascending order, store the next byte position as a gap
                int currentBytePosition = randomAccessVocabTable.readInt();

                bTree.insert(term.toString(), currentBytePosition, false);
            }

        } catch (IOException e) {
            e.printStackTrace();
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
                int currentDocumentId = randomAccessPosting.readInt();
                latestDocumentId = currentDocumentId;
                int positionsSize = randomAccessPosting.readInt();
                int latestPosition = 0;

                for (int j = 0; j < positionsSize; ++j) {
                    int currentPosition = randomAccessPosting.readInt();
                    latestPosition = currentPosition;

                    positions.add(currentPosition);
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
    public List<Posting> getPositionlessPostings(String term) {
        List<Posting> resultPostings = new ArrayList<>();

        try {
            // retrieve the byte position value for the term key within the B+ Tree
            int bytePosition = bTree.get(term);
            // jump to the offset containing the term's postings
            randomAccessPosting.skipBytes(bytePosition);
            // the current int value at the offset is the size of the postings list
            int postingsSize = randomAccessPosting.readInt();
            int latestDocumentId = 0;

            // iterate through all postings for the term
            for (int i = 0; i < postingsSize; ++i) {
                int currentDocumentId = randomAccessPosting.readInt();
                latestDocumentId = currentDocumentId;
                // first document ID is as-is; the rest are gaps
                Posting newPosting = new Posting(currentDocumentId, new ArrayList<>());

                resultPostings.add(newPosting);
            }

        } catch (IOException e) {
            throw new RuntimeException();
        }

        return resultPostings;
    }

    @Override
    public List<String> getVocabulary() {
        List<String> vocabulary = new ArrayList<>();

        try {
            // write the total size of the vocabulary
            int vocabularySize = randomAccessPosting.readInt();

            // traverse through the vocabulary terms
            for (int i = 0; i < vocabularySize; ++i) {
                // read each String byte length as a gap
                int currentBytesLength = randomAccessPosting.readInt();
                StringBuilder term = new StringBuilder();

                // convert each byte to a character and build to our original term
                for (int j = 0; j < currentBytesLength; ++j) {
                    term.append((char) randomAccessPosting.readByte());
                }

                vocabulary.add(term.toString());

                // after reading the term bytes, skip the next byte position since we only need the vocabulary
                randomAccessPosting.skipBytes(4);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // vocabulary should already be sorted
        return vocabulary;
    }

    @Override
    public void close() {
        try {
            randomAccessVocabTable.close();
            randomAccessPosting.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
