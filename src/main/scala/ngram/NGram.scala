package ngram

import annotation.tailrec
import scala.util.Random

/**
  * An n-order Markov chain modeling the provided training data
  *
  * @param trainingData List of training instances to learn from
  * @param order Number of states on which the prediction of the next state depends
  * {{{
  *     val chars = NGram(List("Hey there", "Hello there"), 10) 
  *     val words = NGram(List(List("Hey", "there"), List("Hello", "there")), 1)
  * }}}
  */
class NGram[State](val trainingData:List[List[State]], val order:Int)(implicit rng: Random) {

    val transitions = trainingData.foldLeft(Transitions.empty)((trans, data) => getTransitions(data, trans))

    private def getTransitions(sequence:List[State], existingTransitions:Transitions):Transitions = {

        @tailrec
        def slider(
            remainingStates:List[State], 
            prevNStates:List[State], 
            numStatesTraversed:Int, 
            existing:Transitions
        ):Transitions = 
            remainingStates match {
                case Nil => existing
                case s::ss if (numStatesTraversed < order) => slider(ss, prevNStates:+s, numStatesTraversed+1, existing)
                case s::ss =>     
                    val newTransitions = existing.add(prevNStates -> s)
                    slider(ss, prevNStates.tail:+s, numStatesTraversed, newTransitions) 
            }
        
        slider(sequence, List(), 0, existingTransitions)  
    }
    
    
    /**
     *  Generate data until termination, beginning with the starter input
     * this list totally doesnt' have to be length of N, just has to be at least n, and
     * build off of the last n states
     *
     * @param starter an n-length sequence of States that ends with some subsequence of the training data
    */
    def getDataFromStarter(starter:List[State]):LazyList[State] = {
        val lastN = starter.takeRight(order)
        starter ++: LazyList.unfold(lastN)(prevNStates => {
            transitions
            .availableForOption(prevNStates)
            .map(possibles => {
                val r = rng.nextInt(possibles.length) // inefficient, probability summary would fix
                val nextState = possibles(r) 
                nextState -> (prevNStates.tail:+nextState)
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
        val starters = transitions.map.map(_._1).toList
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


    // Make a MarkovModel class, abstract to an extent
    // then an NGram class that uses the MarkovModel?? Or extends it maybe idk
    // or at least an NGram trainer class, than creates a markov model
    // then a separate execution context for a model
    class Transitions(val map: Map[List[State], List[State]]) {

        def availableFor(nStates: List[State]): List[State] = map.getOrElse(nStates, Nil)
        def availableForOption(nStates: List[State]): Option[List[State]] = map.get(nStates)
       
        def add(transition: (List[State],State)): Transitions = {
            val (nStates, state) = transition
            val ps = this.availableFor(nStates)
            val newTrans = map + (nStates -> (state::ps))
            Transitions(newTrans)
        }

    }

    object Transitions {
        def apply(map: Map[List[State], List[State]]) = new Transitions(map)
        def empty = Transitions(Map.empty[List[State], List[State]])
    }
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