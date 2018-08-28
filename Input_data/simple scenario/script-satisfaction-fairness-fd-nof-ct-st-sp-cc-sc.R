# grafico de contenções

setwd("C:\\Users\\antonio\\Desktop\\Planilhas")

dados.c20 = read.csv2(file="peerCapacity20-users10-peers15-clusters5.csv",sep=",")
dados.c22 = read.csv2(file="peerCapacity22-users10-peers15-clusters5.csv",sep=",")
dados.c24 = read.csv2(file="peerCapacity24-users10-peers15-clusters5.csv",sep=",")
dados.c26 = read.csv2(file="peerCapacity26-users10-peers15-clusters5.csv",sep=",")
dados.c28 = read.csv2(file="peerCapacity28-users10-peers15-clusters5.csv",sep=",")
dados.c30 = read.csv2(file="peerCapacity30-users10-peers15-clusters5.csv",sep=",")

dados = rbind(dados.c20,dados.c22,dados.c24,dados.c26,dados.c28,dados.c30)

#install.packages("ggplot2")
library(ggplot2)

dados$kappa=as.numeric(levels(dados$kappa))[dados$kappa]

ggplot(data=dados, aes(x=t, y=kappa, group = capacity, colour=capacity)) +
  geom_line(size = 0, alpha = 0.9) + facet_grid(capacity ~ .) +
  scale_y_continuous(breaks=c(0, 1, 2),limits=c(0,2)) 

#################################################################################################

# gráfico de nivel de compartilhamento fdnof t e nao t

setwd("C:\\Users\\antonio\\Desktop\\Planilhas")

dados.fdnof_T = read.csv2(file="fdnof-transitiva-20-capacidade-grao-60.csv",sep=",")
dados.fdnof_NT = read.csv2(file="fdnof-naotransitiva-20-capacidade-grao-60.csv",sep=",")
dados = rbind(dados.fdnof_T, dados.fdnof_NT)

#install.packages("ggplot2")
library(ggplot2)

dados$nivelCompartilhamento=as.numeric(levels(dados$nivelCompartilhamento))[dados$nivelCompartilhamento]

ggplot(data=dados, aes(x=Step, y=nivelCompartilhamento, group = NoF, colour=NoF)) +
  geom_line(size = 0, alpha = 0.9) + facet_grid(NoF ~ .) +
  scale_y_continuous(breaks=c(0, 1, 2),limits=c(0,2))

########################################################################################################

# diferença do nivel de compartilhamento

setwd("C:\\Users\\antonio\\Desktop\\Planilhas")

dados.dif = read.csv2(file="diferencaNivelCompartilhamento.csv",sep=",")

dados = rbind(dados.dif)

#install.packages("ggplot2")
library(ggplot2)

dados$Diferenca=as.numeric(levels(dados$Diferenca))[dados$Diferenca]


ggplot(data=dados,  aes(x = Step, y = Diferenca)) +
  geom_bar(stat="identity")

########################################################################################################

# gráficos de satisfacao e justica fdnof t e nao t

setwd("C:\\Users\\antonio\\Desktop\\Graficos\\Planilhas")
dados.fdnof = read.csv2(file="FD-NoF-ST-SP.csv",sep=",")
dados.fdnof$no = "cooperativos"

dados.fdnoftrans = read.csv2(file="FD-NoF-CT-SP.csv",sep=",")
dados.fdnoftrans$no = "cooperativos"

dados.fdnoftransComCar = read.csv2(file="FD-NoF-CT-SP-CC.csv",sep=",")
dados.fdnoftransComCar$no = "cooperativos"
dados.fdnoftransComCar[dados.fdnoftransComCar$Peer==3,]$no="carona"

dados.fdnof$NoF="FD-NoF s/ carona"
dados.fdnoftrans$NoF="FD-NoF Transitiva s/ carona"
dados.fdnoftransComCar$NoF="FD-NoF Transitiva c/ carona"

dados = rbind(dados.fdnof,dados.fdnoftrans, dados.fdnoftransComCar)

#install.packages("ggplot2")
library(ggplot2)

dados$Satisfação=as.numeric(levels(dados$Satisfação))[dados$Satisfação]
dados$Paridade=as.numeric(levels(dados$Paridade))[dados$Paridade]

ggplot(data=dados, aes(x=Passo, y=Paridade, group=Peer, color=NoF, shape=NoF)) +
  geom_point(size = 3, alpha=0.5) + scale_shape_manual(values=c(20, 20, 20))+ 
  scale_color_manual(values= c("blue", "red", "yellow")) +
  scale_y_continuous(breaks=c(0, 0.33, 1),limits=c(0,1))+ theme(legend.position="top")




ggplot(data=dados, aes(x=Passo, y=Satisfação, group=NoF, shape=NoF, color=NoF)) +
  geom_point(size = 3, alpha=0.5) + scale_shape_manual(values=c(20, 20, 19))+ 
  scale_color_manual(values= c("blue", "red", "yellow")) +
  scale_y_continuous(breaks=c(0, 0.33, 1),limits=c(0,1))+ theme(legend.position="none")



##############################################################################################

# grafico de total doado por step

setwd("C:\\Users\\antonio\\Desktop\\Planilhas")

dados.fdnof_T = read.csv2(file="fdnof-transitiva-20-capacidade-grao-60.csv",sep=",")
dados.fdnof_NT = read.csv2(file="fdnof-naotransitiva-20-capacidade-grao-60.csv",sep=",")
dados = rbind(dados.fdnof_T, dados.fdnof_NT)

#install.packages("ggplot2")
library(ggplot2)

dados$TotalDoado=as.numeric(levels(dados$TotalDoado))[dados$TotalDoado]

ggplot(data=dados, aes(x=Step, y=TotalDoado)) +
  geom_line(size = 0, alpha = 0.9) + facet_grid(NoF ~ .) +
  scale_y_continuous(breaks=c(0, 1, 2),limits=c(0,2))

############################################################################################

# grafico diferenca total doado

setwd("C:\\Users\\antonio\\Desktop\\Planilhas")

dados.dif = read.csv2(file="diferencaTDoadoGrao600.csv",sep=",")

dados = rbind(dados.dif)

#install.packages("ggplot2")
library(ggplot2)

dados$Diferenca=as.numeric(levels(dados$Diferenca))[dados$Diferenca]


ggplot(data=dados,  aes(x = Step, y = Diferenca)) +
  geom_bar(stat="identity")

#########################################################################################

# grafico nivel compartilhamento total

setwd("C:\\Users\\antonio\\Desktop\\TCC Fase Final\\resultados cenario simples\\Graficos\\Planilhas")
dados.fdnof = read.csv2(file="FD-NoF-ST-SP-Comp.csv",sep=",")
dados.fdnof$no = "cooperativos"

dados.fdnoftrans = read.csv2(file="FD-NoF-CT-SP-Comp.csv",sep=",")
dados.fdnoftrans$no = "cooperativos"

dados.fdnoftransComCar = read.csv2(file="FD-NoF-CT-SP-CC-Comp.csv",sep=",")

dados.fdnoftransComCar$no = "cooperativos"
dados.fdnoftransComCar[dados.fdnoftransComCar$Peer==3,]$no="carona"

dados.fdnof$NoF="FD-NoF s/ carona"
dados.fdnoftrans$NoF="FD-NoF Transitiva s/ carona"
dados.fdnoftransComCar$NoF="FD-NoF Transitiva c/ carona"

dados = rbind(dados.fdnof,dados.fdnoftrans, dados.fdnoftransComCar)

#install.packages("ggplot2")
library(ggplot2)

dados$Nível_de_Compartilhamento=as.numeric(levels(dados$Nível_de_Compartilhamento))[dados$Nível_de_Compartilhamento]
ggplot(data=dados, aes(x=Passo, y=Nível_de_Compartilhamento, group=NoF, shape=NoF, color=NoF)) +
  geom_point(size = 3, alpha=0.5) + scale_shape_manual(values=c(20, 20, 20))+ geom_line()+
  scale_color_manual(values= c("blue", "red", "yellow")) +
  scale_y_continuous(breaks=c(0, 0.33, 1),limits=c(0,1))+
  scale_x_continuous(breaks=c(0,100,200,300,400, 500),limits=c(0,500)) +
  theme(legend.position="none") + xlab("Passo de Tempo") +
  ylab("Nível de Compartilhamento") 

ggplot(mtcars, aes(x = mpg, y = disp, colour = as.factor(cyl))) +
  geom_point() + 
  facet_grid(.~am)

