import sys
import numpy as np
import string
import tensorflow as tf


def implement_multi_layer_perceptron(train_mtrx, train_mtrx_w_tags, test_mtrx, test_mtrx_w_tags):
    learning_rate = 0.001
    training_epoch = 10
    number_of_neurons = 100

    training_data_placeholder = tf.placeholder(tf.float32, [None, 200])
    output_data_placeholder = tf.placeholder(tf.float32, [None, 2])

    # Declare the weights connecting the input to the hidden layer
    # initialise the values of the weights using a random normal distribution.
    # random_normal allows us to create a matrix of a given size that is initialized with random samples
    w1 = tf.Variable(tf.random_normal([200, number_of_neurons], stddev=0.03), name='w1')
    b1 = tf.Variable(tf.random_normal([number_of_neurons]), name='b1')

    # Declare the weights connecting first hidden layer to the second hidden layer
    w2 = tf.Variable(tf.random_normal([number_of_neurons, 200], stddev=0.03), name='w2')
    b2 = tf.Variable(tf.random_normal([200]), name='b2')

    w3 = tf.Variable(tf.random_normal([200, 2], stddev=0.03), name='w3')
    b3 = tf.Variable(tf.random_normal([2]), name='b3')

    # Calculate the output of the first hidden layer
    hidden_layer_out_one = tf.add(tf.matmul(training_data_placeholder, w1), b1)
    hidden_layer_out_one = tf.nn.relu(hidden_layer_out_one)

    # Calculate the output of the second hidden layer
    hidden_layer_out_two = tf.add(tf.matmul(hidden_layer_out_one, w2), b2)
    hidden_layer_out_two = tf.nn.relu(hidden_layer_out_two)

    # Calculate the output layer
    # After tha apply softmax activation
    output_layer = tf.nn.softmax(tf.add(tf.matmul(hidden_layer_out_two, w3), b3))

    # It is to make sure that we will never get log(0) during training
    output_clipped = tf.clip_by_value(output_layer, 1e-10, 0.9999999)
    cross_entropy = -tf.reduce_mean(tf.reduce_sum(output_data_placeholder * tf.log(output_clipped)
                                                  + (1 - output_data_placeholder) * tf.log(1 - output_clipped)
                                                  , axis=1))
    # Gradient Descent optimizer
    # We are telling that we want to minimize the cross entropy
    optimiser = tf.train.GradientDescentOptimizer(learning_rate=learning_rate).minimize(cross_entropy)

    # setup the operator for variable initialisation
    initialization_operator = tf.global_variables_initializer()

    correct_prediction = tf.equal(tf.argmax(output_data_placeholder, 1), tf.argmax(output_layer, 1))
    accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))

    # create the session
    with tf.Session() as sess:
        sess.run(initialization_operator)

        for epoch in range(training_epoch):
            sess.run([optimiser, cross_entropy], feed_dict={training_data_placeholder: train_mtrx,
                                                            output_data_placeholder: train_mtrx_w_tags})

        accuracy = sess.run(accuracy, feed_dict={training_data_placeholder: test_mtrx, output_data_placeholder: test_mtrx_w_tags})
        print("Accuracy is ", accuracy * 100, " percent")


def strip_punctuation(s):
    """
        Removes punctuations from the given sentence and replaces these punctuations with a space character.
    """
    replace_punctuation = str.maketrans(string.punctuation, ' ' * len(string.punctuation))
    return s.translate(replace_punctuation)


def read_sentences(sentences_file, l_sentences, sentences_w_tags, positive_or_negative):
    """
        It creates a tag according to sentence type.
        This tag is
            - [1, 0] for the positive sentences
            - [0, 1] for the negative sentences
        Then opens the sentences file.
        For each line in these files, it removes punctuations and adds current sentence to the list
        Stores the each sentence with its tag in a dictionary.
    """
    tag = []

    if positive_or_negative == "positives":
        tag.append(1)
        tag.append(0)

    elif positive_or_negative == "negatives":
        tag.append(0)
        tag.append(1)

    with open(sentences_file) as file:
        for line in file:
            line = line.strip()
            line = strip_punctuation(line)

            # add current sentence to the list
            l_sentences.append(line)
            # add current sentence with its tag to the dictionary
            sentences_w_tags[line] = tag


def read_vectors_file(file):
    """
        Opens vectors file.
        For each line in the vectors file, it separates the word from its vectors.
        Stores the each word with its vectors in a dictionary.
    """
    words_and_vectors = {}
    all_vectors = []

    with open(file) as v_file:
        for line in v_file:
            line = line.strip()

            split_result = line.split(':')
            _word = split_result[0]
            temp_vectors = split_result[1]

            all_vectors = temp_vectors.split(' ')
            all_vectors = [float(f) for f in all_vectors]

            # add current word with its vector to the dictionary
            words_and_vectors[_word] = all_vectors

    return words_and_vectors, len(all_vectors)


def split_list(shuf_sentences, t_percentage):
    """
        Splits a list into two pieces according to the given percentage and returns these two pieces.
    """
    index = int(round(t_percentage * len(shuf_sentences)))
    return shuf_sentences[index:], shuf_sentences[:index]


def create_train_and_test_matrices(matrix, matrix_with_tags, sentences, vec_size):
    """
        Splits a list according to the given percentage.
    """
    sentence_index = 0

    # for each sentence in the training and test set,
    # calculate vectors by summing up the vector of each word.
    # then store the sentences with vectors in a matrix.
    for sentence in sentences:
        # create and initialize sum of vectors array
        # it will reinitialized for each sentence
        sum_of_vectors = []
        for i in range(vec_size):
            sum_of_vectors.append(0.0)

        current_sentence_words = sentence.split()
        # for each word in the current sentence
        for word in current_sentence_words:
            # get the vector of the current word in the current sentence
            vector = words_and_their_vectors.get(word)

            if vector is not None:
                sum_of_vectors = np.array(sum_of_vectors) + np.array(vector)

        matrix[sentence_index] = sum_of_vectors

        # get the tag of the current sentence
        tag_of_current_sentence = sentences_with_tags.get(sentence)

        if tag_of_current_sentence is not None:
            matrix_with_tags[sentence_index] = tag_of_current_sentence

        sentence_index += 1


if __name__ == "__main__":
    positives_file = sys.argv[1]
    negatives_file = sys.argv[2]
    vectors_file = sys.argv[3]
    train_percentage = float(sys.argv[4])

    # Read all sentences and store them in an array
    all_sentences = []
    sentences_with_tags = {}
    read_sentences(positives_file, all_sentences, sentences_with_tags, "positives")
    read_sentences(negatives_file, all_sentences, sentences_with_tags, "negatives")

    # Shuffle all the sentences
    # I did it two times because I thought that it shuffled better in this way
    np.random.shuffle(all_sentences)
    np.random.shuffle(all_sentences)

    # Use certain percentage of shuffled sentences as train set
    # Use the remaining sentences as test set
    train_sentences = all_sentences[int(len(all_sentences) * .0): int(len(all_sentences) * (train_percentage / 100))]
    test_sentences = all_sentences[int(len(all_sentences) * (train_percentage / 100)): int(len(all_sentences) * 1)]

    # Get all words and their vectors as dictionary (map)
    # And get vector size (it is 200 in our sample input - vectors.txt)
    words_and_their_vectors, vector_size = read_vectors_file(vectors_file)

    train_matrix = np.zeros((len(train_sentences), vector_size))
    train_matrix_with_tags = np.zeros((len(train_sentences), 2))

    test_matrix = np.zeros((len(test_sentences), vector_size))
    test_matrix_with_tags = np.zeros((len(test_sentences), 2))

    create_train_and_test_matrices(train_matrix, train_matrix_with_tags, train_sentences, vector_size)
    create_train_and_test_matrices(test_matrix, test_matrix_with_tags, test_sentences, vector_size)

    implement_multi_layer_perceptron(train_matrix, train_matrix_with_tags, test_matrix, test_matrix_with_tags)

