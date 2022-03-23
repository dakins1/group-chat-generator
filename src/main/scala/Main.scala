import scala.io.StdIn


import ngram._

object Main extends App {

    //my directory for training data
    val filePath = "../groupMeExport"
    val gmExtract = GroupMeExtractor(filePath)

    val order = 12
    val charLimit = 500
    val ngram = NGram(gmExtract.trainingData, order)
    
    for (_ <- 1 to 10) {
        println(ngram.generateData().mkString)
    }

}

/*
TODO 
going to be too difficult to functionalize file i/o and user input at first
start with functionalizing ngram algies
    - one for humans to read
    - one with HOF
define complexity --- so far it is O(n*m), where n = # of chars from training data and m = somewhat confusingly, the n defined by ngram

make probabilities not use non-distinct Seq[Char] and random index for probabilities - would be a no-no in haskell ;)
    There probably exists some clever algorithm to summarize probabilities and outputs 

also could probs make some more objects for all this, although might be overkill for existing functionality - needs to be driven by advanced features
another idea is to abstract away the fact this algorithm is for groupme messages; allow it to take a Seq of any type instead. Then, make a GroupMe class that
    configures groupme messages to fit this algorithm
Also, with ngram object, could create multiple mapping instances to quickly compare different orders
    Make NGram its own type?? That way revising types is easier. 
    Could start with case class, then work way up to type-paramterized object

and of course, somehow parameterizing the groupme input
    - list out all usernames / group chats, have command line prompt to select who or which chat to emulate

Make an infinite text generator, keeps going till it hits Nil, rather than hitting char limit. Super low on priority 
    list tho, since text almost always self terminates before reaching limit
*/