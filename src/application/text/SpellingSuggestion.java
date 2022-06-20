
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

    public int calculateLevenshteinDistance() {


        return 0;
    }

    public static void main(String[] args) {
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

    }
}
