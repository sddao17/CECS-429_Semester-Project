
package application.utilities;

import java.util.Scanner;

public class Menu {

    public static String showBuildOrQueryIndexMenu(Scanner in) {
        System.out.printf("""
                %nSelect an option:
                1. Build a new index
                2. Query an on-disk index
                 >>\040""");

        return CheckInput.checkMenuInput(in);
    }

    public static String showBooleanOrRankedMenu(Scanner in) {
        System.out.printf("""
                %nSelect a query method:
                1. Boolean queries
                2. Ranked Retrieval queries
                 >>\040""");

        return CheckInput.checkMenuInput(in);
    }

    public static void showSpecialCommandMenu(int vocabPrintSize) {
        System.out.printf("""
                %nSpecial Commands:
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
