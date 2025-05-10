package br.usp.icmc.labes.mealyInference.utils;

import java.io.File;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.words.Word;

public class Dot2Kiss {

		private static final String SUL = "sul";
		private static final String HELP = "h";
		private static final String OUT_DIR = "out";

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
				
				CompactMealy<String, Word<String>> mealy = Utils.getInstance().loadMealyMachineFromDot(f);
				
				
				File out_dir = f.getParentFile();
				if(line.hasOption(OUT_DIR)) {
					out_dir = new File(line.getOptionValue(OUT_DIR));
					out_dir.mkdirs();
				}
				
				Utils.getInstance().saveMealyMachineAsKiss(mealy, new File(out_dir,f.getName().replaceAll("(\\.[(dot)(txt)])+$", "")+".kiss"));
				
			} catch (Exception e) {
				e.printStackTrace();
				formatter.printHelp("BuildOT", options);
			}
		}

		private static Options createOptions() {
			/// create the Options
			Options options = new Options();
			options.addOption( SUL,  true, "System Under Learning (SUL)" );
			options.addOption( HELP, false, "Shows help" );
			options.addOption(OUT_DIR,   true, "Set output directory to save FSM and OT files" );
			
			return options;
		}

	}
