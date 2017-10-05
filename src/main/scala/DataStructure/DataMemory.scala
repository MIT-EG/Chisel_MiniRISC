package DataStructure

import chisel3._
import Common.{Buses, Constants}

class DataMemory extends Chisel.Module
{
  val io = IO(new Bundle
  {
    val mem_if = new Buses.dmem2data();
  })

  val mem = Mem(Constants.DATA_MEMORY_SIZE, UInt(Constants.DATA_WIDTH.W))

  //Írásnak nagyobb a prioritása
  when(io.mem_if.wr === 1.U)
  {
    mem(io.mem_if.addr) := io.mem_if.data2mem
  }
  when(io.mem_if.rd === 1.U && io.mem_if.wr === 0.U)
  {
    io.mem_if.mem2data := mem(io.mem_if.addr)
  }
}
