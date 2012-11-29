import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.htmlparser.util.ParserException;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;


public class Main {
	
	public static void main (String[] args) throws IOException, ParserException
	{
		/*
		RecordManager recman;
		HTree titletree;
		HTree bodytree;
		HTree linktree;		
		Hashtable<String,LinkRelation> linkGraph = new Hashtable<String,LinkRelation>();
		
		String recordmanager="comp4321project";
		recman = RecordManagerFactory.createRecordManager(recordmanager);		
		long recid;
		
		//create spider and crawl
		long start = System.currentTimeMillis();
		Spider spider=new Spider("http://www.cse.ust.hk/~ericzhao/COMP4321/TestPages/testpage.htm",300000);
		spider.crawl();
		
		long end = System.currentTimeMillis();
		System.out.println("Time taken: " + ((end - start)/1000));	
		
		//get all tables		
		recid = recman.getNamedObject("linkGraph");
		linkGraph=(Hashtable<String,LinkRelation>)recman.fetch(recid);		
		recid = recman.getNamedObject("titletable");
		titletree=HTree.load(recman, recid);
		recid = recman.getNamedObject("bodytable");
		bodytree=HTree.load(recman, recid);
		recid = recman.getNamedObject("linktable");
		linktree=HTree.load(recman, recid);
		
		PageRank rank = new PageRank(linkGraph);
		rank.calcRank();		
		ArrayList<LinkRelation> sorted = new ArrayList<LinkRelation>(linkGraph.values());		
		Collections.sort(sorted);
		Iterator<LinkRelation> it = sorted.iterator();
		while(it.hasNext()){
			System.out.println(it.next().getUrl());
		}
		*/
		SEngine se = new SEngine();
		//Result[] r = se.search("\"Jade (1995)\"");
		//Result[] r = se.search("\"Congo (1995)\"");
		Result[] r = se.merge("\"Jade (1995)\"", "\"Congo (1995)\"");
		//Result[] r = se.within("\"Jade (1995)\"", "\"Congo (1995)\"");
		for (int i = 0; i < r.length; i++){
			r[i].printData(false);
			System.out.println();
		}
		
		
	}
	
	
	

}
