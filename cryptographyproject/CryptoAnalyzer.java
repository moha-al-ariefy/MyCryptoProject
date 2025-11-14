package cryptographyproject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet; // Import HashSet
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set; // Import Set
import java.util.stream.Collectors;
import java.util.stream.Stream;

// this is the "brain" class. it holds the text and has all the
// analysis methods that you wrote in your Phase2Main.
// I've UPDATED the validateText function to be much smarter.
public class CryptoAnalyzer {

    // we need the alphabet to know what letters to count, same as in MainCipher.
    private static final String theAlphabet = "abcdefghijklmnopqrstuvwxyz";

    private String rawCipherText;
    private String cleanCipherText;
    private boolean fileLoaded;

    // --- New for Phase 2: The Guessing Map ---
    // this map will hold our guesses for the S6 substitution part.
    // Key = cipher char, Value = plain char.
    private Map<Character, Character> substitutionGuessMap;

    // --- NEW FOR PHASE 3: The Dictionary ---
    // this will hold all the words from dictionary.txt for fast checking.
    private Set<String> dictionary;
    private boolean dictionaryLoaded;


    /**
     * The constructor. When we create an analyzer, it automatically
     * reads the file and cleans the text, getting it ready for analysis.
     * @param filename The name of the file to read (e.g., "cipher.txt")
     */
    public CryptoAnalyzer(String filename) {
        // load the ciphertext
        try {
            this.rawCipherText = new String(Files.readAllBytes(Paths.get(filename)));
            this.cleanCipherText = this.rawCipherText.toLowerCase().replaceAll("[^a-z]", "");
            this.fileLoaded = true;
        } catch (IOException e) {
            System.out.println("Oh no, error reading file: " + filename);
            this.rawCipherText = null;
            this.cleanCipherText = null;
            this.fileLoaded = false;
        }
        
        // initialize our guess map.
        this.substitutionGuessMap = new HashMap<>();
        for (char c : theAlphabet.toCharArray()) {
            substitutionGuessMap.put(c, '?'); // '?' means 'not guessed yet'
        }

        // --- NEW FOR PHASE 3 ---
        // also initialize and load the dictionary
        this.dictionary = new HashSet<>();
        this.dictionaryLoaded = loadDictionary("dictionary.txt");
    }

    /**
     * This is a helper function to load all the words from dictionary.txt
     * into our Set for fast lookups.
     * @param dictFilename the name of the dictionary file
     * @return true if loading succeeded, false otherwise
     */
    private boolean loadDictionary(String dictFilename) {
        // this is a helper to load all the words from dictionary.txt
        try (Stream<String> lines = Files.lines(Paths.get(dictFilename))) {
            lines.map(String::toLowerCase) // make lowercase
                 .map(String::trim)        // remove whitespace
                 .map(line -> line.replaceAll("[^a-z]", "")) // remove non-letters
                 // Only keep words 4+ letters long
                 .filter(line -> line.length() > 0) // <-- REVERTED: Was >= 4, now > 0
                 .forEach(this.dictionary::add); // add each word to our Set
            return true;
        } catch (IOException e) {
            // This is not a critical error, so we just report it.
            // The main program will warn the user.
            return false; // file not found
        }
    }


    // --- Getters to access the text ---
    public String getRawText() { return this.rawCipherText; }
    public String getCleanText() { return this.cleanCipherText; }
    public boolean isFileLoaded() { return this.fileLoaded; }
    public boolean isDictionaryLoaded() { return this.dictionaryLoaded; }
    public int getDictionarySize() { return this.dictionary.size(); }
    
    // =========================================================================
    // === Interactive Methods (Phase 2)
    // =========================================================================

    /**
     * Records a user's guess for the S6 substitution map.
     * @param cipherChar The ciphertext letter (e.g., 'h')
     * @param plainChar The plaintext letter the user thinks it is (e.g., 'e')
     */
    public void makeGuess(char cipherChar, char plainChar) {
        if (theAlphabet.indexOf(cipherChar) != -1 && theAlphabet.indexOf(plainChar) != -1) {
            // we also check if this plain char is already guessed for another cipher char.
            for (Map.Entry<Character, Character> entry : substitutionGuessMap.entrySet()) {
                if (entry.getValue() == plainChar && entry.getKey() != cipherChar) {
                    System.out.printf("  (Warning: You already guessed '%c' for cipher '%c'. Clearing that guess.)\n", plainChar, entry.getKey());
                    entry.setValue('?');
                }
            }
            this.substitutionGuessMap.put(cipherChar, plainChar);
        }
    }

    /**
     * Resets a user's guess for a specific ciphertext letter.
     * @param cipherChar The ciphertext letter to reset (e.g., 'h')
     */
    public void undoGuess(char cipherChar) {
        if (this.substitutionGuessMap.containsKey(cipherChar)) {
            this.substitutionGuessMap.put(cipherChar, '?');
        }
    }

    /**
     * This is a helper to show the user all their current guesses.
     */
    public void printGuessMap() {
        System.out.println("--- Current S6 Guessing Map (Cipher -> Plain) ---");
        // we print 13 letters per line to make it fit nicely.
        int count = 0;
        for (char c : theAlphabet.toCharArray()) {
            System.out.printf("  %c -> %c  ", c, this.substitutionGuessMap.get(c));
            count++;
            if (count % 13 == 0) {
                System.out.println(); // new line
            }
        }
        System.out.println(); // final new line
    }

    /**
     * This is the main interactive helper. It shows the user the results
     * of their S6 guesses, but leaves the C3 parts as underscores.
     * This helps the user spot words in the S6 segments.
     * @return A string like "___???t??___??t???..."
     */
    public String getDecryptedTextWithContext() {
        StringBuilder partialText = new StringBuilder();

        // we go through the text block by block
        for (int i = 0; i < this.cleanCipherText.length(); i += 9) {
            // first, add the C3 part as underscores.
            partialText.append("___");

            // now, process the S6 part
            for (int j = 3; j < 9; j++) {
                if (i + j >= this.cleanCipherText.length()) {
                    break; // stop if we hit the end of the file
                }
                char cipherChar = this.cleanCipherText.charAt(i + j);
                // look up our guess for this char
                char plainChar = this.substitutionGuessMap.get(cipherChar); 
                partialText.append(plainChar);
            }
            partialText.append(" "); // add a space between blocks
        }
        return partialText.toString();
    }

    /**
     * This is the "full attack" function. It tries to decrypt *everything*.
     * It uses the S6 guesses to decrypt the S6 part.
     * THEN, it uses that result to find the Caesar key.
     * THEN, it uses that key to decrypt the C3 part.
     * @return The fully decrypted text, based on current guesses.
     */
    public String getFullyDecryptedText() {
        StringBuilder fullText = new StringBuilder();
        
        // this is our "reverse substitution" map, built from our guesses
        Map<Character, Character> inverseGuessMap = new HashMap<>();
        for (Map.Entry<Character, Character> entry : this.substitutionGuessMap.entrySet()) {
            if (entry.getValue() != '?') {
                inverseGuessMap.put(entry.getKey(), entry.getValue());
            }
        }

        // go through block by block
        for (int i = 0; i < this.cleanCipherText.length(); i += 9) {
            // --- 1. Get the S6 part ---
            StringBuilder decryptedS6 = new StringBuilder();
            boolean s6IsFullyGuessed = true;
            for (int j = 3; j < 9; j++) {
                if (i + j >= this.cleanCipherText.length()) {
                    s6IsFullyGuessed = false;
                    break; 
                }
                char cipherChar = this.cleanCipherText.charAt(i + j);
                char plainChar = inverseGuessMap.getOrDefault(cipherChar, '?');
                if (plainChar == '?') {
                    s6IsFullyGuessed = false; // we can't find the key if this part isn't guessed
                }
                decryptedS6.append(plainChar);
            }

            // --- 2. Try to get the Caesar key ---
            int shiftKey = -1; // -1 means 'unknown'
            if (s6IsFullyGuessed && decryptedS6.length() > 0) {
                char keyChar = decryptedS6.charAt(0); // the key is the first letter of the S6 plain text
                shiftKey = theAlphabet.indexOf(keyChar);
            }

            // --- 3. Decrypt the C3 part ---
            StringBuilder decryptedC3 = new StringBuilder();
            for (int j = 0; j < 3; j++) {
                if (i + j >= this.cleanCipherText.length()) {
                    break;
                }
                char cipherChar = this.cleanCipherText.charAt(i + j);
                
                if (shiftKey == -1) {
                    // we don't know the key, so just put a placeholder
                    decryptedC3.append('?');
                } else {
                    // we know the key! let's decrypt the C3 part.
                    int cIdx = theAlphabet.indexOf(cipherChar);
                    int pIdx = (cIdx - shiftKey + 26) % 26; // +26 handles negatives
                    decryptedC3.append(theAlphabet.charAt(pIdx));
                }
            }
            
            // add this block's result to our final string
            fullText.append(decryptedC3);
            fullText.append(decryptedS6);
            fullText.append(" "); // space between blocks
        }

        return fullText.toString();
    }


    // =========================================================================
    // === NEW Validation Method for Phase 3 (UPDATED)
    // =========================================================================

    /**
     * Overloaded helper method. Defaults to showing only the top 10 results.
     * @param decryptedText The text to score (e.g., "ifwehadan ythingcon")
     * @return A string summarizing the score (e.g., "==> Word Score: 50. Found [confidential, anything]")
     */
    public String validateText(String decryptedText) {
        // Default to NOT showing all words
        return validateText(decryptedText, false);
    }
    
    /**
     * This is the "Word Validation" function from the rubric.
     * It checks a string of text against the dictionary.
     * @param decryptedText The text to score (e.g., "___e?a?an ___in?con")
     * @param showAll If true, shows all found words. If false, shows top 10.
     * @return A string summarizing the score (e.g., "==> Word Score: 50. Found [had, confidential]")
     */
    public String validateText(String decryptedText, boolean showAll) {
        if (!this.dictionaryLoaded) {
            return "==> Dictionary not loaded. Skipping validation.";
        }

        // Use a Set to automatically handle duplicates
        Set<String> foundWordsSet = new HashSet<>();
        int totalScore = 0; // we will score based on the length of words found

        // --- Strategy 1: Check for words *inside* partial fragments ---
        // This checks if "had" is inside "ehadan".
        String[] blocks = decryptedText.split(" "); // ["___e?a?an", "___in?con", ...]
        
        for (String block : blocks) {
            if (block.length() <= 3) continue; // Skip empty/invalid blocks

            // Get just the S6 part, e.g., "e?a?an" or "ential"
            String s6Fragment = block.substring(3);
            String cleanFragment = s6Fragment.replaceAll("\\?", ""); // "eaan" or "ential"
            
            if (cleanFragment.length() < 3) continue; // Not enough letters to find a word

            // Check if any dictionary word is a SUBSTRING of the clean fragment
            for (String word : this.dictionary) {
                if (cleanFragment.contains(word)) {
                    // Example: "ehadan" (from e?a?an) contains "had"
                    if (foundWordsSet.add(word)) { // .add() returns true if the word was new
                        totalScore += word.length() * 2; // Score these partials higher
                    }
                }
            }
        }


        // --- Strategy 2: Check the combined string for fully-formed words ---
        // This finds words like "confidential" that span blocks
        String cleanCombined = decryptedText.replaceAll("[^a-z]", "");
        for (String word : this.dictionary) {
            // REMOVED "word.length() >= 4 &&" from the line below
            if (cleanCombined.contains(word)) {
                if (foundWordsSet.add(word)) { // .add() returns true if the word was new
                    totalScore += word.length();
                }
            }
        }

        if (foundWordsSet.isEmpty()) {
            return "==> Word Score: 0. No common English words (4+ letters) found.";
        }

        // Convert Set to List for sorting
        List<String> foundWords = new ArrayList<>(foundWordsSet);
        // sort found words by length, longest first
        foundWords.sort((s1, s2) -> s2.length() - s1.length());
        
        List<String> displayWords = foundWords;
        String displayMessage = "Found " + foundWords.size() + " words: ";

        // This is the new logic to show top 10 or all
        if (foundWords.size() > 10 && !showAll) {
            displayWords = foundWords.subList(0, 10);
            displayMessage = "Found " + foundWords.size() + " words (top 10): ";
        }
        
        return "==> Word Score: " + totalScore + ". " + displayMessage + displayWords;
    }


    // =========================================================================
    // === All methods from here down are your excellent analysis functions ===
    // =========================================================================

    // === Section 1: Core Frequency Analysis Methods (Rubric Items) ===
    
    public Map<Character, Integer> getSingleLetterFrequencies() {
        Map<Character, Integer> frequencies = new HashMap<>();
        for (char c : theAlphabet.toCharArray()) {
            frequencies.put(c, 0);
        }
        for (char c : this.cleanCipherText.toCharArray()) {
            frequencies.put(c, frequencies.getOrDefault(c, 0) + 1);
        }
        return frequencies;
    }

    public Map<String, Integer> getDiagramFrequencies() {
        Map<String, Integer> frequencies = new HashMap<>();
        for (int i = 0; i < this.cleanCipherText.length() - 1; i++) {
            String diagram = this.cleanCipherText.substring(i, i + 2);
            frequencies.put(diagram, frequencies.getOrDefault(diagram, 0) + 1);
        }
        return frequencies;
    }

    public Map<String, Integer> getTrigramFrequencies() {
        Map<String, Integer> frequencies = new HashMap<>();
        for (int i = 0; i < this.cleanCipherText.length() - 2; i++) {
            String trigram = this.cleanCipherText.substring(i, i + 3);
            frequencies.put(trigram, frequencies.getOrDefault(trigram, 0) + 1);
        }
        return frequencies;
    }

    // === Section 2: The "Special Attack" Method ===

    public Map<Character, Integer> getSegmentedFrequencies(int blockSize, int segmentStart, int segmentLength) {
        Map<Character, Integer> frequencies = new HashMap<>();
        for (char c : theAlphabet.toCharArray()) {
            frequencies.put(c, 0);
        }
        for (int blockStart = 0; blockStart < this.cleanCipherText.length(); blockStart += blockSize) {
            int segmentIndex = blockStart + segmentStart;
            for (int i = 0; i < segmentLength; i++) {
                int charIndex = segmentIndex + i;
                if (charIndex >= this.cleanCipherText.length()) {
                    break; 
                }
                if (charIndex >= blockStart + blockSize) {
                    break;
                }
                char c = this.cleanCipherText.charAt(charIndex);
                frequencies.put(c, frequencies.getOrDefault(c, 0) + 1);
            }
        }
        return frequencies;
    }

    // === Section 3: Helper and Display Methods ===

    public static void printFrequencyMap(String title, Map<?, Integer> dataMap, int topN) {
        Map<?, Integer> sortedMap = sortMapByValue(dataMap);
        long totalCount = 0;
        for (Integer count : sortedMap.values()) {
            totalCount += count;
        }
        int maxCount = 0;
        if (!sortedMap.isEmpty()) {
            maxCount = sortedMap.values().iterator().next(); 
        }
        final int BAR_WIDTH = 40; 

        System.out.println("\n" + title);
        System.out.println("Total items counted: " + totalCount);
        if (totalCount == 0) {
            System.out.println("No data to display.");
            return;
        }
        System.out.println("----------------------------------------------------------");
        
        int itemsShown = 0;
        for (Map.Entry<?, Integer> entry : sortedMap.entrySet()) {
            if (itemsShown >= topN) {
                break; 
            }
            Object key = entry.getKey();
            int count = entry.getValue();
            double percentage = (double) count * 100.0 / totalCount;
            int barLength = 0;
            if (maxCount > 0) {
                barLength = (int) ((double) count * BAR_WIDTH / maxCount);
            }
            StringBuilder bar = new StringBuilder();
            for (int i = 0; i < barLength; i++) {
                bar.append("#");
            }
            System.out.printf("%-8s: %8d (%6.2f%%) | %s%n", 
                "'" + key.toString() + "'", 
                count, 
                percentage, 
                bar.toString());
            itemsShown++;
        }
        System.out.println("----------------------------------------------------------");
    }

    private static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map) {
        return map.entrySet()
                   .stream()
                   .sorted(Map.Entry.<K, V>comparingByValue().reversed())
                   .collect(Collectors.toMap(
                       Map.Entry::getKey,
                       Map.Entry::getValue,
                       (e1, e2) -> e1,
                       LinkedHashMap::new
                   ));
    }
}