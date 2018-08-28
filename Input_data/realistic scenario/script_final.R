## Norms the data within specified groups in a data frame; it normalizes each
## subject (identified by idvar) so that they have the same mean, within each group
## specified by betweenvars.
##   data: a data frame.
##   idvar: the name of a column that identifies each subject (or matched subjects)
##   measurevar: the name of a column that contains the variable to be summariezed
##   betweenvars: a vector containing names of columns that are between-subjects variables
##   na.rm: a boolean that indicates whether to ignore NA's
normDataWithin <- function(data=NULL, idvar, measurevar, betweenvars=NULL,
                           na.rm=FALSE, .drop=TRUE) {
  library(plyr)
  
  # Measure var on left, idvar + between vars on right of formula.
  data.subjMean <- ddply(data, c(idvar, betweenvars), .drop=.drop,
                         .fun = function(xx, col, na.rm) {
                           c(subjMean = mean(xx[,col], na.rm=na.rm))
                         },
                         measurevar,
                         na.rm
  )
  
  # Put the subject means with original data
  data <- merge(data, data.subjMean)
  
  # Get the normalized data in a new column
  measureNormedVar <- paste(measurevar, "_norm", sep="")
  data[,measureNormedVar] <- data[,measurevar] - data[,"subjMean"] +
    mean(data[,measurevar], na.rm=na.rm)
  
  # Remove this subject mean column
  data$subjMean <- NULL
  
  return(data)
}

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

## Summarizes data, handling within-subjects variables by removing inter-subject variability.
## It will still work if there are no within-S variables.
## Gives count, un-normed mean, normed mean (with same between-group mean),
##   standard deviation, standard error of the mean, and confidence interval.
## If there are within-subject variables, calculate adjusted values using method from Morey (2008).
##   data: a data frame.
##   measurevar: the name of a column that contains the variable to be summariezed
##   betweenvars: a vector containing names of columns that are between-subjects variables
##   withinvars: a vector containing names of columns that are within-subjects variables
##   idvar: the name of a column that identifies each subject (or matched subjects)
##   na.rm: a boolean that indicates whether to ignore NA's
##   conf.interval: the percent range of the confidence interval (default is 95%)
summarySEwithin <- function(data=NULL, measurevar, betweenvars=NULL, withinvars=NULL,
                            idvar=NULL, na.rm=FALSE, conf.interval=.95, .drop=TRUE) {
  
  # Ensure that the betweenvars and withinvars are factors
  factorvars <- vapply(data[, c(betweenvars, withinvars), drop=FALSE],
                       FUN=is.factor, FUN.VALUE=logical(1))
  
  if (!all(factorvars)) {
    nonfactorvars <- names(factorvars)[!factorvars]
    message("Automatically converting the following non-factors to factors: ",
            paste(nonfactorvars, collapse = ", "))
    data[nonfactorvars] <- lapply(data[nonfactorvars], factor)
  }
  
  # Get the means from the un-normed data
  datac <- summarySE(data, measurevar, groupvars=c(betweenvars, withinvars),
                     na.rm=na.rm, conf.interval=conf.interval, .drop=.drop)
  
  # Drop all the unused columns (these will be calculated with normed data)
  datac$sd <- NULL
  datac$se <- NULL
  datac$ci <- NULL
  
  # Norm each subject's data
  ndata <- normDataWithin(data, idvar, measurevar, betweenvars, na.rm, .drop=.drop)
  
  # This is the name of the new column
  measurevar_n <- paste(measurevar, "_norm", sep="")
  
  # Collapse the normed data - now we can treat between and within vars the same
  ndatac <- summarySE(ndata, measurevar_n, groupvars=c(betweenvars, withinvars),
                      na.rm=na.rm, conf.interval=conf.interval, .drop=.drop)
  
  # Apply correction from Morey (2008) to the standard error and confidence interval
  #  Get the product of the number of conditions of within-S variables
  nWithinGroups    <- prod(vapply(ndatac[,withinvars, drop=FALSE], FUN=nlevels,
                                  FUN.VALUE=numeric(1)))
  correctionFactor <- sqrt( nWithinGroups / (nWithinGroups-1) )
  
  # Apply the correction factor
  ndatac$sd <- ndatac$sd * correctionFactor
  ndatac$se <- ndatac$se * correctionFactor
  ndatac$ci <- ndatac$ci * correctionFactor
  
  # Combine the un-normed means with the normed results
  merge(datac, ndatac)
}

# Install.packages("gmodels")

dados <- read.csv(file="C:\\Users\\antonio\\Downloads\\script\\resultadoTudao.csv", header=TRUE, sep=",")
head(dados)

library(ggplot2)


######################### mediana ############################

tgc <- summarySE(dados, measurevar="Ganho", groupvars=c("C","n"))
tgc

# Standard error of the mean
ggplot(tgc, aes(x=n, y=Ganho, colour=C)) + 
  geom_errorbar(aes(ymin=Ganho-se, ymax=Ganho+se), width=.1) +
  geom_line() +
  geom_point()

#########################  BOXPLOT 1 ############################

png("boxplot-n60-c50e100-k3e4e5.png", width=500, height=275)
dados$C=factor(dados$C , levels=c("C=50", "C=100"))
g1 <- ggplot(dados[dados$n=="n=60",], aes(x=C , y=Ganho, fill=C)) + 
  geom_boxplot() + facet_grid(.~k) + geom_jitter(aes(colour = C), 
  position = position_jitter(width = .9), alpha = 0.9) +
  theme_bw() +
  theme(axis.title.x=element_blank(), axis.text.x=element_blank(), axis.ticks.x=element_blank()) +
  theme(axis.text=element_text(size=14), axis.title=element_text(size=14)) +
  theme(legend.title=element_blank(),legend.text=element_text(size=14)) +
  theme(strip.text.x = element_text(size = 12)) +
  ylab("increase in donation (%)") + theme(legend.position="bottom")#+ theme(legend.position="none")
dev.off()
g1
#########################  BOXPLOT 2 ############################

g2 <- ggplot(dados[dados$n=="n=20" ,], aes(x=C , y=Ganho, fill=C)) + 
  geom_boxplot() + facet_grid(.~k) + geom_jitter(aes(colour = C), 
                                                 position = position_jitter(width = .9), alpha = 0.9) +
  theme_bw() +
  theme(axis.title.x=element_blank(), axis.text.x=element_blank(), axis.ticks.x=element_blank()) +
  theme(axis.text=element_text(size=14), axis.title=element_text(size=14)) +
  theme(legend.title=element_blank(),legend.text=element_text(size=14)) +
  theme(strip.text.x = element_text(size = 12)) +
  ylab("aumento de doação (%)") #+ theme(legend.position="none")
g2
#########################  BOXPLOT 3 ############################

g3 <- ggplot(dados[dados$n=="n=10" ,], aes(x=C , y=Ganho, fill=C)) + 
  geom_boxplot() + facet_grid(.~k) + geom_jitter(aes(colour = C), 
                                                 position = position_jitter(width = .9), alpha = 0.9) +
  theme_bw() +
  theme(axis.title.x=element_blank(), axis.text.x=element_blank(), axis.ticks.x=element_blank()) +
  theme(axis.text=element_text(size=14), axis.title=element_text(size=14)) +
  theme(legend.title=element_blank(),legend.text=element_text(size=14)) +
  theme(strip.text.x = element_text(size = 12)) +
  ylab("aumento de doação (%)")
g3
#########################  BOXPLOT TESTE FACET ############################
dados$C=factor(dados$C , levels=c("C=50", "C=100"))
ggplot(dados[dados$n=="n=10" | dados$n=="n=20",], aes(x=C , y=Ganho, fill=C)) + 
  geom_boxplot() + facet_grid(n~k) + geom_jitter(aes(colour = C), 
                                                 position = position_jitter(width = .9), alpha = 0.9) +
  theme_bw() +
  theme(axis.title.x=element_blank(), axis.text.x=element_blank(), axis.ticks.x=element_blank()) +
  theme(axis.text=element_text(size=14), axis.title=element_text(size=14)) +
  theme(legend.title=element_blank(),legend.text=element_text(size=14)) +
  theme(strip.text.x = element_text(size = 12)) +
  ylab("increase in donation (%)") + theme(legend.position="bottom")

#########################  DENSIDADE 1 ############################

png("histograma-n10e20-c50-k3e4e5.png", width=500, height=275)

  ggplot(dados[dados$C=="C=50" & dados$n=="n=10" | dados$n=="n=20",], 
         aes(x=Ganho, colour=interaction(k,n))) + 
    
    geom_density() + facet_grid(.~k) +
    
  theme_bw() +
  theme(axis.text=element_text(size=14), axis.title=element_text(size=14)) +
  theme(legend.title=element_blank(),legend.text=element_text(size=14)) +
  theme(strip.text.x = element_text(size = 12)) +
  xlab("increase in donation (%)") + ylab("density") + 
  
  scale_color_discrete(breaks=c("k=3.n=10","k=3.n=20","k=4.n=10","k=4.n=20","k=5.n=10","k=5.n=20"))
dev.off()

png("histograma-n10e20-c100-k3e4e5.png", width=500, height=275)
ggplot(dados[dados$C=="C=100" & dados$n=="n=10" | dados$n=="n=20",], aes(x=Ganho, colour=interaction(k,n))) + geom_density() + facet_grid(.~k) +
  theme_bw() +
  theme(axis.text=element_text(size=14), axis.title=element_text(size=14)) +
  theme(legend.title=element_blank(),legend.text=element_text(size=14)) +
  theme(strip.text.x = element_text(size = 12)) +
  xlab("aumento de doação (%)") + ylab("densidade")
dev.off()



















library(ggplot2)
# boxplot simples
bp <- ggplot(data=PlantGrowth, aes(x=group, y=weight, fill=group)) + geom_boxplot()
bp

bp + scale_fill_discrete(breaks=c("trt2","ctrl","trt1"))







library(gridExtra)

p1 <- ggplot(mtcars, aes(mpg, cyl)) + geom_point()
p2 <- ggplot(mtcars, aes(mpg, cyl)) + geom_line()
p3 <- ggplot(mtcars, aes(mpg, cyl)) + geom_line(color="blue")
p4 <- ggplot(mtcars, aes(mpg, cyl)) + geom_line(color="red")

grid.arrange(g2,g3, ncol=1)


