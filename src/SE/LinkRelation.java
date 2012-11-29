package SE;
import java.util.ArrayList;

public class LinkRelation implements java.io.Serializable, Comparable<LinkRelation>  {


	private static final long serialVersionUID = 1L;
	private int pageID;
	private String url;	
	private ArrayList<LinkRelation> parents;
	private ArrayList<LinkRelation> children;
	private Integer rank;
	private String pageTitle;
	private long lastModified;
	private int pageSize;
	
	public LinkRelation(int ID, LinkRelation parent, String address){
		pageID = ID;
		url = address;
		parents = new ArrayList<LinkRelation>();
		children = new ArrayList<LinkRelation>();
		if(parent != null){
			addParent(parent);
		}
	

	}
	
	public void addParent(LinkRelation parent){		
		try {
			if (!parents.contains(parent))
				parents.add(parent);		
		}
		catch (NullPointerException e){
			System.out.println(e.getMessage());
		}
	}
	
	public void addChild(LinkRelation child){
		try {
			if (!children.contains(child))
				children.add(child);		
		}
		catch (NullPointerException e){
			System.out.println(e.getMessage());
		}
		
	}
	
	public int getPageID() {
		return pageID;
	}

	public String getUrl() {
		return url;
	}

	public ArrayList<LinkRelation> getParents() {
		return parents;
	}

	public ArrayList<LinkRelation> getChildren() {
		return children;
	}

	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}



	@Override
	public int compareTo(LinkRelation o) {
		return this.rank - o.getRank();
	}




}
