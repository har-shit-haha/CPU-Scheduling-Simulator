import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Queue;

public class CPUVisualizer extends JFrame {


private final DefaultTableModel model;
private final JTable table;
private final JComboBox<String> algorithm;
private final JSpinner quantum;
private final JPanel ganttPanel;
private final JTextArea results;

private int nextId = 1;
private List<Segment> segments = new ArrayList<>();

static class Job {
    int id, arrival, burst, priority;
    int remaining, completion, turnaround, waiting;

    Job(int id, int arrival, int burst, int priority) {
        this.id = id;
        this.arrival = arrival;
        this.burst = burst;
        this.priority = priority;
        this.remaining = burst;
    }
}

static class Segment {
    String name;
    int start, end;

    Segment(String name, int start, int end) {
        this.name = name;
        this.start = start;
        this.end = end;
    }
}

public CPUVisualizer() {
    setTitle("CPU Scheduling Simulator");
    setSize(1000, 750);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    setLayout(new BorderLayout(8, 8));

    JPanel top = new JPanel(new GridLayout(2, 1));

    JPanel input = new JPanel(new FlowLayout(FlowLayout.LEFT));

    JTextField arrival = new JTextField("0", 4);
    JTextField burst = new JTextField("5", 4);
    JTextField priority = new JTextField("1", 4);

    input.add(new JLabel("Arrival:"));
    input.add(arrival);
    input.add(new JLabel("Burst:"));
    input.add(burst);
    input.add(new JLabel("Priority:"));
    input.add(priority);

    JButton add = new JButton("Add Process");
    JButton clear = new JButton("Clear");
    input.add(add);
    input.add(clear);

    JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));

    algorithm = new JComboBox<>(new String[]{
        "FCFS", "SJF", "SRTF", "Priority", "Round Robin"
    });

    quantum = new JSpinner(new SpinnerNumberModel(2, 1, 10000, 1));

    JButton run = new JButton("Run Simulation");

    controls.add(new JLabel("Algorithm:"));
    controls.add(algorithm);
    controls.add(new JLabel("Time Quantum:"));
    controls.add(quantum);
    controls.add(run);

    top.add(input);
    top.add(controls);
    add(top, BorderLayout.NORTH);

    model = new DefaultTableModel(
        new String[]{"Process", "Arrival", "Burst", "Priority"}, 0
    );

    table = new JTable(model);
    add(new JScrollPane(table), BorderLayout.CENTER);

    JPanel bottom = new JPanel(new BorderLayout(5, 5));

    ganttPanel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawGantt(g);
        }
    };

    ganttPanel.setBackground(Color.WHITE);
    ganttPanel.setPreferredSize(new Dimension(950, 120));
    ganttPanel.setBorder(
        BorderFactory.createTitledBorder("Gantt Chart")
    );

    results = new JTextArea(12, 70);
    results.setEditable(false);
    results.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

    bottom.add(ganttPanel, BorderLayout.NORTH);
    bottom.add(new JScrollPane(results), BorderLayout.CENTER);

    add(bottom, BorderLayout.SOUTH);

    add.addActionListener(e -> {
        try {
            int a = Integer.parseInt(arrival.getText().trim());
            int b = Integer.parseInt(burst.getText().trim());
            int p = Integer.parseInt(priority.getText().trim());

            if (a < 0 || b <= 0 || p < 0 || model.getRowCount() >= 100) {
                throw new IllegalArgumentException();
            }

            model.addRow(new Object[]{"P" + nextId++, a, b, p});
            results.setText("");
            segments.clear();
            ganttPanel.repaint();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this, "Enter valid non-negative integers; burst must be positive."
            );
        }
    });

    clear.addActionListener(e -> {
        model.setRowCount(0);
        nextId = 1;
        segments.clear();
        results.setText("");
        ganttPanel.repaint();
    });

    run.addActionListener(e -> simulate());

    setVisible(true);
}

private List<Job> readJobs() {
    List<Job> jobs = new ArrayList<>();

    for (int i = 0; i < model.getRowCount(); i++) {
        int id = Integer.parseInt(
            model.getValueAt(i, 0).toString().substring(1)
        );

        int a = Integer.parseInt(model.getValueAt(i, 1).toString());
        int b = Integer.parseInt(model.getValueAt(i, 2).toString());
        int p = Integer.parseInt(model.getValueAt(i, 3).toString());

        jobs.add(new Job(id, a, b, p));
    }

    jobs.sort(Comparator.comparingInt((Job j) -> j.arrival)
                        .thenComparingInt(j -> j.id));

    return jobs;
}

private void addSegment(String name, int start, int end) {
    if (end <= start) return;

    if (!segments.isEmpty()) {
        Segment last = segments.get(segments.size() - 1);

        if (last.name.equals(name) && last.end == start) {
            last.end = end;
            return;
        }
    }

    segments.add(new Segment(name, start, end));
}

private void simulate() {
    if (model.getRowCount() == 0) {
        JOptionPane.showMessageDialog(this, "Add processes first.");
        return;
    }

    try {
        List<Job> jobs = readJobs();
        segments.clear();

        String selected = (String) algorithm.getSelectedItem();

        switch (selected) {
            case "FCFS":
                fcfs(jobs);
                break;
            case "SJF":
                nonPreemptive(jobs, false);
                break;
            case "Priority":
                nonPreemptive(jobs, true);
                break;
            case "SRTF":
                srtf(jobs);
                break;
            case "Round Robin":
                roundRobin(jobs, (Integer) quantum.getValue());
                break;
        }

        displayResults(jobs);
        ganttPanel.repaint();

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(
            this, "Unable to simulate: " + ex.getMessage()
        );
    }
}

private void fcfs(List<Job> jobs) {
    int time = 0;

    for (Job j : jobs) {
        if (time < j.arrival) {
            addSegment("IDLE", time, j.arrival);
            time = j.arrival;
        }

        int start = time;
        time += j.burst;

        addSegment("P" + j.id, start, time);
        finish(j, time);
    }
}

private void nonPreemptive(List<Job> jobs, boolean byPriority) {
    List<Job> remaining = new ArrayList<>(jobs);
    int time = 0;

    while (!remaining.isEmpty()) {
        final int now = time;

        Job chosen = remaining.stream()
            .filter(j -> j.arrival <= now)
            .min(byPriority
                ? Comparator.comparingInt((Job j) -> j.priority)
                    .thenComparingInt(j -> j.arrival)
                    .thenComparingInt(j -> j.id)
                : Comparator.comparingInt((Job j) -> j.burst)
                    .thenComparingInt(j -> j.arrival)
                    .thenComparingInt(j -> j.id))
            .orElse(null);

        if (chosen == null) {
            int next = remaining.stream()
                .mapToInt(j -> j.arrival).min().orElse(time);

            addSegment("IDLE", time, next);
            time = next;
            continue;
        }

        int start = time;
        time += chosen.burst;

        addSegment("P" + chosen.id, start, time);
        finish(chosen, time);
        remaining.remove(chosen);
    }
}


private void srtf(List<Job> jobs) {
    int time = 0;
    int completed = 0;

    while (completed < jobs.size()) {
        Job chosen = null;

        // Find the arrived process with the shortest remaining time.
        for (Job j : jobs) {
            if (j.arrival <= time && j.remaining > 0) {
                if (chosen == null
                        || j.remaining < chosen.remaining
                        || (j.remaining == chosen.remaining
                            && j.arrival < chosen.arrival)
                        || (j.remaining == chosen.remaining
                            && j.arrival == chosen.arrival
                            && j.id < chosen.id)) {
                    chosen = j;
                }
            }
        }

        // If no process is ready, advance to the next arrival.
        if (chosen == null) {
            int next = Integer.MAX_VALUE;

            for (Job j : jobs) {
                if (j.remaining > 0 && j.arrival > time) {
                    next = Math.min(next, j.arrival);
                }
            }

            if (next == Integer.MAX_VALUE) {
                break;
            }

            addSegment("IDLE", time, next);
            time = next;
            continue;
        }

        int start = time;
        time++;
        chosen.remaining--;

        addSegment("P" + chosen.id, start, time);

        if (chosen.remaining == 0) {
            finish(chosen, time);
            completed++;
        }
    }
}



private void roundRobin(List<Job> jobs, int q) {
    Queue<Job> ready = new LinkedList<>();
    int time = 0;
    int index = 0;
    int completed = 0;

    while (completed < jobs.size()) {
        while (index < jobs.size() && jobs.get(index).arrival <= time) {
            ready.add(jobs.get(index++));
        }

        if (ready.isEmpty()) {
            int next = jobs.get(index).arrival;
            addSegment("IDLE", time, next);
            time = next;
            continue;
        }

        Job j = ready.poll();
        int duration = Math.min(q, j.remaining);
        int start = time;

        time += duration;
        j.remaining -= duration;

        addSegment("P" + j.id, start, time);

        while (index < jobs.size() && jobs.get(index).arrival <= time) {
            ready.add(jobs.get(index++));
        }

        if (j.remaining == 0) {
            finish(j, time);
            completed++;
        } else {
            ready.add(j);
        }
    }
}

private void finish(Job j, int time) {
    j.completion = time;
    j.turnaround = time - j.arrival;
    j.waiting = j.turnaround - j.burst;
}

private void displayResults(List<Job> jobs) {
    StringBuilder sb = new StringBuilder();

    sb.append(String.format(
        "%-10s %-10s %-10s %-12s %-12s %-10s%n",
        "Process", "Arrival", "Burst", "Completion",
        "Turnaround", "Waiting"
    ));

    double totalWaiting = 0;
    double totalTurnaround = 0;

    jobs.sort(Comparator.comparingInt(j -> j.id));

    for (Job j : jobs) {
        sb.append(String.format(
            "%-10s %-10d %-10d %-12d %-12d %-10d%n",
            "P" + j.id, j.arrival, j.burst,
            j.completion, j.turnaround, j.waiting
        ));

        totalWaiting += j.waiting;
        totalTurnaround += j.turnaround;
    }

    int elapsed = segments.isEmpty()
        ? 0 : segments.get(segments.size() - 1).end;

    int busy = segments.stream()
        .filter(s -> !s.name.equals("IDLE"))
        .mapToInt(s -> s.end - s.start)
        .sum();

    sb.append("\nAverage Waiting Time: ")
        .append(String.format("%.2f", totalWaiting / jobs.size()));

    sb.append("\nAverage Turnaround Time: ")
        .append(String.format("%.2f", totalTurnaround / jobs.size()));

    sb.append("\nCPU Utilization: ")
        .append(String.format("%.2f%%",
            elapsed == 0 ? 0 : busy * 100.0 / elapsed));

    results.setText(sb.toString());
    results.setCaretPosition(0);
}

private void drawGantt(Graphics g) {
    if (segments.isEmpty()) {
        g.setColor(Color.GRAY);
        g.drawString("Run a simulation to display the timeline.", 20, 60);
        return;
    }

    int x = 20;
    int y = 30;
    int availableWidth = Math.max(100, ganttPanel.getWidth() - 40);
    int total = segments.get(segments.size() - 1).end;

    if (total <= 0) return;

    for (Segment s : segments) {
        int width = Math.max(2,
            (int) ((s.end - s.start) * (availableWidth - 10.0) / total));

        g.setColor(s.name.equals("IDLE")
            ? Color.LIGHT_GRAY
            : new Color(150, 195, 235));

        g.fillRect(x, y, width, 35);
        g.setColor(Color.DARK_GRAY);
        g.drawRect(x, y, width, 35);

        if (width >= 25) {
            g.drawString(s.name, x + 3, y + 22);
        }

        g.drawString(String.valueOf(s.start), x, y + 52);
        x += width;
    }

    g.drawString(
        String.valueOf(segments.get(segments.size() - 1).end),
        x, y + 52
    );
}

public static void main(String[] args) {
    SwingUtilities.invokeLater(CPUVisualizer::new);
}


}
