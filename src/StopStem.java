
import IRUtilities.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
public class StopStem
{
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
			sc2=new Scanner(new File("src/stopwords.txt"));
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
