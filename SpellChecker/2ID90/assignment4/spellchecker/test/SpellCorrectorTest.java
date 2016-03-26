/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author s147569
 */
public class SpellCorrectorTest {
    SpellCorrector instance;
    
    public SpellCorrectorTest() {
        try {
        instance = new SpellCorrector(new CorpusReader(), new ConfusionMatrixReader());
        } catch(IOException e) {
            fail("could not read file");
        }
        
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of correctPhrase method, of class SpellCorrector.
     */
    @Test
    public void testCorrectPhrase() {
    }

    /**
     * Test of getCandidateWords method, of class SpellCorrector.
     */
    @Test
    public void testGetCandidateWords() {
        String[] words = new String[]{
            "this", "essay", "allowed", "us", "to", "measure","a","wide","variety",
            "of","conditions","hme"
        };
        for (String w : words) {
            System.out.println("Get candidates of word: " + w);
            for (Entry<String,Double> e : instance.getCandidateWords(w).entrySet()) {
                System.out.println("\t"+e.getKey()+" "+e.getValue());
            }
        }
    }

    /**
     * Test of insert method, of class SpellCorrector.
     */
    @Test
    public void testInsert() {
    }

    /**
     * Test of delete method, of class SpellCorrector.
     */
    @Test
    public void testDelete() {
    }

    /**
     * Test of transpose method, of class SpellCorrector.
     */
    @Test
    public void testTranspose() {
    }

    /**
     * Test of substitute method, of class SpellCorrector.
     */
    @Test
    public void testSubstitute() {
    }

    /**
     * Test of noisyChannelProbability method, of class SpellCorrector.
     */
    @Test
    public void testNoisyChannelProbability() {
    }
    
}
