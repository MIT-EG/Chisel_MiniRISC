package DataStructure

import chisel3._
import Common.{ Constants}

class DataMemory extends Chisel.Module
{
  val io = IO(new Bundle
  {
    val dmem2data = new Common.dmem2data();
  })

  val mem = Mem(Constants.DATA_MEMORY_SIZE, UInt(Constants.DATA_WIDTH.W))

  //Írásnak nagyobb a prioritása
  when(io.dmem2data.wr === 1.U)
  {
    mem(io.dmem2data.addr) := io.dmem2data.data2mem
  }
  when(io.dmem2data.rd === 1.U && io.dmem2data.wr === 0.U)
  {
    io.dmem2data.mem2data := mem(io.dmem2data.addr)
  }
}
