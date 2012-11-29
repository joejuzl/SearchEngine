package SE;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;

import org.apache.commons.lang3.StringUtils;
import org.htmlparser.util.ParserException;


public class Test {
	
	public static void main(String [ ] args) throws ParserException, MalformedURLException, IOException
	{
		RecordManager recman;
		HTree titletree;
		HTree bodytree;
		HTree linktree;		
		Hashtable<String,LinkRelation> linkGraph = new Hashtable<String,LinkRelation>();
		
		//construct file to output spider.txt
		FileWriter fstream = new FileWriter("spider_result.txt");
		BufferedWriter out=new BufferedWriter(fstream);
		
		String recordmanager="comp4321project";
		recman = RecordManagerFactory.createRecordManager(recordmanager);		
		long recid;	
		recid = recman.getNamedObject("titletable");
		if(recid==0){
			titletree = HTree.createInstance(recman);
			recman.setNamedObject("titletable", titletree.getRecid());
		}		
		recid = recman.getNamedObject("bodytable");
		if(recid==0){
			bodytree = HTree.createInstance(recman);
			recman.setNamedObject("bodytable", bodytree.getRecid());
		}	
		recid = recman.getNamedObject("linktable");
		if(recid==0){
			linktree = HTree.createInstance(recman);
			recman.setNamedObject("linktable", linktree.getRecid());
		}
		recid = recman.getNamedObject("linkGraph");
		if(recid==0){
			recid = recman.insert(linkGraph);			
			recman.setNamedObject("linkGraph",recid);
		}
		
		//create spider and crawl
		long start = System.currentTimeMillis();
		Spider spider=new Spider("http://www.cse.ust.hk/",30);
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

		//Extract and write data
		System.out.println("Size: " + linkGraph.size());
		Enumeration<String> links = linkGraph.keys();
		while(links.hasMoreElements()){
			String link = links.nextElement();
			System.out.println("Writing for URL: " + link);
			LinkRelation node = linkGraph.get(link);	
			if (node.getPageTitle() == null)
				node.setPageTitle("N/A");				
			out.write("--Title: " + node.getPageTitle());	
			out.newLine();
			out.write("--URL: "+link);	
			out.newLine();				
			if (node.getLastModified() == 0)
				out.write("--Last modified: n/a");
			else
				out.write("--Last modified: "+ (new Date(node.getLastModified())).toString());
			out.newLine();	
			int size = node.getPageSize();			
			if (size == -1)
				out.write("--Page length cannot be detected");
			else
				out.write("--Page length: "+node.getPageSize()+ " Bytes");
			out.newLine();			
			String[][] term = (String[][]) linktree.get(link);	
			InvertedfileValue inv;
			String pos;
			out.write("--Title keywords: ");
			int num;
			for(int x = 0; x < term[0].length; x++){
				out.write(term[0][x]);
				inv = (InvertedfileValue) titletree.get(term[0][x]);
				pos = inv.list.get(link);
				num = StringUtils.countMatches(pos, ",")+1;
				out.write(" "+num+", ");
			}
			out.newLine();
			out.write("--Body keywords: ");
			for(int x = 0; x < term[1].length; x++){
				out.write(term[1][x]);
				inv = (InvertedfileValue) bodytree.get(term[1][x]);
				pos = inv.list.get(link);
				num = StringUtils.countMatches(pos, ",")+1;
				out.write(" "+num+", ");
			}
			out.newLine();
			out.write("--Children: ");
			out.newLine();				
			//System.out.println(ID);
			ArrayList<LinkRelation> children = node.getChildren();
			//System.out.println(children.size());
			Iterator<LinkRelation> it = children.iterator();
			while(it.hasNext()){
				LinkRelation temp = it.next();
				out.write(temp.getUrl());
				out.newLine();
			}
			out.newLine();
			out.write("-----------------------------------");
			out.newLine();
			
		}	
		out.close();

		

	}

}
