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
      * Given a string and integer n, slide over all n-length substrings and store the trailing character
      * 
      * @param n length of ngram 
      * @param text input string
      * @param chain An existing markov chain to add on to; by default assumes empty map
      * @return A mapping between n-length substrings and each instance of a following character from this string
      */
    private def parseNgram(n:Int, text:List[A], chain:Map[List[A], List[A]] = Map.empty[List[A], List[A]] ):Map[List[A],List[A]] = {

        def helper(subtext:List[A], nAs:List[A], nAsLength:Int, chain:Map[List[A], List[A]]):Map[List[A],List[A]] = (subtext -> nAs) match {
            case (Nil, _) => chain 
            case (s::ss, nAs) if (nAsLength < n) => helper(ss, nAs:+s, nAsLength+1, chain)
            case (s::ss, nAs) =>     
                val followers = chain.getOrElse(nAs, Nil)
                val newChain = chain + (nAs -> (s::followers))
                helper(ss, nAs.tail:+s, nAsLength, newChain) 
        }
        helper(text.toList, List(), 0, chain)  
    }
    
    /**
      * Parses an ngram from multiple sources of text
      *
      * @param n length of ngram
      * @param texts list of input strings
      * @return A mapping between n-length substrings and each instance of a following character from all strings in texts
      */
    private def parseNgrams(n:Int, texts:List[List[A]]):Map[List[A], List[A]] = {
        texts.foldLeft(Map.empty[List[A], List[A]]) { 
            (chain, text) => parseNgram(n, text, chain)
        }
    }

    /**
      * Given a starting string that is within the possible ngram mappings, generate a text up to specified length
      * using the ngram probabilities
      *
      * @param targetLength max length of generated string
      * @param starter starting input, must be within possible ngram mappings
      * @param gram pre-generated ngram mappings
      * @return
      */
    private def getTextFromStarter(lengthLimit:Int, starter:List[A], gram:Map[List[A],List[A]]):List[A] = {

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
    private def generateChat(lengthLimit:Int, gram:Map[List[A],List[A]]):List[A] = {
        val s = getStarterTextFromGram(gram)
        getTextFromStarter(lengthLimit, s, gram)
    }

    // Defaulting to max int value is lame, but easy temporary solution while I get class interface working
        // Need to re-write getTextFromStarter to handle infinite strings (and make tailrec too if we're going that deep)
    def generateData():List[A] = lengthLimit match {
        case None => generateChat(Int.MaxValue, grams)
        case Some(lim) => generateChat(lim, grams)
    }




}

object NGram {

    def apply[A](trainingData:List[List[A]], order:Int) = new NGram(trainingData, order, None)

    def apply[A](trainingData:List[List[A]], order:Int, lengthLimit:Int) = new NGram(trainingData, order, Some(lengthLimit))


}