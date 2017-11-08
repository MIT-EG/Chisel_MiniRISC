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

    val jmp_addr = Input(UInt(log2Ceil(Constants.PROGRAM_MEMORY_SIZE).W)) //direkt ugrási cím

    val ret_addr = Input(UInt(log2Ceil(Constants.PROGRAM_MEMORY_SIZE).W)) //stackből ugrási cím

    val pc = Output(UInt(log2Ceil(Constants.PROGRAM_MEMORY_SIZE).W))
  })

  //Chisel Counter
  //Return nem igazán kell, az is ugrás

  //PC deklaráció
  var pc_reg = RegInit(UInt(log2Ceil(Constants.PROGRAM_MEMORY_SIZE).W), 0.U)

  //PC összekötése a kimeneti porttal
  io.pc := pc_reg

  //INC
  when(io.inc === 1.U && io.jmp === 0.U && io.ret === 0.U)
  {
    when(pc_reg === (Constants.PROGRAM_MEMORY_SIZE.U - 1.U))
    {
      pc_reg := 0.U;
    } .otherwise
    {
      pc_reg := pc_reg + 1.U;
    }
  //JUMP
  } .elsewhen(io.jmp === 1.U && io.inc === 0.U && io.ret === 0.U)
  {
    pc_reg := io.jmp_addr;
  //RETURN
  } .elsewhen(io.ret === 1.U && io.inc === 0.U && io.jmp === 0.U)
  {
    pc_reg := io.ret_addr;
  }
}
