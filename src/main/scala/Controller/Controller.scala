package Controller

import chisel3._

class Controller extends Chisel.Module
{
  //////////////////////PORT/////////////////////////
  val io = IO( new Bundle
  {
    val data2ctrl = Output(new Common.data2ctrl)

    val ctrl2data = Input(new Common.ctrl2data)

    val ctrl2pmem = new Common.ctrl2pmem
  })

  //////////////////////INSTANTIATE/////////////////////////
  val pc = Module(new Controller.PC())
  val fsm = Module(new Controller.FSM())
  val stack = Module(new Controller.Stack())

  //////////////////////PC/////////////////////////
  io.ctrl2pmem.addr := pc.io.pc

  pc.io.ret_addr := stack.io.pc_out

  //...

  //////////////////////FSM/////////////////////////

  //IR: Instruction Register
  val IR = Reg(UInt(Common.Constants.INSTRUCTION_WIDTH), io.ctrl2pmem.data, 0.U) //datatype, next value, initial value
  fsm.io.inst := IR

  io.ctrl2data := fsm.io.ctrl

  //FSM - Stack
  stack.io.pop := fsm.io.stack_pop
  stack.io.push := fsm.io.stack_push

  //FSM - PC
  pc.io.inc := fsm.io.pc_inc
  pc.io.jmp := fsm.io.pc_jmp
  pc.io.ret := fsm.io.pc_ret
  pc.io.jmp_addr := fsm.io.pc_jmp_addr

  //////////////////////STACK/////////////////////////

  stack.io.pc_in := pc.io.pc

  //////////////////////PROGRAM MEMORY/////////////////////////
}
