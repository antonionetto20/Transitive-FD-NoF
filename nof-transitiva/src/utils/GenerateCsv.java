package utils;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import nof.Interaction;
import nof.NetworkOfFavors;
import peer.Peer;
import simulator.PeerComunity;
import simulator.Simulator;
 
public class GenerateCsv{
	
	private String outputFile;
	private Simulator simulator;	
		
	public GenerateCsv(String outputFile, Simulator simulator){
		this.outputFile = outputFile + ".csv";
		this.simulator = simulator; 
	}
	
	public void outputPeers(){
		FileWriter writer = this.createHeaderForPeer();
		writer = writePeers(writer);
		flushFile(writer);
	}
	
	public void outputPeerDetalhado(){
		FileWriter writer = this.createHeaderForPeerDetalhado();
		writer = writePeerDetalhado(writer);
		flushFile(writer);
	}
	
	public void outputSteps(){
		FileWriter writer = this.createHeaderForPeersStep();
		writer = writeSteps(writer);
		flushFile(writer);
	}
	
	public void outputSharingLevel(){
		FileWriter writer = this.createHeaderForSharingLevel();
		writer = writeSharingLevel(writer); 
		flushFile(writer);
	}
	
	private FileWriter createHeaderForPeersStep(){
		FileWriter writer = null;
		try {
			writer = new FileWriter(this.outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			 writer.append("Step");
			 writer.append(',');
			 writer.append("TotalDoado");
			 writer.append(',');
			 writer.append("nivelCompartilhamento");
			 writer.append(',');
			 writer.append("fairness");
			 writer.append(',');
			 writer.append("satisfaction");
			 writer.append(',');
			 writer.append("overallBalance");
			 writer.append(',');
			 writer.append("capacity");
			 writer.append(',');			 
			 writer.append("kappa");
			 writer.append(',');
			 writer.append("numberOfPeers");
			 writer.append(',');
			 writer.append("NoF");
			 writer.append(',');
			 writer.append("tMin");
			 writer.append(',');
			 writer.append("tMax");
			 writer.append(',');
			 writer.append("delta");
			 writer.append(',');
			 writer.append("seed");
			 writer.append('\n');
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return writer;	    
	}
	
	private FileWriter createHeaderForPeerDetalhado(){
		
		FileWriter writer = null;
		try {
			writer = new FileWriter(this.outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			 writer.append("Step");
			 writer.append(',');
			 writer.append("Peer");
			 writer.append(',');
			 writer.append("fairness");
			 writer.append(',');
			 writer.append("satisfaction");
			 writer.append(',');
			 writer.append("overallBalance");
			 writer.append(',');
			 writer.append("capacity");
			 writer.append(',');			 
			 writer.append("kappa");
			 writer.append(',');
			 writer.append("numberOfPeers");
			 writer.append(',');
			 writer.append("NoF");
			 writer.append(',');
			 writer.append("tMin");
			 writer.append(',');
			 writer.append("tMax");
			 writer.append(',');
			 writer.append("delta");
			 writer.append(',');
			 writer.append("seed");
			 writer.append('\n');
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return writer;	    
	}
	
	private FileWriter createHeaderForPeer(){
		
		FileWriter writer = null;
		try {
			writer = new FileWriter(this.outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			 writer.append("fairness");
			 writer.append(',');
			 writer.append("satisfaction");
			 writer.append(',');
			 writer.append("overallBalance");
			 writer.append(',');
			 writer.append("capacity");
			 writer.append(',');			 
			 writer.append("kappa");
			 writer.append(',');
			 writer.append("numberOfPeers");
			 writer.append(',');
			 writer.append("NoF");
			 writer.append(',');
			 writer.append("tMin");
			 writer.append(',');
			 writer.append("tMax");
			 writer.append(',');
			 writer.append("delta");
			 writer.append(',');
			 writer.append("seed");
			 writer.append('\n');
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return writer;	    
	}
	
	private FileWriter createHeaderForSharingLevel() {
		FileWriter writer = null;
		try {
			writer = new FileWriter(this.outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			 writer.append("step");
			 writer.append(',');
			 writer.append("sharingLevelTotal");
			 writer.append(',');
			 writer.append("NoF");
			 writer.append(',');
			 writer.append("tMin");
			 writer.append(',');
			 writer.append("tMax");
			 writer.append(',');
			 writer.append("delta");
			 writer.append(',');
			 writer.append("deviation");
			 writer.append(',');
			 writer.append("seed");
			 writer.append('\n');
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return writer;
	}
	
	public FileWriter writeSteps(FileWriter writer){
		double kappa;
		int numberOfPeers = simulator.getPeerComunity().getNumCollaborators();
		
		String nof = "";
		if(simulator.isTransitivity())
			nof += "Transitive ";
		if(!simulator.isFdNof())
			nof += "SD-NoF";
		else
			nof += "FD-NoF";
		
		double tMin = simulator.getTMin();
		double tMax = simulator.getTMax();
		double delta = simulator.getDeltaC();
		int seed = simulator.getSeed();
		
		double capacity = 0;
		double overallBalance = 0;
		
		for(Peer p : PeerComunity.peers.values()){
			for(Interaction in : p.getInteractions()){
				overallBalance += NetworkOfFavors.calculateBalance(in.getConsumed(),in.getDonated());
			}
			capacity += p.getInitialCapacity();
		}
		double totalDoado = 0;
		for(int i=0; i < simulator.getNumSteps();i++){
			double donatedTotal = 0;
			double nivel_compartilhamento = 0;
			double satisfacaoTotal = 0;
			double fairnessTotal = 0;
			double satisfactionMedia = 0;
			double fairnessMedia= 0;
			double totalPedido = 0;
			double totalOfertado = 0;
			
			
			for(Peer p : PeerComunity.peers.values()){// consumed é o da federacao
				/*if(i == 41){
					System.out.println("Peer: " + p.getId() + " capacidade: " + p.getInitialCapacity() + " Consumindo: " + (p.getCurrentConsumed(i)-p.getCurrentConsumed(i-1)) + " Doando: " + (p.getCurrentDonated(i)-p.getCurrentDonated(i-1)) + " Demanda: "+ p.getDemand() + " Nível Compartilhamento: "+(p.getCurrentDonated(i)/p.getInitialCapacity()));
				}*/
				
				fairnessTotal += NetworkOfFavors.getFairness(p.getCurrentConsumed(i), p.getCurrentDonated(i));	
				satisfacaoTotal += NetworkOfFavors.getSatisfaction(p.getCurrentConsumed(i), p.getCurrentRequested(i));
				if(i > 0){
					donatedTotal += (p.getCurrentDonated(i) - p.getCurrentDonated(i-1));
					totalPedido += p.getCurrentRequested(i) - p.getCurrentRequested(i-1);
				}else{
					donatedTotal += p.getCurrentDonated(i);
					totalPedido += p.getCurrentRequested(i);
					
				}
				totalOfertado += p.getCapacitySuppliedHistory()[i];
				totalDoado += donatedTotal;
				
			}
			kappa = totalPedido/totalOfertado;
			nivel_compartilhamento = donatedTotal/(simulator.getPeerComunity().getCapacidade()*PeerComunity.peers.size());
			satisfactionMedia = satisfacaoTotal/PeerComunity.peers.size();
			fairnessMedia = fairnessTotal/PeerComunity.peers.size();
			
			try {
				writer.append((i+1)+","+donatedTotal+","+nivel_compartilhamento+","+fairnessMedia+","+satisfactionMedia+","+overallBalance+","+capacity+",");
				writer.append(kappa+","+numberOfPeers+","+nof+","+tMin+","+tMax+","+delta+","+seed+"\n");
				
			} catch (IOException e) {
				Simulator.logger.finest("Exception while writing to output (csv) the performance of peers.");
				e.printStackTrace();
			}	
		}   
		DecimalFormat df = new DecimalFormat("0.##");
		String dx = df.format(totalDoado);
		System.out.println("Total doado: "+dx); // total doado
		return writer;					
	}
	
	public FileWriter writePeerDetalhado(FileWriter writer){
		
		double kappa = simulator.getKappa();
		
		int numberOfPeers = simulator.getPeerComunity().getNumCollaborators();
		
		String nof = "";
		if(simulator.isTransitivity())
			nof += "Transitive ";
		if(!simulator.isFdNof())
			nof += "SD-NoF";
		else
			nof += "FD-NoF";
		
		double tMin = simulator.getTMin();
		double tMax = simulator.getTMax();
		double delta = simulator.getDeltaC();
		
		for(Peer p : PeerComunity.peers.values()){
			double fairness = 0;
			double satisfaction = 0;
			
			double overallBalance = 0;
			for(Interaction i : p.getInteractions())
				overallBalance += NetworkOfFavors.calculateBalance(i.getConsumed(), i.getDonated());
			
			double capacity = p.getInitialCapacity();			
			
			int seed = simulator.getSeed();
			
			for(int i=0; i < simulator.getNumSteps();i++){
				fairness = NetworkOfFavors.getFairness(p.getCurrentConsumed(i), 
						p.getCurrentDonated(i));
				
				satisfaction = NetworkOfFavors.getSatisfaction(p.getCurrentConsumed(i), 
						p.getCurrentRequested(i));
				
				try {
					writer.append((i+1)+","+p.getId()+","+fairness+","+satisfaction+","+overallBalance+","+capacity+",");
					writer.append(kappa+","+numberOfPeers+","+nof+","+tMin+","+tMax+","+delta+","+seed+"\n");
				} catch (IOException e) {
					Simulator.logger.finest("Exception while writing to output (csv) the performance of peers.");
					e.printStackTrace();
				}	
			}
		}
		return writer;					
	}
	
	private FileWriter writePeers(FileWriter writer){
		
		double kappa = simulator.getKappa();
		
		int numberOfPeers = simulator.getPeerComunity().getNumCollaborators();
		
		String nof = "";
		if(simulator.isTransitivity())
			nof += "Transitive ";
		if(!simulator.isFdNof())
			nof += "SD-NoF";
		else
			nof += "FD-NoF";
		
		double tMin = simulator.getTMin();
		double tMax = simulator.getTMax();
		double delta = simulator.getDeltaC();
		
		for(Peer p : PeerComunity.peers.values()){
			double fairness = NetworkOfFavors.getFairness(p.getCurrentConsumed(simulator.getNumSteps()-1), 
					p.getCurrentDonated(simulator.getNumSteps()-1));
			
			double satisfaction = NetworkOfFavors.getSatisfaction(p.getCurrentConsumed(simulator.getNumSteps()-1), 
					p.getCurrentRequested(simulator.getNumSteps()-1));
			
			System.out.println("@@ "+p.getId()+": cons="+p.getCurrentConsumed(simulator.getNumSteps()-1)+
					", req="+p.getCurrentRequested(simulator.getNumSteps()-1)+", sat="+satisfaction);
			
			System.out.println("@@ "+p.getId()+": cons="+p.getCurrentConsumed(simulator.getNumSteps()-1)+
					", prov="+p.getCurrentDonated(simulator.getNumSteps()-1)+", fair="+fairness);
			
			double overallBalance = 0;
			for(Interaction i : p.getInteractions())
				overallBalance += NetworkOfFavors.calculateBalance(i.getConsumed(), i.getDonated());
			
			System.out.println("##" + p.getId() + " " + overallBalance);
			
			double capacity = p.getInitialCapacity();			
			
			int seed = simulator.getSeed();
			
			try {
				writer.append(fairness+","+satisfaction+","+overallBalance+","+capacity+",");
				writer.append(kappa+","+numberOfPeers+","+nof+","+tMin+","+tMax+","+delta+","+seed+"\n");
			} catch (IOException e) {
				Simulator.logger.finest("Exception while writing to output (csv) the performance of peers.");
				e.printStackTrace();
			}			
		}	
		
		return writer;
	}

	
	private FileWriter writeSharingLevel(FileWriter writer){ 
		
		String nof = "";
		if(simulator.isTransitivity())
			nof += "Transitive ";
		if(!simulator.isFdNof())
			nof += "SD-NoF";
		else
			nof += "FD-NoF";
		
		double tMin = simulator.getTMin();
		double tMax = simulator.getTMax();
		double delta = simulator.getDeltaC();
		int seed = simulator.getSeed();
				
		for(int step = 0; step < simulator.getNumSteps(); step++){
			double totalCapacity = 0, provided = 0;
			for(Peer p : PeerComunity.peers.values()){
				/**if(p instanceof Collaborator && p.getStateHistory()[step]==State.PROVIDING){ **///desativei 03/09/17
					totalCapacity += p.getInitialCapacity();					
					provided += p.getDonatedHistory()[step];
			}
			
			try {
				
				writer.append((step+1)+","+(provided/totalCapacity)+","+
								nof+","+tMin+","+tMax+","+delta+","+seed+"\n");
			} catch (IOException e) {
				Simulator.logger.finest("Exception while writing to output (csv) the sharing level of the federation.");
				e.printStackTrace();
			}
		}
		
		return writer;
	}
	
	private void flushFile(FileWriter writer){		
	    try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			Simulator.logger.finest("Exception while flushing data to File.");
			e.printStackTrace();
		}		
	}

}
