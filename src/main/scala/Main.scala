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

    val leon = "54066176"
    val gruesomeFivesome = "30334096"

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

    //groupMe exports all the messages across dozens of randomly numbered folders, this puts all
    //folder names into a list
    val folderNums = getListOfSubDirectories(new File("../groupMeExport"))
    // val messages = folderNums.map(n => getMessages(n)).flatMap(_.filter(m => m.user_id == leon))
    val messages = getMessages(gruesomeFivesome)

    /**
      * Given a string and integer n, slide over all n-length substrings and store the trailing character
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

    //Can't quite remember what was going on here. I think I was working generateChat with an interal "get starter text" function, formerly known as "helper"
        //and then decided to start breaking things up. 

        //get text from starter?? What text am I getting
        //oh i see it's quite literal. and Generate Chat is the main method connecting everything
        //in this case, lastGram can also be starter text. probs speicify in scala docs
        //def needs a better name than this
    def getTextFromStarter(targetLength:Int, lastGram:List[Char], gram:Map[List[Char],List[Char]]):List[Char] = gram.get(lastGram) match {
            case _ if targetLength == 0 => Nil
            case None => Nil
            case Some(value) if targetLength > 0 =>
                    val r = scala.util.Random.nextInt(gram(lastGram).length) //inefficient, maybe probability summary would fix
                    val newChar = gram(lastGram)(r) 
                    newChar::getTextFromStarter(targetLength-1, lastGram.tail:+newChar, gram)
    }

    def generateChat(n:Int, length:Int, gram:Map[List[Char],List[Char]]):String = {
        
        
        val starters = gram.map(_._1).toList //actually isn't ideal cuz it starts out a message very stupidly 
        val r = scala.util.Random.nextInt(starters.length)
        getTextFromStarter(length, starters(r), gram).mkString
    }

    def ngram(n:Int, texts:Seq[String]):Map[String, Seq[Char]] = {
        //A plain recursive function would have been much cleaner code, but sometimes I can't resist scala's HOFs
        //Technically isn't functional since I use a mutable Set :(, but theoretically could be passed around the 
            //fold and could be functional
        var seen = Set[String]().empty
        //this iterates over every message, one message at a time
        //eventually returns a Map[String,Seq[Char]], the markov chain
        texts.foldLeft(Map[String,Seq[Char]]().empty){
            case (oldMap, message) =>
                //unclear why there has to be a max length
                val maxLength = message.length - n - 1 //-1 so there is a following char to add to the gram
                //message.foldLeft returns a tuple, but we throw away the count val and keep the Map[String,Seq[Char]] generated
                val (map, _) = message.foldLeft((oldMap, 0)){
                    case ((old, count), _) =>
                                                     //now why is this count+1?? I think count+1 gets thrown away anyway
                        if (count > maxLength) (old, count+1) //less than n chars left
                        else {
                            //so we move along down the message, and take substrings each step of the way
                            //boooo!! bad!!! Totally could just chomp 1 char at a time, while reading ahead n chars
                            //but then we're doing n operations for every character, if n is the order of gram...still would just be constant while either
                                //length of output or length of message data determines complexity
                            //could probably come up with clever way to functionally slide the window of chars along
                            val str = message.subSequence(count, count+n).toString()
                            //if gram already seen, add the newfound following char to the map
                            //probs could do without if/else 
                            //lol, looking back on this, this is such bad code. Literally just have to check if the map already contains the elem
                            if (seen(str)) (old + (str -> (old(str):+message.charAt(count+n))), count+1)
                            else { 
                                seen+=str
                                (old + (str -> Seq(message.charAt(count+n))), count+1)
                            }
                        }
                }
                map
        }
    }

    def make(n:Int, limit:Int, gram:Map[String, Seq[Char]], possibs:Set[String], str:String):String = {
        if (str.length() == limit) str
        else {
            val k = str.substring(str.length()-n) //i wonder if in haskell this would be Shlemiel
            //possibs kind of redundant, could just check if gramp(k) returns a result or not
            if (possibs(k)) {
                val s = gram(k)
                //this is appending a random character from the sequence of possible trailing characters for this instance of n-gram
                //probability comes from the fact that repeat chars are in the map, then random one is drawn
                //could easily do some sort of probability store - with values updated each time a char is appended
                    //however for simplicity we will keep the non-distinct Seq[Char](random.Int), maybe update in future
                //also important to note that it picks one char at a time, rather than appending n chars
                make(n, limit, gram, possibs, str+s(scala.util.Random.nextInt(s.length)))
            } else str
            
        }
    }
    val order = 10
    val charLimit = 200
    val grams = parseNgrams(order, messages.map(_.text.toLowerCase()).toList)
    val x = grams.filter(s => s._2.distinct.size != s._2.size)
    val r = scala.util.Random
    //Filter out words less than 10 characters long. I guess this is just to start things off
    //can remove this with the length < n conditional in helper
   
    for (_ <- 1 to 10) {
        // val starter = starters(r.nextInt(starters.length))
        // println(make(order, 200, grams, grams.map(_._1).toSet, starter))
        // println(parseNgram(3, "123456789"))
        // println(parseNgrams(3, List("12345", "54321", "12346")))
        println(generateChat(order, charLimit, grams))
        // println(getTextFromStarter(charLimit, grams.map(_._1).toList(4), grams).toString)
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

break down the messages stuff, or at least reduce use of messages.map nonsense
figure out what the randome substring stuff is, and find a better way of explaining/coding that
document the json format, maybe either through comments, case classes, or something idk. Nvm already have case classes,
maybe figure something better out. Or just better explain them

Add comments throughout the code, especially on the parts with argument-dense function calls
    comments for any functional heavy stuff

and of course, somehow parameterizing the groupme input
    - list out all usernames / group chats, have command line prompt to select who or which chat to emulate

Make NGram its own type?? That way revising types is easier. Could start with case class, then work way up to type-paramterized object
*/