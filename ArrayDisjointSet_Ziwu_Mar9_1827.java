package datastructures.concrete;

import datastructures.concrete.dictionaries.ChainedHashDictionary;
import datastructures.interfaces.IDisjointSet;

/**
 * See IDisjointSet for more details.
 */
public class ArrayDisjointSet<T> implements IDisjointSet<T> {
    // Note: do NOT rename or delete this field. We will be inspecting it
    // directly within our private tests.
    private int[] pointers;

    // However, feel free to add more methods and private helper methods.
    // You will probably need to add one or two more fields in order to
    // successfully implement this class.
    private int[] roots;
    private ChainedHashDictionary<Integer, T> family;
    private int size;

    public ArrayDisjointSet() {
        pointers = new int[2];
        roots = new int[2];
        family = new ChainedHashDictionary<>();
        size = 0;
    }

    @Override
    public void makeSet(T item) {
        if (itemRepresentative(item) != -1) {
            throw new IllegalArgumentException();
        }
        family.put(size, item);
        expandArrays();
        pointers[size] = -1;
        roots[size] = size + 1;
        size++;
    }

    @Override
    public int findSet(T item) {
        int representative = itemRepresentative(item);
        if (representative == -1) {
            throw new IllegalArgumentException();
        }
        return findRoot(representative);
    }

    @Override
    public void union(T item1, T item2) {
        int root1 = findRoot(findSet(item1));
        int root2 = findRoot(findSet(item2));
        if (root1 == root2) {
            throw new IllegalArgumentException();
        }
        pointers[root2] = root1;
        roots[root2] = -1;
        
    }
    
    private int itemRepresentative(T item) {
        for (int i = 0; i < size; i++) {
            if (family.get(i).equals(item)) {
                return i;
            }
        }
        return -1;
    }
    
    private void expandArrays() {
        int length = pointers.length;
        if (length == size) {
            int[] newPointers = new int[2 * length];
            int[] newRoots = new int[2 * length];
            for (int i = 0; i < length; i++) {
                newPointers[i] = pointers[i];
                newRoots[i] = roots[i];
            }
            pointers = newPointers;
            roots = newRoots;
        }
    }
    
    private int findRoot(int index) {
        int pointer = pointers[index];
        if (pointer == -1) {
            return index;
        }
        return findRoot(pointer);
    }
}
