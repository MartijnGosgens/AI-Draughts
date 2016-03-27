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
import java.util.Set;
import java.util.TreeMap;

public class CorpusReader 
{
    final static String CNTFILE_LOC = "samplecnt.txt";
    final static String VOCFILE_LOC = "samplevoc.txt";
    
    private HashMap<String,Integer> ngrams;
    private Set<String> vocabulary;
    
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
            int Nc = freqOfFreq.get(c);
            if (freqOfFreq.ceilingKey(c) == null){
                /* ngram has highest frequency so there exists no next.
                * smoothed count will be the frequency divided by sum of all 
                * counts.
                */
                smoothedCount = (double) c / sumCounts;
            } else {
                /* c* = ((c+1) * N_c+1) / N_c
                * where c is the frequency of NGram and
                * N_c is the frequency of frequency c
                */
                int nextC = freqOfFreq.ceilingKey(c);
                int Nc1 = freqOfFreq.get(nextC);

                smoothedCount = (double) ((c + (nextC - c)) * Nc1) / Nc;
            }
        }
        return smoothedCount;
    }
    
    /**
     * Construct the tree map with frequencies as keys and freq. of freq. 
     * as values.
     */
    private void makeFreqOfFreqTreeMap(){
        freqOfFreq = new TreeMap<>();
        Collection values = ngrams.values();
        Iterator iter = values.iterator();
        while(iter.hasNext()){
            int value = (int) iter.next();
            if (!freqOfFreq.containsKey(value)){
                freqOfFreq.put(value, getFreqOfFreqC(value));
            }
        }
    }
    
    /**
     * Returns the freq. of the frequency {@code c}.
     * @param c
     * @return freq. of {@code c}
     */
    private int getFreqOfFreqC(int c){
        int result = 0;
        for (int value : ngrams.values()) {
            if (value == c) {
                result++;
            }
        }
        return result;
    }
    
    /**
     * Returns the sum of the frequencies of all words in the vocabulary.
     * @return sum
     */
    private int getAllCount(){
        int result = 0;
        for (int value : ngrams.values()){
            result += value;
        }
        return result;
    }
    
    /**
     * Returns the conditional probability that {@code next} will follow
     * {@code previous}.
     */
    public double conditionalProbability(String next, String previous) {
        // Smoothening enters an infinite loop every time it sees "the".
        double occurrencePrevious = getSmoothedCount(previous);
        double occurrenceFollowUp = getSmoothedCount(previous + " " + next);
        System.out.println("Smoothed:::: prev :: " + previous + ":::" + occurrencePrevious);
        System.out.println("Smoothed:::: prevnext :: " + previous + " " + next + ":::" + occurrenceFollowUp);
        return occurrenceFollowUp / occurrencePrevious;
//        double total = getNGramCount(previous);
//        double followUp = getNGramCount(previous + " " + next);
//        System.out.println("Smoothed:::: prev :: " + previous + ":::" + total + ":::" + i++);
//        System.out.println("Smoothed:::: prevnext :: " + previous + " " + next + ":::" + followUp + ":::" + i++);
//        return followUp / total;
    }
}
