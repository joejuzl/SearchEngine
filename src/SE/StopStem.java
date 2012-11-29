package SE;

import IRUtilities.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class StopStem
{
	private String path1;
	private String path2;
	private Porter porter;
	private java.util.HashSet<String> stopWords;	
	public boolean isStopWord(String str)
	{
		return stopWords.contains(str);	
	}
	public StopStem()
	{
		super();
		porter = new Porter();
		stopWords = new java.util.HashSet<String>();
		Scanner sc2=null;
		try{
			path1 = "src/stopwords.txt";
			path2 = "/comp4321/lhong/public_html/stopwords.txt";
			sc2=new Scanner(new File(path1));
		} catch (FileNotFoundException e){
			e.printStackTrace();
		}
		while(sc2.hasNext()){
			String s=sc2.next();
			stopWords.add(s);
		}
	}
	public String stem(String str)
	{
		return porter.stripAffixes(str);
	}

}
