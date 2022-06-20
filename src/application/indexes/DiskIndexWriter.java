package application.indexes;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DiskIndexWriter {

    public static void createIndexDirectory(String pathToIndexDirectory) {
        File indexDirectory = new File(pathToIndexDirectory);
        indexDirectory.mkdir();
    }

    /**
     * 2. Create a class DiskIndexWriter with a method writeIndex. You should pass your index
     * variable, as well as the absolute path to save the postings file.
     */
    public static List<Integer> writeIndex(String pathToPostingBin, Index<String, Posting> index) {
        /* 3. writeIndex should return a list of (8-byte) integer values, one value for each of the terms
          in the index vocabulary. Each integer value should equal the byte position of where the postings
          for the corresponding term from the vocabulary begin in postings.bin. */
        List<Integer> bytePositions = new ArrayList<>();

        // 2a. Open a new file called "postings.bin" in binary write mode.
        try (FileOutputStream fileStream = new FileOutputStream(pathToPostingBin);
             BufferedOutputStream bufferStream = new BufferedOutputStream(fileStream);
             DataOutputStream dataStream = new DataOutputStream(bufferStream)) {
            // 2b. Retrieve the sorted vocabulary list from the index.
            List<String> vocabulary = index.getVocabulary();

            // 2c. For each term in the vocabulary:
            for (String term : vocabulary) {
                // add the byte position of the current term to our returning list
                bytePositions.add(dataStream.size());
                // 2 (c, ii). Retrieve the index postings for the term.
                List<Posting> postings = index.getPostings(term);
                // 2 (c, i). Write dft to the file as a 4-byte integer.
                dataStream.writeInt(postings.size());
                int latestDocumentId = 0;

                // 2 (c, iii). For each posting:
                for (Posting currentPosting : postings) {
                    // store values for readability
                    List<Integer> currentPositions = currentPosting.getPositions();

                    /* (2, iii, A). Write the posting's document ID as a 4-byte gap. (The first document in a list
                      is written as-is. All the rest are gaps from the previous value.) */
                    int currentDocumentId = currentPosting.getDocumentId() - latestDocumentId;
                    dataStream.writeInt(currentDocumentId);
                    latestDocumentId = currentDocumentId;

                    // (2, iii, B). Write tf(t,d) as a 4-byte integer.
                    dataStream.writeInt(currentPositions.size());

                    int latestPosition = 0;

                    for (int currentPosition : currentPositions) {
                        /* (2, iii, C). Write the list of positions, each a 4-byte gap. (The first position
                          is written as-is. All the rest are gaps from the previous value.) */
                        currentPosition = currentPosition - latestPosition;
                        dataStream.writeInt(currentPosition);
                        latestPosition = currentPosition;

                    }
                }
                // (2, iv). Repeat for each term in the vocabulary.
            }

        } catch(IOException e) {
            e.printStackTrace();
        }

        return bytePositions;
    }

    public static void writeBTree(String pathToBTreeBin, List<String> vocabulary, List<Integer> bytePositions) {
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
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeKGrams(String pathToKGramsBin, KGramIndex kGramIndex) {
        // overwrite any existing files
        try (FileOutputStream fileStream = new FileOutputStream(pathToKGramsBin, false);
             BufferedOutputStream bufferStream = new BufferedOutputStream(fileStream);
             DataOutputStream dataStream = new DataOutputStream(bufferStream)) {
            // order does not matter when writing map elements that will be read as map elements
            List<String> keys = kGramIndex.getVocabulary();
            // write the main k-grams first, starting with the size of the keys
            dataStream.writeInt(keys.size());

            // iterate through the keys
            for (String key : keys) {
                byte[] keyBytes = key.getBytes();
                int currentKeyBytesLength = keyBytes.length;

                // write how many bytes are in the key itself, then the bytes of the key
                dataStream.writeInt(currentKeyBytesLength);
                dataStream.write(keyBytes);

                List<String> values = kGramIndex.getPostings(key);

                // write the size of the k-grams posting list
                dataStream.writeInt(values.size());

                // iterate through the k-gram values
                for (String value :values) {
                    byte[] valueBytes = value.getBytes();
                    int currentBytesLength = valueBytes.length;

                    // for each individual k-gram, write the length of the k-gram followed by its bytes
                    dataStream.writeInt(currentBytesLength);
                    dataStream.write(valueBytes);
                }
            }

            // now write the distinct k-grams to the end of the file, starting with the size of the set
            Set<String> distinctKGrams = kGramIndex.getDistinctKGrams();
            dataStream.writeInt(distinctKGrams.size());

            // iterate through the set of distinct k-grams
            for (String kGram : distinctKGrams) {
                byte[] bytes = kGram.getBytes();
                int currentBytesLength = bytes.length;

                // for each individual k-gram, write the length of the k-gram followed by its bytes
                dataStream.writeInt(currentBytesLength);
                dataStream.write(bytes);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeBiword(String pathToBiwordBin, BiwordIndex biwordIndex){
        // overwrite any existing files
        try (FileOutputStream fileStream = new FileOutputStream(pathToBiwordBin, false);
             BufferedOutputStream bufferStream = new BufferedOutputStream(fileStream);
             DataOutputStream dataStream = new DataOutputStream(bufferStream)) {
            // order does not matter when writing map elements that will be read as map elements
            List<String> keys = biwordIndex.getVocabulary();
            // write the main k-grams first, starting with the size of the keys
            dataStream.writeInt(keys.size());

            // iterate through the keys
            for (String key : keys) {
                byte[] keyBytes = key.getBytes();
                int currentKeyBytesLength = keyBytes.length;

                // write how many bytes are in the key itself, then the bytes of the key
                dataStream.writeInt(currentKeyBytesLength);
                dataStream.write(keyBytes);

                List<String> values = biwordIndex.getPostings(key);

                // write the size of the k-grams posting list
                dataStream.writeInt(values.size());

                // iterate through the k-gram values
                for (String value :values) {
                    byte[] valueBytes = value.getBytes();
                    int currentBytesLength = valueBytes.length;

                    // for each individual k-gram, write the length of the k-gram followed by its bytes
                    dataStream.writeInt(currentBytesLength);
                    dataStream.write(valueBytes);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void writeLds(String pathToDocWeightsBin, List<Double> lds) {
        // overwrite any existing files
        try (FileOutputStream fileStream = new FileOutputStream(pathToDocWeightsBin, false);
             BufferedOutputStream bufferStream = new BufferedOutputStream(fileStream);
             DataOutputStream dataStream = new DataOutputStream(bufferStream)) {
            for (Double ld : lds) {
                dataStream.writeDouble(ld);
            }

        } catch (IOException e) {
            System.err.println("Invalid path; please restart the program and build an index" +
                    " with a valid directory path.");
            System.exit(0);
        }
    }


}