package utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IndexPermutationTest {

    @Test
    void hasNext() {
        IndexPermutation indexPermutation = new IndexPermutation(10);
        for (int i = 0; i < 10; i++) {
            assertTrue(indexPermutation.hasNext());
            indexPermutation.getNext();
        }
        assertFalse(indexPermutation.hasNext());

        indexPermutation = new IndexPermutation(0);
        assertFalse(indexPermutation.hasNext());

        indexPermutation = new IndexPermutation(1);
        assertTrue(indexPermutation.hasNext());
        indexPermutation.getNext();
        assertFalse(indexPermutation.hasNext());
    }
}