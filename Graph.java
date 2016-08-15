/**
  * Graph.java
  * @author Jonathon Elfar
  * @author Jacob Copus
  * 
  * @version 12/5/13
  *
  * Graph ADT for building an undirected graph
  * implementing breadth-first traversal
  * and "packet-decay" expiration.
  */
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Collections;
import java.util.Queue;

/** Undirected Graph ADT.
* Vertices are labeled with uppercase alphabetic characters, and
* there is a maximum of 26 nodes.
*/
public class Graph {
    private static final int SIZE_OF_GRAPH = 26;
    private static char[] LUT;

    /**
      * Represents a vertex in the graph.
      * Contains connections, known boolean, and 
      * distance from a selected vertex(traverse).
      */
    private class Vertex {
        public LinkedList<Character> connections;
        public boolean known;
        public int dist;
        
        public Vertex() {
            connections = new LinkedList<Character>();
            known = false;
            dist = -1;
        }

        public char getChar(int index) { return connections.get(index); }

        public boolean contains(char c) { return connections.contains(c); }

        public void add(char c) { connections.add(c); }

        public int size() { return connections.size(); }

        public boolean isEmpty() { return connections.isEmpty(); }
    }
    
    private Vertex[] graph;

    /** Construct an empty graph */
    public Graph() {
        graph = new Vertex[SIZE_OF_GRAPH];
        for(int i = 0; i < graph.length; i++)
            graph[i] = new Vertex();

        LUT = new char[SIZE_OF_GRAPH];

        for(int x = 0; x < LUT.length; x++) {
            LUT[x] = (char)('A' + x);
        }
    }

    /** Read a sequence of token pairs that represent a graph.
    * The first token is a character representing a vertex,
    * The second token is a string of characters, each of which is
    * connected by an edge to the first character.
    * E.g. " A BC B CADE C AB D B E B "
    * @param graph is a string containing the input sequence of token pairs.
    * @pre the input sequence is not empty
    * @pre the input sequence has an even number of tokens
    * @pre the input sequence contains only uppercase letters
    * @post the graph is constructed. build() is smart enough to add
    * implied "secondary" edges. Thus "A BC" creates edges to B and C
    * from A, as well as new nodes B and C each with an edge to A.
    * Duplicates are ignored, thus "A BC B A C A" is the same as "A BC"
    */
    public void build(String input) {
        Scanner graphScanner = new Scanner(input);
        while(graphScanner.hasNext()) {
            char vertex = graphScanner.next().charAt(0);
            String connectionsStr = graphScanner.next();

            int graphIndex = vertex - 'A';

            for(int x = 0; x < connectionsStr.length(); x++) {
                char curChar = connectionsStr.charAt(x);
                int curCharIndex = curChar - 'A';
                if(!graph[graphIndex].contains(curChar))
                    graph[graphIndex].add(curChar);

                if(!graph[curCharIndex].contains(vertex))
                    graph[curCharIndex].add(vertex);
            }
        }
        System.out.println(this);
    }

    /**
    * Returns the number of vertices of this graph.
    * @return int number of vertices in the graph
    */
    public int countVertices() {
        int count = 0;
        for(int i = 0; i < graph.length; i++) {
            if(graph[i].size() != 0) count++;
        }

        return count;
    }

    /**
    * Return a string representation of the neighbors of the specified node.
    * @param node the vertex for whom we want the neighboring nodes.
    * @pre node exists in the graph
    * @return String containing letters of neighbors in alphabetical order with no embedded blanks.
    */
    public String getNeighbors(Character node) {
        String result = "";
        int index = node - 'A';
        for(int x = 0; x < graph[index].size(); x++)
            result += graph[index].getChar(x);

        return result;
    }

    /**
    * Breadth-first traverse of this graph.
    * @param startVertex the node from which the traverse is begun
    * @pre start exists in the graph
    * @return String list of nodes in the order visited
    */
    public String traverse(char startVertex) {
        for(int i = 0; i < graph.length; i++) {
            graph[i].dist = -1;
            graph[i].known = false;
        }

        Queue<Character> queue = new LinkedList<Character>();
        String result = "";
        int index = startVertex - 'A';

        graph[index].known = true;
        graph[index].dist = 0;
        queue.add(startVertex);

        while(!queue.isEmpty()) {
            Character curChar = queue.poll();
            int curIndex = curChar - 'A';

            Vertex curVertex = graph[curIndex];
            result += LUT[curIndex];
            LinkedList<Character> connections = curVertex.connections;
            Collections.sort(connections);
        
            for(int i = 0; i < connections.size(); i++) {
                Character curConnectionChar = connections.get(i);
                int curConnectionIndex = curConnectionChar - 'A';
                Vertex curConnectionVertex = graph[curConnectionIndex];

                if(!curConnectionVertex.known) {
                    curConnectionVertex.known = true;
                    curConnectionVertex.dist = curVertex.dist + 1;
                    queue.add(LUT[curConnectionIndex]);
                }
            }
        }
        return result;
    }

    /**
    * Return a string representation of the nodes at each integer distance
    * from the start node in the most recent call to traverse().
    * @pre traverse() has been called for this graph.
    * @return String containing formatted list of distances and nodes.
    * Distances start at one and increase by one. A colon separates the
    * distance from the list of nodes at that distance from the start
    * node. Nodes at each distance are listed alphabetically.
    * For example,
    * 1:BCD 2:EFH 3:GJ 4:Z
    */
    public String getDistances() {
        String[] table = new String[countVertices()];
        for(int i = 0; i < table.length; i++)
            table[i] = new String();

        String result = "";

        for(int i = 0; i < graph.length; i++) {
            if(graph[i].size() > 0) {
                table[graph[i].dist] += LUT[i]; 
            }
        }

        for(int i = 1; i < table.length; i++) {
            if(table[i].length() > 0)
                result += i + ":" + table[i] + " ";
        }

        return result;
    }

    /** Input queries.
    * @param start starting node
    * @param expiration limit (number of hops)
    * @pre assumes limit of 26 nodes
    * @pre start is uppercase alphabetic character existing in the graph
    * @pre expiration > 0
    * @return int count of nodes that can't be reached before expiring
    */
    public int unreachableNodes(char start, int expiration) {
        int count = 0;
        traverse(start);

        for(int i = 0; i < graph.length; i++) {
            if(graph[i].dist > expiration) count++;
        }

        return count;
    }

    /** Process a network description is given on one line as a sequence of token
    * pairs. The first token is a character representing a node, and the second
    * token is a string of characters, each of which is connected by a communication
    * line to the first node. Prints out unreachable nodes as shown in the Sample
    * Output above.
    * @param str Sequence of token pairs. For example, F 2 F 3 H 2
    * @pre the token pair entries are in the format shown
    */
    public void calcExp(String str) {
        Scanner lineScanner = new Scanner(str);
        while(lineScanner.hasNext()) {
            char curChar = lineScanner.next().charAt(0);
            int curExp = lineScanner.nextInt();
            
            System.out.println("Can't reach " + unreachableNodes(curChar, curExp) +
                               " nodes starting at " + curChar + " with expiration of " +
                               curExp);
        }
    }

    /**
    * Return a string representation of this graph.
    * The string is formatted in the style of Map.toString(), e.g.,
    * {A=[B, C, D], B=[A], C=[A], D=[A]}
    * The nodes appear in alphabetical order.
    * @return string representation of this graph
    */
    public String toString() {
        String result = "{";

        for(int i = 0; i < graph.length; i++) {
            if(graph[i].size() > 0) {
                LinkedList<Character> list = graph[i].connections;
                Collections.sort(list);
                result += LUT[i] + "=[";
                for(int x = 0; x < list.size(); x++) {
                    char curChar = list.get(x);
                    if(x == list.size()-1)
                        result += curChar + "], ";
                    else
                        result += curChar + ", ";
                }
            }
        }
        result = result.substring(0, result.length()-2); //Remove last , and space for formatting
        result += "}";
        return result;
    }
}
