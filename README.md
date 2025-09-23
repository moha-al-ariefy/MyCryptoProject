# Project-Phase1: A Custom Crypto Algorithm

this was a project for the CSCI 462 course at RIT Dubai, where i developed from scratch a custom hybrid classical encryption algorithm.

## What it Does

Fundamentally, the code mixes a Caesar Cipher with a Mono-alphabetic Substitution Cipher in alternating order. It encrypts and decrypts files based on the project requirements.

* it reads plaintext from `plain.txt` to encrypt it.
* to encrypt, it reads the plaintext from `plaintext.txt`.
* it preprocesses the text first, making it all lower case and removing spaces and punctuation.
* the whole process is done to the text in 9-character blocks.
* the key of the Caesar Cipher component is dynamically changed, which is very nice.
* the last encrypted text is written to `cipher.txt`, and the decrypted text is written to `decrypted_plain.txt`.

## How to Get it Working

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