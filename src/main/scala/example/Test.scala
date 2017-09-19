//Testing Chisel features
package example

import chisel3._
import chisel3.core.withClock
import chisel3.util.MuxCase

class Test extends Chisel.Module
{
  val io = IO(new Bundle
  {
    val clk = Input(Clock())
    val rst = Input(Bits(1.W))
    val enable = Input(Bits(1.W))

    val sig = Output(Bits(1.W))
  })

  val a, b = Wire(UInt(1.W))
  val s = Wire(UInt(1.W))
  val cout = Wire(UInt(1.W))

  cout := a & b
  s := a ^ b

  io.sig := s
}
