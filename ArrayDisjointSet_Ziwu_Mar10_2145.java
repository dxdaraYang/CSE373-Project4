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
    private ChainedHashDictionary<Integer, Integer> family;
    private int size;

    public ArrayDisjointSet() {
        pointers = new int[2];
        family = new ChainedHashDictionary<>();
        size = 0;
    }

    @Override
    public void makeSet(T item) {
        int hash = item.toString().hashCode();
        if (family.containsKey(hash)) {
            throw new IllegalArgumentException();
        }
        family.put(hash, size);
        if (size == pointers.length) {
            pointers = expandArrays(pointers);
        }
        pointers[size] = -(size+1);
        size++;
    }

    @Override
    public int findSet(T item) {
        int hash = item.toString().hashCode();
        if (!family.containsKey(hash)) {
            throw new IllegalArgumentException();
        }
        int root = findRoot(family.get(hash));
        return root;
    }

    @Override
    public void union(T item1, T item2) {
        int representative1 = findSet(item1);
        int representative2 = findSet(item2);
        if (representative1 == representative2) {
            throw new IllegalArgumentException();
        }
        pointers[representative2] = representative1;
    }
    
    private int[] expandArrays(int[] array) {
        int length = array.length;
        int[] newArray = new int[2 * length];
        for (int i = 0; i < length; i++) {
            newArray[i] = array[i];
        }
        return newArray;
    }
    
    private int findRoot(int index) {
        int pointer = pointers[index];
        if (pointer < 0) {
            return index;
        }
        return findRoot(pointer);
    }
}
