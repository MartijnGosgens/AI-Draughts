import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

public class SpellCorrector {
    final private CorpusReader cr;
    final private ConfusionMatrixReader cmr;
    
    final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz'".toCharArray();
    final char[] SPACE_ALPHABET = "abcdefghijklmnopqrstuvwxyz ".toCharArray();
    
    
    public SpellCorrector(CorpusReader cr, ConfusionMatrixReader cmr) 
    {
        this.cr = cr;
        this.cmr = cmr;
    }
    
    /**
     * Recursively finds the best correction with at most two corrections.
     * @param previousSentence The corrected words
     * @param nextSentence The words that still might need correction.
     * @param currentProbability The probability of the previous corrections.
     * @param correctionsMade The number of words altered thusfar.
     * @return A pair with a corrected sentence and its probability. This sentence
     * may be empty if the corrector did not find a plausible correction. The 
     * probability is log10.
     */
    private SentenceProbabilityPair findCorrect(String previousSentence /*ArrayList<String> previousWords,*/,
                                                String nextSentence /*ArrayList<String> nextWords*/,
                                                double currentProbability,
                                                int correctionsMade) {
        // We split the sentences so that we can more easily use the words.
        String[] previousWords = previousSentence.trim().split(" ");
        String[] nextWords = nextSentence.trim().split(" ");
        // When nextSentence is empty, nextWords tends to contain an empty string
        if (nextWords.length == 1 && nextWords[0].length()==0)
            nextWords = new String[]{};
        
        if (nextWords.length == 0) {
            // Corrected all words, return the sentence
            String sentence = previousSentence+"";
            
            // See whether the last word can possibly end the sentence
            if (cr.conditionalProbability("EoS",previousWords[previousWords.length-1]) > 0) {
                currentProbability += Math.log10(
                        cr.conditionalProbability("EoS",previousWords[previousWords.length-1])
                );
                return new SentenceProbabilityPair(sentence.trim(), currentProbability);
            } else // If not, return 'nothing'
                return new SentenceProbabilityPair("",-Double.MAX_VALUE);
        } else if (correctionsMade == 2) {
            // Maximal number of corrections made, return the whole sentence with
            // its probability.
            String sentence = previousSentence+"";
            
            // Adjust probability and add the rest of the words to the sentence
            String lastWord = previousWords[previousWords.length-1];
            for (String nextWord : nextWords) {
                // Check whether this word can possibly follow the last word.
                if (cr.conditionalProbability(nextWord, lastWord) > 0) {
                    currentProbability += Math.log10(
                            cr.conditionalProbability(nextWord, lastWord)
                    );
                    lastWord = nextWord;
                    sentence += " "+lastWord;
                } else {
                    // Else we return 'nothing'
                    return new SentenceProbabilityPair("",-Double.MAX_VALUE);
                }
            }
            // Check whether we can end this sentence.
            if (cr.conditionalProbability("EoS", lastWord) > 0) {
                currentProbability += Math.log10(
                        cr.conditionalProbability("EoS", lastWord)
                );
                return new SentenceProbabilityPair(sentence.trim(), currentProbability);
            } else {
                return new SentenceProbabilityPair("",-Double.MAX_VALUE);
            }
        } else {
            // The corrector is not finished and we will try to correct the next
            // word.
            String nextWord = nextWords[0];
            String lastWord = previousWords[previousWords.length-1];
            
            // We find the best possible correction for nextSentence
            SentenceProbabilityPair best = new SentenceProbabilityPair("",-Double.MAX_VALUE);
            
            for (Entry<String, Double> candidate : getCandidateWords(nextWord).entrySet()) {
                // Check whether this word can possibly follow.
                if (candidate.getValue() * cr.conditionalProbability(candidate.getKey(), lastWord) > 0) {
                    double nextProbability = currentProbability + Math.log10(
                            candidate.getValue()
                            * cr.conditionalProbability(candidate.getKey(), lastWord)
                    );

                    // Add the candidate to the sentence
                    String newPreviousSentence = previousSentence + " " + candidate.getKey();

                    // Remove from the nextSentence
                    String newNextSentence = "";
                    for (int i = 1; i < nextWords.length; ++ i) {
                        newNextSentence += " "+nextWords[i];
                    }

                    // Recursive call
                    SentenceProbabilityPair candidateSentence;
                    candidateSentence = findCorrect(
                            newPreviousSentence, newNextSentence, 
                            nextProbability,
                            correctionsMade + (candidate.getKey().equals(nextWord) ? 0 : 1));

                    // Compare
                    if (candidateSentence.probability > best.probability)
                        best = candidateSentence;
                }
            }
            return best;
        }
    }
    
    public String correctPhrase(String phrase)
    {
        if(phrase == null || phrase.length() == 0)
        {
            throw new IllegalArgumentException("phrase must be non-empty.");
        }
        
        String finalSuggestion;
        
        // We find the best correction using a recursive call.
        SentenceProbabilityPair bestCorrection = findCorrect("SoS", phrase, 0, 0);
        
        // Remove StartOfSentence ("SoS")
        try {
        finalSuggestion = bestCorrection.getSentence().split("SoS ")[1];
        return finalSuggestion;
        } catch (ArrayIndexOutOfBoundsException e) {
            // The corrector was not able to correct the sentence, return original
            return phrase;
        }
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
                Correction possibleCandidate = insert(word, i, c);
                if (possibleCandidate != null) {
                    mapOfWords.put(possibleCandidate.getCorrect(), 
                            possibleCandidate.getProbability());
                }
            }
        }
        
        // Find all candidate words from deletions.
        for (int i = 0; i < word.length(); ++ i) {
            Correction possibleCandidate = delete(word, i);
            if (possibleCandidate != null) {
                mapOfWords.put(possibleCandidate.getCorrect(), 
                        possibleCandidate.getProbability());
            }
        }
        
        // Find all candidate words from transpositions.
        for (int i = 0; i < word.length() - 1; ++ i) {
            Correction possibleCandidate = transpose(word, i);
            if (possibleCandidate != null) {
                mapOfWords.put(possibleCandidate.getCorrect(), 
                        possibleCandidate.getProbability());
            }
        }
        
        // Find all candidate words from substitutions.
        for (int i = 0; i < word.length(); ++ i) {
            for (char c : ALPHABET) {
                Correction possibleCandidate = substitute(word, i, c);
                if (possibleCandidate != null) {
                    mapOfWords.put(possibleCandidate.getCorrect(), 
                            possibleCandidate.getProbability());
                }
                
            }
        }
        
        // Add the word itself if it is contained in the dictionary.
        if (cr.inVocabulary(word)) {
            mapOfWords.put(word, 250.0d);
        }
        
        // Remove zero probabilities
        Map<String,Double> newMapOfWords = new HashMap<>();
        for (Entry<String, Double> e : mapOfWords.entrySet()) {
            if (e.getValue()>0) {
                newMapOfWords.put(e.getKey(), e.getValue());
            }
        }
        
        return newMapOfWords;
    }        

    double getNormalizationCount(String correct) {
        double count = 0;
        for (char c : SPACE_ALPHABET) {
            for (char d : SPACE_ALPHABET)
                count += cmr.getConfusionCount(c+""+d, correct);
            count += cmr.getConfusionCount(c+"", correct);
        }
        return count;
    }
    
    /**
     * Correct {@code original} replacing the {@code index}th letter with 
     * {@code insertion} (if in vocabulary).
     */
    Correction insert(String original, int index, char insertion) {
        String correct = original.substring(0, index)
                + insertion 
                + (index >= original.length() ? "" : original.substring(index));
        if (cr.inVocabulary(correct)) {
            String errorLetters = (index>0 ? original.charAt(index-1)+"": " ");
            String correctLetters = (index>0 ? original.charAt(index-1)+"": " ")+insertion;
            double probability = cmr.getConfusionCount(errorLetters, correctLetters);
            
            // Normalise
            probability /= getNormalizationCount(correctLetters);
            
            return new Correction(original, correct, probability);
        } else {
            return null;
        }
    }
    
    /**
     * 
     */
    Correction delete(String original, int index) {
        String correct = original.substring(0, index)
                + (index < original.length() ? original.substring(index + 1) : "");
        if (cr.inVocabulary(correct)) {
            String errorLetters = (index > 0 ? original.charAt(index-1) : " ") + "" + original.charAt(index);
            String correctLetters = (index > 0 ? original.charAt(index-1) : " ")+"";
            double probability = cmr.getConfusionCount(errorLetters, correctLetters);
            
            // Normalise
            probability /= getNormalizationCount(correctLetters);
            
            return new Correction(original, correct, probability);
        } else {
            return null;
        }
    }
    
    /**
     * 
     */
    Correction transpose(String original, int index) {
        String correct = original.substring(0, index)
                + original.charAt(index + 1)
                + original.charAt(index)
                + (index >= original.length() ? "" : original.substring(index + 2));
        if (cr.inVocabulary(correct)) {
            String errorLetters = original.substring(index, index + 2);
            String correctLetters = original.charAt(index + 1)+""+ original.charAt(index);
            double probability = cmr.getConfusionCount(errorLetters, correctLetters);
            
            // Normalise
            probability /= getNormalizationCount(correctLetters);
            
            return new Correction(original, correct, probability);
        } else {
            return null;
        }
    }
    
    /**
     * 
     */
    Correction substitute(String original, int index, char insertion) {
        String correct = original.substring(0, index)
                + insertion 
                + (index < original.length() ? original.substring(index + 1) : "");
        if (cr.inVocabulary(correct)) {
            String errorLetters = ""+original.charAt(index);
            String correctLetters = ""+insertion;
            double probability = cmr.getConfusionCount(
                    ""+original.charAt(index), 
                    ""+insertion);
            
            // Normalise
            probability /= getNormalizationCount(correctLetters);
            
            return new Correction(original, correct, probability);
        } else {
            return null;
        }
    }
}

/**
 * 
 * @author s147569
 */
class Correction {
    String error;
    String correct;
    double probability;
    
    String getCorrect() {
        return correct;
    }
    
    double getProbability() {
        return probability;
    }
    
    public Correction(String e, String c, double p) {
        error = e;
        correct = c;
        probability = p;
    }
}

class SentenceProbabilityPair {
    String sentence;
    double probability;
    
    public SentenceProbabilityPair(String s, double p) {
        sentence = s;
        probability = p;
    }
    
    public String getSentence() {
        return sentence;
    }
    
    public double getProbability() {
        return probability;
    }
}