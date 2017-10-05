// See LICENSE for license details.

package Common

import chisel3._
import chisel3.util.log2Ceil

object Constants extends Bundle
{
  val DATA_WIDTH = 8
  val INSTRUCTION_WIDTH = 16

  val ALU_OP_WIDTH = 8
  val NUMBER_OF_REGISTERS = 16
  val DATA_MEMORY_SIZE = 256
  val PROGRAM_MEMORY_SIZE = 256

  val STACK_DEPTH = 16
  val STACK_WIDTH = log2Ceil(PROGRAM_MEMORY_SIZE) + 6;
}