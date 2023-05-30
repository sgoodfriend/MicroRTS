/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.reward;

import java.util.List;

import rts.GameState;
import rts.PhysicalGameState;
import rts.TraceEntry;
import rts.UnitAction;
import rts.units.Unit;
import util.Pair;

/**
 *
 * @author costa
 */
public class WinLossRewardFunction extends RewardFunctionInterface{


    public void computeReward(int maxplayer, int minplayer, TraceEntry te, GameState afterGs) {
        reward = 0.0;
        done = false;
        if (afterGs.gameover()) {
            done = true;
            int winner = afterGs.winner();
            if (winner == maxplayer) {
                reward = 1.0;
            } else if (winner == minplayer) {
                reward = -1.0;
            } else {
                reward = 0.0;
            }
        }

    }
}
