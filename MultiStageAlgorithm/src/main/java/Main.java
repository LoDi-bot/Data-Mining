import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        HashMap<String, ArrayList<String>> baskets = fromCsvToMap("transactions.csv");
        HashMap<HashSet<String>, Integer> frequentItemSets = computeFrequentItemSets(baskets, 4);
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("result.txt"), "utf-8"))) {
            writer.write(frequentItemSets.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<HashSet<String>, Integer> computeFrequentItemSets(HashMap<String, ArrayList<String>> baskets, Integer supportLevel) {
        HashMap<HashSet<String>, Integer> result = new HashMap<>();
        //{PRODUCT_CODE : COUNTER,
        // PRODUCT_CODE : COUNTER}
        HashMap<String, Integer> singletonsCounter = new HashMap<>();
        //{
        //  MODULUS : {[PRODUCT_CODE,PRODUCT_CODE] : COUNTER,
        //             [PRODUCT_CODE,PRODUCT_CODE] : COUNTER},
        //  MODULUS : {[PRODUCT_CODE,PRODUCT_CODE] : COUNTER,
        //             [PRODUCT_CODE,PRODUCT_CODE] : COUNTER}
        //}
        HashMap<Integer, HashMap<HashSet<String>, Integer>> hashBucket1 = new HashMap<>();
        HashMap<Integer, HashMap<HashSet<String>, Integer>> hashBucket2 = new HashMap<>();

        //PASS1
        for (ArrayList<String> products : baskets.values()) {
            //Singletons counting
            for (String product : products) {
                if (!singletonsCounter.containsKey(product)) {
                    singletonsCounter.put(product, 1);
                } else {
                    singletonsCounter.put(product, singletonsCounter.get(product) + 1);
                }
            }

            //Filling bucket 1
            for (int i = 0; i < products.size() - 1; i++) {
                for (int j = i + 1; j < products.size(); j++) {
                    HashSet<String> pair = new HashSet<>();
                    pair.add(products.get(i));
                    pair.add(products.get(j));
                    Integer x = Integer.valueOf(pair.toArray()[0].toString().substring(6));
                    Integer y = Integer.valueOf(pair.toArray()[1].toString().substring(6));
                    Integer modulus = hash1(x, y);
                    HashMap<HashSet<String>, Integer> doubletonsCounter;
                    if (!hashBucket1.containsKey(modulus)) {
                        doubletonsCounter = new HashMap<>();
                        doubletonsCounter.put(pair, 1);
                    } else {
                        doubletonsCounter = hashBucket1.get(modulus);
                        if (!doubletonsCounter.containsKey(pair)) {
                            doubletonsCounter.put(pair, 1);
                        } else {
                            doubletonsCounter.put(pair, doubletonsCounter.get(pair) + 1);
                        }
                    }
                    hashBucket1.put(modulus, doubletonsCounter);
                }
            }
        }

        //PASS2
        for (Map.Entry<Integer, HashMap<HashSet<String>, Integer>> entry : hashBucket1.entrySet()) {
            Integer support = entry.getValue().values().stream().reduce(0, Integer::sum);
            //Filling bucket 2 with pairs from supported hash-buckets from bucket 1
            if (support >= supportLevel) {
                for (Map.Entry<HashSet<String>, Integer> innerEntry : entry.getValue().entrySet()) {
                    Integer x = Integer.valueOf(innerEntry.getKey().toArray()[0].toString().substring(6));
                    Integer y = Integer.valueOf(innerEntry.getKey().toArray()[1].toString().substring(6));
                    Integer modulus = hash2(x, y);
                    HashMap<HashSet<String>, Integer> doubletonsCounter;
                    if (!hashBucket2.containsKey(modulus)) {
                        doubletonsCounter = new HashMap<>();
                    } else {
                        doubletonsCounter = hashBucket2.get(modulus);
                    }
                    doubletonsCounter.put(innerEntry.getKey(), innerEntry.getValue());
                    hashBucket2.put(modulus, doubletonsCounter);
                }
            }
        }

        //PASS 3
        HashSet<String> deletedSingletons = new HashSet<>();
        //Check support for singletons
        for (Map.Entry<String, Integer> entry : singletonsCounter.entrySet()) {
            if (entry.getValue() >= supportLevel) {
                HashSet<String> single = new HashSet<>();
                single.add(entry.getKey());
                result.put(single, entry.getValue());
            } else {
                deletedSingletons.add(entry.getKey());
            }
        }
        //Choose from hashBucket 1 with supported level and matching singletons
        func(supportLevel, result, hashBucket1, deletedSingletons);
        //Choose from hashBucket 2 with supported level and matching singletons
        func(supportLevel, result, hashBucket2, deletedSingletons);
        return result;
    }

    private static void func(Integer supportLevel, HashMap<HashSet<String>, Integer> result, HashMap<Integer, HashMap<HashSet<String>, Integer>> hashBucket1, HashSet<String> deletedSingletons) {
        for (Map.Entry<Integer, HashMap<HashSet<String>, Integer>> entry : hashBucket1.entrySet()) {
            Integer support = entry.getValue().values().stream().reduce(0, Integer::sum);
            if (support >= supportLevel) {
                for (Map.Entry<HashSet<String>, Integer> innerEntry : entry.getValue().entrySet()) {
                    if (!deletedSingletons.contains(innerEntry.getKey().toArray()[0].toString()) && !deletedSingletons.contains(innerEntry.getKey().toArray()[1].toString())) {
                        result.put(innerEntry.getKey(), innerEntry.getValue());
                    }
                }
            }
        }
    }

    public static Integer hash1(Integer x, Integer y) {
        return (x + y) % 10000;
    }

    public static Integer hash2(Integer x, Integer y) {
        return (x + 2 * y) % 10000;
    }

    public static HashMap<String, ArrayList<String>> fromCsvToMap(String fileName) {
        HashMap<String, ArrayList<String>> baskets = new HashMap<>();

        try (BufferedReader csvReader = new BufferedReader(new FileReader(fileName))) {
            String row;

            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(";");
                ArrayList<String> products;
                if (!baskets.containsKey(data[1])) {
                    products = new ArrayList<>();
                } else {
                    products = baskets.get(data[1]);
                }
                products.add(data[0]);
                baskets.put(data[1], products);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return baskets;
    }
}

