package Common

import Controller._
import DataStructure._
import chisel3._

class MiniRISC extends Chisel.Module
{
  val io = IO( new Bundle
  {
    val y = Output(UInt(Constants.DATA_WIDTH.W))
    val flag_out = Output(new flags)
  })

  val ctrl = Module(new Controller())
  val pmem = Module(new ProgramMemory())
  val datastruct = Module(new DataStructure())
  val dmem = Module(new DataMemory())

  ctrl.io.data2ctrl := datastruct.io.data2ctrl
  ctrl.io.ctrl2pmem := pmem.io.pmem2ctrl
  datastruct.io.ctrl2data := ctrl.io.ctrl2pmem
  datastruct.io.data_mem := dmem.io.dmem2data
}
