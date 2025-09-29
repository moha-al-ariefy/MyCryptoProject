package cryptographyproject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

// this is the main class where all the magic happens for the crypto project.
public class MainCipher {

    // these are like the secret keys for our cipher. dont change them unless you know what you are doing.
    private static final String theAlphabet = "abcdefghijklmnopqrstuvwxyz";
    private static final String substTable  = "hilwmkbdpcvazusjgrnqyxfote";

    // Master switch for verbose step-by-step tracing:
    //   null  -> ask the user at runtime
    //   true  -> always show step-by-step
    //   false -> never show step-by-step
    private static final Boolean VERBOSE_DEFAULT = null;

    /**
     * The main method, it's the entry point. Kicks everything off.
     * we will ask the user what they want to do, encrypt or decrypt.
     */
    public static void main(String[] args) {
        Scanner userInputReader = new Scanner(System.in); // making a scanner to get user choice.
        System.out.println("Do you want to (1) Encrypt or (2) Decrypt?");
        String choice = userInputReader.nextLine();

        // === CLI step: ask the user if they want detailed output ===
        // If VERBOSE_DEFAULT is null, the user is prompted.
        // Otherwise, VERBOSE_DEFAULT forces verbose on/off automatically.
        boolean verbose = true;
        if (VERBOSE_DEFAULT == null) {
            System.out.println("Show step-by-step details? (y/n) [y]:");
            String v = userInputReader.nextLine().trim().toLowerCase();
            verbose = (v.isEmpty() || v.startsWith("y"));
        } else {
            verbose = VERBOSE_DEFAULT;
        }

        // === CLI step: branch depending on user choice ===
        if (choice.equals("1")) {
            // If user chose encryption:
            //  - read plain.txt
            //  - encrypt contents
            //  - write result to cipher.txt
            System.out.println("Starting encryption...");
            String contentToEncrypt = readMyFile("plain.txt");
            if (contentToEncrypt != null) {
                String finalCipher = goEncrypt(contentToEncrypt, verbose);
                writeMyFile("cipher.txt", finalCipher);
                System.out.println("\nEncryption is done! check the cipher.txt file.");
            }
        } else if (choice.equals("2")) {
            // If user chose decryption:
            //  - read cipher.txt
            //  - decrypt contents
            //  - write result to decrypted_plain.txt
            System.out.println("Starting decryption...");
            String contentToDecrypt = readMyFile("cipher.txt");
            if(contentToDecrypt != null) {
                String originalText = goDecrypt(contentToDecrypt, verbose);
                writeMyFile("decrypted_plain.txt", originalText);
                System.out.println("\nDecryption is done! check the decrypted_plain.txt file.");
            }
        } else {
            // If the input was neither 1 nor 2, show an error and exit.
            System.out.println("That was not a valid choice. Run me again.");
        }

        userInputReader.close(); // closing the scanner is good practice.
    }

    // Backward-compatible wrapper
    public static String goEncrypt(String rawText) { return goEncrypt(rawText, true); }

    // Verbose-capable encryption
    public static String goEncrypt(String rawText, boolean verbose) {
        if (verbose) {
            System.out.println("\n--- ENCRYPT: Input (raw) ---");
            preview(rawText);
        }

        String cleanText = rawText.toLowerCase().replaceAll("[^a-z]", "");
        if (verbose) {
            System.out.println("\n[1] Normalize to letters-only (lowercase):");
            System.out.println("    " + cleanText);
            System.out.println("    length = " + cleanText.length());
        }

        StringBuilder textInProgress = new StringBuilder(cleanText);
        int letterToAdd = 0;
        while (textInProgress.length() % 9 != 0) {
            textInProgress.append("xyz".charAt(letterToAdd % 3));
            letterToAdd++;
        }
        String readyText = textInProgress.toString();
        if (verbose) {
            System.out.println("\n[2] Pad with \"xyz\" until length % 9 == 0:");
            System.out.println("    padded = " + readyText);
            System.out.println("    length = " + readyText.length());
        }

        StringBuilder resultingCipher = new StringBuilder();

        if (verbose) {
            System.out.println("\n[3] Process blocks of 9 (3 Caesar, 6 Substitution):");
        }
        for (int i = 0, seg = 1; i < readyText.length(); i = i + 9, seg++) {
            String block = readyText.substring(i, i + 9);
            String caesarPart = block.substring(0, 3);
            String substPart  = block.substring(3, 9);

            char shiftChar = substPart.charAt(0);
            int shiftKey = theAlphabet.indexOf(shiftChar);

            if (verbose) {
                System.out.printf("%n  [Segment %d] block [%d..%d): \"%s\"%n", seg, i, i + 9, block);
                System.out.printf("    A) Caesar 3 letters: \"%s\"%n", caesarPart);
                System.out.printf("       Next plaintext letter for shift = '%c' → index %d%n", shiftChar, shiftKey);
            }

            StringBuilder caesarOut = new StringBuilder();
            for (char p : caesarPart.toCharArray()) {
                int pIdx = theAlphabet.indexOf(p);
                int cIdx = (pIdx + shiftKey) % 26;
                char c = theAlphabet.charAt(cIdx);
                caesarOut.append(c);
                if (verbose) {
                    System.out.printf("         %c (%2d) + %2d ⇒ %c (%2d)%n", p, pIdx, shiftKey, c, cIdx);
                }
            }

            if (verbose) {
                System.out.printf("    B) Substitution 6 letters: \"%s\"%n", substPart);
            }
            StringBuilder substOut = new StringBuilder();
            for (char p : substPart.toCharArray()) {
                int pIdx = theAlphabet.indexOf(p);
                char c = substTable.charAt(pIdx);
                substOut.append(c);
                if (verbose) {
                    System.out.printf("         %c → %c (monoalphabetic)%n", p, c);
                }
            }

            resultingCipher.append(caesarOut).append(substOut);

            if (verbose) {
                System.out.println("    Running ciphertext: " + resultingCipher);
            }
        }

        if (verbose) {
            System.out.println("\n=== FINAL CIPHERTEXT ===");
            System.out.println(resultingCipher);
        }

        return resultingCipher.toString();
    }

    // Backward-compatible wrapper
    public static String goDecrypt(String cipherText) { return goDecrypt(cipherText, true); }

    // Verbose-capable decryption
    public static String goDecrypt(String cipherText, boolean verbose) {
        if (verbose) {
            System.out.println("\n--- DECRYPT: Input (raw) ---");
            preview(cipherText);
        }

        String lettersOnly = cipherText.toLowerCase().replaceAll("[^a-z]", "");
        if (verbose) {
            System.out.println("\n[1] Normalize to letters-only (lowercase):");
            System.out.println("    " + lettersOnly);
            System.out.println("    length = " + lettersOnly.length());
            if (lettersOnly.length() % 9 != 0) {
                System.out.println("    WARNING: ciphertext length is not multiple of 9; attempting best-effort block processing.");
            }
        }

        StringBuilder recoveredPlain = new StringBuilder();

        if (verbose) {
            System.out.println("\n[2] Process blocks (expect 9 each: 3 CaesarCiph + 6 SubCiph):");
        }
        for (int i = 0, seg = 1; i < lettersOnly.length(); i += 9, seg++) {
            int end = Math.min(i + 9, lettersOnly.length());
            String block = lettersOnly.substring(i, end);
            String ciphCaesar = block.substring(0, Math.min(3, block.length()));
            String ciphSub    = (block.length() > 3) ? block.substring(3) : "";

            if (ciphCaesar.length() < 3 || ciphSub.length() < 1) {
                if (verbose) {
                    System.out.printf("%n  [Segment %d] Incomplete block \"%s\" — skipping detailed steps%n", seg, block);
                }
                recoveredPlain.append(block);
                continue;
            }

            if (verbose) {
                System.out.printf("%n  [Segment %d] block [%d..%d): \"%s\"%n", seg, i, end, block);
                System.out.printf("    B) Inverse Substitution 6 letters: \"%s\"%n", ciphSub);
            }

            StringBuilder substPartDecrypted = new StringBuilder();
            for (char c : ciphSub.toCharArray()) {
                int encIdx = substTable.indexOf(c);
                char p = theAlphabet.charAt(encIdx);
                substPartDecrypted.append(p);
                if (verbose) {
                    System.out.printf("         %c → %c (inverse monoalphabetic)%n", c, p);
                }
            }

            char keyChar = substPartDecrypted.charAt(0);
            int shiftKey = theAlphabet.indexOf(keyChar);

            if (verbose) {
                System.out.printf("    A) Caesar 3 letters (cipher): \"%s\"%n", ciphCaesar);
                System.out.printf("       Derived next-plaintext letter = '%c' → shift %d%n", keyChar, shiftKey);
            }

            StringBuilder caesarPartDecrypted = new StringBuilder();
            for (char c : ciphCaesar.toCharArray()) {
                int cIdx = theAlphabet.indexOf(c);
                int pIdx = (cIdx - shiftKey) % 26;
                if (pIdx < 0) pIdx += 26;
                char p = theAlphabet.charAt(pIdx);
                caesarPartDecrypted.append(p);
                if (verbose) {
                    System.out.printf("         %c (%2d) - %2d ⇒ %c (%2d)%n", c, cIdx, shiftKey, p, pIdx);
                }
            }

            recoveredPlain.append(caesarPartDecrypted);
            recoveredPlain.append(substPartDecrypted);

            if (verbose) {
                System.out.println("    Running plaintext: " + recoveredPlain);
            }
        }

        String finalDecrypted = recoveredPlain.toString();

        if (verbose) {
            System.out.println("\n[3] Remove trailing padding \"x|y|z\" if present:");
            System.out.println("    before: " + finalDecrypted);
        }

        while(finalDecrypted.endsWith("z") || finalDecrypted.endsWith("y") || finalDecrypted.endsWith("x")) {
            finalDecrypted = finalDecrypted.substring(0, finalDecrypted.length() - 1);
        }

        if (verbose) {
            System.out.println("    after : " + finalDecrypted);
            System.out.println("\n=== FINAL PLAINTEXT (letters only) ===");
            System.out.println(finalDecrypted);
        }

        return finalDecrypted;
    }

    // a simple helper function to read a file, returns null if it fails.
    private static String readMyFile(String filename) {
        try {
            return new String(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException e) {
            System.out.println("Oh no, error reading file: " + filename);
            e.printStackTrace();
            return null;
        }
    }

    // another helper to write text to a file.
    private static void writeMyFile(String filename, String data) {
        try (FileWriter fWriter = new FileWriter(new File(filename))) {
            fWriter.write(data);
        } catch (IOException e) {
            System.out.println("Oh no, error writing file: " + filename);
            e.printStackTrace();
        }
    }

    // pretty-print helper
    private static void preview(String s) {
        if (s == null) {
            System.out.println("  (null)");
            return;
        }
        if (s.length() <= 160) {
            System.out.println("  " + s);
        } else {
            System.out.println("  " + s.substring(0, 160) + " ... (+" + (s.length() - 160) + " chars)");
        }
    }
}