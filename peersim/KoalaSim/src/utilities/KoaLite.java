package utilities;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class KoaLite {
	public static String DB_Path = "out/koa.db";
	private static Connection c = null;
//	public static void main( String args[] )
//	{
//		createDB();
//		ArrayList<String> entries = new ArrayList<String>();
//		entries.add("120-0|310-3;[120-0 320-4 310-3];234234");
//		entries.add("120-2|310-3;[120-0 320-4 310-3];234234");
//		entries.add("120-4|310-3;[120-0 320-4 310-3];234234");
//		entries.add("120-1|310-3;[120-0 320-4 310-3];234234");
//		entries.add("120-8|310-3;[120-0 320-4 310-3];234234");
//		entries.add("120-9|310-3;[120-0 320-4 310-3];234234");
//		//insertBatch(entries);
//		
//		System.out.println(getPath("120-0","310-0"));
//		System.out.println(getAverageLatency());
//		
//		close();
//	}
	
	public static void close(){
		try {
			if(c==null) return;
			c.close();
		} catch (Exception e) {
		}
	}
	
	public static boolean dbExists(){
		
		try {
			Class.forName("org.sqlite.JDBC");
		
			c = DriverManager.getConnection("jdbc:sqlite:"+DB_Path);
			c.setAutoCommit(false);
			
			Statement stmt = c.createStatement();
			
			ResultSet rs = stmt.executeQuery( "SELECT name FROM sqlite_master WHERE name='DIJKSTRA';" );
			if(rs.next()){
				rs = stmt.executeQuery( "SELECT * FROM DIJKSTRA LIMIT 1 ;" );
				if(rs.next())
					return true;
			}
		}catch ( Exception e ) {
			  System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			  System.exit(0);
			}
		return false;
//		if(existed) rs = stmt.executeQuery( "SELECT * FROM DIJKSTRA LIMIT 1 ;" );
//		return Files.exists(Paths.get(DB_Path));
	}
	
	public static void createDB(){
		boolean existed = dbExists();
		
		try {
//			Class.forName("org.sqlite.JDBC");
//			c = DriverManager.getConnection("jdbc:sqlite:"+DB_Path);
//			c.setAutoCommit(false);
//			System.out.println("Opened database successfully");
			if(!existed){
				Statement stmt = c.createStatement();
				
				if(!existed){
				    String sql = "CREATE TABLE DIJKSTRA " +
				    			"(ID CHAR(100) PRIMARY KEY     NOT NULL," +
				    			" PATH           TEXT    NOT NULL, " + 
				    			" LATENCY        REAL     NOT NULL)";
				    stmt.executeUpdate(sql);
				    c.commit();
				    System.out.println("Table created successfully");
				}
				
				stmt.close();
			}
		} catch ( Exception e ) {
		  System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		  System.exit(0);
		}

	}
	
	
	public static void insertBatch(ArrayList<String> entries){
		
		Statement stmt = null;
		try {
		  stmt = c.createStatement();
		  for(String entry : entries){
			  String[] vals = entry.split(";");
			  String sql = "INSERT INTO DIJKSTRA (ID,PATH,LATENCY) " +
				  		"VALUES ('"+vals[0]+"', '"+vals[1]+"', "+vals[2]+");";
			  try{
				  stmt.executeUpdate(sql);
			  }catch ( Exception e ) {}
		  }
		  
		  stmt.close();
		  c.commit();
		  
		} catch ( Exception e ) {
		  System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		  System.exit(0);
		}
		
	}
	
	public static ArrayList<String> getPath(String src, String dst){
		ArrayList<String> sp = null;
		String id = NodeUtilities.getKeyStrID(src, dst);
		Object ret = getEntry(id, "path");
		if(ret != null){
			String path = (String)ret;
			path=path.replace("[", "").replace("]", "");
			sp = new ArrayList<String>(Arrays.asList(path.split(", ")));
			if(sp.get(0).equals(dst))
				Collections.reverse(sp);
		}
		return sp;
	}
	
	
	
	
	public static Double getLatency(String src, String dst){
		String id = NodeUtilities.getKeyStrID(src, dst);
		Object ret = getEntry(id, "lat");
		if(ret != null)
			return (Double)ret;
		return null;
	}
	
	public static Double getAverageLatency(){
		Double lat = null;
		try {
		      Statement stmt = c.createStatement();
		      ResultSet rs = stmt.executeQuery( "SELECT avg(latency) FROM DIJKSTRA;" );
		      
		      if ( rs.next() ) {
		         lat = rs.getDouble("avg(latency)");
		      }
		      rs.close();
		      stmt.close();
		      
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
		return lat;

	}
	
	public static Double getStdLatency(double avg){
		double sum = 0;
		double lat = 0;
		int i = 0;
		try {
		      Statement stmt = c.createStatement();
		      ResultSet rs = stmt.executeQuery( "SELECT latency FROM DIJKSTRA;" );
		      
		      while ( rs.next() ) {
		         lat = rs.getDouble("latency");
		         sum += Math.pow(lat - avg, 2);
		         i++;
		      }
		      rs.close();
		      stmt.close();
		      
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
		return Math.sqrt(sum/i);

	}
	
	
	private static Object getEntry(String id, String pathorLat){
		String path=null;
		Double lat = null;
		try {
		      Statement stmt = c.createStatement();
		      ResultSet rs = stmt.executeQuery( "SELECT * FROM DIJKSTRA WHERE ID='"+id+"';" );
		      
		      if ( rs.next() ) {
		         
		         path = rs.getString("PATH");

		         lat = rs.getDouble("latency");
		      }
		      rs.close();
		      stmt.close();
		      
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
		if(pathorLat.equals("path"))
			return path;
		return lat;
	}
	
	
}
