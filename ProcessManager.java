import java.util.*;

public class ProcessManager {


private final HashMap<Integer, Process> processMap = new HashMap<>();

private final TreeSet<Process> orderedProcesses = new TreeSet<>(
    Comparator.comparingInt((Process p) -> p.arrivalTime)
              .thenComparingInt(p -> p.pid)
);

public void addProcess(Process p) {
    if (processMap.containsKey(p.pid)) {
        System.out.println("Duplicate process ID!");
        return;
    }

    processMap.put(p.pid, p);
    orderedProcesses.add(p);
}

public List<Process> getProcesses() {
    return new ArrayList<>(orderedProcesses);
}

public Process getProcess(int pid) {
    return processMap.get(pid);
}

public int size() {
    return processMap.size();
}


}
