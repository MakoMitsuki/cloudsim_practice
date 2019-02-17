package sim

import java.text.DecimalFormat
import java.util
import java.util.ArrayList
import java.util.Calendar
import java.util.LinkedList
import java.util.List

import org.cloudbus.cloudsim.Cloudlet
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared
import org.cloudbus.cloudsim.Datacenter
import org.cloudbus.cloudsim.DatacenterBroker
import org.cloudbus.cloudsim.DatacenterCharacteristics
import org.cloudbus.cloudsim.Host
import org.cloudbus.cloudsim.Log
import org.cloudbus.cloudsim.Pe
import org.cloudbus.cloudsim.Storage
import org.cloudbus.cloudsim.UtilizationModel
import org.cloudbus.cloudsim.UtilizationModelFull
import org.cloudbus.cloudsim.Vm
import org.cloudbus.cloudsim.VmAllocationPolicySimple
import org.cloudbus.cloudsim.VmSchedulerTimeShared
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple

/*
*
*  SIMULATION 1 -- SCALA VERSION OF CLOUDSIMEXAMPLE 6
*
* */

object Simulation1_1 extends App {
  /** The cloudlet list. */
  private var cloudletList : List[Cloudlet] = _

  /** The vmlist. */
  private var vmlist : List[Vm] = _

  private def createVM(userId: Int, vms: Int) = { //Creates a container to store VMs. This list is passed to the broker later
    val list = new util.LinkedList[Vm]
    //VM Parameters
    val size = 10000
    //image size (MB)
    val ram = 512
    //vm memory (MB)
    val mips = 1000
    val bw = 1000
    val pesNumber = 1
    //number of cpus
    val vmm = "Xen"
    //VMM name
    //create VMs
    val vm = new Array[Vm](vms)
    for ( i <- 1 until (vms - 1)) {
      vm(i) = new Vm(i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared)
      //for creating a VM with a space shared scheduling policy for cloudlets:
      //vm[i] = Vm(i, userId, mips, pesNumber, ram, bw, size, priority, vmm, new CloudletSchedulerSpaceShared());
      list.add(vm(i))
    }
    list
  }


  private def createCloudlet(userId: Int, cloudlets: Int) = { // Creates a container to store Cloudlets
    val list = new util.LinkedList[Cloudlet]
    //cloudlet parameters
    val length = 1000
    val fileSize = 300
    val outputSize = 300
    val pesNumber = 1
    val utilizationModel = new UtilizationModelFull
    val cloudlet = new Array[Cloudlet](cloudlets)
    for ( i <- 1 until (cloudlets - 1)) {
      cloudlet(i) = new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel)
      // setting the owner of these Cloudlets
      cloudlet(i).setUserId(userId)
      list.add(cloudlet(i))
    }
    list
  }

  private def createDatacenter(name: String): Datacenter = {
    // Here are the steps needed to create a PowerDatacenter:
    // 1. We need to create a list to store one or more
    //    Machines
    val hostList = new util.ArrayList[Host]
    // 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
    //    create a list to store these PEs before creating
    //    a Machine.
    val peList1 = new util.ArrayList[Pe]
    val mips:Double = 1000

    // 3. Create PEs and add these into the list.
    //for a quad-core machine, a list of 4 PEs is required:

    peList1.add(new Pe(0, new PeProvisionerSimple(mips))) // need to store Pe id and MIPS Rating
    peList1.add(new Pe(1, new PeProvisionerSimple(mips)))
    peList1.add(new Pe(2, new PeProvisionerSimple(mips)))
    peList1.add(new Pe(3, new PeProvisionerSimple(mips)))

    //Another list, for a dual-core machine
    val peList2 = new util.ArrayList[Pe]
    peList2.add(new Pe(0, new PeProvisionerSimple(mips)))
    peList2.add(new Pe(1, new PeProvisionerSimple(mips)))
    //4. Create Hosts with its id and list of PEs and add them to the list of machines
    var hostId = 0
    val ram = 2048
    //host memory (MB)
    val storage = 1000000
    //host storage
    val bw = 10000
    hostList.add(new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList1, new VmSchedulerTimeShared(peList1))) // This is our first machine

    hostId += 1
    hostList.add(new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList2, new VmSchedulerTimeShared(peList2))) // Second machine

    //To create a host with a space-shared allocation policy for PEs to VMs:
    //hostList.add(
    //		new Host(
    //			hostId,
    //			new CpuProvisionerSimple(peList1),
    //			new RamProvisionerSimple(ram),
    //			new BwProvisionerSimple(bw),
    //			storage,
    //			new VmSchedulerSpaceShared(peList1)
    //		)
    //	);
    //To create a host with a oportunistic space-shared allocation policy for PEs to VMs:
    //hostList.add(
    //		new Host(
    //			hostId,
    //			new CpuProvisionerSimple(peList1),
    //			new RamProvisionerSimple(ram),
    //			new BwProvisionerSimple(bw),
    //			storage,
    //			new VmSchedulerOportunisticSpaceShared(peList1)
    //		)
    //	);
    // 5. Create a DatacenterCharacteristics object that stores the
    //    properties of a data center: architecture, OS, list of
    //    Machines, allocation policy: time- or space-shared, time zone
    //    and its price (G$/Pe time unit).
    val arch = "x86"
    // system architecture
    val os = "Linux"
    // operating system
    val vmm = "Xen"
    val time_zone = 10.0
    // time zone this resource located
    val cost = 3.0
    // the cost of using processing in this resource
    val costPerMem = 0.05
    // the cost of using memory in this resource
    val costPerStorage = 0.1
    // the cost of using storage in this resource
    val costPerBw = 0.1
    // the cost of using bw in this resource
    val storageList = new util.LinkedList[Storage]
    //we are not adding SAN devices by now
    val characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw)
    // 6. Finally, we need to create a PowerDatacenter object.
    var datacenter:Datacenter = null
    try
      datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0)
    catch {
      case e: Exception =>
        e.printStackTrace()
    }

    datacenter
  }

  //We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
  //to the specific rules of the simulated scenario
  private def createBroker: DatacenterBroker = {
    var broker : DatacenterBroker = null
    try
      broker = new DatacenterBroker("Broker")
    catch {
      case e: Exception =>
        e.printStackTrace()
        return null
    }

    broker
  }

  /**
    * Prints the Cloudlet objects
    *
    * @param list list of Cloudlets
    */

  private def printCloudletList(list: util.List[_ <: Cloudlet]): Unit = {
    val size = list.size
    //var cloudlet : Cloudlet = null
    val indent = "    "
    Log.printLine()
    Log.printLine("========== OUTPUT ==========")
    Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + indent + "Time" + indent + "Start Time" + indent + "Finish Time")
    val dft = new DecimalFormat("###.##")
    for ( i <- 1 until (size - 1)) {
      val cloudlet = list.get(i).asInstanceOf[Cloudlet]
      Log.print(indent + cloudlet.getCloudletId + indent + indent)
      if (cloudlet.getCloudletStatusString == "Success") {
        Log.print("SUCCESS")
        Log.printLine(indent + indent + cloudlet.getResourceId + indent + indent + indent + cloudlet.getVmId + indent + indent + indent + dft.format(cloudlet.getActualCPUTime) + indent + indent + dft.format(cloudlet.getExecStartTime) + indent + indent + indent + dft.format(cloudlet.getFinishTime))
      }
    }
  }


  ////////////////////////// MAIN METHOD ///////////////////////

  /**
    * Creates main() to run this example
    */
  //def main(args: Array[String]): Unit = {
  Log.printLine("Simulation 1 -- Mika Cabudol (mcabud2)")
  Log.printLine("Starting simulation 1...")
  try { // First step: Initialize the CloudSim package. It should be called
    // before creating any entities.
    val num_user = 1
    // number of grid users
    val calendar = Calendar.getInstance
    val trace_flag = false // mean trace events
    // Initialize the CloudSim library
    CloudSim.init(num_user, calendar, trace_flag)
    // Second step: Create Datacenters
    //Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
    @SuppressWarnings(Array("unused")) val datacenter0 = createDatacenter("Datacenter_0")
    @SuppressWarnings(Array("unused")) val datacenter1 = createDatacenter("Datacenter_1")
    //Third step: Create Broker
    val broker = createBroker
    val brokerId = broker.getId
    //Fourth step: Create VMs and Cloudlets and send them to broker
    vmlist = createVM(brokerId, 20) //creating 20 vms

    cloudletList = createCloudlet(brokerId, 40) // creating 40 cloudlets

    broker.submitVmList(vmlist)
    broker.submitCloudletList(cloudletList)
    // Fifth step: Starts the simulation
    CloudSim.startSimulation
    // Final step: Print results when simulation is over
    CloudSim.stopSimulation()
    printCloudletList(broker.getCloudletReceivedList())

    Log.printLine("Simulation 1 finished!")
  } catch {
    case e: Exception =>
      e.printStackTrace()
      Log.printLine("The simulation has been terminated due to an unexpected error")
  }
  //}

}
