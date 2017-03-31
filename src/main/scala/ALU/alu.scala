// See LICENSE for license details.

package ALU

import chisel3._
import chisel3.util.{Cat, Fill, MuxCase}

//Flag bus declaration
class Flags extends Bundle
{
  val carry = SInt(1.W) //Bool()
  val negative = Bool()
  val overflow = Bool()
  val zero = Bool()
}

//Module declaration
class Alu extends Module
{
  val io = IO( new Bundle
  {
    val a       = Input(SInt(Constants.DataWidth.W))
    val b       = Input(SInt(Constants.DataWidth.W))
    val op      = Input(UInt(Constants.OperationWidth.W))
    val flagIn  = Input(new Flags)

    val y       = Output(SInt(Constants.DataWidth.W))
    val flagOut = Output(new Flags)
  })

  //***OPERATIONS***
  val add = io.a +& io.b +& io.flagIn.carry //a + b + carry in; bitkiterjesztéssel

  //Kivonás esetén a kimenő carry bitet negáljuk???
  val sub = io.a -& io.b -& io.flagIn.carry //a - b - carry in; bitkiterjesztéssel

  val and = io.a & io.b
  val or = io.a | io.b
  val xor = io.a ^ io.b

  val rol = Cat(io.a(Constants.DataWidth - 2, 0), io.a(Constants.DataWidth - 1)) //rotálás balra. '1'010_1010 -> 0101_010'1'
  val ror = Cat(io.a(0), io.a(Constants.DataWidth - 1, 1))  //rotálás jobbra. 1010_101'0' -> '0'101_0101

  val lshl = (io.a << 1)  //logikai balra shift(minden bitet shiftel)
  val lshr = (io.a >> 1)  //logikai jobbra shift(minden bitet shiftel)

  val ashl = Cat( io.a(Constants.DataWidth - 1), io.a(Constants.DataWidth - 3, 0), 0.U ) //arithmetikai shift, az előjel bit megmarad, LSB-re 0-t shiftel be. 1_010_1010 -> 1_101_0100
  val ashr = Cat( io.a(Constants.DataWidth - 1), 0.U, io.a(Constants.DataWidth - 2, 1)) //arithmetika shift, az előjel megmarad, MSB-re 0-t shiftel be, 1_010_1010 -> 1_001_0101

  val swp = Cat( io.a((Constants.DataWidth / 2) - 1, 0), io.a(Constants.DataWidth - 1, Constants.DataWidth / 2))

  //val cmp = Kivonás eredményét használhatom??? a flagekhez ugyanaz kell, de a kimenet más
  //***


  val result = SInt((Constants.DataWidth + 1).W)

// MuxCase szerkezettel, default: io.a
   result := MuxCase(io.a, wrapRefArray(Array(
    (io.op === Operations.pass.U(Constants.OperationWidth.W)) -> io.a,
    (io.op === Operations.add.U(Constants.OperationWidth.W)) -> add(Constants.DataWidth - 1, 0),
    (io.op === Operations.sub.U(Constants.OperationWidth.W)) -> sub(Constants.DataWidth - 1, 0),
    (io.op === Operations.and.U(Constants.OperationWidth.W)) -> and,
    (io.op === Operations.or.U(Constants.OperationWidth.W)) -> or,
    (io.op === Operations.xor.U(Constants.OperationWidth.W)) -> xor,
    (io.op === Operations.lshl.U(Constants.OperationWidth.W)) -> lshl,
    (io.op === Operations.lshr.U(Constants.OperationWidth.W)) -> lshr,
    (io.op === Operations.ashl.U(Constants.OperationWidth.W)) -> ashl,
    (io.op === Operations.ashr.U(Constants.OperationWidth.W)) -> ashr,
    (io.op === Operations.swp.U(Constants.OperationWidth.W)) -> swp,
    (io.op === Operations.cmp.U(Constants.OperationWidth.W)) -> sub //???
  )))

  io.y := result(Constants.DataWidth - 1, 0)
  io.flagOut.carry := result(Constants.DataWidth)
  io.flagOut.zero := result(Constants.DataWidth - 1, 0).orR //OR bitredukció, megnézi
  io.flagOut.negative := result(Constants.DataWidth - 1)
    //io.flagOut.overflow -> Bemeneti operandusok azonos előjelűek és az aritmetikai művelet(add / sub) eredménye különbözik ettől
}



/*
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
*/