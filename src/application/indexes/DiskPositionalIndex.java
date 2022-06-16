
package application.indexes;

import org.apache.jdbm.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DiskPositionalIndex implements Index<String, Posting> {

    private DBStore database;
    private BTree<String, Integer> bTree;
    private final String indexFileName;
    private final String directoryPath;

    public DiskPositionalIndex(String newIndexFileName, String newDirectoryPath) {
        indexFileName = newIndexFileName;
        directoryPath = newDirectoryPath;
    }

    public void writeBTree(List<String> vocabulary, List<Integer> bytePositions) {
        try {
            database = (DBStore) DBMaker.openFile(indexFileName).make();
            bTree = BTree.createInstance(database);

            for (int i = 0; i < vocabulary.size(); ++i) {
                String currentTerm = vocabulary.get(i);
                int currentBytePosition = bytePositions.get(i);

                // third param specifies whether to replace duplicate entries; this doesn't apply to our index
                bTree.insert(currentTerm, currentBytePosition, true);
            }

        } catch (IOException e) {
            throw new RuntimeException();
        }

        database.commit();

        // after committing, save the record ID to a file, so we can load it later
        String diskIndexRecId = directoryPath + "/diskIndexRecId.bin";
        File fileToWrite = new File(diskIndexRecId);

        try (FileOutputStream fileStream = new FileOutputStream(fileToWrite);
             BufferedOutputStream bufferStream = new BufferedOutputStream(fileStream);
             DataOutputStream dataStream = new DataOutputStream(bufferStream)) {
            dataStream.writeLong(bTree.getRecid());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadBTree() {
        // after committing, save the record ID to a file, so we can load it later
        String diskIndexRecId = directoryPath + "/diskIndexRecId.bin";
        File fileToWrite = new File(diskIndexRecId);
        long bTreeRecId = 0;

        try (FileInputStream fileStream = new FileInputStream(fileToWrite);
             BufferedInputStream bufferStream = new BufferedInputStream(fileStream);
             DataInputStream dataStream = new DataInputStream(bufferStream)) {
            bTreeRecId = dataStream.readLong();
            bTree = BTree.load(database, bTreeRecId);

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
        String pathToPostingsBin = directoryPath + "/postings.bin";
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
        String pathToPostingsBin = directoryPath + "/postings.bin";
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
        return null;
    }
}
