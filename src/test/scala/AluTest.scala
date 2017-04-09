
import Chisel.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import ALU.Alu
import scala.math.pow

class ALUUnitTester(c: Alu) extends PeekPokeTester(c)
{
  private val alu = c
  val max = pow(2, ALU.Constants.DataWidth).toInt
  for (i <- 1 to max by 1)
  {
    for (j <- 1 to max by 1)
    {
      poke(alu.io.op, ALU.Operations.add)

      poke(alu.io.a, i)
      poke(alu.io.b, j)
      step(1)
      expect(alu.io.y, i + j)
    }
  }
}

class ALUTester extends ChiselFlatSpec
{
  private val backendNames = Array[String]("firrtl") //Ez mi?
  for ( backendName <- backendNames )
  {
    "Alu" should s"calculate the sum of it's 'a' and 'b' inputs (with $backendName)" in
      {
      Driver(() => new Alu, backendName)
      {
        c => new ALUUnitTester(c)
      } should be (true)
    }
  }
}

