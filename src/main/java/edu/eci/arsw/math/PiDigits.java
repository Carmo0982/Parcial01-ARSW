package edu.eci.arsw.math;

import java.util.Scanner;

public class PiDigits {

    private static int DigitsPerSum = 8;
    private static double Epsilon = 1e-17;


    public static byte[] getDigits(int start, int count) {
        if (start < 0) {
            throw new RuntimeException("Invalid Interval");
        }

        if (count < 0) {
            throw new RuntimeException("Invalid Interval");
        }

        byte[] digits = new byte[count];
        double sum = 0;

        for (int i = 0; i < count; i++) {
            if (i % DigitsPerSum == 0) {
                sum = 4 * sum(1, start)
                        - 2 * sum(4, start)
                        - sum(5, start)
                        - sum(6, start);

                start += DigitsPerSum;
            }

            sum = 16 * (sum - Math.floor(sum));
            digits[i] = (byte) sum;
        }

        return digits;
    }


    public static byte[] getDigits(int start, int count, int N) {
        if (start < 0 || count < 0) {
            throw new RuntimeException("Invalid Interval");
        }
        if (N <= 0) {
            throw new RuntimeException("Number of threads must be greater than 0");
        }

        int digitsPerThread = count / N;
        int remaining = count % N;

        PiDigitsThread[] threads = new PiDigitsThread[N];
        int currentStart = start;


        for (int i = 0; i < N; i++) {
            int threadCount = digitsPerThread + (i < remaining ? 1 : 0);
            threads[i] = new PiDigitsThread(currentStart, threadCount);
            currentStart += threadCount;
        }


        for (int i = 0; i < N; i++) {
            threads[i].start();
        }


        Thread controller = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (anyAlive(threads)) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                if (!anyAlive(threads)) {
                    break;
                }


                for (PiDigitsThread t : threads) {
                    t.pause();
                }


                System.out.println("\n--- Progreso (pausado) ---");
                int totalProcessed = 0;
                for (int i = 0; i < threads.length; i++) {
                    int processed = threads[i].getProcessedDigits();
                    totalProcessed += processed;
                    System.out.println("Hilo " + i + ": " + processed + " digitos procesados");
                }
                System.out.println("Total: " + totalProcessed + " / " + count + " digitos");
                System.out.println("Presione Enter para continuar...");

                scanner.nextLine();


                for (PiDigitsThread t : threads) {
                    t.resumeThread();
                }
            }
        });
        controller.setDaemon(true);
        controller.start();


        for (int i = 0; i < N; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        byte[] result = new byte[count];
        int offset = 0;
        for (int i = 0; i < N; i++) {
            byte[] partial = threads[i].getDigits();
            System.arraycopy(partial, 0, result, offset, partial.length);
            offset += partial.length;
        }

        return result;
    }

    private static boolean anyAlive(PiDigitsThread[] threads) {
        for (PiDigitsThread t : threads) {
            if (t.isAlive()) {
                return true;
            }
        }
        return false;
    }

    /// <summary>
    /// Returns the sum of 16^(n - k)/(8 * k + m) from 0 to k.
    /// </summary>
    /// <param name="m"></param>
    /// <param name="n"></param>
    /// <returns></returns>
    private static double sum(int m, int n) {
        double sum = 0;
        int d = m;
        int power = n;

        while (true) {
            double term;

            if (power > 0) {
                term = (double) hexExponentModulo(power, d) / d;
            } else {
                term = Math.pow(16, power) / d;
                if (term < Epsilon) {
                    break;
                }
            }

            sum += term;
            power--;
            d += 8;
        }

        return sum;
    }

    /// <summary>
    /// Return 16^p mod m.
    /// </summary>
    /// <param name="p"></param>
    /// <param name="m"></param>
    /// <returns></returns>
    private static int hexExponentModulo(int p, int m) {
        int power = 1;
        while (power * 2 <= p) {
            power *= 2;
        }

        int result = 1;

        while (power > 0) {
            if (p >= power) {
                result *= 16;
                result %= m;
                p -= power;
            }

            power /= 2;

            if (power > 0) {
                result *= result;
                result %= m;
            }
        }

        return result;
    }

}
