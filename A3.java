import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher; 
import java.util.regex.Pattern; 
import java.util.Scanner;


public class A3 {
	public static int totalEmails;
    public static Map<String, Set<String>> mailGraph = new HashMap<>(); 

    /*
     * List files for folder function basic idea from online, parse through the files in a given directory, 
     *      in each of those files, locate "from" and "to" (with "cc") emails, calling addEdge to create adjacencyMap. 
     *      Additional info included in README.md
     * @param File
     */
	public static void listFilesForFolder(final File folder) {
        String line;
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            }
            else {
                try {
                    BufferedReader bufferedreader = new BufferedReader(new FileReader(fileEntry.getPath()));
                    String fromAdd = null;
                    String toAdd = null;
                    while ((line = bufferedreader.readLine()) != null) {
                        if (line.startsWith("From: ")) {
                            fromAdd = extractEmail(line);
                        }
                        // not sure 
                        if (line.startsWith("To: ")) {
                            String[] arr = line.split(" ");
                            for (int i = 1; i < arr.length; i++) {
                                toAdd = extractEmail(arr[i]);
                                if (toAdd != null && fromAdd != null) {
                                    addEdge(mailGraph, fromAdd, toAdd);
                                }
                            }
                        }
                        if (line.startsWith("Cc: ")) {
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
	
	
    /*
     * Extract email function provided by David
     * @param String at line 
     * @return complete email string
     */
	public static String extractEmail(String input) {
        Matcher matcher = Pattern.compile("([a-zA-Z0-9.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z0-9._-]+)").matcher(input);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
	}

    /*
     * Add edge structure taken from slides
     * @param adjacencyMap
     * @param each email
     * @param email's neighbor
     */
    public static void addEdge(Map<String, Set<String>> adjacencyMap, String node1, String node2) {
        // Check if the nodes already exist in the map
        if (!adjacencyMap.containsKey(node1)) {
            adjacencyMap.put(node1, new HashSet<>());
        }
        if (!adjacencyMap.containsKey(node2)) {
            adjacencyMap.put(node2, new HashSet<>());
        }

        // Add the edges (undirected graph)
        adjacencyMap.get(node1).add(node2);
        adjacencyMap.get(node2).add(node1); 
    }


    /*
     * print and return Adjacency Map
     * @param global adjacency Map variable
     */
    public static void printAdjMap(Map<String, Set<String>> adjacencyMap){
    for (String node : adjacencyMap.keySet()) {
        Set<String> neighbors = adjacencyMap.get(node);
        System.out.print(node + ": ");
        for (String neighbor : neighbors) {
            System.out.print(neighbor + " ");
        }
        System.out.println();
    }
    }

    /*
     * DFS structure taken from youtube video listed in README
     * parses through adjacencyMap 
     * @param adjMap, 
     * @param vertex (email user), 
     * @param parent (initial user), 
     * @param dfsnum (assigned when a vertex is created), 
     * @param back (same as dfsnum, but subject to change when a new "pointer" is assigned), 
     * @param connectors (set created as we parse through the map), 
     * @param dfsCount
     * 
     */
    private static void dfs(Map<String, Set<String>> adjacencyMap, String vertex, String parent, Map<String,
            Integer> dfsnum, Map<String, Integer> back, Set<String> connectors, int dfsCount) {
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
        } //identify connector and add them to the connectors set
        if ((parent != null && isConnector) || (parent == null && childCount > 1)) {
            connectors.add(vertex);
        }
    }

    /*
     * Identify connections within the HashMap and wirte them to the new connectors file
     * @param AdjacencyMap
     * @param Connectors file
     */
    public static void findConnectors(Map<String, Set<String>> adjacencyMap, String outputFileName) {
        Set<String> connectors = new HashSet<>();
        Map<String, Integer> dfsnum = new HashMap<>();
        Map<String, Integer> back = new HashMap<>();

        for (String vertex : adjacencyMap.keySet()) {
            if (!dfsnum.containsKey(vertex)) {
                dfs(adjacencyMap, vertex, null, dfsnum, back, connectors, 1);
            }
        }
        System.out.println("Total number of connectors in this file are "+ connectors.size());
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

   /*
    * Req 3: 
    use the set thats corresponds to the key email address, team would be the sizze of that set
    - use the connectors function to help identify if hteyre on the same team 
    */

    public static findEmailInfo(String userInput){
        //for each erpson in the adjacency map, calculate how many people they have sent and recieved mail from 
        // in order to do so, when from is calculated above
    }


	public static void main(String[] args) {
		// change to command line args when finished -- sends uncompressed maildir folder to the program
		final File folder = new File("/Users/jennaviev/Desktop/CS245/Assignment3/maildir");
		listFilesForFolder(folder);
		
        /* for testing */
        //printAdjMap(mailGraph); 
        
        String connectors = new String("connectors.txt");
        findConnectors(mailGraph,connectors);
        
        
        
        //for testing REQ 3
        Scanner scanner= new Scanner(System.in); 
        System.out.println("Email address of the individual (or EXIT to quit):"); 
        String email = scanner.nextLine();
        
        
	}
}





