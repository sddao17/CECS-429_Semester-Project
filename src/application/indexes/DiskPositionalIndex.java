
package application.indexes;

import org.apache.jdbm.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DiskPositionalIndex implements Index<String, Posting> {

    private DBStore database;
    private BTree<String, Integer> bTree;
    private final String pathToIndexBin;
    private final String pathToIndexDirectory;
    private final String pathToVocabList;

    public DiskPositionalIndex(String newPathToIndexBin, String newDirectoryPath) {
        pathToIndexBin = newPathToIndexBin;
        pathToIndexDirectory = newDirectoryPath;
        pathToVocabList = newDirectoryPath + "/vocabList.bin";
    }

    public void writeIndexes(List<String> vocabulary, List<Integer> bytePositions) {
        writeVocabList(vocabulary);
        writeBTree(vocabulary, bytePositions);
    }

    public void writeVocabList(List<String> vocabulary) {
        File fileToWrite = new File(pathToVocabList);

        try (FileOutputStream fileStream = new FileOutputStream(fileToWrite);
             BufferedOutputStream bufferStream = new BufferedOutputStream(fileStream);
             DataOutputStream dataStream = new DataOutputStream(bufferStream)) {
            // write the total size of the vocabulary
            dataStream.writeInt(vocabulary.size());
            int latestBytesLength = 0;

            // traverse through the vocabulary terms
            for (String term : vocabulary) {
                // store values for readability
                byte[] bytes = term.getBytes();
                int currentBytesLength = bytes.length;
                // write each String byte length as a gap
                dataStream.writeInt(currentBytesLength - latestBytesLength);
                latestBytesLength = currentBytesLength;

                dataStream.write(bytes);
            }

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void writeBTree(List<String> vocabulary, List<Integer> bytePositions) {
        try {
            database = (DBStore) DBMaker.openFile(pathToIndexBin).closeOnExit().make();
            bTree = BTree.createInstance(database);

            for (int i = 0; i < vocabulary.size(); ++i) {
                String currentTerm = vocabulary.get(i);
                int currentBytePosition = bytePositions.get(i);

                // third param specifies whether to replace duplicate entries; this doesn't apply to our index
                bTree.insert(currentTerm, currentBytePosition, false);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        database.commit();

        System.out.println(database.calculateStatistics());
    }

    public void loadBTree() {
        // load the persisted database and record ID into the B+ Tree
        try {
            database = new DBStore(pathToIndexBin, false, true, true);
            System.out.println(database.getCollections());
            bTree = BTree.load(database, bTree.getRecid());

            System.out.println(database.calculateStatistics());

        } catch(IOException e) {
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
        String pathToPostingsBin = pathToIndexDirectory + "/postings.bin";
        File fileToRead = new File(pathToPostingsBin);

        try (FileInputStream fileStream = new FileInputStream(fileToRead);
             BufferedInputStream bufferStream = new BufferedInputStream(fileStream);
             DataInputStream dataStream = new DataInputStream(bufferStream)) {
            // retrieve the byte position value for the term key within the B+ Tree
            int bytePosition = bTree.get(term);
            System.out.println(term + " --> " + bytePosition);
            // jump to the offset containing the term's postings
            dataStream.skipBytes(bytePosition);
            // the current int value at the offset is the size of the postings list
            int postingsSize = dataStream.readInt();
            int latestDocumentId = 0;

            // iterate through all postings for the term
            for (int i = 0; i < postingsSize; ++i) {
                ArrayList<Integer> positionsList = new ArrayList<>();
                // after df(t) is doc id, then tf(t, d)
                int currentDocumentId = dataStream.readInt();
                int positionsSize = dataStream.readInt();
                int latestPosition = 0;

                for (int j = 0; j < positionsSize; ++j) {
                    int currentPosition = dataStream.readInt();

                    // the first position is as-is; the rest are gaps
                    positionsList.add(currentPosition + latestPosition);
                    latestPosition = currentPosition;
                }

                // the first document ID is as-is; the rest are gaps
                Posting newPosting = new Posting(currentDocumentId + latestDocumentId, positionsList);

                resultPostings.add(newPosting);
                latestDocumentId = currentDocumentId;
            }

        } catch (IOException e) {
            e.printStackTrace();
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
        String pathToPostingsBin = pathToIndexDirectory + "/postings.bin";
        File fileToRead = new File(pathToPostingsBin);

        try (FileInputStream fileStream = new FileInputStream(fileToRead);
             BufferedInputStream bufferStream = new BufferedInputStream(fileStream);
             DataInputStream dataStream = new DataInputStream(bufferStream)) {
            // retrieve the byte position value for the term key within the B+ Tree
            int bytePosition = bTree.get(term);
            // jump to the offset containing the term's postings
            dataStream.skipBytes(bytePosition);
            // the current int value at the offset is the size of the postings list
            int postingsSize = dataStream.readInt();
            int latestDocumentId = 0;

            // iterate through all postings for the term
            for (int i = 0; i < postingsSize; ++i) {
                int currentDocumentId = dataStream.readInt();
                // first document ID is as-is; the rest are gaps
                Posting newPosting = new Posting(currentDocumentId + latestDocumentId, new ArrayList<>());

                resultPostings.add(newPosting);
                latestDocumentId = currentDocumentId;
            }

        } catch (IOException e) {
            throw new RuntimeException();
        }

        return resultPostings;
    }

    @Override
    public List<String> getVocabulary() {
        List<String> vocabulary = new ArrayList<>();
        File fileToRead = new File(pathToVocabList);

        // read from the `vocabList.bin` file and extract the bytes from each term
        try (FileInputStream fileStream = new FileInputStream(fileToRead);
             BufferedInputStream bufferStream = new BufferedInputStream(fileStream);
             DataInputStream dataStream = new DataInputStream(bufferStream)) {
            // write the total size of the vocabulary
            int vocabularySize = dataStream.readInt();
            int latestBytesLength = 0;

            // traverse through the vocabulary terms
            for (int i = 0; i < vocabularySize; ++i) {
                // read each String byte length as a gap
                int currentBytesLength = dataStream.readInt() + latestBytesLength;
                latestBytesLength = currentBytesLength;
                StringBuilder term = new StringBuilder();

                // convert each byte to a character and build to our original term
                for (int j = 0; j < currentBytesLength; ++j) {
                    term.append((char) dataStream.readByte());
                }

                vocabulary.add(term.toString());
            }

        } catch(IOException e) {
            e.printStackTrace();
        }

        // vocabulary should already be sorted
        return vocabulary;
    }
}
