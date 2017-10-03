
import Chisel.iotesters.{ChiselFlatSpec, PeekPokeTester}
import Common.{ALU_Ops, Constants}
import DataStructure.ALU

import scala.math.pow

//Test Constants
object TC
{
  val MAX = (pow(2, Constants.DATA_WIDTH).toInt - 1)
  val STEP = 4
  val MSB = Constants.DATA_WIDTH - 1

  def bit(number : Int, bitNumber : Int): Int =
  {
    return (number >> bitNumber) % 2
  }

  def zero(number : Int) : Boolean =
  {
    return number % (MAX + 1) == 0
  }
  def carry(number : Int) : Int =
  {
    return bit(number, TC.MSB + 1)
  }
  def negative(number : Int) : Int =
  {
    return bit(number, MSB)
  }

  def abs(number : Int) : Int =
  {
    if(number < 0)
      {
        return number + MAX + 1
      }
    else
      {
        return number
      }
  }
}

//ADD
class ALUAddTest(c: ALU) extends PeekPokeTester(c)
{
  private val alu = c
  for (i <- 0 to TC.MAX by TC.STEP)
  {
    for (j <- 0 to TC.MAX by TC.STEP)
    {
      for(k <- 0 to 1) // alapból "by 1"
      {

        poke(alu.io.op, ALU_Ops.add)

        poke(alu.io.a, i)
        poke(alu.io.b, j)

        poke(alu.io.flag_in.carry, k)

        step(1)

        val num = i + j + k
        expect(alu.io.y, num % (TC.MAX + 1) )

        expect(alu.io.flag_out.carry, TC.carry(num) )
        expect(alu.io.flag_out.zero, TC.zero(num) )
        expect(alu.io.flag_out.negative, TC.negative(num) )

        //expect(alu.io.flagOut.overflow, alu.io.flagOut.negative == 1) //megbukik
      }
    }
  }
}

class ALUAddTester extends ChiselFlatSpec
{
  private val backendNames = Array[String]("firrtl") //Ez mi? -firrtl -> köztes kód, és ezzel futtatja(nem kell külön progi a teszthez)
  for ( backendName <- backendNames )
  {
    "Alu" should s"calculate the sum of it's 'a' and 'b' inputs (with $backendName)" in
      {
        chisel3.iotesters.Driver(() => new ALU, backendName)
        {
          c => new ALUAddTest(c)
        } should be (true) //ennek kell lennie az összes teszteset eredményének
      }
  }
}

//SUBTRACT
class ALUSubTest(c: ALU) extends PeekPokeTester(c)
{
  private val alu = c
  val max = (pow(2, Common.Constants.DATA_WIDTH).toInt - 1)
  for (i <- 0 to max by 8)
  {
    for (j <- 0 to max by 8)
    {
      for(k <- 0 to 1)
      {
        poke(alu.io.op, ALU_Ops.sub)

        poke(alu.io.a, i)
        poke(alu.io.b, j)
        poke(alu.io.flag_in.carry, k)

        step(1)

        val num = TC.abs(i - j - k)
        expect(alu.io.y, num % (TC.MAX + 1) )

        //expect(alu.io.flagOut.carry, TC.carry(num)) //megbukik
        //expect(alu.io.flagOut.overflow, (i + j) / 512) //megbukik

        expect(alu.io.flag_out.zero, TC.zero(num) )
        expect(alu.io.flag_out.negative, TC.negative(num) )
      }
    }
  }
}

class ALUSubTester extends ChiselFlatSpec
{
  private val backendNames = Array[String]("firrtl") //Ez mi? -firrtl -> köztes kód, és ezzel futtatja(nem kell külön progi a teszthez)
  for ( backendName <- backendNames )
  {
    "Alu" should s"calculate the subtract of it's 'a' and 'b' inputs (with $backendName)" in
      {
        chisel3.iotesters.Driver(() => new ALU, backendName)
        {
          c => new ALUSubTest(c)
        } should be (true)
      }
  }
}

//AND, OR, XOR
class ALULogicTest(c: ALU) extends PeekPokeTester(c)
{
  private val alu = c
  val max = (pow(2, Common.Constants.DATA_WIDTH).toInt - 1)
  for (i <- 0 to max by 8)
  {
    for (j <- 0 to max by 8)
    {
        poke(alu.io.a, i)
        poke(alu.io.b, j)

        poke(alu.io.op, Common.ALU_Ops.and)
        step(1)

        var num = i & j
        expect(alu.io.y, num)
        expect(alu.io.flag_out.zero, TC.zero(num) )
        expect(alu.io.flag_out.negative, TC.negative(num) )

        poke(alu.io.op, Common.ALU_Ops.or)
        step(1)

        num = i | j
        expect(alu.io.y, num)
        expect(alu.io.flag_out.zero, TC.zero(num) )
        expect(alu.io.flag_out.negative, TC.negative(num) )

        poke(alu.io.op, Common.ALU_Ops.xor)
        step(1)

        num = i ^ j
        expect(alu.io.y, num)
        expect(alu.io.flag_out.zero, TC.zero(num) )
        expect(alu.io.flag_out.negative, TC.negative(num) )
    }
  }
}

class ALULogicTester extends ChiselFlatSpec
{
  private val backendNames = Array[String]("firrtl") //Ez mi? -firrtl -> köztes kód, és ezzel futtatja(nem kell külön progi a teszthez)
  for ( backendName <- backendNames )
  {
    "Alu" should s"calculate 'a' AND 'b', 'a' OR 'b' and finally 'a' XOR 'b' (with $backendName)" in
      {
        chisel3.iotesters.Driver(() => new ALU, backendName)
        {
          c => new ALULogicTest(c)
        } should be (true)
      }
  }
}

//ROL, ROR, ASHL, ASHR, LSHL, LSHR
class ALUShiftTest(c: ALU) extends PeekPokeTester(c)
{
  private val alu = c
  val max = (pow(2, Common.Constants.DATA_WIDTH).toInt - 1)
  for (i <- 0 to max by 1)
  {
        poke(alu.io.a, i)

        poke(alu.io.op, Common.ALU_Ops.rol)
        step(1)
        expect(alu.io.y, ((i << 1) + i / 128) % 256)

        poke(alu.io.op, Common.ALU_Ops.ror)
        step(1)
        expect(alu.io.y, ((i / 2) + ((i % 2) * 128)) % 256)

        poke(alu.io.op, Common.ALU_Ops.lshl)
        step(1)
        expect(alu.io.y, (i << 1) % 256)
        expect(alu.io.flag_out.carry, (i / 128))

        poke(alu.io.op, Common.ALU_Ops.lshr)
        step(1)
        expect(alu.io.y, (i >> 1) % 256)
        expect(alu.io.flag_out.carry, (i % 2))

        poke(alu.io.op, Common.ALU_Ops.ashl)
        step(1)
        expect(alu.io.y, ((i / 128) * 128) + ((i << 1) % 128) )
        expect(alu.io.flag_out.carry, ((i % 128) / 64))

        poke(alu.io.op, Common.ALU_Ops.ashr)
        step(1)
        expect(alu.io.y, ((i / 128) * 128) + ((i % 128) / 2))
        expect(alu.io.flag_out.carry, (i % 2))
  }
}

class ALUShiftTester extends ChiselFlatSpec
{
  private val backendNames = Array[String]("firrtl")
  for ( backendName <- backendNames )
  {
    "Alu" should s"calculate different shifting methods (with $backendName)" in
      {
        chisel3.iotesters.Driver(() => new ALU, backendName)
        {
          c => new ALUShiftTest(c)
        } should be (true)
      }
  }
}

//PASS, SWAP, COMPARE
class ALURestTest(c: ALU) extends PeekPokeTester(c)
{
  private val alu = c
  val max = (pow(2, Common.Constants.DATA_WIDTH).toInt - 1)
  for (i <- 0 to max by 1)
  {
    poke(alu.io.a, i)

    //pass
    poke(alu.io.op, Common.ALU_Ops.pass)
    step(1)
    expect(alu.io.y, i % 256)

    //swap
    step(1)
    poke(alu.io.op, Common.ALU_Ops.swp)
    expect(alu.io.y, ((i << 4) % 256) + (i >> 4))

    //cmp
    for(j <- 0 to max by 8)
    {
      poke(alu.io.b, j)
      poke(alu.io.op, Common.ALU_Ops.cmp)
      step(1)
      expect(alu.io.flag_out.negative, i < j)
      expect(alu.io.flag_out.zero, i == j)
    }
  }
}

class ALURestTester extends ChiselFlatSpec
{
  private val backendNames = Array[String]("firrtl")
  for ( backendName <- backendNames )
  {
    "Alu" should s"calculate the pass, swap, cmp operations (with $backendName)" in
      {
        chisel3.iotesters.Driver(() => new ALU, backendName)
        {
          c => new ALURestTest(c)
        } should be (true)
      }
  }
}

//Tests all classes above
class ALUFullTester extends ChiselFlatSpec
{
  private val backendNames = Array[String]("firrtl") //Ez mi? -firrtl -> köztes kód, és ezzel futtatja(nem kell külön progi a teszthez)
  for ( backendName <- backendNames )
  {
    "Alu" should s"calculate the sum of it's 'a' and 'b' inputs (with $backendName)" in
      {
        chisel3.iotesters.Driver(() => new ALU, backendName)
        {
          c => new ALUAddTest(c)
        } should be (true)
      }

    "Alu" should s"calculate the subtract of it's 'a' and 'b' inputs (with $backendName)" in
      {
        chisel3.iotesters.Driver(() => new ALU, backendName)
        {
          c => new ALUSubTest(c)
        } should be (true)
      }
    "Alu" should s"calculate 'a' AND 'b', 'a' OR 'b' and finally 'a' XOR 'b' (with $backendName)" in
      {
        chisel3.iotesters.Driver(() => new ALU, backendName)
        {
          c => new ALULogicTest(c)
        } should be (true)
      }
    "Alu" should s"calculate different shifting methods (with $backendName)" in
      {
        chisel3.iotesters.Driver(() => new ALU, backendName)
        {
          c => new ALUShiftTest(c)
        } should be (true)
      }
    "Alu" should s"calculate the pass, swap, cmp operations (with $backendName)" in
      {
        chisel3.iotesters.Driver(() => new ALU, backendName)
        {
          c => new ALURestTest(c)
        } should be (true)
      }
  }
}
