package simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.input.DataReader;
import utils.input.Job;
import utils.input.PeerWorkload;
import utils.input.Task;
import utils.input.User;

public class Define_Capacidade {
	
	private static List<PeerWorkload> workload;
	private static String file;
	private static Map<PeerWorkload, HashMap<Integer, Integer>> requestedPerPeer;
	private static int grainTime;

	public static void main(String[] args) {

		DataReader dr = new DataReader();
		workload = new ArrayList<PeerWorkload>();
		file = "files\\workload_clust_10spt_10ups_gwa-tudo.txt";
		dr.readWorkload(workload, file);
		grainTime = 942;
		requestedPerPeer = new HashMap<PeerWorkload, HashMap<Integer,Integer>>();
		fulfillRequested();
		completaMapas();
		
		System.out.println(getCapacidade());
		

	}
	
	public static int getCapacidade(){
		int soma_medias = 0;
		for(Map<Integer,Integer> map: requestedPerPeer.values()){
			double media_peer = 0;
			for(Integer in : map.values()){
				media_peer += in;
			}
			System.out.println(media_peer);
			soma_medias+=(media_peer/map.size());
		}
		int capacidade = soma_medias/requestedPerPeer.size();
		return capacidade;
	}
	
	public static void completaMapas(){
		int sizeMaiorMapa = 0;
		for(PeerWorkload key: requestedPerPeer.keySet()){
			if(requestedPerPeer.get(key).size() > sizeMaiorMapa){
				sizeMaiorMapa = requestedPerPeer.get(key).size();
			}
		}
		
		for(PeerWorkload key: requestedPerPeer.keySet()){
			if(requestedPerPeer.get(key).size() < sizeMaiorMapa){
				int ultimoGrao = requestedPerPeer.get(key).size();
				int qtdeGraosQueFaltam = (sizeMaiorMapa - requestedPerPeer.get(key).size()); 
				for(int i=0; i < qtdeGraosQueFaltam ;i++){
					requestedPerPeer.get(key).put(ultimoGrao, 0);
					ultimoGrao++;
				}
			}
		}	
	}
	
	public static void fulfillRequested(){ // metodo que cria o mapa de estados de cada peerworkload
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

}
