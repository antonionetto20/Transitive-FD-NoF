package utils.input;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;		

public class DataReader {
	
	private BufferedReader bufReader; 
	
	public void writeWorkload(String path, int grain ,int numberOfCycles, boolean ride) throws IOException {
        BufferedWriter buffWrite = new BufferedWriter(new FileWriter(path));
        String peers[] = new String[]{"U0 P0","U0 P0","U0 P0","U1 P1","U1 P1","U2 P2","U2 P2","U3 P3","U3 P3",
        							  "U1 P1","U1 P1","U1 P1","U2 P2","U2 P2","U3 P3","U3 P3","U4 P4","U4 P4",     							  
        							  "U2 P2","U2 P2","U2 P2","U3 P3","U3 P3","U4 P4","U4 P4","U0 P0","U0 P0",       							  
        							  "U3 P3","U3 P3","U3 P3","U4 P4","U4 P4","U0 P0","U0 P0","U1 P1","U1 P1",        							  
        							  "U4 P4","U4 P4","U4 P4","U0 P0","U0 P0","U1 P1","U1 P1","U2 P2","U2 P2"};
        
        String peers2[] = new String[]{"U0 P0","U0 P0","U0 P0","U1 P1","U1 P1","U2 P2","U2 P2","U3 P3","U3 P3","U5 P5","U5 P5","U5 P5",
				  					   "U1 P1","U1 P1","U1 P1","U2 P2","U2 P2","U3 P3","U3 P3","U4 P4","U4 P4","U5 P5","U5 P5","U5 P5",     							  
				  					   "U2 P2","U2 P2","U2 P2","U3 P3","U3 P3","U4 P4","U4 P4","U0 P0","U0 P0","U5 P5","U5 P5","U5 P5",       							  
				  					   "U3 P3","U3 P3","U3 P3","U4 P4","U4 P4","U0 P0","U0 P0","U1 P1","U1 P1","U5 P5","U5 P5","U5 P5",        							  
				  					   "U4 P4","U4 P4","U4 P4","U0 P0","U0 P0","U1 P1","U1 P1","U2 P2","U2 P2","U5 P5","U5 P5","U5 P5"};
        int submitTime = 10, time = 9;
        if(ride){
        	peers = peers2;
        	time = 12;
        }
        
        buffWrite.append("SubmitTime RunTime JobID UserID PeerID TraceID Cluster.IAT Cluster.JRT Cluster.TRT\n");
        int jobId = 0;
        for(int x=0;x<numberOfCycles;x++){
        	int index = 0;
        	for(int y=0;y<5;y++){
        		for(int z=0;z<time;z++){
        			buffWrite.append(submitTime + " 15 " + jobId + " " + peers[index] + " gwa-t2 C1 C3 C3" + "\n");
            		index++;
        		}
        		jobId ++;
        		submitTime+=grain;
        	}
        }
        buffWrite.close();
    }
	
	public void readWorkload(List<PeerWorkload> peers, String file){		
		try {
			bufReader = new BufferedReader(new FileReader(file));
			
			//skip first line
			String line = bufReader.readLine();
				
			//read the rest of lines
			while((line = bufReader.readLine())!=null){
				PeerWorkload peer = readPeer(line);
				addTaskOnPeersList(peers, peer);				
			}
			
			bufReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private PeerWorkload readPeer(String line){
//		SubmitTime RunTime JobID UserID PeerID TraceID Cluster.IAT Cluster.JRT Cluster.TRT
//		300 1332 1 U251 P26 gwa-t11 C2 C2 C4
		String[] values = line.split(" ");
		String submitTime = values[0];
		String runtime = values[1];
		String jobId = values[2];
		String userId = values[3];
		String peerId = values[4].toLowerCase();
		
		Task task = new Task(Integer.parseInt(runtime));
		Job job = new Job(peerId, userId, Integer.parseInt(jobId), Integer.parseInt(submitTime));		
		job.getTasks().add(task);
		User user = new User(userId);
		user.getJobs().add(job);
		PeerWorkload peer = new PeerWorkload(peerId);
		peer.getUsers().add(user);
		
		return peer;		
	}
	
	private void addTaskOnPeersList(List<PeerWorkload> peers, PeerWorkload newPeer){
		
		if(peers.contains(newPeer)){
			PeerWorkload peerOfList = peers.get(peers.indexOf(newPeer)); 
			User newUser = newPeer.getUsers().get(0);
			
			if(peerOfList.getUsers().contains(newUser)){				
				int userIndex = peerOfList.getUsers().indexOf(newUser);
				User userOfList = peerOfList.getUsers().get(userIndex);
				Job newJob = newUser.getJobs().get(0);
				
				if(userOfList.getJobs().contains(newJob)){
					int jobIndex = userOfList.getJobs().indexOf(newJob);
					Job jobOfList = userOfList.getJobs().get(jobIndex);
					jobOfList.getTasks().add(new Task(newJob.getTasks().get(0).getRuntime()));
				}
				else{	//the job doesn't exist on the user yet
					userOfList.getJobs().add(newJob);
				}				
			}
			else{		//the user doesn't exist on the peer yet
				peerOfList.getUsers().add(newUser);
			}
		}
		else{			//the peer doesn't exist on the list yet
			peers.add(newPeer);
		}
	}
	
	

}