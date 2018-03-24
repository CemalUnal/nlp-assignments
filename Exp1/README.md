# BBM497 Introduction to Natural Language Processing Assignment-1

_Subject: Language Models for E-mails_

_Name and Surname: Cemal ÜNAL_  
_Number: 21328538_

- - - -
## About the Structure

I have wrote my code using **JAVA (JAVA 8)** programming language. And there are nine number of classes.

#### Unigram, Bigram and Trigram Classes

**Unigram**, **Bigram** and **Trigram** **classes** represent the Unigram, Bigram and Trigram Language Models respectively. In each class of these three, there are some methods to;
- Create language models,
- Get unsmoothed/smoothed probability of a sentence,
- Generate unsmoothed/smoothed e-mails.

Also each of these three classes contains a **HashMap** to hold the Unigram, Bigram and Trigram counts. Each **HashMap** represents a **Language Model**. All of these classes extends the **Ngram class**

#### Ngram Class
**Ngram class** is used to prevent writing same methods in each class. Each of **Unigram**, **Bigram** and **Trigram** **classes** extends this class. **Ngram class** contains three methods. These methods are used to;
- Add a word(varies according to Unigram, Bigram and Trigram) to the map.


- Generate e-mails
  - This method contains the logic for the stopping criteria for the e-mails. It creates a random probability. With this random probability, it checks the **ProbabilityChart** and gets appropriate word(varies according to Unigram, Bigram and Trigram) with this random probability.


- To calculate Perplexity of an e-mail.

#### RegexMap Class
This class contains a **HashMap** to hold some regex to be used while eliminating some lines in the **emails.csv** file. This class contains two methods to;
- Initialize the **HashMap**,
- Check whether the line of the **emails.csv** file contains any regex that is in the **HashMap**.
  - Returns **true** if yes, otherwise returns **false**.

#### CsvReader Class
Main purpose of **CsvReader** class is reading the input file. This class contains an **ArrayList** to hold the each email in the **emails.csv** file. It is also able to separate sentences that is in the second training set which name is **dataset.txt** that you gave us. And in this class there are some methods to;
- Read given file,
  - Although its name is CsvReader, it can read the second training set which name is **dataset.txt** that you gave us.


- Separate any punctuation mark that a sentence contains.

#### FWriter Class
Main purpose of **FWriter** class is writing a message to a given output file. This class contains three methods to;
- Open given output file,
- Write a message to given output file,
- Close given output file.

#### ProbabilityChart Class
**ProbabilityChart** class is used to create a virtual table of probabilities. This **probability table** is used while generating emails. And in this class there are some methods to;
- Add a word to **probability table**,
- Get a word from **probability table**.

#### Main Class
This is the main class of the assignment. It calls some methods to;
- Read input file.
- Create language models.
- Generate emails.
- Calculate probability and perplexity of emails.

## Shortcomings of My Code
- Execution time with the second training set which name is **dataset.txt** is nearly 150 seconds. This execution time could be slightly lower.
- Sometimes perplexity gets so small and then it prints out **"Infinity"** to the output file.

## Pros of My Code
- I did not use any other specific libraries to read and parse **emails.csv** file.

## IMPORTANT NOTE BEFORE EXECUTION
!!! I have used JAVA 8 this assignment. It may not be compiled with older versions of JAVA. !!!
