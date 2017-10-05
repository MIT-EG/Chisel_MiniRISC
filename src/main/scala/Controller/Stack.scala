package Controller

import Common.Constants
import Common.Buses.flags
import chisel3._
import chisel3.util.{Cat, log2Ceil}

class Stack extends Chisel.Module
{
  val io = IO( new Bundle
  {
    val pc_in = Input(UInt(log2Ceil(Constants.PROGRAM_MEMORY_SIZE)))
    val pc_out = Output(UInt(log2Ceil(Constants.PROGRAM_MEMORY_SIZE)))

    val flag_in = Input(new flags)
    val flag_out = Output(new flags)

    val pop = Input(Bool())
    val push = Input(Bool())
  })

  val stck = Mem(Constants.STACK_DEPTH, UInt(Constants.STACK_WIDTH.W))

  var sp = UInt(0, Constants.STACK_DEPTH - 1)

  val din = Cat(io.pc_in, io.flag_in.carry, io.flag_in.zero, io.flag_in.negative, io.flag_in.overflow, io.flag_in.it_flag, io.flag_in.it_en)
  val dout = Cat(io.pc_out, io.flag_out.carry, io.flag_out.zero, io.flag_out.negative, io.flag_out.overflow, io.flag_out.it_flag, io.flag_out.it_en)

  //TODO: kell error kezelés ha akkor van push amikor megtelt a stack vagy akkor van pop ha üres? -> error wire?

  when(io.push === 1.U && io.pop === 0.U) //egyszerre nem történhet push és pop!
  {
    when(sp < (Constants.STACK_DEPTH.U - 1.U))
    {
      stck(sp) := din //ez így össze van kötve az io portokkal?
      sp = sp + 1.U
    }
  }

  when(io.pop === 1.U && io.push === 0.U)
  {
    when(sp > 0.U)
    {
      dout := stck(sp - 1.U)
      sp = sp - 1.U
    }
  }
}
