//
package Common

import chisel3._
import chisel3.util.log2Ceil


  class flags extends Bundle
  {
    val carry = Bool()
    val negative = Bool()
    val overflow = Bool()
    val zero = Bool()
  }

  class data2ctrl extends Bundle
  {
    /*
    val carry = Bool()
    val negative = Bool()
    val overflow = Bool()
    val zero = Bool()
    */
    val alu_flag = new flags

    //TODO kell ez nekem? regiszterben tárolt értékre jump van?
    val reg_val = UInt(Constants.ALU_OP_WIDTH.W)
  }

  class ctrl2data extends Bundle
  {
    val mux1sel = Bool()
    val mux2sel = Bool()
    val mux3sel = Bool()

    val alu_op = UInt(Constants.ALU_OP_WIDTH.W)
    val alu_flag = new flags
    val const = UInt(Constants.DATA_WIDTH.W)

    val regs_a = UInt( log2Ceil( Constants.NUMBER_OF_REGISTERS ).W )
    val regs_b = UInt( log2Ceil( Constants.NUMBER_OF_REGISTERS ).W )
    val regs_we = Bool()

    val mem_rd = Bool()
    val mem_wr = Bool()
    val mem_addr = UInt(log2Ceil(Constants.DATA_MEMORY_SIZE).W)
  }

  class data2dmem extends Bundle
  {
    val data = UInt(Constants.DATA_WIDTH.W)

    val addr = UInt(log2Ceil(Constants.DATA_MEMORY_SIZE).W)

    val rd = Bool()
    val wr = Bool()
  }

  class dmem2data extends Bundle
  {
    val mem2data = UInt(Constants.DATA_WIDTH.W)
  }

  class ctrl2pmem extends Bundle
  {
    val addr = Output(UInt(log2Ceil(Constants.PROGRAM_MEMORY_SIZE).W))
  }

  class pmem2ctrl extends Bundle
  {
    val data = UInt(Constants.DATA_WIDTH.W)
  }

