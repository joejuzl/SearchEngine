import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;


public class PageRank {
	private Integer[] PR;
	private Hashtable<String, LinkRelation> graph;	
	
	public PageRank(Hashtable<String, LinkRelation> g){			
		graph = g;
		PR = new Integer[graph.size()];		
		for (int x = 0; x < PR.length;x++)
			PR[x] = 1;		
	}
	
	public void calcRank(){
		Boolean change = true;		
		while (change == true){
			change = false;		
			Enumeration<LinkRelation> enumr = graph.elements();
			while (enumr.hasMoreElements()){				
				LinkRelation link = enumr.nextElement();
				int oldPR = PR[link.getPageID()];
				int newPR = 1;
				ArrayList<LinkRelation> parents = link.getParents();
				Iterator<LinkRelation> it = parents.iterator();				
				while (it.hasNext()){
					LinkRelation parent = it.next();
					newPR += PR[parent.getPageID()]/parent.getChildren().size();
				}
				if (oldPR != newPR)
					change = true;
				PR[link.getPageID()] = newPR;
			}			
		}
		Enumeration<LinkRelation> enumr = graph.elements();
		while (enumr.hasMoreElements()){	
			LinkRelation link = enumr.nextElement();
			link.setRank(PR[link.getPageID()]);
		}
		
	}
}
