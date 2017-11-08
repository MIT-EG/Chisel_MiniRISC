package Controller

import Chisel.Enum
import Common.{ALU_Ops, Constants, ctrl2data}
import chisel3._
import chisel3.util._
import Controller.OpCodes._

class FSM extends Chisel.Module
{
//  object FSM extends Enumeration
//  {
//    val FETCH = Value
//    val DECODE = Value
//    val EXECUTE = Value
//  }

  //chisel3 fsm
  val io = IO( new Bundle
  {
    //Input Instruction
    val inst = Input(UInt(Constants.INSTRUCTION_WIDTH.W))

    //Output to the DataStructure
    val ctrl = Output(new ctrl2data)


    //Interrupt Enable and Interrupt Flag bits
    val IE = Output(Bool())
    val IF = Output(Bool())

    //Stack control signals
    val stack_push = Output(Bool())
    val stack_pop = Output(Bool())

    //PC control signals
    val pc_inc = Output(Bool())
    val pc_jmp = Output(Bool())
    val pc_ret = Output(Bool())
    val pc_jmp_addr = Output((UInt(log2Ceil(Constants.PROGRAM_MEMORY_SIZE).W)))
  })

  //TODO: ez nem jó -> hogy kell állapotgépet csinálni???
  val sFSM :: sFETCH :: sDECODE :: sEXECUTE = Enum(3)
  val fsm = RegInit(sFSM, sFETCH)

  val inst_reg = Reg(UInt(Constants.INSTRUCTION_WIDTH.W))

  val ctrl_signals = new ctrl2data

  val D = io.inst(Constants.INSTRUCTION_WIDTH - 1, Constants.INSTRUCTION_WIDTH - 4)   //15 downto 12
  val C = io.inst(Constants.INSTRUCTION_WIDTH - 5, Constants.INSTRUCTION_WIDTH - 8)   //11 downto 8
  val B = io.inst(Constants.INSTRUCTION_WIDTH - 9, Constants.INSTRUCTION_WIDTH - 12)  //7 downto 4
  val A = io.inst(Constants.INSTRUCTION_WIDTH - 13, Constants.INSTRUCTION_WIDTH - 16) //3 downto 0

  switch (fsm)
  {
    is (sFETCH)
    {
      inst_reg := io.inst
      //TODO: Itt mit csináljon az DATASTRUCTURE?? alu pass és registerfile írásengedélyezés tiltása?
    }
    is (sDECODE)
    {
      /////////////////////////////////////////////////////////////////////////////////
      //B type instruction:
      //|  1111  | rX/vezérlés | opkód  | rY/vezérlés |
      /////////////////////////////////////////////////////////////////////////////////
      when(D === OpCodes.REG_OP_PREFIX.U)
      {
        switch (B)
        {
          is (OPCODE_LD.U)
          {
            // | 1111 |   rX   |  1101  |    rY   |
          }

          is (OPCODE_ST.U)
          {
            // | 1111 |   rX   |  1001  |    rY   |
          }

          is (OPCODE_MOV.U)
          {
            // | 1111 |   rX   |  1100  |    rY   |
          }

          is (OPCODE_ADD.U)
          {
            // | 1111 |   rX   |  00SC  |    rY   |
          }
          is (OPCODE_ADC.U)
          {
            // | 1111 |   rX   |  00SC  |    rY   |
          }
          is (OPCODE_SUB.U)
          {
            // | 1111 |   rX   |  00SC  |    rY   |
          }
          is (OPCODE_SBC.U)
          {
            // | 1111 |   rX   |  00SC  |    rY   |
          }

          is (OPCODE_CMP.U)
          {
            // | 1111 |   rX   |  1010  |    rY   |
          }

          is (OPCODE_AND.U)
          {
            // | 1111 |   rX   |  01AB  |    rY   |
          }
          is (OPCODE_OR.U)
          {
            // | 1111 |   rX   |  01AB  |    rY   |
          }
          is (OPCODE_XOR.U)
          {
            // | 1111 |   rX   |  01AB  |    rY   |
          }

          is (OPCODE_TST.U)
          {
            // | 1111 |   rX   |  1000  |    rY   |
          }

          is (OPCODE_SHIFT.U)
          {
            // | 1111 |   rX   |  0111  |  AIRD   |
          }

          is (OPCODE_CTRL.U)
          {
            // | 1111 | mûvelet|  1011  |    rY   |
          }
        }
      }
      /////////////////////////////////////////////////////////////////////////////////
      //A type instruction:
      //| opkód  | rX/vezérlés |   8 bites konstans   |
      /////////////////////////////////////////////////////////////////////////////////
      .otherwise
      {
        switch(D)
        {
          is (OPCODE_LD.U)
          {
            // | 1101 |   rX   | adatmemória cím  |
            //TODO: így kell???????????
            io.ctrl.regs_a := C
            io.ctrl.mem_addr := Cat(B, A)
            io.pc_inc := 1.U
          }

          is (OPCODE_ST.U)
          {
            // | 1001 |   rX   | adatmemória cím  |
          }

          is (OPCODE_MOV.U)
          {
            // | 1100 |   rX   | 8 bites konstans |
          }

          is (OPCODE_ADD.U)
          {
            // | 00SC |   rX   | 8 bites konstans |
          }
          is (OPCODE_ADC.U)
          {
            // | 00SC |   rX   | 8 bites konstans |
          }
          is (OPCODE_SUB.U)
          {
            // | 00SC |   rX   | 8 bites konstans |
          }
          is (OPCODE_SBC.U)
          {
            // | 00SC |   rX   | 8 bites konstans |
          }

          is (OPCODE_CMP.U)
          {
            // | 1010 |   rX   | 8 bites konstans |
          }

          is (OPCODE_AND.U)
          {
            // | 01AB |   rX   | 8 bites konstans |
          }
          is (OPCODE_OR.U)
          {
            // | 01AB |   rX   | 8 bites konstans |
          }
          is (OPCODE_XOR.U)
          {
            // | 01AB |   rX   | 8 bites konstans |
          }

          is (OPCODE_TST.U)
          {
            // | 1000 |   rX   | 8 bites konstans |
          }

          is (OPCODE_CTRL.U)
          {
            // | 1011 | mûvelet|     00000000     |
          }
        }
      }

      //TODO: Itt mit csináljon az DATASTRUCTURE??
    }
    is (sEXECUTE)
    {

    }
  }
}
