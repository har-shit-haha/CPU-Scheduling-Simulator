public class Process {
    int pid;
    int arrivalTime;
    int burstTime;
    int priority;

    int remainingTime;
    int completionTime;
    int turnaroundTime;
    int waitingTime;

    public Process(int pid, int arrivalTime, int burstTime, int priority) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.remainingTime = burstTime;
    }

    public void reset() {
        remainingTime = burstTime;
        completionTime = 0;
        turnaroundTime = 0;
        waitingTime = 0;
    }

    public void calculateTimes() {
        turnaroundTime = completionTime - arrivalTime;
        waitingTime = turnaroundTime - burstTime;
    }
}