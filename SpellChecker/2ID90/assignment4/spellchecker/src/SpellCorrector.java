import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

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
        for (String word : words){
            Map<String,Double> candidateWords = getCandidateWords(word);
            String maxCandidate = word;
            Double maxProb = null;
            for (Entry<String,Double> entry : candidateWords.entrySet()) {
                if (maxProb == null || entry.getValue().compareTo(maxProb) > 0) {
                    maxCandidate = entry.getKey();
                    maxProb = entry.getValue();
                }
            }
            finalSuggestion += " " + maxCandidate;
        }
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
        
        return mapOfWords;
    }        

    /**
     * 
     */
    Correction insert(String original, int index, char insertion) {
        String correct = original.substring(0, index)
                + insertion 
                + (index >= original.length() ? "" : original.substring(index));
//        if (correct.equals(original))
//            System.out.println("Corrected "+original+" to itself insert"+index+""+insertion);
        if (cr.inVocabulary(correct)) {
            double probability = cmr.getConfusionCount((index>0 ? original.charAt(index-1)+"": " "), 
                    (index>0 ? original.charAt(index-1)+"": " ")+insertion);
//            if (probability==0.0d)
//                System.out.println("insertion not found:"+" | "+insertion);
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
//        if (correct.equals(original))
//            System.out.println("Corrected "+original+" to itself delete"+index);
        if (cr.inVocabulary(correct)) {
            double probability = cmr.getConfusionCount((index > 0 ? original.charAt(index-1) : " ") + "" + original.charAt(index), 
                    (index > 0 ? original.charAt(index-1) : " ")+"");
//            if (probability==0.0d)
//                System.out.println("delete not found:"+" " + original.charAt(index)+"| ");
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
//        if (correct.equals(original))
//            System.out.println("Corrected "+original+" to itself transpose"+index);
        if (cr.inVocabulary(correct)) {
            double probability = cmr.getConfusionCount(
                    original.substring(index, index + 2), 
                    original.charAt(index + 1)+""+ original.charAt(index));
            
//            if (probability==0.0d)
//                System.out.println("transpose not found:"+original.substring(index, index + 2)+"|" +
//                    original.charAt(index + 1)+""+ original.charAt(index));
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
//        if (correct.equals(original))
//            System.out.println("Corrected "+original+" to itself substitute"+index+""+insertion);
        if (cr.inVocabulary(correct)) {
            double probability = cmr.getConfusionCount(
                    ""+original.charAt(index), 
                    ""+insertion);
            
//            if (probability==0.0d)
//                System.out.println("substitute not found:"+""+original.charAt(index)+"|"+
//                    ""+insertion);
            return new Correction(original, correct, probability);
        } else {
            return null;
        }
    }
    
    /**
     * 
     */
    Double noisyChannelProbability(String observation, String word) {
        return 0d;
    }
}

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