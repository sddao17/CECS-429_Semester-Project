
package application.utilities;

import java.util.Scanner;

public class CheckInput {

    public static String checkMenuInput(Scanner in) {
        String input;
        boolean isValidInput = false;

        // simple input check
        do {
            input = in.nextLine();

            if (input.equals("1") || input.equals("2")) {
                isValidInput = true;
            } else {
                System.out.print("Invalid input; please try again: ");
            }
        } while (!isValidInput);

        return input;
    }
}
