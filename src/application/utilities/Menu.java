
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
}
