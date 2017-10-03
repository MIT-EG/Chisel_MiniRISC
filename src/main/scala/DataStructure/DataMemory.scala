package DataStructure

import chisel3._
import Common.{Buses, Constants}

class DataMemory extends Chisel.Module
{
  val io = IO(new Bundle
  {
    val mem = new Buses.dmem2data();
  })

  val memory = Mem(Constants.DATA_MEMORY_SIZE, UInt(Constants.DATA_WIDTH.W))

  //Írásnak nagyobb a prioritása
  when(io.mem.wr === 1.U)
  {
    memory(io.mem.addr) := io.mem.data2mem
  }
  when(io.mem.rd === 1.U && io.mem.wr === 0.U)
  {
    io.mem.mem2data := memory(io.mem.addr)
  }
}
