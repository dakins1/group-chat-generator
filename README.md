# group-chat-generator

This is a text generator to produce funny sentences that sound like something from you and your friends’ group chat. It implements an n-gram language models to generate the text, using GroupMe JSON message exports as training data. 

I started this project because:
  - I genuinely thought it would be funny to see what text it outputs, using my college groupme chats with my close friends as input.
  - I realized it would be a fun way to get experience with Scala's functional features. Since this is essentially a machine learning algorithm, it's a good fit for functional implementation. 

I initially wrote this with a ridiculous, un-readable amounts of foldLefts and other functional functions, because I just enjoy Scala's library that much. But later I realized how awful that code was (totally my fault, though, not Scala's), so I've been re-writing it to be readable.

#### 4/22/2022 Status:
Implemented the text generation with `LazyList`. This eliminated a couple dozen lines of code as it hugely simplifies messing with the `lengthLimit`. No more structual changes of code execution, just provide a `.take(lim)`, or don't. The markov chain algorithm lends itself to a *beautiful* use of `unfold`: the state is just the previous ngram to build off. If that gram mapping exists, continue to generate, else exit. Better yet, the `grams.get` returns an `Option`, which melts like butter into the unfold functional literal's `Option[A,S]` return value. `LazyList` also allows for more experimenting, e.g. can generate thousands of text samples and quickly filter down samples that only meet a predicate, like starting with a certain sequence.

After reading Ch. 5 of fp in scala, it wasn't obvious how I could integrate laziness into this project. But after experimenting just a tad, I am elated with the results. I feel like I stumbled upon the most elegant way to implement this NGram library. 

### Feature Roadmap:
- Make all i/o functional with monads and what not -- but there are lots of other functional fundamentals I want to learn before that
- Convert this from a batch operation to a streaming operation
  - Accept new instances of data to train from and update the Markov chain accordingly
  - Utilize Scala Akka to handle concurrency if two new data are received at once
  - Ideally hook up to a GroupMe chat and train as messages are sent in. But I don't think GroupMe supports that kind of functionality. Would have to think of some other use case for NGrams.
- Command line interface 
  - Allow users to pick a specific user or group chat to use as training data
    - Print out all available options, let user command line input what they want
    - User would just dump all their GroupMe data, and the program would parse available users and group chats
- Optimize gram to character mapping selection 
  - Current implementation is to store every instance -- including duplicates -- of every following char for a given gram, then randomly index from that storage when building text. This iterates over every element since we index a list. Instead, gram mappings should point to a summary of characters with occurences to more quickly determine next char in generated text.
- Look into reversing the order of parsing and data generation to use more prepends than appends
  - But I'm thinking any optimization might wash out since we typically work with both ends of the gram list
  - And parsing has to be in order, unless we constantly reverse each gram
- Convert random indexes to pure functions
- Improve picking out starting text
  - Not sure if it's possible, but maybe defining some sort of HOF that takes function as input for picking a starting text from training data
- Ensure everything is tail-recursive 
- Eventually implement with Scala's collections functions (foldLeft, reduce, map, etc), to make it more Scala-style. But going to build up fundamental recursive logic first before getting jiggy with it
  - Text generation with `LazyList.unfold` is a great start
- Possibly make case classes or even just type aliases for NGram object to make code more readable
- Define complexity after each algorithm modification --- so far it is O(n*m), where n = # of chars from training data and m =, somewhat confusingly, the n defined by *n*gram.
  - For parsing, we slide over every m-length sublist. Since we are using lists, we must traverse m for every new sublist to append the next element (removing first element is constant time). There are (n-2) m-length sublists for each training list. The 2 is dropped since it's a constant, thus n*m. We also re-traverse every m-length sublist for each update to the gram mapping, but again the 2m is constant. 
  - For generating, we also traverse an m-length sublist for each character generated. This is n1*m where n1 is the number of characters generated.
  - I could use a vector or something for constant time appends, but I like my classic recursive data structures.  
- ~~Break out the n-gram algorithm into its own generic class that accepts a Seq of anything; then, have a GroupMe object that handles GroupMe data and implement the n-gram class to produce text output~~