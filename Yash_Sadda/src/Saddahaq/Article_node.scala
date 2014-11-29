package Saddahaq
import org.neo4j.scala.{TypedTraverser, SingletonEmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}
import org.neo4j.scala.Neo4jIndexProvider
import collection.JavaConverters._
import scala.collection.immutable.Map
import org.neo4j.graphdb.Direction
import scala.util.parsing.json.JSONArray
import scala.collection.immutable.ListMap
import org.neo4j.index.lucene.ValueContext
import scala.util.parsing.json.JSONObject
import edu.stanford.nlp.tagger.maxent.MaxentTagger
import scala.util.parsing.json.JSON
import scala.util.control.Breaks

case class category(val name: String,
val pins: String, 
val exclusive: String
)

case class feed(val id: String,
val article: String, 
val event: String,
val petition: String,
val townhall: String,
val debate: String,
val time: Int
)

case class sub_category(val name: String,
    val time_created: Int,
    val weight: Int
    )
    
case class topic(val name: String,
    val sub_topics: String
    )
   
case class poll(val id: String,
    val poll_question: String,
    val poll_status: Int,
    val time_created: Int
    )
    
case class trending(val trend_id: String)
    
case class article(
    val article_id: String,
    val article_title_id: String,
    val article_title: String,
    val article_content: String,
    val article_summary: String,
    val article_featured_img: String,
    val time_created: Int,
    val weight: Int,
    val spam_weight: Int,
    val views: Int,
    val weight_review: Int,
    val latest_views: String,
    val stars: Int,
    val approved: Int,
    val head: Int,
    val space: Int
    )
    
case class space(
    val space_id: String,
    val space_title_id: String,
    val space_title: String,
    val space_tagline: String,
    val space_content: String,
    val space_featured_img: String,
    val time_created: Int,
    val pins: String,
    val closed: Int
    )
    
case class cfpost(
    val user: String,
    val cf_id: String,
    val cf_title: String,
    val cf_content: String,
    val cf_featured_img: String,
    val cf_url: String,
    val cf_tags: String,
    val time_created: Int,
    val end_date: Int,
    val amt_target: Int,
    val amt_raised: Int,
    val ppl_count: Int
    )
    
case class search_word(val name: String)
    
case class tiles(val id: String,
    val value: String
    )
    
case class featured_tiles(val id: String,
    val value: String
    )
    
case class headlines(val id: String,
    val value: String
    ) 

trait Article_node extends Neo4jWrapper with SingletonEmbeddedGraphDatabaseServiceProvider with TypedTraverser with Neo4jIndexProvider {

  
  def create_cfpost(
    cf_user: String,  // unique user name(Writer of the article)
    cf_id: String, // unique article id
    cf_title: String,
    cf_url: String,
    cf_content: String,
    cf_fut_image: String,  // Cover image name
    cf_hashtags: String,  // , separated referred hash tags if any("" if none)
    cf_time_created: Int,
    cf_end_date: Int,
    cf_amt_target: Int,
    cf_amt_raised: Int,
    cf_ppl_count: Int,
    is_edit: Int
    ):Boolean=          
    {
    
    
	  withTx {
	  implicit neo =>
	     
	   var ret = false
	   val cur_time = (System.currentTimeMillis() /1000).toInt
	   if(is_edit == 0)
	   {
	     
	     
	     val CfpostIndex = getNodeIndex("cfpost").get
	     var cf_node = CfpostIndex.get("id",cf_id).getSingle()
	     
	     if(cf_node == null && !cf_hashtags.equals(""))
		 {
	       cf_node = createNode(cfpost(cf_user,cf_id,cf_title,cf_content,cf_fut_image,cf_url,cf_hashtags,cf_time_created,cf_end_date,cf_amt_target,cf_amt_raised,cf_ppl_count))
		   CfpostIndex += (cf_node,"id",cf_id)     //Adding newly created article node to article index
		   ret = true  
	       
		 }
	   }
	   else
	   {
	     val CfpostIndex = getNodeIndex("cfpost").get
	     var cf_node = CfpostIndex.get("id",cf_id).getSingle()
	     
	     if(cf_node != null && !cf_hashtags.equals(""))
		 {
	       
	       cf_node.setProperty("user",cf_user)
	       cf_node.setProperty("cf_title",cf_title)
	       cf_node.setProperty("cf_content",cf_content)
	       cf_node.setProperty("cf_featured_img",cf_fut_image)
	       cf_node.setProperty("cf_url",cf_url)
	       cf_node.setProperty("cf_tags",cf_hashtags)
	       cf_node.setProperty("time_created",cf_time_created)
	       cf_node.setProperty("end_date",cf_end_date)
	       cf_node.setProperty("amt_target",cf_amt_target)
	       cf_node.setProperty("amt_raised",cf_amt_raised)
	       cf_node.setProperty("ppl_count",cf_ppl_count)
	       
	       
	       ret = true
	       
		 }
	   }
	   
	  ret
	  }
	  
	
	}
  
  // Triggered when a new article is created
  def create_space(
    user_name: String,  // unique user name(Writer of the article)
    space_id: String, // unique article id
    space_title_id: String,  //unique article title id
    space_title: String,
    space_tagline: String,
    space_fut_image: String,
    space_time_created: Int,
    is_edit: Int,
    is_closed: Int
    ):Boolean=          
    {
    
    
	  withTx {
	  implicit neo =>
	     
	   var ret = false
	   if(is_edit == 0)
	   {
	     
	     val cur_time = (System.currentTimeMillis() /1000).toInt
	     val SpaceIndex = getNodeIndex("space").get
	     
	     val UserIndex = getNodeIndex("user").get
	     val user_node = UserIndex.get("id",user_name).getSingle()  //Retrieving user node based on userid
	     var space_node = SpaceIndex.get("id",space_id).getSingle()
	     
	     if(user_node != null && space_node == null && space_fut_image != null)
		 {
	         user_node.setProperty("last_seen",space_time_created)
	         
		     // creating new article node and indexing it
		     space_node = createNode(space(space_id,space_title_id,space_title,space_tagline,"",
		                                        space_fut_image,space_time_created,"",is_closed))
		     
		     SpaceIndex += (space_node,"id",space_id)     //Adding newly created article node to article index
		     
		     var rel:org.neo4j.graphdb.Relationship = space_node --> "Space_Created_By" --> user_node  <  //relating article node and user node
		     var rel_time = rel.setProperty("time", space_time_created)
		     
		     var rel1:org.neo4j.graphdb.Relationship = user_node --> "Admin_Of_Space" --> space_node  <  //relating article node and user node
		     var rel_time1 = rel1.setProperty("time", space_time_created)
		     
		     val tagger = new MaxentTagger(
	                "/var/n4j/data/left3words-wsj-0-18.tagger")
	              val tagged = tagger.tagString(space_tagline)
	              //val prev_nouns = user_node.getProperty("nouns").toString()
	              val noun_list =  tagged.split(" ").toList.map(x => x.split("/")).filter( x => (x.size == 2 && x(1).equals("NNP"))).map(x => x(0).toLowerCase())
	              
	              space_node.setProperty("space_content",(noun_list.mkString(" ") + " " + bummy(space_title)))
	         
	         
		     ret = true
		     
		 }
	   }
	   
	   else
	   {
	     val SpaceIndex = getNodeIndex("space").get
	     var space_node = SpaceIndex.get("id",space_id).getSingle()
	     
	     if(space_node != null)
		 {
	       
	       space_node.setProperty("space_title",space_title)
	       space_node.setProperty("space_title_id",space_title_id)
	       space_node.setProperty("space_tagline",space_tagline)
	       space_node.setProperty("space_featured_img",space_fut_image)
	       
	       
	       val tagger = new MaxentTagger(
	                "/var/n4j/data/left3words-wsj-0-18.tagger")
	              val tagged = tagger.tagString(space_tagline)
	              //val prev_nouns = user_node.getProperty("nouns").toString()
	              val noun_list =  tagged.split(" ").toList.map(x => x.split("/")).filter( x => (x.size == 2 && x(1).equals("NNP"))).map(x => x(0).toLowerCase())
	              
	              space_node.setProperty("space_content",(noun_list.mkString(" ") + " " + bummy(space_title)))
	         
	         
	       
	       ret = true
	       
		 }
	   }
	   
	  ret
	  }
    }
  
  
  def space_isclosed(
    id: String,
    is_closed: Int
    ):Boolean=          
    {
    
    
	  withTx {
	  implicit neo =>
	     
	   var ret = false
	   
	     val SpaceIndex = getNodeIndex("space").get
	     var space_node = SpaceIndex.get("id",id).getSingle()
	     
	     if(space_node != null)
		 {
	         space_node.setProperty("closed",is_closed)
	         var art = space_node.getRelationships("Article_Tagged_To_Space").asScala.toList.filterNot(x => x.hasProperty("admin_tagged")).map(_.getOtherNode(space_node))
	         var event = space_node.getRelationships("Event_Tagged_To_Space").asScala.toList.filterNot(x => x.hasProperty("admin_tagged")).map(_.getOtherNode(space_node))
	         var petition = space_node.getRelationships("Petition_Tagged_To_Space").asScala.toList.filterNot(x => x.hasProperty("admin_tagged")).map(_.getOtherNode(space_node))
	         var debate = space_node.getRelationships("Debate_Tagged_To_Space").asScala.toList.filterNot(x => x.hasProperty("admin_tagged")).map(_.getOtherNode(space_node))
	         var townhall = space_node.getRelationships("Townhall_Tagged_To_Space").asScala.toList.filterNot(x => x.hasProperty("admin_tagged")).map(_.getOtherNode(space_node))
	         for(each <- art)  
	         {
	           each.setProperty("space",is_closed)
	         }
		     for(each <- event)  
	         {
	           each.setProperty("space",is_closed)
	         }
		     for(each <- petition)  
	         {
	           each.setProperty("space",is_closed)
	         }
		     for(each <- debate)  
	         {
	           each.setProperty("space",is_closed)
	         }
		     for(each <- townhall)  
	         {
	           each.setProperty("space",is_closed)
	         }
	       
	         ret = true
	       
		 }
	   
	   
	  ret 
	  }
    }
  
  // Triggered when a new article is created
  def create_article(
    user_name: String,  // unique user name(Writer of the article)
    a_id: String, // unique article id
    a_title_id: String,  //unique article title id
    a_title: String,
    a_content: String,
    a_summary: String,
    a_fut_image: String,  // Cover image name
    a_cat: String,  // , separated categories 
    a_subcat: String,  // , separated hash tags if any("" if none)
    a_hashtags: String,  // , separated referred hash tags if any("" if none)
    a_users: String,  // , separated referred users if any("" if none)
    a_time_created: Int,
    related_articles: String,     //List of related articles marked by user
    related_events: String,
    mod_name: String,
    stars: Int,
    is_edit: Int,
    is_closed: Int
    ):Boolean=          
    {
    
    
	  withTx {
	  implicit neo =>
	     
	   var ret = false
	   if(is_edit == 0)
	   {
	     var is_space = is_closed
	     val cur_time = (System.currentTimeMillis() /1000).toInt
	     val ArticleIndex = getNodeIndex("article").get
	     val EventIndex = getNodeIndex("event").get
	     val TilesIndex = getNodeIndex("tiles").get
	     val ArticleContentIndex = getNodeIndex("article_content").get
	     val avg_wt_index = getNodeIndex("avg_weights").get
	     val ArticleTitleIndex = getNodeIndex("article_title").get
	     val article_weight_index = getNodeIndex("article_weight").get
	     val user_weight_index = getNodeIndex("user_weight").get
	     val hash_weight_index = getNodeIndex("hash_weight").get
	     val UserIndex = getNodeIndex("user").get
	     val user_node = UserIndex.get("id",user_name).getSingle()  //Retrieving user node based on userid
	     val art_node = ArticleIndex.get("id",a_id).getSingle()
	     
	     if(user_node != null && art_node == null && !a_cat.equals("") && !a_subcat.equals("") && a_fut_image != null)
		 {
	         user_node.setProperty("last_seen",a_time_created)
	         var u_wt = user_node.getProperty("weight").toString().toInt
		     val a_wt = Math.round(u_wt.asInstanceOf[Float]/100)
		     var weight = 0
		     // creating new article node and indexing it
		     val article_node = createNode(article(a_id,a_title_id,a_title,"",a_summary,
		                                        a_fut_image,a_time_created,a_wt,0,1,0,"",stars,0,0,is_space))
		     
		     ArticleIndex += (article_node,"id",a_id)     //Adding newly created article node to article index
		     val hash_list = a_subcat.split(",").toList.map(x => x.toLowerCase())
		     val topic_words_list = (a_title+" "+a_summary+" "+a_content+" "+hash_list.mkString(" ")).toLowerCase().split(" ").toList
		     val index_data = bummy(a_title+" "+a_summary+" "+a_content)+" "+hash_list.mkString(" ")
		     ArticleContentIndex += (article_node,"time",new ValueContext( a_time_created ).indexNumeric())
		     ArticleTitleIndex += (article_node,"title_id",a_title_id)
		     // relating article to user(author)
		     var rel:org.neo4j.graphdb.Relationship = article_node --> "Article_Written_By" --> user_node  <  //relating article node and user node
		     var rel_time = rel.setProperty("time", a_time_created)
		     
		     // increasing the weight of the article based on the stars given by the moderator
//		     if(stars > 0)
//		     {
//		       val mod_node = UserIndex.get("id",mod_name).getSingle()
//		       var news_node = TilesIndex.get("id","all").getSingle()
//		       if(mod_node != null && news_node != null)
//		       {
//		          
//	              val news = news_node.getProperty("value").toString().split(",").toList.slice(0,10).map( x => ArticleIndex.get("id",x).getSingle().getProperty("weight").toString().toFloat)
//	              val top_10_avg = news.sum/news.size
//	              weight = Math.round(top_10_avg / (6-stars))
//		          val rel: org.neo4j.graphdb.Relationship = article_node --> "Pushed_By" --> mod_node <
//	              val rel_time = rel.setProperty("time", a_time_created)
//	              var rel_in_wt = rel.setProperty("in_weight",0)
//	              var rel_out_wt = rel.setProperty("out_weight", weight)
//	              article_node.setProperty("weight",a_wt + weight)
//		       }
//		     }
		     rel.setProperty("in_weight", 10 + weight)
		     rel.setProperty("out_weight", a_wt + weight)
		     article_weight_index += (article_node,"weight",new ValueContext( a_wt + weight ).indexNumeric())
		     user_node.setProperty("weight",u_wt+10+weight)
		     user_weight_index.remove(user_node)
		     user_weight_index += (user_node,"weight",new ValueContext( u_wt+10+weight).indexNumeric())
		     
		     // rleated article to locations
		     val location_index = getNodeIndex("location").get
		     val all_cities = location_index.get("id","all").getSingle().getProperty("cities").toString().split(",").toList
		     val cities = index_data.toLowerCase().split(" ").toList.distinct.intersect(all_cities)
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
		               article_node --> "Belongs_To_Location_Article" --> e 
		               loop.break;
		             }
		           }
	             }
		       }
		     }
		     
		     // relating article to the related articles
		     if(!related_articles.equalsIgnoreCase(""))
	         {
	            for(art <- related_articles.split(",")) 
	            {
	                
	            	val rel: org.neo4j.graphdb.Relationship = ArticleIndex.get("id",art).getSingle() --> "Related_Article_To" --> article_node < //Relating related article nodes to the new article node
	       			val rel_time = rel.setProperty("time", a_time_created)
	            }
	         }
		     
		     // relating article to the related events
	     	 if(!related_events.equalsIgnoreCase(""))
	     	 {
	     		 for(event <- related_events.split(",")) 
	     		 {
	     		     val event_node = EventIndex.get("id",event).getSingle()
	     		     if(event_node != null)
	     		     {
		     			 val rel: org.neo4j.graphdb.Relationship = event_node --> "Related_Event_To" --> article_node <    //Relating related event nodes to the new article node
		     			 val rel_time = rel.setProperty("time", a_time_created)
	     		     }
	     		 }
	     	 }
	     	 
	     	 // Relating article to the categories
		     val CategoryIndex = getNodeIndex("category").get
		     var cats = a_cat.split(",").toList.map( x => x.toLowerCase()) 
		     cats ::= "all"
		     val cur_time = a_time_created
		     val t = cur_time - 86400
		     for(each <- cats)
		     {
	//	       var news_node = TilesIndex.get("id",each).getSingle()
	//	       if(news_node == null)
	//	       {
	//	         val tiles_node =  createNode(tiles(each,a_id))
	//	         TilesIndex += (tiles_node,"id",each)
	//	       }
	//	       else
	//	       {
	//	         
	//		     var sorted_news  = List[org.neo4j.graphdb.Node]()
	//		     val news = news_node.getProperty("value").toString()
	//		     var news_list = news.split(",").toList
	//		     news_list ::= a_id
	//		     val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
	//             news_node.setProperty("value",news_list_trim)
	//	      
	//		    
	//	       }
		       //ArticleContentIndex += (article_node,"article_cat",each)
		       if(CategoryIndex.get("name",each).getSingle() != null)  
		       {
		        // val CategoryIndex = getNodeIndex("category").get
		         val category_node = CategoryIndex.get("name",each).getSingle()
	
		         rel = article_node --> "Belongs_To_Category" --> category_node <
		         var rel_time = rel.setProperty("time", a_time_created)
		         
		         
		         val topic_nodes = category_node.getRelationships("Topic_Of_Category").asScala.toList.map(_.getStartNode())
		         if(topic_nodes != null)
		         {
		           for(top <- topic_nodes)
		           {
		             val keywords = top.getProperty("sub_topics").toString().split(",").toList
		             val inter = topic_words_list.intersect(keywords).size
		             if(inter > 0)
		             {
//		               println(topic_words_list.intersect(keywords))
		               rel = article_node --> "Belongs_To_Topic" --> top <
		               var rel_time = rel.setProperty("time", a_time_created)
		               var count = rel.setProperty("count", inter)
		             }
		             
		           }
		         }
		         
		         
		       }
		       else
		       {
		         val category_node = createNode(category(each,"",""))
		         CategoryIndex += (category_node,"name",each)
	
		         rel = article_node --> "Belongs_To_Category" --> category_node <
		         var rel_time = rel.setProperty("time", a_time_created)
		         
		       }
		     }
		     
		             if(is_space == 0)
		             {
			             for(each <- cats)
					     {
					       var news_node = TilesIndex.get("id",each).getSingle()
					       if(news_node == null)
					       {
					         val tiles_node =  createNode(tiles(each,a_id))
					         TilesIndex += (tiles_node,"id",each)
					       }
					       else
					       {
					         
						     var sorted_news  = List[org.neo4j.graphdb.Node]()
						     val news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     news_list ::= a_id
						     val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				             news_node.setProperty("value",news_list_trim)
					      
						    
					       }
					     }
		             }
		             
		            var tot_tags = List[org.neo4j.graphdb.Node]()
		            val SubcatIndex = getNodeIndex("sub_category").get
		            val TopicIndex = getNodeIndex("topic").get
		            // relating article to the hashtags
		            if(a_subcat != "")
		            {
		              val hash_tags = a_subcat.split(",").toList.filterNot( x => x.equals("")).map( x => x.toLowerCase())
		              val main_hash = hash_tags(0)
		              for(each <- hash_tags)
		              {
			              if(SubcatIndex.get("name",each).getSingle() != null)
			              {
			                val subcat_node = SubcatIndex.get("name",each).getSingle()
			                tot_tags ::= subcat_node
			                val h_wt = subcat_node.getProperty("weight").toString().toInt
			                subcat_node.setProperty("weight",(h_wt + a_wt))
			                if(each.equals(main_hash))
			                {
				                rel = article_node --> "Belongs_To_Subcategory_Article" --> subcat_node <
				                var rel_main = rel.setProperty("main", 1)
				                var rel_time = rel.setProperty("time", a_time_created)
				                var rel_in_wt = rel.setProperty("in_weight", a_wt)
				                var rel_out_wt = rel.setProperty("out_weight", 0)
			                }
			                
			                else
			                {
			                    rel = article_node --> "Belongs_To_Subcategory_Article" --> subcat_node <
				                var rel_time = rel.setProperty("time", a_time_created)
				                var rel_in_wt = rel.setProperty("in_weight", a_wt)
				                var rel_out_wt = rel.setProperty("out_weight", 0)
			                }
			                hash_weight_index.remove(subcat_node)
			                hash_weight_index += (subcat_node,"weight",new ValueContext( h_wt + a_wt ).indexNumeric())
			              }
			              else
			              {
			                val subcat_node = createNode(sub_category(each,a_time_created,a_wt))
			                SubcatIndex += (subcat_node,"name",each)
			                if(each.equals(main_hash))
			                {
				                rel = article_node --> "Belongs_To_Subcategory_Article" --> subcat_node <
				                var rel_main = rel.setProperty("main", 1)
				                var rel_time = rel.setProperty("time", a_time_created)
				                var rel_in_wt = rel.setProperty("in_weight", a_wt)
				                var rel_out_wt = rel.setProperty("out_weight", 0)
			                }
			                
			                else
			                {
			                    rel = article_node --> "Belongs_To_Subcategory_Article" --> subcat_node <
				                var rel_time = rel.setProperty("time", a_time_created)
				                var rel_in_wt = rel.setProperty("in_weight", a_wt)
				                var rel_out_wt = rel.setProperty("out_weight", 0)
			                }
			                
			                rel = subcat_node --> "Hashtag_Created_By" --> user_node <
			                var rel_time1 = rel.setProperty("time", a_time_created)
			                
			                hash_weight_index += (subcat_node,"weight",new ValueContext( a_wt).indexNumeric())
			                
			                tot_tags ::= subcat_node
			              }
		              }
		            }
		            
		            // relating article to the topics
		            
		           
		          // relating article to the inline hashtags
		          if(!a_hashtags.equals(""))
		          {
		            val a_tags = a_hashtags.split(",").toList.filterNot( x => x.equals("")).map( x => x.toLowerCase()).distinct
		            for(tag_name <- a_tags)
		            {
		              
		              if(tag_name != "")
		              {
		                
		                if(SubcatIndex.get("name",tag_name).getSingle() != null)
		                {
		                  val tag_node = SubcatIndex.get("name",tag_name).getSingle()
		                  tot_tags ::= tag_node
		                  val h_wt = tag_node.getProperty("weight").toString().toInt
		                  tag_node.setProperty("weight",(h_wt + 10))
		                  rel = tag_node --> "Tag_Of_Article" --> article_node <
		                  var rel_time = rel.setProperty("time", a_time_created)
		                  var rel_in_wt = rel.setProperty("in_weight", 0)
		                  var rel_out_wt = rel.setProperty("out_weight", 10)
		                  hash_weight_index.remove(tag_node)
		                  hash_weight_index += (tag_node,"weight",new ValueContext( h_wt + 10 ).indexNumeric())
		                  
		                }
		                else
		                {
		                  val tag_node = createNode(sub_category(tag_name,a_time_created,10))
		                  SubcatIndex += (tag_node,"name",tag_name)
		                  rel = tag_node --> "Tag_Of_Article" --> article_node <
		                  var rel_time = rel.setProperty("time", a_time_created)
		                  var rel_in_wt = rel.setProperty("in_weight", 0)
		                  var rel_out_wt = rel.setProperty("out_weight", 10)
		                  hash_weight_index += (tag_node,"weight",new ValueContext( 10 ).indexNumeric())
		                  rel = tag_node --> "Hashtag_Created_By" --> user_node <
		                  var rel_time1 = rel.setProperty("time", a_time_created)
		                  tot_tags ::= tag_node
		                }
		              }
		            }
		          
			      }
		          
		          // relating article to the tagged users
		          if(!a_users.equals(""))
		          {
		            val a_users_list = a_users.split(",").distinct
		            for(each <- a_users_list)
		            {
		              if(!each.equals(""))
		              {
		              
			              val user_node1 = UserIndex.get("id",each).getSingle()
			              if(user_node1 != null)
			              {
			                val rel1: org.neo4j.graphdb.Relationship = user_node1 --> "User_Of_Article"  --> article_node <
			                val y = rel1.setProperty("time", a_time_created)
			              }
		              }
		          
		            }
		          
			      }
		          
		          
		          //linking hashtags 
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
		          
		          
		          // indexing the proper nouns present in the article
		          val tagger = new MaxentTagger(
	                "/var/n4j/data/left3words-wsj-0-18.tagger")
	              val tagged = tagger.tagString(a_content)
	              //val prev_nouns = user_node.getProperty("nouns").toString()
	              val noun_list =  tagged.split(" ").toList.map(x => x.split("/")).filter( x => (x.size == 2 && x(1).equals("NNP"))).map(x => x(0).toLowerCase())
	              var noun_hash_list = noun_list:::hash_list
	              
	              
	              ArticleContentIndex += (article_node,"article_content",(noun_hash_list.mkString(" ") + " " + bummy(a_title))  )
	              // filtering the common words
	              val proper_nouns = List("2013","2014","india","indian","reddy","rao","singh","world","pti","sunday","monday","tuesday","wednesday","thursday","friday","saturday","january","february","march","april","may","june","july","august","september","october","november","december")
	              noun_hash_list = noun_hash_list.filterNot(x => proper_nouns.contains(x) && x.length() < 3)
	              article_node.setProperty("article_content",noun_hash_list.mkString(" "))
	              
	              if(is_space == 0)
	              {
		              var followers = user_node.getRelationships("Follows",Direction.INCOMING).asScala.toList.map(_.getStartNode())
		              followers ::= user_node
		              for(each <- followers)
		              {
		                  val u_name = each.getProperty("user_name").toString()
			              val user_tiles_index = getNodeIndex("user_tiles").get
			              val tile_node = user_tiles_index.get("id",u_name).getSingle()
			              val perso = tile_node.getProperty("news_Personalized").toString()
			              if(perso.equals(""))
			              {
			                tile_node.setProperty("news_Personalized",a_id)
			              }
				          
			              else
			              {
			                 //val news = news_node.getProperty("value").toString()
						     var perso_list = perso.split(",").toList
						     perso_list ::= a_id
						     val perso_list_trim = perso_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				             tile_node.setProperty("news_Personalized",perso_list_trim)
			              }
		              }
	              }
	
	              ret = true
	     }
	   }
	   
	   
	   else
	   {
		  val cur_time = (System.currentTimeMillis() /1000).toInt
	      val nodeIndex = getNodeIndex("article").get
	      val EventIndex = getNodeIndex("event").get
	      val UserIndex = getNodeIndex("user").get
	      val SubcatIndex = getNodeIndex("sub_category").get
	      val TopicIndex = getNodeIndex("topic").get
	      val TilesIndex = getNodeIndex("tiles").get
	      val FeaturedTilesIndex = getNodeIndex("featured_tiles").get
	      val HeadlinesIndex = getNodeIndex("headlines").get
	      val ArticleContentIndex = getNodeIndex("article_content").get
	      val ArticleTitleIndex = getNodeIndex("article_title").get
	      val hash_weight_index = getNodeIndex("hash_weight").get
	      val article_node = nodeIndex.get("id",a_id).getSingle()
	      val time = article_node.getProperty("time_created").toString().toInt
	      val user_node = UserIndex.get("id",user_name).getSingle()
	      if(article_node != null && user_node != null && !a_cat.equals("") && !a_subcat.equals("") && a_fut_image != null)
		  {
	        
	          val feu = article_node.getProperty("approved").toString().toInt
	          val head = article_node.getProperty("head").toString().toInt
	          val space = article_node.getProperty("space").toString().toInt
	          user_node.setProperty("last_seen",a_time_created)
		      val art_wt = article_node.getProperty("weight").toString().toInt
		      article_node.setProperty("article_title_id",a_title_id)
		      article_node.setProperty("article_title",a_title)
		      article_node.setProperty("article_summary",a_summary)
		      article_node.setProperty("article_featured_img",a_fut_image)
		      article_node.setProperty("stars",stars)
		      ArticleContentIndex.remove(article_node)
		      val h_list = a_subcat.split(",").toList.map( x => x.toLowerCase())
		      val topic_words_list = (a_title+" "+a_summary+" "+a_content+" "+h_list.mkString(" ")).toLowerCase().split(" ").toList
		     
		      val index_data = bummy(a_title+" "+a_summary+" "+a_content)+" "+h_list.mkString(" ")
		      
		      ArticleContentIndex += (article_node,"time",new ValueContext( time ).indexNumeric())
		      ArticleTitleIndex.remove(article_node)
		      ArticleTitleIndex += (article_node,"title_id",a_title_id)
//		      if(stars > 0)
//		      {
//		           
//			       val mod_node = UserIndex.get("id",mod_name).getSingle()
//			       if(mod_node != null && !article_node.hasRelationship("Pushed_By",Direction.OUTGOING))
//			       {
//			          var news_node = TilesIndex.get("id","all").getSingle()
//	                  val news = news_node.getProperty("value").toString().split(",").toList.slice(0,10).map( x => nodeIndex.get("id",x).getSingle().getProperty("weight").toString().toFloat)
//	                  val top_10_avg = news.sum/news.size
//	                  var weight = Math.round(top_10_avg / (6-stars))
//			          val rel: org.neo4j.graphdb.Relationship = article_node --> "Pushed_By" --> mod_node <
//		              val rel_time = rel.setProperty("time", time)
//		              var rel_in_wt = rel.setProperty("in_weight",0)
//		              var rel_out_wt = rel.setProperty("out_weight", weight)
//				      val i_wt = article_node.getProperty("weight").toString().toInt
//				      article_node.setProperty("weight",i_wt + weight)
//				      val weight_index = getNodeIndex("article_weight").get
//				      weight_index.remove(article_node)
//					  weight_index += (article_node,"weight",new ValueContext(i_wt + weight).indexNumeric())
//					  val au_wt = user_node.getProperty("weight").toString().toInt
//			          user_node.setProperty("weight",au_wt + weight)
//			          val author_weight_index = getNodeIndex("user_weight").get
//			          author_weight_index.remove(user_node)
//				      author_weight_index += (user_node,"weight",new ValueContext(au_wt + weight).indexNumeric())
//				    
//				      val art_written_rel = article_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING)
//				      val in_wt = art_written_rel.getProperty("in_weight").toString().toInt
//				      val out_wt = art_written_rel.getProperty("out_weight").toString().toInt
//				      
//				      art_written_rel.setProperty("in_weight",in_wt+weight)
//				      art_written_rel.setProperty("out_weight",out_wt+weight)
//				      
//				      
//			       }
//		      }
		      
		      
		             var cats_list = article_node.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.toList.map(_.getEndNode().getProperty("name").toString().toLowerCase())
				     for(each <- cats_list)
				     {
				         var news_node = TilesIndex.get("id",each).getSingle()
				         if(news_node != null)
				         {
					         var news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     if(news_list.contains(a_id))
					         {
					           news_list = news_list - a_id
					           val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				               news_node.setProperty("value",news_list_trim)
					         }
				         }
				         
				         news_node = FeaturedTilesIndex.get("id",each).getSingle()
				         if(news_node != null)
				         {
					         var news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     if(news_list.contains(a_id))
					         {
					           news_list = news_list - a_id
					           val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				               news_node.setProperty("value",news_list_trim)
					         }
				         }
				         
				         news_node = HeadlinesIndex.get("id",each).getSingle()
				         if(news_node != null)
				         {
					         var news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     if(news_list.contains(a_id))
					         {
					           news_list = news_list - a_id
					           val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				               news_node.setProperty("value",news_list_trim)
					         }
				         }
					     
				      
					    
				       
				     }
		      
		      var rels = article_node.getRelationships("Belongs_To_Topic","Belongs_To_Category","Belongs_To_Location_Article","User_Of_Article").asScala
		      for(each <- rels)
		      {
		        each.delete()
		      }
		      
		      if(!related_articles.equalsIgnoreCase(""))
	          {
	            for(art <- related_articles.split(",")) 
	            {
	            	val rel: org.neo4j.graphdb.Relationship = nodeIndex.get("id",art).getSingle() --> "Related_Article_To" --> article_node < //Relating related article nodes to the new article node
	       			val rel_time = rel.setProperty("time", a_time_created)
	            }
	          }
	     	  if(!related_events.equalsIgnoreCase(""))
	     	  {
	     		 for(event <- related_events.split(",")) 
	     		 {
	     		     val event_node = EventIndex.get("id",event).getSingle()
	     		     if(event_node != null)
	     		     {
		     			 val rel: org.neo4j.graphdb.Relationship = event_node --> "Related_Event_To" --> article_node <    //Relating related event nodes to the new article node
		     			 val rel_time = rel.setProperty("time", a_time_created)
	     		     }
	     		 }
	     	  }
		      
		      var spam_flag = 0
		      if(article_node.hasRelationship("User_Spammed_Of_Category",Direction.OUTGOING))
		      {
		        spam_flag = 1
		        val spam_rels = article_node.getRelationships("User_Spammed_Of_Category",Direction.OUTGOING).asScala
		        for(each <- spam_rels)
		        {
		          each.delete()
		        }
		      }
		
		       val tags_list = a_hashtags.split(",").distinct.toList.filterNot( x => x.equals("")).map( x => x.toLowerCase())
		       var old_tags = List[Any]()
		       rels = article_node.getRelationships("Tag_Of_Article").asScala
		       for(each <- rels)
		       {
		           val tag = each.getOtherNode(article_node).getProperty("name").toString()
		           old_tags ::= tag
		           if(!tags_list.contains(tag))
		           {
		             val tag_node = each.getOtherNode(article_node)
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
		                  var rel = tag_node --> "Tag_Of_Article" --> article_node <
		                  var rel_time = rel.setProperty("time", a_time_created)
		                  var rel_in_wt = rel.setProperty("in_weight", 0)
		                  var rel_out_wt = rel.setProperty("out_weight", 10)
		                  hash_weight_index.remove(tag_node)
		                  hash_weight_index += (tag_node,"weight",new ValueContext( h_wt + 10 ).indexNumeric())
		                  
		                }
		                else
		                {
		                  val tag_node = createNode(sub_category(tag_name,a_time_created,10))
		                  SubcatIndex += (tag_node,"name",tag_name)
		                  var rel = tag_node --> "Tag_Of_Article" --> article_node <
		                  var rel_time = rel.setProperty("time", a_time_created)
		                  var rel_in_wt = rel.setProperty("in_weight", 0)
		                  var rel_out_wt = rel.setProperty("out_weight", 10)
		                  rel = tag_node --> "Hashtag_Created_By" --> user_node <
		                  var rel_time1 = rel.setProperty("time", time)
		                  hash_weight_index += (tag_node,"weight",new ValueContext( 10 ).indexNumeric())
		                  
		                }
		              }
		         }
		       }
		       
		          if(!a_users.equals(""))
		          {
		            val a_users_list = a_users.split(",").distinct
		            for(each <- a_users_list)
		            {
		              if(!each.equals(""))
		              {
		              
			              val user_node1 = UserIndex.get("id",each).getSingle()
			              if(user_node1 != null)
			              {
			                val rel1: org.neo4j.graphdb.Relationship = user_node1 --> "User_Of_Article"  --> article_node <
			                val y = rel1.setProperty("time", a_time_created)
			              }
		              }
		          
		            }
		          
			      }
		       
		       
		       var hash_list = a_subcat.split(",").toList.filterNot( x => x.equals("")).map( x => x.toLowerCase())
		       var main_hash = hash_list(0)
		       var old_hash = List[Any]()
		       rels = article_node.getRelationships("Belongs_To_Subcategory_Article").asScala
		       var r:org.neo4j.graphdb.Relationship = null
		       for(each <- rels)
		       {
		           val tag = each.getOtherNode(article_node).getProperty("name").toString().toLowerCase()
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
		             val tag_node = each.getOtherNode(article_node)
		             val h_wt = tag_node.getProperty("weight").toString().toInt
		             tag_node.setProperty("weight",(h_wt - art_wt))
		             hash_weight_index.remove(tag_node)
		             hash_weight_index += (tag_node,"weight",new ValueContext( h_wt - art_wt ).indexNumeric())
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
		                subcat_node.setProperty("weight",(h_wt + art_wt))
		                if(tag_name.equals(main_hash))
		                {
			                var rel = article_node --> "Belongs_To_Subcategory_Article" --> subcat_node <
			                var rel_main = rel.setProperty("main", 1)
			                var rel_time = rel.setProperty("time", a_time_created)
			                var rel_in_wt = rel.setProperty("in_weight", art_wt)
			                var rel_out_wt = rel.setProperty("out_weight", 0)
		                }
		                else
		                {
		                    var rel = article_node --> "Belongs_To_Subcategory_Article" --> subcat_node <
			                var rel_time = rel.setProperty("time", a_time_created)
			                var rel_in_wt = rel.setProperty("in_weight", art_wt)
			                var rel_out_wt = rel.setProperty("out_weight", 0)
		                }
		                hash_weight_index.remove(subcat_node)
		                hash_weight_index += (subcat_node,"weight",new ValueContext( h_wt + art_wt ).indexNumeric())
		              }
		              else
		              {
		                val subcat_node = createNode(sub_category(tag_name,a_time_created,art_wt))
		                SubcatIndex += (subcat_node,"name",tag_name)
		                if(tag_name.equals(main_hash))
		                {
			                var rel = article_node --> "Belongs_To_Subcategory_Article" --> subcat_node <
			                var rel_main = rel.setProperty("main", 1)
			                var rel_time = rel.setProperty("time", a_time_created)
			                var rel_in_wt = rel.setProperty("in_weight", art_wt)
			                var rel_out_wt = rel.setProperty("out_weight", 0)
		                }
		                else
		                {
		                    var rel = article_node --> "Belongs_To_Subcategory_Article" --> subcat_node <
			                var rel_time = rel.setProperty("time", a_time_created)
			                var rel_in_wt = rel.setProperty("in_weight", art_wt)
			                var rel_out_wt = rel.setProperty("out_weight", 0)
		                }
		                
		                var rel = subcat_node --> "Hashtag_Created_By" --> user_node <
		                var rel_time1 = rel.setProperty("time", time)
		                
		                hash_weight_index += (subcat_node,"weight",new ValueContext( art_wt).indexNumeric())
		              }
		            }
		         }
		         
		         else if(old_hash.contains(tag_name) && tag_name.equals(main_hash))
		         {
		           r.setProperty("main", 1)
		         }
		       }
		       
		       
		       
		       
		     val location_index = getNodeIndex("location").get
		     val all_cities = location_index.get("id","all").getSingle().getProperty("cities").toString().split(",").toList
		     val cities = index_data.toLowerCase().split(" ").toList.distinct.intersect(all_cities)
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
		               article_node --> "Belongs_To_Location_Article" --> e 
	
		               loop.break;
		             }
		           }
	             }
		       }
		     }
		      
		     val CategoryIndex = getNodeIndex("category").get
	         var cats = a_cat.split(",").toList.map( x => x.toLowerCase()) 
		     cats ::= "all"
		       
		     val cur_time = a_time_created
		     val t = cur_time - 86400
		     
		     for(each <- cats)
		     {
		       
		               var news_node = TilesIndex.get("id",each).getSingle()
		               if(space == 0)
		               {
			    	 	   
					       if(news_node == null)
					       {
					         val tiles_node =  createNode(tiles(each,a_id))
					         TilesIndex += (tiles_node,"id",each)
					       }
					       else
					       {
					         
						     var sorted_news  = List[org.neo4j.graphdb.Node]()
						     val news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     news_list ::= a_id
						     val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				             news_node.setProperty("value",news_list_trim)
					      
						    
					       }
		               }
		    	 	   
		    	 	   if(feu == 1)
		    	 	   {
			    	 	   news_node = FeaturedTilesIndex.get("id",each).getSingle()
					       if(news_node == null)
					       {
					         val tiles_node =  createNode(featured_tiles(each,a_id))
					         FeaturedTilesIndex += (tiles_node,"id",each)
					       }
					       else
					       {
					         
						     var sorted_news  = List[org.neo4j.graphdb.Node]()
						     val news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     news_list ::= a_id
						     val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				             news_node.setProperty("value",news_list_trim)
					      
						    
					       }
		    	 	   }
		    	 	   
		    	 	   if(head == 1)
		    	 	   {
			    	 	   news_node = HeadlinesIndex.get("id",each).getSingle()
					       if(news_node == null)
					       {
					         val tiles_node =  createNode(headlines(each,a_id))
					         HeadlinesIndex += (tiles_node,"id",each)
					       }
					       else
					       {
					         
						     var sorted_news  = List[org.neo4j.graphdb.Node]()
						     val news = news_node.getProperty("value").toString()
						     var news_list = news.split(",").toList
						     news_list ::= a_id
						     val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
				             news_node.setProperty("value",news_list_trim)
					      
						    
					       }
		    	 	   }
		       if(CategoryIndex.get("name",each).getSingle() != null)  
		       {
		         
		        // val CategoryIndex = getNodeIndex("category").get
		         val category_node = CategoryIndex.get("name",each).getSingle()
	
		         var rel = article_node --> "Belongs_To_Category" --> category_node <
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
		               rel = article_node --> "Belongs_To_Topic" --> top <
		               var rel_time = rel.setProperty("time", time)
		               var count = rel.setProperty("count", inter)
		             }
		             
		           }
		         }
		       
		         if(spam_flag == 1)
		         {
		           rel = article_node --> "User_Spammed_Of_Category" --> category_node <
		           var rel_time = rel.setProperty("time", time)
		         }
		         
		       }
		       else
		       {
		         val category_node = createNode(category(each,"",""))
		         CategoryIndex += (category_node,"name",each)
	
		         var rel = article_node --> "Belongs_To_Category" --> category_node <
		         var rel_time = rel.setProperty("time", time)
		         if(spam_flag == 1)
		         {
		           rel = article_node --> "User_Spammed_Of_Category" --> category_node <
		           var rel_time = rel.setProperty("time", time)
		         }
		         
		       }
		     }
		     
		  
		          val tagger = new MaxentTagger(
	                "/var/n4j/data/left3words-wsj-0-18.tagger")
	              
	              val tagged = tagger.tagString(a_content)
	              val noun_list =  tagged.split(" ").toList.map(x => x.split("/")).filter( x => (x.size == 2 && x(1).equals("NNP"))).map(x => x(0).toLowerCase())
	              var noun_hash_list = noun_list:::hash_list
	              ArticleContentIndex += (article_node,"article_content",(noun_hash_list.mkString(" ") + " " + bummy(a_title))  )
	              val proper_nouns = List("2013","2014","india","indian","reddy","rao","singh","world","pti","sunday","monday","tuesday","wednesday","thursday","friday","saturday","january","february","march","april","may","june","july","august","september","october","november","december")
	              noun_hash_list = noun_hash_list.filterNot(x => proper_nouns.contains(x) && x.length() < 3)
	              article_node.setProperty("article_content",noun_hash_list.mkString(" "))
	              
	
		          ret = true  
	      }
		}
    
    ret
    }
    
  }
  
  def featured_item(item_type: String,
    item_id: String
    ):Boolean=
    { 
    
    withTx {
      implicit neo =>
         // Relating article to the categories
	     
	     var ret = false
	     val TilesIndex = getNodeIndex("featured_tiles").get
	     if(item_type.toLowerCase().equals("a"))
	     {
	         val ArticleIndex = getNodeIndex("article").get
		     
		     val a_node = ArticleIndex.get("id",item_id).getSingle()
		     if(a_node != null)
		     {
		         val feu = a_node.getProperty("approved").toString().toInt
		         if(feu == 0)
		         {
			         a_node.setProperty("approved",1)
				     var cats = a_node.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.toList.map(_.getEndNode().getProperty("name").toString().toLowerCase())
				     
				     //val cur_time = a_time_created
				     //val t = cur_time - 86400
				     for(each <- cats)
				     {
				       var news_node = TilesIndex.get("id",each).getSingle()
				       if(news_node == null)
				       {
				         val tiles_node =  createNode(featured_tiles(each,item_id))
				         TilesIndex += (tiles_node,"id",each)
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
				     }
			         if(!cats.contains("blog"))
			         {
				         var latest_node = TilesIndex.get("id","latest").getSingle()
				         val latest = latest_node.getProperty("value").toString()
				         var latest_list = latest.split(",").toList
						 latest_list ::= item_id
						 val latest_list_trim = latest_list.slice(0,30).mkString(",").stripPrefix(",").stripSuffix(",").trim
				         latest_node.setProperty("value",latest_list_trim)
			         }
		         }
		         
		         else
		         {
			         a_node.setProperty("approved",0)
			         // unpinning the article
			         pin_item("A",item_id,"","")
			           
			         // removing the article from the exclusive articles
//			         exclusive_article(item_id,"")
				     var cats = a_node.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.toList.map(_.getEndNode().getProperty("name").toString().toLowerCase())
				     
				     //val cur_time = a_time_created
				     //val t = cur_time - 86400
				     for(each <- cats)
				     {
				         var news_node = TilesIndex.get("id",each).getSingle()
				         var sorted_news  = List[org.neo4j.graphdb.Node]()
					     val news = news_node.getProperty("value").toString()
					     var news_list = news.split(",").toList
					     if(news_list.contains(item_id))
				         {
				           news_list = news_list - item_id
				           val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
			               news_node.setProperty("value",news_list_trim)
				         }
					     
				      
					    
				       
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
	     
	     else if(item_type.toLowerCase().equals("e"))
	     {
	         val EventIndex = getNodeIndex("event").get
		     val e_node = EventIndex.get("id",item_id).getSingle()
		     if(e_node != null)
		     {
		         val feu = e_node.getProperty("approved").toString().toInt
		         if(feu == 0)
		         {
			         e_node.setProperty("approved",1)
				     var cats = e_node.getRelationships("Belongs_To_Event_Category",Direction.OUTGOING).asScala.toList.map(_.getEndNode().getProperty("name").toString().toLowerCase())
				     
				     //val cur_time = a_time_created
				     //val t = cur_time - 86400
				     for(each <- cats)
				     {
				       var news_node = TilesIndex.get("id",each).getSingle()
				       if(news_node == null)
				       {
				         val tiles_node =  createNode(featured_tiles(each,item_id))
				         TilesIndex += (tiles_node,"id",each)
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
				     }
			         if(!cats.contains("blog"))
			         {
				         var latest_node = TilesIndex.get("id","latest").getSingle()
				         val latest = latest_node.getProperty("value").toString()
				         var latest_list = latest.split(",").toList
						 latest_list ::= item_id
						 val latest_list_trim = latest_list.slice(0,30).mkString(",").stripPrefix(",").stripSuffix(",").trim
				         latest_node.setProperty("value",latest_list_trim)
			         }
		         }
		         
		         else
		         {
			         e_node.setProperty("approved",0)
			         // unpinning the article
			         pin_item("E",item_id,"","")
			           
			         // removing the article from the exclusive articles
//			         exclusive_article(item_id,"")
				     var cats = e_node.getRelationships("Belongs_To_Event_Category",Direction.OUTGOING).asScala.toList.map(_.getEndNode().getProperty("name").toString().toLowerCase())
				     
				     //val cur_time = a_time_created
				     //val t = cur_time - 86400
				     for(each <- cats)
				     {
				         var news_node = TilesIndex.get("id",each).getSingle()
				         var sorted_news  = List[org.neo4j.graphdb.Node]()
					     val news = news_node.getProperty("value").toString()
					     var news_list = news.split(",").toList
					     if(news_list.contains(item_id))
				         {
				           news_list = news_list - item_id
				           val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
			               news_node.setProperty("value",news_list_trim)
				         }
					     
				      
					    
				       
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
	     
	     else if(item_type.toLowerCase().equals("p"))
	     {
	         val PetitionIndex = getNodeIndex("petition").get
		     val p_node = PetitionIndex.get("id",item_id).getSingle()
		     if(p_node != null)
		     {
		         val feu = p_node.getProperty("approved").toString().toInt
		         if(feu == 0)
		         {
			         p_node.setProperty("approved",1)
				     var cats = p_node.getRelationships("Belongs_To_Petition_Category",Direction.OUTGOING).asScala.toList.map(_.getEndNode().getProperty("name").toString().toLowerCase())
				     
				     //val cur_time = a_time_created
				     //val t = cur_time - 86400
				     for(each <- cats)
				     {
				       var news_node = TilesIndex.get("id",each).getSingle()
				       if(news_node == null)
				       {
				         val tiles_node =  createNode(featured_tiles(each,item_id))
				         TilesIndex += (tiles_node,"id",each)
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
				     }
			         if(!cats.contains("blog"))
			         {
				         var latest_node = TilesIndex.get("id","latest").getSingle()
				         val latest = latest_node.getProperty("value").toString()
				         var latest_list = latest.split(",").toList
						 latest_list ::= item_id
						 val latest_list_trim = latest_list.slice(0,30).mkString(",").stripPrefix(",").stripSuffix(",").trim
				         latest_node.setProperty("value",latest_list_trim)
			         }
		         }
		         
		         else
		         {
			         p_node.setProperty("approved",0)
			         // unpinning the article
			         pin_item("P",item_id,"","")
			           
			         // removing the article from the exclusive articles
//			         exclusive_article(item_id,"")
				     var cats = p_node.getRelationships("Belongs_To_Petition_Category",Direction.OUTGOING).asScala.toList.map(_.getEndNode().getProperty("name").toString().toLowerCase())
				     
				     //val cur_time = a_time_created
				     //val t = cur_time - 86400
				     for(each <- cats)
				     {
				         var news_node = TilesIndex.get("id",each).getSingle()
				         var sorted_news  = List[org.neo4j.graphdb.Node]()
					     val news = news_node.getProperty("value").toString()
					     var news_list = news.split(",").toList
					     if(news_list.contains(item_id))
				         {
				           news_list = news_list - item_id
				           val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
			               news_node.setProperty("value",news_list_trim)
				         }
					     
				      
					    
				       
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
	     else if(item_type.toLowerCase().equals("t"))
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
			         pin_item("T",item_id,"","")
			           
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
			         pin_item("D",item_id,"","")
			           
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
  
  
  def headlines_item(item_type: String,
    item_id: String
    ):Boolean=
    { 
    
    withTx {
      implicit neo =>
         // Relating article to the categories
	     
	     var ret = false
	     val TilesIndex = getNodeIndex("headlines").get
	     if(item_type.toLowerCase().equals("a"))
	     {
	         val ArticleIndex = getNodeIndex("article").get
		     
		     val a_node = ArticleIndex.get("id",item_id).getSingle()
		     if(a_node != null)
		     {
		         val feu = a_node.getProperty("head").toString().toInt
		         if(feu == 0)
		         {
			         a_node.setProperty("head",1)
				     var cats = a_node.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.toList.map(_.getEndNode().getProperty("name").toString().toLowerCase())
				     
				     //val cur_time = a_time_created
				     //val t = cur_time - 86400
				     for(each <- cats)
				     {
				       var news_node = TilesIndex.get("id",each).getSingle()
				       if(news_node == null)
				       {
				         val tiles_node =  createNode(headlines(each,item_id))
				         TilesIndex += (tiles_node,"id",each)
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
				     }
			         
		         }
		         
		         else
		         {
			         a_node.setProperty("head",0)
			         // unpinning the article
			         
			           
			         // removing the article from the exclusive articles
//			         exclusive_article(item_id,"")
				     var cats = a_node.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.toList.map(_.getEndNode().getProperty("name").toString().toLowerCase())
				     
				     //val cur_time = a_time_created
				     //val t = cur_time - 86400
				     for(each <- cats)
				     {
				         var news_node = TilesIndex.get("id",each).getSingle()
				         var sorted_news  = List[org.neo4j.graphdb.Node]()
					     val news = news_node.getProperty("value").toString()
					     var news_list = news.split(",").toList
					     if(news_list.contains(item_id))
				         {
				           news_list = news_list - item_id
				           val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
			               news_node.setProperty("value",news_list_trim)
				         }
					     
				      
					    
				       
				     }
			         
					 
		         }
			     ret = true
		     }
	     }
	     
	     else if(item_type.toLowerCase().equals("e"))
	     {
	         val EventIndex = getNodeIndex("event").get
		     val e_node = EventIndex.get("id",item_id).getSingle()
		     if(e_node != null)
		     {
		         val feu = e_node.getProperty("head").toString().toInt
		         if(feu == 0)
		         {
			         e_node.setProperty("head",1)
				     var cats = e_node.getRelationships("Belongs_To_Event_Category",Direction.OUTGOING).asScala.toList.map(_.getEndNode().getProperty("name").toString().toLowerCase())
				     
				     //val cur_time = a_time_created
				     //val t = cur_time - 86400
				     for(each <- cats)
				     {
				       var news_node = TilesIndex.get("id",each).getSingle()
				       if(news_node == null)
				       {
				         val tiles_node =  createNode(headlines(each,item_id))
				         TilesIndex += (tiles_node,"id",each)
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
				     }
			         
		         }
		         
		         else
		         {
			         e_node.setProperty("head",0)
			         // unpinning the article
			         
			           
			         // removing the article from the exclusive articles
//			         exclusive_article(item_id,"")
				     var cats = e_node.getRelationships("Belongs_To_Event_Category",Direction.OUTGOING).asScala.toList.map(_.getEndNode().getProperty("name").toString().toLowerCase())
				     
				     //val cur_time = a_time_created
				     //val t = cur_time - 86400
				     for(each <- cats)
				     {
				         var news_node = TilesIndex.get("id",each).getSingle()
				         var sorted_news  = List[org.neo4j.graphdb.Node]()
					     val news = news_node.getProperty("value").toString()
					     var news_list = news.split(",").toList
					     if(news_list.contains(item_id))
				         {
				           news_list = news_list - item_id
				           val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
			               news_node.setProperty("value",news_list_trim)
				         }
					     
				      
					    
				       
				     }
			         
					 
		         }
			     ret = true
		     }
	     }
	     
	     else if(item_type.toLowerCase().equals("p"))
	     {
	         val PetitionIndex = getNodeIndex("petition").get
		     val p_node = PetitionIndex.get("id",item_id).getSingle()
		     if(p_node != null)
		     {
		         val feu = p_node.getProperty("head").toString().toInt
		         if(feu == 0)
		         {
			         p_node.setProperty("head",1)
				     var cats = p_node.getRelationships("Belongs_To_Petition_Category",Direction.OUTGOING).asScala.toList.map(_.getEndNode().getProperty("name").toString().toLowerCase())
				     
				     //val cur_time = a_time_created
				     //val t = cur_time - 86400
				     for(each <- cats)
				     {
				       var news_node = TilesIndex.get("id",each).getSingle()
				       if(news_node == null)
				       {
				         val tiles_node =  createNode(headlines(each,item_id))
				         TilesIndex += (tiles_node,"id",each)
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
				     }
			         
		         }
		         
		         else
		         {
			         p_node.setProperty("head",0)
			         // unpinning the article
			         pin_item("P",item_id,"","")
			           
			         // removing the article from the exclusive articles
//			         exclusive_article(item_id,"")
				     var cats = p_node.getRelationships("Belongs_To_Petition_Category",Direction.OUTGOING).asScala.toList.map(_.getEndNode().getProperty("name").toString().toLowerCase())
				     
				     //val cur_time = a_time_created
				     //val t = cur_time - 86400
				     for(each <- cats)
				     {
				         var news_node = TilesIndex.get("id",each).getSingle()
				         var sorted_news  = List[org.neo4j.graphdb.Node]()
					     val news = news_node.getProperty("value").toString()
					     var news_list = news.split(",").toList
					     if(news_list.contains(item_id))
				         {
				           news_list = news_list - item_id
				           val news_list_trim = news_list.mkString(",").stripPrefix(",").stripSuffix(",").trim
			               news_node.setProperty("value",news_list_trim)
				         }
					     
				      
					    
				       
				     }
			         
					 
		         }
			     ret = true
		     }
	     }
	     else if(item_type.toLowerCase().equals("t"))
	     {
	         val TownhallIndex = getNodeIndex("townhall").get
		     val t_node = TownhallIndex.get("id",item_id).getSingle()
		     if(t_node != null)
		     {
		         val feu = t_node.getProperty("head").toString().toInt
		         if(feu == 0)
		         {
			         t_node.setProperty("head",1)
				     var cat = "all"
				     //val cur_time = a_time_created
				     //val t = cur_time - 86400
				       var news_node = TilesIndex.get("id",cat).getSingle()
				       if(news_node == null)
				       {
				         val tiles_node =  createNode(headlines(cat,item_id))
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
				     
			             
			         
		         }
		         
		         else
		         {
			         t_node.setProperty("head",0)
			         // unpinning the article
			         
			           
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
		         val feu = d_node.getProperty("head").toString().toInt
		         if(feu == 0)
		         {
			         d_node.setProperty("head",1)
				     var cat = "all"
				     //val cur_time = a_time_created
				     //val t = cur_time - 86400
				       var news_node = TilesIndex.get("id",cat).getSingle()
				       if(news_node == null)
				       {
				         val tiles_node =  createNode(headlines(cat,item_id))
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
				     
			             
			         
		         }
		         
		         else
		         {
			         d_node.setProperty("head",0)
			         // unpinning the article
			         
			           
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
					     
				      
					    
				       
				     
			         
					 
		         }
			     ret = true
		     }
	     }
	     
    
    ret
    }
    }
  
  // Triggered when a poll is created
  def create_poll(
    item_type: String,
    item_id: String,
    p_id: String,
    p_qtn: String,
    p_status: Int,
    p_time_created: Int
    
    ):Boolean=   
    {
    
    
    withTx {
      implicit neo =>
      var ret = false 
      
       var i_type = "Article"
       var rel_name = "Poll_App_Of"
       if(item_type.equals("E"))
       {
         i_type = "Event"
         rel_name = "Poll_App_Of_Event"
       }
       else if(item_type.equals("P"))
       {
         i_type = "Petition"
         rel_name = "Poll_App_Of_Petition"
       }
       val index_name = i_type.toLowerCase()
       val index = getNodeIndex(index_name).get
       //val avg_wt_index = getNodeIndex("avg_weights").get
       val item_node = index.get("id",item_id).getSingle()
       
      val PollIndex = getNodeIndex("poll").get
      var poll_node = PollIndex.get("id",p_id).getSingle()
      // checking if the poll node and item node exist
      if(poll_node == null && item_node != null)
      {
        // creating a poll node
        poll_node = createNode(poll(p_id,p_qtn,p_status,p_time_created))
        PollIndex += (poll_node,"id",p_id)
        
        // relating poll node to the item node
        var rel: org.neo4j.graphdb.Relationship = poll_node --> rel_name --> item_node <
        var rel_time = rel.setProperty("time", p_time_created)
        ret = true
      }
      
      
    ret  
    }
    }
  
  
  // Triggered when some one responds to a poll
  def respond_poll(
    user_name: String,
    p_id: String,
    time: Int
    
    ):Boolean=   
    {
    
    
    withTx {
      implicit neo =>
      var ret = false 
      val UserIndex = getNodeIndex("user").get
      val PollIndex = getNodeIndex("poll").get
      val poll_node = PollIndex.get("id",p_id).getSingle()
      val user_node = UserIndex.get("id",user_name).getSingle()
      // checking of the user voted to the poll or not
      if(poll_node != null && user_node != null && !poll_node.getRelationships("Voted_To_Poll").asScala.toList.map(_.getOtherNode(poll_node)).contains(user_node))
      {
        user_node.setProperty("last_seen",time)
        // relating user to the poll
        var rel: org.neo4j.graphdb.Relationship = user_node --> "Voted_To_Poll" --> poll_node <
        var rel_time = rel.setProperty("time", time)
        ret = true
        
      }
      
      
    ret  
    }
    }
  
  // Triggered when an article is edited
  def edit_article(
    user_name: String,
    a_id: String,
    a_title_id: String,
    a_title: String,
    a_content: String,
    a_summary: String,
    a_fut_image: String,
    a_cat: String,
    a_subcat: String,
    a_hashtags: String,
    a_users: String,
    a_time_created: Int,
    related_articles: String,     //List of related articles marked by user
    related_events: String,
    mod_name: String,
    stars: Int
    ):Boolean=       //List of related events marked by user   
    {
    
    
    
    withTx {
      implicit neo =>
      var ret = false 
      val cur_time = (System.currentTimeMillis() /1000).toInt
      val nodeIndex = getNodeIndex("article").get
      val EventIndex = getNodeIndex("event").get
      val UserIndex = getNodeIndex("user").get
      val SubcatIndex = getNodeIndex("sub_category").get
      val TopicIndex = getNodeIndex("topic").get
      val TilesIndex = getNodeIndex("tiles").get
      val ArticleContentIndex = getNodeIndex("article_content").get
      val ArticleTitleIndex = getNodeIndex("article_title").get
      val hash_weight_index = getNodeIndex("hash_weight").get
      val article_node = nodeIndex.get("id",a_id).getSingle()
      val time = article_node.getProperty("time_created").toString().toInt
      val user_node = UserIndex.get("id",user_name).getSingle()
      if(article_node != null && user_node != null && !a_cat.equals("") && !a_subcat.equals(""))
	  {
          user_node.setProperty("last_seen",a_time_created)
	      val art_wt = article_node.getProperty("weight").toString().toInt
	      article_node.setProperty("article_title_id",a_title_id)
	      article_node.setProperty("article_title",a_title)
	      article_node.setProperty("article_summary",a_summary)
	      article_node.setProperty("article_featured_img",a_fut_image)
	      article_node.setProperty("stars",stars)
	      ArticleContentIndex.remove(article_node)
	      val h_list = a_subcat.split(",").toList.map( x => x.toLowerCase())
	      val index_data = bummy(a_title+" "+a_summary+" "+a_content)+" "+h_list.mkString(" ")
	      
	      ArticleContentIndex += (article_node,"time",new ValueContext( time ).indexNumeric())
	      ArticleTitleIndex.remove(article_node)
	      ArticleTitleIndex += (article_node,"title_id",a_title_id)
	      if(stars > 0)
	      {
	           
		       val mod_node = UserIndex.get("id",mod_name).getSingle()
		       var news_node = TilesIndex.get("id","all").getSingle()
		       if(news_node != null && mod_node != null && !article_node.hasRelationship("Pushed_By",Direction.OUTGOING))
		       {
		         
                  val news = news_node.getProperty("value").toString().split(",").toList.slice(0,10).map( x => nodeIndex.get("id",x).getSingle().getProperty("weight").toString().toFloat)
                  val top_10_avg = news.sum/news.size
                  var weight = Math.round(top_10_avg / (6-stars))
		          val rel: org.neo4j.graphdb.Relationship = article_node --> "Pushed_By" --> mod_node <
	              val rel_time = rel.setProperty("time", time)
	              var rel_in_wt = rel.setProperty("in_weight",0)
	              var rel_out_wt = rel.setProperty("out_weight", weight)
			      val i_wt = article_node.getProperty("weight").toString().toInt
			      article_node.setProperty("weight",i_wt + weight)
			      val weight_index = getNodeIndex("article_weight").get
			      weight_index.remove(article_node)
				  weight_index += (article_node,"weight",new ValueContext(i_wt + weight).indexNumeric())
				  val au_wt = user_node.getProperty("weight").toString().toInt
		          user_node.setProperty("weight",au_wt + weight)
		          val author_weight_index = getNodeIndex("user_weight").get
		          author_weight_index.remove(user_node)
			      author_weight_index += (user_node,"weight",new ValueContext(au_wt + weight).indexNumeric())
			    
			      val art_written_rel = article_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING)
			      val in_wt = art_written_rel.getProperty("in_weight").toString().toInt
			      val out_wt = art_written_rel.getProperty("out_weight").toString().toInt
			      
			      art_written_rel.setProperty("in_weight",in_wt+weight)
			      art_written_rel.setProperty("out_weight",out_wt+weight)
			      
			      
		       }
	      }
	      
	      
	      
	      var rels = article_node.getRelationships("Belongs_To_Category","Belongs_To_Location_Article","User_Of_Article").asScala
	      for(each <- rels)
	      {
	        each.delete()
	      }
	      
	      if(!related_articles.equalsIgnoreCase(""))
          {
            for(art <- related_articles.split(",")) 
            {
            	val rel: org.neo4j.graphdb.Relationship = nodeIndex.get("id",art).getSingle() --> "Related_Article_To" --> article_node < //Relating related article nodes to the new article node
       			val rel_time = rel.setProperty("time", a_time_created)
            }
          }
     	  if(!related_events.equalsIgnoreCase(""))
     	  {
     		 for(event <- related_events.split(",")) 
     		 {
     		     val event_node = EventIndex.get("id",event).getSingle()
     		     if(event_node != null)
     		     {
	     			 val rel: org.neo4j.graphdb.Relationship = event_node --> "Related_Event_To" --> article_node <    //Relating related event nodes to the new article node
	     			 val rel_time = rel.setProperty("time", a_time_created)
     		     }
     		 }
     	  }
	      
	      var spam_flag = 0
	      if(article_node.hasRelationship("User_Spammed_Of_Category",Direction.OUTGOING))
	      {
	        spam_flag = 1
	        val spam_rels = article_node.getRelationships("User_Spammed_Of_Category",Direction.OUTGOING).asScala
	        for(each <- spam_rels)
	        {
	          each.delete()
	        }
	      }
	
	       val tags_list = a_hashtags.split(",").distinct.toList.map( x => x.toLowerCase())
	       var old_tags = List[Any]()
	       rels = article_node.getRelationships("Tag_Of_Article").asScala
	       for(each <- rels)
	       {
	           val tag = each.getOtherNode(article_node).getProperty("name").toString()
	           old_tags ::= tag
	           if(!tags_list.contains(tag))
	           {
	             val tag_node = each.getOtherNode(article_node)
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
	                  var rel = tag_node --> "Tag_Of_Article" --> article_node <
	                  var rel_time = rel.setProperty("time", a_time_created)
	                  var rel_in_wt = rel.setProperty("in_weight", 0)
	                  var rel_out_wt = rel.setProperty("out_weight", 10)
	                  hash_weight_index.remove(tag_node)
	                  hash_weight_index += (tag_node,"weight",new ValueContext( h_wt + 10 ).indexNumeric())
	                  
	                }
	                else
	                {
	                  val tag_node = createNode(sub_category(tag_name,a_time_created,10))
	                  SubcatIndex += (tag_node,"name",tag_name)
	                  var rel = tag_node --> "Tag_Of_Article" --> article_node <
	                  var rel_time = rel.setProperty("time", a_time_created)
	                  var rel_in_wt = rel.setProperty("in_weight", 0)
	                  var rel_out_wt = rel.setProperty("out_weight", 10)
	                  rel = tag_node --> "Hashtag_Created_By" --> user_node <
	                  var rel_time1 = rel.setProperty("time", time)
	                  hash_weight_index += (tag_node,"weight",new ValueContext( 10 ).indexNumeric())
	                  
	                }
	              }
	         }
	       }
	       
	          if(!a_users.equals(""))
	          {
	            val a_users_list = a_users.split(",").distinct
	            for(each <- a_users_list)
	            {
	              if(!each.equals(""))
	              {
	              
		              val user_node1 = UserIndex.get("id",each).getSingle()
		              if(user_node1 != null)
		              {
		                val rel1: org.neo4j.graphdb.Relationship = user_node1 --> "User_Of_Article"  --> article_node <
		                val y = rel1.setProperty("time", a_time_created)
		              }
	              }
	          
	            }
	          
		      }
	       
	       
	       var hash_list = a_subcat.split(",").toList.map( x => x.toLowerCase())
	       var main_hash = hash_list(0)
	       var old_hash = List[Any]()
	       rels = article_node.getRelationships("Belongs_To_Subcategory_Article").asScala
	       
	       for(each <- rels)
	       {
	           val tag = each.getOtherNode(article_node).getProperty("name").toString().toLowerCase()
	           if(each.hasProperty("main") && !tag.equals(main_hash))
	           {
	             each.removeProperty("main")
	             
	           }
	           old_hash ::= tag
	           if(!hash_list.contains(tag))
	           {
	             val tag_node = each.getOtherNode(article_node)
	             val h_wt = tag_node.getProperty("weight").toString().toInt
	             tag_node.setProperty("weight",(h_wt - art_wt))
	             hash_weight_index.remove(tag_node)
	             hash_weight_index += (tag_node,"weight",new ValueContext( h_wt - art_wt ).indexNumeric())
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
	                subcat_node.setProperty("weight",(h_wt + art_wt))
	                if(tag_name.equals(main_hash))
	                {
		                var rel = article_node --> "Belongs_To_Subcategory_Article" --> subcat_node <
		                var rel_main = rel.setProperty("main", 1)
		                var rel_time = rel.setProperty("time", a_time_created)
		                var rel_in_wt = rel.setProperty("in_weight", art_wt)
		                var rel_out_wt = rel.setProperty("out_weight", 0)
	                }
	                else
	                {
	                    var rel = article_node --> "Belongs_To_Subcategory_Article" --> subcat_node <
		                var rel_time = rel.setProperty("time", a_time_created)
		                var rel_in_wt = rel.setProperty("in_weight", art_wt)
		                var rel_out_wt = rel.setProperty("out_weight", 0)
	                }
	                hash_weight_index.remove(subcat_node)
	                hash_weight_index += (subcat_node,"weight",new ValueContext( h_wt + art_wt ).indexNumeric())
	              }
	              else
	              {
	                val subcat_node = createNode(sub_category(tag_name,a_time_created,art_wt))
	                SubcatIndex += (subcat_node,"name",tag_name)
	                if(tag_name.equals(main_hash))
	                {
		                var rel = article_node --> "Belongs_To_Subcategory_Article" --> subcat_node <
		                var rel_main = rel.setProperty("main", 1)
		                var rel_time = rel.setProperty("time", a_time_created)
		                var rel_in_wt = rel.setProperty("in_weight", art_wt)
		                var rel_out_wt = rel.setProperty("out_weight", 0)
	                }
	                else
	                {
	                    var rel = article_node --> "Belongs_To_Subcategory_Article" --> subcat_node <
		                var rel_time = rel.setProperty("time", a_time_created)
		                var rel_in_wt = rel.setProperty("in_weight", art_wt)
		                var rel_out_wt = rel.setProperty("out_weight", 0)
	                }
	                
	                var rel = subcat_node --> "Hashtag_Created_By" --> user_node <
	                var rel_time1 = rel.setProperty("time", time)
	                
	                hash_weight_index += (subcat_node,"weight",new ValueContext( art_wt).indexNumeric())
	              }
	            }
	         }
	       }
	       
	       
	       
	       
	     val location_index = getNodeIndex("location").get
	     val all_cities = location_index.get("id","all").getSingle().getProperty("cities").toString().split(",").toList
	     val cities = index_data.toLowerCase().split(" ").toList.distinct.intersect(all_cities)
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
	               article_node --> "Belongs_To_Location_Article" --> e 

	               loop.break;
	             }
	           }
             }
	       }
	     }
	      
	     val CategoryIndex = getNodeIndex("category").get
         var cats = a_cat.split(",").toList.map( x => x.toLowerCase()) 
	     cats ::= "all"
	       
	     val cur_time = a_time_created
	     val t = cur_time - 86400
	     
	     for(each <- cats)
	     {
	       
	       
	       if(CategoryIndex.get("name",each).getSingle() != null)  
	       {
	         val category_node = CategoryIndex.get("name",each).getSingle()

	         var rel = article_node --> "Belongs_To_Category" --> category_node <
	         var rel_time = rel.setProperty("time", time)
	         if(spam_flag == 1)
	         {
	           rel = article_node --> "User_Spammed_Of_Category" --> category_node <
	           var rel_time = rel.setProperty("time", time)
	         }
	         
	       }
	       else
	       {
	         val category_node = createNode(category(each,"",""))
	         CategoryIndex += (category_node,"name",each)

	         var rel = article_node --> "Belongs_To_Category" --> category_node <
	         var rel_time = rel.setProperty("time", time)
	         if(spam_flag == 1)
	         {
	           rel = article_node --> "User_Spammed_Of_Category" --> category_node <
	           var rel_time = rel.setProperty("time", time)
	         }
	         
	       }
	     }
	  
	          val tagger = new MaxentTagger(
                "/var/n4j/data/left3words-wsj-0-18.tagger")
              
              val tagged = tagger.tagString(a_content)
              val noun_list =  tagged.split(" ").toList.map(x => x.split("/")).filter( x => (x.size == 2 && x(1).equals("NNP"))).map(x => x(0).toLowerCase())
              var noun_hash_list = noun_list:::hash_list
              ArticleContentIndex += (article_node,"article_content",(noun_hash_list.mkString(" ") + " " + bummy(a_title))  )
              val proper_nouns = List("2013","2014","india","indian","reddy","rao","singh","world","pti","sunday","monday","tuesday","wednesday","thursday","friday","saturday","january","february","march","april","may","june","july","august","september","october","november","december")
              noun_hash_list = noun_hash_list.filterNot(x => proper_nouns.contains(x) && x.length() < 3)
              article_node.setProperty("article_content",noun_hash_list.mkString(" "))
              

	          ret = true  
      }
      
      ret
    }
    
  }
  
  def get_articles_hashtag(
    user_name: String,  // unique user name
    hash: String,
    count: Int,  // No of articles required 
    prev_cnt:Int
    
    )  :String =        
    {
    
    
    
      
        
        
        val a_list = List("Comment_Count_Unique","ev","v_users","votes","Commented_Users","Comment_Count","Is_Neo4j","P_Pin","P_Id","P_Author","P_Author_FullName","P_IsMarkedReadLater","P_Title","P_Title_ID","P_Category","P_SubCategory","P_Num_Comments","P_Feature_Image","P_Smry", "P_TimeCreated", "P_EventLocation", "P_EventStartTime", "P_EventAttendStatus", "P_SignsRequired", "P_PetitionSignStatus")
		val l2 = List("FN","UN")
        val l3 = List("Name","UName")
        
        var art = List[org.neo4j.graphdb.Node]()
        var event = List[org.neo4j.graphdb.Node]()
        var petition = List[org.neo4j.graphdb.Node]()
        var debate = List[org.neo4j.graphdb.Node]()
        var townhall = List[org.neo4j.graphdb.Node]()
        
        var all_items = List[org.neo4j.graphdb.Node]()
        
        val SubcatIndex = getNodeIndex("sub_category").get
        val ArticleIndex = getNodeIndex("article").get
        val EventIndex = getNodeIndex("event").get
        val PetitionIndex = getNodeIndex("petition").get
        val TownhallIndex = getNodeIndex("townhall").get
        val DebateIndex = getNodeIndex("debate").get
        val UserIndex = getNodeIndex("user").get
        
        var hash_node = SubcatIndex.get("name",hash).getSingle()
        var user_node = UserIndex.get("id",user_name).getSingle()
        
        var out = List[Any]()
        if(hash_node != null )
        {
          
                var pin_tiles = List[String]()
			   
                
                // slicing the articles based on count 
			     
			      art = hash_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.map(_.getOtherNode(hash_node))
	              event = hash_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.map(_.getOtherNode(hash_node))
	              petition = hash_node.getRelationships("Belongs_To_Subcategory_Petition").asScala.toList.map(_.getOtherNode(hash_node))
	              debate = hash_node.getRelationships("Belongs_To_Subcategory_Debate").asScala.toList.map(_.getOtherNode(hash_node))
	              townhall = hash_node.getRelationships("Belongs_To_Subcategory_Townhall").asScala.toList.map(_.getOtherNode(hash_node))
	              
                  val sorted_items = (art:::event:::petition:::debate:::townhall).sortBy(-_.getProperty("time_created").toString().toInt).filter( x => x.getProperty("space").toString.toInt == 0)
                  all_items = (sorted_items).distinct.slice(prev_cnt,(prev_cnt+count))
                  
                
			    

                
                for(x <- all_items)
		        {
                  
		          if(x.getProperty("__CLASS__").toString.equals("Saddahaq.article"))
		          {
		            
		            out :+= JSONObject(a_list.zip(List(x.getRelationships("Comment_To_Article").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,0,JSONArray(x.getRelationships("article_voteup").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("article_voteup").asScala.toList.size-x.getRelationships("article_voteup").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Article").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Article").asScala.size,true,false,x.getProperty("article_id"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("article_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("article_title"),x.getProperty("article_title_id"),x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Article").asScala.size,x.getProperty("article_featured_img").toString(),x.getProperty("article_summary").toString(),x.getProperty("time_created").toString(),"","",false,"",false)).toMap)
					          
		          }
		          else if(x.getProperty("__CLASS__").toString.equals("Saddahaq.event"))
		          {
		            
		            out :+= JSONObject(a_list.zip(List(x.getRelationships("Comment_To_Event").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,1,JSONArray(x.getRelationships("Is_Attending").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Is_Attending").asScala.toList.size-x.getRelationships("Is_Attending").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Event").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Event").asScala.size,true,false,x.getProperty("event_id"),x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("event_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Event").asScala.size,x.getProperty("event_featured_img").toString(),x.getProperty("event_summary").toString(),x.getProperty("time_created").toString(),x.getProperty("event_location").toString(),x.getProperty("event_date_time").toString(),x.getRelationships("Is_Attending").asScala.map(_.getOtherNode(x)).toList.contains(user_node), "", false)).toMap)
					          
		          }
		          else if(x.getProperty("__CLASS__").toString.equals("Saddahaq.petition"))
		          {
		            
		            
		            out :+= JSONObject(a_list.zip(List(x.getRelationships("Comment_To_Petition").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,2,JSONArray(x.getRelationships("Signed_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Signed_Petition").asScala.toList.size-x.getRelationships("Signed_Petition").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Petition").asScala.size,true,false,x.getProperty("p_id"),x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("petition_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("p_title"),x.getProperty("p_title_id"),x.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Petition").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Petition").asScala.size,x.getProperty("p_img_url").toString(),x.getProperty("p_content").toString(),x.getProperty("time_created").toString(),"","",false,x.getProperty("p_target").toString().toInt - x.getProperty("p_count").toString().toInt,x.getRelationships("Signed_Petition").asScala.map(_.getOtherNode(x)).toList.contains(user_node))).toMap)
					          
		          }
		          else if(x.getProperty("__CLASS__").toString.equals("Saddahaq.townhall"))
		          {
		            out :+= JSONObject(a_list.zip(List(x.getRelationships("Commented_On_Townhall").asScala.toList.map(y=>y.getOtherNode(x)).distinct.size,3,JSONArray(x.getRelationships("Asked_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Asked_Question").asScala.toList.size-x.getRelationships("Asked_Question").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Commented_On_Townhall").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x)).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Commented_On_Townhall").asScala.size,true,false,x.getProperty("t_id"),x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("townhall_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("t_title"),x.getProperty("t_title_id"),"",x.getRelationships("Belongs_To_Subcategory_Townhall").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Commented_On_Townhall").asScala.size,x.getProperty("t_img_url").toString(),x.getProperty("t_content").toString(),x.getProperty("time_created").toString(),"","",false,"",false)).toMap)
					          
		          }
		          else
		          {
		            out :+= JSONObject(a_list.zip(List(x.getRelationships("Commented_On_Debate").asScala.toList.map(y=>y.getOtherNode(x)).distinct.size,4,JSONArray(x.getRelationships("Asked_Debate_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Asked_Debate_Question").asScala.toList.size-x.getRelationships("Asked_Debate_Question").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Commented_On_Debate").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x)).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Commented_On_Debate").asScala.size,true,false,x.getProperty("d_id"),x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("debate_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("d_title"),x.getProperty("d_title_id"),"",x.getRelationships("Belongs_To_Subcategory_Debate").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Commented_On_Debate").asScala.size,x.getProperty("d_img_url").toString(),x.getProperty("d_content").toString(),x.getProperty("time_created").toString(),"","",false,"",false)).toMap)
					          
		          }
		          
		          
		          
		        }
      
        
        }
        
    JSONArray(out).toString()  
    }
  
  def get_articles_space(
    user_name: String,  // unique user name
    space: String,
    count: Int,  // No of articles required 
    prev_cnt:Int,
    admin_tagged: Int
    )  :String =        
    {
    
    
    
      
        
        
        val a_list = List("Comment_Count_Unique","ev","v_users","votes","Commented_Users","Comment_Count","Is_Neo4j","P_Pin","P_Id","P_Author","P_Author_FullName","P_IsMarkedReadLater","P_Title","P_Title_ID","P_Category","P_SubCategory","P_Num_Comments","P_Feature_Image","P_Smry", "P_TimeCreated", "P_EventLocation", "P_EventStartTime", "P_EventAttendStatus", "P_SignsRequired", "P_PetitionSignStatus")
		val l2 = List("FN","UN")
        val l3 = List("Name","UName")
        
        var art = List[org.neo4j.graphdb.Node]()
        var event = List[org.neo4j.graphdb.Node]()
        var petition = List[org.neo4j.graphdb.Node]()
        var debate = List[org.neo4j.graphdb.Node]()
        var townhall = List[org.neo4j.graphdb.Node]()
        
        var all_items = List[org.neo4j.graphdb.Node]()
        
        val SpaceIndex = getNodeIndex("space").get
        val ArticleIndex = getNodeIndex("article").get
        val EventIndex = getNodeIndex("event").get
        val PetitionIndex = getNodeIndex("petition").get
        val TownhallIndex = getNodeIndex("townhall").get
        val DebateIndex = getNodeIndex("debate").get
        val UserIndex = getNodeIndex("user").get
        
        var space_node = SpaceIndex.get("id",space).getSingle()
        var user_node = UserIndex.get("id",user_name).getSingle()
        
        var out = List[Any]()
        if(space_node != null )
        {
          
                var pin_tiles = List[String]()
			    // getting the pinned articles
	            if(space_node.hasProperty("pins"))
	            {
	                val pin_tiles_string = space_node.getProperty("pins").toString()
		            if(!pin_tiles_string.equals(""))
		            {
		              pin_tiles = pin_tiles_string.split(",").toList.slice(0,5)
		            }
	            	
	            }
                
                // slicing the articles based on count 
			    var pin_tiles_nodes_arts = pin_tiles.filter(x => ArticleIndex.get("id",x).getSingle() != null).map(x => ArticleIndex.get("id",x).getSingle())
			    var pin_tiles_nodes_events = pin_tiles.filter(x => EventIndex.get("id",x).getSingle() != null).map(x => EventIndex.get("id",x).getSingle())
			    var pin_tiles_nodes_petitions = pin_tiles.filter(x => PetitionIndex.get("id",x).getSingle() != null).map(x => PetitionIndex.get("id",x).getSingle())
			    var pin_tiles_nodes_townhalls = pin_tiles.filter(x => TownhallIndex.get("id",x).getSingle() != null).map(x => TownhallIndex.get("id",x).getSingle())
			    var pin_tiles_nodes_debates = pin_tiles.filter(x => DebateIndex.get("id",x).getSingle() != null).map(x => DebateIndex.get("id",x).getSingle())
			    
			    var pinned_tiles = (pin_tiles_nodes_arts:::pin_tiles_nodes_events:::pin_tiles_nodes_petitions:::pin_tiles_nodes_townhalls:::pin_tiles_nodes_debates).sortBy(-_.getProperty("time_created").toString().toInt).slice(0,2)
			    if(admin_tagged == 0)
                {
                  art = space_node.getRelationships("Article_Tagged_To_Space").asScala.toList.filterNot(x => x.hasProperty("admin_tagged")).map(_.getOtherNode(space_node))
	              event = space_node.getRelationships("Event_Tagged_To_Space").asScala.toList.filterNot(x => x.hasProperty("admin_tagged")).map(_.getOtherNode(space_node))
	              petition = space_node.getRelationships("Petition_Tagged_To_Space").asScala.toList.filterNot(x => x.hasProperty("admin_tagged")).map(_.getOtherNode(space_node))
	              debate = space_node.getRelationships("Debate_Tagged_To_Space").asScala.toList.filterNot(x => x.hasProperty("admin_tagged")).map(_.getOtherNode(space_node))
	              townhall = space_node.getRelationships("Townhall_Tagged_To_Space").asScala.toList.filterNot(x => x.hasProperty("admin_tagged")).map(_.getOtherNode(space_node))
	              
	              val sorted_items = (art:::event:::petition:::debate:::townhall).sortBy(-_.getProperty("time_created").toString().toInt)
                  all_items = (pinned_tiles:::sorted_items).distinct.slice(prev_cnt,(prev_cnt+count))
      
        
                }
                else if(admin_tagged == 1)
                {
                  art = space_node.getRelationships("Article_Tagged_To_Space").asScala.toList.filter(x => x.hasProperty("admin_tagged")).map(_.getOtherNode(space_node))
	              event = space_node.getRelationships("Event_Tagged_To_Space").asScala.toList.filter(x => x.hasProperty("admin_tagged")).map(_.getOtherNode(space_node))
	              petition = space_node.getRelationships("Petition_Tagged_To_Space").asScala.toList.filter(x => x.hasProperty("admin_tagged")).map(_.getOtherNode(space_node))
	              debate = space_node.getRelationships("Debate_Tagged_To_Space").asScala.toList.filter(x => x.hasProperty("admin_tagged")).map(_.getOtherNode(space_node))
	              townhall = space_node.getRelationships("Townhall_Tagged_To_Space").asScala.toList.filter(x => x.hasProperty("admin_tagged")).map(_.getOtherNode(space_node))
	              
                  val sorted_items = (art:::event:::petition:::debate:::townhall).sortBy(-_.getProperty("time_created").toString().toInt)
                  all_items = (pinned_tiles:::sorted_items).distinct.slice(prev_cnt,(prev_cnt+count))
      
                }
                else
                {
                  art = space_node.getRelationships("Article_Tagged_To_Space").asScala.toList.map(_.getOtherNode(space_node))
	              event = space_node.getRelationships("Event_Tagged_To_Space").asScala.toList.map(_.getOtherNode(space_node))
	              petition = space_node.getRelationships("Petition_Tagged_To_Space").asScala.toList.map(_.getOtherNode(space_node))
	              debate = space_node.getRelationships("Debate_Tagged_To_Space").asScala.toList.map(_.getOtherNode(space_node))
	              townhall = space_node.getRelationships("Townhall_Tagged_To_Space").asScala.toList.map(_.getOtherNode(space_node))
	              
                  val sorted_items = (art:::event:::petition:::debate:::townhall).sortBy(-_.getProperty("time_created").toString().toInt)
                  all_items = (pinned_tiles:::sorted_items).distinct.slice(prev_cnt,(prev_cnt+count))
                  
                }
			    
//	            val UserIndex = getNodeIndex("user").get
//	            var user_node = UserIndex.get("id",user_name).getSingle()
//			    if(user_node == null  || user_name.equals(""))
//			    {
//			    	articles = art.map(x => JSONObject(l.zip(List(JSONArray(x.getRelationships("article_voteup").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("article_voteup").asScala.toList.size-x.getRelationships("article_voteup").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Article").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Article").asScala.size,true,false,x.getProperty("article_id"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),false,x.getProperty("article_title"),x.getProperty("article_title_id"),x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Article").asScala.size,x.getProperty("article_featured_img").toString(),x.getProperty("article_summary").toString(),x.getProperty("time_created").toString())).toMap))
//	
//			    }
//			    
//			    else
//			    {
//			        articles = art.map(x => JSONObject(l.zip(List(JSONArray(x.getRelationships("article_voteup").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("article_voteup").asScala.toList.size-x.getRelationships("article_voteup").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Article").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Article").asScala.size,true,false,x.getProperty("article_id"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("article_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("article_title"),x.getProperty("article_title_id"),x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Article").asScala.size,x.getProperty("article_featured_img").toString(),x.getProperty("article_summary").toString(),x.getProperty("time_created").toString())).toMap))
//	
//			    }
                
                for(x <- all_items)
		        {
                  
		          if(x.getProperty("__CLASS__").toString.equals("Saddahaq.article"))
		          {
		            
		            out :+= JSONObject(a_list.zip(List(x.getRelationships("Comment_To_Article").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,0,JSONArray(x.getRelationships("article_voteup").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("article_voteup").asScala.toList.size-x.getRelationships("article_voteup").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Article").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Article").asScala.size,true,pinned_tiles.contains(x),x.getProperty("article_id"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("article_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("article_title"),x.getProperty("article_title_id"),x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Article").asScala.size,x.getProperty("article_featured_img").toString(),x.getProperty("article_summary").toString(),x.getProperty("time_created").toString(),"","",false,"",false)).toMap)
					          
		          }
		          else if(x.getProperty("__CLASS__").toString.equals("Saddahaq.event"))
		          {
		            
		            out :+= JSONObject(a_list.zip(List(x.getRelationships("Comment_To_Event").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,1,JSONArray(x.getRelationships("Is_Attending").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Is_Attending").asScala.toList.size-x.getRelationships("Is_Attending").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Event").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Event").asScala.size,true,pinned_tiles.contains(x),x.getProperty("event_id"),x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("event_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Event").asScala.size,x.getProperty("event_featured_img").toString(),x.getProperty("event_summary").toString(),x.getProperty("time_created").toString(),x.getProperty("event_location").toString(),x.getProperty("event_date_time").toString(),x.getRelationships("Is_Attending").asScala.map(_.getOtherNode(x)).toList.contains(user_node), "", false)).toMap)
					          
		          }
		          else if(x.getProperty("__CLASS__").toString.equals("Saddahaq.petition"))
		          {
		            
		            
		            out :+= JSONObject(a_list.zip(List(x.getRelationships("Comment_To_Petition").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,2,JSONArray(x.getRelationships("Signed_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Signed_Petition").asScala.toList.size-x.getRelationships("Signed_Petition").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Petition").asScala.size,true,pinned_tiles.contains(x),x.getProperty("p_id"),x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("petition_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("p_title"),x.getProperty("p_title_id"),x.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Petition").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Petition").asScala.size,x.getProperty("p_img_url").toString(),x.getProperty("p_content").toString(),x.getProperty("time_created").toString(),"","",false,x.getProperty("p_target").toString().toInt - x.getProperty("p_count").toString().toInt,x.getRelationships("Signed_Petition").asScala.map(_.getOtherNode(x)).toList.contains(user_node))).toMap)
					          
		          }
		          else if(x.getProperty("__CLASS__").toString.equals("Saddahaq.townhall"))
		          {
		            out :+= JSONObject(a_list.zip(List(x.getRelationships("Commented_On_Townhall").asScala.toList.map(y=>y.getOtherNode(x)).distinct.size,3,JSONArray(x.getRelationships("Asked_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Asked_Question").asScala.toList.size-x.getRelationships("Asked_Question").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Commented_On_Townhall").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x)).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Commented_On_Townhall").asScala.size,true,pinned_tiles.contains(x),x.getProperty("t_id"),x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("townhall_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("t_title"),x.getProperty("t_title_id"),"",x.getRelationships("Belongs_To_Subcategory_Townhall").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Commented_On_Townhall").asScala.size,x.getProperty("t_img_url").toString(),x.getProperty("t_content").toString(),x.getProperty("time_created").toString(),"","",false,"",false)).toMap)
					          
		          }
		          else
		          {
		            out :+= JSONObject(a_list.zip(List(x.getRelationships("Commented_On_Debate").asScala.toList.map(y=>y.getOtherNode(x)).distinct.size,4,JSONArray(x.getRelationships("Asked_Debate_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Asked_Debate_Question").asScala.toList.size-x.getRelationships("Asked_Debate_Question").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Commented_On_Debate").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x)).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Commented_On_Debate").asScala.size,true,pinned_tiles.contains(x),x.getProperty("d_id"),x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("debate_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("d_title"),x.getProperty("d_title_id"),"",x.getRelationships("Belongs_To_Subcategory_Debate").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Commented_On_Debate").asScala.size,x.getProperty("d_img_url").toString(),x.getProperty("d_content").toString(),x.getProperty("time_created").toString(),"","",false,"",false)).toMap)
					          
		          }
		          
		          
		          
		        }
      
        
        }
        
    JSONArray(out).toString()  
    }
  
  // This function gives the Tiles according to given category and user
  def get_tiles(
    user_name: String,  // unique user name
    cat: String,  // category
    count: Int,  // No of articles required 
    prev_cnt:Int,
    art_id: String,   // user2 user_id "" if it's a first call , non empty string for any other call
    tiles_type: String // "H" for home page or "up" for user profile
    )  :String =        
    {
    
    
    withTx {
    implicit neo =>
    
        val TilesIndex = getNodeIndex("tiles").get
        val HeadlinesIndex = getNodeIndex("headlines").get
        val FeaturedTilesIndex = getNodeIndex("featured_tiles").get
        val CatIndex = getNodeIndex("category").get
        val UserIndex = getNodeIndex("user").get
        val ArticleIndex = getNodeIndex("article").get
        val EventIndex = getNodeIndex("event").get
        val TownhallIndex = getNodeIndex("townhall").get
        val DebateIndex = getNodeIndex("debate").get
        val PetitionIndex = getNodeIndex("petition").get
        val location_index = getNodeIndex("location").get
        var out = List[Any]()
        
        val a_list = List("Comment_Count_Unique","ev","v_users","votes","Commented_Users","Comment_Count","Is_Neo4j","P_Pin","P_Id","P_Author","P_Author_FullName","P_IsMarkedReadLater","P_Title","P_Title_ID","P_Category","P_SubCategory","P_Num_Comments","P_Feature_Image","P_Smry", "P_TimeCreated", "P_EventLocation", "P_EventStartTime", "P_EventAttendStatus", "P_SignsRequired", "P_PetitionSignStatus", "Space_Title", "Space_TitleId")
		val l2 = List("FN","UN")
        val l3 = List("Name","UName")
        // checking if the user is registered or not    
        var user_node = UserIndex.get("id",user_name).getSingle()
	        
        
        if(tiles_type.toLowerCase().equals("f"))
        {
            var present_tiles = List[String]()
	        var present_tile_nodes = List[org.neo4j.graphdb.Node]()
	        var cat_node = CatIndex.get("name",cat.toLowerCase()).getSingle()
	        var news_node = FeaturedTilesIndex.get("id",cat.toLowerCase()).getSingle()
	        
	        if(news_node != null)
		    {
	            
	            
	            // getting the pinned tiles
	            var pin_tiles = List[String]()
	            var arts_list = List[String]()
	            
	            def intersperse[A](a : List[A], b : List[A]): List[A] = a match {
				  case first :: rest => first :: intersperse(b, rest)
				  case _             => b
				}
	            
	            
			    
			        if(cat_node.hasProperty("pins"))
		            {
		                val pin_tiles_string = cat_node.getProperty("pins").toString()
			            if(!pin_tiles_string.equals(""))
			            {
			              pin_tiles = pin_tiles_string.split(",").toList.slice(0,1)
			            }
		            	
		            }
			        
				    // getting the pinned articles
		            
				    
				    
				      // getting the trending articles
			        val news = news_node.getProperty("value").toString()
			        //println(news)
			        var art_list = news.split(",").toList
				    //println(art_list)
				    arts_list = (pin_tiles ::: art_list).distinct.slice(prev_cnt,(prev_cnt+count))
			        //println(art_list)
			    
			    
			    for(item <- arts_list)
		        {
		          if(ArticleIndex.get("id",item).getSingle() != null)
		          {
		            val x = ArticleIndex.get("id",item).getSingle()
		            out :+= JSONObject(a_list.zip(List(
		                x.getRelationships("Comment_To_Article").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,
		                0,
		                JSONArray(x.getRelationships("article_voteup").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("article_voteup").asScala.toList.size-x.getRelationships("article_voteup").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Article").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Article").asScala.size,true,pin_tiles.contains(item),x.getProperty("article_id"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("article_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("article_title"),x.getProperty("article_title_id"),x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Article").asScala.size,x.getProperty("article_featured_img").toString(),x.getProperty("article_summary").toString(),x.getProperty("time_created").toString(),"","",false,
		                "",
		                false,
		                x.getRelationships("Article_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Article_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    		    )).toMap)
					          
		          }
		          else if(EventIndex.get("id",item).getSingle() != null)
		          {
		            val x = EventIndex.get("id",item).getSingle()
		            out :+= JSONObject(a_list.zip(List(
		                x.getRelationships("Comment_To_Event").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,
		                1,
		                JSONArray(x.getRelationships("Is_Attending").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Is_Attending").asScala.toList.size-x.getRelationships("Is_Attending").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Event").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Event").asScala.size,true,pin_tiles.contains(item),x.getProperty("event_id"),x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("event_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Event").asScala.size,x.getProperty("event_featured_img").toString(),x.getProperty("event_summary").toString(),x.getProperty("time_created").toString(),x.getProperty("event_location").toString(),x.getProperty("event_date_time").toString(),x.getRelationships("Is_Attending").asScala.map(_.getOtherNode(x)).toList.contains(user_node),
		                "",
		                false,
    				  x.getRelationships("Event_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Event_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				      )).toMap)
					          
		          }
		          else if(PetitionIndex.get("id",item).getSingle() != null)
		          {
		            val x = PetitionIndex.get("id",item).getSingle()
		            out :+= JSONObject(a_list.zip(List(
		                x.getRelationships("Comment_To_Petition").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,
		                2,
		                JSONArray(x.getRelationships("Signed_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Signed_Petition").asScala.toList.size-x.getRelationships("Signed_Petition").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Petition").asScala.size,true,pin_tiles.contains(item),x.getProperty("p_id"),x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("petition_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("p_title"),x.getProperty("p_title_id"),x.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Petition").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Petition").asScala.size,x.getProperty("p_img_url").toString(),x.getProperty("p_content").toString(),x.getProperty("time_created").toString(),
		                "",
		                "",
		                false,
		                x.getProperty("p_target").toString().toInt - x.getProperty("p_count").toString().toInt,
		                x.getRelationships("Signed_Petition").asScala.map(_.getOtherNode(x)).toList.contains(user_node),
    			x.getRelationships("Petition_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Petition_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				                )).toMap)
					          
		          }
		          else if(TownhallIndex.get("id",item).getSingle() != null)
		          {
		            val x = TownhallIndex.get("id",item).getSingle()
		            out :+= JSONObject(a_list.zip(List(
		                x.getRelationships("Commented_On_Townhall").asScala.toList.map(y=>y.getOtherNode(x)).distinct.size,
		                3,
		                JSONArray(x.getRelationships("Asked_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Asked_Question").asScala.toList.size-x.getRelationships("Asked_Question").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Commented_On_Townhall").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x)).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Commented_On_Townhall").asScala.size,true,pin_tiles.contains(item),x.getProperty("t_id"),x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("townhall_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("t_title"),x.getProperty("t_title_id"),"",x.getRelationships("Belongs_To_Subcategory_Townhall").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Commented_On_Townhall").asScala.size,x.getProperty("t_img_url").toString(),x.getProperty("t_content").toString(),x.getProperty("time_created").toString(),
		                "",
		                "",
		                false,
		                "",
		                false,
    		x.getRelationships("Townhall_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    		x.getRelationships("Townhall_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				                    )).toMap)
					          
		          }
		          else if(DebateIndex.get("id",item).getSingle() != null)
		          {
		            val x = DebateIndex.get("id",item).getSingle()
		            out :+= JSONObject(a_list.zip(List(
		                x.getRelationships("Commented_On_Debate").asScala.toList.map(y=>y.getOtherNode(x)).distinct.size,
		                4,
		                JSONArray(x.getRelationships("Asked_Debate_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Asked_Debate_Question").asScala.toList.size-x.getRelationships("Asked_Debate_Question").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Commented_On_Debate").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x)).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Commented_On_Debate").asScala.size,true,pin_tiles.contains(item),x.getProperty("d_id"),x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("debate_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("d_title"),x.getProperty("d_title_id"),"",x.getRelationships("Belongs_To_Subcategory_Debate").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Commented_On_Debate").asScala.size,x.getProperty("d_img_url").toString(),x.getProperty("d_content").toString(),x.getProperty("time_created").toString(),
		                "",
		                "",
		                false,
		                "",
		                false,
    		x.getRelationships("Debate_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Debate_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				  )).toMap ) 			          
		          }
		          
		          
		          
		        }
			    
		    } 
         }
        
        else if(tiles_type.toLowerCase().equals("h"))
        {
            var present_tiles = List[String]()
	        var present_tile_nodes = List[org.neo4j.graphdb.Node]()
	        var news_node = HeadlinesIndex.get("id",cat.toLowerCase()).getSingle()
	        var pin_tiles = List[String]()
	        if(news_node != null)
		    {
	            
	            
	            // getting the pinned tiles
	            //var pin_tiles = List[String]()
	            var arts_list = List[String]()
	            
	            def intersperse[A](a : List[A], b : List[A]): List[A] = a match {
				  case first :: rest => first :: intersperse(b, rest)
				  case _             => b
				}
	            
	            
			    
			    
			        
				    
				    
				    
				      // getting the trending articles
			        val news = news_node.getProperty("value").toString()
			        //println(news)
			        var art_list = news.split(",").toList
				    //println(art_list)
				    arts_list = (art_list).distinct.slice(prev_cnt,(prev_cnt+count))
			        //println(art_list)
			    
			    
			    for(item <- arts_list)
		        {
		          if(ArticleIndex.get("id",item).getSingle() != null)
		          {
		            val x = ArticleIndex.get("id",item).getSingle()
		            out :+= JSONObject(a_list.zip(List(
		                x.getRelationships("Comment_To_Article").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,
		                0,
		                JSONArray(x.getRelationships("article_voteup").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("article_voteup").asScala.toList.size-x.getRelationships("article_voteup").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Article").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Article").asScala.size,true,pin_tiles.contains(item),x.getProperty("article_id"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("article_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("article_title"),x.getProperty("article_title_id"),x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Article").asScala.size,x.getProperty("article_featured_img").toString(),x.getProperty("article_summary").toString(),x.getProperty("time_created").toString(),
		                "",
		                "",
		                false,
		                "",
		                false,
		               x.getRelationships("Article_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Article_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				    	        )).toMap)
					          
		          }
		          else if(EventIndex.get("id",item).getSingle() != null)
		          {
		            val x = EventIndex.get("id",item).getSingle()
		            out :+= JSONObject(a_list.zip(List(
		                x.getRelationships("Comment_To_Event").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,
		                1,
		                JSONArray(x.getRelationships("Is_Attending").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Is_Attending").asScala.toList.size-x.getRelationships("Is_Attending").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Event").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Event").asScala.size,true,pin_tiles.contains(item),x.getProperty("event_id"),x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("event_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Event").asScala.size,x.getProperty("event_featured_img").toString(),x.getProperty("event_summary").toString(),x.getProperty("time_created").toString(),x.getProperty("event_location").toString(),x.getProperty("event_date_time").toString(),x.getRelationships("Is_Attending").asScala.map(_.getOtherNode(x)).toList.contains(user_node),
		                "",
		                false,
		                
    				 x.getRelationships("Event_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Event_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				    	        )).toMap)
					          
		          }
		          else if(PetitionIndex.get("id",item).getSingle() != null)
		          {
		            val x = PetitionIndex.get("id",item).getSingle()
		            out :+= JSONObject(a_list.zip(List(
		                x.getRelationships("Comment_To_Petition").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,
		                2,
		                JSONArray(x.getRelationships("Signed_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Signed_Petition").asScala.toList.size-x.getRelationships("Signed_Petition").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Petition").asScala.size,true,pin_tiles.contains(item),x.getProperty("p_id"),x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("petition_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("p_title"),x.getProperty("p_title_id"),x.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Petition").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Petition").asScala.size,x.getProperty("p_img_url").toString(),x.getProperty("p_content").toString(),x.getProperty("time_created").toString(),"","",false,x.getProperty("p_target").toString().toInt - x.getProperty("p_count").toString().toInt,
		                x.getRelationships("Signed_Petition").asScala.map(_.getOtherNode(x)).toList.contains(user_node),
		                
    				x.getRelationships("Petition_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Petition_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				    	        )).toMap)
					          
		          }
		          else if(TownhallIndex.get("id",item).getSingle() != null)
		          {
		            val x = TownhallIndex.get("id",item).getSingle()
		            out :+= JSONObject(a_list.zip(List(
		                x.getRelationships("Commented_On_Townhall").asScala.toList.map(y=>y.getOtherNode(x)).distinct.size,
		                3,
		                JSONArray(x.getRelationships("Asked_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Asked_Question").asScala.toList.size-x.getRelationships("Asked_Question").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Commented_On_Townhall").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x)).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Commented_On_Townhall").asScala.size,true,pin_tiles.contains(item),x.getProperty("t_id"),x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("townhall_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("t_title"),x.getProperty("t_title_id"),"",x.getRelationships("Belongs_To_Subcategory_Townhall").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Commented_On_Townhall").asScala.size,x.getProperty("t_img_url").toString(),x.getProperty("t_content").toString(),x.getProperty("time_created").toString(),
		                "",
		                "",
		                false,
		                "",
		                false,
		                
    				 x.getRelationships("Townhall_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Townhall_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				    	        )).toMap)
					          
		          }
		          else if(DebateIndex.get("id",item).getSingle() != null)
		          {
		            val x = DebateIndex.get("id",item).getSingle()
		            out :+= JSONObject(a_list.zip(List(
		                x.getRelationships("Commented_On_Debate").asScala.toList.map(y=>y.getOtherNode(x)).distinct.size,
		                4,
		                JSONArray(x.getRelationships("Asked_Debate_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Asked_Debate_Question").asScala.toList.size-x.getRelationships("Asked_Debate_Question").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Commented_On_Debate").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x)).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Commented_On_Debate").asScala.size,true,pin_tiles.contains(item),x.getProperty("d_id"),x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("debate_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("d_title"),x.getProperty("d_title_id"),"",x.getRelationships("Belongs_To_Subcategory_Debate").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Commented_On_Debate").asScala.size,x.getProperty("d_img_url").toString(),x.getProperty("d_content").toString(),x.getProperty("time_created").toString(),
		                "",
		                "",
		                false,
		                "",
		                false,
		                
    				   x.getRelationships("Debate_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Debate_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				    		        )).toMap)
					          
		          }
		          
		          
		          
		        }
			    
		    } 
         }
        
        else if(tiles_type.toLowerCase().equals("up"))
        {
            var art = List[org.neo4j.graphdb.Node]()
	        var event = List[org.neo4j.graphdb.Node]()
	        var petition = List[org.neo4j.graphdb.Node]()
	        var debate = List[org.neo4j.graphdb.Node]()
	        var townhall = List[org.neo4j.graphdb.Node]()
	        var user_node1 = UserIndex.get("id",art_id).getSingle()
	        var all_items = List[org.neo4j.graphdb.Node]()
	        
	        if(user_node1 != null)
		    {
	            
	            
	            
				  art = user_node1.getRelationships("Article_Written_By").asScala.toList.map(_.getOtherNode(user_node1))
	              event = user_node1.getRelationships("Event_Created_By").asScala.toList.map(_.getOtherNode(user_node1))
	              petition = user_node1.getRelationships("Petition_Written_By").asScala.toList.map(_.getOtherNode(user_node1))
	              debate = user_node1.getRelationships("Debate_Written_By").asScala.toList.map(_.getOtherNode(user_node1))
	              townhall = user_node1.getRelationships("Townhall_Written_By").asScala.toList.map(_.getOtherNode(user_node1))
	              
	              //below commented line is written by Yash, which is not displaying items related to closed space even if item is written by loggen in user
               //val sorted_items = (art:::event:::petition:::debate:::townhall).filter( x =>  x.getProperty("space").toString.toInt == 0 ).sortBy(-_.getProperty("time_created").toString().toInt)
                  //wrote on 28th Nov 2014 at 2:00PM (@author:kalyan kumar komati) to display closed space if requested user is same as item author, for this added one more condition while filtering with or operation
               //val sorted_items = (art:::event:::petition:::debate:::townhall).filter( x =>  x.getProperty("space").toString.toInt == 0 || user_node == user_node1 ).sortBy(-_.getProperty("time_created").toString().toInt)
                  //Above line is change to below (filter is removed as discribed by Yash & dev) on 19th Nov 2014 at 8:30 PM IST - @author: kalyan kumar
                  val sorted_items = (art:::event:::petition:::debate:::townhall).sortBy(-_.getProperty("time_created").toString().toInt)
                  all_items = sorted_items.distinct.slice(prev_cnt,(prev_cnt+count))
                  
			      for(x <- all_items)
			      {
	                  
			          if(x.getProperty("__CLASS__").toString.equals("Saddahaq.article"))
			          {
			            
			            out :+= JSONObject(a_list.zip(List(
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
			                x.getProperty("article_title"),x.getProperty("article_title_id"),
			                x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),
			                x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),
			                x.getRelationships("Comment_To_Article").asScala.size,
			                x.getProperty("article_featured_img").toString(),
			                x.getProperty("article_summary").toString(),
			                x.getProperty("time_created").toString(),
			                "",
			                "",
			                false,
			                "",
			                false,
			                x.getRelationships("Article_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Article_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				    
			                )).toMap)
						          
			          }
			          else if(x.getProperty("__CLASS__").toString.equals("Saddahaq.event"))
			          {
			            
			            out :+= JSONObject(a_list.zip(List(
			                x.getRelationships("Comment_To_Event").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,1,JSONArray(x.getRelationships("Is_Attending").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Is_Attending").asScala.toList.size-x.getRelationships("Is_Attending").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Event").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Event").asScala.size,
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
    				    
			                )).toMap)
						          
			          }
			          else if(x.getProperty("__CLASS__").toString.equals("Saddahaq.petition"))
			          {
			            
			            
			            out :+= JSONObject(a_list.zip(List(
			                x.getRelationships("Comment_To_Petition").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,2,JSONArray(x.getRelationships("Signed_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Signed_Petition").asScala.toList.size-x.getRelationships("Signed_Petition").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Petition").asScala.size,
			                true,
			                false,
			                x.getProperty("p_id"),
			                x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),
			                x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),
			                x.getRelationships("petition_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),
			                x.getProperty("p_title"),
			                x.getProperty("p_title_id"),
			                x.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),
			                x.getRelationships("Belongs_To_Subcategory_Petition").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),
			                x.getRelationships("Comment_To_Petition").asScala.size,
			                x.getProperty("p_img_url").toString(),x.getProperty("p_content").toString(),
			                x.getProperty("time_created").toString(),
			                "",
			                "",
			                false,
			                x.getProperty("p_target").toString().toInt - x.getProperty("p_count").toString().toInt,
			                x.getRelationships("Signed_Petition").asScala.map(_.getOtherNode(x)).toList.contains(user_node),
			                x.getRelationships("Petition_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Petition_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				    
			                )).toMap)
						          
			          }
			          else if(x.getProperty("__CLASS__").toString.equals("Saddahaq.townhall"))
			          {
			            out :+= JSONObject(a_list.zip(List(
			                x.getRelationships("Commented_On_Townhall").asScala.toList.map(y=>y.getOtherNode(x)).distinct.size,3,JSONArray(x.getRelationships("Asked_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Asked_Question").asScala.toList.size-x.getRelationships("Asked_Question").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Commented_On_Townhall").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x)).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Commented_On_Townhall").asScala.size,
			                true,
			                false,
			                x.getProperty("t_id"),
			                x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),
			                x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),
			                x.getRelationships("townhall_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),
			                x.getProperty("t_title"),
			                x.getProperty("t_title_id"),
			                "",
			                x.getRelationships("Belongs_To_Subcategory_Townhall").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),
			                x.getRelationships("Commented_On_Townhall").asScala.size,
			                x.getProperty("t_img_url").toString(),
			                x.getProperty("t_content").toString(),
			                x.getProperty("time_created").toString(),
			                "",
			                "",
			                false,
			                "",
			                false,
			            x.getRelationships("Townhall_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Townhall_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				        
			            )).toMap)
						          
			          }
			          else
			          {
			            out :+= JSONObject(a_list.zip(List(
			                x.getRelationships("Commented_On_Debate").asScala.toList.map(y=>y.getOtherNode(x)).distinct.size,4,JSONArray(x.getRelationships("Asked_Debate_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Asked_Debate_Question").asScala.toList.size-x.getRelationships("Asked_Debate_Question").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Commented_On_Debate").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x)).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Commented_On_Debate").asScala.size,
			                true,
			                false,
			                x.getProperty("d_id"),
			                x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),
			                x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),
			                x.getRelationships("debate_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),
			                x.getProperty("d_title"),
			                x.getProperty("d_title_id"),
			                "",
			                x.getRelationships("Belongs_To_Subcategory_Debate").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),
			                x.getRelationships("Commented_On_Debate").asScala.size,
			                x.getProperty("d_img_url").toString(),
			                x.getProperty("d_content").toString(),
			                x.getProperty("time_created").toString(),
			                "",
			                "",
			                false,
			                "",
			                false,
			                x.getRelationships("Debate_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Debate_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				    
			                )).toMap)
						          
			          }
			      }
				    
			    } 
         }
        else if(tiles_type.toLowerCase().equals("l"))
        {
            var pin_tiles = List[String]()
            var latest_tiles = FeaturedTilesIndex.get("id","latest").getSingle().getProperty("value").toString()
            if(!latest_tiles.equals(""))
            {
	            val all_items = latest_tiles.split(",").toList.slice(0,15)
			    
	            for(item <- all_items)
		        {
		          if(ArticleIndex.get("id",item).getSingle() != null)
		          {
		            val x = ArticleIndex.get("id",item).getSingle()
		            out :+= JSONObject(a_list.zip(List(
		                x.getRelationships("Comment_To_Article").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,0,JSONArray(x.getRelationships("article_voteup").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("article_voteup").asScala.toList.size-x.getRelationships("article_voteup").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Article").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Article").asScala.size,
		                true,
		                pin_tiles.contains(item),
		                x.getProperty("article_id"),
		                x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),
		                x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),
		                x.getRelationships("article_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),
		                x.getProperty("article_title"),
		                x.getProperty("article_title_id"),
		                x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),
		                x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),
		                x.getRelationships("Comment_To_Article").asScala.size,
		                x.getProperty("article_featured_img").toString(),
		                x.getProperty("article_summary").toString(),
		                x.getProperty("time_created").toString(),
		                "",
		                "",
		                false,
		                "",
		                false,
		                x.getRelationships("Article_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Article_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				    
		            )).toMap)
					          
		          }
		          else if(EventIndex.get("id",item).getSingle() != null)
		          {
		            val x = EventIndex.get("id",item).getSingle()
		            out :+= JSONObject(a_list.zip(List(
		                x.getRelationships("Comment_To_Event").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,
		                1,
		                JSONArray(x.getRelationships("Is_Attending").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Is_Attending").asScala.toList.size-x.getRelationships("Is_Attending").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Event").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),
		                x.getRelationships("Comment_To_Event").asScala.size,
		                true,
		                pin_tiles.contains(item),
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
    				    
		                )).toMap)
					          
		          }
		          else if(PetitionIndex.get("id",item).getSingle() != null)
		          {
		            val x = PetitionIndex.get("id",item).getSingle()
		            out :+= JSONObject(a_list.zip(List(
		                x.getRelationships("Comment_To_Petition").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,2,JSONArray(x.getRelationships("Signed_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Signed_Petition").asScala.toList.size-x.getRelationships("Signed_Petition").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Petition").asScala.size,true,pin_tiles.contains(item),
		                x.getProperty("p_id"),
		                x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),
		                x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),
		                x.getRelationships("petition_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),
		                x.getProperty("p_title"),
		                x.getProperty("p_title_id"),
		                x.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),
		                x.getRelationships("Belongs_To_Subcategory_Petition").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),
		                x.getRelationships("Comment_To_Petition").asScala.size,
		                x.getProperty("p_img_url").toString(),x.getProperty("p_content").toString(),
		                x.getProperty("time_created").toString(),
		                "",
		                "",
		                false,
		                x.getProperty("p_target").toString().toInt - x.getProperty("p_count").toString().toInt,
		                x.getRelationships("Signed_Petition").asScala.map(_.getOtherNode(x)).toList.contains(user_node),
		                x.getRelationships("Petition_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Petition_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				    
		                )).toMap)
					          
		          }
		          else if(TownhallIndex.get("id",item).getSingle() != null)
		          {
		            val x = TownhallIndex.get("id",item).getSingle()
		            out :+= JSONObject(a_list.zip(List(
		                x.getRelationships("Commented_On_Townhall").asScala.toList.map(y=>y.getOtherNode(x)).distinct.size,
		                3,
		                JSONArray(x.getRelationships("Asked_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  
		                x.getRelationships("Asked_Question").asScala.toList.size-x.getRelationships("Asked_Question").asScala.toList.slice(0,2).size,  
		                JSONArray(x.getRelationships("Commented_On_Townhall").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x)).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Commented_On_Townhall").asScala.size,true,pin_tiles.contains(item),x.getProperty("t_id"),x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),
		                x.getRelationships("townhall_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),
		                x.getProperty("t_title"),
		                x.getProperty("t_title_id"),
		                "",
		                x.getRelationships("Belongs_To_Subcategory_Townhall").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),
		                x.getRelationships("Commented_On_Townhall").asScala.size,
		                x.getProperty("t_img_url").toString(),
		                x.getProperty("t_content").toString(),
		                x.getProperty("time_created").toString(),
		                "",
		                "",
		                false,
		                "",
		                false,
		                x.getRelationships("Townhall_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Townhall_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				    
		                )).toMap)
					          
		          }
		          else if(DebateIndex.get("id",item).getSingle() != null)
		          {
		            val x = DebateIndex.get("id",item).getSingle()
		            out :+= JSONObject(a_list.zip(List(
		                x.getRelationships("Commented_On_Debate").asScala.toList.map(y=>y.getOtherNode(x)).distinct.size,4,JSONArray(x.getRelationships("Asked_Debate_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Asked_Debate_Question").asScala.toList.size-x.getRelationships("Asked_Debate_Question").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Commented_On_Debate").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x)).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),
		                x.getRelationships("Commented_On_Debate").asScala.size,
		                true,
		                pin_tiles.contains(item),
		                x.getProperty("d_id"),
		                x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),
		                x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),
		                x.getRelationships("debate_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),
		                x.getProperty("d_title"),
		                x.getProperty("d_title_id"),
		                "",
		                x.getRelationships("Belongs_To_Subcategory_Debate").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),
		                x.getRelationships("Commented_On_Debate").asScala.size,
		                x.getProperty("d_img_url").toString(),
		                x.getProperty("d_content").toString(),
		                x.getProperty("time_created").toString(),
		                "",
		                "",
		                false,
		                "",
		                false,
		                x.getRelationships("Debate_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Debate_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				    
		                )).toMap)
					          
		          }
		          
		          
		          
		        }
            }
		   
        }
        
        else
	    {
            var present_tiles = List[String]()
	        var present_tile_nodes = List[org.neo4j.graphdb.Node]()
	        var cat_node = CatIndex.get("name",cat.toLowerCase()).getSingle()
	        var news_node = TilesIndex.get("id",cat.toLowerCase()).getSingle()
	        
	        if(news_node != null)
		    {
	            
	            
	            // getting the pinned tiles
	            var pin_tiles = List[String]()
	            var arts_list = List[String]()
	            
	            def intersperse[A](a : List[A], b : List[A]): List[A] = a match {
				  case first :: rest => first :: intersperse(b, rest)
				  case _             => b
				}
	            
	            
			    
			    if(tiles_type.toLowerCase().equals("p"))
			    {
				      //getting the localized tiles
			        var loc_tiles = List[String]()
		            if(user_node.hasRelationship("Belongs_To_Location",Direction.OUTGOING) && cat.equals("all"))
		            {
		              val loc = user_node.getSingleRelationship("Belongs_To_Location",Direction.OUTGOING).getOtherNode(user_node)
		              val tiles_loc = loc.getProperty("tiles").toString()
		              if(!tiles_loc.equals(""))
		              {
		                loc_tiles = tiles_loc.split(",").toList
		              }
		            }
		            
		            val UserTilesIndex = getNodeIndex("user_tiles").get
		            
		            var user_tiles_node = UserTilesIndex.get("id",user_name).getSingle()
		            
		            // getting the personalized tiles
		            var perso_tiles = List[String]()
		            
		            val perso_tiles_string = user_tiles_node.getProperty("news_Personalized").toString()
			        if(!perso_tiles_string.equals(""))
			        {
			              perso_tiles = perso_tiles_string.split(",").toList
			              //println(perso_tiles)
			              //println(category_node.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.map(_.getEndNode()).toList)
			                
			              if(!cat.equals("all"))
			              {
			                val CategoryIndex = getNodeIndex("category").get
			                val category_node = CategoryIndex.get("name",cat).getSingle()
			                
			                if(category_node != null)
			                {
			                  
			                	perso_tiles = perso_tiles.filter( x => ArticleIndex.get("id",x).getSingle().getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.map(_.getEndNode()).toList.contains(category_node))
			                }
			              }
			        }
		            
		            var perso_loc_tiles = intersperse(perso_tiles,loc_tiles)
		            arts_list = perso_loc_tiles.distinct.slice(prev_cnt,(prev_cnt+count))
			    
			    }
		        
			    else
			    {
			      
			        // getting the pinned articles
		            
				    
				    // slicing the articles based on count 
//				    var pin_tiles_nodes_arts = pin_tiles.filter(x => ArticleIndex.get("id",x).getSingle() != null).map(x => ArticleIndex.get("id",x).getSingle())
//				    var pin_tiles_nodes_events = pin_tiles.filter(x => EventIndex.get("id",x).getSingle() != null).map(x => EventIndex.get("id",x).getSingle())
//				    var pin_tiles_nodes_petitions = pin_tiles.filter(x => PetitionIndex.get("id",x).getSingle() != null).map(x => PetitionIndex.get("id",x).getSingle())
//				    var pin_tiles_nodes_townhalls = pin_tiles.filter(x => TownhallIndex.get("id",x).getSingle() != null).map(x => TownhallIndex.get("id",x).getSingle())
//				    var pin_tiles_nodes_debates = pin_tiles.filter(x => DebateIndex.get("id",x).getSingle() != null).map(x => DebateIndex.get("id",x).getSingle())
//				    
//				    var pinned_tiles = (pin_tiles_nodes_arts:::pin_tiles_nodes_events:::pin_tiles_nodes_petitions:::pin_tiles_nodes_townhalls:::pin_tiles_nodes_debates).sortBy(-_.getProperty("time_created").toString().toInt).slice(0,3)
			    
				      // getting the trending articles
			        val news = news_node.getProperty("value").toString()
			        //println(news)
			        var art_list = news.split(",").toList
				    //println(art_list)
				    arts_list = (pin_tiles ::: art_list).distinct.slice(prev_cnt,(prev_cnt+count))
			        //println(art_list)
			    }
			    
			    for(item <- arts_list)
		        {
		          if(ArticleIndex.get("id",item).getSingle() != null)
		          {
		            val x = ArticleIndex.get("id",item).getSingle()
		            out :+= JSONObject(a_list.zip(List(
		                x.getRelationships("Comment_To_Article").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,0,JSONArray(x.getRelationships("article_voteup").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("article_voteup").asScala.toList.size-x.getRelationships("article_voteup").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Article").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))), 
		                x.getRelationships("Comment_To_Article").asScala.size,
		                true,
		                pin_tiles.contains(item),
		                x.getProperty("article_id"),
		                x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),
		                x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),
		                x.getRelationships("article_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),
		                x.getProperty("article_title"),
		                x.getProperty("article_title_id"),
		                x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),
		                x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),
		                x.getRelationships("Comment_To_Article").asScala.size,
		                x.getProperty("article_featured_img").toString(),
		                x.getProperty("article_summary").toString(),
		                x.getProperty("time_created").toString(),
		                "",
		                "",
		                false,
		                "",
		                false,
		                x.getRelationships("Article_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Article_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				    
		                )).toMap)
					          
		          }
		          else if(EventIndex.get("id",item).getSingle() != null)
		          {
		            val x = EventIndex.get("id",item).getSingle()
		            out :+= JSONObject(a_list.zip(List(
		                x.getRelationships("Comment_To_Event").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,
		                1,
		                JSONArray(x.getRelationships("Is_Attending").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))), 
		                x.getRelationships("Is_Attending").asScala.toList.size-x.getRelationships("Is_Attending").asScala.toList.slice(0,2).size,
		                JSONArray(x.getRelationships("Comment_To_Event").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  
		                x.getRelationships("Comment_To_Event").asScala.size,
		                true,
		                pin_tiles.contains(item),
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
    				    
		                )).toMap)
					          
		          }
		          else if(PetitionIndex.get("id",item).getSingle() != null)
		          {
		            val x = PetitionIndex.get("id",item).getSingle()
		            out :+= JSONObject(a_list.zip(List(
		                x.getRelationships("Comment_To_Petition").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,
		                2,
		                JSONArray(x.getRelationships("Signed_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))), 
		                x.getRelationships("Signed_Petition").asScala.toList.size-x.getRelationships("Signed_Petition").asScala.toList.slice(0,2).size,  
		                JSONArray(x.getRelationships("Comment_To_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  
		                x.getRelationships("Comment_To_Petition").asScala.size,
		                true,
		                pin_tiles.contains(item),
		                x.getProperty("p_id"),
		                x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),
		                x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),
		                x.getRelationships("petition_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("p_title"),
		                x.getProperty("p_title_id"),
		                x.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),
		                x.getRelationships("Belongs_To_Subcategory_Petition").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),
		                x.getRelationships("Comment_To_Petition").asScala.size,
		                x.getProperty("p_img_url").toString(),
		                x.getProperty("p_content").toString(),
		                x.getProperty("time_created").toString(),
		                "",
		                "",
		                false,
		                x.getProperty("p_target").toString().toInt - x.getProperty("p_count").toString().toInt,
		                x.getRelationships("Signed_Petition").asScala.map(_.getOtherNode(x)).toList.contains(user_node),
		                x.getRelationships("Petition_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Petition_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				    
		                )).toMap)
					          
		          }
		          else if(TownhallIndex.get("id",item).getSingle() != null)
		          {
		            val x = TownhallIndex.get("id",item).getSingle()
		            out :+= JSONObject(a_list.zip(List(
		                x.getRelationships("Commented_On_Townhall").asScala.toList.map(y=>y.getOtherNode(x)).distinct.size,
		                3,
		                JSONArray(x.getRelationships("Asked_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),
		                x.getRelationships("Asked_Question").asScala.toList.size-x.getRelationships("Asked_Question").asScala.toList.slice(0,2).size,  
		                JSONArray(x.getRelationships("Commented_On_Townhall").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x)).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  
		                x.getRelationships("Commented_On_Townhall").asScala.size,
		                true,
		                pin_tiles.contains(item),
		                x.getProperty("t_id"),
		                x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),
		                x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),
		                x.getRelationships("townhall_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),
		                x.getProperty("t_title"),
		                x.getProperty("t_title_id"),
		                "",
		                x.getRelationships("Belongs_To_Subcategory_Townhall").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),
		                x.getRelationships("Commented_On_Townhall").asScala.size,
		                x.getProperty("t_img_url").toString(),
		                x.getProperty("t_content").toString(),
		                x.getProperty("time_created").toString(),
		                "",
		                "",
		                false,
		                "",
		                false,
		                x.getRelationships("Townhall_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Townhall_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				    
		                )).toMap)
					          
		          }
		          else if(DebateIndex.get("id",item).getSingle() != null)
		          {
		            val x = DebateIndex.get("id",item).getSingle()
		            out :+= JSONObject(a_list.zip(List(
		                x.getRelationships("Commented_On_Debate").asScala.toList.map(y=>y.getOtherNode(x)).distinct.size,
		                4,
		                JSONArray(x.getRelationships("Asked_Debate_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  
		                x.getRelationships("Asked_Debate_Question").asScala.toList.size-x.getRelationships("Asked_Debate_Question").asScala.toList.slice(0,2).size,  
		                JSONArray(x.getRelationships("Commented_On_Debate").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x)).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  
		                x.getRelationships("Commented_On_Debate").asScala.size,
		                true,
		                pin_tiles.contains(item),
		                x.getProperty("d_id"),
		                x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),
		                x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),
		                x.getRelationships("debate_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),
		                x.getProperty("d_title"),
		                x.getProperty("d_title_id"),
		                "",
		                x.getRelationships("Belongs_To_Subcategory_Debate").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),
		                x.getRelationships("Commented_On_Debate").asScala.size,
		                x.getProperty("d_img_url").toString(),
		                x.getProperty("d_content").toString(),
		                x.getProperty("time_created").toString(),
		                "",
		                "",
		                false,
		                "",
		                false,
		                x.getRelationships("Debate_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title")).toList.mkString(","),
    				    x.getRelationships("Debate_Tagged_To_Space").asScala.toList.map(_.getOtherNode(x)).map(_.getProperty("space_title_id")).toList.mkString(",")
    				    
		                )).toMap)
					          
		          }
		          
		          
		          
		        }
			    
		    } 
         }
      
      JSONArray(out).toString()  
    }
      
      
    }
  
  
  
  // This function gives the Tiles according to given category and user
  def get_tiles_temp(
    user_name: String,  // unique user name
    cat: String,  // category
    count: Int,  // No of articles required 
    prev_cnt:Int,
    art_id: String,   // "" if it's a first call , non empty string for any other call
    tiles_type: String // "H" for home page
    )  :String =        
    {
    
    
    withTx {
      implicit neo =>
        
        val UserIndex = getNodeIndex("user").get
        val CategoryIndex = getNodeIndex("category").get
//        val category_node = CategoryIndex.get("name",each).getSingle()
        
        val ArticleIndex = getNodeIndex("article").get
        val articles = ArticleIndex.query("id", "*" ).iterator().asScala.toList
        val EventIndex = getNodeIndex("event").get
        val events = EventIndex.query("id", "*" ).iterator().asScala.toList
        val TownhallIndex = getNodeIndex("townhall").get
        val townhalls = TownhallIndex.query("id", "*" ).iterator().asScala.toList
        val DebateIndex = getNodeIndex("debate").get
        val debates = DebateIndex.query("id", "*" ).iterator().asScala.toList
        
        val PetitionIndex = getNodeIndex("petition").get
        val petitions = PetitionIndex.query("id", "*" ).iterator().asScala.toList
        
        
				         
        val all_items = (articles:::events:::petitions:::debates:::townhalls).sortBy(-_.getProperty("time_created").toString().toInt).slice(prev_cnt,(prev_cnt+count))
      
        var out = List[Any]()
        
        val a_list = List("ev","v_users","votes","Commented_Users","Comment_Count","Is_Neo4j","P_Pin","P_Id","P_Author","P_Author_FullName","P_IsMarkedReadLater","P_Title","P_Title_ID","P_Category","P_SubCategory","P_Num_Comments","P_Feature_Image","P_Smry", "P_TimeCreated")
		val l2 = List("FN","UN")
        val l3 = List("Name","UName")
        
        
        for(x <- all_items)
        {
          if(x.getProperty("__CLASS__").toString.equals("Saddahaq.article"))
          {
            out :+= JSONObject(a_list.zip(List(0,JSONArray(x.getRelationships("article_voteup").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("article_voteup").asScala.toList.size-x.getRelationships("article_voteup").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Article").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Article").asScala.size,true,false,x.getProperty("article_id"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),false,x.getProperty("article_title"),x.getProperty("article_title_id"),x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Article").asScala.size,x.getProperty("article_featured_img").toString(),x.getProperty("article_summary").toString(),x.getProperty("time_created").toString())).toMap)
			          
          }
          else if(x.getProperty("__CLASS__").toString.equals("Saddahaq.event"))
          {
            out :+= JSONObject(a_list.zip(List(1,JSONArray(x.getRelationships("Is_Attending").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Is_Attending").asScala.toList.size-x.getRelationships("Is_Attending").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Event").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Event").asScala.size,true,false,x.getProperty("event_id"),x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),false,x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Event").asScala.size,"",x.getProperty("event_summary").toString(),x.getProperty("time_created").toString())).toMap)
			          
          }
          else if(x.getProperty("__CLASS__").toString.equals("Saddahaq.petition"))
          {
            out :+= JSONObject(a_list.zip(List(2,JSONArray(x.getRelationships("Signed_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Signed_Petition").asScala.toList.size-x.getRelationships("Signed_Petition").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Petition").asScala.size,true,false,x.getProperty("p_id"),x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),false,x.getProperty("p_title"),x.getProperty("p_title_id"),x.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Petition").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Petition").asScala.size,x.getProperty("p_img_url").toString(),x.getProperty("p_content").toString(),x.getProperty("time_created").toString())).toMap)
			          
          }
          else if(x.getProperty("__CLASS__").toString.equals("Saddahaq.townhall"))
          {
            out :+= JSONObject(a_list.zip(List(3,JSONArray(x.getRelationships("Asked_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Asked_Question").asScala.toList.size-x.getRelationships("Asked_Question").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Commented_On_Townhall").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x)).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Commented_On_Townhall").asScala.size,true,false,x.getProperty("t_id"),x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),false,x.getProperty("t_title"),x.getProperty("t_title_id"),"",x.getRelationships("Belongs_To_Subcategory_Townhall").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Commented_On_Townhall").asScala.size,x.getProperty("t_img_url").toString(),x.getProperty("t_content").toString(),x.getProperty("time_created").toString())).toMap)
			          
          }
          else
          {
            out :+= JSONObject(a_list.zip(List(4,JSONArray(x.getRelationships("Asked_Debate_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Asked_Debate_Question").asScala.toList.size-x.getRelationships("Asked_Debate_Question").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Commented_On_Debate").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x)).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Commented_On_Debate").asScala.size,true,false,x.getProperty("d_id"),x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),false,x.getProperty("d_title"),x.getProperty("d_title_id"),"",x.getRelationships("Belongs_To_Subcategory_Debate").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Commented_On_Debate").asScala.size,x.getProperty("d_img_url").toString(),x.getProperty("d_content").toString(),x.getProperty("time_created").toString())).toMap)
			          
          }
          
          
          
        }
      
      JSONArray(out).toString()
    }
      
      
    }
  
  // This function gives the items to be displayed in the Elections page
  def get_elections_home(
    user_name: String,  // unique user name
    filter_type: String,
    filter_value: String,
    count: Int,  // No of articles required 
    prev_cnt:Int
    )  :String =        
    {
    
    
    withTx {
    implicit neo =>
     
    
        val TilesIndex = getNodeIndex("tiles").get
        val CatIndex = getNodeIndex("category").get
        val SubcatIndex = getNodeIndex("sub_category").get
        val UserIndex = getNodeIndex("user").get
        val ArticleIndex = getNodeIndex("article").get
        val PetitionIndex = getNodeIndex("petition").get
        val DebateIndex = getNodeIndex("debate").get
        val TownhallIndex = getNodeIndex("townhall").get
        val location_index = getNodeIndex("location").get
        
        var list = List[Any]()
        var a_list = List[Any]()
        var e_list = List[Any]()
        var p_list = List[Any]()
        var d_list = List[Any]()
        var ad_list = List[Any]()
        var t_list = List[Any]()
        var at_list = List[Any]()
        
        val cur_time = (System.currentTimeMillis() /1000).toInt
        val t = cur_time - 86400
        
        var cat_node = CatIndex.get("name","politics").getSingle()
        // adding all the election related hashtags to a list
        var subcat_nodes = List[org.neo4j.graphdb.Node]()
        subcat_nodes :+= SubcatIndex.get("name","elections2014").getSingle()
        subcat_nodes :+= SubcatIndex.get("name","2014elections").getSingle()
        subcat_nodes :+= SubcatIndex.get("name","loksabha2014").getSingle()
        subcat_nodes :+= SubcatIndex.get("name","2014loksabha").getSingle()
        subcat_nodes :+= SubcatIndex.get("name","elections").getSingle()
        subcat_nodes :+= SubcatIndex.get("name","election").getSingle()
        subcat_nodes = subcat_nodes.filter(x => x != null)
        var user_node = UserIndex.get("id",user_name).getSingle()
        
	    
	    
            var arts = List[org.neo4j.graphdb.Node]()
		    var events = List[org.neo4j.graphdb.Node]()
		    var petitions = List[org.neo4j.graphdb.Node]()
		    var debates = List[org.neo4j.graphdb.Node]()
		    var arch_debates = List[org.neo4j.graphdb.Node]()
		    var townhalls = List[org.neo4j.graphdb.Node]()
		    var arch_townhalls = List[org.neo4j.graphdb.Node]()

		    //extracting artlces and events at country level
		    if(filter_type.equals("") && subcat_nodes.size > 0)
		    {
		      arts = subcat_nodes.map( x => x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.map(_.getStartNode())).flatten.distinct
		      arts = arts.sortBy( x =>   -(((  (x.getProperty("views").toString().toFloat*5) + 1)+(  (x.getRelationships("article_voteup","article_markfav","Comment_To_Article").asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).map(x => x.getProperty("in_weight").toString().toInt).sum  ) *10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    ).slice(prev_cnt,(prev_cnt+count))
			  
		      events = subcat_nodes.map( x => x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.map(_.getStartNode())).flatten.distinct
		      events = events.sortBy( x =>   -(((x.getProperty("views").toString().toFloat)+(x.getProperty("weight").toString().toFloat*10))/ ((((t-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    ).slice(prev_cnt,(prev_cnt+count))
		   
		    }
		    
            //extracting artlces and events at state level
		    else if(filter_type.toLowerCase().equals("s"))
		    {
		      
		      val location_node = location_index.get("id",filter_value.toLowerCase()).getSingle()
		      if(location_node != null)
		      {
		          if(cat_node != null)
		          {
		        	  arts = location_node.getRelationships("Belongs_To_Location_Article").asScala.toList.map(_.getStartNode()).filter( x => x.getRelationships("Belongs_To_Category").asScala.toList.map( _.getEndNode()).contains(cat_node))
		              events = location_node.getRelationships("Belongs_To_Location_Event").asScala.toList.map(_.getStartNode()).filter( x => x.getRelationships("Belongs_To_Event_Category").asScala.toList.map( _.getEndNode()).contains(cat_node))
				  
		          }
			      var elec_arts =  List[org.neo4j.graphdb.Node]()
			      var elec_events =  List[org.neo4j.graphdb.Node]()
			      if(subcat_nodes.size > 0)
			      {
			    	  elec_arts = arts.filter( x => x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.map( _.getEndNode()).intersect(subcat_nodes).size > 0)
			          elec_events = events.filter( x => x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.map( _.getEndNode()).intersect(subcat_nodes).size > 0)
			      }
			      
			      arts = arts.filterNot( x => elec_arts.contains(x))
			      
			      arts = arts.sortBy( x =>   -(((  (x.getProperty("views").toString().toFloat*5) + 1)+(  (x.getRelationships("article_voteup","article_markfav","Comment_To_Article").asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).map(x => x.getProperty("in_weight").toString().toInt).sum  ) *10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
				  elec_arts = elec_arts.sortBy( x =>   -(((  (x.getProperty("views").toString().toFloat*5) + 1)+(  (x.getRelationships("article_voteup","article_markfav","Comment_To_Article").asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).map(x => x.getProperty("in_weight").toString().toInt).sum  ) *10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
				  
				  arts = (elec_arts:::arts).slice(prev_cnt,(prev_cnt+count))
				  
				  
			      
			      
			      events = events.filterNot( x => elec_events.contains(x))
			      
			      events = events.sortBy( x =>   -(((x.getProperty("views").toString().toFloat)+(x.getProperty("weight").toString().toFloat*10))/ ((((t-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
				  elec_events = elec_events.sortBy( x =>   -(((x.getProperty("views").toString().toFloat)+(x.getProperty("weight").toString().toFloat*10))/ ((((t-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
				  
				  events = (elec_events:::events).slice(prev_cnt,(prev_cnt+count))
				  
		      }
		    }
		    
            //extracting artlces and events at constituency/candidate level
		    else
		    {
		          val article_content_index = getNodeIndex("article_content").get
                  val event_content_index = getNodeIndex("event_content").get
		          
		          val w = "*"+filter_value+"*"
		          if(cat_node != null)
		          {
		        	  arts = article_content_index.query("article_content", w ).iterator().asScala.filter( x => x.getRelationships("Belongs_To_Category").asScala.toList.map( _.getEndNode()).contains(cat_node)).toList
		              events = event_content_index.query("event_content", w ).iterator().asScala.filter( x => x.getRelationships("Belongs_To_Event_Category").asScala.toList.map( _.getEndNode()).contains(cat_node)).toList
				  
		          }
		          var elec_arts =  List[org.neo4j.graphdb.Node]()
		          var elec_events =  List[org.neo4j.graphdb.Node]()
			      if(subcat_nodes.size > 0)
			      {
			    	  elec_arts = arts.filter( x => x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.map( _.getEndNode()).intersect(subcat_nodes).size > 0)
			          elec_events = events.filter( x => x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.map( _.getEndNode()).intersect(subcat_nodes).size > 0)
			      
			      }
			      arts = arts.filterNot( x => elec_arts.contains(x))
			      
			      arts = arts.sortBy( x =>   -(((  (x.getProperty("views").toString().toFloat*5) + 1)+(  (x.getRelationships("article_voteup","article_markfav","Comment_To_Article").asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).map(x => x.getProperty("in_weight").toString().toInt).sum  ) *10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
				  elec_arts = elec_arts.sortBy( x =>   -(((  (x.getProperty("views").toString().toFloat*5) + 1)+(  (x.getRelationships("article_voteup","article_markfav","Comment_To_Article").asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).map(x => x.getProperty("in_weight").toString().toInt).sum  ) *10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
				  
				  arts = (elec_arts:::arts).slice(prev_cnt,(prev_cnt+count))
				  
				  
			      events = events.filterNot( x => elec_events.contains(x))
			      
			      events = events.sortBy( x =>   -(((x.getProperty("views").toString().toFloat)+(x.getProperty("weight").toString().toFloat*10))/ ((((t-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
				  elec_events = elec_events.sortBy( x =>   -(((x.getProperty("views").toString().toFloat)+(x.getProperty("weight").toString().toFloat*10))/ ((((t-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
				  
				  events = (elec_events:::events).slice(prev_cnt,(prev_cnt+count))
				  
		      
		    }
		    
		    // extracting petitions, debates, townhalls related to elections
		    
		    petitions = PetitionIndex.query("id", "*" ).iterator().asScala.toList.sortBy(-_.getProperty("time_created").toString().toInt).slice(prev_cnt,(prev_cnt+count))
         
	        var tot_debates = DebateIndex.query("id", "*" ).iterator().asScala.toList
	        debates = tot_debates.filter( x =>   (x.getProperty("d_date").toString().toInt + x.getProperty("d_duration").toString().toInt) > cur_time  ).sortBy( x => x.getProperty("d_date").toString().toInt).slice(prev_cnt,(prev_cnt+count))
	        arch_debates = tot_debates.filterNot( x => debates.contains(x)).sortBy( x => -x.getProperty("d_date").toString().toInt).slice(prev_cnt,(prev_cnt+count))
	        
	        var tot_townhalls = TownhallIndex.query("id", "*" ).iterator().asScala.toList
	        townhalls = tot_townhalls.filter( x =>   (x.getProperty("t_date").toString().toInt + x.getProperty("t_duration").toString().toInt) > cur_time  ).sortBy( x => x.getProperty("t_date").toString().toInt).slice(prev_cnt,(prev_cnt+count))
	        arch_townhalls = tot_townhalls.filterNot( x => townhalls.contains(x)).sortBy( x => -x.getProperty("t_date").toString().toInt).slice(prev_cnt,(prev_cnt+count))
	        
	        
	        
	        val a = List("P_Perso","P_Pin","P_Id","P_Author","P_Author_FullName","P_IsMarkedReadLater","P_Title","P_Title_ID","P_Category","P_SubCategory","P_Num_Comments","P_Feature_Image","P_Smry")
			val e = List("id","desc","ttl","tid","ctgy","htg","lc","date")
		    val p = List("id","desc","id","ttl","tid","goal","signs","is_signed")
		    val d = List("id","desc","ttl","tid","date","for","against")
		    val th = List("id","guest","ttl","tid","celeb","date")
	    
		    if(user_name.equals("") || user_node == null)
		    {
		      a_list = arts.map(x => JSONObject(a.zip(List(false,false,x.getProperty("article_id"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),false,x.getProperty("article_title"),x.getProperty("article_title_id"),x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Article").asScala.size,x.getProperty("article_featured_img").toString(),x.getProperty("article_summary").toString())).toMap))
              p_list = petitions.map(x => JSONObject(p.zip(List(x.getProperty("p_id"),x.getProperty("p_content"),x.getProperty("p_id"),x.getProperty("p_title"),x.getProperty("p_title_id"),x.getProperty("p_target"),x.getProperty("p_count"),false)).toMap))
            
		    }
		    else
		    {
		      a_list = arts.map(x => JSONObject(a.zip(List(false,false,x.getProperty("article_id"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("article_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("article_title"),x.getProperty("article_title_id"),x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Article").asScala.size,x.getProperty("article_featured_img").toString(),x.getProperty("article_summary").toString())).toMap))
              p_list = petitions.map(x => JSONObject(p.zip(List(x.getProperty("p_id"),x.getProperty("p_content"),x.getProperty("p_id"),x.getProperty("p_title"),x.getProperty("p_title_id"),x.getProperty("p_target"),x.getProperty("p_count"),x.getRelationships("Signed_Petition").asScala.map(_.getOtherNode(x)).toList.contains(user_node))).toMap))
            
		    }
		    e_list = events.map(x => JSONObject(e.zip(List(x.getProperty("event_id"),x.getProperty("event_summary"),x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getProperty("event_location"),x.getProperty("event_date_time"))).toMap))
            d_list = debates.map(x => JSONObject(d.zip(List(x.getProperty("d_id"),x.getProperty("d_content"),x.getProperty("d_title"),x.getProperty("d_title_id"),x.getProperty("d_date"),x.getRelationships("For").asScala.toList.map( x => x.getStartNode().getProperty("first_name").toString() + " " + x.getStartNode().getProperty("last_name").toString() ).mkString(","),x.getRelationships("Against").asScala.toList.map( x => x.getStartNode().getProperty("first_name").toString() + " " + x.getStartNode().getProperty("last_name").toString() ).mkString(",")  )).toMap))
            ad_list = arch_debates.map(x => JSONObject(d.zip(List(x.getProperty("d_id"),x.getProperty("d_content"),x.getProperty("d_title"),x.getProperty("d_title_id"),x.getProperty("d_date"),x.getRelationships("For").asScala.toList.map( x => x.getStartNode().getProperty("first_name").toString() + " " + x.getStartNode().getProperty("last_name").toString() ).mkString(","),x.getRelationships("Against").asScala.toList.map( x => x.getStartNode().getProperty("first_name").toString() + " " + x.getStartNode().getProperty("last_name").toString() ).mkString(",")  )).toMap))
            
            t_list = townhalls.map(x => JSONObject(th.zip(List(x.getProperty("t_id"),x.getSingleRelationship("Townhall_Of",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getProperty("t_title"),x.getProperty("t_title_id"),x.getProperty("t_content"),x.getProperty("t_date")   )).toMap))
            at_list = arch_townhalls.map(x => JSONObject(th.zip(List(x.getProperty("t_id"),x.getSingleRelationship("Townhall_Of",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getProperty("t_title"),x.getProperty("t_title_id"),x.getProperty("t_content"),x.getProperty("t_date")   )).toMap))
            
            list :+= JSONArray(a_list)
            list :+= JSONArray(e_list)
            list :+= JSONArray(p_list)
            list :+= JSONArray(d_list)
            list :+= JSONArray(ad_list)
            list :+= JSONArray(t_list)
            list :+= JSONArray(at_list)
		    
		    
        
        
    JSONArray(list).toString()  
    }
      
      
    }
  
  
  // This function is used to get more items in the elections page
  def get_elections_more(
    user_name: String,  // unique user name
    filter_type: String,
    filter_value: String,
    item_type: String,
    count: Int,  // No of articles required 
    prev_cnt:Int
    )  :String =        
    {
    
    
    withTx {
    implicit neo =>
     
    
        val CatIndex = getNodeIndex("category").get
        val UserIndex = getNodeIndex("user").get
        val SubcatIndex = getNodeIndex("sub_category").get
        
        
        val location_index = getNodeIndex("location").get
        
        var list = List[Any]()
        
        
        val cur_time = (System.currentTimeMillis() /1000).toInt
        val t = cur_time - 86400
        
        var cat_node = CatIndex.get("name","politics").getSingle()
        var subcat_nodes = List[org.neo4j.graphdb.Node]()
        subcat_nodes :+= SubcatIndex.get("name","elections2014").getSingle()
        subcat_nodes :+= SubcatIndex.get("name","2014elections").getSingle()
        subcat_nodes :+= SubcatIndex.get("name","loksabha2014").getSingle()
        subcat_nodes :+= SubcatIndex.get("name","2014loksabha").getSingle()
        subcat_nodes :+= SubcatIndex.get("name","elections").getSingle()
        subcat_nodes :+= SubcatIndex.get("name","election").getSingle()
        subcat_nodes = subcat_nodes.filter(x => x != null)
        var user_node = UserIndex.get("id",user_name).getSingle()
        
        
		    var items = List[org.neo4j.graphdb.Node]()
		    
		    
		    if(item_type.equals("A"))
		    {
		      
		      val ArticleIndex = getNodeIndex("article").get
		      if(filter_type.equals(""))
		      {
		        items = subcat_nodes.map( x => x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.map(_.getStartNode())).flatten.distinct
		        items = items.sortBy( x =>   -(((  (x.getProperty("views").toString().toFloat*5) + 1)+(  (x.getRelationships("article_voteup","article_markfav","Comment_To_Article").asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).map(x => x.getProperty("in_weight").toString().toInt).sum  ) *10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    ).slice(prev_cnt,(prev_cnt+count))
			  
		      }
		      else if(filter_type.toLowerCase().equals("s"))
		      {
		      
			      val location_node = location_index.get("id",filter_value.toLowerCase()).getSingle()
			      if(location_node != null)
			      {
				      items = location_node.getRelationships("Belongs_To_Location_Article").asScala.toList.map(_.getStartNode()).filter( x => x.getRelationships("Belongs_To_Category").asScala.toList.map( _.getEndNode()).contains(cat_node))
				      
				      var elec_items = items.filter( x => x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.map( _.getEndNode()).intersect(subcat_nodes).size > 0)
				      items = items.filterNot( x => elec_items.contains(x))
				      
				      items = items.sortBy( x =>   -(((  (x.getProperty("views").toString().toFloat*5) + 1)+(  (x.getRelationships("article_voteup","article_markfav","Comment_To_Article").asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).map(x => x.getProperty("in_weight").toString().toInt).sum  ) *10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
					  elec_items = elec_items.sortBy( x =>   -(((  (x.getProperty("views").toString().toFloat*5) + 1)+(  (x.getRelationships("article_voteup","article_markfav","Comment_To_Article").asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).map(x => x.getProperty("in_weight").toString().toInt).sum  ) *10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
					  
					  items = (elec_items:::items).slice(prev_cnt,(prev_cnt+count))
					  
				  }
		      }
		      else
		      {
		          val article_content_index = getNodeIndex("article_content").get
                  
		          val w = "*"+filter_value+"*"
		          items = article_content_index.query("article_content", w ).iterator().asScala.filter( x => x.getRelationships("Belongs_To_Category").asScala.toList.map( _.getEndNode()).contains(cat_node)).toList
		            
		          var elec_items = items.filter( x => x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.map( _.getEndNode()).intersect(subcat_nodes).size > 0)
			      items = elec_items.filterNot( x => elec_items.contains(x))
			      
			      items = items.sortBy( x =>   -(((  (x.getProperty("views").toString().toFloat*5) + 1)+(  (x.getRelationships("article_voteup","article_markfav","Comment_To_Article").asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).map(x => x.getProperty("in_weight").toString().toInt).sum  ) *10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
				  elec_items = elec_items.sortBy( x =>   -(((  (x.getProperty("views").toString().toFloat*5) + 1)+(  (x.getRelationships("article_voteup","article_markfav","Comment_To_Article").asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).map(x => x.getProperty("in_weight").toString().toInt).sum  ) *10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
				  
				  items = (elec_items:::items).slice(prev_cnt,(prev_cnt+count))
				  
		      }
		      val a = List("P_Perso","P_Pin","P_Id","P_Author","P_Author_FullName","P_IsMarkedReadLater","P_Title","P_Title_ID","P_Category","P_SubCategory","P_Num_Comments","P_Feature_Image","P_Smry")
		
		      if(user_name.equals("") || user_node == null)
		      {
		    	  list = items.map(x => JSONObject(a.zip(List(false,false,x.getProperty("article_id"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),false,x.getProperty("article_title"),x.getProperty("article_title_id"),x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Article").asScala.size,x.getProperty("article_featured_img").toString(),x.getProperty("article_summary").toString())).toMap))
		      }
		      else
		      {
		        list = items.map(x => JSONObject(a.zip(List(false,false,x.getProperty("article_id"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("article_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("article_title"),x.getProperty("article_title_id"),x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Article").asScala.size,x.getProperty("article_featured_img").toString(),x.getProperty("article_summary").toString())).toMap))
		      
		      }
		      
		    }
		    
		    else if(item_type.equals("E"))
		    {
		      
		      
		      val EventIndex = getNodeIndex("event").get
		      if(filter_type.equals(""))
		      {
		        items = subcat_nodes.map( x => x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.map(_.getStartNode())).flatten.distinct
		        items = items.sortBy( x =>   -(((x.getProperty("views").toString().toFloat)+(x.getProperty("weight").toString().toFloat*10))/ ((((t-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    ).slice(prev_cnt,(prev_cnt+count))
			  
		      }
		      else if(filter_type.toLowerCase().equals("s"))
		      {
		      
			      val location_node = location_index.get("id",filter_value.toLowerCase()).getSingle()
			      if(location_node != null)
			      {
				      items = location_node.getRelationships("Belongs_To_Location_Event").asScala.toList.map(_.getStartNode()).filter( x => x.getRelationships("Belongs_To_Event_Category").asScala.toList.map( _.getEndNode()).contains(cat_node))
				      
				      var elec_items = items.filter( x => x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.map( _.getEndNode()).intersect(subcat_nodes).size > 0)
				      items = items.filterNot( x => elec_items.contains(x))
				      
				      items = items.sortBy( x =>   -(((x.getProperty("views").toString().toFloat)+(x.getProperty("weight").toString().toFloat*10))/ ((((t-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
					  elec_items = elec_items.sortBy( x =>   -(((x.getProperty("views").toString().toFloat)+(x.getProperty("weight").toString().toFloat*10))/ ((((t-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
					  
					  items = (elec_items:::items).slice(prev_cnt,(prev_cnt+count))
					  
				  }
		      }
		      else
		      {
		          val event_content_index = getNodeIndex("event_content").get
                  
		          val w = "*"+filter_value+"*"
		          items = event_content_index.query("event_content", w ).iterator().asScala.filter( x => x.getRelationships("Belongs_To_Event_Category").asScala.toList.map( _.getEndNode()).contains(cat_node)).toList
		            
		          var elec_items = items.filter( x => x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.map( _.getEndNode()).intersect(subcat_nodes).size > 0)
			      items = items.filterNot( x => elec_items.contains(x))
			      
			      items = items.sortBy( x =>   -(((x.getProperty("views").toString().toFloat)+(x.getProperty("weight").toString().toFloat*10))/ ((((t-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
				  elec_items = elec_items.sortBy( x =>   -(((x.getProperty("views").toString().toFloat)+(x.getProperty("weight").toString().toFloat*10))/ ((((t-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
				  
				  items = (elec_items:::items).slice(prev_cnt,(prev_cnt+count))
				  
		      }
		      val e = List("id","desc","ttl","tid","ctgy","htg","lc","date")
	    
		      list = items.map(x => JSONObject(e.zip(List(x.getProperty("event_id"),x.getProperty("event_summary"),x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getProperty("event_location"),x.getProperty("event_date_time"))).toMap))
            
		    }
		    
		    else if(item_type.equals("P"))
		    {
		      val PetitionIndex = getNodeIndex("petition").get
		      items = PetitionIndex.query("id", "*" ).iterator().asScala.toList.sortBy(-_.getProperty("time_created").toString().toInt).slice(prev_cnt,(prev_cnt+count))
              val p = List("id","desc","id","ttl","tid","goal","signs","is_signed")
		    
              if(user_name.equals("") || user_node == null)
		      {
		    	  list = items.map(x => JSONObject(p.zip(List(x.getProperty("p_id"),x.getProperty("p_content"),x.getProperty("p_id"),x.getProperty("p_title"),x.getProperty("p_title_id"),x.getProperty("p_target"),x.getProperty("p_count"),false)).toMap))
              }
		      else
		      {
		          list = items.map(x => JSONObject(p.zip(List(x.getProperty("p_id"),x.getProperty("p_content"),x.getProperty("p_id"),x.getProperty("p_title"),x.getProperty("p_title_id"),x.getProperty("p_target"),x.getProperty("p_count"),x.getRelationships("Signed_Petition").asScala.map(_.getOtherNode(x)).toList.contains(user_node))).toMap))
            
		      }
		      
		    }
		    
		    else if(item_type.equals("D") || item_type.equals("DA"))
		    {
		      val DebateIndex = getNodeIndex("debate").get
		      
		      var tot_debates = DebateIndex.query("id", "*" ).iterator().asScala.toList
	          var debates = tot_debates.filter( x =>   (x.getProperty("d_date").toString().toInt + x.getProperty("d_duration").toString().toInt) > cur_time  ).sortBy( x => x.getProperty("d_date").toString().toInt).slice(prev_cnt,(prev_cnt+count))
	          
		      //items = DebateIndex.query("id", "*" ).iterator().asScala.toList.sortBy( x => x.getProperty("d_date").toString().toInt).slice(prev_cnt,(prev_cnt+count))
	          val d = List("id","desc","ttl","tid","date","for","against")
		      if(item_type.equals("D"))
		      {
		        list = debates.map(x => JSONObject(d.zip(List(x.getProperty("d_id"),x.getProperty("d_content"),x.getProperty("d_title"),x.getProperty("d_title_id"),x.getProperty("d_date"),x.getRelationships("For").asScala.toList.map( x => x.getStartNode().getProperty("first_name").toString() + " " + x.getStartNode().getProperty("last_name").toString() ).mkString(","),x.getRelationships("Against").asScala.toList.map( x => x.getStartNode().getProperty("first_name").toString() + " " + x.getStartNode().getProperty("last_name").toString() ).mkString(",")  )).toMap))
            
		      }
		      else
		      {
		        items = tot_debates.filterNot( x => debates.contains(x)).sortBy( x => -x.getProperty("d_date").toString().toInt).slice(prev_cnt,(prev_cnt+count))
	            list = items.map(x => JSONObject(d.zip(List(x.getProperty("d_id"),x.getProperty("d_content"),x.getProperty("d_title"),x.getProperty("d_title_id"),x.getProperty("d_date"),x.getRelationships("For").asScala.toList.map( x => x.getStartNode().getProperty("first_name").toString() + " " + x.getStartNode().getProperty("last_name").toString() ).mkString(","),x.getRelationships("Against").asScala.toList.map( x => x.getStartNode().getProperty("first_name").toString() + " " + x.getStartNode().getProperty("last_name").toString() ).mkString(",")  )).toMap))
            
		      }
	    
		      
		    }
		    
		    else
		    {
		      val TownhallIndex = getNodeIndex("townhall").get
		      
		      var tot_townhalls = TownhallIndex.query("id", "*" ).iterator().asScala.toList
	          var townhalls = tot_townhalls.filter( x =>   (x.getProperty("t_date").toString().toInt + x.getProperty("t_duration").toString().toInt) > cur_time  ).sortBy( x => x.getProperty("t_date").toString().toInt).slice(prev_cnt,(prev_cnt+count))
	          val th = List("id","guest","ttl","tid","celeb","date")
		      
	          if(item_type.equals("T"))
	          {
	            list = townhalls.map(x => JSONObject(th.zip(List(x.getProperty("t_id"),x.getSingleRelationship("Townhall_Of",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getProperty("t_title"),x.getProperty("t_title_id"),x.getProperty("t_content"),x.getProperty("t_date")   )).toMap))
            
	          }
	          else
	          {
	            items = tot_townhalls.filterNot( x => townhalls.contains(x)).sortBy( x => -x.getProperty("t_date").toString().toInt).slice(prev_cnt,(prev_cnt+count))
	            list = items.map(x => JSONObject(th.zip(List(x.getProperty("t_id"),x.getSingleRelationship("Townhall_Of",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getProperty("t_title"),x.getProperty("t_title_id"),x.getProperty("t_content"),x.getProperty("t_date")   )).toMap))
            
	          }
	          
	          
		      //items = TownhallIndex.query("id", "*" ).iterator().asScala.toList.sortBy( x => x.getProperty("t_date").toString().toInt).slice(prev_cnt,(prev_cnt+count))
	          
	    	  //list = items.map(x => JSONObject(th.zip(List(x.getSingleRelationship("Townhall_Of",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getProperty("t_title"),x.getProperty("t_title_id"),x.getProperty("t_content"),x.getProperty("t_date")   )).toMap))
            
		    }

		    
		    
		    
		    
		    
		    
        
        
    JSONArray(list).toString()  
    }
      
      
    }

  def get_tiles_dummy(
    user_name: String,  // unique user name
    cat: String,  // category
    count: Int,  // No of articles required 
    prev_cnt:Int,
    art_id: String   // "" if it's a first call , non empty string for any other call
    )  :String =        
    {
    
    
    withTx {
    implicit neo =>
     
    
        val TilesIndex = getNodeIndex("tiles").get
        val CatIndex = getNodeIndex("category").get
        val UserIndex = getNodeIndex("user").get
        val ArticleIndex = getNodeIndex("article").get
        val location_index = getNodeIndex("location").get
        var list = List[Any]()
        var present_tiles = List[String]()
        var present_tile_nodes = List[org.neo4j.graphdb.Node]()
        var cat_node = CatIndex.get("name",cat).getSingle()
        var news_node = TilesIndex.get("id",cat).getSingle()
        var user_node = UserIndex.get("id",user_name).getSingle()
        val l = List("P_Perso","P_Pin","P_Id","P_Author","P_Author_FullName","P_IsMarkedReadLater","P_Title","P_Title_ID","P_Category","P_SubCategory","P_Num_Comments","P_Feature_Image","P_Smry")
		    
        if((user_name.equals("") || user_node == null)&& news_node != null)
        {

		    val news = news_node.getProperty("value").toString().split(",").toList
		    
		    var pin_tiles = List[String]()
            if(cat_node.hasProperty("pins"))
            {
                val pin_tiles_string = cat_node.getProperty("pins").toString()
	            if(!pin_tiles_string.equals(""))
	            {
	              pin_tiles = pin_tiles_string.split(",").toList.slice(0,5)
	            }
            }
		    var art_list = (pin_tiles:::news).distinct.slice(prev_cnt,(prev_cnt+count)).map(x => ArticleIndex.get("id",x).getSingle())
		    list = art_list.map(x => JSONObject(l.zip(List(false,pin_tiles.contains(x.getProperty("article_id").toString()),x.getProperty("article_id"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("article_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("article_title"),x.getProperty("article_title_id"),x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Article").asScala.size,x.getProperty("article_featured_img").toString(),x.getProperty("article_summary").toString())).toMap))
        
		    
		    
        }
        
        else if(news_node != null && user_node != null)
	    {
           
            var loc_tiles = List[String]()
            if(user_node.hasRelationship("Belongs_To_Location",Direction.OUTGOING) && cat.equals("all"))
            {
              val loc = user_node.getSingleRelationship("Belongs_To_Location",Direction.OUTGOING).getOtherNode(user_node)
              val tiles_loc = loc.getProperty("tiles").toString()
              if(!tiles_loc.equals(""))
              {
                loc_tiles = tiles_loc.split(",").toList
              }
            }
            val UserTilesIndex = getNodeIndex("user_tiles").get
            
            var user_tiles_node = UserTilesIndex.get("id",user_name).getSingle()
            var perso_tiles = List[String]()
            val perso_tiles_string = user_tiles_node.getProperty(cat+ "_Personalized").toString()
            if(!perso_tiles_string.equals(""))
            {
              perso_tiles = perso_tiles_string.split(",").toList
            }
            var pin_tiles = List[String]()
            if(cat_node.hasProperty("pins"))
            {
                val pin_tiles_string = cat_node.getProperty("pins").toString()
	            if(!pin_tiles_string.equals(""))
	            {
	              pin_tiles = pin_tiles_string.split(",").toList.slice(0,3)
	            }
            }
            def intersperse[A](a : List[A], b : List[A]): List[A] = a match {
			  case first :: rest => first :: intersperse(b, rest)
			  case _             => b
			}
	        val news = news_node.getProperty("value").toString()
		    var art_list = news.split(",").toList
		    var perso_loc_tiles = intersperse(perso_tiles,loc_tiles)
		    var perso_loc_art_tiles = intersperse(perso_loc_tiles,art_list.slice(0,20))
		    var arts_list = (pin_tiles ::: perso_loc_art_tiles ::: art_list).distinct
		    val a_list = arts_list.slice(prev_cnt,(prev_cnt+count)).map(x => ArticleIndex.get("id",x).getSingle())
		    list = a_list.map(x => JSONObject(l.zip(List(perso_tiles.contains(x.getProperty("article_id").toString()),pin_tiles.contains(x.getProperty("article_id").toString()),x.getProperty("article_id"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("article_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("article_title"),x.getProperty("article_title_id"),x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Article").asScala.size,x.getProperty("article_featured_img").toString(),x.getProperty("article_summary").toString())).toMap))
        
		    
	    } 
      
      JSONArray(list).toString()  
    }
      
      
    }
  
    def trending_articles():String =        
    {
    
    
    withTx {
    implicit neo =>
      var ret = ""
      val TilesIndex = getNodeIndex("tiles").get
      val ArticleIndex = getNodeIndex("article").get
      var news_node = TilesIndex.get("id","all").getSingle()
      if(news_node != null)
      {
	      val news = news_node.getProperty("value").toString()
	      var news_list = news.split(",").toList.map(x => ArticleIndex.get("id",x).getSingle())
	      ret = JSONArray(news_list.map(x => x.getProperty("article_title"))).toString()
	  
      }
      
      ret
      }
    }
    
    def get_user_tiles(user_name:String):String =        
    {
    
    
    withTx {
    implicit neo =>
      var ret = "No Tiles"
      //val TilesIndex = getNodeIndex("tiles").get
      val ArticleIndex = getNodeIndex("article").get
      val user_tiles_index = getNodeIndex("user_tiles").get
      val user_index = getNodeIndex("user").get
      val tile_node = user_tiles_index.get("id",user_name).getSingle()
      if(tile_node != null)
      {
          val news = tile_node.getProperty("news_Personalized").toString()
          if(!news.equals(""))
          {
	          var news_list = news.split(",").toList.map(x => ArticleIndex.get("id",x).getSingle())
		      ret = JSONArray(news_list.map(x => x.getProperty("article_title"))).toString()
          }
               
	  
      }
      
      ret
      }
    }
  // This function runs in regular interval and updates the tiles based on many factors
    def update_tiles()       
    {
    
    
    withTx {
    implicit neo =>
      
      val TilesIndex = getNodeIndex("tiles").get
      val ArticleIndex = getNodeIndex("article").get
      val CatIndex = getNodeIndex("category").get

      val cur_time = (System.currentTimeMillis() /1000).toInt
      val t = cur_time - (3600*4)
      val cat_list = TilesIndex.query("id", "*" ).iterator().asScala.toList
      if(cat_list.size > 0)
      {
        for(each <- cat_list)
        {
          val news = each.getProperty("value").toString()
	      if(!news.equals(""))
	      {
			  var news_list = news.split(",").toList.map(x => ArticleIndex.get("id",x).getSingle()).filter( x => x != null ).filterNot( x => x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name").toString()).contains("blog") )
			  var sorted_news = news_list.sortBy( x =>   -(((  (x.getProperty("views").toString().toFloat) + 1)+(  (x.getRelationships("article_voteup","article_markfav","Comment_To_Article").asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).map(x => x.getProperty("in_weight").toString().toInt).sum  ) *10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90))    )
			  each.setProperty("value",sorted_news.map(x => x.getProperty("article_id").toString()).mkString(","))
	      }
        }
      }
      
      
	 
     }
    println("Tiles updated")
    }
    
    def update_tiles_td()       
    {
    
    
    withTx {
    implicit neo =>
      
      var TilesIndex = getNodeIndex("tiles").get
//      val FeaturedTilesIndex = getNodeIndex("featured_tiles").get
      
      val TownhallIndex = getNodeIndex("townhall").get
      val DebateIndex = getNodeIndex("debate").get
      val EventIndex = getNodeIndex("event").get
      
      val cur_time = (System.currentTimeMillis()/1000).toInt
      //val t1 = cur_time - (86400)
      val t2 = cur_time + (86400)
      
      val townhall_list = TownhallIndex.query("id", "*" ).iterator().asScala.toList.filter( x => x.getProperty("approved").toString.toInt == 1).filter( x => ( (x.getProperty("t_date").toString().toInt + 3600) > cur_time)  &&  (x.getProperty("t_date").toString().toInt < t2) ).map( x => x.getProperty("t_id").toString()  )
      val debate_list = DebateIndex.query("id", "*" ).iterator().asScala.toList.filter( x => x.getProperty("approved").toString.toInt == 1).filter( x => ( (x.getProperty("d_date").toString().toInt + 3600) > cur_time)  &&  (x.getProperty("d_date").toString().toInt < t2) ).map( x => x.getProperty("d_id").toString()  )
      val event_list = EventIndex.query("id", "*" ).iterator().asScala.toList.filter( x => x.getProperty("approved").toString.toInt == 1).filter( x => ( (x.getProperty("event_date_time").toString().toInt) > cur_time)  &&  (x.getProperty("event_date_time").toString().toInt < t2) ).map( x => x.getProperty("event_id").toString()  )
      
      var cat_list = TilesIndex.query("id", "*" ).iterator().asScala.toList
      if(cat_list.size > 0)
      {
        for(each <- cat_list)
        {
          val tiles_id = each.getProperty("id").toString().toLowerCase()
          if(tiles_id.equals("all") || tiles_id.equals("latest"))
	      {
            var news =  each.getProperty("value").toString()
	        var art_list = news.split(",").toList
			news = (townhall_list ::: event_list ::: debate_list ::: art_list).distinct.mkString(",").stripPrefix(",").stripSuffix(",").trim
			each.setProperty("value",news)
			
	      }
        }
      }
      
      TilesIndex = getNodeIndex("featured_tiles").get
      cat_list = TilesIndex.query("id", "*" ).iterator().asScala.toList
      if(cat_list.size > 0)
      {
        for(each <- cat_list)
        {
          val tiles_id = each.getProperty("id").toString().toLowerCase()
          if(tiles_id.equals("all") || tiles_id.equals("latest"))
	      {
            var news =  each.getProperty("value").toString()
	        var art_list = news.split(",").toList
			news = (townhall_list ::: event_list ::: debate_list ::: art_list).distinct.mkString(",").stripPrefix(",").stripSuffix(",").trim
			each.setProperty("value",news)
			
	      }
        }
      }
      
      
     }
    }
  
    // This function runs in regular interval and updates the tiles based on many factors
    def update_tiles_temp()       
    {
    
    
    withTx {
    implicit neo =>
      
      val TilesIndex = getNodeIndex("tiles").get
      val FeaturedTilesIndex = getNodeIndex("featured_tiles").get
      val ArticleIndex = getNodeIndex("article").get
      val EventIndex = getNodeIndex("event").get
      val PetitionIndex = getNodeIndex("petition").get
      val TownhallIndex = getNodeIndex("townhall").get
      val DebateIndex = getNodeIndex("debate").get
      val CatIndex = getNodeIndex("category").get

      val cur_time = (System.currentTimeMillis() /1000).toInt
      val t = cur_time - (3600*4)
      val cat_list = TilesIndex.query("id", "*" ).iterator().asScala.toList.filterNot( x => x.getProperty("id").toString().equals("latest") )
      
      def mergeMap[A, B](ms: List[Map[A, B]])(f: (B, B) => B): Map[A, B] =
		  (Map[A, B]() /: (for (m <- ms; kv <- m) yield kv)) { (a, kv) =>
		    a + (if (a.contains(kv._1)) kv._1 -> f(a(kv._1), kv._2) else kv)
		  }
      
      if(cat_list.size > 0)
      {
        for(each <- cat_list)
        {
          
          
          
          
          val tiles_id = each.getProperty("id").toString().toLowerCase()
          
	      if(tiles_id.equals("all"))
	      {
	         
	          val cat_node =  CatIndex.get("name",tiles_id).getSingle()  // get unique user node based on user name
	     
	          val arts = cat_node.getRelationships("Belongs_To_Category").asScala.toList.map( x => x.getOtherNode(cat_node)).filter( x => x != null && x.getProperty("space").toString.toInt == 0)
	          val arts_list1 = arts.map( x => (x.getProperty("article_id").toString() ) )
	          val arts_list2 = arts.map( x => (((  (x.getProperty("views").toString().toFloat) + 1)+(  (x.getRelationships("article_voteup","article_markfav","Comment_To_Article").asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).map(x => x.getProperty("in_weight").toString().toInt).sum  ) *20))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90)))
			  val arts_map = arts_list1.zip(arts_list2).toMap
	          
	          val events = cat_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map( x => x.getOtherNode(cat_node)).filter( x => x != null && x.getProperty("space").toString.toInt == 0)
	          val events_list1 = events.map( x => (x.getProperty("event_id").toString() ) )
	          val events_list2 = events.map( x => (((  (x.getProperty("views").toString().toFloat) + 1)+(  (x.getRelationships("Is_Attending","Comment_To_Event").asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).map(x => x.getProperty("in_weight").toString().toInt).sum  ) *30))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90)))
			  val events_map = events_list1.zip(events_list2).toMap
	          
			  val petitions = cat_node.getRelationships("Belongs_To_Petition_Category").asScala.toList.map( x => x.getOtherNode(cat_node)).filter( x => x != null && x.getProperty("space").toString.toInt == 0)
	          val petitions_list1 = petitions.map( x => (x.getProperty("p_id").toString() ) )
	          val petitions_list2 = petitions.map( x => (((  (x.getProperty("views").toString().toFloat) + 1)+(  (x.getRelationships("Signed_Petition","Comment_To_Petition").asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).map(x => x.getProperty("in_weight").toString().toInt).sum  ) *10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90)))
			  val petitions_map = petitions_list1.zip(petitions_list2).toMap
	          
			  val townhalls = TownhallIndex.query("id", "*" ).iterator().asScala.toList.filter( x => x.getProperty("space").toString.toInt == 0)
	          val townhalls_list1 = townhalls.map( x => (x.getProperty("t_id").toString() ) )
	          val townhalls_list2 = townhalls.map( x => (((  (x.getRelationships("Participated_In_Townhall").asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).size).toFloat + 1)+(  (x.getRelationships("Asked_Question","Voted_Townhall_Question","Voted_Townhall_Answer","Commented_On_Townhall").asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).size  ) *50))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90)))
			  val townhalls_map = townhalls_list1.zip(townhalls_list2).toMap
	          
			  val debates = DebateIndex.query("id", "*" ).iterator().asScala.toList.filter( x => x.getProperty("space").toString.toInt == 0)
	          val debates_list1 = debates.map( x => (x.getProperty("d_id").toString() ) )
	          val debates_list2 = debates.map( x => (((  (x.getRelationships("Participated_In_Debate").asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).size).toFloat + 1)+(  (x.getRelationships("Asked_Debate_Question","Started_Debate_Argument","Commented_On_Debate").asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).size  ) *50))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90)))
			  val debates_map = debates_list1.zip(debates_list2).toMap
	            
			    val final_map = List(arts_map, events_map, petitions_map, townhalls_map, debates_map)
				val merge_map = mergeMap(final_map)((v1, v2) => v1 + v2)
				
				val trending_list = merge_map.keys.toList.sortBy(x => -merge_map(x))
				
	            each.setProperty("value",trending_list.mkString(","))
	            
	            var featured_tiles_node = FeaturedTilesIndex.get("id",tiles_id).getSingle()
	            if(featured_tiles_node == null)
	            {
	              
	              featured_tiles_node =  createNode(featured_tiles(tiles_id,""))
				  FeaturedTilesIndex += (featured_tiles_node,"id",tiles_id)
				  
	              val arts = cat_node.getRelationships("Belongs_To_Category").asScala.toList.map( x => x.getOtherNode(cat_node)).filter( x => x != null ).filter( x => x.getProperty("approved").toString().toInt == 1 )
		          val arts_list1 = arts.map( x => (x.getProperty("article_id").toString() ) )
		          val arts_list2 = arts.map( x => (x.getProperty("time_created").toString().toInt ) )
				  val arts_map = arts_list1.zip(arts_list2).toMap
		          
		          val events = cat_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map( x => x.getOtherNode(cat_node)).filter( x => x != null ).filter( x => x.getProperty("approved").toString().toInt == 1 )
		          val events_list1 = events.map( x => (x.getProperty("event_id").toString() ) )
		          val events_list2 = events.map( x => (x.getProperty("time_created").toString().toInt ) )
				  val events_map = events_list1.zip(events_list2).toMap
		          
				  val petitions = cat_node.getRelationships("Belongs_To_Petition_Category").asScala.toList.map( x => x.getOtherNode(cat_node)).filter( x => x != null ).filter( x => x.getProperty("approved").toString().toInt == 1 )
		          val petitions_list1 = petitions.map( x => (x.getProperty("p_id").toString() ) )
		          val petitions_list2 = petitions.map( x => (x.getProperty("time_created").toString().toInt ) )
				  val petitions_map = petitions_list1.zip(petitions_list2).toMap
		          
				  val townhalls = TownhallIndex.query("id", "*" ).iterator().asScala.toList.filter( x => x.getProperty("approved").toString().toInt == 1 )
		          val townhalls_list1 = townhalls.map( x => (x.getProperty("t_id").toString() ) )
		          val townhalls_list2 = townhalls.map( x => (x.getProperty("time_created").toString().toInt ) )
				  val townhalls_map = townhalls_list1.zip(townhalls_list2).toMap
		          
				  val debates = DebateIndex.query("id", "*" ).iterator().asScala.toList.filter( x => x.getProperty("approved").toString().toInt == 1 )
		          val debates_list1 = debates.map( x => (x.getProperty("d_id").toString() ) )
		          val debates_list2 = debates.map( x => (x.getProperty("time_created").toString().toInt ) )
				  val debates_map = debates_list1.zip(debates_list2).toMap
		            
				    val final_map = List(arts_map, events_map, petitions_map, townhalls_map, debates_map)
					val merge_map = mergeMap(final_map)((v1, v2) => v1 + v2)
					
					val trending_list = merge_map.keys.toList.sortBy(x => -merge_map(x))
					
		            featured_tiles_node.setProperty("value",trending_list.mkString(","))
		            
		            featured_tiles_node =  FeaturedTilesIndex.get("id","latest").getSingle()
		            if(featured_tiles_node == null)
		            {
		              featured_tiles_node =  createNode(featured_tiles("latest",""))
				      FeaturedTilesIndex += (featured_tiles_node,"id","latest")
				    
				      
		            }
	                featured_tiles_node.setProperty("value",trending_list.mkString(","))
		            
		            
		            
	            }
	      }
          
	      else
	      {
	        
	          val cat_node =  CatIndex.get("name",tiles_id).getSingle()  // get unique user node based on user name
	          
	          val arts = cat_node.getRelationships("Belongs_To_Category").asScala.toList.map( x => x.getOtherNode(cat_node)).filter( x => x != null && x.getProperty("space").toString.toInt == 0)
	          val arts_list1 = arts.map( x => (x.getProperty("article_id").toString()) )
	          val arts_list2 = arts.map( x => (((  (x.getProperty("views").toString().toFloat) + 1)+(  (x.getRelationships("article_voteup","article_markfav","Comment_To_Article").asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).map(x => x.getProperty("in_weight").toString().toInt).sum  ) *10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90)))
			  val arts_map = arts_list1.zip(arts_list2).toMap
	          
	          val events = cat_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map( x => x.getOtherNode(cat_node)).filter( x => x != null && x.getProperty("space").toString.toInt == 0)
	          val events_list1 = events.map( x => (x.getProperty("event_id").toString()) )
	          val events_list2 = events.map( x => (((  (x.getProperty("views").toString().toFloat) + 1)+(  (x.getRelationships("Is_Attending","Comment_To_Event").asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).map(x => x.getProperty("in_weight").toString().toInt).sum  ) *10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90)))
			  val events_map = events_list1.zip(events_list2).toMap
	          
			  val petitions = cat_node.getRelationships("Belongs_To_Petition_Category").asScala.toList.map( x => x.getOtherNode(cat_node)).filter( x => x != null && x.getProperty("space").toString.toInt == 0)
	          val petitions_list1 = petitions.map( x => (x.getProperty("p_id").toString() ) )
	          val petitions_list2 = petitions.map( x => (((  (x.getProperty("views").toString().toFloat) + 1)+(  (x.getRelationships("Signed_Petition","Comment_To_Petition").asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).map(x => x.getProperty("in_weight").toString().toInt).sum  ) *10))/ ((((cur_time-x.getProperty("time_created").toString().toInt)/3600)+1 )*90)))
			  val petitions_map = petitions_list1.zip(petitions_list2).toMap
	          
			    val final_map = List(arts_map, events_map, petitions_map)
				val merge_map = mergeMap(final_map)((v1, v2) => v1 + v2)
				
				val trending_list = merge_map.keys.toList.sortBy(x => -merge_map(x))
				
	            each.setProperty("value",trending_list.mkString(","))
	            var featured_tiles_node = FeaturedTilesIndex.get("id",tiles_id).getSingle()
	            if(featured_tiles_node == null)
	            {
	              
	              featured_tiles_node =  createNode(featured_tiles(tiles_id,""))
				  FeaturedTilesIndex += (featured_tiles_node,"id",tiles_id)
				  
	              val arts = cat_node.getRelationships("Belongs_To_Category").asScala.toList.map( x => x.getOtherNode(cat_node)).filter( x => x != null ).filter( x => x.getProperty("approved").toString().toInt == 1 )
		          val arts_list1 = arts.map( x => (x.getProperty("article_id").toString() ) )
		          val arts_list2 = arts.map( x => (x.getProperty("time_created").toString().toInt ) )
				  val arts_map = arts_list1.zip(arts_list2).toMap
		          
		          val events = cat_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map( x => x.getOtherNode(cat_node)).filter( x => x != null ).filter( x => x.getProperty("approved").toString().toInt == 1 )
		          val events_list1 = events.map( x => (x.getProperty("event_id").toString() ) )
		          val events_list2 = events.map( x => (x.getProperty("time_created").toString().toInt ) )
				  val events_map = events_list1.zip(events_list2).toMap
		          
				  val petitions = cat_node.getRelationships("Belongs_To_Petition_Category").asScala.toList.map( x => x.getOtherNode(cat_node)).filter( x => x != null ).filter( x => x.getProperty("approved").toString().toInt == 1 )
		          val petitions_list1 = petitions.map( x => (x.getProperty("p_id").toString() ) )
		          val petitions_list2 = petitions.map( x => (x.getProperty("time_created").toString().toInt ) )
				  val petitions_map = petitions_list1.zip(petitions_list2).toMap
		          
				  
		            
				    val final_map = List(arts_map, events_map, petitions_map)
					val merge_map = mergeMap(final_map)((v1, v2) => v1 + v2)
					
					val trending_list = merge_map.keys.toList.sortBy(x => -merge_map(x))
					
		            featured_tiles_node.setProperty("value",trending_list.mkString(","))
	            }
	      
	      }
        }
      }
      
      
	 
     }
    println("Tiles updated")
    }
    

  
  
  // Triggered when an article is deleted
  def delete_article(
      id: String // Unique article id
      ):Boolean=
  {
    
   
    withTx {
    implicit neo =>
       var ret = false
       val article_weight_index = getNodeIndex("article_weight").get
       val user_weight_index = getNodeIndex("user_weight").get
       val hash_weight_index = getNodeIndex("hash_weight").get
       val ArticleIndex = getNodeIndex("article").get
       val TilesIndex = getNodeIndex("tiles").get
       val FeaturedTilesIndex = getNodeIndex("featured_tiles").get
       val HeadlinesIndex = getNodeIndex("headlines").get
       val PollIndex = getNodeIndex("poll").get
       val location_index = getNodeIndex("location").get
       val ArticleContentIndex = getNodeIndex("article_content").get
       val ArticleTitleIndex = getNodeIndex("article_title").get
       val SubcatIndex = getNodeIndex("sub_category").get
       val CommentIndex = getNodeIndex("comment").get
       val articl_node = ArticleIndex.get("id",id).getSingle()
       //var cat_list = List[String]()
       if(articl_node != null)
	   {
           // unpinning the article
           pin_item("A",id,"","")
           val space_rels = articl_node.getRelationships("Article_Tagged_To_Space").asScala.toList
           for(space_rel <- space_rels)
           {
             val space_name = space_rel.getOtherNode(articl_node).getProperty("space_id").toString()
             pin_item("A",id,"",space_name)
           }
           
           exclusive_article(id,"")
	       val rels = articl_node.getRelationships().asScala
	       val title_id = articl_node.getProperty("article_title_id")
	       val a_wt = articl_node.getProperty("weight").toString().toInt
	       val author_node = articl_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(articl_node)
		   var rel = articl_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING)
		  
		   // taking back the weight gained by the user(author) because of the article
		   var del_wt = rel.getProperty("out_weight").toString().toInt
	       val au_wt = author_node.getProperty("weight").toString().toInt
	       author_node.setProperty("weight",au_wt-(a_wt+10-del_wt))
		   user_weight_index.remove(author_node)
		   user_weight_index += (author_node,"weight",new ValueContext( au_wt-(a_wt+10-del_wt) ).indexNumeric())
		   
		   // iterating through the relations of the article
	       for(each <- rels)
	       {
	         
	         if(each.getOtherNode(articl_node).getProperty("__CLASS__").equals("Saddahaq.comment"))
	         {
	             // dleteing the comments of the article
	             val p_node = each.getOtherNode(articl_node)
	             val p_rels = p_node.getRelationships().asScala
	             for(single <- p_rels)
	             {
	               single.delete()
	             }
	             CommentIndex.remove(p_node)
	             p_node.delete()
	           
	         }
	         
	         else if(each.getOtherNode(articl_node).getProperty("__CLASS__").equals("Saddahaq.location"))
             {
                 // deleting the relations with locations
	             val l_node = each.getOtherNode(articl_node)
	             val loc_tiles =  l_node.getProperty("tiles").toString()
	             if(!loc_tiles.equals(""))
	             {
	               var loc_tiles_list = loc_tiles.split(",").toList
	               if(loc_tiles_list.contains(id))
	               {
	                 loc_tiles_list = loc_tiles_list.filterNot(x => x.equals(id))
	                 if(loc_tiles_list == Nil)
	                 {
	                   l_node.setProperty("tiles","")
	                 }
	                 else
	                 {
	                   l_node.setProperty("tiles",loc_tiles_list.mkString(","))
	                 }
	               }
	             }
                 each.delete()
             }
	         
	         else if(each.getOtherNode(articl_node).getProperty("__CLASS__").equals("Saddahaq.poll"))
             {
                   // deleting the related polls
	               val poll_node = each.getOtherNode(articl_node)
	               poll_node.getSingleRelationship("Poll_App_Of",Direction.OUTGOING).delete()
		           val vote_rels = poll_node.getRelationships("Voted_To_Poll").asScala
		           for(each <- vote_rels)
		           {
		             each.delete()
		           }
			       poll_node.delete()
          
             }
	         
	         else if(each.getOtherNode(articl_node).getProperty("__CLASS__").equals("Saddahaq.sub_category"))
	         {
	             // deleting the relation with hashtags
	             val rel_type = each.getType().toString()
	             if(rel_type.equals("Tag_Of_Article"))
	             {
	               
	               val tag_node = each.getOtherNode(articl_node)
	               val h_wt = tag_node.getProperty("weight").toString().toInt
	               tag_node.setProperty("weight",(h_wt - 10))
	               hash_weight_index.remove(tag_node)
	               hash_weight_index += (tag_node,"weight",new ValueContext( h_wt - 10 ).indexNumeric())
	             }
	             
	             else
	             {
	               val hash_node = each.getOtherNode(articl_node)
		           val hash_wt = hash_node.getProperty("weight").toString().toInt
		           hash_node.setProperty("weight",(hash_wt - a_wt))
		           hash_weight_index.remove(hash_node)
		           hash_weight_index += (hash_node,"weight",new ValueContext( hash_wt - a_wt ).indexNumeric())
		        
	             }
	             each.delete()
	         }
	         
	         
	         else
	         {
	             val rel_type = each.getType().toString()
	             if(rel_type.equals("Belongs_To_Category"))
	             {
	               // deleting the relation with category
	               val c_name = each.getOtherNode(articl_node).getProperty("name").toString()
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
	       
	      // removing article from index
	       
	       ArticleIndex.remove(articl_node)
	       ArticleContentIndex.remove(articl_node)
	       ArticleTitleIndex.remove(articl_node)
	       article_weight_index.remove(articl_node)
	       articl_node.delete()
	       
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
			         
	               
	       // removing article from the personalized tiles of the users
	       val UserTilesIndex = getNodeIndex("user_tiles").get
	       val users_list = UserTilesIndex.query("id", "*" ).iterator().asScala.toList
	       for(each <- users_list)
	       {
	             val tiles = each.getProperty("news_Personalized").toString()
	             var tiles_list = tiles.split(",").toList
	             if(tiles_list.contains(id))
	             {
		             tiles_list = tiles_list.filterNot( x => x.equals(id))
		             if(tiles_list == Nil)
		             {
		               each.setProperty("news_Personalized","")
		             }
		             else
		             {
		               each.setProperty("news_Personalized",tiles_list.mkString(","))
		             }
	             }
	             
	           
	         
	       }
           
	       
	       
	       ret = true
       }
       
    ret
    }
       
  }
  
  // Deleting the poll
  def delete_poll(
      id: String // Unique poll id
      ):Boolean=
  {
   
    withTx {
    implicit neo =>
       var ret = false
       val PollIndex = getNodeIndex("poll").get
       val poll_node = PollIndex.get("id",id).getSingle()
       if(poll_node != null)
       {
           if(poll_node.hasRelationship("Poll_App_Of",Direction.OUTGOING))
           {
             poll_node.getSingleRelationship("Poll_App_Of",Direction.OUTGOING).delete()
	         
           }
           
           else
           {
             poll_node.getSingleRelationship("Poll_App_Of_Event",Direction.OUTGOING).delete()
	         
           }
           
           // deleting the voted to relations with user
           val vote_rels = poll_node.getRelationships("Voted_To_Poll").asScala
           for(each <- vote_rels)
           {
             each.delete()
           }
           
           // deleting the poll
	       poll_node.delete()
	       ret = true
       }
       
    ret   
    }
  }
  
  def space_follow(
      user_name: String, // Unique article id
      id: String
      ):Boolean=
  {
    
   
    withTx {
    implicit neo =>
       var ret = false
       
       val SpaceIndex = getNodeIndex("space").get
       
       val space_node = SpaceIndex.get("id",id).getSingle()
       
       val UserIndex = getNodeIndex("user").get
       val cur_time = (System.currentTimeMillis() /1000).toInt
       val user_node = UserIndex.get("id",user_name).getSingle()
       //var cat_list = List[String]()
       if(space_node != null && user_node != null)
	   {
         
           if(!space_node.getRelationships("Space_Followed_By",Direction.OUTGOING).asScala.toList.map(_.getEndNode()).contains(user_node))
           {
	           var rel:org.neo4j.graphdb.Relationship = space_node --> "Space_Followed_By" --> user_node  <  //relating article node and user node
			   var rel_time = rel.setProperty("time", cur_time)
           }
           else
           {
             val foll_users = space_node.getRelationships("Space_Followed_By",Direction.OUTGOING).asScala
		     for(each <- foll_users)
		     {
		      
		       if(each.getOtherNode(space_node) == user_node)
		       {
		         
		         each.delete()
		       }
		     }
           }
		   ret = true
       }
       
    ret
    }
       
  }
  
  def space_tagitem(
      space_id: String, // Unique article id
      item_type: String,
      item_id: String,
      tag_type: String
      ):Boolean=
  {
    
   
    withTx {
    implicit neo =>
       var ret = false
       
       val SpaceIndex = getNodeIndex("space").get
       
       val space_node = SpaceIndex.get("id",space_id).getSingle()
       val cur_time = (System.currentTimeMillis() /1000).toInt
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
	   
	     
	     val rel_name = i_type + "_" + "Tagged_To_Space"
	     val grp_name = i_type.toLowerCase()
	     val Index = getNodeIndex(grp_name).get  
	     
	     
	     val item_node = Index.get("id",item_id).getSingle()  // Retrieving Item node based on item_id
     
       //var cat_list = List[String]()
       if(space_node != null && item_node != null)
	   {
         
           if(!space_node.getRelationships(rel_name).asScala.toList.map(_.getOtherNode(space_node)).contains(item_node))
           {
	           var rel:org.neo4j.graphdb.Relationship = item_node --> rel_name --> space_node  <  //relating article node and user node
			   var rel_time = rel.setProperty("time", cur_time)
			   if(tag_type.equals("A"))
			   {
			     var rel_type = rel.setProperty("admin_tagged", 1)
			   }
			   
			   
           }
           else
           {
             val tagged_nodes = space_node.getRelationships(rel_name).asScala
		     for(each <- tagged_nodes)
		     {
		      
		       if(each.getOtherNode(space_node) == item_node)
		       {
		         
		         each.delete()
		         
		       }
		     }
           }
		   ret = true
       }
       
    ret
    }
       
  }
  
  
  // Triggered when an article is deleted
  def delete_space(
      id: String // Unique article id
      ):Boolean=
  {
    
   
    withTx {
    implicit neo =>
       var ret = false
       
       val SpaceIndex = getNodeIndex("space").get
       
       val space_node = SpaceIndex.get("id",id).getSingle()
       //var cat_list = List[String]()
       if(space_node != null)
	   {
           // unpinning the article
           
	       val rels = space_node.getRelationships().asScala
	       
		   // iterating through the relations of the article
	       for(each <- rels)
	       {
	         
	         each.delete()
	       }
	       
	      // removing article from index
	       
	       SpaceIndex.remove(space_node)
	       space_node.delete()
	       ret = true
       }
       
    ret
    }
       
  }
  
  
  
  
  def hash_suggestions(
      user_name: String,
      tag_name: String 
      ):String=
  {
   
    withTx {
    implicit neo =>
       var ret = ""
       val Index = getNodeIndex("sub_category").get
       val node = Index.get("name",tag_name.toLowerCase()).getSingle()
       val UserIndex = getNodeIndex("user").get
       val user_node = UserIndex.get("id",user_name).getSingle()  //Retrieving user node based on userid
      
       var tag_friends = List[String]()
       if(user_node != null && node != null)
       {
         tag_friends = node.getRelationships("Tag_To_Tag").asScala.toList.sortBy(-_.getProperty("count").toString.toInt).map( y => y.getOtherNode(node)).filterNot( z => z.getRelationships("Favourite_Hash").asScala.map(_.getOtherNode(z)).toList.contains(user_node)).map( g => g.getProperty("name").toString())
       }
       
       
    JSONArray(tag_friends).toString()   
    }
  }
  
  
  def view_suggestions(
      
      item_type:String, //A/E/P
      item_id:String,	//id of the present article or empty
      a_ids:String,		//comma separated id's of the related articles
      count: Int,		//count to return that many number of articles
      user_name: String,	//user name of the logged in user
      hashtags: String	//comma separated hashtags to consider
      ): String =        
  {
    
       var cat =  List[String]() //list of categories of item_id
       var content = ""
       val toRemove = "~`!@#$%^&*()_-+=*{}[]:;'|/".toSet
       //var item_node: org.neo4j.graphdb.Node = null
       val userIndex = getNodeIndex("user").get	
       val hashIndex = getNodeIndex("sub_category").get
       val user_node = userIndex.get("id",user_name).getSingle() //get user node
       var item_node = user_node
       
       //val article_ids = a_ids.split(":").toList(0)
       val hash_ids = hashtags.filterNot(toRemove).toLowerCase().split(",").toList //remove any special symbols and convert to list
//       System.out.println("Total  number of hash tags inputed is: "+hash_ids.size + " and list is" + hash_ids)	//get the hash tag nodes
       
       var hash_nodes  = hash_ids.map( x => hashIndex.get("name",x).getSingle()).filter( y => y != null) //get hash tag nodes
//       System.out.println("Total  number of hash nodes inputed is: "+hash_nodes.size )
       
       if(item_type.equals("A"))
       {
         val itemIndex = getNodeIndex("article").get
         item_node = itemIndex.get("id",item_id).getSingle() //get the article node if item type is A / Article
         // get the item categories as list
         cat = item_node.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(item_node)).map(y => y.getProperty("name").toString()).toList        
       }
       
       else if(item_type.equals("E"))
       {
         val itemIndex = getNodeIndex("event").get
         item_node = itemIndex.get("id",item_id).getSingle() //get event node, if item type is E / Event
         //get the item categories as list
         cat = item_node.getRelationships("Belongs_To_Event_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(item_node)).map(y => y.getProperty("name").toString()).toList 
       }
       
       else
       {
         val itemIndex = getNodeIndex("petition").get
         item_node = itemIndex.get("id",item_id).getSingle() //get the petetion node, (only possible inputs are A/E/P)
         //get the categories of item node as list
         cat = item_node.getRelationships("Belongs_To_Petition_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(item_node)).map(y => y.getProperty("name").toString()).toList
       }
       
          val cat_filter = cat.filterNot( x => x.equals("all"))(0) //remove "all" category from categories
          //var rem = 4
          val ArticleIndex = getNodeIndex("article").get
          var art_sug = List[org.neo4j.graphdb.Node]()	//this will caontains list of articles i.e., (a_id's articles and present viewing article)
          if(!a_ids.equals(""))
          {
        	  art_sug = a_ids.split(",").toList.map( x => ArticleIndex.get("id",x).getSingle()).filter( y => y != null)
          }
          
      
          art_sug :+= item_node
          
          //get the categeories for each article in art_sug i.e., for input a_id's and viewing item
//          System.out.println("Total articles count including a_id articles : " + art_sug.size)

          var articles  = List[Any]() //list of JSON objects that needs to be retuned

          var art  = List[org.neo4j.graphdb.Node]() //list of articles to generate JSONObjects for above articles list variable

          val l1 = List("smry","v_users","votes","Commented_Users","Comment_Count","url","ttl","Auth","Auth_FN","img","pubTm","rl","id","cat")
          val l2 = List("FN","UN")
          val l3 = List("Name","UName")
          
            art = hash_nodes.map(	//for every hash gat from the user input hash tags
            		x => x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList
            		.map(_.getOtherNode(x))	//get all the articles nodes of the hash tags
            		.sortBy(-_.getProperty("time_created").toString().toInt)	//sort it based on time_created
            		.filter(	//filter the articles based on its category and isClosed property. 
            		    x => (
            		        (x.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)) //get the category nodes of each article
            		            .map(y => y.getProperty("name").toString()).toList.intersect(cat).size) > 1))	//get the article if its category is present in above calculated cat list
            		.toList
            		.filterNot(x => art_sug.contains(x)) //remove the articles which are in art_sug (removing the duplicates)
            		.slice(0,count))	//get top count articles 
            		.flatten	//convert to single list if it contains list of lists
            		.filter( x => x.getProperty("space").toString.toInt == 0).distinct.slice(0,count)	//verify if article is open

//          }
          
//            val TilesIndex = getNodeIndex("tiles").get
//            var news_node = TilesIndex.get("id",cat_filter).getSingle()
//            var all_node = TilesIndex.get("id","all").getSingle()
//            if(news_node != null)
//            {
//			    val news = news_node.getProperty("value").toString()
//			    if(!news.equals(""))
//	            {
//			       val news_list = news.split(",").toList
//			       trend_tiles = news_list.slice(0,8).map( x => ArticleIndex.get("id",x).getSingle()).filter( y => y != null)
//	            }
//			    
//			    val all_news = all_node.getProperty("value").toString()
//			    if(!all_news.equals(""))
//	            {
//			       val news_list = all_news.split(",").toList
//			       all_trend_tiles = news_list.slice(0,8).map( x => ArticleIndex.get("id",x).getSingle()).filter( y => y != null)
//	            }
//            }
		    
//		    art = art.filterNot( x => art_sug.contains(x)).slice(0,count)
//            art = hash_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.map(_.getOtherNode(hash_node)).sortBy(-_.getProperty("time_created").toString().toInt).filter(x => ((x.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name").toString()).toList.intersect(cat).size) > 1)).toList.filterNot(x => art_sug.contains(x)).slice(0,count)

		    if(user_node == null  || user_name.equals(""))
		    {
		    	articles = art.map(x => JSONObject(l1.zip(List(x.getProperty("article_summary"), JSONArray(x.getRelationships("article_voteup").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y => y.getOtherNode(x)).map( z =>  JSONObject(l3.zip(List( (z.getProperty("first_name").toString() + " " + z.getProperty("last_name").toString()),z.getProperty("user_name").toString()  ) ).toMap)))   ,x.getRelationships("article_voteup").asScala.toList.size - x.getRelationships("article_voteup").asScala.toList.slice(0,2).size, JSONArray(x.getRelationships("Comment_To_Article").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y => y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map( z =>  JSONObject(l2.zip(List( (z.getProperty("first_name").toString() + " " + z.getProperty("last_name").toString()),z.getProperty("user_name").toString()  ) ).toMap)))     ,x.getRelationships("Comment_To_Article").asScala.size,"/" + x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + x.getProperty("article_title_id"),x.getProperty("article_title"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("user_name"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("first_name") +" " + x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("last_name"),x.getProperty("article_featured_img"),x.getProperty("time_created"),  false,x.getProperty("article_id"),x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0))).toMap))

		    }
		    
		    else
		    {
		      articles = art.map(x => JSONObject(l1.zip(List(x.getProperty("article_summary"), JSONArray(x.getRelationships("article_voteup").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y => y.getOtherNode(x)).map( z =>  JSONObject(l3.zip(List( (z.getProperty("first_name").toString() + " " + z.getProperty("last_name").toString()),z.getProperty("user_name").toString()  ) ).toMap)))   ,x.getRelationships("article_voteup").asScala.toList.size - x.getRelationships("article_voteup").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Article").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y => y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map( z =>  JSONObject(l2.zip(List( (z.getProperty("first_name").toString() + " " + z.getProperty("last_name").toString()),z.getProperty("user_name").toString()  ) ).toMap))),x.getRelationships("Comment_To_Article").asScala.size,"/" + x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + x.getProperty("article_title_id"),x.getProperty("article_title"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("user_name"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("first_name") +" " + x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("last_name"),x.getProperty("article_featured_img"),x.getProperty("time_created"),  x.getRelationships("article_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("article_id"),x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0))).toMap))

		    }
         JSONArray(articles).toString()
     
  }
  
  def suggestions_turbo(
      item_type:String,
      item_id:String,
      content: String, // Article content written by the user
      cat: String  // , separated categories( "" if no category was selected by the user)
      ): String =        
  {
    
          var i_type = "article"
	      if(item_type.equals("E"))
	      {
	        i_type = "event"
	      }
	      //val index_name = i_type.toLowerCase()
	      val index = getNodeIndex(i_type).get
	      val item_node = index.get("id",item_id).getSingle()
          val article_content_index = getNodeIndex("article_content").get
//          val item_index = getNodeIndex("article").get
          val event_content_index = getNodeIndex("event_content").get
          val CategoryIndex = getNodeIndex("category").get
          val ww = bummy(content)
          var articles  = List[Any]()
          var events  = List[Any]()
          var art  = List[org.neo4j.graphdb.Node]()
          var event  = List[org.neo4j.graphdb.Node]()
          var sorted_arts  = List[org.neo4j.graphdb.Node]()
          var sorted_events  = List[org.neo4j.graphdb.Node]()
          val l1 = List("ev","ttl","tid","ctgy","htg")
          val l2 = List("ev","ttl","tid","ctgy","htg","lc","tmsp")
          
          if(!ww.equals(""))
          {
            
            val cur_time = (System.currentTimeMillis() /1000).toInt
            //val w = "*"+ww+"*"
            var list = ww.split(" ").toList
            var c = 0
            if(list.size <= 30)
            {
              c = 5
            }
            else if(list.size <= 50 && list.size > 30)
            {
              c = 7
            }
            else
            {
              c = 10
            }
            var big_list = list.grouped(c).toList
            var flag = big_list.size / 2
            var art_list = List[org.neo4j.graphdb.Node]()
            var event_list = List[org.neo4j.graphdb.Node]()
            if(!cat.equals(""))
            {
                
                
                
                for(each <- big_list)
                {
                    val w = "*"+each.mkString(" ")+"*"
		            art = article_content_index.query("article_content", w ).iterator().asScala.filter(x => ((x.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.intersect(cat.split(",").toList.map(x => x.toLowerCase())).size) > 0)).toList.filterNot(x => x.equals(item_node))
		            art_list = art_list ::: art
		            event = event_content_index.query("event_content", w ).iterator().asScala.filter(x => ((x.getRelationships("Belongs_To_Event_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.intersect(cat.split(",").toList.map(x => x.toLowerCase())).size) > 0)  && (x.getProperty("event_date_time_closing").toString().toInt > cur_time)  ).toList.filterNot(x => x.equals(item_node))
		            event_list = event_list ::: event
                }
                
                
                
                
	        }
            else
            {
                for(each <- big_list)
                {
                    val w = "*"+each.mkString(" ")+"*"
	                art = article_content_index.query("article_content", w).iterator().asScala.toList.filterNot(x => x.equals(item_node))
		            art_list = art_list ::: art
	                event = event_content_index.query("event_content", w).iterator().asScala.filter(q => (q.getProperty("event_date_time_closing").toString().toInt > cur_time)).toList.filterNot(x => x.equals(item_node))
	                event_list = event_list ::: event
                }
            }
            
            var map = art_list.groupBy(x => x).mapValues(x=>x.length)
            art = map.keys.toList.filter(x => map(x) >= flag).sortBy(x => -map(x)).slice(0,5)
                
            var map1 = event_list.groupBy(x => x).mapValues(x=>x.length)
            event = map1.keys.toList.filter(x => map1(x) >= flag).sortBy(x => -map1(x)).slice(0,5)
            
            articles = art.map(x => JSONObject(l1.zip(List(0,x.getProperty("article_title"),x.getProperty("article_title_id"),x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","))).toMap))
            events = event.map(x => JSONObject(l2.zip(List(1,x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getProperty("event_location"),x.getProperty("event_date_time"))).toMap))
           

          }
          
         JSONArray(articles:::events).toString()
     
  }
  
  def suggestions_morenames(
      item_type:String,
      item_id:String
      ): String =        
  {
    
          def matchTest(x: String): String = x match {
		    case "A" => "Article"
		    case "E" => "Event"
		    case "P" => "Petition"
		    case "D" => "Debate"
		    case "T" => "Townhall"
		    case "a" => "Article"
		    case "e" => "Event"
		    case "p" => "Petition"
		    case "d" => "Debate"
		    case "t" => "Townhall"
		    
		    }
		    val i_type = matchTest(item_type)
		  var ret = ""
		  val index_name = i_type.toLowerCase()
		  val index = getNodeIndex(index_name).get
	      val item_node = index.get("id",item_id).getSingle()
	      if(item_node != null)
	      {
	        if(item_type.toLowerCase().equals("a"))
	        {
	                var voted_users = item_node.getRelationships("article_voteup").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(item_node))
		            voted_users = voted_users.distinct
		            var voted_list = List[String]()
		            for(each <- voted_users)
		            {
		              voted_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
		            }
	                ret = voted_list.mkString(",")
	        }
	        
	        else if(item_type.toLowerCase().equals("e"))
	        {
	                var voted_users = item_node.getRelationships("Is_Attending").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(item_node))
		            voted_users = voted_users.distinct
		            var voted_list = List[String]()
		            for(each <- voted_users)
		            {
		              voted_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
		            }
	                ret = voted_list.mkString(",")
	        }
	        else
	        {
	                var voted_users = item_node.getRelationships("Signed_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(_.getOtherNode(item_node))
		            voted_users = voted_users.distinct
		            var voted_list = List[String]()
		            for(each <- voted_users)
		            {
		              voted_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
		            }
	                ret = voted_list.mkString(",")
	        }
	      }
	      
	      
	      
	ret      
  }
  
  // This function returns the suggestions(Articles/Events) and it is called when a user is writing a new article
  def suggestions(
      item_type:String,
      item_id:String,
      content: String, // Article content written by the user
      cat: String,  // , separated categories( "" if no category was selected by the user)
      hashtags: String,
      count: Int,
      prev_cnt: Int
      ): String =        
  {
    
          def matchTest(x: String): String = x match {
		    case "A" => "Article"
		    case "E" => "Event"
		    case "P" => "Petition"
		    case "D" => "Debate"
		    case "T" => "Townhall"
		    case "S" => "Space"
		    case "a" => "Article"
		    case "e" => "Event"
		    case "p" => "Petition"
		    case "d" => "Debate"
		    case "t" => "Townhall"
		    case "s" => "Space"
		    
		    }
		    val i_type = matchTest(item_type)
		    
		  val index_name = i_type.toLowerCase()
		  val index = getNodeIndex(index_name).get
	      val item_node = index.get("id",item_id).getSingle()
          val article_content_index = getNodeIndex("article_content").get
          
          val event_content_index = getNodeIndex("event_content").get
          val p_content_index = getNodeIndex("petition_content").get
          val d_content_index = getNodeIndex("debate_content").get
          val t_content_index = getNodeIndex("townhall_content").get
          //removing the unwanted content
          val CategoryIndex = getNodeIndex("category").get
          var ww = bummy(content)
          
//          val tagger = new MaxentTagger(
//	                "/var/n4j/data/left3words-wsj-0-18.tagger")
//	      val tagged = tagger.tagString(content)
//	              //val prev_nouns = user_node.getProperty("nouns").toString()
//	      var noun_list = tagged.split(" ").toList.map(x => x.split("/")).filter( x => (x.size == 2 && x(1).equals("NNP"))).map(x => x(0).toLowerCase())
//	              
//	      var ww = noun_list.mkString(" ")       
          val toRemove = "~`!@#$%^&*()_-+=*{}[]:;'|/".toSet
          ww = ww.filterNot(toRemove)
          var articles  = List[Any]()
          var events  = List[Any]()
          var petitions  = List[Any]()
          var debates  = List[Any]()
          var townhalls  = List[Any]()
          
          var hash_art  = List[org.neo4j.graphdb.Node]()
          var hash_event  = List[org.neo4j.graphdb.Node]()
          //var hash_petition  = List[org.neo4j.graphdb.Node]()
          var final_arts  = List[org.neo4j.graphdb.Node]()
          var final_events  = List[org.neo4j.graphdb.Node]()
          
//          var articles  = List[Any]()
          var art  = List[org.neo4j.graphdb.Node]()
          var event  = List[org.neo4j.graphdb.Node]()
          var petition  = List[org.neo4j.graphdb.Node]()
          var debate  = List[org.neo4j.graphdb.Node]()
          var townhall  = List[org.neo4j.graphdb.Node]()
          ww = ww + " " + hashtags.replace(',',' ')
          
          val l1 = List("ev","ttl","tid","ctgy","htg","auth","img")
          val l2 = List("ev","ttl","tid","ctgy","htg","lc","tmsp")
          val p_list = List("ev","id","ttl","tid","goal")
	      val d_list = List("ev","ttl","tid","date")
	      val t_list = List("ev","ttl","tid","celeb","date")
	      
	     // var tot_hash_list = ""
          if(!ww.equals(""))
          {
            val cur_time = (System.currentTimeMillis() /1000).toInt
	        val w = "*"+ww+"*"
            if(index_name.equals("space"))
            {
                    final_arts = article_content_index.query("article_content", w ).iterator().asScala.toList.filter( x =>  x.getProperty("space").toString.toInt == 0).slice(prev_cnt,(prev_cnt+count))
		            final_events = event_content_index.query("event_content", w ).iterator().asScala.toList.filter( x => x.getProperty("space").toString.toInt == 0).filter(q => (q.getProperty("event_date_time_closing").toString().toInt > cur_time)).slice(prev_cnt,(prev_cnt+count)).toList
		            petition = p_content_index.query("p_content", w ).iterator().asScala.toList.filter( x => x.getProperty("space").toString.toInt == 0).slice(prev_cnt,(prev_cnt+count))
			        debate = d_content_index.query("d_content", w ).iterator().asScala.toList.filter( x => x.getProperty("space").toString.toInt == 0).slice(prev_cnt,(prev_cnt+count))
			        townhall = t_content_index.query("t_content", w ).iterator().asScala.toList.filter( x => x.getProperty("space").toString.toInt == 0).slice(prev_cnt,(prev_cnt+count))
			        
            }
            else
            {
	            val SubcatIndex = getNodeIndex("sub_category").get
	            val hash_list = hashtags.split(",").toList.map( x => SubcatIndex.get("name",x).getSingle()).filter( y => y != null)
	            
	            def intersperse[A](a : List[A], b : List[A]): List[A] = a match {
					  case first :: rest => first :: intersperse(b, rest)
					  case _             => b
				 }  
	            // extracting all the suggestions based on content and category
	            if(!cat.equals(""))
	            {
	                hash_art = hash_list.map( x => x.getRelationships("Belongs_To_Subcategory_Article",Direction.INCOMING).asScala.map(_.getStartNode()).toList).flatten.filter(x => ((x.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.intersect(cat.split(",").toList.map(x => x.toLowerCase())).size) > 0))
		            val tot_hash_art_map =  (hash_art).groupBy(x=>x).mapValues(x=>x.length)
	                hash_art = tot_hash_art_map.keys.toList.sortBy(x => -tot_hash_art_map(x)).filter( x => x.getProperty("space").toString.toInt == 0).slice(0,20)
	            
	                hash_event = hash_list.map( x => x.getRelationships("Belongs_To_Subcategory_Event",Direction.INCOMING).asScala.map(_.getStartNode()).toList).flatten.filter(q => (q.getProperty("event_date_time_closing").toString().toInt > cur_time)).filter(x => ((x.getRelationships("Belongs_To_Event_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.intersect(cat.split(",").toList.map(x => x.toLowerCase())).size) > 0)   )
		            val tot_hash_event_map =  (hash_event).groupBy(x=>x).mapValues(x=>x.length)
	                hash_event = tot_hash_event_map.keys.toList.sortBy(x => -tot_hash_event_map(x)).filter( x => x.getProperty("space").toString.toInt == 0).slice(0,20)
	            
	                
	                art = article_content_index.query("article_content", w ).iterator().asScala.toList.filter(x => ((x.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.intersect(cat.split(",").toList.map(x => x.toLowerCase())).size) > 0)).filter( x => x.getProperty("space").toString.toInt == 0).slice(0,20)
		            event = event_content_index.query("event_content", w ).iterator().asScala.toList.filter(q => (q.getProperty("event_date_time_closing").toString().toInt > cur_time)).filter(x => ((x.getRelationships("Belongs_To_Event_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.intersect(cat.split(",").toList.map(x => x.toLowerCase())).size) > 0)   ).filter( x => x.getProperty("space").toString.toInt == 0).slice(0,20)
		            petition = p_content_index.query("p_content", w ).iterator().asScala.toList.filterNot(x => x.equals(item_node)).filter( x => x.getProperty("space").toString.toInt == 0).slice(0,20)
			        debate = d_content_index.query("d_content", w ).iterator().asScala.toList.filterNot(x => x.equals(item_node)).filter( x => x.getProperty("space").toString.toInt == 0).slice(0,20)
			        townhall = t_content_index.query("t_content", w ).iterator().asScala.toList.filterNot(x => x.equals(item_node)).filter( x => x.getProperty("space").toString.toInt == 0).slice(0,20)
			        
			        val art_intersect = hash_art.intersect(art)
			        final_arts = (art_intersect ::: intersperse(hash_art,art) ).distinct.slice(0,20).filterNot(x => x.equals(item_node))
	            
			        val event_intersect = hash_event.intersect(event)
			        final_events = (event_intersect ::: intersperse(hash_event,event) ).distinct.slice(0,20).filterNot(x => x.equals(item_node))
	            
	            }
	            // extracting all the suggestions based on content
	            else
	            {
	              
	                hash_art = hash_list.map( x => x.getRelationships("Belongs_To_Subcategory_Article",Direction.INCOMING).asScala.map(_.getStartNode()).toList).flatten
		            val tot_hash_art_map =  (hash_art).groupBy(x=>x).mapValues(x=>x.length)
	                hash_art = tot_hash_art_map.keys.toList.sortBy(x => -tot_hash_art_map(x)).filter( x => x.getProperty("space").toString.toInt == 0).slice(0,20)
	            
	                hash_event = hash_list.map( x => x.getRelationships("Belongs_To_Subcategory_Event",Direction.INCOMING).asScala.map(_.getStartNode()).toList).flatten.filter(q => (q.getProperty("event_date_time_closing").toString().toInt > cur_time))
		            val tot_hash_event_map =  (hash_event).groupBy(x=>x).mapValues(x=>x.length)
	                hash_event = tot_hash_event_map.keys.toList.sortBy(x => -tot_hash_event_map(x)).filter( x => x.getProperty("space").toString.toInt == 0).slice(0,20)
	            
	                art = article_content_index.query("article_content", w).iterator().asScala.toList.filter( x => x.getProperty("space").toString.toInt == 0).slice(0,20)
		            event = event_content_index.query("event_content", w).iterator().asScala.toList.filter(q => (q.getProperty("event_date_time_closing").toString().toInt > cur_time)).filter( x => x.getProperty("space").toString.toInt == 0).slice(0,20)
	                petition = p_content_index.query("p_content", w ).iterator().asScala.toList.filterNot(x => x.equals(item_node)).filter( x => x.getProperty("space").toString.toInt == 0).slice(0,20)
			        debate = d_content_index.query("d_content", w ).iterator().asScala.toList.filterNot(x => x.equals(item_node)).filter( x => x.getProperty("space").toString.toInt == 0).slice(0,20)
			        townhall = t_content_index.query("t_content", w ).iterator().asScala.toList.filterNot(x => x.equals(item_node)).filter( x => x.getProperty("space").toString.toInt == 0).slice(0,20)
			        
			        val art_intersect = hash_art.intersect(art)
			        final_arts = (art_intersect ::: intersperse(hash_art,art) ).distinct.slice(0,20).filterNot(x => x.equals(item_node))
	            
			        val event_intersect = hash_event.intersect(event)
			        final_events = (event_intersect ::: intersperse(hash_event,event) ).distinct.slice(0,20).filterNot(x => x.equals(item_node))
	            
	            }
            }
            articles = final_arts.map(x => JSONObject(l1.zip(List(0,x.getProperty("article_title"),x.getProperty("article_title_id"),x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("user_name"),x.getProperty("article_featured_img"))).toMap))
            events = final_events.map(x => JSONObject(l2.zip(List(1,x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getProperty("event_location"),x.getProperty("event_date_time"))).toMap))
            petitions = petition.map(x => JSONObject(p_list.zip(List(2,x.getProperty("p_id"),x.getProperty("p_title"),x.getProperty("p_title_id"),x.getProperty("p_target"))).toMap))
            debates = debate.map(x => JSONObject(d_list.zip(List(3,x.getProperty("d_title"),x.getProperty("d_title_id"),x.getProperty("d_date"))).toMap))
            townhalls = townhall.map(x => JSONObject(t_list.zip(List(4,x.getProperty("t_title"),x.getProperty("t_title_id"),x.getProperty("t_content"),x.getProperty("t_date"))).toMap))
            
            
            
          }
          
         JSONArray(articles:::events:::petitions:::debates:::townhalls).toString()
     
  }
  
  def cf_suggestions(
      item_type:String,
      item_id:String,
      user_name: String
      ): String =        
  {
    
          
		   
          var cfs  = List[Any]()
          var hashtags  = List[String]()
          var cf  = List[org.neo4j.graphdb.Node]()
          val userIndex = getNodeIndex("user").get
          val user_node = userIndex.get("id",user_name).getSingle()
          var item_node = user_node  
          
           if(item_type.equals("A"))
	       {
	         val itemIndex = getNodeIndex("article").get
	         item_node = itemIndex.get("id",item_id).getSingle()
	         hashtags = item_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.map(_.getOtherNode(item_node).getProperty("name").toString())
	         
		         
	       }
	       
	       else if(item_type.equals("E"))
	       {
	         val itemIndex = getNodeIndex("event").get
	         item_node = itemIndex.get("id",item_id).getSingle()
	         hashtags = item_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.map(_.getOtherNode(item_node).getProperty("name").toString())
	       
	       }
	       
	       else
	       {
	         val itemIndex = getNodeIndex("petition").get
	         item_node = itemIndex.get("id",item_id).getSingle()
	         hashtags = item_node.getRelationships("Belongs_To_Subcategory_Petition").asScala.toList.map(_.getOtherNode(item_node).getProperty("name").toString())
	         
		 
	       }
          
           
           val l1 = List("user","id","ttl","content","img","url","time_created","end_date","amt_target","amt_raised","ppl_count")
          
           val Cfindex = getNodeIndex("cfpost").get
           val cf_posts = Cfindex.query("id", "*" ).iterator().asScala.toList
           cf = cf_posts.filter(x => x.getProperty("cf_tags").toString().toLowerCase().split(",").toList.intersect(hashtags).size > 0 )
           cfs = cf.map(x => JSONObject(l1.zip(List(x.getProperty("user"),x.getProperty("cf_id"),x.getProperty("cf_title"),x.getProperty("cf_content"),x.getProperty("cf_featured_img"),x.getProperty("cf_url"),x.getProperty("time_created"),x.getProperty("end_date"),x.getProperty("amt_target"),x.getProperty("amt_raised"),x.getProperty("ppl_count"))).toMap))
            
         JSONArray(cfs).toString()
     
  }
  
  // This function returns the suggestions(Articles/Events) and it is called when a user is writing a new article
  def hashtag_suggestions(
      item_type:String,
      item_id:String,
      content: String, // Article content written by the user
      cat: String  // , separated categories( "" if no category was selected by the user)
      ): String =        
  {
    
          def matchTest(x: String): String = x match {
		    case "A" => "Article"
		    case "E" => "Event"
		    case "P" => "Petition"
		    case "D" => "Debate"
		    case "T" => "Townhall"
		    case "a" => "Article"
		    case "e" => "Event"
		    case "p" => "Petition"
		    case "d" => "Debate"
		    case "t" => "Townhall"
		    
		    }
		    val i_type = matchTest(item_type)
		    
		  val index_name = i_type.toLowerCase()
		  val index = getNodeIndex(index_name).get
	      val item_node = index.get("id",item_id).getSingle()
          val article_content_index = getNodeIndex("article_content").get
          
          val event_content_index = getNodeIndex("event_content").get
          
          //removing the unwanted content
          val CategoryIndex = getNodeIndex("category").get
          //var ww = bummy(content)
          
          val tagger = new MaxentTagger(
	                "/var/n4j/data/left3words-wsj-0-18.tagger")
	      val tagged = tagger.tagString(content)
	              //val prev_nouns = user_node.getProperty("nouns").toString()
	      var noun_list = tagged.split(" ").toList.map(x => x.split("/")).filter( x => (x.size == 2 && x(1).equals("NNP"))).map(x => x(0).toLowerCase())
	              
	      var ww = noun_list.mkString(" ") 
          val toRemove = "~`!@#$%^&*()_-+=*{}[]:;'|/".toSet
          ww = ww.filterNot(toRemove)
          
          var art  = List[org.neo4j.graphdb.Node]()
          var event  = List[org.neo4j.graphdb.Node]()
          
          var art_hash_tags = List[org.neo4j.graphdb.Node]()
          var event_hash_tags = List[org.neo4j.graphdb.Node]()
          
          
	      var tot_hash_list = ""
          if(!ww.equals(""))
          {
            
            val cur_time = (System.currentTimeMillis() /1000).toInt
            val w = "*"+ww+"*"
            // extracting all the suggestions based on content and category
            if(!cat.equals(""))
            {
                
	            art = article_content_index.query("article_content", w ).iterator().asScala.filter(x => ((x.getRelationships("Belongs_To_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.intersect(cat.split(",").toList.map(x => x.toLowerCase())).size) > 0)).slice(0,10).toList.filterNot(x => x.equals(item_node)).sortBy(-_.getProperty("time_created").toString().toInt)
	            event = event_content_index.query("event_content", w ).iterator().asScala.filter(q => (q.getProperty("event_date_time_closing").toString().toInt > cur_time)).filter(x => ((x.getRelationships("Belongs_To_Event_Category",Direction.OUTGOING).asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).toList.intersect(cat.split(",").toList.map(x => x.toLowerCase())).size) > 0)   ).slice(0,10).toList.filterNot(x => x.equals(item_node)).sortBy(-_.getProperty("time_created").toString().toInt)
//	            petition = p_content_index.query("p_content", w ).iterator().asScala.toList.filterNot(x => x.equals(item_node)).slice(0,10).sortBy(-_.getProperty("time_created").toString().toInt)
//		        debate = d_content_index.query("d_content", w ).iterator().asScala.toList.filterNot(x => x.equals(item_node)).slice(0,10).sortBy(-_.getProperty("time_created").toString().toInt)
//		        townhall = t_content_index.query("t_content", w ).iterator().asScala.toList.filterNot(x => x.equals(item_node)).slice(0,10).sortBy(-_.getProperty("time_created").toString().toInt)
		        art_hash_tags = art.map(x => x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.map( x => x.getEndNode())).flatten
                event_hash_tags = event.map(x => x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.map( x => x.getEndNode())).flatten
                
            }
            // extracting all the suggestions based on content
            else
            {
                art = article_content_index.query("article_content", w).iterator().asScala.slice(0,10).toList.filterNot(x => x.equals(item_node)).sortBy(-_.getProperty("time_created").toString().toInt)
	            event = event_content_index.query("event_content", w).iterator().asScala.filter(q => (q.getProperty("event_date_time_closing").toString().toInt > cur_time)).slice(0,10).toList.filterNot(x => x.equals(item_node)).sortBy(-_.getProperty("time_created").toString().toInt)
//                petition = p_content_index.query("p_content", w ).iterator().asScala.toList.filterNot(x => x.equals(item_node)).slice(0,10).sortBy(-_.getProperty("time_created").toString().toInt)
//		        debate = d_content_index.query("d_content", w ).iterator().asScala.toList.filterNot(x => x.equals(item_node)).slice(0,10).sortBy(-_.getProperty("time_created").toString().toInt)
//		        townhall = t_content_index.query("t_content", w ).iterator().asScala.toList.filterNot(x => x.equals(item_node)).slice(0,10).sortBy(-_.getProperty("time_created").toString().toInt)
		        art_hash_tags = art.map(x => x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.map( x => x.getEndNode())).flatten
                event_hash_tags = event.map(x => x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.map( x => x.getEndNode())).flatten
                
            }
//            articles = art.map(x => JSONObject(l1.zip(List(0,x.getProperty("article_title"),x.getProperty("article_title_id"),x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("user_name"),x.getProperty("article_featured_img"))).toMap))
//            events = event.map(x => JSONObject(l2.zip(List(1,x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getProperty("event_location"),x.getProperty("event_date_time"))).toMap))
//            petitions = petition.map(x => JSONObject(p_list.zip(List(2,x.getProperty("p_id"),x.getProperty("p_title"),x.getProperty("p_title_id"),x.getProperty("p_target"))).toMap))
//            debates = debate.map(x => JSONObject(d_list.zip(List(3,x.getProperty("d_title"),x.getProperty("d_title_id"),x.getProperty("d_date"))).toMap))
//            townhalls = townhall.map(x => JSONObject(t_list.zip(List(4,x.getProperty("t_title"),x.getProperty("t_title_id"),x.getProperty("t_content"),x.getProperty("t_date"))).toMap))
            
            // filtering the top hash suggestions
            val ae_hashtags = (art_hash_tags:::event_hash_tags).map(x => x.getProperty("name").toString())
            val tot_hash_map =  (ae_hashtags:::noun_list).groupBy(x=>x).mapValues(x=>x.length)
            tot_hash_list = tot_hash_map.keys.toList.sortBy(x => -tot_hash_map(x)).slice(0,20).mkString(",")
            //val tags = JSONObject(h_list.zip(List(5,tot_hash_list)).toMap)
            //articles ::= tags
            
          }
          
         tot_hash_list
     
  }
  
  
  def nonuser_suggestions(
      ): String =        
  {
    
    
    
          val nonuserIndex = getNodeIndex("nonuser").get
          val article_content_index = getNodeIndex("article_content").get
          val event_content_index = getNodeIndex("event_content").get
          val nonusers = nonuserIndex.query("id", "*" ).iterator().asScala.toList
          var articles  = List[Any]()
          var events  = List[Any]()
          var art  = List[org.neo4j.graphdb.Node]()
          var event  = List[org.neo4j.graphdb.Node]()
          var sorted_arts  = List[org.neo4j.graphdb.Node]()
          var sorted_events  = List[org.neo4j.graphdb.Node]()
          val l1 = List("ev","ttl","tid","ctgy","htg")
          val l2 = List("ev","ttl","tid","ctgy","htg","lc","dte")
          var big_list = List[Any]()
          for(each <- nonusers)
          {
           
            val user_content = each.getProperty("content").toString()
            val ww = bummy(user_content)
            val cur_time = (System.currentTimeMillis() /1000).toInt
            val w = "*"+ww+"*"
            art = article_content_index.query("article_content", w ).iterator().asScala.toList.filterNot( x => x.getRelationships("Viewed_By_Nonuser").asScala.map(_.getOtherNode(x)).toList.contains(each)).slice(0,5)
	        event = event_content_index.query("event_content", w ).iterator().asScala.toList.filter(x => (x.getProperty("event_date_time_closing").toString().toInt > cur_time ) && (!x.getRelationships("Viewed_By_Nonuser").asScala.map(_.getOtherNode(x)).toList.contains(each)) ).slice(0,5)
	        articles = art.map(x => JSONObject(l1.zip(List(0,x.getProperty("article_title"),x.getProperty("article_title_id"),x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Article").asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).mkString(","))).toMap))
            events =  event.map(x => JSONObject(l2.zip(List(1,x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.map(_.getOtherNode(x)).map(y => y.getProperty("name")).mkString(","),x.getProperty("event_location"),x.getProperty("event_date_time"))).toMap))
            val json = JSONArray(articles:::events)
            var small_list = List[Any]()
            small_list :+= each.getProperty("user_name")
            small_list :+= json
            big_list :+= JSONArray(small_list)
	      }
          JSONArray(big_list).toString()
     
  }
  
 
  // This function returns the search results(Articles, events and etc)
  def search(
        user_name:String, //unique user name
        content: String,  // search string entered by the user
        cnt:Int,  // No of results required
        prev_cnt:Int, // No of results sent
        item_type:String
        ): String =        
    {
 
    
    withTx {
    implicit neo =>
    
      
          val article_content_index = getNodeIndex("article_content").get
          val event_content_index = getNodeIndex("event_content").get
          val p_content_index = getNodeIndex("petition_content").get
          val d_content_index = getNodeIndex("debate_content").get
          val t_content_index = getNodeIndex("townhall_content").get
          //val qp_content_index = getNodeIndex("quickpost_content").get
          val search_index = getNodeIndex("search_word").get
          val hash_index = getNodeIndex("sub_category").get
          val user_index = getNodeIndex("user").get
          val user_node = user_index.get("id",user_name).getSingle()
          
          var ww = ""
          val toRemove = "~`!@#$%^&*()_-+=*{}[]:;'|/".toSet
          def intersperse[A](a : List[A], b : List[A]): List[A] = a match {
			  case first :: rest => first :: intersperse(b, rest)
			  case _             => b
	      }
          
            // removing the unwanted content from search string
          ww = bummy(content)
          ww = ww.filterNot(toRemove).stripPrefix(" ").stripSuffix(" ").trim
          
          
          var items  = List[Any]()
          var item  = List[org.neo4j.graphdb.Node]()
          var sorted_item  = List[org.neo4j.graphdb.Node]()
          val cur_time = (System.currentTimeMillis() /1000).toInt
          val a_list = List("ev","auth","ttl","tid","ctgy","htg","ccnt","img","sum")
          val e_list = List("ev","ttl","tid","ctgy","htg","lc","dte")
          
          val p_list = List("ev","id","ttl","tid","goal","users")
	      val d_list = List("ev","ttl","tid","date")
	      val t_list = List("ev","ttl","tid","celeb","date")
          // linking user with the search string 
          if(content != "" && !user_name.equals("") && user_node != null && item_type.equals("AE"))
          {
              var search_node = search_index.get("name",content).getSingle()
              if( search_node != null)
              {
                val rel: org.neo4j.graphdb.Relationship = user_node --> "Searched_For" --> search_node <
                val rel_time = rel.setProperty("time", cur_time)
              }
              else
              {
                search_node = createNode(search_word(content))
                search_index += (search_node,"name",content)
                val rel: org.neo4j.graphdb.Relationship = user_node --> "Searched_For" --> search_node <
                val rel_time = rel.setProperty("time", cur_time)
                
              }
          }
          
          // when the user searches for a string
          if( !ww.equals("")  )
          {
            
            
            val w = "*"+ww+"*"
            var art  = List[org.neo4j.graphdb.Node]()
                var event  = List[org.neo4j.graphdb.Node]()
                var townhall  = List[org.neo4j.graphdb.Node]()
                var petition  = List[org.neo4j.graphdb.Node]()
                var debate  = List[org.neo4j.graphdb.Node]()
                var articles  = List[Any]()
                var events  = List[Any]()
                var petitions  = List[Any]()
                var debates  = List[Any]()
                var townhalls  = List[Any]()
            // search more articles
            if(item_type.toLowerCase().equals("all"))
            {
                // if the search string contains only one word
	            if(!ww.contains(" "))
	            {
	              art = article_content_index.query("article_content", w).iterator().asScala.toList.sortBy(-_.getProperty("time_created").toString().toInt)
	              event = event_content_index.query("event_content", w).iterator().asScala.toList.sortBy(-_.getProperty("time_created").toString().toInt)
	              townhall = t_content_index.query("t_content", w ).iterator().asScala.toList.sortBy(-_.getProperty("time_created").toString().toInt)
				  petition = p_content_index.query("p_content", w ).iterator().asScala.toList.sortBy(-_.getProperty("time_created").toString().toInt)
			      debate = d_content_index.query("d_content", w ).iterator().asScala.toList.sortBy(-_.getProperty("time_created").toString().toInt)
				  
                }
	            // if the search string contains more than 1 word
	            else
	            {
	              art = article_content_index.query("article_content", w).iterator().asScala.toList
	              event = event_content_index.query("event_content", w).iterator().asScala.toList
	              townhall = t_content_index.query("t_content", w ).iterator().asScala.toList
				  petition = p_content_index.query("p_content", w ).iterator().asScala.toList
			      debate = d_content_index.query("d_content", w ).iterator().asScala.toList
				  
	            }
	            // mixing the result articles and events
	            
	            var ae_intersperse = intersperse(art,event)
	            
	            var items1 = intersperse(debate,townhall)
	            var items2 = intersperse(petition,items1)
                ae_intersperse = intersperse(ae_intersperse, items2)
                ae_intersperse = ae_intersperse.filter( x => x.getProperty("space").toString.toInt == 0).slice(prev_cnt,(prev_cnt+cnt))
	            val l = List("Comment_Count_Unique","ev","v_users","votes","Commented_Users","Comment_Count","Is_Neo4j","P_Pin","P_Id","P_Author","P_Author_FullName","P_IsMarkedReadLater","P_Title","P_Title_ID","P_Category","P_SubCategory","P_Num_Comments","P_Feature_Image","P_Smry", "P_TimeCreated", "P_EventLocation", "P_EventStartTime", "P_EventAttendStatus", "P_SignsRequired", "P_PetitionSignStatus")
		        val l2 = List("FN","UN")
                val l3 = List("Name","UName")
                for(x <- ae_intersperse)
		        {
                  
		          if(x.getProperty("__CLASS__").toString.equals("Saddahaq.article"))
		          {
		            
		            items :+= JSONObject(l.zip(List(x.getRelationships("Comment_To_Article").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,0,JSONArray(x.getRelationships("article_voteup").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("article_voteup").asScala.toList.size-x.getRelationships("article_voteup").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Article").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Article").asScala.size,true,false,x.getProperty("article_id"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("article_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("article_title"),x.getProperty("article_title_id"),x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Article").asScala.size,x.getProperty("article_featured_img").toString(),x.getProperty("article_summary").toString(),x.getProperty("time_created").toString(),"","",false,"",false)).toMap)
					          
		          }
		          else if(x.getProperty("__CLASS__").toString.equals("Saddahaq.event"))
		          {
		            
		            items :+= JSONObject(l.zip(List(x.getRelationships("Comment_To_Event").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,1,JSONArray(x.getRelationships("Is_Attending").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Is_Attending").asScala.toList.size-x.getRelationships("Is_Attending").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Event").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Event").asScala.size,true,false,x.getProperty("event_id"),x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("event_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Event").asScala.size,x.getProperty("event_featured_img").toString(),x.getProperty("event_summary").toString(),x.getProperty("time_created").toString(),x.getProperty("event_location").toString(),x.getProperty("event_date_time").toString(),x.getRelationships("Is_Attending").asScala.map(_.getOtherNode(x)).toList.contains(user_node), "", false)).toMap)
					          
		          }
		          else if(x.getProperty("__CLASS__").toString.equals("Saddahaq.petition"))
		          {
		            
		            
		            items :+= JSONObject(l.zip(List(x.getRelationships("Comment_To_Petition").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,2,JSONArray(x.getRelationships("Signed_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Signed_Petition").asScala.toList.size-x.getRelationships("Signed_Petition").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Petition").asScala.size,true,false,x.getProperty("p_id"),x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("petition_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("p_title"),x.getProperty("p_title_id"),x.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Petition").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Petition").asScala.size,x.getProperty("p_img_url").toString(),x.getProperty("p_content").toString(),x.getProperty("time_created").toString(),"","",false,x.getProperty("p_target").toString().toInt - x.getProperty("p_count").toString().toInt,x.getRelationships("Signed_Petition").asScala.map(_.getOtherNode(x)).toList.contains(user_node))).toMap)
					          
		          }
		          else if(x.getProperty("__CLASS__").toString.equals("Saddahaq.townhall"))
		          {
		            items :+= JSONObject(l.zip(List(x.getRelationships("Commented_On_Townhall").asScala.toList.map(y=>y.getOtherNode(x)).distinct.size,3,JSONArray(x.getRelationships("Asked_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Asked_Question").asScala.toList.size-x.getRelationships("Asked_Question").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Commented_On_Townhall").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x)).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Commented_On_Townhall").asScala.size,true,false,x.getProperty("t_id"),x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("townhall_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("t_title"),x.getProperty("t_title_id"),"",x.getRelationships("Belongs_To_Subcategory_Townhall").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Commented_On_Townhall").asScala.size,x.getProperty("t_img_url").toString(),x.getProperty("t_content").toString(),x.getProperty("time_created").toString(),"","",false,"",false)).toMap)
					          
		          }
		          else
		          {
		            items :+= JSONObject(l.zip(List(x.getRelationships("Commented_On_Debate").asScala.toList.map(y=>y.getOtherNode(x)).distinct.size,4,JSONArray(x.getRelationships("Asked_Debate_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Asked_Debate_Question").asScala.toList.size-x.getRelationships("Asked_Debate_Question").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Commented_On_Debate").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x)).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Commented_On_Debate").asScala.size,true,false,x.getProperty("d_id"),x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("debate_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("d_title"),x.getProperty("d_title_id"),"",x.getRelationships("Belongs_To_Subcategory_Debate").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Commented_On_Debate").asScala.size,x.getProperty("d_img_url").toString(),x.getProperty("d_content").toString(),x.getProperty("time_created").toString(),"","",false,"",false)).toMap)
					          
		          }
		          
		          
		          
		        }
            }
            
            else if(item_type.toLowerCase().equals("a"))
            {
                // if the search string contains only one word
	            if(!ww.contains(" "))
	            {
	              art = article_content_index.query("article_content", w).iterator().asScala.toList.sortBy(-_.getProperty("time_created").toString().toInt)
	              
                }
	            // if the search string contains more than 1 word
	            else
	            {
	              art = article_content_index.query("article_content", w).iterator().asScala.toList
	              
	            }
	            // mixing the result articles and events
	            
	            art = art.filter( x => x.getProperty("space").toString.toInt == 0).slice(prev_cnt,(prev_cnt+cnt))
	            
	            val l = List("Comment_Count_Unique","ev","v_users","votes","Commented_Users","Comment_Count","Is_Neo4j","P_Pin","P_Id","P_Author","P_Author_FullName","P_IsMarkedReadLater","P_Title","P_Title_ID","P_Category","P_SubCategory","P_Num_Comments","P_Feature_Image","P_Smry", "P_TimeCreated", "P_EventLocation", "P_EventStartTime", "P_EventAttendStatus", "P_SignsRequired", "P_PetitionSignStatus")
		        val l2 = List("FN","UN")
                val l3 = List("Name","UName")
                for(x <- art)
		        {
                  
		          items :+= JSONObject(l.zip(List(x.getRelationships("Comment_To_Article").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,0,JSONArray(x.getRelationships("article_voteup").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("article_voteup").asScala.toList.size-x.getRelationships("article_voteup").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Article").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Article").asScala.size,true,false,x.getProperty("article_id"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("article_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("article_title"),x.getProperty("article_title_id"),x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Article").asScala.size,x.getProperty("article_featured_img").toString(),x.getProperty("article_summary").toString(),x.getProperty("time_created").toString(),"","",false,"",false)).toMap)
					          
		        }
            }
            
            else if(item_type.toLowerCase().equals("e"))
            {
                // if the search string contains only one word
	            if(!ww.contains(" "))
	            {
	              event = event_content_index.query("event_content", w).iterator().asScala.toList.sortBy(-_.getProperty("time_created").toString().toInt)
	              
                }
	            // if the search string contains more than 1 word
	            else
	            {
	              event = event_content_index.query("event_content", w).iterator().asScala.toList
	              
	            }
	            // mixing the result articles and events
	            event = event.filter( x => x.getProperty("space").toString.toInt == 0).slice(prev_cnt,(prev_cnt+cnt))
	            
	            
	            val l = List("Comment_Count_Unique","ev","v_users","votes","Commented_Users","Comment_Count","Is_Neo4j","P_Pin","P_Id","P_Author","P_Author_FullName","P_IsMarkedReadLater","P_Title","P_Title_ID","P_Category","P_SubCategory","P_Num_Comments","P_Feature_Image","P_Smry", "P_TimeCreated", "P_EventLocation", "P_EventStartTime", "P_EventAttendStatus", "P_SignsRequired", "P_PetitionSignStatus")
		        val l2 = List("FN","UN")
                val l3 = List("Name","UName")
                for(x <- event)
		        {
                  
		          items :+= JSONObject(l.zip(List(x.getRelationships("Comment_To_Event").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,1,JSONArray(x.getRelationships("Is_Attending").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Is_Attending").asScala.toList.size-x.getRelationships("Is_Attending").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Event").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Event").asScala.size,true,false,x.getProperty("event_id"),x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("event_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Event").asScala.size,x.getProperty("event_featured_img").toString(),x.getProperty("event_summary").toString(),x.getProperty("time_created").toString(),x.getProperty("event_location").toString(),x.getProperty("event_date_time").toString(),x.getRelationships("Is_Attending").asScala.map(_.getOtherNode(x)).toList.contains(user_node), "", false)).toMap)
				
		        }
            }
            
            else if(item_type.toLowerCase().equals("p"))
            {
                // if the search string contains only one word
	            if(!ww.contains(" "))
	            {
	              petition = p_content_index.query("p_content", w ).iterator().asScala.toList.sortBy(-_.getProperty("time_created").toString().toInt)
			      
                }
	            // if the search string contains more than 1 word
	            else
	            {
	              petition = p_content_index.query("p_content", w ).iterator().asScala.toList
			      
	            }
	            // mixing the result articles and events
	            petition = petition.filter( x => x.getProperty("space").toString.toInt == 0).slice(prev_cnt,(prev_cnt+cnt))
	            
	            val l = List("Comment_Count_Unique","ev","v_users","votes","Commented_Users","Comment_Count","Is_Neo4j","P_Pin","P_Id","P_Author","P_Author_FullName","P_IsMarkedReadLater","P_Title","P_Title_ID","P_Category","P_SubCategory","P_Num_Comments","P_Feature_Image","P_Smry", "P_TimeCreated", "P_EventLocation", "P_EventStartTime", "P_EventAttendStatus", "P_SignsRequired", "P_PetitionSignStatus")
		        val l2 = List("FN","UN")
                val l3 = List("Name","UName")
                for(x <- petition)
		        {
                  
		          
		          items :+= JSONObject(l.zip(List(x.getRelationships("Comment_To_Petition").asScala.toList.map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.size,2,JSONArray(x.getRelationships("Signed_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Signed_Petition").asScala.toList.size-x.getRelationships("Signed_Petition").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Comment_To_Petition").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Comment_To_Petition").asScala.size,true,false,x.getProperty("p_id"),x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("petition_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("p_title"),x.getProperty("p_title_id"),x.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Petition").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Petition").asScala.size,x.getProperty("p_img_url").toString(),x.getProperty("p_content").toString(),x.getProperty("time_created").toString(),"","",false,x.getProperty("p_target").toString().toInt - x.getProperty("p_count").toString().toInt,x.getRelationships("Signed_Petition").asScala.map(_.getOtherNode(x)).toList.contains(user_node))).toMap)
				
		        }
            }
            
            else if(item_type.toLowerCase().equals("t"))
            {
                // if the search string contains only one word
	            if(!ww.contains(" "))
	            {
	              townhall = t_content_index.query("t_content", w ).iterator().asScala.toList.sortBy(-_.getProperty("time_created").toString().toInt)
				  
                }
	            // if the search string contains more than 1 word
	            else
	            {
	              townhall = t_content_index.query("t_content", w ).iterator().asScala.toList
				  
	            }
	            // mixing the result articles and events
	            townhall = townhall.filter( x => x.getProperty("space").toString.toInt == 0).slice(prev_cnt,(prev_cnt+cnt))
	            
	            val l = List("Comment_Count_Unique","ev","v_users","votes","Commented_Users","Comment_Count","Is_Neo4j","P_Pin","P_Id","P_Author","P_Author_FullName","P_IsMarkedReadLater","P_Title","P_Title_ID","P_Category","P_SubCategory","P_Num_Comments","P_Feature_Image","P_Smry", "P_TimeCreated", "P_EventLocation", "P_EventStartTime", "P_EventAttendStatus", "P_SignsRequired", "P_PetitionSignStatus")
		        val l2 = List("FN","UN")
                val l3 = List("Name","UName")
                for(x <- townhall)
		        {
                  
		          items :+= JSONObject(l.zip(List(x.getRelationships("Commented_On_Townhall").asScala.toList.map(y=>y.getOtherNode(x)).distinct.size,3,JSONArray(x.getRelationships("Asked_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Asked_Question").asScala.toList.size-x.getRelationships("Asked_Question").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Commented_On_Townhall").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x)).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Commented_On_Townhall").asScala.size,true,false,x.getProperty("t_id"),x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("townhall_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("t_title"),x.getProperty("t_title_id"),"",x.getRelationships("Belongs_To_Subcategory_Townhall").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Commented_On_Townhall").asScala.size,x.getProperty("t_img_url").toString(),x.getProperty("t_content").toString(),x.getProperty("time_created").toString(),"","",false,"",false)).toMap)
					          
		          
		        }
            }
            
            else if(item_type.toLowerCase().equals("d"))
            {
                // if the search string contains only one word
	            if(!ww.contains(" "))
	            {
	              debate = d_content_index.query("d_content", w ).iterator().asScala.toList.sortBy(-_.getProperty("time_created").toString().toInt)
				  
                }
	            // if the search string contains more than 1 word
	            else
	            {
	              debate = d_content_index.query("d_content", w ).iterator().asScala.toList
				  
	            }
	            // mixing the result articles and events
	            debate = debate.filter( x => x.getProperty("space").toString.toInt == 0).slice(prev_cnt,(prev_cnt+cnt))
	            
	            val l = List("Comment_Count_Unique","ev","v_users","votes","Commented_Users","Comment_Count","Is_Neo4j","P_Pin","P_Id","P_Author","P_Author_FullName","P_IsMarkedReadLater","P_Title","P_Title_ID","P_Category","P_SubCategory","P_Num_Comments","P_Feature_Image","P_Smry", "P_TimeCreated", "P_EventLocation", "P_EventStartTime", "P_EventAttendStatus", "P_SignsRequired", "P_PetitionSignStatus")
		        val l2 = List("FN","UN")
                val l3 = List("Name","UName")
                for(x <- debate)
		        {
                  
		          
		            items :+= JSONObject(l.zip(List(x.getRelationships("Commented_On_Debate").asScala.toList.map(y=>y.getOtherNode(x)).distinct.size,4,JSONArray(x.getRelationships("Asked_Debate_Question").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(x)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Asked_Debate_Question").asScala.toList.size-x.getRelationships("Asked_Debate_Question").asScala.toList.slice(0,2).size,  JSONArray(x.getRelationships("Commented_On_Debate").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).map(y=>y.getOtherNode(x)).distinct.slice(0,2).map(z=>JSONObject(l2.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),  x.getRelationships("Commented_On_Debate").asScala.size,true,false,x.getProperty("d_id"),x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getRelationships("debate_readlater").asScala.map(_.getOtherNode(x)).toList.contains(user_node),x.getProperty("d_title"),x.getProperty("d_title_id"),"",x.getRelationships("Belongs_To_Subcategory_Debate").asScala.toList.filter(x => x.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Commented_On_Debate").asScala.size,x.getProperty("d_img_url").toString(),x.getProperty("d_content").toString(),x.getProperty("time_created").toString(),"","",false,"",false)).toMap)
					
		        }
            }
            
            
            // search suggestions ( events and articles)
            else
            {
                
                
                // if the search string contains only one word
                if(!ww.contains(" "))
	            {
                  art = article_content_index.query("article_content", w).iterator().asScala.toList.sortBy(-_.getProperty("time_created").toString().toInt).filter( x => x.getProperty("space").toString.toInt == 0).slice(0,6)
	              event = event_content_index.query("event_content", w).iterator().asScala.toList.sortBy(-_.getProperty("time_created").toString().toInt).filter( x => x.getProperty("space").toString.toInt == 0).slice(0,6)
	              townhall = t_content_index.query("t_content", w ).iterator().asScala.toList.sortBy(-_.getProperty("time_created").toString().toInt).filter( x => x.getProperty("space").toString.toInt == 0).slice(0,6)
				  petition = p_content_index.query("p_content", w ).iterator().asScala.toList.sortBy(-_.getProperty("time_created").toString().toInt).filter( x => x.getProperty("space").toString.toInt == 0).slice(0,6)
			      debate = d_content_index.query("d_content", w ).iterator().asScala.toList.sortBy(-_.getProperty("time_created").toString().toInt).filter( x => x.getProperty("space").toString.toInt == 0).slice(0,6)
				   
	            }
                // if the search string contains more than 1 word
	            else
	            {
	              art = article_content_index.query("article_content", w).iterator().asScala.toList.filter( x => x.getProperty("space").toString.toInt == 0).slice(0,6)
	              event = event_content_index.query("event_content", w).iterator().asScala.toList.filter( x => x.getProperty("space").toString.toInt == 0).slice(0,6)
	              townhall = t_content_index.query("t_content", w ).iterator().asScala.toList.filter( x => x.getProperty("space").toString.toInt == 0).slice(0,6)
				  petition = p_content_index.query("p_content", w ).iterator().asScala.toList.filter( x => x.getProperty("space").toString.toInt == 0).slice(0,6)
			      debate = d_content_index.query("d_content", w ).iterator().asScala.toList.filter( x => x.getProperty("space").toString.toInt == 0).slice(0,6)
				  
	            }
	            articles = art.map(x => JSONObject(a_list.zip(List(0,x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getOtherNode(x).getProperty("user_name"),x.getProperty("article_title"),x.getProperty("article_title_id"),x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Article").asScala.size,x.getProperty("article_featured_img").toString(),x.getProperty("article_summary").toString())).toMap))
                events = event.map(x => JSONObject(e_list.zip(List(1,x.getProperty("event_title"),x.getProperty("event_title_id"),x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getProperty("event_location"),x.getProperty("event_date_time"))).toMap))
                debates = debate.map(x => JSONObject(d_list.zip(List(3,x.getProperty("d_title"),x.getProperty("d_title_id"),x.getProperty("d_date"))).toMap))
                townhalls = townhall.map(x => JSONObject(t_list.zip(List(4,x.getProperty("t_title"),x.getProperty("t_title_id"),x.getProperty("t_content"),x.getProperty("t_date"))).toMap))
       
                for(ele <- petition)
                {
                      var small_list = List[Any]()
                      small_list :+= 2
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
			          petitions :+= JSONObject(p_list.zip(small_list).toMap)
		   
                }
                // mixing the result articles and events
	            var items1  = List[Any]()
	            var items2  = List[Any]()
	            items = intersperse(articles,events).slice(0,6)
	            
	            items1 = intersperse(debates,townhalls).slice(0,6)
	            items2 = intersperse(petitions,items1).slice(0,6)
                items = intersperse(items, items2)
                if(items.size > 5)
                {
                  items = items.slice(0,5)
                  val extra_list = List("ev")
                  val extra:Any = JSONObject(extra_list.zip(List(-1)).toMap)
                  items :+= extra
                  
                }
                
                
                
            }

          }
          
          
          
          
          
          
         JSONArray(items).toString()
    }
     
  }
  
  
  // This function is used to filter the most common words during search and suggestions
  def bummy(word: String): String = 
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
  
  def add_feed(
        item_type:String, // article id
        item_id: String
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
	    case "P" => "Petition"
	    case "D" => "Debate"
	    case "T" => "Townhall"
	    case "a" => "Article"
	    case "e" => "Event"
	    case "q" => "Quickpost"
	    case "c" => "Comment"
	    case "u" => "User"
	    case "h" => "Sub_category"
	    case "p" => "Petition"
	    case "d" => "Debate"
	    case "t" => "Townhall"
	    case "PO" => "Poll"
	    case "po" => "Poll"
	    
	    }
	    val i_type = matchTest(item_type)
	    val item_name = i_type.toLowerCase()
	    var ret = false
       var FeedIndex = getNodeIndex("feed").get
       var feed_node = FeedIndex.get("id","user_feed").getSingle()
       if(feed_node == null)
       {
         val t = (System.currentTimeMillis() /1000).toInt - (86400)
         feed_node = createNode(feed("user_feed","","","","","",t))  
         FeedIndex += (feed_node,"id","user_feed")
         
         feed_node.setProperty(item_name,item_id)
         ret = true
       }
       
       else
       {
         var feed_value = feed_node.getProperty(item_name).toString()
         if(feed_value.equals(""))
         {
           feed_node.setProperty(item_name,item_id)
         }
         else
         {
           var feed_list = feed_value.split(",").toList
           var new_feed = item_id.split(",").toList
           
           feed_value = (new_feed:::feed_list).distinct.mkString(",").stripPrefix(",").stripSuffix(",").trim
           feed_node.setProperty(item_name,feed_value)
           
         }
         ret = true
         
       }
     ret  
     }
    }

  // called when an article is pinned
  def pin_item(
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
  
  
  
  def exclusive_article(
        a_id:String, // article id
        cat: String
        ):Boolean=
    {
    
   
     withTx {
      
     implicit neo =>
       
       
       val CategoryIndex = getNodeIndex("category").get
       val ArticleIndex = getNodeIndex("article").get
       val art_node = ArticleIndex.get("id",a_id).getSingle()
       var ret_cat_list = List[String]()
       val cat_nodes = CategoryIndex.query( "name", "*" ).iterator().asScala.toList
       val tot_cat_list = cat_nodes.map( x => x.getProperty("name").toString())
       if(!cat.equals(""))
       {
         ret_cat_list = cat.split(",").toList.map(x => x.toLowerCase())
       }
       var ret = false
       if(art_node != null)
       {
           for(each <- cat_nodes)
           {
       
             if(ret_cat_list.contains(each.getProperty("name").toString()))
             {
               var exclusive_list = each.getProperty("exclusive").toString().split(",").toList
               if(!exclusive_list.contains(a_id))
               {
                 exclusive_list ::= a_id
                 each.setProperty("exclusive",exclusive_list.mkString(",").stripPrefix(",").stripSuffix(",").trim)
               }
             }
             else
             {
               var exclusive_list = each.getProperty("exclusive").toString().split(",").toList
               if(exclusive_list.contains(a_id))
               {
                 exclusive_list = exclusive_list.filterNot(x => x.equals(a_id))
                 each.setProperty("exclusive",exclusive_list.mkString(",").stripPrefix(",").stripSuffix(",").trim)
               }
             }
           }
       ret = true  
       }
       
       
       
       
     ret
     }
     
     
  }
  
  // returns all the categories to which an article is pinned to
  def pin_article_category(
        a_id:String // article id
        ):String=
    {
    
   
     withTx {
      
     implicit neo =>
       
       
       val CategoryIndex = getNodeIndex("category").get
       
       
       val cat_nodes = CategoryIndex.query( "name", "*" ).iterator().asScala.toList
       val cat_list = cat_nodes.map( x => x.getProperty("name").toString())
       
       var final_cat_list = List[String]()
       var ret = "Not pinned"
       for(each <- cat_list)
       {
         val cat_node = CategoryIndex.get("name",each).getSingle()
         if(cat_node != null)
         {
           // finding if the article is pinned to that category or not
           if(cat_node.hasProperty("pins"))
           {
             val pins = cat_node.getProperty("pins").toString().split(",").toList
             // adding the category to final_cat_list if the article is pinned 
             if(pins.contains(a_id))
             {
               final_cat_list ::= each
             }
           }
         }
       }
       
       
       
     ret = final_cat_list.mkString(",")
     ret
     }
     
     
  }
  
  def exclusive_article_category(
        a_id:String // article id
        ):String=
    {
    
   
     withTx {
      
     implicit neo =>
       
       
       val CategoryIndex = getNodeIndex("category").get
       val ArticleIndex = getNodeIndex("article").get
       val art_node = ArticleIndex.get("id",a_id).getSingle()
       val cat_nodes = CategoryIndex.query( "name", "*" ).iterator().asScala.toList
       val cat_list = cat_nodes.map( x => x.getProperty("name").toString())
       var final_cat_list = List[String]()
       var ret = "Not exclusive"
       for(each <- cat_list)
       {
         val cat_node = CategoryIndex.get("name",each).getSingle()
         if(cat_node != null)
         {
           if(cat_node.hasProperty("exclusive"))
           {
             val exclusive = cat_node.getProperty("exclusive").toString().split(",").toList
             if(exclusive.contains(a_id))
             {
               final_cat_list ::= each
             }
           }
         }
       }
       
       
       
     ret = final_cat_list.mkString(",")
     ret
     }
     
     
  }
  
  
   // Triggred when a user tries to report an Article/Event as spam
    def report_spam(
        item_type:String,  // Article/Event
        item_id:String, // article id/ event id
        user_name:String,  // unique user name
        time:Int
        ):Boolean=
    {
    
   
     withTx {
      
     implicit neo =>
       
       var i_type = "Article"
       if(item_type.equals("E"))
       {
         i_type = "Event"
       }
       val index_name = i_type.toLowerCase()
       val index = getNodeIndex(index_name).get
       val avg_wt_index = getNodeIndex("avg_weights").get
       val item_node = index.get("id",item_id).getSingle()
       val i_wt = item_node.getProperty("weight").toString().toInt
       val s_wt = item_node.getProperty("spam_weight").toString().toInt
       val nodeIndex = getNodeIndex("user").get
       val user_node = nodeIndex.get("id",user_name).getSingle()
       val u_wt = user_node.getProperty("weight").toString().toInt
       var ret = false
       if(user_node != null && item_node != null)
       {
         user_node.setProperty("last_seen",time)
         if(i_type.equals("Article") && !item_node.getRelationships("Spammed_Article").asScala.map(_.getOtherNode(item_node)).toList.contains(user_node))
         {
           val i_time = item_node.getProperty("time_created").toString().toInt
           val rel: org.neo4j.graphdb.Relationship = user_node --> "Spammed_Article" --> item_node <
           val rel_time = rel.setProperty("time", time)
           
           var cats = item_node.getRelationships("Belongs_To_Category").asScala.map(_.getOtherNode(item_node)).toList
           
           if(!item_node.hasRelationship("User_Spammed_Of_Category",Direction.OUTGOING))
           {
	           for(each <- cats)
	           {
	             val rel: org.neo4j.graphdb.Relationship = item_node --> "User_Spammed_Of_Category" --> each <
	             val rel_time = rel.setProperty("time", time)
	           }
           }
           val age = ((time - i_time)/3600) + 1
           item_node.setProperty("spam_weight", u_wt/age)
           val arts = index.query( "id", "*" ).iterator().asScala.toList
           val as = arts.filter(x => x.hasRelationship("User_Spammed_Of_Category",Direction.OUTGOING)).map(x => x.getProperty("spam_weight").toString().toInt)
		      if(as.size != 0)
		      {
		          
		    	  val as_avg = as.sum / as.size
		    	  
		    	  val as_node = avg_wt_index.get("id","article_spam").getSingle()
		    	  if(as_node != null)
		    	  {
		    	    if(!as_node.hasProperty("top_30_users"))
		    	    {
		    	      as_node.setProperty("top_30_users","")
		    	    }
		    	    as_node.setProperty("average",as_avg)
		    	    as_node.setProperty("total",as.size)
		    	    as_node.setProperty("top",as.max)
		    	  }
		    	  else
		    	  {
		    	    val as_node = createNode(avg_wt("article_spam",as_avg,as.size,as.max,0,0,0,"",0))
		    	    avg_wt_index += (as_node,"id","article_spam")
		    	  }
		    	  
		    	  
		      }
		      
		      else if(avg_wt_index.get("id","article_spam").getSingle() == null)
		      {
		    	    val as_node = createNode(avg_wt("article_spam",0,0,0,0,0,0,"",0))
		    	    avg_wt_index += (as_node,"id","article_spam")
		      }
           ret = true 
         }
         else if(i_type.equals("Event") && !item_node.getRelationships("Spammed_Event").asScala.map(_.getOtherNode(item_node)).toList.contains(user_node))
         {
           val i_time = item_node.getProperty("time_created").toString().toInt
           val rel: org.neo4j.graphdb.Relationship = user_node --> "Spammed_Event" --> item_node <
           val rel_time = rel.setProperty("time", time)
           
           var cats = item_node.getRelationships("Belongs_To_Event_Category").asScala.map(_.getOtherNode(item_node)).toList

           if(!item_node.hasRelationship("User_Spammed_Of_Event_Category",Direction.OUTGOING))
           {
	           for(each <- cats)
	           {
	             val rel: org.neo4j.graphdb.Relationship = item_node --> "User_Spammed_Of_Event_Category" --> each <
	             val rel_time = rel.setProperty("time", time)
	           }
           }
           
           val age = ((time - i_time)/3600) + 1
           item_node.setProperty("spam_weight", u_wt/age)
           val events = index.query( "id", "*" ).iterator().asScala.toList
           val es = events.filter(x => x.hasRelationship("User_Spammed_Of_Event_Category",Direction.OUTGOING)).map(x => x.getProperty("spam_weight").toString().toInt)
		      if(es.size != 0)
		      {
		          
		    	  val es_avg = es.sum / es.size
		    	  
		    	  val es_node = avg_wt_index.get("id","event_spam").getSingle()
		    	  if(es_node != null)
		    	  {
		    	    if(!es_node.hasProperty("top_30_users"))
		    	    {
		    	      es_node.setProperty("top_30_users","")
		    	    }
		    	    es_node.setProperty("average",es_avg)
		    	    es_node.setProperty("total",es.size)
		    	    es_node.setProperty("top",es.max)
		    	  }
		    	  else
		    	  {
		    	    val es_node = createNode(avg_wt("event_spam",es_avg,es.size,es.max,0,0,0,"",0))
		    	    avg_wt_index += (es_node,"id","event_spam")
		    	  }
		    	  
		    	  
		      }
		      
		      else if(avg_wt_index.get("id","event_spam").getSingle() == null)
		      {
		    	    val es_node = createNode(avg_wt("event_spam",0,0,0,0,0,0,"",0))
		    	    avg_wt_index += (es_node,"id","event_spam")
		      }
           ret = true 
         }
         
         
       }
       
      ret
     }
     
     
  }
    
    
    
 // This function returns all the unreviewed articles in a given category   
  def get_articles_spammed(
      category:String,  
      cnt:Int,  // No of articles required
      prev_cnt:Int  // No of articles sent
      ) :String =
  {
    
    
    
     
     var list  = List[Any]()

     val nodeIndex = getNodeIndex("category").get
     val node = nodeIndex.get("name",category).getSingle()
     val avg_wt_index = getNodeIndex("avg_weights").get
     val avg_wt_node = avg_wt_index.get("id","article").getSingle()
     if(node != null && avg_wt_node != null)
     {
	     val avg_wt = avg_wt_node.getProperty("average").toString().toInt
	     if(node != null) //new articles first
	     {
	
	       val articles = node.getRelationships("User_Spammed_Of_Category").asScala.map(_.getOtherNode(node)).toList.filter(x => (x.getProperty("spam_weight").toString().toInt >= avg_wt)).sortBy(-_.getProperty("spam_weight").toString.toInt).slice(prev_cnt,(prev_cnt+cnt))
	       if(articles != null)
	       {
	         
	           val l = List("P_Id","P_Author","P_Author_FullName","P_Title","P_Title_ID","P_Category","P_SubCategory","P_Num_Comments","P_Feature_Image","P_Smry")
		       list = articles.map(x => JSONObject(l.zip(List(x.getProperty("article_id"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("user_name"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString()+ " " + x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),x.getProperty("article_title"),x.getProperty("article_title_id"),x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0),x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(","),x.getRelationships("Comment_To_Article").asScala.size,x.getProperty("article_featured_img").toString(),x.getProperty("article_summary").toString())).toMap))
            
           }    
	
	     }
     }
     JSONArray(list).toString()

  
  }
  
  // Triggered when a moderator reviews an article
  def review_article(
      article_id: String,  // unique article id
      user_name : String,  // unique user name( moderator user name)
      result: Int, // 1 if article is okay , 0 is article is not okay
      time: Int
      ):String=
  {
    
    withTx {
    implicit neo =>
      val ArticleIndex = getNodeIndex("article").get
      val article_node = ArticleIndex.get("id",article_id).getSingle()
      val UserIndex = getNodeIndex("user").get
      val user_node = UserIndex.get("id",user_name).getSingle()
      var ret = ""
      if(article_node != null && user_node != null)
      {
        if(!article_node.hasRelationship("Reviewed_By",Direction.OUTGOING))
	    {
	        if(result == 1)
	        {
	          val rels = article_node.getRelationships("User_Spammed_Of_Category","System_Spammed_Of_Category").asScala
	          for(each <- rels)
	          {
	            each.delete()
	          }
	
	          article_node.setProperty("spam_weight",0)
	          val rel: org.neo4j.graphdb.Relationship = article_node --> "Reviewed_By" --> user_node <
	          val rel_time = rel.setProperty("time", time)
	          ret = "Accepted"
	        }
	        else
	        {
	          ret = "Rejected"
	          val author_node = article_node.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode()
	          val author_spam_wt = author_node.getProperty("spam_weight").toString().toInt
	          author_node.setProperty("spam_weight",author_spam_wt + 1)
	          delete_article(article_id)
	          
	          
	        }
	    }
        
        else
        {
          val mod_node = article_node.getSingleRelationship("Reviewed_By",Direction.OUTGOING).getEndNode()
          ret = "No_Action" + "," + mod_node.getProperty("user_name")
        }
      
      }
    ret 
    }
  }
  
  
def get_trends(cat: String):String =
{
     
    
      val trend_index = getNodeIndex("trending").get
      var ret = ""
     
      val trend_node = trend_index.get("id","hash").getSingle()
      if(trend_node != null)
      {
        if(trend_node.hasProperty(cat.toLowerCase()))
        {
          ret = trend_node.getProperty(cat).toString()
        }
        
        
      }
      
      
    ret  
    
  }
  
  def calc_hash_trends()
  {
    
    withTx {
    implicit neo =>
      

           var ret_pol = ""
           var pol_hash_nodes = List[org.neo4j.graphdb.Node]()
           var sorted_pol = List[org.neo4j.graphdb.Node]()
           val trend_index = getNodeIndex("trending").get
           val hash_index = getNodeIndex("sub_category").get
           val CategoryIndex = getNodeIndex("category").get
           //val hash_nodes = hash_index.query("name", "*" ).iterator().asScala.toList
           val t2 = (System.currentTimeMillis()/1000).toInt - (14400)
           val t3 = (System.currentTimeMillis()/1000).toInt - (86400 * 2)
           val cat_nodes = CategoryIndex.query( "name", "*" ).iterator().asScala.toList
           var t_node:org.neo4j.graphdb.Node = null
           if(trend_index.get("id","hash").getSingle() != null)
	       {
	             t_node = trend_index.get("id","hash").getSingle()
	             if(t_node.hasProperty("value"))
	             {
	               t_node.removeProperty("value")
	             }
	       }
	       else
	       {
	             t_node = createNode(trending("hash"))
	             trend_index += (t_node,"id","hash")
	       }

           if(cat_nodes.size > 0)
           {
             for(each <- cat_nodes)
             {
               pol_hash_nodes = each.getRelationships("Belongs_To_Category","Belongs_To_Event_Category").asScala.map(_.getOtherNode(each)).toList.map( x => x.getRelationships("Belongs_To_Subcategory_Article","Belongs_To_Subcategory_Event").asScala.toList.filter(y => y.hasProperty("main")).map(_.getOtherNode(x))).flatten.distinct
               sorted_pol = pol_hash_nodes.sortBy(x => -(x.getRelationships("Belongs_To_Subcategory_Article","Belongs_To_Subcategory_Event").asScala.map(_.getOtherNode(x)).toList.distinct.filter(z => (z.getProperty("time_created").toString().toInt > t3) ).map(   y => y.getRelationships("article_markfav","article_voteup","Is_Attending","Comment_To_Article","Comment_To_Event").asScala.toList.filter(k => (k.getProperty("time").toString().toInt > t2) ).map( l => l.getProperty("in_weight").toString.toInt   ).sum  ).sum + x.getRelationships("Belongs_To_Subcategory_Article","Belongs_To_Subcategory_Event","Tag_Of_Article","Tag_Of_Event").asScala.map(_.getOtherNode(x)).toList.distinct.filter(a => a.getProperty("time_created").toString().toInt > t3).map( b => b.getProperty("views").toString().toInt).sum ))
	           ret_pol = JSONArray(sorted_pol.slice(0,10).map(x => x.getProperty("name"))).toString()
	           t_node.setProperty(each.getProperty("name").toString(),ret_pol)
             }
           }
       
           
	           
    }
  println("Hash trends updated")  
  }  
  
  def get_itemfeed(
      feed_type: String
      ):String=
  {
    
    withTx {
    implicit neo =>
      
           var ret = ""
           val t = (System.currentTimeMillis()/1000).toInt - (86400)
           
           if(feed_type.toLowerCase().equals("a"))
           {
               val art_index = getNodeIndex("article").get
	           var main_list = List[Any]()
	           val l = List("P_Id","P_Author","P_Title","P_Title_ID","P_Feature_Image","P_Smry","P_Num_Comments","P_Num_Votes","P_Com_Users","P_Voted_Users")
			   var arts = art_index.query( "id", "*" ).iterator().asScala.toList
			   val arts1 = arts.filter(x => x.getRelationships("article_voteup").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t).size > 0)
			   val arts2 = arts.filter(x => x.getRelationships("Comment_To_Article").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t).size > 0)
			   arts = (arts1:::arts2).distinct
			   
			   for(ele <- arts)
		       {
				   			  val auth = ele.getSingleRelationship("Article_Written_By",Direction.OUTGOING)
		                      var small_list = List[Any]()
					          small_list :+= ele.getProperty("article_id")
					          small_list :+= auth.getProperty("user_name").toString() + ":" + auth.getProperty("first_name").toString() + " " + auth.getProperty("last_name").toString()
					          small_list :+= ele.getProperty("article_title")
					          small_list :+= ele.getProperty("article_title_id")
					          small_list :+= ele.getProperty("article_featured_img")
					          small_list :+= ele.getProperty("article_summary")
					          small_list :+= ele.getRelationships("Comment_To_Article").asScala.toList.size
					          small_list :+= ele.getRelationships("article_voteup").asScala.toList.size
					          
					          var com_users = ""
					          if(arts2.contains(ele))
					          {
					              var user_com_list = List[String]()
						          var com_list = ele.getRelationships("Comment_To_Article").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t).sortBy(x => -x.getProperty("time").toString().toInt).map(_.getOtherNode(ele).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.slice(0,2)
				                  if(com_list.size > 0 )
					              {
					                    for(each <- com_list)
							            {
							              user_com_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
							            }
					              }
					              com_users = user_com_list.mkString(",")
					          }
					          
					          small_list :+= com_users
					          
					          var voted_users = ""
					          if(arts1.contains(ele))
					          {
					              var user_voted_list = List[String]()
						          var voted_list = ele.getRelationships("article_voteup").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t).sortBy(x => -x.getProperty("time").toString().toInt).map(_.getOtherNode(ele)).slice(0,2)
				                  if(voted_list.size > 0 )
					              {
					                    for(each <- voted_list)
							            {
							              user_voted_list :+= each.getProperty("user_name").toString() + ":" + each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
							            }
					              }
					              voted_users = user_voted_list.mkString(",")
					          }
					          
					          small_list :+= voted_users
					          main_list :+= JSONObject(l.zip(small_list).toMap)
		       }
		       ret = JSONArray(main_list).toString()
           }
           
           else
           {
               val user_index = getNodeIndex("user").get
	           var main_list = List[Any]()
	           val l = List("P_Id","P_Author","P_Title","P_Title_ID","P_Feature_Image","P_Smry","P_Num_Comments","P_Num_Votes","P_Com_Users","P_Voted_Users")
			   var users = user_index.query( "id", "*" ).iterator().asScala.toList
			   var big_map  = scala.collection.mutable.Map[String,Any]()
			   for(ele <- users)
		       {
			     val u_name = ele.getProperty("user_name").toString()
			     val user_details = ele.getProperty("user_name").toString() + ":" + ele.getProperty("first_name").toString() + " " + ele.getProperty("last_name").toString()
	         
			     val follows = ele.getRelationships("Follows",Direction.OUTGOING).asScala.map(_.getOtherNode(ele)).toList
		         
			     var list  = List[Any]()
			     val com_rels = ele.getRelationships("Comment_Written_By").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t)
				 val vote_rels =  ele.getRelationships("article_voteup").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t)
			     val art_rels =  ele.getRelationships("Article_Written_By").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t)
				 val event_rels =  ele.getRelationships("Event_Created_By").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t)
				 val p_rels =  ele.getRelationships("Petition_Written_By").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t)
				 val p_sign_rels =  ele.getRelationships("Signed_Petition").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t)
				 val event_attend_rels =  ele.getRelationships("Is_Attending").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t)
				 
				 var art_nodes =  List[org.neo4j.graphdb.Node]()
				 var event_nodes =  List[org.neo4j.graphdb.Node]()
				 var petition_nodes =  List[org.neo4j.graphdb.Node]()
				 
				 val rels = (vote_rels:::art_rels:::event_rels:::p_rels:::p_sign_rels:::event_attend_rels).distinct.sortBy(-_.getProperty("time").toString().toInt)
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
					
			        case "Comment_Written_By" => 
	          
	              val com_node = x.getStartNode()
                  if(com_node.getSingleRelationship("Comment_To_Article",Direction.OUTGOING) != null)
		          {
		            
                    val art_node = com_node.getSingleRelationship("Comment_To_Article",Direction.OUTGOING).getOtherNode(com_node)
			        if(!art_nodes.contains(art_node))  
			        {
			            art_nodes :+= art_node
			            if(com_node.hasRelationship("Comment_To_Comment",Direction.OUTGOING))
			            {
	//		                  println("hey")
				              val main_com = com_node.getSingleRelationship("Comment_To_Comment",Direction.OUTGOING).getOtherNode(com_node)
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
				              
				                if(ele != maincom_auth_node)
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
				                
				                else if(ele == maincom_auth_node)
				                {
				                  com_list ::= user_details
					              com_list = com_list.distinct
				                }
				              list :+= JSONObject(l.zip(List(true,m,"A","CC",url,x.getProperty("time"),maincom_auth_node.getProperty("user_name"),maincom_auth_node.getProperty("first_name") + " " + maincom_auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), art_node.getProperty("article_featured_img"), "" , com_list.mkString(",") , art_node.getRelationships("Comment_To_Article").asScala.size,art_node.getRelationships("article_voteup").asScala.size , art_node.getProperty("article_id"))).toMap)
					          
			              
			            }
			            else
			            {
	//		                  println("heyy")
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
				              
				                if(ele != auth_node)
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
				                
				                else if(ele == auth_node)
				                {
				                  com_list ::= user_details
					              com_list = com_list.distinct
				                }
				              list :+= JSONObject(l.zip(List(true,m,"A","CM",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), art_node.getProperty("article_featured_img"), "" , com_list.mkString(",") , art_node.getRelationships("Comment_To_Article").asScala.size,art_node.getRelationships("article_voteup").asScala.size , art_node.getProperty("article_id"))).toMap)
					          
				          
			            }
		             }
		          }
		          else if(com_node.getSingleRelationship("Comment_To_Event",Direction.OUTGOING) != null)
		          {
		            val event_node = com_node.getSingleRelationship("Comment_To_Event",Direction.OUTGOING).getOtherNode(com_node)
			        if(!event_nodes.contains(event_node))  
			        {
				        event_nodes :+= event_node      
			            if(com_node.hasRelationship("Comment_To_Comment",Direction.OUTGOING))
			            {
			                  val main_com = com_node.getSingleRelationship("Comment_To_Comment",Direction.OUTGOING).getOtherNode(com_node)
			              
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
			                  
			                  
			                    if(ele != maincom_auth_node)
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
			                    
			                    else if(ele == maincom_auth_node )
				                {
				                  com_list ::= user_details
					              com_list = com_list.distinct
				                }
				              
				              list :+= JSONObject(l.zip(List(true,m,"E","CC",url,x.getProperty("time"),maincom_auth_node.getProperty("user_name"),maincom_auth_node.getProperty("first_name") + " " + maincom_auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "" , com_list.mkString(","), event_node.getRelationships("Comment_To_Event").asScala.size, 0, event_node.getProperty("event_id"))).toMap)
							  
			              
			            }
			            else
			            {
			              
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
				              
				              
				                if(ele != auth_node)
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
				                
				                else if(ele == auth_node )
				                {
				                  com_list ::= user_details
					              com_list = com_list.distinct
				                }
				              
				              list :+= JSONObject(l.zip(List(true,m,"E","CM",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "" , com_list.mkString(","), event_node.getRelationships("Comment_To_Event").asScala.size, 0, event_node.getProperty("event_id"))).toMap)
							  
				          
			            }
			        }
		            
		      }
	              
		          else
		          {
		            
		            val p_node = com_node.getSingleRelationship("Comment_To_Petition",Direction.OUTGOING).getOtherNode(com_node)
			        if(!petition_nodes.contains(p_node))  
			        {
					    petition_nodes :+= p_node       
			            if(com_node.hasRelationship("Comment_To_Comment",Direction.OUTGOING))
			            {
			                  val main_com = com_node.getSingleRelationship("Comment_To_Comment",Direction.OUTGOING).getOtherNode(com_node)
			              
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
			                  
			                  
			                    if(ele != maincom_auth_node)
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
			                    
			                    else if(ele == maincom_auth_node )
				                {
				                  com_list ::= user_details
					              com_list = com_list.distinct
				                }
				              
				              list :+= JSONObject(l.zip(List(true,m,"P","CC",url,x.getProperty("time"),maincom_auth_node.getProperty("user_name"),maincom_auth_node.getProperty("first_name") + " " + maincom_auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "" , com_list.mkString(","), p_node.getRelationships("Comment_To_Petition").asScala.size, p_node.getProperty("p_target").toString().toInt - p_node.getProperty("p_count").toString().toInt, p_node.getProperty("p_id"))).toMap)
							  
			              
			            }
			            else
			            {
			              
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
				              
				              
				                if(ele != auth_node)
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
				                
				                else if(ele == auth_node )
				                {
				                  com_list ::= user_details
					              com_list = com_list.distinct
				                }
				              
				              list :+= JSONObject(l.zip(List(true,m,"P","CM",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "" , com_list.mkString(","), p_node.getRelationships("Comment_To_Petition").asScala.size, p_node.getProperty("p_target").toString().toInt - p_node.getProperty("p_count").toString().toInt, p_node.getProperty("p_id"))).toMap)
							  
				          
			            }
			        }
		            
		      }
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
		                
		                
		                
		                if( ele != auth_node)
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
		                
		                else if(ele == auth_node )
		                {
		                  voted_list ::= user_details
			              voted_list = voted_list.distinct
		                }
		                
			            var map  = scala.collection.mutable.Map[String,Any]()
			            map("ttl") = art_node.getProperty("article_title")
			            val m = JSONObject(map.toMap).toString()
			            val url = "/" + art_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(art_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + art_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(art_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + art_node.getProperty("article_title_id")
			            list :+= JSONObject(l.zip(List(true,m,"A","V",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), art_node.getProperty("article_featured_img"), "", voted_list.mkString(","), art_node.getRelationships("Comment_To_Article").asScala.size,art_node.getRelationships("article_voteup").asScala.size , art_node.getProperty("article_id"))).toMap)
		        
			        case "Event_Created_By" =>
		          
		            val event_node = x.getStartNode()
		            val auth_node = x.getEndNode()
		            var map  = scala.collection.mutable.Map[String,Any]()
		            map("ttl") = event_node.getProperty("event_title")
		            map("date") = event_node.getProperty("event_date_time")
		            map("loc") = event_node.getProperty("event_location")
		            val m = JSONObject(map.toMap).toString()
		            val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id")
		            list :+= JSONObject(l.zip(List(true,m,"E","C",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", "", event_node.getRelationships("Comment_To_Event").asScala.size, 0, event_node.getProperty("event_id"))).toMap)
				
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
			            
			            if(ele != auth_node)
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
			            
			            else if(ele == auth_node)
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
			            list :+= JSONObject(l.zip(List(true,m,"E","A",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", attend_list.mkString(","), event_node.getRelationships("Comment_To_Event").asScala.size, 0, event_node.getProperty("event_id"))).toMap)
				    
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
			            
			            if(ele != auth_node)
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
			            
			            else if(ele == auth_node)
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
				   
			        case _ => ""
			     }
			     
		         // slicing the relations based on count asked
			     for(each <- rels)
			     {
			       matchTest(each)
			     }
			     
			     big_map(u_name) = JSONArray(list).toString()
				   			  
		       }
		       ret = JSONObject(big_map.toMap).toString()
           }
           
           
           
             
    ret         
    }
  }
  
  def get_user_spaces(user_name: String, relation_type: String):String = {
    
    withTx {
      implicit neo =>
        var ret = ""        
        var spaceIndex = getNodeIndex("space").get
        var allSpaces = spaceIndex.query( "id", "*" ).iterator().asScala.toList
        var relatedSpaces = List[Any]()
        
        var userIndex = getNodeIndex("user").get
        var userNode = userIndex.get("id",user_name).getSingle();
        val a_list = List("Comment_Count_Unique","ev","v_users","votes","Commented_Users","Comment_Count","Is_Neo4j","P_Pin","P_Id","P_Author","P_Author_FullName","P_IsMarkedReadLater","P_Title","P_Title_ID","P_Category","P_SubCategory","P_Num_Comments","P_Feature_Image","P_Smry", "P_TimeCreated", "P_EventLocation", "P_EventStartTime", "P_EventAttendStatus", "P_SignsRequired", "P_PetitionSignStatus", "Space_Title", "Space_TitleId")
		val l2 = List("FN","UN")
        val l3 = List("Name","UName")
        
        //val l1 = List("space_id","space_title_id","space_title","space_tagline","space_fut_image","space_time_created","is_closed")
        var user_related_spaces = List[org.neo4j.graphdb.Node]()
        if(userNode != null)
        {
          if(relation_type.equalsIgnoreCase("f"))
        	  user_related_spaces = userNode.getRelationships("Space_Followed_By",Direction.INCOMING).asScala.map(_.getOtherNode(userNode)).toList
          else if(relation_type.equalsIgnoreCase("c"))
        	  user_related_spaces = userNode.getRelationships("Space_Created_By",Direction.INCOMING).asScala.map(_.getOtherNode(userNode)).toList
          
          
          /*for(eachSpace <- user_related_spaces)
          relatedSpaces :+= JSONObject(
        		  					    l1.zip(
        		  					        List(
        		  					            eachSpace.getProperty("space_id").toString(),
        		  					            eachSpace.getProperty("space_title_id").toString(),
        		  					            eachSpace.getProperty("space_title").toString(),
        		  					            eachSpace.getProperty("space_tagline").toString(),
        		  					            eachSpace.getProperty("space_featured_img").toString(),
        		  					            eachSpace.getProperty("time_created").toString(),
        		  					            eachSpace.getProperty("closed").toString()
        		  					            )
        		  					            ).toMap
        		  					            ) */
        
        	  
        //for(eachSpace <- user_related_spaces)
          relatedSpaces = user_related_spaces.map(eachSpace => JSONObject(
        		  					    a_list.zip(
        		  					        List(
        		  					            0, //Comment_Count
        		  					            5, //ev
        		  					            JSONArray(eachSpace.getRelationships("Space_Followed_By").asScala.toList.sortBy(-_.getProperty("time").toString().toInt).slice(0,2).map(y=>y.getOtherNode(eachSpace)).map(z=>JSONObject(l3.zip(List((z.getProperty("first_name").toString()+" "+z.getProperty("last_name").toString()),z.getProperty("user_name").toString())).toMap))),   //v_uesrs - list of voted users i.e., list of followers
        		  					            eachSpace.getRelationships("Space_Followed_By").asScala.toList.size-eachSpace.getRelationships("Space_Followed_By").asScala.toList.slice(0,2).size, //number of votes i.e., number of followers
        		  					            "",	//commented users
        		  					            0,	//number of comments
        		  					            true,	//Is_Neo4j
        		  					            false,	//P_Pin is space is pinned or not
        		  					            eachSpace.getProperty("space_id").toString(),	//p_id	space id
        		  					            eachSpace.getSingleRelationship("Space_Created_By", Direction.OUTGOING).getEndNode().getProperty("user_name").toString(),//space author user name
        		  					            eachSpace.getSingleRelationship("Space_Created_By", Direction.OUTGOING).getEndNode().getProperty("first_name").toString() + " " + eachSpace.getSingleRelationship("Space_Created_By", Direction.OUTGOING).getEndNode().getProperty("last_name").toString(),//space author first name + last name
        		  					            false, //Is_Marked_read later
        		  					            eachSpace.getProperty("space_title").toString(), //P-Title
        		  					            eachSpace.getProperty("space_title_id").toString(), //P_Title_Id
        		  					            "",	//P_Category
        		  					            "",	//P_Sub_Category i.e., hash_tags
        		  					            "", //P_Num_Comments
        		  					            eachSpace.getProperty("space_featured_img").toString(), //P_Feature_Image
        		  					            eachSpace.getProperty("space_tagline").toString(),	//P_Smry
        		  					            eachSpace.getProperty("time_created").toString(),
        		  					            "",	//P_Event_Location
        		  					            "",	//P_Event_start_Time
        		  					            false,	//P_Event_Attend_Status
        		  					            "",	//P_Signs_Required
        		  					            false, //P_Signs_Status
        		  					            eachSpace.getProperty("space_title").toString(), //Space_Title
        		  					            eachSpace.getProperty("space_title_id").toString() //Space_Title_Id
        		  					            )
        		  					            ).toMap
        		  					            ))
        }
        
	    ret = JSONArray(relatedSpaces).toString()
                
	    ret 
    }
  }
  
  
  
  
  def get_followersfeed(
      feed_type: String
      ):String=
  {
    
    withTx {
    implicit neo =>
      
           var ret = ""
           val t = (System.currentTimeMillis()/1000).toInt - (86400)
           val user_index = getNodeIndex("user").get
           var main_list = List[Any]()
           val l = List("P_Id","P_Author","P_Title","P_Title_ID","P_Feature_Image","P_Smry","P_Num_Comments","P_Num_Votes","P_Com_Users","P_Voted_Users")
		   var users = user_index.query( "id", "*" ).iterator().asScala.toList
		   var big_map  = scala.collection.mutable.Map[String,Any]()
		   for(ele <- users)
	       {
		     val u_name = ele.getProperty("user_name").toString()
		     val user_details = ele.getProperty("user_name").toString() + ":" + ele.getProperty("first_name").toString() + " " + ele.getProperty("last_name").toString()
         
		     val follows = ele.getRelationships("Follows",Direction.OUTGOING).asScala.map(_.getOtherNode(ele)).toList
	         
		     var list  = List[Any]()
//		     val com_rels = ele.getRelationships("Comment_Written_By").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t)
			 val vote_rels =  ele.getRelationships("article_voteup").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t)
		     val art_rels =  ele.getRelationships("Article_Written_By").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t)
			 val event_rels =  ele.getRelationships("Event_Created_By").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t)
			 val p_rels =  ele.getRelationships("Petition_Written_By").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t)
			 val p_sign_rels =  ele.getRelationships("Signed_Petition").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t)
			 val event_attend_rels =  ele.getRelationships("Is_Attending").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t)
			 
			 
			 val rels = (vote_rels:::art_rels:::event_rels:::p_rels:::p_sign_rels:::event_attend_rels).distinct.sortBy(-_.getProperty("time").toString().toInt)
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
	                
	                
	                
	                if( ele != auth_node)
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
	                
	                else if(ele == auth_node )
	                {
	                  voted_list ::= user_details
		              voted_list = voted_list.distinct
	                }
	                
		            var map  = scala.collection.mutable.Map[String,Any]()
		            map("ttl") = art_node.getProperty("article_title")
		            val m = JSONObject(map.toMap).toString()
		            val url = "/" + art_node.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(art_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + art_node.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(art_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + art_node.getProperty("article_title_id")
		            list :+= JSONObject(l.zip(List(true,m,"A","V",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), art_node.getProperty("article_featured_img"), "", voted_list.mkString(","), art_node.getRelationships("Comment_To_Article").asScala.size,art_node.getRelationships("article_voteup").asScala.size , art_node.getProperty("article_id"))).toMap)
	        
		        case "Event_Created_By" =>
	          
	            val event_node = x.getStartNode()
	            val auth_node = x.getEndNode()
	            var map  = scala.collection.mutable.Map[String,Any]()
	            map("ttl") = event_node.getProperty("event_title")
	            map("date") = event_node.getProperty("event_date_time")
	            map("loc") = event_node.getProperty("event_location")
	            val m = JSONObject(map.toMap).toString()
	            val url = "/Events/" + event_node.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(event_node)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + event_node.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(event_node).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + event_node.getProperty("event_title_id")
	            list :+= JSONObject(l.zip(List(true,m,"E","C",url,x.getProperty("time"),auth_node.getProperty("user_name"),auth_node.getProperty("first_name") + " " + auth_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", "", event_node.getRelationships("Comment_To_Event").asScala.size, 0, event_node.getProperty("event_id"))).toMap)
			
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
		            
		            if(ele != auth_node)
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
		            
		            else if(ele == auth_node)
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
		            list :+= JSONObject(l.zip(List(true,m,"E","A",url,x.getProperty("time"),user_node.getProperty("user_name"),user_node.getProperty("first_name") + " " + user_node.getProperty("last_name"), auth_node.getProperty("user_name"), "", "", attend_list.mkString(","), event_node.getRelationships("Comment_To_Event").asScala.size, 0, event_node.getProperty("event_id"))).toMap)
			    
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
		            
		            if(ele != auth_node)
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
		            
		            else if(ele == auth_node)
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
			   
		        case _ => ""
		     }
		     
	         // slicing the relations based on count asked
		     for(each <- rels)
		     {
		       matchTest(each)
		     }
		     
		     big_map(u_name) = JSONArray(list).toString()
			   			  
	       }
	       ret = JSONObject(big_map.toMap).toString()
           
             
    ret         
    }
  }
  
  def get_userfeed(
      feed_type: String
      ):String=
  {
    
    withTx {
    implicit neo =>
      
           var ret = ""
           val cur_time = (System.currentTimeMillis()/1000).toInt
           val user_index = getNodeIndex("user").get
           val user_tiles_index = getNodeIndex("user_tiles").get
           var big_list = List[Any]()
           val l = List("P_Id","P_Author","P_Author_FullName","P_Title","P_Title_ID","P_Category","P_SubCategory","P_Num_Comments","P_Feature_Image","P_Smry")
		   
           if(feed_type.toLowerCase().equals("d"))
           {
             //println("one")
             val t = (System.currentTimeMillis()/1000).toInt - (86400)
             
             val users = user_index.query( "id", "*" ).iterator().asScala.toList
             
             var map3 = scala.collection.mutable.Map[String,Any]()
             for(each <- users)
             {
               val subsc_list = each.getProperty("feed_subscription").toString().split(",").toList
               if(subsc_list.contains("D"))
               {
                 
                 val arts = each.getRelationships("Article_Written_By").asScala.toList.map( x => x.getOtherNode(each)).filter(x => x.getRelationships("Comment_To_Article").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t).size > 0  ||  x.getRelationships("article_voteup").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t).size > 0  )
                 val pets = each.getRelationships("Petition_Written_By").asScala.toList.map( x => x.getOtherNode(each)).filter(x => x.getRelationships("Comment_To_Petition").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t).size > 0  ||  x.getRelationships("Signed_Petition").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t).size > 0  )
                 val l = List("type","ttl","url","vote","com")
                 val l1 = List("type","ttl","url","sign","com")
	             var list = List[Any]()
	             if(arts.size > 0)
	             {
	                 
	                 var in_list = List[Any]()
	                 in_list = arts.map(x => JSONObject(l.zip(List( "A", x.getProperty("article_title").toString() , ("/" + x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + x.getProperty("article_title_id")), x.getRelationships("Comment_To_Article").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t).map(_.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.map( x => x.getProperty("first_name").toString() + " " + x.getProperty("last_name").toString()).mkString(",")   , x.getRelationships("article_voteup").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t).map(_.getOtherNode(x)).distinct.map( x => x.getProperty("first_name").toString() + " " + x.getProperty("last_name").toString()).mkString(",") )).toMap))
	                 list = list:::in_list
	             }
                 
                 if(pets.size > 0)
	             {
	                 
	                 var in_list = List[Any]()
	                 in_list = pets.map(x => JSONObject(l.zip(List( "P", x.getProperty("p_title").toString() , ("/petitions/" + x.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + x.getProperty("p_title_id")), x.getRelationships("Comment_To_Petition").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t).map(_.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.map( x => x.getProperty("first_name").toString() + " " + x.getProperty("last_name").toString()).mkString(",")   , x.getRelationships("Signed_Petition").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t).map(_.getOtherNode(x)).distinct.map( x => x.getProperty("first_name").toString() + " " + x.getProperty("last_name").toString()).mkString(",") )).toMap))
	                 list = list:::in_list
	             }
                 
                 if(list.size > 0)
                 {
		             val u_name = each.getProperty("user_name").toString()
		             map3(u_name) = JSONArray(list)
                 }
               }
             }
             
             
             
             
             ret = JSONObject(map3.toMap).toString()

             
             
           }
           
           if(feed_type.toLowerCase().equals("w"))
           {
             //println("one")
             val t = (System.currentTimeMillis()/1000).toInt - (86400*7)
             
             val users = user_index.query( "id", "*" ).iterator().asScala.toList
             
             var map3 = scala.collection.mutable.Map[String,Any]()
             for(each <- users)
             {
               val subsc_list = each.getProperty("feed_subscription").toString().split(",").toList
               if(subsc_list.contains("D"))
               {
                 
                 val arts = each.getRelationships("Article_Written_By").asScala.toList.map( x => x.getOtherNode(each)).filter(x => x.getRelationships("Comment_To_Article").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t).size > 0  ||  x.getRelationships("article_voteup").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t).size > 0  )
                 val pets = each.getRelationships("Petition_Written_By").asScala.toList.map( x => x.getOtherNode(each)).filter(x => x.getRelationships("Comment_To_Petition").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t).size > 0  ||  x.getRelationships("Signed_Petition").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t).size > 0  )
                 val l = List("type","ttl","url","vote","com")
                 val l1 = List("type","ttl","url","sign","com")
	             var list = List[Any]()
	             if(arts.size > 0)
	             {
	                 
	                 var in_list = List[Any]()
	                 in_list = arts.map(x => JSONObject(l.zip(List( "A", x.getProperty("article_title").toString() , ("/" + x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + x.getProperty("article_title_id")), x.getRelationships("Comment_To_Article").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t).map(_.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.map( x => x.getProperty("first_name").toString() + " " + x.getProperty("last_name").toString()).mkString(",")   , x.getRelationships("article_voteup").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t).map(_.getOtherNode(x)).distinct.map( x => x.getProperty("first_name").toString() + " " + x.getProperty("last_name").toString()).mkString(",") )).toMap))
	                 list = list:::in_list
	             }
                 
                 if(pets.size > 0)
	             {
	                 
	                 var in_list = List[Any]()
	                 in_list = pets.map(x => JSONObject(l.zip(List( "P", x.getProperty("p_title").toString() , ("/petitions/" + x.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + x.getProperty("p_title_id")), x.getRelationships("Comment_To_Petition").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t).map(_.getOtherNode(x).getSingleRelationship("Comment_Written_By",Direction.OUTGOING).getEndNode()).distinct.map( x => x.getProperty("first_name").toString() + " " + x.getProperty("last_name").toString()).mkString(",")   , x.getRelationships("Signed_Petition").asScala.toList.filter(y => y.getProperty("time").toString().toInt > t).map(_.getOtherNode(x)).distinct.map( x => x.getProperty("first_name").toString() + " " + x.getProperty("last_name").toString()).mkString(",") )).toMap))
	                 list = list:::in_list
	             }
	             if(list.size > 0)
                 {
		             val u_name = each.getProperty("user_name").toString()
		             map3(u_name) = JSONArray(list)
                 }
               }
             }
             
             
             
             
             ret = JSONObject(map3.toMap).toString()

             
             
           }
           if(feed_type.toLowerCase().equals("pd"))
           {
             //println("one")
             val t = (System.currentTimeMillis()/1000).toInt - (86400)
             
             val users = user_index.query( "id", "*" ).iterator().asScala.toList
             var list2 = List[String]()
             var small_map = scala.collection.mutable.Map[String,Any]()
             for(each <- users)
             {
               val subsc_list = each.getProperty("feed_subscription").toString().split(",").toList
               if(subsc_list.contains("PD"))
               {
                     var perso_arts = List[org.neo4j.graphdb.Node]()
                     var perso_events = List[org.neo4j.graphdb.Node]()
                     var perso_pets = List[org.neo4j.graphdb.Node]()
		             
		             
		             var votedup_hashtags = each.getRelationships("article_voteup,article_markfav").asScala.toList.map(x => x.getOtherNode(each)).map( y => y.getRelationships("Belongs_To_Subcategory_Article","Tag_Of_Article").asScala.toList.map( z => z.getOtherNode(y))).flatten
		             var viewed_hashtags = each.getRelationships("Viewed_By").asScala.toList.map(x => x.getOtherNode(each)).map( y => y.getRelationships("Belongs_To_Subcategory_Article","Tag_Of_Article").asScala.toList.map( z => z.getOtherNode(y))).flatten
		             
		             val tot_hash_map =  (votedup_hashtags:::votedup_hashtags:::viewed_hashtags).groupBy(x=>x).mapValues(x=>x.length)
		             val hash_list = tot_hash_map.keys.toList.sortBy(x => -tot_hash_map(x)).slice(0,25)
		             perso_arts = hash_list.map(y => y.getRelationships("Belongs_To_Subcategory_Article","Tag_Of_Article").asScala.toList.map( z => z.getOtherNode(y))).flatten.distinct.filter(x => x.getProperty("time_created").toString().toInt > t).sortBy(-_.getProperty("time_created").toString().toInt)
                     perso_events = hash_list.map(y => y.getRelationships("Belongs_To_Subcategory_Event","Tag_Of_Event").asScala.toList.map( z => z.getOtherNode(y))).flatten.distinct.filter(x => x.getProperty("time_created").toString().toInt > t).sortBy(-_.getProperty("time_created").toString().toInt)
                     perso_pets = hash_list.map(y => y.getRelationships("Belongs_To_Subcategory_Petition","Tag_Of_Petition").asScala.toList.map( z => z.getOtherNode(y))).flatten.distinct.filter(x => x.getProperty("time_created").toString().toInt > t).sortBy(-_.getProperty("time_created").toString().toInt)
            
	                 val tot_items = (perso_arts:::perso_events:::perso_pets)
	                 var small_list = List[Any]()
	                 if(tot_items.size > 0)
	                 {
	                     val u_name = each.getProperty("user_name").toString()
	                     list2 :+= u_name
		                 for(e <- tot_items)
		                 {
		                   if(perso_arts.contains(e))
		                   {
		                     var little_map = scala.collection.mutable.Map[String,Any]()
		                     little_map("ttl") = e.getProperty("article_title").toString()
		                     val url = "/" + e.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(e)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + e.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(e).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + e.getProperty("article_title_id")
					         little_map("url") = url
					         small_list :+= JSONObject(little_map.toMap)
		                     
		                   }
		                   
		                   else if(perso_events.contains(e))
		                   {
		                     var little_map = scala.collection.mutable.Map[String,Any]()
		                     little_map("ttl") = e.getProperty("event_title").toString()
		                     val url = "/Events/" + e.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(e)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + e.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(e).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + e.getProperty("event_title_id")
					         little_map("url") = url
		                     small_list :+= JSONObject(little_map.toMap)
		                   }
		                   
		                   else
		                   {
		                     var little_map = scala.collection.mutable.Map[String,Any]()
		                     little_map("ttl") = e.getProperty("p_title").toString()
		                     val url = "/petitions/" + e.getRelationships("Belongs_To_Subcategory_Petition").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(e).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + e.getProperty("p_title_id")
					         little_map("url") = url
		                     small_list :+= JSONObject(little_map.toMap)
		                   }
		                 }
	                     
	                     small_map(u_name) = JSONArray(small_list)
	                 }
	               
	               }
               }
             
             var map3 = scala.collection.mutable.Map[String,Any]()
             map3("followers") = JSONArray(list2)
             map3("data") = JSONObject(small_map.toMap)
             
             ret = JSONObject(map3.toMap).toString()
//             big_map("Users") = JSONArray(tot_users_list)
//             big_map("List") = JSONObject(small_map.toMap)
//             ret = JSONObject(big_map.toMap).toString()
             
             
           }
           
           else if(feed_type.toLowerCase().equals("pw"))
           {
             //println("one")
             val t = (System.currentTimeMillis()/1000).toInt - (86400 * 7)
             
             val users = user_index.query( "id", "*" ).iterator().asScala.toList
             var list2 = List[String]()
             var small_map = scala.collection.mutable.Map[String,Any]()
             for(each <- users)
             {
               val subsc_list = each.getProperty("feed_subscription").toString().split(",").toList
               if(subsc_list.contains("PW"))
               {
                     var perso_arts = List[org.neo4j.graphdb.Node]()
                     var perso_events = List[org.neo4j.graphdb.Node]()
                     var perso_pets = List[org.neo4j.graphdb.Node]()
		             
		             
		             var votedup_hashtags = each.getRelationships("article_voteup,article_markfav").asScala.toList.map(x => x.getOtherNode(each)).map( y => y.getRelationships("Belongs_To_Subcategory_Article","Tag_Of_Article").asScala.toList.map( z => z.getOtherNode(y))).flatten
		             var viewed_hashtags = each.getRelationships("Viewed_By").asScala.toList.map(x => x.getOtherNode(each)).map( y => y.getRelationships("Belongs_To_Subcategory_Article","Tag_Of_Article").asScala.toList.map( z => z.getOtherNode(y))).flatten
		             
		             val tot_hash_map =  (votedup_hashtags:::votedup_hashtags:::viewed_hashtags).groupBy(x=>x).mapValues(x=>x.length)
		             val hash_list = tot_hash_map.keys.toList.sortBy(x => -tot_hash_map(x)).slice(0,25)
		             perso_arts = hash_list.map(y => y.getRelationships("Belongs_To_Subcategory_Article","Tag_Of_Article").asScala.toList.map( z => z.getOtherNode(y))).flatten.distinct.filter(x => x.getProperty("time_created").toString().toInt > t).sortBy(-_.getProperty("time_created").toString().toInt)
                     perso_events = hash_list.map(y => y.getRelationships("Belongs_To_Subcategory_Event","Tag_Of_Event").asScala.toList.map( z => z.getOtherNode(y))).flatten.distinct.filter(x => x.getProperty("time_created").toString().toInt > t).sortBy(-_.getProperty("time_created").toString().toInt)
                     perso_pets = hash_list.map(y => y.getRelationships("Belongs_To_Subcategory_Petition","Tag_Of_Petition").asScala.toList.map( z => z.getOtherNode(y))).flatten.distinct.filter(x => x.getProperty("time_created").toString().toInt > t).sortBy(-_.getProperty("time_created").toString().toInt)
            
	                 val tot_items = (perso_arts:::perso_events:::perso_pets)
	                 var small_list = List[Any]()
	                 if(tot_items.size > 0)
	                 {
	                     val u_name = each.getProperty("user_name").toString()
	                     list2 :+= u_name
		                 for(e <- tot_items)
		                 {
		                   if(perso_arts.contains(e))
		                   {
		                     var little_map = scala.collection.mutable.Map[String,Any]()
		                     little_map("ttl") = e.getProperty("article_title").toString()
		                     val url = "/" + e.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(e)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + e.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(e).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + e.getProperty("article_title_id")
					         little_map("url") = url
					         small_list :+= JSONObject(little_map.toMap)
		                     
		                   }
		                   
		                   else if(perso_events.contains(e))
		                   {
		                     var little_map = scala.collection.mutable.Map[String,Any]()
		                     little_map("ttl") = e.getProperty("event_title").toString()
		                     val url = "/Events/" + e.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(e)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + e.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(e).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + e.getProperty("event_title_id")
					         little_map("url") = url
		                     small_list :+= JSONObject(little_map.toMap)
		                   }
		                   
		                   else
		                   {
		                     var little_map = scala.collection.mutable.Map[String,Any]()
		                     little_map("ttl") = e.getProperty("p_title").toString()
		                     val url = "/petitions/" + e.getRelationships("Belongs_To_Subcategory_Petition").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(e).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + e.getProperty("p_title_id")
					         little_map("url") = url
		                     small_list :+= JSONObject(little_map.toMap)
		                   }
		                 }
	                     
	                     small_map(u_name) = JSONArray(small_list)
	                 }
	               
	               }
               }
             
             var map3 = scala.collection.mutable.Map[String,Any]()
             map3("followers") = JSONArray(list2)
             map3("data") = JSONObject(small_map.toMap)
             
             ret = JSONObject(map3.toMap).toString()
//             big_map("Users") = JSONArray(tot_users_list)
//             big_map("List") = JSONObject(small_map.toMap)
//             ret = JSONObject(big_map.toMap).toString()
             
             
           }
           
           else if(feed_type.toLowerCase().equals("www"))
           {
             //println("one")
             val art_index = getNodeIndex("article").get
             val event_index = getNodeIndex("event").get
             val petition_index = getNodeIndex("petition").get
             val t = (System.currentTimeMillis()/1000).toInt - (86400 * 7)
             val art_users = art_index.query( "id", "*" ).iterator().asScala.toList.filter(x => x.getProperty("time_created").toString().toInt > t).map( x => x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode())
             val event_users = event_index.query( "id", "*" ).iterator().asScala.toList.filter(x => x.getProperty("time_created").toString().toInt > t).map( x => x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode())
             val petition_users = petition_index.query( "id", "*" ).iterator().asScala.toList.filter(x => x.hasRelationship("Petition_Written_By",Direction.OUTGOING) && x.getProperty("time_created").toString().toInt > t   ).map( x => x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode())
             val tot_users = (art_users:::event_users:::petition_users).distinct
             var big_map = scala.collection.mutable.Map[String,Any]()
             var small_map = scala.collection.mutable.Map[String,Any]()
             var tot_users_list = List[String]()
             
             if(tot_users.size > 0)
             {
               
                 //println("two")
                 //tot_users_list = tot_users.map( x => x.getProperty("user_name").toString())
	             for(each <- tot_users)
	             {
	               val follows = each.getRelationships("Follows",Direction.INCOMING).asScala.toList.map( x => x.getStartNode())
	               
	               val arts = each.getRelationships("Article_Written_By",Direction.INCOMING).asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).map( x => x.getStartNode())
	               val events = each.getRelationships("Event_Created_By",Direction.INCOMING).asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).map( x => x.getStartNode())
	               val petitions = each.getRelationships("Petition_Written_By",Direction.INCOMING).asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).map( x => x.getStartNode())
	               if(follows.size > 0)
	               {
	                 val u_name = each.getProperty("user_name").toString()
	                 val tot_items = (arts:::events:::petitions)
	                 var small_list = List[Any]()
	                 small_list :+= each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
//	                 small_list :+= JSONArray(follows.map( x => x.getProperty("user_name").toString()))
	                 for(e <- tot_items)
	                 {
	                   if(arts.contains(e))
	                   {
	                     var little_map = scala.collection.mutable.Map[String,Any]()
	                     little_map("ttl") = e.getProperty("article_title").toString()
	                     val url = "/" + e.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(e)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + e.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(e).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + e.getProperty("article_title_id")
				         little_map("url") = url
				         small_list :+= JSONObject(little_map.toMap)
	                     
	                   }
	                   
	                   else if(events.contains(e))
	                   {
	                     var little_map = scala.collection.mutable.Map[String,Any]()
	                     little_map("ttl") = e.getProperty("event_title").toString()
	                     val url = "/Events/" + e.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(e)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + e.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(e).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + e.getProperty("event_title_id")
				         little_map("url") = url
	                     small_list :+= JSONObject(little_map.toMap)
	                   }
	                   
	                   else
	                   {
	                     var little_map = scala.collection.mutable.Map[String,Any]()
	                     little_map("ttl") = e.getProperty("p_title").toString()
	                     val url = "/petitions/" + e.getRelationships("Belongs_To_Subcategory_Petition").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(e).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + e.getProperty("p_title_id")
				         little_map("url") = url
	                     small_list :+= JSONObject(little_map.toMap)
	                   }
	                 }
	                 
	                 small_map(u_name) = JSONArray(small_list)
	               }
	             }
             }
             var list2 = List[String]()
             var map2 = scala.collection.mutable.Map[String,Any]()
             var map3 = scala.collection.mutable.Map[String,Any]()
             val users = user_index.query( "id", "*" ).iterator().asScala.toList
             for(each <- users)
             {
               val subsc_list = each.getProperty("feed_subscription").toString().split(",").toList
               if(subsc_list.contains("W"))
               {
	               val foll = each.getRelationships("Follows",Direction.OUTGOING).asScala.toList.map(_.getEndNode())
	               var map1 = scala.collection.mutable.Map[String,Any]()
	               if(foll.size > 0)
	               {
	                 val u_name = each.getProperty("user_name").toString()
	                 
	                 for(e <- foll)
	                 {
	                   if(tot_users.contains(e))
	                   {
		                   val user_name = e.getProperty("user_name").toString()
		                   map1(user_name) = small_map(user_name)
	                   }
	                 }
	                 if(map1.size > 0)
	                 {
	                   map2(u_name) =  JSONObject(map1.toMap)
	                   list2 :+= u_name
	                 }
	                 
	               }
               }
             }
             
             map3("followers") = JSONArray(list2)
             map3("data") = JSONObject(map2.toMap)
             
             ret = JSONObject(map3.toMap).toString()

             
             
           }
           
           else if(feed_type.toLowerCase().equals(""))
           {
             val ArticleIndex = getNodeIndex("article").get
             val EventIndex = getNodeIndex("event").get
             val PetitionIndex = getNodeIndex("petition").get
             val DebateIndex = getNodeIndex("debate").get
             val TownhallIndex = getNodeIndex("townhall").get
             val cur_time = (System.currentTimeMillis()/1000).toInt
             val t = cur_time - (86400)
             val user_tiles_index = getNodeIndex("user_tiles").get
             val users = user_tiles_index.query( "id", "*" ).iterator().asScala.toList
             val FeedIndex = getNodeIndex("feed").get
             var feed_node = FeedIndex.get("id","user_feed").getSingle()
             var articles = List[org.neo4j.graphdb.Node]()
             var events = List[org.neo4j.graphdb.Node]()
             var petitions = List[org.neo4j.graphdb.Node]()
             var townhalls = List[org.neo4j.graphdb.Node]()
             var debates = List[org.neo4j.graphdb.Node]()
             //val news = news_node.getProperty("value").toString()
             val l = List("type","sum","name","ttl","url","img","com","votes")
             val l1 = List("type","sum","name","ttl","url","lc","date","attnd")
             val l2 = List("type","name","ttl","url","count","target","img","signs")
             val l3 = List("type","name","ttl","url","celeb","date","img")
             val l4 = List("type","name","ttl","url","date","img","for","against")
             var map3 = scala.collection.mutable.Map[String,Any]()
             var map2 = scala.collection.mutable.Map[String,Any]()
             if(feed_node != null)
             {
               
                 val a = feed_node.getProperty("article").toString()
	             if(!a.equals(""))
	             {
	               articles = a.split(",").toList.map(x => ArticleIndex.get("id",x).getSingle()).filter( y => y != null && y.getProperty("time_created").toString().toInt > t)
	             }
                 val e = feed_node.getProperty("event").toString()
	             if(!e.equals(""))
	             {
	               events = e.split(",").toList.map(x => EventIndex.get("id",x).getSingle()).filter( y => y != null && y.getProperty("time_created").toString().toInt > t)
	             }
                 val p = feed_node.getProperty("petition").toString()
	             if(!p.equals(""))
	             {
	               petitions = p.split(",").toList.map(x => PetitionIndex.get("id",x).getSingle()).filter( y => y != null && y.getProperty("time_created").toString().toInt > t)
	             }
                 val th = feed_node.getProperty("townhall").toString()
	             if(!th.equals(""))
	             {
	               townhalls = th.split(",").toList.map(x => TownhallIndex.get("id",x).getSingle()).filter( y => y != null && y.getProperty("time_created").toString().toInt > t)
	             }
                 val d = feed_node.getProperty("debate").toString()
	             if(!d.equals(""))
	             {
	               debates = d.split(",").toList.map(x => DebateIndex.get("id",x).getSingle()).filter( y => y != null && y.getProperty("time_created").toString().toInt > t)
	             }
                 feed_node.setProperty("article","")
                 feed_node.setProperty("event","")
                 feed_node.setProperty("petition","")
                 feed_node.setProperty("debate","")
                 feed_node.setProperty("townhall","")
                 feed_node.setProperty("time",cur_time)
             
             }
             var list = List[Any]()
             if(articles.size > 0)
             {
                 
                 var in_list = List[Any]()
                 in_list = articles.map(x => JSONObject(l.zip(List( "A",x.getProperty("article_summary"),x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString() +  x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString() , x.getProperty("article_title").toString() , ("/" + x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + x.getProperty("article_title_id")),x.getProperty("article_featured_img"),x.getRelationships("Comment_To_Article").asScala.size,x.getRelationships("article_voteup").asScala.size )).toMap))
                 list = list:::in_list
             }
             if(events.size > 0)
             {
                 
                 var in_list = List[Any]()
                 in_list = events.map(x => JSONObject(l1.zip(List( "E",x.getProperty("event_summary"),x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString() +  x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString() , x.getProperty("event_title").toString() , ("/Events/" + x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + x.getProperty("event_title_id")),x.getProperty("event_location"),x.getProperty("event_date_time"),x.getRelationships("Is_Attending").asScala.size )).toMap))
                 list = list:::in_list
             }
             if(petitions.size > 0)
             {
                 
                 var in_list = List[Any]()
                 in_list = petitions.map(x => JSONObject(l2.zip(List( "P",x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString() +  x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString() , x.getProperty("p_title").toString() , ("/petitions/" + x.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + x.getProperty("p_title_id")),x.getProperty("p_count"),x.getProperty("p_target"),x.getProperty("p_img_url"),x.getRelationships("Signed_Petition").asScala.size)).toMap))
                 list = list:::in_list
             }
             if(townhalls.size > 0)
             {
                 
                 var in_list = List[Any]()
                 in_list = townhalls.map(x => JSONObject(l3.zip(List( "T",x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString() +  x.getSingleRelationship("Townhall_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString() , x.getProperty("t_title").toString() , ("/" + x.getProperty("t_title_id")),x.getSingleRelationship("Townhall_Of",Direction.OUTGOING).getOtherNode(x).getProperty("user_name"),x.getProperty("t_date"),x.getProperty("t_img_url"))).toMap))
                 list = list:::in_list
             }
             if(debates.size > 0)
             {
                 
                 var in_list = List[Any]()
                 in_list = debates.map(x => JSONObject(l4.zip(List( "D",x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString() +  x.getSingleRelationship("Debate_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString() , x.getProperty("d_title").toString() , ("/" + x.getProperty("d_title_id")),x.getProperty("d_date"),x.getProperty("d_img_url"),x.getRelationships("For").asScala.toList.filter(x => x.getProperty("d_status").toString().toInt == 1).map(_.getOtherNode(x).getProperty("user_name").toString()).distinct.mkString(","),x.getRelationships("Against").asScala.toList.filter(x => x.getProperty("d_status").toString().toInt == 1).map(_.getOtherNode(x).getProperty("user_name").toString()).distinct.mkString(","))).toMap))
                 list = list:::in_list
             }
             map3.put("trending", JSONArray(list).toString() )
		     
             for(each <- users)
             {
               var perso_list = List[org.neo4j.graphdb.Node]()
               val perso = each.getProperty("news_Personalized").toString()
               if(!perso.equals(""))
               {
                 perso_list = perso.split(",").toList.map(x => ArticleIndex.get("id",x).getSingle()).filter( y => y != null && y.getProperty("time_created").toString().toInt > t && articles.contains(y))
               }
               //var map2 = scala.collection.mutable.Map[String,Any]()
               if(perso_list.size > 0)
               {
                 var list = List[Any]()
                 
                 list = (perso_list).map(x => JSONObject(l.zip(List( "A",x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("first_name").toString() +  x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode().getProperty("last_name").toString() , x.getProperty("article_title").toString() , ("/" + x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + x.getProperty("article_title_id")),x.getProperty("article_featured_img"),x.getRelationships("Comment_To_Article").asScala.size,x.getRelationships("article_voteup").asScala.size )).toMap))
                 map2.put(each.getProperty("id").toString(), JSONArray(list).toString() )
               }
               
             }
             
             map3.put("followers", JSONObject(map2.toMap) )
			  
			    
             ret = JSONObject(map3.toMap).toString()
           }
           
           else if(feed_type.toLowerCase().equals("f"))
           {
             val ArticleIndex = getNodeIndex("article").get
             val EventIndex = getNodeIndex("event").get
             val PetitionIndex = getNodeIndex("petition").get
             val DebateIndex = getNodeIndex("debate").get
             val TownhallIndex = getNodeIndex("townhall").get
             val cur_time = (System.currentTimeMillis()/1000).toInt
             var t = cur_time - (86400)
             val user_tiles_index = getNodeIndex("user_tiles").get
             val users = user_tiles_index.query( "id", "*" ).iterator().asScala.toList
             val FeedIndex = getNodeIndex("feed").get
             var feed_node = FeedIndex.get("id","user_feed").getSingle()
             var articles = List[org.neo4j.graphdb.Node]()
             var events = List[org.neo4j.graphdb.Node]()
             var petitions = List[org.neo4j.graphdb.Node]()
             var townhalls = List[org.neo4j.graphdb.Node]()
             var debates = List[org.neo4j.graphdb.Node]()
             //val news = news_node.getProperty("value").toString()
             val l = List("id","ttl","url")
             val l1 = List("id","ttl","url")
             val l2 = List("id","ttl","url")
             val l3 = List("id","ttl","url")
             val l4 = List("id","ttl","url")
             var map3 = scala.collection.mutable.Map[String,Any]()
             if(feed_node != null)
             {
               t = feed_node.getProperty("time").toString().toInt
             }
             articles = ArticleIndex.query("id", "*" ).iterator().asScala.toList.filter(x => x.getProperty("time_created").toString().toInt > t)
             events = EventIndex.query("id", "*" ).iterator().asScala.toList.filter(x => x.getProperty("time_created").toString().toInt > t)
             petitions = PetitionIndex.query("id", "*" ).iterator().asScala.toList.filter(x => x.getProperty("time_created").toString().toInt > t)
             debates = DebateIndex.query("id", "*" ).iterator().asScala.toList.filter(x => x.getProperty("time_created").toString().toInt > t)
             townhalls = TownhallIndex.query("id", "*" ).iterator().asScala.toList.filter(x => x.getProperty("time_created").toString().toInt > t)
             
             
             var list = List[Any]()
             list = articles.map(x => JSONObject(l.zip(List( x.getProperty("article_id").toString(), x.getProperty("article_title").toString() , ("/" + x.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + x.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + x.getProperty("article_title_id")) )).toMap))
             map3.put("A", JSONArray(list) )
             
             list = events.map(x => JSONObject(l1.zip(List( x.getProperty("event_id").toString(), x.getProperty("event_title").toString() , ("/Events/" + x.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + x.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(x).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + x.getProperty("event_title_id")) )).toMap))
             map3.put("E", JSONArray(list) )
             
             list = petitions.map(x => JSONObject(l2.zip(List( x.getProperty("p_id").toString(),x.getProperty("p_title").toString() , ("/petitions/" + x.getRelationships("Belongs_To_Petition_Category").asScala.toList.map(_.getOtherNode(x)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + x.getProperty("p_title_id")) )).toMap))
             map3.put("P", JSONArray(list) )
             
             list = townhalls.map(x => JSONObject(l3.zip(List( x.getProperty("t_id").toString(), x.getProperty("t_title").toString() , ("/" + x.getProperty("t_title_id")) )).toMap))
             map3.put("T", JSONArray(list) )
             
             list = debates.map(x => JSONObject(l4.zip(List( x.getProperty("d_id").toString(),x.getProperty("d_title").toString() , ("/" + x.getProperty("d_title_id")) )).toMap))
             map3.put("D", JSONArray(list) )
             
             ret =  JSONObject(map3.toMap).toString()
		     
             
           }
           
           // user daily subscription feed
           else
           {
             val user_index = getNodeIndex("user").get
             val art_index = getNodeIndex("article").get
             val event_index = getNodeIndex("event").get
             val petition_index = getNodeIndex("petition").get
             val t = (System.currentTimeMillis()/1000).toInt - (86400)
             // getting all the users who published an item in last 24 hours
//             val art_users = art_index.query( "id", "*" ).iterator().asScala.toList.filter(x => x.getProperty("time_created").toString().toInt > t).map( x => x.getSingleRelationship("Article_Written_By",Direction.OUTGOING).getEndNode())
//             val event_users = event_index.query( "id", "*" ).iterator().asScala.toList.filter(x => x.getProperty("time_created").toString().toInt > t).map( x => x.getSingleRelationship("Event_Created_By",Direction.OUTGOING).getEndNode())
//             val petition_users = petition_index.query( "id", "*" ).iterator().asScala.toList.filter(x => x.hasRelationship("Petition_Written_By",Direction.OUTGOING) && x.getProperty("time_created").toString().toInt > t   ).map( x => x.getSingleRelationship("Petition_Written_By",Direction.OUTGOING).getEndNode())
             var users = feed_type.split(",").map( x => user_index.get("id",x).getSingle()).toList.filter( x => x != null)
             var tot_users = users.map( x => x.getRelationships("Follows",Direction.OUTGOING).asScala.toList.map( _.getOtherNode(x))).flatten.distinct
             var big_map = scala.collection.mutable.Map[String,Any]()
             var small_map = scala.collection.mutable.Map[String,Any]()
             var tot_users_list = List[String]()
             
             
             if(tot_users.size > 0)
             {
                 // looping through the users who published the items
                 for(each <- tot_users)
	             {
                   
                   // creating a map with user name as the key and the json(items published) as the value
	               //val follows = each.getRelationships("Follows",Direction.INCOMING).asScala.toList.map( x => x.getStartNode())
	               
	               val arts = each.getRelationships("Article_Written_By",Direction.INCOMING).asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).map( x => x.getStartNode())
	               val events = each.getRelationships("Event_Created_By",Direction.INCOMING).asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).map( x => x.getStartNode())
	               val petitions = each.getRelationships("Petition_Written_By",Direction.INCOMING).asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).map( x => x.getStartNode())
	               var items = arts:::events:::petitions
	               if(items.size > 0)
	               {
	                 val u_name = each.getProperty("user_name").toString()
	                 val tot_items = (arts:::events:::petitions)
	                 var small_list = List[Any]()
	                 small_list :+= each.getProperty("first_name").toString() + " " + each.getProperty("last_name").toString()
	                 for(e <- tot_items)
	                 {
	                   if(arts.contains(e))
	                   {
	                     var little_map = scala.collection.mutable.Map[String,Any]()
	                     little_map("ttl") = e.getProperty("article_title").toString()
	                     val url = "/" + e.getRelationships("Belongs_To_Category").asScala.toList.map(_.getOtherNode(e)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0) + "/" + e.getRelationships("Belongs_To_Subcategory_Article").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(e).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + e.getProperty("article_title_id")
				         little_map("url") = url
				         little_map("type") = "A"
				         little_map("img") = e.getProperty("article_featured_img")
				         small_list :+= JSONObject(little_map.toMap)
	                     
	                   }
	                   
	                   else if(events.contains(e))
	                   {
	                     var little_map = scala.collection.mutable.Map[String,Any]()
	                     little_map("ttl") = e.getProperty("event_title").toString()
	                     val url = "/Events/" + e.getRelationships("Belongs_To_Event_Category").asScala.toList.map(_.getOtherNode(e)).map(y => y.getProperty("name")).filterNot(x => x.equals("all"))(0).toString().capitalize + "/" + e.getRelationships("Belongs_To_Subcategory_Event").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(e).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + e.getProperty("event_title_id")
				         little_map("url") = url
				         little_map("type") = "E"
	                     small_list :+= JSONObject(little_map.toMap)
	                   }
	                   
	                   else
	                   {
	                     var little_map = scala.collection.mutable.Map[String,Any]()
	                     little_map("ttl") = e.getProperty("p_title").toString()
	                     val url = "/petitions/" + e.getRelationships("Belongs_To_Subcategory_Petition").asScala.toList.filter( y => y.hasProperty("main")).map(_.getOtherNode(e).getProperty("name").toString()).slice(0,1).mkString(",") + "/" + e.getProperty("p_title_id")
				         little_map("url") = url
				         little_map("type") = "P"
	                     small_list :+= JSONObject(little_map.toMap)
	                     little_map("img") = e.getProperty("p_img_url")
	                   }
	                 }
	                 
	                 small_map(u_name) = JSONArray(small_list)
	               }
	               
	               else
	               {
	                 tot_users = tot_users.filter( x => x != each)
	               }
	             }
             }
             var list2 = List[String]()
             var map2 = scala.collection.mutable.Map[String,Any]()
             var map3 = scala.collection.mutable.Map[String,Any]()
            // val users = user_index.query( "id", "*" ).iterator().asScala.toList
             // looping through all the existing users
             for(each <- users)
             {
               val subsc_list = each.getProperty("feed_subscription").toString().split(",").toList.map( x => x.toLowerCase())
               // checking if the user subscribed for the daily subscription feed
               if(subsc_list.contains("d"))
               {
	               val foll = each.getRelationships("Follows",Direction.OUTGOING).asScala.toList.map(_.getEndNode())
	               var map1 = scala.collection.mutable.Map[String,Any]()
	               // creating a map with user name as key and feed as value
	               if(foll.size > 0)
	               {
	                 val u_name = each.getProperty("user_name").toString()
	                 
	                 for(e <- foll)
	                 {
	                   // creating feed map
	                   if(tot_users.contains(e))
	                   {
		                   val user_name = e.getProperty("user_name").toString()
		                   map1(user_name) = small_map(user_name)
	                   }
	                 }
	                 if(map1.size > 0)
	                 {
	                   map2(u_name) =  JSONObject(map1.toMap)
	                   list2 :+= u_name
	                 }
	                 
	               }
               }
             }
             
             // Adding all the users who subscribed for daily feed along with the entire daily feed to a map and converting it into json
             map3("followers") = JSONArray(list2)
             map3("data") = JSONObject(map2.toMap)
             
             ret = JSONObject(map3.toMap).toString()
     
             
           }
           
           ret       
    }
  }
  
  
  def calc_user_tiles()
  {
    
    withTx {
    implicit neo =>
     
           val cur_time = (System.currentTimeMillis()/1000).toInt
           val t1 = cur_time - (86400)
           val t2 = cur_time - (86400*3)
           val user_tiles_index = getNodeIndex("user_tiles").get
           val user_index = getNodeIndex("user").get
           
           val article_content_index = getNodeIndex("article_content").get
           val user_nodes = user_index.query("id", "*" ).iterator().asScala.toList
           //val user_nodes = tot_user_nodes.filter( x => x.getProperty("last_seen").toString().toInt > t1)
           //val nonactive_users = tot_user_nodes.filterNot( x => user_nodes.contains(x))
           for(each <- user_nodes)
           {
             var perso_arts = List[org.neo4j.graphdb.Node]()
             var friends_arts = List[org.neo4j.graphdb.Node]()
             var hash_arts = List[org.neo4j.graphdb.Node]()
             var total_arts = List[org.neo4j.graphdb.Node]()
             
             var votedup_hashtags = each.getRelationships("article_voteup,article_markfav").asScala.toList.map(x => x.getOtherNode(each)).map( y => y.getRelationships("Belongs_To_Subcategory_Article","Tag_Of_Article").asScala.toList.map( z => z.getOtherNode(y))).flatten
             var viewed_hashtags = each.getRelationships("Viewed_By").asScala.toList.map(x => x.getOtherNode(each)).map( y => y.getRelationships("Belongs_To_Subcategory_Article","Tag_Of_Article").asScala.toList.map( z => z.getOtherNode(y))).flatten
             
             val tot_hash_map =  (votedup_hashtags:::votedup_hashtags:::viewed_hashtags).groupBy(x=>x).mapValues(x=>x.length)
             perso_arts = tot_hash_map.keys.toList.sortBy(x => -tot_hash_map(x)).slice(0,25).map(y => y.getRelationships("Belongs_To_Subcategory_Article","Tag_Of_Article").asScala.toList.map( z => z.getOtherNode(y))).flatten.distinct
            
             var friends = each.getRelationships("Follows").asScala.map(_.getOtherNode(each)).toList
             friends :+= each
             if(friends != Nil)
             {
               friends_arts = friends.map( x => x.getRelationships("Article_Written_By").asScala.map(_.getOtherNode(x)).toList).flatten
             }
             

//             val tag_name = each.getProperty("fav_hash").toString()
//	         if(!tag_name.equals(""))
//             {
//               hash_arts = article_content_index.query("article_content", tag_name ).iterator().asScala.toList
//             }
             
             var topic_arts = List[org.neo4j.graphdb.Node]()
             topic_arts = each.getRelationships("Favourite_Topic").asScala.map(_.getOtherNode(each)).toList.map( y => y.getRelationships("Belongs_To_Topic").asScala.toList.map(_.getOtherNode(y))).flatten.distinct
             
             val one = friends_arts.intersect(topic_arts).filter(x => (x.getProperty("time_created").toString().toInt > t1)  ).sortBy(-_.getProperty("time_created").toString().toInt)
             val two = friends_arts.intersect(perso_arts).filter(x => (x.getProperty("time_created").toString().toInt > t1)  ).sortBy(-_.getProperty("time_created").toString().toInt)
             
             val three = friends_arts.intersect(topic_arts).filter(x => (x.getProperty("time_created").toString().toInt > t2)  ).sortBy(-_.getProperty("time_created").toString().toInt)
             val four = friends_arts.intersect(perso_arts).filter(x => (x.getProperty("time_created").toString().toInt > t2) ).sortBy(-_.getProperty("time_created").toString().toInt)
             
             //val five = (perso_arts:::friends_arts:::topic_arts).filter(x => (x.getProperty("time_created").toString().toInt > t2) ).sortBy(-_.getProperty("time_created").toString().toInt)
             
             val six = (perso_arts:::friends_arts:::topic_arts).sortBy(-_.getProperty("time_created").toString().toInt)
             
             total_arts = (one:::two:::three:::four:::six).distinct
	         val sorted_news = total_arts.filter( x => x != null && x.getProperty("space").toString.toInt == 0).map(x => x.getProperty("article_id")).mkString(",")
	         
  
             val tile_node = user_tiles_index.get("id",each.getProperty("user_name").toString()).getSingle()
             tile_node.setProperty("news_Personalized",sorted_news)

             
           }
           
           
           
           
           
           
             
             
    }
  println("User personalized tiles updated")  
  }
  
  def calc_local_tiles()
  {
    
    withTx {
     implicit neo =>
       
       val location_index = getNodeIndex("location").get
       val nodes = location_index.query( "id", "*" ).iterator().asScala.toList.filterNot( x => x.getProperty("location_id").equals("all"))
       val t1 = (System.currentTimeMillis()/1000).toInt - (86400 * 2)
       var art_list = List[Any]()   
       for(each <- nodes) 
       {
         val list  = each.getRelationships("Belongs_To_Location_Article").asScala.map(_.getOtherNode(each)).toList.distinct.filter(x => (x.getProperty("time_created").toString().toInt > t1)  ).sortBy(-_.getProperty("time_created").toString().toInt).filter( x => x != null && x.getProperty("space").toString.toInt == 0).map( x => x.getProperty("article_id").toString()).mkString(",")
         each.setProperty("tiles",list)
       }
     
    }
  println("Localized tiles updated")  
  }
  
  def nouns_update()
  {
    
    withTx {
     implicit neo =>
       
         val c_time = (System.currentTimeMillis()/1000).toInt
         val pos_index = getNodeIndex("pos").get
         var pos_node = pos_index.get("id","proper_nouns").getSingle()
         val Id_nodeIndex = getNodeIndex("user").get
         val users = Id_nodeIndex.query( "id", "*" ).iterator().asScala.toList
         if(  (  (pos_node.getProperty("last_update").toString().toInt + (7*86400) ) < c_time) && users.size > 0 )
         {
	         
	         
	         for( each <- users )
		     {
		       val nouns = each.getProperty("nouns").toString()
		       if(!nouns.equals("") && !nouns.equals("{}"))
	           {
		         
		           val proper_nouns = List("2013","2014","india","indian","reddy","rao","singh","world","pti","sunday","monday","tuesday","wednesday","thursday","friday","saturday","january","february","march","april","may","june","july","august","september","october","november","december")
	               val nouns_map = JSON.parseFull(nouns).get.asInstanceOf[Map[String,Double]]
	               val new_map = nouns_map.filterKeys(y => y.length() > 2 && !proper_nouns.contains(y))
	               val final_map = ListMap(new_map.toList.sortBy{-_._2}:_*).slice(0,1000)
		           each.setProperty("nouns",JSONObject(final_map).toString())
	          }
		    }
	        pos_node.setProperty("last_update",c_time)
          }
       }
       println("User proper nouns updated")  
  }
  
  
  def calc_views()
  {
    
    withTx {
     implicit neo =>
       
       val art_index = getNodeIndex("article").get
       val art_nodes = art_index.query( "id", "*" ).iterator().asScala.toList
       //current time in sec - 4 hours
       val t = (System.currentTimeMillis()/1000).toInt - (14400) //i.e., 4 hours ago time
       for(each <- art_nodes) 
       {
         var nonuser_views = 0
         //get all the view relations count happend in last 4 hours to each article
         val user_views = each.getRelationships("Viewed_By").asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).size
         
         //get the count of non user views for each article.
         //if it is empty, mark update the view count with user_views
         //if it is not empty, get the number of non user views (For every non user view, latest_views will be updated with comma separated time in seconds
         //														  For every login user view,  View_By relation will be created b/w article and present user)
         val latest_views = each.getProperty("latest_views").toString()
         if(!latest_views.equals(""))
         {
           var list = latest_views.split(",").toList.map( x => x.toInt).filter( x => x > t)
           nonuser_views = list.size
           
         }
         each.setProperty("views",( (user_views*5) + nonuser_views) )
       }
       
       //same are article
       val event_index = getNodeIndex("event").get
       val event_nodes = event_index.query( "id", "*" ).iterator().asScala.toList
       for(each <- event_nodes) 
       {
         var nonuser_views = 0
         val user_views = each.getRelationships("Viewed_By").asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).size
         val latest_views = each.getProperty("latest_views").toString()
         if(!latest_views.equals(""))
         {
           var list = latest_views.split(",").toList.map( x => x.toInt).filter( x => x > t)
           nonuser_views = list.size
           
         }
         each.setProperty("views",user_views + nonuser_views)
       }
       
       //same are article
       val p_index = getNodeIndex("petition").get
       val p_nodes = p_index.query( "id", "*" ).iterator().asScala.toList
       for(each <- p_nodes) 
       {
         var nonuser_views = 0
         val user_views = each.getRelationships("Viewed_By").asScala.toList.filter(x => x.getProperty("time").toString().toInt > t).size
         val latest_views = each.getProperty("latest_views").toString()
         if(!latest_views.equals(""))
         {
           var list = latest_views.split(",").toList.map( x => x.toInt).filter( x => x > t)
           nonuser_views = list.size
           
         }
         each.setProperty("views",user_views + nonuser_views)
       }
     
    }
  println("Views updated")  
  }



}


