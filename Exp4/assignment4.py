# ###################    BONUS SECTION    #########################
# HINT1:  For example, you can change
# - the learning rate,
# - training epoch,
# - the number of neurons
#  in the hidden layers, or even change
# - the number of hidden layers
# in the neural network architecture.
#
# HINT2: You can also try different compositional semantics methods from the literature. For example,
# you can take the weighted average of the word vectors in a sentence or you can use the
# summation of the vectors of the words in the sentence by filtering out the stop words.
# ###################    BONUS SECTION    #########################

import sys
import numpy as np
from string import punctuation

positives_file = sys.argv[1]
negatives_file = sys.argv[2]
vectors_file = sys.argv[3]


def strip_punctuation(s):
    return ''.join(c for c in s if c not in punctuation)


def read_input_file():
    words_and_vectors = {}

    # lines = tuple(open(vectors_file, 'r'))
    # with open('output.txt', 'a') as output_file:
    with open('output.txt', 'a'):
        with open(vectors_file) as v_file:
            for line in v_file:
                line = line.strip()

                split_result = line.split(':')
                word = split_result[0]
                temp_vectors = split_result[1]

                all_vectors = temp_vectors.split(' ')

                all_vectors = [float(i) for i in all_vectors]

                words_and_vectors[word] = all_vectors

                # output_file.write(word + ' ')
                # for vector in all_vectors:
                #     output_file.write(vector + ' ')

    return words_and_vectors


if __name__ == "__main__":
    # print(strip_punctuation('cema.l'))

    example_sentence = 'heyecanlısın yerinde duramıyorsun karşındaki gayet geniş sakin ve rahat, sev ben de özledim'
    example_sentence2 = 'duymak kendini'

    words_and_their_vectors = read_input_file()
    # # for element in myMap:
    # print('duymak: ')
    # print(myMap.get('duymak'))
    #
    # print('kendini: ')
    # print(myMap.get('kendini'))
    #
    # print('Istanbul: ')
    # print(myMap.get('Istanbul'))
    #
    # print('İstanbul: ')
    # value = myMap.get('İstanbul')
    # print(value)

    sentence_elements = example_sentence.split(' ')

    sum_of_vectors = []
    for i in range(200):
        sum_of_vectors.append(0.0)

    for s_element in sentence_elements:
        vector = words_and_their_vectors.get(s_element)
        if vector is not None:
            sum_of_vectors = np.array(sum_of_vectors) + np.array(vector)

    # vector1 = np.array(myMap.get('duymak'))
    # vector2 = np.array(myMap.get('kendini'))
    #
    # # vector1 = np.array([1, 2, 3])
    # # vector2 = np.array([4, 5, 6])
    #
    # sum_vector = vector1 + vector2
    print(sum_of_vectors)


# # import numpy as np
# import tensorflow as tf
# from tensorflow.examples.tutorials.mnist import input_data
#
# mnist = input_data.read_data_sets("MNIST_data/", one_hot=True)
#
# # bizdeki input -> 200 dimensional vector that is summation of the word vectors in the sentence
# # Output layer is composed of two outputs (we have positive and negative sentences).
# # Number of neurons in the first and second hidden layers are 100.
# # You can change the learning rate hyperparameter (learning rate=0.001).
#
# # Python optimisation variables
# learning_rate = 0.001
# training_epoch = 10
# batch_size = 100  # bizde bu olmayacak
# num_of_neurons = 0
# num_of_hidden_layers = 0
#
# # declare the training data placeholders
# # input x - for 28 x 28 pixels = 784
# x = tf.placeholder(tf.float32, [None, 784])
# # now declare the output data placeholder - 10 digits
# y = tf.placeholder(tf.float32, [None, 10])
#
# # now declare the weights connecting the input to the hidden layer
# W1 = tf.Variable(tf.random_normal([784, 300], stddev=0.03), name='W1')
# b1 = tf.Variable(tf.random_normal([300]), name='b1')
# # and the weights connecting the hidden layer to the output layer
# W2 = tf.Variable(tf.random_normal([300, 10], stddev=0.03), name='W2')
# b2 = tf.Variable(tf.random_normal([10]), name='b2')
#
# # calculate the output of the hidden layer
# hidden_out = tf.add(tf.matmul(x, W1), b1)
# hidden_out = tf.nn.relu(hidden_out)
#
# y_ = tf.nn.softmax(tf.add(tf.matmul(hidden_out, W2), b2))
#
# # The first line is an operation converting the output y_ to a clipped
# # version, limited between 1e-10 to 0.999999.  This is to make sure that we never get a case were we
# # have a log(0) operation occurring during training – this would return NaN and break the training process.
# # The second line is the cross entropy calculation.
# y_clipped = tf.clip_by_value(y_, 1e-10, 0.9999999)
# cross_entropy = -tf.reduce_mean(tf.reduce_sum(y * tf.log(y_clipped) + (1 - y) * tf.log(1 - y_clipped), axis=1))
# # add an optimiser
# optimiser = tf.train.GradientDescentOptimizer(learning_rate=learning_rate).minimize(cross_entropy)
#
# # finally setup the initialisation operator
# init_op = tf.global_variables_initializer()
#
# # define an accuracy assessment operation
# # argmax returns the index of the maximum value in a vector / tensor.
# correct_prediction = tf.equal(tf.argmax(y, 1), tf.argmax(y_, 1))
# accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))
#
# # add a summary to store the accuracy
# tf.summary.scalar('accuracy', accuracy)
#
# merged = tf.summary.merge_all()
# writer = tf.summary.FileWriter('./my_graph')
#
# # start the session
# with tf.Session() as sess:
#     # initialise the variables
#     sess.run(init_op)
#     total_batch = int(len(mnist.train.labels) / batch_size)
#     for epoch in range(training_epoch):
#         # average cross entropy cost for each epoch
#         avg_cost = 0
#         for i in range(total_batch):
#             batch_x, batch_y = mnist.train.next_batch(batch_size=batch_size)
#             # supplying [optimiser, cross_entropy] as the list means that both these operations will be performed.
#             _, c = sess.run([optimiser, cross_entropy], feed_dict={x: batch_x, y: batch_y})
#             avg_cost += c / total_batch
#         print("Epoch:", (epoch + 1), "cost =", "{:.3f}".format(avg_cost))
#         summary = sess.run(merged, feed_dict={x: mnist.test.images, y: mnist.test.labels})
#         writer.add_summary(summary, epoch)
#
#     print("\nTraining complete!")
#     writer.add_graph(sess.graph)
#     print(sess.run(accuracy, feed_dict={x: mnist.test.images, y: mnist.test.labels}))

####################################################################################################

# # first, create a TensorFlow constant
# const = tf.constant(2.0, name="const")
#
# # create TensorFlow variables
# b = tf.placeholder(tf.float32, [None, 1], name='b')
# c = tf.Variable(1.0, name='c')
#
# # now create some operations
# d = tf.add(b, c, name='d')
# e = tf.add(c, const, name='e')
# a = tf.multiply(d, e, name='a')
#
# # setup the variable initialisation
# init_op = tf.global_variables_initializer()
#
# # start the session
# with tf.Session() as session:
#     # initialise the variables
#     session.run(init_op)
#     # compute the output of the graph
#     # a_out = session.run(a)
#     a_out = session.run(a, feed_dict={b: np.arange(0, 10)[:, np.newaxis]})
#     print("Variable a is {}".format(a_out))
#     writer = tf.summary.FileWriter('./my_graph', session.graph)
#
#     writer.close()

###############################################################################

# weighting_variable = tf.Variable([.3], tf.float32)
# bias = tf.Variable([-.3], tf.float32)
# x = tf.placeholder(tf.float32)
# y = tf.placeholder(tf.float32)
#
# linear_model = weighting_variable * x + bias
# squared_deltas = tf.square(linear_model - y)
# loss = tf.reduce_sum(squared_deltas)
#
# optimizer = tf.train.GradientDescentOptimizer(0.01)
# train = optimizer.minimize(loss)
#
# session = tf.Session()
#
# # variables have to be initialized
# # they are not reset until we explicitly reset them
# init = tf.global_variables_initializer()
# session.run(init)
#
# for i in range(1000):
#     session.run(train, {x: [1, 2, 3, 4],
#                         y: [0, -1, -2, -3]})
#
# print(session.run([weighting_variable, bias]))
#
# # print(session.run(linear_model, {x: [1, 2, 3, 4]}))
# # # this print produces -> [0.         0.3        0.6        0.90000004]

###############################################################################
# a = tf.constant(5, name="input_a")
# b = tf.constant(3, name="input_b")
# c = tf.multiply(a, b, name="multiply_c")
# d = tf.add(a, b, name="add_d")
# e = tf.add(c, d, name="add_e")
#
# session = tf.Session()
# output = session.run(e)
#
# writer = tf.summary.FileWriter('./my_graph', session.graph)
#
# writer.close()
# session.close()
###############################################################################

###############################################################################
# import sys
# import re
# import tensorflow as tf
# from string import punctuation
#
# node1 = tf.constant(3.0, tf.float32)
# node2 = tf.constant(4.0)
# node3 = tf.add(node1, node2)
#
# # print("node3: ", node3)
#
# session = tf.Session()
#
# # they'll not actually execute without attaching them to a session
# # print(node1, node2)
#
# # print(session.run([node1, node2]))
# # print(session.run(node1 + node2))
#
# a = tf.placeholder(tf.float32)
# b = tf.placeholder(tf.float32)
# adder_node = a + b
#
# # print(session.run(adder_node, {a: 3, b: 4.5}))
#
# add_and_get_triple = adder_node * 3.
#
# print(session.run(add_and_get_triple, {a: 3, b: 4.5}))

# print(session.run(adder_node, {a: [1, 3], b: [2, 4]}))
###############################################################################

###############################################################################
# positives_file = sys.argv[1]
# negatives_file = sys.argv[2]
# vectors_file = sys.argv[3]
#
#
# def strip_punctuation(s):
#     return ''.join(c for c in s if c not in punctuation)
#
#
# lines = tuple(open(vectors_file, 'r'))
# with open('output.txt', 'a') as output_file:
#     with open(vectors_file) as v_file:
#         for line in v_file:
#             print(line)
#             split_result = line.split(':')
#             word = split_result[0]
#             vectors = split_result[1]
#
#             all_vectors = vectors.split(' ')
#
#             output_file.write(word + ' ')
#             output_file.write(all_vectors[0] + ' ')
###############################################################################


# vector_file_lines = v_file.read().splitlines()

# for line in vector_file_lines:
#     print(line)
# print(lines)
# print(negatives_file)
# print(vectors_file)
