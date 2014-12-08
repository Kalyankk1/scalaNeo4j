package Saddahaq;
import sys.ShutdownHookThread
import org.neo4j.scala.{TypedTraverser, SingletonEmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}
import org.neo4j.scala.Neo4jIndexProvider
import collection.JavaConverters._
import org.neo4j.graphdb.Direction
import scala.util.parsing.json.JSONArray
import org.apache.thrift.server.TServer
import org.apache.thrift.server.TThreadPoolServer
import org.apache.thrift.server.TThreadPoolServer.Args
import org.apache.thrift.transport.TServerSocket
import org.apache.thrift.transport.TTransportException
import org.neo4j.index.lucene.ValueContext
import scala.util.parsing.json.JSONObject
import java.util.Timer
import java.util.TimerTask
import edu.stanford.nlp.tagger.maxent.MaxentTagger
import scala.util.parsing.json.JSON
import java.io.BufferedReader
import java.io.FileReader
import scala.util.control.Breaks
import java.util.Calendar
import java.util.Date
import scala.collection.immutable.ListMap
import twitter4j._
import scala.io.Source


case class location(val location_id: String,
    val cities: String,
    val tiles: String)
    
case class tweet(val id: String,
    val pos: Int,
    val neg: Int,
    val neu: Int,
    val tweet_text: String,
    val time_analysis: String)
    
case class pos(val pos_id: String,
    val last_update: Int)

case class user(val first_name: String,
    val last_name: String,
    val user_name: String,
    val email: String,
    val location: String,
    val previlege: Int,
    val time_created: Int,
    val weight: Int,
    val spam_weight: Int,
    val nouns: String,
    val feed_subscription: String,
    val notify_time: Int,
    val last_seen: Int,
    val fav_hash: String)
    
case class user_tiles(val id: String,
    val news: String,
    val news_Personalized: String
    )
    
case class avg_wt(val id: String,
    val average: Int,
    val total: Int,
    val top: Int,
    val top_10: Int,
    val top_20: Int,
    val top_30: Int,
    val top_30_users: String,
    val threshold_weight: Int)

object User_node extends Neo4jWrapper with Article_node with Event_node with Quickpost_node with Comment_node with SingletonEmbeddedGraphDatabaseServiceProvider with TypedTraverser with Neo4jIndexProvider with User_nodeService.Iface{

//with User_nodeService.Iface
    
    
     
ShutdownHookThread {
    shutdown(ds)
    }

override def NodeIndexConfig =   ("user", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
								 ("firstname", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
								 ("email", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("user_weight", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("article", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("user_tiles", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("featured_tiles", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("headlines", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("article_content", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("article_title", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("article_weight", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("event", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("event_content", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("event_weight", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("event_title", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("poll", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("space", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("cfpost", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("feed", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("quickpost", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("quickpost_content", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("petition", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("petition_content", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("debate", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("debate_suggestion", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("townhall_suggestion", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("debate_content", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("townhall", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("townhall_content", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("quickpost_weight", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("petition_weight", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("debate_weight", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("trending", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("comment", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("comment_weight", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("category", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("sub_category", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("topic", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("hash_weight", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("search_word", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("avg_weights", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("location", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("pos", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("tweet", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("tiles", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::Nil
  override def RelationIndexConfig = ("comment_vote", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("comment_voteup", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("comment_votedown", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("quickpost_voteup", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("article_markfav", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("article_readlater", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("event_readlater", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("petition_readlater", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("debate_readlater", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("townhall_readlater", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("event_response", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::
                                 ("article_voteup", Some(Map("provider" -> "lucene", "type" -> "fulltext")))::Nil
  
                              
  //SentimentClassifier sentClassifier;
  def neo4jStoreDir = "/var/n4j/data/graph.db"
//  override def configParams = Map("keep_logical_logs" -> "false")
  //println(configParams("neostore.propertystore.db.strings.mapped_memory"))

//  var map = scala.collection.mutable.Map[String,String]()
//  map("hey") = "hey"
//  override def configParams = map.toMap
//def neo4jStoreDir = "/home/yashwanth/Applications/neo4j-community-1.8.2/data/graph.db"

 
  //Creating new user
  def create_user(first_name: String,
    last_name: String,
    user_name: String,  
    email: String,
    location: String,
    previlege: Int,  
    time_created: Int,
    weight: Int):Boolean=
    {
    
    
    withTx {
    implicit neo =>
     
     val Id_nodeIndex = getNodeIndex("user").get                 // get user index
     val firstname_Index = getNodeIndex("firstname").get         // get user first name index
     val email_Index = getNodeIndex("email").get         // get user first name index
     val User_weight_index = getNodeIndex("user_weight").get     // get user weight index
     val u_node =  Id_nodeIndex.get("id",user_name).getSingle()  // get unique user node based on user name
     var ret = false // return value
     val loop = new Breaks;
     if(u_node == null)  // checking if the user already exists
     {
	     val node = createNode(user(first_name,last_name,user_name,email,location,previlege,time_created,weight,0,"","D",time_created,time_created,""))  // Creating a new user node
	     
	     //Indexing newly created user node
	     
	     Id_nodeIndex += (node,"id",user_name) 
	     firstname_Index += (node,"id",first_name)
	     email_Index += (node,"id",email)
	     User_weight_index += (node,"weight",new ValueContext(weight).indexNumeric())
	     
	     val UserTilesIndex = getNodeIndex("user_tiles").get  // get user tiles index
	     var tiles_node = UserTilesIndex.get("id",user_name).getSingle()
	     if(tiles_node != null)
	     {
	       UserTilesIndex.remove(tiles_node)
	     }
	     tiles_node = createNode(user_tiles(user_name,"",""))  // creating new user tile node to store the personalized tiles of the user
         UserTilesIndex += (tiles_node,"id",user_name) //indexing the user tiles node
         
         //Linking user to a location(state)
         val loc = location.toLowerCase()
         if(!loc.equals(""))
         {
           val location_index = getNodeIndex("location").get
           
           val nodes = location_index.query( "id", "*" ).iterator().asScala.toList.filterNot( x => x.getProperty("location_id").equals("all")) // Getting the list of all location nodes
           loop.breakable{      // looping through the states list
	           for(each <- nodes)
	           {
	             if(each.getProperty("cities").toString().split(",").contains(loc))  // checking if the list of the places of a state contains the user location
	             {
	               node --> "Belongs_To_Location" --> each  // Creating relation between location(state) node and user node
	               loop.break;  // breaking the loop when the user is linked to his location(state)
	             }
	           }
           }
         }
	     ret = true  // changing the return value to TRUE when the user is created successfully
     }
    ret 
    }
     
  }

  def add_friends(user_name: String,
      f_type: String,
      f_ids: String):Boolean=
    {
    
    
    withTx {
    implicit neo =>
     
	     val Id_nodeIndex = getNodeIndex("user").get                 // get user index
	     val u_node =  Id_nodeIndex.get("id",user_name).getSingle()  // get unique user node based on user name
	     var ret = false // return value
	     if(u_node != null)
	     {
	       if(u_node.hasProperty("friends"))
	       {
	         val friends = u_node.getProperty("friends").toString
	         var list = List[String]()
	         list = JSON.parseFull(friends).get.asInstanceOf[List[String]]
	         if(f_type.toLowerCase().equals("f"))
	         {
	           list = list.updated(0,f_ids)
	         }
	         else if(f_type.toLowerCase().equals("t"))
	         {
	           list = list.updated(1,f_ids)
	         }
	         else
	         {
	           list = list.updated(2,f_ids)
	         }
	         val json = JSONArray(list).toString() 
	         u_node.setProperty("friends",json).toString
	         
	       }
	       
	       else
	       {
	         var list: List[String] = List("", "", "")
	         if(f_type.toLowerCase().equals("f"))
	         {
	           list = list.updated(0,f_ids)
	         }
	         else if(f_type.toLowerCase().equals("t"))
	         {
	           list = list.updated(1,f_ids)
	         }
	         else
	         {
	           list = list.updated(2,f_ids)
	         }
	         
	         val json = JSONArray(list).toString() 
	         u_node.setProperty("friends",json).toString
	
	         
	       }
	     ret = true
	     }
      ret
      }
    }
  
    def get_friends(user_name: String):String=
    {
         val Id_nodeIndex = getNodeIndex("user").get                 // get user index
         val Fname_nodeIndex = getNodeIndex("firstname").get 
         val Email_nodeIndex = getNodeIndex("email").get
	     val u_node =  Id_nodeIndex.get("id",user_name).getSingle()  // get unique user node based on user name
	     var ret = "" // return value
	     if(u_node != null)
	     {
	       if(u_node.hasProperty("friends"))
	       {
	         var f_users = List[org.neo4j.graphdb.Node]()
	         var t_users = List[org.neo4j.graphdb.Node]()
	         var g_users = List[org.neo4j.graphdb.Node]()
	         val users = Id_nodeIndex.query("id", "*" ).iterator().asScala.toList
	         val fnames = Fname_nodeIndex.query("id", "*" ).iterator().asScala.toList
	         val emails = Email_nodeIndex.query("id", "*" ).iterator().asScala.toList
	         val id_list = users.map( x => x.getProperty("user_name").toString())
	         val fname_list = fnames.map( x => x.getProperty("first_name").toString().toLowerCase()).distinct
	         val email_list = emails.map( x => x.getProperty("email").toString())
	         val friends = u_node.getProperty("friends").toString
	         var list = List[String]()
	         list = JSON.parseFull(friends).get.asInstanceOf[List[String]]
	         val f = list(0)
	         
	         
	         val t = list(1)
	         val g = list(2)
	         if(!f.equals(""))
	         {
	           val f1 = f.split(",").toList
	           val f2 = f1.map( x => x.split(";").toList(0).toLowerCase()).distinct
	           val f3 = f1.map( x => x.split(";").toList(1).toLowerCase())
	           f_users = f3.intersect(id_list).map( x => Id_nodeIndex.get("id",x).getSingle()).filter( x => x != null) ::: f2.intersect(fname_list).map(x => Fname_nodeIndex.query("id", x ).iterator().asScala.toList).flatten.filter( x => x != null)
	           f_users = f_users.distinct
//	           val f_users1 = f2.intersect(fname_list).map(x => Fname_nodeIndex.query("id", x ).iterator().asScala.toList).flatten.filter( x => x != null)
	           
	           
	         }
	         if(!t.equals(""))
	         {
	           val t1 = t.split(",").toList
	           val t2 = t1.map( x => x.split(";").toList(0).toLowerCase()).distinct
	           val t3 = t1.map( x => x.split(";").toList(1).toLowerCase())
	           t_users = t3.intersect(id_list).map( x => Id_nodeIndex.get("id",x).getSingle()).filter( x => x != null) ::: t2.intersect(fname_list).map(x => Fname_nodeIndex.query("id", x ).iterator().asScala.toList).flatten.filter( x => x != null)
	           t_users = t_users.distinct
	         }
	         if(!g.equals(""))
	         {
	           val g1 = g.split(",").toList
	           val g2 = g1.map( x => x.split(";").toList(0).toLowerCase()).distinct
	           val g3 = g1.map( x => x.split(";").toList(1).toLowerCase())
	           g_users = g3.intersect(email_list).map( x => Email_nodeIndex.get("id",x).getSingle()).filter( x => x != null) ::: g2.intersect(fname_list).map(x => Fname_nodeIndex.query("id", x ).iterator().asScala.toList).flatten.filter( x => x != null)
	           
	         }
	         
	         val prev_friends = u_node.getRelationships("Follows",Direction.OUTGOING).asScala.toList.map( _.getOtherNode(u_node))
	         val tot_users = (f_users:::f_users:::f_users).distinct.filterNot( x => prev_friends.contains(x)).map( x => x.getProperty("user_name").toString())
	         if(tot_users.size > 0)
	         {
	        	 ret = tot_users.mkString(",")
	         }
	       }
	     }
	     
    
       
    ret
    }

  
  // when user subcribes for a feed
  def user_subscribefeed(user_name: String,
      feed_type: String  // "," separated feeds
      ):Boolean=
  {
    
    
    withTx {
    implicit neo =>
      var ret = false
      val nodeIndex = getNodeIndex("user").get
      val node =  nodeIndex.get("id",user_name).getSingle()
      if(node != null)
      {
        
    	  node.setProperty("feed_subscription",feed_type)   // setting the feed_subscription property
    	  ret = true
      }
      
      
      
    ret  
    }
  }
  
  
  // User edit function
  def edit_user(first_name: String,
    last_name: String,
    user_name: String,  // Unique user name of user
    email: String,
    location: String,
    previlege: Int):Boolean=
    {
    
    
    withTx {
    implicit neo =>
     
     val Id_nodeIndex = getNodeIndex("user").get
     val firstname_Index = getNodeIndex("firstname").get
     val email_Index = getNodeIndex("email").get
     val u_node =  Id_nodeIndex.get("id",user_name).getSingle()
     var ret = false
     if(u_node != null) // checking if the user already exists
     {
       
         firstname_Index.remove(u_node)  // removing user node from first name index
         email_Index.remove(u_node)  // removing user node from email index
         
         //setting the user properties again
	     u_node.setProperty("first_name",first_name)
	     u_node.setProperty("last_name",last_name)
	     u_node.setProperty("email",email)
	     u_node.setProperty("location",location)
	     u_node.setProperty("previlege",previlege)
	     
	     
	     firstname_Index += (u_node,"id",first_name) // Adding user to first name index
	     email_Index += (u_node,"id",email) // Adding user to first name index
	     
	     //removing user relation with location
	     if(u_node.hasRelationship("Belongs_To_Location",Direction.OUTGOING))
	     {
	       val loc_rel = u_node.getSingleRelationship("Belongs_To_Location",Direction.OUTGOING)
	       loc_rel.delete()
	     }
         
         // linking user to location
         val loc = location.toLowerCase()
         val loop = new Breaks;
         if(!loc.equals(""))
         {
           val location_index = getNodeIndex("location").get
           
           val nodes = location_index.query( "id", "*" ).iterator().asScala.toList.filterNot( x => x.getProperty("location_id").equals("all"))
           loop.breakable{
	           for(each <- nodes)
	           {
	             if(each.getProperty("cities").toString().split(",").contains(loc))
	             {
	               u_node --> "Belongs_To_Location" --> each 
	               loop.break;
	             }
	           }
           }
         }
	     ret = true
     }
    ret 
    }
     
  }
  
  def add_firstname_index()
  {
    
    
    withTx {
    implicit neo =>
     
     val Id_nodeIndex = getNodeIndex("user").get

     val users = Id_nodeIndex.query( "id", "*" ).iterator().asScala.toList

     val c_time = (System.currentTimeMillis()/1000).toInt
     val pos_index = getNodeIndex("pos").get
     var pos_node = pos_index.get("id","proper_nouns").getSingle()
     if(pos_node == null)
     {
             pos_node = createNode(pos("proper_nouns",100))
	         pos_index += (pos_node,"id","proper_nouns")
     }
     
     else
     {
             pos_node.setProperty("last_update",100)
     }
      
    }
     
  }
  
  
   // deleting a user
   def delete_user(
    user_name: String):Boolean=
    {
    
    
    withTx {
    implicit neo =>
     
     val Id_nodeIndex = getNodeIndex("user").get
     val firstname_Index = getNodeIndex("firstname").get
     val email_Index = getNodeIndex("email").get
     val User_weight_index = getNodeIndex("user_weight").get
     val UserTilesIndex = getNodeIndex("user_tiles").get
     val u_nodes =  Id_nodeIndex.get("id",user_name).iterator().asScala
     var ret = false
     for(u_node <- u_nodes)
     {
	     if(u_node != null)
	     {
		     val rels = u_node.getRelationships().asScala  // getting all the relations of the user
		     for(each <- rels)  // looping through the user relations to delete all of them
		     {
		       
		       // calling the delete functions based on the relations
		       if(each.getType().toString().equals("Article_Written_By"))
		       {
		         val art_node = each.getOtherNode(u_node)
		         delete_article(art_node.getProperty("article_id").toString())
		       }
		       else if(each.getType().toString().equals("Event_Created_By"))
		       {
		         val event_node = each.getOtherNode(u_node)
		         delete_event(event_node.getProperty("event_id").toString())
		       }
		       else if(each.getType().toString().equals("Quickpost_Written_By"))
		       {
		         val qp_node = each.getOtherNode(u_node)
		         delete_quickpost(qp_node.getProperty("qp_id").toString())
		       }
		       else if(each.getType().toString().equals("Petition_Written_By"))
		       {
		         val p_node = each.getOtherNode(u_node)
		         delete_petition(p_node.getProperty("p_id").toString())
		       }
		       else if(each.getType().toString().equals("Debate_Written_By"))
		       {
		         val d_node = each.getOtherNode(u_node)
		         delete_debate(d_node.getProperty("d_id").toString())
		       }
		       else if(each.getType().toString().equals("Townhall_Written_By"))
		       {
		         val t_node = each.getOtherNode(u_node)
		         delete_townhall(t_node.getProperty("t_id").toString())
		       }
		       else if(each.getType().toString().equals("Debate_Suggestion_Written_By"))
		       {
		         val ds_node = each.getOtherNode(u_node)
		         delete_debate_townhall_suggestion("D",ds_node.getProperty("d_id").toString())
		       }
		       else if(each.getType().toString().equals("Townhall_Suggestion_Written_By"))
		       {
		         val ts_node = each.getOtherNode(u_node)
		         delete_debate_townhall_suggestion("T",ts_node.getProperty("t_id").toString())
		       }
		       else if(each.getType().toString().equals("Comment_Written_By"))
		       {
		         val comment_node = each.getOtherNode(u_node)
		         delete_comment(comment_node.getProperty("comment_id").toString())
		       }
		       else
		       {
		         each.delete()
		       }
		     }
		     
		     
		     // removing the user node from all the indexes
		     Id_nodeIndex.remove(u_node)
		     firstname_Index.remove(u_node)
		     email_Index.remove(u_node)
		     User_weight_index.remove(u_node)
		     val user_tiles_node = UserTilesIndex.get("id",user_name).iterator()
		     while(user_tiles_node.hasNext())
		     {
		       val node = user_tiles_node.next()
		       UserTilesIndex.remove(node)
		       node.delete()
		     }
		     
		     //deleting the user node
		     u_node.delete()
		     
		     ret = true
	     }
     }
    ret 
    }
     
  } 
  
  
  // Triggered when user1 tries to follow user2
  def user_follow(
      user_name1:String,   // unique user name of user1
      user_name2:String,   // unique user name of user2
      time:Int
      ):Boolean=
  {
    
    
    
    
    withTx {
    implicit neo =>
   
      
     // getting indexes and nodes
     val nodeIndex = getNodeIndex("user").get
     val User_weight_index = getNodeIndex("user_weight").get
     val t = (System.currentTimeMillis()/1000).toInt - (86400*2)
     val user_tiles_index = getNodeIndex("user_tiles").get
     val article_content_index = getNodeIndex("article_content").get
     val user1 = nodeIndex.get("id",user_name1).getSingle()
     val user2 = nodeIndex.get("id",user_name2).getSingle()
     var ret = false
     if(user1 != null && user2 != null && !user1.getRelationships("Follows",Direction.OUTGOING).asScala.toList.map(_.getEndNode()).contains(user2))  // checking if the users exist
     {
         user1.setProperty("last_seen",time)
	     var u1_wt = user1.getProperty("weight").toString().toInt
	     var u2_wt = user2.getProperty("weight").toString().toInt
	     
	     u2_wt = u2_wt + Math.round(u1_wt.asInstanceOf[Float]/100)  // incresing the weight of user 2 as he got a new follower
	     user2.setProperty("weight",u2_wt)   //setting the new weight
	     val rel:org.neo4j.graphdb.Relationship = user1 --> "Follows" --> user2 < // linking the users with "Follows" relation
	     val rel_time = rel.setProperty("time",time)
	     val rel_in_wt = rel.setProperty("in_weight",Math.round(u1_wt.asInstanceOf[Float]/100))
	     val rel_out_wt = rel.setProperty("out_weight",0)
	     
	     
	     User_weight_index.remove(user2)
	     User_weight_index += (user2,"weight",new ValueContext(u2_wt).indexNumeric())
	     
	         
	         // updating the personalized tiles of user 1
             var perso_arts = List[org.neo4j.graphdb.Node]()
             var friends_arts = List[org.neo4j.graphdb.Node]()
             var hash_arts = List[org.neo4j.graphdb.Node]()
             var total_arts = List[org.neo4j.graphdb.Node]()
             
             var votedup_hashtags = user1.getRelationships("article_voteup,article_markfav").asScala.toList.map(x => x.getOtherNode(user1)).map( y => y.getRelationships("Belongs_To_Subcategory_Article","Tag_Of_Article").asScala.toList.map( z => z.getOtherNode(y))).flatten  // getting hashtags of the articles voted up by the user 1
             var viewed_hashtags = user1.getRelationships("Viewed_By").asScala.toList.map(x => x.getOtherNode(user1)).map( y => y.getRelationships("Belongs_To_Subcategory_Article","Tag_Of_Article").asScala.toList.map( z => z.getOtherNode(y))).flatten  // getting hashtags of the articles viewed up by the user 1
             
             val tot_hash_map =  (votedup_hashtags:::votedup_hashtags:::viewed_hashtags).groupBy(x=>x).mapValues(x=>x.length) // sorting hashtags based on frequency
             perso_arts = tot_hash_map.keys.toList.sortBy(x => -tot_hash_map(x)).slice(0,25).map(y => y.getRelationships("Belongs_To_Subcategory_Article","Tag_Of_Article").asScala.toList.map( z => z.getOtherNode(y))).flatten.distinct // extracting the personalized articles that are less that 2 days old based on the top 25 hashtags
            
             //extracting the articles that are less than 2 days old and written by the users whom user1 is following
             var friends = user1.getRelationships("Follows").asScala.map(_.getOtherNode(user1)).toList
             friends :+= user1
             friends_arts = friends.map( x => x.getRelationships("Article_Written_By").asScala.map(_.getOtherNode(x)).toList).flatten
             
             
             //extracting the articles that are less than 2 days old and realted to the hashtags that the user is following
//             val tag_name = user1.getProperty("fav_hash").toString()
//	         if(!tag_name.equals(""))
//             {
//               hash_arts = article_content_index.query("article_content", tag_name ).iterator().asScala.filter(x => ((x.getProperty("time_created").toString().toInt > t) ) ).toList.sortBy(-_.getProperty("time_created").toString().toInt)
//             }
             
             val cur_time = (System.currentTimeMillis()/1000).toInt
             val t1 = cur_time - (86400)
             val t2 = cur_time - (86400*3)
             var topic_arts = List[org.neo4j.graphdb.Node]()
             topic_arts = user1.getRelationships("Favourite_Topic").asScala.map(_.getOtherNode(user1)).toList.map( y => y.getRelationships("Belongs_To_Topic_Article").asScala.toList.map(_.getOtherNode(y))).flatten.distinct
             
             val one = friends_arts.intersect(topic_arts).filter(x => (x.getProperty("time_created").toString().toInt > t1)  ).sortBy(-_.getProperty("time_created").toString().toInt)
             val two = friends_arts.intersect(perso_arts).filter(x => (x.getProperty("time_created").toString().toInt > t1)  ).sortBy(-_.getProperty("time_created").toString().toInt)
             
             val three = friends_arts.intersect(topic_arts).filter(x => (x.getProperty("time_created").toString().toInt > t2)  ).sortBy(-_.getProperty("time_created").toString().toInt)
             val four = friends_arts.intersect(perso_arts).filter(x => (x.getProperty("time_created").toString().toInt > t2) ).sortBy(-_.getProperty("time_created").toString().toInt)
             
             //val five = (perso_arts:::friends_arts:::topic_arts).filter(x => (x.getProperty("time_created").toString().toInt > t2) ).sortBy(-_.getProperty("time_created").toString().toInt)
             
             val six = (perso_arts:::friends_arts:::topic_arts).sortBy(-_.getProperty("time_created").toString().toInt)
             
             total_arts = (one:::two:::three:::four:::six).distinct.filter( x => x.getProperty("space").toString.toInt == 0)
	         val sorted_news = total_arts.map(x => x.getProperty("article_id")).mkString(",")
	         
  
             
             // updating the personalized tiles property of the user tile node
             val tile_node = user_tiles_index.get("id",user_name1).getSingle()
             tile_node.setProperty("news_Personalized",sorted_news)

	     ret = true
     }
    ret
    }
  }
 
  // Triggered when user1 tries to unfollow user2
  def user_unfollow(
      user_name1:String,  // unique user name of user1
      user_name2:String   // unique user name of user2
      ):Boolean=
  {
    
    
    
    
    withTx {
    implicit neo =>
   
     val nodeIndex = getNodeIndex("user").get
     val User_weight_index = getNodeIndex("user_weight").get
     val t = (System.currentTimeMillis()/1000).toInt - (86400*2)
     val user_tiles_index = getNodeIndex("user_tiles").get
     val article_content_index = getNodeIndex("article_content").get
     val user1 = nodeIndex.get("id",user_name1).getSingle()
     val user2 = nodeIndex.get("id",user_name2).getSingle()
     var ret = false
     if(user1 != null && user2 != null && user1.getRelationships("Follows",Direction.OUTGOING).asScala.toList.map(_.getEndNode()).contains(user2))
     {
       
         
         val foll_users = user1.getRelationships("Follows",Direction.OUTGOING).asScala
	     for(each <- foll_users)
	     {
	      
	       if(each.getOtherNode(user1) == user2)
	       {
	         val del_wt = each.getProperty("in_weight").toString().toInt
	         var u2_wt = user2.getProperty("weight").toString().toInt
	         u2_wt = u2_wt - del_wt
	         user2.setProperty("weight",u2_wt)
	         each.delete()
	         User_weight_index.remove(user2)
	         User_weight_index += (user2,"weight",new ValueContext(u2_wt).indexNumeric())
	         
	         
	         //val nouns = user1.getProperty("nouns").toString()
             var perso_arts = List[org.neo4j.graphdb.Node]()
             var friends_arts = List[org.neo4j.graphdb.Node]()
             var hash_arts = List[org.neo4j.graphdb.Node]()
             var total_arts = List[org.neo4j.graphdb.Node]()
             
             var votedup_hashtags = user1.getRelationships("article_voteup,article_markfav").asScala.toList.map(x => x.getOtherNode(user1)).map( y => y.getRelationships("Belongs_To_Subcategory_Article","Tag_Of_Article").asScala.toList.map( z => z.getOtherNode(y))).flatten
             var viewed_hashtags = user1.getRelationships("Viewed_By").asScala.toList.map(x => x.getOtherNode(user1)).map( y => y.getRelationships("Belongs_To_Subcategory_Article","Tag_Of_Article").asScala.toList.map( z => z.getOtherNode(y))).flatten
             
             val tot_hash_map =  (votedup_hashtags:::votedup_hashtags:::viewed_hashtags).groupBy(x=>x).mapValues(x=>x.length)
             perso_arts = tot_hash_map.keys.toList.sortBy(x => -tot_hash_map(x)).slice(0,25).map(y => y.getRelationships("Belongs_To_Subcategory_Article","Tag_Of_Article").asScala.toList.map( z => z.getOtherNode(y))).flatten.distinct
            

             var friends = user1.getRelationships("Follows").asScala.map(_.getOtherNode(user1)).toList
             friends :+= user1
             friends_arts = friends.map( x => x.getRelationships("Article_Written_By").asScala.map(_.getOtherNode(x)).toList).flatten
             
             

//	         val tag_name = user1.getProperty("fav_hash").toString()
//	         if(!tag_name.equals(""))
//             {
//               hash_arts = article_content_index.query("article_content", tag_name ).iterator().asScala.filter(x => ((x.getProperty("time_created").toString().toInt > t) ) ).toList.sortBy(-_.getProperty("time_created").toString().toInt)
//             }
             
             val cur_time = (System.currentTimeMillis()/1000).toInt
             val t1 = cur_time - (86400)
             val t2 = cur_time - (86400*3)
             var topic_arts = List[org.neo4j.graphdb.Node]()
             topic_arts = user1.getRelationships("Favourite_Topic").asScala.map(_.getOtherNode(user1)).toList.map( y => y.getRelationships("Belongs_To_Topic_Article").asScala.toList.map(_.getOtherNode(y))).flatten.distinct
             
             val one = friends_arts.intersect(topic_arts).filter(x => (x.getProperty("time_created").toString().toInt > t1)  ).sortBy(-_.getProperty("time_created").toString().toInt)
             val two = friends_arts.intersect(perso_arts).filter(x => (x.getProperty("time_created").toString().toInt > t1)  ).sortBy(-_.getProperty("time_created").toString().toInt)
             
             val three = friends_arts.intersect(topic_arts).filter(x => (x.getProperty("time_created").toString().toInt > t2)  ).sortBy(-_.getProperty("time_created").toString().toInt)
             val four = friends_arts.intersect(perso_arts).filter(x => (x.getProperty("time_created").toString().toInt > t2) ).sortBy(-_.getProperty("time_created").toString().toInt)
             
           //  val five = (perso_arts:::friends_arts:::topic_arts).filter(x => (x.getProperty("time_created").toString().toInt > t2) ).sortBy(-_.getProperty("time_created").toString().toInt)
             
             val six = (perso_arts:::friends_arts:::topic_arts).sortBy(-_.getProperty("time_created").toString().toInt)
             
             total_arts = (one:::two:::three:::four:::six).distinct.filter( x => x.getProperty("space").toString.toInt == 0)
	         val sorted_news = total_arts.map(x => x.getProperty("article_id")).mkString(",")
	         
             val tile_node = user_tiles_index.get("id",user_name1).getSingle()
             tile_node.setProperty("news_Personalized",sorted_news)

	       }
	       
	     }
	     ret = true
     }
    ret 
    }
  }
  
  //Triggered when user clicks voteup/markfav/readlater on Article/Event/Comment/Quickpost
  def voteup_markfav_readlater(
      user_name:String,  // unique user name
      item_type:String,  // Article/Event/Comment
      item_id:String, // article id/event id/quickpost id/comment id
      action: String, // voteup/markfav/readlater
      time:Int
      ):Boolean=
  {
    
    
    
    
    withTx {
    implicit neo =>
      
    
      
    def matchTest(x: String): String = x match {
	    case "A" => "Article"
	    case "E" => "Event"
	    case "P" => "Petition"
	    case "T" => "Townhall"
	    case "D" => "Debate"
	    case "C" => "Comment"
	    case "U" => "User"
	    case "H" => "Sub_category"
	    case "a" => "Article"
	    case "e" => "Event"
	    case "p" => "Petition"
	    case "t" => "Townhall"
	    case "d" => "Debate"
	    case "c" => "Comment"
	    case "u" => "User"
	    case "h" => "Sub_category"
	    }
    val i_type = matchTest(item_type)
   
     val event_weight_index = getNodeIndex("event_weight").get
     val user_weight_index = getNodeIndex("user_weight").get
     val article_weight_index = getNodeIndex("article_weight").get
     val quickpost_weight_index = getNodeIndex("quickpost_weight").get
     val hash_weight_index = getNodeIndex("hash_weight").get
     
     val grp_name = i_type.toLowerCase()
     val Index = getNodeIndex(grp_name).get  
     
     
     val item_node = Index.get("id",item_id).getSingle()  // Retrieving Item node based on item_id
     
     val userIndex = getNodeIndex("user").get
     val user_node = userIndex.get("id",user_name).getSingle() // Retrieving user node based on userid
     
     var ret = false
     if(item_node != null && user_node != null)   //  checking if the item node and the user node exists
	 {
         val i_wt = item_node.getProperty("weight").toString().toInt
         val u_wt = user_node.getProperty("weight").toString().toInt
         user_node.setProperty("last_seen",time)  // updating the last seen time of the user node
       
         
	     if(action.equals("markfav"))
	     {
	       
	       val author_node = item_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode()
	       val au_wt = author_node.getProperty("weight").toString().toInt
	       val hash_nodes = item_node.getRelationships("Belongs_To_Subcategory_Article",Direction.OUTGOING).asScala.map(_.getOtherNode(item_node)).toList
	       
	       // checking if the user already added the article to the favourites
	       if(!item_node.getRelationships("article_markfav").asScala.map(_.getOtherNode(item_node)).toList.contains(user_node))
	       {
	         
	         // increasing the weights of article, author and hashtags
	         
	         item_node.setProperty("weight",i_wt + Math.round(u_wt.asInstanceOf[Float]/100))
	         author_node.setProperty("weight",au_wt + Math.round(u_wt.asInstanceOf[Float]/100))
	         for(each <- hash_nodes)
	         {
		         val h_wt = each.getProperty("weight").toString().toInt
			     each.setProperty("weight",h_wt + (Math.round(u_wt.asInstanceOf[Float]/100)))
			     hash_weight_index.remove(each)
				 hash_weight_index += (each,"weight",new ValueContext( h_wt + (Math.round(u_wt.asInstanceOf[Float]/100)) ).indexNumeric())
	         }
	         article_weight_index.remove(item_node)
		     article_weight_index += (item_node,"weight",new ValueContext( i_wt + Math.round(u_wt.asInstanceOf[Float]/100) ).indexNumeric())
		     user_weight_index.remove(author_node)
		     user_weight_index += (author_node,"weight",new ValueContext( au_wt + Math.round(u_wt.asInstanceOf[Float]/100) ).indexNumeric())
	
		     // creating markfav relation between user and the article
	         val rel_name = i_type.toLowerCase()+"_markfav"
	         var rel: org.neo4j.graphdb.Relationship = user_node --> rel_name --> item_node <  //Relating user node to the item node with "Vote_up" relation
	         val rel_time = rel.setProperty("time", time)
	         var rel_in_wt = rel.setProperty("in_weight", Math.round(u_wt.asInstanceOf[Float]/100))
	         var rel_out_wt = rel.setProperty("out_weight", 0)
	         
	         // indexing the markfav relation
	         val Relation_Index = getRelationIndex(rel_name).get
	         val rel_value = user_name+item_id+rel_name
	         Relation_Index += (rel,"rel_id",rel_value)

	          
	       }
	       
	       
	       else
	       {
	         
	         
	         val rel_name = i_type.toLowerCase()+"_markfav"
	         val rel_value = user_name+item_id+rel_name
	         val Relation_Index = getRelationIndex(rel_name).get
	         val relation = Relation_Index.get("rel_id",rel_value).getSingle()
	         val r_wt = relation.getProperty("in_weight").toString().toInt
	         
	         // taking back the weights given to the article, author and hashtags
	         item_node.setProperty("weight",i_wt - r_wt)
	         
	         author_node.setProperty("weight",au_wt - r_wt)

	         for(each <- hash_nodes)
	         {
		         val h_wt = each.getProperty("weight").toString().toInt
			     each.setProperty("weight",h_wt - r_wt)
			     hash_weight_index.remove(each)
				 hash_weight_index += (each,"weight",new ValueContext( h_wt - r_wt ).indexNumeric())
	         }
		     article_weight_index.remove(item_node)
		     article_weight_index += (item_node,"weight",new ValueContext( i_wt - r_wt ).indexNumeric())
		     user_weight_index.remove(author_node)
		     user_weight_index += (author_node,"weight",new ValueContext( au_wt - r_wt ).indexNumeric())
	         
		     // deleting the relation between article and user
		     Relation_Index.remove(relation)
	         relation.delete()

	       }
	     }
	     
	     
	     else if(action.equals("readlater"))
	     {
		       val rel_type = grp_name + "_" + "readlater"
		       if(!item_node.getRelationships(rel_type).asScala.map(_.getOtherNode(item_node)).toList.contains(user_node))
		       {
		          var rel: org.neo4j.graphdb.Relationship = user_node --> rel_type --> item_node <  //Relating user node to the item node with "Vote_up" relation
		          val rel_time = rel.setProperty("time", time)
		          val Relation_Index = getRelationIndex(rel_type).get
	              val rel_value = user_name+item_id+rel_type
	              Relation_Index += (rel,"rel_id",rel_value)
		       }
		       
		       else
		       {
		          val rel_value = user_name+item_id+rel_type
	              val Relation_Index = getRelationIndex(rel_type).get
	              val relation = Relation_Index.get("rel_id",rel_value).getSingle()
	              Relation_Index.remove(relation)
	              relation.delete()
		       }
	     }
	     
	     else
	     {
	       if(i_type.equals("Comment"))
	       {
	         val author_node = item_node.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(item_node)
		         
	         if(!item_node.getRelationships("comment_voteup").asScala.map(_.getOtherNode(item_node)).toList.contains(user_node))
		     {
		         
		         
		         
		         val rel_name = i_type.toLowerCase()+"_voteup"
		         if(!author_node.equals(user_node))
		         {
			         val rel: org.neo4j.graphdb.Relationship = user_node --> rel_name --> item_node <
			         var rel_time = rel.setProperty("time", time)
			         var rel_in_wt = rel.setProperty("in_weight", Math.round(u_wt.asInstanceOf[Float]/300))
		             var rel_out_wt = rel.setProperty("out_weight", 0)
			         val Relation_Index = getRelationIndex(rel_name).get
			         val rel_value = user_name+item_id+rel_name
			         Relation_Index += (rel,"rel_id",rel_value)
			         item_node.setProperty("weight",i_wt+Math.round(u_wt.asInstanceOf[Float]/300))
			         val au_wt = author_node.getProperty("weight").toString().toInt
			         author_node.setProperty("weight",au_wt+Math.round(u_wt.asInstanceOf[Float]/300))
			         user_weight_index.remove(author_node)
			         user_weight_index += (author_node,"weight",new ValueContext( au_wt+Math.round(u_wt.asInstanceOf[Float]/300) ).indexNumeric())
		         }
		         else
		         {
		             val rel: org.neo4j.graphdb.Relationship = user_node --> rel_name --> item_node <
			         var rel_time = rel.setProperty("time", time)
			         val Relation_Index = getRelationIndex(rel_name).get
			         val rel_value = user_name+item_id+rel_name
			         Relation_Index += (rel,"rel_id",rel_value)
			        
		         }
		         
		         
		     }
	         else
		     {
	
		         val rel_name = i_type.toLowerCase()+"_voteup"
		         val rel_value = user_name+item_id+rel_name
		     
		         val Relation_Index = getRelationIndex(rel_name).get
		
		         val relation = Relation_Index.get("rel_id",rel_value).getSingle()
		         if(!author_node.equals(user_node))
		         {
			         val r_wt = relation.getProperty("in_weight").toString().toInt
			         Relation_Index.remove(relation)
			         relation.delete()
			         item_node.setProperty("weight",i_wt-r_wt)
			         val au_wt = author_node.getProperty("weight").toString().toInt
			         author_node.setProperty("weight",au_wt-r_wt)
			         user_weight_index.remove(author_node)
			         user_weight_index += (author_node,"weight",new ValueContext( au_wt-r_wt ).indexNumeric())
		         }
		         else
		         {
		             Relation_Index.remove(relation)
			         relation.delete()
		         }
		         
		         
		     }
	       }
	     
	       else
	       {
	         
	         val rel_name = i_type.toLowerCase()+"_voteup"
	         if(!item_node.getRelationships(rel_name).asScala.map(_.getOtherNode(item_node)).toList.contains(user_node))
	         {
	        	 
		         var rel: org.neo4j.graphdb.Relationship = user_node --> rel_name --> item_node <  //Relating user node to the item node with "Vote_up" relation
		         var rel_time = rel.setProperty("time", time)
		         val Relation_Index = getRelationIndex(rel_name).get
		         val rel_value = user_name+item_id+rel_name
		         Relation_Index += (rel,"rel_id",rel_value)
		         
		         
		         
		         if(i_type.equals("Article"))
		         {
		           
		           var rel_in_wt = rel.setProperty("in_weight", Math.round(u_wt.asInstanceOf[Float]/100))
	               var rel_out_wt = rel.setProperty("out_weight", 0)  
		           val author_node = item_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(item_node)
		           val au_wt = author_node.getProperty("weight").toString().toInt
		           author_node.setProperty("weight",au_wt+Math.round(u_wt.asInstanceOf[Float]/100))
		           article_weight_index.remove(item_node)
		           item_node.setProperty("weight",i_wt+Math.round(u_wt.asInstanceOf[Float]/100))
		           article_weight_index += (item_node,"weight",new ValueContext( i_wt+Math.round(u_wt.asInstanceOf[Float]/100) ).indexNumeric())
		           user_weight_index.remove(author_node)
		           user_weight_index += (author_node,"weight",new ValueContext( au_wt+Math.round(u_wt.asInstanceOf[Float]/100)).indexNumeric())
		           val hash_nodes = item_node.getRelationships("Belongs_To_Subcategory_Article",Direction.OUTGOING).asScala.map(_.getOtherNode(item_node)).toList
	               for(each <- hash_nodes)
	               {
			         val h_wt = each.getProperty("weight").toString().toInt
				     each.setProperty("weight",h_wt + (Math.round(u_wt.asInstanceOf[Float]/100)))
				     hash_weight_index.remove(each)
					 hash_weight_index += (each,"weight",new ValueContext( h_wt + (Math.round(u_wt.asInstanceOf[Float]/100)) ).indexNumeric())
	               }

		         }
		         else
		         {
		           var rel_in_wt = rel.setProperty("in_weight", Math.round(u_wt.asInstanceOf[Float]/300))
	               var rel_out_wt = rel.setProperty("out_weight", 0)  
		           val author_node = item_node.getSingleRelationship("Quickpost_Written_By",Direction.OUTGOING).getOtherNode(item_node)
		           val au_wt = author_node.getProperty("weight").toString().toInt
		           author_node.setProperty("weight",au_wt+Math.round(u_wt.asInstanceOf[Float]/300))
		           quickpost_weight_index.remove(item_node)
		           item_node.setProperty("weight",i_wt+Math.round(u_wt.asInstanceOf[Float]/300))
		           quickpost_weight_index += (item_node,"weight",new ValueContext( i_wt+Math.round(u_wt.asInstanceOf[Float]/300) ).indexNumeric())
		           user_weight_index.remove(author_node)
		           user_weight_index += (author_node,"weight",new ValueContext( au_wt+Math.round(u_wt.asInstanceOf[Float]/300) ).indexNumeric())
		           
		           
		         }
		         
	         }
	         else
	         {
	           if(i_type.equals("Article"))
	           {
	             
	             val rel_name = i_type.toLowerCase()+"_voteup"
		         val rel_value = user_name+item_id+rel_name
		     
		         val Relation_Index = getRelationIndex(rel_name).get
		         val relation = Relation_Index.get("rel_id",rel_value).getSingle()
		         val r_wt = relation.getProperty("in_weight").toString().toInt
		         
		         Relation_Index.remove(relation)
		         relation.delete()
		         
		         item_node.setProperty("weight",i_wt-r_wt)
		         val author_node = item_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(item_node)
		         val au_wt = author_node.getProperty("weight").toString().toInt
		         author_node.setProperty("weight",au_wt-r_wt)
		         article_weight_index.remove(item_node)
		         article_weight_index += (item_node,"weight",new ValueContext( i_wt-r_wt ).indexNumeric())
		         user_weight_index.remove(author_node)
		         user_weight_index += (author_node,"weight",new ValueContext( au_wt-r_wt ).indexNumeric())
		         val hash_nodes = item_node.getRelationships("Belongs_To_Subcategory_Article",Direction.OUTGOING).asScala.map(_.getOtherNode(item_node)).toList
	             for(each <- hash_nodes)
	             {
			         val h_wt = each.getProperty("weight").toString().toInt
				     each.setProperty("weight",h_wt - (Math.round(u_wt.asInstanceOf[Float]/100)))
				     hash_weight_index.remove(each)
					 hash_weight_index += (each,"weight",new ValueContext( h_wt - (Math.round(u_wt.asInstanceOf[Float]/100)) ).indexNumeric())
	             }
	             

	           }
	           else
	           {
	             
	             val rel_name = i_type.toLowerCase()+"_voteup"
		         val rel_value = user_name+item_id+rel_name
		     
		         val Relation_Index = getRelationIndex(rel_name).get
		         val relation = Relation_Index.get("rel_id",rel_value).getSingle()
		         val r_wt = relation.getProperty("in_weight").toString().toInt
		         Relation_Index.remove(relation)
		         relation.delete()
		         
		         item_node.setProperty("weight",i_wt-r_wt)
		         val author_node = item_node.getSingleRelationship("Quickpost_Written_By",Direction.OUTGOING).getOtherNode(item_node)
		         val au_wt = author_node.getProperty("weight").toString().toInt
		         author_node.setProperty("weight",au_wt-r_wt)
		         quickpost_weight_index.remove(item_node)
		         quickpost_weight_index += (item_node,"weight",new ValueContext( i_wt-r_wt ).indexNumeric())
		         user_weight_index.remove(author_node)
		         user_weight_index += (author_node,"weight",new ValueContext( au_wt-r_wt ).indexNumeric())
		         
	           }
	           
	         }
	         
	       }
	     
	     }
	     ret = true
     }
    ret
    }
  }
  
  //This function is triggered when a user votes down an article/comment/quick post
  def votedown(
      user_name:String, // unique user name
      item_type:String, // Comment
      item_id:String, // comment id
      time:Int
      ):Boolean=
  {
    
   withTx {
    implicit neo =>
   
      
    def matchTest(x: String): String = x match {
    case "A" => "Article"
    case "E" => "Event"
    case "Q" => "Quickpost"
    case "C" => "Comment"
    case "U" => "User"
    case "H" => "Sub_category"
    case "a" => "Article"
    case "e" => "Event"
    case "q" => "Quickpost"
    case "c" => "Comment"
    case "u" => "User"
    case "h" => "Sub_category"
    }
    val i_type = matchTest(item_type)
    
     val grp_name = i_type.toLowerCase()
     val Index = getNodeIndex(grp_name).get  
     val user_weight_index = getNodeIndex("user_weight").get
     val item_node = Index.get("id",item_id).getSingle()  // Retrieving Item node based on item_id
     val userIndex = getNodeIndex("user").get
     val user_node = userIndex.get("id",user_name).getSingle() // Retrieving user node based on userid
     var ret = false
     if(item_node != null && user_node !=null)
	 {
         user_node.setProperty("last_seen",time)
         val author_node = item_node.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(item_node)
		         
	     val u_wt = user_node.getProperty("weight").toString().toInt
	     val i_wt = item_node.getProperty("weight").toString().toInt
	     if(!item_node.getRelationships("comment_votedown").asScala.map(_.getOtherNode(item_node)).toList.contains(user_node))  // checking if the user already voted down the item
		 {
		         
		         
		         if(!author_node.equals(user_node))
		         {
		           
		             // creating vote down relation between user and the item
			         val rel_name = i_type.toLowerCase()+"_votedown"
			         val rel: org.neo4j.graphdb.Relationship = user_node --> rel_name --> item_node <
			         var rel_time = rel.setProperty("time", time)
			         var rel_in_wt = rel.setProperty("in_weight", -Math.round(u_wt.asInstanceOf[Float]/300))
			         var rel_out_wt = rel.setProperty("out_weight", 0)
			         val Relation_Index = getRelationIndex(rel_name).get
			         val rel_value = user_name+item_id+rel_name
			         Relation_Index += (rel,"rel_id",rel_value)
			         
			         //reducing the weight of author and item
			         item_node.setProperty("weight",i_wt-Math.round(u_wt.asInstanceOf[Float]/300))
			         val au_wt = author_node.getProperty("weight").toString().toInt
			         author_node.setProperty("weight",au_wt-Math.round(u_wt.asInstanceOf[Float]/300))
			         user_weight_index.remove(author_node)
			         user_weight_index += (author_node,"weight",new ValueContext( au_wt-Math.round(u_wt.asInstanceOf[Float]/300) ).indexNumeric())
		         }
		         else
		         {
		             val rel_name = i_type.toLowerCase()+"_votedown"
			         val rel: org.neo4j.graphdb.Relationship = user_node --> rel_name --> item_node <
			         var rel_time = rel.setProperty("time", time)
		         }
		         
		     }
	         else
		     {
	        	 if(!author_node.equals(user_node))
		         {
			         val rel_name = i_type.toLowerCase()+"_votedown"
			         val rel_value = user_name+item_id+rel_name
			     
			         val Relation_Index = getRelationIndex(rel_name).get
			
			         val relation = Relation_Index.get("rel_id",rel_value).getSingle()
			         val r_wt = relation.getProperty("in_weight").toString().toInt
			         Relation_Index.remove(relation)
			         relation.delete()
			         
			         item_node.setProperty("weight",i_wt-r_wt)
			         val au_wt = author_node.getProperty("weight").toString().toInt
			         author_node.setProperty("weight",au_wt-r_wt)
			         user_weight_index.remove(author_node)
			         user_weight_index += (author_node,"weight",new ValueContext( au_wt-r_wt ).indexNumeric())
		         
		         }
	        	 else
	        	 {
	        	     val rel_name = i_type.toLowerCase()+"_votedown"
			         val rel_value = user_name+item_id+rel_name
			     
			         val Relation_Index = getRelationIndex(rel_name).get
			
			         val relation = Relation_Index.get("rel_id",rel_value).getSingle()
			         Relation_Index.remove(relation)
			         relation.delete()
	        	 }
		  }
	     ret = true
	 }
    ret
    }
  }
  
  
  def nonuser_view(
      user_name:String, // unique user name  ("" if the user is not a registered user)
      item_type:String, // Article/Event
      item_id:String, // article id/event id
      time:Int
      ):Boolean=
  {
    
    
    true
  }
  
  
  
  def user_add_hashfav(
      user_name:String,
      tag_name:String
      ):Boolean=
  {
    
    
    
    
    withTx {
    implicit neo =>
      
      var ret = false
      val cur_time = (System.currentTimeMillis()/1000).toInt
      val t = cur_time - (86400*2)
      val UserIndex = getNodeIndex("user").get
      val user_node = UserIndex.get("id",user_name).getSingle()  //Retrieving user node based on userid
      val HashIndex = getNodeIndex("sub_category").get
      val user_tiles_index = getNodeIndex("user_tiles").get
      val article_content_index = getNodeIndex("article_content").get
      if(user_node != null && !tag_name.equals(""))
      {
        user_node.getProperty("last_seen",cur_time)
        //val hash_list = tag_name.split(",").toList.map(x => x.toLowerCase())
//        for(each <- hash_list)
//        {
//          val hash_node = HashIndex.get("name",each).getSingle()  //Retrieving user node based on userid
//          if(hash_node != null)
//          {
            // user_node --> "Favourite_Hash" --> hash_node
            
            
             //val nouns = user_node.getProperty("nouns").toString()
             var perso_arts = List[org.neo4j.graphdb.Node]()
             var friends_arts = List[org.neo4j.graphdb.Node]()
             var hash_arts = List[org.neo4j.graphdb.Node]()
             var total_arts = List[org.neo4j.graphdb.Node]()
             
             var votedup_hashtags = user_node.getRelationships("article_voteup,article_markfav").asScala.toList.map(x => x.getOtherNode(user_node)).map( y => y.getRelationships("Belongs_To_Subcategory_Article","Tag_Of_Article").asScala.toList.map( z => z.getOtherNode(y))).flatten
             var viewed_hashtags = user_node.getRelationships("Viewed_By").asScala.toList.map(x => x.getOtherNode(user_node)).map( y => y.getRelationships("Belongs_To_Subcategory_Article","Tag_Of_Article").asScala.toList.map( z => z.getOtherNode(y))).flatten
             
             val tot_hash_map =  (votedup_hashtags:::votedup_hashtags:::viewed_hashtags).groupBy(x=>x).mapValues(x=>x.length)
             perso_arts = tot_hash_map.keys.toList.sortBy(x => -tot_hash_map(x)).slice(0,25).map(y => y.getRelationships("Belongs_To_Subcategory_Article","Tag_Of_Article").asScala.toList.map( z => z.getOtherNode(y))).flatten.distinct.filter(x => x.getProperty("time_created").toString().toInt > t).sortBy(-_.getProperty("time_created").toString().toInt)
            
//             if(!nouns.equals("") && !nouns.equals("{}"))
//             {
//               val nouns_map = JSON.parseFull(nouns).get.asInstanceOf[Map[String,Double]]
//               var nouns_list = nouns_map.keys.toList.sortBy(x => -nouns_map(x)).slice(0,50).filter(x => nouns_map(x) > 10).mkString(" ")
//               val toRemove = "~`!@#$%^&*()_-+=*{}[]:;'|/".toSet
//               nouns_list = nouns_list.filterNot(toRemove)
//               if(!nouns_list.equals(""))
//               {
//            	   perso_arts = article_content_index.query("article_content", nouns_list ).iterator().asScala.filter(x => ((x.getProperty("time_created").toString().toInt > t) ) ).slice(0,20).toList
//               }
//             }
             var friends = user_node.getRelationships("Follows").asScala.map(_.getOtherNode(user_node)).toList
             friends :+= user_node
             friends_arts = friends.map( x => x.getRelationships("Article_Written_By").asScala.map(_.getOtherNode(x)).toList).flatten.filter(y => ((y.getProperty("time_created").toString().toInt > t) ) )
             
             
//             val hash_favs = user_node.getRelationships("Favourite_Hash").asScala.map(_.getOtherNode(user_node)).toList
//             if(tag_name.equals())
//             {
               val tag_list = tag_name.replace(',',' ')
               hash_arts = article_content_index.query("article_content", tag_list ).iterator().asScala.filter(x => ((x.getProperty("time_created").toString().toInt > t) ) ).toList
               user_node.setProperty("fav_hash",tag_list)
//             }
             
             def intersperse[A](a : List[A], b : List[A]): List[A] = a match {
				  case first :: rest => first :: intersperse(b, rest)
				  case _             => b
			 }  
             
             total_arts = intersperse(perso_arts,intersperse(hash_arts,friends_arts)).distinct
             val sorted_list = total_arts.sortBy(-_.getProperty("time_created").toString().toInt)
             val sorted_news = sorted_list.map(x => x.getProperty("article_id")).mkString(",")
//             val sorted_pol = sorted_list.filter(x => x.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x).getProperty("name").toString()).toList.contains("politics")).map(x => x.getProperty("article_id")).mkString(",")
//	         val sorted_tech = sorted_list.filter(x => x.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x).getProperty("name").toString()).toList.contains("technology")).map(x => x.getProperty("article_id")).mkString(",")
//	         val sorted_spo = sorted_list.filter(x => x.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x).getProperty("name").toString()).toList.contains("sports")).map(x => x.getProperty("article_id")).mkString(",")
//	         val sorted_ente = sorted_list.filter(x => x.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x).getProperty("name").toString()).toList.contains("entertainment")).map(x => x.getProperty("article_id")).mkString(",")
//	         val sorted_human = sorted_list.filter(x => x.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x).getProperty("name").toString()).toList.contains("humaninterest")).map(x => x.getProperty("article_id")).mkString(",")
	         //tile_node.setProperty("humaninterest_Personalized",sorted_human) 
             val tile_node = user_tiles_index.get("id",user_name).getSingle()
             tile_node.setProperty("news_Personalized",sorted_news)
//             tile_node.setProperty("politics_Personalized",sorted_pol)
//             tile_node.setProperty("technology_Personalized",sorted_tech)
//             tile_node.setProperty("sports_Personalized",sorted_spo)
//             tile_node.setProperty("entertainment_Personalized",sorted_ente)
//             tile_node.setProperty("humaninterest_Personalized",sorted_human) 
               
             
           
            
          
        
        
        
      ret = true
      }
      
      
    ret
    }
  }
  
  
  // called when a user selects favorite topics
  def user_add_favtopic(
      user_name:String,
      topics:String
      ):Boolean=
  {
    
    
    
    
    withTx {
    implicit neo =>
      
      var ret = false
      val cur_time = (System.currentTimeMillis()/1000).toInt
      val t = cur_time - (86400*2)
      val UserIndex = getNodeIndex("user").get
      val user_node = UserIndex.get("id",user_name).getSingle()  //Retrieving user node based on userid
      val TopicIndex = getNodeIndex("topic").get
      val user_tiles_index = getNodeIndex("user_tiles").get
      val article_content_index = getNodeIndex("article_content").get
      if(user_node != null)
      {
        user_node.getProperty("last_seen",cur_time)
        
        // deleting the previous Favourite_Topic relations of user if any
        val fav_topics_list = user_node.getRelationships("Favourite_Topic").asScala.toList
        if(fav_topics_list.size > 0)
        {
          for(each <- fav_topics_list)
          {
            each.delete()
          }
        }
        
        // linking the user node to favorite topic nodes
        if(!topics.equals(""))
        {
            val topic_list = topics.split(",").toList.map(x => x.toLowerCase())
	        for(each <- topic_list)
	        {
	          val topic_node = TopicIndex.get("name",each).getSingle()  //Retrieving user node based on userid
	          if(topic_node != null)
	          {
	             user_node --> "Favourite_Topic" --> topic_node
	          }
	          else
	          {
	        	  			val topic_node = createNode(topic(each,""))
			                TopicIndex += (topic_node,"name",each)
			                user_node --> "Favourite_Topic" --> topic_node
	          }
	        }
        }
            
            
             // updatiung the personalized tiles of the user
             var perso_arts = List[org.neo4j.graphdb.Node]()
             var friends_arts = List[org.neo4j.graphdb.Node]()
             var hash_arts = List[org.neo4j.graphdb.Node]()
             
             var total_arts = List[org.neo4j.graphdb.Node]()
             
             var votedup_hashtags = user_node.getRelationships("article_voteup,article_markfav").asScala.toList.map(x => x.getOtherNode(user_node)).map( y => y.getRelationships("Belongs_To_Subcategory_Article","Tag_Of_Article").asScala.toList.map( z => z.getOtherNode(y))).flatten
             var viewed_hashtags = user_node.getRelationships("Viewed_By").asScala.toList.map(x => x.getOtherNode(user_node)).map( y => y.getRelationships("Belongs_To_Subcategory_Article","Tag_Of_Article").asScala.toList.map( z => z.getOtherNode(y))).flatten
             
             val tot_hash_map =  (votedup_hashtags:::votedup_hashtags:::viewed_hashtags).groupBy(x=>x).mapValues(x=>x.length)
             perso_arts = tot_hash_map.keys.toList.sortBy(x => -tot_hash_map(x)).slice(0,25).map(y => y.getRelationships("Belongs_To_Subcategory_Article","Tag_Of_Article").asScala.toList.map( z => z.getOtherNode(y))).flatten.distinct
            

             val friends = user_node.getRelationships("Follows").asScala.map(_.getOtherNode(user_node)).toList
             if(friends != Nil)
             {
               friends_arts = friends.map( x => x.getRelationships("Article_Written_By").asScala.map(_.getOtherNode(x)).toList).flatten
             }
             

//               val tag_name = user_node.getProperty("fav_hash").toString()
//		       if(!tag_name.equals(""))
//	           {
//	             
//	             hash_arts = article_content_index.query("article_content", tag_name ).iterator().asScala.filter(x => ((x.getProperty("time_created").toString().toInt > t) ) ).toList.sortBy(-_.getProperty("time_created").toString().toInt)
//	           }

             //val cur_time = (System.currentTimeMillis()/1000).toInt
             val t1 = cur_time - (86400)
             val t2 = cur_time - (86400*3)
             var topic_arts = List[org.neo4j.graphdb.Node]()
             topic_arts = user_node.getRelationships("Favourite_Topic").asScala.map(_.getOtherNode(user_node)).toList.map( y => y.getRelationships("Belongs_To_Topic_Article").asScala.toList.map(_.getOtherNode(y))).flatten.distinct
             
             val one = friends_arts.intersect(topic_arts).filter(x => (x.getProperty("time_created").toString().toInt > t1)  ).sortBy(-_.getProperty("time_created").toString().toInt)
             val two = friends_arts.intersect(perso_arts).filter(x => (x.getProperty("time_created").toString().toInt > t1)  ).sortBy(-_.getProperty("time_created").toString().toInt)
             
             val three = friends_arts.intersect(topic_arts).filter(x => (x.getProperty("time_created").toString().toInt > t2)  ).sortBy(-_.getProperty("time_created").toString().toInt)
             val four = friends_arts.intersect(perso_arts).filter(x => (x.getProperty("time_created").toString().toInt > t2) ).sortBy(-_.getProperty("time_created").toString().toInt)
             
            // val five = (perso_arts:::friends_arts:::topic_arts).filter(x => (x.getProperty("time_created").toString().toInt > t2) ).sortBy(-_.getProperty("time_created").toString().toInt)
             
             val six = (perso_arts:::friends_arts:::topic_arts).sortBy(-_.getProperty("time_created").toString().toInt)
             
             total_arts = (one:::two:::three:::four:::six).distinct.filter( x => x.getProperty("space").toString.toInt == 0)
	         val sorted_news = total_arts.map(x => x.getProperty("article_id")).mkString(",")
	         
  
             val tile_node = user_tiles_index.get("id",user_node.getProperty("user_name").toString()).getSingle()
             tile_node.setProperty("news_Personalized",sorted_news)


             
           
            
          
        
        
        
      ret = true
      }
      
      
    ret
    }
  }
  
  def user_remove_hashfav(
      user_name:String,
      tag_name:String
      ):Boolean=
  {
    
    
    
    
    withTx {
    implicit neo =>
      
      var ret = false
      val t = (System.currentTimeMillis()/1000).toInt - (86400*2)
      val UserIndex = getNodeIndex("user").get
      val user_node = UserIndex.get("id",user_name).getSingle()  //Retrieving user node based on userid
      val HashIndex = getNodeIndex("sub_category").get
      val user_tiles_index = getNodeIndex("user_tiles").get
      val article_content_index = getNodeIndex("article_content").get
      if(user_node != null)
      {
        val user_favhash_rels =  user_node.getRelationships("Favourite_Hash").asScala
        val hash_list = tag_name.split(",").toList.map( x => x.toLowerCase())
        for(each <- hash_list)
        {
          val hash_node = HashIndex.get("name",each).getSingle()  //Retrieving user node based on userid
          if(hash_node != null)
          {
            for(e <- user_favhash_rels)
            {
              if(e.getOtherNode(user_node).equals(hash_node))
              {
                 e.delete()
                 //val nouns = user_node.getProperty("nouns").toString()
	             var perso_arts = List[org.neo4j.graphdb.Node]()
	             var friends_arts = List[org.neo4j.graphdb.Node]()
	             var hash_arts = List[org.neo4j.graphdb.Node]()
	             var total_arts = List[org.neo4j.graphdb.Node]()
	             
	             var votedup_hashtags = user_node.getRelationships("article_voteup,article_markfav").asScala.toList.map(x => x.getOtherNode(user_node)).map( y => y.getRelationships("Belongs_To_Subcategory_Article","Tag_Of_Article").asScala.toList.map( z => z.getOtherNode(y))).flatten
	             var viewed_hashtags = user_node.getRelationships("Viewed_By").asScala.toList.map(x => x.getOtherNode(user_node)).map( y => y.getRelationships("Belongs_To_Subcategory_Article","Tag_Of_Article").asScala.toList.map( z => z.getOtherNode(y))).flatten
	             
	             val tot_hash_map =  (votedup_hashtags:::votedup_hashtags:::viewed_hashtags).groupBy(x=>x).mapValues(x=>x.length)
	             perso_arts = tot_hash_map.keys.toList.sortBy(x => -tot_hash_map(x)).slice(0,25).map(y => y.getRelationships("Belongs_To_Subcategory_Article","Tag_Of_Article").asScala.toList.map( z => z.getOtherNode(y))).flatten.distinct.filter(x => x.getProperty("time_created").toString().toInt > t).sortBy(-_.getProperty("time_created").toString().toInt)
            
	       
	             val friends = user_node.getRelationships("Follows").asScala.map(_.getOtherNode(user_node)).toList
	             if(friends != Nil)
	             {
	               friends_arts = friends.map( x => x.getRelationships("Article_Written_By").asScala.map(_.getOtherNode(x)).toList).flatten.filter(y => ((y.getProperty("time_created").toString().toInt > t) ) )
	             }
	             
	             val hash_favs = user_node.getRelationships("Favourite_Hash").asScala.map(_.getOtherNode(user_node)).toList
	             if(hash_favs != Nil)
	             {
	               hash_arts = hash_favs.map( x => x.getRelationships("Belongs_To_Subcategory_Article","Tag_Of_Article").asScala.map(_.getOtherNode(x)).filter(y => (  (y.getProperty("time_created").toString().toInt > t)) ).toList).flatten.distinct
	               
	             }
	             
	             def intersperse[A](a : List[A], b : List[A]): List[A] = a match {
					  case first :: rest => first :: intersperse(b, rest)
					  case _             => b
				 }  
	             
	             total_arts = intersperse(perso_arts,intersperse(hash_arts,friends_arts)).distinct
	             val sorted_list = total_arts.sortBy(-_.getProperty("time_created").toString().toInt)
	             val sorted_news = sorted_list.map(x => x.getProperty("article_id")).mkString(",")

	             val tile_node = user_tiles_index.get("id",user_name).getSingle()
	             tile_node.setProperty("news_Personalized",sorted_news)

              }
            }
            
          }
          
             
        
        }
        
      ret = true
      }
      
      
    ret
    }
  }
  
  
  // Triggered when a user/non user views an Article/Event
  def user_view(
      user_name:String, // unique user name  ("" if the user is not a registered user)
      item_type:String, // A/E/P
      item_id:String, // article id/event id/Petition id
      time:Int
      ):Boolean=
  {
    
    withTx {
    implicit neo =>
   
    def matchTest(x: String): String = x match {
    case "A" => "Article"
    case "E" => "Event"
    case "a" => "Article"
    case "e" => "Event"
    case "P" => "Petition"
    case "p" => "Petition"
    }
     val i_type = matchTest(item_type)
    
     val grp_name = i_type.toLowerCase()
     val Index = getNodeIndex(grp_name).get  
     
     val item_node = Index.get("id",item_id).getSingle()  // Retrieving Item node based on item_id
     var ret = false
     if(item_node != null)
	 {
         val views = item_node.getProperty("views").toString().toInt

         // checking if the user is a registered user or not
	     if(user_name.equals(""))
	     {
	       // Adding the view time stamp to the list of view time stamps for non users
	       val t = (System.currentTimeMillis()/1000).toInt - (86400)
	       val latest_views = item_node.getProperty("latest_views").toString()
	       if(latest_views.equals(""))
	       {
	         item_node.setProperty("latest_views",time.toString())
	         
	       }
	       else
	       {
	         var list = latest_views.split(",").toList
	         list :+= time.toString()
	         item_node.setProperty("latest_views",list.mkString(","))
	         
	         
	         
	         
	       }
	       
	       ret = true
	     }
	     else
	     {
	         
		     val userIndex = getNodeIndex("user").get
		     val user_node = userIndex.get("id",user_name).getSingle() // Retrieving user node based on userid
		     var auth_node = user_node
		     
		     if(i_type.equals("Article"))
		     {
		       auth_node = item_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode()
		     }
		     else if(i_type.equals("Event"))
		     {
		       auth_node = item_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode()
		     }
		     else
		     {
		       if(item_node.hasRelationship("Petition_Written_By",Direction.OUTGOING))
		       {
		    	   auth_node = item_node.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode()
		       }
		       else
		       {
		           auth_node = null
		       }
		     }
		     
		     //checking if the viewer is author or not
		     if(user_node != null && !user_node.equals(auth_node))
		     {
		         user_node.setProperty("last_seen",time)
			     item_node.setProperty("views",views+1)
		         
			     val list = item_node.getRelationships("Viewed_By",Direction.OUTGOING).asScala.toList
			     // checking if the user already viewd the item or not
			     if(list.map(_.getOtherNode(item_node)).toList.contains(user_node))
			     {
			       
			       for(each <- list)
			       {
			         
			         if(each.getOtherNode(item_node).equals(user_node))
			         {
			           
			           val x = each.getProperty("count").toString().toInt
			           //creating a viewed thrice relation between user and the item if the view count is 3 
			           if(x == 3)
			           {
			             val rel: org.neo4j.graphdb.Relationship = user_node --> "Viewed_Thrice" --> item_node <
			             val x = rel.setProperty("time", time)
			           }
			           
			           // updating the view count
			           val y = each.setProperty("count",x+1)
			           val z = each.setProperty("time", time)
			         }
			       }
			     }
			     else
			     {
			       val rel: org.neo4j.graphdb.Relationship = item_node --> "Viewed_By" --> user_node <
			       val x = rel.setProperty("time", time)
			       val y = rel.setProperty("count", 1)

			       
			     }
			     ret = true
	        }
	         
	     }
	 }
     
     
    ret 
    }
  }
  
  // this function decreases the initial weight(weight gained by publishing items) gained by users if the item doesn't do well
  def negative_weights()
  {
    withTx {
    implicit neo =>
      
      
      val article_index = getNodeIndex("article").get
      val avg_wt_index = getNodeIndex("avg_weights").get
      val event_index = getNodeIndex("event").get
      val user_index = getNodeIndex("user").get
      val cur_time = (System.currentTimeMillis() /1000).toInt
      
      val art_avg = avg_wt_index.get("id","article").getSingle()
      val threshold = art_avg.getProperty("threshold_weight").toString().toInt
      val arts = article_index.query( "id", "*" ).iterator().asScala.toList.filter(x => ( ((cur_time - x.getProperty("time_created").toString().toInt) > 86400*3)  && ((cur_time - x.getProperty("time_created").toString().toInt) < 86400*4) ) && x.hasProperty("weight_review") && (x.getProperty("weight_review").toString().toInt == 0)   )
      
      val pass = arts.filter(x => (x.getProperty("views").toString().toFloat + x.getProperty("weight").toString().toFloat - x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getProperty("out_weight").toString().toInt ) >= threshold )
      val fail = arts.filterNot(x => pass.contains(x))
      
      for(each <- pass)
      {
        each.setProperty("weight_review",1)
      }
      
      for(each <- fail)
      {
        val user_weight_index = getNodeIndex("user_weight").get
        val art_weight_index = getNodeIndex("article_weight").get
        val rel = each.getSingleRelationship("Article_Written_By",Direction.OUTGOING)
        val user_node = rel.getEndNode()
        var u_wt = user_node.getProperty("weight").toString().toInt
        val in_wt = rel.getProperty("in_weight").toString().toInt
	    val out_wt = rel.getProperty("out_weight").toString().toInt
	    
	    u_wt = u_wt - in_wt
	    user_node.setProperty("weight",u_wt)
	    
	    var a_wt = each.getProperty("weight").toString().toInt
	    
	    a_wt = a_wt - out_wt
	    each.setProperty("weight",a_wt)
	    
	    user_weight_index.remove(user_node)
	    user_weight_index += (user_node,"weight",new ValueContext( u_wt ).indexNumeric())
	    
	    art_weight_index.remove(each)
	    art_weight_index += (each,"weight",new ValueContext( a_wt ).indexNumeric())
	    
	    rel.setProperty("in_weight", 0)
	    rel.setProperty("out_weight", 0)
	    
        each.setProperty("weight_review",2)
        
      }
      
    }
  }
  
  // This function runs in regular interval and updates the average weights
  def avg_weights()
  {
    
    
    
    
    withTx {
    implicit neo =>
      
      
      val article_index = getNodeIndex("article").get
      val event_index = getNodeIndex("event").get
      val user_index = getNodeIndex("user").get
      val avg_wt_index = getNodeIndex("avg_weights").get
      
      
      val arts = article_index.query( "id", "*" ).iterator().asScala.toList
      val art = arts.map(x => x.getProperty("weight").toString().toInt)
      
      var art_top_10 = 0
      var art_top_20 = 0
      var art_top_30 = 0
      if(art.size != 0)
      {
          val cur_time = (System.currentTimeMillis() /1000).toInt
          val t = cur_time - (86400 * 10)
          
          
    	  val art_avg = art.sum / art.size
    	  val rev_art = art.sorted.reverse
    	  val top_10_list = rev_art.slice(0,10)
    	  val top_20_list = rev_art.slice(0,20)
    	  val top_30_list = rev_art.slice(0,30)
    	  val threshold_list = arts.filter(x => x.hasProperty("weight_review") && x.getProperty("weight_review").toString().toInt == 1 && x.getProperty("time_created").toString().toInt > t).map( x => x.getRelationships("Viewed_By").asScala.toList.filter( y => y.getProperty("time").toString().toInt <= x.getProperty("time_created").toString().toInt + (86400*3)).size + x.getProperty("latest_views").toString().split(",").toList.filterNot(k => k.equals("")).map( y => y.toInt).filter( z => z <= x.getProperty("time_created").toString().toInt + (86400*3)).size + (x.getRelationships("article_voteup","article_markfav","Comment_To_Article").asScala.toList.filter(y => y.getProperty("time").toString().toInt <= x.getProperty("time_created").toString().toInt + (86400*3)).map(z => z.getProperty("in_weight").toString().toInt).sum) ).sortBy( x => x).slice(0,10)
    	  var threshold = 50
    	  if(threshold_list.size > 0)
    	  {
    	    val flag = threshold_list.sum / threshold_list.size
    	    if(flag > 50)
    	    {
    	      threshold = flag
    	    }
    	  }
    	  if(top_10_list.size > 0)
    	  {
    		  art_top_10 = top_10_list.sum/top_10_list.size
    	  
    		  art_top_20 = top_20_list.sum/top_20_list.size
          
    		  art_top_30 = top_30_list.sum/top_30_list.size
    	  }
    	  val art_node = avg_wt_index.get("id","article").getSingle()
    	  if(art_node != null)
    	  {
    	    if(!art_node.hasProperty("top_30_users"))
    	    {
    	      art_node.setProperty("top_30_users","")
    	    }
    	    
    	    if(!art_node.hasProperty("threshold_weight"))
    	    {
    	      art_node.setProperty("threshold_weight",threshold)
    	    }
    	    art_node.setProperty("average",art_avg)
    	    art_node.setProperty("total",art.size)
    	    art_node.setProperty("top",art.max)
    	    art_node.setProperty("top_10",art_top_10)
    	    art_node.setProperty("top_20",art_top_20)
    	    art_node.setProperty("top_30",art_top_30)
    	  }
    	  else
    	  {
    	    val art_node = createNode(avg_wt("article",art_avg,art.size,art.max,art_top_10,art_top_20,art_top_30,"",threshold))
    	    avg_wt_index += (art_node,"id","article")
    	  }
    	  
    	  
      }
      
      else if(avg_wt_index.get("id","article").getSingle() == null)
      {
    	    val art_node = createNode(avg_wt("article",0,0,0,0,0,0,"",0))
    	    avg_wt_index += (art_node,"id","article")
      }
      
      val events = event_index.query( "id", "*" ).iterator().asScala.toList
      val e = events.map(x => x.getProperty("weight").toString().toInt)
      
      var e_top_10 = 0
      var e_top_20 = 0
      var e_top_30 = 0
      if(e.size != 0)
      {
          
    	  val e_avg = e.sum / e.size
    	  val rev_e = e.sorted.reverse
    	  val top_10_list = rev_e.slice(0,10)
    	  val top_20_list = rev_e.slice(0,20)
    	  val top_30_list = rev_e.slice(0,30)
    	  if(top_10_list.size > 0)
    	  {
    		  e_top_10 = top_10_list.sum/top_10_list.size
    	  
    		  e_top_20 = top_20_list.sum/top_20_list.size
          
    		  e_top_30 = top_30_list.sum/top_30_list.size
    	  }
    	  val e_node = avg_wt_index.get("id","event").getSingle()
    	  if(e_node != null)
    	  {
    	    if(!e_node.hasProperty("top_30_users"))
    	    {
    	      e_node.setProperty("top_30_users","")
    	    }
    	    e_node.setProperty("average",e_avg)
    	    e_node.setProperty("total",e.size)
    	    e_node.setProperty("top",e.max)
    	    e_node.setProperty("top_10",e_top_10)
    	    e_node.setProperty("top_20",e_top_20)
    	    e_node.setProperty("top_30",e_top_30)
    	  }
    	  else
    	  {
    	    val e_node = createNode(avg_wt("event",e_avg,e.size,e.max,e_top_10,e_top_20,e_top_30,"",0))
    	    avg_wt_index += (e_node,"id","event")
    	  }
    	  
    	  
      }
      
      else if(avg_wt_index.get("id","event").getSingle() == null)
      {
    	    val e_node = createNode(avg_wt("event",0,0,0,0,0,0,"",0))
    	    avg_wt_index += (e_node,"id","event")
      }
      
      
      val uu = user_index.query( "id", "*" ).iterator().asScala.toList.sortBy(-_.getProperty("weight").toString().toInt)
      val u = uu.map(x => x.getProperty("weight").toString().toInt)
      var u_top_30_users = ""
      var u_top_10 = 0
      var u_top_20 = 0
      var u_top_30 = 0
      if(u.size != 0)
      {
          
    	  val u_avg = u.sum / u.size
    	  val top_10_list = u.slice(0,10)
    	  val top_20_list = u.slice(0,20)
    	  val top_30_list = u.slice(0,30)
    	  u_top_30_users = uu.map( x => x.getProperty("user_name").toString()).mkString(",")
    	  if(top_10_list.size > 0)
    	  {
    		  u_top_10 = top_10_list.sum/top_10_list.size
    	  
    		  u_top_20 = top_20_list.sum/top_20_list.size
         
    		  u_top_30 = top_30_list.sum/top_30_list.size
    	  }
    	  val u_node = avg_wt_index.get("id","user").getSingle()
    	  if(u_node != null)
    	  {
    	    if(!u_node.hasProperty("top_30_users"))
    	    {
    	      u_node.setProperty("top_30_users",u_top_30_users)
    	    }
    	    u_node.setProperty("average",u_avg)
    	    u_node.setProperty("total",u.size)
    	    u_node.setProperty("top",u.max)
    	    u_node.setProperty("top_10",u_top_10)
    	    u_node.setProperty("top_20",u_top_20)
    	    u_node.setProperty("top_30",u_top_30)
    	  }
    	  else
    	  {
    	    val u_node = createNode(avg_wt("user",u_avg,u.size,u.max,u_top_10,u_top_20,u_top_30,u_top_30_users,0))
    	    avg_wt_index += (u_node,"id","user")
    	  }
    	  
    	  
      }
      
      else if(avg_wt_index.get("id","user").getSingle() == null)
      {
    	    val u_node = createNode(avg_wt("user",0,0,0,0,0,0,"",0))
    	    avg_wt_index += (u_node,"id","user")
      }
      
      
      
    }
    println("Average weights updated")
  }
  

   
  // This function is used to trigger the functions update_tiles,avg_wt and etc in regular intervals and it runs in the background
  def sample_test()
  {
    
       
        val timer:Timer  = new Timer ()
		val hourlyTask:TimerTask = new TimerTask () {
		    @Override
		    def run () {
		        calc_views()
		    	update_tiles_temp()
		    	update_tiles_td()
//		    	avg_weights()
//		    	negative_weights()
		    	calc_hash_trends()
		    	nouns_update()
		    	calc_user_tiles()
		    	calc_local_tiles()
		    }
		};

		// schedule the task to run starting now and then 1 n half hour
		timer.schedule (hourlyTask, 0l, 1000*900)
  }

//  def add_articles()
//  {
//    
//   
//     withTx {
//      
//     implicit neo =>
//       
//       val tim = (System.currentTimeMillis() /1000).toInt
//       val index = getNodeIndex("article").get
//       val arts = index.query("id", "*" ).iterator().asScala.toList
//       for(each <- arts)
//       {
//         val user_name = each.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name").toString()
//         val id = each.getProperty("article_id").toString() 
//         val t_id = each.getProperty("article_title_id").toString() 
//         val t = each.getProperty("article_title").toString() 
//         val c = each.getProperty("article_content").toString() 
//         val s = each.getProperty("article_summary").toString()
//         val img = each.getProperty("article_featured_img").toString()
//         val cats = each.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(each)).map(y => y.getProperty("name")).filterNot(x => x.equals("all")).mkString(",")
//         val hts =  each.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.map(_.getOtherNode(each).getProperty("name").toString()).mkString(",")
//         for(i <- 1 to 10)
//         {
//           create_article(user_name,(id + i),(t_id + i),t,c,s,img,cats,hts,"","",tim,"","","",1,"",0)
//    
//         }
//       }
//       
//       
//     }
//  }
  
  
  //This function gives the list of existing Users, Articles, Events and etc
  def get_all_items(
      item_type: String, // User/Article/Event
      user_name: String,
      count: Int,
      prev_cnt: Int
      ): String =
  {
    
    
    def matchTest(x: String): String = x match {
	    case "A" => "Article"
	    case "E" => "Event"
	    case "P" => "Petition"
	    case "T" => "Townhall"
	    case "D" => "Debate"
	    case "AR" => "Article"
	    case "EV" => "Event"
	    case "PE" => "Petition"
	    case "TO" => "Townhall"
	    case "DE" => "Debate"
	    case "C" => "Comment"
	    case "U" => "User"
	    case "H" => "Sub_category"
	    case "a" => "Article"
	    case "e" => "Event"
	    case "p" => "Petition"
	    case "t" => "Townhall"
	    case "d" => "Debate"
	    case "ar" => "Article"
	    case "ev" => "Event"
	    case "pe" => "Petition"
	    case "to" => "Townhall"
	    case "de" => "Debate"
	    case "c" => "Comment"
	    case "u" => "User"
	    case "h" => "Sub_category"
	    }
    val i_type = matchTest(item_type)
    val t = (System.currentTimeMillis() /1000).toInt
    val cur_time = (System.currentTimeMillis() /1000).toInt
    val index_name = i_type.toLowerCase()
    val index = getNodeIndex(index_name).get
    val UserIndex = getNodeIndex("user").get
    var user_node = UserIndex.get("id",user_name).getSingle()
//    val f = index.query("id", "*" ).iterator().asScala.toList.map( x => x.getProperty("article_id").toString())
//    for(ech <- f)
//    {
//      println(get_article_data(ech))
//    }
    var ret = List[Any]()
    val l = List("Comment_Count_Unique","ev","v_users","votes","Commented_Users","Comment_Count","Is_Neo4j","P_Pin","P_Id","P_Author","P_Author_FullName","P_IsMarkedReadLater","P_Title","P_Title_ID","P_Category","P_SubCategory","P_Num_Comments","P_Feature_Image","P_Smry", "P_TimeCreated", "P_EventLocation", "P_EventStartTime", "P_EventAttendStatus", "P_SignsRequired", "P_PetitionSignStatus", "Space_Title", "Space_TitleId")
		val l2 = List("FN","UN")
        val l3 = List("Name","UName")
    // extracting the details of all the items based on the item group specified
    if(item_type.toLowerCase().equals("u"))
      ret = index.query("id", "*" ).iterator().asScala.toList.map(x => JSONArray(List(x.getProperty("user_name"),x.getProperty("weight"))))
    else if(item_type.toLowerCase().equals("a")){
      ret = index.query("id", "*" ).iterator().asScala.toList
    		  .map(x => JSONArray(
    				  List(
    				      x.getProperty("article_title"),
    				      x.getProperty("weight"),
    				      x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("user_name"),
    				      x.getProperty("stars"),
    				      x.getRelationships("Article_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Article_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				    
    				      )))
    }else if(item_type.toLowerCase().equals("ar")){
      ret = index.query("id", "*" ).iterator().asScala.toList
    		  .filter(x => x.getRelationships("Belongs_To_Category").asScala.toList
    		      .map(_.getOtherNode(x))
    		      .map(y => y.getProperty("name"))
    		      .filterNot(x => x.equals("all")).size > 0)
    		      .filter( x => x.getProperty("space").toString.toInt == 0)
    		      .sortBy(-_.getProperty("time_created").toString().toInt)
    		      .slice(prev_cnt,(prev_cnt+count))
    		      .map(x => JSONObject(l.zip(
    		          List(
    		              x.getRelationships("Comment_To_Article").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,
    		              0,
    		              JSONArray(x.getRelationships("article_voteup").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),
    		              x.getRelationships("article_voteup").asScala.toList.size-x.getRelationships("article_voteup").asScala.toList.slice(0,2).size,
    		              JSONArray(x.getRelationships("Comment_To_Article").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))), 
    		              x.getRelationships("Comment_To_Article").asScala.size,
    		              true,
    		              false,
    		              x.getProperty("article_id"),
    		              x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),
    		              x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),
    		              x.getRelationships("article_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),
    		              x.getProperty("article_title"),
    		              x.getProperty("article_title_id"),
    		              x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),
    		              x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),
    		              x.getRelationships("Comment_To_Article").asScala.size,x.getProperty("article_featured_img").toString(),
    		              x.getProperty("article_summary").toString(),
    		              x.getProperty("time_created").toString(),
    		              "",
    		              "",
    		              false,
    		              "",
    		              false,
    		              x.getRelationships("Article_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Article_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				    
    				      )).toMap))
    }else if(item_type.toLowerCase().equals("s"))
      ret = index.query("id", "*" ).iterator().asScala.toList.map(x => JSONArray(List(x.getRelationships("Article_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(y => (y.getProperty("article_title_id") + ":" + y.getProperty("space"))  ).mkString(","),x.getRelationships("Event_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(y => (y.getProperty("event_title_id") + ":" + y.getProperty("space"))  ).mkString(","),x.getRelationships("Petition_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(y => (y.getProperty("p_title_id") + ":" + y.getProperty("space"))  ).mkString(","),x.getRelationships("Townhall_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(y => (y.getProperty("t_title_id") + ":" + y.getProperty("space"))  ).mkString(","),x.getRelationships("Debate_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(y => (y.getProperty("d_title_id") + ":" + y.getProperty("space"))  ).mkString(","),x.getProperty("closed"),x.getProperty("space_title"),x.getProperty("space_id"),x.getSingleRelationship("Space_Created_By",Direction.OUTGOING).getOtherNode(x).getProperty("user_name"))))
    else if(item_type.toLowerCase().equals("ev"))
    {
      if(user_node != null && user_node.hasRelationship("Belongs_To_Location"))
      {
            var loc_events = user_node.getSingleRelationship("Belongs_To_Location",Direction.OUTGOING).getOtherNode(user_node).getRelationships("Belongs_To_Location_Event").asScala.toList.map(_.getStartNode()).filter(x => (x.getProperty("event_date_time_closing").toString().toInt > t)  ).sortBy(_.getProperty("event_date_time").toString().toInt)
	        ret = (loc_events ::: index.query("id", "*" ).iterator().asScala.toList.sortBy(-_.getProperty("event_date_time_closing").toString().toInt))
	        		.distinct
	        		.filter(x => x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all")).size > 0)
	        		.filter( x => x.getProperty("space").toString.toInt == 0)
	        		.slice(prev_cnt,(prev_cnt+count))
	        		.map(x => JSONObject(l.zip(
	        		    List(
	        		        x.getRelationships("Comment_To_Event").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,
	        		        1,
	        		        JSONArray(x.getRelationships("Is_Attending").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),
	        		        x.getRelationships("Is_Attending").asScala.toList.size-x.getRelationships("Is_Attending").asScala.toList.slice(0,2).size,
	        		        JSONArray(x.getRelationships("Comment_To_Event").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),
	        		        x.getRelationships("Comment_To_Event").asScala.size,
	        		        true,
	        		        false,
	        		        x.getProperty("event_id"),
	        		        x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),
	        		        x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),
	        		        x.getRelationships("event_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),
	        		        x.getProperty("event_title"),
	        		        x.getProperty("event_title_id"),
	        		        x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),
	        		        x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),
	        		        x.getRelationships("Comment_To_Event").asScala.size,
	        		        x.getProperty("event_featured_img").toString(),
	        		        x.getProperty("event_summary").toString(),
	        		        x.getProperty("time_created").toString(),
	        		        x.getProperty("event_location").toString(),
	        		        x.getProperty("event_date_time").toString(),
	        		        x.getRelationships("Is_Attending").asScala.map(_.getOtherNode(x)).toList.contains(user_node),
	        		        "", 
	        		        false,
    				      x.getRelationships("Event_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Event_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				    )).toMap))
      }
      else
      {
            ret = index.query("id", "*" ).iterator().asScala.toList.filter(x => x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all")).size > 0).filter( x => x.getProperty("space").toString.toInt == 0).sortBy(-_.getProperty("event_date_time_closing").toString().toInt).slice(prev_cnt,(prev_cnt+count)).map(x => JSONObject(l.zip(List(x.getRelationships("Comment_To_Event").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,1,JSONArray(x.getRelationships("Is_Attending").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Is_Attending").asScala.toList.size-x.getRelationships("Is_Attending").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Event").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Event").asScala.size,true,false,x.getProperty("event_id"),x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("event_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Event").asScala.size,x.getProperty("event_featured_img").toString(),x.getProperty("event_summary").toString(),x.getProperty("time_created").toString(),x.getProperty("event_location").toString(),x.getProperty("event_date_time").toString(),x.getRelationships("Is_Attending").asScala.map(_.getOtherNode(x)).toList.contains(user_node),
                "",
                false,
                x.getRelationships("Event_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    
                x.getRelationships("Event_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    			
            )).toMap))
      }
    }
    else if(item_type.toLowerCase().equals("h"))
      ret = index.query("name", "*" ).iterator().asScala.toList.map(x => JSONArray(List(x.getProperty("name"),x.getProperty("weight"),JSONArray(x.getRelationships("Belongs_To_Subcategory_Article","Belongs_To_Subcategory_Event","Tag_Of_Article","Tag_Of_Event").asScala.toList.map(_.getOtherNode(x)).filter(x => x.getProperty("__CLASS__").equals("Saddahaq.article")).map(x => x.getProperty("article_title_id"))),JSONArray(x.getRelationships("Belongs_To_Subcategory_Article","Belongs_To_Subcategory_Event","Tag_Of_Article","Tag_Of_Event").asScala.toList.map(_.getOtherNode(x)).filter(x => x.getProperty("__CLASS__").equals("Saddahaq.event")).map(x => x.getProperty("event_title_id"))))))
    else if(item_type.toLowerCase().equals("c"))
      ret = index.query("id", "*" ).iterator().asScala.toList.map(x => JSONArray(List(x.getProperty("comment_content"),x.getProperty("weight"),x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("user_name"))))
    else if(item_type.toLowerCase().equals("pe")){
      ret = index.query("id", "*" ).iterator().asScala.toList.filter( x => x.hasRelationship("Petition_Written_By",Direction.OUTGOING) && x.getProperty("space").toString.toInt == 0).sortBy(-_.getProperty("time_created").toString().toInt).slice(prev_cnt,(prev_cnt+count)).map(x => JSONObject(l.zip
    		  (List(x.getRelationships("Comment_To_Petition").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,2,JSONArray(x.getRelationships("Signed_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Signed_Petition").asScala.toList.size-x.getRelationships("Signed_Petition").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Petition").asScala.size,true,false,x.getProperty("p_id"),x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("petition_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("p_title"),x.getProperty("p_title_id"),x.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Petition").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Petition").asScala.size,x.getProperty("p_img_url").toString(),x.getProperty("p_content").toString(),x.getProperty("time_created").toString(),"","",false,x.getProperty("p_target").toString().toInt - x.getProperty("p_count").toString().toInt,
    		      x.getRelationships("Signed_Petition").asScala.map(_.getOtherNode(x)).toList.contains(user_node),
    			x.getRelationships("Petition_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Petition_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				      )).toMap))
    }else if(item_type.toLowerCase().equals("de")){
      ret = index.query("id", "*" ).iterator().asScala.toList.filter( x => x.getProperty("space").toString.toInt == 0).sortBy(-_.getProperty("d_date").toString().toInt).slice(prev_cnt,(prev_cnt+count)).map(x => JSONObject(l.zip(
          List(
              x.getRelationships("Commented_On_Debate").asScala.toList.map(y=>y.getOtherNode(x)).distinct.size,
              4,
              JSONArray(x.getRelationships("Asked_Debate_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Asked_Debate_Question").asScala.toList.size-x.getRelationships("Asked_Debate_Question").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Commented_On_Debate").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x)).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Commented_On_Debate").asScala.size,true,false,x.getProperty("d_id"),x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("debate_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("d_title"),x.getProperty("d_title_id"),"",x.getRelationships("Belongs_To_Subcategory_Debate").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Commented_On_Debate").asScala.size,x.getProperty("d_img_url").toString(),x.getProperty("d_content").toString(),x.getProperty("time_created").toString(),"","",false,
              "",
              false,
              x.getRelationships("Debate_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Debate_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				    )).toMap))
    }else if(item_type.toLowerCase().equals("to")){
      ret = index.query("id", "*" ).iterator().asScala.toList.filter( x => x.getProperty("space").toString.toInt == 0).sortBy(-_.getProperty("t_date").toString().toInt).slice(prev_cnt,(prev_cnt+count)).map(x => JSONObject(l.zip(List(x.getRelationships("Commented_On_Townhall").asScala.toList.map(y=>y.getOtherNode(x)).distinct.size,3,JSONArray(x.getRelationships("Asked_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Asked_Question").asScala.toList.size-x.getRelationships("Asked_Question").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Commented_On_Townhall").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x)).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Commented_On_Townhall").asScala.size,true,false,x.getProperty("t_id"),x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("townhall_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("t_title"),x.getProperty("t_title_id"),"",x.getRelationships("Belongs_To_Subcategory_Townhall").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Commented_On_Townhall").asScala.size,x.getProperty("t_img_url").toString(),x.getProperty("t_content").toString(),x.getProperty("time_created").toString(),"","",
          false,
          "",
          false,
          x.getRelationships("Townhall_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Townhall_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				     )).toMap))
    }else if(item_type.toLowerCase().equals("e")){
      ret = index.query("id", "*" ).iterator().asScala.toList.map(x => JSONArray(
          List(
              x.getProperty("event_title"),
              (((x.getProperty("views").toString().toFloat)+(x.getProperty("weight").toString().toFloat*10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90)),x.getRelationships("Is_Attending").asScala.toList.map(_.getOtherNode(x).getProperty("user_name")).mkString(","),
              x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(x).getProperty("user_name"),
          x.getRelationships("Event_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Event_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				    	          
          )))
    }else if(item_type.toLowerCase().equals("p")){
      ret = index.query("id", "*" ).iterator().asScala.toList.map(x => JSONArray(
          List(
              x.getProperty("p_title"),
              x.getProperty("weight"),
              x.getRelationships("Signed_Petition").asScala.toList.map(_.getOtherNode(x).getProperty("user_name")).mkString(","),
          x.getRelationships("Petition_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Petition_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				    	      )))
    }else if(item_type.toLowerCase().equals("d")){
      ret = index.query("id", "*" ).iterator().asScala.toList.map(x => JSONArray(
    		  List(
    		      x.getProperty("d_title"),
    		      x.getProperty("weight"),
    		      x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("user_name"),
    		   x.getRelationships("Debate_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Debate_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				         )))
    }else if(item_type.toLowerCase().equals("t")){
      ret = index.query("id", "*" ).iterator().asScala.toList.map(x => JSONArray(
          List(
              x.getProperty("t_title"),
              0,
              x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("user_name"),
             x.getRelationships("Townhall_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Townhall_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				         )))
    }
    JSONArray(ret).toString()
  }
  
  
  // this function gives all the details of a user whose user name is given
  def get_user_data(user_name: String):String=
  {
    
    val user_index = getNodeIndex("user").get
    val art_index = getNodeIndex("article").get
    val user_tiles_index = getNodeIndex("user_tiles").get
    
    
             
    val user_node = user_index.get("id",user_name).getSingle()
    var ret = ""
    val l = List("name","feed","topics","wt","follows","followers","arts","events","petitions","hash_tags","location","p_arts","l_arts","hashtags","google","fb","twitter")
    if(user_node != null)
    {
      
//            println(user_name)
            var google = false
            var fb = false
            var twitter = false
            
            if(user_node.hasProperty("friends"))
	        {
//                 println(user_name)
                 val friends = user_node.getProperty("friends").toString
		         var list = List[String]()
		         list = JSON.parseFull(friends).get.asInstanceOf[List[String]]
		         if(!list(0).equals(""))
		         {
		           fb = true
		         }
		         if(!list(1).equals(""))
		         {
		           twitter = true
		         }
		         if(!list(2).equals(""))
		         {
		           google = true
		         }
	        }
            // extracting the favorite hashtags of the user
            var votedup_hashtags = user_node.getRelationships("article_voteup,article_markfav").asScala.toList.map(x => x.getOtherNode(user_node)).map( y => y.getRelationships("Belongs_To_Subcategory_Article","Tag_Of_Article").asScala.toList.map( z => z.getOtherNode(y))).flatten
            var viewed_hashtags = user_node.getRelationships("Viewed_By").asScala.toList.map(x => x.getOtherNode(user_node)).map( y => y.getRelationships("Belongs_To_Subcategory_Article","Tag_Of_Article").asScala.toList.map( z => z.getOtherNode(y))).flatten
            
            val tot_hash_map =  (votedup_hashtags:::votedup_hashtags:::viewed_hashtags).groupBy(x=>x).mapValues(x=>x.length)
            val hash_tags = tot_hash_map.keys.toList.sortBy(x => -tot_hash_map(x)).slice(0,25).map(x => x.getProperty("name").toString())
            var tiles_loc = List[String]()
            var tiles_perso = List[String]()
            
            // extracting the location based tiles of the user
            if(user_node.hasRelationship("Belongs_To_Location",Direction.OUTGOING))
            {
              val loc = user_node.getSingleRelationship("Belongs_To_Location",Direction.OUTGOING).getOtherNode(user_node)
              val loc_tiles = loc.getProperty("tiles").toString()
              if(!loc_tiles.equals(""))
              {
                tiles_loc = loc_tiles.split(",").toList.map( x => art_index.get("id",x).getSingle().getProperty("article_title_id").toString())
              }
            }
            
            // extracting the personalized tiles of the user
            val tile_node = user_tiles_index.get("id",user_name).getSingle()
            
            val perso_tiles = tile_node.getProperty("news_Personalized").toString
//            println(perso_tiles)
            if(!perso_tiles.equals(""))
            {
                val x1 = perso_tiles.split(",").toList
                for(each <- x1)
                {
//                  println(each)
//                  println(art_index.get("id",each).getSingle())
                }
                tiles_perso = perso_tiles.split(",").toList.map( x => art_index.get("id",x).getSingle()).filter( x => x != null).map( x => x.getProperty("article_title_id").toString())
            }
            
            // adding all the extrated details to a map and converting it into json
            ret = JSONObject(l.zip(List(user_node.getProperty("user_name"),user_node.getProperty("feed_subscription"),JSONArray(user_node.getRelationships("Favourite_Topic").asScala.toList.map(_.getEndNode().getProperty("name").toString())),user_node.getProperty("weight"),user_node.getRelationships("Follows",Direction.OUTGOING).asScala.toList.map(_.getOtherNode(user_node).getProperty("user_name")).mkString(","),user_node.getRelationships("Follows",Direction.INCOMING).asScala.toList.map(_.getOtherNode(user_node).getProperty("user_name")).mkString(","),JSONArray(user_node.getRelationships("Article_Written_By").asScala.toList.map(_.getOtherNode(user_node).getProperty("article_title_id"))),JSONArray(user_node.getRelationships("Event_Created_By").asScala.toList.map(_.getOtherNode(user_node).getProperty("event_title_id"))),JSONArray(user_node.getRelationships("Petition_Written_By").asScala.toList.map(_.getOtherNode(user_node).getProperty("p_title_id"))),JSONArray(hash_tags),user_node.getProperty("location").toString(),JSONArray(tiles_perso),JSONArray(tiles_loc),user_node.getRelationships("Hashtag_Created_By").asScala.toList.map(_.getOtherNode(user_node).getProperty("name").toString()).mkString(","),google,fb,twitter)).toMap).toString()
			          
    }
    
    ret
  }
  
  def get_item_data(item_type: String, item_id: String):String=
  {
    
    // val art_index = getNodeIndex("user").get
    
    //val user_tiles_index = getNodeIndex("user_tiles").get
    var ret = ""
    if(item_type.toLowerCase().equals("a"))
    {
	    val art_index = getNodeIndex("article").get         
	    val a_node = art_index.get("id",item_id).getSingle()
	    
	    val l = List("a_feu","a_title","a_content","a_weight","a_cat","a_ht","a_author","a_rank","a_relweight","a_vu","a_mf","a_noc","a_loc","a_age","a_top")
	    if(a_node != null)
	    {
	            
	            val cur_time = (System.currentTimeMillis() /1000).toInt
	            val t = cur_time - 86400
	            val TilesIndex = getNodeIndex("tiles").get
	            val news_node = TilesIndex.get("id","all").getSingle()
	            val news = news_node.getProperty("value").toString().split(",").toList
	            val rank = news.indexOf(item_id) + 1
	            var locations = ""
	            val age = ((cur_time-a_node.getProperty("time_created").toString().toInt)/3600)+1 
	            
	            val rel_wt = (((  (a_node.getProperty("views").toString().toFloat) + 1)+(  (a_node.getRelationships("article_voteup","article_markfav","Comment_To_Article").asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).map(x => x.getProperty("in_weight").toString().toInt).sum  ) *10))/ ((age)*90))
	            
	            if(a_node.hasRelationship("Belongs_To_Location_Article",Direction.OUTGOING))
	            {
	              locations = a_node.getRelationships("Belongs_To_Location_Article",Direction.OUTGOING).asScala.toList.map(_.getOtherNode(a_node).getProperty("location_id").toString()).mkString(",")
	            }
	            // adding all the extrated details to a map and converting it into json
	            ret = JSONObject(l.zip(List(a_node.getProperty("approved"),a_node.getProperty("article_title"),a_node.getProperty("article_content"),a_node.getProperty("weight"),a_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(a_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all")).mkString(","),a_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.map(_.getOtherNode(a_node).getProperty("name").toString()).mkString(","),a_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(a_node).getProperty("user_name"),rank,rel_wt,a_node.getRelationships("article_voteup").asScala.size,a_node.getRelationships("article_markfav").asScala.size,a_node.getRelationships("Comment_To_Article").asScala.size,locations,age,a_node.getRelationships("Belongs_To_Topic").asScala.toList.map(_.getOtherNode(a_node)).map(y => y.getProperty("name")).mkString(","))).toMap).toString()
	    }      
    }
    
    else if(item_type.toLowerCase().equals("s"))
    {
	    val space_index = getNodeIndex("space").get         
	    val s_node = space_index.get("id",item_id).getSingle()
	    
	    val l = List("s_isclosed","s_title","s_tagline","s_author","s_admins","s_followers","s_articles","s_events","s_petitions","s_townhalls","s_debates")
	    if(s_node != null)
	    {
	            
	            
	            // adding all the extrated details to a map and converting it into json
	            ret = JSONObject(l.zip(List(s_node.getProperty("closed"),s_node.getProperty("space_title"),s_node.getProperty("space_tagline"),s_node.getSingleRelationship("Space_Created_By",Direction.OUTGOING).getOtherNode(s_node).getProperty("user_name"),s_node.getRelationships("Admin_Of_Space").asScala.toList.map(_.getOtherNode(s_node)).map(y => y.getProperty("user_name")).mkString(","),s_node.getRelationships("Space_Followed_By").asScala.toList.map(_.getOtherNode(s_node)).map(y => y.getProperty("user_name")).mkString(","),s_node.getRelationships("Article_Tagged_To_Space").asScala.toList.map(_.getOtherNode(s_node)).map(y => (y.getProperty("article_title_id") + ":" + y.getProperty("space"))  ).mkString(","),s_node.getRelationships("Event_Tagged_To_Space").asScala.toList.map(_.getOtherNode(s_node)).map(y => (y.getProperty("event_title_id") + ":" + y.getProperty("space"))  ).mkString(","),s_node.getRelationships("Petition_Tagged_To_Space").asScala.toList.map(_.getOtherNode(s_node)).map(y => (y.getProperty("p_title_id") + ":" + y.getProperty("space"))  ).mkString(","),s_node.getRelationships("Townhall_Tagged_To_Space").asScala.toList.map(_.getOtherNode(s_node)).map(y => (y.getProperty("t_title_id") + ":" + y.getProperty("space"))  ).mkString(","),s_node.getRelationships("Debate_Tagged_To_Space").asScala.toList.map(_.getOtherNode(s_node)).map(y => (y.getProperty("d_title_id") + ":" + y.getProperty("space"))  ).mkString(","))).toMap).toString()
	    }      
    }
    
    else if(item_type.toLowerCase().equals("t"))
    {
        val t_index = getNodeIndex("townhall").get         
	    val t_node = t_index.get("id",item_id).getSingle()
	    
	    val l = List("t_title","t_content","t_author","t_celeb","t_asked_qtn","t_participate","t_voted_qtn","t_voted_ans","t_coms")
	    
	    if(t_node != null)
	    {
	          val t1 = t_node.getRelationships("Asked_Question").asScala.toList.map(_.getOtherNode(t_node).getProperty("user_name").toString()).distinct.mkString(",")
	          val t2 = t_node.getRelationships("Participated_In_Townhall").asScala.toList.map(_.getOtherNode(t_node).getProperty("user_name").toString()).distinct.mkString(",")
	          
	          val t3 = t_node.getRelationships("Voted_Townhall_Question").asScala.toList.map(_.getOtherNode(t_node).getProperty("user_name").toString()).distinct.mkString(",")
	          val t4 = t_node.getRelationships("Voted_Townhall_Answer").asScala.toList.map(_.getOtherNode(t_node).getProperty("user_name").toString()).distinct.mkString(",")
	          val t5 = t_node.getRelationships("Commented_On_Townhall").asScala.toList.map(_.getOtherNode(t_node).getProperty("user_name").toString()).distinct.mkString(",")
	            
	          ret = JSONObject(l.zip(List(   t_node.getProperty("t_title"),t_node.getProperty("t_content"),t_node.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getOtherNode(t_node).getProperty("user_name"),t_node.getSingleRelationship("Townhall_Of",Direction.OUTGOING).getOtherNode(t_node).getProperty("user_name"), t1,t2,t3,t4,t5   )).toMap).toString()

	    }
    }
    
    else if(item_type.toLowerCase().equals("p"))
    {
        val p_index = getNodeIndex("petition").get         
	    val p_node = p_index.get("id",item_id).getSingle()
	    
	    val l = List("p_feu","p_title","p_content","p_author","p_cat","p_ht","p_signed","p_count","p_target","p_signed")
	    
	    if(p_node != null)
	    {
	          val t1 = p_node.getRelationships("Signed_Petition").asScala.toList.map(_.getOtherNode(p_node).getProperty("user_name").toString()).distinct.mkString(",")
//	          val t2 = t_node.getRelationships("Participated_In_Townhall").asScala.toList.map(_.getOtherNode(t_node).getProperty("user_name").toString()).distinct.mkString(",")
//	          
//	          val t3 = t_node.getRelationships("Voted_Townhall_Question").asScala.toList.map(_.getOtherNode(t_node).getProperty("user_name").toString()).distinct.mkString(",")
//	          val t4 = t_node.getRelationships("Voted_Townhall_Answer").asScala.toList.map(_.getOtherNode(t_node).getProperty("user_name").toString()).distinct.mkString(",")
//	          val t5 = t_node.getRelationships("Commented_On_Townhall").asScala.toList.map(_.getOtherNode(t_node).getProperty("user_name").toString()).distinct.mkString(",")
	            
	          ret = JSONObject(l.zip(List(   p_node.getProperty("approved"),p_node.getProperty("p_title"),p_node.getProperty("p_content"),p_node.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getOtherNode(p_node).getProperty("user_name"),p_node.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(p_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all")).mkString(","),p_node.getRelationships("Belongs_To_Subcategory_Petition").asScala.toList.map(_.getOtherNode(p_node).getProperty("name").toString()).mkString(","),p_node.getProperty("p_count"),p_node.getProperty("p_target"),t1   )).toMap).toString()

	    }
    }
    
    else
    {
        val d_index = getNodeIndex("debate").get         
	    val d_node = d_index.get("id",item_id).getSingle()
	    
	    val l = List("d_title","d_content","d_author","d_for","d_against","d_asked_qtn","d_participate","d_started_arg","d_coms")
	    
	    if(d_node != null)
	    {
	      
	          val f = d_node.getRelationships("For").asScala.toList.filter(x => x.getProperty("d_status").toString().toInt == 1).map(_.getOtherNode(d_node).getProperty("user_name").toString()).distinct.mkString(",")
	          val a = d_node.getRelationships("Against").asScala.toList.filter(x => x.getProperty("d_status").toString().toInt == 1).map(_.getOtherNode(d_node).getProperty("user_name").toString()).distinct.mkString(",")
	          
	          val t1 = d_node.getRelationships("Asked_Debate_Question").asScala.toList.map(_.getOtherNode(d_node).getProperty("user_name").toString()).distinct.mkString(",")
	          val t2 = d_node.getRelationships("Participated_In_Debate").asScala.toList.map(_.getOtherNode(d_node).getProperty("user_name").toString()).distinct.mkString(",")
	          
	          val t3 = d_node.getRelationships("Started_Debate_Argument").asScala.toList.map(_.getOtherNode(d_node).getProperty("user_name").toString()).distinct.mkString(",")
	          //val t4 = t_node.getRelationships("Voted_Townhall_Answer").asScala.toList.map(_.getOtherNode(t_node).getProperty("user_name").toString()).distinct.mkString(",")
	          val t4 = d_node.getRelationships("Commented_On_Debate").asScala.toList.map(_.getOtherNode(d_node).getProperty("user_name").toString()).distinct.mkString(",")
	            
	          ret = JSONObject(l.zip(List(   d_node.getProperty("d_title"),d_node.getProperty("d_content"),d_node.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getOtherNode(d_node).getProperty("user_name"),f,a, t1,t2,t3,t4   )).toMap).toString()

	    }
    }
    
    ret
  }
  
  def get_monthly_items(
      ): String =
  {
    
    
        var big = List[Any]()
    
        // finding the start and end time of the present day
        val cur_time = (System.currentTimeMillis() /1000).toInt
        val rightNow : Calendar= Calendar.getInstance();
        val date:Date = new Date();
        // offset to add since we're not UTC
        val offset: Long = rightNow.get(Calendar.ZONE_OFFSET) +
        rightNow.get(Calendar.DST_OFFSET);
        val since: Int = ((rightNow.getTimeInMillis() + offset) % (24 * 60 * 60 * 1000)).toInt/1000;
        val till: Int = (24*3600) - since
     
        var t1 = (System.currentTimeMillis()/1000).toInt - since
        var t2 = (System.currentTimeMillis()/1000).toInt + till  
       
	    val art_index = getNodeIndex("article").get
	    val arts = art_index.query("id", "*" ).iterator().asScala.toList
	    val event_index = getNodeIndex("event").get
	    val events = event_index.query("id", "*" ).iterator().asScala.toList
	    
	    val z:Float = 0
	    var small = List[Float]()
	    
	    // finding the total number of articles published on a given day for each category
	    var pol = arts.filter(x => (x.getProperty("time_created").toString().toInt > t1) && (x.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.contains("politics"))  )
	    var pol1 = pol.map( x => 1/(x.getRelationships("Belongs_To_Category").asScala.toList.size - 1).asInstanceOf[Float] ).sum
	    
	    // adding the number to a small list
	    if(pol1 > 0)
	    small :+= pol1
	    else
	    small :+= z
	    
	    // finding the total number of events published on a given day for each category
	    pol = events.filter(x => (x.getProperty("time_created").toString().toInt > t1) && (x.getRelationships("Belongs_To_Event_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.contains("politics"))  )
	    pol1 = pol.map( x => 1/(x.getRelationships("Belongs_To_Event_Category").asScala.toList.size - 1).asInstanceOf[Float] ).sum
	    
	    // adding the number to a small list
	    if(pol1 > 0)
	    small :+= pol1
	    else
	    small :+= z
	    
	    pol = arts.filter(x => (x.getProperty("time_created").toString().toInt > t1) && (x.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.contains("technology"))  )
	    pol1 = pol.map( x => 1/(x.getRelationships("Belongs_To_Category").asScala.toList.size - 1).asInstanceOf[Float] ).sum
	    if(pol1 > 0)
	    small :+= pol1
	    else
	    small :+= z
	    
	    pol = events.filter(x => (x.getProperty("time_created").toString().toInt > t1) && (x.getRelationships("Belongs_To_Event_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.contains("technology"))  )
	    pol1 = pol.map( x => 1/(x.getRelationships("Belongs_To_Event_Category").asScala.toList.size - 1).asInstanceOf[Float] ).sum
	    if(pol1 > 0)
	    small :+= pol1
	    else
	    small :+= z
	    
	    pol = arts.filter(x => (x.getProperty("time_created").toString().toInt > t1) && (x.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.contains("sports"))  )
	    pol1 = pol.map( x => 1/(x.getRelationships("Belongs_To_Category").asScala.toList.size - 1).asInstanceOf[Float] ).sum
	    if(pol1 > 0)
	    small :+= pol1
	    else
	    small :+= z
	    
	    pol = events.filter(x => (x.getProperty("time_created").toString().toInt > t1) && (x.getRelationships("Belongs_To_Event_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.contains("sports"))  )
	    pol1 = pol.map( x => 1/(x.getRelationships("Belongs_To_Event_Category").asScala.toList.size - 1).asInstanceOf[Float] ).sum
	    if(pol1 > 0)
	    small :+= pol1
	    else
	    small :+= z
	    
	    pol = arts.filter(x => (x.getProperty("time_created").toString().toInt > t1) && (x.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.contains("entertainment"))  )
	    pol1 = pol.map( x => 1/(x.getRelationships("Belongs_To_Category").asScala.toList.size - 1).asInstanceOf[Float] ).sum
	    if(pol1 > 0)
	    small :+= pol1
	    else
	    small :+= z
	    
	    pol = events.filter(x => (x.getProperty("time_created").toString().toInt > t1) && (x.getRelationships("Belongs_To_Event_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.contains("entertainment"))  )
	    pol1 = pol.map( x => 1/(x.getRelationships("Belongs_To_Event_Category").asScala.toList.size - 1).asInstanceOf[Float] ).sum
	    if(pol1 > 0)
	    small :+= pol1
	    else
	    small :+= z
	    
	    pol = arts.filter(x => (x.getProperty("time_created").toString().toInt > t1) && (x.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.contains("humaninterest"))  )
	    pol1 = pol.map( x => 1/(x.getRelationships("Belongs_To_Category").asScala.toList.size - 1).asInstanceOf[Float] ).sum
	    if(pol1 > 0)
	    small :+= pol1
	    else
	    small :+= z
	    
	    pol = events.filter(x => (x.getProperty("time_created").toString().toInt > t1) && (x.getRelationships("Belongs_To_Event_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.contains("humaninterest"))  )
	    pol1 = pol.map( x => 1/(x.getRelationships("Belongs_To_Event_Category").asScala.toList.size - 1).asInstanceOf[Float] ).sum
	    if(pol1 > 0)
	    small :+= pol1
	    else
	    small :+= z
	    
	    pol = arts.filter(x => (x.getProperty("time_created").toString().toInt > t1) )
		pol1 = pol.size
		if(pol1 > 0)
		small :+= pol1.asInstanceOf[Float]
	    else
		small :+= z
		    
		pol = events.filter(x => (x.getProperty("time_created").toString().toInt > t1) )
		pol1 = pol.size
		if(pol1 > 0)
	    small :+= pol1.asInstanceOf[Float]
	    else
		small :+= z
	    
	    // converting the small list into json and adding to the big list
	    big :+= JSONArray(small)
	    
	    var flag = 0
	    while(flag < 30)
	    {
	      
	        t1 = t1 - 86400
	        t2 = t2 - 86400
	        var small = List[Float]()
	        
	        var pol = arts.filter(x => (x.getProperty("time_created").toString().toInt > t1  && x.getProperty("time_created").toString().toInt < t2) && (x.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.contains("politics"))  )
		    var pol1 = pol.map( x => 1/(x.getRelationships("Belongs_To_Category").asScala.toList.size - 1).asInstanceOf[Float] ).sum
		    if(pol1 > 0)
		    small :+= pol1
		    else
		    small :+= z
		    
		    pol = events.filter(x => (x.getProperty("time_created").toString().toInt > t1  && x.getProperty("time_created").toString().toInt < t2) && (x.getRelationships("Belongs_To_Event_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.contains("politics"))  )
		    pol1 = pol.map( x => 1/(x.getRelationships("Belongs_To_Event_Category").asScala.toList.size - 1).asInstanceOf[Float] ).sum
		    if(pol1 > 0)
		    small :+= pol1
		    else
		    small :+= z
	    
		    pol = arts.filter(x => (x.getProperty("time_created").toString().toInt > t1  && x.getProperty("time_created").toString().toInt < t2) && (x.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.contains("technology"))  )
		    pol1 = pol.map( x => 1/(x.getRelationships("Belongs_To_Category").asScala.toList.size - 1).asInstanceOf[Float] ).sum
		    if(pol1 > 0)
		    small :+= pol1
		    else
		    small :+= z
		    
		    pol = events.filter(x => (x.getProperty("time_created").toString().toInt > t1  && x.getProperty("time_created").toString().toInt < t2) && (x.getRelationships("Belongs_To_Event_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.contains("technology"))  )
		    pol1 = pol.map( x => 1/(x.getRelationships("Belongs_To_Event_Category").asScala.toList.size - 1).asInstanceOf[Float] ).sum
		    if(pol1 > 0)
		    small :+= pol1
		    else
		    small :+= z
	    
		    pol = arts.filter(x => (x.getProperty("time_created").toString().toInt > t1  && x.getProperty("time_created").toString().toInt < t2) && (x.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.contains("sports"))  )
		    pol1 = pol.map( x => 1/(x.getRelationships("Belongs_To_Category").asScala.toList.size - 1).asInstanceOf[Float] ).sum
		    if(pol1 > 0)
		    small :+= pol1
		    else
		    small :+= z
		    
		    pol = events.filter(x => (x.getProperty("time_created").toString().toInt > t1  && x.getProperty("time_created").toString().toInt < t2) && (x.getRelationships("Belongs_To_Event_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.contains("sports"))  )
		    pol1 = pol.map( x => 1/(x.getRelationships("Belongs_To_Event_Category").asScala.toList.size - 1).asInstanceOf[Float] ).sum
		    if(pol1 > 0)
		    small :+= pol1
		    else
		    small :+= z
	    
		    pol = arts.filter(x => (x.getProperty("time_created").toString().toInt > t1  && x.getProperty("time_created").toString().toInt < t2) && (x.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.contains("entertainment"))  )
		    pol1 = pol.map( x => 1/(x.getRelationships("Belongs_To_Category").asScala.toList.size - 1).asInstanceOf[Float] ).sum
		    if(pol1 > 0)
		    small :+= pol1
		    else
		    small :+= z
		    
		    pol = events.filter(x => (x.getProperty("time_created").toString().toInt > t1  && x.getProperty("time_created").toString().toInt < t2) && (x.getRelationships("Belongs_To_Event_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.contains("entertainment"))  )
		    pol1 = pol.map( x => 1/(x.getRelationships("Belongs_To_Event_Category").asScala.toList.size - 1).asInstanceOf[Float] ).sum
		    if(pol1 > 0)
		    small :+= pol1
		    else
		    small :+= z
		    
		    pol = arts.filter(x => (x.getProperty("time_created").toString().toInt > t1  && x.getProperty("time_created").toString().toInt < t2) && (x.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.contains("humaninterest"))  )
		    pol1 = pol.map( x => 1/(x.getRelationships("Belongs_To_Category").asScala.toList.size - 1).asInstanceOf[Float] ).sum
		    if(pol1 > 0)
		    small :+= pol1
		    else
		    small :+= z
		    
		    pol = events.filter(x => (x.getProperty("time_created").toString().toInt > t1  && x.getProperty("time_created").toString().toInt < t2) && (x.getRelationships("Belongs_To_Event_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.contains("humaninterest"))  )
		    pol1 = pol.map( x => 1/(x.getRelationships("Belongs_To_Event_Category").asScala.toList.size - 1).asInstanceOf[Float] ).sum
		    if(pol1 > 0)
		    small :+= pol1
		    else
		    small :+= z
		    
		    pol = arts.filter(x => (x.getProperty("time_created").toString().toInt > t1  && x.getProperty("time_created").toString().toInt < t2) )
		    pol1 = pol.size
		    if(pol1 > 0)
		    small :+= pol1.asInstanceOf[Float]
	        else
		    small :+= z
		    
		    pol = events.filter(x => (x.getProperty("time_created").toString().toInt > t1  && x.getProperty("time_created").toString().toInt < t2) )
		    pol1 = pol.size
		    if(pol1 > 0)
		    small :+= pol1.asInstanceOf[Float]
	        else
		    small :+= z
	    
		    
		    big :+= JSONArray(small)
		    flag = flag + 1
	    }
	    
    JSONArray(big).toString()
  }
  
  def article_pushed(id:String):Boolean=
  {
    
    val index = getNodeIndex("article").get
    val node = index.get("id",id).getSingle()
    var ret = false
    if(node != null && node.hasRelationship("Pushed_By",Direction.OUTGOING))
    {
      ret = true
    }
    ret
  }
  
  // Triggered when a moderators wants to add some weight to User/Article/Event
  def add_weight(
      user_name:String,
      item_type:String,   // User/Article/Event
      item_id:String,   // user name/article id/event id
      stars:Int, // weight to be added to the present weight
      time:Int
      ):String=
  {
    
    
    
    
    withTx {
    implicit neo =>
      
    def matchTest(x: String): String = x match {
    case "A" => "Article"
    case "E" => "Event"
    case "Q" => "Quickpost"
    case "C" => "Comment"
    case "U" => "User"
    case "H" => "Sub_category"
    case "a" => "Article"
    case "e" => "Event"
    case "q" => "Quickpost"
    case "c" => "Comment"
    case "u" => "User"
    case "h" => "Sub_category"
    }
      val i_type = matchTest(item_type)
      val index_name = i_type.toLowerCase()
      val weight_index_name = index_name + "_weight"
      val weight_index = getNodeIndex(weight_index_name).get
      val index = getNodeIndex(index_name).get
      val item_node = index.get("id",item_id).getSingle()  // Retrieving Item node based on item_id
      val UserIndex = getNodeIndex("user").get
      val user_node = UserIndex.get("id",user_name).getSingle()  //Retrieving user node based on userid
      val avg_wt_index = getNodeIndex("avg_weights").get
      val TilesIndex = getNodeIndex("tiles").get
      var news_node = TilesIndex.get("id","all").getSingle()
      var ret = ""
      if(news_node != null && item_node != null && user_node != null)
      {
          if(!item_node.hasRelationship("Pushed_By",Direction.OUTGOING))
          {
//              avg_weights()
              
              val news = news_node.getProperty("value").toString().split(",").toList.slice(0,10).map( x => index.get("id",x).getSingle().getProperty("weight").toString().toFloat)
              //val avg_wt_node = avg_wt_index.get("id",index_name).getSingle()
              val top_10_avg = news.sum/news.size
              val weight = Math.round(top_10_avg / (6-stars))
              val rel: org.neo4j.graphdb.Relationship = item_node --> "Pushed_By" --> user_node <
              val rel_time = rel.setProperty("time", time)
              var rel_in_wt = rel.setProperty("in_weight",0)
              var rel_out_wt = rel.setProperty("out_weight", weight)
		      val i_wt = item_node.getProperty("weight").toString().toInt
		      item_node.setProperty("weight",i_wt + weight)
		      weight_index.remove(item_node)
			  weight_index += (item_node,"weight",new ValueContext(i_wt + weight).indexNumeric())
			  
			  if( i_type.equals("Article") )
			  {
			     val author_node = item_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode()
	             val au_wt = author_node.getProperty("weight").toString().toInt
		         author_node.setProperty("weight",au_wt + weight)
		         val author_weight_index = getNodeIndex("user_weight").get
		         author_weight_index.remove(author_node)
			     author_weight_index += (author_node,"weight",new ValueContext(au_wt + weight).indexNumeric())
			     val cats = item_node.getRelationships("Belongs_To_Category").asScala.map(_.getOtherNode(item_node).getProperty("name").toString()).toList
			     val TilesIndex = getNodeIndex("tiles").get
			     val cur_time = (System.currentTimeMillis() /1000).toInt
			     val t = cur_time - 86400
		         for(each <- cats)
			     {
			       var news_node = TilesIndex.get("id",each).getSingle()
			       if(news_node != null)
			       {
			         var sorted_news  = List[org.neo4j.graphdb.Node]()
				     val news = news_node.getProperty("value").toString()
				     var news_list = news.split(",").toList
				     news_list ::= item_id
				     val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
//				     var news_list2 = news_list_trim.split(",").toList.map(x => index.get("id",x).getSingle())
//					 sorted_news = news_list2.sortBy( x =>   -((( (x.getProperty("views").toString().toFloat*5) + 1 )+(  (x.getRelationships("article_voteup","article_markfav","Comment_To_Article").asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).map(x => x.getProperty("in_weight").toString().toInt).sum  ) *10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
					 news_node.setProperty("value",news_list_trim)
			      
			        }
			     }
			  }
              
			  else if( i_type.equals("Event") )
			  {
			    val author_node = item_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode()
	            val au_wt = author_node.getProperty("weight").toString().toInt
		        author_node.setProperty("weight",au_wt + weight)
		        val author_weight_index = getNodeIndex("user_weight").get
		        author_weight_index.remove(author_node)
			    author_weight_index += (author_node,"weight",new ValueContext(au_wt + weight).indexNumeric())
			  }
			  ret = "Added"
			  
          }
          else
          {
            val mod_node = item_node.getSingleRelationship("Pushed_By",Direction.OUTGOING).getOtherNode(item_node)
            ret = "Not_Added" + "," + mod_node.getProperty("user_name")
          }
      }
    ret 
    }
  }
  
   def update_weight(
      item_type:String,
      item_id:String,   // article_id
      weight:Int
      ):Boolean=
  {
    
    withTx {
    implicit neo =>
      
      def matchTest(x: String): String = x match {
	    case "A" => "Article"
	    case "E" => "Event"
	    case "U" => "User"
	    case "P" => "Petition"
	    case "D" => "Debate"
	    case "a" => "Article"
	    case "e" => "Event"
	    case "u" => "User"
	    case "p" => "Petition"
	    case "d" => "Debate"
	    }
	    val i_type = matchTest(item_type)
	    
	    val index_name = i_type.toLowerCase()
	    val weight_index_name = index_name + "_weight"
	    val index = getNodeIndex(index_name).get
	    val weight_index = getNodeIndex(weight_index_name).get
	    val item_node = index.get("id",item_id).getSingle()
	    var ret = false
	    if(item_node != null)
	    {
	        if(item_type.toLowerCase().equals("a"))
	        {
	          var rels = item_node.getRelationships("article_voteup","article_markfav").asScala.toList
	          for(each <- rels)
	          {
	             
	             each.setProperty("in_weight", Math.round(weight.asInstanceOf[Float]/100))
	          }
	          

	        }
		    item_node.setProperty("weight", weight)
		    weight_index.remove(item_node)
		    weight_index += (item_node,"weight", new ValueContext( weight ).indexNumeric())
		    ret = true
	    }
	     
	    
	    
      
    ret  
    }
  }
  
  // Triggered when a moderators wants to reduce weight of User/Article/Event
  def reduce_weight(
      item_id:String   // article_id
      ):String=
  {
    
    
    
    
    withTx {
    implicit neo =>
      
    
      val weight_index = getNodeIndex("article_weight").get
      val avg_wt_index = getNodeIndex("avg_weights").get
      val index = getNodeIndex("article").get
      val UserIndex = getNodeIndex("user").get
      val item_node = index.get("id",item_id).getSingle()  // Retrieving Item node based on item_id
      //val avg_wt_index = getNodeIndex("avg_weights").get
      var ret = "Not reduced"
      if(item_node != null)
      {
        
         
         val avg_u_node = avg_wt_index.get("id","user").getSingle()
         val user_art_rel = item_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING)
         //val a_wt = user_art_rel.getProperty("out_weight")
         val user_node = user_art_rel.getOtherNode(item_node)
         val user_name = user_node.getProperty("user_name").toString()
         var u_wt = user_node.getProperty("weight").toString().toInt
         val top_10_users_list = avg_u_node.getProperty("top_30_users").toString().split(",").slice(0,10).toList
         if(top_10_users_list.contains(user_name))
         {
           val user_10_node = UserIndex.get("id",top_10_users_list(top_10_users_list.size - 1)).getSingle()
           if(user_10_node != null)
           {
             u_wt = user_10_node.getProperty("weight").toString().toInt
             ret = "Reduced"
           }
           else
           {
             u_wt = user_node.getProperty("weight").toString().toInt
           }
         }
         val a_wt = Math.round(u_wt.asInstanceOf[Float]/100)
         item_node.setProperty("weight",a_wt)
         user_art_rel.setProperty("out_weight",a_wt)
         weight_index.remove(item_node)
         weight_index += (item_node,"weight",new ValueContext( a_wt ).indexNumeric())
         
         val cats = item_node.getRelationships("Belongs_To_Category").asScala.map(_.getOtherNode(item_node).getProperty("name").toString()).toList
	     val TilesIndex = getNodeIndex("tiles").get
	     val cur_time = (System.currentTimeMillis() /1000).toInt
	     val t = cur_time - 86400
         for(each <- cats)
	     {
	       var news_node = TilesIndex.get("id",each).getSingle()
	       if(news_node != null)
	       {
	         var sorted_news  = List[org.neo4j.graphdb.Node]()
		     val news = news_node.getProperty("value").toString()
		     var news_list = news.split(",").toList
		     val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
		     var news_list2 = news_list_trim.split(",").toList.map(x => index.get("id",x).getSingle())
			 sorted_news = news_list2.sortBy( x =>   -((( (x.getProperty("views").toString().toFloat*5) + 1 )+(  (x.getRelationships("article_voteup","article_markfav","Comment_To_Article").asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).map(x => x.getProperty("in_weight").toString().toInt).sum  ) *10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
			 news_node.setProperty("value",sorted_news.map(x => x.getProperty("article_id").toString()).mkString(","))
	      
	       }
	     }
         
      }
    ret 
    }
  }

  
  // This function gives friend suggestions in levels
  def friend_suggestions(user_name : String):String=
  {
    
    
    
    
    withTx {
    implicit neo =>
      
      var ret = List[Any]()
      val index = getNodeIndex("user").get
      val user_node = index.get("id",user_name).getSingle()
      if(user_node != null)
	  {
	      var f1_nodes = user_node.getRelationships("Follows",Direction.OUTGOING).asScala.map(_.getOtherNode(user_node)).toList
	      f1_nodes :+= user_node
	      var f2_nodes = f1_nodes.map(x => x.getRelationships("Follows",Direction.OUTGOING).asScala.map(_.getOtherNode(user_node)).toList.filterNot(x =>  f1_nodes.contains(x))).flatten.distinct
	      var f2 = f2_nodes.map( x => (x.getProperty("user_name").toString(),x.getRelationships("Follows",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).toList.intersect(f1_nodes).size) )
	      var f3_nodes = f2_nodes.map(x => x.getRelationships("Follows",Direction.OUTGOING).asScala.map(_.getOtherNode(user_node)).toList.filterNot(x =>  f1_nodes.contains(x) && f2_nodes.contains(x))).flatten.distinct
	      var f3 = f3_nodes.map( x => (x.getProperty("user_name").toString(),x.getRelationships("Follows",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).toList.intersect(f1_nodes).size) )
	      
	      
	      var sorted1 = f2.sortBy(x => -x._2)
	      val sorted2 = f3.sortBy(x => -x._2)
	      
	      ret ::= JSONArray(sorted2)
	      ret ::= JSONArray(sorted1)
	      
      }  
    JSONArray(ret).toString()  
    }
  }
  
  // This function uses the cities.txt to create all the location nodes ( run only once )
  def location_store()
  {
    
    withTx {
    implicit neo =>
      
    val index = getNodeIndex("location").get
    // checkingh if the location store is already added or not
    if(index.get("id","andhra pradesh").getSingle() == null)
	{
        // reading cities.txt
	    val source = scala.io.Source.fromFile("/var/n4j/data/cities.txt")
	    val lines = source.mkString
	    source.close()
	    var all_cities = List[String]()
	    var m = scala.collection.mutable.Map[String, String]()
	    val bl = lines.split('\n').toList
	    
	    // creating state nodes and adding list of constituencies to the state node
	    for(each <- bl)
	    {
	      val sl = each.split("\t").toList.map( x => x.toLowerCase())
	      var item = sl(0).replace("*","")
	      if(item.contains(", "))
	      {
	        item = item.split(", ")(0)
	        
	      }
	      
	      all_cities ::= item
	      
	      val state = sl(1).toLowerCase()
	      val location_node = index.get("id",state).getSingle()
	      
	      if(location_node != null)
	      {
	        var list = location_node.getProperty("cities").toString().split(",").toList
	        list ::= item
	        location_node.setProperty("cities",list.distinct.mkString(",")) 
	      }
	      
	      else
	      {
	        val loc_node = createNode(location(state,item,""))
	        index += (loc_node,"id",state)
	      }
	      
	    }
	    val all_node = createNode(location("all",all_cities.distinct.mkString(","),""))
	    index += (all_node,"id","all")
	    println("Locations Added")   
	 }
   }
  }
  
  def keyword_store()
  {
    
    withTx {
    implicit neo =>
      
      val CategoryIndex = getNodeIndex("category").get
      val TopicIndex = getNodeIndex("topic").get
      
        var dummy_topic_node = TopicIndex.get("name","dummy").getSingle()
        if(dummy_topic_node == null)
        {
          
            
	        val t_nodes = TopicIndex.query( "name", "*" ).iterator().asScala.toList
	        for(each <- t_nodes)
	        {
	          val rels = each.getRelationships().asScala.toList
	          for(e <- rels)
	          {
	            e.delete()
	          }
	          TopicIndex.remove(each)
		      each.delete()
	          
	        }
	        
	        dummy_topic_node = createNode(topic("dummy","" ))
			TopicIndex += (dummy_topic_node,"name","dummy")
	      
	      
	      
	      
	      val list = List("politics","entertainment","technology","business","health","sports")
	      for(each <- list)
	      {
	        val src = Source.fromFile("/var/n4j/data/keywords/" + each + ".csv")
	        val iter = src.getLines().map(_.split(":").toList).toList
	        
	//        println(iter)
	        src.close()
	        
	        val x = iter(0)
	        val y = x(0)
	//        println(x)
	//        println(y)
	        val size = y.split(",").toList.size - 1
	//        println(size)
	        for( a <- 0 to size)
	        {
	           var subcat = List[String]()
	           for(e <- iter)
	           {
	//             println(e)
	             var l = e(0)
	//             println(l)
	             l = l.replaceAll(",",", ")
	//             println(l)
	             
	             val g = l.split(",").toList
	//             println(g)
	             if(!g(a).equals(" "))
	             {
	               
	               subcat :+= g(a).stripPrefix(" ").stripSuffix(" ").trim.toLowerCase()
	//               println(subcat)
	             }
	             
	             
	             
	           }
	           
	            // println(subcat.size)
	             //  println(subcat)
	               
	               if(CategoryIndex.get("name",each).getSingle() != null)  
			       {
			        // val CategoryIndex = getNodeIndex("category").get
			         val category_node = CategoryIndex.get("name",each).getSingle()
		             val top = subcat(0)
		             var topic_node = TopicIndex.get("name",top).getSingle()
		             if(topic_node != null)
		             {
		               topic_node.setProperty("sub_topics",subcat.mkString(","))
		             }
			         
		             else
		             {
		               topic_node = createNode(topic(top,subcat.mkString(",") ))
			           TopicIndex += (topic_node,"name",top)
		
			           var rel = topic_node --> "Topic_Of_Category" --> category_node <
			           //var rel_time = rel.setProperty("time", a_time_created)
		             }
			         //rel = article_node --> "Belongs_To_Category" --> category_node <
			         //var rel_time = rel.setProperty("time", a_time_created)
			       }
			       else
			       {
			         val category_node = createNode(category(each,"",""))
			         CategoryIndex += (category_node,"name",each)
			         val top = subcat(0)
		             var topic_node = TopicIndex.get("name",top).getSingle()
		             if(topic_node != null)
		             {
		               topic_node.setProperty("sub_topics",subcat.mkString(","))
		             }
			         
		             else
		             {
		               topic_node = createNode(topic(top,subcat.mkString(",") ))
	//	               println(top)
	//	               println(subcat.mkString(","))
	//                   println()
			           TopicIndex += (topic_node,"name",top)
		
			           var rel = topic_node --> "Topic_Of_Category" --> category_node <
			           //var rel_time = rel.setProperty("time", a_time_created)
		             }
		
			         
			         
			       }
	           
	        }
	        
	      }
      }
      
      
    }
  }
  
  def add_exclusive_property()
  {
    
   
     withTx {
      
     implicit neo =>
       
       val FeaturedTilesIndex = getNodeIndex("featured_tiles").get
       var featured_latest_node = FeaturedTilesIndex.get("id","latest").getSingle()
       if(featured_latest_node == null)
       {
         var all_featured_nodes = FeaturedTilesIndex.query("id", "*" ).iterator().asScala.toList
         for(each <- all_featured_nodes)
         {
           FeaturedTilesIndex.remove(each)
	       
	       each.delete()
         }
       }
       
       val CIndex = getNodeIndex("category").get
       var c_node = CIndex.get("name","all").getSingle()
       
       if(c_node == null)
       {
         var c_node1 = CIndex.get("name","news").getSingle()
         if(c_node1 != null)
         {
           CIndex.remove(c_node1)
           c_node1.setProperty("name","all")
           CIndex += (c_node1,"name","all")
           
         }
         
        }
       
       val TilesIndex = getNodeIndex("tiles").get
       
       var all_node = TilesIndex.get("id","all").getSingle()
       if(all_node == null)
       {
         var all_node1 = TilesIndex.get("id","news").getSingle()
         if(all_node1 != null)
         {
           TilesIndex.remove(all_node1)
           all_node1.setProperty("id","all")
           TilesIndex += (all_node1,"id","all")
           
         }
         
       }
       
       var latest_node = TilesIndex.get("id","latest").getSingle()
       if(latest_node == null)
       {
         val ArticleIndex = getNodeIndex("article").get
         var news_node = TilesIndex.get("id","all").getSingle()
         if(news_node != null)
         {
        	 val news_list = news_node.getProperty("value").toString().split(",").toList.slice(0,200).map(x => ArticleIndex.get("id",x).getSingle()).sortBy(-_.getProperty("time_created").toString().toInt).slice(0,15).map( x => x.getProperty("article_id").toString())
		     val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
		     //news_node.setProperty("value",news_list_trim)
		     val tiles_node =  createNode(tiles("latest",news_list_trim))
			 TilesIndex += (tiles_node,"id","latest")
         }
         
         else
         {
             val tiles_node =  createNode(tiles("latest",""))
			 TilesIndex += (tiles_node,"id","latest")
         }
       }
       
       
       if(featured_latest_node == null)
       {
         val tiles_node =  createNode(featured_tiles("latest",""))
	     FeaturedTilesIndex += (tiles_node,"id","latest")
       }
       
       
       
       
       val cur_time = (System.currentTimeMillis() /1000).toInt
       val CategoryIndex = getNodeIndex("category").get
       val cat_nodes = CategoryIndex.query( "name", "*" ).iterator().asScala.toList
       for(each <- cat_nodes)
       {
         if(!each.hasProperty("exclusive"))
         {
           each.setProperty("exclusive","")
         }
       }
       
       val UserIndex = getNodeIndex("user").get
       val user_nodes = UserIndex.query( "id", "*" ).iterator().asScala.toList
       
       
       val UserTilesIndex = getNodeIndex("user_tiles").get
       val usertiles_nodes = UserTilesIndex.query( "id", "*" ).iterator().asScala.toList
       for(each <- usertiles_nodes)
       {
         val props = each.getPropertyKeys().asScala.toList
         for(e <- props)
         {
           if( !(e.equals("id") || e.equals("news_Personalized") || e.equals("all")) )
           {
             each.removeProperty(e)
           }
         }
         each.setProperty("all","")
       }
       
       for(each <- user_nodes)
       {
         if(!each.hasProperty("last_seen"))
         {
           each.setProperty("last_seen",cur_time)
         }
         each.setProperty("nouns","")
         
         if(!each.hasProperty("notify_time"))
         {
           each.setProperty("notify_time",cur_time)
         }
         
         if(!each.hasProperty("fav_hash"))
         {
           each.setProperty("fav_hash","")
         }
         
         
         
         
       }
       
       val ArtIndex = getNodeIndex("article").get
       val art_nodes = ArtIndex.query( "id", "*" ).iterator().asScala.toList
       for(each <- art_nodes)
       {
         
         
         if(!each.hasProperty("article_featured_img"))
         {
           each.setProperty("article_featured_img","")
         }
         
         if(!each.hasProperty("space"))
         {
           each.setProperty("space",0)
         }
         
         if(!each.hasProperty("approved"))
         {
           each.setProperty("approved",1)
         }
         if(!each.hasProperty("head"))
         {
           each.setProperty("head",0)
         }
         if(!each.hasProperty("latest_views"))
         {
           each.setProperty("latest_views","")
         }
         if(each.hasProperty("cloud"))
         {
           each.removeProperty("cloud")
         }
         if(!each.hasProperty("stars"))
         {
           each.setProperty("stars",0)
         }
         
         
		         
         
       }
       
       val EventIndex = getNodeIndex("event").get
       val event_nodes = EventIndex.query( "id", "*" ).iterator().asScala.toList
       for(each <- event_nodes)
       {
         if(!each.hasProperty("space"))
         {
           each.setProperty("space",0)
         }
         if(!each.hasProperty("latest_views"))
         {
           each.setProperty("latest_views","")
         }
         
         if(!each.hasProperty("event_featured_img"))
         {
           each.setProperty("event_featured_img","")
         }
         if(!each.hasProperty("event_summary"))
         {
           each.setProperty("event_summary","")
         }
         if(!each.hasProperty("approved"))
         {
           each.setProperty("approved",1)
         }
         if(!each.hasProperty("head"))
         {
           each.setProperty("head",0)
         }
       }
       
       val DebateIndex = getNodeIndex("debate").get
       val debate_nodes = DebateIndex.query( "id", "*" ).iterator().asScala.toList
       for(each <- debate_nodes)
       {
         if(!each.hasProperty("space"))
         {
           each.setProperty("space",0)
         }
         if(!each.hasProperty("d_content"))
         {
           each.setProperty("d_content","")
         }
         if(!each.hasProperty("approved"))
         {
           each.setProperty("approved",1)
         }
         
         if(!each.hasProperty("head"))
         {
           each.setProperty("head",0)
         }
       }
       
       val TownhallIndex = getNodeIndex("townhall").get
       val townhall_nodes = TownhallIndex.query( "id", "*" ).iterator().asScala.toList
       for(each <- townhall_nodes)
       {
         if(!each.hasProperty("space"))
         {
           each.setProperty("space",0)
         }
         if(!each.hasProperty("approved"))
         {
           each.setProperty("approved",1)
         }
         
         if(!each.hasProperty("head"))
         {
           each.setProperty("head",0)
         }
       }
       
       val SpaceIndex = getNodeIndex("space").get
       val space_nodes = SpaceIndex.query( "id", "*" ).iterator().asScala.toList
       for(each <- space_nodes)
       {
         if(!each.hasProperty("closed"))
         {
           each.setProperty("closed",0)
         }
         if(!each.hasProperty("pins"))
         {
           each.setProperty("pins","")
         }
         
         
         if(!each.hasProperty("space_content"))
         {
           val space_tagline = each.getProperty("space_tagline").toString()
           val space_title = each.getProperty("space_title").toString()
	       
	       val tagger = new MaxentTagger(
	                "/var/n4j/data/left3words-wsj-0-18.tagger")
	              val tagged = tagger.tagString(space_tagline)
	              //val prev_nouns = user_node.getProperty("nouns").toString()
	              val noun_list =  tagged.split(" ").toList.map(x => x.split("/")).filter( x => (x.size == 2 && x(1).equals("NNP"))).map(x => x(0).toLowerCase())
	              
	              each.setProperty("space_content",(noun_list.mkString(" ") + " " + bummy(space_title)))
	           
         }
         
       }
       
       val PIndex = getNodeIndex("petition").get
       val p_nodes = PIndex.query( "id", "*" ).iterator().asScala.toList
       //val CategoryIndex = getNodeIndex("category").get
       var cat_node = CategoryIndex.get("name","humaninterest").getSingle()
       var cat_node1 = CategoryIndex.get("name","all").getSingle()
       if(cat_node == null)  
	   {
			cat_node = createNode(category("humaninterest","",""))
			CategoryIndex += (cat_node,"name","humaninterest")
	   }
       if(cat_node1 == null)  
	   {
			cat_node1 = createNode(category("all","",""))
			CategoryIndex += (cat_node1,"name","all")
	   }
       for(each <- p_nodes)
       {
         if(!each.hasProperty("space"))
         {
           each.setProperty("space",0)
         }
         if(!each.hasProperty("latest_views"))
         {
           each.setProperty("latest_views","")
         }
         
         if(!each.hasProperty("p_img_url"))
         {
           each.setProperty("p_img_url","")
         }
         
         if(!each.hasProperty("head"))
         {
           each.setProperty("head",0)
         }
         if(!each.hasProperty("approved"))
         {
           each.setProperty("approved",1)
         }
         
         if(!each.hasRelationship("Belongs_To_Petition_Category",Direction.OUTGOING))
         {
           val p_time = each.getProperty("time_created").toString().toInt
           var rel: org.neo4j.graphdb.Relationship = each --> "Belongs_To_Petition_Category" --> cat_node <
		   var rel_time = rel.setProperty("time", p_time)
		   var rel1: org.neo4j.graphdb.Relationship = each --> "Belongs_To_Petition_Category" --> cat_node1 <
		   var rel_time1 = rel1.setProperty("time", p_time)
         }
         
       }
       
//       for(each <- art_nodes)
//       {
//         
//         if(!each.hasRelationship("Belongs_To_Topic"))
//         {
//             val a_time_created = each.getProperty("time_created").toString().toInt
//             val a_title = each.getProperty("article_title")
//             val a_summary = each.getProperty("article_summary")
//             val a_content = each.getProperty("article_content")
//             val hash_list = each.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.map(_.getEndNode().getProperty("name").toString())
//             val topic_words_list = (a_title+" "+a_summary+" "+a_content+" "+hash_list.mkString(" ")).toLowerCase().split(" ").toList
//		     
//	         val c_nodes = each.getRelationships("Belongs_To_Category").asScala.toList.map(_.getEndNode())
//	         for(e <- c_nodes)
//	         {
//	           if(e != c_node)
//	           {
//	                 val topic_nodes = e.getRelationships("Topic_Of_Category").asScala.toList.map(_.getStartNode())
//			         if(topic_nodes != null)
//			         {
//			           for(top <- topic_nodes)
//			           {
//			             val keywords = top.getProperty("sub_topics").toString().split(",").toList
//			             val inter = topic_words_list.intersect(keywords).size
//			             if(inter > 0)
//			             {
//			               var rel = each --> "Belongs_To_Topic" --> top <
//			               var rel_time = rel.setProperty("time", a_time_created)
//			               var count = rel.setProperty("count", inter)
//			             }
//			             
//			           }
//			         }
//	           }
//	         }
//         
//         }
//         
//         
//       }
       
     }
  }
  
  // this function is used to display the user stream
  def stream(user_name:String,user_type:String,count:Int,prev_cnt:Int): String =
  {
    
     val nodeIndex = getNodeIndex("user").get
     val node = nodeIndex.get("id",user_name).getSingle()
         
     var list  = List[Any]()
     if(node != null)
	 {
         val user_details = node.getProperty("user_name").toString() + ":" + node.getProperty("first_name").toString() + " " + node.getProperty("last_name").toString()
           
         var follows = List[org.neo4j.graphdb.Node]()
         var event_invites =  List[org.neo4j.graphdb.Relationship]()
         var com_mentions =  List[org.neo4j.graphdb.Relationship]()
         var art_com_rels =  List[org.neo4j.graphdb.Relationship]()
         var event_com_rels =  List[org.neo4j.graphdb.Relationship]()
         var p_com_rels =  List[org.neo4j.graphdb.Relationship]()
         var event_atnd_rels =  List[org.neo4j.graphdb.Relationship]()
         var p_signed_rels =  List[org.neo4j.graphdb.Relationship]()
         var sub_com_rels =  List[org.neo4j.graphdb.Relationship]()
         var user_rels =  List[org.neo4j.graphdb.Relationship]()
         var art_voted_rels =  List[org.neo4j.graphdb.Relationship]()
         var art_poll_rels =  List[org.neo4j.graphdb.Relationship]()
         var event_poll_rels =  List[org.neo4j.graphdb.Relationship]()
         var p_poll_rels =  List[org.neo4j.graphdb.Relationship]()
         var event_notify_rels =  List[org.neo4j.graphdb.Relationship]()
         
         var t_part_rels =  List[org.neo4j.graphdb.Relationship]()
         
         var t_qtn_rels =  List[org.neo4j.graphdb.Relationship]()
         var t_com_rels =  List[org.neo4j.graphdb.Relationship]()
         
         var d_part_rels =  List[org.neo4j.graphdb.Relationship]()
         var d_qtn_rels =  List[org.neo4j.graphdb.Relationship]()
         var d_arg_rels =  List[org.neo4j.graphdb.Relationship]()
         var d_com_rels =  List[org.neo4j.graphdb.Relationship]()
         
         var commented_arts =  List[org.neo4j.graphdb.Node]()
         var questioned_townhalls =  List[org.neo4j.graphdb.Node]()
         var participated_townhalls =  List[org.neo4j.graphdb.Node]()
         var commented_townhalls =  List[org.neo4j.graphdb.Node]()
         
         var qtn_debates =  List[org.neo4j.graphdb.Node]()
         var arg_debates =  List[org.neo4j.graphdb.Node]()
         var participated_debates =  List[org.neo4j.graphdb.Node]()
         var commented_debates =  List[org.neo4j.graphdb.Node]()
         
         var commented_petitions =  List[org.neo4j.graphdb.Node]()
         var commented_events =  List[org.neo4j.graphdb.Node]()
         var commented_coms =  List[org.neo4j.graphdb.Node]()
         var voted_arts =  List[org.neo4j.graphdb.Node]()
         
         var atnd_events =  List[org.neo4j.graphdb.Node]()
         var signed_petitions =  List[org.neo4j.graphdb.Node]()
         var responded_polls =  List[org.neo4j.graphdb.Node]()
         // checking if the stream is social or self
	     if(user_type.toLowerCase().equals("s"))
	     {
	         // social stream
	         // extracting all the actions that were done on the items published by the user
	         event_invites = 
	           node.getRelationships("Invited_To_Event","User_Of_Article","User_Of_Event","User_Of_Petition").asScala.toList
	           	.filter( x => x.getOtherNode(node).getProperty("space").toString.toInt == 0)
//	         com_mentions = node.getRelationships("User_Of_Comment").asScala.toList.filter( x => x.getOtherNode(node).getProperty("space").toString.toInt == 0)
	         
	         val art_list = 
	           node.getRelationships("Article_Written_By").asScala.map(_.getOtherNode(node)).toList
	           .filter( x => x.getProperty("space").toString.toInt == 0)
	           
	         art_com_rels = 
	           art_list.map( 
	               x => x.getRelationships("Comment_To_Article").asScala.toList
	               .filterNot( x => x.getStartNode.hasRelationship("Comment_To_Comment",Direction.OUTGOING))
	               .sortBy(-_.getProperty("time").toString().toInt)
	               .slice(0,1)
	               .map(_.getOtherNode(x))
	               )
	               .flatten
	               .map( x => x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING))
		     
	         art_voted_rels = 
	           art_list.map( 
	               x => x.getRelationships("article_voteup").asScala.toList.sortBy(-_.getProperty("time").toString().toInt)
	               .slice(0,1)
	               )
	               .flatten
	               
		     art_poll_rels = 
		       art_list.map(
		    		   x => x.getRelationships("Poll_App_Of").asScala.toList.map(_.getOtherNode(x))
		    		   )
		           .flatten
		           .map( 
		               x => x.getRelationships("Voted_To_Poll").asScala.toList.sortBy(-_.getProperty("time").toString().toInt)
		               .slice(0,1)
		               )
		               .flatten
		     
		     commented_arts = 
		       art_com_rels.map(x => x.getStartNode())
		       	.map( x => x.getSingleRelationship("Comment_To_Article",Direction.OUTGOING).getEndNode())
		     voted_arts = art_voted_rels.map( x => x.getEndNode())
		     
	         val event_list = node.getRelationships("Event_Created_By").asScala.map(_.getOtherNode(node)).toList.filter( x => x.getProperty("space").toString.toInt == 0)
	         event_com_rels = event_list.map( x => x.getRelationships("Comment_To_Event").asScala.toList.filterNot( x => x.getStartNode.hasRelationship("Comment_To_Comment",Direction.OUTGOING)).sortBy(-_.getProperty("time").toString().toInt).slice(0,1).map(_.getOtherNode(x))).flatten.map( x => x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING))
		     event_atnd_rels = event_list.map( x => x.getRelationships("Is_Attending").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten
		     event_poll_rels = event_list.map( x => x.getRelationships("Poll_App_Of_Event").asScala.toList.map(_.getOtherNode(x))).flatten.map( x => x.getRelationships("Voted_To_Poll").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten
		     commented_events = event_com_rels.map( x => x.getStartNode()).map( x => x.getSingleRelationship("Comment_To_Event",Direction.OUTGOING).getEndNode())
		     atnd_events = event_atnd_rels.map( x => x.getEndNode())
		     event_notify_rels = node.getRelationships("Notified_In_Event").asScala.toList.filter( x => x.getOtherNode(node).getProperty("space").toString.toInt == 0)
	    	 
		     val p_list = node.getRelationships("Petition_Written_By").asScala.map(_.getOtherNode(node)).toList.filter( x => x.getProperty("space").toString.toInt == 0)
	         p_com_rels = p_list.map( x => x.getRelationships("Comment_To_Petition").asScala.toList.filterNot( x => x.getStartNode.hasRelationship("Comment_To_Comment",Direction.OUTGOING)).sortBy(-_.getProperty("time").toString().toInt).slice(0,1).map(_.getOtherNode(x))).flatten.map( x => x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING))
		     p_poll_rels = p_list.map( x => x.getRelationships("Poll_App_Of_Petition").asScala.toList.map(_.getOtherNode(x))).flatten.map( x => x.getRelationships("Voted_To_Poll").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten
		     commented_petitions = p_com_rels.map( x => x.getStartNode()).map( x => x.getSingleRelationship("Comment_To_Petition",Direction.OUTGOING).getEndNode())
		     p_signed_rels = p_list.map( x => x.getRelationships("Signed_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten
		     signed_petitions = p_signed_rels.map( x => x.getEndNode())
		     
		     val t_list = node.getRelationships("Townhall_Written_By").asScala.map(_.getOtherNode(node)).toList.filter( x => x.getProperty("space").toString.toInt == 0)
	         t_part_rels = t_list.map( x => x.getRelationships("Participated_In_Townhall").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten
		     participated_townhalls = t_part_rels.map( x => x.getEndNode())
		     t_qtn_rels = t_list.map( x => x.getRelationships("Asked_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten
		     questioned_townhalls = t_qtn_rels.map( x => x.getEndNode())
		     
		     t_com_rels = t_list.map( x => x.getRelationships("Commented_On_Townhall").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten
		     commented_townhalls = t_com_rels.map( x => x.getEndNode())
		     
		     val d_list = node.getRelationships("Debate_Written_By").asScala.map(_.getOtherNode(node)).toList.filter( x => x.getProperty("space").toString.toInt == 0)
	         d_part_rels = d_list.map( x => x.getRelationships("Participated_In_Debate").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten
		     participated_debates = d_part_rels.map( x => x.getEndNode())
		     d_qtn_rels = d_list.map( x => x.getRelationships("Asked_Debate_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten
		     qtn_debates = d_qtn_rels.map( x => x.getEndNode())
		     d_arg_rels = d_list.map( x => x.getRelationships("Started_Debate_Argument").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten
		     arg_debates = d_arg_rels.map( x => x.getEndNode())
		     
		     d_com_rels = d_list.map( x => x.getRelationships("Commented_On_Debate").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten
		     commented_debates = d_com_rels.map( x => x.getEndNode())
		     
		     responded_polls = (art_poll_rels:::event_poll_rels:::p_poll_rels).map( x => x.getEndNode())
//		     sub_com_rels = node.getRelationships("Comment_Written_By").asScala.map(_.getOtherNode(node)).toList.map( x => x.getRelationships("Comment_To_Comment",Direction.INCOMING).asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1).map(_.getOtherNode(x))).flatten.distinct.map( x => x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING))
//             commented_coms = sub_com_rels.map( x => x.getStartNode()).map( x => x.getSingleRelationship("Comment_To_Comment",Direction.OUTGOING).getEndNode())
		     
		     
		     //getting all the users that the user is following
		     follows = node.getRelationships("Follows",Direction.OUTGOING).asScala.map(_.getOtherNode(node)).toList
	         
	     }
         
         // adding user to the follows list
         follows ::= node
         
         var user_follow_rels = follows.map(x => x.getRelationships("Follows",Direction.OUTGOING).asScala).flatten.toList.groupBy(_.getEndNode()).valuesIterator.toList.filter( x => x.size > 0).map( x => x.sortBy(-_.getProperty("time").toString().toInt)).map( x => x(0))
	     var space_follow_rels = follows.map(x => x.getRelationships("Space_Followed_By",Direction.INCOMING).asScala.toList.filter(z => z.getStartNode().getProperty("closed").toString().toInt == 0  || !user_type.equalsIgnoreCase("s"))).flatten.groupBy(_.getStartNode()).valuesIterator.toList.filter( x => x.size > 0).map( x => x.sortBy(-_.getProperty("time").toString().toInt)).map( x => x(0))
	     var space_created_rels = follows.map(x => x.getRelationships("Space_Created_By").asScala.toList.filter(z => z.getStartNode().getProperty("closed").toString().toInt == 0  || !user_type.equalsIgnoreCase("s"))).flatten
	     
         // extracting all the actions that were done by the users in the follows list
	     var rels = follows.map(x => x.getRelationships("Event_Created_By","Article_Written_By","Petition_Written_By","Townhall_Written_By","Debate_Written_By").asScala.toList.filter( y => y.getOtherNode(x).getProperty("space").toString.toInt == 0  || !user_type.equalsIgnoreCase("s"))).flatten
	     var rels2 = follows.map(x => x.getRelationships("article_voteup").asScala.toList.filter( y => y.getOtherNode(x).getProperty("space").toString.toInt == 0 || !user_type.equalsIgnoreCase("s"))).flatten.groupBy(_.getEndNode()).valuesIterator.toList.filter( x => x.size > 0).map( x => x.sortBy(-_.getProperty("time").toString().toInt)).map( x => x(0))
	     rels2 = rels2.filterNot( x => voted_arts.contains(x.getEndNode()))
	     var rels3 = follows.map(x => x.getRelationships("Is_Attending").asScala.toList.filter( y => y.getOtherNode(x).getProperty("space").toString.toInt == 0  || !user_type.equalsIgnoreCase("s"))).flatten.groupBy(_.getEndNode()).valuesIterator.toList.filter( x => x.size > 0).map( x => x.sortBy(-_.getProperty("time").toString().toInt)).map( x => x(0))
	     rels3 = rels3.filterNot( x => atnd_events.contains(x.getEndNode()))
	     var rels5 = follows.map(x => x.getRelationships("Signed_Petition").asScala.toList.filter( y => y.getOtherNode(x).getProperty("space").toString.toInt == 0  || !user_type.equalsIgnoreCase("s"))).flatten.groupBy(_.getEndNode()).valuesIterator.toList.filter( x => x.size > 0).map( x => x.sortBy(-_.getProperty("time").toString().toInt)).map( x => x(0))
	     rels5 = rels5.filterNot( x => signed_petitions.contains(x.getEndNode()))
	     
	     var rels6 = follows.map(x => x.getRelationships("Participated_In_Townhall").asScala.toList.filter( y => y.getOtherNode(x).getProperty("space").toString.toInt == 0  || !user_type.equalsIgnoreCase("s"))).flatten.groupBy(_.getEndNode()).valuesIterator.toList.filter( x => x.size > 0).map( x => x.sortBy(-_.getProperty("time").toString().toInt)).map( x => x(0))
	     rels6 = rels6.filterNot( x => participated_townhalls.contains(x.getEndNode()))
	     var rels7 = follows.map(x => x.getRelationships("Participated_In_Debate").asScala.toList.filter( y => y.getOtherNode(x).getProperty("space").toString.toInt == 0  || !user_type.equalsIgnoreCase("s"))).flatten.groupBy(_.getEndNode()).valuesIterator.toList.filter( x => x.size > 0).map( x => x.sortBy(-_.getProperty("time").toString().toInt)).map( x => x(0))
	     rels7 = rels7.filterNot( x => participated_debates.contains(x.getEndNode()))
	     
//	     var rels4 = follows.map(x => x.getRelationships("Voted_To_Poll").asScala).flatten.toList.groupBy(_.getEndNode()).valuesIterator.toList.filter( x => x.size > 0).map( x => x.sortBy(-_.getProperty("time").toString().toInt)).map( x => x(0))
//	     rels4 = rels4.filterNot( x => responded_polls.contains(x.getEndNode()))
	     
	     var coms_tot = follows.map(x => x.getRelationships("Comment_Written_By").asScala).flatten.toList.map( x => x.getStartNode()).filterNot( x => x.hasRelationship("Comment_To_Comment",Direction.OUTGOING))

	     var art_coms = coms_tot.filter(x => x.hasRelationship("Comment_To_Article",Direction.OUTGOING)).filter( y => y.getSingleRelationship("Comment_To_Article",Direction.OUTGOING).getOtherNode(y).getProperty("space").toString.toInt == 0  || !user_type.equalsIgnoreCase("s")).map( x => x.getSingleRelationship("Comment_To_Article",Direction.OUTGOING)).groupBy(_.getEndNode()).valuesIterator.toList.filter( x => x.size > 0).map( x => x.sortBy(-_.getProperty("time").toString().toInt)).map( x => x(0).getStartNode().getSingleRelationship("Comment_Written_By",Direction.OUTGOING))
         art_coms = art_coms.filterNot(x => commented_arts.contains(x.getStartNode().getSingleRelationship("Comment_To_Article",Direction.OUTGOING).getEndNode()))
	     
	     
	     var event_coms = coms_tot.filter(x => x.hasRelationship("Comment_To_Event",Direction.OUTGOING)).filter( y => y.getSingleRelationship("Comment_To_Event",Direction.OUTGOING).getOtherNode(y).getProperty("space").toString.toInt == 0  || !user_type.equalsIgnoreCase("s")).map( x => x.getSingleRelationship("Comment_To_Event",Direction.OUTGOING)).groupBy(_.getEndNode()).valuesIterator.toList.filter( x => x.size > 0).map( x => x.sortBy(-_.getProperty("time").toString().toInt)).map( x => x(0).getStartNode().getSingleRelationship("Comment_Written_By",Direction.OUTGOING))
	     event_coms = event_coms.filterNot(x => commented_events.contains(x.getStartNode().getSingleRelationship("Comment_To_Event",Direction.OUTGOING).getEndNode()))
	     
	     var p_coms = coms_tot.filter(x => x.hasRelationship("Comment_To_Petition",Direction.OUTGOING)).filter( y => y.getSingleRelationship("Comment_To_Petition",Direction.OUTGOING).getOtherNode(y).getProperty("space").toString.toInt == 0  || !user_type.equalsIgnoreCase("s")).map( x => x.getSingleRelationship("Comment_To_Petition",Direction.OUTGOING)).groupBy(_.getEndNode()).valuesIterator.toList.filter( x => x.size > 0).map( x => x.sortBy(-_.getProperty("time").toString().toInt)).map( x => x(0).getStartNode().getSingleRelationship("Comment_Written_By",Direction.OUTGOING))
	     p_coms = p_coms.filterNot(x => commented_petitions.contains(x.getStartNode().getSingleRelationship("Comment_To_Petition",Direction.OUTGOING).getEndNode()))
	     
	     //follows.map(x => x.getRelationships("Comment_Written_By").asScala).flatten.toList.map( x => x.getStartNode())
	     var t_qtns = follows.map(x => x.getRelationships("Asked_Question").asScala.toList.filter( y => y.getOtherNode(x).getProperty("space").toString.toInt == 0  || !user_type.equalsIgnoreCase("s"))).flatten.groupBy(_.getEndNode()).valuesIterator.toList.filter( x => x.size > 0).map( x => x.sortBy(-_.getProperty("time").toString().toInt)).map( x => x(0))
	     t_qtns =  t_qtns.filterNot(x => questioned_townhalls.contains(x.getEndNode()))
	     
	     var t_coms = follows.map(x => x.getRelationships("Commented_On_Townhall").asScala.toList.filter( y => y.getOtherNode(x).getProperty("space").toString.toInt == 0  || !user_type.equalsIgnoreCase("s"))).flatten.groupBy(_.getEndNode()).valuesIterator.toList.filter( x => x.size > 0).map( x => x.sortBy(-_.getProperty("time").toString().toInt)).map( x => x(0))
	     t_coms =  t_coms.filterNot(x => commented_townhalls.contains(x.getEndNode()))
	     
	     var d_qtns = follows.map(x => x.getRelationships("Asked_Debate_Question").asScala.toList.filter( y => y.getOtherNode(x).getProperty("space").toString.toInt == 0  || !user_type.equalsIgnoreCase("s"))).flatten.groupBy(_.getEndNode()).valuesIterator.toList.filter( x => x.size > 0).map( x => x.sortBy(-_.getProperty("time").toString().toInt)).map( x => x(0))
	     d_qtns =  d_qtns.filterNot(x => qtn_debates.contains(x.getEndNode()))
	     
	     var d_args = follows.map(x => x.getRelationships("Started_Debate_Argument").asScala.toList.filter( y => y.getOtherNode(x).getProperty("space").toString.toInt == 0  || !user_type.equalsIgnoreCase("s"))).flatten.groupBy(_.getEndNode()).valuesIterator.toList.filter( x => x.size > 0).map( x => x.sortBy(-_.getProperty("time").toString().toInt)).map( x => x(0))
	     d_args =  d_args.filterNot(x => arg_debates.contains(x.getEndNode()))
	     
	     var d_coms = follows.map(x => x.getRelationships("Commented_On_Debate").asScala.toList.filter( y => y.getOtherNode(x).getProperty("space").toString.toInt == 0  || !user_type.equalsIgnoreCase("s"))).flatten.groupBy(_.getEndNode()).valuesIterator.toList.filter( x => x.size > 0).map( x => x.sortBy(-_.getProperty("time").toString().toInt)).map( x => x(0))
	     d_coms =  d_coms.filterNot(x => commented_debates.contains(x.getEndNode()))
	     
	     var sub_coms = follows.map(x => x.getRelationships("Comment_Written_By").asScala).flatten.toList.map(_.getStartNode).filter( x => x.hasRelationship("Comment_To_Comment",Direction.OUTGOING)).map( x => x.getSingleRelationship("Comment_To_Comment",Direction.OUTGOING)).groupBy(_.getEndNode()).valuesIterator.toList.filter( x => x.size > 0).map( x => x.sortBy(-_.getProperty("time").toString().toInt)).map( x => x(0).getStartNode().getSingleRelationship("Comment_Written_By",Direction.OUTGOING))
         sub_coms = sub_coms.filterNot(x => commented_coms.contains(x.getStartNode().getSingleRelationship("Comment_To_Comment",Direction.OUTGOING).getEndNode()))
	       
         // sorting all the actions(relations) based on time
	     rels = (space_follow_rels:::space_created_rels:::user_follow_rels:::art_voted_rels:::event_atnd_rels:::p_signed_rels:::user_rels:::event_invites:::rels:::rels2:::rels3:::rels5:::rels6:::rels7:::art_poll_rels:::event_poll_rels:::p_poll_rels:::art_com_rels:::event_com_rels:::p_com_rels:::art_coms:::event_coms:::p_coms:::event_notify_rels:::t_part_rels:::t_qtns:::t_coms:::d_part_rels:::d_qtns:::d_args:::d_coms).distinct.sortBy(-_.getProperty("time").toString().toInt)
	     val l = List("Is_Neo4j","QP_Content","QP_Type","QP_Tag","QP_Url","QP_Timestamp","QP_User","QP_User_FullName","QP_Article_Event_Owner","QP_Featured_Image","QP_Rating","QP_Refer_To","P_Reactions","P_Rating","Article_Event_ID")
	     def matchTest(x: org.neo4j.graphdb.Relationship) = x.getType().toString() match {
	       
	        case "Article_Written_By" =>
	          
	            val art_node = x.getStartNode()
	            val auth_node = x.getEndNode()
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = art_node.getProperty("article_title")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/" + art_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(art_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + art_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(art_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + art_node.getProperty("article_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"A","W",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), art_node.getProperty("article_featured_img"), "", "", art_node.getRelationships("Comment_To_Article").asScala.size,art_node.getRelationships("article_voteup").asScala.size, art_node.getProperty("article_id"))).toMap)
			
	        case "Space_Created_By" =>
	          
	            val space_node = x.getStartNode()
	            val auth_node = x.getEndNode()
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = space_node.getProperty("space_title")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/spaces/" + space_node.getProperty("space_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"S","C",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), space_node.getProperty("space_featured_img"), "", "", 0, 0, space_node.getProperty("space_id"))).toMap)
			
	        case "Townhall_Written_By" =>
	          
	            val t_node = x.getStartNode()
	            val auth_node = x.getEndNode()
	            val celeb_node = t_node.getSingleRelationship("Townhall_Of",Direction.OUTGOING).getEndNode()
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = t_node.getProperty("t_title")
	            map("date") = t_node.getProperty("t_date")
	            map("celeb") = celeb_node.getProperty("first_name") + " " + celeb_node.getProperty("last_name")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/" + t_node.getProperty("t_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"T","W",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", "", 0,0, t_node.getProperty("t_id"))).toMap)
			
	        case "Participated_In_Townhall" =>
	          
	            val t_node = x.getEndNode()
	            val user_node = x.getStartNode()
	            val auth_node = t_node.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode()
	            
	            val celeb_node = t_node.getSingleRelationship("Townhall_Of",Direction.OUTGOING).getEndNode()
	            
	            var part_users = t_node.getRelationships("Participated_In_Townhall").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(t_node))
		            
		            part_users = part_users.distinct
		            var part_list = List[String]()
		            for(each <- part_users)
		            {
		              part_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
		            }
	                
	                
	                
	                if( node != auth_node)
	                {
	                    var inter = part_users.intersect(follows)
		                if(inter.size > 0 )
		                {
		                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
		                  val foll = part_users.filter( x => follows.contains(x))(0)
		                  part_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
			              part_list = part_list.distinct
		                }
		                
		                if(part_list.contains(user_details))
		                {
		                  part_list ::= user_details
		                  part_list = part_list.distinct
		                }
	                }
	                
	                else if(node == auth_node && !user_type.equals("s"))
	                {
	                  part_list ::= user_details
		              part_list = part_list.distinct
	                }
	                
	                
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = t_node.getProperty("t_title")
	            map("date") = t_node.getProperty("t_date")
	            map("celeb") = celeb_node.getProperty("first_name") + " " + celeb_node.getProperty("last_name")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/" + t_node.getProperty("t_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"T","P",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", part_list.mkString(","), 0,0, t_node.getProperty("t_id"))).toMap)
			
	        case "Asked_Question" =>
	          
	            val t_node = x.getEndNode()
	            val user_node = x.getStartNode()
	            val auth_node = t_node.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode()
	            
	            val celeb_node = t_node.getSingleRelationship("Townhall_Of",Direction.OUTGOING).getEndNode()
	            
	            var part_users = t_node.getRelationships("Asked_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(t_node))
		            
		            part_users = part_users.distinct
		            var part_list = List[String]()
		            for(each <- part_users)
		            {
		              part_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
		            }
	                
	                
	                
	                if( node != auth_node)
	                {
	                    var inter = part_users.intersect(follows)
		                if(inter.size > 0 )
		                {
		                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
		                  val foll = part_users.filter( x => follows.contains(x))(0)
		                  part_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
			              part_list = part_list.distinct
		                }
		                
		                if(part_list.contains(user_details))
		                {
		                  part_list ::= user_details
		                  part_list = part_list.distinct
		                }
	                }
	                
	                else if(node == auth_node && !user_type.equals("s"))
	                {
	                  part_list ::= user_details
		              part_list = part_list.distinct
	                }
	                
	                
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = t_node.getProperty("t_title")
	            map("date") = t_node.getProperty("t_date")
	            map("celeb") = celeb_node.getProperty("first_name") + " " + celeb_node.getProperty("last_name")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/" + t_node.getProperty("t_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"T","Q",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", part_list.mkString(","), 0,0, t_node.getProperty("t_id"))).toMap)
			
	        case "Commented_On_Townhall" =>
	          
	            val t_node = x.getEndNode()
	            val user_node = x.getStartNode()
	            val auth_node = t_node.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode()
	            
	            val celeb_node = t_node.getSingleRelationship("Townhall_Of",Direction.OUTGOING).getEndNode()
	            
	            var part_users = t_node.getRelationships("Commented_On_Townhall").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(t_node))
		            
		            part_users = part_users.distinct
		            var part_list = List[String]()
		            for(each <- part_users)
		            {
		              part_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
		            }
	                
	                
	                
	                if( node != auth_node)
	                {
	                    var inter = part_users.intersect(follows)
		                if(inter.size > 0 )
		                {
		                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
		                  val foll = part_users.filter( x => follows.contains(x))(0)
		                  part_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
			              part_list = part_list.distinct
		                }
		                
		                if(part_list.contains(user_details))
		                {
		                  part_list ::= user_details
		                  part_list = part_list.distinct
		                }
	                }
	                
	                else if(node == auth_node && !user_type.equals("s"))
	                {
	                  part_list ::= user_details
		              part_list = part_list.distinct
	                }
	                
	                
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = t_node.getProperty("t_title")
	            map("date") = t_node.getProperty("t_date")
	            map("celeb") = celeb_node.getProperty("first_name") + " " + celeb_node.getProperty("last_name")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/" + t_node.getProperty("t_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"T","CM",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", part_list.mkString(","), 0,0, t_node.getProperty("t_id"))).toMap)
			
	        case "Debate_Written_By" =>
	          
	            val d_node = x.getStartNode()
	            val auth_node = x.getEndNode()
	            //val celeb_node = t_node.getSingleRelationship("Townhall_Of",Direction.OUTGOING).getEndNode()
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = d_node.getProperty("d_title")
	            map("date") = d_node.getProperty("d_date")
	            //map("celeb") = celeb_node.getProperty("first_name") + " " + celeb_node.getProperty("last_name")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/" + d_node.getProperty("d_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"D","W",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", "", 0,0, d_node.getProperty("d_id"))).toMap)
			
	        case "Participated_In_Debate" =>
	          
	            val d_node = x.getEndNode()
	            val user_node = x.getStartNode()
	            val auth_node = d_node.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode()
	            
	            //val celeb_node = t_node.getSingleRelationship("Townhall_Of",Direction.OUTGOING).getEndNode()
	            
	            var part_users = d_node.getRelationships("Participated_In_Debate").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(d_node))
		            
		            part_users = part_users.distinct
		            var part_list = List[String]()
		            for(each <- part_users)
		            {
		              part_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
		            }
	                
	                
	                
	                if( node != auth_node)
	                {
	                    var inter = part_users.intersect(follows)
		                if(inter.size > 0 )
		                {
		                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
		                  val foll = part_users.filter( x => follows.contains(x))(0)
		                  part_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
			              part_list = part_list.distinct
		                }
		                
		                if(part_list.contains(user_details))
		                {
		                  part_list ::= user_details
		                  part_list = part_list.distinct
		                }
	                }
	                
	                else if(node == auth_node && !user_type.equals("s"))
	                {
	                  part_list ::= user_details
		              part_list = part_list.distinct
	                }
	                
	                
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = d_node.getProperty("d_title")
	            map("date") = d_node.getProperty("d_date")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/" + d_node.getProperty("d_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"D","P",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", part_list.mkString(","), 0,0, d_node.getProperty("d_id"))).toMap)
			
	        case "Asked_Debate_Question" =>
	          
	            val d_node = x.getEndNode()
	            val user_node = x.getStartNode()
	            val auth_node = d_node.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode()
	            
	            //val celeb_node = t_node.getSingleRelationship("Townhall_Of",Direction.OUTGOING).getEndNode()
	            
	            var part_users = d_node.getRelationships("Asked_Debate_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(d_node))
		            
		            part_users = part_users.distinct
		            var part_list = List[String]()
		            for(each <- part_users)
		            {
		              part_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
		            }
	                
	                
	                
	                if( node != auth_node)
	                {
	                    var inter = part_users.intersect(follows)
		                if(inter.size > 0 )
		                {
		                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
		                  val foll = part_users.filter( x => follows.contains(x))(0)
		                  part_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
			              part_list = part_list.distinct
		                }
		                
		                if(part_list.contains(user_details))
		                {
		                  part_list ::= user_details
		                  part_list = part_list.distinct
		                }
	                }
	                
	                else if(node == auth_node && !user_type.equals("s"))
	                {
	                  part_list ::= user_details
		              part_list = part_list.distinct
	                }
	                
	                
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = d_node.getProperty("d_title")
	            map("date") = d_node.getProperty("d_date")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/" + d_node.getProperty("d_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"D","Q",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", part_list.mkString(","), 0,0, d_node.getProperty("d_id"))).toMap)
			
	        case "Started_Debate_Argument" =>
	          
	            val d_node = x.getEndNode()
	            val user_node = x.getStartNode()
	            val auth_node = d_node.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode()
	            
	            //val celeb_node = t_node.getSingleRelationship("Townhall_Of",Direction.OUTGOING).getEndNode()
	            
	            var part_users = d_node.getRelationships("Started_Debate_Argument").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(d_node))
		            
		            part_users = part_users.distinct
		            var part_list = List[String]()
		            for(each <- part_users)
		            {
		              part_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
		            }
	                
	                
	                
	                if( node != auth_node)
	                {
	                    var inter = part_users.intersect(follows)
		                if(inter.size > 0 )
		                {
		                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
		                  val foll = part_users.filter( x => follows.contains(x))(0)
		                  part_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
			              part_list = part_list.distinct
		                }
		                
		                if(part_list.contains(user_details))
		                {
		                  part_list ::= user_details
		                  part_list = part_list.distinct
		                }
	                }
	                
	                else if(node == auth_node && !user_type.equals("s"))
	                {
	                  part_list ::= user_details
		              part_list = part_list.distinct
	                }
	                
	                
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = d_node.getProperty("d_title")
	            map("date") = d_node.getProperty("d_date")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/" + d_node.getProperty("d_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"D","A",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", part_list.mkString(","), 0,0, d_node.getProperty("d_id"))).toMap)
			
	        case "Commented_On_Debate" =>
	          
	            val d_node = x.getEndNode()
	            val user_node = x.getStartNode()
	            val auth_node = d_node.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode()
	            
	            //val celeb_node = t_node.getSingleRelationship("Townhall_Of",Direction.OUTGOING).getEndNode()
	            
	            var part_users = d_node.getRelationships("Commented_On_Debate").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(d_node))
		            
		            part_users = part_users.distinct
		            var part_list = List[String]()
		            for(each <- part_users)
		            {
		              part_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
		            }
	                
	                
	                
	                if( node != auth_node)
	                {
	                    var inter = part_users.intersect(follows)
		                if(inter.size > 0 )
		                {
		                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
		                  val foll = part_users.filter( x => follows.contains(x))(0)
		                  part_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
			              part_list = part_list.distinct
		                }
		                
		                if(part_list.contains(user_details))
		                {
		                  part_list ::= user_details
		                  part_list = part_list.distinct
		                }
	                }
	                
	                else if(node == auth_node && !user_type.equals("s"))
	                {
	                  part_list ::= user_details
		              part_list = part_list.distinct
	                }
	                
	                
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = d_node.getProperty("d_title")
	            map("date") = d_node.getProperty("d_date")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/" + d_node.getProperty("d_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"D","CM",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", part_list.mkString(","), 0,0, d_node.getProperty("d_id"))).toMap)
			
	        case "article_voteup" => 
	           
	                val art_node = x.getEndNode()
	            
		            val user_node = x.getStartNode()
		            val auth_node = art_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(art_node)
		            
		            //var voted_rels = art_node.getRelationships("article_voteup").asScala.toList
		            var voted_users = art_node.getRelationships("article_voteup").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(art_node))
		            
		            voted_users = voted_users.distinct
		            var voted_list = List[String]()
		            for(each <- voted_users)
		            {
		              voted_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
		            }
	                
	                
	                
	                if( node != auth_node)
	                {
	                    var inter = voted_users.intersect(follows)
		                if(inter.size > 0 )
		                {
		                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
		                  val foll = voted_users.filter( x => follows.contains(x))(0)
		                  voted_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
			              voted_list = voted_list.distinct
		                }
		                
		                if(voted_list.contains(user_details))
		                {
		                  voted_list ::= user_details
		                  voted_list = voted_list.distinct
		                }
	                }
	                
	                else if(node == auth_node && !user_type.equals("s"))
	                {
	                  voted_list ::= user_details
		              voted_list = voted_list.distinct
	                }
	                
		            var map  = scala.collection.mutable.Map[String,Any]()
		            map("ttl") = art_node.getProperty("article_title")
		            val m = JSONObject(map.toMap).toString()
		            val url = "/" + art_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(art_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + art_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(art_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + art_node.getProperty("article_title_id")
		            list :+= JSONObject(l.zip(List(true,m,"A","V",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), art_node.getProperty("article_featured_img"), "", voted_list.mkString(","), art_node.getRelationships("Comment_To_Article").asScala.size,art_node.getRelationships("article_voteup").asScala.size , art_node.getProperty("article_id"))).toMap)
	        
		    case "Follows" => 
	           
	                val user2 = x.getEndNode()
	            
		            val user1 = x.getStartNode()
		            
		            val auth_node = user2
		            //var voted_rels = art_node.getRelationships("article_voteup").asScala.toList
		            var followers = user2.getRelationships("Follows",Direction.INCOMING).asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(user2))
		            
		            followers = followers.distinct
		            var followers_list = List[String]()
		            for(each <- followers)
		            {
		              followers_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
		            }
	                
	                
	                
	                if( node != user1)
	                {
	                    var inter = followers.intersect(follows)
		                if(inter.size > 0 )
		                {
		                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
		                  val foll = followers.filter( x => follows.contains(x))(0)
		                  followers_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
			              followers_list = followers_list.distinct
		                }
		                
		                if(followers_list.contains(user_details))
		                {
		                  followers_list ::= user_details
		                  followers_list = followers_list.distinct
		                }
	                }
	                
	                else if(node == user1 && !user_type.equals("s"))
	                {
	                  followers_list ::= user_details
		              followers_list = followers_list.distinct
	                }
	                
		            var map  = scala.collection.mutable.Map[String,Any]()
		            map("ttl") = user2.getProperty("first_name").toString() + " " + user2.getProperty("last_name").toString()
		            val m = JSONObject(map.toMap).toString()
		            val url = "/" + user2.getProperty("user_name")
		            list :+= JSONObject(l.zip(List(true,m,"U","F",url,x.getProperty("time"),user1.getProperty("user_name"),user1.getProperty("first_name") + " " + user1.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", followers_list.mkString(","), 0, 0, user2.getProperty("user_name"))).toMap)
	        
		    case "Space_Followed_By" => 
	           
	                val space_node = x.getStartNode()
	            
		            val user_node = x.getEndNode()
		            
		            val auth_node = space_node.getSingleRelationship("Space_Created_By",Direction.OUTGOING).getOtherNode(space_node)
		            //var voted_rels = art_node.getRelationships("article_voteup").asScala.toList
		            var followers = space_node.getRelationships("Space_Followed_By").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(space_node))
		            
		            followers = followers.distinct
		            var followers_list = List[String]()
		            for(each <- followers)
		            {
		              followers_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
		            }
	                
	                
	                
	                if( node != user_node)
	                {
	                    var inter = followers.intersect(follows)
		                if(inter.size > 0 )
		                {
		                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
		                  val foll = followers.filter( x => follows.contains(x))(0)
		                  followers_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
			              followers_list = followers_list.distinct
		                }
		                
		                if(followers_list.contains(user_details))
		                {
		                  followers_list ::= user_details
		                  followers_list = followers_list.distinct
		                }
	                }
	                
	                else if(node == user_node && !user_type.equals("s"))
	                {
	                  followers_list ::= user_details
		              followers_list = followers_list.distinct
	                }
	                
		            var map  = scala.collection.mutable.Map[String,Any]()
		            map("ttl") = space_node.getProperty("space_title")
		            val m = JSONObject(map.toMap).toString()
		            val url = "/spaces/" + space_node.getProperty("space_title_id")
		            list :+= JSONObject(l.zip(List(true,m,"S","F",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), space_node.getProperty("space_featured_img"), "", followers_list.mkString(","), 0, 0, space_node.getProperty("space_id"))).toMap)
	        
			case "Voted_To_Poll" => 
	           
	                val poll_node = x.getEndNode()
	                val user_node = x.getStartNode()
	                var item_node = poll_node
	                if(poll_node.hasRelationship("Poll_App_Of",Direction.OUTGOING))
	                {
	                    item_node = poll_node.getSingleRelationship("Poll_App_Of",Direction.OUTGOING).getEndNode()
		                val auth_node = item_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(item_node)
			            
	                    var voted_users = poll_node.getRelationships("Voted_To_Poll").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(poll_node))
		            
			            voted_users = voted_users.distinct
			            var voted_list = List[String]()
			            for(each <- voted_users)
			            {
			              voted_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			            }
		                
		                
		                
		                if(node != auth_node)
		                {
		                    var inter = voted_users.intersect(follows)
			                if(inter.size > 0)
			                {
			                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
			                  val foll = voted_users.filter( x => follows.contains(x))(0)
			                  voted_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
				              voted_list = voted_list.distinct
			                }
			                
			                if(voted_list.contains(user_details))
			                {
			                  voted_list ::= user_details
			                  voted_list = voted_list.distinct
			                }
		                }
		                
		                else if(node == auth_node && !user_type.equals("s"))
		                {
		                  voted_list ::= user_details
			              voted_list = voted_list.distinct
		                }
			            var map  = scala.collection.mutable.Map[String,Any]()
			            map("ttl") = item_node.getProperty("article_title")
			            val m = JSONObject(map.toMap).toString()
			            val url = "/" + item_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(item_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + item_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(item_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + item_node.getProperty("article_title_id")
			            list :+= JSONObject(l.zip(List(true,m,"A","P",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), item_node.getProperty("article_featured_img"), "", voted_list.mkString(","), item_node.getRelationships("Comment_To_Article").asScala.size,item_node.getRelationships("article_voteup").asScala.size , item_node.getProperty("article_id"))).toMap)
	            
	                }
	                
	                else if(poll_node.hasRelationship("Poll_App_Of_Event",Direction.OUTGOING))
	                {
	                    item_node = poll_node.getSingleRelationship("Poll_App_Of_Event",Direction.OUTGOING).getEndNode()
		                //item_node = poll_node.getSingleRelationship("Poll_App_Of",Direction.OUTGOING).getEndNode()
		                var voted_users = poll_node.getRelationships("Voted_To_Poll").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(poll_node))
		                val auth_node = item_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(item_node)
		                
			            voted_users = voted_users.distinct
			            var voted_list = List[String]()
			            for(each <- voted_users)
			            {
			              voted_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			            }
		                
	                    
	                    if(node != auth_node)
	                    {
			                var inter = voted_users.intersect(follows)
			                
			                
			                if(inter.size > 0)
			                {
			                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
			                  val foll = voted_users.filter( x => follows.contains(x))(0)
			                  voted_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
				              voted_list = voted_list.distinct
			                }
			                
			                if(voted_list.contains(user_details))
			                {
			                  voted_list ::= user_details
			                  voted_list = voted_list.distinct
			                }
	                    }
		                else if(node == auth_node && !user_type.equals("s"))
		                {
		                  voted_list ::= user_details
			              voted_list = voted_list.distinct
		                }
			            var map  = scala.collection.mutable.Map[String,Any]()
			            map("ttl") = item_node.getProperty("event_title")
			            map("date") = item_node.getProperty("event_date_time")
			            map("loc") = item_node.getProperty("event_location")
			            val m = JSONObject(map.toMap).toString()
			            val url = "/Events/" + item_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(item_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + item_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(item_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + item_node.getProperty("event_title_id")
			            list :+= JSONObject(l.zip(List(true,m,"E","P",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", voted_list.mkString(","), item_node.getRelationships("Comment_To_Event").asScala.size, 0, item_node.getProperty("event_id"))).toMap)
				
	                }
	                
	                else
	                {
	                  
	                    item_node = poll_node.getSingleRelationship("Poll_App_Of_Petition",Direction.OUTGOING).getEndNode()
		                //item_node = poll_node.getSingleRelationship("Poll_App_Of",Direction.OUTGOING).getEndNode()
		                var voted_users = poll_node.getRelationships("Voted_To_Poll").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(poll_node))
		                val auth_node = item_node.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getOtherNode(item_node)
		                
			            voted_users = voted_users.distinct
			            var voted_list = List[String]()
			            for(each <- voted_users)
			            {
			              voted_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			            }
		                
	                    
	                    if(node != auth_node)
	                    {
			                var inter = voted_users.intersect(follows)
			                
			                
			                if(inter.size > 0)
			                {
			                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
			                  val foll = voted_users.filter( x => follows.contains(x))(0)
			                  voted_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
				              voted_list = voted_list.distinct
			                }
			                
			                if(voted_list.contains(user_details))
			                {
			                  voted_list ::= user_details
			                  voted_list = voted_list.distinct
			                }
	                    }
		                else if(node == auth_node && !user_type.equals("s"))
		                {
		                  voted_list ::= user_details
			              voted_list = voted_list.distinct
		                }
			            var map  = scala.collection.mutable.Map[String,Any]()
			            map("ttl") = item_node.getProperty("p_title")
//			            map("date") = item_node.getProperty("event_date_time")
//			            map("loc") = item_node.getProperty("event_location")
			            val m = JSONObject(map.toMap).toString()
			            val url = "/petitions/" + item_node.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(item_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + item_node.getProperty("p_title_id")
			           // val l = List("QP_Content","QP_Type","QP_Tag","QP_Url","QP_Timestamp","QP_User","QP_User_FullName","QP_Article_Event_Owner","QP_Featured_Image","QP_Rating","QP_Refer_To","P_Reactions","P_Rating","Article_Event_ID")
	     
			            list :+= JSONObject(l.zip(List(true,m,"P","P",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", voted_list.mkString(","), item_node.getRelationships("Comment_To_Petition").asScala.size, item_node.getProperty("p_target").toString().toInt - item_node.getProperty("p_count").toString().toInt, item_node.getProperty("p_id"))).toMap)
				
	                
	                }
	          
	        case "User_Of_Article" => 
	           
	            val art_node = x.getEndNode()
	            val user_node = x.getStartNode()
	            val auth_node = art_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(art_node)
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = art_node.getProperty("article_title")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/" + art_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(art_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + art_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(art_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + art_node.getProperty("article_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"A","@",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), art_node.getProperty("article_featured_img"), "", user_node.getProperty("user_name") + ":" + user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), art_node.getRelationships("Comment_To_Article").asScala.size,art_node.getRelationships("article_voteup").asScala.size , art_node.getProperty("article_id"))).toMap)
				   
	          
	        case "Comment_Written_By" => 
	          
	              val com_node = x.getStartNode()
                  if(com_node.getSingleRelationship("Comment_To_Article",Direction.OUTGOING) != null)
		          {
		            
		            if(com_node.hasRelationship("Comment_To_Comment",Direction.OUTGOING))
		            {
//		                  println("hey")
			              val main_com = com_node.getSingleRelationship("Comment_To_Comment",Direction.OUTGOING).getOtherNode(com_node)
			              val art_node = com_node.getSingleRelationship("Comment_To_Article",Direction.OUTGOING).getOtherNode(com_node)
			              val auth_node = art_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(art_node)
				          
			              val user_node = x.getEndNode()
			              val maincom_auth_node = main_com.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(main_com)
			              var map  = scala.collection.mutable.Map[String,Any]()
			              map("ttl") = art_node.getProperty("article_title")
			              map("cmnt") = com_node.getProperty("comment_content")
			              val m = JSONObject(map.toMap).toString()
			              val url = "/" + art_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(art_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + art_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(art_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + art_node.getProperty("article_title_id") + "#" + com_node.getProperty("comment_id")
			            
			              var com_users = main_com.getRelationships("Comment_To_Comment",Direction.INCOMING).asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode().getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct
			              var com_list = List[String]()
			              for(each <- com_users)
			              {
			                com_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			              }
			              
			                if(node != maincom_auth_node)
			                {
			                	var inter = com_users.intersect(follows)
				                if(inter.size > 0)
				                {
				                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
				                  val foll = com_users.filter( x => follows.contains(x))(0)
				                  com_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
					              com_list = com_list.distinct
				                }
				                
				                if(com_list.contains(user_details))
				                {
				                  com_list ::= user_details
				                  com_list = com_list.distinct
				                }
			                }
			                
			                else if(node == maincom_auth_node && !user_type.equals("s"))
			                {
			                  com_list ::= user_details
				              com_list = com_list.distinct
			                }
			              list :+= JSONObject(l.zip(List(true,m,"A","CC",url,x.getProperty("time"),maincom_auth_node.getProperty("user_name"),maincom_auth_node.getProperty("first_name") + " " + maincom_auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), art_node.getProperty("article_featured_img"), "" , com_list.mkString(",") , art_node.getRelationships("Comment_To_Article").asScala.size,art_node.getRelationships("article_voteup").asScala.size , art_node.getProperty("article_id"))).toMap)
				          
		              
		            }
		            else
		            {
//		                  println("heyy")
			              val art_node = com_node.getSingleRelationship("Comment_To_Article",Direction.OUTGOING).getOtherNode(com_node)
				          val user_node = x.getEndNode()
				          val auth_node = art_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(art_node)
				          var map  = scala.collection.mutable.Map[String,Any]()
				          map("ttl") = art_node.getProperty("article_title")
				          map("cmnt") = com_node.getProperty("comment_content")
				          val m = JSONObject(map.toMap).toString()
				          val url = "/" + art_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(art_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + art_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(art_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + art_node.getProperty("article_title_id") + "#" + com_node.getProperty("comment_id")
			            
			              var com_users = art_node.getRelationships("Comment_To_Article").asScala.toList.filterNot(x => x.getStartNode().hasRelationship("Comment_To_Comment",Direction.OUTGOING)).sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(art_node).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct
			              var com_list = List[String]()
			              for(each <- com_users)
			              {
			                com_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			              }
			              
			                if(node != auth_node)
			                {
				                var inter = com_users.intersect(follows)
				                if(inter.size > 0)
				                {
				                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
				                  val foll = com_users.filter( x => follows.contains(x))(0)
				                  com_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
					              com_list = com_list.distinct
				                }
				                
				                if(com_list.contains(user_details))
				                {
				                  com_list ::= user_details
				                  com_list = com_list.distinct
				                }
			                }
			                
			                else if(node == auth_node && !user_type.equals("s"))
			                {
			                  com_list ::= user_details
				              com_list = com_list.distinct
			                }
			              list :+= JSONObject(l.zip(List(true,m,"A","CM",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), art_node.getProperty("article_featured_img"), "" , com_list.mkString(",") , art_node.getRelationships("Comment_To_Article").asScala.size,art_node.getRelationships("article_voteup").asScala.size , art_node.getProperty("article_id"))).toMap)
				          
			          
		            }
		          }
		          else if(com_node.getSingleRelationship("Comment_To_Event",Direction.OUTGOING) != null)
		          {
		            if(com_node.hasRelationship("Comment_To_Comment",Direction.OUTGOING))
		            {
		                  val main_com = com_node.getSingleRelationship("Comment_To_Comment",Direction.OUTGOING).getOtherNode(com_node)
		              
			              val event_node = com_node.getSingleRelationship("Comment_To_Event",Direction.OUTGOING).getOtherNode(com_node)
			              val user_node = x.getEndNode()
			              
			              val auth_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
			            
			              val maincom_auth_node = main_com.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(main_com)
			              
			              var map  = scala.collection.mutable.Map[String,Any]()
			              map("ttl") = event_node.getProperty("event_title")
			              map("date") = event_node.getProperty("event_date_time")
			              map("loc") = event_node.getProperty("event_location") 
			              map("cmnt") = com_node.getProperty("comment_content")
			              val m = JSONObject(map.toMap).toString()
			              val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id") + "#" + com_node.getProperty("comment_id")
			            
			              var com_users = main_com.getRelationships("Comment_To_Comment",Direction.INCOMING).asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode().getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct
			              var com_list = List[String]()
			              for(each <- com_users)
			              {
			                com_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			              }
		                  
		                  
		                    if(node != maincom_auth_node)
		                    {
			                    var inter = com_users.intersect(follows)
				                if(inter.size > 0)
				                {
				                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
				                  val foll = com_users.filter( x => follows.contains(x))(0)
				                  com_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
					              com_list = com_list.distinct
				                }
				                
				                if(com_list.contains(user_details))
				                {
				                  com_list ::= user_details
				                  com_list = com_list.distinct
				                }
		                    }
		                    
		                    else if(node == maincom_auth_node && !user_type.equals("s"))
			                {
			                  com_list ::= user_details
				              com_list = com_list.distinct
			                }
			              
			              list :+= JSONObject(l.zip(List(true,m,"E","CC",url,x.getProperty("time"),maincom_auth_node.getProperty("user_name"),maincom_auth_node.getProperty("first_name") + " " + maincom_auth_node.getProperty("last_name"), auth_node.getProperty("user_name"),  event_node.getProperty("event_featured_img"), "" , com_list.mkString(","), event_node.getRelationships("Comment_To_Event").asScala.size, 0, event_node.getProperty("event_id"))).toMap)
						  
		              
		            }
		            else
		            {
		              
			              val event_node = com_node.getSingleRelationship("Comment_To_Event",Direction.OUTGOING).getOtherNode(com_node)
			              val user_node = x.getEndNode()
			              val auth_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
			            
			              var map  = scala.collection.mutable.Map[String,Any]()
			              map("ttl") = event_node.getProperty("event_title")
			              map("date") = event_node.getProperty("event_date_time")
			              map("loc") = event_node.getProperty("event_location") 
			              map("cmnt") = com_node.getProperty("comment_content")
			              val m = JSONObject(map.toMap).toString()
			              val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id") + "#" + com_node.getProperty("comment_id")
			            
			              var com_users = event_node.getRelationships("Comment_To_Event").asScala.toList.filterNot(x => x.getStartNode().hasRelationship("Comment_To_Comment",Direction.OUTGOING)).sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(event_node).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct
			              var com_list = List[String]()
			              for(each <- com_users)
			              {
			                com_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			              }
			              
			              
			                if(node != auth_node)
			                {
				                var inter = com_users.intersect(follows)
				                if(inter.size > 0)
				                {
				                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
				                  val foll = com_users.filter( x => follows.contains(x))(0)
				                  com_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
					              com_list = com_list.distinct
				                }
				                
				                if(com_list.contains(user_details))
				                {
				                  com_list ::= user_details
				                  com_list = com_list.distinct
				                }
			                }
			                
			                else if(node == auth_node && !user_type.equals("s"))
			                {
			                  com_list ::= user_details
				              com_list = com_list.distinct
			                }
			              
			              list :+= JSONObject(l.zip(List(true,m,"E","CM",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"),   event_node.getProperty("event_featured_img"), "" , com_list.mkString(","), event_node.getRelationships("Comment_To_Event").asScala.size, 0, event_node.getProperty("event_id"))).toMap)
						  
			          
		            }
		            
		      }
	              
		          else
		          {
		            if(com_node.hasRelationship("Comment_To_Comment",Direction.OUTGOING))
		            {
		                  val main_com = com_node.getSingleRelationship("Comment_To_Comment",Direction.OUTGOING).getOtherNode(com_node)
		              
			              val p_node = com_node.getSingleRelationship("Comment_To_Petition",Direction.OUTGOING).getOtherNode(com_node)
			              val user_node = x.getEndNode()
			              
			              val auth_node = p_node.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getOtherNode(p_node)
			            
			              val maincom_auth_node = main_com.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(main_com)
			              
			              var map  = scala.collection.mutable.Map[String,Any]()
			              map("ttl") = p_node.getProperty("p_title")
//			              map("date") = event_node.getProperty("event_date_time")
//			              map("loc") = event_node.getProperty("event_location") 
			              map("cmnt") = com_node.getProperty("comment_content")
			              val m = JSONObject(map.toMap).toString()
			              val url = "/petitions/" + p_node.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(p_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + p_node.getProperty("p_title_id") + "#" + com_node.getProperty("comment_id")
			           // p_node.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(p_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0)
			              var com_users = main_com.getRelationships("Comment_To_Comment",Direction.INCOMING).asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode().getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct
			              var com_list = List[String]()
			              for(each <- com_users)
			              {
			                com_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			              }
		                  
		                  
		                    if(node != maincom_auth_node)
		                    {
			                    var inter = com_users.intersect(follows)
				                if(inter.size > 0)
				                {
				                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
				                  val foll = com_users.filter( x => follows.contains(x))(0)
				                  com_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
					              com_list = com_list.distinct
				                }
				                
				                if(com_list.contains(user_details))
				                {
				                  com_list ::= user_details
				                  com_list = com_list.distinct
				                }
		                    }
		                    
		                    else if(node == maincom_auth_node && !user_type.equals("s"))
			                {
			                  com_list ::= user_details
				              com_list = com_list.distinct
			                }
			              
			              list :+= JSONObject(l.zip(List(true,m,"P","CC",url,x.getProperty("time"),maincom_auth_node.getProperty("user_name"),maincom_auth_node.getProperty("first_name") + " " + maincom_auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "" , com_list.mkString(","), p_node.getRelationships("Comment_To_Petition").asScala.size, p_node.getProperty("p_target").toString().toInt - p_node.getProperty("p_count").toString().toInt, p_node.getProperty("p_id"))).toMap)
						  
		              
		            }
		            else
		            {
		              
			              val p_node = com_node.getSingleRelationship("Comment_To_Petition",Direction.OUTGOING).getOtherNode(com_node)
			              val user_node = x.getEndNode()
			              val auth_node = p_node.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getOtherNode(p_node)
			            
			              var map  = scala.collection.mutable.Map[String,Any]()
			              map("ttl") = p_node.getProperty("p_title")
//			              map("date") = event_node.getProperty("event_date_time")
//			              map("loc") = event_node.getProperty("event_location") 
			              map("cmnt") = com_node.getProperty("comment_content")
			              val m = JSONObject(map.toMap).toString()
			              val url = "/petitions/" + p_node.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(p_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + p_node.getProperty("p_title_id") + "#" + com_node.getProperty("comment_id")
			            
			              var com_users = p_node.getRelationships("Comment_To_Petition").asScala.toList.filterNot(x => x.getStartNode().hasRelationship("Comment_To_Comment",Direction.OUTGOING)).sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(p_node).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct
			              var com_list = List[String]()
			              for(each <- com_users)
			              {
			                com_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			              }
			              
			              
			                if(node != auth_node)
			                {
				                var inter = com_users.intersect(follows)
				                if(inter.size > 0)
				                {
				                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
				                  val foll = com_users.filter( x => follows.contains(x))(0)
				                  com_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
					              com_list = com_list.distinct
				                }
				                
				                if(com_list.contains(user_details))
				                {
				                  com_list ::= user_details
				                  com_list = com_list.distinct
				                }
			                }
			                
			                else if(node == auth_node && !user_type.equals("s"))
			                {
			                  com_list ::= user_details
				              com_list = com_list.distinct
			                }
			              
			              list :+= JSONObject(l.zip(List(true,m,"P","CM",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "" , com_list.mkString(","), p_node.getRelationships("Comment_To_Petition").asScala.size, p_node.getProperty("p_target").toString().toInt - p_node.getProperty("p_count").toString().toInt, p_node.getProperty("p_id"))).toMap)
						  
			          
		            }
		            
		      }
	              
	        
	       case "Event_Created_By" =>
	          
	            val event_node = x.getStartNode()
	            val auth_node = x.getEndNode()
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = event_node.getProperty("event_title")
	            map("date") = event_node.getProperty("event_date_time")
	            map("loc") = event_node.getProperty("event_location")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"E","C",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"),   event_node.getProperty("event_featured_img"), "", "", event_node.getRelationships("Comment_To_Event").asScala.size, 0, event_node.getProperty("event_id"))).toMap)
			
	       case "Invited_To_Event" =>
	            
	            val user_node = nodeIndex.get("id",x.getProperty("user").toString()).getSingle()
	            val event_node = x.getEndNode()
	            val auth_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = event_node.getProperty("event_title")
	            map("date") = event_node.getProperty("event_date_time")
	            map("loc") = event_node.getProperty("event_location")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"E","I",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"),   event_node.getProperty("event_featured_img"), "", "", event_node.getRelationships("Comment_To_Event").asScala.size, 0, event_node.getProperty("event_id"))).toMap)
			
	       case "Notified_In_Event" =>
	            
	            val user_node = nodeIndex.get("id",x.getProperty("user").toString()).getSingle()
	            val event_node = x.getEndNode()
	            val auth_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = event_node.getProperty("event_title")
	            map("date") = event_node.getProperty("event_date_time")
	            map("loc") = event_node.getProperty("event_location")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"E","N",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"),   event_node.getProperty("event_featured_img"), "", "", event_node.getRelationships("Comment_To_Event").asScala.size, 0, event_node.getProperty("event_id"))).toMap)
			
	       case "Is_Attending" =>
	            
		            val event_node = x.getEndNode()
		            val user_node = x.getStartNode()
		            var attend_users = event_node.getRelationships("Is_Attending").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(event_node))
			        attend_users = attend_users.distinct
			        var attend_list = List[String]()
			        for(each <- attend_users)
			        {
			            attend_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			        }
		            val auth_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
		            
		            if(node != auth_node)
		            {
		                var inter = attend_users.intersect(follows)
		                
		                
		                if(inter.size > 0)
		                {
		                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
		                  val foll = attend_users.filter( x => follows.contains(x))(0)
		                  attend_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
			              attend_list = attend_list.distinct
		                }
		                
		                if(attend_list.contains(user_details))
		                {
		                  attend_list ::= user_details
		                  attend_list = attend_list.distinct
		                }
		            }
		            
		            else if(node == auth_node && !user_type.equals("s"))
			        {
			                  attend_list ::= user_details
				              attend_list = attend_list.distinct
			        }
		            var map  = scala.collection.mutable.Map[String,Any]()
		            map("ttl") = event_node.getProperty("event_title")
		            map("date") = event_node.getProperty("event_date_time")
		            map("loc") = event_node.getProperty("event_location")
		            val m = JSONObject(map.toMap).toString()
		            val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id")
		            list :+= JSONObject(l.zip(List(true,m,"E","A",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"),   event_node.getProperty("event_featured_img"), "", attend_list.mkString(","), event_node.getRelationships("Comment_To_Event").asScala.size, 0, event_node.getProperty("event_id"))).toMap)
				
	       case "User_Of_Event" =>
	            
	            val user_node = x.getStartNode()
	            val event_node = x.getEndNode()
	            val auth_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = event_node.getProperty("event_title")
	            map("date") = event_node.getProperty("event_date_time")
	            map("loc") = event_node.getProperty("event_location")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"E","@",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"),   event_node.getProperty("event_featured_img"), "", "", event_node.getRelationships("Comment_To_Event").asScala.size, 0, event_node.getProperty("event_id"))).toMap)
		  
	       case "Petition_Written_By" =>
	          
	            val p_node = x.getStartNode()
	            val auth_node = x.getEndNode()
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = p_node.getProperty("p_title")
//	            map("date") = event_node.getProperty("event_date_time")
//	            map("loc") = event_node.getProperty("event_location")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/petitions/" + p_node.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(p_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + p_node.getProperty("p_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"P","C",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", "", p_node.getRelationships("Comment_To_Petition").asScala.size, p_node.getProperty("p_target"), p_node.getProperty("p_id"))).toMap)
			
	       case "Signed_Petition" =>
	            
		            val p_node = x.getEndNode()
		            val user_node = x.getStartNode()
		            var signed_users = p_node.getRelationships("Signed_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(p_node))
			        signed_users = signed_users.distinct
			        var signed_list = List[String]()
			        for(each <- signed_users)
			        {
			            signed_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			        }
		            val auth_node = p_node.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getOtherNode(p_node)
		            
		            if(node != auth_node)
		            {
		                var inter = signed_users.intersect(follows)
		                
		                
		                if(inter.size > 0)
		                {
		                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
		                  val foll = signed_users.filter( x => follows.contains(x))(0)
		                  signed_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
			              signed_list = signed_list.distinct
		                }
		                
		                if(signed_list.contains(user_details))
		                {
		                  signed_list ::= user_details
		                  signed_list = signed_list.distinct
		                }
		            }
		            
		            else if(node == auth_node && !user_type.equals("s"))
			        {
			                  signed_list ::= user_details
				              signed_list = signed_list.distinct
			        }
		            var map  = scala.collection.mutable.Map[String,Any]()
		            map("ttl") = p_node.getProperty("p_title")
//		            map("date") = event_node.getProperty("event_date_time")
//		            map("loc") = event_node.getProperty("event_location")
		            val m = JSONObject(map.toMap).toString()
		            val url = "/petitions/" + p_node.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(p_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + p_node.getProperty("p_title_id")
		            list :+= JSONObject(l.zip(List(true,m,"P","S",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", signed_list.mkString(","), p_node.getRelationships("Comment_To_Petition").asScala.size, p_node.getProperty("p_target").toString().toInt - p_node.getProperty("p_count").toString().toInt, p_node.getProperty("p_id"))).toMap)
			
	       case "User_Of_Petition" =>
	            
	            val user_node = x.getStartNode()
	            val p_node = x.getEndNode()
	            val auth_node = p_node.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getOtherNode(p_node)
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = p_node.getProperty("p_title")
//	            map("date") = event_node.getProperty("event_date_time")
//	            map("loc") = event_node.getProperty("event_location")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/petitions/" + p_node.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(p_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + p_node.getProperty("p_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"P","@",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", "", p_node.getRelationships("Comment_To_Petition").asScala.size, p_node.getProperty("p_target").toString().toInt - p_node.getProperty("p_count").toString().toInt, p_node.getProperty("p_id"))).toMap)
		  
	       case "User_Of_Comment" =>
	            
	            val user_node = x.getStartNode()
	            val com_node = x.getEndNode()
	            val com_auth = com_node.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode
	            
	            if(com_node.getSingleRelationship("Comment_To_Article",Direction.OUTGOING) != null)
	            {
		            val art_node = com_node.getSingleRelationship("Comment_To_Article",Direction.OUTGOING).getOtherNode(com_node)
		            //val user_node = x.getEndNode()
		            val auth_node = art_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(art_node)
		            var map  = scala.collection.mutable.Map[String,Any]()
		            map("ttl") = art_node.getProperty("article_title")
		            map("cmnt") = com_node.getProperty("comment_content")
		            val m = JSONObject(map.toMap).toString()
		            val url = "/" + art_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(art_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + art_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(art_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + art_node.getProperty("article_title_id") + "#" + com_node.getProperty("comment_id")
		            list :+= JSONObject(l.zip(List(true,m,"A","@C",url,x.getProperty("time"),com_auth.getProperty("user_name"),com_auth.getProperty("first_name") + " " + com_auth.getProperty("last_name"), auth_node.getProperty("user_name"), art_node.getProperty("article_featured_img"), "" , "" , art_node.getRelationships("Comment_To_Article").asScala.size,art_node.getRelationships("article_voteup").asScala.size , art_node.getProperty("article_id"))).toMap)
			        
	            }
	            else if(com_node.getSingleRelationship("Comment_To_Event",Direction.OUTGOING) != null)
	            {
		            val event_node = com_node.getSingleRelationship("Comment_To_Event",Direction.OUTGOING).getOtherNode(com_node)
		            //val user_node = x.getEndNode()
		            val auth_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
		            
		            var map  = scala.collection.mutable.Map[String,Any]()
		            map("ttl") = event_node.getProperty("event_title")
		            map("date") = event_node.getProperty("event_date_time")
			        map("loc") = event_node.getProperty("event_location")
		            map("cmnt") = com_node.getProperty("comment_content")
		            val m = JSONObject(map.toMap).toString()
		            val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id") + "#" + com_node.getProperty("comment_id")
		            list :+= JSONObject(l.zip(List(true,m,"E","@C",url,x.getProperty("time"),com_auth.getProperty("user_name"),com_auth.getProperty("first_name") + " " + com_auth.getProperty("last_name"), auth_node.getProperty("user_name"),   event_node.getProperty("event_featured_img"), "" , "", event_node.getRelationships("Comment_To_Event").asScala.size, 0, event_node.getProperty("event_id"))).toMap)
					
	            
	            }
	            else
	            {
		            val p_node = com_node.getSingleRelationship("Comment_To_Petition",Direction.OUTGOING).getOtherNode(com_node)
		            //val user_node = x.getEndNode()
		            val auth_node = p_node.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getOtherNode(p_node)
		            
		            var map  = scala.collection.mutable.Map[String,Any]()
		            map("ttl") = p_node.getProperty("p_title")
//		            map("date") = event_node.getProperty("event_date_time")
//			        map("loc") = event_node.getProperty("event_location")
		            map("cmnt") = com_node.getProperty("comment_content")
		            val m = JSONObject(map.toMap).toString()
		            val url = "/petitions/" + p_node.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(p_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + p_node.getProperty("p_title_id") + "#" + com_node.getProperty("comment_id")
		            list :+= JSONObject(l.zip(List(true,m,"P","@C",url,x.getProperty("time"),com_auth.getProperty("user_name"),com_auth.getProperty("first_name") + " " + com_auth.getProperty("last_name"), auth_node.getProperty("user_name"), "", "" , "", p_node.getRelationships("Comment_To_Petition").asScala.size, p_node.getProperty("p_target").toString().toInt - p_node.getProperty("p_count").toString().toInt, p_node.getProperty("p_id"))).toMap)
					
	            
	            }
	            		
	       case _ => ""
	     }
	     
         // slicing the relations based on count asked
	     for(each <- rels.slice(prev_cnt,(prev_cnt+count)))
	     {
	       matchTest(each)
	     }
	     
	 }
     JSONArray(list.distinct).toString()
     
  }
  
  
  
  def notifications(user_name:String,count:Int,prev_cnt:Int): String =
  {
    
     withTx {
     implicit neo =>
    
     val nodeIndex = getNodeIndex("user").get
     val node = nodeIndex.get("id",user_name).getSingle()
     var ret = ""    
     var list  = List[Any]()
     if(node != null)
	 {
         val cur_time = (System.currentTimeMillis() /1000).toInt
         
         val t = node.getProperty("notify_time").toString().toInt
         val user_details = node.getProperty("user_name").toString() + ":" + node.getProperty("first_name").toString() + " " + node.getProperty("last_name").toString()
           
         var follows = List[org.neo4j.graphdb.Node]()
         var event_invites =  List[org.neo4j.graphdb.Relationship]()
         var art_com_rels =  List[org.neo4j.graphdb.Relationship]()
         var event_com_rels =  List[org.neo4j.graphdb.Relationship]()
         var p_com_rels =  List[org.neo4j.graphdb.Relationship]()
         var event_atnd_rels =  List[org.neo4j.graphdb.Relationship]()
         var sub_com_rels =  List[org.neo4j.graphdb.Relationship]()
         var sub_coms =  List[org.neo4j.graphdb.Relationship]()
         var user_rels =  List[org.neo4j.graphdb.Relationship]()
         var art_voted_rels =  List[org.neo4j.graphdb.Relationship]()
         var p_sign_rels =  List[org.neo4j.graphdb.Relationship]()
         var art_poll_rels =  List[org.neo4j.graphdb.Relationship]()
         var event_poll_rels =  List[org.neo4j.graphdb.Relationship]()
         var p_poll_rels =  List[org.neo4j.graphdb.Relationship]()
         var event_notify_rels =  List[org.neo4j.graphdb.Relationship]()
         var commented_arts =  List[org.neo4j.graphdb.Node]()
         var commented_events =  List[org.neo4j.graphdb.Node]()
         var commented_coms =  List[org.neo4j.graphdb.Node]()
         //var voted_arts =  List[org.neo4j.graphdb.Node]()
        // var signed_petitions =  List[org.neo4j.graphdb.Node]()
         //var atnd_events =  List[org.neo4j.graphdb.Node]()
         var responded_polls =  List[org.neo4j.graphdb.Node]()
         var vote_up_rels = List[org.neo4j.graphdb.Relationship]()
         var vote_down_rels = List[org.neo4j.graphdb.Relationship]()
         var user_follow_rels = List[org.neo4j.graphdb.Relationship]()
         
         
             // extracting all the actions that were done one the items published by the user
             event_invites = node.getRelationships("Invited_To_Event","User_Of_Article","User_Of_Event","User_Of_Comment").asScala.toList
	         var art_list = node.getRelationships("Article_Written_By").asScala.map(_.getOtherNode(node)).toList
             art_com_rels = art_list.map( x => x.getRelationships("Comment_To_Article").asScala.toList.filterNot( x => x.getStartNode.hasRelationship("Comment_To_Comment",Direction.OUTGOING)).sortBy(-_.getProperty("time").toString().toInt).slice(0,1).map(_.getOtherNode(x))).flatten.map( x => x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING))
		     var event_list = node.getRelationships("Event_Created_By").asScala.map(_.getOtherNode(node)).toList
             var p_list = node.getRelationships("Petition_Written_By").asScala.map(_.getOtherNode(node)).toList
             
	         p_poll_rels = p_list.map( x => x.getRelationships("Poll_App_Of_Petition").asScala.toList.map(_.getOtherNode(x))).flatten.map( x => x.getRelationships("Voted_To_Poll").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten
		     
	         art_poll_rels = art_list.map( x => x.getRelationships("Poll_App_Of").asScala.toList.map(_.getOtherNode(x))).flatten.map( x => x.getRelationships("Voted_To_Poll").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten
		     event_poll_rels = event_list.map( x => x.getRelationships("Poll_App_Of_Event").asScala.toList.map(_.getOtherNode(x))).flatten.map( x => x.getRelationships("Voted_To_Poll").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten
		     
	         event_com_rels = event_list.map( x => x.getRelationships("Comment_To_Event").asScala.toList.filterNot( x => x.getStartNode.hasRelationship("Comment_To_Comment",Direction.OUTGOING)).sortBy(-_.getProperty("time").toString().toInt).slice(0,1).map(_.getOtherNode(x))).flatten.map( x => x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING))
		     event_atnd_rels = event_list.map( x => x.getRelationships("Is_Attending").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten
		     art_voted_rels = art_list.map( x => x.getRelationships("article_voteup").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten
             p_sign_rels = p_list.map( x => x.getRelationships("Signed_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten
             p_com_rels = p_list.map( x => x.getRelationships("Comment_To_Petition").asScala.toList.filterNot( x => x.getStartNode.hasRelationship("Comment_To_Comment",Direction.OUTGOING)).sortBy(-_.getProperty("time").toString().toInt).slice(0,1).map(_.getOtherNode(x))).flatten.map( x => x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING))
		     
             user_follow_rels = node.getRelationships("Follows",Direction.INCOMING).asScala.toList
		     var com_list = node.getRelationships("Comment_Written_By").asScala.toList
		     
		     var p_completed_list = node.getRelationships("Signs_Completed").asScala.toList
		     
		     sub_com_rels = com_list.map(_.getOtherNode(node)).map( x => x.getRelationships("Comment_To_Comment",Direction.INCOMING).asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1).map(_.getOtherNode(x))).flatten.distinct.map( x => x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING))

		     event_notify_rels = node.getRelationships("Notified_In_Event").asScala.toList
	    	
             vote_up_rels = com_list.map( x => x.getStartNode().getRelationships("comment_voteup").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten
		     vote_down_rels = com_list.map( x => x.getStartNode().getRelationships("comment_votedown").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten
		     val t_qtn_rels = node.getRelationships("Asked_Question").asScala.toList.filter(x => x.getProperty("q_status").toString().toInt == 1)
             val d_for_rels = node.getRelationships("For").asScala.toList.filter(x => x.getProperty("d_status").toString().toInt == 1)
             val d_against_rels = node.getRelationships("Against").asScala.toList.filter(x => x.getProperty("d_status").toString().toInt == 1)
             val t_part_rels = node.getRelationships("Participated_In_Townhall").asScala.toList.filter(x => x.getEndNode().getProperty("t_date").toString().toInt >= cur_time  && x.getEndNode().getProperty("t_date").toString().toInt <= (cur_time+1800)  )
             val t_part = t_part_rels.map( x => x.getEndNode())
             val d_part_rels = node.getRelationships("Participated_In_Debate").asScala.toList.filter(x => x.getEndNode().getProperty("d_date").toString().toInt >= cur_time  && x.getEndNode().getProperty("d_date").toString().toInt <= (cur_time+1800)  )
             //val d_part = d_part_rels.map( x => x.getEndNode())
             
             val t_related = node.getRelationships("Participated_In_Townhall").asScala.toList.map(_.getEndNode().getSingleRelationship("Townhall_Of",Direction.OUTGOING).getEndNode()).distinct.map( x => x.getRelationships("Townhall_Of").asScala.toList.map(_.getStartNode())).flatten.distinct.filter(x => !t_part.contains(x) && x.getProperty("t_date").toString().toInt >= cur_time  && x.getProperty("t_date").toString().toInt <= (cur_time+1800)  ).map( x => x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING))
		     //val d_related = node.getRelationships("Participated_In_Debate").asScala.toList.map(_.getEndNode().getSingleRelationship("Townhall_Of",Direction.OUTGOING).getEndNode()).distinct.map( x => x.getRelationships("Townhall_Of").asScala.toList.map(_.getStartNode())).flatten.distinct.filter(x => !t_part.contains(x) && x.getProperty("t_date").toString().toInt >= cur_time  && x.getProperty("t_date").toString().toInt <= (cur_time+1800)  ).map( x => x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING))
		     
             // sorting all the actions(relations) based on time
	     var rels = (user_follow_rels:::p_completed_list:::t_qtn_rels:::p_sign_rels:::p_poll_rels:::p_com_rels:::art_voted_rels:::event_atnd_rels:::user_rels:::event_invites:::art_poll_rels:::event_poll_rels:::art_com_rels:::event_com_rels:::sub_com_rels:::event_notify_rels:::vote_up_rels:::vote_down_rels).distinct.sortBy(-_.getProperty("time").toString().toInt)
         rels = (t_part_rels:::d_part_rels:::rels)
	     // sending notofications count when count = 0 and prev_cnt = 0
	     if(count == 0 && prev_cnt == 0)
	     {
	       ret = rels.filter( x => x.getProperty("time").toString().toInt > t ).size + ""
	     }
         
	     else
		 {
		     val k = List("Is_Neo4j","N_Article_Event_Title","N_Content","N_Type","N_Tag","N_Link","N_Timestamp","N_Author","N_Author_Full_Name","N_Feature_Image","N_Refer_To","Article_Event_ID","N_New")
		     val cur_time = (System.currentTimeMillis() /1000).toInt
		     def matchTest(x: org.neo4j.graphdb.Relationship) = x.getType().toString() match {
		       
		        case "article_voteup" => 
		           
		                val art_node = x.getEndNode()
		                val user_node = x.getStartNode()
			            val auth_node = art_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(art_node)
			            
			            //var voted_rels = art_node.getRelationships("article_voteup").asScala.toList
			            var voted_users = art_node.getRelationships("article_voteup").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(art_node))
			            
			            voted_users = voted_users.distinct
			            var voted_list = List[String]()
			            for(each <- voted_users)
			            {
			              voted_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			            }
		                
		                var n_new = 0
		                if(x.getProperty("time").toString().toInt > t)
		                {
		                  n_new = 1
		                }
		               
			            val url = "/" + art_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(art_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + art_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(art_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + art_node.getProperty("article_title_id")
			            list :+= JSONObject(k.zip(List(true,art_node.getProperty("article_title"),"","A","V",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), art_node.getProperty("article_featured_img"), voted_list.mkString(","), art_node.getProperty("article_id"),n_new)).toMap)
		        
			    case "Follows" => 
		           
		                val user2 = x.getEndNode()
		                val user1 = x.getStartNode()
			            val auth_node = user1
			            
		                var n_new = 0
		                if(x.getProperty("time").toString().toInt > t)
		                {
		                  n_new = 1
		                }
		               
			            val url =  "/" + user1.getProperty("user_name")
			            list :+= JSONObject(k.zip(List(true,user1.getProperty("user_name"),"","U","F",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), "", "", user1.getProperty("user_name"),n_new)).toMap)
		        
		        case "Asked_Question" => 
		           
		          
		                
		                val t_node = x.getEndNode()
		            
			            val user_node = x.getStartNode()
			            val auth_node = t_node.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getOtherNode(t_node)
			            
			            
		                var n_new = 0
		                if(x.getProperty("time").toString().toInt > t)
		                {
		                  n_new = 1
		                }
		               
		                val url = "/" + t_node.getProperty("t_title_id")
	                    list :+= JSONObject(k.zip(List(true,t_node.getProperty("t_title"),"","T","QA",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), "", "", t_node.getProperty("t_id"),n_new)).toMap)
		        
	            case "Participated_In_Townhall" => 
		           
		                val t_node = x.getEndNode()
		                val user_node = x.getStartNode()
			            val auth_node = t_node.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getOtherNode(t_node)
			            var n_new = 1
		                val url = "/" + t_node.getProperty("t_title_id")
	                    list :+= JSONObject(k.zip(List(true,t_node.getProperty("t_title"),"","T","R",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), "", "", t_node.getProperty("t_id"),n_new)).toMap)
		        
	            case "Townhall_Written_By" => 
		           
		                val t_node = x.getStartNode()
		                //val user_node = x.getStartNode()
			            val auth_node = x.getEndNode()
			            var n_new = 1
		                val url = "/" + t_node.getProperty("t_title_id")
	                    list :+= JSONObject(k.zip(List(true,t_node.getProperty("t_title"),"","T","RO",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), "", "", t_node.getProperty("t_id"),n_new)).toMap)
		        
	            case "Participated_In_Debate" => 
		           
		                val d_node = x.getEndNode()
		                val user_node = x.getStartNode()
			            val auth_node = d_node.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getOtherNode(d_node)
			            var n_new = 1
		                val url = "/" + d_node.getProperty("d_title_id")
	                    list :+= JSONObject(k.zip(List(true,d_node.getProperty("d_title"),"","D","R",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), "", "", d_node.getProperty("d_id"),n_new)).toMap)
		        
	            case "For" => 
		           
		                val d_node = x.getEndNode()
		                val user_node = x.getStartNode()
			            val auth_node = d_node.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getOtherNode(d_node)
			            var n_new = 0
		                if(x.getProperty("time").toString().toInt > t)
		                {
		                  n_new = 1
		                }
		                val url = "/" + d_node.getProperty("d_title_id")
	                    list :+= JSONObject(k.zip(List(true,d_node.getProperty("d_title"),"","D","FS",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), "", "", d_node.getProperty("d_id"),n_new)).toMap)
		         
	            case "Against" => 
		           
		                val d_node = x.getEndNode()
		                val user_node = x.getStartNode()
			            val auth_node = d_node.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getOtherNode(d_node)
			            var n_new = 0
		                if(x.getProperty("time").toString().toInt > t)
		                {
		                  n_new = 1
		                }
		                val url = "/" + d_node.getProperty("d_title_id")
	                    list :+= JSONObject(k.zip(List(true,d_node.getProperty("d_title"),"","D","AS",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), "", "", d_node.getProperty("d_id"),n_new)).toMap)
		         
			    case "Signed_Petition" => 
		           
		          
		                
		                val p_node = x.getEndNode()
		            
			            val user_node = x.getStartNode()
			            val auth_node = p_node.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getOtherNode(p_node)
			            
			            //var voted_rels = art_node.getRelationships("article_voteup").asScala.toList
			            var signed_users = p_node.getRelationships("Signed_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(p_node))
			            
			            signed_users = signed_users.distinct
			            var signed_list = List[String]()
			            for(each <- signed_users)
			            {
			              signed_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			            }
		                
		                var n_new = 0
		                if(x.getProperty("time").toString().toInt > t)
		                {
		                  n_new = 1
		                }
		                
		                //list :+= JSONObject(l.zip(List(m,"P","S",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", signed_list.mkString(","), p_node.getRelationships("Comment_To_Petition").asScala.size, p_node.getProperty("p_count"), p_node.getProperty("p_id"),0)).toMap)
			
			            val url = "/petitions/" + p_node.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(p_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + p_node.getProperty("p_title_id")
		                list :+= JSONObject(k.zip(List(true,p_node.getProperty("p_title"),"","P","S",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"),p_node.getProperty("p_img_url"), signed_list.mkString(","), p_node.getProperty("p_id"),n_new)).toMap)
		         
		        case "Signs_Completed" => 
		           
		                val p_node = x.getEndNode()
		                val user_node = x.getStartNode()
			            val auth_node = p_node.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getOtherNode(p_node)
			            
			            //var voted_rels = art_node.getRelationships("article_voteup").asScala.toList
			            var signed_users = p_node.getRelationships("Signed_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(p_node))
			            
			            signed_users = signed_users.distinct
			            var signed_list = List[String]()
			            for(each <- signed_users)
			            {
			              signed_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			            }
		                
		                var n_new = 0
		                if(x.getProperty("time").toString().toInt > t)
		                {
		                  n_new = 1
		                }
		                
		                //list :+= JSONObject(l.zip(List(m,"P","S",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", signed_list.mkString(","), p_node.getRelationships("Comment_To_Petition").asScala.size, p_node.getProperty("p_count"), p_node.getProperty("p_id"),0)).toMap)
			
			            val url = "/petitions/" + p_node.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(p_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + p_node.getProperty("p_title_id")
		                list :+= JSONObject(k.zip(List(true,p_node.getProperty("p_title"),"","P","SC",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"),p_node.getProperty("p_img_url"), signed_list.mkString(","), p_node.getProperty("p_id"),n_new)).toMap)
		         
				case "Voted_To_Poll" => 
		           
		                val poll_node = x.getEndNode()
		                val user_node = x.getStartNode()
		                var item_node = poll_node
		                if(poll_node.hasRelationship("Poll_App_Of",Direction.OUTGOING))
		                {
		                    item_node = poll_node.getSingleRelationship("Poll_App_Of",Direction.OUTGOING).getEndNode()
			                val auth_node = item_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(item_node)
				            
		                    var voted_users = poll_node.getRelationships("Voted_To_Poll").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(poll_node))
			            
				            voted_users = voted_users.distinct
				            var voted_list = List[String]()
				            for(each <- voted_users)
				            {
				              voted_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
				            }
			                
			                
				            var n_new = 0
			                if(x.getProperty("time").toString().toInt > t)
			                {
			                  n_new = 1
			                }
				            val url = "/" + item_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(item_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + item_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(item_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + item_node.getProperty("article_title_id")
				            list :+= JSONObject(k.zip(List(true,item_node.getProperty("article_title"),"","A","P",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), item_node.getProperty("article_featured_img"), voted_list.mkString(","), item_node.getProperty("article_id"),n_new)).toMap)
		            
		                }
		                
		                else if(poll_node.hasRelationship("Poll_App_Of_Event",Direction.OUTGOING))
		                {
		                    item_node = poll_node.getSingleRelationship("Poll_App_Of_Event",Direction.OUTGOING).getEndNode()
			                //item_node = poll_node.getSingleRelationship("Poll_App_Of",Direction.OUTGOING).getEndNode()
			                var voted_users = poll_node.getRelationships("Voted_To_Poll").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(poll_node))
			                val auth_node = item_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(item_node)
			                
				            voted_users = voted_users.distinct
				            var voted_list = List[String]()
				            for(each <- voted_users)
				            {
				              voted_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
				            }
			                
		                    var n_new = 0
			                if(x.getProperty("time").toString().toInt > t)
			                {
			                  n_new = 1
			                }
		                   
				            val url = "/Events/" + item_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(item_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + item_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(item_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + item_node.getProperty("event_title_id")
				            list :+= JSONObject(k.zip(List(true,item_node.getProperty("event_title"),"D:"+item_node.getProperty("event_date_time"),"E","P",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), "", voted_list.mkString(","), item_node.getProperty("event_id"),n_new,0)).toMap)
		            
		                }
		                
		                else
		                {
		                  
		                    item_node = poll_node.getSingleRelationship("Poll_App_Of_Petition",Direction.OUTGOING).getEndNode()
			                val auth_node = item_node.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getOtherNode(item_node)
				            
		                    var voted_users = poll_node.getRelationships("Voted_To_Poll").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(poll_node))
			            
				            voted_users = voted_users.distinct
				            var voted_list = List[String]()
				            for(each <- voted_users)
				            {
				              voted_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
				            }
			                
			                
				            var n_new = 0
			                if(x.getProperty("time").toString().toInt > t)
			                {
			                  n_new = 1
			                }
				            
				            val url = "/petitions/" + item_node.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(item_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + item_node.getProperty("p_title_id")
		                    //list :+= JSONObject(k.zip(List(p_node.getProperty("p_title"),"","P","S",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"),"", signed_list.mkString(","), p_node.getProperty("p_id"),n_new)).toMap)
		         
				            //val url = "/" + item_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(item_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + item_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(item_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + item_node.getProperty("article_title_id")
				            list :+= JSONObject(k.zip(List(true,item_node.getProperty("p_title"),"","P","P",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), item_node.getProperty("p_img_url"), voted_list.mkString(","), item_node.getProperty("p_id"),n_new)).toMap)
		            
		                
		                }
		          
		        case "User_Of_Article" => 
		           
		            val art_node = x.getEndNode()
		            val user_node = x.getStartNode()
		            val auth_node = art_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(art_node)
		            
		            var n_new = 0
		            if(x.getProperty("time").toString().toInt > t)
		            {
		              n_new = 1
		            }
		            val url = "/" + art_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(art_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + art_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(art_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + art_node.getProperty("article_title_id")
		            list :+= JSONObject(k.zip(List(true,art_node.getProperty("article_title"),"","A","@",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), art_node.getProperty("article_featured_img"), auth_node.getProperty("user_name").toString() + ":" + auth_node.getProperty("first_name").toString() + " " + auth_node.getProperty("last_name").toString(), art_node.getProperty("article_id"),n_new)).toMap)
		               
		          
		        case "Comment_Written_By" => 
		          
		              val com_node = x.getStartNode()
	                  if(com_node.getSingleRelationship("Comment_To_Article",Direction.OUTGOING) != null)
			          {
			            
			            if(com_node.hasRelationship("Comment_To_Comment",Direction.OUTGOING))
			            {
	//		                  println("hey")
				              val main_com = com_node.getSingleRelationship("Comment_To_Comment",Direction.OUTGOING).getOtherNode(com_node)
				              val art_node = com_node.getSingleRelationship("Comment_To_Article",Direction.OUTGOING).getOtherNode(com_node)
				              val auth_node = art_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(art_node)
					          
				              val user_node = x.getEndNode()
				              val maincom_auth_node = main_com.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(main_com)
				              
				              val url = "/" + art_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(art_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + art_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(art_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + art_node.getProperty("article_title_id") + "#" + com_node.getProperty("comment_id")
				            
				              var com_users = main_com.getRelationships("Comment_To_Comment",Direction.INCOMING).asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode().getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct
				              var com_list = List[String]()
				              for(each <- com_users)
				              {
				                com_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
				              }
				              var n_new = 0
				              if(x.getProperty("time").toString().toInt > t)
				              {
				                n_new = 1
				              }
				              list :+= JSONObject(k.zip(List(true,art_node.getProperty("article_title"),"","A","CC",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), art_node.getProperty("article_featured_img"), com_list.mkString(","), art_node.getProperty("article_id"),n_new)).toMap)
		            
			              
			            }
			            else
			            {
	//		                  println("heyy")
				              val art_node = com_node.getSingleRelationship("Comment_To_Article",Direction.OUTGOING).getOtherNode(com_node)
					          val user_node = x.getEndNode()
					          val auth_node = art_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(art_node)
					          
					          val url = "/" + art_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(art_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + art_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(art_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + art_node.getProperty("article_title_id") + "#" + com_node.getProperty("comment_id")
				            
				              var com_users = art_node.getRelationships("Comment_To_Article").asScala.toList.filterNot(x => x.getStartNode().hasRelationship("Comment_To_Comment",Direction.OUTGOING)).sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(art_node).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct
				              var com_list = List[String]()
				              for(each <- com_users)
				              {
				                com_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
				              }
				              var n_new = 0
				                if(x.getProperty("time").toString().toInt > t)
				                {
				                  n_new = 1
				                }
				              list :+= JSONObject(k.zip(List(true,art_node.getProperty("article_title"),"","A","CM",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), art_node.getProperty("article_featured_img"), com_list.mkString(","), art_node.getProperty("article_id"),n_new)).toMap)
		            
				          
			            }
			          }
			          else if(com_node.getSingleRelationship("Comment_To_Event",Direction.OUTGOING) != null)
			          {
			            if(com_node.hasRelationship("Comment_To_Comment",Direction.OUTGOING))
			            {
			                  val main_com = com_node.getSingleRelationship("Comment_To_Comment",Direction.OUTGOING).getOtherNode(com_node)
			              
				              val event_node = com_node.getSingleRelationship("Comment_To_Event",Direction.OUTGOING).getOtherNode(com_node)
				              val user_node = x.getEndNode()
				              
				              val auth_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
				            
				              val maincom_auth_node = main_com.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(main_com)
				              
				              val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id") + "#" + com_node.getProperty("comment_id")
				            
				              var com_users = main_com.getRelationships("Comment_To_Comment",Direction.INCOMING).asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode().getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct
				              var com_list = List[String]()
				              for(each <- com_users)
				              {
				                com_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
				              }
			                  
			                  var n_new = 0
				                if(x.getProperty("time").toString().toInt > t)
				                {
				                  n_new = 1
				                }
				              list :+= JSONObject(k.zip(List(true,event_node.getProperty("event_title"),"D:"+event_node.getProperty("event_date_time"),"E","CC",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"),   event_node.getProperty("event_featured_img"), com_list.mkString(","), event_node.getProperty("event_id"),n_new,0)).toMap)
		            
			              
			            }
			            else
			            {
			              
				              val event_node = com_node.getSingleRelationship("Comment_To_Event",Direction.OUTGOING).getOtherNode(com_node)
				              val user_node = x.getEndNode()
				              val auth_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
				            
				              val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id") + "#" + com_node.getProperty("comment_id")
				            
				              var com_users = event_node.getRelationships("Comment_To_Event").asScala.toList.filterNot(x => x.getStartNode().hasRelationship("Comment_To_Comment",Direction.OUTGOING)).sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(event_node).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct
				              var com_list = List[String]()
				              for(each <- com_users)
				              {
				                com_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
				              }
				              
				              
				              var n_new = 0
				                if(x.getProperty("time").toString().toInt > t)
				                {
				                  n_new = 1
				                }
				              list :+= JSONObject(k.zip(List(true,event_node.getProperty("event_title"),"D:"+event_node.getProperty("event_date_time"),"E","CM",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"),   event_node.getProperty("event_featured_img"), com_list.mkString(","), event_node.getProperty("event_id"),n_new,0)).toMap)
		            
				          
			            }
			            
			      }
		              
		          else
			          {
			            if(com_node.hasRelationship("Comment_To_Comment",Direction.OUTGOING))
			            {
			                  val main_com = com_node.getSingleRelationship("Comment_To_Comment",Direction.OUTGOING).getOtherNode(com_node)
			              
				              val p_node = com_node.getSingleRelationship("Comment_To_Petition",Direction.OUTGOING).getOtherNode(com_node)
				              val user_node = x.getEndNode()
				              
				              val auth_node = p_node.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getOtherNode(p_node)
				            
				              val maincom_auth_node = main_com.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(main_com)
				              
				              val url = "/petitions/" + p_node.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(p_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + p_node.getProperty("p_title_id") + "#" + com_node.getProperty("comment_id")
		                    
				              var com_users = main_com.getRelationships("Comment_To_Comment",Direction.INCOMING).asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode().getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct
				              var com_list = List[String]()
				              for(each <- com_users)
				              {
				                com_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
				              }
			                  
			                  var n_new = 0
				                if(x.getProperty("time").toString().toInt > t)
				                {
				                  n_new = 1
				                }
				              list :+= JSONObject(k.zip(List(true,p_node.getProperty("p_title"),"","P","CC",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), p_node.getProperty("p_img_url"), com_list.mkString(","), p_node.getProperty("p_id"),n_new)).toMap)
		            
			              
			            }
			            else
			            {
			              
				              val p_node = com_node.getSingleRelationship("Comment_To_Petition",Direction.OUTGOING).getOtherNode(com_node)
				              val user_node = x.getEndNode()
				              val auth_node = p_node.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getOtherNode(p_node)
				            
				              val url = "/petitions/" + p_node.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(p_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + p_node.getProperty("p_title_id") + "#" + com_node.getProperty("comment_id")
		                    
				              var com_users = p_node.getRelationships("Comment_To_Petition").asScala.toList.filterNot(x => x.getStartNode().hasRelationship("Comment_To_Comment",Direction.OUTGOING)).sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(p_node).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct
				              var com_list = List[String]()
				              for(each <- com_users)
				              {
				                com_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
				              }
				              
				              
				              var n_new = 0
				              if(x.getProperty("time").toString().toInt > t)
				              {
				                n_new = 1
				              }
				              list :+= JSONObject(k.zip(List(true,p_node.getProperty("p_title"),"","P","CM",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), p_node.getProperty("p_img_url"), com_list.mkString(","), p_node.getProperty("p_id"),n_new)).toMap)
		            
				          
			            }
			            
			      }
		        
		       
		       case "Invited_To_Event" =>
		            
		            val user_node = nodeIndex.get("id",x.getProperty("user").toString()).getSingle()
		            val event_node = x.getEndNode()
		            
		            val auth_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
		            
		            
		            var n_new = 0
		                if(x.getProperty("time").toString().toInt > t)
		                {
		                  n_new = 1
		                }
		            val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id")
		            list :+= JSONObject(k.zip(List(true,event_node.getProperty("event_title"),"D:"+event_node.getProperty("event_date_time"),"E","I",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"),   event_node.getProperty("event_featured_img"), user_node.getProperty("user_name").toString() + ":" + user_node.getProperty("first_name").toString() + " " + user_node.getProperty("last_name").toString(), event_node.getProperty("event_id"),n_new,0)).toMap)
		            
		       case "Notified_In_Event" =>
		            
		            val user_node = nodeIndex.get("id",x.getProperty("user").toString()).getSingle()
		            val event_node = x.getEndNode()
		            val auth_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
		            var n_new = 0
		                if(x.getProperty("time").toString().toInt > t)
		                {
		                  n_new = 1
		                }
		            val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id")
		            list :+= JSONObject(k.zip(List(true,event_node.getProperty("event_title"),"D:"+event_node.getProperty("event_date_time"),"E","N",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"),   event_node.getProperty("event_featured_img"), user_node.getProperty("user_name").toString() + ":" + user_node.getProperty("first_name").toString() + " " + user_node.getProperty("last_name").toString(), event_node.getProperty("event_id"),n_new,0)).toMap)
		            
		       case "Is_Attending" =>
		            
			            val event_node = x.getEndNode()
			            val user_node = x.getStartNode()
			            var attend_users = event_node.getRelationships("Is_Attending").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(event_node))
				        attend_users = attend_users.distinct
				        var attend_list = List[String]()
				        for(each <- attend_users)
				        {
				            attend_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
				        }
			            val auth_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
			            
			            var n_new = 0
		                if(x.getProperty("time").toString().toInt > t)
		                {
		                  n_new = 1
		                }
			            val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id")
			            list :+= JSONObject(k.zip(List(true,event_node.getProperty("event_title"),"D:"+event_node.getProperty("event_date_time"),"E","A",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"),   event_node.getProperty("event_featured_img"), attend_list.mkString(","), event_node.getProperty("event_id"),n_new,0)).toMap)
		            
		       case "User_Of_Event" =>
		            
		            val user_node = x.getStartNode()
		            val event_node = x.getEndNode()
		            val auth_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
		            var n_new = 0
		                if(x.getProperty("time").toString().toInt > t)
		                {
		                  n_new = 1
		                }
		            val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id")
		            list :+= JSONObject(k.zip(List(true,event_node.getProperty("event_title"),"D:"+event_node.getProperty("event_date_time"),"E","@",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"),   event_node.getProperty("event_featured_img"), auth_node.getProperty("user_name").toString() + ":" + auth_node.getProperty("first_name").toString() + " " + auth_node.getProperty("last_name").toString(), event_node.getProperty("event_id"),n_new,0)).toMap)
		            
		       case "User_Of_Comment" =>
		            
		            val user_node = x.getStartNode()
		            val com_node = x.getEndNode()
		            val com_auth = com_node.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode
		            
		            if(com_node.getSingleRelationship("Comment_To_Article",Direction.OUTGOING) != null)
		            {
			            val art_node = com_node.getSingleRelationship("Comment_To_Article",Direction.OUTGOING).getOtherNode(com_node)
			            //val user_node = x.getEndNode()
			            val auth_node = art_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(art_node)
			            var n_new = 0
		                if(x.getProperty("time").toString().toInt > t)
		                {
		                  n_new = 1
		                }
			            val url = "/" + art_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(art_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + art_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(art_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + art_node.getProperty("article_title_id") + "#" + com_node.getProperty("comment_id")
			            list :+= JSONObject(k.zip(List(true,art_node.getProperty("article_title"),"","A","@C",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), art_node.getProperty("article_featured_img"), com_auth.getProperty("user_name").toString() + ":" + com_auth.getProperty("first_name").toString() + " " + com_auth.getProperty("last_name").toString(), art_node.getProperty("article_id"),n_new)).toMap)
		            
		            }
		            else if(com_node.getSingleRelationship("Comment_To_Event",Direction.OUTGOING) != null)
		            {
			            val event_node = com_node.getSingleRelationship("Comment_To_Event",Direction.OUTGOING).getOtherNode(com_node)
			            //val user_node = x.getEndNode()
			            val auth_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
			            var n_new = 0
		                if(x.getProperty("time").toString().toInt > t)
		                {
		                  n_new = 1
		                }
			            val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id") + "#" + com_node.getProperty("comment_id")
			            list :+= JSONObject(k.zip(List(true,event_node.getProperty("event_title"),"D:"+event_node.getProperty("event_date_time"),"E","@C",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"),   event_node.getProperty("event_featured_img"), com_auth.getProperty("user_name").toString() + ":" + com_auth.getProperty("first_name").toString() + " " + com_auth.getProperty("last_name").toString(), event_node.getProperty("event_id"),n_new,0)).toMap)
		            
		            
		            }
		            else
		            {
		                val p_node = com_node.getSingleRelationship("Comment_To_Petition",Direction.OUTGOING).getOtherNode(com_node)
			            //val user_node = x.getEndNode()
			            val auth_node = p_node.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getOtherNode(p_node)
			            var n_new = 0
		                if(x.getProperty("time").toString().toInt > t)
		                {
		                  n_new = 1
		                }
			            val url = "/petitions/" + p_node.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(p_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + p_node.getProperty("p_title_id") + "#" + com_node.getProperty("comment_id")
		                list :+= JSONObject(k.zip(List(true,p_node.getProperty("p_title"),"","P","@C",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), p_node.getProperty("p_img_url"), com_auth.getProperty("user_name").toString() + ":" + com_auth.getProperty("first_name").toString() + " " + com_auth.getProperty("last_name").toString(), p_node.getProperty("p_id"),n_new)).toMap)
		            
		            }
		       
		       case "comment_voteup" =>
		            
		            val user_node = x.getStartNode()
		            val com_node = x.getEndNode()
		            val com_auth = com_node.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode
		            
		            if(com_node.hasRelationship("Comment_To_Article",Direction.OUTGOING))
		            {
			            val art_node = com_node.getSingleRelationship("Comment_To_Article",Direction.OUTGOING).getOtherNode(com_node)
			            //val user_node = x.getEndNode()
			            val auth_node = art_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(art_node)
			            
			            var voted_users = com_node.getRelationships("comment_voteup").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(com_node))
			            
			            voted_users = voted_users.distinct
			            var voted_list = List[String]()
			            for(each <- voted_users)
			            {
			              voted_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			            }
			            var n_new = 0
		                if(x.getProperty("time").toString().toInt > t)
		                {
		                  n_new = 1
		                }
			            val url = "/" + art_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(art_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + art_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(art_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + art_node.getProperty("article_title_id") + "#" + com_node.getProperty("comment_id")
			            list :+= JSONObject(k.zip(List(true,art_node.getProperty("article_title"),"","A","U",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), art_node.getProperty("article_featured_img"), voted_list.mkString(","), art_node.getProperty("article_id"),n_new)).toMap)
		            
		            }
		            else if(com_node.hasRelationship("Comment_To_Event",Direction.OUTGOING))
		            {
			            val event_node = com_node.getSingleRelationship("Comment_To_Event",Direction.OUTGOING).getOtherNode(com_node)
			            //val user_node = x.getEndNode()
			            val auth_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
			            var voted_users = com_node.getRelationships("comment_voteup").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(com_node))
			            
			            voted_users = voted_users.distinct
			            var voted_list = List[String]()
			            for(each <- voted_users)
			            {
			              voted_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			            }
			            
			            
			            var n_new = 0
		                if(x.getProperty("time").toString().toInt > t)
		                {
		                  n_new = 1
		                }
			            val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id") + "#" + com_node.getProperty("comment_id")
			            list :+= JSONObject(k.zip(List(true,event_node.getProperty("event_title"),"D:"+event_node.getProperty("event_date_time"),"E","U",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"),   event_node.getProperty("event_featured_img"), voted_list.mkString(","), event_node.getProperty("event_id"),n_new,0)).toMap)
		            
		            
		            }
		            else
		            {
			            
			                val p_node = com_node.getSingleRelationship("Comment_To_Petition",Direction.OUTGOING).getOtherNode(com_node)
				              //val user_node = x.getEndNode()
				              val auth_node = p_node.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getOtherNode(p_node)
				            
				              val url = "/petitions/" + p_node.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(p_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + p_node.getProperty("p_title_id") + "#" + com_node.getProperty("comment_id")
		                    
				              var voted_users = com_node.getRelationships("comment_voteup").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(com_node))
			            
				            voted_users = voted_users.distinct
				            var voted_list = List[String]()
				            for(each <- voted_users)
				            {
				              voted_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
				            }
				            
				            
				            var n_new = 0
			                if(x.getProperty("time").toString().toInt > t)
			                {
			                  n_new = 1
			                }
				              list :+= JSONObject(k.zip(List(true,p_node.getProperty("p_title"),"","P","U",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), p_node.getProperty("p_img_url"), voted_list.mkString(","), p_node.getProperty("p_id"),n_new)).toMap)
		            
		            }
		            
		            
		       case "comment_votedown" =>
		            
		            val user_node = x.getStartNode()
		            val com_node = x.getEndNode()
		            val com_auth = com_node.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode
		            
		            if(com_node.hasRelationship("Comment_To_Article",Direction.OUTGOING))
		            {
			            val art_node = com_node.getSingleRelationship("Comment_To_Article",Direction.OUTGOING).getOtherNode(com_node)
			            //val user_node = x.getEndNode()
			            val auth_node = art_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(art_node)
			            
			            var voted_users = com_node.getRelationships("comment_votedown").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(com_node))
			            
			            voted_users = voted_users.distinct
			            var voted_list = List[String]()
			            for(each <- voted_users)
			            {
			              voted_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			            }
			            var n_new = 0
		                if(x.getProperty("time").toString().toInt > t)
		                {
		                  n_new = 1
		                }
			            val url = "/" + art_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(art_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + art_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(art_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + art_node.getProperty("article_title_id") + "#" + com_node.getProperty("comment_id")
			            list :+= JSONObject(k.zip(List(true,art_node.getProperty("article_title"),"","A","D",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), art_node.getProperty("article_featured_img"), voted_list.mkString(","), art_node.getProperty("article_id"),n_new)).toMap)
		            
		            }
		            else if(com_node.hasRelationship("Comment_To_Event",Direction.OUTGOING))
		            {
			            val event_node = com_node.getSingleRelationship("Comment_To_Event",Direction.OUTGOING).getOtherNode(com_node)
			            //val user_node = x.getEndNode()
			            val auth_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
			            var voted_users = com_node.getRelationships("comment_votedown").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(com_node))
			            
			            voted_users = voted_users.distinct
			            var voted_list = List[String]()
			            for(each <- voted_users)
			            {
			              voted_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			            }
			            var n_new = 0
		                if(x.getProperty("time").toString().toInt > t)
		                {
		                  n_new = 1
		                }
			            val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id") + "#" + com_node.getProperty("comment_id")
			            list :+= JSONObject(k.zip(List(true,event_node.getProperty("event_title"),"D:"+event_node.getProperty("event_date_time"),"E","D",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"),   event_node.getProperty("event_featured_img"), voted_list.mkString(","), event_node.getProperty("event_id"),n_new,0)).toMap)
		            
		            
		            }
		            
		            else
		            {
			            
			                val p_node = com_node.getSingleRelationship("Comment_To_Petition",Direction.OUTGOING).getOtherNode(com_node)
				              //val user_node = x.getEndNode()
				              val auth_node = p_node.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getOtherNode(p_node)
				            
				              val url = "/petitions/" + p_node.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(p_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + p_node.getProperty("p_title_id") + "#" + com_node.getProperty("comment_id")
		                    
				              var voted_users = com_node.getRelationships("comment_votedown").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(com_node))
			            
				            voted_users = voted_users.distinct
				            var voted_list = List[String]()
				            for(each <- voted_users)
				            {
				              voted_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
				            }
				            
				            
				            var n_new = 0
			                if(x.getProperty("time").toString().toInt > t)
			                {
			                  n_new = 1
			                }
				              list :+= JSONObject(k.zip(List(true,p_node.getProperty("p_title"),"","P","D",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), p_node.getProperty("p_img_url"), voted_list.mkString(","), p_node.getProperty("p_id"),n_new)).toMap)
		            
		            }
		            
		       case _ => ""
		     }
		     
		     //slicing relations based on count and prev_cnt 
		     for(each <- rels.slice(prev_cnt,(prev_cnt+count)))
		     {
		       matchTest(each)
		     }
		         
		     node.setProperty("notify_time",cur_time)
		     ret = JSONArray(list.distinct).toString()
		 }
	 }
     
     
     
     ret
     }
     
  }
  
  
  def context(user_name:String,item_type:String,item_id:String,count:Int,prev_cnt:Int): String =
  {
    
       val SubcatIndex = getNodeIndex("sub_category").get
       val article_content_index = getNodeIndex("article_content").get
       val event_content_index = getNodeIndex("event_content").get
       val petition_content_index = getNodeIndex("petition_content").get
       var hash_nodes =  List[org.neo4j.graphdb.Node]()
       var other_arts =  List[org.neo4j.graphdb.Node]()
       var other_events =  List[org.neo4j.graphdb.Node]()
       var other_petitions =  List[org.neo4j.graphdb.Node]()
//       var cat =  List[String]()
       var content = ""
       val toRemove = "~`!@#$%^&*()_-+=*{}[]:;'|/".toSet
       var is_space = false
       if(item_type.equals("L"))
       {
         // extracting hash nodes
         val TilesIndex = getNodeIndex("tiles").get
         val PetitionIndex = getNodeIndex("petition").get
         val CatIndex = getNodeIndex("category").get
         val ArticleIndex = getNodeIndex("article").get
         val EventIndex = getNodeIndex("event").get
         var news_node = TilesIndex.get("id","latest").getSingle()
//         var cat_node = CatIndex.get("name",cat).getSingle()
         
         if(news_node != null)
         {
          
           
            var latest_tiles = news_node.getProperty("value").toString()
            if(!latest_tiles.equals(""))
            {
                val news = latest_tiles.split(",").toList.map(x => ArticleIndex.get("id",x).getSingle()).filter(x => x!= null)
			    hash_nodes = news.map( x => x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.map(_.getOtherNode(x)) ).flatten
            
            }
		    
		    		    
		    var petitions = List[org.neo4j.graphdb.Node]()
		    var events = List[org.neo4j.graphdb.Node]()
		    var p_hash_nodes =  List[org.neo4j.graphdb.Node]()
		    var e_hash_nodes =  List[org.neo4j.graphdb.Node]()
		    
		    petitions = PetitionIndex.query("id", "*" ).iterator().asScala.toList.filter(  x => x.getProperty("space").toString.toInt == 0 ).sortBy(-_.getProperty("time_created").toString().toInt).slice(0,2)
            events = EventIndex.query("id", "*" ).iterator().asScala.toList.filter( x => x.getProperty("space").toString.toInt == 0 ).sortBy(-_.getProperty("time_created").toString().toInt).slice(0,2)
         
            p_hash_nodes = petitions.map( x => x.getRelationships("Belongs_To_Subcategory_Petition").asScala.toList.map(_.getOtherNode(x)) ).flatten
            e_hash_nodes = events.map( x => x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.map(_.getOtherNode(x)) ).flatten
            
            hash_nodes = (hash_nodes:::p_hash_nodes:::e_hash_nodes).distinct
		    
		   
         }
         
         
       }
       else if(item_type.equals("H"))
       {
         // extracting hash nodes
         hash_nodes = item_id.split(",").toList.map(x => SubcatIndex.get("name",x.toLowerCase()).getSingle).filter( x => (x != null))
       
       }
       
       else if(item_type.equals("S"))
       {
         val itemIndex = getNodeIndex("space").get
         var item_node = itemIndex.get("id",item_id).getSingle()
         if(item_node != null)
         {
	         is_space = true
         }
       }
       
       else if(item_type.equals("A"))
       {
         // extracting hash nodes based on item node
         val itemIndex = getNodeIndex("article").get
         var item_node = itemIndex.get("id",item_id).getSingle()
         if(item_node != null)
         {
	         hash_nodes = item_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(item_node))
	         // extracting the categories of the item node
//	         cat = item_node.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(item_node)).map(y => y.getProperty("name").toString()).toList
	         // extracting the content of the item node
	         //content = item_node.getProperty("article_content").toString
	         //content = content.filterNot(toRemove)
         }
       }
       
       else if(item_type.equals("E"))
       {
         val itemIndex = getNodeIndex("event").get
         var item_node = itemIndex.get("id",item_id).getSingle()
         if(item_node != null)
         {
	         hash_nodes = item_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(item_node))
//	         cat = item_node.getRelationships("Belongs_To_Event_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(item_node)).map(y => y.getProperty("name").toString()).toList
//	         content = item_node.getProperty("event_content").toString
//	         content = content.filterNot(toRemove)
         }
       }
       
       else
       {
         val itemIndex = getNodeIndex("petition").get
         var item_node = itemIndex.get("id",item_id).getSingle()
         //println(item_node)
         if(item_node != null)
         {
	         hash_nodes = item_node.getRelationships("Belongs_To_Subcategory_Petition").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(item_node))
//	         cat = item_node.getRelationships("Belongs_To_Petition_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(item_node)).map(y => y.getProperty("name").toString()).toList
	        // println(hash_nodes)
//	         content = item_node.getProperty("p_content").toString
//	         content = content.filterNot(toRemove)
	        // println(content)
         }
         
         //println(content)
       }
       
       
       // extracting articles, events and petitions based on item content
//       if(!content.equals(""))
//       {
//         val w = "*"+content+"*"
//         other_arts = article_content_index.query("article_content", w).iterator().asScala.toList.slice(0,5)
//	     other_events = event_content_index.query("event_content", w).iterator().asScala.toList.slice(0,5)
//	     other_petitions = petition_content_index.query("petition_content", w).iterator().asScala.toList.slice(0,5)
//	     
//       }
     
       var list  = List[Any]()
       
       if(hash_nodes.size > 0 || is_space == true)
	   {
         
           //println(hash_nodes)
           // extracting related hashtags to the hashtags of the item
           //val realted_tags = hash_nodes.map( x => x.getRelationships("Tag_To_Tag").asScala.toList.map(_.getOtherNode(x))).flatten
           //hash_nodes = (hash_nodes:::realted_tags).distinct
           val nodeIndex = getNodeIndex("user").get
           val node = nodeIndex.get("id",user_name).getSingle()
           var user_details = ""
           var follows = List[org.neo4j.graphdb.Node]()
           if(!user_name.equals("")  && (node != null)  )
           {
	           user_details = node.getProperty("user_name").toString() + ":" + node.getProperty("first_name").toString() + " " + node.getProperty("last_name").toString()
	           follows = node.getRelationships("Follows",Direction.OUTGOING).asScala.map(_.getOtherNode(node)).toList
           }
           
           var arts =  List[org.neo4j.graphdb.Node]()
           var events =  List[org.neo4j.graphdb.Node]()
           var petitions =  List[org.neo4j.graphdb.Node]()
           var townhalls =  List[org.neo4j.graphdb.Node]()
           var debates =  List[org.neo4j.graphdb.Node]()
           if(is_space == true)
           {
             val itemIndex = getNodeIndex("space").get
             var item_node = itemIndex.get("id",item_id).getSingle()
             arts = item_node.getRelationships("Article_Tagged_To_Space").asScala.map(_.getOtherNode(item_node)).toList
             events = item_node.getRelationships("Event_Tagged_To_Space").asScala.map(_.getOtherNode(item_node)).toList
             petitions = item_node.getRelationships("Petition_Tagged_To_Space").asScala.map(_.getOtherNode(item_node)).toList
             townhalls = item_node.getRelationships("Townhall_Tagged_To_Space").asScala.map(_.getOtherNode(item_node)).toList
             debates = item_node.getRelationships("Debate_Tagged_To_Space").asScala.map(_.getOtherNode(item_node)).toList
           }
           
           else
           {
               // extracting articles, events and petitions based on hash tags and filtering them based on item category
	           arts = hash_nodes.map( x => x.getRelationships("Belongs_To_Subcategory_Article").asScala.map(_.getOtherNode(x)).toList).flatten.distinct.filter( x => x != null && x.getProperty("space").toString.toInt == 0)
	//	       arts = (arts:::other_arts).distinct
		       //println(arts)
		       events = hash_nodes.map( x => x.getRelationships("Belongs_To_Subcategory_Event").asScala.map(_.getOtherNode(x)).toList).flatten.distinct.filter( x => x != null && x.getProperty("space").toString.toInt == 0)
	//	       events = (events:::other_events).distinct
		       petitions = hash_nodes.map( x => x.getRelationships("Belongs_To_Subcategory_Petition").asScala.map(_.getOtherNode(x)).toList).flatten.distinct.filter( x => x != null && x.getProperty("space").toString.toInt == 0)
	//	       petitions = (petitions:::other_petitions).distinct
		       townhalls = hash_nodes.map( x => x.getRelationships("Belongs_To_Subcategory_Townhall").asScala.map(_.getOtherNode(x)).toList).flatten.distinct.filter( x => x != null && x.getProperty("space").toString.toInt == 0)
		       debates = hash_nodes.map( x => x.getRelationships("Belongs_To_Subcategory_Debate").asScala.map(_.getOtherNode(x)).toList).flatten.distinct.filter( x => x != null && x.getProperty("space").toString.toInt == 0)
		       
		       
		       
           }
           
	       val art_pols = arts.map( x => x.getRelationships("Poll_App_Of").asScala.map(_.getOtherNode(x)).toList).flatten.distinct
	       
	       val event_pols = events.map( x => x.getRelationships("Poll_App_Of_Event").asScala.map(_.getOtherNode(x)).toList).flatten.distinct
	       val petition_pols = petitions.map( x => x.getRelationships("Poll_App_Of_Petition").asScala.map(_.getOtherNode(x)).toList).flatten.distinct
	       
	       val art_coms = arts.map( x => x.getRelationships("Comment_To_Article").asScala.toList.filterNot(x => x.getStartNode().hasRelationship("Comment_To_Comment",Direction.OUTGOING)).sortBy(-_.getProperty("time").toString().toInt).slice(0,1).map(_.getOtherNode(x))).flatten.distinct
	       val event_coms = events.map( x => x.getRelationships("Comment_To_Event").asScala.toList.filterNot(x => x.getStartNode().hasRelationship("Comment_To_Comment",Direction.OUTGOING)).sortBy(-_.getProperty("time").toString().toInt).slice(0,1).map(_.getOtherNode(x))).flatten.distinct
	       val petition_coms = petitions.map( x => x.getRelationships("Comment_To_Petition").asScala.toList.filterNot(x => x.getStartNode().hasRelationship("Comment_To_Comment",Direction.OUTGOING)).sortBy(-_.getProperty("time").toString().toInt).slice(0,1).map(_.getOtherNode(x))).flatten.distinct
	       
	       
	       val art_p_rels = arts.map( x => x.getSingleRelationship("Article_Written_By",Direction.OUTGOING))
	       val event_p_rels = events.map( x => x.getSingleRelationship("Event_Created_By",Direction.OUTGOING))
	       val petition_p_rels = petitions.map( x => x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING))
	       val t_p_rels = townhalls.map( x => x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING))
	       val d_p_rels = debates.map( x => x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING))
	       
	       val art_v_rels = arts.map( x => x.getRelationships("article_voteup").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten.distinct
	       val t_part_rels = townhalls.map( x => x.getRelationships("Participated_In_Townhall").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten.distinct
	       val d_part_rels = debates.map( x => x.getRelationships("Participated_In_Debate").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten.distinct
	       val t_qtn_rels = townhalls.map( x => x.getRelationships("Asked_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten.distinct
	       val d_qtn_rels = debates.map( x => x.getRelationships("Asked_Debate_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten.distinct
	       val d_arg_rels = debates.map( x => x.getRelationships("Started_Debate_Argument").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten.distinct
	       val t_com_rels = townhalls.map( x => x.getRelationships("Commented_On_Townhall").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten.distinct
	       val d_com_rels = debates.map( x => x.getRelationships("Commented_On_Debate").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten.distinct
	       
//	       val d_for_rels = debates.map( x => x.getRelationships("For").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten.distinct
//	       val d_against_rels = debates.map( x => x.getRelationships("Against").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten.distinct
//	       val d_for_rels1 = debates.map( x => x.getRelationships("For").asScala.toList.filter(x => x.getProperty("d_status").toString().toInt == 1).sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten.distinct
//	       val d_against_rels1 = debates.map( x => x.getRelationships("Against").asScala.toList.filter(x => x.getProperty("d_status").toString().toInt == 1).sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten.distinct
	       
	       
	       val poll_rels = (art_pols:::event_pols:::petition_pols).map( x => x.getRelationships("Voted_To_Poll").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten.distinct
	       val art_event_petition_c_rels = (art_coms:::event_coms:::petition_coms).map( x => x.getRelationships("Comment_Written_By").asScala.toList).flatten.distinct
	       val event_a_rels = events.map( x => x.getRelationships("Is_Attending").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten.distinct
	       val event_n_rels = events.map( x => x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(x)).map( x => x.getRelationships("Notified_In_Event").asScala.toList).flatten.distinct.sortBy(-_.getProperty("time").toString().toInt)
	       
	       val petition_s_rels = petitions.map( x => x.getRelationships("Signed_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten.distinct
	       
	       
	       var rels = (petition_s_rels:::event_n_rels:::event_a_rels:::art_p_rels:::petition_p_rels:::event_p_rels:::art_v_rels:::art_event_petition_c_rels:::poll_rels:::t_p_rels:::d_p_rels:::t_part_rels:::d_part_rels:::t_qtn_rels:::d_qtn_rels:::d_arg_rels:::t_com_rels:::d_com_rels).sortBy(-_.getProperty("time").toString().toInt)
	       //println(rels)
	       val l = List("Is_Neo4j","QP_Content","QP_Type","QP_Tag","QP_Url","QP_Timestamp","QP_User","QP_User_FullName","QP_Article_Event_Owner","QP_Featured_Image","QP_Rating","QP_Refer_To","P_Reactions","P_Rating","Article_Event_ID")
	       def matchTest(x: org.neo4j.graphdb.Relationship) = x.getType().toString() match {
	       
	        case "Article_Written_By" =>
	          
	            val art_node = x.getStartNode()
	            val auth_node = x.getEndNode()
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = art_node.getProperty("article_title")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/" + art_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(art_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + art_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(art_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + art_node.getProperty("article_title_id")
	           // println()
	            list :+= JSONObject(l.zip(List(true,m,"A","W",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), art_node.getProperty("article_featured_img"), "", "", art_node.getRelationships("Comment_To_Article").asScala.size,art_node.getRelationships("article_voteup").asScala.size, art_node.getProperty("article_id"))).toMap)
			 
	         case "Townhall_Written_By" =>
	          
	            val t_node = x.getStartNode()
	            val auth_node = x.getEndNode()
	            val celeb_node = t_node.getSingleRelationship("Townhall_Of",Direction.OUTGOING).getEndNode()
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = t_node.getProperty("t_title")
	            map("date") = t_node.getProperty("t_date")
	            map("celeb") = celeb_node.getProperty("first_name") + " " + celeb_node.getProperty("last_name")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/" + t_node.getProperty("t_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"T","W",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", "", 0,0, t_node.getProperty("t_id"))).toMap)
			
	         case "Participated_In_Townhall" =>
	          
	            val t_node = x.getEndNode()
	            val user_node = x.getStartNode()
	            val auth_node = t_node.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode()
	            
	            val celeb_node = t_node.getSingleRelationship("Townhall_Of",Direction.OUTGOING).getEndNode()
	            
	            var part_users = t_node.getRelationships("Participated_In_Townhall").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(t_node))
		            
		            part_users = part_users.distinct
		            var part_list = List[String]()
		            for(each <- part_users)
		            {
		              part_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
		            }
	                
	                
	                
	                if(node != null && node != auth_node)
	                {
		                var inter = part_users.intersect(follows)
		                
		                
		                if(inter.size > 0)
		                {
		                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
		                  val foll = part_users.filter( x => follows.contains(x))(0)
		                  part_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
			              part_list = part_list.distinct
		                }
		                
		                if(part_list.contains(user_details))
		                {
		                  part_list ::= user_details
		                  part_list = part_list.distinct
		                }
	                }
	                
	                
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = t_node.getProperty("t_title")
	            map("date") = t_node.getProperty("t_date")
	            map("celeb") = celeb_node.getProperty("first_name") + " " + celeb_node.getProperty("last_name")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/" + t_node.getProperty("t_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"T","P",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", part_list.mkString(","), 0,0, t_node.getProperty("t_id"))).toMap)
			
	         case "Asked_Question" =>
	          
	            val t_node = x.getEndNode()
	            val user_node = x.getStartNode()
	            val auth_node = t_node.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode()
	            
	            val celeb_node = t_node.getSingleRelationship("Townhall_Of",Direction.OUTGOING).getEndNode()
	            
	            var part_users = t_node.getRelationships("Asked_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(t_node))
		            
		            part_users = part_users.distinct
		            var part_list = List[String]()
		            for(each <- part_users)
		            {
		              part_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
		            }
	                
	                
	                
	                if(node != null && node != auth_node)
	                {
		                var inter = part_users.intersect(follows)
		                
		                
		                if(inter.size > 0)
		                {
		                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
		                  val foll = part_users.filter( x => follows.contains(x))(0)
		                  part_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
			              part_list = part_list.distinct
		                }
		                
		                if(part_list.contains(user_details))
		                {
		                  part_list ::= user_details
		                  part_list = part_list.distinct
		                }
	                }
	                
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = t_node.getProperty("t_title")
	            map("date") = t_node.getProperty("t_date")
	            map("celeb") = celeb_node.getProperty("first_name") + " " + celeb_node.getProperty("last_name")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/" + t_node.getProperty("t_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"T","Q",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", part_list.mkString(","), 0,0, t_node.getProperty("t_id"))).toMap)
			
	        case "Commented_On_Townhall" =>
	          
	            val t_node = x.getEndNode()
	            val user_node = x.getStartNode()
	            val auth_node = t_node.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode()
	            
	            val celeb_node = t_node.getSingleRelationship("Townhall_Of",Direction.OUTGOING).getEndNode()
	            
	            var part_users = t_node.getRelationships("Commented_On_Townhall").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(t_node))
		            
		            part_users = part_users.distinct
		            var part_list = List[String]()
		            for(each <- part_users)
		            {
		              part_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
		            }
	                
	                
	                
	                if(node != null && node != auth_node)
	                {
		                var inter = part_users.intersect(follows)
		                
		                
		                if(inter.size > 0)
		                {
		                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
		                  val foll = part_users.filter( x => follows.contains(x))(0)
		                  part_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
			              part_list = part_list.distinct
		                }
		                
		                if(part_list.contains(user_details))
		                {
		                  part_list ::= user_details
		                  part_list = part_list.distinct
		                }
	                }
	                
	                
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = t_node.getProperty("t_title")
	            map("date") = t_node.getProperty("t_date")
	            map("celeb") = celeb_node.getProperty("first_name") + " " + celeb_node.getProperty("last_name")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/" + t_node.getProperty("t_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"T","CM",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", part_list.mkString(","), 0,0, t_node.getProperty("t_id"))).toMap)
			  
	         case "Asked_Debate_Question" =>
	          
	            val d_node = x.getEndNode()
	            val user_node = x.getStartNode()
	            val auth_node = d_node.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode()
	            
	            //val celeb_node = t_node.getSingleRelationship("Townhall_Of",Direction.OUTGOING).getEndNode()
	            
	            var part_users = d_node.getRelationships("Asked_Debate_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(d_node))
		            
		            part_users = part_users.distinct
		            var part_list = List[String]()
		            for(each <- part_users)
		            {
		              part_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
		            }
	                
	                
	                
	                if(node != null && node != auth_node)
	                {
		                var inter = part_users.intersect(follows)
		                
		                
		                if(inter.size > 0)
		                {
		                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
		                  val foll = part_users.filter( x => follows.contains(x))(0)
		                  part_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
			              part_list = part_list.distinct
		                }
		                
		                if(part_list.contains(user_details))
		                {
		                  part_list ::= user_details
		                  part_list = part_list.distinct
		                }
	                }
	                
	                
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = d_node.getProperty("d_title")
	            map("date") = d_node.getProperty("d_date")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/" + d_node.getProperty("d_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"D","Q",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", part_list.mkString(","), 0,0, d_node.getProperty("d_id"))).toMap)
			
	        case "Started_Debate_Argument" =>
	          
	            val d_node = x.getEndNode()
	            val user_node = x.getStartNode()
	            val auth_node = d_node.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode()
	            
	            //val celeb_node = t_node.getSingleRelationship("Townhall_Of",Direction.OUTGOING).getEndNode()
	            
	            var part_users = d_node.getRelationships("Started_Debate_Argument").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(d_node))
		            
		            part_users = part_users.distinct
		            var part_list = List[String]()
		            for(each <- part_users)
		            {
		              part_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
		            }
	                
	                
	                
	                if(node != null && node != auth_node)
	                {
		                var inter = part_users.intersect(follows)
		                
		                
		                if(inter.size > 0)
		                {
		                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
		                  val foll = part_users.filter( x => follows.contains(x))(0)
		                  part_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
			              part_list = part_list.distinct
		                }
		                
		                if(part_list.contains(user_details))
		                {
		                  part_list ::= user_details
		                  part_list = part_list.distinct
		                }
	                }
	                
	                
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = d_node.getProperty("d_title")
	            map("date") = d_node.getProperty("d_date")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/" + d_node.getProperty("d_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"D","A",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", part_list.mkString(","), 0,0, d_node.getProperty("d_id"))).toMap)
			
	        case "Commented_On_Debate" =>
	          
	            val d_node = x.getEndNode()
	            val user_node = x.getStartNode()
	            val auth_node = d_node.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode()
	            
	            //val celeb_node = t_node.getSingleRelationship("Townhall_Of",Direction.OUTGOING).getEndNode()
	            
	            var part_users = d_node.getRelationships("Commented_On_Debate").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(d_node))
		            
		            part_users = part_users.distinct
		            var part_list = List[String]()
		            for(each <- part_users)
		            {
		              part_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
		            }
	                
	                if(node != null && node != auth_node)
	                {
		                var inter = part_users.intersect(follows)
		                
		                
		                if(inter.size > 0)
		                {
		                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
		                  val foll = part_users.filter( x => follows.contains(x))(0)
		                  part_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
			              part_list = part_list.distinct
		                }
		                
		                if(part_list.contains(user_details))
		                {
		                  part_list ::= user_details
		                  part_list = part_list.distinct
		                }
	                }
	                
	                
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = d_node.getProperty("d_title")
	            map("date") = d_node.getProperty("d_date")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/" + d_node.getProperty("d_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"D","CM",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", part_list.mkString(","), 0,0, d_node.getProperty("d_id"))).toMap)
			   
	         case "Debate_Written_By" =>
	          
	            val d_node = x.getStartNode()
	            val auth_node = x.getEndNode()
	            //val celeb_node = t_node.getSingleRelationship("Townhall_Of",Direction.OUTGOING).getEndNode()
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = d_node.getProperty("d_title")
	            map("date") = d_node.getProperty("d_date")
	            //map("celeb") = celeb_node.getProperty("first_name") + " " + celeb_node.getProperty("last_name")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/" + d_node.getProperty("d_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"D","W",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", "", 0,0, d_node.getProperty("d_id"))).toMap)
			
	        case "Participated_In_Debate" =>
	          
	            val d_node = x.getEndNode()
	            val user_node = x.getStartNode()
	            val auth_node = d_node.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode()
	            
	            //val celeb_node = t_node.getSingleRelationship("Townhall_Of",Direction.OUTGOING).getEndNode()
	            
	            var part_users = d_node.getRelationships("Participated_In_Debate").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(d_node))
		            
		            part_users = part_users.distinct
		            var part_list = List[String]()
		            for(each <- part_users)
		            {
		              part_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
		            }
	                
	                
	                
	                if(node != null && node != auth_node)
	                {
		                var inter = part_users.intersect(follows)
		                
		                
		                if(inter.size > 0)
		                {
		                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
		                  val foll = part_users.filter( x => follows.contains(x))(0)
		                  part_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
			              part_list = part_list.distinct
		                }
		                
		                if(part_list.contains(user_details))
		                {
		                  part_list ::= user_details
		                  part_list = part_list.distinct
		                }
	                }
	                
	                
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = d_node.getProperty("d_title")
	            map("date") = d_node.getProperty("d_date")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/" + d_node.getProperty("d_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"D","P",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", part_list.mkString(","), 0,0, d_node.getProperty("d_id"))).toMap)
			
	         case "Petition_Written_By" =>
	          
	            val p_node = x.getStartNode()
	            val auth_node = x.getEndNode()
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = p_node.getProperty("p_title")
//	            map("date") = event_node.getProperty("event_date_time")
//	            map("loc") = event_node.getProperty("event_location")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/petitions/" + p_node.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(p_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + p_node.getProperty("p_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"P","C",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", "", p_node.getRelationships("Comment_To_Petition").asScala.size, p_node.getProperty("p_target"), p_node.getProperty("p_id"))).toMap)
			
	       
	       case "Signed_Petition" =>
	            
		            val p_node = x.getEndNode()
		            val user_node = x.getStartNode()
		            var signed_users = p_node.getRelationships("Signed_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(p_node))
			        signed_users = signed_users.distinct
			        var signed_list = List[String]()
			        for(each <- signed_users)
			        {
			            signed_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			        }
		            val auth_node = p_node.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getOtherNode(p_node)
		            
		            if(node != null && node != auth_node)
	                {
		                var inter = signed_users.intersect(follows)
		                
		                
		                if(inter.size > 0)
		                {
		                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
		                  val foll = signed_users.filter( x => follows.contains(x))(0)
		                  signed_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
			              signed_list = signed_list.distinct
		                }
		                
		                if(signed_list.contains(user_details))
		                {
		                  signed_list ::= user_details
		                  signed_list = signed_list.distinct
		                }
	                }
		            var map  = scala.collection.mutable.Map[String,Any]()
		            map("ttl") = p_node.getProperty("p_title")
//		            map("date") = event_node.getProperty("event_date_time")
//		            map("loc") = event_node.getProperty("event_location")
		            val m = JSONObject(map.toMap).toString()
		            val url = "/petitions/" + p_node.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(p_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + p_node.getProperty("p_title_id")
		            list :+= JSONObject(l.zip(List(true,m,"P","S",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", signed_list.mkString(","), p_node.getRelationships("Comment_To_Petition").asScala.size,p_node.getProperty("p_target").toString().toInt - p_node.getProperty("p_count").toString().toInt, p_node.getProperty("p_id"))).toMap)
			
	         case "article_voteup" => 
	           
	                val art_node = x.getEndNode()
	            
		            val user_node = x.getStartNode()
		            //var voted_rels = art_node.getRelationships("article_voteup").asScala.toList
		            var voted_users = art_node.getRelationships("article_voteup").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(art_node))
		            
		            voted_users = voted_users.distinct
		            var voted_list = List[String]()
		            for(each <- voted_users)
		            {
		              voted_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
		            }
	                val auth_node = art_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(art_node)
		            
	                if(node != null && node != auth_node)
	                {
		                var inter = voted_users.intersect(follows)
		                
		                
		                if(inter.size > 0)
		                {
		                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
		                  val foll = voted_users.filter( x => follows.contains(x))(0)
		                  voted_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
			              voted_list = voted_list.distinct
		                }
		                
		                if(voted_list.contains(user_details))
		                {
		                  voted_list ::= user_details
		                  voted_list = voted_list.distinct
		                }
	                }
		            var map  = scala.collection.mutable.Map[String,Any]()
		            map("ttl") = art_node.getProperty("article_title")
		            val m = JSONObject(map.toMap).toString()
		            val url = "/" + art_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(art_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + art_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(art_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + art_node.getProperty("article_title_id")
		            list :+= JSONObject(l.zip(List(true,m,"A","V",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), art_node.getProperty("article_featured_img"), "", voted_list.mkString(","), art_node.getRelationships("Comment_To_Article").asScala.size,art_node.getRelationships("article_voteup").asScala.size , art_node.getProperty("article_id"))).toMap)
	        
		    case "Voted_To_Poll" => 
	           
	                val poll_node = x.getEndNode()
	                val user_node = x.getStartNode()
	                var item_node = poll_node
	                if(poll_node.hasRelationship("Poll_App_Of",Direction.OUTGOING))
	                {
	                    item_node = poll_node.getSingleRelationship("Poll_App_Of",Direction.OUTGOING).getEndNode()
		                var voted_users = poll_node.getRelationships("Voted_To_Poll").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(poll_node))
		            
			            voted_users = voted_users.distinct
			            var voted_list = List[String]()
			            for(each <- voted_users)
			            {
			              voted_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			            }
		                val auth_node = item_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(item_node)
			            if(node != null && node != auth_node)
			            {
			                var inter = voted_users.intersect(follows)
			                
			                
			                if(inter.size > 0)
			                {
			                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
			                  val foll = voted_users.filter( x => follows.contains(x))(0)
			                  voted_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
				              voted_list = voted_list.distinct
			                }
			                
			                if(voted_list.contains(user_details))
			                {
			                  voted_list ::= user_details
			                  voted_list = voted_list.distinct
			                }
			            }
			            var map  = scala.collection.mutable.Map[String,Any]()
			            map("ttl") = item_node.getProperty("article_title")
			            val m = JSONObject(map.toMap).toString()
			            val url = "/" + item_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(item_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + item_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(item_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + item_node.getProperty("article_title_id")
			            list :+= JSONObject(l.zip(List(true,m,"A","P",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), item_node.getProperty("article_featured_img"), "", voted_list.mkString(","), item_node.getRelationships("Comment_To_Article").asScala.size,item_node.getRelationships("article_voteup").asScala.size , item_node.getProperty("article_id"))).toMap)
	            
	                }
	                
	                else if(poll_node.hasRelationship("Poll_App_Of_Event",Direction.OUTGOING))
	                {
	                    item_node = poll_node.getSingleRelationship("Poll_App_Of_Event",Direction.OUTGOING).getEndNode()
		                //item_node = poll_node.getSingleRelationship("Poll_App_Of",Direction.OUTGOING).getEndNode()
		                var voted_users = poll_node.getRelationships("Voted_To_Poll").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(poll_node))
		            
			            voted_users = voted_users.distinct
			            var voted_list = List[String]()
			            for(each <- voted_users)
			            {
			              voted_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			            }
		                val auth_node = item_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(item_node)
		                
	                    if(node != null && node != auth_node)
	                    {
			                var inter = voted_users.intersect(follows)
			                
			                
			                if(inter.size > 0)
			                {
			                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
			                  val foll = voted_users.filter( x => follows.contains(x))(0)
			                  voted_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
				              voted_list = voted_list.distinct
			                }
			                
			                if(voted_list.contains(user_details))
			                {
			                  voted_list ::= user_details
			                  voted_list = voted_list.distinct
			                }
	                    }
		                
			            var map  = scala.collection.mutable.Map[String,Any]()
			            map("ttl") = item_node.getProperty("event_title")
			            map("date") = item_node.getProperty("event_date_time")
			            map("loc") = item_node.getProperty("event_location")
			            val m = JSONObject(map.toMap).toString()
			            val url = "/Events/" + item_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(item_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + item_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(item_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + item_node.getProperty("event_title_id")
			            list :+= JSONObject(l.zip(List(true,m,"E","P",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", voted_list.mkString(","), item_node.getRelationships("Comment_To_Event").asScala.size, 0, item_node.getProperty("event_id"))).toMap)
				
	                }
	                
	                else
	                {
	                  
	                    item_node = poll_node.getSingleRelationship("Poll_App_Of_Petition",Direction.OUTGOING).getEndNode()
		                //item_node = poll_node.getSingleRelationship("Poll_App_Of",Direction.OUTGOING).getEndNode()
		                var voted_users = poll_node.getRelationships("Voted_To_Poll").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(poll_node))
		                val auth_node = item_node.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getOtherNode(item_node)
		                
			            voted_users = voted_users.distinct
			            var voted_list = List[String]()
			            for(each <- voted_users)
			            {
			              voted_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			            }
		                
	                    if(node != null && node != auth_node)
	                    {
			                var inter = voted_users.intersect(follows)
			                
			                
			                if(inter.size > 0)
			                {
			                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
			                  val foll = voted_users.filter( x => follows.contains(x))(0)
			                  voted_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
				              voted_list = voted_list.distinct
			                }
			                
			                if(voted_list.contains(user_details))
			                {
			                  voted_list ::= user_details
			                  voted_list = voted_list.distinct
			                }
	                    }
			            var map  = scala.collection.mutable.Map[String,Any]()
			            map("ttl") = item_node.getProperty("p_title")
//			            map("date") = item_node.getProperty("event_date_time")
//			            map("loc") = item_node.getProperty("event_location")
			            val m = JSONObject(map.toMap).toString()
			            val url = "/petitions/" + item_node.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(item_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + item_node.getProperty("p_title_id")
			            val l = List("QP_Content","QP_Type","QP_Tag","QP_Url","QP_Timestamp","QP_User","QP_User_FullName","QP_Article_Event_Owner","QP_Featured_Image","QP_Rating","QP_Refer_To","P_Reactions","P_Rating","Article_Event_ID")
	     
			            list :+= JSONObject(l.zip(List(true,m,"P","P",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", voted_list.mkString(","), item_node.getRelationships("Comment_To_Petition").asScala.size, item_node.getProperty("p_target").toString().toInt - item_node.getProperty("p_count").toString().toInt, item_node.getProperty("p_id"))).toMap)
				
	                
	                }
	                
	            
	        case "Comment_Written_By" => 
	          
	              val com_node = x.getStartNode()
                  if(com_node.getSingleRelationship("Comment_To_Article",Direction.OUTGOING) != null && !com_node.hasRelationship("Comment_To_Comment",Direction.OUTGOING))
		          {
		            
		            
		            val art_node = com_node.getSingleRelationship("Comment_To_Article",Direction.OUTGOING).getOtherNode(com_node)
		            
			            val user_node = x.getEndNode()
			            val auth_node = art_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(art_node)
			            var map  = scala.collection.mutable.Map[String,Any]()
			            map("ttl") = art_node.getProperty("article_title")
			            map("cmnt") = com_node.getProperty("comment_content")
			            val m = JSONObject(map.toMap).toString()
			            val url = "/" + art_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(art_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + art_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(art_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + art_node.getProperty("article_title_id") + "#" + com_node.getProperty("comment_id")
			            var com_users = art_node.getRelationships("Comment_To_Article").asScala.toList.filterNot(x => x.getStartNode().hasRelationship("Comment_To_Comment",Direction.OUTGOING)).sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(art_node).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct
			            var com_list = List[String]()
			            for(each <- com_users)
			            {
			              com_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			            }
		            
		            
		                if(node != null &&  node != auth_node)
		                {
			                var inter = com_users.intersect(follows)
		                
		                
			                if(inter.size > 0)
			                {
			                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
			                  val foll = com_users.filter( x => follows.contains(x))(0)
			                  com_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
				              com_list = com_list.distinct
			                }
			                
			                if(com_list.contains(user_details))
			                {
			                  com_list ::= user_details
			                  com_list = com_list.distinct
			                }
		                }
			            list :+= JSONObject(l.zip(List(true,m,"A","CM",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), art_node.getProperty("article_featured_img"), "" , com_list.mkString(",") , art_node.getRelationships("Comment_To_Article").asScala.size,art_node.getRelationships("article_voteup").asScala.size , art_node.getProperty("article_id"))).toMap)
				   
		           
		          }
		          else if(com_node.getSingleRelationship("Comment_To_Event",Direction.OUTGOING) != null && !com_node.hasRelationship("Comment_To_Comment",Direction.OUTGOING))
		          {
		                val event_node = com_node.getSingleRelationship("Comment_To_Event",Direction.OUTGOING).getOtherNode(com_node)
		            
			            val user_node = x.getEndNode()
			            val auth_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
			            
			            var map  = scala.collection.mutable.Map[String,Any]()
			            map("ttl") = event_node.getProperty("event_title")
			            map("date") = event_node.getProperty("event_date_time")
			            map("loc") = event_node.getProperty("event_location")
			            map("cmnt") = com_node.getProperty("comment_content")
			            val m = JSONObject(map.toMap).toString()
			            val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id") + "#" + com_node.getProperty("comment_id")
			            var com_users = event_node.getRelationships("Comment_To_Event").asScala.toList.filterNot(x => x.getStartNode().hasRelationship("Comment_To_Comment",Direction.OUTGOING)).sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(event_node).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct
			            var com_list = List[String]()
			            for(each <- com_users)
			            {
			              com_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			            }
		            
		            
		                if(node != null && node != auth_node)
		                {
			            	var inter = com_users.intersect(follows)
		                
		                
			                if(inter.size > 0)
			                {
			                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
			                  val foll = com_users.filter( x => follows.contains(x))(0)
			                  com_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
				              com_list = com_list.distinct
			                }
			                
			                if(com_list.contains(user_details))
			                {
			                  com_list ::= user_details
			                  com_list = com_list.distinct
			                }
		                }
			            list :+= JSONObject(l.zip(List(true,m,"E","CM",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"),   event_node.getProperty("event_featured_img"), "" , com_list.mkString(","), event_node.getRelationships("Comment_To_Event").asScala.size, 0, event_node.getProperty("event_id"))).toMap)
					
		    }
	              
	        else
		          {
		                val p_node = com_node.getSingleRelationship("Comment_To_Petition",Direction.OUTGOING).getOtherNode(com_node)
		            
			            val user_node = x.getEndNode()
			            val auth_node = p_node.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getOtherNode(p_node)
			            
			            var map  = scala.collection.mutable.Map[String,Any]()
			            map("ttl") = p_node.getProperty("p_title")
//			            map("date") = item_node.getProperty("event_date_time")
//			            map("loc") = item_node.getProperty("event_location")
			            val m = JSONObject(map.toMap).toString()
			            val url = "/petitions/" + p_node.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(p_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + p_node.getProperty("p_title_id")
			            //val l = List("QP_Content","QP_Type","QP_Tag","QP_Url","QP_Timestamp","QP_User","QP_User_FullName","QP_Article_Event_Owner","QP_Featured_Image","QP_Rating","QP_Refer_To","P_Reactions","P_Rating","Article_Event_ID")
	     
			            //list :+= JSONObject(l.zip(List(m,"P","P",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", voted_list.mkString(","), item_node.getRelationships("Comment_To_Petition").asScala.size, item_node.getProperty("p_count").toString().toInt, item_node.getProperty("p_id"),0)).toMap)
				        var com_users = p_node.getRelationships("Comment_To_Petition").asScala.toList.filterNot(x => x.getStartNode().hasRelationship("Comment_To_Comment",Direction.OUTGOING)).sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(p_node).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct
			            var com_list = List[String]()
			            for(each <- com_users)
			            {
			              com_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			            }
		            
		            
		                if(node != null && node != auth_node)
		                {
			            	var inter = com_users.intersect(follows)
		                
		                
			                if(inter.size > 0)
			                {
			                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
			                  val foll = com_users.filter( x => follows.contains(x))(0)
			                  com_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
				              com_list = com_list.distinct
			                }
			                
			                if(com_list.contains(user_details))
			                {
			                  com_list ::= user_details
			                  com_list = com_list.distinct
			                }
		                }
			            list :+= JSONObject(l.zip(List(true,m,"P","CM",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "" , com_list.mkString(","), p_node.getRelationships("Comment_To_Petition").asScala.size, p_node.getProperty("p_target").toString().toInt - p_node.getProperty("p_count").toString().toInt, p_node.getProperty("p_id"))).toMap)
					
		    }
	          
	        case "Event_Created_By" =>
	          
	            val event_node = x.getStartNode()
	            val auth_node = x.getEndNode()
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = event_node.getProperty("event_title")
	            map("date") = event_node.getProperty("event_date_time")
	            map("loc") = event_node.getProperty("event_location")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"E","C",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"),   event_node.getProperty("event_featured_img"), "", "", event_node.getRelationships("Comment_To_Event").asScala.size, 0, event_node.getProperty("event_id"))).toMap)
			
	        case "Notified_In_Event" =>
	            
	            val user_node = nodeIndex.get("id",x.getProperty("user").toString()).getSingle()
	            val event_node = x.getEndNode()
	            val auth_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = event_node.getProperty("event_title")
	            map("date") = event_node.getProperty("event_date_time")
	            map("loc") = event_node.getProperty("event_location")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"E","N",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"),   event_node.getProperty("event_featured_img"), "", "", event_node.getRelationships("Comment_To_Event").asScala.size, 0, event_node.getProperty("event_id"))).toMap)
			
	        case "Is_Attending" =>
	            
		            val event_node = x.getEndNode()
		            val user_node = x.getStartNode()
		            var attend_users = event_node.getRelationships("Is_Attending").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(event_node))
			        attend_users = attend_users.distinct
			        var attend_list = List[String]()
			        for(each <- attend_users)
			        {
			            attend_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			        }
		            
		            val auth_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
		            if(node != null && node != auth_node)
		            {
		                var inter = attend_users.intersect(follows)
		                
		                
		                if(inter.size > 0)
		                {
		                  //val sorted_voters = voted_rels.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode())
		                  val foll = attend_users.filter( x => follows.contains(x))(0)
		                  attend_list ::= foll.getProperty("user_name").toString() + ":" + foll.getProperty("first_name").toString() + " " + foll.getProperty("last_name").toString()
			              attend_list = attend_list.distinct
		                }
		                
		                if(attend_list.contains(user_details))
		                {
		                  attend_list ::= user_details
		                  attend_list = attend_list.distinct
		                }
		            }
		            var map  = scala.collection.mutable.Map[String,Any]()
		            map("ttl") = event_node.getProperty("event_title")
		            map("date") = event_node.getProperty("event_date_time")
		            map("loc") = event_node.getProperty("event_location")
		            val m = JSONObject(map.toMap).toString()
		            val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id")
		            list :+= JSONObject(l.zip(List(true,m,"E","A",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"),   event_node.getProperty("event_featured_img"), "", attend_list.mkString(","), event_node.getRelationships("Comment_To_Event").asScala.size, 0, event_node.getProperty("event_id"))).toMap)
			
		            
		            
	        case _ => ""
	     }
	     
	     for(each <- rels.slice(prev_cnt,(prev_cnt+count)))
	     {
	       matchTest(each)
	     }
	 }
     JSONArray(list.distinct).toString()
     
}
  
  def email_notifications(user_name:String,time:Int): String =
  {
    
     val nodeIndex = getNodeIndex("user").get
     val node = nodeIndex.get("id",user_name).getSingle()
         
     var list  = List[Any]()
     if(node != null)
	 {
         val user_details = node.getProperty("user_name").toString() + ":" + node.getProperty("first_name").toString() + " " + node.getProperty("last_name").toString()
         var follows = List[org.neo4j.graphdb.Node]()
         var event_invites =  List[org.neo4j.graphdb.Relationship]()
         var art_com_rels =  List[org.neo4j.graphdb.Relationship]()
         var event_com_rels =  List[org.neo4j.graphdb.Relationship]()
         var event_atnd_rels =  List[org.neo4j.graphdb.Relationship]()
         var sub_com_rels =  List[org.neo4j.graphdb.Relationship]()
         var sub_coms =  List[org.neo4j.graphdb.Relationship]()
         var user_rels =  List[org.neo4j.graphdb.Relationship]()
         var art_voted_rels =  List[org.neo4j.graphdb.Relationship]()
         var art_poll_rels =  List[org.neo4j.graphdb.Relationship]()
         var event_poll_rels =  List[org.neo4j.graphdb.Relationship]()
         var event_notify_rels =  List[org.neo4j.graphdb.Relationship]()
         var commented_arts =  List[org.neo4j.graphdb.Node]()
         var commented_events =  List[org.neo4j.graphdb.Node]()
         var commented_coms =  List[org.neo4j.graphdb.Node]()
         var voted_arts =  List[org.neo4j.graphdb.Node]()
         var atnd_events =  List[org.neo4j.graphdb.Node]()
         var responded_polls =  List[org.neo4j.graphdb.Node]()
	     
	         event_invites = node.getRelationships("Invited_To_Event","User_Of_Article","User_Of_Event","User_Of_Comment").asScala.toList
	         art_com_rels = node.getRelationships("Article_Written_By").asScala.map(_.getOtherNode(node)).toList.map( x => x.getRelationships("Comment_To_Article").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1).map(_.getOtherNode(x)).filterNot( x => x.hasRelationship("Comment_To_Comment",Direction.OUTGOING))).flatten.map( x => x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING))
		     
	         
	         art_poll_rels = node.getRelationships("Article_Written_By").asScala.map(_.getOtherNode(node)).toList.map( x => x.getRelationships("Poll_App_Of").asScala.toList.map(_.getOtherNode(x))).flatten.map( x => x.getRelationships("Voted_To_Poll").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten
		     event_poll_rels = node.getRelationships("Event_Created_By").asScala.map(_.getOtherNode(node)).toList.map( x => x.getRelationships("Poll_App_Of_Event").asScala.toList.map(_.getOtherNode(x))).flatten.map( x => x.getRelationships("Voted_To_Poll").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten
		     
	         event_com_rels = node.getRelationships("Event_Created_By").asScala.map(_.getOtherNode(node)).toList.map( x => x.getRelationships("Comment_To_Event").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1).map(_.getOtherNode(x)).filterNot( x => x.hasRelationship("Comment_To_Comment",Direction.OUTGOING))).flatten.map( x => x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING))
		     event_atnd_rels = node.getRelationships("Event_Created_By").asScala.map(_.getOtherNode(node)).toList.map( x => x.getRelationships("Is_Attending").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten
		     art_voted_rels = node.getRelationships("Article_Written_By").asScala.map(_.getOtherNode(node)).toList.map( x => x.getRelationships("article_voteup").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1)).flatten
		     
		     commented_arts = art_com_rels.map( x => x.getStartNode()).map( x => x.getSingleRelationship("Comment_To_Article",Direction.OUTGOING).getEndNode())
		     commented_events = event_com_rels.map( x => x.getStartNode()).map( x => x.getSingleRelationship("Comment_To_Event",Direction.OUTGOING).getEndNode())
		     
		     
		     sub_com_rels = node.getRelationships("Comment_Written_By").asScala.map(_.getOtherNode(node)).toList.map( x => x.getRelationships("Comment_To_Comment",Direction.INCOMING).asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,1).map(_.getOtherNode(x))).flatten.distinct.map( x => x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING))
             
		     user_rels = node.getRelationships("Comment_Written_By").asScala.toList
		     event_notify_rels = node.getRelationships("Notified_In_Event").asScala.toList
	    
	     //         println(sub_coms)  
	     var rels = (art_voted_rels:::event_atnd_rels:::user_rels:::event_invites:::art_poll_rels:::event_poll_rels:::art_com_rels:::event_com_rels:::sub_com_rels:::event_notify_rels).distinct.filter(x => x.getProperty("time").toString().toInt >= time).sortBy(-_.getProperty("time").toString().toInt)
	     val l = List("QP_Content","QP_Type","QP_Tag","QP_Url","QP_Timestamp","QP_User","QP_User_FullName","QP_Article_Event_Owner","QP_Featured_Image","QP_Rating","QP_Refer_To","P_Reactions","P_Rating","Article_Event_ID")
	     def matchTest(x: org.neo4j.graphdb.Relationship) = x.getType().toString() match {
	       
	        case "article_voteup" => 
	           
	                val art_node = x.getEndNode()
	            
		            val user_node = x.getStartNode()
		            val auth_node = art_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(art_node)
		            
		            //var voted_rels = art_node.getRelationships("article_voteup").asScala.toList
		            var voted_users = art_node.getRelationships("article_voteup").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(art_node))
		            
		            voted_users = voted_users.distinct
		            var voted_list = List[String]()
		            for(each <- voted_users)
		            {
		              voted_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
		            }
	                
	                
	                
	               
		            var map  = scala.collection.mutable.Map[String,Any]()
		            map("ttl") = art_node.getProperty("article_title")
		            val m = JSONObject(map.toMap).toString()
		            val url = "/" + art_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(art_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + art_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(art_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + art_node.getProperty("article_title_id")
		            list :+= JSONObject(l.zip(List(m,"A","V",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), art_node.getProperty("article_featured_img"), "", voted_list.mkString(","), art_node.getRelationships("Comment_To_Article").asScala.size,art_node.getRelationships("article_voteup").asScala.size , art_node.getProperty("article_id"))).toMap)
	            
			case "Voted_To_Poll" => 
	           
	                val poll_node = x.getEndNode()
	                val user_node = x.getStartNode()
	                var item_node = poll_node
	                if(poll_node.hasRelationship("Poll_App_Of",Direction.OUTGOING))
	                {
	                    item_node = poll_node.getSingleRelationship("Poll_App_Of",Direction.OUTGOING).getEndNode()
		                val auth_node = item_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(item_node)
			            
	                    var voted_users = poll_node.getRelationships("Voted_To_Poll").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(poll_node))
		            
			            voted_users = voted_users.distinct
			            var voted_list = List[String]()
			            for(each <- voted_users)
			            {
			              voted_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			            }
		                
		                
		                
		                
			            var map  = scala.collection.mutable.Map[String,Any]()
			            map("ttl") = item_node.getProperty("article_title")
			            val m = JSONObject(map.toMap).toString()
			            val url = "/" + item_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(item_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + item_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(item_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + item_node.getProperty("article_title_id")
			            list :+= JSONObject(l.zip(List(m,"A","P",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), item_node.getProperty("article_featured_img"), "", voted_list.mkString(","), item_node.getRelationships("Comment_To_Article").asScala.size,item_node.getRelationships("article_voteup").asScala.size , item_node.getProperty("article_id"))).toMap)
	            
	                }
	                
	                else
	                {
	                    item_node = poll_node.getSingleRelationship("Poll_App_Of_Event",Direction.OUTGOING).getEndNode()
		                item_node = poll_node.getSingleRelationship("Poll_App_Of",Direction.OUTGOING).getEndNode()
		                var voted_users = poll_node.getRelationships("Voted_To_Poll").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(poll_node))
		                val auth_node = item_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(item_node)
		                
			            voted_users = voted_users.distinct
			            var voted_list = List[String]()
			            for(each <- voted_users)
			            {
			              voted_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			            }
		                
	                    
	                    
		                
			            var map  = scala.collection.mutable.Map[String,Any]()
			            map("ttl") = item_node.getProperty("event_title")
			            map("date") = item_node.getProperty("event_date_time")
			            map("loc") = item_node.getProperty("event_location")
			            val m = JSONObject(map.toMap).toString()
			            val url = "/Events/" + item_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(item_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + item_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(item_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + item_node.getProperty("event_title_id")
			            list :+= JSONObject(l.zip(List(m,"E","P",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", voted_list.mkString(","), item_node.getRelationships("Comment_To_Event").asScala.size, 0, item_node.getProperty("event_id"))).toMap)
				
	                }
	          
	        case "User_Of_Article" => 
	           
	            val art_node = x.getEndNode()
	            val user_node = x.getStartNode()
	            val auth_node = art_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(art_node)
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = art_node.getProperty("article_title")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/" + art_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(art_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + art_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(art_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + art_node.getProperty("article_title_id")
	            list :+= JSONObject(l.zip(List(m,"A","@",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), art_node.getProperty("article_featured_img"), "", user_node.getProperty("user_name") + ":" + user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), art_node.getRelationships("Comment_To_Article").asScala.size,art_node.getRelationships("article_voteup").asScala.size , art_node.getProperty("article_id"))).toMap)
				   
	          
	        case "Comment_Written_By" => 
	          
	              val com_node = x.getStartNode()
                  if(com_node.getSingleRelationship("Comment_To_Article",Direction.OUTGOING) != null)
		          {
		            
		            if(com_node.hasRelationship("Comment_To_Comment",Direction.OUTGOING))
		            {
//		                  println("hey")
			              val main_com = com_node.getSingleRelationship("Comment_To_Comment",Direction.OUTGOING).getOtherNode(com_node)
			              val art_node = com_node.getSingleRelationship("Comment_To_Article",Direction.OUTGOING).getOtherNode(com_node)
			              val auth_node = art_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(art_node)
				          
			              val user_node = x.getEndNode()
			              val maincom_auth_node = main_com.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(main_com)
			              var map  = scala.collection.mutable.Map[String,Any]()
			              map("ttl") = art_node.getProperty("article_title")
			              map("cmnt") = com_node.getProperty("comment_content")
			              val m = JSONObject(map.toMap).toString()
			              val url = "/" + art_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(art_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + art_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(art_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + art_node.getProperty("article_title_id")
			            
			              var com_users = main_com.getRelationships("Comment_To_Comment",Direction.INCOMING).asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode().getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct
			              var com_list = List[String]()
			              for(each <- com_users)
			              {
			                com_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			              }
			              
			              list :+= JSONObject(l.zip(List(m,"A","CC",url,x.getProperty("time"),maincom_auth_node.getProperty("user_name"),maincom_auth_node.getProperty("first_name") + " " + maincom_auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), art_node.getProperty("article_featured_img"), "" , com_list.mkString(",") , art_node.getRelationships("Comment_To_Article").asScala.size,art_node.getRelationships("article_voteup").asScala.size , art_node.getProperty("article_id"))).toMap)
				          
		              
		            }
		            else
		            {
//		                  println("heyy")
			              val art_node = com_node.getSingleRelationship("Comment_To_Article",Direction.OUTGOING).getOtherNode(com_node)
				          val user_node = x.getEndNode()
				          val auth_node = art_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(art_node)
				          var map  = scala.collection.mutable.Map[String,Any]()
				          map("ttl") = art_node.getProperty("article_title")
				          map("cmnt") = com_node.getProperty("comment_content")
				          val m = JSONObject(map.toMap).toString()
				          val url = "/" + art_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(art_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + art_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(art_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + art_node.getProperty("article_title_id")
			            
			              var com_users = art_node.getRelationships("Comment_To_Article").asScala.toList.filterNot(x => x.getStartNode().hasRelationship("Comment_To_Comment",Direction.OUTGOING)).sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(art_node).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct
			              var com_list = List[String]()
			              for(each <- com_users)
			              {
			                com_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			              }
			              
			               
			              list :+= JSONObject(l.zip(List(m,"A","CM",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), art_node.getProperty("article_featured_img"), "" , com_list.mkString(",") , art_node.getRelationships("Comment_To_Article").asScala.size,art_node.getRelationships("article_voteup").asScala.size , art_node.getProperty("article_id"))).toMap)
				          
			          
		            }
		          }
		          else if(com_node.getSingleRelationship("Comment_To_Event",Direction.OUTGOING) != null)
		          {
		            if(com_node.hasRelationship("Comment_To_Comment",Direction.OUTGOING))
		            {
		                  val main_com = com_node.getSingleRelationship("Comment_To_Comment",Direction.OUTGOING).getOtherNode(com_node)
		              
			              val event_node = com_node.getSingleRelationship("Comment_To_Event",Direction.OUTGOING).getOtherNode(com_node)
			              val user_node = x.getEndNode()
			              
			              val auth_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
			            
			              val maincom_auth_node = main_com.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(main_com)
			              
			              var map  = scala.collection.mutable.Map[String,Any]()
			              map("ttl") = event_node.getProperty("event_title")
			              map("date") = event_node.getProperty("event_date_time")
			              map("loc") = event_node.getProperty("event_location") 
			              map("cmnt") = com_node.getProperty("comment_content")
			              val m = JSONObject(map.toMap).toString()
			              val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id")
			            
			              var com_users = main_com.getRelationships("Comment_To_Comment",Direction.INCOMING).asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getStartNode().getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct
			              var com_list = List[String]()
			              for(each <- com_users)
			              {
			                com_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			              }
		                  
		                  
		                   
			              
			              list :+= JSONObject(l.zip(List(m,"E","CC",url,x.getProperty("time"),maincom_auth_node.getProperty("user_name"),maincom_auth_node.getProperty("first_name") + " " + maincom_auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "" , com_list.mkString(","), event_node.getRelationships("Comment_To_Event").asScala.size, 0, event_node.getProperty("event_id"))).toMap)
						  
		              
		            }
		            else
		            {
		              
			              val event_node = com_node.getSingleRelationship("Comment_To_Event",Direction.OUTGOING).getOtherNode(com_node)
			              val user_node = x.getEndNode()
			              val auth_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
			            
			              var map  = scala.collection.mutable.Map[String,Any]()
			              map("ttl") = event_node.getProperty("event_title")
			              map("date") = event_node.getProperty("event_date_time")
			              map("loc") = event_node.getProperty("event_location") 
			              map("cmnt") = com_node.getProperty("comment_content")
			              val m = JSONObject(map.toMap).toString()
			              val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id")
			            
			              var com_users = event_node.getRelationships("Comment_To_Event").asScala.toList.filterNot(x => x.getStartNode().hasRelationship("Comment_To_Comment",Direction.OUTGOING)).sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(event_node).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct
			              var com_list = List[String]()
			              for(each <- com_users)
			              {
			                com_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			              }
			              
			              
			              list :+= JSONObject(l.zip(List(m,"E","CM",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "" , com_list.mkString(","), event_node.getRelationships("Comment_To_Event").asScala.size, 0, event_node.getProperty("event_id"))).toMap)
						  
			          
		            }
		            
		      }
	        
	       
	       case "Invited_To_Event" =>
	            
	            val user_node = nodeIndex.get("id",x.getProperty("user").toString()).getSingle()
	            val event_node = x.getEndNode()
	            val auth_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = event_node.getProperty("event_title")
	            map("date") = event_node.getProperty("event_date_time")
	            map("loc") = event_node.getProperty("event_location")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id")
	            list :+= JSONObject(l.zip(List(m,"E","I",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", "", event_node.getRelationships("Comment_To_Event").asScala.size, 0, event_node.getProperty("event_id"))).toMap)
			
	       case "Notified_In_Event" =>
	            
	            val user_node = nodeIndex.get("id",x.getProperty("user").toString()).getSingle()
	            val event_node = x.getEndNode()
	            val auth_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = event_node.getProperty("event_title")
	            map("date") = event_node.getProperty("event_date_time")
	            map("loc") = event_node.getProperty("event_location")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id")
	            list :+= JSONObject(l.zip(List(m,"E","N",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", "", event_node.getRelationships("Comment_To_Event").asScala.size, 0, event_node.getProperty("event_id"))).toMap)
			
	       case "Is_Attending" =>
	            
		            val event_node = x.getEndNode()
		            val user_node = x.getStartNode()
		            var attend_users = event_node.getRelationships("Is_Attending").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(event_node))
			        attend_users = attend_users.distinct
			        var attend_list = List[String]()
			        for(each <- attend_users)
			        {
			            attend_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
			        }
		            val auth_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
		            
		            
		            var map  = scala.collection.mutable.Map[String,Any]()
		            map("ttl") = event_node.getProperty("event_title")
		            map("date") = event_node.getProperty("event_date_time")
		            map("loc") = event_node.getProperty("event_location")
		            val m = JSONObject(map.toMap).toString()
		            val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id")
		            list :+= JSONObject(l.zip(List(m,"E","A",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", attend_list.mkString(","), event_node.getRelationships("Comment_To_Event").asScala.size, 0, event_node.getProperty("event_id"))).toMap)
				
	       case "User_Of_Event" =>
	            
	            val user_node = x.getStartNode()
	            val event_node = x.getEndNode()
	            val auth_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = event_node.getProperty("event_title")
	            map("date") = event_node.getProperty("event_date_time")
	            map("loc") = event_node.getProperty("event_location")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id")
	            list :+= JSONObject(l.zip(List(m,"E","@",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", "", event_node.getRelationships("Comment_To_Event").asScala.size, 0, event_node.getProperty("event_id"))).toMap)
		  
	       case "User_Of_Comment" =>
	            
	            val user_node = x.getStartNode()
	            val com_node = x.getEndNode()
	            val com_auth = com_node.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode
	            
	            if(com_node.getSingleRelationship("Comment_To_Article",Direction.OUTGOING) != null)
	            {
		            val art_node = com_node.getSingleRelationship("Comment_To_Article",Direction.OUTGOING).getOtherNode(com_node)
		            //val user_node = x.getEndNode()
		            val auth_node = art_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(art_node)
		            var map  = scala.collection.mutable.Map[String,Any]()
		            map("ttl") = art_node.getProperty("article_title")
		            map("cmnt") = com_node.getProperty("comment_content")
		            val m = JSONObject(map.toMap).toString()
		            val url = "/" + art_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(art_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + art_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(art_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + art_node.getProperty("article_title_id")
		            list :+= JSONObject(l.zip(List(m,"A","@C",url,x.getProperty("time"),com_auth.getProperty("user_name"),com_auth.getProperty("first_name") + " " + com_auth.getProperty("last_name"), auth_node.getProperty("user_name"), art_node.getProperty("article_featured_img"), "" , "" , art_node.getRelationships("Comment_To_Article").asScala.size,art_node.getRelationships("article_voteup").asScala.size , art_node.getProperty("article_id"))).toMap)
			        
	            }
	            else if(com_node.getSingleRelationship("Comment_To_Event",Direction.OUTGOING) != null)
	            {
		            val event_node = com_node.getSingleRelationship("Comment_To_Event",Direction.OUTGOING).getOtherNode(com_node)
		            //val user_node = x.getEndNode()
		            val auth_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
		            
		            var map  = scala.collection.mutable.Map[String,Any]()
		            map("ttl") = event_node.getProperty("event_title")
		            map("date") = event_node.getProperty("event_date_time")
			        map("loc") = event_node.getProperty("event_location")
		            map("cmnt") = com_node.getProperty("comment_content")
		            val m = JSONObject(map.toMap).toString()
		            val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id")
		            list :+= JSONObject(l.zip(List(m,"E","@C",url,x.getProperty("time"),com_auth.getProperty("user_name"),com_auth.getProperty("first_name") + " " + com_auth.getProperty("last_name"), auth_node.getProperty("user_name"), "", "" , "", event_node.getRelationships("Comment_To_Event").asScala.size, 0, event_node.getProperty("event_id"))).toMap)
					
	            
	            }
	            		
	       case _ => ""
	     }
	     
	     for(each <- rels)
	     {
	       matchTest(each)
	     }
	     
	 }
     JSONArray(list.distinct).toString()
     
  }
  
  
  // Used to check if the jar is working perfectly
  def jar_check()
  {
  
   println("Calling location_store")
   location_store()
   val t = (System.currentTimeMillis()/1000).toInt 
   val x = "Tendulkar won the 2010 Sir Garfield Sobers Trophy for cricketer of the year at the ICC awards. Sachin played many matches in Hyderabad and he loves the place a lot. He has been recommended for the receipt of the Bharat Ratna award, in fact it has been speculated that the criteria for the award of the Bharat Ratna were changed to allow him receive the award. He is also a member of Rajya Sabha of Parliament of India. Tendulkar passed 30,000 runs in international cricket on 20 November 2009. On 5 December 2012, Tendulkar became first batsman in history to cross the 34,000 run aggregate in all formats of the game put together. At 36 years and 306 days, he became the first ever player to score a double-century in the history of ODIs. Two years later he became the first player to score 100 international centuries. As of June 2013, Tendulkar has played 662 matches in international cricket. Meanwhile at school, he developed a reputation as a child prodigy. He had become a common conversation point in local cricketing circles, where there were suggestions already that he would become one of the greats. Besides school cricket, he also played club cricket, initially representing John Bright Cricket Club in Mumbai's premier club cricket tournament, the Kanga League, and later went on to play for the Cricket Club of India. In 1987, at the age of 14, he attended the MRF Pace Foundation in Madras (now Chennai) to train as a fast bowler, but Australian fast bowler Dennis Lillee, who took a world record 355 Test wickets, was unimpressed, suggesting that Tendulkar focus on his batting instead. A couple of months later, former Indian batsman Sunil Gavaskar gave him a pair of his own ultra light pads."
  
   println("Calling create_user")
   create_user("fname1","lname1","user1","email","hyd",1,t,200)
   create_user("fname1","lname1","user2","email","hyd",1,t,300)    
   create_user("fname1","lname1","user3","email","hyd",1,t,400)
   
   create_user("fname1","lname1","user4","email","hyd",1,t,200)
   create_user("fname1","lname1","user5","email","hyd",1,t,300)    
   create_user("fname1","lname1","user6","email","hyd",1,t,400)

   println("Calling user_follow")
   user_follow("user1", "user2",t)
   user_follow("user2", "user3",t)
   println("Calling user_unfollow")
   user_unfollow("user1", "user2")

   
    println("Calling create_article")
    create_article("user2","art4","title_id4","article 4",x,"summary","img1","technology","hash1,hash101","hash12","",t+2,"","","",1,0,0)
    create_article("user1","art1","title_id1","article 1",x,"summary","img1","politics","hash,hashemd","hashed","",t,"","","",1,0,0)
    create_article("user2","art3","title_id3","article 3",x,"summary","img1","technology","hash1,hashd,hasho","hash11","",t+2,"","","",1,0,0)
    create_article("user2","art6","title_id6","article 6",x,"summary","img1","entertainment","hash1,hashd,hasho","hash11","",t+2,"","","",1,0,0)
    
    println("Calling edit_article")
    create_article("user2","art4","title_id4","article 4",x,"summary","img1","technology","hash1,hash101","hash12","",t+2,"","","",1,1,0)
    create_article("user1","art1","title_id1","article 1",x,"summary","img1","politics","hash,hashemd","hashed","",t,"","","",1,1,0)
    create_article("user2","art3","title_id3","article 3",x,"summary","img1","technology","hash1,hashd,hasho","hash11","",t+2,"","","",1,1,0)
    create_article("user2","art6","title_id6","article 6",x,"summary","img1","entertainment","hash1,hashd,hasho","hash11","",t+2,"","","",1,1,0)
    
   println("Calling delete_article")
   delete_article("art6")

   println("Calling user_view")
   user_view("user1", "A", "art1", t)
   user_view("user3", "A", "art3", t)

  
   println("Calling voteup")
   voteup_markfav_readlater("user2", "A", "art1", "voteup", t)
   println("Calling markfav")
   voteup_markfav_readlater("user3", "A", "art1", "markfav", t)
   println("Calling readlater")
   voteup_markfav_readlater("user3", "A", "art1", "readlater", t)

   println("Calling create_event")
   create_event("user1","event1","title_id1","this is my first event",x,t,t+50000,0,"Hyderabad","Politics","hash1,hash2,hash3","hash4,hash5","",t,"","",0,"",0)
   create_event("user1","event2","title_id2","this is my second event",x,t,t+10000,0,"Hyderabad","Sports","hash5,hash2,hash6","hash7,hash8,hash1","",t,"art1","",0,"",0)
   create_event("user1","event3","title_id3","this is my third event",x,t+50000,0,0,"Hyderabad","Technology","hash5,hash2,hash6","hash7,hash8,hash1","",t,"","",0,"",0)
   create_event("user2","event4","title_id4","this is my fourth event",x,t+50000,0,0,"Hyderabad","Technology","hash5,hash2,hash6","hash7,hash8,hash1","",t,"","",0,"",0)
   create_event("user1","event5","title_id5","this is my fifth event",x,t,0,0,"Hyderabad","Technology","hash5,hash2,hash6","hash7,hash8,hash1","",t,"","",0,"",0)

   println("Calling edit_event")
   edit_event("user1","event1","title_id1","this is my first event",x,t,t+50000,0,"Hyderabad","Politics","hash1,hash2,hash3","hash4,hash5","",t,"","")
   edit_event("user1","event2","title_id2","this is my second event",x,t,t+10000,0,"Hyderabad","Sports","hash5,hash2,hash6","hash7,hash8,hash1","",t,"","")

   println("Calling delete_event")
   delete_event("event5")

 
   println("Calling nonuser_view")
   nonuser_view("user1@gmail.com", "E", "event2", t)
  
    
   println("Calling create_comment")
   create_comment("art1","A","com1","com1","",t,"user2")
   create_comment("art1","A","com2","com2","",t,"user2")
   create_comment("art1","A","com3","com3","",t,"user2")
   create_comment("art1","A","com4","com4","",t,"user2")
   create_comment("com4","C","com5","com5","",t,"user2")

   println("Calling create_spam")
   comment_spam("com2","user1",t)

   println("Calling create_comment_own")
   create_comment_own("art1","A","com9","com9","",t,"user1")
   create_comment_own("art1","A","com10","com10","",t,"user1")

 
   println("Calling event_response")
   event_response("event1","user2",t)
   
   println("Calling event_response")
   event_response("event4","user1",t)
   
   println("Calling event_invite")
   event_invite("event2","user1","user2,user3",t)
  
   println("Calling create_comment")
   create_comment("event1","E","com19","com19","",t,"user3")
   create_comment("event1","E","com20","com20","",t,"user3")
  
   println("Calling voteup_markfav_readlater")
   voteup_markfav_readlater("user1", "C", "com19", "voteup", t)
  
   println("Calling votedown")
   votedown("user1", "C", "com20", t)
  
   println("Calling event_changeresponse")
   event_changeresponse("event1","user2",t)
  
   println("Calling pin_article")
   pin_item("A","art1","news,politics","")
   pin_item("A","art1","","")
   
   println("Calling exclusive_article")
   exclusive_article("art1","news,politics")
   exclusive_article("art1","")

    
   

   println("Calling update_tiles")
   update_tiles()
   println("Calling avg_weights")
   avg_weights()
   println("Calling calc_hash_trends")
   calc_hash_trends()
   println("Calling calc_user_tiles")
   calc_user_tiles()
   println("Calling calc_local_tiles")
   calc_local_tiles()
  
   println("Calling search")
   println(search("user1","tendulkar",10,0,"A"))
   println("Calling suggestions")
   println(suggestions("A","","tendulkar","","hash1",0,0))
   
   println("Calling suggestions turbo")
   println(suggestions_turbo("A","","tendulkar",""))
   
   println("Calling view suggestions")
//   println(view_suggestions("A","art1",""))

println("Calling get_tiles before approval")
println(get_tiles("user1","all",4,0,"","t"))



println("Calling get_tiles after approval")
println(get_tiles("user1","all",4,0,"","t"))

println("Calling get_events_category")
println(get_events_category("user1","all"))
println("Calling get_more_events_category")
println(get_more_events_category("user1","all",0))
println("Calling get_more_events_category")
println(get_more_events_category("user1","all",1))
//println("Calling nonuser_suggestions")
//println(nonuser_suggestions())




println("Calling delete_comment")
delete_comment("com1")

println("Calling get_comments")
println(get_comments("user1","com2"))

println("Calling get_all_comments")
println(get_all_comments("user1","A","art1"))




println("Calling report_spam")
report_spam("A","art1","user2",t)
report_spam("E","event1","user2",t)

println("Calling get_articles_unreviewed")
println(get_articles_spammed("all",10,0))

println("Calling get_events_unreviewed")
println(get_events_spammed("all",10,0))

println("Calling review_article")
review_article("art1","user3",1,t)

println("Calling review_event")
review_event("event1","user3",0,t)

println("Calling add_weight")
add_weight("user3","A","art1",3,t)


println("Calling debate_townhall_suggestion")
debate_townhall_suggestion("T","user1","dts1","topic","celebrity",t)
debate_townhall_suggestion("D","user1","dts2","topic","celebrity",t)

println("Calling debate_townhall_suggestion_voteup")
debate_townhall_suggestion_voteup("D","user2","dts1")

println("Calling delete_debate_townhall_suggestion")
delete_debate_townhall_suggestion("T","dts1")

delete_debate_townhall_suggestion("D","dts2")

println("Calling create_townhall")
create_townhall("user1","t1","title","title_id",x,"image",t+50000,3600,t,"user2","user3,user4","hash,hashemd","",0,0)

println("Calling townhall_action Q")
townhall_action("user2","t1","Q","q1","townhall qtn", t + 100)

println("Calling townhall_approve_question")
townhall_approve_question("t1","q1")

println("Calling townhall_action VQ")
townhall_action("user3","t1","VQ","q1","townhall qtn", t + 100)

println("Calling townhall_action VA")
townhall_action("user4","t1","VA","q1","townhall qtn", t + 100)

println("Calling townhall_action P")
townhall_action("user3","t1","P","q1","townhall qtn", t + 100)

println("Calling townhall_change_moderator")
townhall_change_moderator("t1","user4,user5,user6",t + 100)


println("Calling edit_townhall")
//edit_townhall("user1","t1","title","title_id",x,"image edited",t+50000,3600,t,"user2","")



println("Calling create_petition")
create_petition("","user1","p1","title","title_id",x,"image","to",100,0,t,t+50000,"hashhdh,hashed,hashmed","hau,hasl","",0,0)

println("Calling edit_petition")
edit_petition("","user1","p1","title","title_id",x,"image","to",100,0,t,t+50000,"hashhdh,jjoke","killed","")



println("Calling create_debate")
create_debate("user1","d1","title","title_id",x,"image","criteria",3600,t+50000,t,"hashhdh,hashed,hashmed","killed",0,0)

println("Calling debate_change_moderator")
debate_change_moderator("d1","user5,user6",t + 100)

println("Calling debate_participate FOR")
debate_participate("d1","user3","For","message",t + 100)

println("Calling debate_participate AGAINST")
debate_participate("d1","user4","Against","message",t + 100)

println("Calling debate_shortlist_guests FOR")
debate_shortlist_guests("d1","For","user3")

println("Calling debate_shortlist_guests AGAINST")
debate_shortlist_guests("d1","Against","user4")

println("Calling debate_action P")
debate_action("user3","d1","P","","", t + 100)

println("Calling debate_action Q")
debate_action("user4","d1","Q","dq1","debate qtn", t + 100)

println("Calling debate_action A")
debate_action("user3","d1","A","da1","debate arg", t + 100)

println("Calling debate_comment")
debate_comment("d1","user5","debate comment",t + 100)



println("Calling edit_debate")
edit_debate("user1","d1","title","title_id",x,"image","criteria",3600,t+50000,t,"hey,heyd","killed")

println("Calling get_all_items")
//println(get_all_items("A",""))
//println(get_all_items("E",""))
//println(get_all_items("Q",""))
//println(get_all_items("C",""))
//println(get_all_items("P",""))
//println(get_all_items("T",""))
//println(get_all_items("D",""))

println("Calling get_leftpane")
println(get_leftpane("user1","","","all"))

println("Calling get_more_events")
println(get_more_events("user1","all"))

println("Calling get_more_petitions")
println(get_more_petitions())

println("Calling get_more_debates")
println(get_more_debates())

println("Calling get_more_townhalls")
println(get_more_townhalls())

println("Calling get_all_events")
println(get_all_events("user1","all",10,0))

println("Calling get_all_petitions")
println(get_all_petitions(10,0))

println("Calling get_all_debates")
println(get_all_debates(10,0))

println("Calling get_all_townhalls")
println(get_all_townhalls(10,0))

println("Calling view_suggestions")
//println(view_suggestions("A","art1",""))

println("Calling delete_debate")
println(delete_debate("d1"))
 
println("Calling delete_townhall")
println(delete_townhall("t1"))

println("Calling delete_petition")
println(delete_petition("p1"))



println("Calling update_tiles")
update_tiles()
println("Calling avg_weights")
avg_weights()
println("Calling calc_hash_trends")
calc_hash_trends()
println("Calling calc_user_tiles")
calc_user_tiles()
println("Calling calc_local_tiles")
calc_local_tiles()

println("Calling stream")
println(stream("user1","S",100,0))

println("Calling context for article")
println(context("user1","H","hash,hashemd",100,0))

println("Calling context for event")
println(context("user1","H","hash5,hash2,hash6",100,0))

delete_article("art1")
delete_article("art2")
delete_article("art3")
delete_article("art4")
delete_article("art5")
delete_article("art6")

delete_event("event1")
delete_event("event2")
delete_event("event3")
delete_event("event4")
delete_event("event5")
println("Calling get_all_items")
//println(get_all_items("A",""))
//println(get_all_items("E",""))
//println(get_all_items("Q",""))
//println(get_all_items("C",""))
//println(get_all_items("P",""))
//println(get_all_items("T",""))
//println(get_all_items("D",""))

println()
println()
println()
println()
println()

println("Jar Ok")




  }
  
  def sentiment_analysis(hashtag: String):String =
  {
    var ret = ""
    var ret_map = scala.collection.mutable.Map[String,Any]()
    val tweet_index = getNodeIndex("tweet").get     // get user weight index
    var tweet_node =  tweet_index.get("id",hashtag).getSingle()  // get unique user node based on user name 
    if(tweet_node != null)
    {
      val senti = tweet_node.getProperty("pos").toString() + "," + tweet_node.getProperty("neg").toString() + tweet_node.getProperty("neu").toString()
      val tweet_text = tweet_node.getProperty("tweet_text").toString()
      val tagger = new MaxentTagger(
                "/var/n4j/data/left3words-wsj-0-18.tagger")
      val tagged = tagger.tagString(tweet_text)
              //val prev_nouns = user_node.getProperty("nouns").toString()
      val noun_list = tagged.split(" ").toList.map(x => x.split("/")).filter( x => (x.size == 2 && x(1).equals("NNP"))).map(x => x(0).toLowerCase())
      val noun_map = noun_list.groupBy(x=>x).mapValues(x=>x.length) 
      val nouns = JSONObject(noun_map.toMap).toString()
      val word_list = tweet_text.split(" ").toList
      val hash_map = word_list.filter( x => x.startsWith("#")).map( y => y.replace("#","")).groupBy(x=>x).mapValues(x=>x.length) 
      val hashtags = JSONObject(hash_map.toMap).toString()
      val users_map = word_list.filter( x => x.startsWith("@")).map( y => y.replace("@","")).groupBy(x=>x).mapValues(x=>x.length) 
      val users = JSONObject(users_map.toMap).toString()
      val time = tweet_node.getProperty("time_analysis").toString()
      ret_map("sentiment") = senti
      ret_map("tags") = hashtags
      ret_map("users") = users
      ret_map("nouns") = nouns
      ret_map("time_analysis") = time
      ret = JSONObject(ret_map.toMap).toString()
    }
    ret
  }
  
  def create_tweet(t_id: String,t_text: String,t_time: String)
  {
    
    
    withTx {
    implicit neo =>
      
          val tweet_index = getNodeIndex("tweet").get     // get user weight index
          var tweet_node =  tweet_index.get("id",t_id).getSingle()  // get unique user node based on user name
          val sentClassifier = new SentimentClassifier()
	      val sent = sentClassifier.classify(t_text);
          if(tweet_node == null)
          {
            
            var time_map = scala.collection.mutable.Map[String,Double]()
	        time_map += (t_time -> 1)
	        val time = JSONObject(time_map.toMap).toString()
            tweet_node = createNode(tweet(t_id,0,0,0,t_text,time))  // Creating a new user node
	        tweet_index += (tweet_node,"id",t_id) 
	        
	      }
          
          else
          {
            val flag = tweet_node.getProperty(sent).toString().toInt
            tweet_node.setProperty(sent,flag + 1)
            val tweet_text = tweet_node.getProperty("tweet_text").toString() + " " + t_text
            tweet_node.setProperty("tweet_text",tweet_text)
            
            val time = tweet_node.getProperty("time_analysis").toString()
            val time_map_new = JSON.parseFull(time).get.asInstanceOf[Map[String,Double]]
            //println(time_map_new)
            val n = collection.mutable.Map(time_map_new.toSeq: _*)
            
            val f = n(t_time) + 1
            n(t_time) = f
            val time_json = JSONObject(n.toMap).toString()
            
            tweet_node.setProperty("time_analysis",time_json)
          }
      
    }
  }
  
  def tweet_sentiment(hashtags: String)
  {
    
    val a1 = hashtags.split(",").map( x => x.toLowerCase())
    val config = new twitter4j.conf.ConfigurationBuilder()
    .setOAuthConsumerKey("uzUOk0hpd5LjdxQ2UMvzPBXZ8")
    .setOAuthConsumerSecret("cNkrIDDaXRGRQn6MwZaQZDjybxXh4vyhT44Iq4vtwmXDyw0XXe")
    .setOAuthAccessToken("2475082063-I3wAWPg9FqMHfpm2uq5AhqUkQ9NZBxj9YfeeGfg")
    .setOAuthAccessTokenSecret("iHcj2e5rHXW7l2pvTxswiNg23cZ0k5xsJQXeYMNSnROIQ")
    .build
    
    
 // var x = 0  
    val twitterStream = new TwitterStreamFactory(config).getInstance
    twitterStream.addListener(simpleStatusListener)
    def simpleStatusListener = new StatusListener() {
    def onStatus(status: Status) {
          
      if(!status.isRetweet())
      {
          val t_id = status.getId() + ""
          val tweet_time = status.getCreatedAt().getHours() + ""
          //println(t_id)
          //create_tweet(t_id)
          val tweet_text = status.getText().toLowerCase()
          //println(tweet_text) 
          for(each <- a1)
          {
            if(tweet_text.contains(each))
            {
              create_tweet(each,tweet_text,tweet_time)
            }
          }
      }
    }
	  def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {}
	  def onTrackLimitationNotice(numberOfLimitedStatuses: Int) {}
	  def onException(ex: Exception) { ex.printStackTrace }
	  def onScrubGeo(arg0: Long, arg1: Long) {}
	  def onStallWarning(warning: StallWarning) {}
	}
	    
	    // val a1 = hashtags.split(",")
	     val a2 = Array("en")
	   // var i = 0
	    twitterStream.filter(new FilterQuery().track(a1).language(a2))
	    
	    
    
  
  }
  
  
  def get_relation_names()       
    {
    
    
    withTx {
    implicit neo =>
      
      var TilesIndex = getNodeIndex("tiles").get
//      val FeaturedTilesIndex = getNodeIndex("featured_tiles").get
      
      val TownhallIndex = getNodeIndex("townhall").get
      val DebateIndex = getNodeIndex("debate").get
      val EventIndex = getNodeIndex("event").get
      val PetitionIndex = getNodeIndex("petition").get
      val ArticleIndex = getNodeIndex("article").get
      val UserIndex = getNodeIndex("user").get
      
      val cur_time = (System.currentTimeMillis()/1000).toInt
      
      println("Townhall relations")
      println("------------------")
      println()
      val townhall_list = TownhallIndex.query("id", "*" ).iterator().asScala.toList
      var townhalls  = List[String]()
      for(each <- townhall_list)
      {
        val rels = each.getRelationships().asScala.toList
        for(e <- rels)
        {
          val rel_name = e.getStartNode().getProperty("__CLASS__").toString + " --> " + e.getType().toString + " --> " + e.getEndNode().getProperty("__CLASS__").toString
          if(!townhalls.contains(rel_name))
          {
            townhalls :+= rel_name
          }
        }
      }
      
      println(townhalls)
      println()
      println()
      println()
      println()
      
      println("Debate relations")
      println("------------------")
      println()
      val debate_list = DebateIndex.query("id", "*" ).iterator().asScala.toList
      var debates  = List[String]()
      for(each <- debate_list)
      {
        val rels = each.getRelationships().asScala.toList
        for(e <- rels)
        {
          val rel_name = e.getStartNode().getProperty("__CLASS__").toString + " --> " + e.getType().toString + " --> " + e.getEndNode().getProperty("__CLASS__").toString
          if(!debates.contains(rel_name))
          {
            debates :+= rel_name
          }
        }
      }
      
      println(debates)
      println()
      println()
      println()
      println()
      
      println("Events relations")
      println("------------------")
      println()
      val event_list = EventIndex.query("id", "*" ).iterator().asScala.toList
      var events  = List[String]()
      for(each <- event_list)
      {
        val rels = each.getRelationships().asScala.toList
        for(e <- rels)
        {
          val rel_name = e.getStartNode().getProperty("__CLASS__").toString + " --> " + e.getType().toString + " --> " + e.getEndNode().getProperty("__CLASS__").toString
          if(!events.contains(rel_name))
          {
            events :+= rel_name
          }
        }
      }
      println(events)
      println()
      println()
      println()
      println()
      
      
      println("Petition relations")
      println("------------------")
      println()
      val petition_list = PetitionIndex.query("id", "*" ).iterator().asScala.toList
      var petitions  = List[String]()
      for(each <- petition_list)
      {
        val rels = each.getRelationships().asScala.toList
        for(e <- rels)
        {
          val rel_name = e.getStartNode().getProperty("__CLASS__").toString + " --> " + e.getType().toString + " --> " + e.getEndNode().getProperty("__CLASS__").toString
          if(!petitions.contains(rel_name))
          {
            petitions :+= rel_name
          }
        }
      }
      println(petitions)
      println()
      println()
      println()
      println()
      
      println("Article relations")
      println("------------------")
      println()
      val article_list = ArticleIndex.query("id", "*" ).iterator().asScala.toList
      var articles  = List[String]()
      for(each <- article_list)
      {
        val rels = each.getRelationships().asScala.toList
        for(e <- rels)
        {
          val rel_name = e.getStartNode().getProperty("__CLASS__").toString + " --> " + e.getType().toString + " --> " + e.getEndNode().getProperty("__CLASS__").toString
          if(!articles.contains(rel_name))
          {
            articles :+= rel_name
          }
        }
      }
      println(articles)
      println()
      println()
      println()
      println()
      
      println("User relations")
      println("------------------")
      println()
      val user_list = UserIndex.query("id", "*" ).iterator().asScala.toList
      var users  = List[String]()
      for(each <- user_list)
      {
        val rels = each.getRelationships().asScala.toList
        for(e <- rels)
        {
          val rel_name = e.getStartNode().getProperty("__CLASS__").toString + " --> " + e.getType().toString + " --> " + e.getEndNode().getProperty("__CLASS__").toString
          if(!users.contains(rel_name))
          {
            users :+= rel_name
          }
        }
      }
      println(users)
      println()
      println()
      println()
      println()
      
     }
    }

  
  def main(): Unit = {
    //jar_check()
    //insert_dummy_data()
    
		  try
        {
            val serverTransport : TServerSocket = new TServerSocket(9779);
            val processor = new User_nodeService.Processor(User_node);
            val args1 : Args = new Args(serverTransport);
            args1.processor(processor);
            val server : TServer = new TThreadPoolServer(args1);
            System.out.println("Sadda haq ethe rakh");
            server.serve();
        }
        catch {
          
            case x: Exception => x.printStackTrace();
        } 
  }
  
  /**
   * @author kalyan kumar komati
   * 
   * 
   * To test localhost by inserting and verifying graph db and functions
   * 
   */
  def insert_dummy_data(): Unit = {
    
    System.out.println("Inserting dummy data");
    
    //initialize graph.db with pre required data
    calc_views()
    update_tiles_temp()
    update_tiles_td()
    calc_hash_trends()
    nouns_update()
    calc_user_tiles()
    calc_local_tiles()
    
    var t = (System.currentTimeMillis()/1000).toInt
    
    var i =0
    
    /*
    //creating users from user1 to user100
    //for(i <- 1 to 100)
      //create_user("fname"+i,"lname"+i,"user"+i,"email"+i,"hyd",1,t,200);
    //System.out.println("users created from user1 to user100");
    
    //display users info from user1 to user100
    //get the user node index
    var userNodeIndex = getNodeIndex("user").get
    //get all user nodes into a list
      //create_user("PMO","India","pmoindia","email"+i,"hyd",1,t,200);
      edit_user("USER1","user","user100","email","location",2);
    var userNodes = userNodeIndex.query("id","*").iterator().asScala.toList
    //new list to represent key set for json
    val l1 : List[String] = List("first_name","last_name","user_name","email","location","time_created")
    //output list that contains json
    var list = List[Any]()
    
    var user = userNodeIndex.get("id","user100").getSingle()
    for( x <- userNodes )	//for each user / user node from the list
    {
      
      if(x == user){
      list :+= JSONObject (	//create json and add it to list
    		  				l1.zip(//zip l1 as keys and node properties as values
    		  				    List(	//creating new list with node properties
    		  						x.getProperty("first_name").toString(),
    		  						x.getProperty("last_name").toString(),
    		  						x.getProperty("user_name").toString(),
    		  						x.getProperty("email").toString(),
    		  						x.getProperty("location").toString(),
    		  						x.getProperty("time_created").toString()
    		  						)
    		                ).toMap //convert the list to Map
    		             );
      list :+= JSONObject (	//create json and add it to list
    		  				l1.zip(//zip l1 as keys and node properties as values
    		  				    List(	//creating new list with node properties
    		  						x.getProperty("first_name")+ " " +x.getProperty("last_name"),
    		  						x.getProperty("user_name").toString(),
    		  						x.getProperty("email").toString(),
    		  						x.getProperty("location").toString(),
    		  						x.getProperty("time_created").toString()
    		  						)
    		                ).toMap //convert the list to Map
    		             );
        System.out.println(x.getProperty("first_name") + " " + x.getProperty("last_name"))
        System.out.println(x.getProperty("first_name").toString() + " " + x.getProperty("last_name").toString())
      }
    }
    System.out.println(JSONArray(list).toString());
    System.out.println("Display users from user1 to user100"); 
    * 
    * 
    */
   /* System.out.println(
    create_space("user98","sk1","sk1","sk1","sk1","sk1",t,0,0));
    System.out.println(
    create_space("user99","sk2","sk2","sk2","sk2","sk2",t,0,0));
    System.out.println(
    create_space("user99","sk3","sk3","sk3","sk3","sk3",t,0,1));
    System.out.println(
    create_space("user100","sk4","sk4","sk4","sk4","sk4",t,0,0));
    
    */
    //space_follow("user99","sk4");
    /*System.out.println(get_user_spaces("user98","c"));
    System.out.println(get_user_spaces("user99","f"));
    System.out.println(get_user_spaces("user100","f"));*/
    
    /*//create three articles with different hash tags
    System.out.println(
    create_article("user98","article98","article-title-id-98","article title 1","article contnet 1","article summery 1","article fut image2", "technology","hashtag1,hashtag2","","",t,"","","",0,0,0))
    System.out.println(
    create_article("user99","article99","article-title-id-99","article title 2","article contnet 2","article summery 2","article fut image2", "politics","hashtag2,hashtag3","","",t+10000,"","","",0,0,0))
    System.out.println(
    create_article("user100","article100","article-title-id-100","article title 3","article contnet 3","article summery 3","article fut image", "sports","hashtag3,hashtag4","","",t+20000,"","","",0,0,0))
    //search for suggestions by calling view_suggestions
    
    System.out.println(view_suggestions("A","article98","",2,"user97","hashtag4")) 
    
    create_space("user98","spaceid1","spacetitle1","spacetitle1","spacetagline1","spacefutimage1",t,0,0)
    create_space("user98","spaceid2","spacetitle2","spacetitle2","spacetagline2","spacefutimage2",t,0,0)
    space_tagitem("spaceid1","A","article98","A")
    space_tagitem("spaceid2","A","article98","")
    space_tagitem("spaceid2","A","article99","")
    System.out.println(get_all_items("A","user99",10,0)) */
    
    
    //System.out.println(1 + get_tiles("user98","all",4,0,"spaceid1","T"))
    /*System.out.println(2 + get_articles_space("user98","spaceid1",4,0,0))
    System.out.println(3 + get_articles_space("user98","spaceid1",4,0,1))
    System.out.println(4 + get_articles_space("user98","spaceid1",4,0,2))
    System.out.println(5 + get_articles_space("user98","spaceid2",4,0,0))
    System.out.println(6 + get_articles_space("user98","spaceid2",4,0,1))
    System.out.println(7 + get_articles_space("user98","spaceid2",4,0,2))
    System.out.println(8 + get_articles_space("user98","sk1",4,0,0))
    System.out.println(9 + get_articles_space("user98","sk1",4,0,1))
    System.out.println(10 + get_articles_space("user98","sk1",4,0,2))
    System.out.println(11 + get_articles_space("user98","sk2",4,0,0))
    System.out.println(12 + get_articles_space("user98","sk2",4,0,1))
    System.out.println(13 + get_articles_space("user98","sk2",4,0,2))
    System.out.println(14 + get_articles_space("user98","sk3",4,0,0))
    System.out.println(15 + get_articles_space("user98","sk3",4,0,1))
    System.out.println(16 + get_articles_space("user98","sk3",4,0,2))
    System.out.println(17 + get_articles_space("user98","sk4",4,0,0))
    System.out.println(18 + get_articles_space("user98","sk4",4,0,1))
    System.out.println(19 + get_articles_space("user98","sk4",4,0,2))
    
    System.out.println(create_event("user98", "event1_user98", "event_title_id_1", "event title 1", "event content", t , t+t, 0, "hyderabad", "sports", "sport_event", "sport_event_refer_tag","",t-10000, "","event_summery", 0, "event fut image", 0));
    System.out.println(create_event("user98", "event2_user98", "event_title_id_2", "event title 2", "event content", t , t+t, 0, "hyderabad", "sports", "sport_event", "sport_event_refer_tag","",t-10000, "","event_summery", 0, "event fut image", 0));
    
    System.out.println("\n\n empty \n\n" + get_tiles("user98", "all", 100, 0, "", ""));
    System.out.println("\n\n f \n\n" + get_tiles("user98", "all", 100, 0, "", "f"));
    System.out.println("\n\n h \n\n" + get_tiles("user98", "all", 100, 0, "", "h"));
    System.out.println("\n\n up \n\n" + get_tiles("user98", "all", 100, 0, "user98", "up"));
    System.out.println("\n\n l \n\n" + get_tiles("user98", "all", 100, 0, "", "l")); */
    /*
    System.out.println("User 98, 99, 100 created spaces");
    System.out.println(get_spaces("user98","c"));
    System.out.println(get_spaces("user99","c"));
    System.out.println(get_spaces("user100","c"));
    
    
    System.out.println("User 98, 99, 100 following spaces");
    System.out.println(get_spaces("user98","f"));
    System.out.println(get_spaces("user99","f"));
    System.out.println(get_spaces("user100","f"));
    
    
    System.out.println("User all spaces");
    System.out.println(get_spaces("user98","s"));
    System.out.println(get_spaces("user99","s"));
    System.out.println(get_spaces("user100","s"));
    
    
    System.out.println("User all open spaces");
    System.out.println(get_spaces("user98","so"));
    System.out.println(get_spaces("user99","so"));
    System.out.println(get_spaces("user100","so"));
    
    System.out.println("User all closed spaces");
    System.out.println(get_spaces("user98","sc"));
    System.out.println(get_spaces("user99","sc"));
    System.out.println(get_spaces("user100","sc"));
    
    System.out.println("User attending event1_user98" + event_response("event1_user98", "user95", t))
    System.out.println("User Not attending event1_user98" + event_response("event1_user98", "user95", t))
    System.out.println("User attending event1_user98" + event_response("event1_user98", "user95", t))
    System.out.println("User Not attending event1_user98" + event_response("event1_user98", "user95", t))
    System.out.println("User attending event1_user98" + event_response("event1_user98", "user95", t))
    System.out.println("User Not attending event1_user98" + event_response("event1_user98", "user95", t))
    System.out.println("User attending event1_user98" + event_response("event1_user98", "user95", t)) 
    
    
    
    System.out.println("User attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User Not attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User Not attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User Not attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User Not attending event1_user98" + event_response("event1_user98", "user100", t))
    
    System.out.println("User attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User Not attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User Not attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User Not attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User Not attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User Not attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User Not attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User Not attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User Not attending event1_user98" + event_response("event1_user98", "user100", t))
    
    System.out.println("User attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User Not attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User Not attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User Not attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User attending event1_user98" + event_response("event1_user98", "user100", t))
    System.out.println("User Not attending event1_user98" + event_response("event1_user98", "user100", t)) */
    
  }

}
