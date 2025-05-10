package de.learnlib.algorithms.kv;

import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealy;
import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealyBuilder;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import java.util.List;

import de.learnlib.algorithms.kv.AdaptiveKearnsVaziraniMealy;

public class AdaptiveKearnsVaziraniMealyBuilder<I, O>{
	
	boolean repeatedCounterexampleEvaluation;

	private net.automatalib.words.Alphabet<I> alphabet;
	private de.learnlib.api.oracle.MembershipOracle<I,net.automatalib.words.Word<O>> oracle;
	private java.util.List<net.automatalib.words.Word<I>> initialPrefixes;
	private java.util.List<net.automatalib.words.Word<I>> initialSuffixes;
	private de.learnlib.acex.AcexAnalyzer counterexampleAnalyzer;
	
	public AdaptiveKearnsVaziraniMealyBuilder() {
		this.initialPrefixes = de.learnlib.algorithms.kv.AdaptiveKearnsVaziraniMealy.BuilderDefaults.initialPrefixes();
		this.initialSuffixes = de.learnlib.algorithms.kv.AdaptiveKearnsVaziraniMealy.BuilderDefaults.initialSuffixes();
		this.repeatedCounterexampleEvaluation = de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealy.BuilderDefaults.repeatedCounterexampleEvaluation();
		this.counterexampleAnalyzer = de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealy.BuilderDefaults.counterexampleAnalyzer();
	}
	

	public AdaptiveKearnsVaziraniMealy<I, O> create() {
		System.out.println("BBBBBBBBBBBBB");
		return new AdaptiveKearnsVaziraniMealy(this.alphabet, this.oracle, 
				this.initialPrefixes, this.initialSuffixes, this.counterexampleAnalyzer);
	}
	public void setAlphabet(net.automatalib.words.Alphabet<I> alphabet) {
		this.alphabet = alphabet;
	}
	public void setOracle(de.learnlib.api.oracle.MembershipOracle<I,net.automatalib.words.Word<O>> oracle) {
		this.oracle = oracle;
	}
	public void setInitialPrefixes(java.util.List<net.automatalib.words.Word<I>> initialPrefixes) {
		this.initialPrefixes = initialPrefixes;
	}
	public void setInitialSuffixes(java.util.List<net.automatalib.words.Word<I>> initialSuffixes) {
		this.initialSuffixes = initialSuffixes;
	}
	public void setCexHandler(de.learnlib.acex.AcexAnalyzer cexHandler) {
		this.counterexampleAnalyzer = cexHandler;
	}
}

