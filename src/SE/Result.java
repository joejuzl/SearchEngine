package SE;
import java.util.Date;


public class Result {
	double score;
	String title;
	long date;
	int size;
	String url;
	String[] keywords;
	String[] parents;
	String[] children;
	
	public Result(double score, String title, String url, long date, int size,
			String[] keywords, String[] parents, String[] children) {
		super();
		this.score = score;
		this.title = title;
		this.url = url;
		this.date = date;
		this.size = size;
		this.keywords = keywords;
		this.parents = parents;
		this.children = children;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public long getDate() {
		return date;
	}
	public void setDate(long date) {
		this.date = date;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public String[] getKeywords() {
		return keywords;
	}
	public void setKeywords(String[] keywords) {
		this.keywords = keywords;
	}
	public String[] getParents() {
		return parents;
	}
	public void setParents(String[] parents) {
		this.parents = parents;
	}
	public String[] getChildren() {
		return children;
	}
	public void setChildren(String[] children) {
		this.children = children;
	}
	
	public void printData(boolean lots){
		System.out.println("Title: "+title);
		System.out.println("URL: "+url);
		System.out.println("Score: "+score);
		System.out.println("Size: "+size);
		System.out.println("Date: "+new Date(date));		
		if (lots){
			System.out.println("Children:");
			for(int i = 0; i < children.length;i++)
				System.out.println("	"+children[i]);
			System.out.println("Parents:");
			for(int i = 0; i < parents.length;i++)
				System.out.println("	"+parents[i]);
			System.out.println("Keywords:");
			for(int i = 0; i < keywords.length;i++)
				System.out.println("	"+keywords[i]);
		}
	}
	@Override 
	public boolean equals(Object other) {
        boolean eq = false;
        if (other instanceof Result) {
            Result that = (Result) other;
            eq = (this.getUrl().equals(that.getUrl()));
        }
        return eq;
    }

    @Override 
    public int hashCode() {
        return url.hashCode();
    }
	

}
