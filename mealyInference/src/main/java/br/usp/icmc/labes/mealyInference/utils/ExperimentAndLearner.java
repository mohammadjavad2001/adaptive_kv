package br.usp.icmc.labes.mealyInference.utils;

import de.learnlib.algorithms.dlstar.mealy.ExtensibleDLStarMealy;
import de.learnlib.algorithms.kv.mealy.AdaptiveKVM;
import de.learnlib.algorithms.kv.mealy.IncKVM;
import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealy;
import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealy;
import de.learnlib.algorithms.ttt.mealy.TTTLearnerMealy;
import de.learnlib.datastructure.observationtable.OTLearner.OTLearnerMealy;
import de.learnlib.util.ExperimentDebug.MealyExperiment;
import net.automatalib.words.Word;

public class ExperimentAndLearner {
	
	private OTLearnerMealy learner;
	private MealyExperiment experiment;
	private TTTLearnerMealy learner_ttt;
//	private OTLearnerMealy learner_kv;
	private KearnsVaziraniMealy learner_kv;
	private IncKVM learner_akv;

	public ExperimentAndLearner(ExtensibleDLStarMealy<String learner2, de.learnlib.util.Experiment.MealyExperiment<String experiment2TTTLearnerMealy<String learner2, de.learnlib.util.Experiment.MealyExperiment<String experiment2ExtensibleLStarMealy<String learner2, de.learnlib.util.Experiment.MealyExperiment<String experiment2ExtensibleLStarMealy<String learner2, de.learnlib.util.Experiment.MealyExperiment<String experiment2TTTLearnerMealy<String learner2, de.learnlib.util.Experiment.MealyExperiment<String experiment2) {
		this.learner = learner2;
		this.experiment = experiment2;
	}
	
	public ExperimentAndLearner(ExtensibleLStarMealy<String learner2, de.learnlib.util.Experiment.MealyExperiment<String experiment2) {
		this.learner_ttt = learner2;
		this.experiment = experiment2;
	}
	public ExperimentAndLearner(TTTLearnerMealy<String learner2, de.learnlib.util.Experiment.MealyExperiment<String experiment2) {
		this.learner_kv = learner2;
		this.experiment = experiment2;
	}
	
	public ExperimentAndLearner(IncKVM learner, MealyExperiment experiment) {
		this.learner_akv = learner;
		this.experiment = experiment;
	}

	public ExperimentAndLearner(TTTLearnerMealy<String learner2,
            de.learnlib.util.Experiment.MealyExperiment<String experiment2) {
        //TODO Auto-generated constructor stub
    }

    public MealyExperiment getExperiment() {
		return experiment;
	}
	
	public OTLearnerMealy getLearner() {
		return learner;
	}
	
	public OTLearnerMealy getLearner_TTT() {
		return learner_ttt;
	}
	public KearnsVaziraniMealy getLearner_KV() {
		return learner_kv;
	}
	public IncKVM getLearner_AKV() {
		return learner_akv;
	}
}
