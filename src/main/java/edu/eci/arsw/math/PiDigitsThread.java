package edu.eci.arsw.math;

public class PiDigitsThread extends Thread {

    private static final int DigitsPerSum = 8;
    private static final double Epsilon = 1e-17;

    private int start;
    private int count;
    private byte[] digits;
    private int processedDigits;
    private boolean paused;
    private final Object lock = new Object();

    public PiDigitsThread(int start, int count) {
        this.start = start;
        this.count = count;
        this.digits = new byte[count];
        this.processedDigits = 0;
        this.paused = false;
    }

    @Override
    public void run() {
        double sum = 0;
        int currentStart = start;

        for (int i = 0; i < count; i++) {
            // Verificar si debe pausarse
            synchronized (lock) {
                while (paused) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }

            if (i % DigitsPerSum == 0) {
                sum = 4 * sum(1, currentStart)
                        - 2 * sum(4, currentStart)
                        - sum(5, currentStart)
                        - sum(6, currentStart);
                currentStart += DigitsPerSum;
            }

            sum = 16 * (sum - Math.floor(sum));
            digits[i] = (byte) sum;
            processedDigits = i + 1;
        }
    }

    public void pause() {
        synchronized (lock) {
            paused = true;
        }
    }

    public void resumeThread() {
        synchronized (lock) {
            paused = false;
            lock.notifyAll();
        }
    }

    public int getProcessedDigits() {
        return processedDigits;
    }

    public byte[] getDigits() {
        return this.digits;
    }


    private static double sum(int m, int n) {
        double s = 0;
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
            s += term;
            power--;
            d += 8;
        }
        return s;
    }

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
