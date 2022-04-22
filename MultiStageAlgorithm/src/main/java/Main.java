import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        HashMap<String, ArrayList<String>> baskets = new HashMap<>();
        try {
            String row = "";
            BufferedReader csvReader = new BufferedReader(new FileReader("transactions.csv"));
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
            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        HashMap<String, Integer> singletons = new HashMap<>();
        System.out.println(baskets.toString());
    }
}

