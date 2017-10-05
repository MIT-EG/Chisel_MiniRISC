//Register array of the MiniRISC CPU

package DataStructure

import Common.Constants
import chisel3._
import chisel3.util.log2Ceil

class RegisterFile extends Chisel.Module
{
  val io = IO( new Bundle
  {
    val addr_A = Input(UInt( log2Ceil( Constants.NUMBER_OF_REGISTERS ).W ))
    val addr_B = Input(UInt( log2Ceil( Constants.NUMBER_OF_REGISTERS ).W ))

    val we = Input(Bool())
    val din  = Input(UInt(Constants.DATA_WIDTH.W))

    val ra = Output(UInt(Constants.DATA_WIDTH.W))
    val rb = Output(UInt(Constants.DATA_WIDTH.W))
  })

  val regs = Mem(Constants.NUMBER_OF_REGISTERS, UInt(Constants.DATA_WIDTH.W))

  when(io.we === 1.U)
  {
    regs(io.addr_A) := io.din
  }

  io.ra := regs(io.addr_A)
  io.rb := regs(io.addr_B)
}