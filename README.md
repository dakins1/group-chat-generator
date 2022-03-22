# group-chat-generator

This is a text generator to produce funny sentences that sound like something from you and your friends’ group chat. It implements an n-gram language models to generate the text, using GroupMe JSON message exports as training data. 

I started this project because:
  - I genuinely thought it would be funny to see what text it outputs, using my college groupme chats with my close friends as input.
  - I realized it would be a fun way to get experience with Scala's functional features. Since this is essentially a machine learning algorithm, it's a good fit for functional implementation. 

I initially wrote this with a ridiculous, un-readable amounts of foldLefts and other functional functions, because I just enjoy Scala's library that much. But later I realized how awful that code was (totally my fault, though, not Scala's), so I've been re-writing it to be readable.

### Feature Roadmap:
- Create separate GroupMe object that handles GroupMe data and implement the n-gram class to produce text output
- Make all i/o functional with monads and what not -- but there are lots of other functional fundamentals I want to learn before that
- Command line interface 
  - Allow users to pick a specific user or group chat to use as training data
    - Print out all available options, let user command line input what they want
    - User would just dump all their GroupMe data, and the program would parse available users and group chats
- Optimize gram to character mapping selection 
  - Current implementation is to store every instance -- including duplicates -- of every following char for a given gram, then randomly index from that storage when building text. This iterates over every element, however, since we index a list. Instead, gram mappings should point to a summary of characters and occurences to more quickly determine next char in generated text.
- Ensure everything is tail-recursive 
- Eventually implement with Scala's collections functions (foldLeft, reduce, map, etc), to make it more Scala-style. But going to build up fundamental recursive logic first before getting jiggy with it
