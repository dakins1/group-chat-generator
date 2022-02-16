import scala.io.StdIn
import net.liftweb.json.DefaultFormats
import net.liftweb.json._
import scala.io.Source
import java.io.File
import scala.io.Codec

case class Message(
    name:String,
    text:String,
    user_id:String
)

case class Group(
    name:String,
    id:String
)

/*
json message structure
export parent folder -> randomly numbered subfolder -> message.json
array
    groupid
    id
    sender_id
    basically just an array of messages, and straightforward attribute names
.json files have all json on one line, no \n formatting for the json itself
*/


object Main extends App {

    implicit val formats = DefaultFormats

    def getGF(folderNumber:String):Unit= {
        val filePath = "../groupMeExport/" + folderNumber + "/conversation.json"
        val messages = Source.fromFile(filePath)(Codec("utf-8")).getLines().toArray.apply(0)
        //not sure what this line does, keeping it for now but should delete later        
        // val json = parse(messages).children.map(_.extract[Group])//.filter(_.name != null).filter(_.name == "Gruesome Fivesome").head.id
        if (messages.contains("Gruesome Fivesome")) println(messages)
    }

    //given a subfolder number, extract all messages from that subfolder
    def getMessages(folderNumber:String):Seq[Message] = {
        val filePath = "../groupMeExport/" + folderNumber + "/message.json"
        //entirety of json object is one big array of messages, so call .children to get all messages
        val messageArray = Source.fromFile(filePath)(Codec("utf-8")).mkString
        val json = parse(messageArray).children 
        json.map(_.extract[Message]).filter(_.text != null)
    }

    //From alvin alexander
    def getListOfSubDirectories(dir: File): List[String] = dir.listFiles
        .filter(_.isDirectory)
        .map(_.getName)
        .toList

    /**
      * Given a string and integer n, slide over all n-length substrings and store the trailing character
      * 
      * @param n length of ngram 
      * @param text input string
      * @param chain An existing markov chain to add on to; by default assumes empty map
      * @return A mapping between n-length substrings and each instance of a following character from this string
      */
    def parseNgram(n:Int, text:String, chain:Map[List[Char], List[Char]] = Map.empty[List[Char], List[Char]] ):Map[List[Char],List[Char]] = {

        def helper(subtext:List[Char], nChars:List[Char], nCharsLength:Int, chain:Map[List[Char], List[Char]]):Map[List[Char],List[Char]] = (subtext -> nChars) match {
            case (Nil, _) => chain 
            case (s::ss, nChars) if (nCharsLength < n) => helper(ss, nChars:+s, nCharsLength+1, chain)
            case (s::ss, nChars) =>     
                val followers = chain.getOrElse(nChars, Nil)
                val newChain = chain + (nChars -> (s::followers))
                helper(ss, nChars.tail:+s, nCharsLength, newChain) 
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
    def parseNgrams(n:Int, texts:List[String]):Map[List[Char], List[Char]] = {
        texts.foldLeft(Map.empty[List[Char], List[Char]]) { 
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
    def getTextFromStarter(lengthLimit:Int, starter:List[Char], gram:Map[List[Char],List[Char]]):List[Char] = {

        def helper(limit:Int, prevGram:List[Char], gram:Map[List[Char],List[Char]]):List[Char] = gram.get(prevGram) match {
            case _ if limit == 0 => Nil
            case None => Nil
            case Some(value) if limit > 0 =>
                    val r = scala.util.Random.nextInt(gram(prevGram).length) //inefficient, maybe probability summary would fix
                    val newChar = gram(prevGram)(r) 
                    newChar::helper(limit-1, prevGram.tail:+newChar, gram)
        }
        starter ++ helper(lengthLimit-starter.length, starter, gram)
    }

    /**
      * Given a gram mapping, randomly pick out a key that reasonably looks like 
      * the beginning of a sentence/statement
      *
      * @param gram ngram mapping
      */
    def getStarterTextFromGram(gram:Map[List[Char], List[Char]]):List[Char] = {
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
    def generateChat(lengthLimit:Int, gram:Map[List[Char],List[Char]]):String = {
        val s = getStarterTextFromGram(gram)
        getTextFromStarter(lengthLimit, s, gram).mkString
    }
    
    val leon = "54066176"
    val gruesomeFivesome = "30334096"

    //groupMe exports all the messages across dozens of randomly numbered folders, this puts all
    //folder names into a list
    val folderNums = getListOfSubDirectories(new File("../groupMeExport"))
    // val messages = folderNums.map(n => getMessages(n)).flatMap(_.filter(m => m.user_id == leon))
    val messages = getMessages(gruesomeFivesome)
    
    val order = 10
    val charLimit = 500
    val grams = parseNgrams(order, messages.map(_.text.toLowerCase()).toList)
   
    for (_ <- 1 to 10) {
        println(generateChat(charLimit, grams))
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