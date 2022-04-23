package ngram

import annotation.tailrec

/**
  * NGram class to build an ngram model off training data
  *
  * @param trainingData List of examples to learn from
  * @param order Length of sub-list to train over
  */
class NGram[A](val trainingData:List[List[A]], val order:Int, val lengthLimit:Option[Int]) {
    // Could have made trainingData just a List[A], but if we want to train over multiple sets of data, 
        // e.g. multiple books of an author, texts from someone, or seasons of data, then we need
        // List[List[A]] to provide us the stopping points/breaks in the data

    val grams = parseNgrams(order, trainingData)

    /**
      * Given a list of data and integer n, slide over all n-length sublists and store the trailing element
      * 
      * @param n length of ngram 
      * @param data input data
      * @param chain An existing markov chain to add on to; by default assumes empty map
      * @return A mapping between n-length sublists and each instance of a following element from data
      */
    private def parseNgram(n:Int, data:List[A], chain:Map[List[A], List[A]] = Map.empty[List[A], List[A]] ):Map[List[A],List[A]] = {

        def helper(sublist:List[A], nAs:List[A], nAsLength:Int, chain:Map[List[A], List[A]]):Map[List[A],List[A]] = sublist match {
            case Nil => chain 
            case s::ss if (nAsLength < n) => helper(ss, nAs:+s, nAsLength+1, chain)
            case s::ss =>     
                val followers = chain.getOrElse(nAs, Nil)
                val newChain = chain + (nAs -> (s::followers))
                helper(ss, nAs.tail:+s, nAsLength, newChain) 
        }
        helper(data, List(), 0, chain)  
    }
    
    /**
      * Parses an ngram from multiple sources of data
      *
      * @param n length of ngram
      * @param data list of input data
      * @return A mapping between n-length sublists and each instance of a following element from all lists in data
      */
    private def parseNgrams(n:Int, data:List[List[A]]):Map[List[A], List[A]] = {
        data.foldLeft(Map.empty[List[A], List[A]]) { 
            (chain, text) => parseNgram(n, text, chain)
        }
    }
    
    /**
     * Given a starting List[A] that is within the possible ngram mappings, generate data using the ngram probabilities 
     * until the chain reaches a terminal 
     *
     * @param starter starting input, must be within possible ngram mappings
     * @param gram pre-generated ngram mappings
     * @return
    */
    def getDataFromStarter(starter:List[A]):LazyList[A] = {
        starter ++: LazyList.unfold(starter){case (prevGram) => {
            grams.get(prevGram).map(possibles => {
                    // Need to make this Random a pure function
                    val r = scala.util.Random.nextInt(possibles.length) // inefficient, probability summary would fix
                    val newA = possibles(r) 
                    newA -> (prevGram.tail:+newA)
            })
        }}
    }

    /**
      * Given a gram mapping, randomly pick out a key that reasonably looks like 
      * the beginning of a sentence/statement
      *
      * @param gram ngram mapping
      */
    def getStarterTextFromGrams:List[A] = {
        // Don't want to begin sentence in the middle of a word, so only pick an ngram that starts
            //with a space, that way we know sentence starts with a whole word
        //There are probably more clever ways to do this, but doing this easy option for now
            //rly hate this though because this automatically filters out beginning of messages, which are 
            //the most realistic sentence starters
        // TODO: keeping this as '...Text...' function since it still can only work with text. 
            // Need to devise way for starters with generic type
        // val starters = gram.map(_._1).filter(m => m.head == ' ').toList
        val starters = grams.map(_._1).toList
        starters(scala.util.Random.nextInt(starters.length))
    }

    /**
      * Lazily generate text from a random starting list until markov chain reaches a terminal
      *
      * @return a lazy list of data to be generated
      */
    def generateLazyData:LazyList[A] = {
        getDataFromStarter(getStarterTextFromGrams)
    }
    
    /**
      * Generate data from random starting list until markov chain reaches a terminal.
      * Adheres to this NGram object's length limit if specified
      * 
      * @return a list of the generated data
      */
    def generateData = {
        val data = generateLazyData
        lengthLimit
            .map(lim => data.take(lim).toList)
            .getOrElse(data.toList)
    }

    /**
      * Generate data from random starting list up to specified length or until markov chain
      * reaches a terminal
      *
      * @param lengthLimit
      * @return a list of the generated data
      */
    def generateData(limit:Int) =
        generateLazyData.take(limit).toList

}

object NGram {

    /**
      * Be very careful with not providing a length limit. It can hijack your JVM. Ensure your data will
      * produce a majority of terminals in the Markov chain.
      *
      * @param trainingData
      * @param order
      * @return
      */
    def apply[A](trainingData:List[List[A]], order:Int) = new NGram(trainingData, order, None)

    def apply[A](trainingData:List[List[A]], order:Int, lengthLimit:Int) = new NGram(trainingData, order, Some(lengthLimit))


}