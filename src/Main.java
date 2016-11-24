import pacman.*;
import stats.StatisticalSummary;
import stats.StatisticalTests;
import stats.Stats;
import utilities.*;


class Main {
  public static void main(String[] args) throws Exception {
    int delay = 10;
    boolean display = true;

    int x = 528;
    int y = 260;

    MsPacInterface ms = new MsPacInterface(display);
    StatisticalSummary ss = new StatisticalSummary();
    PacMover pm = new PacMover();
    DirectionComponent dc = DirectionComponent.easyUse();
    PacAgent pa = new LeftRight();

    TestMonitor tm = new TestMonitor();
    while(true) {
        ElapsedTimer t = new ElapsedTimer();

        int[] pix = ms.getPixels();

        ms.analyseComponents(pix);
        ms.ce.gs.updateState();

        ss.add(t.elapsed());
        int action = ms.ce.gs.agent.move(ms.ce.gs);
        // int action = pa.move(ms.ce.gs);
        pm.move(action);
        tm.log(action, ms.ce.gs);

        if (ms.display)
            dc.update(action);

        Thread.sleep(delay);
    }
  }
}
