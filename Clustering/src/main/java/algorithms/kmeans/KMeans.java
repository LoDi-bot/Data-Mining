package algorithms.kmeans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Encapsulates an implementation of KMeans clustering algorithm.
 *
 * @author LoDi-BoT
 */
public class KMeans {

    private KMeans() {
        throw new IllegalAccessError("You shouldn't call this constructor");
    }

    /**
     * Will be used to generate random numbers.
     */
    private static final Random random = new Random();

    /**
     * Performs the K-Means clustering algorithm on the given dataset.
     *
     * @param records       The dataset.
     * @param k             Number of Clusters.
     * @param distance      To calculate the distance between two items.
     * @param maxIterations Upper bound for the number of iterations.
     * @return K clusters along with their records.
     */
    public static Map<Centroid, List<Record>> fit(List<Record> records, int k, Distance distance, int maxIterations) {
        applyPreconditions(records, k, distance, maxIterations);

        List<Centroid> centroids = randomCentroids(records, k, distance);
        Map<Centroid, List<Record>> tempClusters = new HashMap<>();
        centroids.forEach(centroid -> assignToCluster(tempClusters, new Record(centroid.getCoordinates()), centroid));
        Map<Centroid, List<Record>> clusters = new HashMap<>(tempClusters);
        Map<Centroid, List<Record>> lastState = new HashMap<>();

        // iterate for a pre-defined number of times
        for (int i = 0; i < maxIterations; i++) {
            boolean isLastIteration = i == maxIterations - 1;

            // in each iteration we should find the nearest centroid for each record
            for (Record record : records) {
                Centroid nearestCentroid = nearestCentroid(record, centroids, distance);
                assignToCluster(clusters, record, nearestCentroid);
            }

            // if the assignment does not change, then the algorithm terminates
            boolean shouldTerminate = isLastIteration || clusters.equals(lastState);
            lastState = clusters;
            if (shouldTerminate) {
                break;
            }

            // at the end of each iteration we should relocate the centroids
            centroids = relocateCentroids(clusters);
            clusters = new HashMap<>();
        }

        return lastState;
    }

    /**
     * Move all cluster centroids to the average of all assigned features.
     *
     * @param clusters The current cluster configuration.
     * @return Collection of new and relocated centroids.
     */
    private static List<Centroid> relocateCentroids(Map<Centroid, List<Record>> clusters) {
        return clusters
                .entrySet()
                .stream()
                .map(entry -> average(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Moves the given centroid to the average position of all assigned features. If
     * the centroid has no feature in its cluster, then there would be no need for a
     * relocation. Otherwise, for each entry we calculate the average of all records
     * first by summing all the entries and then dividing the final summary value by
     * the number of records.
     *
     * @param centroid The centroid to move.
     * @param records  The assigned features.
     * @return The moved centroid.
     */
    private static Centroid average(Centroid centroid, List<Record> records) {
        // if this cluster is empty, then we shouldn't move the centroid
        if (records == null || records.isEmpty()) {
            return centroid;
        }

        // Since some records don't have all possible attributes, we initialize
        // average coordinates equal to current centroid coordinates
        Map<String, Double> average = centroid.getCoordinates();

        // The average function works correctly if we clear all coordinates corresponding
        // to present record attributes
        records.stream()
                .flatMap(record -> record
                        .getFeatures()
                        .keySet()
                        .stream())
                .forEach(k -> average.put(k, 0.0));

        for (Record record : records) {
            record
                    .getFeatures()
                    .forEach((k, v) -> average.compute(k, (k1, currentValue) -> v + currentValue));
        }

        average.forEach((k, v) -> average.put(k, v / records.size()));

        return new Centroid(average);
    }

    /**
     * Assigns a feature vector to the given centroid. If this is the first assignment for this centroid,
     * first we should create the list.
     *
     * @param clusters The current cluster configuration.
     * @param record   The feature vector.
     * @param centroid The centroid.
     */
    private static void assignToCluster(Map<Centroid, List<Record>> clusters, Record record, Centroid centroid) {
        clusters.compute(centroid, (key, list) -> {
            if (list == null) {
                list = new ArrayList<>();
            }

            list.add(record);
            return list;
        });
    }

    /**
     * With the help of the given distance calculator, iterates through centroids and finds the
     * nearest one to the given record.
     *
     * @param record    The feature vector to find a centroid for.
     * @param centroids Collection of all centroids.
     * @param distance  To calculate the distance between two items.
     * @return The nearest centroid to the given feature vector.
     */
    private static Centroid nearestCentroid(Record record, List<Centroid> centroids, Distance distance) {
        double minimumDistance = Double.MAX_VALUE;
        Centroid nearestCentroid = null;

        for (Centroid centroid : centroids) {
            double currentDistance = distance.calculate(record.getFeatures(), centroid.getCoordinates());

            if (currentDistance < minimumDistance) {
                minimumDistance = currentDistance;
                nearestCentroid = centroid;
            }
        }

        return nearestCentroid;
    }

    /**
     * Generates k centroids, by picking points that are as far away from one another as possible
     *
     * @param records The dataset.
     * @param k       Number of clusters.
     * @return Collection of generated centroids.
     */
    private static List<Centroid> randomCentroids(List<Record> records, int k, Distance distance) {
        List<Centroid> centroids = new ArrayList<>();
        int rand = random.nextInt(0, records.size());
        centroids.add(new Centroid(records.get(rand).getFeatures()));
        records.remove(rand);

        while (centroids.size() < k) {
            double max = Double.MIN_VALUE;
            Record point = null;

            for (Record record : records) {
                // Minimum distance from the selected centroids
                double dist = centroids.stream()
                        .map(centroid -> distance.calculate(record.getFeatures(), centroid.getCoordinates()))
                        .sorted()
                        .findFirst()
                        .orElseThrow(IllegalArgumentException::new);

                if (dist > max) {
                    max = dist;
                    point = record;
                }
            }

            assert point != null;
            centroids.add(new Centroid(point.getFeatures()));
            records.remove(point);
        }

        return centroids;
    }

    private static void applyPreconditions(List<Record> records, int k, Distance distance, int maxIterations) {
        if (records == null || records.isEmpty()) {
            throw new IllegalArgumentException("The dataset can't be empty");
        }

        if (k <= 1) {
            throw new IllegalArgumentException("It doesn't make sense to have less than or equal to 1 cluster");
        }

        if (distance == null) {
            throw new IllegalArgumentException("The distance calculator realisation is required");
        }

        if (maxIterations <= 0) {
            throw new IllegalArgumentException("Max iterations should be a positive number");
        }
    }
}