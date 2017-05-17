// See LICENSE for license details.

package example

import chisel3._

class SPI extends Chisel.Module
{
  val io = IO(new Bundle
  {
    val ss = Output(UInt(4.W))
    val sck = Output(UInt(1.W))
    val miso = Input(UInt(8.W))
    val mosi = Output(UInt(8.W))
  })

  //Belső működés
}

class GCD extends Chisel.Module
{
  val io = IO( new Bundle
  {
    val a  = Input(UInt(16.W))
    val b  = Input(UInt(16.W))
    val e  = Input(Bool())
    val z  = Output(UInt(16.W))
    val v  = Output(Bool())
  })

  val x  = Reg(UInt())
  val y  = Reg(UInt())

  //test

  //end test

  when (x > y) { x := x - y }
    .otherwise { y := y - x }

  when (io.e) { x := io.a; y := io.b }
  io.z := x
  io.v := y === 0.U
}
