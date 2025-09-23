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
    private static final String substTable = "hilwmkbdpcvazusjgrnqyxfote";

    /**
     * The main method, it's the entry point. Kicks everything off.
     * we will ask the user what they want to do, encrypt or decrypt.
     * @param args Not used here.
     */
    public static void main(String[] args) {
        Scanner userInputReader = new Scanner(System.in); // making a scanner to get user choice.
        System.out.println("Do you want to (1) Encrypt or (2) Decrypt?");
        String choice = userInputReader.nextLine();

        if (choice.equals("1")) {
            System.out.println("Starting encryption...");
            String contentToEncrypt = readMyFile("plain.txt");
            if (contentToEncrypt != null) {
                String finalCipher = goEncrypt(contentToEncrypt);
                writeMyFile("cipher.txt", finalCipher);
                System.out.println("Encryption is done! check the cipher.txt file.");
            }
        } else if (choice.equals("2")) {
            System.out.println("Starting decryption...");
            String contentToDecrypt = readMyFile("cipher.txt");
            if(contentToDecrypt != null) {
                String originalText = goDecrypt(contentToDecrypt);
                writeMyFile("decrypted_plain.txt", originalText);
                System.out.println("Decryption is done! check the decrypted_plain.txt file.");
            }
        } else {
            System.out.println("That was not a valid choice. Run me again.");
        }

        userInputReader.close(); // closing the scanner is good practice.
    }

    /**
     * this is the main encryption logic function.
     * it takes the plain text and runs the whole algorithm on it.
     */
    public static String goEncrypt(String rawText) {
        String cleanText = rawText.toLowerCase().replaceAll("[^a-z]", ""); // must clean the text first, no spaces or anything.

        /* now we need to make sure the text length is a multiple of 9
         if it's not, we have to add padding using x, y, and z.
        */
        StringBuilder textInProgress = new StringBuilder(cleanText);
        int letterToAdd = 0;
        while (textInProgress.length() % 9 != 0) {
            textInProgress.append("xyz".charAt(letterToAdd % 3));
            letterToAdd++;
        }
        String readyText = textInProgress.toString();

        StringBuilder resultingCipher = new StringBuilder(); // this will hold the final encrypted string.

        // this loop will go through the text in chunks of 9 chars. thats the main pattern. 3 then 6.
        for (int i = 0; i < readyText.length(); i = i + 9) {
            /* first part of the block is 3 letters for the Caesar cipher */
            String caesarPart = readyText.substring(i, i + 3);
            int shiftKey = 0; // the default shift is 0.
            if (i + 3 < readyText.length()) { // we need to check if there is a next letter to get the shift from.
                char shiftChar = readyText.charAt(i + 3);
                shiftKey = theAlphabet.indexOf(shiftChar);
            }
            
            // here we do the actual caesar shifting for the 3 letters
            for(char c : caesarPart.toCharArray()) {
                int originalIndex = theAlphabet.indexOf(c);
                int newIndex = (originalIndex + shiftKey) % 26;
                resultingCipher.append(theAlphabet.charAt(newIndex));
            }

            /* second part of the block is 6 letters for the substitution table */
            String substPart = readyText.substring(i + 3, i + 9);
            for(char c : substPart.toCharArray()) {
                int originalIndex = theAlphabet.indexOf(c);
                resultingCipher.append(substTable.charAt(originalIndex));
            }
        }

        return resultingCipher.toString();
    }
    
    /**
     * this is the decryption function. it has to reverse what goEncrypt did.
     * its a bit tricky because of the caesar key.
     */
    public static String goDecrypt(String cipherText) {
        StringBuilder recoveredPlain = new StringBuilder(); // this will hold the final decrypted string.
        
        // just like encryption, we process in chunks of 9.
        for (int i = 0; i < cipherText.length(); i += 9) {
            String caesarPartEncrypted = cipherText.substring(i, i + 3);
            String substPartEncrypted = cipherText.substring(i + 3, i + 9);
            
            // first, we decrypt the substitution part because we need it to find the caesar key.
            StringBuilder substPartDecrypted = new StringBuilder();
            for(char c : substPartEncrypted.toCharArray()) {
                int encryptedIndex = substTable.indexOf(c);
                substPartDecrypted.append(theAlphabet.charAt(encryptedIndex));
            }
            
            /* the key for the caesar part was the first letter of the original substitution block.
             so now that we decrypted it, we can get that character and find its index.
            */
            char keyChar = substPartDecrypted.charAt(0);
            int shiftKey = theAlphabet.indexOf(keyChar);
            
            // now we can finally decrypt the caesar part using the key we just found.
            StringBuilder caesarPartDecrypted = new StringBuilder();
            for(char c : caesarPartEncrypted.toCharArray()) {
                int encryptedIndex = theAlphabet.indexOf(c);
                // the formula is (encrypted index - shift + 26) % 26 to handle negative numbers.
                int originalIndex = (encryptedIndex - shiftKey + 26) % 26;
                caesarPartDecrypted.append(theAlphabet.charAt(originalIndex));
            }
            
            // putting the block back together.
            recoveredPlain.append(caesarPartDecrypted);
            recoveredPlain.append(substPartDecrypted);
        }
        
        // now we have to try and remove the padding from the end.
        String finalDecrypted = recoveredPlain.toString();
        // this is a simple way, it might remove real letters if the message ended with x, y, or z.
        // but for the project its okay.
        while(finalDecrypted.endsWith("z") || finalDecrypted.endsWith("y") || finalDecrypted.endsWith("x")) {
            if (finalDecrypted.endsWith("z")) {
                finalDecrypted = finalDecrypted.substring(0, finalDecrypted.length() - 1);
            }
             if (finalDecrypted.endsWith("y")) {
                finalDecrypted = finalDecrypted.substring(0, finalDecrypted.length() - 1);
            }
             if (finalDecrypted.endsWith("x")) {
                finalDecrypted = finalDecrypted.substring(0, finalDecrypted.length() - 1);
            }
        }
        
        return finalDecrypted;
    }

    // a simple helper function to read a file, returns null if it fails.
    private static String readMyFile(String filename) {
        try {
            return new String(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException e) { // if something goes wrong reading the file.
            System.out.println("Oh no, error reading file: " + filename);
            e.printStackTrace();
            return null;
        }
    }

    // another helper to write text to a file.
    private static void writeMyFile(String filename, String data) {
        try (FileWriter fWriter = new FileWriter(new File(filename))) {
            fWriter.write(data);
        } catch (IOException e) { // if something goes wrong writing the file.
            System.out.println("Oh no, error writing file: " + filename);
            e.printStackTrace();
        }
    }
}
