// See LICENSE for license details.

package Common

import chisel3._

object Constants extends Bundle
{
  val DATA_WIDTH = 8
  val OPERATION_WIDTH = 8
  val NUMBER_OF_REGISTERS = 16
  val DATA_MEMORY_SIZE = 256
  val PROGRAM_MEMORY_SIZE = 256
}