package Controller

import Common.pmem2ctrl
import Common.Constants
import chisel3._

import scala.io.Source

class ProgramMemory extends Chisel.Module
{
  val io = IO( new Bundle
  {
    val pmem2ctrl = new pmem2ctrl()
  })

  //Read Program Code
  val fileName = "code.hex"

  val inst=for(line <- Source.fromFile(fileName).getLines)yield(("h" +
    line).U(Constants.INSTRUCTION_WIDTH.W))

  //Load Program Code to ROM
  val mem = Vec(inst.toIndexedSeq)

  //Read Instruction From ROM
  io.pmem2ctrl.data := mem(io.pmem2ctrl.addr)
}