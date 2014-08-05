import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created with IntelliJ IDEA.
 * User: Bostjan
 * Date: 8.4.2013
 * Time: 8:24
 * To change this template use File | Settings | File Templates.
 */
public class PingPong {
    interface BallListener {
        public void BallWasHit();
    }


    public static void main(String[] args) {
        PingPong pingPong = new PingPong();


    }

    public PingPong() {
        CountDownLatch latch = new CountDownLatch(2);
        Ball ball = new Ball(false); //Sets if we start with pong or ping false= Ping
        Game game = new Game(3); //Change Game Duration number of Exchanges
        ball.addListener(game);
        PingPongPlayer player1 = new PingPongPlayer(latch, game, ball, false); // last bool Set if thread pings or pongs
        PingPongPlayer player2 = new PingPongPlayer(latch, game, ball, true);
        System.out.println("Ready… Set… Go!");
        player1.start();
        player2.start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Done");

    }


    private class PingPongPlayer extends Thread {
        private final CountDownLatch _latch;
        private final Game _game;
        private final Ball _ball;
        private boolean iPong = false;


        public PingPongPlayer(CountDownLatch latch, Game game, Ball ball, boolean pong) {
            this._latch = latch;
            this._game = game;
            this._ball = ball;
            this.iPong = pong;
        }

        @Override
        public void run() {


            while (_game.AreWeStillPlaying()) {

                if (iPong) {
                    _ball.WaitForPong();
                    System.out.println("Pong");
                    _ball.Hit();
                } else {
                    _ball.WaitForPing();
                    System.out.println("Ping");
                    _ball.Hit();
                }

            }
            _latch.countDown();
        }
    }
}

class Game implements PingPong.BallListener {

    private volatile int _gameDuration = 4;
    private volatile boolean FirstHit = true;

    public Game(int gameDuration) {
        _gameDuration = gameDuration * 2;
    }


    public synchronized boolean AreWeStillPlaying() {

            return _gameDuration > 0;

    }


    @Override
    public synchronized void BallWasHit() {
        if (FirstHit) {
            FirstHit = false;      //First Round Is ignored once because both treads go in at once
            _gameDuration--;
        }
        _gameDuration--;

    }
}

class Ball {
    List<PingPong.BallListener> listeners = new ArrayList<PingPong.BallListener>();
    private volatile boolean OnPongSide = true;

    public Ball(boolean StartWithPong) {
        OnPongSide = StartWithPong;   
            }

    public void addListener(PingPong.BallListener toAdd) {
        listeners.add(toAdd);
    }

    public synchronized void Hit() {
        OnPongSide = !OnPongSide;
        for (PingPong.BallListener hl : listeners)
            hl.BallWasHit();
        notify();
    }

    public void WaitForPong() {
        synchronized (this) {
            try {
                if (!OnPongSide)
                    wait();
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public void WaitForPing() {
        synchronized (this) {
            try {
                if (OnPongSide)
                    wait();
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }


}





