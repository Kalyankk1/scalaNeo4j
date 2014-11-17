package Saddahaq

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
import java.util.Calendar
import java.util.Date
//import org.neo4j.graphdb.index.IndexHits
//import scala.collection.immutable.ListMap
//import org.neo4j.index.lucene.QueryContext
import org.neo4j.index.lucene.ValueContext
import scala.util.parsing.json.JSONObject
import edu.stanford.nlp.tagger.maxent.MaxentTagger
import scala.util.parsing.json.JSON


case class event(
    val event_id: String,
    val event_title_id: String,
    val event_title: String,
    val event_content: String,
    val event_summary: String,
    val event_featured_img: String,
    val event_date_time: Int,
    val event_date_time_closing: Int,
    val event_limit: Int,
    val event_location: String,
    val time_created: Int,
    val weight: Int,
    val spam_weight: Int,
    val views: Int,
    val latest_views: String,
    val approved: Int,
    val head: Int,
    val space: Int
    )
    
trait Event_node extends Neo4jWrapper with SingletonEmbeddedGraphDatabaseServiceProvider with TypedTraverser with Neo4jIndexProvider{

  
// Triggered when a new event is created
def create_event(
    user_name: String, // unique user name( author user name)
    e_id: String,  // unique event id
    e_title_id: String,  // unique event title id
    e_title: String,
    e_content: String,
    e_date_time: Int,  // Event date and time ( 10 digit int)
    e_date_time_closing: Int,  // Event closing date and time ( 10 digit int) ( same as starting date if not given)
    e_limit: Int,  // (0 if not given)
    e_location: String, // event locatiion
    e_cat: String, // , separated categories
    e_subcat: String, // , separated hashtags if any("" if none)
    e_hashtags: String, // , separated referred hashtags if any("" if none)
    e_users: String, // , separated referred users if any("" if none)
    e_time_created: Int,
    a_id: String, 
    e_summary: String,
    is_edit: Int,
    e_fut_image: String,
    is_closed: Int
    ):Boolean=
  {
    
    
    
    withTx {
      
    implicit neo =>
     var ret = false
     
     if(is_edit == 0)
     {
       
         var is_space = is_closed
	     val UserIndex = getNodeIndex("user").get
	     val TilesIndex = getNodeIndex("tiles").get
	     val user_node = UserIndex.get("id",user_name).getSingle()
	     val event_index = getNodeIndex("event").get
	     val event_content_index = getNodeIndex("event_content").get
	     val event_title_index = getNodeIndex("event_title").get
	     val event_weight_index = getNodeIndex("event_weight").get
	     val user_weight_index = getNodeIndex("user_weight").get
	     val hash_weight_index = getNodeIndex("hash_weight").get
	     val avg_wt_index = getNodeIndex("avg_weights").get
	     val evt_node = event_index.get("id",e_id).getSingle()
	     val ArticleIndex = getNodeIndex("article").get
	     val art_node = ArticleIndex.get("id",a_id).getSingle()
	     
	     if(evt_node == null && user_node != null  && !e_cat.equals("") && !e_subcat.equals(""))
		 {
	         user_node.setProperty("last_seen",e_time_created)
	         var u_wt = user_node.getProperty("weight").toString().toInt
		     val e_wt = Math.round(u_wt.asInstanceOf[Float]/200)
		     var closing = e_date_time_closing
		     if(e_date_time_closing == 0)
		     {
		        closing = e_date_time
		     }
		     val event_node = createNode(event(e_id,e_title_id,e_title,"",e_summary,e_fut_image,e_date_time,closing,e_limit,e_location,e_time_created,e_wt,0,1,"",0,0,is_space))
		    
		     
		     val hash_list = e_subcat.split(",").toList.map( x => x.toLowerCase())
		     event_index += (event_node,"id",e_id)
		     val topic_words_list = (e_title+" "+e_content+" "+e_location+" "+hash_list.mkString(" ")).toLowerCase().split(" ").toList
		     val txt = gummy(e_title+" "+e_content)+" "+e_location+" "+hash_list.mkString(" ")
		     event_title_index += (event_node,"title_id",e_title_id)
		     event_weight_index += (event_node,"weight",new ValueContext( e_wt ).indexNumeric())
		     event_content_index += (event_node,"time",new ValueContext( e_date_time ).indexNumeric())
		     user_node.setProperty("weight",u_wt+5)
		     user_weight_index.remove(user_node)
		     user_weight_index += (user_node,"weight",new ValueContext( u_wt+5).indexNumeric())
		     var rel: org.neo4j.graphdb.Relationship = event_node --> "Event_Created_By" --> user_node <
		     var rel_time = rel.setProperty("time", e_time_created)
		     var rel_in_wt = rel.setProperty("in_weight", 5)
		     var rel_out_wt = rel.setProperty("out_weight", e_wt)
		     
		     
		     if(!a_id.equals("") && art_node != null)
		     {
		       rel = event_node --> "Event_App_Of" --> art_node <
		       var rel_time = rel.setProperty("time", e_time_created)
		     }
		     
		     
		     val CategoryIndex = getNodeIndex("category").get
		     
		     var cats = e_cat.split(",").toList.map( x => x.toLowerCase()) 
		     cats ::= "all"
		     for(each <- cats)
		     {
		       //event_content_index += (event_node,"event_cat",each)
		       if(CategoryIndex.get("name",each).getSingle() != null)  
		       {
		         val category_node = CategoryIndex.get("name",each).getSingle()
		         rel = event_node --> "Belongs_To_Event_Category" --> category_node <
		         var rel_time = rel.setProperty("time", e_time_created)
		         val topic_nodes = category_node.getRelationships("Topic_Of_Category").asScala.toList.map(_.getStartNode())
		         if(topic_nodes != null)
		         {
		           for(top <- topic_nodes)
		           {
		             val keywords = top.getProperty("sub_topics").toString().split(",").toList
		             val inter = topic_words_list.intersect(keywords).size
		             if(inter > 0)
		             {
		               rel = event_node --> "Belongs_To_Event_Topic" --> top <
		               var rel_time = rel.setProperty("time", e_time_created)
		               var count = rel.setProperty("count", inter)
		             }
		             
		           }
		         }
		       }
		       else
		       {
		         val category_node = createNode(category(each,"",""))
		         CategoryIndex += (category_node,"name",each)
		         rel = event_node --> "Belongs_To_Event_Category" --> category_node <
		         var rel_time = rel.setProperty("time", e_time_created)
		       }
		     }
		             if(is_space == 0)
		             {
			             for(each <- cats)
					     {
					       var news_node = TilesIndex.get("id",each).getSingle()
					       if(news_node == null)
					       {
					         val tiles_node =  createNode(tiles(each,e_id))
					         TilesIndex += (tiles_node,"id",each)
					       }
					       else
					       {
					         
						     var sorted_news  = List[org.neo4j.graphdb.Node]()
						     val news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     news_list ::= e_id
						     val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				             news_node.setProperty("value",news_list_trim)
					      
						    
					       }
					     }
		             }
		     
		     
		        var tot_tags = List[ org.neo4j.graphdb.Node]()
		        val SubcatIndex = getNodeIndex("sub_category").get
	            if(e_subcat != "")
	            {
	              val hash_tags = e_subcat.split(",").toList.filterNot( x => x.equals("")).map( x => x.toLowerCase())
	              val main_hash = hash_tags(0)
	              for(each <- hash_tags)
	              {
		              if(SubcatIndex.get("name",each).getSingle() != null)
		              {
		                val subcat_node = SubcatIndex.get("name",each).getSingle()
		                tot_tags ::= subcat_node
		                val h_wt = subcat_node.getProperty("weight").toString().toInt
		                subcat_node.setProperty("weight",(h_wt + e_wt))
		                if(each.equals(main_hash))
		                {
			                rel = event_node --> "Belongs_To_Subcategory_Event" --> subcat_node <
			                var rel_main = rel.setProperty("main", 1)
			                var rel_time = rel.setProperty("time", e_time_created)
			                var rel_in_wt = rel.setProperty("in_weight", e_wt)
			                var rel_out_wt = rel.setProperty("out_weight", 0)
		                }
		                else
		                {
			                rel = event_node --> "Belongs_To_Subcategory_Event" --> subcat_node <
			                var rel_time = rel.setProperty("time", e_time_created)
			                var rel_in_wt = rel.setProperty("in_weight", e_wt)
			                var rel_out_wt = rel.setProperty("out_weight", 0)
		                }
		                hash_weight_index.remove(subcat_node)
		                hash_weight_index += (subcat_node,"weight",new ValueContext( h_wt + e_wt ).indexNumeric())
		              }
		              else
		              {
		                val subcat_node = createNode(sub_category(each,e_time_created,e_wt))
		                SubcatIndex += (subcat_node,"name",each)
		                if(each.equals(main_hash))
		                {
			                rel = event_node --> "Belongs_To_Subcategory_Event" --> subcat_node <
			                var rel_main = rel.setProperty("main", 1)
			                var rel_time = rel.setProperty("time", e_time_created)
			                var rel_in_wt = rel.setProperty("in_weight", e_wt)
			                var rel_out_wt = rel.setProperty("out_weight", 0)
		                }
		                else
		                {
			                rel = event_node --> "Belongs_To_Subcategory_Event" --> subcat_node <
			                var rel_time = rel.setProperty("time", e_time_created)
			                var rel_in_wt = rel.setProperty("in_weight", e_wt)
			                var rel_out_wt = rel.setProperty("out_weight", 0)
		                }
		                
		                rel = subcat_node --> "Hashtag_Created_By" --> user_node <
		                var rel_time1 = rel.setProperty("time", e_time_created)
		                
		                hash_weight_index += (subcat_node,"weight",new ValueContext( e_wt).indexNumeric())
		                tot_tags ::= subcat_node
		              }
	              }
	            }
		        
		        
		          
		          if(e_hashtags != "")
		          {
		            val e_tags = e_hashtags.split(",").distinct.toList.filterNot( x => x.equals("")).map( x => x.toLowerCase())
		            for(tag_name <- e_tags)
		            {
		              
		              if(tag_name != "")
		              {
		                
		                if(SubcatIndex.get("name",tag_name).getSingle() != null)
		                {
		                  val tag_node = SubcatIndex.get("name",tag_name).getSingle()
		                  tot_tags ::= tag_node
		                  val h_wt = tag_node.getProperty("weight").toString().toInt
		                  tag_node.setProperty("weight",(h_wt + 10))
		                  rel = tag_node --> "Tag_Of_Event" --> event_node <
		                  var rel_time = rel.setProperty("time", e_time_created)
		                  var rel_in_wt = rel.setProperty("in_weight", 0)
		                  var rel_out_wt = rel.setProperty("out_weight", 10)
		                  hash_weight_index.remove(tag_node)
		                  hash_weight_index += (tag_node,"weight",new ValueContext( h_wt + 10 ).indexNumeric())
		                  
		                }
		                else
		                {
		                  val tag_node = createNode(sub_category(tag_name,e_time_created,10))
		                  SubcatIndex += (tag_node,"name",tag_name)
		                  rel = tag_node --> "Tag_Of_Event" --> event_node <
		                  var rel_time = rel.setProperty("time", e_time_created)
		                  var rel_in_wt = rel.setProperty("in_weight", 0)
		                  var rel_out_wt = rel.setProperty("out_weight", 10)
		                  hash_weight_index += (tag_node,"weight",new ValueContext( 10 ).indexNumeric())
		                  rel = tag_node --> "Hashtag_Created_By" --> user_node <
		                  var rel_time1 = rel.setProperty("time", e_time_created)
		                  tot_tags ::= tag_node
		                  
		                }
		              }
		            }
		          
			      }
		          
		          if(!e_users.equals(""))
		          {
		            val e_users_list = e_users.split(",").distinct
		            for(each <- e_users_list)
		            {
		              if(!each.equals(""))
		              {
		              
			              val user_node1 = UserIndex.get("id",each).getSingle()
			              if(user_node1 != null)
			              {
			                val rel1: org.neo4j.graphdb.Relationship = user_node1 --> "User_Of_Event"  --> event_node <
			                val y = rel1.setProperty("time", e_time_created)
			              }
		              }
		          
		            }
		          
			      }
		          
		          var flag_list = List[org.neo4j.graphdb.Node]()
		          tot_tags = tot_tags.distinct
		          for(each <- tot_tags)
		          {
		              flag_list ::= each
		              val fil = tot_tags.filterNot( x => flag_list.contains(x))
				      for(e <- fil)
				      {
				        if(each.getRelationships("Tag_To_Tag").asScala.toList.map(_.getOtherNode(each)).contains(e))
				        {
				          for( rel <- each.getRelationships("Tag_To_Tag").asScala.toList )
				          {
				            if(rel.getOtherNode(each).equals(e))
				            {
				              val count = rel.getProperty("count").toString().toInt
				              rel.setProperty("count",count + 1)
				            }
				          }
				        }
				        
				        else
				        {
				          val rel: org.neo4j.graphdb.Relationship = each --> "Tag_To_Tag" --> e <
				          val rel_count = rel.setProperty("count", 1)
				        }
				      }
		            
		          }
		          
		          
		          val location_index = getNodeIndex("location").get
		          val states = location_index.query( "id", "*" ).iterator().asScala.toList.map(x => x.getProperty("location_id").toString())
	              val locations = e_location.split(", ").toList.distinct.map(x => x.toLowerCase()).intersect(states)
	              for(each <- locations)
	              {
	                val loc_node = location_index.get("id",each).getSingle()
	                event_node --> "Belongs_To_Location_Event" --> loc_node 
	              }
	              val tagger = new MaxentTagger(
	                "/var/n4j/data/left3words-wsj-0-18.tagger")
	              val tagged = tagger.tagString(e_content)
	              //val prev_nouns = user_node.getProperty("nouns").toString()
	              val noun_list = tagged.split(" ").toList.map(x => x.split("/")).filter( x => (x.size == 2 && x(1).equals("NNP"))).map(x => x(0).toLowerCase())
	              var noun_hash_list = noun_list:::hash_list
	              event_content_index += (event_node,"event_content", (noun_hash_list.mkString(" ") + " " + gummy(e_title))  )
	              val proper_nouns = List("2013","2014","india","indian","reddy","rao","singh","world","pti","sunday","monday","tuesday","wednesday","thursday","friday","saturday","january","february","march","april","may","june","july","august","september","october","november","december")
	              noun_hash_list = noun_hash_list.filterNot(x => proper_nouns.contains(x) && x.length() < 3)
	              event_node.setProperty("event_content",noun_hash_list.mkString(" "))
	              
	
		          ret = true
	     }
     }
     
     else
     {
       val EventIndex = getNodeIndex("event").get
       val UserIndex = getNodeIndex("user").get
       val SubcatIndex = getNodeIndex("sub_category").get
       val TilesIndex = getNodeIndex("tiles").get
       val HeadlinesIndex = getNodeIndex("headlines").get
       val FeaturedTilesIndex = getNodeIndex("featured_tiles").get
	      
       val TopicIndex = getNodeIndex("topic").get
       val event_content_index = getNodeIndex("event_content").get
       val event_title_index = getNodeIndex("event_title").get
       val hash_weight_index = getNodeIndex("hash_weight").get
       val event_node = EventIndex.get("id",e_id).getSingle()
       
       val user_node = UserIndex.get("id",user_name).getSingle()
       //var ret = false
       var closing = e_date_time_closing
	   if(e_date_time_closing == 0)
	   {
	      closing = e_date_time
	   }
       if(user_node != null && event_node != null  && !e_cat.equals("") && !e_subcat.equals(""))
	   {
         
           val feu = event_node.getProperty("approved").toString().toInt
           val head = event_node.getProperty("head").toString().toInt
           val space = event_node.getProperty("space").toString().toInt
           user_node.setProperty("last_seen",e_time_created)
           val time = event_node.getProperty("time_created").toString().toInt
	       val event_wt = event_node.getProperty("weight").toString().toInt
	       event_node.setProperty("event_title",e_title)
	       event_node.setProperty("event_summary",e_summary)
	       event_node.setProperty("event_featured_img",e_fut_image)
	       event_node.setProperty("event_title_id",e_title_id)
	       event_node.setProperty("event_date_time",e_date_time)
	       event_node.setProperty("event_date_time_closing",closing)
	       event_node.setProperty("event_limit",e_limit)
	       event_node.setProperty("event_location",e_location)
	       event_content_index.remove(event_node)
	       event_content_index += (event_node,"time",new ValueContext( e_date_time ).indexNumeric())
	       event_title_index.remove(event_node)
	       event_title_index += (event_node,"title_id",e_title_id)
	       val h_list = e_subcat.split(",").toList.map( x => x.toLowerCase())
	       val topic_words_list = (e_title+" "+e_content+" "+e_location+" "+h_list.mkString(" ")).toLowerCase().split(" ").toList
		   var cats_list = event_node.getRelationships("Belongs_To_Event_Category",Direction.OUTGOING).asScala.toList.map(_.getEndNode().getProperty("name").toString().toLowerCase())
				    
		             for(each <- cats_list)
				     {
				         var news_node = TilesIndex.get("id",each).getSingle()
				         if(news_node != null)
				         {
					         var news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     if(news_list.contains(e_id))
					         {
					           news_list = news_list - e_id
					           val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				               news_node.setProperty("value",news_list_trim)
					         }
				         }
				         
				         news_node = FeaturedTilesIndex.get("id",each).getSingle()
				         if(news_node != null)
				         {
					         var news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     if(news_list.contains(e_id))
					         {
					           news_list = news_list - e_id
					           val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				               news_node.setProperty("value",news_list_trim)
					         }
				         }
				         
				         news_node = HeadlinesIndex.get("id",each).getSingle()
				         if(news_node != null)
				         {
					         var news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     if(news_list.contains(e_id))
					         {
					           news_list = news_list - e_id
					           val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				               news_node.setProperty("value",news_list_trim)
					         }
				         }
					     
				      
					    
				       
				     }  
	       var rels = event_node.getRelationships("Belongs_To_Event_Topic","Belongs_To_Event_Category","Belongs_To_Location_Event","User_Of_Event").asScala
	       for(each <- rels)
	       {
	         each.delete()
	       }
	       
	      var spam_flag = 0
	      if(event_node.hasRelationship("User_Spammed_Of_Event_Category",Direction.OUTGOING))
	      {
	        spam_flag = 1
	        val spam_rels = event_node.getRelationships("User_Spammed_Of_Event_Category",Direction.OUTGOING).asScala
	        for(each <- spam_rels)
	        {
	          each.delete()
	        }
	      }
	       
	       val tags_list = e_hashtags.split(",").distinct.toList.filterNot( x => x.equals("")).map( x => x.toLowerCase())
	       var old_tags = List[Any]()
	       rels = event_node.getRelationships("Tag_Of_Event").asScala
	       for(each <- rels)
	       {
	           val tag = each.getOtherNode(event_node).getProperty("name").toString()
	           old_tags ::= tag
	           if(!tags_list.contains(tag))
	           {
	             val tag_node = each.getOtherNode(event_node)
	             val h_wt = tag_node.getProperty("weight").toString().toInt
	             tag_node.setProperty("weight",(h_wt - 10))
	             hash_weight_index.remove(tag_node)
	             hash_weight_index += (tag_node,"weight",new ValueContext( h_wt - 10 ).indexNumeric())
	             each.delete() 
	           }
	        	 
	       }
	       
	       for(tag_name <- tags_list)
	       {
	         if(!old_tags.contains(tag_name))
	         {
	           if(tag_name != "")
	              {
	                
	                if(SubcatIndex.get("name",tag_name).getSingle() != null)
	                {
	                  val tag_node = SubcatIndex.get("name",tag_name).getSingle()
	                  val h_wt = tag_node.getProperty("weight").toString().toInt
	                  tag_node.setProperty("weight",(h_wt + 10))
	                  var rel = tag_node --> "Tag_Of_Event" --> event_node <
	                  var rel_time = rel.setProperty("time", e_time_created)
	                  var rel_in_wt = rel.setProperty("in_weight", 0)
	                  var rel_out_wt = rel.setProperty("out_weight", 10)
	                  hash_weight_index.remove(tag_node)
	                  hash_weight_index += (tag_node,"weight",new ValueContext( h_wt + 10 ).indexNumeric())
	                  
	                }
	                else
	                {
	                  val tag_node = createNode(sub_category(tag_name,e_time_created,10))
	                  SubcatIndex += (tag_node,"name",tag_name)
	                  var rel = tag_node --> "Tag_Of_Event" --> event_node <
	                  var rel_time = rel.setProperty("time", e_time_created)
	                  var rel_in_wt = rel.setProperty("in_weight", 0)
	                  var rel_out_wt = rel.setProperty("out_weight", 10)
	                  rel = tag_node --> "Hashtag_Created_By" --> user_node <
	                  var rel_time1 = rel.setProperty("time", time)
	                  hash_weight_index += (tag_node,"weight",new ValueContext( 10 ).indexNumeric())
	                  
	                }
	              }
	         }
	       }
	       
	       
	          if(!e_users.equals(""))
	          {
	            val e_users_list = e_users.split(",").distinct
	            for(each <- e_users_list)
	            {
	              if(!each.equals(""))
	              {
	              
		              val user_node1 = UserIndex.get("id",each).getSingle()
		              if(user_node1 != null)
		              {
		                val rel1: org.neo4j.graphdb.Relationship = user_node1 --> "User_Of_Event"  --> event_node <
		                val y = rel1.setProperty("time", e_time_created)
		              }
	              }
	          
	            }
	          
		      }  
	       
	   var hash_list = e_subcat.split(",").toList.filterNot( x => x.equals("")).map( x => x.toLowerCase())
	   var main_hash = hash_list(0)
       var old_hash = List[Any]()
       rels = event_node.getRelationships("Belongs_To_Subcategory_Event").asScala
       var r:org.neo4j.graphdb.Relationship = null
       for(each <- rels)
       {
           val tag = each.getOtherNode(event_node).getProperty("name").toString().toLowerCase()
           if(tag.equals(main_hash))
		   {
		             r = each
		   }
           if(each.hasProperty("main") && !tag.equals(main_hash))
	       {
	          each.removeProperty("main")
	             
	       }
           old_hash ::= tag
           if(!hash_list.contains(tag))
           {
             val tag_node = each.getOtherNode(event_node)
             val h_wt = tag_node.getProperty("weight").toString().toInt
             tag_node.setProperty("weight",(h_wt - event_wt))
             hash_weight_index.remove(tag_node)
             hash_weight_index += (tag_node,"weight",new ValueContext( h_wt - event_wt ).indexNumeric())
             each.delete() 
           }
         
        	 
       }
       
       for(tag_name <- hash_list)
       {
         if(!old_hash.contains(tag_name))
         {
           if(tag_name != "")
            {
              if(SubcatIndex.get("name",tag_name).getSingle() != null)
              {
                val subcat_node = SubcatIndex.get("name",tag_name).getSingle()
                val h_wt = subcat_node.getProperty("weight").toString().toInt
                subcat_node.setProperty("weight",(h_wt + event_wt))
                if(tag_name.equals(main_hash))
                {
	                var rel = event_node --> "Belongs_To_Subcategory_Event" --> subcat_node <
	                var rel_main = rel.setProperty("main", 1)
	                var rel_time = rel.setProperty("time", e_time_created)
	                var rel_in_wt = rel.setProperty("in_weight", event_wt)
	                var rel_out_wt = rel.setProperty("out_weight", 0)
                }
                else
                {
	                var rel = event_node --> "Belongs_To_Subcategory_Event" --> subcat_node <
	                var rel_main = rel.setProperty("main", 1)
	                var rel_time = rel.setProperty("time", e_time_created)
	                var rel_in_wt = rel.setProperty("in_weight", event_wt)
	                var rel_out_wt = rel.setProperty("out_weight", 0)
                }
                hash_weight_index.remove(subcat_node)
                hash_weight_index += (subcat_node,"weight",new ValueContext( h_wt + event_wt ).indexNumeric())
              }
              else
              {
                val subcat_node = createNode(sub_category(tag_name,e_time_created,event_wt))
                SubcatIndex += (subcat_node,"name",tag_name)
                if(tag_name.equals(main_hash))
                {
	                var rel = event_node --> "Belongs_To_Subcategory_Event" --> subcat_node <
	                var rel_main = rel.setProperty("main", 1)
	                var rel_time = rel.setProperty("time", e_time_created)
	                var rel_in_wt = rel.setProperty("in_weight", event_wt)
	                var rel_out_wt = rel.setProperty("out_weight", 0)
                }
                else
                {
	                var rel = event_node --> "Belongs_To_Subcategory_Event" --> subcat_node <
	                var rel_main = rel.setProperty("main", 1)
	                var rel_time = rel.setProperty("time", e_time_created)
	                var rel_in_wt = rel.setProperty("in_weight", event_wt)
	                var rel_out_wt = rel.setProperty("out_weight", 0)
                }
                
                var rel = subcat_node --> "Hashtag_Created_By" --> user_node <
                var rel_time1 = rel.setProperty("time", time)
                
                hash_weight_index += (subcat_node,"weight",new ValueContext(event_wt).indexNumeric())
              }
            }
         }
         else if(old_hash.contains(tag_name) && tag_name.equals(main_hash))
		 {
		           r.setProperty("main", 1)
		 }
       }
       
       	   
	       
	     val CategoryIndex = getNodeIndex("category").get
	     
	     val cur_time = (System.currentTimeMillis() /1000).toInt
	     var cats = e_cat.split(",").toList.map( x => x.toLowerCase()) 
	     cats ::= "all"
	     for(each <- cats)
	     {
	                   
	    	 		   var news_node = TilesIndex.get("id",each).getSingle()
	    	 		   if(space == 0)
		               {
					       if(news_node == null)
					       {
					         val tiles_node =  createNode(tiles(each,e_id))
					         TilesIndex += (tiles_node,"id",each)
					       }
					       else
					       {
					         
						     var sorted_news  = List[org.neo4j.graphdb.Node]()
						     val news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     if(closing > cur_time)
						     {
						    	 news_list ::= e_id
						     }
						     else
						     {
						       news_list :+= e_id
						     }
						     val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				             news_node.setProperty("value",news_list_trim)
					       }
		               }
	    	 		   
	    	 		   if(feu == 1)
		    	 	   {
			    	 	   news_node = FeaturedTilesIndex.get("id",each).getSingle()
					       if(news_node == null)
					       {
					         val tiles_node =  createNode(featured_tiles(each,e_id))
					         FeaturedTilesIndex += (tiles_node,"id",each)
					       }
					       else
					       {
					         
						     var sorted_news  = List[org.neo4j.graphdb.Node]()
						     val news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     if(closing > cur_time)
						     {
						    	 news_list ::= e_id
						     }
						     else
						     {
						       news_list :+= e_id
						     }
						     val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				             news_node.setProperty("value",news_list_trim)
					      
						    
					       }
		    	 	   }
	    	 		   
	    	 		   if(head == 1)
		    	 	   {
			    	 	   news_node = HeadlinesIndex.get("id",each).getSingle()
					       if(news_node == null)
					       {
					         val tiles_node =  createNode(headlines(each,e_id))
					         HeadlinesIndex += (tiles_node,"id",each)
					       }
					       else
					       {
					         
						     var sorted_news  = List[org.neo4j.graphdb.Node]()
						     val news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     if(closing > cur_time)
						     {
						    	 news_list ::= e_id
						     }
						     else
						     {
						       news_list :+= e_id
						     }
						     val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				             news_node.setProperty("value",news_list_trim)
					      
						    
					       }
		    	 	   }
	    	 		   
	       if(CategoryIndex.get("name",each).getSingle() != null)  
	       {
	         
		         val category_node = CategoryIndex.get("name",each).getSingle()
		         var rel = event_node --> "Belongs_To_Event_Category" --> category_node <
		         var rel_time = rel.setProperty("time", time)
		         val topic_nodes = category_node.getRelationships("Topic_Of_Category").asScala.toList.map(_.getStartNode())
		         if(topic_nodes != null)
		         {
		           for(top <- topic_nodes)
		           {
		             val keywords = top.getProperty("sub_topics").toString().split(",").toList
		             val inter = topic_words_list.intersect(keywords).size
		             if(inter > 0)
		             {
		               rel = event_node --> "Belongs_To_Event_Topic" --> top <
		               var rel_time = rel.setProperty("time", time)
		               var count = rel.setProperty("count", inter)
		             }
		             
		           }
		         }
		       
	         if(spam_flag == 1)
	         {
	           rel = event_node --> "User_Spammed_Of_Event_Category" --> category_node <
	           var rel_time = rel.setProperty("time", time)
	         }
	       }
	       else
	       {
	         val category_node = createNode(category(each,"",""))
	         CategoryIndex += (category_node,"name",each)
	         var rel = event_node --> "Belongs_To_Event_Category" --> category_node <
	         var rel_time = rel.setProperty("time", time)
	         if(spam_flag == 1)
	         {
	           rel = event_node --> "User_Spammed_Of_Event_Category" --> category_node <
	           var rel_time = rel.setProperty("time", time)
	         }
	         
	       }
	     }
	     
	          val location_index = getNodeIndex("location").get
	          val states = location_index.query( "id", "*" ).iterator().asScala.toList.map(x => x.getProperty("location_id").toString())
              val locations = e_location.split(", ").toList.distinct.map(x => x.toLowerCase()).intersect(states)
              for(each <- locations)
              {
                val loc_node = location_index.get("id",each).getSingle()
                event_node --> "Belongs_To_Location_Event" --> loc_node 
              }
	          
	          val tagger = new MaxentTagger(
                "/var/n4j/data/left3words-wsj-0-18.tagger")
              val tagged = tagger.tagString(e_content)
              val noun_list = tagged.split(" ").toList.map(x => x.split("/")).filter( x => (x.size == 2 && x(1).equals("NNP"))).map(x => x(0).toLowerCase())
              var noun_hash_list = noun_list:::hash_list
              event_content_index += (event_node,"event_content",(noun_hash_list.mkString(" ") + " " + gummy(e_title)) )
              val proper_nouns = List("2013","2014","india","indian","reddy","rao","singh","world","pti","sunday","monday","tuesday","wednesday","thursday","friday","saturday","january","february","march","april","may","june","july","august","september","october","november","december")
              noun_hash_list = noun_hash_list.filterNot(x => proper_nouns.contains(x) && x.length() < 3)
              event_node.setProperty("event_content",noun_hash_list.mkString(" "))

	          ret = true
       }
     }
    ret
    }
     
     
    
  }

// Triggered when a event is edited
def edit_event(user_name: String,
    e_id: String,
    e_title_id: String,
    e_title: String,
    e_content: String,
    e_date_time: Int,
    e_date_time_closing: Int,
    e_limit: Int,
    e_location: String,
    e_cat: String,
    e_subcat: String,
    e_hashtags: String,
    e_users: String,
    e_time_created: Int,
    e_topics: String,
    e_summary: String):Boolean=
  {
    
    
    withTx {
    implicit neo =>
       val EventIndex = getNodeIndex("event").get
       val UserIndex = getNodeIndex("user").get
       val SubcatIndex = getNodeIndex("sub_category").get
       val TopicIndex = getNodeIndex("topic").get
       val event_content_index = getNodeIndex("event_content").get
       val event_title_index = getNodeIndex("event_title").get
       val hash_weight_index = getNodeIndex("hash_weight").get
       val event_node = EventIndex.get("id",e_id).getSingle()
       
       val user_node = UserIndex.get("id",user_name).getSingle()
       var ret = false
       var closing = e_date_time_closing
	   if(e_date_time_closing == 0)
	   {
	      closing = e_date_time
	   }
       if(user_node != null && event_node != null  && !e_cat.equals("") && !e_subcat.equals(""))
	   {
           user_node.setProperty("last_seen",e_time_created)
           val time = event_node.getProperty("time_created").toString().toInt
	       val event_wt = event_node.getProperty("weight").toString().toInt
	       event_node.setProperty("event_title",e_title)
	       event_node.setProperty("event_summary",e_summary)
	       event_node.setProperty("event_title_id",e_title_id)
	       event_node.setProperty("event_date_time",e_date_time)
	       event_node.setProperty("event_date_time_closing",closing)
	       event_node.setProperty("event_limit",e_limit)
	       event_node.setProperty("event_location",e_location)
	       event_content_index.remove(event_node)
	       event_content_index += (event_node,"time",new ValueContext( e_date_time ).indexNumeric())
	       event_title_index.remove(event_node)
	       event_title_index += (event_node,"title_id",e_title_id)
	       var rels = event_node.getRelationships("Belongs_To_Event_Category","Belongs_To_Location_Event","User_Of_Event").asScala
	       for(each <- rels)
	       {
	         each.delete()
	       }
	       
	      var spam_flag = 0
	      if(event_node.hasRelationship("User_Spammed_Of_Event_Category",Direction.OUTGOING))
	      {
	        spam_flag = 1
	        val spam_rels = event_node.getRelationships("User_Spammed_Of_Event_Category",Direction.OUTGOING).asScala
	        for(each <- spam_rels)
	        {
	          each.delete()
	        }
	      }
	       
	       val tags_list = e_hashtags.split(",").distinct.toList.map( x => x.toLowerCase())
	       var old_tags = List[Any]()
	       rels = event_node.getRelationships("Tag_Of_Event").asScala
	       for(each <- rels)
	       {
	           val tag = each.getOtherNode(event_node).getProperty("name").toString()
	           old_tags ::= tag
	           if(!tags_list.contains(tag))
	           {
	             val tag_node = each.getOtherNode(event_node)
	             val h_wt = tag_node.getProperty("weight").toString().toInt
	             tag_node.setProperty("weight",(h_wt - 10))
	             hash_weight_index.remove(tag_node)
	             hash_weight_index += (tag_node,"weight",new ValueContext( h_wt - 10 ).indexNumeric())
	             each.delete() 
	           }
	        	 
	       }
	       
	       for(tag_name <- tags_list)
	       {
	         if(!old_tags.contains(tag_name))
	         {
	           if(tag_name != "")
	              {
	                
	                if(SubcatIndex.get("name",tag_name).getSingle() != null)
	                {
	                  val tag_node = SubcatIndex.get("name",tag_name).getSingle()
	                  val h_wt = tag_node.getProperty("weight").toString().toInt
	                  tag_node.setProperty("weight",(h_wt + 10))
	                  var rel = tag_node --> "Tag_Of_Event" --> event_node <
	                  var rel_time = rel.setProperty("time", e_time_created)
	                  var rel_in_wt = rel.setProperty("in_weight", 0)
	                  var rel_out_wt = rel.setProperty("out_weight", 10)
	                  hash_weight_index.remove(tag_node)
	                  hash_weight_index += (tag_node,"weight",new ValueContext( h_wt + 10 ).indexNumeric())
	                  
	                }
	                else
	                {
	                  val tag_node = createNode(sub_category(tag_name,e_time_created,10))
	                  SubcatIndex += (tag_node,"name",tag_name)
	                  var rel = tag_node --> "Tag_Of_Event" --> event_node <
	                  var rel_time = rel.setProperty("time", e_time_created)
	                  var rel_in_wt = rel.setProperty("in_weight", 0)
	                  var rel_out_wt = rel.setProperty("out_weight", 10)
	                  rel = tag_node --> "Hashtag_Created_By" --> user_node <
	                  var rel_time1 = rel.setProperty("time", time)
	                  hash_weight_index += (tag_node,"weight",new ValueContext( 10 ).indexNumeric())
	                  
	                }
	              }
	         }
	       }
	       
	       
	          if(!e_users.equals(""))
	          {
	            val e_users_list = e_users.split(",").distinct
	            for(each <- e_users_list)
	            {
	              if(!each.equals(""))
	              {
	              
		              val user_node1 = UserIndex.get("id",each).getSingle()
		              if(user_node1 != null)
		              {
		                val rel1: org.neo4j.graphdb.Relationship = user_node1 --> "User_Of_Event"  --> event_node <
		                val y = rel1.setProperty("time", e_time_created)
		              }
	              }
	          
	            }
	          
		      }  
	       
	   var hash_list = e_subcat.split(",").toList.map( x => x.toLowerCase())
	   var main_hash = hash_list(0)
       var old_hash = List[Any]()
       rels = event_node.getRelationships("Belongs_To_Subcategory_Event").asScala
       for(each <- rels)
       {
           val tag = each.getOtherNode(event_node).getProperty("name").toString().toLowerCase()
           if(each.hasProperty("main") && !tag.equals(main_hash))
	       {
	          each.removeProperty("main")
	             
	       }
           old_hash ::= tag
           if(!hash_list.contains(tag))
           {
             val tag_node = each.getOtherNode(event_node)
             val h_wt = tag_node.getProperty("weight").toString().toInt
             tag_node.setProperty("weight",(h_wt - event_wt))
             hash_weight_index.remove(tag_node)
             hash_weight_index += (tag_node,"weight",new ValueContext( h_wt - event_wt ).indexNumeric())
             each.delete() 
           }
         
        	 
       }
       
       for(tag_name <- hash_list)
       {
         if(!old_hash.contains(tag_name))
         {
           if(tag_name != "")
            {
              if(SubcatIndex.get("name",tag_name).getSingle() != null)
              {
                val subcat_node = SubcatIndex.get("name",tag_name).getSingle()
                val h_wt = subcat_node.getProperty("weight").toString().toInt
                subcat_node.setProperty("weight",(h_wt + event_wt))
                if(tag_name.equals(main_hash))
                {
	                var rel = event_node --> "Belongs_To_Subcategory_Event" --> subcat_node <
	                var rel_main = rel.setProperty("main", 1)
	                var rel_time = rel.setProperty("time", e_time_created)
	                var rel_in_wt = rel.setProperty("in_weight", event_wt)
	                var rel_out_wt = rel.setProperty("out_weight", 0)
                }
                else
                {
	                var rel = event_node --> "Belongs_To_Subcategory_Event" --> subcat_node <
	                var rel_main = rel.setProperty("main", 1)
	                var rel_time = rel.setProperty("time", e_time_created)
	                var rel_in_wt = rel.setProperty("in_weight", event_wt)
	                var rel_out_wt = rel.setProperty("out_weight", 0)
                }
                hash_weight_index.remove(subcat_node)
                hash_weight_index += (subcat_node,"weight",new ValueContext( h_wt + event_wt ).indexNumeric())
              }
              else
              {
                val subcat_node = createNode(sub_category(tag_name,e_time_created,event_wt))
                SubcatIndex += (subcat_node,"name",tag_name)
                if(tag_name.equals(main_hash))
                {
	                var rel = event_node --> "Belongs_To_Subcategory_Event" --> subcat_node <
	                var rel_main = rel.setProperty("main", 1)
	                var rel_time = rel.setProperty("time", e_time_created)
	                var rel_in_wt = rel.setProperty("in_weight", event_wt)
	                var rel_out_wt = rel.setProperty("out_weight", 0)
                }
                else
                {
	                var rel = event_node --> "Belongs_To_Subcategory_Event" --> subcat_node <
	                var rel_main = rel.setProperty("main", 1)
	                var rel_time = rel.setProperty("time", e_time_created)
	                var rel_in_wt = rel.setProperty("in_weight", event_wt)
	                var rel_out_wt = rel.setProperty("out_weight", 0)
                }
                
                var rel = subcat_node --> "Hashtag_Created_By" --> user_node <
                var rel_time1 = rel.setProperty("time", time)
                
                hash_weight_index += (subcat_node,"weight",new ValueContext(event_wt).indexNumeric())
              }
            }
         }
       }
       
       	   	       
	     val CategoryIndex = getNodeIndex("category").get
	     
	     var cats = e_cat.split(",").toList.map( x => x.toLowerCase()) 
	     cats ::= "all"
	     for(each <- cats)
	     {
	       //event_content_index += (event_node,"event_cat",each)
	       if(CategoryIndex.get("name",each).getSingle() != null)  
	       {
	         val category_node = CategoryIndex.get("name",each).getSingle()
	         var rel = event_node --> "Belongs_To_Event_Category" --> category_node <
	         var rel_time = rel.setProperty("time", time)
	         if(spam_flag == 1)
	         {
	           rel = event_node --> "User_Spammed_Of_Event_Category" --> category_node <
	           var rel_time = rel.setProperty("time", time)
	         }
	       }
	       else
	       {
	         val category_node = createNode(category(each,"",""))
	         CategoryIndex += (category_node,"name",each)
	         var rel = event_node --> "Belongs_To_Event_Category" --> category_node <
	         var rel_time = rel.setProperty("time", time)
	         if(spam_flag == 1)
	         {
	           rel = event_node --> "User_Spammed_Of_Event_Category" --> category_node <
	           var rel_time = rel.setProperty("time", time)
	         }
	         
	       }
	     }
	     
	          val location_index = getNodeIndex("location").get
	          val states = location_index.query( "id", "*" ).iterator().asScala.toList.map(x => x.getProperty("location_id").toString())
              val locations = e_location.split(", ").toList.distinct.map(x => x.toLowerCase()).intersect(states)
              for(each <- locations)
              {
                val loc_node = location_index.get("id",each).getSingle()
                event_node --> "Belongs_To_Location_Event" --> loc_node 
              }
	          
	          val tagger = new MaxentTagger(
                "/var/n4j/data/left3words-wsj-0-18.tagger")
              val tagged = tagger.tagString(e_content)
              val noun_list = tagged.split(" ").toList.map(x => x.split("/")).filter( x => (x.size == 2 && x(1).equals("NNP"))).map(x => x(0).toLowerCase())
              var noun_hash_list = noun_list:::hash_list
              event_content_index += (event_node,"event_content",(noun_hash_list.mkString(" ") + " " + gummy(e_title)) )
              val proper_nouns = List("2013","2014","india","indian","reddy","rao","singh","world","pti","sunday","monday","tuesday","wednesday","thursday","friday","saturday","january","february","march","april","may","june","july","august","september","october","november","december")
              noun_hash_list = noun_hash_list.filterNot(x => proper_nouns.contains(x) && x.length() < 3)
              event_node.setProperty("event_content",noun_hash_list.mkString(" "))

	          ret = true
       }
    ret
    }
  }

// called when an article is pinned
  def pin_item_e(
        item_type: String,
        item_id: String, // article id
        cat: String,
        space_id: String
        ):Boolean=
    {
    
    withTx {
      
     implicit neo =>
       
       
       
       val CategoryIndex = getNodeIndex("category").get
       def matchTest(x: String): String = x match {
	    case "A" => "Article"
	    case "E" => "Event"
	    case "P" => "Petition"
	    case "T" => "Townhall"
	    case "D" => "Debate"
	    case "C" => "Comment"
	    case "U" => "User"
	    case "H" => "Sub_category"
	    case "S" => "Space"
	    case "a" => "Article"
	    case "e" => "Event"
	    case "p" => "Petition"
	    case "t" => "Townhall"
	    case "d" => "Debate"
	    case "c" => "Comment"
	    case "u" => "User"
	    case "h" => "Sub_category"
	    case "s" => "Space"
	    }
	    val i_type = matchTest(item_type)
	   
	     
	     val grp_name = i_type.toLowerCase()
	     val Index = getNodeIndex(grp_name).get
	     
	     val item_node = Index.get("id",item_id).getSingle()  // Retrieving Item node based on item_id
	     
	     var ret = false
	     if(!space_id.equals(""))
	     {
	       val SpaceIndex = getNodeIndex("space").get
	       val space_node = SpaceIndex.get("id",space_id).getSingle()  // Retrieving Item node based on item_id
	       if(item_node != null && space_node != null)
	       {
	               var pin_list = space_node.getProperty("pins").toString().split(",").toList
	               // checking if the article is already pinned to that category
	               if(!pin_list.contains(item_id))
	               {
	                 // adding article to the pinned list of a category
	                 pin_list ::= item_id
	                 space_node.setProperty("pins",pin_list.mkString(",").stripPrefix(",").stripSuffix(",").trim)
	               }
	               else
	               {
	                 pin_list = pin_list.filterNot(x => x.equals(item_id))
	                 space_node.setProperty("pins",pin_list.mkString(",").stripPrefix(",").stripSuffix(",").trim)
	               
	               }
	               ret = true
	       }
	       
	     }
	    
	     else
	     {
	       
	       var ret_cat_list = List[String]()
	       val cat_nodes = CategoryIndex.query( "name", "*" ).iterator().asScala.toList
	       val tot_cat_list = cat_nodes.map( x => x.getProperty("name").toString())
	       if(!cat.equals(""))
	       {
	         ret_cat_list = cat.split(",").toList.map(x => x.toLowerCase())
	       }
	       
	       
	       if(item_node != null)
	       {
	           for(each <- cat_nodes)
	           {
	       
	             if(ret_cat_list.contains(each.getProperty("name").toString()))
	             {
	               var pin_list = each.getProperty("pins").toString().split(",").toList
	               // checking if the article is already pinned to that category
	               if(!pin_list.contains(item_id))
	               {
	                 // adding article to the pinned list of a category
	                 pin_list ::= item_id
	                 each.setProperty("pins",pin_list.mkString(",").stripPrefix(",").stripSuffix(",").trim)
	               }
	             }
	             else
	             {
	               var pin_list = each.getProperty("pins").toString().split(",").toList
	               // removing article from pinned list
	               if(pin_list.contains(item_id))
	               {
	                 pin_list = pin_list.filterNot(x => x.equals(item_id))
	                 each.setProperty("pins",pin_list.mkString(",").stripPrefix(",").stripSuffix(",").trim)
	               }
	             }
	           }
	       ret = true  
	       }
	     }
	     
       
       
       
       
     ret
     }
     
     
  }

// Triggered when a event is deleted
  def delete_event(
      id: String // unique event id
      ):Boolean=
  {
    
    withTx {
    implicit neo =>
       val event_weight_index = getNodeIndex("event_weight").get
       val user_weight_index = getNodeIndex("user_weight").get
       val hash_weight_index = getNodeIndex("hash_weight").get
       val EventIndex = getNodeIndex("event").get
       val TilesIndex = getNodeIndex("tiles").get
       val FeaturedTilesIndex = getNodeIndex("featured_tiles").get
       val HeadlinesIndex = getNodeIndex("headlines").get
       val CommentIndex = getNodeIndex("comment").get
       val EventContentIndex = getNodeIndex("event_content").get
       val EventTitleIndex = getNodeIndex("event_title").get
       val SubcatIndex = getNodeIndex("sub_category").get
       val event_node = EventIndex.get("id",id).getSingle()
       val c_time = (System.currentTimeMillis()/1000).toInt
       
       var ret = false
       if(event_node != null)
	   {
           pin_item_e("E",id,"","")
           val space_rels = event_node.getRelationships("Event_Tagged_To_Space").asScala.toList
	           for(space_rel <- space_rels)
	           {
	             val space_name = space_rel.getOtherNode(event_node).getProperty("space_id").toString()
	             pin_item_e("E",id,"",space_name)
	           }
           val e_end_time = event_node.getProperty("event_date_time_closing").toString().toInt
	       val rels = event_node.getRelationships().asScala
	       val title_id = event_node.getProperty("event_title_id")
	       val e_wt = event_node.getProperty("weight").toString().toInt
	       val author_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
		   var rel = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING)
		   var del_wt = rel.getProperty("out_weight").toString().toInt
	       val au_wt = author_node.getProperty("weight").toString().toInt
	       if(c_time < e_end_time)
	       {
		       author_node.setProperty("weight",au_wt-(e_wt+5-del_wt))
			   user_weight_index.remove(author_node)
			   user_weight_index += (author_node,"weight",new ValueContext( au_wt-(e_wt+5-del_wt) ).indexNumeric())
	       }
		   
	       for(each <- rels)
	       {
	         
	         if(each.getOtherNode(event_node).getProperty("__CLASS__").equals("Saddahaq.comment"))
	         {
	           
	             val p_node = each.getOtherNode(event_node)
	             val p_rels = p_node.getRelationships().asScala
	             for(single <- p_rels)
	             {
	               single.delete()
	             }
	             CommentIndex.remove(p_node)
	             p_node.delete()
	           
	         }
	         
	         else if(each.getOtherNode(event_node).getProperty("__CLASS__").equals("Saddahaq.poll"))
             {
           
	               val poll_node = each.getOtherNode(event_node)
	               poll_node.getSingleRelationship("Poll_App_Of_Event",Direction.OUTGOING).delete()
		           val vote_rels = poll_node.getRelationships("Voted_To_Poll").asScala
		           for(each <- vote_rels)
		           {
		             each.delete()
		           }
			       poll_node.delete()
          
             }
	         
	         else if(each.getOtherNode(event_node).getProperty("__CLASS__").equals("Saddahaq.sub_category"))
	         {
	             val rel_type = each.getType().toString() 
	             if(c_time < e_end_time)
	             {
		             if(rel_type.equals("Tag_Of_Event"))
		             {
		               val tag_node = each.getOtherNode(event_node)
		               val h_wt = tag_node.getProperty("weight").toString().toInt
		               tag_node.setProperty("weight",(h_wt - 10))
		               hash_weight_index.remove(tag_node)
		               hash_weight_index += (tag_node,"weight",new ValueContext( h_wt - 10 ).indexNumeric())
		             }
		             
		             else
		             {
		               val hash_node = each.getOtherNode(event_node)
			           val hash_wt = hash_node.getProperty("weight").toString().toInt
			           hash_node.setProperty("weight",(hash_wt - e_wt))
			           hash_weight_index.remove(hash_node)
			           hash_weight_index += (hash_node,"weight",new ValueContext( hash_wt - e_wt ).indexNumeric())
			        
		             }
	             }
	             each.delete()
	         }
	         
	         
	         else
	         {
	             val rel_type = each.getType().toString()
	             if(rel_type.equals("Belongs_To_Event_Category"))
	             {
	               // deleting the relation with category
	               val c_name = each.getOtherNode(event_node).getProperty("name").toString()
	               var news_node = TilesIndex.get("id",c_name).getSingle()
	               if(news_node != null)
	               {
	                 val news = news_node.getProperty("value").toString()
			         var news_list = news.split(",").toList
			         if(news_list.contains(id))
			         {
			           news_list = news_list - id
			           news_node.setProperty("value",news_list.mkString(","))
			         }
			         
	               }
		           
	               news_node = FeaturedTilesIndex.get("id",c_name).getSingle()
	               if(news_node != null)
	               {
	                 val news = news_node.getProperty("value").toString()
			         var news_list = news.split(",").toList
			         if(news_list.contains(id))
			         {
			           news_list = news_list - id
			           news_node.setProperty("value",news_list.mkString(","))
			         }
			         
	               }
	               
	               news_node = HeadlinesIndex.get("id",c_name).getSingle()
	               if(news_node != null)
	               {
	                 val news = news_node.getProperty("value").toString()
			         var news_list = news.split(",").toList
			         if(news_list.contains(id))
			         {
			           news_list = news_list - id
			           news_node.setProperty("value",news_list.mkString(","))
			         }
			         
	               }

	             }
	             each.delete()
	         }
	       }
	         
	       EventIndex.remove(event_node)
	       EventContentIndex.remove(event_node)
	       EventTitleIndex.remove(event_node)
	       event_weight_index.remove(event_node)
	       event_node.delete()
	       
	                 var latest_node = TilesIndex.get("id","latest").getSingle()
	       			 if(latest_node != null)
	       			 {
		                 val news = latest_node.getProperty("value").toString()
				         var news_list = news.split(",").toList
				         if(news_list.contains(id))
				         {
				           news_list = news_list - id
				           val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
			               latest_node.setProperty("value",news_list_trim)
				         }
	       			 }
	       
	       			 latest_node = FeaturedTilesIndex.get("id","latest").getSingle()
	       			 if(latest_node != null)
	       			 {
		                 val news = latest_node.getProperty("value").toString()
				         var news_list = news.split(",").toList
				         if(news_list.contains(id))
				         {
				           news_list = news_list - id
				           val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
			               latest_node.setProperty("value",news_list_trim)
				         }
	       			 }
	       
	       ret = true
       } 
    ret
    }
       
  }
  
 

  // This function is trigged when a user responds to an event( Only YES) and also when a user changes his responce from No to YES
  def event_response(
      id:String, // event id
      user_name:String,  // unique user name
      time:Int
      ):Boolean=
  {
    
    
    withTx {
    implicit neo =>
      
     val EventIndex = getNodeIndex("event").get
     val event_node = EventIndex.get("id",id).getSingle()
     val UserIndex = getNodeIndex("user").get
     val user_node = UserIndex.get("id",user_name).getSingle()
     val event_weight_index = getNodeIndex("event_weight").get
     val user_weight_index = getNodeIndex("user_weight").get
     val hash_weight_index = getNodeIndex("hash_weight").get
     var ret = false
     if(user_node != null && event_node != null)
	 {
         user_node.setProperty("last_seen",time)
	     val u_wt = user_node.getProperty("weight").toString().toInt
	     // getting hash nodes related to the event
	     val hash_nodes = event_node.getRelationships("Belongs_To_Subcategory_Event",Direction.OUTGOING).asScala.map(_.getOtherNode(event_node)).toList
	       
	     // creating relation between event and user
	     val rel_id = id+user_name+"Is_Attending"  
	     val Relation_Index = getRelationIndex("event_response").get
	     val rel: org.neo4j.graphdb.Relationship = user_node --> "Is_Attending" --> event_node <
	     val rel_time = rel.setProperty("time", time)
	     // increasing the weights of user, author, event and hash nodes
	     var rel_in_wt = rel.setProperty("in_weight", Math.round(u_wt.asInstanceOf[Float]/200))
	     var rel_out_wt = rel.setProperty("out_weight", 5)
	     Relation_Index += (rel,"rel_id",rel_id)
	     val e_wt = event_node.getProperty("weight").toString().toInt
	     event_node.setProperty("weight",e_wt+(Math.round(u_wt.asInstanceOf[Float]/200)))
	     event_weight_index.remove(event_node)
	     event_weight_index += (event_node,"weight",new ValueContext( e_wt+(Math.round(u_wt.asInstanceOf[Float]/200)) ).indexNumeric())
	     
	     user_node.setProperty("weight",u_wt+5)
	     user_weight_index.remove(user_node)
	     user_weight_index += (user_node,"weight",new ValueContext( u_wt+5 ).indexNumeric())
	     val author_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
	     val au_wt = author_node.getProperty("weight").toString().toInt
	     author_node.setProperty("weight",au_wt+(Math.round(u_wt.asInstanceOf[Float]/200)))
	     user_weight_index.remove(author_node)
	     user_weight_index += (author_node,"weight",new ValueContext( au_wt+(Math.round(u_wt.asInstanceOf[Float]/200))).indexNumeric())
	     for(each <- hash_nodes)
	     {
	         val h_wt = each.getProperty("weight").toString().toInt
		     each.setProperty("weight",h_wt + (Math.round(u_wt.asInstanceOf[Float]/200)))
		     hash_weight_index.remove(each)
			 hash_weight_index += (each,"weight",new ValueContext( h_wt + (Math.round(u_wt.asInstanceOf[Float]/200)) ).indexNumeric())
	     }
		 ret = true
     }
     ret
    }
  }
  
  // triggered when a user is invited to an event
  def event_invite(
      id:String, // event id
      user_name:String,  // unique user name
      users:String,
      time:Int
      ):Boolean=
  {
    
    
    withTx {
    implicit neo =>
      
     var ret = false
     val EventIndex = getNodeIndex("event").get
     val event_node = EventIndex.get("id",id).getSingle()
     val UserIndex = getNodeIndex("user").get
     val user_node = UserIndex.get("id",user_name).getSingle()
     val user_list = users.split(",")
     if(user_node != null && event_node != null)
     {
         user_node.setProperty("last_seen",time)
	     for(each <- user_list)
	     {
	       val node = UserIndex.get("id",each).getSingle()
	       // checking if the user was already invited
	       if(node != null && !event_node.getRelationships("Invited_To_Event").asScala.map(_.getOtherNode(event_node)).toList.contains(node))
	       {
	         // creating relation between user and the event
	         val rel: org.neo4j.graphdb.Relationship = node --> "Invited_To_Event" --> event_node <
		     val rel_time = rel.setProperty("time", time)
		     val rel_user = rel.setProperty("user", user_name)
		   }
	     }
	     ret = true
     } 
      
    ret  
    }
  }
  
  def event_notify(
      user_name:String, // event id
      id:String,  // unique user name
      content:String,
      time:Int
      ):Boolean=
  {
    
    
    withTx {
    implicit neo =>
      
     var ret = false
     val EventIndex = getNodeIndex("event").get
     val event_node = EventIndex.get("id",id).getSingle()
     val UserIndex = getNodeIndex("user").get
     val user_node = UserIndex.get("id",user_name).getSingle()
     
     if(user_node != null && event_node != null)
     {   
         user_node.setProperty("last_seen",time)
         var user_list = event_node.getRelationships("Is_Attending").asScala.map(_.getOtherNode(event_node)).toList
         val event_auth = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode()
         user_list :+= event_auth
    
	     for(each <- user_list)
	     {
	         val rel: org.neo4j.graphdb.Relationship = each --> "Notified_In_Event" --> event_node <
		     val rel_time = rel.setProperty("time", time)
		     val rel_user = rel.setProperty("user", user_name)
		     val rel_content = rel.setProperty("content", content)
		   
	     }
	     ret = true
     } 
      
    ret  
    }
  }
  
  // This function is trigged when a user changes his response to an event ( Only form Yes To No)
  def event_changeresponse(
      id:String,  // event id
      user_name:String,  // unique user name
      time:Int
      ):Boolean=
  {
   
    withTx {
    implicit neo =>
      
     val EventIndex = getNodeIndex("event").get
     val event_node = EventIndex.get("id",id).getSingle()
     val UserIndex = getNodeIndex("user").get
     val user_node = UserIndex.get("id",user_name).getSingle()
     val event_weight_index = getNodeIndex("event_weight").get
     val user_weight_index = getNodeIndex("user_weight").get
     val hash_weight_index = getNodeIndex("hash_weight").get
     val Relation_Index = getRelationIndex("event_response").get
     var ret = false
     if(user_node != null && event_node != null)
	 {
       
         user_node.setProperty("last_seen",time)
	     val rel_id = id+user_name+"Is_Attending"
	     val relation = Relation_Index.get("rel_id",rel_id).getSingle()
	     val r_wt = relation.getProperty("in_weight").toString().toInt
	     val u_wt = user_node.getProperty("weight").toString().toInt
	     val hash_nodes = event_node.getRelationships("Belongs_To_Subcategory_Event",Direction.OUTGOING).asScala.map(_.getOtherNode(event_node)).toList
	     
		 val e_wt = event_node.getProperty("weight").toString().toInt
		 val author_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getOtherNode(event_node)
	     val au_wt = author_node.getProperty("weight").toString().toInt
	     event_node.setProperty("weight",e_wt-r_wt)
	     author_node.setProperty("weight",au_wt-r_wt)
	     user_node.setProperty("weight",u_wt-5)
	     Relation_Index.remove(relation)
	     relation.delete()
	     
	     event_weight_index.remove(event_node)
	     event_weight_index += (event_node,"weight",new ValueContext( e_wt-r_wt ).indexNumeric())
	     user_weight_index.remove(author_node)
	     user_weight_index += (author_node,"weight",new ValueContext( au_wt-r_wt).indexNumeric())
	     user_weight_index.remove(user_node)
	     user_weight_index += (user_node,"weight",new ValueContext( u_wt-5).indexNumeric())
	     for(each <- hash_nodes)
	     {
	         val h_wt = each.getProperty("weight").toString().toInt
		     each.setProperty("weight",h_wt-r_wt)
		     hash_weight_index.remove(each)
			 hash_weight_index += (each,"weight",new ValueContext( h_wt-r_wt ).indexNumeric())
	     }
	     ret = true
     }
     ret
    }
  }
  
  // This function gives first 3 events belonging to a category
    def get_events_category(
        user_name:String, // unique user name
        category:String 
        
        ) :String =
    {
    
	   var main_list = List[Any]()
       var today_list = List[Any]()
       var upcoming_list = List[Any]()
       val rightNow : Calendar= Calendar.getInstance();
       val date:Date = new Date();
       // offset to add since we're not UTC
       val offset: Long = rightNow.get(Calendar.ZONE_OFFSET) +
       rightNow.get(Calendar.DST_OFFSET);
       val since: Int = ((rightNow.getTimeInMillis() + offset) % (24 * 60 * 60 * 1000)).toInt/1000;
       val till: Int = (24*3600) - since
     
       val t1 = (System.currentTimeMillis()/1000).toInt - since
       val t2 = (System.currentTimeMillis()/1000).toInt + till
       var sorted_events_today  = List[org.neo4j.graphdb.Node]()
       var sorted_events_upcoming  = List[org.neo4j.graphdb.Node]()
       val cur_time = (System.currentTimeMillis() /1000).toInt
       val nodeIndex = getNodeIndex("category").get
       val node = nodeIndex.get("name",category).getSingle()
       val UserIndex = getNodeIndex("user").get
       val user_node = UserIndex.get("id",user_name).getSingle()
       val l = List("E_ID","E_Title","E_Title_ID","E_Category","E_SubCategory","E_Location","E_Time")
       var today_cnt = 0;
       var up_cnt = 0;
       if(node != null && (user_name.equals("") || user_node == null))
	   {
           val events_total = node.getRelationships("Belongs_To_Event_Category",Direction.INCOMING).asScala.map(_.getOtherNode(node)).toList.filter(x => (x.getProperty("event_date_time_closing").toString().toInt > cur_time)  )
	       val events_upcoming = events_total.filter(x => (x.getProperty("event_date_time").toString().toInt >= t2)  )
	       sorted_events_upcoming = events_upcoming.sortBy( x =>   -(((x.getProperty("views").toString().toFloat)+(x.getProperty("weight").toString().toFloat*10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
	       up_cnt = sorted_events_upcoming.size
	       if(events_upcoming != null)
           {
		       
		       upcoming_list = sorted_events_upcoming.slice(0,3).map(x => JSONObject(l.zip(List(x.getProperty("event_id"),x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getProperty("event_location"),x.getProperty("event_date_time"))).toMap))
           }

	       val events_today = events_total.filterNot(x => events_upcoming.contains(x) )
	       sorted_events_today = events_today.sortBy( x =>   -(((x.getProperty("views").toString().toFloat)+(x.getProperty("weight").toString().toFloat*10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
	       today_cnt = sorted_events_today.size
	       if(events_today != null)
           {
		       
		       today_list = sorted_events_today.slice(0,3).map(x => JSONObject(l.zip(List(x.getProperty("event_id"),x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getProperty("event_location"),x.getProperty("event_date_time"))).toMap))
           }     

       }
       
       else if(node != null && user_node != null)
	   {
	         
	       
	       var loc_events = List[org.neo4j.graphdb.Node]()
	       var loc_events_upcoming = List[org.neo4j.graphdb.Node]()
	       var loc_events_today = List[org.neo4j.graphdb.Node]()
	       if(user_node.hasRelationship("Belongs_To_Location"))
	       {
	         loc_events = user_node.getSingleRelationship("Belongs_To_Location",Direction.OUTGOING).getOtherNode(user_node).getRelationships("Belongs_To_Location_Event").asScala.toList.map(_.getStartNode()).filter(x => (x.getProperty("event_date_time_closing").toString().toInt > cur_time)  )
	         loc_events_upcoming = loc_events.filter(x => (x.getProperty("event_date_time").toString().toInt >= t2)  ).sortBy( x =>   -(((x.getProperty("views").toString().toFloat)+(x.getProperty("weight").toString().toFloat*10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
	         loc_events_today = loc_events.filterNot(x => loc_events_upcoming.contains(x) ).sortBy( x =>   -(((x.getProperty("views").toString().toFloat)+(x.getProperty("weight").toString().toFloat*10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
	       }
	       
	       val events_total = node.getRelationships("Belongs_To_Event_Category",Direction.INCOMING).asScala.map(_.getOtherNode(node)).toList.filter(x => (x.getProperty("event_date_time_closing").toString().toInt > cur_time)  )
	       val events_upcoming = events_total.filter(x => (x.getProperty("event_date_time").toString().toInt >= t2)  )
	       sorted_events_upcoming = events_upcoming.sortBy( x =>   -(((x.getProperty("views").toString().toFloat)+(x.getProperty("weight").toString().toFloat*10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
	       up_cnt = sorted_events_upcoming.size
	       if(events_upcoming != null)
           {
		       upcoming_list = (loc_events_upcoming:::sorted_events_upcoming).distinct.slice(0,3).map(x => JSONObject(l.zip(List(x.getProperty("event_id"),x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getProperty("event_location"),x.getProperty("event_date_time"))).toMap))
           }

	       val events_today = events_total.filterNot(x => events_upcoming.contains(x) )
	       sorted_events_today = events_today.sortBy( x =>   -(((x.getProperty("views").toString().toFloat)+(x.getProperty("weight").toString().toFloat*10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
	      
	       today_cnt = sorted_events_today.size
	       if(events_today != null)
           {
		       
		       today_list = (loc_events_today:::sorted_events_today).distinct.slice(0,3).map(x => JSONObject(l.zip(List(x.getProperty("event_id"),x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getProperty("event_location"),x.getProperty("event_date_time"))).toMap))
           }    

	   }
         
         var t_list = List[Any]()
         var u_list = List[Any]()
         var m_list = List[Any]()
         var today_map = scala.collection.mutable.Map[String, Any]()
         var today_map_cnt = scala.collection.mutable.Map[String, Int]()
         today_map_cnt("Events_Count") = today_cnt
         today_map("Events") = JSONArray(today_list)
         var up_map = scala.collection.mutable.Map[String, Any]()
         var up_map_cnt = scala.collection.mutable.Map[String, Int]()
         up_map_cnt("Events_Count") = up_cnt
         up_map("Events") = JSONArray(upcoming_list)
	    
	     
	     
	     
	     t_list ::= JSONObject(today_map_cnt.toMap)
	     t_list ::= JSONObject(today_map.toMap)
	     
	     u_list ::= JSONObject(up_map_cnt.toMap)
	     u_list ::= JSONObject(up_map.toMap)
	     
	     var u_map = scala.collection.mutable.Map[String, Any]()
	     var t_map = scala.collection.mutable.Map[String, Any]()
	     
	     u_map("Ongoing") = JSONArray(t_list)
	     t_map("Upcoming") = JSONArray(u_list)
	     
	     m_list ::= JSONObject(u_map.toMap)
	     m_list ::= JSONObject(t_map.toMap)
	     
	     JSONArray(m_list).toString()
  
  }
    
    def get_leftpane(
        user_name:String, // unique user name
        item_type:String,
        content:String,
        category:String
        
        ) :String =
          
    {
       val t = (System.currentTimeMillis() /1000).toInt
       val c_time = (System.currentTimeMillis() /1000).toInt - 86400
       val catnode_index = getNodeIndex("category").get
       val cat_node = catnode_index.get("name",category).getSingle()
       val UserIndex = getNodeIndex("user").get
       val PetitionIndex = getNodeIndex("petition").get
       val DebateIndex = getNodeIndex("debate").get
       val TownhallIndex = getNodeIndex("townhall").get
       val event_content_index = getNodeIndex("event_content").get
       val p_content_index = getNodeIndex("petition_content").get
       val d_content_index = getNodeIndex("debate_content").get
       val t_content_index = getNodeIndex("townhall_content").get
       val subcatnode_index = getNodeIndex("sub_category").get
       val user_node = UserIndex.get("id",user_name).getSingle()
       var events_total  = List[org.neo4j.graphdb.Node]()
       var petitions_total  = List[org.neo4j.graphdb.Node]()
       var total_debates  = List[org.neo4j.graphdb.Node]()
       var total_townhalls  = List[org.neo4j.graphdb.Node]()
       var sorted_events  = List[org.neo4j.graphdb.Node]()
       var sorted_petitions  = List[org.neo4j.graphdb.Node]()
       var sorted_debates  = List[org.neo4j.graphdb.Node]()
       var sorted_townhalls  = List[org.neo4j.graphdb.Node]()
       var follows  = List[org.neo4j.graphdb.Node]()
       if(item_type.equals("") && cat_node != null)
	   {
           var loc_events = List[org.neo4j.graphdb.Node]()
           events_total = cat_node.getRelationships("Belongs_To_Event_Category",Direction.INCOMING).asScala.map(_.getOtherNode(cat_node)).toList.filter(x => (x.getProperty("approved").toString().toInt == 1 && x.getProperty("event_date_time_closing").toString().toInt > t)  )
           
	       if(user_node != null && user_node.hasRelationship("Belongs_To_Location"))
	       {
	         loc_events = user_node.getSingleRelationship("Belongs_To_Location",Direction.OUTGOING).getOtherNode(user_node).getRelationships("Belongs_To_Location_Event").asScala.toList.map(_.getStartNode()).filter(x => (x.getProperty("approved").toString().toInt == 1 && x.getProperty("event_date_time_closing").toString().toInt > t)  ).sortBy(_.getProperty("event_date_time").toString().toInt)
	         loc_events = loc_events.filter( x => events_total.contains(x))
           
	       }
	       
           sorted_events = events_total.sortBy( x =>   -(((x.getProperty("views").toString().toFloat)+(x.getProperty("weight").toString().toFloat*10))/ ((((t-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
		   sorted_events = (loc_events:::sorted_events).distinct.slice(0,4)
		   
		   petitions_total = PetitionIndex.query("id", "*" ).iterator().asScala.toList.filter(x => (x.getProperty("approved").toString().toInt == 1 && x.getProperty("end_date").toString().toInt > t)  )
         
	       total_debates = DebateIndex.query("id", "*" ).iterator().asScala.toList.filter(x => (x.getProperty("d_date").toString().toInt + 3600 > t)  ).sortBy( x => x.getProperty("d_date").toString().toInt)
	       total_townhalls = TownhallIndex.query("id", "*" ).iterator().asScala.toList.filter(x => (x.getProperty("t_date").toString().toInt + 3600 > t)  ).sortBy( x => x.getProperty("t_date").toString().toInt)
	       sorted_petitions = petitions_total.sortBy(-_.getProperty("time_created").toString().toInt).slice(0,4)
	       
	       sorted_debates = total_debates.slice(0,4)
	       
	       sorted_townhalls = total_townhalls.slice(0,4) 
	   } 
       
       else if(item_type.equals("H"))
       {
         
         val hash_node = subcatnode_index.get("name",content).getSingle()
         if(hash_node != null)
         {
           val w = "*"+content+"*"
           events_total = hash_node.getRelationships("Belongs_To_Subcategory_Event","Tag_Of_Event").asScala.map(_.getOtherNode(hash_node)).toList.distinct
           sorted_events = events_total.sortBy(-_.getProperty("time_created").toString().toInt).slice(0,4)
		   petitions_total = hash_node.getRelationships("Belongs_To_Subcategory_Petition","Tag_Of_Petition").asScala.map(_.getOtherNode(hash_node)).toList.distinct
           
           sorted_petitions = petitions_total.sortBy(-_.getProperty("time_created").toString().toInt).slice(0,4)
		   
           total_debates = d_content_index.query("d_content", w ).iterator().asScala.toList.sortBy(-_.getProperty("time_created").toString().toInt)
		   total_townhalls = t_content_index.query("t_content", w ).iterator().asScala.toList.sortBy(-_.getProperty("time_created").toString().toInt)
		   
		   sorted_debates = total_debates.slice(0,4)
	       sorted_townhalls = total_townhalls.slice(0,4) 
		   
         }
       }
       
       else
       {
          //var search_type = 0
          var ww = ""
          val toRemove = "~`!@#$%^&*()_-+=*{}[]:;'|/".toSet
          ww = gummy(content)
          ww = ww.filterNot(toRemove).stripPrefix(" ").stripSuffix(" ").trim
          
          
           val w = "*"+ww+"*"
           
           events_total = event_content_index.query("event_content", w ).iterator().asScala.toList.sortBy(-_.getProperty("time_created").toString().toInt)
		   petitions_total = p_content_index.query("p_content", w ).iterator().asScala.toList.sortBy(-_.getProperty("time_created").toString().toInt)
		   
           total_debates = d_content_index.query("d_content", w ).iterator().asScala.toList.sortBy(-_.getProperty("time_created").toString().toInt)
		   total_townhalls = t_content_index.query("t_content", w ).iterator().asScala.toList.sortBy(-_.getProperty("time_created").toString().toInt)
		   
		   sorted_events = events_total.slice(0,4)
	       sorted_petitions = petitions_total.slice(0,4) 
		   sorted_debates = total_debates.slice(0,4)
	       sorted_townhalls = total_townhalls.slice(0,4) 
            
          
       }
       
       if(!user_name.equals("") && user_node != null)
       {
    	   follows = user_node.getRelationships("Follows",Direction.OUTGOING).asScala.map(_.getOtherNode(user_node)).toList
       }      
       
        var events_left = events_total.size
        var petitions_left = petitions_total.size
        var debates_left = total_debates.size
        var townhalls_left = total_townhalls.size
        var l = List[List[org.neo4j.graphdb.Node]]()
        var out = List[Any]()
	    var ret = List[List[List[org.neo4j.graphdb.Node]]]()
	    
	    val e_list = List("ev","ttl","tid","ctgy","htg","lc","date")
	    val p_list = List("ev","id","ttl","tid","goal","users")
	    val d_list = List("ev","ttl","tid","date")
	    val t_list = List("ev","ttl","tid","celeb","date")
	    val tot_list = List("e","p","d","t")
	    
	    var flag = 0
	    var count = 0
	    
	    l :+= sorted_events
	    l :+= sorted_petitions
	    l :+= sorted_debates
	    l :+= sorted_townhalls
	    
	    ret :+= l
	    ret :+= l
	    ret :+= l
	    ret :+= l
	    
	    for(e <- ret)
	    {
		    for(each <- e)
		    {
		      if(flag < 4)
		      {
			      if(each.size > count)
			      {
			        val ele = each(count)
			        if(sorted_events.contains(ele))
			        {
			          out :+= JSONObject(e_list.zip(List(1,ele.getProperty("event_title"),ele.getProperty("event_title_id"),ele.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(ele)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),ele.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(ele).getProperty("name").toString()).slice(0,1).mkString(","),ele.getProperty("event_location"),ele.getProperty("event_date_time"))).toMap)
			          events_left = events_left - 1
			        }
			        else if(sorted_petitions.contains(ele))
			        {
			          var small_list = List[Any]()
			          small_list :+= 2
			          small_list :+= ele.getProperty("p_id")
			          small_list :+= ele.getProperty("p_title")
			          small_list :+= ele.getProperty("p_title_id")
			          small_list :+= ele.getProperty("p_target").toString().toInt - ele.getProperty("p_count").toString().toInt
			          var user_signed_list = List[String]()
			          var signed_list = ele.getRelationships("Signed_Petition",Direction.INCOMING).asScala.toList.sortBy(x => -x.getProperty("time").toString().toInt).map(_.getOtherNode(ele))
	                  var inter = signed_list.intersect(follows)
	                  
	                  signed_list = (inter:::signed_list).distinct.slice(0,5)
	                  if(signed_list.size > 0 )
		              {
		                    for(each <- signed_list)
				            {
				              user_signed_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
				            }
		              }
			          small_list :+= user_signed_list.mkString(",")
			          out :+= JSONObject(p_list.zip(small_list).toMap)
			          petitions_left = petitions_left - 1
                    }
			        else if(sorted_debates.contains(ele))
			        {
			          out :+= JSONObject(d_list.zip(List(3,ele.getProperty("d_title"),ele.getProperty("d_title_id"),ele.getProperty("d_date"))).toMap)
			          debates_left = debates_left - 1
			        }
			        else
			        {
			          out :+= JSONObject(t_list.zip(List(4,ele.getProperty("t_title"),ele.getProperty("t_title_id"),ele.getProperty("t_content"),ele.getProperty("t_date"))).toMap)
			          townhalls_left = townhalls_left - 1
			        }
			        flag = flag + 1
			      }
			      
		      }
		      
		    }
		    count = count + 1
	    }
       
       out :+= JSONObject(tot_list.zip(List(events_left,petitions_left,debates_left,townhalls_left)).toMap)
       
    JSONArray(out).toString()  
    }
    
    
    def get_leftpane_more(
        user_name:String, // unique user name
        item_type:String,
        item_name:String,
        content:String,
        category:String,
        count:Int,
        prev_cnt:Int
        ) :String =
          
    {
       var main_list = List[Any]()
       val t = (System.currentTimeMillis() /1000).toInt
       val c_time = (System.currentTimeMillis() /1000).toInt - 86400
       val catnode_index = getNodeIndex("category").get
       val cat_node = catnode_index.get("name",category).getSingle()
       val UserIndex = getNodeIndex("user").get
       val PetitionIndex = getNodeIndex("petition").get
       val DebateIndex = getNodeIndex("debate").get
       val TownhallIndex = getNodeIndex("townhall").get
       val event_content_index = getNodeIndex("event_content").get
       val p_content_index = getNodeIndex("petition_content").get
       val d_content_index = getNodeIndex("debate_content").get
       val t_content_index = getNodeIndex("townhall_content").get
       val subcatnode_index = getNodeIndex("sub_category").get
       val user_node = UserIndex.get("id",user_name).getSingle()
       var events_total  = List[org.neo4j.graphdb.Node]()
       var petitions_total  = List[org.neo4j.graphdb.Node]()
       var total_debates  = List[org.neo4j.graphdb.Node]()
       var total_townhalls  = List[org.neo4j.graphdb.Node]()
       var sorted_events  = List[org.neo4j.graphdb.Node]()
       var sorted_petitions  = List[org.neo4j.graphdb.Node]()
       var sorted_debates  = List[org.neo4j.graphdb.Node]()
       var sorted_townhalls  = List[org.neo4j.graphdb.Node]()
       var follows  = List[org.neo4j.graphdb.Node]()
       
         val hash_node = subcatnode_index.get("name",content).getSingle()
         
           val w = "*"+content+"*"
           if(item_name.equals("E"))
           {
	           val e_list = List("ttl","tid","ctgy","htg","lc","date")
	           if(item_type.equals("H"))
	           {
	        	   events_total = hash_node.getRelationships("Belongs_To_Subcategory_Event","Tag_Of_Event").asScala.map(_.getOtherNode(hash_node)).toList.distinct
	           }
	           else
	           {
	              //var search_type = 0
		          var ww = ""
		          val toRemove = "~`!@#$%^&*()_-+=*{}[]:;'|/".toSet
		          ww = gummy(content)
		          ww = ww.filterNot(toRemove).stripPrefix(" ").stripSuffix(" ").trim
		          
		           val w = "*"+ww+"*"
		           
		           events_total = event_content_index.query("event_content", w ).iterator().asScala.toList
				  // sorted_events = events_total.slice(prev_cnt,(prev_cnt+count))
			       
		            
		          
		          
		          
		          
		          
	           }
	           sorted_events = events_total.sortBy(-_.getProperty("time_created").toString().toInt)
			   sorted_events = events_total.slice(prev_cnt,(prev_cnt+count))
			   main_list = sorted_events.map(x => JSONObject(e_list.zip(List(x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getProperty("event_location"),x.getProperty("event_date_time"))).toMap))
           }
           
           else if(item_name.equals("P"))
           {
             val p_list = List("id","ttl","tid","goal","users")
             if(item_type.equals("H"))
             {
            	 petitions_total = hash_node.getRelationships("Belongs_To_Subcategory_Petition","Tag_Of_Petition").asScala.map(_.getOtherNode(hash_node)).toList.distinct
             }
             
             else
	         {
	          //var search_type = 0
	          var ww = ""
	          val toRemove = "~`!@#$%^&*()_-+=*{}[]:;'|/".toSet
	          ww = gummy(content)
	          ww = ww.filterNot(toRemove).stripPrefix(" ").stripSuffix(" ").trim
	          
	          
	           val w = "*"+ww+"*"
	           
	           petitions_total = p_content_index.query("p_content", w ).iterator().asScala.toList
			   //sorted_petitions = petitions_total.slice(prev_cnt,(prev_cnt+count)) 
	            
	          
	         }
             sorted_petitions = petitions_total.sortBy(-_.getProperty("time_created").toString().toInt)
             sorted_petitions = sorted_petitions.slice(prev_cnt,(prev_cnt+count))
             for(ele <- sorted_petitions)
             {
                      var small_list = List[Any]()
			          small_list :+= ele.getProperty("p_id")
			          small_list :+= ele.getProperty("p_title")
			          small_list :+= ele.getProperty("p_title_id")
			          small_list :+= ele.getProperty("p_target")
			          var user_signed_list = List[String]()
			          var signed_list = ele.getRelationships("Signed_Petition",Direction.INCOMING).asScala.toList.sortBy(x => -x.getProperty("time").toString().toInt).map(_.getOtherNode(ele)).slice(0,5)
	                 // var inter = signed_list.intersect(follows)
	                  
	                  //signed_list = (inter:::signed_list).distinct.slice(0,5)
	                  if(signed_list.size > 0 )
		              {
		                    for(each <- signed_list)
				            {
				              user_signed_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
				            }
		              }
			          small_list :+= user_signed_list.mkString(",")
			          main_list :+= JSONObject(p_list.zip(small_list).toMap)
		   
             }
           }
             
           else if(item_name.equals("D"))
           {
	           val d_list = List("ttl","tid","date")
	           if(item_type.equals("H"))
	           {
	        	   total_debates = d_content_index.query("d_content", w ).iterator().asScala.toList.sortBy(-_.getProperty("time_created").toString().toInt)
		       }
	           else
			   {
		          //var search_type = 0
		          var ww = ""
		          val toRemove = "~`!@#$%^&*()_-+=*{}[]:;'|/".toSet
		          ww = gummy(content)
		          ww = ww.filterNot(toRemove).stripPrefix(" ").stripSuffix(" ").trim
		          
		          
		           val w = "*"+ww+"*"
		           
		           total_debates = d_content_index.query("d_content", w ).iterator().asScala.toList.sortBy(-_.getProperty("time_created").toString().toInt)
				   
				   //sorted_debates = total_debates.slice(prev_cnt,(prev_cnt+count))
			       
		          
		     }
	           sorted_debates = total_debates.slice(prev_cnt,(prev_cnt+count))
			   
		       
		       main_list = sorted_debates.map(x => JSONObject(d_list.zip(List(x.getProperty("d_title"),x.getProperty("d_title_id"),x.getProperty("d_date"))).toMap))
       
		   }
           
           else if(item_name.equals("T"))
           {
             
               val t_list = List("ttl","tid","celeb","date")
               if(item_type.equals("H"))
               {
            	   total_townhalls = t_content_index.query("t_content", w ).iterator().asScala.toList.sortBy(-_.getProperty("time_created").toString().toInt)
		       }
               else
               {
		          //var search_type = 0
		          var ww = ""
		          val toRemove = "~`!@#$%^&*()_-+=*{}[]:;'|/".toSet
		          ww = gummy(content)
		          ww = ww.filterNot(toRemove).stripPrefix(" ").stripSuffix(" ").trim
		          
		          
		           val w = "*"+ww+"*"
		           
		           total_townhalls = t_content_index.query("t_content", w ).iterator().asScala.toList.sortBy(-_.getProperty("time_created").toString().toInt)
				   
				   //sorted_townhalls = total_townhalls.slice(prev_cnt,(prev_cnt+count))
			       
		          
		     }
               sorted_townhalls = total_townhalls.slice(prev_cnt,(prev_cnt+count))
		       main_list = sorted_townhalls.map(x => JSONObject(t_list.zip(List(x.getProperty("t_title"),x.getProperty("t_title_id"),x.getProperty("t_content"),x.getProperty("t_date"))).toMap))
       
	           
	           
		   }
           
           
         
       
       JSONArray(main_list).toString()  
    }
    
  // This function gives all events belonging to a category
    def get_more_events_category(
        user_name:String, // unique user name
        category:String,
        event_type:Int // 0 for todayz events, 1 for upcoming events
        
        ) :String =
    {
    
	   var main_list = List[Any]()
       var today_list = List[Any]()
       var upcoming_list = List[Any]()
       val rightNow : Calendar= Calendar.getInstance();
       val date:Date = new Date();
       // offset to add since we're not UTC
       val offset: Long = rightNow.get(Calendar.ZONE_OFFSET) +
       rightNow.get(Calendar.DST_OFFSET);
       val since: Int = ((rightNow.getTimeInMillis() + offset) % (24 * 60 * 60 * 1000)).toInt/1000;
       val till: Int = (24*3600) - since
     
       val t1 = (System.currentTimeMillis()/1000).toInt - since
       val t2 = (System.currentTimeMillis()/1000).toInt + till
       var sorted_events_today  = List[org.neo4j.graphdb.Node]()
       var sorted_events_upcoming  = List[org.neo4j.graphdb.Node]()
       val cur_time = (System.currentTimeMillis() /1000).toInt
       val nodeIndex = getNodeIndex("category").get
       val node = nodeIndex.get("name",category).getSingle()
       val UserIndex = getNodeIndex("user").get
       val user_node = UserIndex.get("id",user_name).getSingle()
       val l = List("E_ID","E_Title","E_Title_ID","E_Category","E_SubCategory","E_Location","E_Time")
       var today_cnt = 0;
       var up_cnt = 0;
       if(node != null && (user_name.equals("") || user_node == null))
	   {
           val events_total = node.getRelationships("Belongs_To_Event_Category",Direction.INCOMING).asScala.map(_.getOtherNode(node)).toList.filter(x => (x.getProperty("event_date_time").toString().toInt > cur_time)  )
           val events_upcoming = events_total.filter(x => (x.getProperty("event_date_time").toString().toInt > t2)  )
		       
           if(event_type == 1)
           {
		       sorted_events_upcoming = events_upcoming.sortBy( x =>   -(((x.getProperty("views").toString().toFloat)+(x.getProperty("weight").toString().toFloat*10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
		       up_cnt = sorted_events_upcoming.size
		       if(events_upcoming != null)
	           {
			       
			       upcoming_list = sorted_events_upcoming.map(x => JSONObject(l.zip(List(x.getProperty("event_id"),x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getProperty("event_location"),x.getProperty("event_date_time"))).toMap))
	           }
           }
 
           else
           {
		       val events_today = events_total.filterNot(x => events_upcoming.contains(x) )
		       sorted_events_today = events_today.sortBy( x =>   -(((x.getProperty("views").toString().toFloat)+(x.getProperty("weight").toString().toFloat*10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
		       today_cnt = sorted_events_today.size
		       if(events_today != null)
	           {
			       
			       today_list = sorted_events_today.map(x => JSONObject(l.zip(List(x.getProperty("event_id"),x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getProperty("event_location"),x.getProperty("event_date_time"))).toMap))
	           }   
           }

       }
       
       else if(node != null && user_node != null)
	   {
	         
	       var loc_events = List[org.neo4j.graphdb.Node]()
	       var loc_events_upcoming = List[org.neo4j.graphdb.Node]()
	       var loc_events_today = List[org.neo4j.graphdb.Node]()
	       
	       
	       if(user_node.hasRelationship("Belongs_To_Location"))
	       {
	         loc_events = user_node.getSingleRelationship("Belongs_To_Location",Direction.OUTGOING).getOtherNode(user_node).getRelationships("Belongs_To_Location_Event").asScala.toList.map(_.getStartNode()).filter(x => (x.getProperty("event_date_time").toString().toInt > cur_time)  )
	         loc_events_upcoming = loc_events.filter(x => (x.getProperty("event_date_time").toString().toInt > t2)  )
	         loc_events_today = loc_events.filterNot(x => loc_events_upcoming.contains(x) )
	       }
	       val events_total = node.getRelationships("Belongs_To_Event_Category",Direction.INCOMING).asScala.map(_.getOtherNode(node)).toList.filter(x => (x.getProperty("event_date_time").toString().toInt > cur_time)  )
           
	       val events_upcoming = events_total.filter(x => (x.getProperty("event_date_time").toString().toInt > t2)  )
	       
	       if(event_type == 1)
	       {
	         sorted_events_upcoming = events_upcoming.sortBy( x =>   -(((x.getProperty("views").toString().toFloat)+(x.getProperty("weight").toString().toFloat*10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
	         up_cnt = sorted_events_upcoming.size
	         if(events_upcoming != null)
             {
		       upcoming_list = (loc_events_upcoming:::sorted_events_upcoming).distinct.map(x => JSONObject(l.zip(List(x.getProperty("event_id"),x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getProperty("event_location"),x.getProperty("event_date_time"))).toMap))
             }
	       }
	       
	       
	       
	       else
	       { 

		       val events_today = events_total.filterNot(x => events_upcoming.contains(x) )
		       sorted_events_today = events_today.sortBy( x =>   -(((x.getProperty("views").toString().toFloat)+(x.getProperty("weight").toString().toFloat*10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
		      
		       today_cnt = sorted_events_today.size
		       if(events_today != null)
	           {
			       
			       today_list = (loc_events_today:::sorted_events_today).distinct.map(x => JSONObject(l.zip(List(x.getProperty("event_id"),x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getProperty("event_location"),x.getProperty("event_date_time"))).toMap))
	           }    
	       }

	   }
       
         var m_list = List[Any]()
         if(event_type == 0)
         {
           
           var today_map = scala.collection.mutable.Map[String, Any]()
           var today_map_cnt = scala.collection.mutable.Map[String, Int]()
           today_map_cnt("Events_Count") = today_cnt
           today_map("Events") = JSONArray(today_list)
           m_list ::= JSONObject(today_map_cnt.toMap)
	       m_list ::= JSONObject(today_map.toMap)
         }
         
         else
         {
             var up_map = scala.collection.mutable.Map[String, Any]()
	         var up_map_cnt = scala.collection.mutable.Map[String, Int]()
	         up_map_cnt("Events_Count") = up_cnt
	         up_map("Events") = JSONArray(upcoming_list)
		    
		     m_list ::= JSONObject(up_map_cnt.toMap)
		     m_list ::= JSONObject(up_map.toMap)
         }
         
         JSONArray(m_list).toString()
  
  }
    
  def get_more_events(
        user_name:String, // unique user name
        category:String) :String =
    {
    
	   var main_list = List[Any]()
	   var events_total  = List[org.neo4j.graphdb.Node]()
       var sorted_events  = List[org.neo4j.graphdb.Node]()
       val t = (System.currentTimeMillis() /1000).toInt
       val cat_nodeIndex = getNodeIndex("category").get
       val cat_node = cat_nodeIndex.get("name",category).getSingle()
       val UserIndex = getNodeIndex("user").get
       val user_node = UserIndex.get("id",user_name).getSingle()
       val e_list = List("ttl","tid","ctgy","htg","lc","date","tot")
       if(cat_node != null && (user_name.equals("") || user_node == null))
	   {
         events_total = cat_node.getRelationships("Belongs_To_Event_Category",Direction.INCOMING).asScala.map(_.getOtherNode(cat_node)).toList.filter(x => (x.getProperty("approved").toString().toInt == 1 && x.getProperty("event_date_time_closing").toString().toInt > t)  )
         sorted_events = events_total.sortBy( x =>   -(((x.getProperty("views").toString().toFloat)+(x.getProperty("weight").toString().toFloat*10))/ ((((t-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    ).slice(0,5)
		 main_list = sorted_events.map(x => JSONObject(e_list.zip(List(x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getProperty("event_location"),x.getProperty("event_date_time"),events_total.size)).toMap))
           
	   }
       
       else if(cat_node != null && user_node != null)
	   {
           var loc_events = List[org.neo4j.graphdb.Node]()
           events_total = cat_node.getRelationships("Belongs_To_Event_Category",Direction.INCOMING).asScala.map(_.getOtherNode(cat_node)).toList.filter(x => (x.getProperty("approved").toString().toInt == 1 && x.getProperty("event_date_time_closing").toString().toInt > t)  )
           
	       if(user_node.hasRelationship("Belongs_To_Location"))
	       {
	         loc_events = user_node.getSingleRelationship("Belongs_To_Location",Direction.OUTGOING).getOtherNode(user_node).getRelationships("Belongs_To_Location_Event").asScala.toList.map(_.getStartNode()).filter(x => (x.getProperty("approved").toString().toInt == 1 && x.getProperty("event_date_time_closing").toString().toInt > t)  ).slice(0,5)
	         loc_events = loc_events.filter(x => events_total.contains(x))
	       }
	       sorted_events = events_total.sortBy( x =>   -(((x.getProperty("views").toString().toFloat)+(x.getProperty("weight").toString().toFloat*10))/ ((((t-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    ).slice(0,5)
		   main_list = (loc_events:::sorted_events).distinct.slice(0,5).map(x => JSONObject(e_list.zip(List(x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getProperty("event_location"),x.getProperty("event_date_time"),events_total.size)).toMap))
         
	   }
       
       JSONArray(main_list).toString()
  
  }
  
  
  
  def get_more_petitions() :String =
  {
    
	   var main_list = List[Any]()
	   val PetitionIndex = getNodeIndex("petition").get
       var sorted_petitions  = List[org.neo4j.graphdb.Node]()
       val t = (System.currentTimeMillis() /1000).toInt
       val p_list = List("id","ttl","tid","goal","tot")
       val petitions_total = PetitionIndex.query("id", "*" ).iterator().asScala.toList.filter(x => (x.getProperty("approved").toString().toInt == 1 && x.getProperty("end_date").toString().toInt > t)  )
       sorted_petitions = petitions_total.sortBy( x =>   -x.getProperty("views").toString().toFloat / ((t-x.getProperty("time_created").toString().toInt)/3600 + 1)   ).slice(0,5)
       main_list = sorted_petitions.map(x => JSONObject(p_list.zip(List(x.getProperty("p_id"),x.getProperty("p_title"),x.getProperty("p_title_id"),x.getProperty("p_target"),petitions_total.size)).toMap))
           
	   JSONArray(main_list).toString()
  
  }
  
  def get_more_debates() :String =
  {
    
	   var main_list = List[Any]()
	   val DebateIndex = getNodeIndex("debate").get
       var sorted_debates  = List[org.neo4j.graphdb.Node]()
       val t = (System.currentTimeMillis() /1000).toInt
       val d_list = List("ttl","tid","date","tot")
       val total_debates = DebateIndex.query("id", "*" ).iterator().asScala.toList.filter(x => (x.getProperty("d_date").toString().toInt > t)  ).sortBy( x => x.getProperty("d_date").toString().toInt)
       sorted_debates = total_debates.slice(0,5)
       main_list = sorted_debates.map(x => JSONObject(d_list.zip(List(x.getProperty("d_title"),x.getProperty("d_title_id"),x.getProperty("d_date"),total_debates.size)).toMap))
           
	   JSONArray(main_list).toString()
  
  }
  
  def get_more_townhalls() :String =
  {
    
	   var main_list = List[Any]()
	   val TownhallIndex = getNodeIndex("townhall").get
       var sorted_townhalls  = List[org.neo4j.graphdb.Node]()
       val t = (System.currentTimeMillis() /1000).toInt
       val t_list = List("ttl","tid","celeb","date","tot")
       val total_townhalls = TownhallIndex.query("id", "*" ).iterator().asScala.toList.filter(x => (x.getProperty("t_date").toString().toInt > t)  ).sortBy( x => x.getProperty("t_date").toString().toInt)
       sorted_townhalls = total_townhalls.slice(0,5)
       main_list = sorted_townhalls.map(x => JSONObject(t_list.zip(List(x.getProperty("t_title"),x.getProperty("t_title_id"),x.getProperty("t_content"),x.getProperty("t_date"),total_townhalls.size)).toMap))
           
	   JSONArray(main_list).toString()
  
  }
  
  
  
  def get_all_events(
        user_name:String, // unique user name
        category:String,
        count:Int,
        prev_cnt:Int) :String =
    {
      
	   var main_list = List[Any]()
       var sorted_events  = List[org.neo4j.graphdb.Node]()
       val t = (System.currentTimeMillis() /1000).toInt
       val cat_nodeIndex = getNodeIndex("category").get
       val cat_node = cat_nodeIndex.get("name",category).getSingle()
       val UserIndex = getNodeIndex("user").get
       val user_node = UserIndex.get("id",user_name).getSingle()
       val e_list = List("ttl","tid","ctgy","htg","lc","date")
       var today_cnt = 0
       var up_cnt = 0
       if(cat_node != null && (user_name.equals("") || user_node == null))
	   {
         val events_total = cat_node.getRelationships("Belongs_To_Event_Category",Direction.INCOMING).asScala.map(_.getOtherNode(cat_node)).toList.filter(x => (x.getProperty("approved").toString().toInt == 1 && x.getProperty("event_date_time_closing").toString().toInt > t)  )
         sorted_events = events_total.sortBy( x =>   -(((x.getProperty("views").toString().toFloat)+(x.getProperty("weight").toString().toFloat*10))/ ((((t-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    ).slice(prev_cnt,(prev_cnt+count))
		 main_list = sorted_events.map(x => JSONObject(e_list.zip(List(x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getProperty("event_location"),x.getProperty("event_date_time"))).toMap))
           
	   }
       
       else if(cat_node != null && user_node != null)
	   {
           var loc_events = List[org.neo4j.graphdb.Node]()
           val events_total = cat_node.getRelationships("Belongs_To_Event_Category",Direction.INCOMING).asScala.map(_.getOtherNode(cat_node)).toList.filter(x => (x.getProperty("approved").toString().toInt == 1 && x.getProperty("event_date_time_closing").toString().toInt > t)  )
           
	       if(user_node.hasRelationship("Belongs_To_Location"))
	       {
	         loc_events = user_node.getSingleRelationship("Belongs_To_Location",Direction.OUTGOING).getOtherNode(user_node).getRelationships("Belongs_To_Location_Event").asScala.toList.map(_.getStartNode()).filter(x => (x.getProperty("approved").toString().toInt == 1 && x.getProperty("event_date_time_closing").toString().toInt > t)  ).sortBy(_.getProperty("event_date_time").toString().toInt)
	         loc_events = loc_events.filter( x => events_total.contains(x))
	       
	       }
	       sorted_events = events_total.sortBy( x =>   -(((x.getProperty("views").toString().toFloat)+(x.getProperty("weight").toString().toFloat*10))/ ((((t-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
		   main_list = (loc_events:::sorted_events).distinct.slice(prev_cnt,(prev_cnt+count)).map(x => JSONObject(e_list.zip(List(x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getProperty("event_location"),x.getProperty("event_date_time"))).toMap))
         
	   }
       
       JSONArray(main_list).toString()
  
  }
  
  def get_all_petitions(count:Int,
        prev_cnt:Int) :String =
  {
    
	   var main_list = List[Any]()
	   val PetitionIndex = getNodeIndex("petition").get
       var sorted_petitions  = List[org.neo4j.graphdb.Node]()
       val t = (System.currentTimeMillis() /1000).toInt
       val c_time = (System.currentTimeMillis() /1000).toInt - 86400
       val p_list = List("id","ttl","tid","goal","users")
       val petitions_total = PetitionIndex.query("id", "*" ).iterator().asScala.toList.filter(x => (x.getProperty("end_date").toString().toInt > t)  )
       //sorted_petitions = petitions_total.sortBy( x =>   -x.getProperty("views").toString().toFloat / ((t-x.getProperty("time_created").toString().toInt)/3600 + 1)   ).slice(prev_cnt,(prev_cnt+count))
       //val follows = user_node.getRelationships("Follows",Direction.OUTGOING).asScala.map(_.getOtherNode(user_node)).toList
	   sorted_petitions = petitions_total.sortBy(-_.getProperty("time_created").toString().toInt).slice(prev_cnt,(prev_cnt+count))
       
       for(ele <- sorted_petitions)
       {
                      var small_list = List[Any]()
			          small_list :+= ele.getProperty("p_id")
			          small_list :+= ele.getProperty("p_title")
			          small_list :+= ele.getProperty("p_title_id")
			          small_list :+= ele.getProperty("p_target").toString().toInt - ele.getProperty("p_count").toString().toInt
			          var user_signed_list = List[String]()
			          var signed_list = ele.getRelationships("Signed_Petition",Direction.INCOMING).asScala.toList.sortBy(x => -x.getProperty("time").toString().toInt).map(_.getOtherNode(ele)).slice(0,5)
	                 // var inter = signed_list.intersect(follows)
	                  
	                  //signed_list = (inter:::signed_list).distinct.slice(0,5)
	                  if(signed_list.size > 0 )
		              {
		                    for(each <- signed_list)
				            {
				              user_signed_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
				            }
		              }
			          small_list :+= user_signed_list.mkString(",")
			          main_list :+= JSONObject(p_list.zip(small_list).toMap)
       }
       JSONArray(main_list).toString()
  
  }
  
  def get_all_debates(count:Int,
        prev_cnt:Int) :String =
  {
    
	   var main_list = List[Any]()
	   val DebateIndex = getNodeIndex("debate").get
       var sorted_debates  = List[org.neo4j.graphdb.Node]()
       val t = (System.currentTimeMillis() /1000).toInt
       val d_list = List("ttl","tid","date")
       val total_debates = DebateIndex.query("id", "*" ).iterator().asScala.toList.filter(x => (x.getProperty("d_date").toString().toInt > t)  ).sortBy( x => x.getProperty("d_date").toString().toInt)
       sorted_debates = total_debates.slice(prev_cnt,(prev_cnt+count))
       main_list = sorted_debates.map(x => JSONObject(d_list.zip(List(x.getProperty("d_title"),x.getProperty("d_title_id"),x.getProperty("d_date"))).toMap))
           
	   JSONArray(main_list).toString()
  
  }
  
  def get_all_townhalls(count:Int,
        prev_cnt:Int) :String =
  {
    
	   var main_list = List[Any]()
	   val TownhallIndex = getNodeIndex("townhall").get
       var sorted_townhalls  = List[org.neo4j.graphdb.Node]()
       val t = (System.currentTimeMillis() /1000).toInt
       val t_list = List("ttl","tid","celeb","date")
       val total_townhalls = TownhallIndex.query("id", "*" ).iterator().asScala.toList.filter(x => (x.getProperty("t_date").toString().toInt > t)  ).sortBy( x => x.getProperty("t_date").toString().toInt)
       sorted_townhalls = total_townhalls.slice(prev_cnt,(prev_cnt+count))
       main_list = sorted_townhalls.map(x => JSONObject(t_list.zip(List(x.getProperty("t_title"),x.getProperty("t_title_id"),x.getProperty("t_content"),x.getProperty("t_date"))).toMap))
           
	   JSONArray(main_list).toString()
  
  }
  
  // Triggered when a moderator reviews an event
  def review_event(
      event_id: String, // unique event id
      user_name : String, // unique user name( moderator user name)
      result: Int, // 1 if event is okay , 0 is event is not okay
      time: Int
      ):String=
  {
    
    withTx {
    implicit neo =>
      val EventIndex = getNodeIndex("event").get
      val event_node = EventIndex.get("id",event_id).getSingle()
      val UserIndex = getNodeIndex("user").get
      val user_node = UserIndex.get("id",user_name).getSingle()
      var ret = ""
      if(event_node != null && user_node != null)
      {
        if(!event_node.hasRelationship("Reviewed_By",Direction.OUTGOING))
	    {
	        if(result == 1)
	        {
	          val rels = event_node.getRelationships("User_Spammed_Of_Event_Category","System_Spammed_Of_Event_Category").asScala
	          for(each <- rels)
	          {
	            each.delete()
	          }
	
	          event_node.setProperty("spam_weight",0)
	          val rel: org.neo4j.graphdb.Relationship = event_node --> "Reviewed_By" --> user_node <
	          val rel_time = rel.setProperty("time", time)
	          ret = "Accepted"
	        }
	        else
	        {
	          ret = "Rejected"
	          val author_node = event_node.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode()
	          val author_spam_wt = author_node.getProperty("spam_weight").toString().toInt
	          author_node.setProperty("spam_weight",author_spam_wt + 1)
	          delete_event(event_id)
	          
	          
	        }
	    }
        
        else
        {
          val mod_node = event_node.getSingleRelationship("Reviewed_By",Direction.OUTGOING).getEndNode()
          ret = "No_Action" + "," + mod_node.getProperty("user_name")
        }
      
      }
    ret 
    }
  }
  
  
  // This function returns all the unreviewed events in a given category
  def get_events_spammed(
      category:String,
      cnt:Int,  // No of events required
      prev_cnt:Int  // No of events sent
      ) :String =
  {
    
    
     var list  = List[Any]()
     val t = (System.currentTimeMillis()/1000).toInt 
     val nodeIndex = getNodeIndex("category").get
     val node = nodeIndex.get("name",category).getSingle()
     val avg_wt_index = getNodeIndex("avg_weights").get
     val avg_wt_node = avg_wt_index.get("id","event").getSingle()
     val avg_wt = avg_wt_node.getProperty("average").toString().toInt
     if(node != null) //new articles first
     {

       val events = node.getRelationships("User_Spammed_Of_Event_Category").asScala.map(_.getOtherNode(node)).toList.filter(x => ((x.getProperty("spam_weight").toString().toInt >= avg_wt) && (x.getProperty("event_date_time_closing").toString().toInt > t))).sortBy(-_.getProperty("spam_weight").toString.toInt).slice(prev_cnt,(prev_cnt+cnt))
       if(events != null)
       {
	       val l = List("E_ID","E_Title","E_Title_ID","E_Category","E_SubCategory","E_Location","E_Time")
           list = events.map(x => JSONObject(l.zip(List(x.getProperty("event_id"),x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getProperty("event_location"),x.getProperty("event_date_time"))).toMap))
	   }   

     }
     JSONArray(list).toString()

  
  }

  
  // This function is used to filter the most common words during search and suggestions
  def gummy(word: String): String = 
  {
    
    val common_words = List("other", "tried", "become", "therefore", "ending", "about", "less", "they’ve",
        "group", "really", "put", "finds", "whereby", "yours", "into", "z", "ignored", "much", "weren't",
        "getting", "years", "seemed", "outside", "furthers", "yes", "got", "it", "did", "theres",
        "while", "newer", "presented", "cant", "ought", "won’t", "her", "indicated", "they'll", "meanwhile",
        "differ", "up", "nevertheless", "let", "today", "he's", "too", "neither", "hither", "c", "sup", "the",
        "clear", "which", "mustn't", "its", "tends", "knew", "they", "concerning", "would", "seriously", "ends",
        "hasn't", "same", "our", "either", "older", "evenly", "inasmuch", "going", "could", "they'd", "let’s",
        "show", "ours", "que", "only", "t", "how", "lately", "ever", "et", "m", "taken", "itself", "weren’t",
        "every", "thereafter", "whether", "they’re", "selves", "parting", "seven", "whatever", "know", "behind",
        "allows", "they’d", "without", "wherever", "though", "myself", "year", "accordingly", "indeed", "y",
        "wasn’t", "can’t", "first", "third", "been", "co", "sensible", "wasn't", "differently", "under", "soon",
        "you’ve", "mainly", "making", "useful", "inc", "f", "it’s", "who", "certain", "who's", "relatively",
        "some", "you", "want", "then", "sees", "p", "perhaps", "places", "followed", "becomes", "asks", "that's",
        "downed", "who’s", "say", "different", "th", "take", "they've", "however", "considering", "except",
        "point", "ordering", "downs", "in", "entirely", "believe", "latterly", "must", "whose", "it’ll",
        "happens", "him", "made", "beings", "wouldn’t", "doesn't", "ie", "namely", "therein", "you'd", "sub",
        "needed", "next", "inward", "i’m", "aren't", "r", "thanks", "does", "had", "normally", "sure", "anyways",
        "among", "done", "currently", "asking", "still", "out", "course", "he’s", "it’d", "instead", "whither",
        "doesn’t", "obviously", "thence", "we’ve", "above", "high", "don’t", "there’s", "ways", "novel", "will",
        "is", "hello", "we’re", "set", "he'd", "enough", "merely", "rather", "pointed", "works", "viz", "saw", "old",
        "once", "until", "let's", "were", "kind", "presumably", "but", "wish", "regards", "am", "com", "like", "contain",
        "hasn’t", "what's", "wonder", "your", "described", "welcome", "couldn’t", "we're", "she'd", "cause", "says",
        "wanting", "twice", "e", "wherein", "serious", "again", "plus", "afterwards", "big", "use", "l", "noone",
        "you’d", "upon", "c’mon", "quite", "consequently", "at", "herein", "isn't", "especially", "greetings",
        "seeming", "here", "something", "across", "aside", "around", "unfortunately", "needing", "theirs", "nobody",
        "new", "that", "respectively", "where", "are", "causes", "be", "mean", "indicates", "grouping", "turns",
        "placed", "couldn't", "probably", "near", "tries", "hereby", "shows", "changes", "from", "such", "having",
        "six", "always", "face", "awfully", "vs", "ones", "yourselves", "area", "needs", "you’re", "none", "latter",
        "thru", "furthering", "presents", "little", "we'll", "q", "being", "where's", "longer", "inner", "best", "former",
        "comes", "zero", "good", "have", "hopefully", "yourself", "wells", "whereafter", "w", "further", "points",
        "although", "available", "wouldn't", "look", "presenting", "i've", "everywhere", "specifying", "when", "over",
        "fully", "he", "here's", "never", "also", "h", "thorough", "whole", "opened", "think", "men", "not", "seem",
        "howbeit", "said", "along", "any", "right", "seeing", "if", "ended", "four", "me", "goes", "might", "together",
        "j", "using", "later", "back", "qv", "furthermore", "everybody", "overall", "last", "see", "according", "showing",
        "gets", "self", "un", "s", "hereupon", "she", "began", "kept", "we’d", "now", "corresponding", "all", "i'll",
        "that’s", "sometime", "keep", "appreciate", "whence", "moreover", "should", "opens", "looking", "there's",
        "they’ll", "after", "grouped", "showed", "full", "through", "tell", "give", "unto", "became", "elsewhere",
        "what", "thanx", "whereas", "you’ll", "likely", "eg", "backed", "away", "whereupon", "provides", "you're",
        "gives", "numbers", "please", "able", "used", "as", "ok", "backs", "thank", "rd", "than", "d", "alone",
        "willing", "has", "hers", "own", "hence", "etc", "thinks", "since", "so", "don't", "anyhow", "mostly",
        "haven’t", "five", "three", "various", "she's", "liked", "turn", "we'd", "order", "work", "edu", "everything",
        "i'm", "between", "he'll", "another", "go", "it's", "brief", "this", "just", "despite", "several", "man",
        "besides", "these", "thats", "hadn’t", "anyone", "downing", "worked", "v", "certainly", "seen", "o", "somebody",
        "sorry", "two", "i'd", "their", "open", "nd", "felt", "came", "we", "general", "someone", "thoroughly", "was",
        "make", "anywhere", "immediate", "often", "looks", "both", "orders", "groups", "appear", "far", "very", "generally",
        "largely", "doing", "there", "cases", "himself", "gone", "apart", "faces", "turned", "maybe", "becoming", "ltd",
        "c’s", "one", "latest", "necessary", "turning", "exactly", "i’ve", "because", "parts", "few", "via", "consider",
        "ordered", "things", "parted", "down", "associated", "known", "non", "appropriate", "help", "unless", "early",
        "i", "long", "well", "oh", "furthered", "toward", "possible", "haven't", "what’s", "during", "contains", "nothing",
        "off", "hadn't", "specify", "okay", "hi", "re", "containing", "following", "k", "whenever", "clearly", "specified",
        "anyway", "throughout", "least", "name", "somewhat", "place", "shall", "sometimes", "anybody", "you'll", "need",
        "or", "nine", "didn’t", "saying", "wants", "i’ll", "here’s", "puts", "reasonably", "regardless", "we've", "lest",
        "and", "backing", "usually", "towards", "unlikely", "somewhere", "uses", "thing", "gave", "nowhere", "forth", "whom",
        "get", "follows", "thereupon", "went", "sent", "trying", "can't", "you've", "anything", "ex", "secondly", "no",
        "find", "can", "beside", "fifth", "b", "ask", "formerly", "hereafter", "allow", "truly", "come", "below",
        "beforehand", "otherwise", "u", "on", "us", "thus", "ourselves", "his", "gotten", "onto", "why", "when's",
        "n", "won't", "themselves", "a", "amongst", "before", "try", "by", "nearly", "lets", "g", "number", "per",
        "indicate", "working", "we’ll", "particularly", "mr", "isn’t", "asked", "side", "mrs", "of", "second", "my",
        "a’s", "end", "most", "case", "took", "opening", "those", "already", "eight", "seems", "with", "given", "more",
        "insofar", "value", "i’d", "each", "particular", "how's", "part", "areas", "example", "cannot", "them", "definitely",
        "somehow", "whoever", "within", "for", "do", "everyone", "almost", "didn't", "sides", "shan't", "t’s", "keeps",
        "others", "regarding", "x", "aren’t", "else", "actually", "knows", "thereby", "herself", "ain’t", "downwards",
        "against", "she'll", "to", "nor", "better", "may", "shouldn’t", "hardly", "why's", "even", "yet", "they're",
        "way", "many", "beyond", "an", "where’s", "shouldn't", "help", "join", "joined")


    val it = word.split(" ").toList
    val f = it.filter(x => common_words.contains(x.toLowerCase())==false && !x.equals(""))
    f.mkString(" ")
    
  }

}