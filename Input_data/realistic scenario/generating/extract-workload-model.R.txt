#################################################################################################
#
# Tools for the workload model extraction process proposed in the paper:
#
# - Marcus Carvalho, Francisco Brasileiro. 
#   Modelagem da Carga de Trabalho de Grades Computacionais Baseada no Comportamento dos Usuários. 
#   Submitted to: Simpósio Brasileiro de Redes de Computadores e Sistemas Distribuídos (SBRC), 2012.
#
# Last update: 05-Dec-2011
#
#################################################################################################

##
# List of traces that will be used in extraction of workload models. The traces must contain
# the Bag-of-Task (BoT) ID in the field JobStructureParams. One can generate this information
# by running the function "GenerateBoTInfoFromGWA(...)" before starting the extraction process.
# Each trace is represented by the tuple: c(Trace ID, GWA-format trace file).
##
kTraces <- list(c("gwa-t1", "/local/marcus/workloads/gwa-t1/anon_jobs.gwf_jid.txt"),
                c("gwa-t2", "/local/marcus/workloads/gwa-t2/grid5000_clean_trace.log_jid.txt"),
                c("gwa-t3", "/local/marcus/workloads/gwa-t3/anon_jobs.gwf_jid.txt"),
                c("gwa-t4", "/local/marcus/workloads/gwa-t4/anon_jobs.gwf_jid.txt"),
                c("gwa-t10", "/local/marcus/workloads/gwa-t10/anon_jobs.gwf_jid.txt"),
                c("gwa-t11", "/local/marcus/workloads/gwa-t11/anon_jobs.gwf_jid.txt"))


##
# Main function to apply the whole extraction process. First, the models for each attribute
# and trace are extracted. Then, the best distributions according to the goodness-of-fit
# results are extracted. The whole execution may take several hours.
#
# Input:
# - k.values: range of the amount of clusters to be used in the models
# - traces.list: list of traces to be used in the extraction
# - wl.attributes: list of attributes to model
# Output:
# - Several files describing the models' distributions are generated during the process.
##
ApplyExtractionProccess <- function(k.values=1:20, traces.list=kTraces, 
                                    wl.attributes=c("bot_user_iat", "bot_runtime_sum", 
                                                    "task_runtime"), do.log=TRUE) {
  GenerateWorkloadModels(k.values, wl.attributes=wl.attributes, traces.list, do.log=do.log)
  ExtractBestDistributions(in.file.prefix="./clustgof", wl.attributes=wl.attributes, 
                           traces.ids=sapply(traces.list, function(x) x[1]))
} 

##
# Generate workload models by applying clustering and fitting distributions for each
# trace and attribute.
#
# Input:
# - k.values: range of the amount of clusters to be used in the models
# - traces.list: list of traces to be used in the extraction
# - wl.attributes: list of attributes to model
# - cldist.file: file containing the distance matrix for the clustering. If the empty string "" is
#   passed, the distance matrix file will be generated.
# - do.log: if TRUE, a log2 transformation will be applied on the data for clustering and fitting.
# Output:
# - Files with prefix "clustdist_" containing the distance matrix for the clustering.
# - Files with prefix "clustgof_" containing the distribution fitting results for each cluster.
##
GenerateWorkloadModels <- function(k.values=1:20, 
                                   wl.attributes=c("bot_user_iat", "bot_runtime_sum", 
                                                   "task_runtime"), 
                                   traces.list=kTraces, cldist.file="", do.log=FALSE) {
  require(foreach)
  require(doMC)
  registerDoMC()
  
  foreach(attribute=wl.attributes, .combine=rbind) %do% {
    foreach(trace=traces.list, .combine=rbind) %do% {
      print(paste(trace[1], attribute))
      df <- ReadGWATrace(trace[2])
      if (attribute == "task_runtime") {
        if (do.log) {
          df$RunTime <- with(df, ifelse(RunTime <= 2, 2, RunTime))
          df$RunTime <- log2(df$RunTime)
        } else {
          df$RunTime <- df$RunTime/3600
        }
        data <- data.frame(UserID=df$UserID, Trace=trace[1], task_runtime=df$RunTime)
      } else if (attribute == "bot_user_iat") {
        df <- GroupByUsersStats(GroupByBoTs(df))
        df$IAT <- with(df, ifelse(IAT <= 2, 2, IAT))
        if (do.log) {
          df$IAT <- log2(df$IAT)
        } else {
          df$IAT <- df$IAT/3600
        }
        data <- data.frame(UserID=df$UserID, Trace=trace[1], bot_user_iat=df$IAT)
      } else if (attribute == "bot_user_iat_ttn") {
        df <- GroupByUsersStats(GroupByBoTs(df))
        df <- subset(df, ThinkTime <= 0)
        df$IAT <- with(df, ifelse(IAT <= 2, 2, IAT))
        if (do.log) {
          df$IAT <- log2(df$IAT)
        } else {
          df$IAT <- df$IAT/3600
        }
        data <- data.frame(UserID=df$UserID, Trace=trace[1], bot_user_iat_ttn=df$IAT)
      } else if (attribute == "bot_size") {
        df <- GroupByBoTs(df)
        if (do.log) {
          df$BoTSize <- with(df, ifelse(BoTSize <= 2, 2, BoTSize))
          df$BoTSize <- log2(df$BoTSize)
        }
        data <- data.frame(UserID=df$UserID, Trace=trace[1], bot_size=df$BoTSize)
      } else if (attribute == "batch_size") {
        df <- GroupByBoTs(df)
        df <- df[order(df$UserID, df$SubmitTime),]
        df <- GroupByUsersStats(df)
        df$BatchID <- group.batch.thinktime(df)
        df <- with(df, aggregate(BatchID, list(BatchID, UserID), length))
        colnames(df) <- c("BatchID", "UserID", "BatchSize")
        if (do.log) {
          df$BatchSize <- log2(df$BatchSize)
        }
        data <- data.frame(UserID=df$UserID, Trace=trace[1], batch_size=df$BatchSize)
      } else if (attribute == "bot_user_thinktime") {
        df <- GroupByBoTs(df)
        df <- GroupByUsersStats(df)
        df <- subset(df, ThinkTime > 0)
        if (do.log) {
          df$ThinkTime <- with(df, ifelse(ThinkTime <= 2, 2, ThinkTime))
          df$ThinkTime <- log2(df$ThinkTime)
        } else {
          df$ThinkTime <- df$ThinkTime/3600
        }
        data <- data.frame(UserID=df$UserID, Trace=trace[1], bot_user_thinktime=df$ThinkTime)
      } else if (attribute == "bot_user_thinktime_0_20") {
        df <- GroupByBoTs(df)
        df <- GroupByUsersStats(df)
        df <- subset(df, ThinkTime > 0 & ThinkTime <= 20*60)
        if (do.log) {
          df$ThinkTime <- with(df, ifelse(ThinkTime <= 2, 2, ThinkTime))
          df$ThinkTime <- log2(df$ThinkTime)
        } else {
          df$ThinkTime <- df$ThinkTime/3600
        }
        data <- data.frame(UserID=df$UserID, Trace=trace[1], bot_user_thinktime_0_20=df$ThinkTime)
      } else if (attribute == "bot_user_thinktime_20_") {
        df <- GroupByBoTs(df)
        df <- GroupByUsersStats(df)
        df <- subset(df, ThinkTime > 20*60)
        if (do.log) {
          df$ThinkTime <- with(df, ifelse(ThinkTime <= 2, 2, ThinkTime))
          df$ThinkTime <- log2(df$ThinkTime)
        } else {
          df$ThinkTime <- df$ThinkTime/3600
        }
        data <- data.frame(UserID=df$UserID, Trace=trace[1], bot_user_thinktime_20_=df$ThinkTime)
      } else if (attribute == "bot_runtime") {
        df <- GroupByBoTs(df)
        df$RunTime <- with(df, ifelse(RunTime <= 2, 2, RunTime))
        if (do.log) {
          df$RunTime <- log2(df$RunTime)
        } else {
          df$RunTime <- df$RunTime/3600
        }
        data <- data.frame(UserID=df$UserID, Trace=trace[1], bot_runtime=df$RunTime)
      } else if (attribute == "bot_runtime_sd") {
        df <- GroupByBoTs(df)
        df$RunTime.SD <- with(df, ifelse(RunTime.SD <= 2, 2, RunTime.SD))
        if (do.log) {
          df$RunTime.SD <- log2(df$RunTime.SD)
        } else {
          df$RunTime.SD <- df$RunTime.SD/3600
        }
        data <- data.frame(UserID=df$UserID, Trace=trace[1], bot_runtime_sd=df$RunTime.SD)
      } else if (attribute == "bot_runtime_sum") {
        df <- GroupByBoTs(df)
        df$RunTime.sum <- with(df, ifelse(RunTime*BoTSize <= 2, 2, RunTime*BoTSize))
        if (do.log) {
          df$RunTime.sum <- log2(df$RunTime.sum)
        } else {
          df$RunTime.sum <- df$RunTime.sum/3600
        }
        data <- data.frame(UserID=df$UserID, Trace=trace[1], bot_runtime_sum=df$RunTime.sum)
      }
      
      print(summary(data))
      if (cldist.file == "") {
        print("Calculating distance matrix...")
        users.freq <- FilterByFrequency(data, "UserID", 30)
        data <- subset(data, is.element(UserID, users.freq))
        data$UserID <- factor(data$UserID)
        cldist <- CalculateDistanceMatrixWithKSTest(data, id.var=c("UserID"), val.var=attribute)
        filename <- paste("clustdist",trace[1],attribute,".txt",sep="_")
        write.table(as.matrix(cldist), file=filename, quote=FALSE, row.names=TRUE)
      } else {
        print("Reading distance matrix...")
        cldist <- read.table(cldist.file, header=TRUE)
        cldist <- as.dist(cldist, upper=TRUE, diag=TRUE)
      }
      
      clgof <- FitAndTestGOF(data, cldist, k=k.values, value.var=attribute)
      clgof$Trace <- trace[1]
      clgof$stats.attr <- attribute 
      write.table(clgof, file=paste("clustgof",attribute,trace[1],".txt",sep="_"), quote=FALSE, 
                  row.names=FALSE)
      clgof
    }
  }
}

##
# Extract the best distributions generated by the models according to the goodness-of-fit
# metric proposed.
#
# Input:
# - in.file.prefix: prefix of the files generated by the "GenerateWorkloadModels(...)" function.
# - wl.attributes: list of attributes used in the "GenerateWorkloadModels(...)" function.
# - traces.ids: list of traces ids used in the workload models generation.
# Output:
# - file "wl_fitdist_best_peruser.txt" with results for the best distributions for each each user.
# - file "wl_fitdist_best.txt" with results for the best distributions for each cluster.
# - file "wl_fitdist_all.txt" with results for distributions used in the fitting for each cluster.
##
ExtractBestDistributions <- function(in.file.prefix="./clustgof", 
                                     wl.attributes=c("task_runtime", "bot_runtime_sum",
                                                     "bot_user_iat"),
                                     traces.ids=c("gwa-t1", "gwa-t2", "gwa-t3", "gwa-t4", "gwa-t10", 
                                                  "gwa-t11")) {
  require(foreach)
  i <- 0
  foreach(attribute=wl.attributes, .combine=rbind) %do% {
    foreach(trace.id=traces.ids, .combine=rbind) %do% {
      i <- i + 1
      in.file <- paste(in.file.prefix, attribute, trace.id, ".txt", sep="_")
      print(in.file)
      cl.fit <- read.table(in.file, header=T)
      cl.fit.agg <- with(cl.fit, aggregate(avg.pvalue, 
              list(distribution, Cluster, k, Trace, stats.attr), 
              HigherThanValueProportion))
      colnames(cl.fit.agg) <- c("distribution", "Cluster", "k", "Trace", "stats.attr", "ratio")
      cl.best.ind.percluster <- with(cl.fit.agg, aggregate(ratio, 
              list(Cluster, k, Trace, stats.attr), 
              which.max))
      cl.size <- with(cl.fit.agg, aggregate(ratio, list(Cluster, k, Trace, stats.attr), length))
      cl.first.ind <- c(0,cumsum(cl.size$x[1:(nrow(cl.size)-1)])) 
      cl.best.ind <- cl.first.ind + cl.best.ind.percluster$x
      cl.best <- cl.fit.agg[cl.best.ind,]
      cl.best <- merge(cl.fit, cl.best, by=c("Trace", "k", "stats.attr", "Cluster", "distribution"))
      cl.best <- cl.best[,c("avg.pvalue", "UserID.Trace", "Param1", "Param2", "Cluster",
              "distribution","k","Trace","stats.attr")]
      write.table(cl.best, "wl_fitdist_best_peruser.txt", quote=F, row.names=F, col.names=i == 1, 
                  append=i > 1)
      
      cl.best.agg <- with(cl.best, aggregate(UserID.Trace, 
              list(Cluster, distribution, k, Trace, stats.attr), 
              length))
      colnames(cl.best.agg) <- c("Cluster", "distribution",  "k", "Trace", "stats.attr", "Size")
      cl.best.agg$Param1 <- with(cl.best, aggregate(Param1, list(Cluster, distribution, k, Trace, 
                  stats.attr), unique))$x
      cl.best.agg$Param2 <- with(cl.best, aggregate(Param2, list(Cluster, distribution, k, Trace, 
                  stats.attr), unique))$x
      cl.best.agg.sizes <- with(cl.best.agg, aggregate(Size, list(k, Trace, stats.attr), sum))
      colnames(cl.best.agg.sizes) <- c("k", "Trace", "stats.attr", "Sum")
      cl.best.agg.merge <- merge(cl.best.agg, cl.best.agg.sizes, by=c("k", "Trace", "stats.attr"))
      cl.best.agg.merge$Fraction <- cl.best.agg.merge$Size / cl.best.agg.merge$Sum 
      cl.best.agg.merge <- cl.best.agg.merge[,c("Param1","Param2","Cluster", "distribution", "k", 
              "Trace", "stats.attr", "Size", "Fraction")]
      write.table(cl.best.agg.merge, "wl_fitdist_best.txt", quote=F, row.names=F, col.names=i == 1, 
          append=i > 1)
      write.table(subset(cl.best.agg.merge, k == 5), "wl_fitdist_best_k5.txt", quote=F, 
                  row.names=F, col.names=i == 1, append=i > 1)
      cl.best$distribution <- "combined"
      cl.fit.all <- rbind(cl.fit, cl.best)
      cl.fit.all.agg <- with(cl.fit.all, aggregate(avg.pvalue, 
                                                   list(distribution, k, Trace, stats.attr), 
                                                   HigherThanValueProportion))
      colnames(cl.fit.all.agg) <- c("distribution", "k", "Trace", "stats.attr", "ratio")
      write.table(cl.fit.all.agg, "wl_fitdist_all.txt", quote=F, row.names=F, col.names=i == 1, 
                  append=i > 1)
      return(cl.best.agg.merge)
    }
  }                                   
}

##
# Calculate distance matrix between users' attribute values for hierarchical clustering, based on
# the Kolmogorov-Smirnov test for two samples.
#
# Input:
# - data: values for the workload attribute grouped by users
# - id.var: vector of field names used for grouping the data
# - val.var: field name for the data values being modeled
# - freq.min: minimum frequency of data values for each group to filter data.
# Output:
# - files with prefix "clustdist" containing the distance matrix between users for each attribute 
# - returns a matrix of distances between groups as a "dist" object, used by the "hclust" function
##
CalculateDistanceMatrixWithKSTest <- function(data, id.var=c("UserID"), val.var="task_runtime", 
                                              freq.min=100) {
  
  freq <- table(data[[id.var]])
  ids <- names(freq[freq >= freq.min])
  
  n <- length(ids)
  
  dists <- matrix(nrow=n, ncol=n)
  
  for(i in 1:n) {
    
    id1 <- ids[i]
    x1 <- data[data[[id.var]]==id1,][[val.var]]
    
    row.res <- foreach(j=1:n, .combine=c) %dopar% {
      
      if (i == j) {
        return(0)
      } else if (i > j) {
        return(dists[j,i])
      } else {
        id2 <- ids[j]
        x2 <- data[data[[id.var]]==id2,][[val.var]]
        return(KSTestTwoSample(x1,x2))
      }
    }
    dists[i,] <- row.res
  }
  
  colnames(dists) <- ids
  rownames(dists) <- ids
  
  return(as.dist(dists, upper=TRUE, diag=TRUE))
}

##
# Fit distributions for each cluster using the hierarchical clustering approach and apply
# a goodness-of-fit test for the fittings.
#
# Input:
# - data: values for the attribute being modeled.
# - cl.dist: distance matrix returned by the CalculateDistanceMatrixWithKSTest(...) function
# - k.values: range of number of clusters (k) to be used in the clustering analysis
# - id.var: vector of field names used for grouping the data
# - val.var: field name for the data values being modeled
# Output:
# - returns data frame with parameters of fitted distributions and GoF results for each user
##
FitAndTestGOF <- function(data, cl.dist, k.values, id.var=c("UserID"), value.var="IAT") {
  k.values <- k.values[k.values <= nrow(as.matrix(cl.dist))]
  print("Generating hierarquical cluster...")
  cl.hc <- hclust(cl.dist, method="complete")
  foreach(kval=k.values, .combine=rbind, .errorhandling='remove') %dopar% {
    print(paste(">>> k = ", kval))
    clusters <- stack(cutree(cl.hc, k=kval))
    colnames(clusters) <- c("Cluster",id.var)
    cl.gof <- FitAndTestGOFForACluster(data, clusters, id.var=id.var, value.var=value.var)                
    cl.gof$k <- kval
    cl.gof
  }
}

##
# Fit distributions and apply goodness-of-fit tests for the given data and its clusters.
#
# Input:
# - data: values for the attribute being modeled.
# - clusters: id of the cluster of the users for each row of the data.
# - id.var: vector of field names used for grouping the data.
# - val.var: field name for the data values being modeled
# - distributions: list of distributions used on the fitting. The distributions are represented
#   by: c(Distribution ID, Probability Density Function, Random Number Generator Function)
# Output:
# - Returns data frame with parameters of fitted distributions and GoF results for each user
##
FitAndTestGOFForACluster <- function(data, clusters, id.var=c("Trace","UserID"), 
                                     value.var="RunTime", 
                                     distributions = list(c("normal","pnorm","rnorm"), 
                                                          c("lognormal","plnorm","rlnorm"),
                                                          c("weibull","pweibull","rweibull"), 
                                                          c("exponential","pexp","rexp"),
                                                          c("gamma","pgamma","rgamma"))) {
  require(MASS)
  require(Hmisc)
  cl.trace <- merge(data, clusters, by=id.var)
  rm(data, clusters); gc()
  
  cl.trace$UserID <- factor(cl.trace$UserID)
  
  foreach(distribution=distributions, .combine=rbind, .errorhandling="remove") %do% {
    
    dist.id <- distribution[1]
    dist.p <- distribution[2]
    
    fit <- tapply(cl.trace[[value.var]], list(cl.trace$Cluster), 
        function(x) { MASS::fitdistr(x,dist.id) })
    
    print(fit)
    
    g <- cl.trace[id.var]   
    
    sp.cl.trace <- split(cl.trace, g, drop=TRUE)
    gof.ks <- lapply(sp.cl.trace, function(x) {
          gkt <- KSTestOneSample(x[[value.var]], dist.p=dist.p, dist.id=dist.id, 
                             dist.param=fit[[as.character(unique(x$Cluster))]]$estimate)
          return(gkt$avg.pvalue)
        })
    
    fit.param1 <- stack(lapply(sp.cl.trace, function(x) {
                    return(fit[[as.character(unique(x$Cluster))]]$estimate[[1]])
                  }))
    
    if (!is.element(dist.id, c("exponential","geometric","Poisson"))) {
      fit.param2 <- stack(lapply(sp.cl.trace, 
                                 function(x) {
                                   return(fit[[as.character(unique(x$Cluster))]]$estimate[[2]])
                                 }))
    } else {
      fit.param2 <- data.frame(x=rep(NA, nrow(fit.param1)))
    }  
    
    fit.cluster <- stack(lapply(sp.cl.trace, 
                                function(x) {
                                  return(as.character(unique(x$Cluster)))
                                })) 
    st.gof.ks <- stack(gof.ks)
    st.gof.ks <- cbind(st.gof.ks, fit.param1[,1], fit.param2[,1], fit.cluster[,1])
    colnames(st.gof.ks) <- c("avg.pvalue","UserID.Trace", "Param1", "Param2", "Cluster")
    st.gof.ks$distribution <- dist.id
    return(st.gof.ks)
  }
}

##
# Kolmogorov-Smirnov goodness-of-fit (GoF) test for one sample that checks whether several 
# samples from the data comes from the theoretical distribution.
#
# Input:
# - x: fitted data values
# - repetitions: number of repetitions for the GoF test, using different data samples for each test
# - n: data sample size for each GoF test execution
# - dist.p: probability density function for the distribution used in the fitting
# - dist.id: id of the distribution used in the fitting
# - dist.params: vector of fitted parameters' values for the distribution used in the fitting
# Output:
# - data frame with average p-value calculated by the GoF test for the fitted distribution
##
KSTestOneSample <- function(x, repetitions=1000, n=30, dist.p, dist.id, dist.params) {
  d.dist = NULL
  res <- foreach(i=1:repetitions, .combine=rbind) %do% {
    if (length(x) >= n)
      sx <- sample(x,n)          
    else
      sx <- x
    
    if (length(dist.params) == 1)
      ks = ks.test(sx, dist.p, dist.params[1])
    else
      ks = ks.test(sx, dist.p, dist.params[1], dist.params[2])
    
    data.frame(p.dist=ks$p.value, d.dist=ks$statistic)
  }
  return(data.frame(Distribution=dist.id, avg.pvalue=mean(res$p.dist)))
}

##
# Kolmogorov-Smirnov goodness-of-fit (GoF) test for two samples that checks how distant two
# empirical distributions are.
#
# Input:
# - x: first data values to be tested
# - y: second data values to be tested
# Output:
# - returns the result of the KS (statistic "D") test for the two sample data
##
KSTestTwoSample <- function(x, y) {
  ks <- ks.test(x, y)
  return(ks$statistic)
}

##
# Group workload trace by Bag-of-Tasks (BoT) providing BoT data.
#
# Input:
# - trace: data frame of the workload trace in the GWA format
# Output:
# - returns data frame containing BoT data
##
GroupByBoTs <- function(trace) {
  if (max(as.integer(trace$JobStructureParams)) != -1) {
    trace.job <- aggregate(trace$SubmitTime, list(trace$UserID, trace$JobStructureParams), min)
    colnames(trace.job) <- c("UserID","JobStructureParams", "SubmitTime")
    trace.job$FinishTime <- aggregate(trace$FinishTime, 
                                      list(trace$UserID, trace$JobStructureParams), max)$x
    trace.job$RunTime <- aggregate(trace$RunTime, 
                                   list(trace$UserID, trace$JobStructureParams), mean)$x
    trace.job$RunTime.SD <- aggregate(trace$RunTime, 
                                      list(trace$UserID, trace$JobStructureParams), sd)$x
    trace.job$BoTSize <- with(trace, aggregate(JobStructureParams, 
                                               list(UserID, JobStructureParams), length))$x
    trace <- trace.job
  }
  return(trace[order(trace$SubmitTime),]) 
}

##
# Group workload trace by users providing users' data.
#
# Input:
# - trace: data frame of the workload trace in the GWA format
# Output:
# returns data frame containing Users' data
##
GroupByUsersStats <- function(trace) {
  
  trace <- subset(trace, RunTime > 0 & UserID != -1)
  trace <- trace[order(trace$UserID, trace$SubmitTime),]
  
  tt <- data.frame(SubmitTime.last=trace[1:(length(trace$SubmitTime)-1),]$SubmitTime, 
                   SubmitTime.next=trace[2:length(trace$SubmitTime),]$SubmitTime, 
                   UserID.next=trace[2:length(trace$SubmitTime),]$UserID, 
                   FinishTime.last=trace[1:(length(trace$SubmitTime)-1),]$FinishTime, 
                   UserID.last=trace[1:(length(trace$SubmitTime)-1),]$UserID, 
                   RunTime.last=trace[1:(length(trace$RunTime)-1),]$RunTime)
  
  tt$ThinkTime <- tt$SubmitTime.next - tt$FinishTime.last
  tt$IAT <- tt$SubmitTime.next - tt$SubmitTime.last
  tt <- subset(tt, UserID.next == UserID.last) 
  tt$UserID <- tt$UserID.last
  tt$UserID.last <- NULL; tt$UserID.next <- NULL
  return(tt)
}

##
# Load GWA trace file in as a data frame.
#
# Input:
# - trace.file: workload trace input file, as available in the GWA website
# - comment.char: character used to indicate a comment line in the file
# - sep: separator of fields in the file
# Output:
# - returns a data frame of the workload trace
##
ReadGWATrace <- function(trace.file, comment.char="&", sep="\t") {
  trace <- read.table(trace.file, 
      col.names=c("JobID", "SubmitTime", "WaitTime", "RunTime", "NProcs", "AverageCPUTimeUsed", 
          "UsedMemory", "ReqNProcs", "ReqTime", "ReqMemory", "Status", "UserID", "GroupID", 
          "ExecutableID", "QueueID", "PartitionID", "OrigSiteID", "LastRunSiteID", "JobStructure", 
          "JobStructureParams", "UsedNetwork", "UsedLocalDiskSpace", "UsedResources", "ReqPlatform", 
          "ReqNetwork", "ReqLocalDiskSpace", "ReqResources", "VOID", "ProjectID"), 
      colClasses=c(NA, NA, NA, NA, "NULL", "NULL", "NULL", NA, NA, "NULL", NA, NA, "NULL", NA, 
                   "NULL", "NULL", NA, NA, "NULL", NA, "NULL", "NULL", "NULL", "NULL", "NULL", 
                   "NULL", "NULL", "NULL", "NULL"),
      comment.char=comment.char, sep=sep)   
  trace$FinishTime <- trace$SubmitTime + trace$RunTime
  trace$FinishTime <- with(trace, ifelse(WaitTime > 0, FinishTime+WaitTime, FinishTime))
  return(trace[order(trace$SubmitTime),])
}

##
# Generates new workload file with information about the Bag-of-Tasks (BoT) job id in the
# "JobStructureParams" field.
#
# Input:
# - trace.file: workload trace file as available in the GWA website
# Output:
# - file containing the workload trace with added information about the BoT ID in the field
#   "JobStructureParams"
##
GenerateBoTInfoFromGWA <- function(trace.file) {
  trace <- ReadGWATrace(trace.file)
  trace <- subset(trace, RunTime > 0 & UserID != -1)
  trace <- trace[order(trace$UserID, trace$SubmitTime),]
  trace$JobStructureParams <- GroupBoTByLastTaskDeltaT(trace)
  write.table(trace, file=paste(file, "jid.txt", sep="_"), sep="\t", col.names=FALSE, 
              row.names=FALSE, quote=FALSE)
}

##
# Group tasks in Bag-of-Tasks (BoT) jobs according to the approach proposed in: 
# Iosup, A., Jan, M., Sonmez, O., and Epema, D. 
# The characteristics and performance of groups of jobs in grids. In: Euro-Par, 2005.
#
# Input:
# - trace: data frame of GWA workload trace
# - deltat: time window to group tasks into BoT, as specified by Iosup et al.
# Output:
# - returns a vector of BoT IDs, one for each task in each row of "trace"
##
GroupBoTByLastTaskDeltaT <- function(trace, deltat) {
  iat <- diff(trace$SubmitTime)
  newbatch <- c(1,as.numeric(iat > deltat | iat < 0))
  cumsum(newbatch)
}

##
# Apply filter to remove values that occur less than "threshold" times.
#
# Input: 
# - df: data frame
# - attribute: attribute from the data frame to apply the filter
# - threshold: lower bound for the frequency of attribute values in the data frame
# Output:
# - returns the vector of attribute values that occurs more or equal than "threshold" times
##
FilterByFrequency <- function(df, attribute, threshold=100) {
  freq <- table(df[attribute])
  return(names(freq[freq >= threshold])) 
}

##
# Calculate the proportion of data values that are higher than the threshold.
#
# Input:
# - x: data values
# - threshold: lower bound to calculate the proportion
# Output:
# - returns the proportion of data values in "x" that are higher than the "threshold"
##
HigherThanValueProportion <- function(x, threshold=0.05) {
  length(x[x>threshold])/length(x)
}