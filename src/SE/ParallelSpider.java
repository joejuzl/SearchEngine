package SE;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.htmlparser.beans.LinkBean;
import org.htmlparser.util.ParserException;


public class ParallelSpider {
	private LinkRelation start;
	private int noPages;
	private int IDCounter;
	private Hashtable<String, LinkRelation> visitedPages;
	private static final Executor executor = Executors.newFixedThreadPool(3);
	Queue<LinkRelation> nextPages; 
	
	public ParallelSpider(String startpage, int pages){
		noPages = pages;
		IDCounter = 0;
		start = new LinkRelation(IDCounter, null, startpage);
		visitedPages = new Hashtable<String, LinkRelation>();
		nextPages = new LinkedList<LinkRelation>();
	}	
	
	public LinkRelation crawl() throws ParserException{
		visitedPages.put(start.getUrl(),start);
		crawler(start);
		return start;
	}	
	
	public Runnable crawler(LinkRelation page) throws ParserException{
		//System.out.println(noPages + ":" + page.getUrl());
		if (noPages <= 0)
			return null;
		Vector<String> links = extractLinks(page.getUrl());
		Iterator<String> it = links.iterator();
		
		//threads?
		while (it.hasNext()){
			String address = it.next();			
			LinkRelation child;
			if (visitedPages.containsKey(address)){
				//call indexer update?
				child = (LinkRelation) visitedPages.get(address);
				child.addParent(page);
				page.addChild(child);	
			}
			else{	
				//call indexer
				noPages--;
				System.out.println(noPages + ":" + address);
				IDCounter++;
				child = new LinkRelation(IDCounter, page, address);
				visitedPages.put(address, child);
				page.addChild(child);
				nextPages.add(child);		
				
			}				
		}
		
		while (!nextPages.isEmpty())
			executor.execute(crawler(nextPages.remove()));
		
		return null;
	}
	
	public Vector<String> extractLinks(String url) throws ParserException

	{
		Vector<String> links = new Vector<String>();
	    LinkBean lb = new LinkBean();
	    lb.setURL(url);
	    URL[] linkArray = lb.getLinks();
	    for(int i=0; i<linkArray.length; i++){
	    	links.add(linkArray[i].toString());
	    }

		
		return links;
	}
}
