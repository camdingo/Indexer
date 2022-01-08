package edu.ucr.indexer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.text.html.parser.ParserDelegator;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.MutableAttributeSet;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import edu.ucr.indexer.snowball.SnowballStemmer;
import edu.ucr.indexer.snowball.StopKeywords;
import edu.ucr.indexer.snowball.porterStemmer;


public class index {
  private index() {}

  static Directory directory;
  static Analyzer analyzer;
  static String query;
  static List<String> dl_urls = new ArrayList<String> ();
  static List<String> titles = new ArrayList<String> ();
  static HashMap<String, String> associations = new HashMap<String, String>();
  //static HashMap<String, Float> results = new HashMap<String, Float>();
  
  public static List<String> extractText(Reader reader) throws IOException {
    final ArrayList<String> list = new ArrayList<String>();

    ParserDelegator parserDelegator = new ParserDelegator();
    ParserCallback parserCallback = new ParserCallback() {
    	boolean inScript = false;
    	boolean isTitle = false;
    	boolean morethanone = false;
      public void handleText(final char[] data, final int pos) {
    	  if(inScript){
    		  return;
    	  }
    	  if(isTitle && !morethanone){
    		  String titl = "";
    		  for(int i =0;i <data.length; i++){
    			  titl += data[i];
    		  }
    		titles.add(titl);
    		morethanone = true;
    	  }
    	  
        list.add(new String(data));
      }
      public void handleStartTag(Tag tag, MutableAttributeSet attribute, int pos) {
    	  if(tag == tag.SCRIPT){
    		  inScript = true;
    	  }
    	  if(tag == tag.TITLE){
    		  isTitle = true;
    	  }
      }
      public void handleEndTag(Tag t, final int pos) {
    	  if(t == t.SCRIPT){
    		  inScript = false;
    	  }
    	  if(t == t.TITLE){
    		  isTitle = false;
    	  }
      }
      public void handleSimpleTag(Tag t, MutableAttributeSet a, final int pos) { }
      public void handleComment(final char[] data, final int pos) { }
      public void handleError(final java.lang.String errMsg, final int pos) { }
    };
    parserDelegator.parse(reader, parserCallback, true);
    
    return list;
  }


  
  public static void add(String url, String title, String content)
  throws CorruptIndexException, LockObtainFailedException, IOException{


	IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(	
			Version.LUCENE_33, analyzer));  
  
	Document doc = new Document();
	
	doc.add(new Field("title", title, Field.Store.YES,
			Field.Index.NOT_ANALYZED_NO_NORMS));
	doc.add(new Field("url", ""+url, Field.Store.YES,
			Field.Index.NOT_ANALYZED_NO_NORMS));
	doc.add(new Field("content", content, Field.Store.NO,
			Field.Index.ANALYZED));
	
	writer.addDocument(doc);
	writer.commit();
	writer.close();
  }
  
  
  static void init(String d){
	  String dirName = d;
	  try{
		  directory = new SimpleFSDirectory(new File(dirName),
				  NoLockFactory.getNoLockFactory());
		  	  
		  // MUST HAVE FILE dl_urls.txt WHICH CONTAINS THE LINKS IN ORDER OF THE FILES ITS PROCESSING (ie http://www.cs.ucr.edu)
		 
		  FileInputStream fstream = new FileInputStream(d+"\\association.txt");
		  // Get the object of DataInputStream
		  DataInputStream in = new DataInputStream(fstream);
		  BufferedReader br = new BufferedReader(new InputStreamReader(in));
		  String strLine;
		  
		  //Read File Line By Line
		  while ((strLine = br.readLine()) != null)   {
			  String[] a = strLine.split("\t");
			  //key = file value = url
			  associations.put(a[1],a[0]);
		  }
		  //Close the input stream
		  in.close();
		  
	  }catch (IOException e){
		  e.printStackTrace();
	  }
	  analyzer = new StandardAnalyzer(Version.LUCENE_33);
  }
 
 private static SnowballStemmer _stemmer = new porterStemmer();
  

 
	public static Map<String, Float> query(String q){
		IndexSearcher searcher;
		Map<String, Float>  map = new HashMap<String, Float>(); 
		
		try {
			searcher = new IndexSearcher(directory, true);

			QueryParser parser = new QueryParser(Version.LUCENE_33, "content",
					analyzer);

			Query q2 = parser.parse(q.toLowerCase());
			TopScoreDocCollector collector = TopScoreDocCollector.create(10,true);
			searcher.search(q2, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			
			for (int j = 0; j < hits.length; j++) {
				int docId = hits[j].doc;
				Document d = searcher.doc(docId);
				//String url = d.get("url");
				String title = d.get("title");
				//do something
				map.put(title, hits[j].score);
				
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	
 
  public final static void main(String[] args) throws Exception{
    //Loads Files and Set Directory
	init(args[0]);
	int docnum = 0;
	File dl_directory = new File(args[0] +"\\" + args[1]);
	File fileName[] = dl_directory.listFiles();
	ArrayList <String> words = new ArrayList<String> ();
	List<String> cleanedWords = new ArrayList<String> ();
	int titlesize =0;
	//Goes through each file in the folder
	for(File f : fileName){

			FileReader reader = new FileReader(f);            
	
			//Parses File into lines
			titlesize = titles.size();
			List<String> lines = index.extractText(reader);
			if(titlesize == titles.size()){
				titles.add("No Title");
			}
			
			String test = f.getPath().substring(27);
			test = test.replace('\\' , '/');
			dl_urls.add(associations.get(test));
			
			//Parse each line into tokens
			for (String line : lines) {
				StringTokenizer st = new StringTokenizer(line);
				while (st.hasMoreTokens()) {
					//If the word is a stop word then dont output  
					words.add(st.nextToken());
				}
			}
		
			
			//Remove stop words and stem
			for(int i =0; i<words.size();i++){
				if(!StopKeywords.isStopWord(words.get(i))){
					String w = words.get(i);
					w = clean(w);
					_stemmer.setCurrent(w);
					_stemmer.stem();
					cleanedWords.add(_stemmer.getCurrent());
				}
			}
			
			
			//Convert array to string
			String listString = "";
			for (String s : cleanedWords)
			{
				if(!s.contains(" ") || !s.contains("") ){
					listString += s + " " ;
				}
			}

			// ADD TO LUCENE
			index.add(dl_urls.get(docnum), titles.get(docnum), listString);
			
			//Clean up
			cleanedWords.clear();
			words.clear();
			docnum++;	
		 
 		}
        
}
  

private static String clean(String w) {
	w = w.replace('/', ' ');
	w = w.replace('"', ' ');
	w = w.replace('>',' ');
	w = w.replace('<', ' ');
	w = w.replace('@', ' ');
	w = w.replace('[', ' ');
	w = w.replace(']', ' ');
	w = w.replace(',', ' ');
	w = w.replace('.', ' ');
	w = w.replace(':', ' ');
	w = w.replace(')', ' ');
	w = w.replace('(', ' ');
	w = w.replace('&', ' ');
	w = w.replace('^', ' ');
	
	if(w.contains("?") || w.contains("www1") || w.contains("=") ){
		w = "";
	}
	return w;

}
}

