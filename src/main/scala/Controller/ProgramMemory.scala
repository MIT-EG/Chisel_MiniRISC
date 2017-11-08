package Controller

import Common.pmem2ctrl
import Common.Constants
import chisel3._

import scala.io.Source

class ProgramMemory extends Chisel.Module
{
  val io = IO( new Bundle
  {
    val mem_if = new pmem2ctrl()
  })

  //Read Program Code
  val inst = List(UInt(Constants.INSTRUCTION_WIDTH.W))
  val fileName = "code.hex"
  for (line <- Source.fromFile(fileName).getLines())
  {
    inst :+ line.asUInt(Constants.INSTRUCTION_WIDTH.W) //Append item???
    //TODO: Exception in thread "main" java.lang.NumberFormatException: For input string: "222"
  }

  //Load Program Code to ROM
  //val mem = Vec(inst)
  val mem = VecInit(inst)

  //Read Instruction From ROM
  io.mem_if.data := mem(io.mem_if.addr)
}
