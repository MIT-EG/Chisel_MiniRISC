import Chisel.iotesters.{ChiselFlatSpec, PeekPokeTester}
import DataStructure.DataStructure

//Fibonacci
class FibonacciTest(c: DataStructure) extends PeekPokeTester(c)
{
  private val ds = c

  //////////////////////INITIALISE/////////////////////////
  //Load 1 to reg_0:
  poke(ds.io.ctrl2data.alu_op, Common.ALU_Ops.pass)
  poke(ds.io.ctrl2data.mux1sel, 0)  //Alu.y := RegFile.din
  poke(ds.io.ctrl2data.mux2sel, 1)  //const := alu.b
  poke(ds.io.ctrl2data.const, 1)
  poke(ds.io.ctrl2data.regs_a, 0)
  poke(ds.io.ctrl2data.regs_we, 1)  //RegFile(0) = 1
  step(1)
  expect(ds.io.data2ctrl.reg_val, 1)
  step(1)

  //Load 1 to reg_1:
  poke(ds.io.ctrl2data.alu_op, Common.ALU_Ops.pass)
  poke(ds.io.ctrl2data.mux1sel, 0)
  poke(ds.io.ctrl2data.mux2sel, 1)
  poke(ds.io.ctrl2data.const, 1)
  poke(ds.io.ctrl2data.regs_a, 1)
  poke(ds.io.ctrl2data.regs_we, 1)  //RegFile(1) = 1
  step(1)
  expect(ds.io.data2ctrl.reg_val, 1)
  step(1)

  //////////////////////ADD/////////////////////////
  //Set wire for ADD operation
  poke(ds.io.ctrl2data.regs_we, 1)
  poke(ds.io.ctrl2data.alu_op, Common.ALU_Ops.add)
  poke(ds.io.ctrl2data.alu_flag.carry, 0)

  poke(ds.io.ctrl2data.mux2sel, 0)  //Alu.b := RegFile.rb
  poke(ds.io.ctrl2data.mux1sel, 0)  //Alu.y := RegFile.din

  //Set values to compute addresses and fibonacci values
  var addr_a = 1
  var addr_b = 0
  var a = 1
  var b = 1
  var sum = 0

  //Cyclic
  for (i <- 0 to 32 by 1)
  {
    //Compute fibonacci value and addresses
    sum = (a + b) % 256
    b = a
    a = sum

    if (addr_a == 1)
      addr_a = 0
    else
      addr_a = 1

    if (addr_b == 1)
      addr_b = 0
    else
      addr_b = 1

    //Set RegisterFile addresses, expect the sum of the 2 register values
    poke(ds.io.ctrl2data.regs_a, addr_a)
    poke(ds.io.ctrl2data.regs_b, addr_b)
    expect(ds.io.data2ctrl.reg_val, sum)

    step(1)
  }
}

class FibonacciTester extends ChiselFlatSpec
{
  private val backendNames = Array[String]("firrtl")
  for ( backendName <- backendNames )
  {
    "DataStructure" should s"calculate the fibonacci values (with $backendName)" in
      {
        chisel3.iotesters.Driver(() => new DataStructure, backendName)
        {
          c => new FibonacciTest(c)
        } should be (true)
      }
  }
}

//DataMemoryTest
class DataMemoryTest(c: DataStructure) extends PeekPokeTester(c)
{
  private val ds = c

  //////////////////////WRITE/////////////////////////
  poke(ds.io.ctrl2data.alu_op, Common.ALU_Ops.pass) //PASS operation
  poke(ds.io.ctrl2data.regs_a, 0)                   //RegisterFile(0)
  poke(ds.io.ctrl2data.regs_b, 0)                   //RegisterFile(0)
  poke(ds.io.ctrl2data.regs_we, 1)
  poke(ds.io.ctrl2data.mem_wr, 1)                   //Write memory -> 1
  poke(ds.io.ctrl2data.mem_rd, 0)                   //Read memory -> 0
  poke(ds.io.ctrl2data.mux1sel, 0)                  //MUX1: RF(din) := ALU y
  poke(ds.io.ctrl2data.mux2sel, 1)                  //MUX2: Constant
  poke(ds.io.ctrl2data.mux3sel, 0)                  //MUX3: Address from control bus

  //i-edik helyre betöltöm i-t
  for (i <- 0 to Common.Constants.DATA_MEMORY_SIZE - 1 by 1)
  {
    poke(ds.io.ctrl2data.mem_addr, i)
    poke(ds.io.ctrl2data.const, i)
    step(1)
    expect(ds.io.data2ctrl.reg_val, i)
  }

  //////////////////////READ/////////////////////////
  poke(ds.io.ctrl2data.alu_op, Common.ALU_Ops.pass) //PASS operation
  poke(ds.io.ctrl2data.regs_a, 0)                   //RegisterFile(0)
  poke(ds.io.ctrl2data.regs_b, 0)                   //RegisterFile(0)
  poke(ds.io.ctrl2data.mem_wr, 0)                   //Write memory -> 0
  poke(ds.io.ctrl2data.mem_rd, 1)                   //Read memory -> 1
  poke(ds.io.ctrl2data.mux1sel, 1)                  //MUX1: RF(din) := DataMemory Dout
  poke(ds.io.ctrl2data.mux2sel, 0)                  //MUX2: ALU(b) := RF(rb)
  poke(ds.io.ctrl2data.mux3sel, 0)                  //MUX3: Address from control bus
  poke(ds.io.ctrl2data.const, 0)

  //i-edik helyre betöltöm i-t
  for (i <- 0 to Common.Constants.DATA_MEMORY_SIZE - 1 by 1)
  {
    poke(ds.io.ctrl2data.mem_addr, i)
    step(1)
    expect(ds.io.data2ctrl.reg_val, i)
  }
}
class DataMemoryTester extends ChiselFlatSpec
{
  private val backendNames = Array[String]("firrtl")
  for ( backendName <- backendNames )
  {
    "DataStructure" should s"make some memory operations (with $backendName)" in
      {
        chisel3.iotesters.Driver(() => new DataStructure, backendName)
        {
          c => new DataMemoryTest(c)
        } should be (true)
      }
  }
}