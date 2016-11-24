package pacman;

import games.math.Vector2d;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * User: Simon
 * Date: 09-Mar-2007
 * Time: 11:33:00
 * The purpose of this is to capture the state of the game to
 * give to a decision making agent.
 * <p/>
 * The state is based on analysing a screen image, and may
 * give incorrect readings at any given instant - for example,
 * power pills flash, but no account is taken of this, so if
 * a power pill is still in the game, but the screen was captured
 * while it was in the 'blinked off' state, then the GameState will
 * indicate that there is no power pill at that location (in fact,
 * the power pills flash in unison, so it could appear that there
 * were no power pills, when in fact they were all present in the
 * true game state.
 */
public class GameState implements Drawable {
    // might as well have separate collections for each item?

    static int strokeWidth = 5;
    static Stroke stroke =  new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
    Collection<ConnectedSet> pills;

    Collection<ConnectedSet> ghosts;
    ArrayList<ConnectedSet> ghostPositions;
    ArrayList<ConnectedSet> ediblePositions;
    ArrayList<ConnectedSet> powerPills;
    boolean eat = false;
    int iter;

    public Agent agent;
    public int state;
    Vector2d closestPill;
    Vector2d closestGhost;
    Vector2d closestEdible;
    Vector2d closestPowerPill;
    Vector2d tmp;

    static int nFeatures = 13;
    double[] vec;


    static HashMap<Integer,Integer> ghostLut = new HashMap<Integer,Integer>();
    static {
        // map these into positions of ghost rather than anything else -
        ghostLut.put(MsPacInterface.blinky, 0);
        ghostLut.put(MsPacInterface.inky, 1);
        ghostLut.put(MsPacInterface.pinky, 2);
        ghostLut.put(MsPacInterface.sue, 3);
        ghostLut.put(MsPacInterface.edible, 4);
    }

    public GameState() {
        agent = new Agent();
        tmp = new Vector2d();
        vec = new double[nFeatures];
        state = 0;
        ghostPositions = new ArrayList<>();
        ediblePositions = new ArrayList<>();
        powerPills = new ArrayList<>();
        iter = 0;
    }

    public void reset() {
        closestPill = null;
        closestGhost = null;
        closestEdible = null;
        closestPowerPill = null;
    }

    public void update(ConnectedSet cs, int[] pix) {
    	
        if (cs.isPacMan()) {
            agent.update(cs, pix);
        }
        else if (cs.ghostLike()) {
            // update the state of the ghost distance
        	if (cs.edible()) {
                this.state = 3;
                iter += 1;
                eat = true;
                if (! ediblePositions.contains(cs)) {
                    ediblePositions.add(cs);
                } else {
                    int edibleIndex = ediblePositions.indexOf(cs);
                    ediblePositions.remove(edibleIndex);
                   // ediblePositions.add(cs);
                }
                
                tmp.set(cs.x, cs.y);
                //tmp.set(ediblePositions.get(0).x,ediblePositions.get(0).y);
               // ediblePositions.remove(0);
                if (closestEdible == null) {
                    closestEdible = new Vector2d(tmp);
                } else if (tmp.dist(agent.cur) < closestEdible.dist(agent.cur) && !isInBox(tmp)) {
                    closestEdible.set(tmp);
                }
            } else {
                if (iter >= 20) {
                    eat = false;
                    iter = 0;
                } else {
                    iter += 1;
                }
                if (! ghostPositions.contains(cs)) {
                    ghostPositions.add(cs);
                } else {
                    int ghostIndex = ghostPositions.indexOf(cs);
                    ghostPositions.remove(ghostIndex);
                    ghostPositions.add(cs);
                }

                tmp.set(cs.x, cs.y);
                if (closestGhost == null) {
                    closestGhost = new Vector2d(tmp);
                } else if (tmp.dist(agent.cur) < closestGhost.dist(agent.cur)) {
                    closestGhost.set(tmp);
                }
            }
        } else if (cs.powerPill()) {
        	state = 3;
            if (! powerPills.contains(cs))
                powerPills.add(cs);

            tmp.set(cs.x, cs.y);
            if (closestPowerPill == null) {
                closestPowerPill = new Vector2d(tmp);
            } else if (tmp.dist(agent.cur) < closestPowerPill.dist(agent.cur)) {
                closestPowerPill.set(tmp);
            }
        } else if (cs.pill()) {
            // keep track of the position of the closest pill
            tmp.set(cs.x, cs.y);
            if (closestPill == null) {
                closestPill = new Vector2d(tmp);
            } else if (tmp.dist(agent.cur) < closestPill.dist(agent.cur)) {
                closestPill.set(tmp);
            }
        }
    }
    
    public boolean isInBox(Vector2d pos){
    	if((pos.x<=141 && pos.y<=133) && (pos.x<=141 && pos.y>=103) && (pos.x>=87 && pos.y>=103) && (pos.x>=87 && pos.y<=133))
    		return true;
     return false;
    }

    public void updateState() {
        int st = this.state; // Comer pills
        int danger = 40, warning = 60;
        if (this.eat == true && closestEdible != null) {
            st = 3; // Comer edible
            //if (closestPowerPill != null && (agent.cur.dist(closestGhost) >= warning && agent.cur.dist(closestPowerPill) >= danger)){
            //	st = 1;
            //}
        } else if (closestGhost != null) {
            if (agent.cur.dist(closestGhost) <= danger) {
                st = 1; // Escapar de ghosts
            } else if (closestPowerPill != null && (agent.cur.dist(closestGhost) <= warning && agent.cur.dist(closestPowerPill) <= danger)) {
                st = 2; // Comer PowerPill
            } else if (agent.cur.dist(closestGhost) > warning) {
                st = 0;
            }
        }
        this.state = st;
    }

    public void draw(Graphics gg, int w, int h) {
        //To change body of implemented methods use File | Settings | File Templates.
        Graphics2D g = (Graphics2D) gg;

        if (agent != null) {
            agent.draw(g, w, h);
        }
        if (closestPill != null && agent != null) {
            g.setStroke(stroke);
            g.setColor(Color.cyan);
            g.drawLine((int) closestPill.x, (int) closestPill.y, (int) agent.cur.x, (int) agent.cur.y);
        }
    }
}
