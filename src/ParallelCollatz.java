
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
//Callable - використовується для представлення задачі - можна викликати та повертати значення
class CollatzTask implements Callable<Integer> {
    private final int number;
    private final Lock lock; //мютекс

    public CollatzTask(int number) {
        this.number = number;
        this.lock = new ReentrantLock();
    }

    @Override
    public Integer call() {
        int steps = 0;
        int n = number;

        while (n != 1) {
            lock.lock(); //Робота з мютексом перед входом у критичну зону
            try {
                if (n % 2 == 0) {
                    n /= 2;
                } else {
                    n = 3 * n + 1;
                }
                steps++;
            } finally {
                lock.unlock(); // Звільнення мютексу після виходу з критичної секції
            }
        }

        System.out.println("Число " + number + ": Кількість кроків = " + steps);

        return steps;
    }
}

public class ParallelCollatz {
    public static void main(String[] args) {
        int N = 100000;// Задається кількість чисел
        int numThreads = 4; // кількість потоків

        //Пул потоків
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        //Створення сервісу завершення щоб результати отримувати в порядку завершення
        CompletionService<Integer> completionService = new ExecutorCompletionService<>(executorService);

        // Створення задач для обчислення гіпотези Колаца для кожного числа
        for (int i = 1; i <= N; i++) {
            completionService.submit(new CollatzTask(i));
        }

        int totalSteps = 0;

        try {
            // Вивід результатів та обчислення середньої кількості кроків
            for (int i = 0; i < N; i++) {
                Future<Integer> result = completionService.take();
                totalSteps += result.get();
            }

            double averageSteps = (double) totalSteps / N;
            System.out.println(" Середня кількість кроків: " + averageSteps);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
    }
}
