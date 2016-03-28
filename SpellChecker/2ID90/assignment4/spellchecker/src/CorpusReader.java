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
    
    private HashMap<String,Integer> ngrams;
    private int numMonograms = 0;
    private int numBigrams = 0;
    
    private Set<String> vocabulary;
    private int numWords = 0;
    
    private int maxFreq;
    private int sumCounts;
    private TreeMap<Integer, Integer> freqOfFreq;
        
    public CorpusReader() throws IOException
    {  
        readNGrams();
        readVocabulary();
        makeFreqOfFreqTreeMap();
        maxFreq = freqOfFreq.lastKey();
        sumCounts = getAllCount();
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
            numWords ++;
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
     * Calculates the smoothed count of string {@code NGram}.
     * @param NGram
     * @return Smoothed count
     */
    public double getSmoothedCount(String NGram)
    {
        if(NGram == null || NGram.length() == 0)
        {
            throw new IllegalArgumentException("NGram must be non-empty.");
        }
        
        double smoothedCount = 0.0;
        int c = getNGramCount(NGram);
        
        // good Turing smoothing:
        if (c == 0) {
            /* c* = N_1 / N
            * with N_1 is the frequency of words with frequency 1 and
            * N is the sum of frequencies of all words
            */
            smoothedCount = (double) freqOfFreq.get(1) / sumCounts;
        } else if (c == maxFreq) {
            /* If the word ngram has the highest frequency, divide by the sum of
            * frequencies of all words.      
            */
            smoothedCount = (double) c / sumCounts;
        } else {
            /* c* = ((c+1) * N_c+1) / N_c
            * where c is the frequency of NGram and
            * N_c is the frequency of frequency c
            * smoothedCount is c* / sumcounts
            */
            int Nc = freqOfFreq.get(c);
            int nextC = freqOfFreq.ceilingKey(c);
            int Nc1 = freqOfFreq.get(nextC);

            double evalC = (double) ((c + (nextC - c)) * Nc1) / Nc;
            smoothedCount = evalC / sumCounts;
        }
        return smoothedCount;
    }
    
    /**
     * Returns the probability that a certain word is {@code w}. (uses Add-one)
     * @param w
     * @return 
     */
    double wordProbability(String w) {
        if (inVocabulary(w)) {
            return (getNGramCount(w)+1)/(double)(numMonograms + numWords);
        } else {
            return 0;
        }
    }
    
    /**
     * Construct the tree map with frequencies as keys and freq. of freq. 
     * as values.
     */
    private void makeFreqOfFreqTreeMap(){
        freqOfFreq = new TreeMap<>();
        for (Entry<String,Integer> entry : ngrams.entrySet()) {
            int value = entry.getValue();
            if (!freqOfFreq.containsKey(entry.getValue())) {
                freqOfFreq.put(value , 1);
            } else {
                freqOfFreq.put(value , freqOfFreq.get(entry.getValue())+1);
            }
        }
    }
    
    /**
     * Returns the sum of the frequencies of all words in the vocabulary.
     * @return sum
     */
    private int getAllCount(){
        int result = 0;
        for (Entry<String, Integer> entry : ngrams.entrySet()){
            result += entry.getValue();
        }
        return result;
    }
    
    /**
     * Returns the conditional probability that {@code next} will follow
     * {@code previous}.
     */
    public double conditionalProbability(String next, String previous) {
        double occurrencePrevious = getSmoothedCount(previous);
        double occurrenceFollowUp = getSmoothedCount(previous + " " + next);
//        System.out.println("Smoothed:::: prev :: " + previous + ":::" + occurrencePrevious);
//        System.out.println("Smoothed:::: prevnext :: " + previous + " " + next + ":::" + occurrenceFollowUp);
        if (occurrenceFollowUp > occurrencePrevious) {
            System.out.println("P("+next+"|"+previous+")="+occurrenceFollowUp+"/"+occurrencePrevious+">1");
        }
        return occurrenceFollowUp / occurrencePrevious;

        // No smoothening
//        double total = getNGramCount(previous);
//        double followUp = getNGramCount(previous + " " + next);
//        System.out.println("Smoothed:::: prev :: " + previous + ":::" + total + ":::" + i++);
//        System.out.println("Smoothed:::: prevnext :: " + previous + " " + next + ":::" + followUp + ":::" + i++);
//        return followUp / total;

        // AddOne Smoothening:
//        if (!inVocabulary(next)) 
//            return 0;
//        
//        double total = getNGramCount(previous) + 1;
//        double followUp = getNGramCount(previous + " " + next) + 1;
////        System.out.println("Smoothed:::: prev :: " + previous + ":::" + total + ":::" + i++);
////        System.out.println("Smoothed:::: prevnext :: " + previous + " " + next + ":::" + followUp + ":::" + i++);
//        if (followUp > total) {
//            System.out.println("P("+next+"|"+previous+")="+followUp+"/"+total+">1");
//        }
//        return followUp / total;
    
    }
}
