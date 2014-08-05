import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Math.abs;

/**
 * Created with IntelliJ IDEA.
 * User: Bostjan
 * Date: 15.4.2013
 * Time: 7:41
 * To change this template use File | Settings | File Templates.
 */
public class DiningPhilosophers {
    final CountDownLatch countdown ;

    public static void main(String[] args) {

        DiningPhilosophers phi = new DiningPhilosophers();


    }

    public DiningPhilosophers() {
        int NumberOfPhilosophers = 5;
        int LengthOfMeal = 5;
        countdown = new CountDownLatch(NumberOfPhilosophers);
        TableMonitor mainTable = new TableMonitor(NumberOfPhilosophers);
        Philosopher[] philosophers = new Philosopher[NumberOfPhilosophers];
        for (int i = 0; i < NumberOfPhilosophers; i++) {
            philosophers[i] = new Philosopher(LengthOfMeal, mainTable, i);
        }
        System.out.println("Dinner is starting!");
        for (Philosopher phi : philosophers) {
            phi.start();
        }
        try {
            countdown.await();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        System.out.println("Dinner is over!");

    }

    class Philosopher extends Thread {

        private int timesToEat;
        private TableMonitor table;
        private int philosopherId;

        Philosopher(int timesToEat, TableMonitor table, int PhilosopherId) {
            this.timesToEat = timesToEat;
            this.table = table;
            philosopherId = PhilosopherId;
        }

        @Override
        public void run() {

            while (timesToEat > 0) {

                try {
                    table.PickUpChopSticks(philosopherId);
                } catch (InterruptedException e) {
                    table.PutDownChopSticks(philosopherId);
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                System.out.println("Philosopher " + philosopherId + " picks up left chopstick.");
                System.out.println("Philosopher " + philosopherId + " picks up right chopstick.");
                System.out.println("Philosopher " + philosopherId + " eats.");

                table.PutDownChopSticks(philosopherId);
                timesToEat--;
            }
                countdown.countDown();
            return;

        }
    }

    public enum Status {
        HUNGRY, EATING, THINKING
    }

    public class TableMonitor {

        private final int NumOfPhilosophers;
        private final Lock lock = new ReentrantLock();
        private volatile Status[] state;
        private volatile Condition[] self;


        public TableMonitor(int numOfPhi) {
            NumOfPhilosophers = numOfPhi;
            state = new Status[numOfPhi];
            self = new Condition[numOfPhi];
            for (int i = 0; i < NumOfPhilosophers; i++) {
                state[i] = Status.THINKING;
                self[i] = lock.newCondition();
                numOfPhi--;
            }

        }

        public void PickUpChopSticks(int i) throws InterruptedException {
            lock.lock();
            try {
                state[i] = Status.HUNGRY;
                TryToEat(i);
                if (state[i] != Status.EATING)
                    self[i].await();
            } finally {
                lock.unlock();
            }

        }

        public void PutDownChopSticks(int i) {
            lock.lock();
            try {
                state[i] = Status.THINKING;
              //  System.out.println("Down" + i + " / " + (i + 1) % NumOfPhilosophers + " / " + (i - 1) % NumOfPhilosophers);
                TryToEat(Mod((i + 1), NumOfPhilosophers));
                TryToEat(Mod((i - 1), NumOfPhilosophers));
            } finally {
                lock.unlock();
            }

        }

        private void TryToEat(int i) {
           // System.out.println(i + " / " + (i + 1) % NumOfPhilosophers + " / " + ((i - 1) % NumOfPhilosophers));
            if ((state[Mod((i + 1), NumOfPhilosophers)] != Status.EATING)
                    && (state[Mod((i - 1), NumOfPhilosophers)] != Status.EATING) &&
                    (state[i] == Status.HUNGRY)) {
                state[i] = Status.EATING;
                self[i].signal();

            }


        }
    }

    public  int Mod(int n, int m) {
        return (n < 0) ? (m - (abs(n) % m)) % m : (n % m);
    }

}
