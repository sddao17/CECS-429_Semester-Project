
package application.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpellingSuggestion {

    List<String> vocabulary;

    public SpellingSuggestion(List<String> inputVocabulary) {
        vocabulary = inputVocabulary;
    }

    public static double calculateJaccardCoeff(List<String> leftList, List<String> rightList) {
        // remember - O(n) intersections / unions involved sorted lists
        Collections.sort(leftList);
        Collections.sort(rightList);

        List<String> intersections = intersectKGrams(leftList, rightList);
        List<String> unions = unionizeKGrams(leftList,rightList);

        // JC = |A ∩ B| / |A ∪ B|
        return (double) intersections.size() / unions.size();
    }

    public static List<String> intersectKGrams(List<String> leftList, List<String> rightList) {
        List<String> intersections = new ArrayList<>();

        int leftIndex = 0;
        int rightIndex = 0;

        // use logic similar to the intersection algorithm of AndQuery
        while (leftIndex < leftList.size() && rightIndex < rightList.size()) {
            // store values for readability
            String leftKGram = leftList.get(leftIndex);
            String rightKGram = rightList.get(rightIndex);

            // add the intersection if the k-grams are the same and progress the index iterators
            if (leftKGram.equals(rightKGram)) {
                intersections.add(leftKGram);
                ++leftIndex;
                ++rightIndex;
            } else if (leftKGram.compareTo(rightKGram) < 0){
                ++leftIndex;
            } else {
                ++rightIndex;
            }
        }

        return intersections;
    }

    public static List<String> unionizeKGrams(List<String> leftList, List<String> rightList) {
        List<String> unions = new ArrayList<>();

        int leftIndex = 0;
        int rightIndex = 0;

        // iterate through the lists until one (or both) have been fully traversed
        while (leftIndex < leftList.size() && rightIndex < rightList.size()) {
            // store values for readability
            String leftKGram = leftList.get(leftIndex);
            String rightKGram = rightList.get(rightIndex);

            // add the union if the k-grams are the same and progress the index iterators
            if (leftKGram.compareTo(rightKGram) < 0) {
                unions.add(leftKGram);
                ++leftIndex;
            } else if (leftKGram.compareTo(rightKGram) > 0) {
                unions.add(rightKGram);
                ++rightIndex;
            } else {
                // add one of the duplicate elements and then increment both iterators
                unions.add(rightKGram);
                ++leftIndex;
                ++rightIndex;
            }
        }

        // similar to mergeSort, add any leftovers of any non-fully-traversed list
        if (leftIndex < leftList.size()) {
            unions.addAll(leftList.subList(leftIndex, leftList.size()));
        } else if (rightIndex < rightList.size()) {
            unions.addAll(rightList.subList(rightIndex, rightList.size()));
        }

        return unions;
    }

    public static int calculateLevenshteinDistance(String leftToken, String rightToken) {
        /* dynamic programming algorithm from lecture material:
          d(i, j) =
          { i, if j = 0
          { j, if i = 0
          { min( (d(i - 1, j) + 1, d(i, j - 1) + 1, d(i - 1, j - 1) + (u(i) =/= v(j)) )

          Ex: leftToken = fries, rightToken = fryz
               (j) f  r  y  z
              +--------------
          (i) | 0  1  2  3  4
           f  | 1  0  1  2  3
           r  | 2  1  0  1  2
           i  | 3  2  1  1  2
           e  | 4  3  2  2  2
           s  | 5  4  3  3  3 < levenshtein edit distance = 3
                          ^
          */
        // index pointers to the last character of the left /right token
        int i = leftToken.length() - 1;
        int j = rightToken.length() - 1;

        if (i < 0 && j >= 0) {
            return j + 1;
        } else if (j < 0 && i >= 0) {
            return i + 1;
        } else if (i < 0) {
            return 0;
        }

        return calculateEdits(leftToken, rightToken, i, j);
    }

    public static int calculateEdits(String leftToken, String rightToken, int i, int j) {
        // top edit distance is just the value of the previous top value + 1, if it exists
        int topEdit = ( (i - 1 >= 0) ? calculateEdits(leftToken, rightToken, i - 1, j) + 1 : Integer.MAX_VALUE );
        // left edit distance is just the value of the previous left value + 1, if it exists
        int leftEdit = ( (j - 1 >= 0) ? calculateEdits(leftToken, rightToken, i, j - 1) + 1: Integer.MAX_VALUE );
        /* diagonal edit distance = 0 if both index pointers are at the start of their tokens, or it = the previous
          diagonal edit distance if it exists, or it is not considered if the diagonal would be out of bounds otherwise;
          we additionally add + 1 if the current characters don't match */
        int previousDiagonal;

        if (i == 0 && j == 0) {
            previousDiagonal = 0;
        } else if (i - 1 >= 0 && j - 1 >= 0) {
            previousDiagonal = calculateEdits(leftToken, rightToken, i - 1, j - 1);
        } else {
            previousDiagonal = Integer.MAX_VALUE - 1;
        }

        int diagonalEdit = previousDiagonal + ( (leftToken.charAt(i) != rightToken.charAt(j)) ? 1 : 0 );

        //System.out.println("i = " + i + ", j = " + j + "\n" + topEdit + ", " + leftEdit + ", " + diagonalEdit);
        return Math.min(Math.min(topEdit, leftEdit), diagonalEdit);
    }

    // testing purposes only
    public static void main(String[] args) {
        // testing jaccard coefficients
        List<String> leftList = new ArrayList<>(){{
            add("data");
            add("is");
            add("the");
            add("new");
            add("oil");
            add("of");
            add("digital");
            add("economy");
        }};

        List<String> rightList = new ArrayList<>(){{
            add("data");
            add("is");
            add("a");
            add("new");
            add("oil");
        }};

        // remember - O(n) intersections / unions involved sorted lists!
        Collections.sort(leftList);
        Collections.sort(rightList);

        List<String> intersections = intersectKGrams(leftList, rightList);
        List<String> unions = unionizeKGrams(leftList,rightList);

        System.out.println("Intersection: " + intersections +
                "\nUnion: " + unions +
                "\nJC = " + intersections.size() + " / " + unions.size() +
                "\n   = " + calculateJaccardCoeff(leftList, rightList));

        // testing levenshtein edit distance
        leftList = new ArrayList<>(){{
            //add("mavs");
            //add("spurs");
            //add("lakers");
            //add("cavs");
            //add("fries");
            add("aboard");
            //add("indubitably");
            //add("");
            //add("");
        }};

        rightList = new ArrayList<>(){{
            //add("rockets");
            //add("pacers");
            //add("warriors");
            //add("celtics");
            //add("fryz");
            add("bort");
            //add("");
            //add("frighteningly");
            //add("");
        }};

        for (int i = 0; i < leftList.size(); ++i) {
            String leftToken = leftList.get(i);
            String rightToken = rightList.get(i);

            System.out.println("Levenshtein edit distance for (" + leftToken + ", " + rightToken + ") = " +
                    calculateLevenshteinDistance(leftToken, rightToken));
        }
    }
}
