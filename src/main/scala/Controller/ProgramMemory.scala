package Controller

import Common.{Constants, ctrl2pmem, pmem2ctrl}
import chisel3._

import scala.io.Source

class ProgramMemory extends Chisel.Module
{
  val io = IO( new Bundle
  {
    val pmem2ctrl = Output(new pmem2ctrl())
    val ctrl2pmem = Input(new ctrl2pmem())
  })

  //Read Program Code
  val fileName = "code.hex"

  val inst=for(line <- Source.fromFile(fileName).getLines)yield(("h" +
    line).U(Constants.INSTRUCTION_WIDTH.W))

  //Load Program Code to ROM
  val mem = Vec(inst.toIndexedSeq)

  //Read Instruction From ROM
  io.pmem2ctrl.data := mem(io.ctrl2pmem.addr)
}