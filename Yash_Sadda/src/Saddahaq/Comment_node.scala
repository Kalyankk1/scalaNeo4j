package Saddahaq
//import sys.ShutdownHookThread
import org.neo4j.scala.{TypedTraverser, SingletonEmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}
//import org.neo4j.graphdb.Node
import org.neo4j.scala.Neo4jIndexProvider
import collection.JavaConverters._
//import scala.collection.immutable.Map
import org.neo4j.graphdb.Direction
import scala.util.parsing.json.JSONArray
//import org.apache.thrift.server.TServer
//import org.apache.thrift.server.TThreadPoolServer
//import org.apache.thrift.server.TThreadPoolServer.Args
//import org.apache.thrift.transport.TServerSocket
//import org.apache.thrift.transport.TTransportException
//import java.util.Calendar
//import java.util.Date
//import org.neo4j.graphdb.index.IndexHits
//import scala.collection.immutable.ListMap
//import org.neo4j.index.lucene.QueryContext
import org.neo4j.index.lucene.ValueContext
import scala.util.parsing.json.JSONObject
import scala.collection.mutable.ListBuffer

case class comment(val comment_id: String,
    val comment_content: String,
    val time_created: Int,
    val weight: Int,
    val spam_weight: Int)
    
trait Comment_node extends Neo4jWrapper with SingletonEmbeddedGraphDatabaseServiceProvider with TypedTraverser with Neo4jIndexProvider{

  
// Triggered when a new comment is created
def create_comment(
      c_itemid: String,  // Parent id of the comment (article id, event id, comment id)
	  c_itemgroup: String, // Article/Event/Comment
	  c_id: String, // unique comment id
	  c_content: String,
	  c_users: String,
      c_time_created: Int,
      user_name: String  // unique user name
      ):Boolean =
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
    case "P" => "Petition"
    case "a" => "Article"
    case "e" => "Event"
    case "q" => "Quickpost"
    case "c" => "Comment"
    case "u" => "User"
    case "h" => "Sub_category"
    case "p" => "Petition"
    }
      
     val i_type = matchTest(c_itemgroup)
     val event_weight_index = getNodeIndex("event_weight").get
     val hash_weight_index = getNodeIndex("hash_weight").get
     val user_weight_index = getNodeIndex("user_weight").get
     val comment_index = getNodeIndex("comment").get
     val article_weight_index = getNodeIndex("article_weight").get
     val p_weight_index = getNodeIndex("petition_weight").get
     val UserIndex = getNodeIndex("user").get
     val user_node = UserIndex.get("id",user_name).getSingle()
     var comment_node = comment_index.get("id",c_id).getSingle()
     val ParentIndex = getNodeIndex(i_type.toLowerCase()).get
     val parent_node = ParentIndex.get("id",c_itemid).getSingle()
     var ret = false
     var hash_nodes = List[org.neo4j.graphdb.Node]()
     if(comment_node == null && user_node != null && parent_node != null)
	 {
         user_node.setProperty("last_seen",c_time_created)
	     val u_wt = user_node.getProperty("weight").toString().toInt
	     val c_wt = Math.round(u_wt.asInstanceOf[Float]/300)
	    
	     
	     comment_node = createNode(comment(c_id,c_content,c_time_created,c_wt,0))
	     comment_index += (comment_node,"id",c_id)
	     user_node.setProperty("weight",u_wt+1)
	     user_weight_index.remove(user_node)
	     user_weight_index += (user_node,"weight",new ValueContext( u_wt+1 ).indexNumeric())
	     
	     var rel: org.neo4j.graphdb.Relationship = comment_node --> "Comment_Written_By" --> user_node <
	     var rel_time = rel.setProperty("time", c_time_created)
	     var rel_in_wt = rel.setProperty("in_weight", 1)
	     var rel_out_wt = rel.setProperty("out_weight", c_wt)
	
	     val p_wt = parent_node.getProperty("weight").toString().toInt
	     parent_node.setProperty("weight",p_wt+1)
	     if(i_type.equals("Article"))
	     {
	       hash_nodes = parent_node.getRelationships("Belongs_To_Subcategory_Article",Direction.OUTGOING).asScala.map(_.getOtherNode(parent_node)).toList
	       
	       article_weight_index.remove(parent_node)
	       article_weight_index += (parent_node,"weight",new ValueContext( p_wt+1 ).indexNumeric())
	       var rel = comment_node --> "Comment_To_Article" --> parent_node <
	       var rel_time = rel.setProperty("time", c_time_created)
	       var rel_in_wt = rel.setProperty("in_weight", 1)
	       var rel_out_wt = rel.setProperty("out_weight", 0)
	       val author_node = parent_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode()
	       val au_wt = author_node.getProperty("weight").toString().toInt
	       author_node.setProperty("weight",au_wt+1)
	       user_weight_index.remove(author_node)
	       user_weight_index += (author_node,"weight",new ValueContext( au_wt+1 ).indexNumeric())   
	       
	     }
	     else if(i_type.equals("Event"))
	     {
	        hash_nodes = parent_node.getRelationships("Belongs_To_Subcategory_Event",Direction.OUTGOING).asScala.map(_.getOtherNode(parent_node)).toList
	        event_weight_index.remove(parent_node)
	        event_weight_index += (parent_node,"weight",new ValueContext( p_wt+1 ).indexNumeric())
	        var rel = comment_node --> "Comment_To_Event" --> parent_node <
	        var rel_time = rel.setProperty("time", c_time_created)
	        var rel_in_wt = rel.setProperty("in_weight", 1)
	        var rel_out_wt = rel.setProperty("out_weight", 0)
	        val author_node = parent_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode()
	        
	        val au_wt = author_node.getProperty("weight").toString().toInt
	        author_node.setProperty("weight",au_wt+1)
	        user_weight_index.remove(author_node)
	        user_weight_index += (author_node,"weight",new ValueContext( au_wt+1 ).indexNumeric())
	        
	     }
	     
	     else if(i_type.equals("Petition"))
	     {
	        hash_nodes = parent_node.getRelationships("Belongs_To_Subcategory_Petition",Direction.OUTGOING).asScala.map(_.getOtherNode(parent_node)).toList
	        p_weight_index.remove(parent_node)
	        p_weight_index += (parent_node,"weight",new ValueContext( p_wt+1 ).indexNumeric())
	        var rel = comment_node --> "Comment_To_Petition" --> parent_node <
	        var rel_time = rel.setProperty("time", c_time_created)
	        var rel_in_wt = rel.setProperty("in_weight", 1)
	        var rel_out_wt = rel.setProperty("out_weight", 0)
	        val author_node = parent_node.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode()
	        
	        val au_wt = author_node.getProperty("weight").toString().toInt
	        author_node.setProperty("weight",au_wt+1)
	        user_weight_index.remove(author_node)
	        user_weight_index += (author_node,"weight",new ValueContext( au_wt+1 ).indexNumeric())
	        
	     }
	     else if(i_type.equals("Comment"))
	     {
	    	var rel = comment_node --> "Comment_To_Comment" --> parent_node <
	        var rel_time = rel.setProperty("time", c_time_created)
	        var rel_in_wt = rel.setProperty("in_weight", 1)
	        var rel_out_wt = rel.setProperty("out_weight", 0)
	        if(parent_node.getSingleRelationship("Comment_To_Article",Direction.OUTGOING) != null)
	        {
	          val art_node = parent_node.getSingleRelationship("Comment_To_Article",Direction.OUTGOING).getEndNode()
	          hash_nodes = art_node.getRelationships("Belongs_To_Subcategory_Article",Direction.OUTGOING).asScala.map(_.getOtherNode(art_node)).toList
	          val author_node = art_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode()
	          val a_wt = art_node.getProperty("weight").toString().toInt
	          art_node.setProperty("weight",a_wt+1)
	          article_weight_index.remove(art_node)
	          article_weight_index += (art_node,"weight",new ValueContext( a_wt+1 ).indexNumeric())
	          var rel = comment_node --> "Comment_To_Article" --> art_node <
	          var rel_time = rel.setProperty("time", c_time_created)
	          var rel_in_wt = rel.setProperty("in_weight", 1)
	          var rel_out_wt = rel.setProperty("out_weight", 0)
	          
	          val au_wt = author_node.getProperty("weight").toString().toInt
	          author_node.setProperty("weight",au_wt+1)
	          user_weight_index.remove(author_node)
	          user_weight_index += (author_node,"weight",new ValueContext( au_wt+1 ).indexNumeric())
	          
	          
	        }
	        
	        else if(parent_node.getSingleRelationship("Comment_To_Event",Direction.OUTGOING) != null)
	        {
	          val event_node = parent_node.getSingleRelationship("Comment_To_Event",Direction.OUTGOING).getEndNode()
	          hash_nodes = event_node.getRelationships("Belongs_To_Subcategory_Event",Direction.OUTGOING).asScala.map(_.getOtherNode(event_node)).toList
	          val author_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode()
	          val e_wt = event_node.getProperty("weight").toString().toInt
	          event_node.setProperty("weight",e_wt+1)
	          event_weight_index.remove(event_node)
	          event_weight_index += (event_node,"weight",new ValueContext( e_wt+1 ).indexNumeric())
	          var rel = comment_node --> "Comment_To_Event" --> event_node <
	          var rel_time = rel.setProperty("time", c_time_created)
	          var rel_in_wt = rel.setProperty("in_weight", 1)
	          var rel_out_wt = rel.setProperty("out_weight", 0)
	          
	          val au_wt = author_node.getProperty("weight").toString().toInt
	          author_node.setProperty("weight",au_wt+1)
	          user_weight_index.remove(author_node)
	          user_weight_index += (author_node,"weight",new ValueContext( au_wt+1 ).indexNumeric())
	          
	          
	        }
	    	
	    	else
	        {
	          val p_node = parent_node.getSingleRelationship("Comment_To_Petition",Direction.OUTGOING).getEndNode()
	          hash_nodes = p_node.getRelationships("Belongs_To_Subcategory_Petition",Direction.OUTGOING).asScala.map(_.getOtherNode(p_node)).toList
	          val author_node = p_node.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode()
	          val pet_wt = p_node.getProperty("weight").toString().toInt
	          p_node.setProperty("weight",pet_wt+1)
	          p_weight_index.remove(p_node)
	          p_weight_index += (p_node,"weight",new ValueContext( pet_wt+1 ).indexNumeric())
	          var rel = comment_node --> "Comment_To_Petition" --> p_node <
	          var rel_time = rel.setProperty("time", c_time_created)
	          var rel_in_wt = rel.setProperty("in_weight", 1)
	          var rel_out_wt = rel.setProperty("out_weight", 0)
	          
	          val au_wt = author_node.getProperty("weight").toString().toInt
	          author_node.setProperty("weight",au_wt+1)
	          user_weight_index.remove(author_node)
	          user_weight_index += (author_node,"weight",new ValueContext( au_wt+1 ).indexNumeric())
	          
	          
	        }
	        
	       
	     }
	     
	     for(each <- hash_nodes)
	     {
		     val h_wt = each.getProperty("weight").toString().toInt
		     each.setProperty("weight",h_wt+1)
		     hash_weight_index.remove(each)
		     hash_weight_index += (each,"weight",new ValueContext( h_wt+1 ).indexNumeric())
	     }
	     
	          if(!c_users.equals(""))
	          {
	            val c_users_list = c_users.split(",").distinct
	            for(each <- c_users_list)
	            {
	              if(!each.equals(""))
	              {
	              
		              val user_node1 = UserIndex.get("id",each).getSingle()
		              if(user_node1 != null)
		              {
		                val rel1: org.neo4j.graphdb.Relationship = user_node1 --> "User_Of_Comment"  --> comment_node <
		                val y = rel1.setProperty("time", c_time_created)
		              }
	              }
	          
	            }
	          
		      }
	     ret = true
     }
    ret
    }
    
  }

// Triggered when a user comments on his own Article/Event
  def create_comment_own(
      c_itemid: String,  // Parent id of the comment (article id, event id, comment id)
	  c_itemgroup: String, // Article/Event/Comment
	  c_id: String, // unique comment id
	  c_content: String,
	  c_users: String,
      c_time_created: Int,
      user_name: String  // unique user name
      ):Boolean =
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
    case "P" => "Petition"
    case "a" => "Article"
    case "e" => "Event"
    case "q" => "Quickpost"
    case "c" => "Comment"
    case "u" => "User"
    case "h" => "Sub_category"
    case "p" => "Petition"
    }
    val i_type = matchTest(c_itemgroup)
    
     val UserIndex = getNodeIndex("user").get
     val user_node = UserIndex.get("id",user_name).getSingle()
     val ParentIndex = getNodeIndex(i_type.toLowerCase()).get
     val comment_index = getNodeIndex("comment").get
     var comment_node = comment_index.get("id",c_id).getSingle()
     val parent_node = ParentIndex.get("id",c_itemid).getSingle()
     
     var ret = false
     if(comment_node == null && user_node != null && parent_node != null)
	 {
         user_node.setProperty("last_seen",c_time_created)
	     val u_wt = user_node.getProperty("weight").toString().toInt
	     val c_wt = Math.round(u_wt.asInstanceOf[Float]/300)
	    
	     
	     comment_node = createNode(comment(c_id,c_content,c_time_created,c_wt,0))
	     
	     comment_index += (comment_node,"id",c_id)
	    
	     var rel: org.neo4j.graphdb.Relationship = comment_node --> "Comment_Written_By" --> user_node <
	     var rel_time = rel.setProperty("time", c_time_created)
	     var rel_in_wt = rel.setProperty("in_weight", 0)
	     var rel_out_wt = rel.setProperty("out_weight", c_wt)
	
	     if(i_type.equals("Article"))
	     {
	       
	       var rel = comment_node --> "Comment_To_Article" --> parent_node <
	       var rel_time = rel.setProperty("time", c_time_created)
	       var rel_in_wt = rel.setProperty("in_weight", 0)
	       var rel_out_wt = rel.setProperty("out_weight", 0)
	       
	     }
	     else if(i_type.equals("Event"))
	     {
	        
	        var rel = comment_node --> "Comment_To_Event" --> parent_node <
	        var rel_time = rel.setProperty("time", c_time_created)
	        var rel_in_wt = rel.setProperty("in_weight", 0)
	        var rel_out_wt = rel.setProperty("out_weight", 0)
	        
	        
	     }
	     else if(i_type.equals("Petition"))
	     {
	        
	        var rel = comment_node --> "Comment_To_Petition" --> parent_node <
	        var rel_time = rel.setProperty("time", c_time_created)
	        var rel_in_wt = rel.setProperty("in_weight", 0)
	        var rel_out_wt = rel.setProperty("out_weight", 0)
	        
	        
	     }
	     else if(i_type.equals("Comment"))
	     {
	        
	        var rel = comment_node --> "Comment_To_Comment" --> parent_node <
	        var rel_time = rel.setProperty("time", c_time_created)
	        var rel_in_wt = rel.setProperty("in_weight", 0)
	        var rel_out_wt = rel.setProperty("out_weight", 0)
	        
	        if(parent_node.getSingleRelationship("Comment_To_Article",Direction.OUTGOING) != null)
	        {
	          val art_node = parent_node.getSingleRelationship("Comment_To_Article",Direction.OUTGOING).getEndNode()
	          var rel = comment_node --> "Comment_To_Article" --> art_node <
	          var rel_time = rel.setProperty("time", c_time_created)
	          var rel_in_wt = rel.setProperty("in_weight", 0)
	          var rel_out_wt = rel.setProperty("out_weight", 0)
	         
	        }
	        
	        else if(parent_node.getSingleRelationship("Comment_To_Event",Direction.OUTGOING) != null)
	        {
	          val event_node = parent_node.getSingleRelationship("Comment_To_Event",Direction.OUTGOING).getEndNode()
	          
	          var rel = comment_node --> "Comment_To_Event" --> event_node <
	          var rel_time = rel.setProperty("time", c_time_created)
	          var rel_in_wt = rel.setProperty("in_weight", 0)
	          var rel_out_wt = rel.setProperty("out_weight", 0)
	          
	        }
	        
	        else
	        {
	          val p_node = parent_node.getSingleRelationship("Comment_To_Petition",Direction.OUTGOING).getEndNode()
	          
	          var rel = comment_node --> "Comment_To_Petition" --> p_node <
	          var rel_time = rel.setProperty("time", c_time_created)
	          var rel_in_wt = rel.setProperty("in_weight", 0)
	          var rel_out_wt = rel.setProperty("out_weight", 0)
	          
	        }
	        
	       
	     }
	     
	          if(!c_users.equals(""))
	          {
	            val c_users_list = c_users.split(",").distinct
	            for(each <- c_users_list)
	            {
	              if(!each.equals(""))
	              {
	              
		              val user_node1 = UserIndex.get("id",each).getSingle()
		              if(user_node1 != null)
		              {
		                val rel1: org.neo4j.graphdb.Relationship = user_node1 --> "User_Of_Comment"  --> comment_node <
		                val y = rel1.setProperty("time", c_time_created)
		              }
	              }
	          
	            }
	          
		      }
	     ret = true
     }
     ret
    }
    
  }

  // Triggered when a comment is edited
def edit_comment(user_name: String,
      c_id: String,
      c_content: String,
      c_users: String,
      c_time_created: Int
      ):Boolean =
  {
    
    
    withTx {
    implicit neo =>
       val user_index = getNodeIndex("user").get
       val commentIndex = getNodeIndex("comment").get
       val comment_node = commentIndex.get("id",c_id).getSingle()
       val user_node = user_index.get("id",user_name).getSingle()
       var ret = false
       
       
       if(user_node != null && comment_node != null)
	   {
         user_node.setProperty("last_seen",c_time_created)
         val author_node = comment_node.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()
         if(user_node.equals(author_node))
         {
           
             var rels = comment_node.getRelationships("User_Of_Comment").asScala.toList
             if(rels != null)
             {
			     for(each <- rels)
			     {
			       each.delete()
			     }
             }
	         comment_node.setProperty("comment_content",c_content)
	         
	          if(!c_users.equals(""))
	          {
	            val c_users_list = c_users.split(",").distinct
	            for(each <- c_users_list)
	            {
	              if(!each.equals(""))
	              {
	              
		              val user_node1 = user_index.get("id",each).getSingle()
		              if(user_node1 != null)
		              {
		                val rel1: org.neo4j.graphdb.Relationship = user_node1 --> "User_Of_Comment"  --> comment_node <
		                val y = rel1.setProperty("time", c_time_created)
		              }
	              }
	          
	            }
	          
		      }
	         ret = true
         }
	   } 
     ret  
    }
  }


  // Triggered when a comment is deleted
  def delete_comment(
      id: String  // unique comment id
      ):Boolean =
  {
    
   
    withTx {
    implicit neo =>
       
       
       val user_weight_index = getNodeIndex("user_weight").get
       val article_weight_index = getNodeIndex("article_weight").get
       val event_weight_index = getNodeIndex("event_weight").get
       val p_weight_index = getNodeIndex("petition_weight").get
       val hash_weight_index = getNodeIndex("hash_weight").get
       val CommentIndex = getNodeIndex("comment").get
       val comment_node = CommentIndex.get("id",id).getSingle()
       var ret = false
       var hash_nodes =  List[org.neo4j.graphdb.Node]()
       if(comment_node != null)
	   {
	       var item_node = comment_node
	       var author_node = comment_node
	       if(comment_node.hasRelationship("Comment_To_Article",Direction.OUTGOING))
	       {
	          item_node = comment_node.getSingleRelationship("Comment_To_Article",Direction.OUTGOING).getEndNode()
	          author_node = item_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode()
	          hash_nodes = item_node.getRelationships("Belongs_To_Subcategory_Article",Direction.OUTGOING).asScala.map(_.getOtherNode(item_node)).toList
	       
	       }
	       else if(comment_node.hasRelationship("Comment_To_Event",Direction.OUTGOING))
	       {
	         
	         item_node = comment_node.getSingleRelationship("Comment_To_Event",Direction.OUTGOING).getEndNode()
	         author_node = item_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode()
	         hash_nodes = item_node.getRelationships("Belongs_To_Subcategory_Event",Direction.OUTGOING).asScala.map(_.getOtherNode(item_node)).toList
	       
	       }
	       else
	       {
	         
	         item_node = comment_node.getSingleRelationship("Comment_To_Petition",Direction.OUTGOING).getEndNode()
	         author_node = item_node.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode()
	         hash_nodes = item_node.getRelationships("Belongs_To_Subcategory_Petition",Direction.OUTGOING).asScala.map(_.getOtherNode(item_node)).toList
	       
	       }
	       var i_wt = item_node.getProperty("weight").toString().toInt
	       var au_wt = author_node.getProperty("weight").toString().toInt
	       var h_wt = 0
	       var del_list  = ListBuffer[org.neo4j.graphdb.Node]()
	       var empt_list = ListBuffer[org.neo4j.graphdb.Node]()
	       del_list = deleteEntry(comment_node,empt_list)
	       
	       for(each <- del_list)
	       {
	         val rels = each.getRelationships().asScala
	         val c_author_node = each.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()
	         if(c_author_node.equals(author_node))
	         {
	           
	           val u_del_wt = each.getProperty("weight").toString().toInt - each.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getProperty("out_weight").toString().toInt
	           c_author_node.setProperty("weight",au_wt - u_del_wt)
		       user_weight_index.remove(c_author_node)
			   user_weight_index += (c_author_node,"weight",new ValueContext(au_wt - u_del_wt).indexNumeric())
	           for(e <- rels)
	           {
	             e.delete()
	           }
	           
	         }
	         else
	         {
		         
		         val parents = each.getRelationships("Comment_To_Comment",Direction.OUTGOING).asScala.map(_.getOtherNode(each))
		         for(e <- parents)
		         {
		           val e_wt = e.getProperty("weight").toString().toInt
		           e.setProperty("weight",e_wt - 1)
		         }
		         
		         val u_wt = c_author_node.getProperty("weight").toString().toInt
		         val u_del_wt = each.getProperty("weight").toString().toInt - (each.getRelationships("Comment_To_Comment",Direction.INCOMING).asScala.size)
		         c_author_node.setProperty("weight",u_wt - u_del_wt)
		         user_weight_index.remove(c_author_node)
			     user_weight_index += (c_author_node,"weight",new ValueContext(u_wt - u_del_wt).indexNumeric())
		         i_wt = i_wt - 1
		         h_wt = h_wt + 1
		         au_wt = au_wt - 1
		         
		         for(e <- rels)
		         {
		           e.delete()
		         }
		         
		       }
	           
	           CommentIndex.remove(each)
	           each.delete()
	           }
	           item_node.setProperty("weight",i_wt)
		       
		       author_node.setProperty("weight",au_wt)
	         
	           if(comment_node.hasRelationship("Comment_To_Article",Direction.OUTGOING))
		       {
		          article_weight_index.remove(item_node)
		          article_weight_index += (item_node,"weight",new ValueContext( i_wt ).indexNumeric())
		       }
	           else if(comment_node.hasRelationship("Comment_To_Event",Direction.OUTGOING))
		       {
		          event_weight_index.remove(item_node)
		          event_weight_index += (item_node,"weight",new ValueContext( i_wt ).indexNumeric())
		          
		       }
	           else
		       {
		          p_weight_index.remove(item_node)
		          p_weight_index += (item_node,"weight",new ValueContext( i_wt ).indexNumeric())
		          
		       }
	           user_weight_index.remove(author_node)
			   user_weight_index += (author_node,"weight",new ValueContext( au_wt ).indexNumeric())
		       for(each <- hash_nodes)
		       {
		           var wt = each.getProperty("weight").toString().toInt
		           each.setProperty("weight",wt-h_wt)
			       hash_weight_index.remove(each)
			       hash_weight_index += (each,"weight",new ValueContext( wt-h_wt ).indexNumeric())
			       
		       }
	           ret = true
       }
       ret
       }
       
  }
  
  // This function is called inside the delete_comment method to get the list of childs of the parent comment to be deleted
  def deleteEntry(com_node:org.neo4j.graphdb.Node,com_list: ListBuffer[org.neo4j.graphdb.Node]):ListBuffer[org.neo4j.graphdb.Node]=
  {
       
	        if(com_node != null)
	        {
		        val list = com_node.getRelationships("Comment_To_Comment",Direction.INCOMING).asScala.map(_.getOtherNode(com_node)).toList
		        
		        com_list += com_node
		        
		        if (list != null) {
		            
		            for(each <- list) {
		                
		                deleteEntry(each, com_list);
		            }
		        }
	        }
	    
	    com_list
  }
  
  // Triggered when a comment is marked as spam by the user
    def comment_spam(
        id:String, // unique comment id
        user_name:String,  // unique user name
        time:Int
        ):Boolean =
    {
    
    
     withTx {
      
     implicit neo =>
       val CommentIndex = getNodeIndex("comment").get
       val comment_node = CommentIndex.get("id",id).getSingle()
       val UserIndex = getNodeIndex("user").get
       val user_node = UserIndex.get("id",user_name).getSingle()
       var ret = false
       if(comment_node != null && user_node != null && !comment_node.getRelationships("Spammed_Comment").asScala.map(_.getOtherNode(comment_node)).toList.contains(user_node))
	   {
         
           user_node.setProperty("last_seen",time)
	       var s_wt = comment_node.getProperty("spam_weight").toString().toInt
	       s_wt = s_wt + 1
	       if(s_wt > 5)
	       {
	         delete_comment(id)
	       }
	       else
	       {
	         comment_node.setProperty("spam_weight",s_wt)
	         val rel: org.neo4j.graphdb.Relationship = user_node --> "Spammed_Comment" --> comment_node <
	         val rel_time = rel.setProperty("time", time)
	       }
	       ret = true
	   }
       ret
     }
    }
    
    
    // This function returns the list of comments sorted according to popularity
    def get_all_comments(
        user_name: String,
        c_itemtype: String,  // Article/Event
        c_itemid: String // article id/ event id
        ):String=
    {
      
    
      
        def matchTest(x: String): String = x match {
	    case "A" => "Article"
	    case "E" => "Event"
	    case "Q" => "Quickpost"
	    case "C" => "Comment"
	    case "U" => "User"
	    case "H" => "Sub_category"
	    case "P" => "Petition"
	    case "a" => "Article"
	    case "e" => "Event"
	    case "q" => "Quickpost"
	    case "c" => "Comment"
	    case "u" => "User"
	    case "h" => "Sub_category"
	    case "p" => "Petition"
	    }
        val i_type = matchTest(c_itemtype)
        
        
        
        val user_index = getNodeIndex("user").get
        val user_node = user_index.get("id",user_name).getSingle()
        val ParentIndex = getNodeIndex(i_type.toLowerCase()).get
        val parent_node = ParentIndex.get("id",c_itemid).getSingle()
        var list = List[Any]()
        var sorted_coms  = List[org.neo4j.graphdb.Node]()
        val cur_time = (System.currentTimeMillis() /1000).toInt
        val l1 = List("id","con","auth","u_no","d_no","time","up","down","react")
	    
        if(!user_name.equals("") )
        {
          if(parent_node != null && user_node != null)
          {
	        if(i_type.equals("Article"))
	        {
	          val com_nodes = parent_node.getRelationships("Comment_To_Article").asScala.map(_.getOtherNode(parent_node)).toList
		      sorted_coms = com_nodes.sortBy( x =>   -(((x.getProperty("weight").toString().toInt + (((cur_time-x.getProperty("time_created").toString().toInt)/86400)+1)))/(((cur_time-x.getProperty("time_created").toString().toInt)/86400)+1))  )
	          list = sorted_coms.map(x => JSONObject(l1.zip(List(x.getProperty("comment_id"),x.getProperty("comment_content"),x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("user_name") + "::" +x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("first_name")+" " +x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("last_name"),x.getRelationships("comment_voteup").asScala.size,x.getRelationships("comment_votedown").asScala.size,x.getProperty("time_created"),x.getRelationships("comment_voteup").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getRelationships("comment_votedown").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getRelationships("Comment_To_Comment",Direction.INCOMING).asScala.size)).toMap))
          
	        }
	        else if(i_type.equals("Event"))
	        {
	          val com_nodes = parent_node.getRelationships("Comment_To_Event").asScala.map(_.getOtherNode(parent_node)).toList
		      sorted_coms = com_nodes.sortBy( x =>   -(((x.getProperty("weight").toString().toInt + (((cur_time-x.getProperty("time_created").toString().toInt)/86400)+1)))/(((cur_time-x.getProperty("time_created").toString().toInt)/86400)+1))  )
	          list = sorted_coms.map(x => JSONObject(l1.zip(List(x.getProperty("comment_id"),x.getProperty("comment_content"),x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("user_name") + "::" +x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("first_name")+" " +x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("last_name"),x.getRelationships("comment_voteup").asScala.size,x.getRelationships("comment_votedown").asScala.size,x.getProperty("time_created"),x.getRelationships("comment_voteup").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getRelationships("comment_votedown").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getRelationships("Comment_To_Comment",Direction.INCOMING).asScala.size)).toMap))
          
	        }
	        else
	        {
	          val com_nodes = parent_node.getRelationships("Comment_To_Petition").asScala.map(_.getOtherNode(parent_node)).toList
		      sorted_coms = com_nodes.sortBy( x =>   -(((x.getProperty("weight").toString().toInt + (((cur_time-x.getProperty("time_created").toString().toInt)/86400)+1)))/(((cur_time-x.getProperty("time_created").toString().toInt)/86400)+1))  )
	          list = sorted_coms.map(x => JSONObject(l1.zip(List(x.getProperty("comment_id"),x.getProperty("comment_content"),x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("user_name") + "::" +x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("first_name")+" " +x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("last_name"),x.getRelationships("comment_voteup").asScala.size,x.getRelationships("comment_votedown").asScala.size,x.getProperty("time_created"),x.getRelationships("comment_voteup").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getRelationships("comment_votedown").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getRelationships("Comment_To_Comment",Direction.INCOMING).asScala.size)).toMap))
          
	        }
          }
        }
        
        else if((user_name.equals("") || user_node == null) && parent_node != null)
        {
            if(i_type.equals("Article"))
	        {
	          val com_nodes = parent_node.getRelationships("Comment_To_Article").asScala.map(_.getOtherNode(parent_node)).toList
		      sorted_coms = com_nodes.sortBy( x =>   -(((x.getProperty("weight").toString().toInt + (((cur_time-x.getProperty("time_created").toString().toInt)/86400)+1)))/(((cur_time-x.getProperty("time_created").toString().toInt)/86400)+1))  )
	          list = sorted_coms.map(x => JSONObject(l1.zip(List(x.getProperty("comment_id"),x.getProperty("comment_content"),x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("user_name") + "::" +x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("first_name")+" " +x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("last_name"),x.getRelationships("comment_voteup").asScala.size,x.getRelationships("comment_votedown").asScala.size,x.getProperty("time_created"),false,false,x.getRelationships("Comment_To_Comment",Direction.INCOMING).asScala.size)).toMap))
          
	        }
	        else if(i_type.equals("Event"))
	        {
	          val com_nodes = parent_node.getRelationships("Comment_To_Event").asScala.map(_.getOtherNode(parent_node)).toList
		      sorted_coms = com_nodes.sortBy( x =>   -(((x.getProperty("weight").toString().toInt + (((cur_time-x.getProperty("time_created").toString().toInt)/86400)+1)))/(((cur_time-x.getProperty("time_created").toString().toInt)/86400)+1))   )
	          list = sorted_coms.map(x => JSONObject(l1.zip(List(x.getProperty("comment_id"),x.getProperty("comment_content"),x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("user_name") + "::" +x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("first_name")+" " +x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("last_name"),x.getRelationships("comment_voteup").asScala.size,x.getRelationships("comment_votedown").asScala.size,x.getProperty("time_created"),false,false,x.getRelationships("Comment_To_Comment",Direction.INCOMING).asScala.size)).toMap))
          
	        }
            else
	        {
	          val com_nodes = parent_node.getRelationships("Comment_To_Petition").asScala.map(_.getOtherNode(parent_node)).toList
		      sorted_coms = com_nodes.sortBy( x =>   -(((x.getProperty("weight").toString().toInt + (((cur_time-x.getProperty("time_created").toString().toInt)/86400)+1)))/(((cur_time-x.getProperty("time_created").toString().toInt)/86400)+1))   )
	          list = sorted_coms.map(x => JSONObject(l1.zip(List(x.getProperty("comment_id"),x.getProperty("comment_content"),x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("user_name") + "::" +x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("first_name")+" " +x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("last_name"),x.getRelationships("comment_voteup").asScala.size,x.getRelationships("comment_votedown").asScala.size,x.getProperty("time_created"),false,false,x.getRelationships("Comment_To_Comment",Direction.INCOMING).asScala.size)).toMap))
          
	        }
        }
        
        
      JSONArray(list).toString() 
     
    }
    
// This function returns the list of comments related to a given comment
    def get_comments(
        user_name: String,
        c_id: String // comment_id
        ):String=
    {
      
     
        val user_index = getNodeIndex("user").get
        val user_node = user_index.get("id",user_name).getSingle()
        val comment_index = getNodeIndex("comment").get
        val comment_node = comment_index.get("id",c_id).getSingle()
        var list = List[Any]()
        var sorted_coms  = List[org.neo4j.graphdb.Node]()
        val cur_time = (System.currentTimeMillis() /1000).toInt
        val l1 = List("id","con","auth","u_no","d_no","time","up","down","react")
	        
        if(!user_name.equals(""))
        {
          if(comment_node != null && user_node != null)
          {
            sorted_coms ::= comment_node
            var c_node = get_com(comment_node)
            while(c_node != null)
            {
              
              sorted_coms ::= c_node
              c_node = get_com(c_node)
            }
            
            sorted_coms = sorted_coms ::: comment_node.getRelationships("Comment_To_Comment",Direction.INCOMING).asScala.map(_.getOtherNode(comment_node)).toList
            list = sorted_coms.map(x => JSONObject(l1.zip(List(x.getProperty("comment_id"),x.getProperty("comment_content"),x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("user_name") + "::" +x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("first_name")+" " +x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("last_name"),x.getRelationships("comment_voteup").asScala.size,x.getRelationships("comment_votedown").asScala.size,x.getProperty("time_created"),x.getRelationships("comment_voteup").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getRelationships("comment_votedown").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getRelationships("Comment_To_Comment",Direction.INCOMING).asScala.size)).toMap))
          }
        }
        
        
        
        else if((user_name.equals("") || user_node == null) && comment_node != null)
        {
          
            sorted_coms ::= comment_node
            var c_node = get_com(comment_node)
            while(c_node != null)
            {
              sorted_coms ::= c_node
              c_node = get_com(c_node)
            }
            
            sorted_coms = sorted_coms ::: comment_node.getRelationships("Comment_To_Comment",Direction.INCOMING).asScala.map(_.getOtherNode(comment_node)).toList
            
	        list = sorted_coms.map(x => JSONObject(l1.zip(List(x.getProperty("comment_id"),x.getProperty("comment_content"),x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("user_name") + "::" +x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("first_name")+" " +x.getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("last_name"),x.getRelationships("comment_voteup").asScala.size,x.getRelationships("comment_votedown").asScala.size,x.getProperty("time_created"),false,false,x.getRelationships("Comment_To_Comment",Direction.INCOMING).asScala.size)).toMap))
          
          
        }
        
      def get_com(node : org.neo4j.graphdb.Node):org.neo4j.graphdb.Node=
      {
          var x = node
          if(node.getSingleRelationship("Comment_To_Comment",Direction.OUTGOING) != null)
          {
            x = node.getSingleRelationship("Comment_To_Comment",Direction.OUTGOING).getOtherNode(node)
          }
          else
          {
            x = null
          }
          
      x 
      }
        
      JSONArray(list).toString() 
     
    
    }
  

}