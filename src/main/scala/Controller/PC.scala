package Controller

import Common.Constants
import chisel3._
import chisel3.util.log2Ceil

class PC extends Chisel.Module
{
  val io = IO( new Bundle
  {
    val inc = Input(Bool())
    val jmp = Input(Bool())
    val ret = Input(Bool())

    val jmp_addr = Input(UInt(log2Ceil(Constants.PROGRAM_MEMORY_SIZE)))
    val ret_addr = Input(UInt(log2Ceil(Constants.PROGRAM_MEMORY_SIZE)))

    val pc = Output(UInt(log2Ceil(Constants.PROGRAM_MEMORY_SIZE)))
  })

  //PC deklaráció
  var pc_reg = UInt(log2Ceil(Constants.PROGRAM_MEMORY_SIZE))
  pc_reg = 0.U
  io.pc := pc_reg

  //INC
  when(io.inc === 1.U && io.jmp === 0.U && io.ret === 0.U)
  {
    when(pc_reg === (Constants.PROGRAM_MEMORY_SIZE.U - 1.U))
    {
      pc_reg = 0.U;
    } .otherwise
    {
      pc_reg = pc_reg + 1.U;
    }
  }

  //JUMP
  when(io.jmp === 1.U && io.inc === 0.U && io.ret === 0.U)
  {
    pc_reg = io.jmp_addr;
  }

  //RETURN
  when(io.jmp === 1.U && io.inc === 0.U && io.ret === 0.U)
  {
    pc_reg = io.ret_addr;
  }
}
