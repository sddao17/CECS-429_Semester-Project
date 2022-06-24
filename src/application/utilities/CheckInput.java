
package application.utilities;

import java.util.Scanner;

public class CheckInput {

    public static String checkMenuInput2(Scanner in) {
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

    public static String checkMenuInput3(Scanner in) {
        String input;
        boolean isValidInput = false;

        // simple input check
        do {
            input = in.nextLine();

            if (input.equals("1") || input.equals("2") || input.equals("3")) {
                isValidInput = true;
            } else {
                System.out.print("Invalid input; please try again: ");
            }
        } while (!isValidInput);

        return input;
    }

    public static String checkMenuInput4(Scanner in) {
        String input;
        boolean isValidInput = false;

        // simple input check
        do {
            input = in.nextLine();

            if (input.equals("1") || input.equals("2") || input.equals("3") || input.equals("4")) {
                isValidInput = true;
            } else {
                System.out.print("Invalid input; please try again: ");
            }
        } while (!isValidInput);

        return input;
    }

    public static String checkMenuInput5(Scanner in) {
        String input;
        boolean isValidInput = false;

        // simple input check
        do {
            input = in.nextLine();

            if (input.equals("1") || input.equals("2") || input.equals("3") || input.equals("4") || input.equals("5")) {
                isValidInput = true;
            } else {
                System.out.print("Invalid input; please try again: ");
            }
        } while (!isValidInput);

        return input;
    }

    public static String checkMenuInput6(Scanner in) {
        String input;
        boolean isValidInput = false;

        // simple input check
        do {
            input = in.nextLine();

            if (input.equals("1") || input.equals("2") || input.equals("3") || input.equals("4") || input.equals("5")
                    || input.equals("6")) {
                isValidInput = true;
            } else {
                System.out.print("Invalid input; please try again: ");
            }
        } while (!isValidInput);

        return input;
    }
}
