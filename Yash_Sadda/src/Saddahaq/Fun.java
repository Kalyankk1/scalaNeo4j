package Saddahaq;



public class Fun{

	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
	    
		System.out.println("11/25/2014 - 12:26 P.M");
		
//		User_node.jar_check();
		
		
		
		User_node.add_firstname_index();
		User_node.location_store();
		User_node.keyword_store();
		User_node.add_exclusive_property();
		User_node.sample_test();
		User_node.main();


	}

	
//	    public ArrayList<String> dummy(String content) {
//		
//		
//		TODO Auto-generated method stub
//		System.out.println("this is Dummy");
//		System.out.println(System.getProperty("java.vendor"));
//		LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz"); //<--TODO path to grammar goes here
//		lp.setOptionFlags(new String[]{"-maxLength", "10000", "-retainTmpSubcategories"});
//
//		BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
//		ArrayList<String> nouns = new ArrayList<String>();
//		iterator.setText(content);
//		int start = iterator.first();
//		for (int end = iterator.next();
//		    end != BreakIterator.DONE;
//		    start = end, end = iterator.next()) {
//		  String s = content.substring(start,end);
//		  
//		  Tree parse = (Tree) lp.apply(s);
//		  
//		  List taggedWords = parse.taggedYield();
//		 //   System.out.println(taggedWords);
//		   
//		    Iterator iter = taggedWords.iterator();
//		    
//			while (iter.hasNext()) {
//				String str = iter.next().toString();
//			    String[] result = str.split("/");
//			    if(result[1].equals("NNP"))
//			    {
//			    	nouns.add(result[0]);  //Adding proper nouns to Arraylist
//			    }
//				
//			}
//		
//	    }
//		return nouns;
//	}

}

