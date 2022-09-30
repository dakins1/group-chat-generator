package ngram

import annotation.tailrec
import scala.util.Random

/**
  * An n-order Markov chain modeling the provided training data
  *
  * @param trainingData List of training instances to learn from
  * @param order Number of states on which the prediction of the next state depends
  * {{{
  * val chars = NGram(List("Hey there", "Hello there"), 10) 
  * val words = NGram(List(List("Hey", "there"), List("Hello", "there")), 1)
  * }}}
  */
class NGram[State](val trainingData:List[List[State]], val order:Int)(implicit rng: Random) {

    private val emptyTransitions = Map.empty[List[State], List[State]]
    val transitions = trainingData.foldLeft(emptyTransitions)((trans, data) => getTransitions(data, trans))

    // TODO: consider de-coupling the logic of deriving transitions from combining transitions
    private def getTransitions(sequence:List[State], existingTransitions:Transitions):Transitions = {

        @tailrec
        def slider(
            remainingStates:List[State], 
            prevNStates:List[State], 
            numStatesTraversed:Int, 
            existing:Transitions
        ):Transitions = remainingStates match {
            case Nil => existing
            case s::ss if (numStatesTraversed < order) => slider(ss, prevNStates:+s, numStatesTraversed+1, existing)
            case s::ss =>     
                val followers = existing.getOrElse(prevNStates, Nil)
                val newTransitions = existing + (prevNStates -> (s::followers))
                slider(ss, prevNStates.tail:+s, numStatesTraversed, newTransitions) 
        }
        slider(sequence, List(), 0, existingTransitions)  
    }
    
    
    /**
     *  Generate data until termination, beginning with the starter input
     *
     * @param starter an n-length sequence of States that is some subsequence of the training data
    */
    def getDataFromStarter(starter:List[State]):LazyList[State] = {
        starter ++: LazyList.unfold(starter)(prevGram => {
            transitions
            .get(prevGram)
            .map(possibles => {
                val r = rng.nextInt(possibles.length) // inefficient, probability summary would fix
                val newA = possibles(r) 
                newA -> (prevGram.tail:+newA)
            })
        })
    }

    /**
      * Randomly pick out a key that reasonably looks like 
      * the beginning of a sentence/statement
      *
      * @param gram ngram mapping
      */
    def generateStartingSubsequence:List[State] = {
        // Don't want to begin sentence in the middle of a word, so only pick an ngram that starts
            //with a space, that way we know sentence starts with a whole word
        //There are probably more clever ways to do this, but doing this easy option for now
            //rly hate this though because this automatically filters out beginning of messages, which are 
            //the most realistic sentence starters
        // TODO: keeping this as '...Text...' function since it still can only work with text. 
            // Need to devise way for starters with generic type
        // val starters = gram.map(_._1).filter(m => m.head == ' ').toList
        val starters = transitions.map(_._1).toList
        starters(rng.nextInt(starters.length))
    }

    /**
      * Lazily generate data from a random starting subsequence until termination
      *
      * @return a lazy list of data to be generated
      */
    def generateLazyData: LazyList[State] = {
        getDataFromStarter(generateStartingSubsequence)
    }

    /**
      * Generate data from random starting list up to specified length or until markov chain terminates
      */
    def generateData(limit:Int): List[State] =
        generateLazyData.take(limit).toList


    type Transitions = Map[List[State], List[State]]
}

object NGram {
    /**
     * An n-order Markov chain modeling the provided training data
     *
     * @param trainingData List of examples to learn from, e.g. List("Hey there", "Hello there")
     * @param order Number of states on which the prediction of the next state depends
     */
    def apply[State](trainingData:List[List[State]], order:Int)(implicit rng: Random) = new NGram(trainingData, order)
}