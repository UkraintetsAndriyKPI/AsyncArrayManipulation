import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws Exception {
        // Створюємо пул потоків для обробки масивів (ExecutorService)
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Завдання для обробки кожного масиву
        Future<int[]> futureArray1 = executor.submit(() -> {
            int[] array = generateRandomArray();
            writeArrayToFile(array, "array1.txt");
            array = readArrayFromFile("array1.txt");
            return filterOddNumbers(array);  // Фільтруємо непарні числа
        });

        Future<int[]> futureArray2 = executor.submit(() -> {
            int[] array = generateRandomArray();
            writeArrayToFile(array, "array2.txt");
            array = readArrayFromFile("array2.txt");
            return divideByThree(array);  // Ділимо на 3
        });

        Future<int[]> futureArray3 = executor.submit(() -> {
            int[] array = generateRandomArray();
            writeArrayToFile(array, "array3.txt");
            array = readArrayFromFile("array3.txt");
            return filterRange(array, 50, 250);  // Фільтруємо в діапазоні [50, 250]
        });

        // Отримуємо результати масивів
        int[] array1 = futureArray1.get();
        int[] array2 = futureArray2.get();
        int[] array3 = futureArray3.get();

        // Завершуємо пул потоків після завершення задач
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        // Сортуємо масиви за допомогою ForkJoinPool
        ForkJoinPool forkJoinPool = new ForkJoinPool();

        // Об'єднуємо і фільтруємо масиви за допомогою ForkJoinTask
        MergeAndFilterTask mergeTask = new MergeAndFilterTask(array1, array2, array3);
        int[] mergedArray = forkJoinPool.invoke(mergeTask);

        // Виводимо об'єднаний масив
        System.out.println("Merged and filtered array: " + Arrays.toString(mergedArray));

        // Завершуємо роботу ForkJoinPool
        forkJoinPool.shutdown();
    }

    // Завдання ForkJoin для об'єднання і фільтрації масивів
    static class MergeAndFilterTask extends RecursiveTask<int[]> {
        private final int[] array1, array2, array3;

        public MergeAndFilterTask(int[] array1, int[] array2, int[] array3) {
            this.array1 = array1;
            this.array2 = array2;
            this.array3 = array3;
        }

        @Override
        protected int[] compute() {
            Set<Integer> set3 = new HashSet<>();
            for (int num : array3) {
                set3.add(num);
            }

            List<Integer> mergedList = new ArrayList<>();
            for (int num : array1) {
                if (!set3.contains(num)) {
                    mergedList.add(num);
                }
            }
            for (int num : array2) {
                if (!set3.contains(num)) {
                    mergedList.add(num);
                }
            }

            // Перетворюємо на масив і сортуємо
            int[] mergedArray = mergedList.stream().mapToInt(Integer::intValue).toArray();
            Arrays.sort(mergedArray);
            return mergedArray;
        }
    }

    // Генерація випадкового масиву
    public static int[] generateRandomArray() {
        Random random = new Random();
        int size = 15 + random.nextInt(11);  // від 15 до 25 елементів
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(1001); // випадкові числа від 0 до 1000
        }
        return array;
    }

    // Запис масиву в файл
    public static void writeArrayToFile(int[] array, String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for (int num : array) {
            writer.write(num + " ");
        }
        writer.close();
    }

    // Читання масиву з файлу
    public static int[] readArrayFromFile(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String[] numbers = reader.readLine().split(" ");
        reader.close();

        int[] array = new int[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            array[i] = Integer.parseInt(numbers[i]);
        }
        return array;
    }

    // Фільтрація непарних чисел
    public static int[] filterOddNumbers(int[] array) {
        return Arrays.stream(array).filter(num -> num % 2 != 0).toArray();
    }

    // Ділимо числа на 3 і залишаємо лише цілу частину
    public static int[] divideByThree(int[] array) {
        return Arrays.stream(array).map(num -> num / 3).toArray();
    }

    // Фільтрація чисел у вказаному діапазоні
    public static int[] filterRange(int[] array, int lower, int upper) {
        return Arrays.stream(array).filter(num -> num >= lower && num <= upper).toArray();
    }
}