
package application.indexes;

import org.apache.jdbm.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DiskPositionalIndex implements Index<String, Posting>, Closeable {

    private final DB database;
    private BTree<String, Integer> bTree;
    private final String pathToBTreeBin;    // the path to the B+ Tree mappings of terms -> byte positions
    private RandomAccessFile randomAccessPosting;   // keep the Posting file open for getPosting() calls

    public DiskPositionalIndex(String newDirectoryPath) {
        database = DBMaker.openFile(newDirectoryPath + "/db").closeOnExit().make();
        pathToBTreeBin = newDirectoryPath + "/bTree.bin";
        // the path to the `postings.bin` files
        String pathToPostingsBin = newDirectoryPath + "/postings.bin";

        try {
            bTree = BTree.createInstance((DBAbstract) database);
            // be able to read from the postings file and extract the index data
            randomAccessPosting = new RandomAccessFile(pathToPostingsBin, "r");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void writeBTreeToDisk(List<String> vocabulary, List<Integer> bytePositions) {
        // overwrite any existing files
        try (FileOutputStream fileStream = new FileOutputStream(pathToBTreeBin, false);
            BufferedOutputStream bufferStream = new BufferedOutputStream(fileStream);
            DataOutputStream dataStream = new DataOutputStream(bufferStream)) {
            // write the size of the vocabulary as the first 4 bytes
            dataStream.writeInt(vocabulary.size());

            for (int i = 0; i < vocabulary.size(); ++i) {
                String currentTerm = vocabulary.get(i);
                byte[] bytes = currentTerm.getBytes();
                int currentBytesLength = bytes.length;
                int currentBytePosition = bytePositions.get(i);

                // write the byte length, followed by the bytes themselves, then the byte position
                dataStream.writeInt(currentBytesLength);
                dataStream.write(bytes);
                dataStream.writeInt(currentBytePosition);
                bTree.insert(currentTerm, currentBytePosition, false);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        database.commit();
    }

    public void readBTreeFromDisk() {
        // load the persisted index into the B+ Tree
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
                    term.append((char) dataStream.read());
                }

                // insert the byte position associated with the term
                int currentBytePosition = dataStream.readInt();
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
                // first document ID is as-is; the rest are gaps
                int currentDocumentId = randomAccessPosting.readInt() + latestDocumentId;
                latestDocumentId = currentDocumentId - latestDocumentId;

                // ignore reading the positions and only add the document ID
                Posting newPosting = new Posting(currentDocumentId, new ArrayList<>());
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
    public void close() {
        try {
            randomAccessPosting.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
