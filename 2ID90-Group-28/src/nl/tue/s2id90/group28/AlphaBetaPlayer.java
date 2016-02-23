/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.s2id90.group28;

import java.util.List;
import nl.tue.s2id90.draughts.DraughtsState;
import nl.tue.s2id90.draughts.player.DraughtsPlayer;
import org10x10.dam.game.Move;

/**
 *
 * @author s147569
 */
public class AlphaBetaPlayer extends DraughtsPlayer {

    private final int DEPTH_LIMIT = 4;
    
    @Override
    public Move getMove(DraughtsState s) {
        List<Move> moves = s.getMoves();
        Integer bestValue = Integer.MIN_VALUE;
        Move bestMove = moves.get(0);
        for (Move m : moves) {
            s.doMove(m);
            Integer value = alphaBetaMax(s, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
            if (value > bestValue) {
                bestValue = value;
                bestMove = m;
            }
            s.undoMove(m);
        }
        return bestMove;
    }
    
    /**
     * Evaluates the state {@code s}..
     * @param s the state
     * @return the evaluated value
     */
    private Integer getValue(DraughtsState s) {
        return 0;
    }
    
    private Integer alphaBetaMax(DraughtsState s, Integer alpha, Integer beta,
                                int depth) {
        if (depth>DEPTH_LIMIT) {
            return getValue(s);
        } else {
            List<Move> moves = s.getMoves();
            for (Move m : moves) {
               s.doMove(m);
               Integer min = alphaBetaMin(s, alpha, beta, depth + 1);
               alpha = (alpha > min ? alpha : min);
               s.undoMove(m);
               if (alpha >= beta) {
                   return beta;
               }
            }
            return alpha;
        }
    }
    
    private Integer alphaBetaMin(DraughtsState s, Integer alpha, Integer beta,
                                int depth) {
        if (depth>DEPTH_LIMIT) {
            return getValue(s);
        } else {
            List<Move> moves = s.getMoves();
            for (Move m : moves) {
               s.doMove(m);
               Integer max = alphaBetaMax(s, alpha, beta, depth + 1);
               beta = (beta > max ? beta : max);
               s.undoMove(m);
               if (alpha >= beta) {
                   return alpha;
               }
            }
            return beta;
        }
    }
    
}
