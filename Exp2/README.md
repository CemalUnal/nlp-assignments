# BBM497 Introduction to Natural Language Processing
# Assignment-2

_Subject: Spelling Correction with Hidden Markov Model Using Viterbi Algorithm_

_Name and Surname: Cemal ÜNAL_  
_Number: 21328538_

- - - -
## About the Structure

I have wrote my code using **JAVA (JAVA 8)** programming language. And there are six number of classes.

#### EditDistanceCalculator.java
This class contains a method to calculate **min edit distance** between two words. This method also determines the type of operation to get wrong word from given correct word. For example correct word is watch and wrong word is wach. Then;
- The edit distance will be **1**.
- Type of operation is **deletion**
- Letters **"tc"** in the correct word became **"c"** in the wrong word.
 - In the code, variable **correctLetters** corresponds to **"tc"**
 - Variable **wrongLetters** corresponds to **"c"**

After determining the **type of operation**, **correctLetters** and **wrongLetters**, they are all put to a map. These maps store how many times which letters are replaced by which letters. These maps are;
- InsertionInfoMap to store the words related to insertion operation,
- DeletionInfoMap to store the words related to deletion operation,
- SubstitutionInfoMap to store the words related to substitution operation

Also **initial probabilities**, **transition probabilities** and **emission probabilities** are calculated in this class.

#### Preprocessing.java
Main purposes of **Preprocessing** class are;
- Reading the given input file line by line,
- Extracting error tag from input file line,
- Determining the correct form of the given line,
- Adding sentence boundaries to the given line,
- Calculating and storing unigram and bigram counts in a map,
- Storing each **wrong word** with its corresponding **correct words** in a map


#### Viterbi.java
**Viterbi class** aims to implement **Viterbi algorithm** on **Hidden Markov Model** with given wrong sentence to find the correct version of it. For each wrong line in the dataset, it does the following;

- Checks if the current word has any candidate correct words,
  - If no - then simply adds the transition probability to the **viterbiNodeList** which contains **ViterbiNode** type objects
  - If yes - **then for each candidate correct word**, it calculates **emission** and **transition probabilities**. Also it multiplies them with the previous ViterbiNode probability.

- And adds the ViterbiNode that has the max probability to the **viterbiNodeList**.

- After generating the **viterbiNodeList**, it evaluates the accuracy. If the correct version of a word matches with the word that is generated from viterbi algorithm, it increments the **correctGuessCount** by one.
- Then it divides **correctGuessCount** by total number of wrong words in the dataset which name is **totalWrongWordCount** (*attribute of Preprocessing class*)

- After all, it writes the wrong version of the sentence along with the viterbi version of this sentence to the output file. Also it writes the accuracy to this file.

#### ViterbiNode.java
It represents a state in the Hidden Markov Model. Attributes of this class are;
- **wordWithMaxProb** - The word that has the maximum probability (*This probability is coming from viterbi algorithm.*) In other words, it is the word that most likely to be correct one.
- **wrongWord** - Wrong typed word
- **viterbiProbability** - Probabiliy of **wordWithMaxProb**

#### FWriter.java
Main purpose of **FWriter** class is writing a message to a given output file. This class contains three methods to;
- Open given output file,
- Write a message to given output file,
- Close given output file.

#### Main.java
This is the main class of the assignment. It calls the method reads and processes the given input file. And also it calls the method that implements viterbi algorithm.

#### Accuracy
Accuracy of my code is approximately 67.79 percent.

## Shortcomings of My Code
- Execution time is nearly 40 minutes. This execution time could be slightly lower.

## IMPORTANT NOTE BEFORE EXECUTION
!!! I have used JAVA 8 this assignment. It may not be compiled with older versions of JAVA. !!!
