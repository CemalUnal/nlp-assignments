# BBM497 Introduction to Natural Language Processing Assignment-4

_**Subject:** Sentiment Analysis With Deep Learning_

_**Name and Surname:** Cemal ÜNAL_  
_**Number:** 21328538_

- - - -

## Introduction

In this assignment I have used Python programming language (Python 3) along with TensorFlow (tensorflow 1.8.0) library.

## TensorFlow Functions and Utilities That I Used in My Assignment

#### tf.placeholder
In the TensorFlow documentation it says that `Placeholder inserts a placeholder for a tensor that will be always fed.` In simple words, a placeholder is simply a variable that we will assign data to at a later date. In my code, I did not know what the value of the array `x` and `y` would be during the declaration phase. So I defined them as placeholders.

For example `training_data_placeholder = tf.placeholder(tf.float32, [None, 200])` and `output_data_placeholder = tf.placeholder(tf.float32, [None, 2])`. Since I am not making any initialization in the declaration, TensorFlow needs to know what data type each element within the tensor is going to be. I am providing it by using `tf.float32`. `[None, 200]` is used to specify the shape of the data that will injected to this variable. I have used **None** since the size of the train and test matrices are different. And TensorFlow will decide it later on while execution.

#### tf.Variable

#### tf.random_normal

#### tf.add

#### tf.nn.relu

#### tf.nn.softmax

#### tf.clip_by_value

#### tf.reduce_mean

#### tf.reduce_sum

#### tf.log

#### tf.train.GradientDescentOptimizer

#### tf.global_variables_initializer

#### tf.equal

#### tf.argmax

#### tf.cast

#### tf.Session
session.run'i  filan burada anlat.
