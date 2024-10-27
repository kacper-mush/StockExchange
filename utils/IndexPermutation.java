package utils;

import java.util.ArrayList;

public class IndexPermutation {
    ArrayList<Integer> idxList;
    public IndexPermutation(int n) {
        idxList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            idxList.add(i);
        }
        java.util.Collections.shuffle(idxList);
    }

    public int getNext() {
        return idxList.remove(0);
    }

    public boolean hasNext() {
        return !idxList.isEmpty();
    }
}
