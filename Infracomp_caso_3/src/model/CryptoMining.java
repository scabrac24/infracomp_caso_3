package model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class CryptoMining {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Ingrese el valor de inputString: ");
            String inputString = scanner.nextLine();

            System.out.print("Ingrese el valor de numZeros (20, 24, 28, 32 o 36): ");
            int numZeros = scanner.nextInt();

            System.out.print("Ingrese el valor de numThreads (1 o 2): ");
            int numThreads = scanner.nextInt();
            scanner.nextLine(); // Consumir la nueva línea

            System.out.print("Ingrese el valor de algorithm (SHA-256 o SHA-512): ");
            String algorithm = scanner.nextLine();

            if (!algorithm.equals("SHA-256") && !algorithm.equals("SHA-512")) {
                System.out.println("Algoritmo no válido. Debe ser SHA-256 o SHA-512.");
                return;
            }

            AtomicBoolean solutionFound = new AtomicBoolean(false);
            long startTime = System.currentTimeMillis();

            MineTask[] threads = new MineTask[numThreads];
            for (int i = 0; i < numThreads; i++) {
                threads[i] = new MineTask(inputString, numZeros, algorithm, solutionFound, startTime);
                threads[i].start();
            }

            // Espera a que al menos un hilo encuentre una solución o todos terminen
            boolean allThreadsFinished = true;
            for (int i = 0; i < numThreads; i++) {
                try {
                    threads[i].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (threads[i].isSolutionFound()) {
                    allThreadsFinished = false;
                }
            }

            if (allThreadsFinished) {
                long endTime = System.currentTimeMillis();
                System.out.println("No se encontró una solución.");
                System.out.println("Tiempo de búsqueda total: " + (endTime - startTime) + " ms");
            }
        }
    }
}

class MineTask extends Thread {
    private final String inputString;
    private final int numZeros;
    private final AtomicBoolean solutionFound;
    private final String algorithm;
    private final long startTime;

    public MineTask(String inputString, int numZeros, String algorithm, AtomicBoolean solutionFound, long startTime) {
        this.inputString = inputString;
        this.numZeros = numZeros;
        this.solutionFound = solutionFound;
        this.algorithm = algorithm;
        this.startTime = startTime;
    }

    @Override
    public void run() {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            String prefixZeros = "0".repeat(numZeros);

            for (int v = 0; !solutionFound.get() && v < 26 * 7 * 7 * 7 * 7 * 7 * 7; v++) {
                String data = inputString + getAlphabeticString(v);

                md.update(data.getBytes());
                byte[] digest = md.digest();

                if (bytesToHex(digest).startsWith(prefixZeros)) {
                    solutionFound.set(true);
                    long endTime = System.currentTimeMillis();
                    System.out.println("Cadena de entrada: " + inputString);
                    System.out.println("Valor v: " + v);
                    System.out.println("Tiempo de búsqueda: " + (endTime - startTime) + " ms");
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private String getAlphabeticString(int value) {
        StringBuilder sb = new StringBuilder();
        int base = 26;
        for (int i = 0; i < 7; i++) {
            char ch = (char) ('a' + (value % base));
            sb.insert(0, ch);
            value /= base;
        }
        return sb.toString();
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    public boolean isSolutionFound() {
        return solutionFound.get();
    }
}
