package simulator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import peer.Peer;
import peer.PeerState;
import utils.input.DataReader;
import utils.input.Job;
import utils.input.PeerWorkload;
import utils.input.Task;
import utils.input.User;

public class PeerComunity {
	
	int numStep;
	int capacidade;
	public static Map<Integer,Peer> peers; // lista de peers
	private List<PeerWorkload> workload; // lista de Peerworkloads
	private int grainTime; // grão corrente
	private Map<PeerWorkload, HashMap<Integer, Integer>> requestedPerPeer; // mapa onde chave é um peerworkload e obj é um mapa com seus estados a cada grao tempo
	String inputFile;
	
	public PeerComunity(int grainTime, String inputFile, int capacity){
		
		this.capacidade = capacity;
		this.grainTime = grainTime; // atribuindo o valor do grão
		DataReader dr = new DataReader(); // instanciando a classe que lê e escreve arquivo
		workload = new ArrayList<PeerWorkload>(); // criando a lista de peerWorkload
		this.inputFile = inputFile;
		
		dr.readWorkload(workload, this.inputFile); // metodo que lê arquivo e cria os peerWorkloads 
		PeerComunity.peers = new HashMap<Integer,Peer>(); // cria a lista de Peers
		requestedPerPeer = new HashMap<PeerWorkload, HashMap<Integer,Integer>>(); // criando o mapa
		fulfillRequested();	//construindo mapa de qtdade de requisicoes por grao
		this.numStep = getNumberStep();
		createPeers(capacity); // criando os peers a partir dos peerworkloads
		completaMapas();
	}
	
	public int getNumberStep(){
		int sizeMaiorMapa = 0;
		for(PeerWorkload key: requestedPerPeer.keySet()){
			if(requestedPerPeer.get(key).size() > sizeMaiorMapa){
				sizeMaiorMapa = requestedPerPeer.get(key).size();
			}
		}
		/*int numberStep = 0, submitTime = 0, runTime = 0;
		String line_array[];
		try {
			BufferedReader bufReader = new BufferedReader(new FileReader(this.inputFile));
			String line = bufReader.readLine();
			
			while((line = bufReader.readLine())!=null){
				line_array = line.split(" ");
				submitTime = Integer.parseInt(line_array[0]);
				runTime = Integer.parseInt(line_array[1]);
				if((submitTime+runTime) > numberStep){
					numberStep = (submitTime+runTime);
				}				
			}
			bufReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return numberStep;*/
		
		return sizeMaiorMapa;
	}
	
	public int getCapacidade(){
		return this.capacidade;
	}
	public void createPeers(int capacity){ // metodo que cria os peers
		
		/*int numSteps = 0;
		Iterator<Entry<PeerWorkload, HashMap<Integer, Integer>>> it = requestedPerPeer.entrySet().iterator();
		while(it.hasNext()){
			Entry<PeerWorkload, HashMap<Integer, Integer>> e = it.next();
			int size = e.getValue().size();
			if(size > numSteps){
				numSteps = size;
			}
		}*/
		for(PeerWorkload p : workload){
			int peerId = Integer.parseInt(p.getPeerId().substring(1, p.getPeerId().length()));
			peers.put(peerId, new Peer(peerId,capacity,this.numStep));
		}
		
		//Collections.sort(peers);
		
	}
	
	public void setupPeers(int currentStep){ //se demanda < capacidade entao d = 0 senao d = d-c usar mapa requestedPerPeer, receber como param o graoCorrente						
		
		for(Peer p : peers.values()){
			PeerWorkload peer = new PeerWorkload("p" + p.getId());
			int demand = requestedPerPeer.get(peer).get(currentStep);
			if(demand < p.getINITIAL_CAPACITY()){
				p.setResourcesDonatedInCurrentStep(demand);
				p.setDemand(0);
				p.setState(PeerState.PROVIDER);
				p.getCapacitySuppliedHistory()[currentStep] = p.getINITIAL_CAPACITY() - demand;
			}
			else if(demand == p.getINITIAL_CAPACITY()){
				p.setResourcesDonatedInCurrentStep(demand);
				p.setDemand(0);
				p.setState(PeerState.IDLE);
			}
			else{
				p.setResourcesDonatedInCurrentStep(p.getINITIAL_CAPACITY());
				p.setDemand(demand - p.getINITIAL_CAPACITY());
				p.setState(PeerState.CONSUMER);
				p.getRequestedHistory()[currentStep] = demand - p.getINITIAL_CAPACITY();
			}	
		}
	}
	
	public void completaMapas(){
		int sizeMaiorMapa = 0;
		for(PeerWorkload key: requestedPerPeer.keySet()){
			if(requestedPerPeer.get(key).size() > sizeMaiorMapa){
				sizeMaiorMapa = requestedPerPeer.get(key).size();
			}
		}
		
		for(PeerWorkload key: requestedPerPeer.keySet()){
			if(requestedPerPeer.get(key).size() < this.numStep/**sizeMaiorMapa**/){
				int ultimoGrao = requestedPerPeer.get(key).size();
				int qtdeGraosQueFaltam = (/**sizeMaiorMapa**/ this.numStep - requestedPerPeer.get(key).size()); 
				for(int i=0; i < qtdeGraosQueFaltam ;i++){
					requestedPerPeer.get(key).put(ultimoGrao, 0);
					ultimoGrao++;
				}
			}
		}
		
		
	}
	
	public void fulfillRequested(){ // metodo que cria o mapa de estados de cada peerworkload
		for(PeerWorkload peer : workload){
			requestedPerPeer.put(peer, new HashMap<Integer, Integer>());	//adding the peer in the hashMap
			
			List<Job> jobsOfApeer = new ArrayList<Job>();
			for(User user : peer.getUsers())
				jobsOfApeer.addAll(user.getJobs());
			
			Collections.sort(jobsOfApeer);							//sorting the jobs by the submit time	
			int lastTaskEndTime = 0;
			for(Job job : jobsOfApeer){
				Integer initialKey = job.getSubmitTime()/grainTime;
				
				for(Task task : job.getTasks()){
					int endTime = job.getSubmitTime()+task.getRuntime();
					lastTaskEndTime = (endTime>lastTaskEndTime)? endTime : lastTaskEndTime;
					Integer finalKey = endTime/grainTime;
					
					for(int i = initialKey; i<=finalKey; i++){
						Integer currentValue = requestedPerPeer.get(peer).get(i);
						if(currentValue==null)
							requestedPerPeer.get(peer).put(i, 1);
						else
							requestedPerPeer.get(peer).put(i, currentValue+1);
					}				
				}			
			}
			
			//fulfilling the rest of map that doesn't have any request
			for(int i = 0; i <= lastTaskEndTime/grainTime; i++){
				Integer currentValue = requestedPerPeer.get(peer).get(i);
				if(currentValue==null)
					requestedPerPeer.get(peer).put(i, 0);
			}	
		}		
	}
	
	public List<PeerWorkload> getWorkload() {
		return workload;
	}

	public int getNumCollaborators() {
		return peers.size();
	}
	
	public Map<PeerWorkload, HashMap<Integer, Integer>> getRequestedPerPeer() {
		return requestedPerPeer;
	}
	
	public Map<Integer, Peer> getPeers(){
		return this.peers;
	}
}