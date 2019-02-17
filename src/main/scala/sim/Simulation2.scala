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
*  SIMULATION 2 -- COMPARES TWO DATACENTERS WITH VARIABLE DATACENTER CHARACTERISTICS
*
* */

object Simulation2 extends App {
  /** The cloudlet list. */
  private var cloudletList : List[Cloudlet] = _

  /** The vmlist. */
  private var vmlist : List[Vm] = _

  private def createVM(userId: Int, vms: Int, size: Int, ram: Int, mips: Int, bw:Int) = { //Creates a container to store VMs. This list is passed to the broker later
    val list = new util.LinkedList[Vm]
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

  private def createDatacenter(name: String, arch: String, os: String, vmm: String, time_zone: Double, cost: Double, costPerMem: Double, costPerStorage: Double, costPerBw: Double): Datacenter = {
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
    Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + indent + "Time" + indent + "Start Time" + indent + "Finish Time" + indent + "Cost/sec" + indent + "Processing Cost" + indent + "CPU Utilization" + indent + "Bandwidth Utilization")
    val dft = new DecimalFormat("###.##")
    val i = 0
    for ( i <- 1 until (size - 1)) {
      val cloudlet = list.get(i).asInstanceOf[Cloudlet]
      Log.print(indent + cloudlet.getCloudletId + indent + indent)
      if (cloudlet.getCloudletStatusString == "Success") {
        Log.print("SUCCESS")
        Log.printLine(indent + indent + cloudlet.getResourceId + indent + indent + indent + cloudlet.getVmId + indent + indent + indent + dft.format(cloudlet.getActualCPUTime) + indent + indent + dft.format(cloudlet.getExecStartTime) + indent + indent + indent + dft.format(cloudlet.getFinishTime) + indent + indent + indent +  cloudlet.getCostPerSec + indent + indent + indent + cloudlet.getProcessingCost + indent + indent + indent + indent + cloudlet.getUtilizationOfCpu(cloudlet.getActualCPUTime) + indent + indent + indent + indent + cloudlet.getUtilizationOfBw(cloudlet.getActualCPUTime) )
      }
    }
  }

  ////////////////////////// MAIN METHOD ///////////////////////

  /////////////////////// helper functions

  private def parseDoubleHelper(message: String) : Double = {
    // **** NOTE TO GRADERS: VARS PRESENT FOR ERROR CHECKING PURPOSES ONLY
    var d_correct : Boolean = false
    var d : Double = 0.0
    while(!d_correct)
    {
      println(message)
      try{
        d = scala.io.StdIn.readLine().toDouble
        d_correct = true
      }
      catch {
        case e: Exception => println("Not a valid number. Try again.")
      }
    }
    d
  }

  private def customDC() : Datacenter = {
    // DATACENTER CHARACTERISTICS
    println("-- System Architecture? [x86/x64]")
    val arch  = scala.io.StdIn.readLine()
    println("-- Operating System? [Linux/Windows/MacOS/etc.]")
    val os = scala.io.StdIn.readLine()
    println("-- Virtual Machine Monitor? [Xen/etc.]")
    val vmm = scala.io.StdIn.readLine()
    val time_zone = parseDoubleHelper("-- Time zone location? [number required]")
    val cost = parseDoubleHelper("-- Cost of using processing? [number required]")
    val costPerMem = parseDoubleHelper("-- Cost of using memory? [number required]")
    val costPerStorage = parseDoubleHelper("-- Cost of using storage? [number required]")
    val costPerBw = parseDoubleHelper("-- Cost of using bandwidth? [number required]")
    println("-- Datacenter name?")
    val name = scala.io.StdIn.readLine()

    @SuppressWarnings(Array("unused")) val dc = createDatacenter("Datacenter_0", arch, os, vmm, time_zone, cost, costPerMem, costPerStorage, costPerBw)

    dc
  }

  /**
    * Main Method
    */
  //def main(args: Array[String]): Unit = {
  Log.printLine("Simulation 2 -- Mika Cabudol (mcabud2)")
  Log.printLine("Starting simulation 2...")
  try {
    val num_user = 1
    val calendar = Calendar.getInstance
    val trace_flag = false // mean trace events
    // Initialize the CloudSim library
    CloudSim.init(num_user, calendar, trace_flag)

    // create datacenters
    println("==========ENTER THE CHARACTERISTICS OF THE 1ST DATACENTER=========")
    val datacenter0 = customDC()
    println("==========ENTER THE CHARACTERISTICS OF THE 2ND DATACENTER=========")
    val datacenter1 = customDC()

    val broker = createBroker
    val brokerId = broker.getId

    // CREATE VMS TO BE SENT TO BROKERS
    println("==========ENTER THE CHARACTERISTICS OF THE VIRTUAL MACHINES=========")
    val ram = parseDoubleHelper("-- VM RAM (MB)? [number required]").asInstanceOf[Int]
    val mips = parseDoubleHelper("-- VM computing capacity? [number required]").asInstanceOf[Int]
    val bw = parseDoubleHelper("-- VM Bandwidth? [number required]").asInstanceOf[Int]
    vmlist = createVM(brokerId, 20, size, ram, mips, bw) //creating 20 vms

    // ENTER AMOUNT OF CLOUDLETS TO BE COMPARED
    println()
    val nCloudlet : Double = parseDoubleHelper("-- Enter Amount of Cloudlets to be Compared")

    cloudletList = createCloudlet(brokerId, nCloudlet.toInt)

    broker.submitVmList(vmlist)
    broker.submitCloudletList(cloudletList)

    // Fifth step: Starts the simulation
    CloudSim.startSimulation

    // Final step: Print results when simulation is over
    CloudSim.stopSimulation()
    printCloudletList(broker.getCloudletReceivedList())

    Log.printLine("Simulation 2 finished!")
  } catch {
    case e: Exception =>
      e.printStackTrace()
      Log.printLine("The simulation has been terminated due to an unexpected error")
  }
  //}

}