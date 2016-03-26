import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CorpusReader 
{
    final static String CNTFILE_LOC = "samplecnt.txt";
    final static String VOCFILE_LOC = "samplevoc.txt";
    
    private HashMap<String,Integer> ngrams;
    private Set<String> vocabulary;
        
    public CorpusReader() throws IOException
    {  
        readNGrams();
        readVocabulary();
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
    
    public double getSmoothedCount(String NGram)
    {
        if(NGram == null || NGram.length() == 0)
        {
            throw new IllegalArgumentException("NGram must be non-empty.");
        }
        
        double smoothedCount = 0.0;
        int c = getNGramCount(NGram);
       /*(TODO:) - Bij het woord met grootste frequency werkt het niet, want er
        * is er geen die hoger is. - Niet accuraat als de gap tussen C en C+1 
        * groot is.
        */
        /* good Turing smoothing:
        * c* = N_1 / N
        * with N_1 is the frequency of words with frequency 1 and
        * N is the sum of frequencies of all words
        */
        if (c == 0) {
            System.out.println("!inVocabulary");
            smoothedCount = (double) getFreqOfFreqC(1, 0) / getAllCount();
        } 
        /* c* = ((c+1) * N_c+1) / N_c
        * where c is the frequency of NGram and
        * N_c is the frequency of frequency c
        */
        else {
            double Nc = getFreqOfFreqC(c, 0);
            int d = 1;
            int Nc1 = 0;
            while (Nc1 == 0){
                Nc1 = getFreqOfFreqC(c, d);
                d++;
            }
//            System.out.println("c: " + c + " ::: Nc: " + Nc + " ::: Nc1: " + Nc1 + " ::: c+d: " + (c+(d-1)));
            smoothedCount = (double) ((c + 1) * Nc1) / Nc;
        }
        
        return smoothedCount;        
    }
    
    private int getFreqOfFreqC(int c, int d){
        int result = 0;
        for (int value : ngrams.values()) {
            if (value == c + d) {
                result++;
            }
        }
        return result;
    }
    
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
//        double occurrencePrevious = getSmoothedCount(previous);
//        double occurrenceFollowUp = getSmoothedCount(previous + " " + next);
//        return occurrenceFollowUp / occurrencePrevious;
        double total = getNGramCount(previous);
        double followUp = getNGramCount(previous + " " + next);
        return followUp / total;
    }
}
