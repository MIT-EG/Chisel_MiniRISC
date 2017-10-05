

package DataStructure

import Common.Buses.alu_flags
import Common.{ALU_Ops, Constants}
import chisel3._
import chisel3.util.{Cat, MuxCase}

//Module declaration
class ALU extends Chisel.Module
{
  val io = IO( new Bundle
  {
    val a       = Input(UInt(Constants.DATA_WIDTH.W))
    val b       = Input(UInt(Constants.DATA_WIDTH.W))
    val op      = Input(UInt(Constants.ALU_OP_WIDTH.W))
    val flag_in  = Input(new alu_flags)

    val y       = Output(UInt(Constants.DATA_WIDTH.W))
    val flag_out = Output(new alu_flags)
  })

  //***OPERATIONS***

  //*ARITMETICAL*

  //ADD: a + b + carry in; bitkiterjesztéssel
  val add_result = io.a +& io.b +& io.flag_in.carry
  val add_carry = add_result(Constants.DATA_WIDTH)

  //TODO Van beépített overflow operátor: `val flagsOut = flagsIn , tesztelni
  //Vagy a "Functional Abstraction" részben saját overflow számítót definiálni

  val add_ovf = ( ~(io.a(Constants.DATA_WIDTH - 1) ^ io.b(Constants.DATA_WIDTH - 1)) )  ^ add_result(Constants.DATA_WIDTH - 1)
    //(io.a(Constants.DataWidth - 1) & io.b(Constants.DataWidth - 1)) ^ add_result(Constants.DataWidth - 1)
  /*( (io.a(Constants.DataWidth - 1) & io.b(Constants.DataWidth - 1)) & ~(add_result(Constants.DataWidth - 1))) |
    ((~io.a(Constants.DataWidth - 1) & ~io.b(Constants.DataWidth - 1)) & add_result(Constants.DataWidth - 1))*/
    //(io.a(Constants.DataWidth - 1) & io.b(Constants.DataWidth - 1)) ^ add_result(Constants.DataWidth - 1)
    /**/


  val sub_result = io.a -& io.b -& io.flag_in.carry //a - b - carry in; bitkiterjesztéssel
  val sub_carry = !sub_result(Constants.DATA_WIDTH) //carry flag negáltja
  val sub_ovf = (io.a(Constants.DATA_WIDTH - 1) & io.b(Constants.DATA_WIDTH - 1)) ^ add_result(Constants.DATA_WIDTH - 1)

  //*LOGICAL*
  //Nincs flag állítás
  val and_result = io.a & io.b
  val or_result = io.a | io.b
  val xor_result = io.a ^ io.b

  //*SHIFT, ROTATE*
  val rol_result = Cat(io.a(Constants.DATA_WIDTH - 2, 0), io.a(Constants.DATA_WIDTH - 1)) //rotálás balra. '1'010_1010 -> 0101_010'1'

  val ror_result = Cat(io.a(0), io.a(Constants.DATA_WIDTH - 1, 1))  //rotálás jobbra. 1010_101'0' -> '0'101_0101

  val lshl_result = (io.a << 1)  //logikai balra shift(minden bitet shiftel)
  val lshl_carry = io.a(Constants.DATA_WIDTH - 1)

  val lshr_result = (io.a >> 1)  //logikai jobbra shift(minden bitet shiftel)
  val lshr_carry = io.a(0)

  val ashl_result = Cat( io.a(Constants.DATA_WIDTH - 1), io.a(Constants.DATA_WIDTH - 3, 0), 0.U ) //arithmetikai shift, az előjel bit megmarad, LSB-re 0-t shiftel be. 1_010_1010 -> 1_101_0100
  val ashl_carry = io.a(Constants.DATA_WIDTH - 2)

  val ashr_result = Cat( io.a(Constants.DATA_WIDTH - 1), 0.U, io.a(Constants.DATA_WIDTH - 2, 1)) //arithmetika shift, az előjel megmarad, MSB-re 0-t shiftel be, 1_010_1010 -> 1_001_0101
  val ashr_carry = io.a(0)

  //*SWAP*
  val swp_result = Cat( io.a((Constants.DATA_WIDTH / 2) - 1, 0), io.a(Constants.DATA_WIDTH - 1, Constants.DATA_WIDTH / 2))

  //*COMPARE*
  val cmp_zero = io.a === io.b
  val cmp_neg = io.a < io.b


  //Result and flags of the operation
  val result = Wire(UInt((Constants.DATA_WIDTH + 1).W))
  val carry = Wire(Bool())
  val overflow = Wire(Bool())
  val negative = Wire(Bool())
  val zero = Wire(Bool())

  // Result MUX, default: 0.U
   result := MuxCase( 0.U, wrapRefArray(Array(
    (io.op === ALU_Ops.pass.U(Constants.ALU_OP_WIDTH.W)) -> io.b,
    (io.op === ALU_Ops.add.U(Constants.ALU_OP_WIDTH.W)) -> add_result(Constants.DATA_WIDTH - 1, 0),
    (io.op === ALU_Ops.sub.U(Constants.ALU_OP_WIDTH.W)) -> sub_result(Constants.DATA_WIDTH - 1, 0),
    (io.op === ALU_Ops.and.U(Constants.ALU_OP_WIDTH.W)) -> and_result,
    (io.op === ALU_Ops.or.U(Constants.ALU_OP_WIDTH.W)) -> or_result,
    (io.op === ALU_Ops.xor.U(Constants.ALU_OP_WIDTH.W)) -> xor_result,
    (io.op === ALU_Ops.rol.U(Constants.ALU_OP_WIDTH.W)) -> rol_result,
    (io.op === ALU_Ops.ror.U(Constants.ALU_OP_WIDTH.W)) -> ror_result,
    (io.op === ALU_Ops.lshl.U(Constants.ALU_OP_WIDTH.W)) -> lshl_result,
    (io.op === ALU_Ops.lshr.U(Constants.ALU_OP_WIDTH.W)) -> lshr_result,
    (io.op === ALU_Ops.ashl.U(Constants.ALU_OP_WIDTH.W)) -> ashl_result,
    (io.op === ALU_Ops.ashr.U(Constants.ALU_OP_WIDTH.W)) -> ashr_result,
    (io.op === ALU_Ops.swp.U(Constants.ALU_OP_WIDTH.W)) -> swp_result,
    (io.op === ALU_Ops.cmp.U(Constants.ALU_OP_WIDTH.W)) -> 0.U
  )))

    //Carry MUX, default: 0
    carry := MuxCase(0.U, wrapRefArray(Array(
    (io.op === ALU_Ops.add.U(Constants.ALU_OP_WIDTH.W))  -> add_carry,
    (io.op === ALU_Ops.sub.U(Constants.ALU_OP_WIDTH.W))  -> sub_carry,
    (io.op === ALU_Ops.lshl.U(Constants.ALU_OP_WIDTH.W)) -> lshl_carry,
    (io.op === ALU_Ops.lshr.U(Constants.ALU_OP_WIDTH.W)) -> lshr_carry,
    (io.op === ALU_Ops.ashl.U(Constants.ALU_OP_WIDTH.W)) -> ashl_carry,
    (io.op === ALU_Ops.ashr.U(Constants.ALU_OP_WIDTH.W)) -> ashr_carry
  )))

  //Overflow MUX, default: 0
  overflow := MuxCase(0.U, wrapRefArray(Array(
    (io.op === ALU_Ops.add.U(Constants.ALU_OP_WIDTH.W))  -> add_ovf,
    (io.op === ALU_Ops.sub.U(Constants.ALU_OP_WIDTH.W))  -> sub_ovf,
    (io.op === ALU_Ops.cmp.U(Constants.ALU_OP_WIDTH.W))  -> sub_ovf
  )))

  //Zero MUX, default: (result === 0.U)
  zero := MuxCase(( result === 0.U), wrapRefArray(Array(
    (io.op === ALU_Ops.pass.U(Constants.ALU_OP_WIDTH.W)) -> 0.U,
    (io.op === ALU_Ops.cmp.U(Constants.ALU_OP_WIDTH.W))  -> cmp_zero
  )))

  //Negative MUX, default: result(Constants.DataWidth - 1)
  negative := MuxCase( result(Constants.DATA_WIDTH - 1), wrapRefArray(Array(
    (io.op === ALU_Ops.pass.U(Constants.ALU_OP_WIDTH.W)) -> 0.U,
    (io.op === ALU_Ops.cmp.U(Constants.ALU_OP_WIDTH.W))  -> cmp_neg
  )))


  io.y := result
  io.flag_out.carry := carry
  io.flag_out.overflow := overflow
  io.flag_out.zero := zero
  io.flag_out.negative := negative
}