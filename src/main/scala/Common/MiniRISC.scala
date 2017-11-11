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
  datastruct.io.ctrl2data := ctrl.io.ctrl2data

  ctrl.io.pmem2ctrl := pmem.io.pmem2ctrl
  pmem.io.ctrl2pmem := ctrl.io.ctrl2pmem

  datastruct.io.dmem2data := dmem.io.dmem2data
  dmem.io.data2dmem := datastruct.io.data2dmem
}
