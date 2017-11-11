package DataStructure

import chisel3._
import Common.{Constants, data2dmem, dmem2data}

class DataMemory extends Chisel.Module
{
  val io = IO(new Bundle
  {
    val dmem2data = Output(new dmem2data())
    val data2dmem = Input(new data2dmem())
  })

  val mem = Mem(Constants.DATA_MEMORY_SIZE, UInt(Constants.DATA_WIDTH.W))

  //Írásnak nagyobb a prioritása
  when(io.data2dmem.wr === 1.U)
  {
    mem(io.data2dmem.addr) := io.data2dmem.data
  }
  when(io.data2dmem.rd === 1.U && io.data2dmem.wr === 0.U)
  {
    io.dmem2data.mem2data := mem(io.data2dmem.addr)
  }
}
