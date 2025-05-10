list.of.packages <- c(
  "xml2","devtools","plyr",
  "ggrepel","ggplot2",
  "gtools","stringr","scales","effsize",
  "SortableHTMLTables","RColorBrewer","nortest")

new.packages <- list.of.packages[!(list.of.packages %in% installed.packages(lib.loc="/home/damasceno/Rpackages/")[,"Package"])]
if(length(new.packages)) install.packages(new.packages,lib="/home/damasceno/Rpackages/")
lapply(list.of.packages,require,character.only=TRUE, lib.loc="/home/damasceno/Rpackages/")

# new.packages <- list.of.packages[!(list.of.packages %in% installed.packages()[,"Package"])]
# if(length(new.packages)) install.packages(new.packages, dependencies = TRUE)
# lapply(list.of.packages,require,character.only=TRUE)

devtools::install_github("wilkelab/cowplot")
devtools::install_github("kassambara/ggpubr")
devtools::install_github("hadley/reshape")
rm(new.packages,list.of.packages)


## Gives count, mean, standard deviation, standard error of the mean, and confidence interval (default 95%).
##   data: a data frame.
##   measurevar: the name of a column that contains the variable to be summariezed
##   groupvars: a vector containing names of columns that contain grouping variables
##   na.rm: a boolean that indicates whether to ignore NA's
##   conf.interval: the percent range of the confidence interval (default is 95%)
summarySE <- function(data=NULL, measurevar, groupvars=NULL, na.rm=FALSE,
                      conf.interval=.95, .drop=TRUE) {
  library(plyr)
  
  # New version of length which can handle NA's: if na.rm==T, don't count them
  length2 <- function (x, na.rm=FALSE) {
    if (na.rm) sum(!is.na(x))
    else       length(x)
  }
  
  # This does the summary. For each group's data frame, return a vector with
  # N, mean, and sd
  datac <- ddply(data, groupvars, .drop=.drop,
                 .fun = function(xx, col) {
                   c(N    = length2(xx[[col]], na.rm=na.rm),
                     mean = mean   (xx[[col]], na.rm=na.rm),
                     sd   = sd     (xx[[col]], na.rm=na.rm)
                   )
                 },
                 measurevar
  )
  
  # Rename the "mean" column    
  datac <- rename(datac, c("mean" = measurevar))
  
  datac$se <- datac$sd / sqrt(datac$N)  # Calculate standard error of the mean
  
  # Confidence interval multiplier for standard error
  # Calculate t-statistic for confidence interval: 
  # e.g., if conf.interval is .95, use .975 (above/below), and use df=N-1
  ciMult <- qt(conf.interval/2 + .5, datac$N-1)
  datac$ci <- datac$se * ciMult
  
  return(datac)
}

prepareTab<-function(logdir,logfname,tab){
  tab_filename<-paste(logdir,logfname,".tab",sep="")
  tab <- read.table(tab_filename, sep="|", header=TRUE)
  
  names(tab) <- c("SUL","Seed","Cache","CloS","CExH","EqO","Method","Reuse","Reuse_Resets","Reuse_Symbols","Rounds","MQ_Resets","MQ_Symbols","EQ_Resets","EQ_Symbols","L_ms","SCEx","Q_Size","I_Size","Correct","Info","ReusedPref","ReusedSuf" )
  tab$Info<-gsub("^N/A$","null",tab$Info)
  
  tab$L_ms    <- as.numeric(tab$L_ms)
  tab$Rounds  <- as.numeric(tab$Rounds)
  tab$SCEx    <- as.numeric(tab$SCEx)
  tab$MQ_Resets  <- as.numeric(tab$MQ_Resets)
  tab$MQ_Symbols <- as.numeric(tab$MQ_Symbols)
  tab$EQ_Resets  <- as.numeric(tab$EQ_Resets)
  tab$EQ_Symbols <- as.numeric(tab$EQ_Symbols)
  tab$Correct    <- as.character(tab$Correct)
  tab$Seed  <- as.character(tab$Seed)
  
  tab$TQ_Resets  <- tab$MQ_Resets+tab$EQ_Resets
  tab$TQ_Symbols  <- tab$MQ_Symbols+tab$EQ_Symbols
  
  
  tab$SUL <- gsub('\\.[a-z]+$', '', gsub('\\.[a-z]+$', '', gsub('\\.[a-z]+$', '', tab$SUL)))
  tab$Reuse <- gsub('\\.[a-z]+$', '', gsub('\\.[a-z]+$', '', gsub('\\.[a-z]+$', '', tab$Reuse)))
  tab$EqO <- gsub('\\([^\\(]+\\)$', '', tab$EqO)
  
  tab$Method<-gsub("^Adaptive","Adp",tab$Method)
  tab$Method<-factor(tab$Method,levels = c("DL*M_v2", "DL*M_v1", "DL*M_v0", "Adp", "L*M", "L1","TTT"))
  tab$Correct<-factor(tab$Correct,levels = c("OK","NOK"))
  
  tab$SUL  <-gsub("^SUL_","",gsub('\\.[a-z]+$',"",gsub("^client_","cli_",gsub('^server_',"srv_",tab$SUL))))
  tab$Reuse<-gsub("^SUL_","",gsub('\\.[a-z]+$',"",gsub("^client_","cli_",gsub('^server_',"srv_",tab$Reuse))))
  
  return(tab)
}

mk_sul_ot_srv<-function(){
  sul_ot <- data.frame(matrix(ncol = 2, nrow = 0))
  sul_ot<-rbind(sul_ot,data.frame(SUL="srv_097e"  	, Reuse="srv_097"  	))
  sul_ot<-rbind(sul_ot,data.frame(SUL="srv_097e"  	, Reuse="srv_097c"  	))
  sul_ot<-rbind(sul_ot,data.frame(SUL="srv_098l"  	, Reuse="srv_097"  	))
  sul_ot<-rbind(sul_ot,data.frame(SUL="srv_098l"  	, Reuse="srv_097e"  	))
  sul_ot<-rbind(sul_ot,data.frame(SUL="srv_098m"  	, Reuse="srv_097"  	))
  sul_ot<-rbind(sul_ot,data.frame(SUL="srv_098m"  	, Reuse="srv_098l"  	))
  sul_ot<-rbind(sul_ot,data.frame(SUL="srv_100"  		, Reuse="srv_097"  	))
  sul_ot<-rbind(sul_ot,data.frame(SUL="srv_100"  		, Reuse="srv_098m"  	))
  sul_ot<-rbind(sul_ot,data.frame(SUL="srv_098s"  	, Reuse="srv_097"  	))
  sul_ot<-rbind(sul_ot,data.frame(SUL="srv_098s"  	, Reuse="srv_100"  	))
  sul_ot<-rbind(sul_ot,data.frame(SUL="srv_098u"  	, Reuse="srv_097"  	))
  sul_ot<-rbind(sul_ot,data.frame(SUL="srv_098u"  	, Reuse="srv_098s"  	))
  sul_ot<-rbind(sul_ot,data.frame(SUL="srv_101"  		, Reuse="srv_097"  	))
  sul_ot<-rbind(sul_ot,data.frame(SUL="srv_101"  		, Reuse="srv_098u"  	))
  sul_ot<-rbind(sul_ot,data.frame(SUL="srv_098za"  	, Reuse="srv_097"  	))
  sul_ot<-rbind(sul_ot,data.frame(SUL="srv_098za"  	, Reuse="srv_101"  	))
  sul_ot<-rbind(sul_ot,data.frame(SUL="srv_101k"  	, Reuse="srv_097"  	))
  sul_ot<-rbind(sul_ot,data.frame(SUL="srv_101k"  	, Reuse="srv_098za"  	))
  sul_ot<-rbind(sul_ot,data.frame(SUL="srv_102"  		, Reuse="srv_097"  	))
  sul_ot<-rbind(sul_ot,data.frame(SUL="srv_102"  		, Reuse="srv_101k"  	))
  sul_ot<-rbind(sul_ot,data.frame(SUL="srv_110pre1" 	, Reuse="srv_097"  	))
  sul_ot<-rbind(sul_ot,data.frame(SUL="srv_110pre1" 	, Reuse="srv_102"  	))
  return(sul_ot)
}