
package application.utilities;

import application.Application;

import java.util.Scanner;

public class Menu {

    public static int showBuildOrQueryIndexMenu() {
        System.out.printf("""
                %nSelect an option:
                1. Build a new index
                2. Query an on-disk index
                 >>\040""");

        return CheckInput.checkIntRange(0, 2);
    }

    public static int showQueryMenu() {
        System.out.printf("""
                %nSelect a query method:
                1. Boolean queries
                2. Ranked Retrieval queries
                3. Classify documents
                 >>\040""");

        return CheckInput.checkIntRange(0, 3);
    }

    public static int showClassificationMenu() {
        System.out.printf("""
                %nSelect a classification method:
                1. Bayesian
                2. Rocchio
                3. kNN
                 >>\040""");

        return CheckInput.checkIntRange(0, 3);
    }

    public static int showRocchioMenu() {
        System.out.printf("""
                %nSelect an option:
                1. Classify a document
                2. Classify all documents
                3. Get a centroid vector
                4. Get a document weight vector
                5. Get a vocabulary list
                0. Quit
                 >>\040""");

        return CheckInput.checkIntRange(0, 5);
    }

    public static void showHelpMenu(int vocabPrintSize) {
        if (Application.getAllDirectoryPaths().size() > 1) {
            System.out.printf("""
                    Detected %s subdirectories within the root directory.
                    By default, the corpus is set to the root (see `:set` command):
                    """, Application.getAllDirectoryPaths().size());
            Application.getAllDirectoryPaths().forEach(System.out::println);
        }

        System.out.printf("""
                %n:set `subdirectory-path`  --  Sets the components to read the contents of the subdirectory path.
                 :index `directory-name`  --  Index the folder at the specified path.
                           :stem `token`  --  Stem, then print the token string.
                                  :vocab  --  Print the first %s terms in the vocabulary of the corpus,
                                              then print the total number of vocabulary terms.
                                 :kgrams  --  Print the first %s k-gram mappings of vocabulary types to
                                              k-gram tokens, then print the total number of vocabulary types.
                           `query` --log  --  Enable printing a debugging log to the console before printing
                                              the query results.
                                      :q  --  Exit the program.
                """, vocabPrintSize, vocabPrintSize);
    }
}
