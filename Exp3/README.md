# BBM497 Introduction to Natural Language Processing Assignment-3

_**Subject:** Word Sense Disambiguation using Naive Bayes_

_**Name and Surname:** Cemal ÜNAL_  
_**Number:** 21328538_

- - - -

## Introduction

I wrote my code using **JAVA (JAVA 8)** programming language. And there are six number of classes.

#### Feature Vector Structure

I have created a Map that contains the **Sense Ids** as keys and another Map as values. This inner map holds the **frequencies** of the context words belonging to a specific **Sense Id**. These context words does not contain stop words, and all of them are stemmed via a stemmer method. (I did not implement the stemmer method, it is taken from [here](https://github.com/uttesh/exude/blob/master/src/main/java/com/uttesh/exude/stemming/Stemmer.java). And all rights goes to the [@uttesh](https://github.com/uttesh))

#### Preprocessing.java

In **Preprocessing** class the following operations are performed respectively:
- Read the stop words and store them into an ArrayList.
- Read the given input file line by line,
- If the current line contains;
  - _**instance**_ tag, then extract the **word id** from it.
  - _**answer**_ tag, then extract the **sense id** from it.


- If the current line is inside the **context tags**, then;
  - Extract the words from **p tag**.
  - Add this extracted words to an array (I will mention it as _'unorderedWordList'_ in the following lines).
  - If *head* tag is found in the line, then extract the word which is inside of this tag. And save it as _'currentAmbiguousWord'_


- If the current line contains **context end tag** (ex. </context>), then;
  - Get the index of the _currentAmbiguousWord_ in the _unorderedWordList_.
  - Take previous three and next three elements and also their tags (ex. NN, RB, NNP) according to index of the _currentAmbiguousWord_ (Since our window size is 3 in this assignment)


- If stopWords list does not contain these previous three and next three elements, then;
  - Send them to the stemmer method and if current input file is the train file, then;
    - add returned stems to the feature vector.
  - If the current input file is the test file, then;
    - Create a mini-feature vector from the current context and call Naive Bayes calculator method. (Inside the **NaiveBayes** class)

#### NaiveBayes.java
Main purpose of **NaiveBayes** class is calculating the Naive Bayes probabilites and taking the sense id that has the maximum probability for an ambiguous word.
Each element in the formula is described below according to my structure with the annotations as exactly as specified in the assignment sheet.

  - _**C(si)**_ is equal to the total frequencies of each feature vector item belonging to a specific sense id.
    - To find this count, first I am retrieving the each inner map related to a specific sense id, and summing up all the frequencies. 
  - _**N**_ is equal to the number of all context words in the training corpus including stop words.
  - _**C(fj, si)**_ is equal to the frequency of the current feature vector item belonging to a specific sense id.
    - To find this count, first I am retrieving the inner map related to a specific sense id, in this inner map I am retrieving the frequency.

#### Main.java
This is the main class of the assignment. It calls the methods that reads and processes the given input file, and it calls the methods to open and close the stopWords file.

#### FWriter.java
Main purpose of **FWriter** class is writing a message to a given output file. This class contains three methods to;
  - Open given output file,
  - Write a message to given output file,
  - Close given output file.
