# BBM497 Introduction to Natural Language Processing Assignment-4

_**Subject:** Sentiment Analysis With Deep Learning_

_**Name and Surname:** Cemal ÜNAL_  
_**Number:** 21328538_

- - - -

## Introduction

In this assignment I have used Python programming language (Python 3) along with TensorFlow (tensorflow 1.8.0) library.

## Algorithm
- Read all sentences and tag current sentence as ``[1,0]`` if the meaning of it is _positive_ or tag it as ``[0,1]`` if the meaning of it is _negative_. And store them in a list
- Shuffle them using `numpy.random.shuffle`.
- Split the tagged sentences according to a given percentage. Store the first part as train data and store the remaining part as test data.
- Read the vectors file and store the each word with its vectors in a `dictionary`.
- Then create 4 matrices. If we think that the given percentage is 75 and since there are 149 sentences in total containing positive and negatives. These matrices are;
  - Train matrix
    - It is a 111x200 matrix that contains vector sums for each word in each train sentence.
  - Train matrix with tags
    - It is a 111x2 matrix that contains tags for each train sentence. ([1,0]  for the positives and [0,1] for the negatives)
  - Test matrix
    - It is a 38x200 matrix that contains vector sums for each word in each test sentence.
  - Test matrix with tags
    - It is a 38x2 matrix that contains tags for each test sentence. ([1,0]  for the positives and [0,1] for the negatives)
- Call the function to implement multilayer perceptron.
- The TensorFlow functions used in this implementor function are described unde the below title.

## TensorFlow Functions and Utilities That I Used in My Assignment

#### tf.placeholder
In the TensorFlow documentation it says that **_Placeholder inserts a placeholder for a tensor that will be always fed._** In simple words, a placeholder is simply a variable that we will assign data to at a later date. In my code, I did not know what the value of the array `training_data_placeholder` and `output_data_placeholder` would be during the declaration phase. So I defined them as placeholders.

For example
- `training_data_placeholder = tf.placeholder(tf.float32, [None, 200])`
- `output_data_placeholder = tf.placeholder(tf.float32, [None, 2])`

Since I am not making any initialization in the declaration, TensorFlow needs to know what data type each element within the tensor is going to be. I am providing it by using `tf.float32`. `[None, 200]` is used to specify the shape of the data that will injected to this variable. I have used _**None**_ since the size of the train and test matrices are different. And TensorFlow will decide it later on while execution.

#### tf.Variable and tf.random_normal
I have declared weights and bias values for the connections between input layer and first hidden layer, between other hidden layers, and lastly between last hidden layer and the output layer.
For example below two line represents the weight and bias value between input layer and first hidden layer.
- `w1 = tf.Variable(tf.random_normal([200, number_of_neurons], stddev=0.03), name='w1')`
- `b1 = tf.Variable(tf.random_normal([number_of_neurons]), name='b1')`

I have initialized the values of the weights using a random normal distribution with a standard deviation 0.03.
**tf.random_normal** Gives us random values from a normal distribution. Its usage can be seen from the above code fragment.

#### tf.add, tf.matmul and tf.nn.relu
In the first line, we execute the standard matrix multiplication of the weights (W1) by the input vector x and we add the bias b1.
- `hidden_layer_out_one = tf.add(tf.matmul(training_data_placeholder, w1), b1)`
- `hidden_layer_out_one = tf.nn.relu(hidden_layer_out_one)`

**tf.matmul** allows us to perform matrix multiplication easily. And using the **tf.add** we can add our result matrix and bias value.
Using **tf.nn.relu**, I applied _**ReLU (Rectified Linear Unit)**_ activation function to the output of each hidden layer.

#### tf.nn.softmax
We were expecting to apply softmax to the output layer in this assignment, so I have used **tf.nn.softmax** as the following;

- `output_layer = tf.nn.softmax(tf.add(tf.matmul(hidden_layer_out_two, w3), b3))`

#### tf.clip_by_value, tf.reduce_mean, tf.reduce_sum and tf.log
It is used to make sure that we will never get log(0) during the training phase.

- `output_clipped = tf.clip_by_value(output_layer, 1.1754943508222875e-38, 0.9999999)`

It converts the **output_layer** to a clipped version, result is between 1.1754943508222875e-38 and 0.999999.

To calculate the cross entropy, I have used below code;

`cross_entropy = -tf.reduce_mean(tf.reduce_sum(output_data_placeholder * tf.log(output_clipped) + (1 - output_data_placeholder) * tf.log(1 - output_clipped), axis=1))`

**tf.reduce_sum** basically takes the sum of a given axis of the tensor we are supplying to it.

#### tf.train.GradientDescentOptimizer
As the optimization method, we were expecting to use Gradient Descent. I have used **tf.train.GradientDescentOptimizer** to implement Gradient Descent. You can see the following code;

- ` optimiser = tf.train.GradientDescentOptimizer(learning_rate=learning_rate).minimize(cross_entropy)`

I have initialized it with a learning_rate. And as can be seen above, optimizer is trying to minimize the **cross_entropy**.

#### tf.global_variables_initializer
tf.global_variables_initializer returns an operator that initializes global variables.
- `initialization_operator = tf.global_variables_initializer()`

#### tf.equal, tf.argmax and tf.cast
`correct_prediction = tf.equal(tf.argmax(output_data_placeholder, 1), tf.argmax(output_layer, 1))
    accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))`

**tf.equal** simply returns true if the given two parameters are equal, false otherwise.
**tf.argmax** returns the index of the maximum value in the given vector.
**tf.cast** is used to cast correct_prediction from boolean to TensorFlow float

#### tf.Session
A Session object encapsulates the environment in which Operation objects are executed, and Tensor objects are evaluated. With using **tf.Session()**, I have started the session.

With usage of **session.run**, I easily executed previously declared operators.

## Conclusion
I am getting an accuracy value between 35% and 68% and I keep getting different values for the accuracy every time I execute the program.
I am using;
- learning rate as 0.001,
- training epoch as 10,
- number of neurons as 100,
- number of hidden layers as 2
