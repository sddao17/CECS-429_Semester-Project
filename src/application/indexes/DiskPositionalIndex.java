
package application.indexes;

import org.apache.jdbm.*;

import java.io.IOException;
import java.util.List;

public class DiskPositionalIndex implements Index<String, Posting> {

    DBStore database;
    BTree<String, Posting> bTree;

    public DiskPositionalIndex(String indexFileName) {
        database = (DBStore) DBMaker.openFile(indexFileName).make();

        try {
            bTree = BTree.createInstance(database);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public void writeBTree(List<String> vocabulary, List<Integer> bytePositions) {
        // clear any existing data before we write a new B+ Tree
        try {
            clearDatabase(vocabulary);
        } catch (IOException e) {
            throw new RuntimeException();
        }

        database.commit();
    }

    public void clearDatabase(List<String> vocabulary) throws IOException {
        for (String term : vocabulary) {
            bTree.remove(term);
        }

        database.commit();
    }

    @Override
    public List<Posting> getPostings(String term) {
        return null;
    }

    @Override
    public List<String> getVocabulary() {
        return null;
    }
}
