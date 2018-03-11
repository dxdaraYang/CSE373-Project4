package misc.graphs;

import java.util.Iterator;

import datastructures.concrete.ArrayDisjointSet;
import datastructures.concrete.ArrayHeap;
import datastructures.concrete.ChainedHashSet;
import datastructures.concrete.DoubleLinkedList;
import datastructures.concrete.KVPair;
import datastructures.concrete.dictionaries.ChainedHashDictionary;
import datastructures.interfaces.IDisjointSet;
import datastructures.interfaces.IList;
import datastructures.interfaces.ISet;
import misc.Searcher;
import misc.exceptions.NoPathExistsException;

/**
 * Represents an undirected, weighted graph, possibly containing self-loops, parallel edges,
 * and unconnected components.
 *
 * Note: This class is not meant to be a full-featured way of representing a graph.
 * We stick with supporting just a few, core set of operations needed for the
 * remainder of the project.
 */
public class Graph<V, E extends Edge<V> & Comparable<E>> {
    // NOTE 1:
    //
    // Feel free to add as many fields, private helper methods, and private
    // inner classes as you want.
    //
    // And of course, as always, you may also use any of the data structures
    // and algorithms we've implemented so far.
    //
    // Note: If you plan on adding a new class, please be sure to make it a private
    // static inner class contained within this file. Our testing infrastructure
    // works by copying specific files from your project to ours, and if you
    // add new files, they won't be copied and your code will not compile.
    //
    //
    // NOTE 2:
    //
    // You may notice that the generic types of Graph are a little bit more
    // complicated then usual.
    //
    // This class uses two generic parameters: V and E.
    //
    // - 'V' is the type of the vertices in the graph. The vertices can be
    //   any type the client wants -- there are no restrictions.
    //
    // - 'E' is the type of the edges in the graph. We've contrained Graph
    //   so that E *must* always be an instance of Edge<V> AND Comparable<E>.
    //
    //   What this means is that if you have an object of type E, you can use
    //   any of the methods from both the Edge interface and from the Comparable
    //   interface
    //
    // If you have any additional questions about generics, or run into issues while
    // working with them, please ask ASAP either on Piazza or during office hours.
    //
    // Working with generics is really not the focus of this class, so if you
    // get stuck, let us know we'll try and help you get unstuck as best as we can.

    private ChainedHashDictionary<Integer, ChainedHashSet<V>> AList;
    private IList<V> vertices;
    private IList<E> edges;
    
    /**
     * Constructs a new graph based on the given vertices and edges.
     *
     * @throws IllegalArgumentException  if any of the edges have a negative weight
     * @throws IllegalArgumentException  if one of the edges connects to a vertex not
     *                                   present in the 'vertices' list
     */
    public Graph(IList<V> vertices, IList<E> edges) {
        for (E edge : edges) {
            if (edge.getWeight() < 0 || !vertices.contains(edge.getVertex1()) ||
                    !vertices.contains(edge.getVertex2())) {
                throw new IllegalArgumentException();
            }
        }
        this.vertices = vertices;
        this.edges = edges;
        this.AList = new ChainedHashDictionary<>();
        for (V vertex : vertices) {
            AList.put(vertices.indexOf(vertex), new ChainedHashSet<V>());
        }
        for (E edge : edges) {
            AList.get(vertices.indexOf(edge.getVertex1())).add(edge.getVertex2());
        }
    }

    /**
     * Sometimes, we store vertices and edges as sets instead of lists, so we
     * provide this extra constructor to make converting between the two more
     * convenient.
     */
    public Graph(ISet<V> vertices, ISet<E> edges) {
        // You do not need to modify this method.
        this(setToList(vertices), setToList(edges));
    }

    // You shouldn't need to call this helper method -- it only needs to be used
    // in the constructor above.
    private static <T> IList<T> setToList(ISet<T> set) {
        IList<T> output = new DoubleLinkedList<>();
        for (T item : set) {
            output.add(item);
        }
        return output;
    }

    /**
     * Returns the number of vertices contained within this graph.
     */
    public int numVertices() {
        return vertices.size();
    }

    /**
     * Returns the number of edges contained within this graph.
     */
    public int numEdges() {
        return edges.size();
    }

    /**
     * Returns the set of all edges that make up the minimum spanning tree of
     * this graph.
     *
     * If there exists multiple valid MSTs, return any one of them.
     *
     * Precondition: the graph does not contain any unconnected components.
     */
    public ISet<E> findMinimumSpanningTree() {
        IDisjointSet<V> mstSet = new ArrayDisjointSet<>();
        
        for (V vertex : vertices) {
            mstSet.makeSet(vertex);
        }
        
        ISet<E> mst = new ChainedHashSet<E>();
        IList<E> top = Searcher.topKSort(edges.size(), edges);
        
        for (E edge : top) {
            V vertex1 = edge.getVertex1();
            V vertex2 = edge.getVertex2();
            if (mstSet.findSet(vertex1) != mstSet.findSet(vertex2)) {
                mstSet.union(vertex1, vertex2);
                mst.add(edge);
            }
        }
        
        return mst;
    }

    /**
     * Returns the edges that make up the shortest path from the start
     * to the end.
     *
     * The first edge in the output list should be the edge leading out
     * of the starting node; the last edge in the output list should be
     * the edge connecting to the end node.
     *
     * Return an empty list if the start and end vertices are the same.
     *
     * @throws NoPathExistsException  if there does not exist a path from the start to the end
     */
    public IList<E> findShortestPathBetween(V start, V end) {
        if (start.equals(end)) {
            return new DoubleLinkedList<E>();
        }
        boolean exist = false;
        for (E edge : edges) {
            if ((edge.getVertex1().equals(start) && edge.getVertex2().equals(end)) ||
                    (edge.getVertex2().equals(start) && edge.getVertex1().equals(end))) {
                exist = true;
                break;
            }
        }
        if (!exist) {
            throw new NoPathExistsException();
        }
        
        /*ISet<E> path = new ChainedHashSet<E>();
        ChainedHashDictionary<V, Double> costs = new ChainedHashDictionary<>();
        ISet<V> visited = new ChainedHashSet<V>();
        for (V vertex : vertices) {
            costs.put(vertex, Double.POSITIVE_INFINITY);
        }
        
        ArrayHeap<Double> heap = new ArrayHeap<>();
        heap.insert(0.0);
        costs.put(start, 0.0);
        while (!heap.isEmpty()) {
            Double currentCost = heap.removeMin();
            V current = costs.iterator().next().getKey();
            for (KVPair<V, Double> costPair : costs) {
                if (costPair.getValue() == currentCost) {
                    current = costPair.getKey();
                }
            }
            if (!visited.contains(current)) {
                visited.add(current);
            }
            
            ISet<E> visitable = new ChainedHashSet<E>();
            for (V vertex : AList.get(vertices.indexOf(current))) {
                for (E edge : edges) {
                    if ((edge.getVertex1().equals(current) && edge.getVertex2().equals(vertex)) ||
                        (edge.getVertex2().equals(current) && edge.getVertex1().equals(vertex))) {
                        visitable.add(edge);
                    }
                }
            }
            for (E edge : visitable) {
                if (!path.contains(edge)) {
                    V target = edge.getOtherVertex(current);
                    double newCost = Math.min(costs.get(current)+edge.getWeight(), costs.get(target));
                    costs.put(target, newCost);
                    path.add(edge);
                    heap.insert(newCost);
                }
            }
        }
        return setToList(path);*/
        
       /* ChainedHashDictionary<V, Double> costs = new ChainedHashDictionary<>();
        ISet<E> path = new ChainedHashSet<E>();
        ISet<V> visited = new ChainedHashSet<V>();
        for (V vertex : vertices) {
            costs.put(vertex, Double.POSITIVE_INFINITY);
        }
        costs.put(start, 0.0);
        while (visited.size() != edges.size()) {
            V current = start;
            for (V vertex : vertices) {
                if (costs.get(vertex) < costs.get(current)) {
                    current = vertex;
                }
            }
            ISet<E> visitable = new ChainedHashSet<E>();
            for (V vertex : AList.get(vertices.indexOf(current))) {
                for (E edge : edges) {
                    if ((edge.getVertex1().equals(current) && edge.getVertex2().equals(vertex)) ||
                        (edge.getVertex2().equals(current) && edge.getVertex1().equals(vertex))) {
                        visitable.add(edge);
                    }
                }
            }
            for (E edge : visitable) {
                V target = edge.getOtherVertex(current);
                double newCost = Math.min(costs.get(current)+edge.getWeight(), costs.get(target));
                costs.put(target, newCost);
                path.add(edge);
            }
        }
        return setToList(path);*/
        
        /*ISet<E> path = new ChainedHashSet<E>();
        ISet<V> visited = new ChainedHashSet<V>();
        ISet<E> visitable = new ChainedHashSet<E>();
        V current = start;
        visited.add(current);
        while (path.size() != edges.size()) {
            for (V vertex : AList.get(vertices.indexOf(current))) {
                for (E edge : edges) {
                    if ((edge.getVertex1().equals(current) && edge.getVertex2().equals(vertex)) ||
                        (edge.getVertex2().equals(current) && edge.getVertex1().equals(vertex)) && 
                        !visitable.contains(edge)) {
                        visitable.add(edge);
                    }
                }
            }
            E minimum = visitable.iterator().next();
            for (E edge : visitable) {
                if (edge.compareTo(minimum) < 0) {
                    minimum = edge;
                }
            }
            visitable.remove(minimum);
            V vertex1 = minimum.getVertex1();
            V vertex2 = minimum.getVertex2();
            if (!visited.contains(vertex1)) {
                visited.add(vertex1);
                current = vertex1;
            } else if (!visited.contains(vertex2)) {
                visited.add(vertex2);
                current = vertex2;
            }
            path.add(minimum);
        }
        return setToList(path);*/
    
    /*private static class VDPair<V, Double> implements Comparable<KVPair<V, Double>> {
        private V vertex;
        private Double cost;
        
        public VDPair(V vertex, Double cost) {
            this.vertex = vertex;
            this.cost = cost;
        }

        public int compareTo(KVPair<V, Double> o) {
            return cost.toString().compareTo(o.getValue().toString());
        }*/
    }
}
