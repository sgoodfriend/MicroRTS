/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.minimax.ABCD;

import ai.AI;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.EvaluationFunctionWithActions;
import ai.minimax.MiniMaxResult;
import java.util.List;
import java.util.Random;
import rts.GameState;
import rts.PlayerAction;
import rts.PlayerActionGenerator;

/**
 *
 * @author santi:
 * 
 * - This is the ABCD (Alpha-Beta considering durations) 
 *   algorithm presented by Churchill and Buro at AIIDE 2012
 * - In particular, this version uses the "alt" tree alteration technique to improve the
 *   estimation of the alphabeta values when there are simultaneous moves.
 */
public class DownsamplingABCD extends AI {
    public static int DEBUG = 0;
    
    // reset at each execution of minimax:
    int nLeaves = 0;
    
    int max_depth_so_far = 0;
    long max_branching_so_far = 0;
    long max_leaves_so_far = 0;
    
    int MAXDEPTH = 4;
    AI playoutAI = null;
    int maxPlayoutTime = 100;
    EvaluationFunction ef = null;
    
    Random r = new Random();
    double downsampling = 1.0;  // at each node, each action is explored with "downsampling" probability
                                // except the "noop" action, which is always explored.
    
    public DownsamplingABCD(int md, double a_downsampling, AI a_playoutAI, int a_maxPlayoutTime, EvaluationFunction a_ef) {
        MAXDEPTH = md;
        playoutAI = a_playoutAI;
        maxPlayoutTime = a_maxPlayoutTime;
        ef = a_ef;
        downsampling = a_downsampling;
    }
            
    
    public void reset() {
    }
    
    public AI clone() {
        return new DownsamplingABCD(MAXDEPTH, downsampling, playoutAI, maxPlayoutTime, ef);
    }     
    
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        
        if (gs.canExecuteAnyAction(player) && gs.winner()==-1) {
            PlayerAction pa = ABCD(player, gs, MAXDEPTH); 
            pa.fillWithNones(gs, player);
            return pa;
        } else {
            return new PlayerAction();
        }

    }
    
    
    public PlayerAction ABCD(int player, GameState gs, int depthLeft) throws Exception {
        long start = System.currentTimeMillis();
        float alpha = -EvaluationFunctionWithActions.VICTORY;
        float beta = EvaluationFunctionWithActions.VICTORY;
        int maxplayer = player;
        int minplayer = 1 - player;
        if (DEBUG>=1) System.out.println("Starting ABCD... " + player);
        if (nLeaves>max_leaves_so_far) max_leaves_so_far = nLeaves;
        nLeaves = 0;
        MiniMaxResult bestMove = ABCD(gs, maxplayer, minplayer, alpha, beta, depthLeft, maxplayer);
        if (DEBUG>=1) System.out.println("ABCD: " + bestMove + " in " + (System.currentTimeMillis()-start));
        return bestMove.action;
    }
    

    public MiniMaxResult ABCD(GameState gs, int maxplayer, int minplayer, float alpha, float beta, int depthLeft, int nextPlayerInSimultaneousNode) throws Exception {
//        System.out.println("realTimeMinimaxAB(" + alpha + "," + beta + ") at " + gs.getTime());
//        gs.dumpActionAssignments();
        
        if (depthLeft<=0 || gs.winner()!=-1) {
            nLeaves++;
            
            // Run the play out:
            GameState gs2 = gs.clone();
            AI playoutAI1 = playoutAI.clone();
            AI playoutAI2 = playoutAI.clone();
            int timeOut = gs2.getTime() + maxPlayoutTime;
            boolean gameover = false;
            while(!gameover && gs2.getTime()<timeOut) {
                if (gs2.isComplete()) {
                    gameover = gs2.cycle();
                } else {
                    gs2.issue(playoutAI1.getAction(0, gs2));
                    gs2.issue(playoutAI2.getAction(1, gs2));
                }
            }            
            
//            System.out.println("Eval (at " + gs.getTime() + "): " + EvaluationFunction.evaluate(maxplayer, minplayer, gs));
//            System.out.println(gs);
            return new MiniMaxResult(null,ef.evaluate(maxplayer, minplayer, gs2), gs2);
        }
        
        int toMove = -1;        
        if (gs.canExecuteAnyAction(maxplayer)) {
            if (gs.canExecuteAnyAction(minplayer)) {
                toMove = nextPlayerInSimultaneousNode;
                nextPlayerInSimultaneousNode = 1 - nextPlayerInSimultaneousNode;
            } else {
                toMove = maxplayer;
            }
        } else {
            if (gs.canExecuteAnyAction(minplayer)) toMove = minplayer;
        }

        if (toMove == maxplayer) {
            PlayerActionGenerator actions = new PlayerActionGenerator(gs, maxplayer);
            long l = actions.getSize();
            if (l>max_branching_so_far) max_branching_so_far = l;
            if (depthLeft==2) System.out.println("l: " + l);
            MiniMaxResult best = null;
            PlayerAction next = null;
            do{
                next = actions.getNextAction(-1);
                if (next!=null) {
                    if (best==null || r.nextDouble()<=downsampling) {
                        GameState gs2 = gs.cloneIssue(next);
                        MiniMaxResult tmp = ABCD(gs2, maxplayer, minplayer, alpha, beta, depthLeft-1, nextPlayerInSimultaneousNode);
                        alpha = Math.max(alpha,tmp.evaluation);
                        if (best==null || tmp.evaluation>best.evaluation) {
                            best = tmp;
                            best.action = next;
                        }
                        if (beta<=alpha) return best;                
                    }
                }
            }while(next!=null);
            return best;
        } else if (toMove == minplayer) {
            PlayerActionGenerator actions = new PlayerActionGenerator(gs, minplayer);
            long l = actions.getSize();
            if (l>max_branching_so_far) max_branching_so_far = l;
            MiniMaxResult best = null;
            PlayerAction next = null;
            do{
                next = actions.getNextAction(-1);
                if (next!=null) {
                    if (best==null || r.nextDouble()<=downsampling) {
                        GameState gs2 = gs.cloneIssue(next);
                        MiniMaxResult tmp = ABCD(gs2, maxplayer, minplayer, alpha, beta, depthLeft-1, nextPlayerInSimultaneousNode);
                        beta = Math.min(beta,tmp.evaluation);
                        if (best==null || tmp.evaluation<best.evaluation) {
                            best = tmp;
                            best.action = next;
                        }
                        if (beta<=alpha) return best;
                    }
                }
            }while(next!=null);
            return best;
        } else {
            GameState gs2 = gs.clone();
            while(gs2.winner()==-1 && 
                  !gs2.gameover() && 
                  !gs2.canExecuteAnyAction(maxplayer) && 
                  !gs2.canExecuteAnyAction(minplayer)) gs2.cycle();
            return ABCD(gs2, maxplayer, minplayer, alpha, beta, depthLeft, nextPlayerInSimultaneousNode);
        }
    }       
}