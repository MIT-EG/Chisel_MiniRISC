import Chisel.iotesters.{ChiselFlatSpec, PeekPokeTester}
import Common.MiniRISC

//DataMemoryTest
class MiniRISCTest(c: MiniRISC) extends PeekPokeTester(c)
{
  val mr = c


}
class MiniRISCTester extends ChiselFlatSpec
{
  private val backendNames = Array[String]("firrtl")
  for ( backendName <- backendNames )
  {
    "MiniRisc" should s"make some operations (with $backendName)" in
      {
        chisel3.iotesters.Driver(() => new MiniRISC, backendName)
        {
          c => new MiniRISCTest(c)
        } should be (true)
      }
  }
}
