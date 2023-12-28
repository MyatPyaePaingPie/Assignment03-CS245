import java.io.*;
import java.util.*;

public class A3 {
    public static int totalEmails;
    public static Map<String, Set<String>> mailGraph = new HashMap<>();

    // Function to list files in a folder and process each file to extract email information
    public static void listFilesForFolder(final File folder) {
        String line;
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                try {
                    // Read each line in the file and extract "From", "To", and "Cc" email addresses
                    BufferedReader bufferedreader = new BufferedReader(new FileReader(fileEntry.getPath()));
                    String fromAdd = null;
                    String toAdd = null;
                    while ((line = bufferedreader.readLine()) != null) {
                        if (line.startsWith("From: ")) {
                            fromAdd = extractEmail(line);
                        }
                        if (line.startsWith("To: ") || line.startsWith("Cc: ")) {
                            String[] arr = line.split(" ");
                            for (int i = 1; i < arr.length; i++) {
                                toAdd = extractEmail(arr[i]);
                                if (toAdd != null && fromAdd != null) {
                                    addEdge(mailGraph, fromAdd, toAdd);
                                }
                            }
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    // Function to extract email from a given line
    public static String extractEmail(String input) {
        Matcher matcher = Pattern.compile("([a-zA-Z0-9.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z0-9._-]+)").matcher(input);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    // Function to add an edge between two nodes in the graph
    public static void addEdge(Map<String, Set<String>> adjacencyMap, String node1, String node2) {
        if (!adjacencyMap.containsKey(node1)) {
            adjacencyMap.put(node1, new HashSet<>());
        }
        if (!adjacencyMap.containsKey(node2)) {
            adjacencyMap.put(node2, new HashSet<>());
        }
        adjacencyMap.get(node1).add(node2);
        adjacencyMap.get(node2).add(node1);
    }

    // Function to print the adjacency map
    public static void printAdjMap(Map<String, Set<String>> adjacencyMap) {
        for (String node : adjacencyMap.keySet()) {
            Set<String> neighbors = adjacencyMap.get(node);
            System.out.print(node + ": ");
            for (String neighbor : neighbors) {
                System.out.print(neighbor + " ");
            }
            System.out.println();
        }
    }

    // Depth-First Search (DFS) to find connectors in the graph
    private static void dfs(Map<String, Set<String>> adjacencyMap, String vertex, String parent,
                            Map<String, Integer> dfsnum, Map<String, Integer> back,
                            Set<String> connectors, int dfsCount) {
        dfsnum.put(vertex, dfsCount);
        back.put(vertex, dfsCount);
        dfsCount++;
        int childCount = 0;
        boolean isConnector = false;
        Set<String> neighbors = adjacencyMap.get(vertex);

        if (neighbors != null) {
            for (String neighbor : neighbors) {
                if (neighbor.equals(parent)) {
                    continue;
                }
                if (!dfsnum.containsKey(neighbor)) {
                    dfs(adjacencyMap, neighbor, vertex, dfsnum, back, connectors, dfsCount);
                    childCount++;
                    if (dfsnum.get(vertex) <= back.get(neighbor)) {
                        isConnector = true;
                    } else {
                        back.put(vertex, Math.min(back.get(vertex), back.get(neighbor)));
                    }
                } else {
                    back.put(vertex, Math.min(back.get(vertex), dfsnum.get(neighbor)));
                }
            }
        }
        if ((parent != null && isConnector) || (parent == null && childCount > 1)) {
            connectors.add(vertex);
        }
    }

    // Function to find connectors and write them to a file
    public static void findConnectors(Map<String, Set<String>> adjacencyMap, String outputFileName) {
        Set<String> connectors = new HashSet<>();
        Map<String, Integer> dfsnum = new HashMap<>();
        Map<String, Integer> back = new HashMap<>();

        for (String vertex : adjacencyMap.keySet()) {
            if (!dfsnum.containsKey(vertex)) {
                dfs(adjacencyMap, vertex, null, dfsnum, back, connectors, 1);
            }
        }
        System.out.println("Total number of connectors in this file are " + connectors.size());
        try {
            FileWriter writer = null;
            if (outputFileName != null) {
                try {
                    writer = new FileWriter(outputFileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (writer != null) {
                for (String connector : connectors) {
                    System.out.println(connector);
                    writer.write(connector + "\n");
                }
                writer.close();
            } else {
                for (String connector : connectors) {
                    System.out.println(connector);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Function to find email information for a given user
    public static void findEmailInfo(String userInput) {
        // Implementation required for REQ 3
    }

    public static void main(String[] args) {
        // Change to command line args when finished -- sends uncompressed maildir folder to the program
        final String folderPath = "Users/myatp/OneDrive/Desktop/CS 245/Assignment03/enron_mail_20150507/maildir";
        final File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Error: The specified folder does not exist or is not a directory.");
            return;
        }

        listFilesForFolder(folder);

        /* for testing */
        // printAdjMap(mailGraph);

        String connectors = new String("connectors.txt");
        findConnectors(mailGraph, connectors);

        // For testing REQ 3
        Scanner scanner = new Scanner(System.in);
        System.out.println("Email address of the individual (or EXIT to quit):");
        String email = scanner.nextLine();
    }
}