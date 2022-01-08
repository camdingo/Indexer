package edu.ucr.indexer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.ucr.indexer.snowball.SnowballStemmer;
import edu.ucr.indexer.snowball.porterStemmer;

public class MyServlet extends HttpServlet {
	
	public class Pair<String, Float> {
	    private String first;
	    private Float second;

	    public Pair(String first, Float second) {
	        super();
	        this.first = first;
	        this.second = second;
	    }

	   
	    public String getFirst() {
	        return first;
	    }

	    public void setFirst(String first) {
	        this.first = first;
	    }

	    public Float getSecond() {
	        return second;
	    }

	    public void setSecond(Float second) {
	        this.second = second;
	    }
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processRequest(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processRequest(req, resp);
	}
	static {
		index.init("C:\\Users\\Cameron\\Documents\\webpages");
	}
	private static SnowballStemmer _stemmer = new porterStemmer();
	
	//class MyComparator implements Comparator<Pair>{
	//	  public int compare(Pair ob1, Pair ob2){
	//	   return ob1.second > ob2.second ;
	//	  }
	//	}
	
	private void processRequest(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		PrintWriter out = resp.getWriter();
		
		String query = req.getParameter("query");
		String unstemmed = query;
		
		String cleaned = "";
		StringTokenizer st = new StringTokenizer(query);
		ArrayList <String> words = new ArrayList<String> ();
		while (st.hasMoreTokens()) {
			_stemmer.setCurrent(st.nextToken());
			_stemmer.stem();
			 cleaned += _stemmer.getCurrent() + " ";
			 
		}
		
		
		out.write("<html>");
		out.write("<head>");
		out.write("<title> CS 172 Search </title>");
		out.write("</head>");
		out.write("<body>");

		out.write("<h1>Search results for: "+unstemmed + " cleaned as: "+ cleaned + "</h1>");
		
		//List<Pair> output = new ArrayList<Pair>();
		int num =1;
		for (Entry<String, Float> entry : index.query(cleaned).entrySet()) {
			//Pair<String, Float> a = Pair(entry.getKey(), entry.getValue());
			//output.add(a);
			out.write("<p>");
			out.write(num + ") " + entry.getKey()+ " "+ entry.getValue());
			out.write("</p>");
			num++;
		}
		//Collections.sort(output, new MyComparator());
		

		
		out.write("</body>");
		out.write("</html>");

		out.flush();
		out.close();
	}



}
