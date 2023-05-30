/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.jni;

import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.ParameterSpecification;
import ai.evaluation.SimpleEvaluationFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import rts.GameState;
import rts.InvalidPlayerActionStats;
import rts.PlayerAction;
import rts.units.UnitTypeTable;
import util.NDBuffer;
import util.Pair;

/**
 *
 * @author costa
 */
public class JNIAI extends AIWithComputationBudget implements JNIInterface {
    UnitTypeTable utt = null;
    double reward = 0.0;
    double oldReward = 0.0;
    boolean firstRewardCalculation = true;
    SimpleEvaluationFunction ef = new SimpleEvaluationFunction();
    InvalidPlayerActionStats ipas;
    int maxAttackRadius;

    public JNIAI(int timeBudget, int iterationsBudget, UnitTypeTable a_utt) {
        super(timeBudget, iterationsBudget);
        utt = a_utt;
        maxAttackRadius = utt.getMaxAttackRange() * 2 + 1;
    }

    public double computeReward(int maxplayer, int minplayer, GameState gs) throws Exception {
        // do something
        if (firstRewardCalculation) {
            oldReward = ef.evaluate(maxplayer, minplayer, gs);
            reward = 0;
            firstRewardCalculation = false;
        } else {
            double newReward = ef.evaluate(maxplayer, minplayer, gs);
            reward = newReward - oldReward;
            oldReward = newReward;
        }
        return reward;
    }

    public PlayerAction getAction(int player, GameState gs, int[][] action) throws Exception {
        Pair<PlayerAction, InvalidPlayerActionStats> p = PlayerAction.fromActionArrays(action, gs, utt, player, maxAttackRadius);
        p.m_a.fillWithNones(gs, player, 1);
        ipas = p.m_b;
        return p.m_a;
    }

    public PlayerAction getActionFromBuffer(int player, GameState gs, int clientOffset, NDBuffer action) throws Exception {
        Pair<PlayerAction, InvalidPlayerActionStats> p = PlayerAction.fromActionBuffer(clientOffset, action, gs, utt, player, maxAttackRadius);
        p.m_a.fillWithNones(gs, player, 1);
        ipas = p.m_b;
        return p.m_a;
    }

    public int[][][] getObservation(int player, GameState gs) throws Exception {
        return gs.getMatrixObservation(player);
    }

    public ArrayList[] getEntityObservation(int player, GameState gs) throws Exception {
        return gs.getEntityObservation(player);
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
    }

    @Override
    public AI clone() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ParameterSpecification> getParameters() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String computeInfo(int player, GameState gs) throws Exception {
        Map<String, Object> data = new HashMap<String, Object>();
            data.put("invalid_action_stats", ipas);
        Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();
        return gson.toJson(data);
    }
    
}
