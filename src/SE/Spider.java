package SE;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

import org.htmlparser.beans.LinkBean;
import org.htmlparser.util.ParserException;


public class Spider {
	private LinkRelation start;
	private int noPages;
	private int IDCounter;
	private Hashtable<String, LinkRelation> visitedPages;
	Queue<LinkRelation> nextPages; 
	private RecordManager recman;
	private Indexer indexer;
	private Hashtable<String, LinkRelation> oldGraph;
	public Spider(String startpage, int pages) throws IOException{		
		noPages = pages;
		IDCounter = 0;
		start = new LinkRelation(IDCounter, null, startpage);
		visitedPages = new Hashtable<String, LinkRelation>();
		nextPages = new LinkedList<LinkRelation>();		
		recman = RecordManagerFactory.createRecordManager(SEngine.getPath());
		long recid = recman.getNamedObject("linkGraph");
		if (recid != 0)
			oldGraph = (Hashtable<String, LinkRelation>) recman.fetch(recid);		
	}	
	
	public void crawl() throws ParserException, MalformedURLException, IOException{
		visitedPages.put(start.getUrl(),start);
		recman.commit();
		indexPage(start);
		crawler(start);	
		storeData();
		recman.close();		
		//return visitedPages;
	}
	
	public void crawler(LinkRelation page) throws ParserException, MalformedURLException, IOException{
		//System.out.println(noPages + ":" + page.getUrl());	
		if(noPages%30 == 0)
			storeData();
		if (noPages <= 0)
			return;
		Vector<String> links = extractLinks(page.getUrl());
		Iterator<String> it = links.iterator();			
		while (it.hasNext()){
			String address = it.next();			
			LinkRelation child;
			if (visitedPages.containsKey(address)){
				child = (LinkRelation) visitedPages.get(address);
				child.addParent(page);
				page.addChild(child);	
			}
			else{	
				if (noPages <= 0)					
					return;	
				noPages--;				
				System.out.println(noPages + ":" + address);	
				IDCounter++;
				child = new LinkRelation(IDCounter, page, address);
				visitedPages.put(address, child);				
				page.addChild(child);				
				nextPages.add(child);				
				indexPage(child);					
				}
			}				
		
		if (nextPages.isEmpty())
			return;
		crawler(nextPages.remove());		
	}
	
	private Vector<String> extractLinks(String url) throws ParserException

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
	
	private void indexPage(LinkRelation node) throws MalformedURLException, IOException{		
		URLConnection conn = new URL(node.getUrl()).openConnection();		
		Long newDate = conn.getLastModified();
		Long oldDate = (long) 0;
		if (oldGraph != null){				
				if (oldGraph.get(node.getUrl()) != null)
					oldDate = oldGraph.get(node.getUrl()).getLastModified();
		}
		//Needs to access the database to check date!!!!!!!	
		
		
		if(oldDate == 0 || newDate == 0 || oldDate == null || newDate == null || (newDate >= oldDate)){		
			indexer=new Indexer(node.getUrl(),recman);
			node.setLastModified(newDate);
			node.setPageSize(conn.getContentLength());
			node.setPageTitle(indexer.getpagetitle());			
		}		
		else{
			//System.out.println("oops!!!!!!!!! OLD DATE: "+ oldDate + " NEW DATE: "+newDate);
			node.setLastModified(newDate);
			node.setPageSize(conn.getContentLength());
			node.setPageTitle(indexer.getpagetitle());		
		}
	}
	
	private void storeData() throws IOException{
		long recID;	
		
		recID = recman.getNamedObject("linkGraph");
		if (recID != 0) {
			recman.update(recID, visitedPages);
		}
		else {
			recID = recman.insert(visitedPages);
			recman.setNamedObject("linkGraph", recID);
		}	
		
		recman.commit();	
		
	}
}
