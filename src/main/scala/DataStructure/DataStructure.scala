//
package DataStructure

import Common._
import chisel3._
import chisel3.util.{Cat, MuxCase}

class DataStructure extends Chisel.Module
{
  //////////////////////PORT/////////////////////////
  val io = IO(new Bundle
  {
    val data_mem = new Common.data2dmem()

    val ctrl2data = Input(new ctrl2data())

    val data2ctrl = Output(new data2ctrl())
  })

  //Instantiate ALU and REGISTER FILE
  val alu = Module(new ALU())
  val rf = Module(new RegisterFile())

  //////////////////////MUX1/////////////////////////
  //MUX1: inputs: memory data in, alu dout; output: regs data in
  //y = Mux(sel, 1, 0)
  rf.io.din := Mux(io.ctrl2data.mux1sel === 1.U, io.data_mem.mem2data, alu.io.y)

  //////////////////////MUX2/////////////////////////
  //MUX2: inputs: constant from control, regb from regs; output alu op2
  //y = Mux(sel, 1, 0)
  alu.io.b := Mux(io.ctrl2data.mux2sel === 1.U, io.ctrl2data.const, rf.io.rb)

  //////////////////////MUX3/////////////////////////
  //MUX3: inputs: data mem address from control, rb from acc; output data mem.io.address
  //y = Mux(sel, 1, 0)
  io.data_mem.addr := Mux(io.ctrl2data.mux3sel === 1.U, rf.io.rb, io.ctrl2data.mem_addr)

  //////////////////////ALU/////////////////////////

  //ALU: a, op, flag_in, flag_out

  //ALU 'a' bemenete := regiszter tömb 'ra' kimenete
  alu.io.a := rf.io.ra

  //ALU op := ctrl2data.op
  alu.io.op := io.ctrl2data.alu_op

  //ALU flag_in
  alu.io.flag_in := io.ctrl2data.alu_flag

  //ALU flag out
  //TODO: ez nem jó valamiért :/
  //alu.io.flag_out := io.data2ctrl.alu_flag

  //ALU y a control felé
  //TODO kell ez ide? regiszterben tárolt értékre jump van?
  io.data2ctrl.reg_val := alu.io.y

  //////////////////////REGISTER FILE/////////////////////////
  //RF: addr_a, addr_b, we, ra

  //addr_a
  rf.io.addr_A := io.ctrl2data.regs_a

  //addr_b
  rf.io.addr_B := io.ctrl2data.regs_b

  //we
  rf.io.we := io.ctrl2data.regs_we

  //RF ra := data mem din
  io.data_mem.data2mem := rf.io.ra
}
