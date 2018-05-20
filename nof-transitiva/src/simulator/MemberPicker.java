package simulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import peer.Peer;
import peer.Quadruplet;
import peer.Quintuplet;
import peer.Triplet;
import peer.balance.PeerInfo;

public class MemberPicker {
	
	private Random randomGenerator;
	private int fraction;
	
	public MemberPicker(int seed, int fraction){
		randomGenerator = new Random(seed); // construtor instanciando um random
		this.fraction = fraction;
	}	
	
	
    private int anyPeer(List <Integer> peerList){ 
        return this.randomGenerator.nextInt(peerList.size()); // sorteia um id de uma lista de ids de peers
    }
	
	
	public Peer choosesRandomPeer(List <Integer> peerList){
		int id = peerList.get(anyPeer(peerList)); 
		return PeerComunity.peers.get(id);   // retorna o peer com o id sorteado da lista de peers
	}
	
	// Apenas troca para o Quintuplet
	public List<Quintuplet> getConsumersWithPositiveBalance(Peer provider, List<Integer> consumers){
		
		List<Quintuplet> peersWithPositiveBalance = new ArrayList<Quintuplet>();	// cria uma lista de triplet vazia	
		
		Collections.sort(provider.getBalances()); //ordena a lista de creditos do peer provedor
		
		for(int i = provider.getBalances().size()-1; i >= 0 ; i--){ //começa a varrer do fim da lista, o maior credito
			
			PeerInfo balance = provider.getBalances().get(i); // pega a informação do peer da vez
			
			Peer peer = PeerComunity.peers.get(balance.getId()); // pega o peer que contém a informacao acima 
			
			if(balance.getBalance()>0 && consumers.contains(peer.getId()) && peer.getDemand()>0) // se atender as condicoes
				
				peersWithPositiveBalance.add(new Quintuplet(PeerComunity.peers.get(balance.getId()), balance.getBalance(), null,null,null)); 
		}
		
		return peersWithPositiveBalance;		// retorna uma lista com todas as possiveis <Triplet sem transitividade?????> que tenham credito positivo
	}
	
	public List<Quintuplet> getConsumersWithTransitiveBalance(Peer provider, int numTransitivePeers){
		List<Quintuplet> peersTransitiveBalance = getConsumersWith3PeersTransitiveBalance(provider);
		if(numTransitivePeers > 3){
			// TODO verificar a ordem em termos de qtde de peers transitivos e de débito
			peersTransitiveBalance.addAll(getConsumersWith4PeersTransitiveBalance(provider));			
		}if(numTransitivePeers > 4){
			peersTransitiveBalance.addAll(getConsumersWith5PeersTransitiveBalance(provider));
		}
		
		return peersTransitiveBalance;
	}
	
	private List<Quintuplet> getConsumersWith5PeersTransitiveBalance(Peer provider) {
		List<Quintuplet> peersWithTransitiveBalance = new ArrayList<Quintuplet>(); // cria uma lista de triplet vazia 
		
		List <Peer> peersWithPositiveBalance = new ArrayList<Peer>(); // cria uma lista de peer vazia	
		
		Collections.sort(provider.getBalances()); //ordena a lista de creditos do peer provedor
		
		for(int i = provider.getBalances().size()-1; i >= 0 ; i--){ //começa a varrer do fim da lista, o maior credito
			
			PeerInfo balance = provider.getBalances().get(i); // pega a informação do peer da vez
			
			Peer transitivePeer = PeerComunity.peers.get(balance.getId()); // pega o possivel peer transitivo que contém a informacao acima 
			
			if(balance.getBalance()>0) // se credito for maior que zero
				
				peersWithPositiveBalance.add(transitivePeer); // coloca na lista de peers com credito positivo
		}
		
		for(int p=0;p< peersWithPositiveBalance.size();p++){ // varre a listas de possiveis peers transitivos criada acima
			
			Collections.sort(peersWithPositiveBalance.get(p).getBalances()); 
			
			int x;
			if((peersWithPositiveBalance.get(p).getBalances().size()-1) < this.fraction){
				x = (peersWithPositiveBalance.get(p).getBalances().size()-1);
			}else{
				x = this.fraction;
			}
			
			for(int i = /**peersWithPositiveBalance.get(p).getBalances().size()-1**/ x; i >= 0 ; i--){ // começa a varrer do fim da lista de creditos do peer da vez, o maior credito
				
				PeerInfo idleTransitive2 = peersWithPositiveBalance.get(p).getBalances().get(i); // pega a informação do peer transitivo da vez
				
				Peer transitive2 = PeerComunity.peers.get(idleTransitive2.getId()); // pega o peer da informação acima
				
				int y;
				if((transitive2.getBalances().size()-1) < this.fraction){
					y = (transitive2.getBalances().size()-1);
				}else{
					y = this.fraction;
				}
				
				for(int k = /**transitive2.getBalances().size()-1**/ y;k>=0;k--){
					
					PeerInfo idleTransitive3 = transitive2.getBalances().get(k); // pega a informação do peer transitivo da vez
					
					Peer transitive3 = PeerComunity.peers.get(idleTransitive3.getId()); // pega o peer da informação acima
					
					int u;
					if((transitive3.getBalances().size()-1) < this.fraction){
						u = (transitive3.getBalances().size()-1);
					}else{
						u = this.fraction;
					}
					
					for(int z = /**transitive3.getBalances().size()-1**/ u;z>=0;z--){
						
						PeerInfo idleConsumer = transitive3.getBalances().get(z); // pega a informação do peer transitivo da vez
						
						Peer consumer = PeerComunity.peers.get(idleConsumer.getId()); // pega o peer da informação acima
						
						if(idleConsumer.getBalance()>0 /**&& consumer.getState() == State.CONSUMING**/ && consumer.getDemand()>0){ //se atender
							
							PeerInfo providerIdle = provider.getBalances().get(provider.getBalances().indexOf(new PeerInfo(peersWithPositiveBalance.get(p).getId()))); //??????????
							
							double debtProviderWithIdle = providerIdle.getBalance(); // debito 1
							
							double debtTransitive3 = idleTransitive3.getBalance(); // debito 2
							
							double debtTransitive2 = idleTransitive2.getBalance(); // debito 3
							
							double debtIdleWithConsumer = idleConsumer.getBalance(); // debito 4
							
							double debt = Math.min(Math.min(debtProviderWithIdle,debtTransitive3), Math.min(debtTransitive2, debtIdleWithConsumer)); // debito 3
							
							peersWithTransitiveBalance.add(new Quintuplet(consumer, debt, peersWithPositiveBalance.get(p),transitive2, transitive3));
						}
						
					}	
				
				}
			}			
		}
			
		return peersWithTransitiveBalance;
	}


	public List<Quintuplet> getConsumersWith4PeersTransitiveBalance(Peer provider){
		List<Quintuplet> peersWithTransitiveBalance = new ArrayList<Quintuplet>(); // cria uma lista de triplet vazia 
		
		List <Peer> peersWithPositiveBalance = new ArrayList<Peer>(); // cria uma lista de peer vazia	
		
		Collections.sort(provider.getBalances()); //ordena a lista de creditos do peer provedor
		
		for(int i = provider.getBalances().size()-1; i >= 0 ; i--){ //começa a varrer do fim da lista, o maior credito
			
			PeerInfo balance = provider.getBalances().get(i); // pega a informação do peer da vez
			
			Peer transitivePeer = PeerComunity.peers.get(balance.getId()); // pega o possivel peer transitivo que contém a informacao acima 
			
			if(balance.getBalance()>0) // se credito for maior que zero
				
				peersWithPositiveBalance.add(transitivePeer); // coloca na lista de peers com credito positivo
		}
		
		for(int p=0;p< peersWithPositiveBalance.size();p++){ // varre a listas de possiveis peers transitivos criada acima
			
			Collections.sort(peersWithPositiveBalance.get(p).getBalances()); 
			
			int x;
			if((peersWithPositiveBalance.get(p).getBalances().size()-1) < this.fraction){
				x = (peersWithPositiveBalance.get(p).getBalances().size()-1);
			}else{
				x = this.fraction;
			}
			
			for(int i = /**peersWithPositiveBalance.get(p).getBalances().size()-1**/ x; i >= 0 ; i--){ // começa a varrer do fim da lista de creditos do peer da vez, o maior credito
				
				PeerInfo idleTransitive2 = peersWithPositiveBalance.get(p).getBalances().get(i); // pega a informação do peer transitivo da vez
				
				Peer transitive2 = PeerComunity.peers.get(idleTransitive2.getId()); // pega o peer da informação acima
				
				int y;
				if((transitive2.getBalances().size()-1) < this.fraction){
					y = (transitive2.getBalances().size()-1);
				}else{
					y = this.fraction;
				}
				
				for(int k = /**transitive2.getBalances().size()-1**/ y;k>=0;k--){
					PeerInfo idleConsumer = transitive2.getBalances().get(k); // pega a informação do peer transitivo da vez
					
					Peer consumer = PeerComunity.peers.get(idleConsumer.getId()); // pega o peer da informação acima
					
					if(idleConsumer.getBalance()>0 /**&& consumer.getState() == State.CONSUMING**/ && consumer.getDemand()>0){ //se atender
						
						PeerInfo providerIdle = provider.getBalances().get(provider.getBalances().indexOf(new PeerInfo(peersWithPositiveBalance.get(p).getId()))); //??????????
						
						double debtProviderWithIdle = providerIdle.getBalance(); // debito 1
						
						double debtTransitive2 = idleTransitive2.getBalance(); // debito 1
						
						double debtIdleWithConsumer = idleConsumer.getBalance(); // debito 2
						
						double debt = Math.min(debtProviderWithIdle, Math.min(debtTransitive2, debtIdleWithConsumer)); // debito 3
						
						peersWithTransitiveBalance.add(new Quintuplet(consumer, debt, peersWithPositiveBalance.get(p),transitive2, null));
					}
				
				}
			}			
		}
			
		return peersWithTransitiveBalance;
		
	}
	
	
	// TODO Adaptar Triplet para Quadruplet
	public List<Quintuplet> getConsumersWith3PeersTransitiveBalance(Peer provider){	
		
		List<Quintuplet> peersWithTransitiveBalance = new ArrayList<Quintuplet>(); // cria uma lista de triplet vazia 
		
		List <Peer> peersWithPositiveBalance = new ArrayList<Peer>(); // cria uma lista de peer vazia	
		
		Collections.sort(provider.getBalances()); //ordena a lista de creditos do peer provedor
		
		for(int i = provider.getBalances().size()-1; i >= 0 ; i--){ //começa a varrer do fim da lista, o maior credito
			
			PeerInfo balance = provider.getBalances().get(i); // pega a informação do peer da vez
			
			Peer transitivePeer = PeerComunity.peers.get(balance.getId()); // pega o possivel peer transitivo que contém a informacao acima 
			
			if(balance.getBalance()>0) // se credito for maior que zero
				
				peersWithPositiveBalance.add(transitivePeer); // coloca na lista de peers com credito positivo
		}
		
		
		
		for(Peer idlePeer : peersWithPositiveBalance){ // varre a listas de possiveis peers transitivos criada acima
			
			Collections.sort(idlePeer.getBalances()); // ordena a lista de creditos do peerTransitivo da vez
			
			int x;
			if((idlePeer.getBalances().size()-1) < this.fraction){
				x = (idlePeer.getBalances().size()-1);
			}else{
				x = this.fraction;
			}
			for(int i = /**idlePeer.getBalances().size()-1**/ x; i >= 0 ; i--){ // começa a varrer do fim da lista de creditos do peer da vez, o maior credito
				
				PeerInfo idleConsumer = idlePeer.getBalances().get(i); // pega a informação do peer transitivo da vez
				
				Peer consumer = PeerComunity.peers.get(idleConsumer.getId()); // pega o peer da informação acima
				
				if(idleConsumer.getBalance()>0 /**&& consumer.getState() == State.CONSUMING**/ && consumer.getDemand()>0){ //se atender
					
					PeerInfo providerIdle = provider.getBalances().get(provider.getBalances().indexOf(new PeerInfo(idlePeer.getId()))); //??????????
					
					double debtProviderWithIdle = providerIdle.getBalance(); // debito 1
					
					double debtIdleWithConsumer = idleConsumer.getBalance(); // debito 2
					
					double debt = Math.min(debtProviderWithIdle, debtIdleWithConsumer); // debito 3
					
					peersWithTransitiveBalance.add(new Quintuplet(consumer, debt, idlePeer,null,null)); // add na lista de transitividades possiveis
				}
			}			
		}
			
		return peersWithTransitiveBalance;		
	}
	
	public List<Quintuplet> getConsumersWithZeroBalance(Peer provider){ 
		
		List<Quintuplet> peersWithZeroBalance = new ArrayList<Quintuplet>();	// cria uma lista de triplet vazia com creditos zero	
		
		for(Peer peer : PeerComunity.peers.values()){ // a lista de todos os peers
			
			if(peer.getId()==provider.getId()) // ???????? faz sentido?
				continue; // não executa o que tem abaixo e vai para próxima rodada do for
			
			int balanceIndex = provider.getBalances().indexOf(new PeerInfo(peer.getId())); // ?????????
			
			if((balanceIndex==-1 || (balanceIndex!=-1 && provider.getBalances().get(balanceIndex).getBalance()==0 /**&& peer.getState()==State.CONSUMING **/&& peer.getDemand()>0)) /**&&
					(peer.getState()==State.CONSUMING **/&& peer.getDemand()>0) //????????????
				peersWithZeroBalance.add(new Quintuplet(peer, 0, null, null,null));	// se atender add na lista o triplet sem transitivo		
		}
		
		return peersWithZeroBalance;	
	}
	
	public List<Quintuplet> getNextConsumersWithSameBalance(List<Quintuplet> consumers){
		
		List<Quintuplet> peersWithSameBalance = new ArrayList<Quintuplet>(); // cria uma lista de triplet vazia 
		
		Collections.sort(consumers); // ordena 
		
		Quintuplet consumerWithHigherBalance = consumers.get(consumers.size()-1); // ?????????
		
		peersWithSameBalance.add(consumerWithHigherBalance); // add na lista
		
		if(consumers.size() > 1){	
			
			for(int i = consumers.size()-2; i>=0; i--){
				
				if(consumerWithHigherBalance.getDebt() == consumers.get(i).getDebt())
					
					peersWithSameBalance.add(consumers.get(i));
				
				else
					break;
			}
		}
		
		return peersWithSameBalance;
	}
	

}
