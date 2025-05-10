package br.usp.icmc.labes.mealyInference.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ProcessLogFiles {
	
	private static final String REUSED_QUERIES_SYMBOLS = "Reused queries [symbols]:";
	private static final String REUSED_QUERIES_RESETS = "Reused queries [resets]:";
	private static final String READING_OT = "Reading OT:";
	private static final String INFO = "Info:";
	private static final String EQUIVALENT = "Equivalent:";
	private static final String ISIZE = "Isize:";
	private static final String QSIZE = "Qsize:";
	private static final String SEARCHING_CEX = "Searching for counterexample [ms]:";
	private static final String LEARNING_MS = "Learning [ms]:";
	private static final String EQ_SYMBOLS = "EQ [symbols]:";
	private static final String EQ_RESETS = "EQ [resets]:";
	private static final String MQ_SYMBOLS = "MQ [symbols]:";
	private static final String MQ_RESETS = "MQ [resets]:";
	private static final String ROUNDS = "Rounds:";
	private static final String METHOD = "Method:";
	private static final String EQ_ORACLE = "EquivalenceOracle:";
	private static final String OT_HANDLER = "ObservationTableCEXHandler:";
	private static final String CLOS = "ClosingStrategy:";
	private static final String CACHE = "Cache:";
	private static final String SEED = "Seed:";
	private static final String SUL_NAME = "SUL name:";
	private static final String RUZD_PREF = "Reused prefixes:";
	private static final String RUZD_SUFF = "Reused suffixes:";
	
	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

	private static final List<String> mapIdx = new ArrayList<>();
	
	public static void main(String[] args) {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		
		Map<String,Map<String,String>> logMap = new HashMap<>();
		String lineRead;
		try {
			createLogMapIndex();			
			
			List<String> outLine = new ArrayList<>();
			mapIdx.forEach(col -> outLine.add(col.replace(":", "")));
			System.out.println(String.join("|",outLine)); outLine.clear();
			
			while ((lineRead=br.readLine())!=null) {
				logMap.clear();
				File f = new File(lineRead);
				try { loadLogIntoMap(f, logMap); } 
				catch (Exception e) { e.printStackTrace(); }
				completeLogMap(f,logMap);
				String key = f.getAbsolutePath()+"/"+f.getName();
				mapIdx.forEach(col -> outLine.add(logMap.get(key).get(col.replace(":", ""))));
				System.out.println(String.join("|",outLine)); outLine.clear();
			}
			//saveLogMap(logMap);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	private static void saveLogMap(Map<String, Map<String, String>> logMap) throws FileNotFoundException, IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("./logmap"+sdf.format(new Date())+".csv")));
		
		for (String col : mapIdx) {
			col = col.replace(":", "");
			bw.append(col);
			if(mapIdx.indexOf(col)==mapIdx.size()-1) {
				bw.append("\n");
			}else {
				bw.append("\t");
			}
		}
		
		for (String line : logMap.keySet()) {
			for (String col : mapIdx) {
				col = col.replace(":", "");
				bw.append(logMap.get(line).get(col));
				if(mapIdx.indexOf(col)==mapIdx.size()-1) {
					bw.append("\n");
				}else {
					bw.append("\t");
				}
			}
		}
		bw.close();
	}

	private static void completeLogMap(File f, Map<String, Map<String, String>> logMap) {
		String key = f.getAbsolutePath()+"/"+f.getName();
		for (String idx : mapIdx) {
			logMap.get(key).putIfAbsent(idx, "");			
		}		
	}

	private static void createLogMapIndex() {
		
		mapIdx.add(SUL_NAME);
		mapIdx.add(SEED);
		mapIdx.add(CACHE);
		mapIdx.add(CLOS);
		mapIdx.add(OT_HANDLER);
		mapIdx.add(EQ_ORACLE);
		mapIdx.add(METHOD);
		mapIdx.add(READING_OT);
		mapIdx.add(REUSED_QUERIES_RESETS);
		mapIdx.add(REUSED_QUERIES_SYMBOLS);
		mapIdx.add(ROUNDS);
		mapIdx.add(MQ_RESETS);
		mapIdx.add(MQ_SYMBOLS);
		mapIdx.add(EQ_RESETS);
		mapIdx.add(EQ_SYMBOLS);
		mapIdx.add(LEARNING_MS);
		mapIdx.add(SEARCHING_CEX);
		mapIdx.add(QSIZE);
		mapIdx.add(ISIZE);
		mapIdx.add(EQUIVALENT);
		mapIdx.add(INFO);
		mapIdx.add(RUZD_PREF);
		mapIdx.add(RUZD_SUFF);
	}

	private static void loadLogIntoMap(File f, Map<String, Map<String, String>> logMap) throws FileNotFoundException, IOException {
		String line = f.getPath()+"/"+f.getName();
		logMap.putIfAbsent(line, new HashMap<>());
		
		BufferedReader br = new BufferedReader(new FileReader(f));
		
		Set<String> mapIdx_clone = new HashSet<>(mapIdx);
		String input;
		String[] split = null;
		int posMatch;
		while (br.ready()) {
			input = br.readLine();
			for (String col : mapIdx_clone) {
				posMatch = input.indexOf(col);
				if(posMatch>-1) {
					split = input.substring(posMatch).split(":");
					split[1]=split[1].replaceFirst("^\\ +", "");
					logMap.get(line).put(split[0], split[1]);
					mapIdx_clone.remove(col);
					break;
				}
			}
		}
		br.close();
	}

}
