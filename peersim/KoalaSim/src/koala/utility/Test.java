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
		
		int total = 3000;
		int categories = 50;
		int mean = total/categories;
		int std = mean;
		double min = 0.1 * mean;
		
		ArrayList<Integer> meas = new ArrayList<Integer>();
		int sum = 0;
		for(int i=0; i < categories; i++){
			int val = (int)(r.nextGaussian()*std + mean);
			if (val < min)
				val = (int)min;
			meas.add(val);
			sum += val;
		}
		
		
		int diff = sum - total;
		
		if(diff != 0){
			for(int i =0; i < Math.abs(diff); i++){
				int index = r.nextInt(meas.size()-1);
				if(meas.get(index)>1){ 
					if(diff > 0)
						meas.set(index, meas.get(index)-1);
					else
						meas.set(index, meas.get(index)+1);
				}else
					i--;
				
			}
		}
		Collections.sort(meas);
		sum = 0;
		for(int i = 0; i < meas.size(); i++){
			sum += meas.get(i);
			System.out.println(meas.get(i));
		}
		System.out.println("--------------");
		System.out.println(sum);
//		double[] cats = new double[categories];
//		cats[0] = mean - 4*std;
//		cats[cats.length-1] = mean + 4*std;
//		double unit = (double)(cats[cats.length-1] - cats[0]) /categories;
//		
//		for(int i = 1; i < cats.length-1; i++){
//			cats[i] = (i * unit);
//			
//		}
////		for(int i = 0; i < cats.length; i++)
////			System.out.println(cats[i]);
////		
//		int[] ret = new int[categories];
//		
//		for(int i=0; i < total; i++){
//			double next = r.nextGaussian()*std + mean;
//			for(int j = 0; j < cats.length-1; j++){
//				if(next >= cats[j] && next < cats[j+1]){
//					ret[j]++;
//					break;
//				}
//			}
//		}
//		
//		for(int i = 0; i < ret.length; i++)
//			System.out.println(ret[i]);
//		
//		ArrayList<Double> meas = new ArrayList<Double>();
//		for(int i=0; i < total; i++){
//			meas.add(r.nextGaussian()*15 + mean);
//		}
//		Collections.sort(meas);
//		double min = Collections.min(meas);
//		double max = Collections.max(meas);
//		double diff= max-min;
//		double unit = (double)diff/categories;
//		//unit = 2;
//		double start = min;
//		double end = start + unit;
//		int j= 0;
//		ArrayList<Integer> dist = new ArrayList<Integer>();
//		
//		for(int i=0; i < meas.size(); i++){
//			if(meas.get(i) >= start && meas.get(i) < end){
//				if(dist.size() == j)	
//					dist.add(j, 0);
//				dist.set(j, dist.get(j)+1);
//			}else{
//				j++;
//				start = end;
//				end = start + unit;
//			}
//			//content.add(i+ " " + r.nextGaussian()*15+60);
//			
//		}
//		
//		for(int o : dist){
//			System.out.println(o);
//		}
//		
//		printToFile(ret);
//		plotIt("gnuplot/simple.plt");
		
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
	
	private static void printToFile(int[] content){
		ArrayList<String> strcontent = new ArrayList<String>();
		for(int i = 0; i < content.length; i++)
			strcontent.add(i+" " + content[i]);
		printToFile( strcontent);
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
