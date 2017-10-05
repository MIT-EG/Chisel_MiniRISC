//
package DataStructure

import Common.Buses
import chisel3._
import chisel3.util.{Cat, MuxCase}

class DataStructure extends Chisel.Module
{
  //////////////////////PORT/////////////////////////
  val io = IO(new Bundle
  {
    val data_mem = new Buses.data2dmem()

    val ctrl2data = Input(new Buses.ctrl2data())

    val data2ctrl = Output(new Buses.data2ctrl())
  })

  val alu = new ALU()
  val rf = new RegisterFile()

  //////////////////////MUX1/////////////////////////
  //MUX1: inputs: memory data in, alu dout; output: regs data in
  val mux1 = new Bool()
  when(io.ctrl2data.mux1sel === 1.U)
  {
    mux1 := io.data_mem.mem2data
  } .otherwise
  {
    mux1 := alu.io.y
  }
  rf.io.din := mux1

  //////////////////////MUX2/////////////////////////
  //MUX2: inputs: constant from control, regb from regs; output alu op2
  val mux2 = new Bool()
  when(io.ctrl2data.mux2sel === 1.U)
  {
    mux2 := io.ctrl2data.const
  } .otherwise
  {
    mux2 := rf.io.rb
  }
  alu.io.b := mux2

  //////////////////////MUX3/////////////////////////
  //MUX3: inputs: data mem address from control, rb from acc; output data mem.io.address
  val mux3 = new Bool()
  when(io.ctrl2data.mux3sel === 1.U)
  {
    mux3 := rf.io.rb
  }   .otherwise
  {
    mux3 := io.ctrl2data.mem_addr
  }
  io.data_mem.addr := mux3

  //////////////////////ALU & REGS/////////////////////////

  alu.io.a := rf.io.ra
  rf.io.ra := io.data_mem.mem2data
  rf.io.we := io.ctrl2data.regs_we
}
