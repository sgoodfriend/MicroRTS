/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.jni;
import java.util.ArrayList;

import rts.GameState;
import rts.PlayerAction;

/**
 *
 * @author costa
 */

public interface JNIInterface {
	public PlayerAction getAction(int player, GameState gs, int[][] action) throws Exception;
    public int[][][] getObservation(int player, GameState gs) throws Exception;
    public ArrayList[] getEntityObservation(int player, GameState gs) throws Exception;
    public void reset();
    public double computeReward(int i, int j, GameState gs) throws Exception;
    public String computeInfo(int player, GameState gs) throws Exception;
}