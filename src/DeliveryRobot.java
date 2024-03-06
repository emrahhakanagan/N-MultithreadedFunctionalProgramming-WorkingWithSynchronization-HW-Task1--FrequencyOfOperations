import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DeliveryRobot {

    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();
    private static final Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {
        Thread[] threads = new Thread[1000];
        Thread monitorThread = createMonitorThread();

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                String route = generateRoute("RLRFR", 100);
                int rCount = countOccurrences(route, 'R');

                synchronized (lock) {
                    sizeToFreq.put(rCount, sizeToFreq.getOrDefault(rCount, 0) + 1);
                    lock.notify();
                }
            });
            threads[i].start();
        }

        monitorThread.start();

        for (Thread thread : threads) {
            thread.join();
        }

        monitorThread.interrupt();
    }

    public static Thread createMonitorThread() {
        return new Thread(() -> {
            while (!Thread.interrupted()) {
                synchronized (lock) {
                    try {
                        lock.wait();
                        printCurrentLeader();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }

    public static int countOccurrences(String str, char letter) {
        return (int) str.chars().filter(ch -> ch == letter).count();
    }

    public static void printCurrentLeader() {
        int mostFrequentCount = sizeToFreq.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(-1);

        if (mostFrequentCount != -1) {
            System.out.println("Текущий лидер: " + mostFrequentCount + ", частота: " + sizeToFreq.get(mostFrequentCount));
        }
    }

}
