package simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import nof.Interaction;
import nof.NetworkOfFavors;
import peer.Peer;
import peer.PeerState;
import peer.Quadruplet;
import peer.Quintuplet;
import peer.Triplet;
import utils.GenerateCsv;

public class Simulator {	
	
	private PeerComunity peerComunity; 
	private MemberPicker memberPicker; // acho que seja a classe que sorteia a quem doar e a quem prover
	private Market market; // acho que algo parecido coma linha de cima
	private List <Integer> consumersList, idlePeersList, providersList;	// lista dos estados
	private int numSteps; // numero de passos
	private int currentStep;
	private int qtdePeersTransitivos;
	//nof
	private boolean fdNof, transitivity;						
	private double tMin, tMax;
	private double deltaC;					
	//others
	public final static Logger logger = Logger.getLogger(Simulator.class.getName());
	private String outputFile;
	private double kappa;	
	private int seed;
	
	public Simulator(boolean fdNoF, boolean transitivity, double tMin, double tMax, double deltaC, 
			int seed, Level level, PeerComunity pc, String outputFile, double kappa, int qtdePeersTransitivos) {	
		
		peerComunity = pc;	//the constructor also creates the peers
		memberPicker = new MemberPicker(seed);
		market = new Market(this);
		consumersList = new ArrayList<Integer> ();		
		idlePeersList = new ArrayList<Integer> ();		
		providersList = new ArrayList<Integer> ();
		//this.numSteps = 500;
		this.numSteps = peerComunity.getNumberStep();
		System.out.println(numSteps);
		this.currentStep = 0;
		this.qtdePeersTransitivos = qtdePeersTransitivos;
		this.fdNof = fdNoF;
		this.transitivity = transitivity;
		this.tMin = tMin;
		this.tMax = tMax;
		this.deltaC = deltaC;
		/* Logger setup */
		Simulator.logger.setLevel(level);
		Simulator.logger.setUseParentHandlers(false);
		ConsoleHandler handler = new ConsoleHandler();
	    handler.setLevel(level);
	    logger.addHandler(handler);
		this.outputFile = outputFile;
		this.kappa = kappa;
		this.seed = seed;
	}
	
	public void startSimulation(){	
		//the constructor of PeerComunity already creates the peers
		for(int i = 0; i < this.numSteps; i++){
			if(i == 41){
				System.out.println("ok");
			}
			
			this.setupPeersState();
			this.performCurrentStepDonations();
			
		}
		exportData();
	}
	
	private void setupPeersState(){
		peerComunity.setupPeers(currentStep);
		for(Peer p : PeerComunity.peers.values()){
			Simulator.logger.finest(p.toString());
			if(p.getState()==PeerState.CONSUMER)
				consumersList.add(p.getId());
			else if(p.getState()==PeerState.PROVIDER)
				providersList.add(p.getId());
		}
	}	
	
	//performs all donations of the current step. Executa todas as doações do passo atual
	private void performCurrentStepDonations(){
		Peer provider = null;
		//while there is any collaborator willing to donate, choose one
		//Enquanto houver algum colaborador disposto a doar, escolha um
		while(!providersList.isEmpty() && !consumersList.isEmpty()){
			provider = memberPicker.choosesRandomPeer(providersList);
			
			List <Quintuplet> consumingPeers = new ArrayList<Quintuplet>();
			
			consumingPeers.addAll(memberPicker.getConsumersWithPositiveBalance(provider,consumersList));
			// TODO fazer doação com todos os consumingPeers
			
			if(transitivity)
				
				consumingPeers.addAll(memberPicker.getConsumersWithTransitiveBalance(provider,this.qtdePeersTransitivos)); // parametro simulador
			consumingPeers.addAll(memberPicker.getConsumersWithZeroBalance(provider));
			
			while(consumingPeers.size()>0 && provider.getResourcesDonatedInCurrentStep()<(provider.getInitialCapacity()-0.000000000000001)){
				List<Quintuplet> peersToDonate = new ArrayList<Quintuplet>();
				peersToDonate.addAll(memberPicker.getNextConsumersWithSameBalance(consumingPeers));
				
				market.performDonation(provider, peersToDonate);
				for(Quintuplet peer : peersToDonate)
					consumingPeers.remove(peer);
			}
			market.removePeerThatDonated(provider);
		}
		setupNextStep();
	}
	
	private void setupNextStep(){	// configura proximo passo	
		Simulator.logger.info("Step "+currentStep);
		//here we update the consumed and donated values of each peer
		for(Peer p : PeerComunity.peers.values()){			
				//save last values and update status for next step
				for (Interaction interaction : p.getInteractions())
					interaction.saveLastValues();		
		}		
		Simulator.logger.finest("\n\n\nPasso: "+currentStep);
		for(Peer p : PeerComunity.peers.values()){
			Simulator.logger.finest("Id: "+p.getId()+"; Consumed: "+p.getConsumedHistory()[currentStep]+"; Donated: "+p.getDonatedHistory()[currentStep]);
		}
		// zera as listas de estado
		consumersList.clear();
		idlePeersList.clear();
		providersList.clear();	
		if(this.fdNof)
			this.updateCapacitySupplied();
		
		this.currentStep++;			
	}
	
	//the global and pairwise controllers
	private void updateCapacitySupplied(){ // atualiza a capacidade e fornecimento dos peers	
		if(currentStep>0){		
			for(Peer p : PeerComunity.peers.values()){	
					//pairwise
					for (Interaction interaction : p.getInteractions()) {					
						double lastFairness = NetworkOfFavors.getFairness(interaction.getLastConsumed(), interaction.getLastDonated());	
						double currentFairness = NetworkOfFavors.getFairness(interaction.getConsumed(), interaction.getDonated());						
						boolean change = false;
						if(currentFairness>=0){
							if(currentFairness < tMin)
								interaction.setIncreasingCapacity(false);
							else if(currentFairness > tMax)
								interaction.setIncreasingCapacity(true);
							else{
								if(currentFairness <= lastFairness)
									interaction.setIncreasingCapacity(!interaction.isIncreasingCapacity());
							}
							change = true;
						}							
						if(change){
							double totalAmountOfResources = p.getInitialCapacity();								
							if(interaction.isIncreasingCapacity())		//try to increase the current maxCapacitySupplied
								interaction.setMaxCapacityToSupply(Math.min(totalAmountOfResources, interaction.getMaxCapacityToSupply()+deltaC*totalAmountOfResources));	
							else										//try to decrease the current maxCapacitySupplied
								interaction.setMaxCapacityToSupply(Math.max(0, Math.min(totalAmountOfResources, interaction.getMaxCapacityToSupply()-deltaC*totalAmountOfResources)));
						}						
						interaction.getCapacitySuppliedHistory()[currentStep] = interaction.getMaxCapacityToSupply();						
					}
					//global
					double currentFairness = NetworkOfFavors.getFairness(p.getCurrentConsumed(currentStep), p.getCurrentDonated(currentStep));
					double lastFairness = NetworkOfFavors.getFairness(p.getCurrentConsumed(currentStep-1), p.getCurrentDonated(currentStep-1));					
					boolean change = false;
					if(currentFairness>=0){
						if(currentFairness < tMin)
							p.setIncreasingCapacitySupplied(false);
						else if(currentFairness > tMax)
							p.setIncreasingCapacitySupplied(true);
						else{
							if(currentFairness <= lastFairness)
								p.setIncreasingCapacitySupplied(!p.isIncreasingCapacitySupplied());
						}
						change = true;
					}
					if(change){
						double totalAmountOfResources = p.getInitialCapacity();													
						if(p.isIncreasingCapacitySupplied())				
							p.setMaxCapacityToSupply(Math.min(totalAmountOfResources, p.getMaxCapacityToSupply()+deltaC*totalAmountOfResources));	
						else
							p.setMaxCapacityToSupply(Math.max(0, Math.min(totalAmountOfResources, p.getMaxCapacityToSupply()-deltaC*totalAmountOfResources)));	
//						if(collab.getId()==50)
//							System.out.println("Id: "+collab.getId()+"; Fairness global: "+currentFairness+"; Alfa-Global: "+collab.getMaxCapacityToSupply());
					}														
				}				
		}	
		Simulator.logger.fine("FIM Update capacity supplied");
	}	

	private void exportData(){		// exporta os dados
		GenerateCsv csvGen; //= new GenerateCsv(outputFile+"TotalPorPeer", this);
		//csvGen.outputPeers();
		//csvGen = new GenerateCsv(outputFile+"sharingLevel", this);
		//csvGen.outputSharingLevel();
		//csvGen = new GenerateCsv(outputFile+"StepPeers", this);
		//csvGen.outputPeerDetalhado();
		csvGen = new GenerateCsv(outputFile+"StepFederacao", this);
		csvGen.outputSteps();;
//		WriteExcel writeExcel = new WriteExcel(outputFile, this);
//		writeExcel.outputPeers();
	}
	
    //getters
    public int getNumSteps(){
    	return numSteps;
    }
    
    public int getCurrentStep(){
    	return currentStep;
    }
    
    public List <Integer> getConsumersList(){
    	return consumersList;
    }
    
    public List <Integer> getProvidersList(){
    	return providersList;
    }
    
    public List <Integer> getIdlePeersList(){
    	return idlePeersList;
    }
    
	public double getTMin() {
		return tMin;
	}
	
	public double getTMax() {
		return tMax;
	}
	
	public double getDeltaC() {
		return deltaC;
	}
	
	public boolean isFdNof(){
		return this.fdNof;
	}
	
	public boolean isTransitivity() {
		return transitivity;
	}

	public double getKappa() {
		return kappa;
	}
	
	public int getSeed(){
		return seed;
	}

	public PeerComunity getPeerComunity() {
		return peerComunity;
	}	
}