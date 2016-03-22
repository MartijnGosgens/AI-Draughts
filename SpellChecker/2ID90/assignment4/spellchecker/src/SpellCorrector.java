import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class SpellCorrector {
    final private CorpusReader cr;
    final private ConfusionMatrixReader cmr;
    
    final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz'".toCharArray();
    
    
    public SpellCorrector(CorpusReader cr, ConfusionMatrixReader cmr) 
    {
        this.cr = cr;
        this.cmr = cmr;
    }
    
    public String correctPhrase(String phrase)
    {
        if(phrase == null || phrase.length() == 0)
        {
            throw new IllegalArgumentException("phrase must be non-empty.");
        }
            
        String[] words = phrase.split(" ");
        String finalSuggestion = "";
        
        /** CODE TO BE ADDED **/
        
        return finalSuggestion.trim();
    }    
      
    /** returns a map with candidate words and their noisy channel probability. **/
    public Map<String,Double> getCandidateWords(String word)
    {
        Map<String,Double> mapOfWords = new HashMap<>();
        
        // All misspelled words have Damerau-Levenstein distance of 1 to the
        // correct words. Therefore the words only need to be altered by one
        // insertion, deletion, transposition or substitution.
        
        // Find all candidate words from insertions.
        for (int i = 0; i < word.length() + 1; ++ i) {
            for (char c : ALPHABET) {
                String possibleCandidate = insert(word, i, c);
                if (cr.inVocabulary(possibleCandidate)) {
                    mapOfWords.put(possibleCandidate, 
                            noisyChannelProbability(word, possibleCandidate));
                }
            }
        }
        
        // Find all candidate words from deletions.
        for (int i = 0; i < word.length(); ++ i) {
            String possibleCandidate = delete(word, i);
            if (cr.inVocabulary(word)) {
                    mapOfWords.put(possibleCandidate, 
                            noisyChannelProbability(word, possibleCandidate));
            }
        }
        
        // Find all candidate words from transpositions.
        for (int i = 0; i < word.length() - 1; ++ i) {
            String possibleCandidate = transpose(word, i);
            if (cr.inVocabulary(word)) {
                    mapOfWords.put(possibleCandidate, 
                            noisyChannelProbability(word, possibleCandidate));
            }
        }
        
        // Find all candidate words from substitutions.
        for (int i = 0; i < word.length(); ++ i) {
            for (char c : ALPHABET) {
                String possibleCandidate = substitute(word, i, c);
                if (cr.inVocabulary(possibleCandidate)) {
                    mapOfWords.put(possibleCandidate, 
                            noisyChannelProbability(word, possibleCandidate));
                }
            }
        }
        
        return mapOfWords;
    }        

    /**
     * Inserts {@code insertion} at the {@code index}-th position of 
     * {@code original}.
     * @param original The original word
     * @param index The index where the character needs to be inserted.
     * @param insertion The character to be inserted.
     * @return The new string.
     */
    String insert(String original, int index, char insertion) {
        return original.substring(0, index)
                + insertion 
                + (index >= original.length() ? "" : original.substring(index));
    }
    
    /**
     * Deletes the character at {@code index} from {@code original}.
     * @param original
     * @param index
     * @return 
     */
    String delete(String original, int index) {
        return original.substring(0,index) 
                + (index < original.length() ? original.substring(index + 1) : "");
    }
    
    /**
     * Swaps the characters at {@code index} and {@code index + 1} from 
     * {@code original}.
     * @param original
     * @param index
     * @return 
     */
    String transpose(String original, int index) {
        return original.substring(0, index)
                + original.charAt(index + 1)
                + original.charAt(index)
                + (index >= original.length() ? "" : original.substring(index + 2));
    }
    
    /**
     * Substitutes the character at {@code index} from {@code original} with
     * {@code insertion}.
     * @param original
     * @param index
     * @param insertion
     * @return 
     */
    String substitute(String original, int index, char insertion) {
        return original.substring(0, index)
                + insertion 
                + (index < original.length() ? original.substring(index + 1) : "");
    }
    
    /**
     * Gives P({@code observation}|{@code word}), the chance that 
     * {@code observation} exits the noisy channel while {@code word} has
     * entered it.
     * @param observation The observed word
     * @param word The original word
     * @return The probability that {@code observed} has been typed while
     * meaning to type {@code word}.
     */
    Double noisyChannelProbability(String observation, String word) {
        return 0d;
    }
}