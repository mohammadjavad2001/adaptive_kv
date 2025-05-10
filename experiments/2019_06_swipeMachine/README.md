# README

This README file describes a basic scenario for running the Dynamic L*M algorithm using the *Windscreen Wiper System FSM* example.

## Introduction

The Dynamic L*M algorithm is implemented as a command line interface (CLI) program.
A binary version of the algorithm is available in this folder and named as mylearn.jar.
To run the algorithm, you need a JRE v

## What parameters does it support?

To find the parameters supported by the binary version of the Dynamic L*M algorithm, you can run the myjearn.jar with the ```--help``` parametr as follows:

```bash
    java -jar ./mylearn.jar --help # prints the help menu
```
As output, you should read:

```bash
    usage: Infer_LearnLib
    -cache          Use caching.
    -cexh <arg>     Set counter example (CE) processing method.
                    Options: {ClassicLStar, MalerPnueli, RivestSchapire,
                    RivestSchapireAllSuffixes, Shahbaz, Suffix1by1}
    -clos <arg>     Set closing strategy.
                    Options: {CloseFirst, CloseShortest}
    -config <arg>   Configuration file
    -eq <arg>       Set equivalence query generator.
                    Options: {rndWalk, rndWords, wp, wphyp, w, whyp, wrnd, wrndhyp}
    -help           Shows help
    -info <arg>     Add extra information as string
    -learn <arg>    Model learning algorithm.
                    Options: {lstar, l1, adaptive, dlstar_v2, dlstar_v1, dlstar_v0, ttt}
    -ot <arg>       Load observation table (OT)
    -out <arg>      Set output directory
    -seed <arg>     Seed used by the random generator
    -sot            Save observation table (OT)
    -sul <arg>      System Under Learning (SUL)
```

## How to execute the Windscreen Wiper running example?

As you read above, the ```mylearn.jar```` has several parameters.
To execute the Windscreen Wiper FSM example with default parameters, run the command below:

```bash
    java -jar ./mylearn.jar  -sul ./wipeSystem_basic.txt
```
The ```-sul``` parameter receives an FSM model (in KISS format).

By default, the tool uses the following parameters:


```bash
    Cache: N
    ClosingStrategy: CloseFirst
    ObservationTableCEXHandler: RIVEST_SCHAPIRE
    EquivalenceOracle: WpMethodEQOracle(2)
    Method: L*M # LearnLib version of the Angluin's L*M algorithm
```

## How to run the DL*M algorithm using the Windscreen Wiper running example?

To run the DL*M algorithm parameters, you need two input files:

1. The input file with the SUL model in KISS format
2. The input file with the reused OT.

Below you find an example for running the D*LM algorithm with the Windscreen Wiper example.


```bash
    java -jar mylearn.jar -learn dlstar_v2 -sul ./wipeSystem_perm.txt -ot ./reused.ot
    # Running a learning experiment with the Partial Dynamic L*M algorithm (aka dlstar_v2)
    # on the Windscreen wiper with permanent movement FSM (aka wipeSystem_perm.txt)
    # by reusing the observation table described in the input file reused.ot
```

### Details about the command above

#### 1) The ```-sul``` parameter

The ```-sul``` parameter receives the path to an input file describing the FSM model in KISS format. 
In this file format, each row describes a transition as ```s_from -- label_in / laber_out -> s_to```, such that 
the origin state is indicated by ```s_from```,
the destination state is indicated by ```s_to```,
and the input output symbols are depicted by ```label_in``` and ```label_out```, respectively.

#### 2) The ```-learn``` parameter

The ```-learn``` parameter defines the learning algorithm to be used. 
The algorithms currently supported are shown in the table below.

| Algorithm                  | Reuses prefix? | Reuses suffix? | Restore properties? | Parameter      | Reference                                                                                |
|----------------------------|----------------|----------------|---------------------|----------------|------------------------------------------------------------------------------------------|
| Angluin's L*M              | -              | -              | -                   | ```lstar```    | [Angluin (1987)](https://www.sciencedirect.com/science/article/pii/0890540187900526)     |
| Huistra et al.'s Adaptive  | No             | Complete       | No                  | ```adaptive``` | [Huistra et al. (2018)](https://link.springer.com/chapter/10.1007/978-3-030-00244-2_11)  |
| Chaki et al.'s DL*M        | x              | x              | No                  | ```dlstar_v0```| [Chaki et al. (2008)](https://link.springer.com/article/10.1007/s10703-008-0053-x)       |
| DL*M+                      | Indiscriminate | Indiscriminate | Yes                 | ```dlstar_v1```| [Damasceno et al. (2019)](http://doi.org/10.1007/978-3-030-34968-4_8)                    |
| Partial DL*M               | On-the-fly     | On-the-fly     | No                  | ```dlstar_v2```| [Damasceno et al. (2019)](http://doi.org/10.1007/978-3-030-34968-4_8)                    |

#### 3) The ```-ot``` parameter

The ```-ot``` patameter defines the path to the input file describing the reused observation table.
This is a text file where the initial sets of prefixes and suffixes are indicated in the first and second lines, respectively.

- The empty sequence is explicitly represented by the first semi-colon. 
- Each input sequences is separated by a semi-colon.
- Each input symbol is separated by a colon.
