# group-chat-generator

This is a text generator to produce funny sentences that sound like something from you and your friends’ group chat. It implements an n-gram language model to generate the text, using GroupMe JSON message exports as training data. 

More specifically, I am primarily building an n-gram/markov chain Scala library, then using that library for this group chat generator. 

I started this project because:
  - I genuinely thought it would be funny to see what text it outputs, using my college groupme chats with my close friends as input.
  - I realized it would be a fun way to get experience with Scala's functional features. The nature of Markov models lends itself well to Scala and functional programming.

### Roadmap:

#### Design
- De-couple the building of Markov Model from the execution of using the model to generate data
  - Build a MarkovGeneration class or something that takes in a MarkovModel and uses that to generate data
- Make a MarkovModel trait, and instantiate a few different types of Markov models, e.g. Hidden Markov model
- Make a MarkovFactory of some sort that handles the implementation of how to source data
  - For reading from a big file, implement something that reads the entire file but only ever has a small piece of the file in memory

#### NGram implementation
- Convert this from a batch operation to a streaming operation
  - Accept new instances of data to train from and update the Markov chain accordingly
  - Perhaps utilize concurrency if two new data are received at once
  - Ideally hook up to a GroupMe chat and train as messages are sent in. But I don't think GroupMe supports that kind of functionality. Would have to think of some other use case for NGrams.
- Optimize transition implementation
  - Current implementation is to store every instance -- including duplicates -- of every following char for a given gram, then randomly index from that storage when building text. This iterates over every element since we index a list. Instead, gram mappings should point to a summary of characters with occurences to more quickly determine next char in generated text.
- Look into reversing the order of parsing and data generation to use more prepends than appends
  - But I'm thinking any optimization might wash out since we typically work with both ends of the gram list
  - And parsing has to be in order, unless we constantly reverse each gram
- Improve picking out starting text
  - Not sure if it's possible, but maybe defining some sort of HOF that takes function as input for picking a starting text from training data
- Possibly make case classes or even just type aliases for NGram object to make code more readable
- Define complexity after each algorithm modification --- so far it is *O(nm)*, where *n* = # of chars from training data and *m* =, somewhat confusingly, the *n* defined by *n*gram.
  - For parsing, we slide over every m-length sublist. Since we are using lists, we must traverse m for every new sublist to append the next element (removing first element is constant time). There are (n-2) m-length sublists for each training list. The 2 is dropped since it's a constant, thus n*m. We also re-traverse every m-length sublist for each update to the gram mapping, but again the 2m is constant. 
  - For generating, we also traverse an m-length sublist for each character generated. This is n1*m where n1 is the number of characters generated.
  - I could use a vector or something for constant time appends, but I like my classic recursive data structures. 
- ~~Ensure everything is tail-recursive~~
- ~~Eventually implement with Scala's collections functions (foldLeft, reduce, map, etc), to make it more Scala-style. But going to build up fundamental recursive logic first before getting jiggy with it~~
  - ~~Text generation with `LazyList.unfold` is a great start~~
- ~~Convert random indexes to pure functions~~ 
- ~~Break out the n-gram algorithm into its own generic class that accepts a Seq of anything; then, have a GroupMe object that handles GroupMe data and implement the n-gram class to produce text output~~

#### Features
- Add more NGram functionality, like combining two NGram instances into one model
- Command line interface 
  - Allow users to pick a specific user or group chat to use as training data
    - Print out all available options, let user command line input what they want
    - User would just dump all their GroupMe data, and the program would parse available users and group chats

#### Groupme Implementation
- Make all i/o functional with monads and what not -- but there are lots of other functional fundamentals I want to learn before that
- Make separate Ngram using the words as states, not characters

#### Other
- Really need to get some testing up in here
