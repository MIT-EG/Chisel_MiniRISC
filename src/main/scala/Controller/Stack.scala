package Controller

import Common.Constants
import Common.flags
import chisel3._
import chisel3.util.{Cat, log2Ceil}

class Stack extends Chisel.Module
{
  //Inner class for storing the program counter and the alu flags
  class StackItem extends Bundle
  {
    val pc = (UInt(log2Ceil(Constants.PROGRAM_MEMORY_SIZE).W))
    val flags = new flags()
  }

  //Interface of the module
  val io = IO( new Bundle
  {
    val pc_in = Input(new StackItem())

    val pc_out = Output(new StackItem())

    val pop = Input(Bool())
    val push = Input(Bool())
  })

  //Stack as memory
  val stck = Mem(Constants.STACK_DEPTH, (new StackItem)) //StackItem

  //Stack pointer
  val sp = RegInit(UInt(Constants.STACK_DEPTH.W), 0.U)

  //Output register
  val out = Reg(new StackItem())

  //New item added to the stack -> PUSH
  when(io.push === 1.U && io.pop === 0.U) //egyszerre nem történhet push és pop!
  {
    when(sp < (Constants.STACK_DEPTH.U - 1.U))
    {
      stck(sp) := io.pc_in
      sp := sp + 1.U
    }
  }

  //Item read and removed from the stack -> POP
  when(io.pop === 1.U && io.push === 0.U)
  {
    when(sp > 0.U)
    {
      out := stck(sp - 1.U)
      sp := sp - 1.U
    }
  }
  io.pc_out := out
}
