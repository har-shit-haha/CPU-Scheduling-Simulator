import java.util.*;

public class GanttChart {

    private final LinkedList<String> execution = new LinkedList<>();
    private final LinkedList<Integer> startTimes = new LinkedList<>();
    private final LinkedList<Integer> endTimes = new LinkedList<>();

    public void add(String process, int start, int end) {
        if (start == end) {
            return;
        }

        // Merge consecutive execution intervals for the same process.
        if (!execution.isEmpty()
                && execution.getLast().equals(process)
                && endTimes.getLast() == start) {

            endTimes.removeLast();
            endTimes.add(end);
        } else {
            execution.add(process);
            startTimes.add(start);
            endTimes.add(end);
        }
    }

    public void display() {
        System.out.println("\nGANTT CHART");

        for (String p : execution) {
            System.out.print("+----------");
        }
        System.out.println("+");

        for (String p : execution) {
            System.out.printf("| %-8s ", p);
        }
        System.out.println("|");

        for (int i = 0; i < execution.size(); i++) {
            System.out.printf("%-11d", startTimes.get(i));
        }

        if (!endTimes.isEmpty()) {
            System.out.println(endTimes.getLast());
        } else {
            System.out.println("0");
        }
    }
}