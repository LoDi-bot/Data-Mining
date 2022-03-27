import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        HashSet<String> wordsSet = countIntoSet();
        int m = wordsSet.size();
        System.out.println("Text contains " + m + "unique words");
        System.out.println("With estimated accuracy 90%:");
        int n = (int) Math.ceil((- m * Math.log(0.1d)) / Math.pow(Math.log(2.0d), 2));
        System.out.println("n - number of bits: " + n);
        int k = (int) Math.ceil((n / m) * Math.log(2.0d));
        System.out.println("k - number of hash-functions: " + k);
        BloomFilter bloomFilter = new BloomFilter(n, k);
        for (String word : wordsSet.stream().toList()) {
            bloomFilter.add(word);
        }

        String[] check = new String[] {"data", "mining", "flex", "empire", "feather", "Anna", "mobile", "computer", "cyrus", "itis"};

        for (String word : check) {
            bloomFilter.contains(word);
        }
    }

    public static HashSet<String> countIntoSet() throws IOException {
        HashSet<String> resultSet = new HashSet<>();
        BufferedReader reader = new BufferedReader(new FileReader("/Users/lodi/Desktop/Classwork/Java/Lesson 11/vim1.txt", StandardCharsets.UTF_8));
        String currentLine = reader.readLine();
        while (currentLine != null) {
            String[] words = currentLine.toLowerCase().split("[\"\\[\\]'.,;:=@#%<>_!?(){}\\-+|& */]");

            for (String word : words) {
                if (!word.isBlank()) {
                    resultSet.add(word);
                }
            }
            currentLine = reader.readLine();
        }
        return resultSet;
    }
}