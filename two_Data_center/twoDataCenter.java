/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */


package org.cloudbus.cloudsim.examples;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;


/**
 * An example showing how to create
 * scalable simulations.
 */
public class twoDataCenter {

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList1,cloudletList2,cloudletList3;
	
	private static List<VTasks> taskList;

	/** The vmlist. */
	private static List<Vm> vmlist1,vmlist2,vmlist3;
	
	private static List<List<Vm>> VCs;
	
	
	//All the instance share cloudletNewArrivalQueue and cloudletBatchqueue, both of them are synchronized list
	private static List<Cloudlet> cloudletNewArrivalQueue = Collections.synchronizedList(new ArrayList<Cloudlet>());
	private static List<Cloudlet> cloudletBatchQueue = Collections.synchronizedList(new ArrayList<Cloudlet>());
	
	public static Properties prop = new Properties();
	

	private static List<Vm> createVM_N(int userId, int vms, int mips, int idShift) {
        //Creates a container to store VMs. This list is passed to the broker later
        LinkedList<Vm> list = new LinkedList<Vm>();

        //VM Parameters
        long size = 10000; //image size (MB)
        int ram = 512; //vm memory (MB)
        //int mips = 2000;
        long bw = 1000;
        int pesNumber = 1; //number of cpus
        String vmm = "Xen"; //VMM name
         int hmips = 100;
        //create VMs
        Vm[] vm = new Vm[vms];
        
        for (int i = 0; i < vms; i++) {
            vm[i] = new Vm(idShift + i, userId, hmips+mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
            list.add(vm[i]);
            hmips= hmips+500;
        }
        
        return list;
    }
	
/*
	// creating 2 different VM(Base Stations) 
	
	private static List<Vm> create2VM(int userId) {

		//Creates a container to store VMs. This list is passed to the broker later
		LinkedList<Vm> list = new LinkedList<Vm>();

		//VM_1 Parameters ---- weak one
		long size_1 = 10000; //image size (MB)
		int ram_1 = 512; //vm memory (MB)
		int mips_1 = 3000;
		long bw_1 = 1000;
		int pesNumber = 1; //number of cpus
		String vmm = "Xen"; //VMM name
		
		//VM_2 Parameters ---- strong one
		long size_2 = 8000; //image size (MB)
		int ram_2 = 1024; //vm memory (MB)
		int mips_2 = 5000;
		long bw_2 = 2000;
		

		//create VMs(Base Stations)
		Vm[] vm = new Vm[2];

		vm[0] = new Vm(0, userId, mips_1, pesNumber, ram_1, bw_1, size_1, vmm, new CloudletSchedulerSpaceShared());
			list.add(vm[0]);
			
		vm[1] =  new Vm(1, userId, mips_2, pesNumber, ram_2, bw_2, size_2, vmm, new CloudletSchedulerSpaceShared());
			list.add(vm[1]);
			//for creating a VM with a space shared scheduling policy for cloudlets:
			
		return list;
	}
	
	*/
    // creating the tasks(cloudlets) for base stations
	private static List<Cloudlet> createCloudlet(int userId, int cloudlets, int START, int END, int idShift){
		// Creates a container to store Cloudlets
		LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

		//tasks(Cloudlets) parameters
		long length = 2000; // mips of cloudlet
		long fileSize = 300;
		long outputSize = 300;
		int pesNumber = 1;
		UtilizationModel utilizationModel = new UtilizationModelFull();

		Cloudlet[] cloudlet = new Cloudlet[cloudlets];

		for(int i=0;i<cloudlets;i++){
			Random rObj = new Random();
			
			cloudlet[i] = new Cloudlet(idShift+i, (length+showRandomInteger(START, END,rObj)), pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			// setting the owner of these Cloudlets
			cloudlet[i].setUserId(userId);
			list.add(cloudlet[i]);
		}

		return list;
	}
	 
	
	
	// creating the tasks(vTasks) for base stations currently not used
		private static List<VTasks> createVTasks(int userId, int numOfTasks, int START, int END, int idShift){
			// Creates a container to store Cloudlets
			LinkedList<VTasks> list = new LinkedList<VTasks>();

			//tasks(Cloudlets) parameters
			long length = 2000; // mips of cloudlet
			long fileSize = 300;
			long outputSize = 300;
			int pesNumber = 1;
			UtilizationModel utilizationModel = new UtilizationModelFull();

			VTasks[] tasks = new VTasks[numOfTasks];

			for(int i=0;i<numOfTasks;i++){
				Random rObj = new Random();
				
				//tasks[i] = new VTasks(idShift+i, (length+showRandomInteger(START, END,rObj)),showRandomInteger(1, 10,rObj),10,pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
				// setting the owner of these Cloudlets
				tasks[i].setUserId(userId);
				list.add(tasks[i]);
			}

			return list;
		}
	
	
	//function for randomly generate task for different task arriving situations. This can be used for creating over subscribe situation.
	private static int generateTasksRandomly(int aStart, int aEnd, Random aRandom){
	    if (aStart > aEnd) {
		      throw new IllegalArgumentException("Start cannot exceed End.");
		    }
		    //get the range, casting to long to avoid overflow problems
		    long range = (long)aEnd - (long)aStart + 1;
		    // compute a fraction of the range, 0 <= frac < range
		    long fraction = (long)(range * aRandom.nextDouble());
		    int randomNumber =  (int)(fraction + aStart);    
		    
		    return randomNumber;
	 }	
		
		
	
	private static int showRandomInteger(int aStart, int aEnd, Random aRandom){
	    if (aStart > aEnd) {
	      throw new IllegalArgumentException("Start cannot exceed End.");
	    }
	    //get the range, casting to long to avoid overflow problems
	    long range = (long)aEnd - (long)aStart + 1;
	    // compute a fraction of the range, 0 <= frac < range
	    long fraction = (long)(range * aRandom.nextDouble());
	    int randomNumber =  (int)(fraction + aStart);    
	    
	    return randomNumber;
	  }
	
	//customized cloudlet creation for testing
	/*
	private static List<Cloudlet> create3Cloudlet(int userId){
		// Creates a container to store Cloudlets
		LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();
		
		//task 1(Cloudlets) parameters
		long length1 = 1000;
		long fileSize1 = 300;
		long outputSize1 = 300;
		int pesNumber1 = 1;
		UtilizationModel utilizationModel = new UtilizationModelFull();
		
		Cloudlet[] cloudlet = new Cloudlet[3];
		
		cloudlet[0] = new Cloudlet(0, length1, pesNumber1, fileSize1, outputSize1, utilizationModel, utilizationModel, utilizationModel);
		cloudlet[0].setUserId(userId);
		list.add(cloudlet[0]);
		
		//task 2(Cloudlets) parameters
		long length2 = 1000;
		long fileSize2 = 300;
		long outputSize2 = 300;
		int pesNumber2 = 1;

		
		cloudlet[1] = new Cloudlet(1, length2, pesNumber2, fileSize2, outputSize2, utilizationModel, utilizationModel, utilizationModel);
		cloudlet[1].setUserId(userId);
		list.add(cloudlet[1]);
		
		
		//task 3(Cloudlets) parameters
		long length3 = 1000;
		long fileSize3 = 300;
		long outputSize3 = 300;
		int pesNumber3 = 1;
		
		cloudlet[2] = new Cloudlet(2, length3, pesNumber3, fileSize3, outputSize3, utilizationModel, utilizationModel, utilizationModel);
		cloudlet[2].setUserId(userId);
		list.add(cloudlet[2]);
		
		return list;
	}
	*/
	

	////////////////////////// STATIC METHODS ///////////////////////

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {
		Log.printLine("Starting Simulation for V2I task processing...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 1;   // number of grid users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;  // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create 3 Datacenters
			//@SuppressWarnings("unused")
			Datacenter datacenter0 = createDatacenter("BaseStation_0",1);
			//@SuppressWarnings("unused")
			Datacenter datacenter1 = createDatacenter("BaseStation_1",1);
			//@SuppressWarnings("unused")
			Datacenter datacenter2 = createDatacenter("BaseStation_2",1);
			
			//Third step: Create Broker
			DatacenterBroker broker1 = createBroker("broker1");// create broker 1
			vmlist1 = createVM_N(broker1.getId(), 5,2000, 1);// create 5 vm with mips of 1000(weak) and id starting from 1 in broker 1
			broker1.submitVmList(vmlist1);
			
			
			Random randomTaskObject = new Random();// random task generator object
			int startingRange = 1;
			int endingRange = 5;
			
			
			
			cloudletList1 = createCloudlet(broker1.getId(), generateTasksRandomly(startingRange, endingRange,randomTaskObject),100,200,1);// cloudlet length from 1100-1200
			//taskList = createVTasks(broker1.getId(), 6, 100, 200, 1);
			broker1.submitCloudletList(cloudletList1); // submitting cloudlets to a datacenter.
			
			
			int minOversubcribe = 5;
			int maxOversubsribe = 10;
			
				
			DatacenterBroker broker2 = createBroker("broker2");//create broker 2
			vmlist2 = createVM_N(broker2.getId(), generateTasksRandomly(minOversubcribe, maxOversubsribe,randomTaskObject), 15000, 1001);// create 5 vm with mips of 2000(strong) and id starting from 1001 in broker 2
			broker2.submitVmList(vmlist2);
			cloudletList2 = createCloudlet(broker2.getId(), 5,100,200,1001);
			broker2.submitCloudletList(cloudletList2);
			
			
			DatacenterBroker broker3 = createBroker("broker3");//create broker 2
			vmlist3 = createVM_N(broker3.getId(), 5, 4000, 1001);// create 5 vm with mips of 2000(strong) and id starting from 1001 in broker 2
			broker3.submitVmList(vmlist3);
			cloudletList3 = createCloudlet(broker3.getId(), 5,100,200,2001);
			
			broker3.submitCloudletList(cloudletList3);
			
			
			
			//DatacenterBroker broker3 = createBroker("broker3");//create broker 3
			//vmlist3 = createVM_N(broker3.getId(), 5, 3000, 2001);// create 5 vm with mips of 2000(strong) and id starting from 1001 in broker 2
			//broker3.submitVmList(vmlist3);
			//cloudletList3 = createCloudlet(broker3.getId(), 10,100,200,2001);
			//broker3.submitCloudletList(cloudletList3);
			
			
			// A thread that will create a new broker at 200 clock time
		   /*	Runnable monitor = new Runnable() {
					@Override
					public void run() {
						CloudSim.pauseSimulation(200);
						while (true) {
							if (CloudSim.isPaused()) {
								
								break;
							}
							try {
								
								
								Thread.sleep(100);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}

						
						
						Log.printLine("\n\n\n" + CloudSim.clock() + ": The simulation is paused for 3 sec \n\n");

						try {
							Thread.sleep(3000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						
							


							CloudSim.resumeSimulation();
						}
			};

			new Thread(monitor).start();
			Thread.sleep(1000);
			*/

			// Fifth step: Starts the simulation
			CloudSim.startSimulation();
			

			//broker.submitVmList(vmlist2);
			//broker.submitCloudletList(cloudletList2);
			

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker1.getCloudletReceivedList();
			
			//List<VTasks> newList1 = broker1.getVTasksReceivedList();
			
			 newList.addAll(broker2.getCloudletReceivedList());
			 newList.addAll(broker3.getCloudletReceivedList());

			CloudSim.stopSimulation();

			printCloudletList(newList);
			
			Log.printLine("V2I task processing finished!");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
	}

	private static Datacenter createDatacenter(String name, int hostNumber){

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store one or more
		//    Machines
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
		//    create a list to store these PEs before creating
		//    a Machine.
		List<Pe> peList1 = new ArrayList<Pe>();

		int mips = 40000; // this is the mips for each core of host of datacenter

		// 3. Create PEs and add these into the list.
		//for a quad-core machine, a list of 4 PEs is required:
		peList1.add(new Pe(0, new PeProvisionerSimple(20000))); // need to store Pe id and MIPS Rating
		peList1.add(new Pe(1, new PeProvisionerSimple(10000)));
		peList1.add(new Pe(2, new PeProvisionerSimple(40000)));
		peList1.add(new Pe(3, new PeProvisionerSimple(40000)));
		peList1.add(new Pe(4, new PeProvisionerSimple(50000))); // need to store Pe id and MIPS Rating
		
		//peList1.add(new Pe(5, new PeProvisionerSimple(mips)));
		//peList1.add(new Pe(6, new PeProvisionerSimple(mips)));
		//peList1.add(new Pe(7, new PeProvisionerSimple(mips)));
		
		
		//Another list, for a dual-core machine
		//List<Pe> peList2 = new ArrayList<Pe>();

		//peList2.add(new Pe(0, new PeProvisionerSimple(mips)));
		//peList2.add(new Pe(1, new PeProvisionerSimple(mips)));

		//4. Create Hosts with its id and list of PEs and add them to the list of machines
		int hostId=0;
		int ram = 8192; //host memory (MB) 8 GB given by Razin
		long storage = 1000000; //host storage
		int bw = 20000;

		for (int i = 0; i < hostNumber; i++) {
		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList1,
    				new VmSchedulerSpaceShared(peList1)// SpaceScheduler distribute VMs to different data centers
    			)
    		); // This is our first machine

			hostId++;
		}


		// 5. Create a DatacenterCharacteristics object that stores the
		//    properties of a data center: architecture, OS, list of
		//    Machines, allocation policy: time- or space-shared, time zone
		//    and its price (G$/Pe time unit).
		String arch = "x86";      // system architecture
		String os = "Linux";          // operating system
		String vmm = "Xen";
		double time_zone = 10.0;         // time zone this resource located
		double cost = 3.0;              // the cost of using processing in this resource
		double costPerMem = 0.05;		// the cost of using memory in this resource
		double costPerStorage = 0.1;	// the cost of using storage in this resource
		double costPerBw = 0.1;			// the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 100);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	//Need to develop own broker policies, to submit vms and cloudlets according
	//to the specific rules of the simulated scenario
	private static DatacenterBroker createBroker(String name){

		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker(name);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects
	 * @param list  list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +indent+
				"Data center ID" + indent+indent+ "VM ID" + indent + indent+"  "+ "Time"+indent+indent +"Task Length"+ indent+indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
				Log.print("SUCCESS");

				Log.printLine( indent + indent+cloudlet.getResourceName(cloudlet.getResourceId()) + indent + indent + indent + cloudlet.getVmId() +
						indent + indent + indent + dft.format(cloudlet.getActualCPUTime()) +
						indent + indent + cloudlet.getCloudletLength()+ indent + indent +indent +dft.format(cloudlet.getExecStartTime())+ indent + indent + indent + dft.format(cloudlet.getFinishTime()));
			}
		}

	}
	
	
	 //Inner-Class GLOBAL BROKER...
    public static class GlobalBroker extends SimEntity {
        
        private static final int CREATE_BROKER = 0;
        private List<Vm> vmList;
        private List<Cloudlet> cloudletList;
        
        private List<VTasks> vtaskList;
        
        private DatacenterBroker broker;
        
        public GlobalBroker(String name) {
            super(name);
        }
        
        @Override
        public void processEvent(SimEvent ev) {
            switch (ev.getTag()) {
                case CREATE_BROKER:
                    setBroker(createBroker(super.getName() + "_"));

                    //Create VMs and Cloudlets and send them to broker
//                    setVmList(createVM(getBroker().getId(), 5, 100)); //creating 5 vms
//                    setCloudletList(createCloudlet(getBroker().getId(), 10, 100)); // creating 10 cloudlets

                    
                    broker.submitVmList(getVmList());
                    broker.submitCloudletList(getCloudletList());
                    
                    //broker.submitVTaskList(getVTaskList());
                    
                    

//                    CloudSim.resumeSimulation();

                    break;
                
                default:
                    Log.printLine(getName() + ": unknown event type");
                    break;
            }
        }
        
        @Override
        public void startEntity() {
            Log.printLine(CloudSim.clock() + super.getName() + " is starting...");
            schedule(getId(), 200, CREATE_BROKER);
            
        }
        
        @Override
        public void shutdownEntity() {
            System.out.println("Global Broker is shutting down...");
        }
        
        public List<Vm> getVmList() {
            return vmList;
        }
        
        protected void setVmList(List<Vm> vmList) {
            this.vmList = vmList;
        }
        
        public List<Cloudlet> getCloudletList() {
            return cloudletList;
        }
        
        
        //for vtasklist
        public List<VTasks> getVTaskList() {
            return vtaskList;
        }
        
        
        protected void setCloudletList(List<Cloudlet> cloudletList) {
            this.cloudletList = cloudletList;
        }
        
        protected void setVTaskList(List<VTasks> vtaskList) {
            this.vtaskList = vtaskList;
        }
        
        
        public DatacenterBroker getBroker() {
            return broker;
        }
        
        protected void setBroker(DatacenterBroker broker) {
            this.broker = broker;
        }
    }
}
