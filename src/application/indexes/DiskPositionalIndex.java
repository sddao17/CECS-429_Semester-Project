
package application.indexes;

import org.apache.jdbm.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DiskPositionalIndex implements Index<String, Posting> {

    DBStore database;
    BTree<String, Integer> bTree;
    String pathToPostingsBin;

    public DiskPositionalIndex(String indexFileName, String newPathToPostingsBin) {
        database = (DBStore) DBMaker.openFile(indexFileName).make();
        pathToPostingsBin = newPathToPostingsBin;

        try {
            bTree = BTree.createInstance(database);

        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public void writeBTree(List<String> vocabulary, List<Integer> bytePositions) {
        try {
            for (int i = 0; i < vocabulary.size(); ++i) {
                String currentTerm = vocabulary.get(i);
                int currentBytePosition = bytePositions.get(i);

                // third param specifies whether to replace duplicate entries; this doesn't apply to our index
                bTree.insert(currentTerm, currentBytePosition, false);
            }

        } catch (IOException e) {
            throw new RuntimeException();
        }

        database.commit();
    }

    public void clearTree() {
        try {
           bTree.clear();

        } catch (IOException e) {
            throw new RuntimeException();
        }

        database.commit();
    }

    @Override
    public List<Posting> getPostings(String term) {
        List<Posting> resultPostings = new ArrayList<>();
        String postingsFileName = getPostingsFileName();
        File fileToRead = new File(pathToPostingsBin, postingsFileName);

        try (FileInputStream fileStream = new FileInputStream(fileToRead);
             BufferedInputStream bufferStream = new BufferedInputStream(fileStream);
             DataInputStream dataStream = new DataInputStream(bufferStream)) {
            // retrieve the byte position value for the term key within the B+ Tree
            int bytePosition = bTree.get(term);
            // jump to the offset containing the term's postings
            dataStream.skipBytes(bytePosition);
            // the current int value at the offset is the size of the postings list
            int postingsSize = dataStream.readInt();

            // iterate through all postings for the term
            for (int i = 0; i < postingsSize; ++i) {
                
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

    private String getPostingsFileName() {
        int indexOfLastSlash = pathToPostingsBin.lastIndexOf("/");

        return pathToPostingsBin.substring(indexOfLastSlash + 1);
    }
}
