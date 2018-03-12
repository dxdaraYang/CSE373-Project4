package misc.graphs;


import datastructures.concrete.ArrayDisjointSet;
import datastructures.concrete.ArrayHeap;
import datastructures.concrete.ChainedHashSet;
import datastructures.concrete.DoubleLinkedList;
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

    /**
     * Constructs a new graph based on the given vertices and edges.
     *
     * @throws IllegalArgumentException  if any of the edges have a negative weight
     * @throws IllegalArgumentException  if one of the edges connects to a vertex not
     *                                   present in the 'vertices' list
     */
    private ChainedHashDictionary<Integer, ChainedHashSet<V>> AList;
    private IList<V> vertices;
    private IList<E> edges;
    
    public Graph(IList<V> vertices, IList<E> edges) {
        
        for (E edge: edges) {
            if(edge.getWeight() < 0 || !vertices.contains(edge.getVertex1())
                    || !vertices.contains(edge.getVertex2())) {
                throw new IllegalArgumentException();
            }
        }
        this.vertices = vertices;
        this.edges = edges;
        this.AList = new ChainedHashDictionary<>();
        for(V vertex : vertices) {
            AList.put(vertices.indexOf(vertex), new ChainedHashSet<V>() );
        }
        for(E edge:edges) {
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
        return this.vertices.size();
                
    }

    /**
     * Returns the number of edges contained within this graph.
     */
    public int numEdges() {
        return this.edges.size();
    }

    /**
     * Returns the set of all edges that make up the minimum spanning tree of
     * this graph.
     *
     * If there exists multiple valid MSTs, return any one of them.
     *
     * Precondition: the graph does not contain any unconnected components.
     */
    public ISet<E> findMinimumSpanningTree() { // use Kruskal's
        
        IDisjointSet<V> mstSet = new ArrayDisjointSet<>();                     
        for(V vertex : vertices) {
            mstSet.makeSet(vertex);
        }
        
        IList<E> top = Searcher.topKSort(edges.size(), this.edges);
        
        ISet<E> mst = new ChainedHashSet<>();
        for(E edge: top) {
            if(mstSet.findSet(edge.getVertex1()) != mstSet.findSet(edge.getVertex2())){
                mstSet.union(edge.getVertex1(),edge.getVertex2());
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
    public IList<E> findShortestPathBetween(V start, V end) { // Dijkastra
        // create a inner class of comparable vertexDic<vertex, weight> compare by weight
        // insert  vertexDic into heap
        if (start.equals(end)) {
            return new DoubleLinkedList<E>();
        }
        
        ChainedHashDictionary<V, Double> costs = new ChainedHashDictionary<>();
        ISet<E> path = new ChainedHashSet<E>();
        ISet<V> visited = new ChainedHashSet<V>();
        ChainedHashDictionary<V, V> smallestEdge = new ChainedHashDictionary<>();
        ISet<E> visitedEdge = new ChainedHashSet<E>();
        ArrayHeap<VDPair<V>> heap = new ArrayHeap<>();
        
        for (V vertex : vertices) {
            costs.put(vertex, Double.POSITIVE_INFINITY);
        }
        costs.put(start, 0.0);
        VDPair<V> currPair = new VDPair<>(start, 0.0);
        heap.insert(currPair);        
        V current = start;
        Double currCost = 0.0;
        
        while (!heap.isEmpty() && !current.equals(end) && !currCost.equals(Double.POSITIVE_INFINITY)) {
 
            current = heap.removeMin().getVertex();
            visited.add(current);
            ISet<E> visitable = findVisitable(current);            
            for (E edge : visitable) {
                V target = edge.getOtherVertex(current);
                if(costs.get(current)+edge.getWeight() <= costs.get(target)) {
                    smallestEdge.put(edge.getOtherVertex(current), current);
                }
                double newCost = Math.min(costs.get(current)+edge.getWeight(), costs.get(target));
                if(!visited.contains(target)) {
                    costs.put(target, newCost);
                    heap.insert(new VDPair<>(target, newCost));        
                }                            
                visitedEdge.add(edge);
            }
            
            V nextCheap = heap.peekMin().getVertex();
            for(E prev: visitedEdge) {
                if((prev.getVertex1().equals(nextCheap) || prev.getVertex2().equals(nextCheap))
                        && prev.getOtherVertex(nextCheap).equals(smallestEdge.get(nextCheap))) {
                    E addedpath = prev;
                    path.add(addedpath);
                }
            }            
            
            currCost = heap.peekMin().getCost();
        }
        
        if(current != end) {
            throw new NoPathExistsException();
        }
       
        return setToList(path);
        
        
    }
    
    private ISet<E> findVisitable (V start){
        ISet<E> visitable = new ChainedHashSet<>();
        for(E edge: edges) {
            if(edge.getVertex1().equals(start) || edge.getVertex2().equals(start)) {
                visitable.add(edge);
            }
        }
        return visitable;
    }
    
//    private E findEdge(V vertex1, V vertex2) {
//        for(E edge : edges) {
//            if((edge.getVertex1().equals(vertex1) && edge.getVertex2().equals(vertex2)) ||
//                    (edge.getVertex1().equals(vertex2) && edge.getVertex2().equals(vertex1))) {
//                return edge;
//            }else {
//                if(edge.getVertex1().equals(vertex2)；
//            }
//            
//        }
//        return null;
//        
//    }
    
    private static class VDPair<V> implements Comparable<VDPair<V>>{
        private V vertex;
        private Double cost;
        
        public VDPair(V vertex, Double cost) {
            this.vertex = vertex;
            this.cost = cost;
        }
        @Override
        public int compareTo(VDPair<V> another) {
            return(this.cost.compareTo(another.getCost()));
            
        }
        
        private V getVertex() {
            return this.vertex;
        }
        
        private Double getCost() {
            return this.cost;
        }
    }

        
}
