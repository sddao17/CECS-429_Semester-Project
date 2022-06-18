
package application.indexes;

import org.apache.jdbm.BTree;
import org.apache.jdbm.DB;
import org.apache.jdbm.DBMaker;
import org.apache.jdbm.DBStore;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DiskIndexReader {

    public static BTree<String, Integer> readBTree(String pathToBTreeBin) {
        // initialize the database and B+ Tree
        DB database = DBMaker.openFile(pathToBTreeBin).deleteFilesAfterClose().closeOnExit().make();
        BTree<String, Integer> bTree = new BTree<>();

        try {
            bTree = BTree.createInstance((DBStore) database);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // load the persisted term -> byte positions map into the B+ Tree
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

                // insert the byte position associated with the term
                int currentBytePosition = dataStream.readInt();
                bTree.insert(term.toString(), currentBytePosition, false);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return bTree;
    }

    public static KGramIndex readKGrams(String pathToKGramsBin) {
        KGramIndex kgramIndex = new KGramIndex();
        // overwrite any existing files
        try (FileInputStream fileStream = new FileInputStream(pathToKGramsBin);
             BufferedInputStream bufferStream = new BufferedInputStream(fileStream);
             DataInputStream dataStream = new DataInputStream(bufferStream)) {
            // read the main k-grams first, starting with the size of the keys
            int keysSize = dataStream.readInt();

            // iterate through the keys
            for (int i = 0; i < keysSize; ++i) {
                // read the length of the current key
                int keyLength = dataStream.readInt();
                StringBuilder key = new StringBuilder();

                // add the following bytes to re-construct the key
                for (int j = 0; j < keyLength; ++j) {
                    key.append((char) dataStream.readByte());
                }

                // read the size of the key's list of values
                int valuesSize = dataStream.readInt();
                List<String> kGrams = new ArrayList<>();

                // iterate through the k-gram values
                for (int j = 0; j < valuesSize; ++j) {
                    // read each String byte length normally
                    int currentBytesLength = dataStream.readInt();
                    StringBuilder kGram = new StringBuilder();

                    // convert each byte to a character and build to our original k-gram
                    for (int k = 0; k < currentBytesLength; ++k) {
                        kGram.append((char) dataStream.readByte());
                    }
                    kGrams.add(kGram.toString());
                }

                kgramIndex.addKeyValue(key.toString(), kGrams);
            }

            // now read the distinct k-grams at the end of the file, starting with the size of the set
            int distinctKGramsSize = dataStream.readInt();

            // iterate through the set of k-grams
            for (int i = 0; i < distinctKGramsSize; ++i) {
                // read each String byte length normally
                int currentBytesLength = dataStream.readInt();
                StringBuilder kGram = new StringBuilder();

                // convert each byte to a character and build to our original k-gram
                for (int k = 0; k < currentBytesLength; ++k) {
                    kGram.append((char) dataStream.readByte());
                }

                kgramIndex.addDistinctKGram(kGram.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return kgramIndex;
    }

    public static double readLdFromBinFile(RandomAccessFile randomAccessor, int documentId) {
        int bytePosition = documentId * Double.BYTES;
        double documentWeight = 0;

        try {
            randomAccessor.seek(bytePosition);
            documentWeight = randomAccessor.readDouble();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return documentWeight;
    }
}
