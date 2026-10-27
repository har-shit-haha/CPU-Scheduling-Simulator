import java.util.*;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ProcessManager manager = new ProcessManager();

        System.out.println("==================================");
        System.out.println("   CPU PROCESS SCHEDULING SIMULATOR");
        System.out.println("==================================");

        int n = readInt(sc, "Enter number of processes: ", 1, 10000);

        for (int i = 1; i <= n; i++) {
            System.out.println("\nProcess P" + i);

            int arrival = readInt(sc, "Arrival time: ", 0, Integer.MAX_VALUE);
            int burst = readInt(sc, "Burst time: ", 1, Integer.MAX_VALUE);
            int priority = readInt(sc, "Priority (lower = higher priority): ",
                    0, Integer.MAX_VALUE);

            manager.addProcess(new Process(i, arrival, burst, priority));
        }

        SchedulingAlgorithms scheduler =
                new SchedulingAlgorithms(manager.getProcesses());

        while (true) {
            System.out.println("\n========== MENU ==========");
            System.out.println("1. FCFS");
            System.out.println("2. SJF (Non-preemptive)");
            System.out.println("3. SRTF (Preemptive)");
            System.out.println("4. Priority Scheduling");
            System.out.println("5. Round Robin");
            System.out.println("6. Exit");

            int choice = readInt(sc, "Choose an option: ", 1, 6);

            switch (choice) {
                case 1:
                    scheduler.fcfs();
                    break;

                case 2:
                    scheduler.sjf();
                    break;

                case 3:
                    scheduler.srtf();
                    break;

                case 4:
                    scheduler.priorityScheduling();
                    break;

                case 5:
                    int quantum = readInt(
                            sc, "Enter time quantum: ", 1, Integer.MAX_VALUE);
                    scheduler.roundRobin(quantum);
                    break;

                case 6:
                    System.out.println("Simulator closed.");
                    sc.close();
                    return;
            }
        }
    }

    private static int readInt(
            Scanner sc, String message, int min, int max) {

        while (true) {
            System.out.print(message);
            String input = sc.nextLine().trim();

            try {
                int value = Integer.parseInt(input);

                if (value >= min && value <= max) {
                    return value;
                }

                System.out.println(
                        "Enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Enter a whole number.");
            }
        }
    }
}