import os, glob, sys

REGEX_date      = "(\d+)-(\d+)-(\d+)\s+(\d+):(\d+):(\d+)\s+INFO\s+"
REGEX_sul       = REGEX_date+"(Infer_LearnLib)\s+\|(SUL)\s+name:\s+(.+)"
REGEX_seed      = REGEX_date+"(Infer_LearnLib)\s+\|(Seed):\s+(.+)"
REGEX_cache     = REGEX_date+"(Infer_LearnLib)\s+\|(Cache):\s+(.+)"

REGEX_otclosing = REGEX_date+"(Infer_LearnLib)\s+\|(ClosingStrategy):\s+(.+)"
REGEX_cehandler = REGEX_date+"(Infer_LearnLib)\s+\|(ObservationTableCEXHandler):\s+(.+)"
REGEX_eqoracle  = REGEX_date+"(Infer_LearnLib)\s+\|(EquivalenceOracle):\s+(.+)"

REGEX_method    = REGEX_date+"(Infer_LearnLib)\s+\|(Method):\s+(.+)"
REGEX_reused    = REGEX_date+"(Infer_LearnLib)\s+\|(Reading\s+OT):\s+(.+)"

REGEX_round     = REGEX_date+"(Experiment(Debug)?)\s+\|(Starting\s+round)\s+(.+)"
REGEX_iterinfo  = REGEX_date+"WpMethodHypEQOracle\s+\|EquivalenceOracle:\s+[^:]+:\s+{(HypothesisSize)=(.+);SULSize=(.+);}"

REGEX_stats_rounds = REGEX_date+"(Infer_LearnLib)\s+\|(Rounds):\s+(.+)"
REGEX_stats_mqr    = REGEX_date+"(Infer_LearnLib)\s+\|(MQ\s+\[Resets\]):\s+(.+)"
REGEX_stats_eqr    = REGEX_date+"(Infer_LearnLib)\s+\|(EQ\s+\[Resets\]):\s+(.+)"
REGEX_stats_mqs    = REGEX_date+"(Infer_LearnLib)\s+\|(MQ\s+\[Symbols\]):\s+(.+)"
REGEX_stats_eqs    = REGEX_date+"(Infer_LearnLib)\s+\|(EQ\s+\[Symbols\]):\s+(.+)"

REGEX_stats_lrn = REGEX_date+"(SimpleProfiler)\s+\|(Learning\s+\[ms\]):\s+(.+)"
REGEX_stats_sce = REGEX_date+"(SimpleProfiler)\s+\|(Searching\s+for\s+counterexample\s+\[ms\]):\s+(.+)"
REGEX_stats_qsz = REGEX_date+"(Infer_LearnLib)\s+\|(Qsize):\s+(.+)"
REGEX_stats_isz = REGEX_date+"(Infer_LearnLib)\s+\|(Isize):\s+(.+)"
REGEX_stats_eqv = REGEX_date+"(Infer_LearnLib)\s+\|(Equivalent):\s+(.+)"
REGEX_info      = REGEX_date+"(Infer_LearnLib)\s+\|(Info):\s+(.+)"

class LogData:
    def __init__(self):
        self.SUL   = None
        self.Seed  = None
        self.Cache = None
        self.ClosingStrategy            = None
        self.ObservationTableCEXHandler = None
        self.EquivalenceOracle          = None
        self.Method   = None
        self.ReusedOT = None
        self.Rounds   = None
        self.MQ_Resets        = None
        self.MQ_Symbols       = None
        self.EQ_Resets        = None
        self.EQ_Symbols       = None
        self.Learning_ms      = None
        self.Searching_for_CE = None
        self.Qsize      = None
        self.Isize      = None
        self.Equivalent = None
        self.Info       = None
        self.Iterations = {
            "Method": [],
            "SUL": [],
            "ReusedOT": [],
            "Seed": [],
            "Round": [],
            "HypothesisSize": [],
            "SULSize": [],
        }

def load_log(logpath):
    import re

    logdata = LogData()

    with open(logpath, 'r') as reader:
        curr_round = None
        for line in reader:
            # print(line)
            match_date = re.search(REGEX_date, line)
            if not match_date: continue
            match_sul = re.search(REGEX_sul, line)
            match_seed = re.search(REGEX_seed, line)
            match_cache = re.search(REGEX_cache, line)
            match_otclosing = re.search(REGEX_otclosing, line)
            match_cehandler = re.search(REGEX_cehandler, line)
            match_eqoracle = re.search(REGEX_eqoracle, line)
            match_method = re.search(REGEX_method, line)
            match_reused = re.search(REGEX_reused, line)
            match_round = re.search(REGEX_round, line)
            match_iterinfo= re.search(REGEX_iterinfo, line)
            match_stats_rounds = re.search(REGEX_stats_rounds, line)
            match_stats_mqr = re.search(REGEX_stats_mqr, line)
            match_stats_eqr = re.search(REGEX_stats_eqr, line)
            match_stats_mqs = re.search(REGEX_stats_mqs, line)
            match_stats_eqs = re.search(REGEX_stats_eqs, line)
            match_stats_lrn = re.search(REGEX_stats_lrn, line)
            match_stats_sce = re.search(REGEX_stats_sce, line)
            match_stats_qsz = re.search(REGEX_stats_qsz, line)
            match_stats_isz = re.search(REGEX_stats_isz, line)
            match_stats_eqv = re.search(REGEX_stats_eqv, line)
            match_info = re.search(REGEX_info, line)

            if (match_sul):
                logdata.SUL = match_sul.group(match_sul.lastindex)
            if (match_seed):
                logdata.Seed = match_seed.group(match_seed.lastindex)
            if (match_cache):
                logdata.Cache = match_cache.group(match_cache.lastindex)
            if (match_otclosing):
                logdata.ClosingStrategy = match_otclosing.group(match_otclosing.lastindex)
            if (match_cehandler):
                logdata.ObservationTableCEXHandler = match_cehandler.group(match_cehandler.lastindex)
            if (match_eqoracle):
                logdata.EquivalenceOracle = match_eqoracle.group(match_eqoracle.lastindex)
            if (match_method):
                logdata.Method = match_method.group(match_method.lastindex)
            if (match_reused):
                logdata.ReusedOT = match_reused.group(match_reused.lastindex)
            if (match_round):
                curr_round = match_round.group(match_round.lastindex)
            if (match_iterinfo):
                logdata.Iterations["Round"].append(curr_round)
                logdata.Iterations["HypothesisSize"].append(match_iterinfo.group(match_iterinfo.lastindex-1))
                logdata.Iterations["SULSize"].append(match_iterinfo.group(match_iterinfo.lastindex))
            if (match_stats_rounds):
                logdata.Rounds = int(match_stats_rounds.group(match_stats_rounds.lastindex))
            if (match_stats_mqr):
                logdata.MQ_Resets = int(match_stats_mqr.group(match_stats_mqr.lastindex))
            if (match_stats_eqr):
                logdata.EQ_Resets = int(match_stats_eqr.group(match_stats_eqr.lastindex))
            if (match_stats_mqs):
                logdata.MQ_Symbols = int(match_stats_mqs.group(match_stats_mqs.lastindex))
            if (match_stats_eqs):
                logdata.EQ_Symbols = int(match_stats_eqs.group(match_stats_eqs.lastindex))
            if (match_stats_lrn):
                logdata.Learning_ms = match_stats_lrn.group(match_stats_lrn.lastindex)
            if (match_stats_sce):
                logdata.Searching_for_CE = match_stats_sce.group(match_stats_sce.lastindex)
            if (match_stats_qsz):
                logdata.Qsize = match_stats_qsz.group(match_stats_qsz.lastindex)
            if (match_stats_isz):
                logdata.Isize = match_stats_isz.group(match_stats_isz.lastindex)
            if (match_stats_eqv):
                logdata.Equivalent = match_stats_eqv.group(match_stats_eqv.lastindex)
            if (match_info):
                logdata.Info = match_info.group(match_info.lastindex)
        logdata.Iterations["SUL"]      = [logdata.SUL] * logdata.Rounds
        logdata.Iterations["Seed"]     = [logdata.Seed] * logdata.Rounds
        logdata.Iterations["Method"]   = [logdata.Method] * logdata.Rounds
        logdata.Iterations["ReusedOT"] = [logdata.ReusedOT] * logdata.Rounds
        return logdata



def splitall(path):
    allparts = []
    while 1:
        parts = os.path.split(path)
        if parts[0] == path:  # sentinel for absolute paths
            allparts.insert(0, parts[0])
            break
        elif parts[1] == path: # sentinel for relative paths
            allparts.insert(0, parts[1])
            break
        else:
            path = parts[0]
            allparts.insert(0, parts[1])
    return allparts
