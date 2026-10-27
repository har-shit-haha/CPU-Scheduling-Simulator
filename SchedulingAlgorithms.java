import java.util.*;

public class SchedulingAlgorithms {

    private final List<Process> processes;
    private final GanttChart chart = new GanttChart();

    public SchedulingAlgorithms(List<Process> processes) {
        this.processes = processes;
    }

    private void reset() {
        for (Process p : processes) {
            p.reset();
        }
    }

    // 1. First Come, First Served
    public void fcfs() {
        reset();

        List<Process> list = new ArrayList<>(processes);
        list.sort(Comparator.comparingInt((Process p) -> p.arrivalTime)
                            .thenComparingInt(p -> p.pid));

        int time = 0;

        for (Process p : list) {
            if (time < p.arrivalTime) {
                chart.add("IDLE", time, p.arrivalTime);
                time = p.arrivalTime;
            }

            int start = time;
            time += p.burstTime;

            chart.add("P" + p.pid, start, time);

            p.completionTime = time;
            p.calculateTimes();
        }

        displayResults();
    }

    // 2. Shortest Job First (Non-preemptive)
    public void sjf() {
        reset();

        PriorityQueue<Process> ready = new PriorityQueue<>(
            Comparator.comparingInt((Process p) -> p.burstTime)
                      .thenComparingInt(p -> p.arrivalTime)
                      .thenComparingInt(p -> p.pid)
        );

        List<Process> list = new ArrayList<>(processes);
        list.sort(Comparator.comparingInt((Process p) -> p.arrivalTime)
                            .thenComparingInt(p -> p.pid));

        int time = 0;
        int index = 0;
        int completed = 0;

        while (completed < list.size()) {
            while (index < list.size()
                    && list.get(index).arrivalTime <= time) {
                ready.add(list.get(index++));
            }

            if (ready.isEmpty()) {
                int next = list.get(index).arrivalTime;
                chart.add("IDLE", time, next);
                time = next;
                continue;
            }

            Process p = ready.poll();

            int start = time;
            time += p.burstTime;

            chart.add("P" + p.pid, start, time);

            p.completionTime = time;
            p.calculateTimes();
            completed++;
        }

        displayResults();
    }

    // 3. Shortest Remaining Time First (Preemptive)
    public void srtf() {
        reset();

        PriorityQueue<Process> ready = new PriorityQueue<>(
            Comparator.comparingInt((Process p) -> p.remainingTime)
                      .thenComparingInt(p -> p.arrivalTime)
                      .thenComparingInt(p -> p.pid)
        );

        List<Process> list = new ArrayList<>(processes);
        list.sort(Comparator.comparingInt((Process p) -> p.arrivalTime)
                            .thenComparingInt(p -> p.pid));

        int time = 0;
        int index = 0;
        int completed = 0;

        while (completed < list.size()) {
            while (index < list.size()
                    && list.get(index).arrivalTime <= time) {
                ready.add(list.get(index++));
            }

            if (ready.isEmpty()) {
                int next = list.get(index).arrivalTime;
                chart.add("IDLE", time, next);
                time = next;
                continue;
            }

            Process p = ready.poll();

            int nextArrival = index < list.size()
                    ? list.get(index).arrivalTime
                    : Integer.MAX_VALUE;

            int duration = Math.min(
                p.remainingTime,
                nextArrival - time
            );

            if (duration <= 0) {
                ready.add(p);
                continue;
            }

            int start = time;
            time += duration;
            p.remainingTime -= duration;

            chart.add("P" + p.pid, start, time);

            if (p.remainingTime == 0) {
                p.completionTime = time;
                p.calculateTimes();
                completed++;
            } else {
                ready.add(p);
            }
        }

        displayResults();
    }

    // 4. Priority Scheduling (Non-preemptive)
    // Lower priority number means higher priority.
    public void priorityScheduling() {
        reset();

        PriorityQueue<Process> ready = new PriorityQueue<>(
            Comparator.comparingInt((Process p) -> p.priority)
                      .thenComparingInt(p -> p.arrivalTime)
                      .thenComparingInt(p -> p.pid)
        );

        List<Process> list = new ArrayList<>(processes);
        list.sort(Comparator.comparingInt((Process p) -> p.arrivalTime)
                            .thenComparingInt(p -> p.pid));

        int time = 0;
        int index = 0;
        int completed = 0;

        while (completed < list.size()) {
            while (index < list.size()
                    && list.get(index).arrivalTime <= time) {
                ready.add(list.get(index++));
            }

            if (ready.isEmpty()) {
                int next = list.get(index).arrivalTime;
                chart.add("IDLE", time, next);
                time = next;
                continue;
            }

            Process p = ready.poll();

            int start = time;
            time += p.burstTime;

            chart.add("P" + p.pid, start, time);

            p.completionTime = time;
            p.calculateTimes();
            completed++;
        }

        displayResults();
    }

    // 5. Round Robin
    public void roundRobin(int quantum) {
        if (quantum <= 0) {
            System.out.println("Time quantum must be positive.");
            return;
        }

        reset();

        Queue<Process> ready = new LinkedList<>();

        List<Process> list = new ArrayList<>(processes);
        list.sort(Comparator.comparingInt((Process p) -> p.arrivalTime)
                            .thenComparingInt(p -> p.pid));

        int time = 0;
        int index = 0;
        int completed = 0;

        while (completed < list.size()) {
            while (index < list.size()
                    && list.get(index).arrivalTime <= time) {
                ready.add(list.get(index++));
            }

            if (ready.isEmpty()) {
                int next = list.get(index).arrivalTime;
                chart.add("IDLE", time, next);
                time = next;
                continue;
            }

            Process p = ready.poll();

            int duration = Math.min(quantum, p.remainingTime);
            int start = time;

            time += duration;
            p.remainingTime -= duration;

            chart.add("P" + p.pid, start, time);

            // Include processes that arrived during this time slice.
            while (index < list.size()
                    && list.get(index).arrivalTime <= time) {
                ready.add(list.get(index++));
            }

            if (p.remainingTime == 0) {
                p.completionTime = time;
                p.calculateTimes();
                completed++;
            } else {
                ready.add(p);
            }
        }

        displayResults();
    }

    // Display process metrics and averages.
    private void displayResults() {
        chart.display();

        List<Process> sorted = new ArrayList<>(processes);
        sorted.sort(Comparator.comparingInt(p -> p.pid));

        double totalWaiting = 0;
        double totalTurnaround = 0;

        System.out.println("\nPROCESS TABLE");
        System.out.printf("%-10s %-10s %-10s %-10s %-10s %-10s%n",
                "Process", "Arrival", "Burst", "Completion",
                "Turnaround", "Waiting");

        for (Process p : sorted) {
            System.out.printf("%-10s %-10d %-10d %-10d %-10d %-10d%n",
                    "P" + p.pid,
                    p.arrivalTime,
                    p.burstTime,
                    p.completionTime,
                    p.turnaroundTime,
                    p.waitingTime);

            totalWaiting += p.waitingTime;
            totalTurnaround += p.turnaroundTime;
        }

        if (!sorted.isEmpty()) {
            System.out.printf("%nAverage Waiting Time: %.2f%n",
                    totalWaiting / sorted.size());

            System.out.printf("Average Turnaround Time: %.2f%n",
                    totalTurnaround / sorted.size());
        }
    }
}