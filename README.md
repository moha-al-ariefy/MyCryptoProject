# CSCI 462: Hybrid Cipher & Cryptanalysis

This repository contains the two phases for the CSCI 462 project:
1.  **Project 1:** The design and implementation of a custom hybrid cipher.
2.  **Project 2:** A full cryptanalysis tool to break the Phase 1 cipher.

---

## Part 1: Project-Phase1 (The Cipher)

this was a project for the CSCI 462 course at RIT Dubai, where i developed from scratch a custom hybrid classical encryption algorithm.

### What it Does (The Cipher)

Fundamentally, the code mixes a Caesar Cipher with a Mono-alphabetic Substitution Cipher in alternating order. It encrypts and decrypts files based on the project requirements.

* it reads plaintext from `plain.txt` to encrypt it.
* it preprocesses the text first, making it all lower case and removing spaces and punctuation.
* [cite_start]the whole process is done to the text in 9-character blocks. [cite: 206]
* [cite_start]the key of the Caesar Cipher component is dynamically changed, which is very nice. [cite: 204]
* the last encrypted text is written to `cipher.txt`, and the decrypted text is written to `decrypted_plain.txt`.

### How to Get it Working (The Cipher)

1.  **Organize your folders properly:** // this is absolutely essential if the package is going to work
    * Set a top-level folder for the project.
    * Inside it, set another folder and name it precisely `cryptographyproject`.
    * Set the `MainCipher.java` file inside the `cryptographyproject` folder.
    * All the rest of the files (`plain.txt`, `cipher.txt`, etc.) go into the top-level folder.

2.  **Put it together:**
    * Open your terminal in the **main** project directory and execute this:
    ```sh
    javac cryptographyproject/MainCipher.java
    ```

3.  **Execute it:**
    * From the same location, execute this command:
    ```sh
    java cryptographyproject.MainCipher
    ```

4.  **Execute the instructions:**
    * The program will ask you what to do (encrypt or decrypt).
    * **Encryption:** takes `plain.txt` and makes `cipher.txt`.
    * **Decryption:** takes `cipher.txt` and makes `decrypted_plain.txt`.

---

## Part 2: Project-Phase2 (The Cryptanalysis Tool)

[cite_start]this is the attack tool for Phase 2, built to reverse-engineer the cipher from Phase 1 without knowing the key. [cite: 168-169]

### What it Does (The Attack)

* [cite_start]It reads a `cipher.txt` file and runs a full frequency analysis (single, diagram, and trigram). [cite: 170, 274, 276]
* [cite_start]**This is the main attack logic:** It separates the ciphertext into the 3-letter Caesar (C3) parts and the 6-letter Substitution (S6) parts. [cite: 171, 196, 213]
* [cite_start]It proves the C3 graph is "flat" (polyalphabetic) [cite: 215, 228] [cite_start]while the S6 graph is "spiky" (monoalphabetic)[cite: 214, 230], which is the cipher's weak point.
* [cite_start]It has a full interactive cracker that uses a `dictionary.txt` file to "score" your guesses and find real English words. [cite: 172-173, 236, 241]

### How to Get it Working (The Attack Tool)

1.  **File Setup:**
    * [cite_start]This uses two new files: `AttackMain.java` (the UI) [cite: 740] [cite_start]and `CryptoAnalyzer.java` (the brain). [cite: 301-303]
    * [cite_start]You *must* have `cipher.txt` and `dictionary.txt` in the main folder for it to work. [cite: 191, 236]

2.  **Compile it:**
    * Open your terminal in the **main** project directory and execute this (it compiles both files):
    ```sh
    javac cryptographyproject/AttackMain.java
    ```

3.  **Execute it:**
    * From the same location, execute this command:
    ```sh
    java cryptographyproject.AttackMain
    ```

### Interactive Commands

[cite_start]Once it's running, you can use these commands to break the cipher: [cite: 233]

* [cite_start]`(g)uess`: Make a guess (e.g., `g m e` means cipher 'm' = plain 'e'). [cite: 233]
* [cite_start]`(u)ndo`: Undo a guess (e.g., `u m`). [cite: 233]
* [cite_start]`(r)eshow`: Reshow all the S6 frequency graphs. [cite: 233]
* [cite_start]`(v)alidate`: Check your partial text against the dictionary for a word score. [cite: 233]
* [cite_start]`(a)ttempt`: Run a full decryption attempt based on your current guesses. [cite: 233]
* [cite_start]`(q)uit`: Exit the program. [cite: 233]