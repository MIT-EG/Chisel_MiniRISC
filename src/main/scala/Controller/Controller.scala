package Controller

import chisel3._

class Controller extends Chisel.Module
{
  //////////////////////PORT/////////////////////////
  val io = IO( new Bundle
  {
    val data2ctrl = Input(new Common.data2ctrl)

    val ctrl2data = Output(new Common.ctrl2data)

    val ctrl2pmem = Output(new Common.ctrl2pmem)

    val pmem2ctrl = Input(new Common.pmem2ctrl)
  })

  //////////////////////INSTANTIATE/////////////////////////
  val pc = Module(new Controller.PC())
  val fsm = Module(new Controller.FSM())
  val stack = Module(new Controller.Stack())

  //////////////////////FSM/////////////////////////

  //TODO: Ez kell ide? FSM - Fetch is ezt csinálja
  //IR: Instruction Register
  val IR = Reg(UInt(Common.Constants.INSTRUCTION_WIDTH.W), io.ctrl2pmem.addr, 0.U) //datatype, next value, initial value
  fsm.io.inst := IR

  io.ctrl2data := fsm.io.ctrl

  //DataStructure to Control
  fsm.io.alu_y := io.data2ctrl.reg_val

  //FSM - Stack
  stack.io.pop := fsm.io.stack_pop
  stack.io.push := fsm.io.stack_push

  //FSM - PC
  pc.io.inc := fsm.io.pc_inc
  pc.io.jmp := fsm.io.pc_jmp
  pc.io.ret := fsm.io.pc_ret
  pc.io.jmp_addr := fsm.io.pc_jmp_addr

  //////////////////////PC/////////////////////////
  io.ctrl2pmem.addr := pc.io.pc

  pc.io.ret_addr := stack.io.pc_out.pc

  //////////////////////STACK/////////////////////////

  stack.io.pc_in.pc := pc.io.pc

  //////////////////////PROGRAM MEMORY/////////////////////////

  //////////////////////FLAGS/////////////////////////
  val IE = RegInit(Bool(), 0.U)
  val IF = RegInit(Bool(), 0.U)
  val Z = RegInit(Bool(), 0.U)
  val C = RegInit(Bool(), 0.U)
  val N = RegInit(Bool(), 0.U)
  val V = RegInit(Bool(), 0.U)
  when (fsm.io.valid === 1.U)
  {
    IE := fsm.io.IE
    IF := fsm.io.IF
    Z := io.data2ctrl.alu_flag.zero
    C := io.data2ctrl.alu_flag.carry
    N := io.data2ctrl.alu_flag.negative
    V := io.data2ctrl.alu_flag.overflow
  }

  fsm.io.flag_in.zero := Z
  fsm.io.flag_in.carry := C
  fsm.io.flag_in.negative := N
  fsm.io.flag_in.overflow := V
}
