package ngram

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
      * Given a starting List[A] that is within the possible ngram mappings, generate data up to specified length
      * using the ngram probabilities
      *
      * @param targetLength max length of generated data
      * @param starter starting input, must be within possible ngram mappings
      * @param gram pre-generated ngram mappings
      * @return
      */
    private def getDataFromStarter(lengthLimit:Int, starter:List[A], gram:Map[List[A],List[A]]):List[A] = {
        //use limit to count down rather than up
        def helper(limit:Int, prevGram:List[A], gram:Map[List[A],List[A]]):List[A] = gram.get(prevGram) match {
            case _ if limit == 0 => Nil
            case None => Nil
            case Some(value) if limit > 0 =>
                    //Also need functional way to handle Random generation
                    val r = scala.util.Random.nextInt(gram(prevGram).length) //inefficient, maybe probability summary would fix
                    val newA = gram(prevGram)(r) 
                    newA::helper(limit-1, prevGram.tail:+newA, gram)
        }
        starter ++ helper(lengthLimit-starter.length, starter, gram)
    }

    /**
      * Given a gram mapping, randomly pick out a key that reasonably looks like 
      * the beginning of a sentence/statement
      *
      * @param gram ngram mapping
      */
    private def getStarterTextFromGram(gram:Map[List[A], List[A]]):List[A] = {
        // Don't want to begin sentence in the middle of a word, so only pick an ngram that starts
            //with a space, that way we know sentence starts with a whole word
        //There are probably more clever ways to do this, but doing this easy option for now
            //rly hate this though because this automatically filters out beginning of messages, which are 
            //the most realistic sentence starters
        // TODO: keeping this as '...Text...' function since it still can only work with text. 
            // Need to devise way for starters with generic type
        val starters = gram.map(_._1).filter(m => m.head == ' ').toList
        starters(scala.util.Random.nextInt(starters.length))
    }

    /**
      * Given a length limit and ngram mappings, randomly generate text
      *
      * @param targetLength
      * @param gram
      * @return
      */
    private def generateData(lengthLimit:Int, gram:Map[List[A],List[A]]):List[A] = {
        val s = getStarterTextFromGram(gram)
        getDataFromStarter(lengthLimit, s, gram)
    }

    // Defaulting to max int value is lame, but easy temporary solution while I get class interface working
        // Need to re-write getTextFromStarter to handle infinite strings (and make tailrec too if we're going that deep)
    def generateData():List[A] = lengthLimit match {
        case None => generateData(Int.MaxValue, grams)
        case Some(lim) => generateData(lim, grams)
    }




}

object NGram {

    def apply[A](trainingData:List[List[A]], order:Int) = new NGram(trainingData, order, None)

    def apply[A](trainingData:List[List[A]], order:Int, lengthLimit:Int) = new NGram(trainingData, order, Some(lengthLimit))


}