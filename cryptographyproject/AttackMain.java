package cryptographyproject;

import java.util.Map;
import java.util.Scanner;

// this is the main class for our Phase 2 project.
// it *uses* the CryptoAnalyzer to perform the attack.
public class AttackMain {

    /**
     * The main method, it's the entry point for our Phase 2 analysis.
     */
    public static void main(String[] args) {
        Scanner userInputReader = new Scanner(System.in);
        System.out.println("--- CSCI 462: Project Phase 2 - Cryptanalysis ---");
        System.out.println("Enter the name of the ciphertext file to analyze (e.g., cipher.txt):");
        
        String filename = userInputReader.nextLine();
        
        // we will use our analyzer 'brain' to read and clean the file
        // when we create this, it will *also* try to load "dictionary.txt"
        CryptoAnalyzer analyzer = new CryptoAnalyzer(filename);
        
        if (!analyzer.isFileLoaded()) {
            System.out.println("File not found or error reading file. Exiting.");
            userInputReader.close();
            return;
        }

        System.out.println("Successfully read " + analyzer.getRawText().length() + " raw characters.");
        System.out.println("Text cleaned. Cleaned text length: " + analyzer.getCleanText().length());
        
        // This message comes from the analyzer's constructor
        if (analyzer.isDictionaryLoaded()) {
            System.out.println("Dictionary loaded with " + analyzer.getDictionarySize() + " words.");
        } else {
            System.out.println("!! Warning: dictionary.txt not found. Word validation will be skipped.");
        }


        // === Step 1: Run and Display Initial Analyses ===
        
        // --- Single-Letter Analysis (Overall) ---
        Map<Character, Integer> singleFreqs = analyzer.getSingleLetterFrequencies();
        CryptoAnalyzer.printFrequencyMap(
            "--- [Overall] Single-Letter Frequencies (All text) ---", 
            singleFreqs, 
            26 // show all 26 letters
        );
        
        System.out.println("\nPress Enter to continue to the *targeted* attack analysis...");
        userInputReader.nextLine();

        // --- Analysis of C3 (Caesar) Parts ---
        Map<Character, Integer> caesarPartFreqs = analyzer.getSegmentedFrequencies(9, 0, 3);
        CryptoAnalyzer.printFrequencyMap(
            "--- [ATTACK] Frequencies for C3 (Caesar) Segments Only ---",
            caesarPartFreqs,
            26 // show all 26
        );
        System.out.println("==> ANALYSIS: This graph is FLAT, proving it's polyalphabetic.");


        // --- Analysis of S6 (Substitution) Parts ---
        // this is the most important graph. we save it to show the user every time.
        Map<Character, Integer> substPartFreqs = analyzer.getSegmentedFrequencies(9, 3, 6);
        CryptoAnalyzer.printFrequencyMap(
            "--- [ATTACK] Frequencies for S6 (Substitution) Segments Only ---",
            substPartFreqs,
            26 // show all 26
        );
        System.out.println("==> ANALYSIS: This graph is SPIKY, proving it's monoalphabetic.");
        System.out.println("This is the weak point. We will now attack this part.");

        
        // === Step 2: Begin Interactive Cracking Loop ===
        System.out.println("\n--- Starting Interactive S6 Cracker ---");

        // this is our main loop. it will keep running until the user quits.
        while (true) {
            System.out.println("\n----------------------------------------------------------");
            // at the start of each loop, we show the current state of our guesses.
            analyzer.printGuessMap();

            // now we show the *partial decryption* based on our guesses.
            // this is the core of the "interactivity" part of the rubric.
            System.out.println("\n--- Partially Decrypted S6 Text (C3 parts are '_') ---");
            String partialText = analyzer.getDecryptedTextWithContext();
            System.out.println(partialText);
            System.out.println("----------------------------------------------------------");

            // now we give the user their options.
            System.out.println("\nEnter command:");
            System.out.println("  (g)uess    -> (e.g., 'g h e' means 'guess Cipher H is Plain E')");
            System.out.println("  (u)ndo     -> (e.g., 'u h' means 'undo guess for Cipher H')");
            System.out.println("  (r)eshow   -> (reshow the S6 frequency graph)");
            System.out.println("  (v)alidate -> (check partial text, 'v all' to show all)"); // <-- UPDATED
            System.out.println("  (a)ttempt  -> (run full decryption with current guesses)");
            System.out.println("  (q)uit     -> (exit the program)");
            System.out.print("Your command: ");

            String commandLine = userInputReader.nextLine().trim().toLowerCase();
            
            if (commandLine.length() == 0) {
                continue; // user just pressed enter, loop again.
            }

            char command = commandLine.charAt(0);
            String[] parts = commandLine.split(" "); // split 'g h e' into parts

            // now we figure out what the user wanted to do.
            switch (command) {
                case 'g': // Guess
                    if (parts.length != 3) {
                        System.out.println("!! ERROR: Guess command needs 3 parts. Example: g h e");
                    } else {
                        char cipherChar = parts[1].charAt(0);
                        char plainChar = parts[2].charAt(0);
                        analyzer.makeGuess(cipherChar, plainChar);
                        System.out.printf("==> OK. Guessing cipher '%c' = plain '%c'\n", cipherChar, plainChar);
                    }
                    break;
                
                case 'u': // Undo
                    if (parts.length != 2) {
                        System.out.println("!! ERROR: Undo command needs 2 parts. Example: u h");
                    } else {
                        char cipherChar = parts[1].charAt(0);
                        analyzer.undoGuess(cipherChar);
                        System.out.printf("==> OK. Cleared guess for cipher '%c'.\n", cipherChar);
                    }
                    break;
                
                case 'r': // Reshow S6 frequencies
                    CryptoAnalyzer.printFrequencyMap(
                        "--- [ATTACK] Frequencies for S6 (Substitution) Segments Only ---",
                        substPartFreqs, // use the saved map
                        26 // show all 26
                    );
                    break;

                case 'v': // Validate (UPDATED)
                    System.out.println("\n--- Validating Partial S6 Text ---");
                    
                    // Check if the user typed "v all"
                    boolean showAll = false;
                    if (parts.length == 2 && parts[1].equals("all")) {
                        showAll = true;
                    }

                    String s6Text = analyzer.getDecryptedTextWithContext();
                    // Call the new overloaded method
                    String s6Validation = analyzer.validateText(s6Text, showAll); 
                    System.out.println(s6Validation);
                    break;
                
                case 'a': // Attempt full decryption (UPDATED FOR PHASE 3)
                    System.out.println("\n--- ATTEMPTING FULL DECRYPTION WITH CURRENT GUESSES ---");
                    // this is the big one.
                    // it decrypts S6, then finds the caesar key, then decrypts C3.
                    String fullAttempt = analyzer.getFullyDecryptedText();
                    System.out.println(fullAttempt);
                    
                    // --- NEW FOR PHASE 3 ---
                    System.out.println("\n--- Dictionary Validation ---");
                    // This will call the default validateText(text, false) to show top 10
                    String validationResult = analyzer.validateText(fullAttempt);
                    System.out.println(validationResult);
                    // --- END OF ATTEMPT ---
                    break;

                case 'q': // Quit
                    System.out.println("Exiting analyzer. Goodbye.");
                    userInputReader.close(); // closing the scanner
                    return; // this exits the main method and stops the program.

                default:
                    System.out.println("!! ERROR: Unknown command. Try 'g', 'u', 'r', 'v', 'a', or 'q'.");
                    break;
            }
        }
    }
}