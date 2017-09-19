//
package Common

import chisel3._
import chisel3.util.log2Ceil

object Buses
{
  class Flags extends Bundle
  {
    val carry = Bool()
    val negative = Bool()
    val overflow = Bool()
    val zero = Bool()
  }

  class data2ctrl extends Bundle
  {
    val carry = Bool()
    val negative = Bool()
    val overflow = Bool()
    val zero = Bool()
  }

  class ctrl2data extends Bundle
  {
    val mux1sel = Bool()
    val mux2sel = Bool()

    val alu_op = UInt(Constants.OPERATION_WIDTH.W)
    val const = UInt(Constants.DATA_WIDTH.W)

    val regs_a = UInt( log2Ceil( Constants.NUMBER_OF_REGISTERS ).W )
    val regs_b = UInt( log2Ceil( Constants.NUMBER_OF_REGISTERS ).W )

    val mem_rd = Bool()
    val mem_wr = Bool()
  }

  class data2dmem extends Bundle
  {
    val data2mem = Output(UInt(Constants.DATA_WIDTH.W))
    val mem2data = Input(UInt(Constants.DATA_WIDTH.W))

    val addr = Output(UInt(log2Ceil(Constants.DATA_MEMORY_SIZE).W))

    val rd = Output(Bool())
    val wr = Output(Bool())
  }

  class ctrl2pmem extends Bundle
  {
    val addr = Output(UInt(Constants.PROGRAM_MEMORY_SIZE.W))

    val data = Input(UInt(Constants.DATA_WIDTH.W))
  }
}
