import java.io.IOException;
import java.util.logging.Level;
import simulator.PeerComunity;
import simulator.Simulator;
import utils.input.DataReader;


public class ScenarioAntonio {
	public static void main(String[] args) throws IOException {
		int grain = 942;
		int peerCapacity = 100;
		int qtdePeersTransitivos = 5;
		//FIXME numSteps será baseado no requestedPerPeer de peerComunity
		//int numCiclos = 100;
		boolean fdNof = true; //se false é sd
		boolean transitivity = true;
		double tMin = 0.75, tMax = 0.95, deltaC = 0.05;
		double kappa = 0.5;
		Level level = Level.SEVERE;
		String outputFile_p = "C:\\Users\\antonio\\workspace\\nof-transitiva\\results\\";
		String file = "files\\workload_clust_10spt_10ups_gwa-tudo.txt";
		String outputFile;
		int fraction = 60;
		int seed_inicial = 18;
		int seed_final = 18;
		for(int seed = seed_inicial; seed < (seed_final + 1); seed++){
			outputFile = outputFile_p + "seed-" + seed + "-";
			PeerComunity pc = new PeerComunity(grain, file, peerCapacity);
			Simulator sim = new Simulator(fdNof, transitivity, tMin, tMax, deltaC, seed, level, pc, outputFile, kappa, qtdePeersTransitivos, fraction);
			sim.startSimulation();	
		}
		//int seed = 1;
		
		
		
		//DataReader data = new DataReader();
		//data.writeWorkload("files\\cenario.txt",grain ,numCiclos, true);
		
		//String file;
		//file = "files\\cenario.txt";
		
		
		
		// testes novos
		
		//file = "files\\workload_clust_20spt_10ups_gwa_1sem.txt";
		
		
		
	}

}
