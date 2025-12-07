import java.io.*;
import java.util.*;

/**
 * Product Ordering by Feature Similarity
 * 
 * Orders products so that each product learned is most similar to previously
 * learned products, maximizing the benefit of tree and alphabet reuse.
 * 
 * Strategy:
 * 1. Read features from .config files
 * 2. Start with product 0 (arbitrary)
 * 3. For each next product, select the one with highest similarity to learned products
 * 4. Similarity = number of shared features / total unique features (Jaccard similarity)
 */
public class ProductOrderingBySimilarity {
    
    /**
     * Represents a product with its features
     */
    public static class Product {
        public String fileName;      // e.g., "00001_fsm.dot"
        public String configFile;    // e.g., "00001.config"
        public Set<String> features; // Features from config file
        public int originalIndex;    // Original position in array
        
        public Product(String fileName, int index) {
            this.fileName = fileName;
            this.originalIndex = index;
            this.features = new HashSet<>();
            
            // Extract config file name
            this.configFile = fileName.replace("_fsm.dot", ".config");
        }
        
        @Override
        public String toString() {
            return fileName + " (features: " + features.size() + ")";
        }
    }
    
    /**
     * Read features from a .config file
     */
    public static Set<String> readFeaturesFromConfig(File configFile) throws IOException {
        Set<String> features = new HashSet<>();
        
        if (!configFile.exists()) {
            System.err.println("WARNING: Config file not found: " + configFile.getAbsolutePath());
            return features;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    features.add(line);
                }
            }
        }
        
        return features;
    }
    
    /**
     * Calculate Jaccard similarity between two feature sets
     * Jaccard = |A ∩ B| / |A ∪ B|
     * Returns value between 0 (no overlap) and 1 (identical)
     */
    public static double calculateJaccardSimilarity(Set<String> features1, Set<String> features2) {
        if (features1.isEmpty() && features2.isEmpty()) {
            return 1.0; // Both empty = identical
        }
        
        // Calculate intersection
        Set<String> intersection = new HashSet<>(features1);
        intersection.retainAll(features2);
        
        // Calculate union
        Set<String> union = new HashSet<>(features1);
        union.addAll(features2);
        
        if (union.isEmpty()) {
            return 0.0;
        }
        
        return (double) intersection.size() / union.size();
    }
    
    /**
     * Calculate maximum similarity between a product and a set of learned products
     */
    public static double calculateMaxSimilarity(Product candidate, List<Product> learnedProducts) {
        double maxSim = 0.0;
        
        for (Product learned : learnedProducts) {
            double sim = calculateJaccardSimilarity(candidate.features, learned.features);
            maxSim = Math.max(maxSim, sim);
        }
        
        return maxSim;
    }
    
    /**
     * Calculate average similarity between a product and all learned products
     */
    public static double calculateAverageSimilarity(Product candidate, List<Product> learnedProducts) {
        if (learnedProducts.isEmpty()) {
            return 0.0;
        }
        
        double totalSim = 0.0;
        for (Product learned : learnedProducts) {
            totalSim += calculateJaccardSimilarity(candidate.features, learned.features);
        }
        
        return totalSim / learnedProducts.size();
    }
    
    /**
     * Order products by feature similarity using greedy algorithm
     * 
     * @param directory Directory containing .config files
     * @param productFiles Array of product file names (e.g., ["00001_fsm.dot", "00002_fsm.dot", ...])
     * @return Ordered array of product file names
     */
    public static String[] orderProductsBySimilarity(String directory, String[] productFiles) throws IOException {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║     ORDERING PRODUCTS BY FEATURE SIMILARITY                ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        
        // Step 1: Load all products and their features
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < productFiles.length; i++) {
            Product product = new Product(productFiles[i], i);
            File configFile = new File(directory, product.configFile);
            product.features = readFeaturesFromConfig(configFile);
            products.add(product);
            
            System.out.println("\nProduct " + i + ": " + productFiles[i]);
            System.out.println("  Config: " + product.configFile);
            System.out.println("  Features (" + product.features.size() + "):");
            for (String feature : product.features) {
                System.out.println("    - " + feature);
            }
        }
        
        // Step 2: Greedy ordering algorithm
        List<Product> orderedProducts = new ArrayList<>();
        List<Product> remainingProducts = new ArrayList<>(products);
        
        // Start with the first product (arbitrary choice)
        Product first = remainingProducts.remove(0);
        orderedProducts.add(first);
        
        System.out.println("\n════════════════════════════════════════════════════════════");
        System.out.println("ORDERING ALGORITHM (Greedy - Maximum Similarity)");
        System.out.println("════════════════════════════════════════════════════════════");
        System.out.println("Starting with: " + first.fileName);
        
        // Iteratively select the most similar remaining product
        while (!remainingProducts.isEmpty()) {
            Product bestCandidate = null;
            double bestSimilarity = -1.0;
            
            // Find product with highest similarity to learned products
            for (Product candidate : remainingProducts) {
                // Use average similarity to all learned products
                double similarity = calculateAverageSimilarity(candidate, orderedProducts);
                
                if (similarity > bestSimilarity) {
                    bestSimilarity = similarity;
                    bestCandidate = candidate;
                }
            }
            
            // Add best candidate to ordered list
            orderedProducts.add(bestCandidate);
            remainingProducts.remove(bestCandidate);
            
            // Calculate shared features with most similar learned product
            Product mostSimilar = null;
            double maxSim = 0.0;
            int sharedFeatures = 0;
            
            for (Product learned : orderedProducts) {
                if (learned == bestCandidate) continue;
                double sim = calculateJaccardSimilarity(bestCandidate.features, learned.features);
                if (sim > maxSim) {
                    maxSim = sim;
                    mostSimilar = learned;
                    Set<String> intersection = new HashSet<>(bestCandidate.features);
                    intersection.retainAll(learned.features);
                    sharedFeatures = intersection.size();
                }
            }
            
            System.out.println("\nStep " + orderedProducts.size() + ": Selected " + bestCandidate.fileName);
            System.out.println("  Average similarity: " + String.format("%.3f", bestSimilarity));
            if (mostSimilar != null) {
                System.out.println("  Most similar to: " + mostSimilar.fileName + " (Jaccard=" + String.format("%.3f", maxSim) + ")");
                System.out.println("  Shared features: " + sharedFeatures + "/" + bestCandidate.features.size());
            }
        }
        
        // Step 3: Create ordered array
        String[] orderedFiles = new String[productFiles.length];
        for (int i = 0; i < orderedProducts.size(); i++) {
            orderedFiles[i] = orderedProducts.get(i).fileName;
        }
        
        // Step 4: Print final ordering
        System.out.println("\n════════════════════════════════════════════════════════════");
        System.out.println("FINAL PRODUCT ORDER");
        System.out.println("════════════════════════════════════════════════════════════");
        for (int i = 0; i < orderedFiles.length; i++) {
            Product p = orderedProducts.get(i);
            System.out.println(i + ". " + orderedFiles[i] + " (" + p.features.size() + " features)");
        }
        
        // Step 5: Calculate expected benefit
        System.out.println("\n════════════════════════════════════════════════════════════");
        System.out.println("EXPECTED ADAPTIVE LEARNING BENEFIT");
        System.out.println("════════════════════════════════════════════════════════════");
        
        double totalSimilarity = 0.0;
        for (int i = 1; i < orderedProducts.size(); i++) {
            List<Product> previous = orderedProducts.subList(0, i);
            double avgSim = calculateAverageSimilarity(orderedProducts.get(i), previous);
            totalSimilarity += avgSim;
            System.out.println("Product " + i + " → avg similarity to previous: " + String.format("%.3f", avgSim));
        }
        
        double avgBenefit = totalSimilarity / (orderedProducts.size() - 1);
        System.out.println("\nOverall average similarity: " + String.format("%.3f", avgBenefit));
        System.out.println("Expected query reduction: " + String.format("%.0f", avgBenefit * 60) + "-" + String.format("%.0f", avgBenefit * 80) + "%");
        System.out.println("════════════════════════════════════════════════════════════\n");
        
        return orderedFiles;
    }
    
    /**
     * Analyze similarity matrix for all products
     */
    public static void printSimilarityMatrix(String directory, String[] productFiles) throws IOException {
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < productFiles.length; i++) {
            Product product = new Product(productFiles[i], i);
            File configFile = new File(directory, product.configFile);
            product.features = readFeaturesFromConfig(configFile);
            products.add(product);
        }
        
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║            PRODUCT SIMILARITY MATRIX (Jaccard)             ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");
        
        // Print header
        System.out.print("       ");
        for (int i = 0; i < products.size(); i++) {
            System.out.printf("  P%-2d ", i);
        }
        System.out.println();
        
        // Print matrix
        for (int i = 0; i < products.size(); i++) {
            System.out.printf("P%-2d    ", i);
            for (int j = 0; j < products.size(); j++) {
                if (i == j) {
                    System.out.print(" 1.00 ");
                } else {
                    double sim = calculateJaccardSimilarity(products.get(i).features, products.get(j).features);
                    System.out.printf(" %.2f ", sim);
                }
            }
            System.out.println(" " + products.get(i).fileName);
        }
        System.out.println();
    }
}

