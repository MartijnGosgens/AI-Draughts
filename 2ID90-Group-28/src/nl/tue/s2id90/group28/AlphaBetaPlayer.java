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
 * @author s140511
 */
public class AlphaBetaPlayer extends DraughtsPlayer {

    private boolean isBlack;
    
    private boolean stopped = false;
    
    @Override
    public void stop() { stopped = true; }
    
    @Override
    public Move getMove(DraughtsState s) {
        List<Move> moves = s.getMoves();
        Move bestMove = null;
        
        //int bruteForceValue = bruteForceMax(s, 0);
        
        // See whether the player plays black or white
        isBlack = moves.get(0).isBlackMove();
        
        // Use iterative deepening
        for (int d = 3; d < 100; d++) {
            try {
                bestMove = alphaBetaSearch(s, d);
            } catch (AIStoppedException e) {
                System.out.println("Depth level "+d+" reached.");
                return bestMove;
            }
        }
        
        // Test whether alpha-beta gets the same value as brute force.
//        if (bruteForceValue != bestValue) {
//            System.out.println("AlphaBeta does not work, brute force: "
//                    + bruteForceValue + ", alpha-beta: " + bestValue);
//        }
        return bestMove;
    }
    
    /**
     * Evaluates the state {@code s}..
     * @param s the state
     * @return the evaluated value
     */
    private double getValue(DraughtsState s) {
        int[] pieces = s.getPieces();
        double blackValue = 0;
        double whiteValue = 0;
        //retrieve values of each piece
        for(int p = 1; p < pieces.length; p++){
            switch (pieces[p]){
                case s.BLACKPIECE:
                    blackValue += getPieceValue(0, getRowNumber(p), false);
                    break;
                case s.BLACKKING:
                    blackValue += getPieceValue(getPosition(p), 0, true);
                    break;
                case s.WHITEPIECE:
                    whiteValue += getPieceValue(0, getRowNumber(p), false);
                    break;
                case s.WHITEKING:
                    whiteValue += getPieceValue(getPosition(p), 0, true);
                    break;
            }
        }
        
        //return total value as a ratio of black and white
        if(isBlack){
            try {
                return blackValue / whiteValue;
            } catch (ArithmeticException e) {
                System.out.println("whiteValue = 0, white has lost the game.");
                return Integer.MAX_VALUE;
            }
        } else {
            try {
                return whiteValue / blackValue;
            } catch (ArithmeticException e) {
                System.out.println("blackValue = 0, black has lost the game.");
                return Integer.MAX_VALUE;
            }
        }
    }
    
    private int getRowNumber(int p){
        return (int) (p / 5 - 0.1);
    }
    
//    MIDDLE = 0
//    LEFTSIDE = 1
//    RIGHTSIDE = 2
//    TOPSIDE = 3
//    BOTTOMSIDE = 4
    private int getPosition(int p){
        switch (p){
            case 1: case 2: case 3: case 4: case 5:
                return 3;
            case 46: case 47: case 48: case 49: case 50:
                return 4;
            default:
                if (p % 5 == 0 && p % 10 != 0){
                    return 2;
                } else if ((p + 1) % 5 == 0 && (p + 1) % 10 != 0) {
                    return 1;
                } else {
                    return 0;
                }
        }
    }
    
    private int getPieceValue(int position, int rowNumber, boolean isKing){
        int value = 100;
        if (isKing) {
            value *= 2; //double the value for kings
            if (position > 0) value += 20; //King is on the side
        } else {
            if (rowNumber == 9){ //defensive pieces are worth more
                value += 20;
            } else { 
                value += (8-rowNumber) * 3; //the closer to becoming king, the more worth
            }
        }

        return value;
    }
    
    private Move alphaBetaSearch(DraughtsState s, int depth) throws AIStoppedException{
        List<Move> moves = s.getMoves();
        
        int bestValue = Integer.MIN_VALUE;
        Move bestMove = null;
        
        // Find the best move
        for (Move m : moves) {
            s.doMove(m);
            int value = alphaBetaMin(s, Integer.MIN_VALUE, Integer.MAX_VALUE, depth);
            if (value > bestValue) {
                bestValue = value;
                bestMove = m;
            }
            s.undoMove(m);
        }
        
        return bestMove;
    }
    
    private int alphaBetaMax(DraughtsState s, int alpha, int beta,
                            int depth) throws AIStoppedException{
        checkAndThrow();
        if (depth < 0) {
            return getValue(s);
        } else {
            List<Move> moves = s.getMoves();
            for (Move m : moves) {
                s.doMove(m);
                int min = alphaBetaMin(s, alpha, beta, depth - 1);
                alpha = (alpha > min ? alpha : min);
                s.undoMove(m);
                if (alpha >= beta) {
                    return beta;
                }
            }
            return alpha;
        }
    }
    
    private int alphaBetaMin(DraughtsState s, int alpha, int beta,
                            int depth) throws AIStoppedException{
        checkAndThrow();
        if (depth < 0) {
            return getValue(s);
        } else {
            List<Move> moves = s.getMoves();
            for (Move m : moves) {
                s.doMove(m);
                int max = alphaBetaMax(s, alpha, beta, depth - 1);
                beta = (beta < max ? beta : max);
                s.undoMove(m);
                if (alpha >= beta) {
                    return alpha;
                }
            } 
            return beta;
        }
    }
    
    int bruteForceMax(DraughtsState s, int depth) {
        if (depth < 0) {
            return getValue(s);
        } else {
            List<Move> moves = s.getMoves();
            if (moves.size() > 0) {
                int max = Integer.MIN_VALUE;
                for (Move m : moves) {
                    s.doMove(m);
                    int value = bruteForceMin(s, depth - 1);
                    max = (value > max ? value : max);
                    s.undoMove(m);
                }
                return max;
            } else {
                return getValue(s);
            }
        }
    }
    
    int bruteForceMin(DraughtsState s, int depth) {
        if (depth < 0) {
            return getValue(s);
        } else {
            List<Move> moves = s.getMoves();
            if (moves.size() > 0) {
                int min = Integer.MAX_VALUE;
                for (Move m : moves) {
                    s.doMove(m);
                    int value = bruteForceMax(s, depth - 1);
                    min = (value < min ? value : min);
                    s.undoMove(m);
                }
                return min;
            } else {
                return getValue(s);
            }
        }
    }
    
    private void checkAndThrow() throws AIStoppedException {
        if (stopped) {
            stopped = false;
            throw new AIStoppedException();
        }
    }
}
