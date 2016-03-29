import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class CorpusReader 
{
    final static String CNTFILE_LOC = "samplecnt.txt";
    final static String VOCFILE_LOC = "samplevoc.txt";
    
    final double LEGAL_FRACTION = 500d;
    
    private HashMap<String,Integer> ngrams;
    private int numMonograms = 0;
    private int numBigrams = 0;
    
    private Set<String> vocabulary;
    private int numWords = 0;
    
    private int maxFreqMono;
    private int maxFreqBi;
    
    //private TreeMap<Integer, Integer> freqOfFreq;
    private TreeMap<Integer, Integer> freqOfFreqMono;
    private TreeMap<Integer, Integer> freqOfFreqBi;
        
    public CorpusReader() throws IOException
    {  
        readNGrams();
        readVocabulary();
        makeFreqOfFreqMaps();
        maxFreqMono = freqOfFreqMono.lastKey();
        maxFreqBi = freqOfFreqBi.lastKey();
    }
    
    /**
     * Returns the n-gram count of <NGram> in the file
     * 
     * 
     * @param nGram : space-separated list of words, e.g. "adopted by him"
     * @return 0 if <NGram> cannot be found, 
     * otherwise count of <NGram> in file
     */
     public int getNGramCount(String nGram) throws  NumberFormatException
    {
        if(nGram == null || nGram.length() == 0)
        {
            throw new IllegalArgumentException("NGram must be non-empty.");
        }
        Integer value = ngrams.get(nGram);
        return value==null?0:value;
    }
    
    private void readNGrams() throws 
            FileNotFoundException, IOException, NumberFormatException
    {
        ngrams = new HashMap<>();

        FileInputStream fis;
        fis = new FileInputStream(CNTFILE_LOC);
        BufferedReader in = new BufferedReader(new InputStreamReader(fis));

        while (in.ready()) {
            String phrase = in.readLine().trim();
            String s1, s2;
            int j = phrase.indexOf(" ");

            s1 = phrase.substring(0, j);
            s2 = phrase.substring(j + 1, phrase.length());

            int count = 0;
            try {
                count = Integer.parseInt(s1);
                ngrams.put(s2, count);
                
                // We count the number of bigrams and monograms for normalizaton
                if (s2.contains(" ")) {
                    numBigrams += count;
                } else {
                    numMonograms += count;
                }
            } catch (NumberFormatException nfe) {
                throw new NumberFormatException("NumberformatError: " + s1);
            }
        }
    }
    
    
    private void readVocabulary() throws FileNotFoundException, IOException {
        vocabulary = new HashSet<>();
        
        FileInputStream fis = new FileInputStream(VOCFILE_LOC);
        BufferedReader in = new BufferedReader(new InputStreamReader(fis));
        
        while(in.ready())
        {
            String line = in.readLine();
            vocabulary.add(line);
        }
    }
    
    /**
     * Returns the size of the number of unique words in the dataset
     * 
     * @return the size of the number of unique words in the dataset
     */
    public int getVocabularySize() 
    {
        return vocabulary.size();
    }
    
    /**
     * Returns the subset of words in set that are in the vocabulary
     * 
     * @param set
     * @return 
     */
    public HashSet<String> inVocabulary(Set<String> set) 
    {
        HashSet<String> h = new HashSet<>(set);
        h.retainAll(vocabulary);
        return h;
    }
    
    public boolean inVocabulary(String word) 
    {
       return vocabulary.contains(word);
    }
    
    /**
     * Returns the probability of {@code w} occurring. (uses Good-Turing)
     * @param w
     * @return 
     */
    double wordProbability(String w) {
        if (inVocabulary(w)) {
            // Get the count of the word
            int c = getNGramCount(w);
            // good Turing smoothing:
            if (c == maxFreqMono) {
                /* If the word has the highest frequency, divide by the sum of
                * frequencies of all words.      
                */
                return (double) c / numMonograms;
            } else {
                /* c* = ((c+1) * N_c+1) / N_c
                * where c is the frequency of the word and
                * N_c is the frequency of frequency c
                * smoothedProbability is c* / sumcounts
                */
                int Nc = freqOfFreqMono.get(c);
                int nextC = freqOfFreqMono.ceilingKey(c);
                int Nc1 = freqOfFreqMono.get(nextC);

                double evalC = (double) (nextC * Nc1) / Nc;
                return evalC / numMonograms;
            }
        } else {
            return 0;
        }
    }
    
    double combinationProbability(String w1, String w2) {
        if (inVocabulary(w1) && inVocabulary(w2)) {
            // Get the count of the word
            int c = getNGramCount(w1+" "+w2);
            // good Turing smoothing:
            if (c == maxFreqBi) {
                /* If the word has the highest frequency, divide by the sum of
                * frequencies of all words.      
                */
                return (double) c / numBigrams;
            } else {
                /* c* = ((c+1) * N_c+1) / N_c
                * where c is the frequency of NGram and
                * N_c is the frequency of frequency c
                * smoothedProbability is c* / sumcounts
                */
                int Nc = freqOfFreqBi.get(c);
                int nextC = freqOfFreqBi.ceilingKey(c+1);
                int Nc1 = freqOfFreqBi.get(nextC);

                double evalC = (double) (nextC * Nc1) / Nc;
                return evalC / numBigrams;
            }
        } else {
            return 0;
        }
    }
    
    /**
     * Construct the tree maps with frequencies as keys and freq. of freq. 
     * as values for both the Monograms as the Bigrams.
     */
    private void makeFreqOfFreqMaps() {
        freqOfFreqMono = new TreeMap<>();
        freqOfFreqBi = new TreeMap<>();
        double numDistinctBigrams = 0;
        for (Entry<String,Integer> entry : ngrams.entrySet()) {
            int value = entry.getValue();
            if (entry.getKey().contains(" ")) {
                // Increment the number of distinct bigrams
                numDistinctBigrams += 1d;
                
                // Bigram
                if (!freqOfFreqBi.containsKey(entry.getValue())) {
                    freqOfFreqBi.put(value, 1);
                } else {
                    freqOfFreqBi.put(value, freqOfFreqBi.get(entry.getValue())+1);
                }
            } else {
                // Monogram
                if (!freqOfFreqMono.containsKey(entry.getValue())) {
                    freqOfFreqMono.put(value, 1);
                } else {
                    freqOfFreqMono.put(value, freqOfFreqMono.get(entry.getValue())+1);
                }
            }
        }
        double numUnseenBigrams = Math.pow((double)(vocabulary.size()),2)
                - numDistinctBigrams;
        freqOfFreqBi.put(0, (int)(numUnseenBigrams/LEGAL_FRACTION));
    }
    
    /**
     * Returns the conditional probability that {@code next} will follow
     * {@code previous}.
     */
    public double conditionalProbability(String next, String previous) {
        double occurrencePrevious = wordProbability(previous);
        double occurrenceFollowUp = combinationProbability(previous, next);
        return occurrenceFollowUp / occurrencePrevious;
    
    }
}
