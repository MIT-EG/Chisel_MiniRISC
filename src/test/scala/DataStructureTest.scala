import Chisel.iotesters.{ChiselFlatSpec, PeekPokeTester}
import Common.{ALU_Ops, Constants}
import DataStructure.DataStructure

import scala.math.pow

private object Tasks
{
  def ld (ds : DataStructure, value : Int, addr : Int)
  {
    if(ds == null)
      throw new IllegalArgumentException("DataStructure is null");
    if(addr > 15 || addr < 0)
      throw new IllegalArgumentException("addr is out of range. Must be between 0 and 15")
    if(value > 255 || value < 0)
      throw new IllegalArgumentException("value is out of range. Must be between 0 and 255")


  }
}

//Fibonacci
class FibonacciTest(c: DataStructure) extends PeekPokeTester(c)
{
  private val ds = c

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


  //add reg_0, reg_1 -> expect 2
  poke(ds.io.ctrl2data.regs_we, 1)
  poke(ds.io.ctrl2data.alu_op, Common.ALU_Ops.add)
  poke(ds.io.ctrl2data.alu_flag.carry, 0)
  poke(ds.io.ctrl2data.mux2sel, 0)  //Alu.b := RegFile.rb
  poke(ds.io.ctrl2data.mux1sel, 0)  //Alu.y := RegFile.din

  poke(ds.io.ctrl2data.regs_a, 0)
  poke(ds.io.ctrl2data.regs_b, 1)
  expect(ds.io.data2ctrl.reg_val, 2)
  step(1)

  expect(ds.io.data2ctrl.reg_val, 3)
  step(1)
  expect(ds.io.data2ctrl.reg_val, 4)
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