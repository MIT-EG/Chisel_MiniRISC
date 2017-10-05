package Controller

import Common.Buses.pmem2ctrl
import Common.Constants
import chisel3._

class ProgramMemory extends Chisel.Module
{
  val io = IO( new Bundle
  {
    val mem_if = new pmem2ctrl()
  })

  //Read Program Code
  val src = scala.io.Source.fromFile("code.asm")
  val lines = try src.getLines() finally src.close()

  val inst = Array(UInt(Constants.INSTRUCTION_WIDTH.W))
  var cntr = 0
  for (line <- lines)
  {
    inst(cntr) = line.asUInt(Constants.INSTRUCTION_WIDTH.W)
  }

  //Load Program Code to ROM
  val mem = Vec(inst)

  //Read Instruction From ROM
  io.mem_if.data := mem(io.mem_if.addr)
}
