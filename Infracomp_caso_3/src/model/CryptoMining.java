package model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class CryptoMining {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Ingrese el valor de inputString: ");
            String inputString = scanner.nextLine();

            System.out.print("Ingrese el valor de numZeros (20, 24, 28, 32 o 36): ");
            int numZeros = scanner.nextInt();
            if (numZeros % 4 != 0 || numZeros < 20 || numZeros > 36) {
                System.out.println("Número de ceros no válido. Debe ser 20, 24, 28, 32 o 36 y múltiplo de 4.");
                return;
            }

            System.out.print("Ingrese el valor de numThreads (1 o 2): ");
            int numThreads = scanner.nextInt();
            scanner.nextLine(); 
            if (numThreads != 1 && numThreads != 2) {
                System.out.println("Número de threads no válido. Debe ser 1 o 2.");
                return;
            }

            System.out.print("Ingrese el valor de algorithm (SHA-256 o SHA-512): ");
            String algorithm = scanner.nextLine();
            if (!algorithm.equals("SHA-256") && !algorithm.equals("SHA-512")) {
                System.out.println("Algoritmo no válido. Debe ser SHA-256 o SHA-512.");
                return;
            }

            AtomicBoolean solutionFound = new AtomicBoolean(false);
            long startTime = System.currentTimeMillis();

            int numPossibleValues = (int) Math.pow(26, 7); 
            int range = numPossibleValues / numThreads;

            MineTask[] threads = new MineTask[numThreads];
            for (int i = 0; i < numThreads; i++) {
                int startRange = i * range;
                int endRange = (i + 1) * range;
                if (i == numThreads - 1) {
                    endRange = numPossibleValues;
                }
                threads[i] = new MineTask(inputString, numZeros, algorithm, solutionFound, startRange, endRange, startTime);
                threads[i].start();
            }

            for (MineTask thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (!solutionFound.get()) {
                long endTime = System.currentTimeMillis();
                System.out.println("No se encontró una solución.");
                System.out.println("Tiempo de búsqueda total: " + (endTime - startTime) + " ms");
            }
        }
    }
}

class MineTask extends Thread {
    private static final AtomicLong totalHashOperations = new AtomicLong(0);
    private static long timeHash  = 0;
     static AtomicLong totalTimeSpentHashing = new AtomicLong(0);
    private final String inputString;
    private final int numZeros;
    private final AtomicBoolean solutionFound;
    private final String algorithm;
    private final int startRange;
    private final int endRange;
    private final long startTime;

    public MineTask(String inputString, int numZeros, String algorithm, AtomicBoolean solutionFound, int startRange, int endRange, long startTime) {
        this.inputString = inputString;
        this.numZeros = numZeros / 4; // bits -> digitos hexadecimales
        this.solutionFound = solutionFound;
        this.algorithm = algorithm;
        this.startRange = startRange;
        this.endRange = endRange;
        this.startTime = startTime;
    }

    @Override
public void run() {
    try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            String target = String.format("%0" + numZeros + "d", 0);

            for (int v = startRange; v < endRange && !solutionFound.get(); v++) {
                String data = inputString + getAlphabeticString(v);

                long startTimeHash = System.nanoTime(); 
                md.update(data.getBytes());
                byte[] digest = md.digest();
                long endTimeHash = System.nanoTime(); 

                totalHashOperations.incrementAndGet();

                totalTimeSpentHashing.set(totalTimeSpentHashing.get() + (endTimeHash - startTimeHash));

            if (bytesToHex(digest).startsWith(target )) {
                solutionFound.set(true);
                long endTime = System.currentTimeMillis();
                System.out.println("Cadena de entrada: " + inputString);
                System.out.println("Valor v: " + v);
                System.out.println("Hash: " + bytesToHex(digest));
                System.out.println("Tiempo de búsqueda: " + (endTime - startTime) + " ms");
                double processorSpeedGHz = 2.1; 
                long totalHashes = MineTask.getTotalHashOperations();
                long totalTimeNanos = endTime - startTime; 
                double totalTimeSeconds = totalTimeNanos / 1e3;
                long averageTimePerHash = totalTimeSpentHashing.get() / totalHashOperations.get();

                double cyclesPerHash = (processorSpeedGHz * 1e9) * averageTimePerHash ;
                System.out.println("Velocidad del procesador: " + processorSpeedGHz + " GHz");
                System.out.println("Total de operaciones de hash: " + totalHashes);
                System.out.println("Tiempo total de ejecución (segundos): " + totalTimeSeconds);
                System.out.println("Ciclos de procesador promedio por hash: " + cyclesPerHash);

            }

            if (solutionFound.get()) {
                // se sale del ciclo si otro hilo ya encontró una soluciooón xd
                return;
            }
        }
    } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
    }
}
    public static long getTotalHashOperations() {
        return totalHashOperations.get();
    }

    private String getAlphabeticString(int value) {
        StringBuilder sb = new StringBuilder();
        int base = 26;
        while (value > 0) {
            char ch = (char) ('a' + (value % base));
            sb.insert(0, ch);
            value /= base;
        }
        // Asegurarse de que la cadena tenga 7 caracteres
        while (sb.length() < 7) {
            sb.insert(0, 'a');
        }
        return sb.toString();
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
