import lombok.Data;

@Data
public class BloomFilter {
    private int[] array;
    private final int k;

    public BloomFilter(int n, int k) {
        this.array = new int[n];
        this.k = k;
    }

    public void add(String word) {
        for (int i = 1; i <= this.k; i++) {
            int pos = (int) Math.abs((((long) word.hashCode() * 3 * i + 14 * Math.log(i)) % array.length));
            this.array[pos]++;
        }
    }

    public void delete(String word) {
        for (int i = 1; i <= this.k; i++) {
            int pos = (int) Math.abs((((long) word.hashCode() * 3 * i + 14 * Math.log(i)) % array.length));
            this.array[pos]--;
        }
    }

    public boolean contains(String word) {
        for (int i = 1; i <= this.k; i++) {
            int pos = (int) Math.abs((((long) word.hashCode() * 3 * i + 14 * Math.log(i)) % array.length));
            if (this.array[pos] == 0) {
                System.out.println("Set doesn't contains word: " + word);
                return false;
            }
        }
        System.out.println("Set contains word " + word + " with 90% probability: ");
        return true;
    }
}
