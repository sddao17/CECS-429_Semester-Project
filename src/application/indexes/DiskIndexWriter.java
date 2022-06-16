
package application.indexes;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DiskIndexWriter {

    /**
     * 2. Create a class DiskIndexWriter with a single method writeIndex. You should pass your index
     * variable, as well as the absolute path to save the postings file.
     */
    public static List<Integer> writeIndex(Index<String, Posting> index, String pathToPostingBin) {
        // 2a. Open a new file called "postings.bin" in binary write mode.
        String postingFileName = "postings.bin";
        File fileToWrite = new File(pathToPostingBin, postingFileName);
        /* 3. writeIndex should return a list of (8-byte) integer values, one value for each of the terms
          in the index vocabulary. Each integer value should equal the byte position of where the postings
          for the corresponding term from the vocabulary begin in postings.bin. */
        List<Integer> bytePositions = new ArrayList<>();

        try (FileOutputStream fileStream = new FileOutputStream(fileToWrite);
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
                for (int i = 0; i < postings.size(); ++i) {
                    // store values for readability
                    Posting currentPosting = postings.get(i);
                    List<Integer> currentPositions = currentPosting.getPositions();
                    int currentDocumentId = currentPosting.getDocumentId();
                    int latestPosition = 0;

                    /* (2, iii, A). Write the posting's document ID as a 4-byte gap. (The first document in a list
                      is written as-is. All the rest are gaps from the previous value.) */
                    if (i == 0) {
                        dataStream.writeInt(currentDocumentId);
                        latestDocumentId = currentDocumentId;
                    } else {
                        int gap = currentDocumentId - latestDocumentId;
                        dataStream.writeInt(gap);
                        latestDocumentId = gap;
                    }

                    // (2, iii, B). Write tf(t,d) as a 4-byte integer.
                    dataStream.writeInt(currentPositions.size());

                    for (int j = 0; j < currentPositions.size(); ++j) {
                        int currentPosition = currentPositions.get(j);

                        /* (2, iii, C). Write the list of positions, each a 4-byte gap. (The first position
                          is written as-is. All the rest are gaps from the previous value.) */
                        if (j == 0) {
                            dataStream.writeInt(currentPosition);
                            latestPosition = currentPosition;
                        } else {
                            int gap = currentPosition - latestPosition;
                            dataStream.writeInt(gap);
                            latestPosition = gap;
                        }
                    }
                }
                // (2, iv). Repeat for each term in the vocabulary.
            }
            System.out.println("\nData written to `" + pathToPostingBin + "/" + postingFileName + "` successfully.");

        } catch(IOException e) {
            throw new RuntimeException();
        }

        return bytePositions;
    }
}
