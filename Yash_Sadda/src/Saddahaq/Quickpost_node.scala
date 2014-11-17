package Saddahaq
//import sys.ShutdownHookThread
import org.neo4j.scala.{TypedTraverser, SingletonEmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}
import org.neo4j.scala.Neo4jIndexProvider
import collection.JavaConverters._
import org.neo4j.graphdb.Direction
import org.neo4j.index.lucene.ValueContext
import scala.util.parsing.json.JSONObject
import edu.stanford.nlp.tagger.maxent.MaxentTagger
import scala.util.parsing.json.JSON
import scala.util.control.Breaks
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement
import java.sql.ResultSet
import org.jsoup.Jsoup
import java.text.SimpleDateFormat


case class quickpost(val qp_id: String,
    val qp_content: String,
    val time_created: Int,
    val weight: Int,
    val spam_weight: Int
    )
    
case class debate_suggestion(val d_id: String,
    val d_topic: String,
    val time_created: Int,
    val weight: Int
    )
    
case class townhall_suggestion(val t_id: String,
    val t_topic: String,
    val t_celebrity: String,
    val time_created: Int,
    val weight: Int
    )

case class townhall(
    val t_id: String,
    val t_title: String,
    val t_title_id: String,
    val t_content: String,
    val t_img_url: String,
    val t_date: Int,
    val t_duration: Int,
    val weight: Int,
    val time_created: Int,
    val approved: Int,
    val head: Int,
    val space: Int
    )
    
case class petition(
    val p_type: String,
    val p_id: String,
    val p_title: String,
    val p_title_id: String,
    val p_content: String,
    val p_img_url: String,
    val p_to: String,
    val p_target: Int,
    val p_count: Int,
    val time_created: Int,
    val end_date: Int,
    val weight: Int,
    val spam_weight: Int,
    val views: Int,
    val latest_views: String,
    val approved: Int,
    val head: Int,
    val space: Int
    )
    
 case class debate(
    val d_id: String,
    val d_title: String,
    val d_title_id: String,
    val d_img_url: String,
    val d_content: String,
    val d_criteria: String,
    val d_duration: Int,
    val d_date: Int,
    val time_created: Int,
    val weight: Int,
    val spam_weight: Int,
    val views: Int,
    val approved: Int,
    val head: Int,
    val space: Int
    )

    
trait Quickpost_node extends Neo4jWrapper with SingletonEmbeddedGraphDatabaseServiceProvider with TypedTraverser with Neo4jIndexProvider{

  
def debate_townhall_suggestion(item_type: String,
      user_name: String,  // unique user name
      item_id: String,  // unique quickpost id
      item_topic: String,
      item_celebrity: String,
      item_time_created: Int):Boolean =       
  {
    
   
    withTx {
    implicit neo =>
      
	      var i_type = "debate_suggestion"
	      if(item_type.equals("T"))
	      {
	        i_type = "townhall_suggestion"
	      }
       
          val user_weight_index = getNodeIndex("user_weight").get
          val user_index = getNodeIndex("user").get
          val user_node = user_index.get("id",user_name).getSingle()
          val index = getNodeIndex(i_type).get
          var item_node = index.get("id",item_id).getSingle()
          var ret = false
          if(user_node != null && item_node == null)
	      {
              user_node.setProperty("last_seen",item_time_created)
	          val u_wt = user_node.getProperty("weight").toString().toInt
	          val i_wt =Math.round(u_wt.asInstanceOf[Float]/300)
	          if(item_type.equals("D"))
	          {
	            item_node = createNode(debate_suggestion(item_id,item_topic,item_time_created,i_wt))
	            var rel: org.neo4j.graphdb.Relationship = item_node --> "Debate_Suggestion_Written_By" --> user_node < //relating quickpost node and user node
	            var rel_time = rel.setProperty("time", item_time_created)
	            var rel_in_wt = rel.setProperty("in_weight", 3)
	            var rel_out_wt = rel.setProperty("out_weight", i_wt)
	            ret = true
	          }
	          
	          else
	          {
	            item_node = createNode(townhall_suggestion(item_id,item_topic,item_celebrity,item_time_created,i_wt))
	            var rel: org.neo4j.graphdb.Relationship = item_node --> "Townhall_Suggestion_Written_By" --> user_node < //relating quickpost node and user node
	            var rel_time = rel.setProperty("time", item_time_created)
	            var rel_in_wt = rel.setProperty("in_weight", 3)
	            var rel_out_wt = rel.setProperty("out_weight", i_wt)
	            ret = true
	          }
	          
	          index += (item_node,"id",item_id)
	          user_node.setProperty("weight",u_wt+3)
	          user_weight_index.remove(user_node)
	          user_weight_index += (user_node,"weight",new ValueContext( u_wt+3).indexNumeric())
	          
	      }
    ret
      
    }
  }

  def delete_debate_townhall_suggestion(item_type: String,id:String):Boolean =       
  {
    
   
    withTx {
    implicit neo =>
      
	      var i_type = "debate_suggestion"
	      var rel_name = "Debate_Suggestion_Written_By"
	      if(item_type.equals("T"))
	      {
	        i_type = "townhall_suggestion"
	        rel_name = "Townhall_Suggestion_Written_By"
	      }
       
          val user_weight_index = getNodeIndex("user_weight").get
          val index = getNodeIndex(i_type).get
          var item_node = index.get("id",id).getSingle()
          var ret = false
          if(item_node != null)
	      {
            
           val rels = item_node.getRelationships().asScala
	       val i_wt = item_node.getProperty("weight").toString().toInt
	       
	       val author_node = item_node.getSingleRelationship(rel_name,Direction.OUTGOING).getOtherNode(item_node)
		   var rel = item_node.getSingleRelationship(rel_name,Direction.OUTGOING)
		   var del_wt = rel.getProperty("out_weight").toString().toInt
	       val au_wt = author_node.getProperty("weight").toString().toInt
		   author_node.setProperty("weight",au_wt-(i_wt+3-del_wt))
		   user_weight_index.remove(author_node)
		   user_weight_index += (author_node,"weight",new ValueContext( au_wt - 3 ).indexNumeric())
		         
		   rel.delete()        
	       index.remove(item_node)
	       item_node.delete()
	       ret = true
            
            
	      }
    ret
      
    }
  }

  def debate_townhall_suggestion_voteup(item_type: String,
      user_name: String,  // unique user name
      item_id: String):Boolean =       
  {
    
   
    withTx {
    implicit neo =>
      
	      var i_type = "debate_suggestion"
	      if(item_type.equals("T"))
	      {
	        i_type = "townhall_suggestion"
	      }
       
          //val user_weight_index = getNodeIndex("user_weight").get
          val user_index = getNodeIndex("user").get
          val user_node = user_index.get("id",user_name).getSingle()
          val index = getNodeIndex(i_type).get
          var item_node = index.get("id",item_id).getSingle()
          var ret = false
          if(user_node != null && item_node != null)
	      {
	          val u_wt = user_node.getProperty("weight").toString().toInt
	          val i_wt = item_node.getProperty("weight").toString().toInt
	          val weight = Math.round(u_wt.asInstanceOf[Float]/300)
	          item_node.setProperty("weight",i_wt + u_wt)
	          ret = true
	          
	      }
    ret
      
    }
  }
  


def create_townhall(user_name: String,  // unique user name
      t_id: String,  // unique quickpost id
      t_title: String,
      t_title_id: String,
      t_content: String,
      t_img_url: String,
      t_date: Int,
      t_duration: Int,
      t_time_created: Int,
      t_celeb: String,
      t_moderators: String,
      t_subcat: String,
      t_hashtags: String,
      is_edit: Int,
      is_closed: Int):Boolean =       
  {
    
   
    withTx {
    implicit neo =>
     
          var ret = false
          if(is_edit == 0)
          {
             var is_space = is_closed
              val TilesIndex = getNodeIndex("tiles").get
	          val user_weight_index = getNodeIndex("user_weight").get
	          val hash_weight_index = getNodeIndex("hash_weight").get
	          val user_index = getNodeIndex("user").get
	          val user_node = user_index.get("id",user_name).getSingle()
	          val celeb_node = user_index.get("id",t_celeb).getSingle()
	          val t_index = getNodeIndex("townhall").get
	          var t_node = t_index.get("id",t_id).getSingle()
	          
	          if(celeb_node != null && user_node != null && t_node == null)
		      {
	              user_node.setProperty("last_seen",t_time_created)
		          val u_wt = user_node.getProperty("weight").toString().toInt
		          val t_wt =Math.round(u_wt.asInstanceOf[Float]/300)
		          t_node = createNode(townhall(t_id,t_title,t_title_id,t_content,t_img_url,t_date,t_duration,t_wt,t_time_created,0,0,is_space))
		     
		          
		          val t_content_index = getNodeIndex("townhall_content").get
		          t_index += (t_node,"id",t_id)
		          var h_list = t_subcat.split(",").toList.map( x => x.toLowerCase())
		          val t_con = dummy(t_title+ " " +t_content +" " + h_list.mkString(" ") + " "+ celeb_node.getProperty("first_name")+ " " + celeb_node.getProperty("last_name"))
		          t_node.setProperty("t_content", t_con)
		          t_content_index += (t_node,"t_content",t_con)
		          user_node.setProperty("weight",u_wt+5)
		          user_weight_index.remove(user_node)
		          user_weight_index += (user_node,"weight",new ValueContext( u_wt+5).indexNumeric())
		          var rel: org.neo4j.graphdb.Relationship = t_node --> "Townhall_Written_By" --> user_node < //relating quickpost node and user node
		          var rel_time = rel.setProperty("time", t_time_created)
		          var rel_in_wt = rel.setProperty("in_weight", 5)
		          var rel_out_wt = rel.setProperty("out_weight", t_wt)
		          
		          var rel1: org.neo4j.graphdb.Relationship = t_node --> "Townhall_Of" --> celeb_node < //relating quickpost node and user node
		          var rel_time1 = rel1.setProperty("time", t_time_created)
		          if(!t_moderators.equals(""))
		          {
		            val mod_list = t_moderators.split(",").toList.map(x => user_index.get("id",x).getSingle()).filter( y => y != null)
		            for(each <- mod_list)
		            {
		              var rel: org.neo4j.graphdb.Relationship = t_node --> "Townhall_Moderated_By" --> each < //relating quickpost node and user node
		              var rel_time = rel.setProperty("time", t_time_created)
		            }
		          }
		          
		          var cats = List[String]()
		          cats ::= "all"
		          if(is_space == 0)
		          {
			          for(each <- cats)
					     {
					       var news_node = TilesIndex.get("id",each).getSingle()
					       if(news_node == null)
					       {
					         val tiles_node =  createNode(tiles(each,t_id))
					         TilesIndex += (tiles_node,"id",each)
					       }
					       else
					       {
					         
						     var sorted_news  = List[org.neo4j.graphdb.Node]()
						     val news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     news_list ::= t_id
						     val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				             news_node.setProperty("value",news_list_trim)
					      
						    
					       }
					     }
		          }
		          val SubcatIndex = getNodeIndex("sub_category").get
		          var tot_tags = List[ org.neo4j.graphdb.Node]()
	              if(t_subcat != "")
		          {
		              val hash_tags = t_subcat.split(",").toList.filterNot( x => x.equals("")).map( x => x.toLowerCase())
		              val main_hash = hash_tags(0)
		              for(each <- hash_tags)
		              {
			              if(SubcatIndex.get("name",each).getSingle() != null)
			              {
			                
			                val subcat_node = SubcatIndex.get("name",each).getSingle()
			                tot_tags ::= subcat_node
			                val h_wt = subcat_node.getProperty("weight").toString().toInt
			                subcat_node.setProperty("weight",(h_wt + t_wt))
			                if(each.equals(main_hash))
			                {
				                rel = t_node --> "Belongs_To_Subcategory_Townhall" --> subcat_node <
				                var rel_main = rel.setProperty("main", 1)
				                var rel_time = rel.setProperty("time", t_time_created)
				                var rel_in_wt = rel.setProperty("in_weight", t_wt)
				                var rel_out_wt = rel.setProperty("out_weight", 0)
			                }
			                else
			                {
				                rel = t_node --> "Belongs_To_Subcategory_Townhall" --> subcat_node <
				                var rel_time = rel.setProperty("time", t_time_created)
				                var rel_in_wt = rel.setProperty("in_weight", t_wt)
				                var rel_out_wt = rel.setProperty("out_weight", 0)
			                }
			                hash_weight_index.remove(subcat_node)
			                hash_weight_index += (subcat_node,"weight",new ValueContext( h_wt + t_wt ).indexNumeric())
			              }
			              else
			              {
			                val subcat_node = createNode(sub_category(each,t_time_created,t_wt))
			                SubcatIndex += (subcat_node,"name",each)
			                if(each.equals(main_hash))
			                {
				                rel = t_node --> "Belongs_To_Subcategory_Townhall" --> subcat_node <
				                var rel_main = rel.setProperty("main", 1)
				                var rel_time = rel.setProperty("time", t_time_created)
				                var rel_in_wt = rel.setProperty("in_weight", t_wt)
				                var rel_out_wt = rel.setProperty("out_weight", 0)
			                }
			                else
			                {
				                rel = t_node --> "Belongs_To_Subcategory_Townhall" --> subcat_node <
				                var rel_time = rel.setProperty("time", t_time_created)
				                var rel_in_wt = rel.setProperty("in_weight", t_wt)
				                var rel_out_wt = rel.setProperty("out_weight", 0)
			                }
			                
			                rel = subcat_node --> "Hashtag_Created_By" --> user_node <
			                var rel_time1 = rel.setProperty("time", t_time_created)
			                
			                hash_weight_index += (subcat_node,"weight",new ValueContext( t_wt).indexNumeric())
			                tot_tags ::= subcat_node
			              }
		              }
		            }
		          
		            if(t_hashtags != "")
			          {
			            val t_tags = t_hashtags.split(",").distinct.toList.filterNot( x => x.equals("")).map( x => x.toLowerCase())
			            for(tag_name <- t_tags)
			            {
			              
			              if(tag_name != "")
			              {
			                
			                if(SubcatIndex.get("name",tag_name).getSingle() != null)
			                {
			                  val tag_node = SubcatIndex.get("name",tag_name).getSingle()
			                  tot_tags ::= tag_node
			                  val h_wt = tag_node.getProperty("weight").toString().toInt
			                  tag_node.setProperty("weight",(h_wt + 10))
			                  rel = tag_node --> "Tag_Of_Townhall" --> t_node <
			                  var rel_time = rel.setProperty("time", t_time_created)
			                  var rel_in_wt = rel.setProperty("in_weight", 0)
			                  var rel_out_wt = rel.setProperty("out_weight", 10)
			                  hash_weight_index.remove(tag_node)
			                  hash_weight_index += (tag_node,"weight",new ValueContext( h_wt + 10 ).indexNumeric())
			                  
			                }
			                else
			                {
			                  val tag_node = createNode(sub_category(tag_name,t_time_created,10))
			                  SubcatIndex += (tag_node,"name",tag_name)
			                  rel = tag_node --> "Tag_Of_Townhall" --> t_node <
			                  var rel_time = rel.setProperty("time", t_time_created)
			                  var rel_in_wt = rel.setProperty("in_weight", 0)
			                  var rel_out_wt = rel.setProperty("out_weight", 10)
			                  hash_weight_index += (tag_node,"weight",new ValueContext( 10 ).indexNumeric())
			                  rel = tag_node --> "Hashtag_Created_By" --> user_node <
			                  var rel_time1 = rel.setProperty("time", t_time_created)
			                  tot_tags ::= tag_node
			                  
			                }
			              }
			            }
			          
				      }
			          var flag_list = List[org.neo4j.graphdb.Node]()
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
			      featured_item_q("T",t_id)
		          ret = true
		          
		      }
          }
          
          else
          {
            
            val TilesIndex = getNodeIndex("tiles").get
       val FeaturedTilesIndex = getNodeIndex("featured_tiles").get
       val HeadlinesIndex = getNodeIndex("headlines").get
	   
            val user_weight_index = getNodeIndex("user_weight").get
          val user_index = getNodeIndex("user").get
          val user_node = user_index.get("id",user_name).getSingle()
          val celeb_node = user_index.get("id",t_celeb).getSingle()
          val t_index = getNodeIndex("townhall").get
          var t_node = t_index.get("id",t_id).getSingle()
          val hash_weight_index = getNodeIndex("hash_weight").get
          //var ret = false
          if(celeb_node != null && user_node != null && t_node != null)
	      {
            val feu = t_node.getProperty("approved").toString().toInt
            val head = t_node.getProperty("head").toString().toInt
            val space = t_node.getProperty("space").toString().toInt
              val time = t_node.getProperty("time_created").toString().toInt
              val t_wt = t_node.getProperty("weight").toString().toInt
              user_node.setProperty("last_seen",t_time_created)
	          t_node.setProperty("t_title",t_title)
	          t_node.setProperty("t_title_id",t_title_id)
	          t_node.setProperty("t_content",t_content)
	          t_node.setProperty("t_img_url",t_img_url)
	          t_node.setProperty("t_date",t_date)
	          t_node.setProperty("t_duration",t_duration)
	          val t_content_index = getNodeIndex("townhall_content").get
	          t_content_index.remove(t_node)
	          
	          
	                     var news_node1 = TilesIndex.get("id","all").getSingle()
	                     if(news_node1 != null)
				         {
					         var news = news_node1.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     if(news_list.contains(t_id))
					         {
					           news_list = news_list - t_id
					           val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				               news_node1.setProperty("value",news_list_trim)
					         }
				         }
				         
				         news_node1 = FeaturedTilesIndex.get("id","all").getSingle()
				         if(news_node1 != null)
				         {
					         var news = news_node1.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     if(news_list.contains(t_id))
					         {
					           news_list = news_list - t_id
					           val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				               news_node1.setProperty("value",news_list_trim)
					         }
				         }
				         
				         news_node1 = HeadlinesIndex.get("id","all").getSingle()
				         if(news_node1 != null)
				         {
					         var news = news_node1.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     if(news_list.contains(t_id))
					         {
					           news_list = news_list - t_id
					           val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				               news_node1.setProperty("value",news_list_trim)
					         }
				         }
	          
	          var h_list = t_subcat.split(",").toList.map( x => x.toLowerCase())
		          val t_con = dummy(t_title+ " " +t_content +" " + h_list.mkString(" ") + " "+ celeb_node.getProperty("first_name")+ " " + celeb_node.getProperty("last_name"))
		          t_node.setProperty("t_content", t_con)
		          
	          t_content_index += (t_node,"t_content",t_con)
	          
	          val rel = t_node.getSingleRelationship("Townhall_Of",Direction.OUTGOING)
	          rel.delete()
	          
	          var rel1: org.neo4j.graphdb.Relationship = t_node --> "Townhall_Of" --> celeb_node < //relating quickpost node and user node
	          var rel_time1 = rel1.setProperty("time", t_time_created)
	          
	          if(!t_moderators.equals(""))
	          {
	            val prev_mod_rels = t_node.getRelationships("Townhall_Moderated_By").asScala.toList
	            val prev_mods = t_node.getRelationships("Townhall_Moderated_By").asScala.toList.map( x => x.getEndNode())
	            var mod_list = t_moderators.split(",").toList.map(x => user_index.get("id",x).getSingle()).filter( y => y != null)
	            
	            for(each <- prev_mod_rels)
	            {
	              if(!mod_list.contains(each.getEndNode()))
	              {
	                each.delete()
	              }
	            }
	            
	            mod_list = mod_list.filterNot(x => prev_mods.contains(x))
	            for(each <- mod_list)
	            {
	              var rel: org.neo4j.graphdb.Relationship = t_node --> "Townhall_Moderated_By" --> each < //relating quickpost node and user node
	              var rel_time = rel.setProperty("time", t_time_created)
	            }
	          }
	          
	           val SubcatIndex = getNodeIndex("sub_category").get
	           val tags_list = t_hashtags.split(",").distinct.toList.filterNot( x => x.equals("")).map( x => x.toLowerCase())
		       var old_tags = List[Any]()
		       var rels = t_node.getRelationships("Tag_Of_Townhall").asScala
		       for(each <- rels)
		       {
		           val tag = each.getOtherNode(t_node).getProperty("name").toString()
		           old_tags ::= tag
		           if(!tags_list.contains(tag))
		           {
		             val tag_node = each.getOtherNode(t_node)
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
		                  var rel = tag_node --> "Tag_Of_Townhall" --> t_node <
		                  var rel_time = rel.setProperty("time", t_time_created)
		                  var rel_in_wt = rel.setProperty("in_weight", 0)
		                  var rel_out_wt = rel.setProperty("out_weight", 10)
		                  hash_weight_index.remove(tag_node)
		                  hash_weight_index += (tag_node,"weight",new ValueContext( h_wt + 10 ).indexNumeric())
		                  
		                }
		                else
		                {
		                  val tag_node = createNode(sub_category(tag_name,t_time_created,10))
		                  SubcatIndex += (tag_node,"name",tag_name)
		                  var rel = tag_node --> "Tag_Of_Townhall" --> t_node <
		                  var rel_time = rel.setProperty("time", t_time_created)
		                  var rel_in_wt = rel.setProperty("in_weight", 0)
		                  var rel_out_wt = rel.setProperty("out_weight", 10)
		                  rel = tag_node --> "Hashtag_Created_By" --> user_node <
		                  var rel_time1 = rel.setProperty("time", time)
		                  hash_weight_index += (tag_node,"weight",new ValueContext( 10 ).indexNumeric())
		                  
		                }
		              }
		         }
		       }
		       val cur_time = (System.currentTimeMillis() /1000).toInt
		               var news_node = TilesIndex.get("id","all").getSingle()
		               if(space == 0)
		               {
					       if(news_node == null)
					       {
					         val tiles_node =  createNode(tiles("all",t_id))
					         TilesIndex += (tiles_node,"id","all")
					       }
					       else
					       {
					         
						     var sorted_news  = List[org.neo4j.graphdb.Node]()
						     val news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     if( (t_date + 3600) > cur_time)
						     {
						    	 news_list ::= t_id
						     }
						     else
						     {
						       news_list :+= t_id
						     }
						     val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				             news_node.setProperty("value",news_list_trim)
					       }
		               }
	    	 		   
	    	 		   if(feu == 1)
		    	 	   {
			    	 	   news_node = FeaturedTilesIndex.get("id","all").getSingle()
					       if(news_node == null)
					       {
					         val tiles_node =  createNode(featured_tiles("all",t_id))
					         FeaturedTilesIndex += (tiles_node,"id","all")
					       }
					       else
					       {
					         
						     var sorted_news  = List[org.neo4j.graphdb.Node]()
						     val news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     if( (t_date + 3600) > cur_time)
						     {
						    	 news_list ::= t_id
						     }
						     else
						     {
						       news_list :+= t_id
						     }
						     val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				             news_node.setProperty("value",news_list_trim)
					      
						    
					       }
		    	 	   }
	    	 		   
	    	 		   if(head == 1)
		    	 	   {
			    	 	   news_node = HeadlinesIndex.get("id","all").getSingle()
					       if(news_node == null)
					       {
					         val tiles_node =  createNode(headlines("all",t_id))
					         HeadlinesIndex += (tiles_node,"id","all")
					       }
					       else
					       {
					         
						     var sorted_news  = List[org.neo4j.graphdb.Node]()
						     val news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     if( (t_date + 3600) > cur_time)
						     {
						    	 news_list ::= t_id
						     }
						     else
						     {
						       news_list :+= t_id
						     }
						     val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				             news_node.setProperty("value",news_list_trim)
					      
						    
					       }
		    	 	   }
		       
		       var hash_list = t_subcat.split(",").toList.filterNot( x => x.equals("")).map( x => x.toLowerCase())
			   var main_hash = hash_list(0)
		       var old_hash = List[Any]()
		       rels = t_node.getRelationships("Belongs_To_Subcategory_Townhall").asScala
		       var r:org.neo4j.graphdb.Relationship = null
		       for(each <- rels)
		       {
		           val tag = each.getOtherNode(t_node).getProperty("name").toString()
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
		             val tag_node = each.getOtherNode(t_node)
		             val h_wt = tag_node.getProperty("weight").toString().toInt
		             tag_node.setProperty("weight",(h_wt - t_wt))
		             hash_weight_index.remove(tag_node)
		             hash_weight_index += (tag_node,"weight",new ValueContext( h_wt - t_wt ).indexNumeric())
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
		                subcat_node.setProperty("weight",(h_wt + t_wt))
		                if(tag_name.equals(main_hash))
		                {
			                var rel = t_node --> "Belongs_To_Subcategory_Townhall" --> subcat_node <
			                var rel_main = rel.setProperty("main", 1)
			                var rel_time = rel.setProperty("time", t_time_created)
			                var rel_in_wt = rel.setProperty("in_weight", t_wt)
			                var rel_out_wt = rel.setProperty("out_weight", 0)
		                }
		                else
		                {
			                var rel = t_node --> "Belongs_To_Subcategory_Townhall" --> subcat_node <
			                var rel_main = rel.setProperty("main", 1)
			                var rel_time = rel.setProperty("time", t_time_created)
			                var rel_in_wt = rel.setProperty("in_weight", t_wt)
			                var rel_out_wt = rel.setProperty("out_weight", 0)
		                }
		                hash_weight_index.remove(subcat_node)
		                hash_weight_index += (subcat_node,"weight",new ValueContext( h_wt + t_wt ).indexNumeric())
		              }
		              else
		              {
		                val subcat_node = createNode(sub_category(tag_name,t_time_created,t_wt))
		                SubcatIndex += (subcat_node,"name",tag_name)
		                if(tag_name.equals(main_hash))
		                {
			                var rel = t_node --> "Belongs_To_Subcategory_Townhall" --> subcat_node <
			                var rel_main = rel.setProperty("main", 1)
			                var rel_time = rel.setProperty("time", t_time_created)
			                var rel_in_wt = rel.setProperty("in_weight", t_wt)
			                var rel_out_wt = rel.setProperty("out_weight", 0)
		                }
		                else
		                {
			                var rel = t_node --> "Belongs_To_Subcategory_Townhall" --> subcat_node <
			                var rel_main = rel.setProperty("main", 1)
			                var rel_time = rel.setProperty("time", t_time_created)
			                var rel_in_wt = rel.setProperty("in_weight", t_wt)
			                var rel_out_wt = rel.setProperty("out_weight", 0)
		                }
		                
		                var rel = subcat_node --> "Hashtag_Created_By" --> user_node <
		                var rel_time1 = rel.setProperty("time", time)
		                
		                hash_weight_index += (subcat_node,"weight",new ValueContext(t_wt).indexNumeric())
		              }
		            }
		         }
		         
		         else if(old_hash.contains(tag_name) && tag_name.equals(main_hash))
		         {
		           r.setProperty("main", 1)
		         }
		       }
	          
	          ret = true
	      }
          }
    ret
    }
  }

def townhall_action(user_name: String,  // unique user name
    t_id: String,
    action_type: String,  // Q/VA/VQ/P
    qtn_id: String,
    qtn_content: String,
    time: Int
    
      ):Boolean =       
  {
    
   
    withTx {
    implicit neo =>
      
          var ret = false
          val user_index = getNodeIndex("user").get
          val user_node = user_index.get("id",user_name).getSingle()
          
          val t_index = getNodeIndex("townhall").get
          var t_node = t_index.get("id",t_id).getSingle()
          if(user_node != null && t_node != null)
	      {
            if(action_type.toLowerCase().equals("p"))
            {
              val rels = user_node.getRelationships("Participated_In_Townhall").asScala.toList.map(_.getEndNode())
              if(!rels.contains(t_node))
              {
                var rel: org.neo4j.graphdb.Relationship = user_node --> "Participated_In_Townhall" --> t_node < //relating quickpost node and user node
	            var rel_time = rel.setProperty("time", time)
              }
              
	          ret = true
            }
            else if(action_type.toLowerCase().equals("q"))
            {
              var rel: org.neo4j.graphdb.Relationship = user_node --> "Asked_Question" --> t_node < //relating quickpost node and user node
	          var rel_time = rel.setProperty("time", time)
	          var q_id = rel.setProperty("q_id", qtn_id)
	          var q_content = rel.setProperty("q_content", qtn_content)
	          var q_status = rel.setProperty("q_status",0)
	          ret = true
            }
            else if(action_type.toLowerCase().equals("vq"))
            {
              var rel: org.neo4j.graphdb.Relationship = user_node --> "Voted_Townhall_Question" --> t_node < //relating quickpost node and user node
	          var rel_time = rel.setProperty("time", time)
	          var q_id = rel.setProperty("q_id", qtn_id)
	          ret = true
            }
            else
            {
              var rel: org.neo4j.graphdb.Relationship = user_node --> "Voted_Townhall_Answer" --> t_node < //relating quickpost node and user node
	          var rel_time = rel.setProperty("time", time)
	          var q_id = rel.setProperty("q_id", qtn_id)
	          ret = true
            }
	      }
      
       
    ret   
    }
  }

def townhall_comment(t_id: String,
    user_name: String,
    comment: String,
    time: Int):Boolean = 
    {
  
       withTx {
       implicit neo =>
         
          val user_index = getNodeIndex("user").get
          val user_node = user_index.get("id",user_name).getSingle()
          val t_index = getNodeIndex("townhall").get
          var t_node = t_index.get("id",t_id).getSingle()
          
	          
          var ret = false
          if(user_node != null && t_node != null)
	      {
            var rel: org.neo4j.graphdb.Relationship = user_node --> "Commented_On_Townhall" --> t_node < //relating quickpost node and user node
		    var rel_time = rel.setProperty("time", time)
		    var rel_msg = rel.setProperty("t_comment", comment)
		    ret = true
	      }
        
          
       ret   
       }
  
    }

def townhall_approve_question(
    t_id: String,
    qtn_id: String
    ):Boolean =       
  {
    
   
    withTx {
    implicit neo =>
      
       var ret = false
       val t_index = getNodeIndex("townhall").get
       var t_node = t_index.get("id",t_id).getSingle()
       if(t_node != null)
       {
         val rels = t_node.getRelationships("Asked_Question").asScala.toList
         for(each <- rels)
         {
           if(each.getProperty("q_id").toString().equals(qtn_id))
           {
             each.setProperty("q_status",1)
             ret = true
           }
         }
       }
      
       
    ret   
    }
  }

def townhall_change_moderator(
    t_id: String,
    t_moderators: String,
    time: Int
    ):Boolean =       
  {
    
   
    withTx {
    implicit neo =>
      
       		  var ret = false
              val user_index = getNodeIndex("user").get
              val t_index = getNodeIndex("townhall").get
              var t_node = t_index.get("id",t_id).getSingle()
	          if(t_node != null && !t_moderators.equals(""))
	          {
	            val prev_mod_rels = t_node.getRelationships("Townhall_Moderated_By").asScala.toList
	            val prev_mods = t_node.getRelationships("Townhall_Moderated_By").asScala.toList.map( x => x.getEndNode())
	            var mod_list = t_moderators.split(",").toList.map(x => user_index.get("id",x).getSingle()).filter( y => y != null)
	            
	            for(each <- prev_mod_rels)
	            {
	              if(!mod_list.contains(each.getEndNode()))
	              {
	                each.delete()
	              }
	            }
	            
	            mod_list = mod_list.filterNot(x => prev_mods.contains(x))
	            for(each <- mod_list)
	            {
	              var rel: org.neo4j.graphdb.Relationship = t_node --> "Townhall_Moderated_By" --> each < //relating quickpost node and user node
	              var rel_time = rel.setProperty("time", time)
	            }
	            ret = true
	          }
      
       
    ret   
    }
  }

def edit_townhall(user_name: String,  // unique user name
      t_id: String,  // unique quickpost id
      t_title: String,
      t_title_id: String,
      t_content: String,
      t_img_url: String,
      t_date: Int,
      t_duration: Int,
      t_time_created: Int,
      t_celeb: String,
      t_moderators: String):Boolean =       
  {
    
   
    withTx {
    implicit neo =>
     
          
          val user_weight_index = getNodeIndex("user_weight").get
          val user_index = getNodeIndex("user").get
          val user_node = user_index.get("id",user_name).getSingle()
          val celeb_node = user_index.get("id",t_celeb).getSingle()
          val t_index = getNodeIndex("townhall").get
          var t_node = t_index.get("id",t_id).getSingle()
          var ret = false
          if(celeb_node != null && user_node != null && t_node != null)
	      {
              user_node.setProperty("last_seen",t_time_created)
	          t_node.setProperty("t_title",t_title)
	          t_node.setProperty("t_title_id",t_title_id)
	          t_node.setProperty("t_content",t_content)
	          t_node.setProperty("t_img_url",t_img_url)
	          t_node.setProperty("t_date",t_date)
	          t_node.setProperty("t_duration",t_duration)
	          val t_content_index = getNodeIndex("townhall_content").get
	          t_content_index.remove(t_node)
	          t_content_index += (t_node,"t_content",dummy(t_title+ " " +t_content))
	          
	          val rel = t_node.getSingleRelationship("Townhall_Of",Direction.OUTGOING)
	          rel.delete()
	          
	          var rel1: org.neo4j.graphdb.Relationship = t_node --> "Townhall_Of" --> celeb_node < //relating quickpost node and user node
	          var rel_time1 = rel1.setProperty("time", t_time_created)
	          
	          if(!t_moderators.equals(""))
	          {
	            val prev_mod_rels = t_node.getRelationships("Townhall_Moderated_By").asScala.toList
	            val prev_mods = t_node.getRelationships("Townhall_Moderated_By").asScala.toList.map( x => x.getEndNode())
	            var mod_list = t_moderators.split(",").toList.map(x => user_index.get("id",x).getSingle()).filter( y => y != null)
	            
	            for(each <- prev_mod_rels)
	            {
	              if(!mod_list.contains(each.getEndNode()))
	              {
	                each.delete()
	              }
	            }
	            
	            mod_list = mod_list.filterNot(x => prev_mods.contains(x))
	            for(each <- mod_list)
	            {
	              var rel: org.neo4j.graphdb.Relationship = t_node --> "Townhall_Moderated_By" --> each < //relating quickpost node and user node
	              var rel_time = rel.setProperty("time", t_time_created)
	            }
	          }
	          
	          ret = true
	      }
    ret
    }
  }
  // Triggered when a new quick post is created 
def create_quickpost(user_name: String,  // unique user name
      qp_id: String,  // unique quickpost id
      qp_content: String,
      qp_hashtags: String,  // , separated referred hashtags if any("" if none)
      qp_users: String,  // , separated referred users if any("" if none)
      qp_time_created: Int):Boolean =       
  {
    
   
    withTx {
    implicit neo =>
     
          val quickpost_weight_index = getNodeIndex("quickpost_weight").get
          val user_weight_index = getNodeIndex("user_weight").get
          val hash_weight_index = getNodeIndex("hash_weight").get
          val user_index = getNodeIndex("user").get
          val user_node = user_index.get("id",user_name).getSingle()
          val qp_index = getNodeIndex("quickpost").get
          var quickpost_node = qp_index.get("id",qp_id).getSingle()
          var ret = false
          if(user_node != null && quickpost_node == null)
	      {
              user_node.setProperty("last_seen",qp_time_created)
	          val u_wt = user_node.getProperty("weight").toString().toInt
	          val qp_wt =Math.round(u_wt.asInstanceOf[Float]/300)
	          quickpost_node = createNode(quickpost(qp_id,qp_content,qp_time_created,qp_wt,0))
	     
	          
	          val qp_content_index = getNodeIndex("quickpost_content").get
	          qp_index += (quickpost_node,"id",qp_id)
	          quickpost_weight_index += (quickpost_node,"weight",new ValueContext( qp_wt ).indexNumeric())
	          user_node.setProperty("weight",u_wt+3)
	          user_weight_index.remove(user_node)
	          user_weight_index += (user_node,"weight",new ValueContext( u_wt+3).indexNumeric())
	          var rel: org.neo4j.graphdb.Relationship = quickpost_node --> "Quickpost_Written_By" --> user_node < //relating quickpost node and user node
	          var rel_time = rel.setProperty("time", qp_time_created)
	          var rel_in_wt = rel.setProperty("in_weight", 3)
	          var rel_out_wt = rel.setProperty("out_weight", qp_wt)
	          
	          if(qp_hashtags != null)
	          {
	            val SubcatIndex = getNodeIndex("sub_category").get
	            val qp_tags = qp_hashtags.split(",").distinct.toList.map( x => x.toLowerCase())
	            for(tag_name <- qp_tags)
	            {
	              
	              if(tag_name != "")
	              {
	                
	                if(SubcatIndex.get("name",tag_name).getSingle() != null)
	                {
	                  val tag_node = SubcatIndex.get("name",tag_name).getSingle()
	                  val h_wt = tag_node.getProperty("weight").toString().toInt
	                  tag_node.setProperty("weight",(h_wt + 10))
	                  rel = tag_node --> "Tag_Of_Quickpost" --> quickpost_node <
	                  var rel_time = rel.setProperty("time", qp_time_created)
	                  var rel_in_wt = rel.setProperty("in_weight", 0)
	                  var rel_out_wt = rel.setProperty("out_weight", 10)
	                  hash_weight_index.remove(tag_node)
	                  hash_weight_index += (tag_node,"weight",new ValueContext( h_wt + 10 ).indexNumeric())
	                  
	                }
	                else
	                {
	                  val tag_node = createNode(sub_category(tag_name,qp_time_created,10))
	                  SubcatIndex += (tag_node,"name",tag_name)
	                  rel = tag_node --> "Tag_Of_Quickpost" --> quickpost_node <
	                  var rel_time = rel.setProperty("time", qp_time_created)
	                  var rel_in_wt = rel.setProperty("in_weight", 0)
	                  var rel_out_wt = rel.setProperty("out_weight", 10)
	                  hash_weight_index += (tag_node,"weight",new ValueContext( 10 ).indexNumeric())
	                  rel = tag_node --> "Hashtag_Created_By" --> user_node <
	                  var rel_time1 = rel.setProperty("time", qp_time_created)
	                  
	                }
	              }
	            }
	          
		      }
	          
	          
	          if(!qp_users.equals(""))
	          {
	            val qp_users_list = qp_users.split(",").distinct
	            for(each <- qp_users_list)
	            {
	              if(!each.equals(""))
	              {
	              
		              val user_node1 = user_index.get("id",each).getSingle()
		              if(user_node1 != null)
		              {
		                val rel1: org.neo4j.graphdb.Relationship = user_node1 --> "User_Of_Quickpost"  --> quickpost_node <
		                val y = rel1.setProperty("time", qp_time_created)
		              }
	              }
	          
	            }
	          
		      }
	          
	          val tagger = new MaxentTagger(
                "/var/n4j/data/left3words-wsj-0-18.tagger")
              val tagged = tagger.tagString(qp_content)
             // val prev_nouns = user_node.getProperty("nouns").toString()
              val noun_list = tagged.split(" ").toList.map(x => x.split("/")).filter( x => (x.size == 2 && x(1).equals("NNP"))).map(x => x(0).toLowerCase())
              qp_content_index += (quickpost_node,"quickpost_content",noun_list.mkString(" "))
//              if(prev_nouns.equals(""))
//              {
//                val noun_map = noun_list.groupBy(x=>x).mapValues(x=>x.length.toDouble)
//                user_node.setProperty("nouns",JSONObject(noun_map).toString())
//              }
//              else
//              {
//                val noun_map = noun_list.groupBy(x=>x).mapValues(x=>x.length)
//                val prev_map = JSON.parseFull(prev_nouns).get.asInstanceOf[Map[String,Double]]
//                val merge_map = noun_map ++ prev_map.map{ case (k,v) => k -> (v + noun_map.getOrElse(k,0)) }
//                user_node.setProperty("nouns",JSONObject(merge_map).toString())
//              }

	          ret = true
          }
     ret     
    }
    
  }

def index_petition(
    p_id: String,  // unique quickpost id
    p_data: String):Boolean=
 {
          
              val p_index = getNodeIndex("petition").get
	          val data_list = JSON.parseFull(p_data)
	          var p_node = p_index.get("id",p_id).getSingle()
	          var ret = false
	          if(!data_list.isEmpty)
	          {
	              if(p_node == null)
	              {
			          //println(p_id)
			          val data_list1 = data_list.get
			          val map = data_list1.asInstanceOf[Map[String,Any]]
			          var p_content = map.get("overview").get.toString()
			          p_content = Jsoup.parse(p_content).text();
			          var p_title = map.get("title").get.toString()
			          
			          var p_count = Math.round(map.get("signature_count").get.toString().toFloat)
			          var p_goal = Math.round(map.get("goal").get.toString().toFloat)
			          var p_created_at = map.get("created_at").get.toString()
			          val dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
				      val c_time = dt.parse(p_created_at)
				      val time_created = (c_time.getTime()/1000).toInt
				      var p_end_at = map.get("end_at").get.toString()
				      val e_time = dt.parse(p_end_at)
				      val end_time = (e_time.getTime()/1000).toInt
				      create_petition("C","",p_id,p_title,"",p_content,"","",p_goal,p_count,time_created,end_time,"","","",0,0)
				      ret = true
	              }
	              
	              else
	              {
	                  val data_list1 = data_list.get
			          val map = data_list1.asInstanceOf[Map[String,Any]]
	                  var p_count = Math.round(map.get("signature_count").get.toString().toFloat)
			          var p_goal = Math.round(map.get("goal").get.toString().toFloat)
			          
			          val dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
				      
				      var p_end_at = map.get("end_at").get.toString()
				      val e_time = dt.parse(p_end_at)
				      val end_time = (e_time.getTime()/1000).toInt
				      //edit_petition("C","",p_id,"","","","","",p_goal,p_count,0,end_time,"","")
				      ret = true
	              }
			      
	          }
	          
	          
          
	          
	       
ret          
}


def create_petition(
      p_type: String,
      user_name: String,  // unique user name
      p_id: String,  // unique petition id
      p_title: String,
      p_title_id: String,
      p_content: String, 
      p_img_url: String,
      p_to: String,
      p_target: Int,
      p_count: Int,
      p_time_created: Int,
      p_end_date: Int,
      p_subcat: String,
      p_hashtags: String,
      p_cat: String,
      is_edit: Int,
      is_closed: Int):Boolean =       
    {
    
   
    withTx {
    implicit neo =>
     
          var ret = false
          if(is_edit == 0)
          {
               var is_space = is_closed
	          val p_weight_index = getNodeIndex("petition_weight").get
	          val p_index = getNodeIndex("petition").get
	          val TilesIndex = getNodeIndex("tiles").get
	          var p_node = p_index.get("id",p_id).getSingle()
	          
	          
	          if(p_node == null)
	          {
	            val user_weight_index = getNodeIndex("user_weight").get
	            val hash_weight_index = getNodeIndex("hash_weight").get
	            val user_index = getNodeIndex("user").get
	            val user_node = user_index.get("id",user_name).getSingle()
	            if(user_node != null && p_node == null)
	            {
	              user_node.setProperty("last_seen",p_time_created)
		          val u_wt = user_node.getProperty("weight").toString().toInt
		          val p_wt =Math.round(u_wt.asInstanceOf[Float]/200)
		          if(p_type.toLowerCase().equals("c"))
		          {
		            p_node = createNode(petition("C",p_id,p_title,p_title_id,"",p_img_url,p_to,p_target,p_count,p_time_created,p_end_date,p_wt,0,1,"",0,0,is_space))
		     
		          }
		          else
		          {
		            p_node = createNode(petition("S",p_id,p_title,p_title_id,"",p_img_url,p_to,p_target,p_count,p_time_created,p_end_date,p_wt,0,1,"",0,0,is_space))
		     
		          }
		          
		          val p_content_index = getNodeIndex("petition_content").get
		          val hash_list = p_subcat.split(",").toList.map(x => x.toLowerCase())
		     
		          val topic_words_list = (dummy(p_title+" "+p_content)+" "+hash_list.mkString(" ")).toLowerCase().split(" ").toList
		          val index_data = dummy(p_title+" "+p_content)+" "+hash_list.mkString(" ")
		          p_content_index += (p_node,"time",new ValueContext( p_time_created ).indexNumeric())
		     
		          
		          p_index += (p_node,"id",p_id)
		          p_weight_index += (p_node,"weight",new ValueContext( p_wt ).indexNumeric())
		          user_node.setProperty("weight",u_wt+5)
		          user_weight_index.remove(user_node)
		          user_weight_index += (user_node,"weight",new ValueContext( u_wt+5).indexNumeric())
		          var rel: org.neo4j.graphdb.Relationship = p_node --> "Petition_Written_By" --> user_node < //relating quickpost node and user node
		          var rel_time = rel.setProperty("time", p_time_created)
		          var rel_in_wt = rel.setProperty("in_weight", 5)
		          var rel_out_wt = rel.setProperty("out_weight", p_wt)
		          var hash_tags = List[String]()
		          var cats = List[String]()
		             val CategoryIndex = getNodeIndex("category").get
		             if(p_cat.equals(""))
		             {
		               cats ::= "all"
		               cats ::= "humaninterest"
		             }
		             else
		             {
		               cats = p_cat.split(",").toList.map( x => x.toLowerCase()) 
				       cats ::= "all"
		             }
		             
				     for(each <- cats)
				     {
				       //event_content_index += (event_node,"event_cat",each)
				       if(CategoryIndex.get("name",each).getSingle() != null)  
				       {
				         val category_node = CategoryIndex.get("name",each).getSingle()
				         rel = p_node --> "Belongs_To_Petition_Category" --> category_node <
				         var rel_time = rel.setProperty("time", p_time_created)
				         val topic_nodes = category_node.getRelationships("Topic_Of_Category").asScala.toList.map(_.getStartNode())
				         if(topic_nodes != null)
				         {
				           for(top <- topic_nodes)
				           {
				             val keywords = top.getProperty("sub_topics").toString().split(",").toList
				             val inter = topic_words_list.intersect(keywords).size
				             if(inter > 0)
				             {
				               rel = p_node --> "Belongs_To_Petition_Topic" --> top <
				               var rel_time = rel.setProperty("time", p_time_created)
				               var count = rel.setProperty("count", inter)
				             }
				             
				           }
				         }
				       }
				       else
				       {
				         val category_node = createNode(category(each,"",""))
				         CategoryIndex += (category_node,"name",each)
				         rel = p_node --> "Belongs_To_Petition_Category" --> category_node <
				         var rel_time = rel.setProperty("time", p_time_created)
				       }
				     }
				     if(is_space == 0)
		             {
					     for(each <- cats)
					     {
					       var news_node = TilesIndex.get("id",each).getSingle()
					       if(news_node == null)
					       {
					         val tiles_node =  createNode(tiles(each,p_id))
					         TilesIndex += (tiles_node,"id",each)
					       }
					       else
					       {
					         
						     var sorted_news  = List[org.neo4j.graphdb.Node]()
						     val news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     news_list ::= p_id
						     val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				             news_node.setProperty("value",news_list_trim)
					      
						    
					       }
					     }
		             }
		          
		          val SubcatIndex = getNodeIndex("sub_category").get
		          var tot_tags = List[ org.neo4j.graphdb.Node]()
	              if(p_subcat != "")
		          {
		              hash_tags = p_subcat.split(",").toList.filterNot( x => x.equals("")).map( x => x.toLowerCase())
		              val main_hash = hash_tags(0)
		              for(each <- hash_tags)
		              {
			              if(SubcatIndex.get("name",each).getSingle() != null)
			              {
			                
			                val subcat_node = SubcatIndex.get("name",each).getSingle()
			                tot_tags ::= subcat_node
			                val h_wt = subcat_node.getProperty("weight").toString().toInt
			                subcat_node.setProperty("weight",(h_wt + p_wt))
			                if(each.equals(main_hash))
			                {
				                rel = p_node --> "Belongs_To_Subcategory_Petition" --> subcat_node <
				                var rel_main = rel.setProperty("main", 1)
				                var rel_time = rel.setProperty("time", p_time_created)
				                var rel_in_wt = rel.setProperty("in_weight", p_wt)
				                var rel_out_wt = rel.setProperty("out_weight", 0)
			                }
			                else
			                {
				                rel = p_node --> "Belongs_To_Subcategory_Petition" --> subcat_node <
				                var rel_time = rel.setProperty("time", p_time_created)
				                var rel_in_wt = rel.setProperty("in_weight", p_wt)
				                var rel_out_wt = rel.setProperty("out_weight", 0)
			                }
			                hash_weight_index.remove(subcat_node)
			                hash_weight_index += (subcat_node,"weight",new ValueContext( h_wt + p_wt ).indexNumeric())
			              }
			              else
			              {
			                val subcat_node = createNode(sub_category(each,p_time_created,p_wt))
			                SubcatIndex += (subcat_node,"name",each)
			                if(each.equals(main_hash))
			                {
				                rel = p_node --> "Belongs_To_Subcategory_Petition" --> subcat_node <
				                var rel_main = rel.setProperty("main", 1)
				                var rel_time = rel.setProperty("time", p_time_created)
				                var rel_in_wt = rel.setProperty("in_weight", p_wt)
				                var rel_out_wt = rel.setProperty("out_weight", 0)
			                }
			                else
			                {
				                rel = p_node --> "Belongs_To_Subcategory_Petition" --> subcat_node <
				                var rel_time = rel.setProperty("time", p_time_created)
				                var rel_in_wt = rel.setProperty("in_weight", p_wt)
				                var rel_out_wt = rel.setProperty("out_weight", 0)
			                }
			                
			                rel = subcat_node --> "Hashtag_Created_By" --> user_node <
			                var rel_time1 = rel.setProperty("time", p_time_created)
			                
			                hash_weight_index += (subcat_node,"weight",new ValueContext( p_wt).indexNumeric())
			                tot_tags ::= subcat_node
			              }
		              }
		            }
		          
		          if(p_hashtags != "")
		          {
		            val p_tags = p_hashtags.split(",").distinct.toList.filterNot( x => x.equals("")).map( x => x.toLowerCase())
		            for(tag_name <- p_tags)
		            {
		              
		              if(tag_name != "")
		              {
		                
		                if(SubcatIndex.get("name",tag_name).getSingle() != null)
		                {
		                  val tag_node = SubcatIndex.get("name",tag_name).getSingle()
		                  tot_tags ::= tag_node
		                  val h_wt = tag_node.getProperty("weight").toString().toInt
		                  tag_node.setProperty("weight",(h_wt + 10))
		                  rel = tag_node --> "Tag_Of_Petition" --> p_node <
		                  var rel_time = rel.setProperty("time", p_time_created)
		                  var rel_in_wt = rel.setProperty("in_weight", 0)
		                  var rel_out_wt = rel.setProperty("out_weight", 10)
		                  hash_weight_index.remove(tag_node)
		                  hash_weight_index += (tag_node,"weight",new ValueContext( h_wt + 10 ).indexNumeric())
		                  
		                }
		                else
		                {
		                  val tag_node = createNode(sub_category(tag_name,p_time_created,10))
		                  SubcatIndex += (tag_node,"name",tag_name)
		                  rel = tag_node --> "Tag_Of_Petition" --> p_node <
		                  var rel_time = rel.setProperty("time", p_time_created)
		                  var rel_in_wt = rel.setProperty("in_weight", 0)
		                  var rel_out_wt = rel.setProperty("out_weight", 10)
		                  hash_weight_index += (tag_node,"weight",new ValueContext( 10 ).indexNumeric())
		                  rel = tag_node --> "Hashtag_Created_By" --> user_node <
		                  var rel_time1 = rel.setProperty("time", p_time_created)
		                  tot_tags ::= tag_node
		                  
		                }
		              }
		            }
		          
			      }
		          
		          var flag_list = List[org.neo4j.graphdb.Node]()
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
			      val all_cities = location_index.get("id","all").getSingle().getProperty("cities").toString().split(",").toList
			      val cities = index_data.split(" ").toList.distinct.intersect(all_cities)
			      val location_nodes = location_index.query( "id", "*" ).iterator().asScala.toList.filterNot( x => x.getProperty("location_id").equals("all"))
		          val loop = new Breaks; 
		          if(cities.size > 0)
			      {
			       for(each <- cities)
			       {
			         loop.breakable{
			           for(e <- location_nodes)
			           {
			             if(e.getProperty("cities").toString().split(",").contains(each))
			             {
			               p_node --> "Belongs_To_Location_Petition" --> e 
			               loop.break;
			             }
			           }
		             }
			       }
			     }
		          
		          val tagger = new MaxentTagger(
	                "/var/n4j/data/left3words-wsj-0-18.tagger")
	              val tagged = tagger.tagString(p_content)
	              //val prev_nouns = user_node.getProperty("nouns").toString()
	              val noun_list =  tagged.split(" ").toList.map(x => x.split("/")).filter( x => (x.size == 2 && x(1).equals("NNP"))).map(x => x(0).toLowerCase())
	              var noun_hash_list = noun_list:::hash_list
	              p_content_index += (p_node,"p_content",(noun_hash_list.mkString(" ") + " " + dummy(p_title))  )
	              val proper_nouns = List("2013","2014","india","indian","reddy","rao","singh","world","pti","sunday","monday","tuesday","wednesday","thursday","friday","saturday","january","february","march","april","may","june","july","august","september","october","november","december")
	              noun_hash_list = noun_hash_list.filterNot(x => proper_nouns.contains(x) && x.length() < 3)
	              p_node.setProperty("p_content",noun_hash_list.mkString(" "))
	//              if(prev_nouns.equals(""))
	//              {
	//                
	//                val noun_map = (noun_hash_list).groupBy(x=>x).mapValues(x=>x.length.toDouble)
	//                user_node.setProperty("nouns",JSONObject(noun_map).toString())
	//              }
	//              else
	//              {
	//                val noun_map = (noun_hash_list).groupBy(x=>x).mapValues(x=>x.length)
	//                val prev_map = JSON.parseFull(prev_nouns).get.asInstanceOf[Map[String,Double]]
	//                val merge_map = noun_map ++ prev_map.map{ case (k,v) => k -> (v + noun_map.getOrElse(k,0)) }
	//                user_node.setProperty("nouns",JSONObject(merge_map).toString())
	//              }
		          
		     
		          
	
		          ret = true
	            }
	          }
          }
          else
          {
            val p_weight_index = getNodeIndex("petition_weight").get
          val user_weight_index = getNodeIndex("user_weight").get
          val hash_weight_index = getNodeIndex("hash_weight").get
          val user_index = getNodeIndex("user").get
          val user_node = user_index.get("id",user_name).getSingle()
          val p_index = getNodeIndex("petition").get
          var p_node = p_index.get("id",p_id).getSingle()
          val TilesIndex = getNodeIndex("tiles").get
       val FeaturedTilesIndex = getNodeIndex("featured_tiles").get
       val HeadlinesIndex = getNodeIndex("headlines").get
	   
          
	          
          //var ret = false
          
          
          if(p_node != null && user_node != null)
          {
	          
                  val feu = p_node.getProperty("approved").toString().toInt
                  val head = p_node.getProperty("head").toString().toInt
                  val space = p_node.getProperty("space").toString().toInt
	              user_node.setProperty("last_seen",p_time_created)
	              val time = p_node.getProperty("time_created").toString().toInt
	              val p_wt = p_node.getProperty("weight").toString().toInt
	              p_node.setProperty("p_title",p_title)
	              p_node.setProperty("p_title_id",p_title_id)
	              //p_node.setProperty("p_content",p_content)
	              p_node.setProperty("p_img_url",p_img_url)
	              p_node.setProperty("p_to",p_to)
	              p_node.setProperty("p_target",p_target)
	              p_node.setProperty("p_end_date",p_end_date)
	              
		          
		          val p_content_index = getNodeIndex("petition_content").get
		          p_content_index.remove(p_node)
		          p_content_index += (p_node,"time",new ValueContext( time ).indexNumeric())
		          val h_list = p_subcat.split(",").toList.map( x => x.toLowerCase())
		          val topic_words_list = (dummy(p_title+" "+p_content)+" "+h_list.mkString(" ")).toLowerCase().split(" ").toList
		          
		          var cats_list = p_node.getRelationships("Belongs_To_Petition_Category",Direction.OUTGOING).asScala.toList.map(_.getEndNode().getProperty("name").toString().toLowerCase())
				    
		             for(each <- cats_list)
				     {
				         var news_node = TilesIndex.get("id",each).getSingle()
				         if(news_node != null)
				         {
					         var news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     if(news_list.contains(p_id))
					         {
					           news_list = news_list - p_id
					           val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				               news_node.setProperty("value",news_list_trim)
					         }
				         }
				         
				         news_node = FeaturedTilesIndex.get("id",each).getSingle()
				         if(news_node != null)
				         {
					         var news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     if(news_list.contains(p_id))
					         {
					           news_list = news_list - p_id
					           val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				               news_node.setProperty("value",news_list_trim)
					         }
				         }
				         
				         news_node = HeadlinesIndex.get("id",each).getSingle()
				         if(news_node != null)
				         {
					         var news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     if(news_list.contains(p_id))
					         {
					           news_list = news_list - p_id
					           val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				               news_node.setProperty("value",news_list_trim)
					         }
				         }
				     }
		          var rels = p_node.getRelationships("Belongs_To_Location_petition","Belongs_To_Petition_Category").asScala
			      for(each <- rels)
			      {
			         each.delete()
			      }
		          val SubcatIndex = getNodeIndex("sub_category").get
		          val tags_list = p_hashtags.split(",").distinct.toList.filterNot( x => x.equals("")).map( x => x.toLowerCase())
			       var old_tags = List[Any]()
			       rels = p_node.getRelationships("Tag_Of_Petition").asScala
			       for(each <- rels)
			       {
			           val tag = each.getOtherNode(p_node).getProperty("name").toString()
			           old_tags ::= tag
			           if(!tags_list.contains(tag))
			           {
			             val tag_node = each.getOtherNode(p_node)
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
			                  var rel = tag_node --> "Tag_Of_Petition" --> p_node <
			                  var rel_time = rel.setProperty("time", p_time_created)
			                  var rel_in_wt = rel.setProperty("in_weight", 0)
			                  var rel_out_wt = rel.setProperty("out_weight", 10)
			                  hash_weight_index.remove(tag_node)
			                  hash_weight_index += (tag_node,"weight",new ValueContext( h_wt + 10 ).indexNumeric())
			                  
			                }
			                else
			                {
			                  val tag_node = createNode(sub_category(tag_name,p_time_created,10))
			                  SubcatIndex += (tag_node,"name",tag_name)
			                  var rel = tag_node --> "Tag_Of_Petition" --> p_node <
			                  var rel_time = rel.setProperty("time", p_time_created)
			                  var rel_in_wt = rel.setProperty("in_weight", 0)
			                  var rel_out_wt = rel.setProperty("out_weight", 10)
			                  rel = tag_node --> "Hashtag_Created_By" --> user_node <
			                  var rel_time1 = rel.setProperty("time", time)
			                  hash_weight_index += (tag_node,"weight",new ValueContext( 10 ).indexNumeric())
			                  
			                }
			              }
			         }
			       }
			       
			       
			             val CategoryIndex = getNodeIndex("category").get
			             
	     
				         var cats = List[String]()
			             
			             if(p_cat.equals(""))
			             {
			               cats ::= "all"
			               cats ::= "humaninterest"
			             }
			             else
			             {
			               cats = p_cat.split(",").toList.map( x => x.toLowerCase()) 
					       cats ::= "all"
			             }
				     val cur_time = (System.currentTimeMillis() /1000).toInt
				     for(each <- cats)
				     {
				       //event_content_index += (event_node,"event_cat",each)
				       
				       var news_node = TilesIndex.get("id",each).getSingle()
				       if(space == 0)
		               {
					       if(news_node == null)
					       {
					         val tiles_node =  createNode(tiles(each,p_id))
					         TilesIndex += (tiles_node,"id",each)
					       }
					       else
					       {
					         
						     var sorted_news  = List[org.neo4j.graphdb.Node]()
						     val news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     if(p_end_date > cur_time)
						     {
						    	 news_list ::= p_id
						     }
						     else
						     {
						       news_list :+= p_id
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
					         val tiles_node =  createNode(featured_tiles(each,p_id))
					         FeaturedTilesIndex += (tiles_node,"id",each)
					       }
					       else
					       {
					         
						     var sorted_news  = List[org.neo4j.graphdb.Node]()
						     val news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     if(p_end_date > cur_time)
						     {
						    	 news_list ::= p_id
						     }
						     else
						     {
						       news_list :+= p_id
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
					         val tiles_node =  createNode(headlines(each,p_id))
					         HeadlinesIndex += (tiles_node,"id",each)
					       }
					       else
					       {
					         
						     var sorted_news  = List[org.neo4j.graphdb.Node]()
						     val news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     if(p_end_date > cur_time)
						     {
						    	 news_list ::= p_id
						     }
						     else
						     {
						       news_list :+= p_id
						     }
						     val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				             news_node.setProperty("value",news_list_trim)
					      
						    
					       }
		    	 	   }
				       
				       if(CategoryIndex.get("name",each).getSingle() != null)  
				       {
				         
				         val category_node = CategoryIndex.get("name",each).getSingle()
				         var rel = p_node --> "Belongs_To_Petition_Category" --> category_node <
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
				               rel = p_node --> "Belongs_To_Petition_Topic" --> top <
				               var rel_time = rel.setProperty("time", time)
				               var count = rel.setProperty("count", inter)
				             }
				             
				           }
				         }
				       
				         
				       }
				       else
				       {
				         val category_node = createNode(category(each,"",""))
				         CategoryIndex += (category_node,"name",each)
				         var rel = p_node --> "Belongs_To_Petition_Category" --> category_node <
				         var rel_time = rel.setProperty("time", time)
				         
				         
				       }
				     }
			       
			       var hash_list = p_subcat.split(",").toList.filterNot( x => x.equals("")).map( x => x.toLowerCase())
				   var main_hash = hash_list(0)
			       var old_hash = List[Any]()
			       var r:org.neo4j.graphdb.Relationship = null
			       rels = p_node.getRelationships("Belongs_To_Subcategory_Petition").asScala
			       for(each <- rels)
			       {
			           val tag = each.getOtherNode(p_node).getProperty("name").toString().toLowerCase()
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
			             val tag_node = each.getOtherNode(p_node)
			             val h_wt = tag_node.getProperty("weight").toString().toInt
			             tag_node.setProperty("weight",(h_wt - p_wt))
			             hash_weight_index.remove(tag_node)
			             hash_weight_index += (tag_node,"weight",new ValueContext( h_wt - p_wt ).indexNumeric())
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
			                subcat_node.setProperty("weight",(h_wt + p_wt))
			                if(tag_name.equals(main_hash))
			                {
				                var rel = p_node --> "Belongs_To_Subcategory_Petition" --> subcat_node <
				                var rel_main = rel.setProperty("main", 1)
				                var rel_time = rel.setProperty("time", p_time_created)
				                var rel_in_wt = rel.setProperty("in_weight", p_wt)
				                var rel_out_wt = rel.setProperty("out_weight", 0)
			                }
			                else
			                {
				                var rel = p_node --> "Belongs_To_Subcategory_Petition" --> subcat_node <
				                var rel_main = rel.setProperty("main", 1)
				                var rel_time = rel.setProperty("time", p_time_created)
				                var rel_in_wt = rel.setProperty("in_weight", p_wt)
				                var rel_out_wt = rel.setProperty("out_weight", 0)
			                }
			                hash_weight_index.remove(subcat_node)
			                hash_weight_index += (subcat_node,"weight",new ValueContext( h_wt + p_wt ).indexNumeric())
			              }
			              else
			              {
			                val subcat_node = createNode(sub_category(tag_name,p_time_created,p_wt))
			                SubcatIndex += (subcat_node,"name",tag_name)
			                if(tag_name.equals(main_hash))
			                {
				                var rel = p_node --> "Belongs_To_Subcategory_Petition" --> subcat_node <
				                var rel_main = rel.setProperty("main", 1)
				                var rel_time = rel.setProperty("time", p_time_created)
				                var rel_in_wt = rel.setProperty("in_weight", p_wt)
				                var rel_out_wt = rel.setProperty("out_weight", 0)
			                }
			                else
			                {
				                var rel = p_node --> "Belongs_To_Subcategory_Petition" --> subcat_node <
				                var rel_main = rel.setProperty("main", 1)
				                var rel_time = rel.setProperty("time", p_time_created)
				                var rel_in_wt = rel.setProperty("in_weight", p_wt)
				                var rel_out_wt = rel.setProperty("out_weight", 0)
			                }
			                
			                var rel = subcat_node --> "Hashtag_Created_By" --> user_node <
			                var rel_time1 = rel.setProperty("time", time)
			                
			                hash_weight_index += (subcat_node,"weight",new ValueContext(p_wt).indexNumeric())
			              }
			            }
			         }
			         
			         else if(old_hash.contains(tag_name) && tag_name.equals(main_hash))
			         {
			           r.setProperty("main", 1)
			         }
			       }
			       
			      //hash_list = p_subcat.split(",").toList.map(x => x.toLowerCase())
		          val index_data = dummy(p_title+" "+p_content)+" "+hash_list.mkString(" ")
		           
			       
			      val location_index = getNodeIndex("location").get
			      val all_cities = location_index.get("id","all").getSingle().getProperty("cities").toString().split(",").toList
			      val cities = index_data.split(" ").toList.distinct.intersect(all_cities)
			      val location_nodes = location_index.query( "id", "*" ).iterator().asScala.toList.filterNot( x => x.getProperty("location_id").equals("all"))
		          val loop = new Breaks; 
		          if(cities.size > 0)
			      {
			       for(each <- cities)
			       {
			         loop.breakable{
			           for(e <- location_nodes)
			           {
			             if(e.getProperty("cities").toString().split(",").contains(each))
			             {
			               p_node --> "Belongs_To_Location_Petition" --> e 
			               loop.break;
			             }
			           }
		             }
			       }
			     }
		          
		          val tagger = new MaxentTagger(
	                "/var/n4j/data/left3words-wsj-0-18.tagger")
	              
	              val tagged = tagger.tagString(p_content)
	              //val prev_nouns = user_node.getProperty("nouns").toString()
	              val noun_list =  tagged.split(" ").toList.map(x => x.split("/")).filter( x => (x.size == 2 && x(1).equals("NNP"))).map(x => x(0).toLowerCase())
	              var noun_hash_list = noun_list:::hash_list
	              p_content_index += (p_node,"p_content",(noun_hash_list.mkString(" ") + " " + dummy(p_title))  )
	              val proper_nouns = List("2013","2014","india","indian","reddy","rao","singh","world","pti","sunday","monday","tuesday","wednesday","thursday","friday","saturday","january","february","march","april","may","june","july","august","september","october","november","december")
                  noun_hash_list = noun_hash_list.filterNot(x => proper_nouns.contains(x) && x.length() < 3)
                  p_node.setProperty("p_content",noun_hash_list.mkString(" "))
              

		          ret = true
	          
          }
          }
          
     ret     
    }
    
  }

  def user_signed_petition(
      user_name: String,  // unique user name
      p_id: String  // unique petition id
      ):Boolean =       
    {
     
          val p_index = getNodeIndex("petition").get
          var p_node = p_index.get("id",p_id).getSingle()
          val u_index = getNodeIndex("user").get
          var u_node = u_index.get("id",user_name).getSingle()
          var ret = false
          if(p_node != null && u_node != null && p_node.getRelationships("Signed_Petition").asScala.map(_.getOtherNode(p_node)).toList.contains(u_node))
          {
            
            ret = true
          }
          
    ret      
    }

def sign_petition(
      user_name: String,  // unique user name
      p_id: String,  // unique petition id4
      time: Int
      ):Boolean =       
    {
    
   
    withTx {
    implicit neo =>
     
          val p_weight_index = getNodeIndex("petition_weight").get
          val p_index = getNodeIndex("petition").get
          var p_node = p_index.get("id",p_id).getSingle()
          val u_index = getNodeIndex("user").get
          val hash_weight_index = getNodeIndex("hash_weight").get
          val user_weight_index = getNodeIndex("user_weight").get
          var u_node = u_index.get("id",user_name).getSingle()
          var ret = false
          if(p_node != null && u_node != null && !p_node.getRelationships("Signed_Petition").asScala.map(_.getOtherNode(p_node)).toList.contains(u_node))
          {
		         u_node.setProperty("last_seen",time)
		         val p_target = p_node.getProperty("p_target").toString().toInt
	             val i_wt = p_node.getProperty("weight").toString().toInt
	             val u_wt = u_node.getProperty("weight").toString().toInt
		         p_node.setProperty("weight",i_wt + Math.round(u_wt.asInstanceOf[Float]/100))
		         
		         val signs = p_node.getProperty("p_count").toString().toInt
		         p_node.setProperty("p_count", signs + 1)
		         
		         if(p_node.hasRelationship("Petition_Written_By",Direction.OUTGOING))
		         {
			         val author_node = p_node.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode()
		             val au_wt = author_node.getProperty("weight").toString().toInt
		       
			         author_node.setProperty("weight",au_wt + Math.round(u_wt.asInstanceOf[Float]/100))
			         user_weight_index.remove(author_node)
				     user_weight_index += (author_node,"weight",new ValueContext( au_wt + Math.round(u_wt.asInstanceOf[Float]/100) ).indexNumeric())
		         }
		         val hash_nodes = p_node.getRelationships("Belongs_To_Subcategory_Petition",Direction.OUTGOING).asScala.map(_.getOtherNode(p_node)).toList
	            
		         if(hash_nodes.size > 0)
		         {
			         for(each <- hash_nodes)
			         {
				         val h_wt = each.getProperty("weight").toString().toInt
					     each.setProperty("weight",h_wt + (Math.round(u_wt.asInstanceOf[Float]/100)))
					     hash_weight_index.remove(each)
						 hash_weight_index += (each,"weight",new ValueContext( h_wt + (Math.round(u_wt.asInstanceOf[Float]/100)) ).indexNumeric())
			         }
		         }
		         p_weight_index.remove(p_node)
			     p_weight_index += (p_node,"weight",new ValueContext( i_wt + Math.round(u_wt.asInstanceOf[Float]/100) ).indexNumeric())
			     
		         //val rel_name = i_type.toLowerCase()+"_markfav"
		         var rel: org.neo4j.graphdb.Relationship = u_node --> "Signed_Petition" --> p_node <  //Relating user node to the item node with "Vote_up" relation
		         val rel_time = rel.setProperty("time", time)
		         var rel_in_wt = rel.setProperty("in_weight", Math.round(u_wt.asInstanceOf[Float]/100))
		         var rel_out_wt = rel.setProperty("out_weight", 0)
		         if(signs + 1 == p_target)
		         {
		           val signed_users = p_node.getRelationships("Signed_Petition").asScala.toList.map(_.getOtherNode(p_node))
		           for(each <- signed_users)
		           {
		             rel = each --> "Signs_Completed" --> p_node < 
		             val rel_time = rel.setProperty("time", time)
		           }
		         }

		         ret = true
	         
	         
	         
	         
	          
	       }
          
          
        ret  
        }
          
  }

def update_petition_signs(
      p_id: String,  // unique petition id
      signs: Int
      ):Boolean =       
    {
    
   
    withTx {
    implicit neo =>
     
          
          val p_index = getNodeIndex("petition").get
          var p_node = p_index.get("id",p_id).getSingle()
          
          var ret = false
          if(p_node != null)
          {
		         
		         p_node.setProperty("p_count", signs)
		         ret = true
	      }
          
          
        ret  
        }
          
  }

def edit_petition(
      p_type: String,
      user_name: String,  // unique user name
      p_id: String,  // unique petition id
      p_title: String,
      p_title_id: String,
      p_content: String,  //Why is this important field
      p_img_url: String,
      p_to: String,
      p_target: Int,
      p_count: Int,
      p_time_created: Int,
      p_end_date: Int,
      p_subcat: String,
      p_hashtags: String,
      p_cat: String):Boolean =       
  {
    
   
    withTx {
    implicit neo =>
     
          val p_weight_index = getNodeIndex("petition_weight").get
          val user_weight_index = getNodeIndex("user_weight").get
          val hash_weight_index = getNodeIndex("hash_weight").get
          val user_index = getNodeIndex("user").get
          val user_node = user_index.get("id",user_name).getSingle()
          val p_index = getNodeIndex("petition").get
          var p_node = p_index.get("id",p_id).getSingle()
          
	          
          var ret = false
          
          
          if(p_node != null && user_node != null)
          {
	          
	              user_node.setProperty("last_seen",p_time_created)
	              val time = p_node.getProperty("time_created").toString().toInt
	              val p_wt = p_node.getProperty("weight").toString().toInt
	              p_node.setProperty("p_title",p_title)
	              p_node.setProperty("p_title_id",p_title_id)
	              //p_node.setProperty("p_content",p_content)
	              p_node.setProperty("p_img_url",p_img_url)
	              p_node.setProperty("p_to",p_to)
	              p_node.setProperty("p_target",p_target)
	              
	              p_node.setProperty("p_end_date",p_end_date)
	              
	              val p_count = p_node.getProperty("p_count").toString().toInt
	              if(p_node.hasRelationship("Signs_Completed") && p_target > p_count)
	              {
	                val finished_rels = p_node.getRelationships("Signs_Completed").asScala.toList
		            for(each <- finished_rels)
		            {
		              each.delete()
		            }
	              }
	              
		          
		          val p_content_index = getNodeIndex("petition_content").get
		          p_content_index.remove(p_node)
		          p_content_index += (p_node,"time",new ValueContext( time ).indexNumeric())
		          
		          var rels = p_node.getRelationships("Belongs_To_Location_petition","Belongs_To_Petition_Category").asScala
			      for(each <- rels)
			      {
			         each.delete()
			      }
		          val SubcatIndex = getNodeIndex("sub_category").get
		          val tags_list = p_hashtags.split(",").distinct.toList.map( x => x.toLowerCase())
			       var old_tags = List[Any]()
			       rels = p_node.getRelationships("Tag_Of_Petition").asScala
			       for(each <- rels)
			       {
			           val tag = each.getOtherNode(p_node).getProperty("name").toString()
			           old_tags ::= tag
			           if(!tags_list.contains(tag))
			           {
			             val tag_node = each.getOtherNode(p_node)
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
			                  var rel = tag_node --> "Tag_Of_Petition" --> p_node <
			                  var rel_time = rel.setProperty("time", p_time_created)
			                  var rel_in_wt = rel.setProperty("in_weight", 0)
			                  var rel_out_wt = rel.setProperty("out_weight", 10)
			                  hash_weight_index.remove(tag_node)
			                  hash_weight_index += (tag_node,"weight",new ValueContext( h_wt + 10 ).indexNumeric())
			                  
			                }
			                else
			                {
			                  val tag_node = createNode(sub_category(tag_name,p_time_created,10))
			                  SubcatIndex += (tag_node,"name",tag_name)
			                  var rel = tag_node --> "Tag_Of_Petition" --> p_node <
			                  var rel_time = rel.setProperty("time", p_time_created)
			                  var rel_in_wt = rel.setProperty("in_weight", 0)
			                  var rel_out_wt = rel.setProperty("out_weight", 10)
			                  rel = tag_node --> "Hashtag_Created_By" --> user_node <
			                  var rel_time1 = rel.setProperty("time", time)
			                  hash_weight_index += (tag_node,"weight",new ValueContext( 10 ).indexNumeric())
			                  
			                }
			              }
			         }
			       }
			       
			       
			             val CategoryIndex = getNodeIndex("category").get
	     
				         var cats = List[String]()
			             
			             if(p_cat.equals(""))
			             {
			               cats ::= "all"
			               cats ::= "humaninterest"
			             }
			             else
			             {
			               cats = p_cat.split(",").toList.map( x => x.toLowerCase()) 
					       cats ::= "all"
			             }
				     
				     for(each <- cats)
				     {
				       //event_content_index += (event_node,"event_cat",each)
				       if(CategoryIndex.get("name",each).getSingle() != null)  
				       {
				         val category_node = CategoryIndex.get("name",each).getSingle()
				         var rel = p_node --> "Belongs_To_Petition_Category" --> category_node <
				         var rel_time = rel.setProperty("time", time)
				         
				       }
				       else
				       {
				         val category_node = createNode(category(each,"",""))
				         CategoryIndex += (category_node,"name",each)
				         var rel = p_node --> "Belongs_To_Petition_Category" --> category_node <
				         var rel_time = rel.setProperty("time", time)
				         
				         
				       }
				     }
			       
			       var hash_list = p_subcat.split(",").toList.map( x => x.toLowerCase())
				   var main_hash = hash_list(0)
			       var old_hash = List[Any]()
			       rels = p_node.getRelationships("Belongs_To_Subcategory_Petition").asScala
			       for(each <- rels)
			       {
			           val tag = each.getOtherNode(p_node).getProperty("name").toString().toLowerCase()
			           if(each.hasProperty("main") && !tag.equals(main_hash))
				       {
				          each.removeProperty("main")
				             
				       }
			           old_hash ::= tag
			           if(!hash_list.contains(tag))
			           {
			             val tag_node = each.getOtherNode(p_node)
			             val h_wt = tag_node.getProperty("weight").toString().toInt
			             tag_node.setProperty("weight",(h_wt - p_wt))
			             hash_weight_index.remove(tag_node)
			             hash_weight_index += (tag_node,"weight",new ValueContext( h_wt - p_wt ).indexNumeric())
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
			                subcat_node.setProperty("weight",(h_wt + p_wt))
			                if(tag_name.equals(main_hash))
			                {
				                var rel = p_node --> "Belongs_To_Subcategory_Petition" --> subcat_node <
				                var rel_main = rel.setProperty("main", 1)
				                var rel_time = rel.setProperty("time", p_time_created)
				                var rel_in_wt = rel.setProperty("in_weight", p_wt)
				                var rel_out_wt = rel.setProperty("out_weight", 0)
			                }
			                else
			                {
				                var rel = p_node --> "Belongs_To_Subcategory_Petition" --> subcat_node <
				                var rel_main = rel.setProperty("main", 1)
				                var rel_time = rel.setProperty("time", p_time_created)
				                var rel_in_wt = rel.setProperty("in_weight", p_wt)
				                var rel_out_wt = rel.setProperty("out_weight", 0)
			                }
			                hash_weight_index.remove(subcat_node)
			                hash_weight_index += (subcat_node,"weight",new ValueContext( h_wt + p_wt ).indexNumeric())
			              }
			              else
			              {
			                val subcat_node = createNode(sub_category(tag_name,p_time_created,p_wt))
			                SubcatIndex += (subcat_node,"name",tag_name)
			                if(tag_name.equals(main_hash))
			                {
				                var rel = p_node --> "Belongs_To_Subcategory_Petition" --> subcat_node <
				                var rel_main = rel.setProperty("main", 1)
				                var rel_time = rel.setProperty("time", p_time_created)
				                var rel_in_wt = rel.setProperty("in_weight", p_wt)
				                var rel_out_wt = rel.setProperty("out_weight", 0)
			                }
			                else
			                {
				                var rel = p_node --> "Belongs_To_Subcategory_Petition" --> subcat_node <
				                var rel_main = rel.setProperty("main", 1)
				                var rel_time = rel.setProperty("time", p_time_created)
				                var rel_in_wt = rel.setProperty("in_weight", p_wt)
				                var rel_out_wt = rel.setProperty("out_weight", 0)
			                }
			                
			                var rel = subcat_node --> "Hashtag_Created_By" --> user_node <
			                var rel_time1 = rel.setProperty("time", time)
			                
			                hash_weight_index += (subcat_node,"weight",new ValueContext(p_wt).indexNumeric())
			              }
			            }
			         }
			       }
			       
			      //hash_list = p_subcat.split(",").toList.map(x => x.toLowerCase())
		          val index_data = dummy(p_title+" "+p_content)+" "+hash_list.mkString(" ")
		           
			       
			      val location_index = getNodeIndex("location").get
			      val all_cities = location_index.get("id","all").getSingle().getProperty("cities").toString().split(",").toList
			      val cities = index_data.split(" ").toList.distinct.intersect(all_cities)
			      val location_nodes = location_index.query( "id", "*" ).iterator().asScala.toList.filterNot( x => x.getProperty("location_id").equals("all"))
		          val loop = new Breaks; 
		          if(cities.size > 0)
			      {
			       for(each <- cities)
			       {
			         loop.breakable{
			           for(e <- location_nodes)
			           {
			             if(e.getProperty("cities").toString().split(",").contains(each))
			             {
			               p_node --> "Belongs_To_Location_Petition" --> e 
			               loop.break;
			             }
			           }
		             }
			       }
			     }
		          
		          val tagger = new MaxentTagger(
	                "/var/n4j/data/left3words-wsj-0-18.tagger")
	              
	              val tagged = tagger.tagString(p_content)
	              //val prev_nouns = user_node.getProperty("nouns").toString()
	              val noun_list =  tagged.split(" ").toList.map(x => x.split("/")).filter( x => (x.size == 2 && x(1).equals("NNP"))).map(x => x(0).toLowerCase())
	              var noun_hash_list = noun_list:::hash_list
	              p_content_index += (p_node,"p_content",(noun_hash_list.mkString(" ") + " " + dummy(p_title))  )
	              val proper_nouns = List("2013","2014","india","indian","reddy","rao","singh","world","pti","sunday","monday","tuesday","wednesday","thursday","friday","saturday","january","february","march","april","may","june","july","august","september","october","november","december")
                  noun_hash_list = noun_hash_list.filterNot(x => proper_nouns.contains(x) && x.length() < 3)
                  p_node.setProperty("p_content",noun_hash_list.mkString(" "))
              

		          ret = true
	          
          }
     ret     
    }
    
  }

def create_debate(user_name: String,  // unique user name
      d_id: String,  // unique debate id
      d_title: String,
      d_title_id: String,
      d_content: String,
      d_img_url: String,
      d_criteria: String,
      d_duration: Int,
      d_date: Int,
      d_time_created: Int,
      d_subcat: String,
      d_hashtags: String,
      is_edit: Int,
      is_closed: Int):Boolean =       
  {
    
   
    withTx {
    implicit neo =>
     
      
          var ret = false
          if(is_edit == 0)
          {
             var is_space = is_closed
	          val d_weight_index = getNodeIndex("debate_weight").get
	          val user_weight_index = getNodeIndex("user_weight").get
	          val hash_weight_index = getNodeIndex("hash_weight").get
	          val user_index = getNodeIndex("user").get
	          val user_node = user_index.get("id",user_name).getSingle()
	          val d_index = getNodeIndex("debate").get
	          var d_node = d_index.get("id",d_id).getSingle()
	          
		          
	          if(user_node != null && d_node == null)
		      {
	              user_node.setProperty("last_seen",d_time_created)
		          val u_wt = user_node.getProperty("weight").toString().toInt
		          val d_wt =Math.round(u_wt.asInstanceOf[Float]/200)
		          d_node = createNode(debate(d_id,d_title,d_title_id,d_img_url,d_content,d_criteria,d_duration,d_date,d_time_created,d_wt,0,1,0,0,is_space))
		     
		          val d_content_index = getNodeIndex("debate_content").get
		          val hash_list = d_subcat.split(",").toList.map(x => x.toLowerCase())
		     
		          val index_data = dummy(d_title+" "+d_content)+" "+hash_list.mkString(" ")
		          d_content_index += (d_node,"time",new ValueContext( d_date ).indexNumeric())
		     
		          
		          d_index += (d_node,"id",d_id)
		          d_weight_index += (d_node,"weight",new ValueContext( d_wt ).indexNumeric())
		          user_node.setProperty("weight",u_wt+5)
		          user_weight_index.remove(user_node)
		          user_weight_index += (user_node,"weight",new ValueContext( u_wt+5).indexNumeric())
		          var rel: org.neo4j.graphdb.Relationship = d_node --> "Debate_Written_By" --> user_node < //relating quickpost node and user node
		          var rel_time = rel.setProperty("time", d_time_created)
		          var rel_in_wt = rel.setProperty("in_weight", 5)
		          var rel_out_wt = rel.setProperty("out_weight", d_wt)
		          var rel1: org.neo4j.graphdb.Relationship = d_node --> "Debate_Moderated_By" --> user_node < //relating quickpost node and user node
		          var rel_time1 = rel1.setProperty("time", d_time_created)
		          var hash_tags = List[String]()
		          
	//	          if(!d_for.equals(""))
	//	          {
	//		          val for_list = d_for.split(",").toList
	//		          for(each <- for_list)
	//		          {
	//		            val f = user_index.get("id",each).getSingle()
	//		            if(f != null)
	//		            {
	//		              var rel1: org.neo4j.graphdb.Relationship = f --> "For" --> d_node < //relating quickpost node and user node
	//		              var rel_time1 = rel1.setProperty("time", d_time_created)
	//		            }
	//		          }
	//	          }
	//	          
	//	          if(!d_against.equals(""))
	//	          {
	//		          val against_list = d_against.split(",").toList
	//		          for(each <- against_list)
	//		          {
	//		            val a = user_index.get("id",each).getSingle()
	//		            if(a != null)
	//		            {
	//		              var rel1: org.neo4j.graphdb.Relationship = a --> "Against" --> d_node < //relating quickpost node and user node
	//		              var rel_time1 = rel1.setProperty("time", d_time_created)
	//		            }
	//		          }
	//	          }
		          
		          val TilesIndex = getNodeIndex("tiles").get
		          var cats = List[String]()
		          cats ::= "all"
		          if(is_space == 0)
		          {
			          for(each <- cats)
					     {
					       var news_node = TilesIndex.get("id",each).getSingle()
					       if(news_node == null)
					       {
					         val tiles_node =  createNode(tiles(each,d_id))
					         TilesIndex += (tiles_node,"id",each)
					       }
					       else
					       {
					         
						     var sorted_news  = List[org.neo4j.graphdb.Node]()
						     val news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     news_list ::= d_id
						     val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				             news_node.setProperty("value",news_list_trim)
					      
						    
					       }
					     }
		          }
		          
		          val SubcatIndex = getNodeIndex("sub_category").get
		          var tot_tags = List[ org.neo4j.graphdb.Node]()
	              if(d_subcat != "")
		          {
		              hash_tags = d_subcat.split(",").toList.filterNot( x => x.equals("")).map( x => x.toLowerCase())
		              val main_hash = hash_tags(0)
		              for(each <- hash_tags)
		              {
			              if(SubcatIndex.get("name",each).getSingle() != null)
			              {
			                
			                val subcat_node = SubcatIndex.get("name",each).getSingle()
			                tot_tags ::= subcat_node
			                val h_wt = subcat_node.getProperty("weight").toString().toInt
			                subcat_node.setProperty("weight",(h_wt + d_wt))
			                if(each.equals(main_hash))
			                {
				                rel = d_node --> "Belongs_To_Subcategory_Debate" --> subcat_node <
				                var rel_main = rel.setProperty("main", 1)
				                var rel_time = rel.setProperty("time", d_time_created)
				                var rel_in_wt = rel.setProperty("in_weight", d_wt)
				                var rel_out_wt = rel.setProperty("out_weight", 0)
			                }
			                else
			                {
				                rel = d_node --> "Belongs_To_Subcategory_Debate" --> subcat_node <
				                var rel_time = rel.setProperty("time", d_time_created)
				                var rel_in_wt = rel.setProperty("in_weight", d_wt)
				                var rel_out_wt = rel.setProperty("out_weight", 0)
			                }
			                hash_weight_index.remove(subcat_node)
			                hash_weight_index += (subcat_node,"weight",new ValueContext( h_wt + d_wt ).indexNumeric())
			              }
			              else
			              {
			                val subcat_node = createNode(sub_category(each,d_time_created,d_wt))
			                SubcatIndex += (subcat_node,"name",each)
			                if(each.equals(main_hash))
			                {
				                rel = d_node --> "Belongs_To_Subcategory_Debate" --> subcat_node <
				                var rel_main = rel.setProperty("main", 1)
				                var rel_time = rel.setProperty("time", d_time_created)
				                var rel_in_wt = rel.setProperty("in_weight", d_wt)
				                var rel_out_wt = rel.setProperty("out_weight", 0)
			                }
			                else
			                {
				                rel = d_node --> "Belongs_To_Subcategory_Debate" --> subcat_node <
				                var rel_time = rel.setProperty("time", d_time_created)
				                var rel_in_wt = rel.setProperty("in_weight", d_wt)
				                var rel_out_wt = rel.setProperty("out_weight", 0)
			                }
			                
			                rel = subcat_node --> "Hashtag_Created_By" --> user_node <
			                var rel_time1 = rel.setProperty("time", d_time_created)
			                
			                hash_weight_index += (subcat_node,"weight",new ValueContext( d_wt).indexNumeric())
			                tot_tags ::= subcat_node
			              }
		              }
		            }
		          
		          if(d_hashtags != "")
		          {
		            val d_tags = d_hashtags.split(",").distinct.toList.filterNot( x => x.equals("")).map( x => x.toLowerCase())
		            for(tag_name <- d_tags)
		            {
		              
		              if(tag_name != "")
		              {
		                
		                if(SubcatIndex.get("name",tag_name).getSingle() != null)
		                {
		                  val tag_node = SubcatIndex.get("name",tag_name).getSingle()
		                  tot_tags ::= tag_node
		                  val h_wt = tag_node.getProperty("weight").toString().toInt
		                  tag_node.setProperty("weight",(h_wt + 10))
		                  rel = tag_node --> "Tag_Of_Debate" --> d_node <
		                  var rel_time = rel.setProperty("time", d_time_created)
		                  var rel_in_wt = rel.setProperty("in_weight", 0)
		                  var rel_out_wt = rel.setProperty("out_weight", 10)
		                  hash_weight_index.remove(tag_node)
		                  hash_weight_index += (tag_node,"weight",new ValueContext( h_wt + 10 ).indexNumeric())
		                  
		                }
		                else
		                {
		                  val tag_node = createNode(sub_category(tag_name,d_time_created,10))
		                  SubcatIndex += (tag_node,"name",tag_name)
		                  rel = tag_node --> "Tag_Of_Debate" --> d_node <
		                  var rel_time = rel.setProperty("time", d_time_created)
		                  var rel_in_wt = rel.setProperty("in_weight", 0)
		                  var rel_out_wt = rel.setProperty("out_weight", 10)
		                  hash_weight_index += (tag_node,"weight",new ValueContext( 10 ).indexNumeric())
		                  rel = tag_node --> "Hashtag_Created_By" --> user_node <
		                  var rel_time1 = rel.setProperty("time", d_time_created)
		                  tot_tags ::= tag_node
		                  
		                }
		              }
		            }
		          
			      }
		          var flag_list = List[org.neo4j.graphdb.Node]()
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
		          
		          
		          val tagger = new MaxentTagger(
	                "/var/n4j/data/left3words-wsj-0-18.tagger")
	              val tagged = tagger.tagString(d_content)
	              //val prev_nouns = user_node.getProperty("nouns").toString()
	              val noun_list =  tagged.split(" ").toList.map(x => x.split("/")).filter( x => (x.size == 2 && x(1).equals("NNP"))).map(x => x(0).toLowerCase())
	              var noun_hash_list = noun_list:::hash_list
	              d_content_index += (d_node,"d_content",(noun_hash_list.mkString(" ") + " " + dummy(d_title))  )
	              val proper_nouns = List("2013","2014","india","indian","reddy","rao","singh","world","pti","sunday","monday","tuesday","wednesday","thursday","friday","saturday","january","february","march","april","may","june","july","august","september","october","november","december")
	              noun_hash_list = noun_hash_list.filterNot(x => proper_nouns.contains(x) && x.length() < 3)
	              d_node.setProperty("d_content",noun_hash_list.mkString(" "))
	              featured_item_q("D",d_id)
		          ret = true
		          
	          }
          }
          
          else
          {
            val d_weight_index = getNodeIndex("debate_weight").get
          val user_weight_index = getNodeIndex("user_weight").get
          val hash_weight_index = getNodeIndex("hash_weight").get
          val user_index = getNodeIndex("user").get
          val user_node = user_index.get("id",user_name).getSingle()
          val d_index = getNodeIndex("debate").get
          var d_node = d_index.get("id",d_id).getSingle()
          val TilesIndex = getNodeIndex("tiles").get
                         val FeaturedTilesIndex = getNodeIndex("featured_tiles").get
                         val HeadlinesIndex = getNodeIndex("headlines").get
	          
          //var ret = false
          if(user_node != null && d_node != null)
	      {
            
              val feu = d_node.getProperty("approved").toString().toInt
              val head = d_node.getProperty("head").toString().toInt
               val space = d_node.getProperty("space").toString().toInt
              user_node.setProperty("last_seen",d_time_created)
              val time = d_node.getProperty("time_created").toString().toInt
              val d_wt = d_node.getProperty("weight").toString().toInt
              d_node.setProperty("d_title",d_title)
              d_node.setProperty("d_title_id",d_title_id)
              d_node.setProperty("d_img_url",d_img_url)
              d_node.setProperty("d_content",d_content)
              d_node.setProperty("d_criteria",d_criteria)
              d_node.setProperty("d_duration",d_duration)
              d_node.setProperty("d_date",d_date)
              
	          
	          val d_content_index = getNodeIndex("debate_content").get
	          d_content_index.remove(d_node)
	          d_content_index += (d_node,"time",new ValueContext( d_date ).indexNumeric())
	          
	          
	          
	                     var news_node1 = TilesIndex.get("id","all").getSingle()
	                     if(news_node1 != null)
				         {
					         var news = news_node1.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     if(news_list.contains(d_id))
					         {
					           news_list = news_list - d_id
					           val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				               news_node1.setProperty("value",news_list_trim)
					         }
				         }
				         
				         news_node1 = FeaturedTilesIndex.get("id","all").getSingle()
				         if(news_node1 != null)
				         {
					         var news = news_node1.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     if(news_list.contains(d_id))
					         {
					           news_list = news_list - d_id
					           val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				               news_node1.setProperty("value",news_list_trim)
					         }
				         }
				         
				         news_node1 = HeadlinesIndex.get("id","all").getSingle()
				         if(news_node1 != null)
				         {
					         var news = news_node1.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     if(news_list.contains(d_id))
					         {
					           news_list = news_list - d_id
					           val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				               news_node1.setProperty("value",news_list_trim)
					         }
				         }
              
              
	           val SubcatIndex = getNodeIndex("sub_category").get
	           val tags_list = d_hashtags.split(",").distinct.toList.filterNot( x => x.equals("")).map( x => x.toLowerCase())
		       var old_tags = List[Any]()
		       var rels = d_node.getRelationships("Tag_Of_Debate").asScala
		       for(each <- rels)
		       {
		           val tag = each.getOtherNode(d_node).getProperty("name").toString()
		           old_tags ::= tag
		           if(!tags_list.contains(tag))
		           {
		             val tag_node = each.getOtherNode(d_node)
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
		                  var rel = tag_node --> "Tag_Of_Debate" --> d_node <
		                  var rel_time = rel.setProperty("time", d_time_created)
		                  var rel_in_wt = rel.setProperty("in_weight", 0)
		                  var rel_out_wt = rel.setProperty("out_weight", 10)
		                  hash_weight_index.remove(tag_node)
		                  hash_weight_index += (tag_node,"weight",new ValueContext( h_wt + 10 ).indexNumeric())
		                  
		                }
		                else
		                {
		                  val tag_node = createNode(sub_category(tag_name,d_time_created,10))
		                  SubcatIndex += (tag_node,"name",tag_name)
		                  var rel = tag_node --> "Tag_Of_Debate" --> d_node <
		                  var rel_time = rel.setProperty("time", d_time_created)
		                  var rel_in_wt = rel.setProperty("in_weight", 0)
		                  var rel_out_wt = rel.setProperty("out_weight", 10)
		                  rel = tag_node --> "Hashtag_Created_By" --> user_node <
		                  var rel_time1 = rel.setProperty("time", time)
		                  hash_weight_index += (tag_node,"weight",new ValueContext( 10 ).indexNumeric())
		                  
		                }
		              }
		         }
		       }
		       
		       var hash_list = d_subcat.split(",").toList.filterNot( x => x.equals("")).map( x => x.toLowerCase())
			   var main_hash = hash_list(0)
		       var old_hash = List[Any]()
		       var r:org.neo4j.graphdb.Relationship = null
		       rels = d_node.getRelationships("Belongs_To_Subcategory_Debate").asScala
		       for(each <- rels)
		       {
		           val tag = each.getOtherNode(d_node).getProperty("name").toString()
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
		             val tag_node = each.getOtherNode(d_node)
		             val h_wt = tag_node.getProperty("weight").toString().toInt
		             tag_node.setProperty("weight",(h_wt - d_wt))
		             hash_weight_index.remove(tag_node)
		             hash_weight_index += (tag_node,"weight",new ValueContext( h_wt - d_wt ).indexNumeric())
		             each.delete() 
		           }
		         
		        	 
		       }
		       val cur_time = (System.currentTimeMillis() /1000).toInt
		       var news_node = TilesIndex.get("id","all").getSingle()
		               if(space == 0)
		               {
					       if(news_node == null)
					       {
					         val tiles_node =  createNode(tiles("all",d_id))
					         TilesIndex += (tiles_node,"id","all")
					       }
					       else
					       {
					         
						     var sorted_news  = List[org.neo4j.graphdb.Node]()
						     val news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     if( (d_date + 3600) > cur_time)
						     {
						    	 news_list ::= d_id
						     }
						     else
						     {
						       news_list :+= d_id
						     }
						     val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				             news_node.setProperty("value",news_list_trim)
					       }
		               }
	    	 		   
	    	 		   if(feu == 1)
		    	 	   {
			    	 	   news_node = FeaturedTilesIndex.get("id","all").getSingle()
					       if(news_node == null)
					       {
					         val tiles_node =  createNode(featured_tiles("all",d_id))
					         FeaturedTilesIndex += (tiles_node,"id","all")
					       }
					       else
					       {
					         
						     var sorted_news  = List[org.neo4j.graphdb.Node]()
						     val news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     if( (d_date + 3600) > cur_time)
						     {
						    	 news_list ::= d_id
						     }
						     else
						     {
						       news_list :+= d_id
						     }
						     val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				             news_node.setProperty("value",news_list_trim)
					      
						    
					       }
		    	 	   }
	    	 		   if(head == 1)
		    	 	   {
			    	 	   news_node = HeadlinesIndex.get("id","all").getSingle()
					       if(news_node == null)
					       {
					         val tiles_node =  createNode(headlines("all",d_id))
					         HeadlinesIndex += (tiles_node,"id","all")
					       }
					       else
					       {
					         
						     var sorted_news  = List[org.neo4j.graphdb.Node]()
						     val news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     if( (d_date + 3600) > cur_time)
						     {
						    	 news_list ::= d_id
						     }
						     else
						     {
						       news_list :+= d_id
						     }
						     val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				             news_node.setProperty("value",news_list_trim)
					      
						    
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
		                subcat_node.setProperty("weight",(h_wt + d_wt))
		                if(tag_name.equals(main_hash))
		                {
			                var rel = d_node --> "Belongs_To_Subcategory_Debate" --> subcat_node <
			                var rel_main = rel.setProperty("main", 1)
			                var rel_time = rel.setProperty("time", d_time_created)
			                var rel_in_wt = rel.setProperty("in_weight", d_wt)
			                var rel_out_wt = rel.setProperty("out_weight", 0)
		                }
		                else
		                {
			                var rel = d_node --> "Belongs_To_Subcategory_Debate" --> subcat_node <
			                var rel_main = rel.setProperty("main", 1)
			                var rel_time = rel.setProperty("time", d_time_created)
			                var rel_in_wt = rel.setProperty("in_weight", d_wt)
			                var rel_out_wt = rel.setProperty("out_weight", 0)
		                }
		                hash_weight_index.remove(subcat_node)
		                hash_weight_index += (subcat_node,"weight",new ValueContext( h_wt + d_wt ).indexNumeric())
		              }
		              else
		              {
		                val subcat_node = createNode(sub_category(tag_name,d_time_created,d_wt))
		                SubcatIndex += (subcat_node,"name",tag_name)
		                if(tag_name.equals(main_hash))
		                {
			                var rel = d_node --> "Belongs_To_Subcategory_Debate" --> subcat_node <
			                var rel_main = rel.setProperty("main", 1)
			                var rel_time = rel.setProperty("time", d_time_created)
			                var rel_in_wt = rel.setProperty("in_weight", d_wt)
			                var rel_out_wt = rel.setProperty("out_weight", 0)
		                }
		                else
		                {
			                var rel = d_node --> "Belongs_To_Subcategory_Debate" --> subcat_node <
			                var rel_main = rel.setProperty("main", 1)
			                var rel_time = rel.setProperty("time", d_time_created)
			                var rel_in_wt = rel.setProperty("in_weight", d_wt)
			                var rel_out_wt = rel.setProperty("out_weight", 0)
		                }
		                
		                var rel = subcat_node --> "Hashtag_Created_By" --> user_node <
		                var rel_time1 = rel.setProperty("time", time)
		                
		                hash_weight_index += (subcat_node,"weight",new ValueContext(d_wt).indexNumeric())
		              }
		            }
		         }
		         
		         else if(old_hash.contains(tag_name) && tag_name.equals(main_hash))
		         {
		           r.setProperty("main", 1)
		         }
		       }
		       
		      //hash_list = p_subcat.split(",").toList.map(x => x.toLowerCase())
	          val index_data = dummy(d_title+" "+d_content)+" "+hash_list.mkString(" ")
	           
		       
		      
	          
	          val tagger = new MaxentTagger(
                "/var/n4j/data/left3words-wsj-0-18.tagger")
              
              val tagged = tagger.tagString(d_content)
              //val prev_nouns = user_node.getProperty("nouns").toString()
              val noun_list =  tagged.split(" ").toList.map(x => x.split("/")).filter( x => (x.size == 2 && x(1).equals("NNP"))).map(x => x(0).toLowerCase())
              var noun_hash_list = noun_list:::hash_list
              d_content_index += (d_node,"d_content",(noun_hash_list.mkString(" ") + " " + dummy(d_title))  )
              val proper_nouns = List("2013","2014","india","indian","reddy","rao","singh","world","pti","sunday","monday","tuesday","wednesday","thursday","friday","saturday","january","february","march","april","may","june","july","august","september","october","november","december")
              noun_hash_list = noun_hash_list.filterNot(x => proper_nouns.contains(x) && x.length() < 3)
              d_node.setProperty("d_content",noun_hash_list.mkString(" "))
//              if(prev_nouns.equals(""))
//              {
//                val noun_map = noun_hash_list.groupBy(x=>x).mapValues(x=>x.length.toDouble)
//                user_node.setProperty("nouns",JSONObject(noun_map).toString())
//              }
//              else
//              {
//                val noun_map = noun_hash_list.groupBy(x=>x).mapValues(x=>x.length)
//                val prev_map = JSON.parseFull(prev_nouns).get.asInstanceOf[Map[String,Double]]
//                val merge_map = noun_map ++ prev_map.map{ case (k,v) => k -> (v + noun_map.getOrElse(k,0)) }
//                user_node.setProperty("nouns",JSONObject(merge_map).toString())
//              }
	          ret = true
          }
          }
     ret     
    }
    
  }

def featured_item_q(item_type: String,
    item_id: String
    ):Boolean=
    { 
    
    withTx {
      implicit neo =>
         // Relating article to the categories
	     
	     var ret = false
	     val TilesIndex = getNodeIndex("featured_tiles").get
	     if(item_type.toLowerCase().equals("t"))
	     {
	         val TownhallIndex = getNodeIndex("townhall").get
		     val t_node = TownhallIndex.get("id",item_id).getSingle()
		     if(t_node != null)
		     {
		         val feu = t_node.getProperty("approved").toString().toInt
		         if(feu == 0)
		         {
			         t_node.setProperty("approved",1)
				     var cat = "all"
				     //val cur_time = a_time_created
				     //val t = cur_time - 86400
				       var news_node = TilesIndex.get("id",cat).getSingle()
				       if(news_node == null)
				       {
				         val tiles_node =  createNode(featured_tiles(cat,item_id))
				         TilesIndex += (tiles_node,"id",cat)
				       }
				       else
				       {
				         
					     var sorted_news  = List[org.neo4j.graphdb.Node]()
					     val news = news_node.getProperty("value").toString()
					     var news_list = news.split(",").toList
					     news_list ::= item_id
					     val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
			             news_node.setProperty("value",news_list_trim)
				      
					    
				       }
				     
			             var latest_node = TilesIndex.get("id","latest").getSingle()
				         val latest = latest_node.getProperty("value").toString()
				         var latest_list = latest.split(",").toList
						 latest_list ::= item_id
						 val latest_list_trim = latest_list.slice(0,30).mkString(",").stripPrefix(",").stripSuffix(",").trim
				         latest_node.setProperty("value",latest_list_trim)
			         
		         }
		         
		         else
		         {
			         t_node.setProperty("approved",0)
			         // unpinning the article
			         pin_item_q("T",item_id,"","")
			           
			         // removing the article from the exclusive articles
//			         exclusive_article(item_id,"")
				     var cat = "all"
				     //val cur_time = a_time_created
				     //val t = cur_time - 86400
				     
				         var news_node = TilesIndex.get("id",cat).getSingle()
				         var sorted_news  = List[org.neo4j.graphdb.Node]()
					     val news = news_node.getProperty("value").toString()
					     var news_list = news.split(",").toList
					     if(news_list.contains(item_id))
				         {
				           news_list = news_list - item_id
				           val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
			               news_node.setProperty("value",news_list_trim)
				         }
					     
				      
					    
				       
				     
			         var latest_node = TilesIndex.get("id","latest").getSingle()
			         val latest = latest_node.getProperty("value").toString()
			         var latest_list = latest.split(",").toList
					 if(latest_list.contains(item_id))
			         {
					   
					   latest_list = latest_list - item_id
				       val latest_list_trim = latest_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
			           latest_node.setProperty("value",latest_list_trim)
			           
			           
			         }
					 
		         }
			     ret = true
		     }
	     }
	     
	     else
	     {
	         val DebateIndex = getNodeIndex("debate").get
		     val d_node = DebateIndex.get("id",item_id).getSingle()
		     if(d_node != null)
		     {
		         val feu = d_node.getProperty("approved").toString().toInt
		         if(feu == 0)
		         {
			         d_node.setProperty("approved",1)
				     var cat = "all"
				     //val cur_time = a_time_created
				     //val t = cur_time - 86400
				       var news_node = TilesIndex.get("id",cat).getSingle()
				       if(news_node == null)
				       {
				         val tiles_node =  createNode(featured_tiles(cat,item_id))
				         TilesIndex += (tiles_node,"id",cat)
				       }
				       else
				       {
				         
					     var sorted_news  = List[org.neo4j.graphdb.Node]()
					     val news = news_node.getProperty("value").toString()
					     var news_list = news.split(",").toList
					     news_list ::= item_id
					     val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
			             news_node.setProperty("value",news_list_trim)
				      
					    
				       }
				     
			             var latest_node = TilesIndex.get("id","latest").getSingle()
				         val latest = latest_node.getProperty("value").toString()
				         var latest_list = latest.split(",").toList
						 latest_list ::= item_id
						 val latest_list_trim = latest_list.slice(0,30).mkString(",").stripPrefix(",").stripSuffix(",").trim
				         latest_node.setProperty("value",latest_list_trim)
			         
		         }
		         
		         else
		         {
			         d_node.setProperty("approved",0)
			         // unpinning the article
			         pin_item_q("D",item_id,"","")
			           
			         // removing the article from the exclusive articles
//			         exclusive_article(item_id,"")
				     var cat = "all"
				     //val cur_time = a_time_created
				     //val t = cur_time - 86400
				     
				         var news_node = TilesIndex.get("id",cat).getSingle()
				         var sorted_news  = List[org.neo4j.graphdb.Node]()
					     val news = news_node.getProperty("value").toString()
					     var news_list = news.split(",").toList
					     if(news_list.contains(item_id))
				         {
				           news_list = news_list - item_id
				           val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
			               news_node.setProperty("value",news_list_trim)
				         }
					     
				      
					    
				       
				     
			         var latest_node = TilesIndex.get("id","latest").getSingle()
			         val latest = latest_node.getProperty("value").toString()
			         var latest_list = latest.split(",").toList
					 if(latest_list.contains(item_id))
			         {
					   
					   latest_list = latest_list - item_id
				       val latest_list_trim = latest_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
			           latest_node.setProperty("value",latest_list_trim)
			           
			           
			         }
					 
		         }
			     ret = true
		     }
	     }
	     
    
    ret
    }
    }


def debate_action(user_name: String,  // unique user name
    d_id: String,
    action_type: String,  // P/Q/A
    qtn_id: String,
    qtn_content: String,
    time: Int
    
      ):Boolean =       
  {
    
   
    withTx {
    implicit neo =>
      
          var ret = false
          val user_index = getNodeIndex("user").get
          val user_node = user_index.get("id",user_name).getSingle()
          
          val d_index = getNodeIndex("debate").get
          var d_node = d_index.get("id",d_id).getSingle()
          if(user_node != null && d_node != null)
	      {
            if(action_type.toLowerCase().equals("p"))
            {
              val rels = user_node.getRelationships("Participated_In_Debate").asScala.toList.map(_.getEndNode())
              if(!rels.contains(d_node))
              {
                var rel: org.neo4j.graphdb.Relationship = user_node --> "Participated_In_Debate" --> d_node < //relating quickpost node and user node
	            var rel_time = rel.setProperty("time", time)
              }
              
	          ret = true
            }
            else if(action_type.toLowerCase().equals("q"))
            {
              var rel: org.neo4j.graphdb.Relationship = user_node --> "Asked_Debate_Question" --> d_node < //relating quickpost node and user node
	          var rel_time = rel.setProperty("time", time)
	          var q_id = rel.setProperty("q_id", qtn_id)
	          var q_content = rel.setProperty("q_content", qtn_content)
	          ret = true
            }
            
            else if(action_type.toLowerCase().equals("a"))
            {
              var rel: org.neo4j.graphdb.Relationship = user_node --> "Started_Debate_Argument" --> d_node < //relating quickpost node and user node
	          var rel_time = rel.setProperty("time", time)
	          var a_id = rel.setProperty("a_id", qtn_id)
	          var a_content = rel.setProperty("a_content", qtn_content)
	          ret = true
            }
            
	      }
      
       
    ret   
    }
  }

def debate_comment(d_id: String,
    user_name: String,
    comment: String,
    time: Int):Boolean = 
    {
  
       withTx {
       implicit neo =>
         
          val user_index = getNodeIndex("user").get
          val user_node = user_index.get("id",user_name).getSingle()
          val d_index = getNodeIndex("debate").get
          var d_node = d_index.get("id",d_id).getSingle()
          
	          
          var ret = false
          if(user_node != null && d_node != null)
	      {
            var rel: org.neo4j.graphdb.Relationship = user_node --> "Commented_On_Debate" --> d_node < //relating quickpost node and user node
		    var rel_time = rel.setProperty("time", time)
		    var rel_msg = rel.setProperty("d_comment", comment)
		    ret = true
	      }
        
          
       ret   
       }
  
    }


def debate_participate(d_id: String,
    user_name: String,
    grp_name: String,  // For/Against
    message: String,
    time: Int):Boolean = 
    {
  
       withTx {
       implicit neo =>
         
          val user_index = getNodeIndex("user").get
          val user_node = user_index.get("id",user_name).getSingle()
          val d_index = getNodeIndex("debate").get
          var d_node = d_index.get("id",d_id).getSingle()
          
	          
          var ret = false
          if(user_node != null && d_node != null)
	      {
            var rel: org.neo4j.graphdb.Relationship = user_node --> grp_name --> d_node < //relating quickpost node and user node
		    var rel_time = rel.setProperty("time", time)
		    var rel_msg = rel.setProperty("d_message", message)
		    var rel_status = rel.setProperty("d_status", 0)
		    ret = true
	      }
        
          
       ret   
       }
  
    }


def debate_shortlist_guests(d_id: String,
    grp_name: String,  // For/Against
    user_names: String):Boolean = 
    {
  
       withTx {
       implicit neo =>
         
          
          val d_index = getNodeIndex("debate").get
          var d_node = d_index.get("id",d_id).getSingle()
          
	          
          var ret = false
          if(d_node != null && !user_names.equals(""))
	      {
            
            var rels = d_node.getRelationships(grp_name).asScala.toList
            for(each <- rels)
	        {
	          each.setProperty("d_status",0)
	          
	        }
            val user_index = getNodeIndex("user").get
	        var guest_list = user_names.split(",").toList.map(x => user_index.get("id",x).getSingle()).filter( y => y != null)
	        for(each <- rels)
	        {
	          if(guest_list.contains(each.getStartNode()))
	          {
	            each.setProperty("d_status",1)
	          }
	        }
            
	      ret = true      
	      }
        
          
       ret   
       }
  
    }

def debate_change_moderator(
    d_id: String,
    d_moderators: String,
    time: Int
    ):Boolean =       
  {
    
   
    withTx {
    implicit neo =>
      
       		  var ret = false
              val user_index = getNodeIndex("user").get
              val d_index = getNodeIndex("debate").get
              var d_node = d_index.get("id",d_id).getSingle()
	          if(d_node != null && !d_moderators.equals(""))
	          {
	            val prev_mod_rels = d_node.getRelationships("Debate_Moderated_By").asScala.toList
	            val prev_mods = d_node.getRelationships("Debate_Moderated_By").asScala.toList.map( x => x.getEndNode())
	            var mod_list = d_moderators.split(",").toList.map(x => user_index.get("id",x).getSingle()).filter( y => y != null)
	            
	            for(each <- prev_mod_rels)
	            {
	              if(!mod_list.contains(each.getEndNode()))
	              {
	                each.delete()
	              }
	            }
	            
	            mod_list = mod_list.filterNot(x => prev_mods.contains(x))
	            for(each <- mod_list)
	            {
	              var rel: org.neo4j.graphdb.Relationship = d_node --> "Debate_Moderated_By" --> each < //relating quickpost node and user node
	              var rel_time = rel.setProperty("time", time)
	            }
	            ret = true
	          }
      
       
    ret   
    }
  }



def edit_debate(user_name: String,  // unique user name
      d_id: String,  // unique petition id
      d_title: String,
      d_title_id: String,
      d_content: String,
      d_img_url: String,
      d_criteria: String,
      d_duration: Int,
      d_date: Int,
      d_time_created: Int,
      d_subcat: String,
      d_hashtags: String
      ):Boolean =       
  {
    
   
    withTx {
    implicit neo =>
     
          val d_weight_index = getNodeIndex("debate_weight").get
          val user_weight_index = getNodeIndex("user_weight").get
          val hash_weight_index = getNodeIndex("hash_weight").get
          val user_index = getNodeIndex("user").get
          val user_node = user_index.get("id",user_name).getSingle()
          val d_index = getNodeIndex("debate").get
          var d_node = d_index.get("id",d_id).getSingle()
          
	          
          var ret = false
          if(user_node != null && d_node != null)
	      {
              user_node.setProperty("last_seen",d_time_created)
              val time = d_node.getProperty("time_created").toString().toInt
              val d_wt = d_node.getProperty("weight").toString().toInt
              d_node.setProperty("d_title",d_title)
              d_node.setProperty("d_title_id",d_title_id)
              d_node.setProperty("d_img_url",d_img_url)
              d_node.setProperty("d_content",d_content)
              d_node.setProperty("d_criteria",d_criteria)
              d_node.setProperty("d_duration",d_duration)
              d_node.setProperty("d_date",d_date)
              
	          
	          val d_content_index = getNodeIndex("debate_content").get
	          d_content_index.remove(d_node)
	          d_content_index += (d_node,"time",new ValueContext( d_date ).indexNumeric())
	          
	          
	         
              
              
	           val SubcatIndex = getNodeIndex("sub_category").get
	           val tags_list = d_hashtags.split(",").distinct.toList.map( x => x.toLowerCase())
		       var old_tags = List[Any]()
		       var rels = d_node.getRelationships("Tag_Of_Debate").asScala
		       for(each <- rels)
		       {
		           val tag = each.getOtherNode(d_node).getProperty("name").toString()
		           old_tags ::= tag
		           if(!tags_list.contains(tag))
		           {
		             val tag_node = each.getOtherNode(d_node)
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
		                  var rel = tag_node --> "Tag_Of_Debate" --> d_node <
		                  var rel_time = rel.setProperty("time", d_time_created)
		                  var rel_in_wt = rel.setProperty("in_weight", 0)
		                  var rel_out_wt = rel.setProperty("out_weight", 10)
		                  hash_weight_index.remove(tag_node)
		                  hash_weight_index += (tag_node,"weight",new ValueContext( h_wt + 10 ).indexNumeric())
		                  
		                }
		                else
		                {
		                  val tag_node = createNode(sub_category(tag_name,d_time_created,10))
		                  SubcatIndex += (tag_node,"name",tag_name)
		                  var rel = tag_node --> "Tag_Of_Debate" --> d_node <
		                  var rel_time = rel.setProperty("time", d_time_created)
		                  var rel_in_wt = rel.setProperty("in_weight", 0)
		                  var rel_out_wt = rel.setProperty("out_weight", 10)
		                  rel = tag_node --> "Hashtag_Created_By" --> user_node <
		                  var rel_time1 = rel.setProperty("time", time)
		                  hash_weight_index += (tag_node,"weight",new ValueContext( 10 ).indexNumeric())
		                  
		                }
		              }
		         }
		       }
		       
		       var hash_list = d_subcat.split(",").toList.map( x => x.toLowerCase())
			   var main_hash = hash_list(0)
		       var old_hash = List[Any]()
		       rels = d_node.getRelationships("Belongs_To_Subcategory_Debate").asScala
		       for(each <- rels)
		       {
		           val tag = each.getOtherNode(d_node).getProperty("name").toString()
		           if(each.hasProperty("main") && !tag.equals(main_hash))
			       {
			          each.removeProperty("main")
			             
			       }
		           old_hash ::= tag
		           if(!hash_list.contains(tag))
		           {
		             val tag_node = each.getOtherNode(d_node)
		             val h_wt = tag_node.getProperty("weight").toString().toInt
		             tag_node.setProperty("weight",(h_wt - d_wt))
		             hash_weight_index.remove(tag_node)
		             hash_weight_index += (tag_node,"weight",new ValueContext( h_wt - d_wt ).indexNumeric())
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
		                subcat_node.setProperty("weight",(h_wt + d_wt))
		                if(tag_name.equals(main_hash))
		                {
			                var rel = d_node --> "Belongs_To_Subcategory_Debate" --> subcat_node <
			                var rel_main = rel.setProperty("main", 1)
			                var rel_time = rel.setProperty("time", d_time_created)
			                var rel_in_wt = rel.setProperty("in_weight", d_wt)
			                var rel_out_wt = rel.setProperty("out_weight", 0)
		                }
		                else
		                {
			                var rel = d_node --> "Belongs_To_Subcategory_Debate" --> subcat_node <
			                var rel_main = rel.setProperty("main", 1)
			                var rel_time = rel.setProperty("time", d_time_created)
			                var rel_in_wt = rel.setProperty("in_weight", d_wt)
			                var rel_out_wt = rel.setProperty("out_weight", 0)
		                }
		                hash_weight_index.remove(subcat_node)
		                hash_weight_index += (subcat_node,"weight",new ValueContext( h_wt + d_wt ).indexNumeric())
		              }
		              else
		              {
		                val subcat_node = createNode(sub_category(tag_name,d_time_created,d_wt))
		                SubcatIndex += (subcat_node,"name",tag_name)
		                if(tag_name.equals(main_hash))
		                {
			                var rel = d_node --> "Belongs_To_Subcategory_Debate" --> subcat_node <
			                var rel_main = rel.setProperty("main", 1)
			                var rel_time = rel.setProperty("time", d_time_created)
			                var rel_in_wt = rel.setProperty("in_weight", d_wt)
			                var rel_out_wt = rel.setProperty("out_weight", 0)
		                }
		                else
		                {
			                var rel = d_node --> "Belongs_To_Subcategory_Debate" --> subcat_node <
			                var rel_main = rel.setProperty("main", 1)
			                var rel_time = rel.setProperty("time", d_time_created)
			                var rel_in_wt = rel.setProperty("in_weight", d_wt)
			                var rel_out_wt = rel.setProperty("out_weight", 0)
		                }
		                
		                var rel = subcat_node --> "Hashtag_Created_By" --> user_node <
		                var rel_time1 = rel.setProperty("time", time)
		                
		                hash_weight_index += (subcat_node,"weight",new ValueContext(d_wt).indexNumeric())
		              }
		            }
		         }
		       }
		       
		      //hash_list = p_subcat.split(",").toList.map(x => x.toLowerCase())
	          val index_data = dummy(d_title+" "+d_content)+" "+hash_list.mkString(" ")
	           
		       
		      
	          
	          val tagger = new MaxentTagger(
                "/var/n4j/data/left3words-wsj-0-18.tagger")
              
              val tagged = tagger.tagString(d_content)
              //val prev_nouns = user_node.getProperty("nouns").toString()
              val noun_list =  tagged.split(" ").toList.map(x => x.split("/")).filter( x => (x.size == 2 && x(1).equals("NNP"))).map(x => x(0).toLowerCase())
              var noun_hash_list = noun_list:::hash_list
              d_content_index += (d_node,"d_content",(noun_hash_list.mkString(" ") + " " + dummy(d_title))  )
              val proper_nouns = List("2013","2014","india","indian","reddy","rao","singh","world","pti","sunday","monday","tuesday","wednesday","thursday","friday","saturday","january","february","march","april","may","june","july","august","september","october","november","december")
              noun_hash_list = noun_hash_list.filterNot(x => proper_nouns.contains(x) && x.length() < 3)
              
//              if(prev_nouns.equals(""))
//              {
//                val noun_map = noun_hash_list.groupBy(x=>x).mapValues(x=>x.length.toDouble)
//                user_node.setProperty("nouns",JSONObject(noun_map).toString())
//              }
//              else
//              {
//                val noun_map = noun_hash_list.groupBy(x=>x).mapValues(x=>x.length)
//                val prev_map = JSON.parseFull(prev_nouns).get.asInstanceOf[Map[String,Double]]
//                val merge_map = noun_map ++ prev_map.map{ case (k,v) => k -> (v + noun_map.getOrElse(k,0)) }
//                user_node.setProperty("nouns",JSONObject(merge_map).toString())
//              }
	          ret = true
          }
     ret     
    }
    
  }

  // called when an article is pinned
  def pin_item_q(
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


  def dummy(word: String): String = 
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


// Triggered when a new quick post is edited
def edit_quickpost(user_name: String,
      qp_id: String,
      qp_content: String,
      qp_hashtags: String,
      qp_users: String,
      qp_time_created: Int
      ):Boolean =
  {
    
    withTx {
    implicit neo =>
       val user_index = getNodeIndex("user").get
       val QuickpostIndex = getNodeIndex("quickpost").get
       val qp_content_index = getNodeIndex("quickpost_content").get
       val SubcatIndex = getNodeIndex("sub_category").get
       val hash_weight_index = getNodeIndex("hash_weight").get
       val quickpost_node = QuickpostIndex.get("id",qp_id).getSingle()
       val user_node = user_index.get("id",user_name).getSingle()
       var ret = false
       if(user_node != null && quickpost_node != null)
	   {
           user_node.setProperty("last_seen",qp_time_created)
	       quickpost_node.setProperty("qp_content",qp_content)
	       qp_content_index.remove(quickpost_node)
	       val tags_list = qp_hashtags.split(",").distinct.toList.map( x => x.toLowerCase())
	       val rels = quickpost_node.getRelationships("Tag_Of_Quickpost","User_Of_Quickpost").asScala
	       var old_tags = List[Any]()
	       for(each <- rels)
	       {
	           val node = each.getOtherNode(quickpost_node)
	           if(node.getProperty("__CLASS__").equals("Saddahaq.sub_category"))
	           {
		           val tag = node.getProperty("name").toString()
		           old_tags ::= tag
		           if(!tags_list.contains(tag))
		           {
		             val tag_node = each.getOtherNode(quickpost_node)
		             val h_wt = tag_node.getProperty("weight").toString().toInt
		             tag_node.setProperty("weight",(h_wt - 10))
		             hash_weight_index.remove(tag_node)
		             hash_weight_index += (tag_node,"weight",new ValueContext( h_wt - 10 ).indexNumeric())
		             each.delete() 
		           }
	           }
	           
	           else
	           {
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
	                  var rel = tag_node --> "Tag_Of_Quickpost" --> quickpost_node <
	                  var rel_time = rel.setProperty("time", qp_time_created)
	                  var rel_in_wt = rel.setProperty("in_weight", 0)
	                  var rel_out_wt = rel.setProperty("out_weight", 10)
	                  hash_weight_index.remove(tag_node)
	                  hash_weight_index += (tag_node,"weight",new ValueContext( h_wt + 10 ).indexNumeric())
	                  
	                }
	                else
	                {
	                  val tag_node = createNode(sub_category(tag_name,qp_time_created,10))
	                  SubcatIndex += (tag_node,"name",tag_name)
	                  var rel = tag_node --> "Tag_Of_Quickpost" --> quickpost_node <
	                  var rel_time = rel.setProperty("time", qp_time_created)
	                  var rel_in_wt = rel.setProperty("in_weight", 0)
	                  var rel_out_wt = rel.setProperty("out_weight", 10)
	                  hash_weight_index += (tag_node,"weight",new ValueContext( 10 ).indexNumeric())
	                  rel = tag_node --> "Hashtag_Created_By" --> user_node <
	                  var rel_time1 = rel.setProperty("time", qp_time_created)
	                }
	              }
	         }
	       }
	       
	       if(!qp_users.equals(""))
	          {
	            val qp_users_list = qp_users.split(",").distinct
	            for(each <- qp_users_list)
	            {
	              if(!each.equals(""))
	              {
	              
		              val user_node1 = user_index.get("id",each).getSingle()
		              if(user_node1 != null)
		              {
		                val rel1: org.neo4j.graphdb.Relationship = user_node1 --> "User_Of_Quickpost"  --> quickpost_node <
		                val y = rel1.setProperty("time", qp_time_created)
		              }
	              }
	          
	            }
	          
		      }
	       
	          val tagger = new MaxentTagger(
                "/var/n4j/data/left3words-wsj-0-18.tagger")
              val tagged = tagger.tagString(qp_content)
              //val prev_nouns = user_node.getProperty("nouns").toString()
              val noun_list = tagged.split(" ").toList.map(x => x.split("/")).filter( x => (x.size == 2 && x(1).equals("NNP"))).map(x => x(0).toLowerCase())
              qp_content_index += (quickpost_node,"quickpost_content",noun_list.mkString(" "))
              
//              if(prev_nouns.equals(""))
//              {
//                val noun_map = noun_list.groupBy(x=>x).mapValues(x=>x.length)
//                user_node.setProperty("nouns",JSONObject(noun_map).toString())
//              }
//              else
//              {
//                val noun_map = noun_list.groupBy(x=>x).mapValues(x=>x.length)
//                val prev_map = JSON.parseFull(prev_nouns).get.asInstanceOf[Map[String,Double]]
//                val merge_map = noun_map ++ prev_map.map{ case (k,v) => k -> (v + noun_map.getOrElse(k,0)) }
//                user_node.setProperty("nouns",JSONObject(merge_map).toString())
//              }
	          ret = true
       } 
     ret  
    }
  }

// Triggered when a new quick post is deleted
  def delete_quickpost(
      id: String  // unique quick post id
      ):Boolean =
  {
    
    
    withTx {
    implicit neo =>
       val QuickpostIndex = getNodeIndex("quickpost").get
       val qp_content_index = getNodeIndex("quickpost_content").get
       val qp_weight_index = getNodeIndex("quickpost_weight").get
       val user_weight_index = getNodeIndex("user_weight").get
       val hash_weight_index = getNodeIndex("hash_weight").get
       val quickpost_node = QuickpostIndex.get("id",id).getSingle()
       var ret = false
       if(quickpost_node != null)
	   {
	       val rels = quickpost_node.getRelationships().asScala
	       val q_wt = quickpost_node.getProperty("weight").toString().toInt
	       
	       val author_node = quickpost_node.getSingleRelationship("Quickpost_Written_By",Direction.OUTGOING).getOtherNode(quickpost_node)
		   var rel = quickpost_node.getSingleRelationship("Quickpost_Written_By",Direction.OUTGOING)
		   var del_wt = rel.getProperty("out_weight").toString().toInt
	       val au_wt = author_node.getProperty("weight").toString().toInt
		   author_node.setProperty("weight",au_wt-(q_wt+3-del_wt))
		   user_weight_index.remove(author_node)
		   user_weight_index += (author_node,"weight",new ValueContext( au_wt-(q_wt+3-del_wt) ).indexNumeric())
		         
		           
	       for(each <- rels)
	       {
	         val rel_type = each.getType().toString()
	         if(rel_type.equals("Tag_Of_Quickpost"))
	         {
	           
	           val tag_node = each.getOtherNode(quickpost_node)
	           val h_wt = tag_node.getProperty("weight").toString().toInt
	           tag_node.setProperty("weight",(h_wt - 10))
	           hash_weight_index.remove(tag_node)
	           hash_weight_index += (tag_node,"weight",new ValueContext( h_wt - 10 ).indexNumeric())
	           
	         }
	         each.delete() 
	       }
	       
	       
	       
	       QuickpostIndex.remove(quickpost_node)
	       qp_content_index.remove(quickpost_node)
	       qp_weight_index.remove(quickpost_node)
	       quickpost_node.delete()
	       ret = true
       }
     ret
    }
  }
 
  def delete_petition(
      id: String  // unique petition id
      ):Boolean =
  {
    
    withTx {
    implicit neo =>
       val p_weight_index = getNodeIndex("petition_weight").get
       val user_weight_index = getNodeIndex("user_weight").get
       val hash_weight_index = getNodeIndex("hash_weight").get
       val PIndex = getNodeIndex("petition").get
       val TilesIndex = getNodeIndex("tiles").get
       val FeaturedTilesIndex = getNodeIndex("featured_tiles").get
       val HeadlinesIndex = getNodeIndex("headlines").get
       val CommentIndex = getNodeIndex("comment").get
       val PContentIndex = getNodeIndex("petition_content").get
       val SubcatIndex = getNodeIndex("sub_category").get
       val p_node = PIndex.get("id",id).getSingle()
       val c_time = (System.currentTimeMillis()/1000).toInt
      // val e_end_time = event_node.getProperty("event_date_time_closing").toString().toInt
       var ret = false
       if(p_node != null)
	   {
         
               pin_item_q("P",id,"","")
               val space_rels = p_node.getRelationships("Petition_Tagged_To_Space").asScala.toList
	           for(space_rel <- space_rels)
	           {
	             val space_name = space_rel.getOtherNode(p_node).getProperty("space_id").toString()
	             pin_item_q("P",id,"",space_name)
	           }
               val p_end_time = p_node.getProperty("end_date").toString().toInt
		       val rels = p_node.getRelationships().asScala
		       val p_wt = p_node.getProperty("weight").toString().toInt
		       
		       if(p_node.hasRelationship("Petition_Written_By",Direction.OUTGOING) && (c_time < p_end_time) )
		       {
		           val author_node = p_node.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getOtherNode(p_node)
				   var rel = p_node.getSingleRelationship("Petition_Written_By",Direction.OUTGOING)
				   var del_wt = rel.getProperty("out_weight").toString().toInt
			       val au_wt = author_node.getProperty("weight").toString().toInt
			       author_node.setProperty("weight",au_wt-(p_wt+5-del_wt))
				   user_weight_index.remove(author_node)
				   user_weight_index += (author_node,"weight",new ValueContext( au_wt-(p_wt+5-del_wt) ).indexNumeric())
		       }
			   
		       for(each <- rels)
		       {
		         
		         if(each.getOtherNode(p_node).getProperty("__CLASS__").equals("Saddahaq.comment"))
		         {
		           
		             val c_node = each.getOtherNode(p_node)
		             val c_rels = c_node.getRelationships().asScala
		             for(single <- c_rels)
		             {
		               single.delete()
		             }
		             CommentIndex.remove(c_node)
		             c_node.delete()
		           
		         }
		         
		         else if(each.getOtherNode(p_node).getProperty("__CLASS__").equals("Saddahaq.poll"))
                 {
           
	               val poll_node = each.getOtherNode(p_node)
	               poll_node.getSingleRelationship("Poll_App_Of_Petition",Direction.OUTGOING).delete()
		           val vote_rels = poll_node.getRelationships("Voted_To_Poll").asScala
		           for(each <- vote_rels)
		           {
		             each.delete()
		           }
			       poll_node.delete()
          
                 }
		         
		         else if(each.getOtherNode(p_node).getProperty("__CLASS__").equals("Saddahaq.sub_category"))
		         {
		             val rel_type = each.getType().toString() 
		             if(c_time < p_end_time)
		             {
			             if(rel_type.equals("Tag_Of_Petition"))
			             {
			               val tag_node = each.getOtherNode(p_node)
			               val h_wt = tag_node.getProperty("weight").toString().toInt
			               tag_node.setProperty("weight",(h_wt - 10))
			               hash_weight_index.remove(tag_node)
			               hash_weight_index += (tag_node,"weight",new ValueContext( h_wt - 10 ).indexNumeric())
			             }
			             
			             else
			             {
			               val hash_node = each.getOtherNode(p_node)
				           val hash_wt = hash_node.getProperty("weight").toString().toInt
				           hash_node.setProperty("weight",(hash_wt - p_wt))
				           hash_weight_index.remove(hash_node)
				           hash_weight_index += (hash_node,"weight",new ValueContext( hash_wt - p_wt ).indexNumeric())
				        
			             }
		             }
		             each.delete()
		         }
		         
		         
		         else
		         {
		             
		             val rel_type = each.getType().toString()
		             if(rel_type.equals("Belongs_To_Petition_Category"))
		             {
		               // deleting the relation with category
		               val c_name = each.getOtherNode(p_node).getProperty("name").toString()
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
		         
		       PIndex.remove(p_node)
		       PContentIndex.remove(p_node)
		       p_weight_index.remove(p_node)
		       p_node.delete()
		       
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
  
  def delete_debate(
      id: String  // unique debate id
      ):Boolean =
  {
    
    withTx {
    implicit neo =>
       val d_weight_index = getNodeIndex("debate_weight").get
       val user_weight_index = getNodeIndex("user_weight").get
       val hash_weight_index = getNodeIndex("hash_weight").get
       val DIndex = getNodeIndex("debate").get
       val TilesIndex = getNodeIndex("tiles").get
       val FeaturedTilesIndex = getNodeIndex("featured_tiles").get
       val HeadlinesIndex = getNodeIndex("headlines").get
       val CommentIndex = getNodeIndex("comment").get
       val DContentIndex = getNodeIndex("debate_content").get
       val SubcatIndex = getNodeIndex("sub_category").get
       val d_node = DIndex.get("id",id).getSingle()
       val c_time = (System.currentTimeMillis()/1000).toInt
      // val e_end_time = event_node.getProperty("event_date_time_closing").toString().toInt
       var ret = false
       if(d_node != null)
	   {
         
                   pin_item_q("D",id,"","")
               
                   val space_rels = d_node.getRelationships("Debate_Tagged_To_Space").asScala.toList
		           for(space_rel <- space_rels)
		           {
		             val space_name = space_rel.getOtherNode(d_node).getProperty("space_id").toString()
		             pin_item_q("D",id,"",space_name)
		           }
                   var news_node = TilesIndex.get("id","all").getSingle()
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
                   
                   news_node = FeaturedTilesIndex.get("id","all").getSingle()
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
                   
                   news_node = HeadlinesIndex.get("id","all").getSingle()
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
//               val d_end_time = d_node.getProperty("d_date").toString().toInt + 3600
		       val rels = d_node.getRelationships().asScala
//		       val d_wt = d_node.getProperty("weight").toString().toInt
//		       
//		       if(c_time < d_end_time)
//		       {
//		           val author_node = d_node.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getOtherNode(d_node)
//				   var rel = d_node.getSingleRelationship("Debate_Written_By",Direction.OUTGOING)
//				   var del_wt = rel.getProperty("out_weight").toString().toInt
//			       val au_wt = author_node.getProperty("weight").toString().toInt
//			       author_node.setProperty("weight",au_wt-(d_wt+5-del_wt))
//				   user_weight_index.remove(author_node)
//				   user_weight_index += (author_node,"weight",new ValueContext( au_wt-(d_wt+5-del_wt) ).indexNumeric())
//		       }
			   
		       for(each <- rels)
		       {
		         
		         each.delete()
		         
		       }
		         
		       DIndex.remove(d_node)
		       DContentIndex.remove(d_node)
		       d_weight_index.remove(d_node)
		       d_node.delete()
		       
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
  
  def delete_townhall(
      id: String  // unique townhall id
      ):Boolean =
  {
    
    withTx {
    implicit neo =>
       
       val user_weight_index = getNodeIndex("user_weight").get
       val TIndex = getNodeIndex("townhall").get
       val TilesIndex = getNodeIndex("tiles").get
       val FeaturedTilesIndex = getNodeIndex("featured_tiles").get
       val HeadlinesIndex = getNodeIndex("headlines").get
       val CommentIndex = getNodeIndex("comment").get
       val TContentIndex = getNodeIndex("townhall_content").get
       val t_node = TIndex.get("id",id).getSingle()
       val c_time = (System.currentTimeMillis()/1000).toInt
       var ret = false
       if(t_node != null)
	   {
         
               pin_item_q("T",id,"","")
               val space_rels = t_node.getRelationships("Townhall_Tagged_To_Space").asScala.toList
	           for(space_rel <- space_rels)
	           {
	             val space_name = space_rel.getOtherNode(t_node).getProperty("space_id").toString()
	             pin_item_q("T",id,"",space_name)
	           }
               
                   var news_node = TilesIndex.get("id","all").getSingle()
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
                   
                   news_node = FeaturedTilesIndex.get("id","all").getSingle()
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
                   
                   news_node = HeadlinesIndex.get("id","all").getSingle()
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
               
		       val rels = t_node.getRelationships().asScala
               for(each <- rels)
		       {
		         
		             each.delete()
		       }
		         
		       TIndex.remove(t_node)
		       TContentIndex.remove(t_node)
		       t_node.delete()
		       
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

  
   // Triggered when a quick post is marked as spam by the user
  def quickpost_spam(
        id:String, // unique quickpost id
        user_name:String,  // unique user name
        time:Int
        ):Boolean =
    {
    
   
     withTx {
      
     implicit neo =>
       val QuickpostIndex = getNodeIndex("quickpost").get
       val qp_node = QuickpostIndex.get("id",id).getSingle()
       val UserIndex = getNodeIndex("user").get
       val user_node = UserIndex.get("id",user_name).getSingle()
       var ret = false
       if(qp_node != null  && user_node != null && !qp_node.getRelationships("Spammed_Quickpost").asScala.map(_.getOtherNode(qp_node)).toList.contains(user_node))
	   {
           user_node.setProperty("last_seen",time)
	       var s_wt = qp_node.getProperty("spam_weight").toString().toInt
	       s_wt = s_wt + 1
	       if(s_wt > 5)
	       {
	         delete_quickpost(id)
	       }
	       else
	       {
	         qp_node.setProperty("spam_weight",s_wt)
	         val rel: org.neo4j.graphdb.Relationship = user_node --> "Spammed_Quickpost" --> qp_node <
	         val rel_time = rel.setProperty("time", time)
	       }
	       ret = true
       }
       ret
     }
    }

}
