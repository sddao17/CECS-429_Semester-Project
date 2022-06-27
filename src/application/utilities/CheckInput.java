
package application.utilities;

import java.util.Scanner;

public class CheckInput {

    /**
     * Checks if the inputted value is an integer and
     * within the specified range (ex: 1-10)
     *
     * @param low  lower bound of the range.
     * @param high upper bound of the range.
     * @return the valid input.
     */
    public static int checkIntRange(int low, int high) {
        Scanner in = new Scanner(System.in);
        int input = 0;
        boolean valid = false;

        while (!valid) {
            if (in.hasNextInt()) {
                input = in.nextInt();
                if (input <= high && input >= low) {
                    valid = true;
                } else {
                    System.out.print("Invalid range; please try again: ");
                }
            } else {
                in.next(); //clear invalid string
                System.out.print("Invalid input; please try again: ");
            }
        }
        return input;
    }

    public static int promptNumOfResults(Scanner in, int size) {
        int numOfResults;

        try {
            numOfResults = Integer.parseInt(in.nextLine());

            // error handling: if the requested number of results exceeds the max, set it to the max
            numOfResults = Math.min(numOfResults, size);
        } catch (NumberFormatException e) {
            numOfResults = size;
        }
        return numOfResults;
    }
}
