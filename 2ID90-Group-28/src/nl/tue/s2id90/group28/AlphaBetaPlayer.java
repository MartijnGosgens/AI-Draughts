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
        return bestMove;
    }
    
    /**
     * Evaluates the state {@code s}..
     * @param s the state
     * @return the evaluated value
     */
    private double getValue(DraughtsState s, List<Move> moves) {
        
        // Check whether this player has won or lost
        if (moves.isEmpty() &&  (s.isWhiteToMove() && isBlack
             || !s.isWhiteToMove() && !isBlack)) {
            return Integer.MAX_VALUE;
        } else if (moves.isEmpty()){
            return Integer.MIN_VALUE+1;
        }
        
        int[] pieces = s.getPieces();
        double blackValue = 0;
        double whiteValue = 0;
        //retrieve values of each piece
        for (int p = 1; p < pieces.length; p++) {
            switch (pieces[p]){
                case DraughtsState.BLACKPIECE:
                    blackValue += getPieceValue(0, getRowNumber(p), false);
                    break;
                case DraughtsState.BLACKKING:
                    blackValue += getPieceValue(getPosition(p), 0, true);
                    break;
                case DraughtsState.WHITEPIECE:
                    whiteValue += getPieceValue(0, getRowNumber(p), false);
                    break;
                case DraughtsState.WHITEKING:
                    whiteValue += getPieceValue(getPosition(p), 0, true);
                    break;
            }
        }
        
        // Return total value as a ratio of black and white
        // Note: divide by zero cannot occur since then the function would have
        // already been terminated.
        if(isBlack){
            return blackValue / whiteValue;
        } else {
            return whiteValue / blackValue;
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
        if (p < 6) {
            return 3;
        } else if (p > 45) {
            return 4;
        } else if (p % 5 == 0 && p % 10 != 0){
            return 2;
        } else if ((p + 1) % 5 == 0 && (p + 1) % 10 != 0) {
            return 1;
        } else {
            return 0;
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
        
        double bestValue = Integer.MIN_VALUE;
        Move bestMove = null;
        
        // Find the best move
        for (Move m : moves) {
            s.doMove(m);
            double value = alphaBetaMin(s, Integer.MIN_VALUE, Integer.MAX_VALUE, depth);
            if (value > bestValue) {
                bestValue = value;
                bestMove = m;
            }
            s.undoMove(m);
        }
        
        return bestMove;
    }
    
    private double alphaBetaMax(DraughtsState s, double alpha, double beta,
                            int depth) throws AIStoppedException{
        checkAndThrow();
        List<Move> moves = s.getMoves();
        if (depth < 0 || moves.isEmpty()) {
            return getValue(s, moves);
        } else {
            for (Move m : moves) {
                s.doMove(m);
                double min = alphaBetaMin(s, alpha, beta, depth - 1);
                alpha = (alpha > min ? alpha : min);
                s.undoMove(m);
                if (alpha >= beta) {
                    return beta;
                }
            }
            
            // Ensure the value of each move is higher than the value of the
            // null move in alphaBetaSearch.
            return (alpha==Integer.MIN_VALUE ? alpha+1 : alpha);
        }
    }
    
    private double alphaBetaMin(DraughtsState s, double alpha, double beta,
                            int depth) throws AIStoppedException{
        checkAndThrow();
        List<Move> moves = s.getMoves();
        if (depth < 0 || moves.isEmpty()) {
            return getValue(s, moves);
        } else {
            for (Move m : moves) {
                s.doMove(m);
                double max = alphaBetaMax(s, alpha, beta, depth - 1);
                beta = (beta < max ? beta : max);
                s.undoMove(m);
                if (alpha >= beta) {
                    return alpha;
                }
            } 
            return beta;
        }
    }
    
    private void checkAndThrow() throws AIStoppedException {
        if (stopped) {
            stopped = false;
            throw new AIStoppedException();
        }
    }
}
