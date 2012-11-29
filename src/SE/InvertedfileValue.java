package SE;
import java.util.Hashtable;

public class InvertedfileValue implements java.io.Serializable {

	public int size;
	public Hashtable<String,String> list;
	public InvertedfileValue(){
		size=0;
		list=new Hashtable<String,String>();
	}
	public boolean add(String url, int pos){//return true if not exist before
		String posstring=list.get(url);
		if(posstring!=null){
			//check duplicate
			String[] poss=posstring.split("[,]+");
			for(int i=0;i<poss.length;i++){
				if(poss[i].equals(pos+"")) return false;
			}
			list.put(url, posstring+","+pos);
		}else{
			list.put(url, ""+pos);
		}
		++size;	
		//System.out.println("the url("+url+") is added and the size is "+size+" String is "+list.get(url) );//test
		return true;
	}
	public boolean remove(String url){//return true if url exist, return false if nothing to remove
		String posstring=list.remove(url);
		if(posstring!=null){
			String[] poss=posstring.split("[,]+");
			size-=poss.length;
			return true;
		}
		return false;
	}
	public String get(String url){
		return list.get(url);
	}
}
