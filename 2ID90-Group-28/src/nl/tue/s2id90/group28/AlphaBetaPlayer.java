/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.s2id90.group28;

import java.util.List;
import java.util.Objects;
import nl.tue.s2id90.draughts.DraughtsState;
import nl.tue.s2id90.draughts.player.DraughtsPlayer;
import org10x10.dam.game.Move;

/**
 *
 * @author s147569
 */
public class AlphaBetaPlayer extends DraughtsPlayer {

    private final int DEPTH_LIMIT = 4;
    private boolean isBlack;
    
    @Override
    public Move getMove(DraughtsState s) {
        List<Move> moves = s.getMoves();
        Integer bestValue = Integer.MIN_VALUE;
        Move bestMove = moves.get(0);
        
        Integer bruteForceValue = bruteForceMax(s, 0);
        
        // See whether the player plays black or white
        isBlack = moves.get(0).isBlackMove();
        for (Move m : moves) {
            s.doMove(m);
            Integer value = alphaBetaMax(s, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
            if (value > bestValue) {
                bestValue = value;
                bestMove = m;
            }
            s.undoMove(m);
        }
        
        // Test whether alpha-beta gets the same value as brute force.
        if (!Objects.equals(bruteForceValue, bestValue)) {
            System.out.println("AlphaBeta does not work, brute force: "
                    + bruteForceValue + ", alpha-beta: " + bestValue);
        }
        return bestMove;
    }
    
    /**
     * Evaluates the state {@code s}..
     * @param s the state
     * @return the evaluated value
     */
    private Integer getValue(DraughtsState s) {
        int[] pieces = s.getPieces();
        int[] occurences = {0,0,0,0,0};
        int value;
        int blackValue;
        int whiteValue;
        for(int p = 1; p < pieces.length; p++){
            occurences[p]++;
        }
        blackValue = occurences[s.BLACKPIECE] + 2*occurences[s.BLACKKING];
        whiteValue = occurences[s.WHITEPIECE] + 2*occurences[s.WHITEKING];
//        TODO: calculate value for black player or white player?
//        if(player == black){
//            value = blackValue - whiteValue;
//        } else {
//            value = whiteValue - blackValue;
//        }
        value = blackValue - whiteValue;
        return value;
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
    
    Integer bruteForceMax(DraughtsState s, int depth) {
        if (depth>DEPTH_LIMIT) {
            return getValue(s);
        } else {
            List<Move> moves = s.getMoves();
            Integer max = Integer.MIN_VALUE;
            for (Move m : moves) {
                s.doMove(m);
                Integer value = bruteForceMin(s, depth + 1);
                max = (value > max ? value : max);
                s.undoMove(m);
            }
            return max;
        }
    }
    
    Integer bruteForceMin(DraughtsState s, int depth) {
        if (depth>DEPTH_LIMIT) {
            return getValue(s);
        } else {
            List<Move> moves = s.getMoves();
            Integer min = Integer.MAX_VALUE;
            for (Move m : moves) {
                s.doMove(m);
                Integer value = bruteForceMax(s, depth + 1);
                min = (value > min ? value : min);
                s.undoMove(m);
            }
            return min;
        }
    }
}
