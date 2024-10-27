package io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

class InputReaderTest {

    @Test
    void readInput() {
        String[] args = new String[2];
        args[0] = "io/testfiles/testValid.txt";
        args[1] = "10";
        try {
            InputInfo io = InputReader.readInput(args);
        } catch (InputReader.InputException e) {
            System.out.println(e.getMessage());
            fail("Valid input file failed");
            // If this throws, add "src/" to the path
        }
        System.out.println("Valid input test passed");

        for (int i = 1; i < 9; i++) {
            String filename = "io/testfiles/test" + i + ".txt";
            args[0] = filename;
            args[1] = "10";
            assertThrows(InputReader.InputException.class, () -> InputReader.readInput(args));
        }

        args[0] = null;
        args[1] = "10";
        assertThrows(InputReader.InputException.class, () -> InputReader.readInput(args));

        args[0] = "io/testfiles/testValid.txt";
        args[1] = null;
        assertThrows(InputReader.InputException.class, () -> InputReader.readInput(args));

        args[0] = "io/testfiles/testValid.txt\"";
        args[1] = "ALLALALA";
        assertThrows(InputReader.InputException.class, () -> InputReader.readInput(args));

        args[0] = "invalidppppppathhh";
        args[1] = "10";
        assertThrows(InputReader.InputException.class, () -> InputReader.readInput(args));

        System.out.println("Bad input test passed");
    }
}