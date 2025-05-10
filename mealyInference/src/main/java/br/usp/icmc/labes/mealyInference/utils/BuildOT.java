package br.usp.icmc.labes.mealyInference.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.automata.transducers.impl.compact.CompactMealyTransition;
import net.automatalib.commons.util.comparison.CmpUtil;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import br.usp.icmc.labes.mealyInference.Infer_LearnLib;
import br.usp.icmc.labes.mealyInference.utils.Utils;

public class BuildOT {

	private static final String SUL = "sul";
	private static final String HELP = "h";
	private static final String SUL_AS_KISS = "kiss";
	private static final String SUL_AS_DOT = "dot";
	private static final String RANDOMNESS = "rnd";
	private static final String OUT_DIR = "out";
	private static final String MK_OT = "mkot";

	private static boolean shuffle_abc = false;
	
	
	public static void main(String[] args) {

		// create the command line parser
		CommandLineParser parser = new BasicParser();
		// create the Options
		Options options = createOptions();
		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();

		try {
			// parse the command line arguments
			CommandLine line = parser.parse( options, args);
			
			if(!line.hasOption(SUL)) throw new Exception("The model of a SUL is required!");
			
			File f = new File(line.getOptionValue(SUL));
			
			CompactMealy<String, Word<String>> mealy = null;
			
			if(line.hasOption(SUL_AS_DOT)) {
				InputModelDeserializer<String, CompactMealy<String, Word<String>>> mealy_parser = DOTParsers.mealy(Infer_LearnLib.MEALY_EDGE_WORD_STR_PARSER);
				mealy = mealy_parser.readModel(f).model;
			}else if(line.hasOption(SUL_AS_KISS)) {
				mealy = Utils.getInstance().loadMealyMachine(f);				
			}else {
				mealy = Utils.getInstance().loadMealyMachine(f);
			}
			
			File out_dir = f.getParentFile();
			if(line.hasOption(OUT_DIR)) {
				out_dir = new File(line.getOptionValue(OUT_DIR));
				out_dir.mkdirs();
			}

			
			Utils.getInstance().saveMealyMachineAsKiss(mealy, new File(out_dir,f.getName().replaceAll("(\\.[(dot)(txt)])+$", "")+".kiss"));
			
			if(line.hasOption(MK_OT)) {
				
				Map<Integer, Word<String>> accessStringMap = new HashMap<>();
				Integer initState = mealy.getInitialState();
				accessStringMap.put(initState, Word.epsilon());
				
				if(line.hasOption(RANDOMNESS)) shuffle_abc = true;
				
				dfs(mealy,initState,accessStringMap);
				
				List<Word<String>> accessString = new ArrayList<>(accessStringMap.values());
				Alphabet<String> abc = mealy.getInputAlphabet();
				Collections.sort(accessString, new Comparator<Word<String>>() {
		            @Override
		            public int compare(Word<String> o1, Word<String> o2) { return CmpUtil.lexCompare(o1, o2, abc ); }
		        });
				
				//System.out.println(accessString);
				
				BufferedWriter bw_ot = new BufferedWriter(new FileWriter(new File(out_dir,f.getName().replaceAll("(\\.[(dot)(txt)])+$", "")+".ot")));
	
				for (int i = 1; i < accessString.size(); i++) {
					bw_ot.append(";");
					bw_ot.append(accessString.get(i).getSymbol(0));
					for (int j = 1; j < accessString.get(i).size(); j++) {
						bw_ot.append(",");
						bw_ot.append(accessString.get(i).getSymbol(j));
					}
				}
				bw_ot.append("\n");
				List<Word<String>> listOfSuff = Automata.characterizingSet(mealy, mealy.getInputAlphabet());
				for (String input : mealy.getInputAlphabet()) {
					Word word  = Word.epsilon().append(input);
					if(!listOfSuff.contains(word)) {
						listOfSuff.add(word); 
					}
				}
				
				int idx = 0;
				for (Word<String> word : listOfSuff) {
					idx++;
					bw_ot.append(String.join(",", word));
					if(idx<listOfSuff.size()) {
						bw_ot.append(";");
					}
				}
				
				bw_ot.close();
			}			

		} catch (Exception e) {
			e.printStackTrace();
			formatter.printHelp("BuildOT", options);
		}
	}

	private static void dfs(CompactMealy<String, Word<String>> mealy, Integer si,
			Map<Integer, Word<String>> accessString) {
		List<String> alphabet = new ArrayList<>(mealy.getInputAlphabet());
		if(shuffle_abc) Collections.shuffle(alphabet);
		for (String in : alphabet) {
			CompactMealyTransition<Word<String>> tr = mealy.getTransition(si, in);
			if(accessString.containsKey(tr.getSuccId())) continue;
			accessString.put(tr.getSuccId(),accessString.get(si).append(in));
			dfs(mealy, tr.getSuccId(), accessString);			
		}		
	}

	private static Options createOptions() {
		/// create the Options
		Options options = new Options();
		options.addOption( SUL,  true, "System Under Learning (SUL)" );
		options.addOption( SUL_AS_DOT,   false, "SUL is formatted as dot file" );
		options.addOption( SUL_AS_KISS,  false, "SUL is formatted as kiss file" );
		options.addOption( HELP, false, "Shows help" );
		options.addOption(MK_OT,   false, "Make an observation table" );
		options.addOption(RANDOMNESS,   false, "Shuffle alphabet to obtain a random set of prefixes" );
		options.addOption(OUT_DIR,   true, "Set output directory to save FSM and OT files" );
		
		return options;
	}

}
