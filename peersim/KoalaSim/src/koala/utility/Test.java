package koala.utility;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Test {
	
	
	
	public static void main(String[] args)
	{
		ArrayList<String> content = new ArrayList<String>();
		Random r = new Random();
		
		int total = 100;
		int categories = 10;
		ArrayList<Double> meas = new ArrayList<Double>();
		for(int i=0; i < total; i++){
			meas.add(r.nextGaussian()*15 + 60);
		}
		Collections.sort(meas);
		double min = Collections.min(meas);
		double max = Collections.min(meas);
		double diff= max-min;
		double unit = (double) diff/categories;
		
		for(int i =0; i < 10; i++){
			//content.add(i+ " " + r.nextGaussian()*15+60);
			System.out.println(i+ " " + ((r.nextGaussian()*15) + 60));
		}
		
		//printToFile(content);
		//plotIt("gnuplot/simple.plt");
	}
	
	
	private static void printToFile(ArrayList<String> content){
		FileOutputStream fos;
		try {
			fos = new FileOutputStream("out/test.dat");
			PrintStream ps = new PrintStream(fos);
			
			for(String line: content){
				ps.println(line);
			}
			fos.close();
            ps.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	protected static void plotIt(String script){
		try {
			Process p = new ProcessBuilder("gnuplot", "-persistent", script).start();
//			BufferedReader reader = new BufferedReader (new InputStreamReader(p.getErrorStream()));
//			String line;
//			while ((line = reader.readLine ()) != null) {
//				System.out.println ("Stdout: " + line);
//			}

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
