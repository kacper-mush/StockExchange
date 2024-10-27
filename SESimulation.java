import io.InputInfo;
import io.InputReader;
import system.SESystem;



public class SESimulation {
    public static void main(String[] args) {
        InputInfo inputInfo = null;
        try {
            inputInfo = InputReader.readInput(args);
        }
        catch (InputReader.InputException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        System.out.println(inputInfo);
        System.out.println("\nSTARTING SIMULATION\n");

        SESystem seSystem = new SESystem(inputInfo);
        seSystem.run();
        seSystem.printResults();
    }
}