package SE;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;


public class SEngine {
	private RecordManager recman;
	private HTree titletree;
	private HTree bodytree;
	private HTree linktree;		
	private Hashtable<String,LinkRelation> linkGraph = new Hashtable<String,LinkRelation>();
	private StopStem stopstem;	
	private static String path = "comp4321project";	
	//private static String path = "/comp4321/lhong/public_html/comp4321project";	
	public SEngine(){		
		try {
			recman = RecordManagerFactory.createRecordManager(path);			
			long recid;		
			//get all tables		
			recid = recman.getNamedObject("linkGraph");
			linkGraph=(Hashtable<String,LinkRelation>)recman.fetch(recid);		
			recid = recman.getNamedObject("titletable");
			titletree=HTree.load(recman, recid);
			recid = recman.getNamedObject("bodytable");
			bodytree=HTree.load(recman, recid);
			recid = recman.getNamedObject("linktable");
			linktree=HTree.load(recman, recid);	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stopstem = new StopStem();
	}
	//given query, returns results in order of relevance 
	public Result[] search(String query) throws IOException{
		//extract phrases
		ArrayList<String[]> phrases = new ArrayList<String[]>();
		Pattern p = Pattern.compile("\"([^\"]*)\"");
		Matcher m = p.matcher(query);		
		while (m.find()) {
			//splits the phrase into terms and adds to list			
			phrases.add(m.group(1).split(" +"));
		}
		//remove phrases from query
		query = m.replaceAll("");	
		//split into keywords
		String[] terms = query.split(" +");		
		HashSet<String> docs = new HashSet<String>();	
		//add all docs that contain at least one keyword to doc set
		for(int i = 0; i < terms.length; i++){	
			if(!stopstem.isStopWord(terms[i])){					
				try {
					terms[i]=stopstem.stem(terms[i]);
					//System.out.println(terms[i]);				
					docs.addAll(((InvertedfileValue) bodytree.get(terms[i])).list.keySet());
					docs.addAll(((InvertedfileValue) titletree.get(terms[i])).list.keySet());
				}
					catch (Exception e){				
				}
			}
			else{
				try {
					terms[i]=stopstem.stem(terms[i]);
					//System.out.println(terms[i]);						
					docs.addAll(((InvertedfileValue) titletree.get(terms[i])).list.keySet());
				}
					catch (Exception e){				
				}				
			}
		}	
		Iterator<String[]> it = phrases.iterator();
		//add all docs that have the first word in a phrase
		while(it.hasNext()){
			String[] temp = it.next();			
			try {
			docs.addAll(((InvertedfileValue) bodytree.get(temp[0])).list.keySet());
			docs.addAll(((InvertedfileValue) titletree.get(temp[0])).list.keySet());
			} catch (Exception e){				
			}
		}		
		//put docs in array
		String[] docArray = docs.toArray(new String[docs.size()]);
		//array for scores
		Double[] scores = new Double[docArray.length];	
		//for each doc calculate score
		for(int i = 0; i < docArray.length; i++){			
			scores[i] = cosSim(terms, phrases, docArray[i]);			
		}	

		return packageData(docArray,scores);
	}
	
	//put data into ordered array of Result objects
	private Result[] packageData(String[] docs, Double[] score) throws IOException{
		Result[] results = new Result[docs.length];		
		//for each doc get data from linkGraph
		for (int i = 0; i < docs.length; i ++){
			LinkRelation info = linkGraph.get(docs[i]);			
			
			String[] children = new String[info.getChildren().size()];
			Iterator<LinkRelation> itc = info.getChildren().iterator();
			for(int j = 0; j < children.length; j++)
				children[j]= itc.next().getUrl();
			
			String[] parents = new String[info.getParents().size()];
			Iterator<LinkRelation> itp = info.getParents().iterator();
			for(int j = 0; j < parents.length; j++)
				parents[j]= itp.next().getUrl();			
			
			results[i] = new Result(score[i], info.getPageTitle(), docs[i],info.getLastModified(),info.getPageSize(), ((String[][])linktree.get(docs[i]))[1], parents, children);			
		}
		//sort array by score
		sortResult(results);
		return results;
	}
	
	//Sorts an array of Results
	private void sortResult(Result[] results){
		Arrays.sort(results, new Comparator<Result>(){
			@Override			
			public int compare(Result arg0, Result arg1) {	
				if (((Result)arg0).getScore() > ((Result)arg1).getScore())
					return -1;
				else if (((Result)arg0).getScore() < ((Result)arg1).getScore())
					return 1;
				else 
					return 0;			
			}			
		});
	}
	
	//Merges the results of two queries
	public Result[] merge(String query1, String query2) throws IOException{
		Result[] r1 = search(query1);
		Result[] r2 = search(query2);
		Set<Result> set = new HashSet<Result>();
		for(int i = 0; i < r1.length;i++){
			set.add(r1[i]);			
		}
		for(int i = 0; i < r2.length;i++){
			set.add(r2[i]);			
		}
		//can't see if equal!
		Result[] results = set.toArray(new Result[set.size()]);
		sortResult(results);
		return results;
	}
	
	//Searches within another query
	public Result[] within(String query1, String query2) throws IOException{
		Result[] r1 = search(query1);
		Result[] r2 = search(query2);
		Set<Result> set1 = new HashSet<Result>();
		Set<Result> set2 = new HashSet<Result>();
		for(int i = 0; i < r1.length;i++){
			set1.add(r1[i]);			
		}
		for(int i = 0; i < r2.length;i++){
			set2.add(r2[i]);			
		}
		set2.retainAll(set1);
		Result[] results = set2.toArray(new Result[set2.size()]);
		sortResult(results);
		return results;
	}
	
	//Calculates the weight of a term with respect to a document
	private Double termWeight(String term, String doc) throws IOException{
		Double score;
		//get docs that have term in title and body		
		InvertedfileValue invt = (InvertedfileValue) titletree.get(term);	
		InvertedfileValue invb = (InvertedfileValue) bodytree.get(term);
		Double numt;		
		try {				
			//count number of times in doc
			numt = (double) invt.get(doc).split("[,]+").length;	
			//multiply by idf logbase2(total number of docs/number of docs with term)
			numt = numt*(Math.log(linkGraph.size()/invt.size)/Math.log(2));		
			//divide by number of terms in doc
			//ALSO BY TFMAX?!?!?!?!?!?!?!?!?!?
			numt = numt/(((String[][])linktree.get(doc))[0].length);			
		} catch (Exception e){
			numt = (double) 0;
		}
		Double numb;
		//same for body
		try {
			numb = (double) invb.get(doc).split("[,]+").length;
			numb = numb*(Math.log(linkGraph.size()/invb.size)/Math.log(2));
			numb = numb/(((String[][])linktree.get(doc))[1].length);		
		} catch (Exception e){
			numb = (double) 0;
		}		
//		System.out.println("Doc:"+doc+", Term: " + term +", Title score: "+ numt+ ", Body score: "+ numb);
		//add scores, title weighted higher because generally shorter
		score = (1*numb)+(1*numt);
		return score;
	}
	
	//Calculates the weight of a phrase with respect to a document
	private double phraseWeight(String[] terms, String doc) {
		double numT = 0;
		try{
			terms = filterkw(terms,false);
			//gets words in title
			String[] titleTerms = ((String[][])linktree.get(doc))[0];	
			//gets positions of first word in phrase
			String[] possT = ((InvertedfileValue) titletree.get(terms[0])).get(doc).split("[,]+");	
			//for each position, checks if phrase is there
			for(int i = 0; i < possT.length; i++){
				String[] inPhrase = Arrays.copyOfRange(titleTerms, Integer.parseInt(possT[i]),Integer.parseInt(possT[i])+terms.length); 
				if (Arrays.deepEquals(terms, inPhrase))
					numT++;
			}
			//xidf
			numT = numT*(Math.log(linkGraph.size())/Math.log(2));
		} catch (Exception e){
			numT = 0;
		}
		double numB = 0;
		//same for body
		try {
			terms = filterkw(terms,true);
			String[] bodyTerms = ((String[][])linktree.get(doc))[1];
			String[] possB = ((InvertedfileValue) bodytree.get(terms[0])).get(doc).split("[,]+");			
			for(int i = 0; i < possB.length; i++){
				String[] inPhrase = Arrays.copyOfRange(bodyTerms, Integer.parseInt(possB[i]),Integer.parseInt(possB[i])+terms.length); 
				if (Arrays.deepEquals(terms, inPhrase))
					numB++;
			}
			numB = numB*(Math.log(linkGraph.size())/Math.log(2));			
		} catch (Exception e){
			numB = 0;
		}
		return numT+numB;
	}
	
	//Calculates the cosine similarity of a document with respect to a query 
	private double cosSim(String[] terms, ArrayList<String[]> phrases, String doc) throws IOException{
		Double total = (double) 0;
		
		//sum termweights 
		for(int i = 0; i < terms.length; i++){
			total += termWeight(terms[i], doc);
		}
		Iterator<String[]> it = phrases.iterator();
		//sum phrase weights 
		while(it.hasNext()){
			total += phraseWeight(it.next(), doc);
		}
		//divide by query length to normalise
		return total/(terms.length+phrases.size());
	}	

	//Stems a list of terms and removes keywords
	public String[] filterkw(String[] kw, boolean rmstop){
		String[] newkw;
		int j=0;
		
		for(int i=0;i<kw.length;i++){
			//remove stop words
			if(rmstop){
				if(stopstem.isStopWord(kw[i])|| kw[i].equals("")){
					kw[i]=null;
				}else{
	//				System.out.println(kw[i]);//test
					kw[i]=stopstem.stem(kw[i]);
					j++;
				}
			}else{
				if( kw[i].equals("")){
					kw[i]=null;
				}else{
//					System.out.println(kw[i]);//test
					kw[i]=stopstem.stem(kw[i]);
					j++;
				}	
			}
		}
		newkw=new String[j];
		j=0;
		for(int i=0;i<kw.length;i++){
			if(kw[i]!=null){
				newkw[j]=kw[i];
				++j;
			}
		}
		return newkw;

	}
	
	public static String getPath() {
		return path;
	}
	
	public String[] getStemWords(){
		//just body is enough
		return null;
	}


	
	
}
