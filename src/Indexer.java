import java.io.IOException;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Indexer {
	private String url;
	private RecordManager recman;
	private HTree linktable;
	private HTree titletable;
	private HTree bodytable;

	private String[] titlekw;
	private String[] bodykw;
	private StopStem stopstem;
	private String pagetitle;

	
	public Indexer(String _url) throws IOException
	{
		//initialize
		url = _url;
		stopstem=new StopStem();
		
		String recordmanager="comp4321project";
		recman = RecordManagerFactory.createRecordManager(recordmanager);
		//used to empty jdbm
	//	if(deleterecord("linktable")) printerr("linktable removed");
	//	if(deleterecord("titletable")) printerr("titletable removed ");
	//	if(deleterecord("bodytable")) printerr("bodytable removed ");
		//recman.commit();

		//update linktable
		long recid = recman.getNamedObject("linktable");
		if (recid != 0){
				System.out.println("fetched object for "+recid+" ");//test
				linktable=HTree.load(recman, recid);
		}else{
			linktable = HTree.createInstance(recman);
			recman.setNamedObject( "linktable", linktable.getRecid() );
			printerr("new recid for linktable: "+linktable.getRecid());
		}


		String[][] linktablevalue=(String[][]) linktable.get(url);
		if(linktablevalue!=null){
			//link exist already, delete keywords in invertedfile
			removeInvertedfile(titletable,linktablevalue[0],"titletable");
			removeInvertedfile(bodytable,linktablevalue[1],"bodytable");
			System.out.println("invertedfile is removed (as first step of update) for link "+url);//test
		}else{
			linktablevalue=new String[2][];
			printerr("new linktablevalue"+url);//test
		}
		//add link
		//get title and body keywords
		parseURL();
		linktablevalue[0]=titlekw;
		linktablevalue[1]=bodykw;
		linktable.put(url, linktablevalue);
		//recman.commit();//needed?
		System.out.println("end of updating linktable");
	
		//update inverted file
		updateInvertedfile(titletable,titlekw,"titletable");
		System.out.println("end of updating title invertedfile");
		updateInvertedfile(bodytable,bodykw,"bodytable");	
		System.out.println("end of updaing body ssinvertedfile");

		this.finalize();
	}
	public void removeInvertedfile(HTree invertedtable, String[] kw, String filename) throws IOException{
		long recid =recman.getNamedObject(filename);
		if(recid!=0){
			invertedtable=HTree.load(recman, recid);
		}else{
			invertedtable = HTree.createInstance(recman);
			recman.setNamedObject(filename, invertedtable.getRecid());
			printerr("new recid for invertedtable: "+invertedtable.getRecid());
			return;//since the inverted file not even exist
		}
		for(int i=0;i<kw.length;i++){
			InvertedfileValue invertedfilevalue=(InvertedfileValue) invertedtable.get(kw[i]);
			if(invertedfilevalue==null){
				printerr("previous stored kw hasn't been indexed!!!!!");
			}else{
				invertedfilevalue.remove(url);
			}
		}
		//recman.commit();
	}
	public void updateInvertedfile(HTree invertedtable, String[] kw,String filename) throws IOException {
		long recid =recman.getNamedObject(filename);
		if(recid!=0){
			invertedtable=HTree.load(recman, recid);
		}else{
			invertedtable = HTree.createInstance(recman);
			recman.setNamedObject(filename, invertedtable.getRecid());
			System.out.println("HASH TREE CREATED: "+ filename+ " " +invertedtable.getRecid());
			
		}
		//insert keyword one by one
		for(int i=0;i<kw.length;i++){
			InvertedfileValue list=(InvertedfileValue) invertedtable.get(kw[i]);
			if(list==null){
				list=new InvertedfileValue();
				list.add(url, i);
				invertedtable.put(kw[i], list);
			}else{
				if(!list.add(url, i)) printerr("the postion of kw is duplicated!!!!!");	
				invertedtable.put(kw[i], list);
			}
		//	System.out.println("update kw "+kw[i]+"; size of list is "+list.size+"; String is "+ list.get(url));//test
		}	
		//recman.commit();
	}
	public void printerr(String str){
		System.out.println(str);
	}
	public boolean deleterecord(String objectname) throws IOException{//return true if record existed before deletion
		long recid = recman.getNamedObject(objectname);
		if (recid != 0){
			recman.delete(recid);
			recman.setNamedObject(objectname, 0);
			return true;
		}
		return false;
	}
	public String getpagetitle(){
		return pagetitle;
	}
	public void parseURL() throws IOException{

		Document document = Jsoup.connect(url).timeout(0).get();

		Element title = document.select("title").first();
		//insert into pagetitletable
		pagetitle=title.text();
		
		//replace all non-letter,numeric with space
		String delim="[ ]+";
		titlekw=title.text().replaceAll("[^\\p{L}\\p{N}]", " ").split(delim);
//		System.out.println("Title: " +title.text());
//		System.out.println("Title: " +title.text().replaceAll("[^\\p{L}\\p{N}]", " ") );

		Element body = document.select("body").first();
		bodykw=body.text().replaceAll("[^\\p{L}\\p{N}]", " ").split(delim);

//		System.out.println("Body: " +body.text());
//		System.out.println("Body: " +body.text().replaceAll("[^\\p{L}\\p{N}]", " ") );

		titlekw=filterkw(titlekw);
		bodykw=filterkw(bodykw);
/*		for(int i=0;i<bodykw.length;i++){
			System.out.println(bodykw[i]);
		}
*/
	}
	
	public String[] filterkw(String[] kw){
		String[] newkw;
		int j=0;
		
		for(int i=0;i<kw.length;i++){
			//remove stop words
			if(stopstem.isStopWord(kw[i])|| kw[i].equals("")){
				kw[i]=null;
			}else{
//				System.out.println(kw[i]);//test
				kw[i]=stopstem.stem(kw[i]);
				j++;
			}
		}
		newkw=new String[j];
		j=0;
		for(int i=0;i<kw.length;i++){
			if(kw[i]!=null){
				newkw[j]=kw[i];
				++j;
			}
		}
		return newkw;

	}
	

	public void finalize() throws IOException
	{
		recman.commit();
		recman.close();				
	} 
	
	/*
	public static void main (String[] args) throws IOException
	{
		//Indexer indexer1=new Indexer("http://www.ust.hk/");
		//Indexer indexer2=new Indexer("http://www.rutgers.edu/");
		
	}
	*/
	
}
