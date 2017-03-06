// See LICENSE for license details.

package example

import chisel3._

//Flag bus declaration
class Flags extends Bundle
{
  val carry = Bool()
  val negative = Bool()
  val overflow = Bool()
  val zero = Bool()
}

//Module declaration
class ALU extends Module
{
  val io = IO( new Bundle
  {
    val a       = Input(UInt(Constants.DataWidth.W))
    val b       = Input(UInt(Constants.DataWidth.W))
    val op      = Input(UInt(Constants.OperationWidth.W))
    val flagIn  = Input(new Flags)

    val y       = Output(UInt(Constants.DataWidth.W))
    val flagOut = Output(new Flags)
  })

  val add = new ADD{io.a := io.a} // ilyen nincs
  add.io.a := io.a
}

abstract class AluIo extends Module { //inkább bundle csak
  val io = IO(new Bundle {
    val a = Input(UInt(Constants.DataWidth.W))
    val b = Input(UInt(Constants.DataWidth.W))
    val y = Output(UInt(Constants.DataWidth.W))

    val FlagIn = Input(new Flags)
    val FlagOut = Output(new Flags)
  })
}

//Adder
class ADD extends Module
{
  val io = IO( new Bundle
  {
    val a = Input(UInt(Constants.DataWidth.W))
    val b = Input(UInt(Constants.DataWidth.W))
    val y = Output(UInt(Constants.DataWidth.W))

    val FlagIn = Input(new Flags)
    val FlagOut = Output(new Flags)
  })

  val x = io.a +& io.b +& io.FlagIn.carry     //Add with width expansion

  io.FlagOut.carry := x(Constants.DataWidth)  //MSB : carry
  io.y := x(Constants.DataWidth - 1, 0)       //others : sum
  //overflow?? negative?? zero??
}

//Subtract
class SUB extends Module
{
  val io = IO(new Bundle
  {
    val a = Input(UInt(Constants.DataWidth.W))
    val b = Input(UInt(Constants.DataWidth.W))
    val y = Output(UInt(Constants.DataWidth.W))

    val FlagIn = Input(new Flags)
    val FlagOut = Output(new Flags)
  })

  io.y := io.a - io.b
  //Flag?
}


class SHL extends AluIo
{
  io.y := (io.a << 1)
  io.FlagOut.carry := io.a(Constants.DataWidth - 1)
}

class SHR extends AluIo
{
  io.y := (io.a >> 1)
  io.FlagOut.carry := io.a(0) //LSB a carry bitbe kerül?
}