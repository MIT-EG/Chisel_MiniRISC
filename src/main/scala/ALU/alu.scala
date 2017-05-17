// See LICENSE for license details.

package ALU

import chisel3._
import chisel3.util.{Cat, MuxCase}

//Flag bus declaration
class Flags extends Bundle
{
  val carry = Bool() // UInt(1.W)
  val negative = Bool()
  val overflow = Bool()
  val zero = Bool()
}

//Module declaration
class Alu extends Chisel.Module
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

  //***OPERATIONS***

  //*ARITMETICAL*

  //ADD: a + b + carry in; bitkiterjesztéssel
  val add_result = io.a +& io.b +& io.flagIn.carry
  val add_carry = add_result(Constants.DataWidth)

  //TODO Van beépített overflow operátor: `val flagsOut = flagsIn , tesztelni
  //Vagy a "Functional Abstraction" részben saját overflow számítót definiálni

  val add_ovf = ( ~(io.a(Constants.DataWidth - 1) ^ io.b(Constants.DataWidth - 1)) ) ^ add_result(Constants.DataWidth - 1)
    //(io.a(Constants.DataWidth - 1) & io.b(Constants.DataWidth - 1)) ^ add_result(Constants.DataWidth - 1)
  /*( (io.a(Constants.DataWidth - 1) & io.b(Constants.DataWidth - 1)) & ~(add_result(Constants.DataWidth - 1))) |
    ((~io.a(Constants.DataWidth - 1) & ~io.b(Constants.DataWidth - 1)) & add_result(Constants.DataWidth - 1))*/
    //(io.a(Constants.DataWidth - 1) & io.b(Constants.DataWidth - 1)) ^ add_result(Constants.DataWidth - 1)
    /**/


  val sub_result = io.a -& io.b -& io.flagIn.carry //a - b - carry in; bitkiterjesztéssel
  val sub_carry = !sub_result(Constants.DataWidth) //carry flag negáltja
  val sub_ovf = (io.a(Constants.DataWidth - 1) & io.b(Constants.DataWidth - 1)) ^ add_result(Constants.DataWidth - 1)

  //*LOGICAL*
  //Nincs flag állítás
  val and_result = io.a & io.b
  val or_result = io.a | io.b
  val xor_result = io.a ^ io.b

  //*SHIFT, ROTATE*
  val rol_result = Cat(io.a(Constants.DataWidth - 2, 0), io.a(Constants.DataWidth - 1)) //rotálás balra. '1'010_1010 -> 0101_010'1'

  val ror_result = Cat(io.a(0), io.a(Constants.DataWidth - 1, 1))  //rotálás jobbra. 1010_101'0' -> '0'101_0101

  val lshl_result = (io.a << 1)  //logikai balra shift(minden bitet shiftel)
  val lshl_carry = io.a(Constants.DataWidth - 1)

  val lshr_result = (io.a >> 1)  //logikai jobbra shift(minden bitet shiftel)
  val lshr_carry = io.a(0)

  val ashl_result = Cat( io.a(Constants.DataWidth - 1), io.a(Constants.DataWidth - 3, 0), 0.U ) //arithmetikai shift, az előjel bit megmarad, LSB-re 0-t shiftel be. 1_010_1010 -> 1_101_0100
  val ashl_carry = io.a(Constants.DataWidth - 2)

  val ashr_result = Cat( io.a(Constants.DataWidth - 1), 0.U, io.a(Constants.DataWidth - 2, 1)) //arithmetika shift, az előjel megmarad, MSB-re 0-t shiftel be, 1_010_1010 -> 1_001_0101
  val ashr_carry = io.a(0)

  //*SWAP*
  val swp_result = Cat( io.a((Constants.DataWidth / 2) - 1, 0), io.a(Constants.DataWidth - 1, Constants.DataWidth / 2))

  //*COMPARE*
  val cmp_zero = io.a === io.b
  val cmp_neg = io.a < io.b


  //Result and flags of the operation
  val result = Wire(UInt((Constants.DataWidth + 1).W))
  val carry = Wire(Bool())
  val overflow = Wire(Bool())
  val negative = Wire(Bool())
  val zero = Wire(Bool())

  // Result MUX, default: 0.U
   result := MuxCase( 0.U, wrapRefArray(Array(
    (io.op === Operations.pass.U(Constants.OperationWidth.W)) -> io.a,
    (io.op === Operations.add.U(Constants.OperationWidth.W)) -> add_result(Constants.DataWidth - 1, 0),
    (io.op === Operations.sub.U(Constants.OperationWidth.W)) -> sub_result(Constants.DataWidth - 1, 0),
    (io.op === Operations.and.U(Constants.OperationWidth.W)) -> and_result,
    (io.op === Operations.or.U(Constants.OperationWidth.W)) -> or_result,
    (io.op === Operations.xor.U(Constants.OperationWidth.W)) -> xor_result,
    (io.op === Operations.rol.U(Constants.OperationWidth.W)) -> rol_result,
    (io.op === Operations.ror.U(Constants.OperationWidth.W)) -> ror_result,
    (io.op === Operations.lshl.U(Constants.OperationWidth.W)) -> lshl_result,
    (io.op === Operations.lshr.U(Constants.OperationWidth.W)) -> lshr_result,
    (io.op === Operations.ashl.U(Constants.OperationWidth.W)) -> ashl_result,
    (io.op === Operations.ashr.U(Constants.OperationWidth.W)) -> ashr_result,
    (io.op === Operations.swp.U(Constants.OperationWidth.W)) -> swp_result,
    (io.op === Operations.cmp.U(Constants.OperationWidth.W)) -> 0.U
  )))

    //Carry MUX, default: 0
    carry := MuxCase(0.U, wrapRefArray(Array(
    (io.op === Operations.add.U(Constants.OperationWidth.W))  -> add_carry,
    (io.op === Operations.sub.U(Constants.OperationWidth.W))  -> sub_carry,
    (io.op === Operations.lshl.U(Constants.OperationWidth.W)) -> lshl_carry,
    (io.op === Operations.lshr.U(Constants.OperationWidth.W)) -> lshr_carry,
    (io.op === Operations.ashl.U(Constants.OperationWidth.W)) -> ashl_carry,
    (io.op === Operations.ashr.U(Constants.OperationWidth.W)) -> ashr_carry
  )))

  //Overflow MUX, default: 0
  overflow := MuxCase(0.U, wrapRefArray(Array(
    (io.op === Operations.add.U(Constants.OperationWidth.W))  -> add_ovf,
    (io.op === Operations.sub.U(Constants.OperationWidth.W))  -> sub_ovf,
    (io.op === Operations.cmp.U(Constants.OperationWidth.W))  -> sub_ovf
  )))

  //Zero MUX, default: (result === 0.U)
  zero := MuxCase(( result === 0.U), wrapRefArray(Array(
    (io.op === Operations.pass.U(Constants.OperationWidth.W)) -> 0.U,
    (io.op === Operations.cmp.U(Constants.OperationWidth.W))  -> cmp_zero
  )))

  //Negative MUX, default: result(Constants.DataWidth - 1)
  negative := MuxCase( result(Constants.DataWidth - 1), wrapRefArray(Array(
    (io.op === Operations.pass.U(Constants.OperationWidth.W)) -> 0.U,
    (io.op === Operations.cmp.U(Constants.OperationWidth.W))  -> cmp_neg
  )))


  io.y := result
  io.flagOut.carry := carry
  io.flagOut.overflow := overflow
  io.flagOut.zero := zero
  io.flagOut.negative := negative
}