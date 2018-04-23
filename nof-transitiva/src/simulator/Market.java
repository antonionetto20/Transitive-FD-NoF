package simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nof.Interaction;
import nof.NetworkOfFavors;
import peer.Peer;
import peer.Quadruplet;
import peer.Quintuplet;
import peer.Triplet;
import peer.balance.PeerInfo;

public class Market {
	
	private Simulator simulator;
	
	public Market(Simulator simulator){
		this.simulator = simulator;
	}
	
	public void performDonation(Peer provider, List<Quintuplet> consumingPeers){
		if(consumingPeers.size()==1){
			Quintuplet peer = consumingPeers.get(0);
			performDonation(provider, peer, -1);		//involves donation to peer with balance>0 or transitiveBalance>0			
		}else
			performDonationToPeersWithSameBalance(provider, consumingPeers); //donate to more than one peer	
	}
	
	//the provider should donate the amount specified in resources, but only when resource >0	
	public double performDonation(Peer provider, Quintuplet consumerQuintuplet, double resources){
		//########################################################################################################????
		resources = getAmountToDonate(provider, consumerQuintuplet, resources);
		
		//update the values of interactions and the balances of each peer
		updatePeersInteraction(provider, consumerQuintuplet, resources);
		
		Peer[] peersInvolvedInDonation = null;
		if(consumerQuintuplet.getTransitivePeer1()==null && consumerQuintuplet.getTransitivePeer2()==null && consumerQuintuplet.getTransitivePeer3()==null)
			peersInvolvedInDonation = new Peer[]{provider, consumerQuintuplet.getConsumer()}; 
		else if(consumerQuintuplet.getTransitivePeer1()!=null && consumerQuintuplet.getTransitivePeer2()==null && consumerQuintuplet.getTransitivePeer3()==null)
			peersInvolvedInDonation = new Peer[]{provider, consumerQuintuplet.getConsumer(),consumerQuintuplet.getTransitivePeer1()}; 
		else if(consumerQuintuplet.getTransitivePeer1()!=null && consumerQuintuplet.getTransitivePeer2()!=null && consumerQuintuplet.getTransitivePeer3()==null)
			peersInvolvedInDonation = new Peer[]{provider, consumerQuintuplet.getConsumer(),consumerQuintuplet.getTransitivePeer1(),consumerQuintuplet.getTransitivePeer2()}; 
		else
			peersInvolvedInDonation = new Peer[]{provider, consumerQuintuplet.getConsumer(),consumerQuintuplet.getTransitivePeer1(),consumerQuintuplet.getTransitivePeer2(), consumerQuintuplet.getTransitivePeer3()}; 
		
		sortBalances(peersInvolvedInDonation);						//sort the balances of all peers
		
		//update the donated and consumed amount in this step
		provider.getDonatedHistory()[simulator.getCurrentStep()] += resources;
//		if(consumerTriplet.getTransitivePeer() != null)
//			provider.getDonatedByTransitivityHistory()[simulator.getCurrentStep()] += resources;
		consumerQuintuplet.getConsumer().getConsumedHistory()[simulator.getCurrentStep()] += resources;
		
		return resources;
	}
	
	//remember that free rider are always zero credit 
	// Lembre-se de que o carona sempre é zero
	public void performDonationToPeersWithSameBalance(Peer provider, List<Quintuplet> consumingPeers) {	
		List<Quintuplet> consumingPeersLocal = new ArrayList<Quintuplet>();
		consumingPeersLocal.addAll(consumingPeers);	
		for(Quintuplet quintuplet : consumingPeers){
			if(quintuplet.getConsumer().getDemand()==0)
				consumingPeersLocal.remove(quintuplet);
		}
			
		//-0.0000000001
		while(consumingPeersLocal.size()>0 && provider.getResourcesDonatedInCurrentStep()<provider.getInitialCapacity()){	//TODO what should we do with this bug
			double smallestDemand = Double.MAX_VALUE;
			for(Quintuplet consumerQuintuplet : consumingPeersLocal)
				smallestDemand = smallestDemand < consumerQuintuplet.getConsumer().getDemand()? smallestDemand : consumerQuintuplet.getConsumer().getDemand();
			
			double resourcesForPeersWithSameBalance = provider.getInitialCapacity() - provider.getResourcesDonatedInCurrentStep();
			double howMuchShouldEachPeerReceive = resourcesForPeersWithSameBalance/consumingPeersLocal.size();			
			double howMuchWillEachPeerReceiveInThisRound = Math.min(smallestDemand, howMuchShouldEachPeerReceive);
			
			//The provider still has spare resources but the consumers already consumed what they needed
			if(howMuchWillEachPeerReceiveInThisRound==0)
				break;
			
			List<Quintuplet> consumingPeersAux = new ArrayList<Quintuplet>();
			consumingPeersAux.addAll(consumingPeersLocal);			
			for(Quintuplet consumerQuintuplet : consumingPeersAux){
				double donated = performDonation(provider, consumerQuintuplet, howMuchWillEachPeerReceiveInThisRound);
				removePeerIfFullyConsumed(consumerQuintuplet.getConsumer());
				if(consumerQuintuplet.getConsumer().getDemand()<=0 || donated<0.00000000000001)	//if donated==0 then that debt might have already been paid
					consumingPeersLocal.remove(consumerQuintuplet);
			}
		}		
	}
	
	private double getAmountToDonate(Peer provider, Quintuplet consumerQuintuplet, double resources) {
		Peer consumer = consumerQuintuplet.getConsumer();
		
		double consumerDemand = consumer.getDemand();
		double freeResources = Math.max(0,provider.getInitialCapacity() - provider.getResourcesDonatedInCurrentStep());
		double maxToBeDonated = Math.max(0,Math.min(freeResources, consumerDemand));		
		
		//maxToBeDonated is constrained by the resources specified, if it is not -1
		if(resources!=-1)
				maxToBeDonated = Math.min(maxToBeDonated, resources);		
		
		if(simulator.isTransitivity() && (consumerQuintuplet.getTransitivePeer1()!=null || consumerQuintuplet.getTransitivePeer2()!=null)){
			double transitiveCredit = 0;
			Peer idlePeer1 = null, idlePeer2 = null, idlePeer3 = null;
			
			if(consumerQuintuplet.getTransitivePeer1()!=null && consumerQuintuplet.getTransitivePeer2()!=null && consumerQuintuplet.getTransitivePeer3()!=null ){
				idlePeer1 = consumerQuintuplet.getTransitivePeer1();
				idlePeer2 = consumerQuintuplet.getTransitivePeer2();
				idlePeer3 = consumerQuintuplet.getTransitivePeer3();
				
				double gammaProviderIdle1 = provider.getBalances().get(provider.getBalances().indexOf(new PeerInfo(idlePeer1.getId()))).getBalance();
				double gammaIdle1Idle2 = idlePeer1.getBalances().get(idlePeer1.getBalances().indexOf(new PeerInfo(idlePeer2.getId()))).getBalance();	
				double gammaIdle1Idle3 = idlePeer2.getBalances().get(idlePeer2.getBalances().indexOf(new PeerInfo(idlePeer3.getId()))).getBalance();
				double gammaIdleConsumer = idlePeer3.getBalances().get(idlePeer3.getBalances().indexOf(new PeerInfo(consumer.getId()))).getBalance();
				
				transitiveCredit = Math.min(Math.min(gammaProviderIdle1, gammaIdle1Idle2), Math.min(gammaIdle1Idle3, gammaIdleConsumer));
				
			}else if(consumerQuintuplet.getTransitivePeer1()!=null && consumerQuintuplet.getTransitivePeer2()!=null && consumerQuintuplet.getTransitivePeer3()==null ){
				idlePeer1 = consumerQuintuplet.getTransitivePeer1();
				idlePeer2 = consumerQuintuplet.getTransitivePeer2();
				
				double gammaProviderIdle1 = provider.getBalances().get(provider.getBalances().indexOf(new PeerInfo(idlePeer1.getId()))).getBalance();
				double gammaIdle1Idle2 = idlePeer1.getBalances().get(idlePeer1.getBalances().indexOf(new PeerInfo(idlePeer2.getId()))).getBalance();	
				double gammaIdle2Consumer = idlePeer2.getBalances().get(idlePeer2.getBalances().indexOf(new PeerInfo(consumer.getId()))).getBalance();
				
				transitiveCredit = Math.min(gammaProviderIdle1, Math.min(gammaIdle1Idle2, gammaIdle2Consumer));
				
			}else if(consumerQuintuplet.getTransitivePeer1()!=null && consumerQuintuplet.getTransitivePeer2()==null && consumerQuintuplet.getTransitivePeer3()==null ){
				idlePeer1 = consumerQuintuplet.getTransitivePeer1();
				double gammaProviderIdle = provider.getBalances().get(provider.getBalances().indexOf(new PeerInfo(idlePeer1.getId()))).getBalance();
				double gammaIdleConsumer = idlePeer1.getBalances().get(idlePeer1.getBalances().indexOf(new PeerInfo(consumer.getId()))).getBalance();
				transitiveCredit = Math.min(gammaProviderIdle, gammaIdleConsumer);
			}else{
				System.out.println("Erro na compensação dos créditos transitivos..." + consumerQuintuplet.toString());
				System.exit(0);
			}
			if(!simulator.isFdNof())	//TODO adicionei depois de rodas as simulaÃ§Ãµes
				maxToBeDonated = Math.min(maxToBeDonated, transitiveCredit);	//limit the maxToBeDonated by the transitiveCredit			
			//here we limit the maxToBeDonated by the alfa the provider has towards the idle peer, and also by the alfa the idle peer has towards the consumer
			else{
				maxToBeDonated = Math.min(maxToBeDonated, getAlfa(provider, idlePeer1));
				if(idlePeer2 != null && idlePeer3 == null){
					maxToBeDonated = Math.min(maxToBeDonated, getAlfa(idlePeer1, idlePeer2));
					maxToBeDonated = Math.min(maxToBeDonated, getAlfa((Peer)idlePeer2, consumer));
				}else if(idlePeer2 != null && idlePeer3 != null){
					maxToBeDonated = Math.min(maxToBeDonated, getAlfa(idlePeer1, idlePeer2));
					maxToBeDonated = Math.min(maxToBeDonated, getAlfa(idlePeer2, idlePeer3));
					maxToBeDonated = Math.min(maxToBeDonated, getAlfa((Peer)idlePeer3, consumer));
				}else{
					maxToBeDonated = Math.min(maxToBeDonated, getAlfa((Peer)idlePeer1, consumer));
				}
				
			}	
		}
		//here we limit the maxToBeDonated by the alfa the provider has towards the consumer set by controller		
		else if(simulator.isFdNof())
			maxToBeDonated = Math.min(maxToBeDonated, getAlfa(provider, consumer));	
		
		return maxToBeDonated;
	}
	
	private double getAlfa(Peer provider, Peer consumer){
		double fairnessPairwise = -1, maxToBeDonated = Double.MAX_VALUE;			
		int index = provider.getInteractions().indexOf(new Interaction(consumer, 0, 1));
		Interaction interaction = null;
		if(index != -1){
			interaction = provider.getInteractions().get(index);				//retrieve the interaction object with its history
			fairnessPairwise = NetworkOfFavors.getFairness(interaction.getConsumed(), interaction.getDonated());		
		}
		
		if(interaction == null || fairnessPairwise<0) 
			maxToBeDonated = Math.min(maxToBeDonated, provider.getMaxCapacityToSupply());	//global
		else
			maxToBeDonated = Math.min(maxToBeDonated,interaction.getMaxCapacityToSupply());	//pairwise
		
		return maxToBeDonated;
		
	}
	
	private void updatePeersInteraction(Peer provider, Quintuplet consumerQuintuplet, double resources){	
		Peer consumer = consumerQuintuplet.getConsumer();
		Peer idlePeer1 = consumerQuintuplet.getTransitivePeer1(); // ??????????????????????????
		Peer idlePeer2 = consumerQuintuplet.getTransitivePeer2();
		Peer idlePeer3 = consumerQuintuplet.getTransitivePeer3();
		
		if(idlePeer1!=null && idlePeer2==null && idlePeer3 == null){						
			//make sure all interaction exist, and if not, create them
			createInteraction(provider, idlePeer1);
			createInteraction((Peer)idlePeer1, provider);
			createInteraction((Peer)idlePeer1, consumer);
			createInteraction((Peer)consumer, idlePeer1);
				
			int index = provider.getInteractions().indexOf(new Interaction(idlePeer1, 0, 1));
			Interaction interactionProviderIdle = provider.getInteractions().get(index);	//retrieve the interaction object with its history
			interactionProviderIdle.donate(resources);
			updateBalance(provider, idlePeer1, interactionProviderIdle);
			
			index = idlePeer1.getInteractions().indexOf(new Interaction(provider, 0, 1));
			Interaction interactionIdleProvider = idlePeer1.getInteractions().get(index);	
			interactionIdleProvider.consume(resources);
			updateBalance((Peer)idlePeer1, provider, interactionIdleProvider);
			
			index = idlePeer1.getInteractions().indexOf(new Interaction(consumer, 0, 1));
			Interaction interactionIdleConsumer = idlePeer1.getInteractions().get(index);
			interactionIdleConsumer.donate(resources);
			updateBalance((Peer)idlePeer1, consumer, interactionIdleConsumer);
			
			index = consumer.getInteractions().indexOf(new Interaction(idlePeer1, 0, 1));
			Interaction interactionConsumerIdle = consumer.getInteractions().get(index);	
			interactionConsumerIdle.consume(resources);
			updateBalance((Peer)consumer, idlePeer1, interactionConsumerIdle);	
		}else if(idlePeer1!=null && idlePeer2!=null && idlePeer3 == null){
			
			createInteraction(provider, idlePeer1);
			createInteraction((Peer)idlePeer1, provider);
			createInteraction(idlePeer1, idlePeer2);
			createInteraction(idlePeer2, idlePeer1);		
			createInteraction((Peer)idlePeer2, consumer);
			createInteraction((Peer)consumer, idlePeer2);
				
			int index = provider.getInteractions().indexOf(new Interaction(idlePeer1, 0, 1));
			Interaction interactionProviderIdle1 = provider.getInteractions().get(index);	//retrieve the interaction object with its history
			interactionProviderIdle1.donate(resources);
			updateBalance(provider, idlePeer1, interactionProviderIdle1);
			
			index = idlePeer1.getInteractions().indexOf(new Interaction(provider, 0, 1));
			Interaction interactionIdle1Provider = idlePeer1.getInteractions().get(index);	
			interactionIdle1Provider.consume(resources);
			updateBalance((Peer)idlePeer1, provider, interactionIdle1Provider);
			
			index = idlePeer1.getInteractions().indexOf(new Interaction(idlePeer2, 0, 1));
			Interaction interactionIdle1Idle2 = idlePeer1.getInteractions().get(index);	//retrieve the interaction object with its history
			interactionIdle1Idle2.donate(resources);
			updateBalance(idlePeer1, idlePeer2, interactionIdle1Idle2);
			
			index = idlePeer2.getInteractions().indexOf(new Interaction(idlePeer1, 0, 1));
			Interaction interactionIdle2idlePeer1 = idlePeer2.getInteractions().get(index);	
			interactionIdle2idlePeer1.consume(resources);
			updateBalance((Peer)idlePeer2, idlePeer1, interactionIdle2idlePeer1);
			
			index = idlePeer2.getInteractions().indexOf(new Interaction(consumer, 0, 1));
			Interaction interactionIdle2Consumer = idlePeer2.getInteractions().get(index);
			interactionIdle2Consumer.donate(resources);
			updateBalance((Peer)idlePeer2, consumer, interactionIdle2Consumer);
			
			index = consumer.getInteractions().indexOf(new Interaction(idlePeer2, 0, 1));
			Interaction interactionConsumerIdle2 = consumer.getInteractions().get(index);	
			interactionConsumerIdle2.consume(resources);
			updateBalance((Peer)consumer, idlePeer2, interactionConsumerIdle2);
		}else if(idlePeer1!=null && idlePeer2!=null && idlePeer3 != null){
		
			createInteraction(provider, idlePeer1);
			createInteraction((Peer)idlePeer1, provider);
			createInteraction(idlePeer1, idlePeer2);
			createInteraction(idlePeer2, idlePeer1);
			createInteraction((Peer)idlePeer2, idlePeer3);
			createInteraction((Peer)idlePeer3, idlePeer2);
			createInteraction((Peer)idlePeer3, consumer);
			createInteraction((Peer)consumer, idlePeer3);
			
		int index = provider.getInteractions().indexOf(new Interaction(idlePeer1, 0, 1));
		Interaction interactionProviderIdle1 = provider.getInteractions().get(index);	//retrieve the interaction object with its history
		interactionProviderIdle1.donate(resources);
		updateBalance(provider, idlePeer1, interactionProviderIdle1);
		
		index = idlePeer1.getInteractions().indexOf(new Interaction(provider, 0, 1));
		Interaction interactionIdle1Provider = idlePeer1.getInteractions().get(index);	
		interactionIdle1Provider.consume(resources);
		updateBalance((Peer)idlePeer1, provider, interactionIdle1Provider);
		
		index = idlePeer1.getInteractions().indexOf(new Interaction(idlePeer2, 0, 1));
		Interaction interactionIdle1Idle2 = idlePeer1.getInteractions().get(index);	//retrieve the interaction object with its history
		interactionIdle1Idle2.donate(resources);
		updateBalance(idlePeer1, idlePeer2, interactionIdle1Idle2);
		
		index = idlePeer2.getInteractions().indexOf(new Interaction(idlePeer1, 0, 1));
		Interaction interactionIdle2IdlePeer1 = idlePeer2.getInteractions().get(index);	
		interactionIdle2IdlePeer1.consume(resources);
		updateBalance((Peer)idlePeer2, idlePeer1, interactionIdle2IdlePeer1);
		
		index = idlePeer2.getInteractions().indexOf(new Interaction(idlePeer3, 0, 1));
		Interaction interactionIdle2Idle3 = idlePeer2.getInteractions().get(index);
		interactionIdle2Idle3.donate(resources);
		updateBalance((Peer)idlePeer2, idlePeer3, interactionIdle2Idle3);
		
		index = idlePeer3.getInteractions().indexOf(new Interaction(idlePeer2, 0, 1));
		Interaction interactionIdle3Idle2 = idlePeer3.getInteractions().get(index);	
		interactionIdle3Idle2.consume(resources);
		updateBalance((Peer)idlePeer3, idlePeer2, interactionIdle3Idle2);
		
		index = idlePeer3.getInteractions().indexOf(new Interaction(consumer, 0, 1));
		Interaction interactionIdle3Consumer = idlePeer3.getInteractions().get(index);
		interactionIdle3Consumer.donate(resources);
		updateBalance((Peer)idlePeer3, consumer, interactionIdle3Consumer);
		
		index = consumer.getInteractions().indexOf(new Interaction(idlePeer3, 0, 1));
		Interaction interactionConsumerIdle3 = consumer.getInteractions().get(index);	
		interactionConsumerIdle3.consume(resources);
		updateBalance((Peer)consumer, idlePeer3, interactionConsumerIdle3);
	}else{
			//make sure all interaction exists
			createInteraction(provider, consumer);
			
			int index = provider.getInteractions().indexOf(new Interaction(consumer, 0, 1));
			Interaction interactionProviderConsumer = provider.getInteractions().get(index);	
			interactionProviderConsumer.donate(resources);
			updateBalance(provider, consumer, interactionProviderConsumer);
			
			index = consumer.getInteractions().indexOf(new Interaction(provider, 0, 1));
			Interaction interactionConsumerProvider = consumer.getInteractions().get(index);	
			interactionConsumerProvider.consume(resources);
			updateBalance(provider, consumer, interactionConsumerProvider);
		}
		provider.setResourcesDonatedInCurrentStep(provider.getResourcesDonatedInCurrentStep()+resources);
		consumer.setDemand(Math.max(0,consumer.getDemand()-resources));
	}	
	
	private void createInteraction(Peer provider, Peer consumer){
		if(!provider.getInteractions().contains(new Interaction(consumer, 0, 1))){	//just to retrieve the real interaction by comparison			
			//creates an interaction for the provider and for the consumer
			Interaction providersInteraction = new Interaction(consumer, provider.getInitialCapacity(), simulator.getNumSteps());
			provider.getInteractions().add(providersInteraction);
			provider.getBalances().add(new PeerInfo(consumer.getId()));
			Interaction consumersInteraction = new Interaction(provider, consumer.getInitialCapacity(), simulator.getNumSteps());
			consumer.getInteractions().add(consumersInteraction);
			consumer.getBalances().add(new PeerInfo(provider.getId()));
		}
	}	
	
	private void updateBalance(Peer provider, Peer consumer, Interaction interaction){	//we have to call it twice: each for the interaction of each peer	
		
		double balance = NetworkOfFavors.calculateBalance(interaction.getConsumed(), interaction.getDonated());
		if(interaction.getPeerB() == consumer){
			int consumerIndex = provider.getBalances().indexOf(new PeerInfo(consumer.getId()));
			provider.getBalances().get(consumerIndex).setBalance(balance);
		}
		else{
			int providerIndex = consumer.getBalances().indexOf(new PeerInfo(provider.getId()));
			consumer.getBalances().get(providerIndex).setBalance(balance);
		}
	}
	
	public void removePeerIfFullyConsumed(Peer consumer){
		if(consumer.getDemand()<0.0000000000000000001){
//		if(consumer.getDemand()==0){
			simulator.getConsumersList().remove((Integer)consumer.getId());
		}
		else if(consumer.getDemand()<0){
			Simulator.logger.finest("Consumer demand should never be smaller than consumer.getInitialCapacity(). Some sheet happened here."
					+ "We should find the origin of this bug!");
			Simulator.logger.finest("Demand: "+consumer.getDemand());
			System.exit(0);
		}	
	}
	
	public void removePeerThatDonated(Peer provider){
		simulator.getProvidersList().remove((Integer)provider.getId());
	}
	
	private void sortBalances(Peer ... peers){
		for(Peer p : peers)
			Collections.sort(p.getBalances());
    }

}
